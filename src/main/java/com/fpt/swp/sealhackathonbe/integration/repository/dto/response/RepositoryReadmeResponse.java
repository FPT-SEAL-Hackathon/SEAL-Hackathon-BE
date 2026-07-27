package com.fpt.swp.sealhackathonbe.integration.repository.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * README raw markdown cua repo, lay lazy khi nguoi dung mo. content = null neu khong co README.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryReadmeResponse {
    private String content;
}
