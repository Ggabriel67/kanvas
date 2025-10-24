package io.github.ggabriel67.kanvas.authorization.workspace;

import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMember;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkspaceAuthorization Unit Tests")
class WorkspaceAuthorizationTest
{
    @Mock private WorkspaceMemberRepository workspaceMemberRepository;

    @InjectMocks
    private WorkspaceAuthorization workspaceAuth;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContextWithUserId(Integer userId) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userId);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);
    }

    @Test
    void getCurrentUserId_shouldReturnUserId() {
        mockSecurityContextWithUserId(1);

        Integer userId = workspaceAuth.getCurrentUserId();
        assertThat(userId).isEqualTo(1);
    }

    @Test
    void getCurrentUserId_shouldThrow_WhenNoUser() {
        assertThatThrownBy(() -> workspaceAuth.getCurrentUserId())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No user ID in SecurityContext");
    }

    @Test
    void hasRole_shouldReturnTrue_WhenUserIsOwner() {
        Integer workspaceId = 100;
        WorkspaceMember member = WorkspaceMember.builder().role(WorkspaceRole.OWNER).build();

        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(1, workspaceId))
                .thenReturn(Optional.of(member));

        assertThat(workspaceAuth.hasRole(1, workspaceId, WorkspaceRole.OWNER)).isTrue();
        assertThat(workspaceAuth.hasRole(1, workspaceId, WorkspaceRole.ADMIN)).isTrue();
        assertThat(workspaceAuth.hasRole(1, workspaceId, WorkspaceRole.MEMBER)).isTrue();
    }

    @Test
    void hasRole_shouldReturnFalse_WhenUserNotInWorkspace() {
        Integer workspaceId = 100;
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(1, workspaceId))
                .thenReturn(Optional.empty());

        assertThat(workspaceAuth.hasRole(1, workspaceId, WorkspaceRole.OWNER)).isFalse();
        assertThat(workspaceAuth.hasRole(1, workspaceId, WorkspaceRole.ADMIN)).isFalse();
        assertThat(workspaceAuth.hasRole(1, workspaceId, WorkspaceRole.MEMBER)).isFalse();
    }

    @Test
    void isAdminOrOwner_shouldReturnTrue_WhenUserIsAdminOrOwner() {
        mockSecurityContextWithUserId(1);

        WorkspaceMember member = WorkspaceMember.builder().role(WorkspaceRole.ADMIN).build();

        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(1, 100))
                .thenReturn(Optional.of(member));

        assertThat(workspaceAuth.isAdminOrOwner(100)).isTrue();
    }

    @Test
    void isMember_shouldReturnTrue_WhenUserIsAnyMember() {
        mockSecurityContextWithUserId(1);

        WorkspaceMember member = WorkspaceMember.builder().role(WorkspaceRole.MEMBER).build();

        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(1, 100))
                .thenReturn(Optional.of(member));

        assertThat(workspaceAuth.isMember(100)).isTrue();
    }

    @Test
    void canModerate_shouldReturnTrue_WhenCallerIsOwner() {
        mockSecurityContextWithUserId(1);

        WorkspaceMember caller = WorkspaceMember.builder().role(WorkspaceRole.OWNER).build();

        WorkspaceMember target = WorkspaceMember.builder().role(WorkspaceRole.ADMIN).build();

        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(1, 100))
                .thenReturn(Optional.of(caller));
        when(workspaceMemberRepository.findById(2))
                .thenReturn(Optional.of(target));

        assertThat(workspaceAuth.canModerate(100, 2)).isTrue();
    }

    @Test
    void canModerate_shouldReturnTrue_WhenCallerIsAdminAndTargetIsMember() {
        mockSecurityContextWithUserId(1);

        WorkspaceMember caller = WorkspaceMember.builder().role(WorkspaceRole.ADMIN).build();

        WorkspaceMember target = WorkspaceMember.builder().role(WorkspaceRole.MEMBER).build();

        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(1, 100))
                .thenReturn(Optional.of(caller));
        when(workspaceMemberRepository.findById(2))
                .thenReturn(Optional.of(target));

        assertThat(workspaceAuth.canModerate(100, 2)).isTrue();
    }

    @Test
    void canModerate_shouldReturnFalse_WhenCallerIsAdminAndTargetIsAdmin() {
        mockSecurityContextWithUserId(1);

        WorkspaceMember caller = WorkspaceMember.builder().role(WorkspaceRole.ADMIN).build();

        WorkspaceMember target = WorkspaceMember.builder().role(WorkspaceRole.ADMIN).build();

        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(1, 100))
                .thenReturn(Optional.of(caller));
        when(workspaceMemberRepository.findById(2))
                .thenReturn(Optional.of(target));

        assertThat(workspaceAuth.canModerate(100, 2)).isFalse();
    }

    @Test
    void canModerate_shouldReturnFalse_WhenCallerOrTargetNotFound() {
        mockSecurityContextWithUserId(1);

        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(1, 100))
                .thenReturn(Optional.empty());
        when(workspaceMemberRepository.findById(2))
                .thenReturn(Optional.empty());

        assertThat(workspaceAuth.canModerate(100, 2)).isFalse();
    }
}