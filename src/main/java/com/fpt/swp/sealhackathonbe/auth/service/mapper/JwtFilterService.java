package com.fpt.swp.sealhackathonbe.auth.service.mapper;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Định nghĩa cách lấy Bearer token từ HTTP request.
 */
public interface JwtFilterService {

    /**
     * Trả về JWT thô hoặc null nếu request không có Bearer token.
     */
    public String resolveToken(HttpServletRequest request);
}
