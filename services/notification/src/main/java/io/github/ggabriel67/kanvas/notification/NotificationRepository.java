package io.github.ggabriel67.kanvas.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer>
{
    @Query(value = """
SELECT * FROM notifications WHERE user_id = :userId AND (payload->>'invitationId')::int = :invitationId AND (payload->>'scope' = :scope)
""", nativeQuery = true)
    Notification findByInvitationIdAndUserId(
            @Param("invitationId") Integer invitationId,
            @Param("userId") Integer userId,
            @Param("scope") String scope);

    @Query("""
SELECT n FROM Notification n
WHERE n.userId = :userId AND n.status != :status
ORDER BY n.sentAt DESC
""")
    List<Notification> findValidNotificationsForUser(@Param("userId") Integer userId,
                                                     @Param("status") NotificationStatus status);

    @Query("""
SELECT COUNT (*) FROM Notification  n
WHERE n.userId = :userId and n.status = :status
""")
    Integer getUnreadNotificationsCount(@Param("userId") Integer userId, @Param("status") NotificationStatus status);

    List<Notification> findByIdIn(List<Integer> ids);
}
