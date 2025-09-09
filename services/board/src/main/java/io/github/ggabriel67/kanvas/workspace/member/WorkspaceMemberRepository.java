package io.github.ggabriel67.kanvas.workspace.member;

import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.workspace.Workspace;
import io.github.ggabriel67.kanvas.workspace.WorkspaceDtoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Integer>
{
    @Query("""
SELECT new io.github.ggabriel67.kanvas.workspace.WorkspaceDtoProjection(
    w.id, w.name
)
FROM WorkspaceMember wm
JOIN wm.workspace w
WHERE wm.user = :user
""")
    List<WorkspaceDtoProjection> findWorkspacesByUser(@Param("user") User user);

    List<WorkspaceMember> findAllByWorkspace(@Param("workspace") Workspace workspace);
}
