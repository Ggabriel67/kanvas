package io.github.ggabriel67.kanvas.board;

import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.workspace.Workspace;
import io.github.ggabriel67.kanvas.workspace.WorkspaceBoardFlatDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
    List<BoardDtoProjection> findBoardsByWorkspace(@Param("workspace") Workspace workspace);

    @Query("""
SELECT NEW io.github.ggabriel67.kanvas.board.BoardDtoProjection(
    b.id, b.name
)
FROM Board b
LEFT JOIN BoardMember bm ON bm.board = b AND bm.user.id = :userId
WHERE b.workspace = :workspace
AND (b.visibility = :visibility OR bm.user.id IS NOT NULL)
""")
    List<BoardDtoProjection> findBoardsByMembershipAndVisibility(
            @Param("userId") Integer userId,
            @Param("workspace") Workspace workspace,
            @Param("visibility") BoardVisibility visibility
    );


    @Query("""
SELECT new io.github.ggabriel67.kanvas.workspace.WorkspaceBoardFlatDto(
    b.workspace.id, b.workspace.name, bm.board.id, bm.board.name
)
FROM BoardMember bm
JOIN bm.board b
WHERE bm.user = :user
AND NOT EXISTS (
    SELECT 1
    FROM WorkspaceMember wm
    WHERE wm.user = :user AND wm.workspace = b.workspace
)
""")
    List<WorkspaceBoardFlatDto> findGuestWorkspacesBoardData(@Param("user") User user);

    List<Board> findAllByWorkspace(Workspace workspace);

    @Modifying(clearAutomatically = true)
    @Query("""
DELETE FROM Board b WHERE b.workspace = :workspace
""")
    void deleteAllByWorkspace(@Param("workspace") Workspace workspace);
}
