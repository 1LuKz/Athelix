package br.com.athletix.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Sessões HTTP preservam a identidade; a atlética selecionada é validada em filtro próprio.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(passwordEncoder);
        provider.setUserDetailsService(userDetailsService);
        return provider::authenticate;
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler,
            SecurityContextRepository securityContextRepository,
            AtleticaContextFilter atleticaContextFilter
    ) throws Exception {
        return http
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .securityContext(context -> context.securityContextRepository(securityContextRepository)
                        .requireExplicitSave(false))
                .sessionManagement(session -> session.sessionFixation(fixation -> fixation.changeSessionId()))
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/auth/csrf", "/auth/login").permitAll()
                        .requestMatchers("/auth/logout", "/auth/me", "/auth/selecionar-atletica").authenticated()
                        .anyRequest().authenticated())
                .addFilterAfter(atleticaContextFilter,
                        org.springframework.security.web.context.SecurityContextHolderFilter.class)
                .build();
    }
}
