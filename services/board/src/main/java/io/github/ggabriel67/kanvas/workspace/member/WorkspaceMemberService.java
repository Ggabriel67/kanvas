package io.github.ggabriel67.kanvas.workspace.member;

import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceRole;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.user.UserService;
import io.github.ggabriel67.kanvas.workspace.Workspace;
import io.github.ggabriel67.kanvas.workspace.WorkspaceDtoProjection;
import io.github.ggabriel67.kanvas.workspace.WorkspaceService;
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
    private final WorkspaceService workspaceService;
    private final WorkspaceMemberMapper memberMapper;

    public void addWorkspaceMember(WorkspaceMemberRequest request) {
        User user = userService.getUserById(request.userId());
        Workspace workspace =  workspaceService.getWorkspaceById(request.workspaceId());

        memberRepository.save(
                WorkspaceMember.builder()
                        .user(user)
                        .workspace(workspace)
                        .role(request.role())
                        .build()
        );
    }

    public void addWorkspaceOwner(User user, Workspace workspace) {
        memberRepository.save(
                WorkspaceMember.builder()
                        .user(user)
                        .workspace(workspace)
                        .role(WorkspaceRole.OWNER)
                        .build()
        );
    }

    public List<WorkspaceDtoProjection> getAllUserWorkspaces(Integer userId) {
        User user = userService.getUserById(userId);

        return memberRepository.findWorkspacesByUser(user);
    }

    public List<WorkspaceMemberDto> getAllWorkspaceMembers(Integer workspaceId) {
        Workspace workspace = workspaceService.getWorkspaceById(workspaceId);

        List<WorkspaceMember> members = memberRepository.findAllByWorkspace(workspace);
        return members.stream()
                .map(memberMapper::toWorkspaceMemberDto)
                .collect(Collectors.toList());
    }
}
