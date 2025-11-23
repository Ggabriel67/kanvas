package io.github.ggabriel67.kanvas.workspace.member;

import io.github.ggabriel67.kanvas.board.Board;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.workspace.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Integer>
{
    List<WorkspaceMember> findAllByWorkspace(@Param("workspace") Workspace workspace);

    Optional<WorkspaceMember> findByUserIdAndWorkspaceId(Integer userId, Integer workspaceId);

    @Modifying(clearAutomatically = true)
    @Query("""
DELETE FROM WorkspaceMember wm WHERE wm.workspace = :workspace
""")
    void deleteAllByWorkspace(@Param("workspace") Workspace workspace);
}
