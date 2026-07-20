package com.bahvago.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                               HttpServletResponse response,
                                               Authentication authentication) throws IOException, ServletException {
                boolean isHoteleiro = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_HOTELEIRO"));
                response.sendRedirect(isHoteleiro ? "/dashboard" : "/");
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .authorizeHttpRequests((authz) -> authz
                .requestMatchers("/", "/static/**", "/css/**", "/js/**", "/img/**").permitAll()
                .requestMatchers("/login", "/cadastro", "/login-hoteleiro", "/usuarios/cadastro").permitAll()
                .requestMatchers("/dashboard", "/estatisticas", "/gerenciar-hotel", "/gerenciar-quartos", "/gerenciar-avaliacoes").hasRole("HOTELEIRO")
                .requestMatchers("/usuarios/perfil/**", "/usuarios/atualizar/**", "/usuarios/deletar/**").authenticated()
                .requestMatchers("/hoteis/**", "/quartos/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("senha")
                .successHandler(authenticationSuccessHandler())
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
