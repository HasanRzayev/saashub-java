package com.saashub.common.exception;

import org.springframework.http.HttpStatus;

public class TenantAccessDeniedException extends BusinessException {
    public TenantAccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
