package com.example.oauth2client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class IndexController{

    private final RestTemplate restTemplate;


    @GetMapping("/")
    public String index(@AuthenticationPrincipal OAuth2User user){
        return "<h1>Hello, " + user.getAttributes().get("nickname") + "</h1>";
    }

    @GetMapping("user")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User user){
        return user.getAttributes();
    }

    @GetMapping("external")
    public ExternalUserInfo getExternal(@RegisteredOAuth2AuthorizedClient("github") OAuth2AuthorizedClient client){

        String uri = client.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUri();

        ResponseEntity<ExternalUserInfo> response = restTemplate.exchange(uri, HttpMethod.GET, null, ExternalUserInfo.class);


        return response.getBody();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static private class ExternalUserInfo {
        private String nickname;
        private String email;
        private boolean emailVerified;
        private String name;
        private LocalDateTime exp;
    }

}
