package com.fpt.swp.sealhackathonbe.settings.repository;

import com.fpt.swp.sealhackathonbe.settings.entity.FptStudentCodePrefix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FptStudentCodePrefixRepository extends JpaRepository<FptStudentCodePrefix, String> {
    List<FptStudentCodePrefix> findByIsActiveTrueOrderByMajorGroupAscPrefixAsc();
    List<FptStudentCodePrefix> findAllByOrderByMajorGroupAscPrefixAsc();
    boolean existsByPrefixIgnoreCaseAndIsActiveTrue(String prefix);
}
