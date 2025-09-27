package io.github.ggabriel67.kanvas.workspace;

import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceAuthorization;
import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceRole;
import io.github.ggabriel67.kanvas.board.BoardDtoProjection;
import io.github.ggabriel67.kanvas.board.BoardRepository;
import io.github.ggabriel67.kanvas.board.BoardService;
import io.github.ggabriel67.kanvas.exception.WorkspaceNotFoundException;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.user.UserService;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMember;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberRepository;
import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceService
{
    private final WorkspaceAuthorization workspaceAuth;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserService userService;
    private final BoardService boardService;
    private final BoardRepository boardRepository;

    public Integer createWorkspace(WorkspaceRequest request) {
        User user = userService.getUserById(request.creatorId());

        Workspace workspace = workspaceRepository.save(
                Workspace.builder()
                        .name(request.name())
                        .description(request.description())
                        .build()
        );

        workspaceMemberRepository.save(
                WorkspaceMember.builder()
                        .user(user)
                        .workspace(workspace)
                        .role(WorkspaceRole.OWNER)
                        .build()
        );

        return workspace.getId();
    }

    public void updateWorkspace(WorkspaceRequest request, Integer workspaceId) {
        Workspace workspace = this.getWorkspaceById(workspaceId);

        mergeWorkspace(workspace, request);
        workspaceRepository.save(workspace);
    }

    private void mergeWorkspace(Workspace workspace, WorkspaceRequest request) {
        workspace.setName(request.name());
        workspace.setDescription(request.description());
    }

    public Workspace getWorkspaceById(Integer id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));
    }

    public WorkspaceDto getWorkspace(Integer workspaceId) {
        Integer userId = workspaceAuth.getCurrentUserId();

        Workspace workspace = getWorkspaceById(workspaceId);
        WorkspaceRole workspaceRole = workspaceMemberRepository.findByUserIdAndWorkspaceId(userId, workspaceId)
                .map(WorkspaceMember::getRole)
                .orElseThrow(() -> new ForbiddenException("Not a member"));

        List<BoardDtoProjection> boardProjections;
        if (workspaceRole == WorkspaceRole.OWNER || workspaceRole == WorkspaceRole.ADMIN) {
            boardProjections = boardService.getAllBoardsByWorkspace(workspace);
        }
        else {
            boardProjections = boardService.getPublicBoardsByWorkspaceAndMember(userId, workspace);
        }

        return new WorkspaceDto(
                workspace.getId(),
                workspace.getName(),
                workspace.getDescription(),
                workspace.getCreatedAt(),
                workspaceRole,
                boardProjections
        );
    }

    public List<WorkspaceDtoProjection> getAllUserWorkspaces(Integer userId) {
        User user = userService.getUserById(userId);

        return workspaceRepository.findWorkspacesByUser(user);
    }

    public List<GuestWorkspaceDto> getGuestWorkspaces(Integer userId) {
        User user = userService.getUserById(userId);
        var flatResults = boardRepository.findGuestWorkspacesBoardData(user);
        Map<Integer, List<WorkspaceBoardFlatDto>> grouped = flatResults.stream()
                .collect(Collectors.groupingBy(WorkspaceBoardFlatDto::workspaceId));

        return grouped.entrySet().stream()
                .map(entry -> {
                    Integer workspaceId = entry.getKey();
                    var flatRes = entry.getValue();
                    String workspaceName = flatRes.getFirst().workspaceName();

                    var boardProjections = flatRes.stream()
                            .map(res -> new BoardDtoProjection(res.boardId(), res.boardName()))
                            .toList();

                    return new GuestWorkspaceDto(workspaceId, workspaceName, boardProjections);
                })
                .toList();
    }
}
