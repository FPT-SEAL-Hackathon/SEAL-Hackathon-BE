package com.fpt.swp.sealhackathonbe.user.repository;


import com.fpt.swp.sealhackathonbe.user.entity.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountStatusRepository extends JpaRepository<AccountStatus, UUID> {

    Optional<AccountStatus> findByStatusName(String statusName);

}