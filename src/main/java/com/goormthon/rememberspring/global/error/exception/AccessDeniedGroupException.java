package com.goormthon.rememberspring.global.error.exception;

/*
엑세스 거부 그룹
ex) 본인 소유가 아님
 */

public abstract class AccessDeniedGroupException extends RuntimeException {
    public AccessDeniedGroupException(String message) {
        super(message);
    }
}
