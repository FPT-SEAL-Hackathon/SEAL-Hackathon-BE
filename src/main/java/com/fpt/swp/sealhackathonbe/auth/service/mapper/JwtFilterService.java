package com.fpt.swp.sealhackathonbe.auth.service.mapper;

import jakarta.servlet.http.HttpServletRequest;

public interface JwtFilterService {
    public String resolveToken(HttpServletRequest request);
}
