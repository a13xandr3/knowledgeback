package br.com.teixeiraesteves.atividades.infra;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    /*
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Permite o frontend acessar a API (substitua pelo domínio do Angular)
        config.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://localhost:4205",
                "http://localhost:8081"
        ));

        // Métodos HTTP permitidos
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Permitir envio de credenciais (caso JWT precise)
        config.setAllowCredentials(true);

        // Permitir todos os cabeçalhos
        config.setAllowedHeaders(List.of("*"));

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
    */

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // ajuste os origins conforme seu front

        config.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://localhost:4205",
                "http://127.0.0.1:4200"
                ));

        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"));

        config.setAllowedHeaders(List.of("*"));

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
