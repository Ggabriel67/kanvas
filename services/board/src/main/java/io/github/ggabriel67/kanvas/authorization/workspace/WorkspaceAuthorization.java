package io.github.ggabriel67.kanvas.authorization.workspace;

import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("workspaceAuth")
@RequiredArgsConstructor
public class WorkspaceAuthorization
{
    private final WorkspaceMemberRepository memberRepository;

    public boolean hasRole(Integer userId, Integer workspaceId, WorkspaceRole requiredRole) {
        return memberRepository.findByUserIdAndWorkspaceId(userId, workspaceId)
                .map(member -> switch (requiredRole) {
                    case OWNER -> member.getRole() == WorkspaceRole.OWNER;
                    case ADMIN -> member.getRole() == WorkspaceRole.OWNER || member.getRole() == WorkspaceRole.ADMIN;
                    case MEMBER -> true;
                })
                .orElse(false);
    }

    public boolean isOwner(Integer userId, Integer workspaceId) {
        return hasRole(userId, workspaceId, WorkspaceRole.OWNER);
    }

    public boolean isAdminOrOwner(Integer userId, Integer workspaceId) {
        return hasRole(userId, workspaceId, WorkspaceRole.ADMIN);
    }

    public boolean isMember(Integer userId, Integer workspaceId) {
        return hasRole(userId, workspaceId, WorkspaceRole.MEMBER);
    }
}
