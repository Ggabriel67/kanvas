package io.github.ggabriel67.kanvas.workspace.member;

import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceRole;
import io.github.ggabriel67.kanvas.exception.UserNotFoundException;
import io.github.ggabriel67.kanvas.exception.WorkspaceNotFoundException;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.workspace.Workspace;
import io.github.ggabriel67.kanvas.workspace.WorkspaceRepository;
import io.github.ggabriel67.kanvas.workspace.invitation.WorkspaceInvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceMemberService
{
    private final WorkspaceMemberRepository memberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberMapper memberMapper;
    private final WorkspaceInvitationRepository workspaceInvitationRepository;

    public void addWorkspaceMember(Workspace workspace, User invitee, WorkspaceRole role) {
        memberRepository.save(
                WorkspaceMember.builder()
                        .user(invitee)
                        .workspace(workspace)
                        .role(role)
                        .build()
        );
    }

    public void changeMemberRole(WorkspaceRoleChangeRequest request) {
        if (workspaceRepository.findById(request.workspaceId()).isEmpty()) {
            throw new WorkspaceNotFoundException("Workspace not found");
        }

        WorkspaceMember member = getMemberById(request.targetMemberId());

        member.setRole(request.newRole());
        memberRepository.save(member);
    }

    @Transactional
    public void removeMember(WorkspaceMemberRemoveRequest request) {
        WorkspaceMember member = getMemberById(request.targetMemberId());
        workspaceInvitationRepository.deleteAllByInvitee(member.getUser());
        memberRepository.delete(member);
    }

    public List<WorkspaceMemberDto> getAllWorkspaceMembers(Integer workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));

        List<WorkspaceMember> members = memberRepository.findAllByWorkspace(workspace);
        return members.stream()
                .map(memberMapper::toWorkspaceMemberDto)
                .collect(Collectors.toList());
    }

    private WorkspaceMember getMemberById(Integer memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public void leaveWorkspace(WorkspaceMemberRemoveRequest request) {
        WorkspaceMember member = memberRepository.findById(request.targetMemberId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        memberRepository.delete(member);
    }
}
