package com.example.oauth2client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
public class Oauth2ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(Oauth2ClientApplication.class, args);
    }
}


@Configuration
@EnableWebSecurity
class Oauth2Configuration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                // Говорим что можно юзать только пользователям с CREATE_MESSAGE authority
                .mvcMatchers("/create-message").hasAuthority("CREATE_MESSAGE")
                // Говорим что можно юзать только пользователям с DELETE_MESSAGE authority
                .mvcMatchers("/delete-message").hasAuthority("DELETE_MESSAGE")
                // Говорим что можно юзать только пользователям с MANAGE_MESSAGE authority
                .mvcMatchers("/manage-message").hasAuthority("MANAGE_MESSAGE")
                .anyRequest().authenticated()
                .and()
                // Говорим что нужно юзать оаус для аутентификации, это связано с тем что когда мы включаем
                // @EnableWebSecurity наш автоконфигуратор отключается и мы должны включить его явно
                .oauth2Login()
                // Настраиваем ЮзерИнфо ендпоинт, и передаем мапер для того чтобы замапить поле с idToken на привилегии
                // Это необходимо если у нас сложный подход к привелегиям
                .userInfoEndpoint().userAuthoritiesMapper(getGrantedAuthoritiesMapper());

    }

    /**
     * Собственно говоря, сам мапер
     * Его задача достать с токена список authorities и вернуть сет Grant`ов
     *
     * Обратите внимание!!! чтобы это работало корректно нужно в auth0 добавить дополнительно поле в idToken
     * Решается это путем добавления RULE в Auth0
     * Содержание рула представлено в readme файле в этой ветке
     *
     *
     *
     * @return
     */
    private GrantedAuthoritiesMapper getGrantedAuthoritiesMapper() {
        // нейм спейс используется для аус0 т.к. auth0 не позволяет добавлять аттрибуты в idToken если просто указно имя атрибута
        // должен быть полноценный url, в нашем случае мы указали http://localhost/
        String namespace = "http://localhost/";
        // Собственно само имя аттрибута
        String attributeName = namespace + "authorities";

        // создатель токена, вы измените на свой домен
        // он используется ниже, но можно не использовать т.к. мы наверняка знаем кто issuer в нашем случае он один
        String issuer = "https://activx.auth0.com/";

        // Собственно сам мапер
        return authorities ->
                // берем стрим наших authorities
                authorities.stream()
                        // фильтруем по типу OIDC
                        .filter(OidcUserAuthority.class::isInstance)
                        // кастуем к этому типу
                        .map(OidcUserAuthority.class::cast)
                        // убеждаемся что выдано нашим серваком, в нашем случае не обязательно

//                        .filter(userAuthority -> userAuthority.getIdToken().getIssuer().toString().equals(issuer))
                        // проверяем наличие поля attributeName
//                        .filter(userAuthority -> userAuthority.getIdToken().containsClaim(attributeName))
                        // достаем список наших привелегий
                        .map(userAuthority -> userAuthority.getIdToken().getClaimAsStringList(attributeName))
                        // и делаем из этого стрим
                        .flatMap(Collection::stream)
                        // здесь мы создаем SimpleGrantedAuthority из каждой привелегии
                        .map(SimpleGrantedAuthority::new)
                        // и складываем в сет
                        .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}

/**
 *  Это ендпоинты которые описаны выше в секюрити конфиге можете проверить
 *  вызвать /manage-message и получить 403 тогда как все остальные ендпоинты будут работать
  */

@RestController
class IndexController {

    @GetMapping("/")
    public String index(@AuthenticationPrincipal OAuth2User user) {
        return "<h1>Hello, " + user.getAttributes().get("nickname") + "</h1>";
    }

    @GetMapping("user")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User user) {
        return user.getAttributes();
    }

    @GetMapping("/delete-message")
    public String delete() {
        return "<h1>DELETED</h1>";
    }

    @GetMapping("/create-message")
    public String create() {
        return "<h1>created</h1>";
    }

    @GetMapping("/manage-message")
    public String manage() {
        return "<h1>managed</h1>";
    }


}
