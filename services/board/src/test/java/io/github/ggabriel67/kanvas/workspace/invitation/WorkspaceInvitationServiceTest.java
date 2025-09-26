package io.github.ggabriel67.kanvas.workspace.invitation;

import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceRole;
import io.github.ggabriel67.kanvas.event.invitation.InvitationCreated;
import io.github.ggabriel67.kanvas.exception.*;
import io.github.ggabriel67.kanvas.invitation.InvitationStatus;
import io.github.ggabriel67.kanvas.kafka.producer.InvitationEventProducer;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.user.UserService;
import io.github.ggabriel67.kanvas.workspace.Workspace;
import io.github.ggabriel67.kanvas.workspace.WorkspaceService;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMember;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberRepository;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkspaceInvitationService Unit Tests")
class WorkspaceInvitationServiceTest
{
    @Mock private WorkspaceInvitationRepository invitationRepository;
    @Mock private WorkspaceMemberRepository memberRepository;
    @Mock private UserService userService;
    @Mock private WorkspaceService workspaceService;
    @Mock private WorkspaceMemberService memberService;
    @Mock private InvitationEventProducer invitationEventProducer;

    @InjectMocks
    private WorkspaceInvitationService invitationService;

    @Nested
    class CreateInvitationTests {
        @Test
        void shouldThrowException_WhenUserIsAlreadyMember() {
            WorkspaceInvitationRequest request = new WorkspaceInvitationRequest(1, 2, 10, WorkspaceRole.MEMBER);

            when(memberRepository.findByUserIdAndWorkspaceId(2, 10))
                    .thenReturn(Optional.of(new WorkspaceMember()));

            assertThatThrownBy(() -> invitationService.createInvitation(request))
                    .isInstanceOf(MemberAlreadyExistsException.class)
                    .hasMessageContaining("This user is already a member of the workspace");

            verify(invitationRepository, never()).save(any());
            verify(invitationEventProducer, never()).sendInvitationCreated(any());
        }

        @Test
        void shouldThrowException_WhenPendingInvitationAlreadyExists() {
            WorkspaceInvitationRequest request = new WorkspaceInvitationRequest(1, 2, 10, WorkspaceRole.MEMBER);

            when(memberRepository.findByUserIdAndWorkspaceId(2, 10)).thenReturn(Optional.empty());

            WorkspaceInvitation existing = WorkspaceInvitation.builder()
                    .status(InvitationStatus.PENDING)
                    .build();
            when(invitationRepository.findByInviteeIdAndWorkspaceId(2, 10))
                    .thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> invitationService.createInvitation(request))
                    .isInstanceOf(InvitationPendingException.class)
                    .hasMessageContaining("Invitation already pending");

            verify(invitationRepository, never()).save(any());
            verify(invitationEventProducer, never()).sendInvitationCreated(any());
        }

        @Test
        void shouldSaveInvitationAndSendKafkaEvent_WhenValidRequest() {
            WorkspaceInvitationRequest request = new WorkspaceInvitationRequest(1, 2, 10, WorkspaceRole.MEMBER);

            when(memberRepository.findByUserIdAndWorkspaceId(2, 10)).thenReturn(Optional.empty());
            when(invitationRepository.findByInviteeIdAndWorkspaceId(2, 10)).thenReturn(Optional.empty());

            User inviter = User.builder().id(1).username("john").build();
            User invitee = User.builder().id(2).username("bob").build();
            Workspace workspace = Workspace.builder().id(10).name("Workspace").build();

            when(userService.getUserById(1)).thenReturn(inviter);
            when(userService.getUserById(2)).thenReturn(invitee);
            when(workspaceService.getWorkspaceById(10)).thenReturn(workspace);

            WorkspaceInvitation savedInvitation = WorkspaceInvitation.builder()
                    .id(99)
                    .inviter(inviter)
                    .invitee(invitee)
                    .workspace(workspace)
                    .status(InvitationStatus.PENDING)
                    .role(WorkspaceRole.MEMBER)
                    .expirationTime(Instant.now().plus(14, ChronoUnit.DAYS))
                    .build();
            when(invitationRepository.save(any())).thenReturn(savedInvitation);

            invitationService.createInvitation(request);

            verify(invitationRepository).save(any(WorkspaceInvitation.class));
            verify(invitationEventProducer).sendInvitationCreated(argThat(event ->
                    event.invitationId().equals(99) &&
                            event.inviteeId().equals(2) &&
                            event.inviterUsername().equals("john") &&
                            event.targetName().equals("Workspace")
            ));
        }
    }

    @Nested
    class AcceptInvitationTests {
        @Test
        void shouldThrowException_WhenInvitationDoesNotExist() {
            when(invitationRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> invitationService.acceptInvitation(99))
                    .isInstanceOf(InvitationNotFoundException.class)
                    .hasMessageContaining("Invitation does not exist");

            verifyNoInteractions(memberService, invitationEventProducer);
        }

        @Test
        void shouldAcceptInvitationAndSendKafkaEvent_WhenValid() {
            User invitee = User.builder().id(5).username("bob").build();
            Workspace workspace = Workspace.builder().id(42).name("Workspace").build();

            WorkspaceInvitation invitation = WorkspaceInvitation.builder()
                    .id(100)
                    .invitee(invitee)
                    .workspace(workspace)
                    .role(WorkspaceRole.MEMBER)
                    .status(InvitationStatus.PENDING)
                    .expirationTime(Instant.now().plus(14, ChronoUnit.DAYS))
                    .build();

            when(invitationRepository.findById(100)).thenReturn(Optional.of(invitation));

            invitationService.acceptInvitation(100);

            assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);

            verify(invitationRepository).save(invitation);
            verify(memberService).addWorkspaceMember(workspace, invitee, WorkspaceRole.MEMBER);
            verify(invitationEventProducer).sendInvitationUpdated(argThat(event ->
                    event.invitationId().equals(100) &&
                            event.inviteeId().equals(5) &&
                            event.status().equals(InvitationStatus.ACCEPTED.name())
            ));
        }
    }

    @Nested
    class DeclineInvitationTests {
        @Test
        void shouldThrowException_WhenInvitationDoesNotExist() {
            when(invitationRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> invitationService.declineInvitation(99))
                    .isInstanceOf(InvitationNotFoundException.class)
                    .hasMessageContaining("Invitation does not exist");

            verifyNoInteractions(memberService, invitationEventProducer);
        }

        @Test
        void shouldDeclineInvitationAndSendKafkaEvent_WhenValid() {
            User invitee = User.builder().id(5).username("bob").build();
            Workspace workspace = Workspace.builder().id(42).name("Workspace").build();

            WorkspaceInvitation invitation = WorkspaceInvitation.builder()
                    .id(100)
                    .invitee(invitee)
                    .workspace(workspace)
                    .role(WorkspaceRole.MEMBER)
                    .status(InvitationStatus.PENDING)
                    .expirationTime(Instant.now().plus(14, ChronoUnit.DAYS))
                    .build();

            when(invitationRepository.findById(100)).thenReturn(Optional.of(invitation));

            invitationService.declineInvitation(100);

            assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.DECLINED);

            verify(invitationRepository).save(invitation);
            verify(invitationEventProducer).sendInvitationUpdated(argThat(event ->
                    event.invitationId().equals(100) &&
                            event.inviteeId().equals(5) &&
                            event.status().equals(InvitationStatus.DECLINED.name())
            ));
        }
    }

    @Test
    void shouldThrowExceptionAndSendKafkaEvent_WhenInvitationIsExpired() {
        User invitee = User.builder().id(5).username("bob").build();
        Workspace workspace = Workspace.builder().id(42).name("Workspace").build();

        WorkspaceInvitation invitation = WorkspaceInvitation.builder()
                .id(100)
                .invitee(invitee)
                .workspace(workspace)
                .role(WorkspaceRole.MEMBER)
                .status(InvitationStatus.PENDING)
                .expirationTime(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();

        when(invitationRepository.findById(100)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.acceptInvitation(100))
                .isInstanceOf(InvitationExpiredException.class)
                .hasMessageContaining("Invitation has expired");

        verify(invitationEventProducer).sendInvitationUpdated(argThat(event ->
                event.invitationId().equals(100) &&
                        event.inviteeId().equals(5) &&
                        event.status().equals(InvitationStatus.EXPIRED.name())
        ));
    }

    @Test
    void shouldThrowException_WhenInvitationIsNotPending() {
        User invitee = User.builder().id(5).username("bob").build();
        Workspace workspace = Workspace.builder().id(42).name("Workspace").build();

        WorkspaceInvitation invitation = WorkspaceInvitation.builder()
                .id(100)
                .invitee(invitee)
                .workspace(workspace)
                .role(WorkspaceRole.MEMBER)
                .status(InvitationStatus.DECLINED)
                .expirationTime(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();

        when(invitationRepository.findById(100)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.acceptInvitation(100))
                .isInstanceOf(InvalidStatusException.class)
                .hasMessageContaining("Invitation is not pending");
    }
}
