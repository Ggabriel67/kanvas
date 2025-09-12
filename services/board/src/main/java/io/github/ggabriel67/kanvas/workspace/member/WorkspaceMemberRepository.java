package io.github.ggabriel67.kanvas.workspace.member;

import io.github.ggabriel67.kanvas.workspace.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Integer>
{
    List<WorkspaceMember> findAllByWorkspace(@Param("workspace") Workspace workspace);

    Optional<WorkspaceMember> findByUserIdAndWorkspaceId(Integer userId, Integer workspaceId);
}
