package io.github.ggabriel67.kanvas.workspace;

import org.springframework.stereotype.Service;

@Service
public class WorkspaceMapper
{
    public WorkspaceDto toWorkspaceDto(Workspace workspace) {
        return new WorkspaceDto(
                workspace.getId(),
                workspace.getCreatedBy().getId(),
                workspace.getName(),
                workspace.getDescription(),
                workspace.getCreatedAt()
        );
    }
}
