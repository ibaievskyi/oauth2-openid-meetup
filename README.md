# oauth2-openid-meetup
Материалы с лекции о Auth2 и OpenID Connect

## Какие случаи будут рассмотрены
- простая конфигурация github ветка github
- простая конфигурация facebook ветка facebook
- несколько провайдеров ветка several-standart-providers
- конфигурация нового провайдера ветка new-provider-auth0
- java-конфигурация ветка Java-configuration
- мапинг Authorities ветка authorities-mapping
- использование Access Token ветка using-access-token-auth0



## AUTH0 RULE
Зайдите в dashboard Auth0 раздел Rules и создайте новое правило с этим содержанием:
```
    function (user, context, callback) {
      const namespace = 'http://localhost/';
      context.idToken[namespace + 'authorities'] = ["DELETE_MESSAGE", "CREATE_MESSAGE"];  
      callback(null, user, context);
    }
```

## Dependencies
```
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-oauth2-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-oauth2-jose</artifactId>
        </dependency>
```


## Обратите внимание все client-id и client-secret нужно использовать свои!