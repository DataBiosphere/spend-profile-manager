package bio.terra.profile.app.configuration;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerDocumentationConfig {

  @Bean
  public Docket customImplementation() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.basePackage("bio.terra"))
        .paths(or(regex("/api.*"), regex("/status"), regex("/version")))
        .build()
        .apiInfo(apiInfo());
  }

  private ApiInfo apiInfo() {
    return new ApiInfoBuilder()
        .title("Spend Profile Manager API")
        .description("Manages spend profiles")
        .license("Apache 2.0")
        .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0.html")
        .termsOfServiceUrl("")
        .version("0.1.0")
        .contact(new Contact("", "", ""))
        .build();
  }
}
