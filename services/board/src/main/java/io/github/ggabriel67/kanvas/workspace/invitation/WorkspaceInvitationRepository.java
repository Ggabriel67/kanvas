package io.github.ggabriel67.kanvas.workspace.invitation;

import io.github.ggabriel67.kanvas.invitation.InvitationStatus;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.workspace.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkspaceInvitationRepository extends JpaRepository<WorkspaceInvitation, Integer>
{
    void deleteAllByInvitee(User user);

    @Query("""
SELECT i FROM WorkspaceInvitation i
WHERE i.invitee.id = :inviteeId AND i.workspace.id = :workspaceId
AND i.status = :status
""")
    Optional<WorkspaceInvitation> findPendingByInviteeIdAndWorkspaceId(
            @Param("inviteeId") Integer inviteeId,
            @Param("workspaceId") Integer workspaceId,
            @Param("status") InvitationStatus status);

    @Modifying
    @Query("""
DELETE FROM WorkspaceInvitation wi WHERE wi.workspace = :workspace
""")
    void deleteAllByWorkspace(@Param("workspace") Workspace workspace);
}
