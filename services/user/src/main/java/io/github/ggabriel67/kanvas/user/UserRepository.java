package io.github.ggabriel67.kanvas.user;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    @Query("""
SELECT u FROM User u
WHERE u.email LIKE %:query% OR u.username LIKE %:query%
""")
    List<User> searchUsers(@Param("query") String query, Pageable pageable);
}
