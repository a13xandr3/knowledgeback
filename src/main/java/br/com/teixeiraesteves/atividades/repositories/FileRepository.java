package br.com.teixeiraesteves.atividades.repositories;

import br.com.teixeiraesteves.atividades.entities.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    Optional<FileEntity> findBySha256Hex(String sha256Hex);

}
