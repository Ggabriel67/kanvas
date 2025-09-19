package io.github.ggabriel67.kanvas.board.invitation;

import io.github.ggabriel67.kanvas.authorization.board.BoardRole;
import io.github.ggabriel67.kanvas.board.Board;
import io.github.ggabriel67.kanvas.invitation.InvitationStatus;
import io.github.ggabriel67.kanvas.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "board_invitations")
public class BoardInvitation
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "inviter_id")
    private User inviter;

    @ManyToOne
    @JoinColumn(name = "invitee_id")
    private User invitee;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;

    @Enumerated(EnumType.STRING)
    private BoardRole role;

    @Enumerated(EnumType.STRING)
    private InvitationStatus status;

    private Instant expirationTime;
}
