package io.github.ggabriel67.kanvas.board.member;

import io.github.ggabriel67.kanvas.authorization.board.BoardRole;
import io.github.ggabriel67.kanvas.board.Board;
import io.github.ggabriel67.kanvas.workspace.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardMemberRepository extends JpaRepository<BoardMember, Integer>
{
    Optional<BoardMember> findByUserIdAndBoardId(Integer userId, Integer boardId);

    List<BoardMember> findByBoard(Board board);

    void deleteAllByBoard(Board board);

    @Query("""
SELECT bm.role FROM BoardMember bm
WHERE bm.user.id = :userId AND bm.board.id = :boardId
""")
    Optional<BoardRole> findRoleByUserIdAndBoardId(@Param("userId") Integer userId, @Param("boardId") Integer boardId);

    @Modifying(clearAutomatically = true)
    @Query("""
DELETE FROM BoardMember bm WHERE bm.board IN :boards
""")
    void deleteAllWhereBoardIn(@Param("boards") List<Board> boards);

    @Query("""
SELECT new io.github.ggabriel67.kanvas.board.member.MemberDto(
    bm.id, u.id, u.firstname, u.lastname, u.username, u.avatarColor, bm.role, wm.role, bm.joinedAt
)
FROM BoardMember bm
JOIN bm.user u
LEFT JOIN WorkspaceMember wm ON wm.workspace = :workspace AND wm.user = u
WHERE bm.board = :board
""")
    List<MemberDto> findMembersWithWorkspaceRole(
            @Param("board") Board board,
            @Param("workspace") Workspace workspace
    );
}
