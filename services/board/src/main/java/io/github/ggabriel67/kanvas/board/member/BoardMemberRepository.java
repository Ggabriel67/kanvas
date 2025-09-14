package io.github.ggabriel67.kanvas.board.member;

import io.github.ggabriel67.kanvas.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardMemberRepository extends JpaRepository<BoardMember, Integer>
{
    Optional<BoardMember> findByUserIdAndBoardId(Integer userId, Integer boardId);

    List<BoardMember> findByBoard(Board board);

    void deleteAllByBoard(Board board);
}
