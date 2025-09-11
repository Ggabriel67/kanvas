package io.github.ggabriel67.kanvas.board;

import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.workspace.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Integer>
{
    Optional<Board> findByNameAndWorkspace(String name, Workspace workspace);
}
