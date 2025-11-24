package es.luigi.chefsitoLuigi.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI chefsitoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Chefsito API")
                        .version("1.0.0")
                        .description("API REST para gestión de ingredientes, recetas y listas de compra en Chefsito.")
                        .contact(new Contact()
                                .name("Luigi")
                                .email("luigi@chefsito.app")
                                .url("https://github.com/luigi/chefsito"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html"))
                );
    }
}