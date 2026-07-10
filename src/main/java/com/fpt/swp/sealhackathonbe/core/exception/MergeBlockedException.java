package com.fpt.swp.sealhackathonbe.core.exception;

/**
 * Không thể tự động gộp tài khoản vì tài khoản tạm đã có dữ liệu nghiệp vụ.
 * Trả 409 MERGE_BLOCKED; cần hỗ trợ/xử lý thủ công.
 */
public class MergeBlockedException extends RuntimeException {

    public MergeBlockedException(String message) {
        super(message);
    }
}
