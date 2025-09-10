package io.github.ggabriel67.kanvas.workspace.invitation;

import io.github.ggabriel67.kanvas.invitation.InvitationStatus;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.workspace.Workspace;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "workspace_invitations")
public class WorkspaceInvitation
{
    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    private User inviter;

    @ManyToOne
    private User invitee;

    @ManyToOne
    private Workspace workspace;

    @Enumerated(EnumType.STRING)
    private InvitationStatus status;

    private Instant expirationTime;
}
