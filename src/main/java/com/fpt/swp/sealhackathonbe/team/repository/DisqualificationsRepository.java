package com.fpt.swp.sealhackathonbe.team.repository;

import com.fpt.swp.sealhackathonbe.team.entity.Disqualifications;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DisqualificationsRepository extends JpaRepository<Disqualifications, UUID> {
    // Dư thừa hiện tại trong package team: service chỉ cần save bản ghi loại team.
    // Nếu sau này cần lịch sử loại theo team thì mở lại method này.
     List<Disqualifications> findByTeamId(UUID teamId);
}
