package io.github.ggabriel67.kanvas;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceRole;
import io.github.ggabriel67.kanvas.kafka.producer.BoardEventProducer;
import io.github.ggabriel67.kanvas.kafka.producer.InvitationEventProducer;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.user.UserRepository;
import io.github.ggabriel67.kanvas.workspace.Workspace;
import io.github.ggabriel67.kanvas.workspace.WorkspaceDtoProjection;
import io.github.ggabriel67.kanvas.workspace.WorkspaceRepository;
import io.github.ggabriel67.kanvas.workspace.WorkspaceRequest;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMember;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberRemoveRequest;
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
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WorkspaceControllerIntegrationTest
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

    @BeforeEach
    void cleanDatabase() {
        workspaceMemberRepository.deleteAll();
        userRepository.deleteAll();
        workspaceRepository.deleteAll();
    }

    @Test
    void createWorkspace_ShouldCreateWorkspaceSuccessfully() throws Exception {
        Integer userId = 1;
        User user = User.builder().id(userId).build();
        userRepository.save(user);

        WorkspaceRequest request = new WorkspaceRequest(userId, "Test Workspace", "Test Workspace description");

        var result = mockMvc.perform(post("/api/v1/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Integer workspaceId = objectMapper.readValue(result.getResponse().getContentAsString(), Integer.class);
        assertThat(workspaceId).isNotNull();

        var savedWorkspace = workspaceRepository.findById(workspaceId);
        assertThat(savedWorkspace).isPresent();

        var member = workspaceMemberRepository.findByUserIdAndWorkspaceId(userId, workspaceId);
        assertThat(member).isPresent();
        assertThat(member.get().getRole()).isEqualTo(WorkspaceRole.OWNER);
    }

    @Test
    void getAllUserWorkspaces_ShouldReturnUserWorkspaces() throws Exception {
        Workspace w1 = Workspace.builder().name("Workspace 1").build();
        Workspace w2 = Workspace.builder().name("Workspace 2").build();
        workspaceRepository.saveAll(List.of(w1, w2));

        Integer w1Id = w1.getId();
        Integer w2Id = w2.getId();

        User user = User.builder().id(1).build();
        userRepository.save(user);
        WorkspaceMember wm1 = WorkspaceMember.builder().workspace(w1).user(user).role(WorkspaceRole.MEMBER).build();
        WorkspaceMember wm2 = WorkspaceMember.builder().workspace(w2).user(user).role(WorkspaceRole.ADMIN).build();

        workspaceMemberRepository.saveAll(List.of(wm1, wm2));

        var result = mockMvc.perform(get("/api/v1/workspaces/{userId}/workspaces", 1))
                .andExpect(status().isOk())
                .andReturn();

        var workspaceDtos = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<WorkspaceDtoProjection>>() {});

        Map<Integer, WorkspaceDtoProjection> dtoMap = workspaceDtos.stream()
                .collect(Collectors.toMap(WorkspaceDtoProjection::id, Function.identity()));

        assertThat(dtoMap.get(w1Id).name()).isEqualTo("Workspace 1");
        assertThat(dtoMap.get(w1Id).role()).isEqualTo(WorkspaceRole.MEMBER);
        assertThat(dtoMap.get(w2Id).name()).isEqualTo("Workspace 2");
        assertThat(dtoMap.get(w2Id).role()).isEqualTo(WorkspaceRole.ADMIN);
    }

    @Test
    void removeWorkspaceMember_ShouldRemoveTargetMember() throws Exception {
        Workspace workspace = Workspace.builder().name("Workspace 1").build();
        workspaceRepository.save(workspace);

        User user1 = User.builder().id(10).build();
        User user2 = User.builder().id(20).build();
        userRepository.saveAll(List.of(user1, user2));

        WorkspaceMember wm1 = WorkspaceMember.builder().workspace(workspace).user(user1).role(WorkspaceRole.OWNER).build();
        WorkspaceMember wm2 = WorkspaceMember.builder().workspace(workspace).user(user2).role(WorkspaceRole.MEMBER).build();
        workspaceMemberRepository.saveAll(List.of(wm1, wm2));

        WorkspaceMemberRemoveRequest request = new WorkspaceMemberRemoveRequest(wm2.getId(), workspace.getId());

        mockMvc.perform(delete("/api/v1/workspaces/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", 10))
                .andExpect(status().isOk());

        assertThat(workspaceRepository.findById(wm2.getId()).isEmpty());
    }
}
