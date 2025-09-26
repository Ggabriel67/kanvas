package io.github.ggabriel67.kanvas.workspace;

import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceAuthorization;
import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceRole;
import io.github.ggabriel67.kanvas.board.BoardRepository;
import io.github.ggabriel67.kanvas.board.BoardService;
import io.github.ggabriel67.kanvas.exception.UserNotFoundException;
import io.github.ggabriel67.kanvas.exception.WorkspaceNotFoundException;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.user.UserService;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMember;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkspaceService Unit Tests")
class WorkspaceServiceTest
{
    @Mock private WorkspaceAuthorization workspaceAuth;
    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private WorkspaceMemberRepository workspaceMemberRepository;
    @Mock private UserService userService;
    @Mock private BoardService boardService;
    @Mock private BoardRepository boardRepository;

    @InjectMocks
    private WorkspaceService workspaceService;

    private WorkspaceRequest workspaceRequest;

    @Nested
    class CreateWorkspaceTests {
        private User creator;

        @BeforeEach
        void setUp() {
            creator = User.builder().id(40).build();

            workspaceRequest = new WorkspaceRequest(creator.getId(), "My Workspace", null);
        }

        @Test
        void shouldPersistWorkspaceAndOwnerMember() {
            when(userService.getUserById(creator.getId())).thenReturn(creator);
            Workspace persistedWorkspace = Workspace.builder().id(100).name(workspaceRequest.name()).build();

            when(workspaceRepository.save(any(Workspace.class))).thenReturn(persistedWorkspace);
            Integer workspaceId = workspaceService.createWorkspace(workspaceRequest);
            assertThat(workspaceId).isEqualTo(100);

            verify(userService).getUserById(creator.getId());

            ArgumentCaptor<Workspace> workspaceCaptor = ArgumentCaptor.forClass(Workspace.class);
            verify(workspaceRepository).save(workspaceCaptor.capture());
            Workspace savedWorkspace = workspaceCaptor.getValue();
            assertThat(savedWorkspace.getName()).isEqualTo("My Workspace");

            ArgumentCaptor<WorkspaceMember> memberCaptor = ArgumentCaptor.forClass(WorkspaceMember.class);
            verify(workspaceMemberRepository).save(memberCaptor.capture());
            WorkspaceMember savedMember = memberCaptor.getValue();
            assertThat(savedMember.getUser()).isEqualTo(creator);
            assertThat(savedMember.getWorkspace()).isEqualTo(persistedWorkspace);
            assertThat(savedMember.getRole()).isEqualTo(WorkspaceRole.OWNER);
        }

        @Test
        void shouldThrowException_WhenUserDoesNotExist() {
            when(userService.getUserById(any()))
                    .thenThrow(new UserNotFoundException("User not found"));

            assertThatThrownBy(() -> workspaceService.createWorkspace(workspaceRequest))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(workspaceRepository, never()).save(any());
            verify(workspaceMemberRepository, never()).save(any());
        }
    }

    @Nested
    class GetWorkspaceByIdTests {
        @Test
        void shouldReturnWorkspace() {
            Workspace existingWorkspace = Workspace.builder().id(100).build();
            when(workspaceRepository.findById(100))
                    .thenReturn(Optional.of(existingWorkspace));

            assertThat(workspaceService.getWorkspaceById(100)).isEqualTo(existingWorkspace);
        }

        @Test
        void shouldThrowException_WhenWorkspaceDoesNotExist() {
            when(workspaceRepository.findById(999))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> workspaceService.getWorkspaceById(999))
                    .isInstanceOf(WorkspaceNotFoundException.class)
                    .hasMessageContaining("Workspace not found");
        }
    }

    @Nested
    class UpdateWorkspaceTests {
        @Test
        void shouldUpdateWorkspace_WhenWorkspaceExists() {
            Workspace existing = Workspace.builder().id(100).name("Old name").description("Old description").build();

            WorkspaceRequest updateRequest = new WorkspaceRequest(1, "New name", "New description");

            when(workspaceRepository.findById(100)).thenReturn(Optional.of(existing));

            workspaceService.updateWorkspace(updateRequest, 100);

            assertThat(existing.getName()).isEqualTo("New name");
            assertThat(existing.getDescription()).isEqualTo("New description");
            verify(workspaceRepository).save(existing);
        }

        @Test
        void shouldThrowException_WhenWorkspaceNotFound() {
            WorkspaceRequest updateRequest = new WorkspaceRequest(1, "Name", "Description");
            when(workspaceRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> workspaceService.updateWorkspace(updateRequest, 999))
                    .isInstanceOf(WorkspaceNotFoundException.class)
                    .hasMessageContaining("Workspace not found");

            verify(workspaceRepository, never()).save(any());
        }
    }

    @Nested
    class GetAllUserWorkspacesTests {
        @Test
        void shouldReturnAllWorkspacesForUser() {
            User user = User.builder().id(1).build();
            when(userService.getUserById(1)).thenReturn(user);

            WorkspaceDtoProjection w1 = mock(WorkspaceDtoProjection.class);
            WorkspaceDtoProjection w2 = mock(WorkspaceDtoProjection.class);

            List<WorkspaceDtoProjection> workspaces = List.of(w1, w2);
            when(workspaceRepository.findWorkspacesByUser(user)).thenReturn(workspaces);

            List<WorkspaceDtoProjection> result = workspaceService.getAllUserWorkspaces(1);

            assertThat(result.size() == 2 && result.containsAll(List.of(w1, w2)));

            verify(userService).getUserById(1);
            verify(workspaceRepository).findWorkspacesByUser(user);
        }

        @Test
        void shouldThrowException_WhenUserNotFound() {
            when(userService.getUserById(any()))
                    .thenThrow(new UserNotFoundException("User not found"));

            assertThatThrownBy(() -> workspaceService.getAllUserWorkspaces(999))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(workspaceRepository, never()).findWorkspacesByUser(any());
        }
    }

    @Nested
    class GetGuestWorkspacesTests {
        @Test
        void shouldReturnGuestWorkspaces() {
            User user = User.builder().id(1).build();
            when(userService.getUserById(1)).thenReturn(user);

            List<WorkspaceBoardFlatDto> flatBoards = List.of(
                    new WorkspaceBoardFlatDto(10, "Workspace A", 100, "Board 1"),
                    new WorkspaceBoardFlatDto(10, "Workspace A", 101, "Board 2"),
                    new WorkspaceBoardFlatDto(20, "Workspace B", 200, "Board 3")
            );

            when(boardRepository.findGuestWorkspacesBoardData(user)).thenReturn(flatBoards);

            List<GuestWorkspaceDto> result = workspaceService.getGuestWorkspaces(1);
            assertThat(result.size() == 2);

            GuestWorkspaceDto wsA = result.stream()
                    .filter(ws -> ws.workspaceId().equals(10))
                    .findFirst()
                    .orElseThrow();

            assertThat(wsA.workspaceName()).isEqualTo("Workspace A");

            GuestWorkspaceDto wsB = result.stream()
                    .filter(ws -> ws.workspaceId().equals(20))
                    .findFirst()
                    .orElseThrow();

            assertThat(wsB.workspaceName()).isEqualTo("Workspace B");

            verify(userService).getUserById(1);
            verify(boardRepository).findGuestWorkspacesBoardData(user);
        }

        @Test
        void shouldThrowException_WhenUserNotFound() {
            when(userService.getUserById(any()))
                    .thenThrow(new UserNotFoundException("User not found"));

            assertThatThrownBy(() -> workspaceService.getGuestWorkspaces(999))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(boardRepository, never()).findGuestWorkspacesBoardData(any());
        }
    }
}
