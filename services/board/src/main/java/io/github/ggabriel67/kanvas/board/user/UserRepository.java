package io.github.ggabriel67.kanvas.board.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>
{
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
}
