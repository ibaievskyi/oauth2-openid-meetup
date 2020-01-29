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

    /**
     * Ендпоинт для получения с Resource server объекта  user info. Данные с ендпоинт userinfo можно получить
     * только имея Access token, что в свою очередь идентично получению любых данных с Resource server.
     * Сам урл для этого объекта мы получаем из конфигурации OAuth.
     *
     * @param client - в данном случае это авторизованный клиент
     *               (ассоциация client registration + аутентифицированный юзер)
     *               посмотрите из чего состоит этот объект будет полезно,
     *               так же этот объект другим способом мы получаем в конфигурации для рест темплейта
     *               {@link RestTemplateConfig}
     *
     * @return возвращает сущность описаную ниже
     */
    @GetMapping("external")
    public ExternalUserInfo getExternal(@RegisteredOAuth2AuthorizedClient("github") OAuth2AuthorizedClient client){

        String uri = client.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUri();

        ResponseEntity<ExternalUserInfo> response = restTemplate.exchange(uri, HttpMethod.GET, null, ExternalUserInfo.class);


        return response.getBody();
    }

    /**
     * Entity - необходима для того чтобы получать данные с Resource Service
     * мы знаем какие поля класс т.к. эта же информация представлена в idToken но мы будем получать
     * эти данные с user-uri с Auth0
     *
     * Используется в контроллере выше
     */
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
