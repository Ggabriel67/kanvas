package io.github.ggabriel67.kanvas.board.invitation;

import io.github.ggabriel67.kanvas.board.Board;
import io.github.ggabriel67.kanvas.invitation.InvitationStatus;
import io.github.ggabriel67.kanvas.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardInvitationRepository extends JpaRepository<BoardInvitation, Integer>
{
    void deleteAllByBoard(Board board);

    void deleteAllByInvitee(User user);

    @Query("""
SELECT i FROM BoardInvitation i
WHERE i.invitee.id = :inviteeId AND i.board.id = :boardId
AND i.status = :status
""")
    Optional<Object> findPendingByInviteeIdAndBoardId(
            @Param("inviteeId") Integer inviteeId,
            @Param("boardId") Integer boardId,
            @Param("status") InvitationStatus status);

    @Modifying(clearAutomatically = true)
    @Query("""
DELETE FROM BoardInvitation bi WHERE bi.board IN :boards
""")
    void deleteAllWhereBoardIn(@Param("boards") List<Board> boards);
}
