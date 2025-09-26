package io.github.ggabriel67.kanvas.workspace.member;

import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceRole;
import io.github.ggabriel67.kanvas.exception.UserNotFoundException;
import io.github.ggabriel67.kanvas.exception.WorkspaceNotFoundException;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.workspace.Workspace;
import io.github.ggabriel67.kanvas.workspace.WorkspaceRepository;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkspaceMemberService Unit Tests")
class WorkspaceMemberServiceTest
{
    @Mock private WorkspaceMemberRepository memberRepository;
    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private WorkspaceMemberMapper memberMapper;

    @InjectMocks
    private WorkspaceMemberService workspaceMemberService;

    @Test
    void addWorkspaceMember_shouldPersistWorkspaceMember() {
        User user = User.builder().id(1).build();
        Workspace workspace = Workspace.builder().id(100).build();

        workspaceMemberService.addWorkspaceMember(workspace, user, any());

        ArgumentCaptor<WorkspaceMember> memberCaptor = ArgumentCaptor.forClass(WorkspaceMember.class);
        verify(memberRepository).save(memberCaptor.capture());
        WorkspaceMember savedMember = memberCaptor.getValue();

        assertThat(savedMember.getUser().equals(user));
        assertThat(savedMember.getWorkspace().equals(workspace));
    }

    @Nested
    class ChangeMemberRoleTests {
        @Test
        void shouldChangeMemberRole_WhenWorkspaceMemberExists() {
            WorkspaceRoleChangeRequest request = new WorkspaceRoleChangeRequest(5, 10, WorkspaceRole.ADMIN);

            Workspace workspace = Workspace.builder().id(10).build();
            WorkspaceMember member = WorkspaceMember.builder().id(5).role(WorkspaceRole.MEMBER).build();

            when(workspaceRepository.findById(10)).thenReturn(Optional.of(workspace));
            when(memberRepository.findById(5)).thenReturn(Optional.of(member));

            workspaceMemberService.changeMemberRole(request);

            assertThat(member.getRole().equals(WorkspaceRole.ADMIN));
            verify(memberRepository).save(member);
        }

        @Test
        void shouldThrowException_WhenWorkspaceDoesNotExist() {
            WorkspaceRoleChangeRequest request = new WorkspaceRoleChangeRequest(5, 999, WorkspaceRole.ADMIN);

            when(workspaceRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> workspaceMemberService.changeMemberRole(request))
                    .isInstanceOf(WorkspaceNotFoundException.class)
                    .hasMessageContaining("Workspace not found");

            verify(memberRepository, never()).save(any());
        }

        @Test
        void shouldThrowException_WhenMemberDoesNotExist() {
            WorkspaceRoleChangeRequest request = new WorkspaceRoleChangeRequest(99, 10, WorkspaceRole.ADMIN);

            when(workspaceRepository.findById(10)).thenReturn(Optional.of(new Workspace()));
            when(memberRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> workspaceMemberService.changeMemberRole(request))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(memberRepository, never()).save(any());
        }
    }

    @Nested
    class RemoveMemberTests {
        @Test
        void shouldRemoveMember_WhenMemberExists() {
            WorkspaceMemberRemoveRequest request = new WorkspaceMemberRemoveRequest(5, 10);
            WorkspaceMember member = WorkspaceMember.builder().id(5).role(WorkspaceRole.MEMBER).build();
            when(memberRepository.findById(5)).thenReturn(Optional.of(member));

            workspaceMemberService.removeMember(request);

            verify(memberRepository).delete(member);
        }

        @Test
        void shouldThrowException_WhenMemberDoesNotExist() {
            WorkspaceMemberRemoveRequest request = new WorkspaceMemberRemoveRequest(99, 10);

            when(memberRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> workspaceMemberService.removeMember(request))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(memberRepository, never()).delete(any());
        }
    }

    @Nested
    class GetAllWorkspaceMembersTests {
        @Test
        void shouldReturnAllWorkspaceMembers_WhenWorkspaceExists() {
            Workspace workspace = Workspace.builder().id(10).build();
            when(workspaceRepository.findById(10)).thenReturn(Optional.of(workspace));

            WorkspaceMember member1 = mock(WorkspaceMember.class);
            WorkspaceMember member2 = mock(WorkspaceMember.class);
            List<WorkspaceMember> members = List.of(member1, member2);
            when(memberRepository.findAllByWorkspace(workspace)).thenReturn(members);

            WorkspaceMemberDto dto1 = mock(WorkspaceMemberDto.class);
            WorkspaceMemberDto dto2 = mock(WorkspaceMemberDto.class);

            when(memberMapper.toWorkspaceMemberDto(member1)).thenReturn(dto1);
            when(memberMapper.toWorkspaceMemberDto(member2)).thenReturn(dto2);

            List<WorkspaceMemberDto> result = workspaceMemberService.getAllWorkspaceMembers(10);

            assertThat(result.containsAll(List.of(dto1, dto2)));

            verify(workspaceRepository).findById(10);
            verify(memberRepository).findAllByWorkspace(workspace);
            verify(memberMapper).toWorkspaceMemberDto(member1);
            verify(memberMapper).toWorkspaceMemberDto(member2);
        }

        @Test
        void shouldThrowException_WhenWorkspaceDoesNotExist() {
            when(workspaceRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> workspaceMemberService.getAllWorkspaceMembers(999))
                    .isInstanceOf(WorkspaceNotFoundException.class)
                    .hasMessageContaining("Workspace not found");

            verify(memberRepository, never()).findAllByWorkspace(any());
            verify(memberMapper, never()).toWorkspaceMemberDto(any());
        }
    }
}
