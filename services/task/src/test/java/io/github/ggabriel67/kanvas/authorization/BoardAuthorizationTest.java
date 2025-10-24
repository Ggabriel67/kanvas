package io.github.ggabriel67.kanvas.authorization;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("BoardAuthorization Unit Tests")
class BoardAuthorizationTest
{
    private BoardAuthorization boardAuthorization;

    @BeforeEach
    void setUp() {
        boardAuthorization = new BoardAuthorization();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContextWithRole(BoardRole role) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(role);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);
    }

    @Test
    void shouldReturnCurrentUserBoardRole_WhenPresentInSecurityContext() {
        mockSecurityContextWithRole(BoardRole.EDITOR);

        BoardRole result = boardAuthorization.getCurrentUserBoardRole();

        assertThat(result).isEqualTo(BoardRole.EDITOR);
    }

    @Test
    void shouldThrowException_WhenNoAuthenticationPresent() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> boardAuthorization.getCurrentUserBoardRole())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No user board role in SecurityContext");
    }

    @Test
    void shouldThrowException_WhenPrincipalIsNotBoardRole() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("not a board role");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);

        assertThatThrownBy(() -> boardAuthorization.getCurrentUserBoardRole())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No user board role in SecurityContext");
    }

    @Test
    void shouldReturnTrue_WhenUserIsAdminAndRequiredIsEditorOrViewer() {
        mockSecurityContextWithRole(BoardRole.ADMIN);

        assertThat(boardAuthorization.hasRole(BoardRole.ADMIN)).isTrue();
        assertThat(boardAuthorization.hasRole(BoardRole.EDITOR)).isTrue();
        assertThat(boardAuthorization.hasRole(BoardRole.VIEWER)).isTrue();
    }

    @Test
    void shouldReturnTrue_WhenUserIsEditorAndRequiredIsViewerOrEditor() {
        mockSecurityContextWithRole(BoardRole.EDITOR);

        assertThat(boardAuthorization.hasRole(BoardRole.ADMIN)).isFalse();
        assertThat(boardAuthorization.hasRole(BoardRole.EDITOR)).isTrue();
        assertThat(boardAuthorization.hasRole(BoardRole.VIEWER)).isTrue();
    }

    @Test
    void shouldReturnTrue_WhenUserIsViewerAndRequiredIsViewerOnly() {
        mockSecurityContextWithRole(BoardRole.VIEWER);

        assertThat(boardAuthorization.hasRole(BoardRole.ADMIN)).isFalse();
        assertThat(boardAuthorization.hasRole(BoardRole.EDITOR)).isFalse();
        assertThat(boardAuthorization.hasRole(BoardRole.VIEWER)).isTrue();
    }

    @Test
    void canEdit_ShouldReturnTrue_ForEditorAndAdmin() {
        mockSecurityContextWithRole(BoardRole.EDITOR);
        assertThat(boardAuthorization.canEdit()).isTrue();

        mockSecurityContextWithRole(BoardRole.ADMIN);
        assertThat(boardAuthorization.canEdit()).isTrue();
    }

    @Test
    void canEdit_ShouldReturnFalse_ForViewer() {
        mockSecurityContextWithRole(BoardRole.VIEWER);
        assertThat(boardAuthorization.canEdit()).isFalse();
    }

    @Test
    void canView_ShouldAlwaysReturnTrue() {
        mockSecurityContextWithRole(BoardRole.ADMIN);
        assertThat(boardAuthorization.canView()).isTrue();

        mockSecurityContextWithRole(BoardRole.EDITOR);
        assertThat(boardAuthorization.canView()).isTrue();

        mockSecurityContextWithRole(BoardRole.VIEWER);
        assertThat(boardAuthorization.canView()).isTrue();
    }
}
