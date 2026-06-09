package com.fpt.swp.sealhackathonbe.award.service.impl;

import com.fpt.swp.sealhackathonbe.award.dto.AwardRequest;
import com.fpt.swp.sealhackathonbe.award.dto.AwardResponse;
import com.fpt.swp.sealhackathonbe.award.dto.HallOfFameResponse;
import com.fpt.swp.sealhackathonbe.award.entity.Award;
import com.fpt.swp.sealhackathonbe.award.entity.AwardTier;
import com.fpt.swp.sealhackathonbe.award.repository.AwardRepository;
import com.fpt.swp.sealhackathonbe.award.repository.AwardTierRepository;
import com.fpt.swp.sealhackathonbe.award.service.AwardService;
import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryRepository;
import com.fpt.swp.sealhackathonbe.event.entity.Category;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.repository.CategoryRepository;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AwardServiceImpl implements AwardService {

    private final AwardRepository awardRepository;
    private final AwardTierRepository awardTierRepository;

    // Các Repository inject từ package của các thành viên khác
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final TeamsRepository teamsRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AwardResponse grantAward(AwardRequest request, UUID adminId) {
        // 1. Validation: Kiểm tra xem các thực thể liên quan có tồn tại thực tế không
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Sự kiện với ID: " + request.getEventId()));

        Teams team = teamsRepository.findById(request.getTeamId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Đội thi với ID: " + request.getTeamId()));

        AwardTier tier = awardTierRepository.findById(request.getAwardTierId())
                .orElseThrow(() -> new RuntimeException("Hạng giải thưởng (Tier) không hợp lệ."));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Tài khoản người thực hiện không tồn tại."));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Hạng mục thi đấu."));
        }

        // 2. Mapping dữ liệu từ DTO sang Entity để lưu trữ
        Award award = new Award();
        award.setEvent(event);
        award.setCategory(category);
        award.setTeam(team);
        award.setAwardTier(tier);
        award.setAwardTitle(request.getAwardTitle());
        award.setDescription(request.getDescription());
        award.setPrizeValue(request.getPrizeValue());

        // Xử lý giá trị mặc định cho Tiền tệ nếu Frontend gửi rỗng
        award.setPrizeCurrency(request.getPrizeCurrency() != null ? request.getPrizeCurrency() : "VND");

        // Tự động gán thời gian hiện tại chuẩn UTC và ID người thực hiện
        award.setAwardedAt(Instant.now());
        award.setAwardedBy(admin);

        // Mặc định giải thưởng mới tạo ở trạng thái chưa công bố (Draft)
        award.setIsPublished(false);

        // 3. Lưu vào Database
        Award savedAward = awardRepository.save(award);

        // 4. Trả kết quả về dưới dạng Response DTO
        return convertToResponse(savedAward);
    }

    @Override
    @Transactional(readOnly = true)
    public AwardResponse getAwardById(UUID awardId) {
        Award award = awardRepository.findById(awardId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu giải thưởng."));
        return convertToResponse(award);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AwardResponse> getAwardsByEvent(UUID eventId) {
        // Bạn có thể viết thêm hàm findByEventId ở AwardRepository để gọi chỗ này
        // Tạm thời trả về danh sách convert mẫu
        return awardRepository.findAll().stream()
                .filter(a -> a.getEvent().getId().equals(eventId))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Hàm Helper đóng vai trò chuyển đổi nhanh dữ liệu từ Entity sang Response DTO
    private AwardResponse convertToResponse(Award award) {
        AwardResponse response = new AwardResponse();
        response.setId(award.getId());
        response.setEventId(award.getEvent().getId());
        response.setEventName(award.getEvent().getEventName());
        response.setTeamId(award.getTeam().getId());
        response.setTeamName(award.getTeam().getTeamName());
        response.setAwardTierId(award.getAwardTier().getId());
        response.setAwardTierName(award.getAwardTier().getTierName());
        response.setAwardTitle(award.getAwardTitle());
        response.setDescription(award.getDescription());
        response.setPrizeValue(award.getPrizeValue());
        response.setPrizeCurrency(award.getPrizeCurrency());
        response.setAwardedAt(award.getAwardedAt());
        response.setAwardedByName(award.getAwardedBy().getFullName());
        response.setIsPublished(award.getIsPublished());
        response.setPublishedAt(award.getPublishedAt());

        if (award.getCategory() != null) {
            response.setCategoryId(award.getCategory().getId());
            response.setCategoryName(award.getCategory().getCategoryName());
        }
        return response;


    }
    @Override
    @Transactional(readOnly = true)
    public List<HallOfFameResponse> getHallOfFameData() {
        // Lấy danh sách Entity
        List<Award> publishedAwards = awardRepository.findPublishedAwardsForHallOfFame();

        // Map sang DTO
        return publishedAwards.stream().map(award -> {
            HallOfFameResponse response = new HallOfFameResponse();
            response.setEventName(award.getEvent().getEventName());

            // Xử lý an toàn nếu giải thưởng không thuộc một hạng mục (category) cụ thể
            if (award.getCategory() != null) {
                response.setCategoryName(award.getCategory().getCategoryName());
            } else {
                response.setCategoryName("Giải Toàn Sự Kiện");
            }

            response.setTeamName(award.getTeam().getTeamName());
            response.setAwardTierName(award.getAwardTier().getTierName());
            response.setAwardTitle(award.getAwardTitle());

            // Lấy tên đội trưởng (Leader) từ quan hệ Team -> User
            response.setLeaderName(award.getTeam().getLeaderUserID().toString()); // Đoạn này khi ráp Entity Team của TV3, bạn gọi .getFullName() là đẹp nhất

            return response;
        }).collect(Collectors.toList());
    }
}