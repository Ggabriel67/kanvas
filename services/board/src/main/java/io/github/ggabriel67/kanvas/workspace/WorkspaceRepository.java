package io.github.ggabriel67.kanvas.workspace;

import io.github.ggabriel67.kanvas.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Integer>
{
    Optional<Workspace> findByNameAndCreatedBy(String name, User user);
}
