package io.github.ggabriel67.kanvas;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ggabriel67.kanvas.authorization.board.BoardRole;
import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceRole;
import io.github.ggabriel67.kanvas.board.Board;
import io.github.ggabriel67.kanvas.board.BoardRepository;
import io.github.ggabriel67.kanvas.board.BoardRequest;
import io.github.ggabriel67.kanvas.board.BoardVisibility;
import io.github.ggabriel67.kanvas.board.member.BoardMember;
import io.github.ggabriel67.kanvas.board.member.BoardMemberRepository;
import io.github.ggabriel67.kanvas.kafka.producer.BoardEventProducer;
import io.github.ggabriel67.kanvas.kafka.producer.InvitationEventProducer;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.user.UserRepository;
import io.github.ggabriel67.kanvas.workspace.Workspace;
import io.github.ggabriel67.kanvas.workspace.WorkspaceRepository;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMember;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BoardControllerIntegrationTest
{
    @MockitoBean
    private BoardEventProducer boardEventProducer;

    @MockitoBean
    private InvitationEventProducer invitationEventProducer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WorkspaceRepository workspaceRepository;
    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private BoardMemberRepository boardMemberRepository;

    @BeforeEach
    void cleanDatabase() {
        boardMemberRepository.deleteAll();
        boardRepository.deleteAll();
        workspaceMemberRepository.deleteAll();
        userRepository.deleteAll();
        workspaceRepository.deleteAll();
    }

    @Test
    void createBoard_ShouldCreateBoardSuccessfully() throws Exception {
        Integer userId = 1;
        User user = User.builder().id(userId).build();
        userRepository.save(user);

        Workspace workspace = Workspace.builder().name("Workspace").build();
        workspaceRepository.save(workspace);

        WorkspaceMember workspaceMember = WorkspaceMember.builder().workspace(workspace).user(user).role(WorkspaceRole.MEMBER).build();
        workspaceMemberRepository.save(workspaceMember);

        BoardRequest request = new BoardRequest(userId, workspace.getId(), "Test Board", "Test Board description", BoardVisibility.WORKSPACE_PUBLIC);

        var result = mockMvc.perform(post("/api/v1/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", userId))
                .andExpect(status().isCreated())
                .andReturn();

        Integer boardId = objectMapper.readValue(result.getResponse().getContentAsString(), Integer.class);
        assertThat(boardId).isNotNull();

        var savedBoard = boardRepository.findById(boardId);
        assertThat(savedBoard).isPresent();

        var member = boardMemberRepository.findByUserIdAndBoardId(userId, boardId);
        assertThat(member).isPresent();
        assertThat(member.get().getRole()).isEqualTo(BoardRole.ADMIN);
    }

    @Test
    void createBoard_ShouldReturnBadRequestWhenBoardNameAlreadyExists() throws Exception {
        Integer userId = 1;
        User user = User.builder().id(userId).build();
        userRepository.save(user);

        Workspace workspace = Workspace.builder().name("Workspace").build();
        workspaceRepository.save(workspace);

        boardRepository.save(Board.builder().workspace(workspace).name("Existing name").build());

        BoardRequest request = new BoardRequest(userId, workspace.getId(), "Existing name", "Test Board description", BoardVisibility.WORKSPACE_PUBLIC);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteBoard_ShouldDeleteBoardAndItsMembersSuccessfully() throws Exception {
        Integer userId = 1;
        User user = User.builder().id(userId).build();
        userRepository.save(user);

        Workspace workspace = Workspace.builder().name("Workspace").build();
        workspaceRepository.save(workspace);

        Board board = Board.builder().workspace(workspace).name("To delete board").build();
        boardRepository.save(board);

        BoardMember bm1 = BoardMember.builder().board(board).user(user).role(BoardRole.ADMIN).build();
        BoardMember bm2 = BoardMember.builder().board(board).role(BoardRole.ADMIN).build();
        boardMemberRepository.saveAll(List.of(bm1, bm2));

        mockMvc.perform(delete("/api/v1/boards/{boardId}", board.getId())
                        .header("X-User-Id", userId))
                .andExpect(status().isOk());

        assertThat(boardMemberRepository.findById(bm1.getId()).isEmpty());
        assertThat(boardMemberRepository.findById(bm2.getId()).isEmpty());
        assertThat(boardRepository.findById(board.getId()).isEmpty());
    }
}
