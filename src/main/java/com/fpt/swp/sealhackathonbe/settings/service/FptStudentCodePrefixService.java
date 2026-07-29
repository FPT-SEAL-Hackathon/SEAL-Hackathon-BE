package com.fpt.swp.sealhackathonbe.settings.service;

import com.fpt.swp.sealhackathonbe.settings.dto.FptStudentCodePrefixRequest;
import com.fpt.swp.sealhackathonbe.settings.dto.FptStudentCodePrefixResponse;
import com.fpt.swp.sealhackathonbe.user.entity.User;

import java.util.List;

public interface FptStudentCodePrefixService {
    List<FptStudentCodePrefixResponse> list(boolean includeInactive);
    FptStudentCodePrefixResponse upsert(FptStudentCodePrefixRequest request);
    FptStudentCodePrefixResponse setActive(String prefix, boolean active);
    boolean isValidActiveFptStudentCode(String value);
    String normalizeFptStudentCode(String value);
    List<String> profileIssues(User user);
}
