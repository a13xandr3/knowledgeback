package br.com.teixeiraesteves.atividades.repositories;

import br.com.teixeiraesteves.atividades.entities.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.twoFaSecret = :secret, u.twoFaEnabled = true WHERE u.username = :username")
    void updateSecret(String username, String secret);

    @Query("SELECT u.twoFaSecret FROM User u WHERE u.username = :username")
    String getSecret(String username);

}
