package com.goormthon.rememberspring.auth.application;


import com.goormthon.rememberspring.auth.api.dto.response.UserInfo;

public interface AuthService {
    UserInfo getUserInfo(String idToken);
}
