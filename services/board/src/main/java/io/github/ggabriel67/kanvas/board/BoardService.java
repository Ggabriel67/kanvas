package io.github.ggabriel67.kanvas.board;

import io.github.ggabriel67.kanvas.authorization.board.BoardRole;
import io.github.ggabriel67.kanvas.board.member.BoardMember;
import io.github.ggabriel67.kanvas.board.member.BoardMemberDto;
import io.github.ggabriel67.kanvas.board.member.BoardMemberMapper;
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
    private final BoardRepository boardRepository;
    private final UserService userService;
    private final WorkspaceRepository workspaceRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final BoardMemberMapper boardMemberMapper;

    public void createBoard(BoardRequest request) {
        User user = userService.getUserById(request.creatorId());

        Workspace workspace = workspaceRepository.findById(request.workspaceId())
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));

        if (boardRepository.findByNameAndWorkspace(request.name(), workspace).isPresent()) {
            throw new NameAlreadyInUseException("Board with name " +  request.name() + " already exists in this workspace");
        }

        Board board = boardRepository.save(Board.builder()
                        .workspace(workspace)
                        .name(request.name())
                        .description(request.description())
                        .visibility(request.visibility())
                        .build()
        );

        boardMemberRepository.save(
                BoardMember.builder()
                        .user(user)
                        .board(board)
                        .role(BoardRole.ADMIN)
                        .build()
        );
    }

    public Board getBoardById(Integer id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));
    }

    public List<BoardDtoProjection> getBoardsByWorkspace(Workspace workspace) {
        return boardRepository.findByWorkspace(workspace);
    }

    public List<BoardDtoProjection> getVisibleBoardsForMember(Integer userId, Workspace workspace) {
        return boardRepository.findBoardsForMemberByVisibility(userId, workspace, BoardVisibility.WORKSPACE_PUBLIC);
    }

    public BoardDto getBoard(Integer boardId) {
        Board board = getBoardById(boardId);

        List<BoardMemberDto> boardMembers= boardMemberRepository.findByBoard(board).stream()
                .map(boardMemberMapper::toBoardMemberDto)
                .toList();

        return new BoardDto(
                board.getId(),
                board.getName(),
                board.getDescription(),
                board.getCreatedAt(),
                board.getVisibility(),
                boardMembers
        );
   }
}
