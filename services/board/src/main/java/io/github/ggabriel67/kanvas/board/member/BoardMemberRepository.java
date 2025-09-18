package io.github.ggabriel67.kanvas.board.member;

import io.github.ggabriel67.kanvas.authorization.board.BoardRole;
import io.github.ggabriel67.kanvas.board.Board;
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

    List<BoardMember> findByBoard(Board board);

    void deleteAllByBoard(Board board);

    @Query("""
SELECT bm.role FROM BoardMember bm
WHERE bm.user.id = :userId AND bm.board.id = :boardId
""")
    BoardRole findRoleByUserIdAndBoardId(@Param("userId") Integer userId, @Param("boardId") Integer boardId);
}
