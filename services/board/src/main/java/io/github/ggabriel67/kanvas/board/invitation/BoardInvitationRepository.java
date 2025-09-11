package io.github.ggabriel67.kanvas.board.invitation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardInvitationRepository extends JpaRepository<BoardInvitation, Integer> {
}
