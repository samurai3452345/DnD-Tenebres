package com.java_dragons.dnd_tenebres.infrastructure.web;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.*;
@Configuration
public class OpenApiConfig {
 @Bean OpenAPI gameApi() {
  return new OpenAPI().info(new Info().title("DnD Tenebres API").version("v1")
    .description("Серверный контракт боя, мира, инвентаря, экономики и квестов"))
    .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
      .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
    .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
 }
}
