package com.fpt.swp.sealhackathonbe.integration.repository.client;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.fpt.swp.sealhackathonbe.integration.repository.dto.RepositoryActivity;
import com.fpt.swp.sealhackathonbe.integration.repository.dto.RepositoryMetadata;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositoryProvider;
import com.fpt.swp.sealhackathonbe.integration.repository.exception.RepositoryMetadataErrorCode;
import com.fpt.swp.sealhackathonbe.integration.repository.exception.RepositoryMetadataException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
@Slf4j
public class GitHubRepositoryMetadataClient implements GitRepositoryMetadataClient {

    private static final String GITHUB_API_BASE = "https://api.github.com";
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 10000;

    private final RestClient restClient;

    @org.springframework.beans.factory.annotation.Autowired
    public GitHubRepositoryMetadataClient(
            @Value("${app.github.metadata-token:}") String metadataToken) {
        this(RestClient.builder(), metadataToken);
    }

    public GitHubRepositoryMetadataClient(
            RestClient.Builder restClientBuilder,
            String metadataToken) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        requestFactory.setReadTimeout(READ_TIMEOUT_MS);

        RestClient.Builder builder = restClientBuilder
                .baseUrl(GITHUB_API_BASE)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader(HttpHeaders.USER_AGENT, "SEAL-Hackathon");

        if (metadataToken != null && !metadataToken.trim().isEmpty()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + metadataToken.trim());
            log.info("GitHubRepositoryMetadataClient initialized with server-side metadata token.");
        }

        this.restClient = builder.build();
    }

    GitHubRepositoryMetadataClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public RepositoryProvider getProvider() {
        return RepositoryProvider.GITHUB;
    }

    @Override
    public boolean supports(String repositoryUrl) {
        try {
            GitHubRepositoryCoordinates coords = GitHubRepositoryUrlParser.parse(repositoryUrl);
            return coords != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public RepositoryMetadata fetchPublicMetadata(String repositoryUrl) {
        GitHubRepositoryCoordinates coords = GitHubRepositoryUrlParser.parse(repositoryUrl);
        String encodedOwner = URLEncoder.encode(coords.getOwner(), StandardCharsets.UTF_8);
        String encodedRepo = URLEncoder.encode(coords.getRepository(), StandardCharsets.UTF_8);
        String apiPath = String.format("/repos/%s/%s", encodedOwner, encodedRepo);

        try {
            ResponseEntity<String> responseEntity = restClient.get()
                    .uri(apiPath)
                    .retrieve()
                    .toEntity(String.class);

            String body = responseEntity.getBody();
            HttpHeaders headers = responseEntity.getHeaders();

            if (body == null || body.trim().isEmpty()) {
                throw new RepositoryMetadataException(
                        RepositoryMetadataErrorCode.GITHUB_INVALID_RESPONSE,
                        "GitHub response body is empty",
                        null,
                        responseEntity.getStatusCode().value(),
                        extractHeader(headers, "X-GitHub-Request-Id"),
                        extractHeader(headers, "X-RateLimit-Remaining"),
                        coords.getOwner() + "/" + coords.getRepository()
                );
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(body);

            validateRequiredFields(root, coords);

            boolean isPrivate = root.has("private") && root.get("private").asBoolean();
            String visibility = root.hasNonNull("visibility")
                    ? root.get("visibility").asText().toUpperCase()
                    : (isPrivate ? "PRIVATE" : "PUBLIC");

            if (isPrivate) {
                throw new RepositoryMetadataException(
                        RepositoryMetadataErrorCode.PRIVATE_REPOSITORY_NOT_SUPPORTED,
                        "Private repositories are not supported for public metadata retrieval",
                        null,
                        200,
                        extractHeader(headers, "X-GitHub-Request-Id"),
                        extractHeader(headers, "X-RateLimit-Remaining"),
                        coords.getOwner() + "/" + coords.getRepository()
                );
            }

            String externalId = root.get("id").asText();
            String fullName = root.get("full_name").asText();
            String name = root.get("name").asText();
            String ownerLogin = root.get("owner").get("login").asText();
            String htmlUrl = root.get("html_url").asText();
            String defaultBranch = root.get("default_branch").asText();

            // Verify identity match
            String expectedFullName = coords.getOwner() + "/" + coords.getRepository();
            if (!fullName.equalsIgnoreCase(expectedFullName) && !name.equalsIgnoreCase(coords.getRepository())) {
                throw new RepositoryMetadataException(
                        RepositoryMetadataErrorCode.GITHUB_INVALID_RESPONSE,
                        "Returned repository identity (" + fullName + ") does not match requested (" + expectedFullName + ")",
                        null,
                        200,
                        extractHeader(headers, "X-GitHub-Request-Id"),
                        extractHeader(headers, "X-RateLimit-Remaining"),
                        fullName
                );
            }

            String description = root.hasNonNull("description") ? root.get("description").asText() : null;
            String primaryLanguage = root.hasNonNull("language") ? root.get("language").asText() : null;

            LocalDateTime createdAt = root.hasNonNull("created_at") ? parseUtcIsoDate(root.get("created_at").asText()) : null;
            LocalDateTime updatedAt = root.hasNonNull("updated_at") ? parseUtcIsoDate(root.get("updated_at").asText()) : null;
            LocalDateTime pushedAt = root.hasNonNull("pushed_at") ? parseUtcIsoDate(root.get("pushed_at").asText()) : null;

            Integer starCount = root.hasNonNull("stargazers_count") ? root.get("stargazers_count").asInt() : 0;
            Integer forkCount = root.hasNonNull("forks_count") ? root.get("forks_count").asInt() : 0;
            Integer openIssuesCount = root.hasNonNull("open_issues_count") ? root.get("open_issues_count").asInt() : 0;

            return RepositoryMetadata.builder()
                    .provider(RepositoryProvider.GITHUB)
                    .externalRepositoryId(externalId)
                    .repositoryUrl(coords.getNormalizedUrl())
                    .externalUrl(htmlUrl)
                    .owner(ownerLogin)
                    .repositoryName(name)
                    .fullName(fullName)
                    .description(description)
                    .visibility(visibility)
                    .defaultBranch(defaultBranch)
                    .primaryLanguage(primaryLanguage)
                    .repositoryCreatedAt(createdAt)
                    .repositoryUpdatedAt(updatedAt)
                    .lastPushedAt(pushedAt)
                    .starCount(starCount)
                    .forkCount(forkCount)
                    .openIssuesCount(openIssuesCount)
                    .build();

        } catch (RepositoryMetadataException rme) {
            throw rme;
        } catch (ResourceAccessException rae) {
            throw new RepositoryMetadataException(
                    RepositoryMetadataErrorCode.GITHUB_TIMEOUT,
                    "GitHub API request timed out: " + rae.getMessage(),
                    rae,
                    null,
                    null,
                    null,
                    coords.getOwner() + "/" + coords.getRepository()
            );
        } catch (org.springframework.web.client.HttpStatusCodeException hsce) {
            throw handleHttpStatusError(hsce.getStatusCode(), hsce.getResponseHeaders(), coords);
        } catch (Exception e) {
            throw new RepositoryMetadataException(
                    RepositoryMetadataErrorCode.GITHUB_UPSTREAM_ERROR,
                    "Failed to fetch metadata from GitHub: " + e.getMessage(),
                    e,
                    null,
                    null,
                    null,
                    coords.getOwner() + "/" + coords.getRepository()
            );
        }
    }

    private static final ObjectMapper ACTIVITY_MAPPER = new ObjectMapper();
    // Link header cua GitHub: <...&page=13>; rel="last"  → so trang cuoi = tong so item khi per_page=1.
    private static final java.util.regex.Pattern LINK_LAST_PAGE =
            java.util.regex.Pattern.compile("[?&]page=(\\d+)>\\s*;\\s*rel=\"last\"");

    /**
     * Best-effort: goi cac API phu (languages / contributors / commits). Moi call tu bao ve
     * bang try/catch — loi (rate-limit, mang...) chi khien field tuong ung null, KHONG nem
     * exception, KHONG chan luong submit. Toi da 3 request phu.
     */
    @Override
    public RepositoryActivity fetchActivity(String repositoryUrl) {
        RepositoryActivity.RepositoryActivityBuilder builder = RepositoryActivity.builder();
        GitHubRepositoryCoordinates coords;
        try {
            coords = GitHubRepositoryUrlParser.parse(repositoryUrl);
        } catch (Exception e) {
            return builder.build();
        }
        if (coords == null) {
            return builder.build();
        }
        String owner = URLEncoder.encode(coords.getOwner(), StandardCharsets.UTF_8);
        String repo = URLEncoder.encode(coords.getRepository(), StandardCharsets.UTF_8);
        String base = String.format("/repos/%s/%s", owner, repo);
        String fullName = coords.getOwner() + "/" + coords.getRepository();

        // 1) Languages: {"Java": 12345, "TypeScript": 6789} (bytes)
        try {
            String body = restClient.get().uri(base + "/languages").retrieve().toEntity(String.class).getBody();
            if (body != null && !body.isBlank()) {
                JsonNode node = ACTIVITY_MAPPER.readTree(body);
                if (node.isObject() && node.size() > 0) {
                    builder.languagesJson(node.toString());
                }
            }
        } catch (Exception e) {
            log.debug("fetchActivity[languages] failed for {}: {}", fullName, e.getMessage());
        }

        // 2) Contributors (per_page=100 du cho doi hackathon; contributorCount = so phan tu, top 10)
        try {
            String body = restClient.get().uri(base + "/contributors?per_page=100&anon=0")
                    .retrieve().toEntity(String.class).getBody();
            if (body != null && !body.isBlank()) {
                JsonNode arr = ACTIVITY_MAPPER.readTree(body);
                if (arr.isArray()) {
                    builder.contributorCount(arr.size());
                    ArrayNode top = ACTIVITY_MAPPER.createArrayNode();
                    for (int i = 0; i < arr.size() && i < 10; i++) {
                        JsonNode c = arr.get(i);
                        ObjectNode o = ACTIVITY_MAPPER.createObjectNode();
                        o.put("login", c.path("login").asText(null));
                        o.put("contributions", c.path("contributions").asInt(0));
                        o.put("avatarUrl", c.path("avatar_url").asText(null));
                        top.add(o);
                    }
                    builder.topContributorsJson(top.toString());
                }
            }
        } catch (Exception e) {
            log.debug("fetchActivity[contributors] failed for {}: {}", fullName, e.getMessage());
        }

        // 3) Commits: SHA moi nhat + tong so commit (uoc luong qua Link header khi per_page=1)
        try {
            ResponseEntity<String> res = restClient.get().uri(base + "/commits?per_page=1")
                    .retrieve().toEntity(String.class);
            String body = res.getBody();
            if (body != null && !body.isBlank()) {
                JsonNode arr = ACTIVITY_MAPPER.readTree(body);
                if (arr.isArray() && arr.size() > 0) {
                    String sha = arr.get(0).path("sha").asText(null);
                    if (sha != null && !sha.isBlank()) {
                        builder.lastCommitSha(sha);
                    }
                    Integer lastPage = lastPageFromLink(res.getHeaders());
                    builder.commitCount(lastPage != null ? lastPage : arr.size());
                }
            }
        } catch (Exception e) {
            log.debug("fetchActivity[commits] failed for {}: {}", fullName, e.getMessage());
        }

        return builder.build();
    }

    private static final int README_MAX_CHARS = 100_000;

    /**
     * README raw markdown (Accept: application/vnd.github.raw). Best-effort: 404/loi → null.
     * Cat bot neu qua dai de tranh payload khong lo.
     */
    @Override
    public String fetchReadme(String repositoryUrl) {
        GitHubRepositoryCoordinates coords;
        try {
            coords = GitHubRepositoryUrlParser.parse(repositoryUrl);
        } catch (Exception e) {
            return null;
        }
        if (coords == null) {
            return null;
        }
        String path = String.format("/repos/%s/%s/readme",
                URLEncoder.encode(coords.getOwner(), StandardCharsets.UTF_8),
                URLEncoder.encode(coords.getRepository(), StandardCharsets.UTF_8));
        try {
            String body = restClient.get()
                    .uri(path)
                    .header(HttpHeaders.ACCEPT, "application/vnd.github.raw")
                    .retrieve()
                    .body(String.class);
            if (body == null) {
                return null;
            }
            return body.length() > README_MAX_CHARS ? body.substring(0, README_MAX_CHARS) : body;
        } catch (Exception e) {
            log.debug("fetchReadme failed for {}/{}: {}", coords.getOwner(), coords.getRepository(), e.getMessage());
            return null;
        }
    }

    private Integer lastPageFromLink(HttpHeaders headers) {
        String link = extractHeader(headers, "Link");
        if (link == null) {
            return null;
        }
        java.util.regex.Matcher m = LINK_LAST_PAGE.matcher(link);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private RepositoryMetadataException handleHttpStatusError(
            HttpStatusCode statusCode, HttpHeaders headers, GitHubRepositoryCoordinates coords) {
        int status = statusCode.value();
        String reqId = extractHeader(headers, "X-GitHub-Request-Id");
        String rateRemaining = extractHeader(headers, "X-RateLimit-Remaining");
        String retryAfter = extractHeader(headers, "Retry-After");
        String fullName = coords.getOwner() + "/" + coords.getRepository();

        if (status == 404) {
            return new RepositoryMetadataException(
                    RepositoryMetadataErrorCode.GITHUB_REPOSITORY_NOT_FOUND,
                    "GitHub repository not found: " + fullName,
                    null, status, reqId, rateRemaining, fullName
            );
        } else if (status == 429) {
            return new RepositoryMetadataException(
                    RepositoryMetadataErrorCode.GITHUB_RATE_LIMITED,
                    "GitHub rate limit exceeded (HTTP 429)",
                    null, status, reqId, rateRemaining, fullName
            );
        } else if (status == 401 || status == 403) {
            boolean isRateLimited = "0".equals(rateRemaining) || retryAfter != null;
            if (isRateLimited) {
                return new RepositoryMetadataException(
                        RepositoryMetadataErrorCode.GITHUB_RATE_LIMITED,
                        "GitHub rate limit exceeded",
                        null, status, reqId, rateRemaining, fullName
                );
            }
            return new RepositoryMetadataException(
                    RepositoryMetadataErrorCode.GITHUB_REPOSITORY_INACCESSIBLE,
                    "GitHub repository inaccessible or unauthorized",
                    null, status, reqId, rateRemaining, fullName
            );
        } else if (status >= 500) {
            return new RepositoryMetadataException(
                    RepositoryMetadataErrorCode.GITHUB_UPSTREAM_ERROR,
                    "GitHub server error: " + status,
                    null, status, reqId, rateRemaining, fullName
            );
        } else {
            return new RepositoryMetadataException(
                    RepositoryMetadataErrorCode.GITHUB_UPSTREAM_ERROR,
                    "GitHub HTTP error: " + status,
                    null, status, reqId, rateRemaining, fullName
            );
        }
    }

    private void validateRequiredFields(JsonNode root, GitHubRepositoryCoordinates coords) {
        String[] required = {"id", "name", "full_name", "html_url", "default_branch", "created_at", "updated_at"};
        for (String field : required) {
            if (!root.hasNonNull(field)) {
                throw new RepositoryMetadataException(
                        RepositoryMetadataErrorCode.GITHUB_INVALID_RESPONSE,
                        "Required response field missing: " + field,
                        null, 200, null, null, coords.getOwner() + "/" + coords.getRepository()
                );
            }
        }
        if (!root.hasNonNull("owner") || !root.get("owner").hasNonNull("login")) {
            throw new RepositoryMetadataException(
                    RepositoryMetadataErrorCode.GITHUB_INVALID_RESPONSE,
                    "Required response field missing: owner.login",
                    null, 200, null, null, coords.getOwner() + "/" + coords.getRepository()
            );
        }
    }

    private LocalDateTime parseUtcIsoDate(String isoString) {
        if (isoString == null || isoString.trim().isEmpty()) return null;
        try {
            return OffsetDateTime.parse(isoString.trim())
                    .withOffsetSameInstant(ZoneOffset.UTC)
                    .toLocalDateTime();
        } catch (Exception e) {
            throw new RepositoryMetadataException(
                    RepositoryMetadataErrorCode.GITHUB_INVALID_RESPONSE,
                    "Invalid ISO-8601 date timestamp format: " + isoString,
                    e
            );
        }
    }

    private String extractHeader(HttpHeaders headers, String headerName) {
        if (headers == null) return null;
        return headers.getFirst(headerName);
    }
}
