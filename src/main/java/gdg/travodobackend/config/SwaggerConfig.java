package gdg.travodobackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // Security Scheme 설정 (JWT Bearer Token)
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        // 서버 정보 설정
        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("로컬 개발 서버");

        Server prodServer = new Server()
                .url("https://your-domain.com")
                .description("운영 서버");

        // API 정보 설정
        Info info = new Info()
                .title("Travodo API Documentation")
                .version("1.0.0")
                .description("Travodo 백엔드 API 문서")
                .contact(new Contact()
                        .name("GDG Team")
                        .email("contact@travodo.com")
                        .url("https://travodo.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0.html"));

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer, prodServer))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}

