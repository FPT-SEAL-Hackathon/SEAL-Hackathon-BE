package com.fpt.swp.sealhackathonbe.core.exception;

/**
 * Dang nhap khi tai khoan chua xac minh email.
 *
 * Truoc day cho nem IllegalStateException, roi handler chung map thanh 400 BAD_REQUEST —
 * frontend khong co cach nao phan biet voi cac loi 400 khac de dieu huong nguoi dung
 * sang buoc gui lai email xac minh. Exception rieng cho ra error code EMAIL_NOT_VERIFIED.
 */
public class EmailNotVerifiedException extends RuntimeException {

    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
