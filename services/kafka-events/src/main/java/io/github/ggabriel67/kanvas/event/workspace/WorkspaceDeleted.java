package io.github.ggabriel67.kanvas.event.workspace;

import java.util.List;

public record WorkspaceDeleted(
        List<Integer> boardIds
) {
}
