package com.goormthon.rememberspring.diary.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

/*
    RestTemplate은 스프링 프레임워크가 제공하는 클래스로, RESTful 서비스를 소비하기 위한 동기식 HTTP 클라이언트이다.
    RestTemplate을 사용하면 HTTP 요청을 보내고, 응답을 받아, 자바 객체로 변환하는 과정을 간편하게 처리할 수 있다.
    RestTemplate은 다양한 HTTP 메소드(GET, POST, DELETE 등)를 지원하며, HttpEntity를 통해 요청 본문과 헤더를 설정할 수 있고,
    ResponseEntity를 통해 응답 본문과 상태 코드 등을 받을 수 있다.
 */

@Configuration
public class ChatGptConfig {

    @Value("${spring.openai.api.key}")
    private String openAiKey;

    @Bean
    public RestTemplate template(){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("Authorization", "Bearer " + openAiKey);
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}
