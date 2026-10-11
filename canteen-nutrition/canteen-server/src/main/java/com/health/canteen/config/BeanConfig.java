package com.health.canteen.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BeanConfig {

    /**
     * 密码加密器：数据库中只保存BCrypt密文
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public OpenAPI canteenOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("校园食堂菜品营养分析与推荐系统 接口文档")
                .description("需要登录的接口请在请求头携带 token")
                .version("1.0.0"));
    }

}
