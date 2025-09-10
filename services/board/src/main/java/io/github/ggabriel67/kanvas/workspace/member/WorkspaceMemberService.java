package io.github.ggabriel67.kanvas.workspace.member;

import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceRole;
import io.github.ggabriel67.kanvas.exception.UserNotFoundException;
import io.github.ggabriel67.kanvas.exception.WorkspaceNotFoundException;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.user.UserService;
import io.github.ggabriel67.kanvas.workspace.Workspace;
import io.github.ggabriel67.kanvas.workspace.WorkspaceDtoProjection;
import io.github.ggabriel67.kanvas.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceMemberService
{
    private final WorkspaceMemberRepository memberRepository;
    private final UserService userService;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberMapper memberMapper;

    public void addWorkspaceMember(Workspace workspace, User invitee, WorkspaceRole role) {
        memberRepository.save(
                WorkspaceMember.builder()
                        .user(invitee)
                        .workspace(workspace)
                        .role(role)
                        .build()
        );
    }

    public void changeWorkspaceMemberRole(WorkspaceRoleChangeRequest request) {
        WorkspaceMember member = getMemberById(request.targetMemberId());

        member.setRole(request.newRole());
    }

    public void removeMember(WorkspaceMemberRemoveRequest request) {
        WorkspaceMember member = getMemberById(request.targetMemberId());
        memberRepository.delete(member);
    }

    public List<WorkspaceDtoProjection> getAllUserWorkspaces(Integer userId) {
        User user = userService.getUserById(userId);

        return memberRepository.findWorkspacesByUser(user);
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
}
