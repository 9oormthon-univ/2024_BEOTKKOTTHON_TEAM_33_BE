package com.goormthon.rememberspring.global.oauth.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goormthon.rememberspring.auth.api.dto.response.KakaoTokenResponse;
import com.goormthon.rememberspring.auth.api.dto.response.UserInfo;
import com.goormthon.rememberspring.auth.application.AuthService;
import com.goormthon.rememberspring.global.oauth.exception.OAuthException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional(readOnly = true)
public class KaKaoAuthService implements AuthService {

    private static final String JWT_DELIMITER = "\\.";

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${oauth.kakao.id-token-url}")
    private String kakaoIdTokenUrl;

    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    public KaKaoAuthService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public UserInfo getUserInfo(String idToken) {
//        KakaoTokenResponse kaKaoIdToken = getKaKaoIdToken(code);
        String decodePayload = getDecodePayload(idToken);

        UserInfo userInfo;
        try {
            userInfo = objectMapper.readValue(decodePayload, UserInfo.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return userInfo;
    }

    private String getDecodePayload(String idToken) {
        String payload = getPayload(idToken);
        return new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
    }

    private String getPayload(String idToken) {
        return idToken.split(JWT_DELIMITER)[1];
    }

    private KakaoTokenResponse getKaKaoIdToken(String code) {
        HttpEntity<MultiValueMap<String, String>> requestEntity = createRequestEntity(code);

        try {
            ResponseEntity<KakaoTokenResponse> responseTokenEntity = restTemplate.postForEntity(
                    kakaoIdTokenUrl,
                    requestEntity,
                    KakaoTokenResponse.class);

            return responseTokenEntity.getBody();
        } catch (RestClientException e) {
            throw new OAuthException();
        }
    }

    private HttpEntity<MultiValueMap<String, String>> createRequestEntity(String code) {
        return new HttpEntity<>(createRequestParams(code), createHttpHeaders());
    }

    private MultiValueMap<String, String> createRequestParams(String code) {
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();

        requestParams.add("code", code);
        requestParams.add("client_id", kakaoClientId);
        requestParams.add("grant_type", "authorization_code");
        requestParams.add("redirect_uri", kakaoRedirectUri);

        return requestParams;
    }

    private HttpHeaders createHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        return httpHeaders;
    }

}
