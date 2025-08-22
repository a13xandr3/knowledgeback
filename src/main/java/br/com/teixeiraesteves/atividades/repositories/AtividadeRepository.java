package br.com.teixeiraesteves.atividades.repositories;

import br.com.teixeiraesteves.atividades.entities.Atividade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {}