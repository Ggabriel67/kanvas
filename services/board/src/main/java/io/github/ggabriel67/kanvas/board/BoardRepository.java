package io.github.ggabriel67.kanvas.board;

import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.workspace.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Integer>
{
    Optional<Board> findByNameAndWorkspace(String name, Workspace workspace);

    @Query("""
SELECT NEW io.github.ggabriel67.kanvas.board.BoardDtoProjection(
    b.id, b.name
)
FROM Board b
WHERE b.workspace = :workspace
""")
    List<BoardDtoProjection> findByWorkspace(@Param("workspace") Workspace workspace);
}
