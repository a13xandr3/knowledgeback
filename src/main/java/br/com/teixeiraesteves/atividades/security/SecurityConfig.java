package br.com.teixeiraesteves.atividades.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {

    //Injete o CorsConfigurationSource definido em CorsConfig
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // habilita CORS com base no CorsConfig
            .cors(cors -> {})
            .csrf(csrf -> csrf.disable())          // desabilita CSRF para APIs REST

            //Configura headers, incluindo X-Frame-Options
            .headers(headers -> headers
                    .frameOptions(frame -> frame.sameOrigin())
                    // ou .disable() se quiser liberar totalmente
            )

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/atividade").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/atividade/*").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/atividade").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/fxpreview").permitAll()
                .requestMatchers(HttpMethod.PUT,"/api/atividade/*").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/atividade/*").permitAll()
                .anyRequest().authenticated()
            )

            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable());

            //.cors(cors -> cors.configurationSource(corsConfigurationSource))

        return http.build();
    }
}
