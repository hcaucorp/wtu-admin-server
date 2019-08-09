package es.coffeebyt.wtu.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

import io.swagger.annotations.ApiOperation;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * API will be published on https://app.swaggerhub.com/apis-docs/wallet-top-up/wtu-public-api/1.0.0
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.ant("/api/vouchers/*"))
                .build()
                .apiInfo(apiInfo())
                .host("api.wallettopup.co.uk");
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "Wallet Top Up REST API",
                "Documentation for third party wallet integration.",
                "0.2.0",
                "Terms of service",
                new Contact("Contact Us", "https://www.wallettopup.co.uk/contact", "hubert.czerpak@wallettopup.co.uk"),
                "Apache 2.0", "http://www.apache.org/licenses/LICENSE-2.0", Collections.emptyList());
    }
}
