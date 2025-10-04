package io.github.ggabriel67.kanvas.workspace.invitation;

import io.github.ggabriel67.kanvas.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkspaceInvitationRepository extends JpaRepository<WorkspaceInvitation, Integer>
{
    Optional<WorkspaceInvitation> findByInviteeIdAndWorkspaceId(Integer inviteeId, Integer workspaceId);

    void deleteAllByInvitee(User user);
}
