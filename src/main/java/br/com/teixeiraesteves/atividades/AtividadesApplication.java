package br.com.teixeiraesteves.atividades;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AtividadesApplication {

	public static void main(String[] args) {

		SpringApplication.run(AtividadesApplication.class, args);

	}

}