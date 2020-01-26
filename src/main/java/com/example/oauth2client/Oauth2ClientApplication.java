package com.example.oauth2client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@SpringBootApplication
public class Oauth2ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(Oauth2ClientApplication.class, args);
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(){
        return new InMemoryClientRegistrationRepository(auth0());
    }

    private ClientRegistration auth0(){
        return ClientRegistration.withRegistrationId("auth0")
                .clientName("OAuth2 client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientId("mTRDwuz9sT3GkkA8SHf2pt4eNHXqnpYB")
                .clientSecret("Oc0TFAqcGLVdBJBebCqEqIzy9Rnvc1PsSgTPrDuh4-cDVhWhKjkcL07ZAQze62bk")
                .scope("openid", "profile", "email")
                .redirectUriTemplate("{baseUrl}/login/oauth2/code/{registrationId}")

                .authorizationUri("https://activx.auth0.com/authorize")
                .tokenUri("https://activx.auth0.com/oauth/token")
                .userInfoUri("https://activx.auth0.com/userinfo")
                .userNameAttributeName("sub")
                .jwkSetUri("https://activx.auth0.com/.well-known/jwks.json")
                .build();
    }



}


@RestController
class IndexController{

    @GetMapping("/")
    public String index(@AuthenticationPrincipal OAuth2User user){
        return "<h1>Hello, " + user.getAttributes().get("nickname") + "</h1>";
    }

    @GetMapping("user")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User user){
        return user.getAttributes();
    }

}
