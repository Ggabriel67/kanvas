package io.github.ggabriel67.kanvas.workspace;

import io.github.ggabriel67.kanvas.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Integer>
{
    @Query("""
SELECT new io.github.ggabriel67.kanvas.workspace.WorkspaceDtoProjection(
    w.id, w.name
)
FROM Workspace w
JOIN WorkspaceMember wm ON wm.workspace = w
WHERE wm.user = :user
""")
    List<WorkspaceDtoProjection> findWorkspacesByUser(@Param("user") User user);
}
