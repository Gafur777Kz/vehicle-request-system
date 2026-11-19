package kz.narxoz.vrs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

// пользователи и проверка пароля для логина
@Configuration
public class UsersConfig {

    // BCrypt: в памяти хранится хеш вида {bcrypt}$2a$10$..., а не сам пароль
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // два учебных пользователя, есть только на профиле dev (ноутбук)
    @Bean
    @Profile("dev")
    public UserDetailsService devUsers(PasswordEncoder encoder) {
        UserDetails employee = User.withUsername("employee")
                .password(encoder.encode("employee-dev"))
                .roles("EMPLOYEE")
                .build();
        UserDetails manager = User.withUsername("manager")
                .password(encoder.encode("manager-dev"))
                .roles("MANAGER")
                .build();
        return new InMemoryUserDetailsManager(employee, manager);
    }

    // "найди пользователя и сравни хеш пароля" - его вызывает AuthController
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService users, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(users);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }
}
