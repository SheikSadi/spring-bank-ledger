package com.paypay.learn.ledger;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

  // Add Authorize button
  @Bean
  public OpenAPI customOpenAPI() {
    String securitySchemeName = "bearerAuth";
    return new OpenAPI()
      .addSecurityItem(
        new SecurityRequirement().addList(securitySchemeName)
      )
      .components(
        new Components()
          .addSecuritySchemes(
            securitySchemeName,
            new SecurityScheme()
              .name(securitySchemeName)
              .type(SecurityScheme.Type.HTTP)
              .scheme("bearer")
              .bearerFormat("JWT")
          )
      )
    ;
  }

  @Bean
  public GroupedOpenApi authApi() {
    return GroupedOpenApi.builder()
      .group("auth")
      .pathsToMatch("/auth/**")
      .build()
    ;
  }


  /* Why /accounts doesn't need a Customizer
  (Automatic Reflection)

  /accounts endpoints are standard Spring MVC Controllers
  (@RestController on AccountController). At startup, Springdoc (Swagger)
  scans the Spring ApplicationContext using Java Reflection: It reads
  Spring MVC annotations (@GetMapping, @PostMapping, @PathVariable).
  It inspects request DTO types (CreateAccountRequest) and response
  types (ResponseEntity<AccountResponse>). It automatically builds the
  OpenAPI specification for all paths under /accounts/**. */

  @Bean
  public GroupedOpenApi accountsApi(OperationCustomizer idempotencyHeaderCustomizer) {
    return GroupedOpenApi.builder()
      .group("accounts")
      .pathsToMatch("/accounts/**")
      .addOperationCustomizer(idempotencyHeaderCustomizer)
      .build()
    ;
  }

  /* Why /actuator needed .addOpenApiCustomizer
  (Framework Endpoint Abstraction)

  Actuator endpoints (/actuator/health, /actuator/metrics, etc.) are not standard
  Spring MVC @RestController classes. They are managed internally by Spring Boot 
  Actuator's own endpoint abstraction (@Endpoint, HealthEndpoint, etc.): Springdoc's
  controller scanner does not treat Actuator health contributors like normal Java methods.
  Without manual customization, Swagger UI won't render detailed descriptions,
  liveness/readiness sub-paths, or custom status response schemas for Actuator endpoints.
  Using .addOpenApiCustomizer(...) allows you to programmatically inject custom
  OpenAPI PathItem and Operation definitions directly into the generated Swagger
  document for the actuator group. */

  @Bean
  public GroupedOpenApi actuatorApi() {
    return GroupedOpenApi.builder()
      .group("actuator")
      .pathsToMatch("/actuator/**")
      .addOpenApiCustomizer(openApi -> {
        Paths paths = openApi.getPaths();
        if (paths == null) {
          paths = new Paths();
          openApi.setPaths(paths);
        }

        paths.addPathItem("/actuator/health", new PathItem()
          .get(new Operation()
            .summary("Application Health Status")
            .description("Returns the liveness and readiness health state of the application components.")
            .responses(new ApiResponses().addApiResponse("200", new ApiResponse()
              .description("OK - App is healthy")
            ))
          )
        );

        paths.addPathItem("/actuator/health/liveness", new PathItem()
          .get(new Operation()
            .summary("Liveness Probe")
            .description("Checks whether the application is running.")
            .responses(new ApiResponses().addApiResponse("200", new ApiResponse()
              .description("OK - App is alive")
            ))
          )
        );

        paths.addPathItem("/actuator/health/readiness", new PathItem()
          .get(new Operation()
            .summary("Readiness Probe")
            .description("Checks whether the application is ready to handle traffic.")
            .responses(new ApiResponses().addApiResponse("200", new ApiResponse()
              .description("OK - App is ready")
            ))
          )
        );
      })
      .build()
    ;
  }

  // Adds an optional header to PUT/POST/DELETE endpoints
  @Bean
    public OperationCustomizer idempotencyHeaderCustomizer() {
        return (operation, handlerMethod) -> {
            boolean isWriteOperation = 
                handlerMethod.hasMethodAnnotation(PostMapping.class) ||
                handlerMethod.hasMethodAnnotation(PutMapping.class) ||
                handlerMethod.hasMethodAnnotation(DeleteMapping.class);

            if (isWriteOperation) {
                operation.addParametersItem(
                    new Parameter()
                        .in("header")
                        .name("Idempotency-Key")
                        .description("Unique key to guarantee idempotent execution for retried write requests")
                        .required(false) // optional header
                        .schema(new StringSchema())
                );
            }
            return operation;
        };
    }

}
