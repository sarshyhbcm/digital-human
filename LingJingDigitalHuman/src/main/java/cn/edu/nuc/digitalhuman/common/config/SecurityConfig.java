package cn.edu.nuc.digitalhuman.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 安全配置
 *
 * 把 BCryptPasswordEncoder 交给Spring管理，
 * 这样其他地方（如 UserServiceImpl）通过构造方法注入就能直接使用。
 *
 * BCrypt 特点：
 * - 每次加密结果都不同（加了随机盐值）
 * - 加密速度慢（故意设计的，让暴力破解成本变高）
 * - 对比时用 matches() 方法，不是直接 equals()
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
