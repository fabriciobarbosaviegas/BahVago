package com.bahvago.config;

import java.io.IOException;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {

            boolean isHoteleiro = authentication.getAuthorities()
                    .contains(new SimpleGrantedAuthority("ROLE_HOTELEIRO"));

            String referer = request.getHeader("Referer");

            if (referer != null && referer.contains("/login-hoteleiro")) {

                if (isHoteleiro) {
                    response.sendRedirect("/dashboard");
                } else {
                    response.sendRedirect("/login-hoteleiro?error");
                }

                return;
            }

            response.sendRedirect("/");
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // No Spring Security 6, o CookieCsrfTokenRepository sozinho não grava
        // o cookie XSRF-TOKEN em toda resposta. O CsrfTokenRequestAttributeHandler
        // garante que o token seja resolvido e o cookie seja gravado em cada request,
        // permitindo que o JavaScript leia o token e o envie nos POSTs (AJAX/fetch).
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null);

        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(requestHandler)
            )

            .authorizeHttpRequests(auth -> auth

                // Permite o despacho interno para páginas de erro (404, 500, etc.)
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                // Endpoint de erro do Spring Boot
                .requestMatchers("/error", "/error/**").permitAll()

                // Recursos estáticos
                .requestMatchers(
                        "/",
                        "/static/**",
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/uploads/**"
                ).permitAll()

                // Login e cadastro
                .requestMatchers(
                        "/login",
                        "/login-hoteleiro",
                        "/cadastro",
                        "/usuarios/cadastro"
                ).permitAll()

                // Área do hoteleiro (precisa vir antes de "/hoteis/**"/"/quartos/**" abaixo,
                // que são públicos — regras do authorizeHttpRequests usam a primeira que casar).
                // "/quartos/hotel/*" (form de novo quarto) e ".../numero/*/editar" ficam aqui
                // porque são administrativos; "/quartos/hotel/*/numero/*" (página pública do
                // quarto) e ".../ofertas" continuam cobertos pela regra pública mais abaixo.
                .requestMatchers(
                        "/dashboard",
                        "/estatisticas",
                        "/hoteis/gerenciar-hotel",
                        "/hoteis/criar",
                        "/hoteis/atualizar/**",
                        "/hoteis/deletar/**",
                        "/gerenciar-quartos",
                        "/quartos/criar",
                        "/quartos/atualizar/**",
                        "/quartos/deletar/**",
                        "/quartos/hotel/*",
                        "/quartos/hotel/*/numero/*/editar",
                        "/gerenciar-avaliacoes"
                ).hasRole("HOTELEIRO")

                // Páginas públicas
                .requestMatchers(
                        "/hoteis/**",
                        "/quartos/**"
                ).permitAll()

                // Área autenticada
                .requestMatchers(
                        "/usuarios/perfil/**",
                        "/usuarios/atualizar/**",
                        "/usuarios/deletar/**",
                        "/favoritos",
                        "/favoritos/**"
                ).authenticated()

                // Todo o restante exige autenticação
                .anyRequest().permitAll()
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