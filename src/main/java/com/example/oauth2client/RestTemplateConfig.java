package com.example.oauth2client;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.web.client.RestTemplate;


/**
 * Здесь мы создаем бин нашего RestTemplate для выполнения запроса к ресурс серверу.
 */
@RequiredArgsConstructor
@Configuration
public class RestTemplateConfig {

    /**
     * Бин рест темплейта
     * @param oAuth2AuthorizedClientService - автовайрим сервис который знает откуда достать ассоциацию
     *                                      аутентификации и конфига OAuth
     *                                      ({@link OAuth2AuthorizedClientRepository - это хранилище которое использует под капотом сервис})
     * @return рест темплейт
     */
    @Bean
    public RestTemplate restTemplate(OAuth2AuthorizedClientService oAuth2AuthorizedClientService) {
        return new RestTemplateBuilder()
                // Добавляем интерцептор, который в свою очередь будет добавлять Access Token к каждому
                // реквесту, сделанному с помощью этого RestTemplate

                .interceptors((httpRequest, bytes, execution) -> {

                    //Достаем объект аутентификации текущий
                    OAuth2AuthenticationToken auth = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();


                    //Загружаем с ассоциацию нашей аутентификации с параметрами ClientRegistration(OAuth конфиг)
                    // Делаем это чтобы достать  Access Token так как в объекте аутентификации он не хранится
                    OAuth2AuthorizedClient authorizedClient = oAuth2AuthorizedClientService.loadAuthorizedClient(
                            auth.getAuthorizedClientRegistrationId(),
                            auth.getName());

                    // Добавляем токен
                    httpRequest.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + authorizedClient.getAccessToken().getTokenValue());

                    return execution.execute(httpRequest, bytes);
                }).build();
    }
}
