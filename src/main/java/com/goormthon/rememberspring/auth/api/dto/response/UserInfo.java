package com.goormthon.rememberspring.auth.api.dto.response;

public record UserInfo(
        String email,
        String nickname,
        String picture
) {
}
