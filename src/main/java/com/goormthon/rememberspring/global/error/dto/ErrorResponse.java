package com.goormthon.rememberspring.global.error.dto;

public record ErrorResponse(
        int statusCode,
        String message
) {
}