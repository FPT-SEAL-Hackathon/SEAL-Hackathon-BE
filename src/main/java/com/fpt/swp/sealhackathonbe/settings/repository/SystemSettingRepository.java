package com.fpt.swp.sealhackathonbe.settings.repository;

import com.fpt.swp.sealhackathonbe.settings.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {
}
