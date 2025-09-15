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
    WHERE u.id = :userId AND (t.expired = FALSE OR t.revoked = FALSE) AND
    t.tokenType = :tokenType
""")
    List<Token> findAllValidTokensByUserAndType(@Param("userId") Integer userId, @Param("tokenType") TokenType tokenType);

    Optional<Token> findByToken(String token);
}
