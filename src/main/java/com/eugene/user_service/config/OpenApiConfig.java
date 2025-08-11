package com.eugene.user_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig
{
    
    @Value("${keycloak.url}")
    private String keycloakBaseUrl;
    
    @Bean
    public OpenAPI accessOpenAPI() {
        
        OAuthFlow authorizationCodeFlow = new OAuthFlow()
                .authorizationUrl(keycloakBaseUrl + "/auth")
                .tokenUrl(keycloakBaseUrl + "/token")
                .scopes(new Scopes()
                                .addString("openid",
                                           "OpenID scope")
                                .addString("profile",
                                           "Profile information")
                                .addString("email",
                                           "Email information"));
        
        SecurityScheme oAuthScheme = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows().authorizationCode(authorizationCodeFlow));
        
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("keycloakAuth"))
                .components(new Components().addSecuritySchemes("keycloakAuth",
                                                                oAuthScheme))
                .info(new Info()
                              .title("User service")
                              .description("This is the user service API of MF Library.")
                              .version("1.0"));
    }
}
