package io.github.ggabriel67.kanvas;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceRole;
import io.github.ggabriel67.kanvas.invitation.InvitationStatus;
import io.github.ggabriel67.kanvas.kafka.producer.BoardEventProducer;
import io.github.ggabriel67.kanvas.kafka.producer.InvitationEventProducer;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.user.UserRepository;
import io.github.ggabriel67.kanvas.workspace.Workspace;
import io.github.ggabriel67.kanvas.workspace.WorkspaceRepository;
import io.github.ggabriel67.kanvas.workspace.invitation.WorkspaceInvitation;
import io.github.ggabriel67.kanvas.workspace.invitation.WorkspaceInvitationRepository;
import io.github.ggabriel67.kanvas.workspace.invitation.WorkspaceInvitationRequest;
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
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class InvitationControllerIntegrationTest
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
    private WorkspaceInvitationRepository workspaceInvitationRepository;

    @BeforeEach
    void cleanDatabase() {
        workspaceInvitationRepository.deleteAll();
        workspaceMemberRepository.deleteAll();
        userRepository.deleteAll();
        workspaceRepository.deleteAll();
    }

    @Test
    void createWorkspaceInvitation_ShouldSuccessfullyCreateInvitation() throws Exception {
        Integer userId = 1;
        User user1 = User.builder().id(userId).build();
        User user2 = User.builder().id(20).build();
        userRepository.saveAll(List.of(user1, user2));

        Workspace workspace = Workspace.builder().name("Workspace 1").build();
        workspaceRepository.save(workspace);

        WorkspaceMember workspaceMember = WorkspaceMember.builder().workspace(workspace).user(user1).role(WorkspaceRole.ADMIN).build();
        workspaceMemberRepository.save(workspaceMember);

        WorkspaceInvitationRequest request = new WorkspaceInvitationRequest(
            userId, user2.getId(), workspace.getId(), WorkspaceRole.MEMBER
        );

        mockMvc.perform(post("/api/v1/invitations/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", userId))
                .andExpect(status().isCreated());

        var invitation = workspaceInvitationRepository.findPendingByInviteeIdAndWorkspaceId(
                user2.getId(), workspace.getId(), InvitationStatus.PENDING);

        assertThat(invitation.isPresent());
    }

    @Test
    void createWorkspaceInvitation_ShouldReturnBadRequestWhenUserIsAlreadyMember() throws Exception {
        Integer userId = 1;
        User user1 = User.builder().id(userId).build();
        User user2 = User.builder().id(20).build();
        userRepository.saveAll(List.of(user1, user2));

        Workspace workspace = Workspace.builder().name("Workspace 1").build();
        workspaceRepository.save(workspace);

        WorkspaceMember wm1 = WorkspaceMember.builder().workspace(workspace).user(user1).role(WorkspaceRole.ADMIN).build();
        WorkspaceMember wm2 = WorkspaceMember.builder().workspace(workspace).user(user2).role(WorkspaceRole.MEMBER).build();
        workspaceMemberRepository.saveAll(List.of(wm1, wm2));

        WorkspaceInvitationRequest request = new WorkspaceInvitationRequest(
                userId, user2.getId(), workspace.getId(), WorkspaceRole.MEMBER
        );

        mockMvc.perform(post("/api/v1/invitations/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-User-Id", userId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void acceptWorkspaceInvitation_ShouldCreateNewWorkspaceMember() throws Exception {
        Integer userId = 1;
        User user1 = User.builder().id(userId).build();
        User user2 = User.builder().id(20).build();
        userRepository.saveAll(List.of(user1, user2));

        Workspace workspace = Workspace.builder().name("Workspace 1").build();
        workspaceRepository.save(workspace);

        WorkspaceMember workspaceMember = WorkspaceMember.builder().workspace(workspace).user(user1).role(WorkspaceRole.ADMIN).build();
        workspaceMemberRepository.save(workspaceMember);

        WorkspaceInvitation existingInvitation = WorkspaceInvitation.builder().inviter(user1).invitee(user2)
                .workspace(workspace).role(WorkspaceRole.MEMBER).status(InvitationStatus.PENDING)
                .expirationTime(Instant.now().plus(14, ChronoUnit.DAYS)).build();
        workspaceInvitationRepository.save(existingInvitation);

        mockMvc.perform(post("/api/v1/invitations/workspaces/{invitationId}/accept", existingInvitation.getId()))
                .andExpect(status().isCreated());

        var savedMember = workspaceMemberRepository.findByUserIdAndWorkspaceId(user2.getId(), workspace.getId());

        var updatedInvitation = workspaceInvitationRepository.findById(existingInvitation.getId()).get();

        assertThat(updatedInvitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);

        assertThat(savedMember.isPresent());
        assertThat(savedMember.get().getRole()).isEqualTo(WorkspaceRole.MEMBER);
    }

    @Test
    void acceptWorkspaceInvitation_ShouldFailWhenInvitationExpired() throws Exception {
        Integer userId = 1;
        User user1 = User.builder().id(userId).build();
        User user2 = User.builder().id(20).build();
        userRepository.saveAll(List.of(user1, user2));

        Workspace workspace = Workspace.builder().name("Workspace 1").build();
        workspaceRepository.save(workspace);

        WorkspaceMember workspaceMember = WorkspaceMember.builder().workspace(workspace).user(user1).role(WorkspaceRole.ADMIN).build();
        workspaceMemberRepository.save(workspaceMember);

        WorkspaceInvitation existingInvitation = WorkspaceInvitation.builder().inviter(user1).invitee(user2)
                .workspace(workspace).role(WorkspaceRole.MEMBER).status(InvitationStatus.PENDING)
                .expirationTime(Instant.now().minus(1, ChronoUnit.DAYS)).build();
        workspaceInvitationRepository.save(existingInvitation);

        mockMvc.perform(post("/api/v1/invitations/workspaces/{invitationId}/accept", existingInvitation.getId()))
                .andExpect(status().isBadRequest());

        var savedMember = workspaceMemberRepository.findByUserIdAndWorkspaceId(user2.getId(), workspace.getId());

        var updatedInvitation = workspaceInvitationRepository.findById(existingInvitation.getId()).get();

        assertThat(updatedInvitation.getStatus()).isEqualTo(InvitationStatus.EXPIRED);

        assertThat(savedMember.isEmpty());
    }
}
