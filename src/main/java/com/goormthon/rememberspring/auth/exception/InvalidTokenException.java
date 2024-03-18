package com.goormthon.rememberspring.auth.exception;


import com.goormthon.rememberspring.global.error.exception.InvalidGroupException;

public class InvalidTokenException extends InvalidGroupException {
    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException() {
        this("토큰이 유효하지 않습니다.");
    }
}
