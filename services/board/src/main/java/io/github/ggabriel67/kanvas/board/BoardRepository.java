package io.github.ggabriel67.kanvas.board;

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

    @Query("""
SELECT NEW io.github.ggabriel67.kanvas.board.BoardDtoProjection(
    b.id, b.name
)
FROM Board b
LEFT JOIN BoardMember bm ON bm.board = b AND bm.user.id = :userId
WHERE b.workspace = :workspace
AND (b.visibility = :visibility OR bm.user.id IS NOT NULL)
""")
    List<BoardDtoProjection> findBoardsForMemberByVisibility(
            @Param("userId") Integer userId,
            @Param("workspace") Workspace workspace,
            @Param("visibility") BoardVisibility visibility
    );
}
