package io.github.ggabriel67.kanvas.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer>
{
    @Query("""
    SELECT t from Token t INNER JOIN User u ON t.user.id = u.id
    WHERE u.id = :userId and (t.expired = false OR t.revoked = FALSE)
""")
    List<Token> findAllValidTokensByUser(@Param("userId") Integer userId);

    Optional<Token> findByToken(String token);
}
