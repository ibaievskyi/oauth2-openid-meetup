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
                .mvcMatchers("/create-message").hasAuthority("CREATE_MESSAGE")
                .mvcMatchers("/delete-message").hasAuthority("DELETE_MESSAGE")
                .mvcMatchers("/manage-message").hasAuthority("MANAGE_MESSAGE")
                .anyRequest().authenticated()
                .and()
                .oauth2Login()
                .userInfoEndpoint().userAuthoritiesMapper(getGrantedAuthoritiesMapper());

    }

    private GrantedAuthoritiesMapper getGrantedAuthoritiesMapper() {
        String namespace = "http://localhost/";
        String attributeName = namespace + "authorities";
        String issuer = "https://activx.auth0.com/";

        return authorities ->
                authorities.stream()
                        .filter(OidcUserAuthority.class::isInstance)
                        .map(OidcUserAuthority.class::cast)
//                        .filter(userAuthority -> userAuthority.getIdToken().getIssuer().toString().equals(issuer))
//                        .filter(userAuthority -> userAuthority.getIdToken().containsClaim(attributeName))
                        .map(userAuthority -> userAuthority.getIdToken().getClaimAsStringList(attributeName))
                        .flatMap(Collection::stream)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}


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
