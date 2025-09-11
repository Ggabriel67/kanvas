package io.github.ggabriel67.kanvas.authorization.workspace;

import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMember;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("workspaceAuth")
@RequiredArgsConstructor
public class WorkspaceAuthorization
{
    private final WorkspaceMemberRepository memberRepository;

    public Integer getCurrentUserId() throws IllegalStateException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Integer userId) {
            return  userId;
        }
        throw new IllegalStateException("No user ID in SecurityContext");
    }

    public boolean hasRole(Integer userId, Integer workspaceId, WorkspaceRole requiredRole) {
        return memberRepository.findByUserIdAndWorkspaceId(userId, workspaceId)
                .map(member -> switch (requiredRole) {
                    case OWNER -> member.getRole() == WorkspaceRole.OWNER;
                    case ADMIN -> member.getRole() == WorkspaceRole.OWNER || member.getRole() == WorkspaceRole.ADMIN;
                    case MEMBER -> true;
                })
                .orElse(false);
    }

    public boolean isOwner(Integer workspaceId) {
        return hasRole(getCurrentUserId(), workspaceId, WorkspaceRole.OWNER);
    }

    public boolean isAdminOrOwner(Integer workspaceId) {
        return hasRole(getCurrentUserId(), workspaceId, WorkspaceRole.ADMIN);
    }

    public boolean isMember(Integer workspaceId) {
        return hasRole(getCurrentUserId(), workspaceId, WorkspaceRole.MEMBER);
    }

    public boolean canModerate(Integer workspaceId, Integer targetMemberId) {
        Integer callerId = getCurrentUserId();
        WorkspaceRole callerRole = memberRepository.findByUserIdAndWorkspaceId(callerId, workspaceId)
                .map(WorkspaceMember::getRole)
                .orElse(null);

        WorkspaceRole targetRole = memberRepository.findById(targetMemberId)
                .map(WorkspaceMember::getRole)
                .orElse(null);

        if (callerRole == null || targetRole == null) {
            return false;
        }

        return switch (callerRole) {
            case OWNER -> true;
            case ADMIN -> targetRole == WorkspaceRole.MEMBER;
            default -> false;
        };
    }
}
