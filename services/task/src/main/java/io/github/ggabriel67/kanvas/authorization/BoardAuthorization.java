package io.github.ggabriel67.kanvas.authorization;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("boardAuth")
public class BoardAuthorization
{
    public BoardRole getCurrentUserBoardRole() throws IllegalStateException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof BoardRole boardRole) {
            return  boardRole;
        }
        throw new IllegalStateException("No user board role in SecurityContext");
    }

    public boolean hasRole(BoardRole requiredRole) {
        BoardRole userBordRole = getCurrentUserBoardRole();
        return switch (requiredRole) {
            case ADMIN -> userBordRole == BoardRole.ADMIN;
            case EDITOR -> userBordRole == BoardRole.ADMIN || userBordRole == BoardRole.EDITOR;
            case VIEWER -> true;
        };
    }

    public boolean canEdit() {
        return hasRole(BoardRole.EDITOR);
    }

    public boolean canView() {
        return hasRole(BoardRole.VIEWER);
    }
}
