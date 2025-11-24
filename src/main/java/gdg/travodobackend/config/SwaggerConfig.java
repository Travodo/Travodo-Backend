package gdg.travodobackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

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

        // 서버 정보 설정 (환경별로 다르게 설정)
        List<Server> servers = new ArrayList<>();
        
        if ("local".equals(activeProfile)) {
            // 로컬 환경: 로컬 서버와 운영 서버 모두 제공
            Server localServer = new Server()
                    .url("http://localhost:8080")
                    .description("로컬 개발 서버");
            Server prodServer = new Server()
                    .url("https://travodo.duckdns.org")
                    .description("운영 서버");
            servers.add(localServer);
            servers.add(prodServer);
        } else {
            // 프로덕션 환경: 운영 서버만 제공
            Server prodServer = new Server()
                    .url("https://travodo.duckdns.org")
                    .description("운영 서버");
            servers.add(prodServer);
        }

        // API 정보 설정
        Info info = new Info()
                .title("Travodo API Documentation")
                .version("1.0.0")
                .description("Travodo 백엔드 API 문서")
                .contact(new Contact()
                        .name("IOS team6")
                        .email("gdg.travodo@gmail.com")
                        .url("https://travodo.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0.html"));

        return new OpenAPI()
                .info(info)
                .servers(servers)
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}

