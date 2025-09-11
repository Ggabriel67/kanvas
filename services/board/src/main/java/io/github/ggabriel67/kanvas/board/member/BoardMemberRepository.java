package io.github.ggabriel67.kanvas.board.member;

import io.github.ggabriel67.kanvas.board.BoardDtoProjection;
import io.github.ggabriel67.kanvas.board.BoardVisibility;
import io.github.ggabriel67.kanvas.workspace.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardMemberRepository extends JpaRepository<BoardMember, Integer>
{
    Optional<BoardMember> findByUserIdAndBoardId(Integer userId, Integer boardId);

    @Query("""
SELECT NEW io.github.ggabriel67.kanvas.board.BoardDtoProjection(
    b.id, b.name
)
FROM BoardMember bm
LEFT JOIN bm.board b
WHERE (b.visibility = :visibility AND b.workspace = :workspace)
OR (bm.user.id = :userId AND b.workspace = :workspace)
""")
    List<BoardDtoProjection> findByUserIdAndWorkspace(
            @Param("userId") Integer userId,
            @Param("workspace") Workspace workspace,
            @Param("visibility") BoardVisibility visibility
    );
}
