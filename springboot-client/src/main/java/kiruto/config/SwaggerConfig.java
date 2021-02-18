package kiruto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket userApi() {
        return new Docket(DocumentationType.SWAGGER_2).groupName("秒杀案例")
            .apiInfo(getInfo()).select()
            .apis(RequestHandlerSelectors.basePackage("kiruto.web"))
            .paths(PathSelectors.any()).build();
    }

    /**
     * 设置显示相关信息
     */
    private ApiInfo getInfo() {
        return new ApiInfoBuilder().title("基于自制RPC框架的秒杀案例文档").build();
    }
}
