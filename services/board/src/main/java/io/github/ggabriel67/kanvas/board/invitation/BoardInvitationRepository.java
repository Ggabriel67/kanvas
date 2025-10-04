package io.github.ggabriel67.kanvas.board.invitation;

import io.github.ggabriel67.kanvas.board.Board;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.workspace.invitation.WorkspaceInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardInvitationRepository extends JpaRepository<BoardInvitation, Integer>
{
    Optional<BoardInvitation> findByInviteeIdAndBoardId(Integer inviteeId, Integer boardId);

    void deleteAllByBoard(Board board);

    void deleteAllByInvitee(User user);
}
