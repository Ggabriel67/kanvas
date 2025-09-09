package io.github.ggabriel67.kanvas.workspace.member;

import org.springframework.stereotype.Service;

@Service
public class WorkspaceMemberMapper
{
    public WorkspaceMemberDto toWorkspaceMemberDto(WorkspaceMember member) {
        return new WorkspaceMemberDto(
                member.getId(),
                member.getUser().getFirstname(),
                member.getUser().getLastname(),
                member.getUser().getUsername(),
                member.getUser().getAvatarColor(),
                member.getRole(),
                member.getJoinedAt()
        );
    }
}
