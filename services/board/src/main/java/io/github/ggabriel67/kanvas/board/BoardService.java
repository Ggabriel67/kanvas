package io.github.ggabriel67.kanvas.board;

import io.github.ggabriel67.kanvas.authorization.board.BoardRole;
import io.github.ggabriel67.kanvas.board.member.BoardMember;
import io.github.ggabriel67.kanvas.board.member.BoardMemberRepository;
import io.github.ggabriel67.kanvas.exception.BoardNotFoundException;
import io.github.ggabriel67.kanvas.exception.NameAlreadyInUseException;
import io.github.ggabriel67.kanvas.exception.WorkspaceNotFoundException;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.user.UserService;
import io.github.ggabriel67.kanvas.workspace.Workspace;
import io.github.ggabriel67.kanvas.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService
{
    private final BoardRepository boardMemberRepository;
    private final UserService userService;
    private final WorkspaceRepository workspaceRepository;
    private final BoardMemberRepository memberRepository;

    public void createBoard(BoardRequest request) {
        User user = userService.getUserById(request.createdById());

        Workspace workspace = workspaceRepository.findById(request.workspaceId())
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));

        if (boardMemberRepository.findByNameAndWorkspace(request.name(), workspace).isPresent()) {
            throw new NameAlreadyInUseException("Board with name " +  request.name() + " already exists in this workspace");
        }

        Board board = boardMemberRepository.save(Board.builder()
                        .createdBy(user)
                        .workspace(workspace)
                        .name(request.name())
                        .description(request.description())
                        .visibility(request.visibility())
                        .build()
        );

        memberRepository.save(
                BoardMember.builder()
                        .user(user)
                        .board(board)
                        .role(BoardRole.ADMIN)
                        .build()
        );
    }

    public Board getBoardById(Integer id) {
        return boardMemberRepository.findById(id)
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));
    }

    public List<BoardDtoProjection> getBoardsByWorkspace(Workspace workspace) {
        return boardMemberRepository.findByWorkspace(workspace);
    }

    public List<BoardDtoProjection> getVisibleBoardsForMember(Integer userId, Workspace workspace) {
        return memberRepository.findByUserIdAndWorkspace(userId, workspace, BoardVisibility.WORKSPACE_PUBLIC);
    }
}
