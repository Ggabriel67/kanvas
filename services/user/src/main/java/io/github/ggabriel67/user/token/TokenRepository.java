package io.github.ggabriel67.user.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer>
{
    @Query("""
    SELECT t from Token t INNER JOIN User u ON t.user.id = u.id
    WHERE u.id = :userId and (t.isExpired = false OR t.isRevoked = FALSE)
""")
    List<Token> findAllValidTokensByUser(@Param("userId") Integer userId);

    Optional<Token> findByToken(String token);
}
