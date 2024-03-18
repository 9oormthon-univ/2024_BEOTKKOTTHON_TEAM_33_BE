package com.goormthon.rememberspring.global.error.exception;

/*
인증 그룹
ex) 소셜 로그인 통신 문제
 */

public abstract class AuthGroupException extends RuntimeException {
    public AuthGroupException(String message) {
        super(message);
    }
}
