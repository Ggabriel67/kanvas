package io.github.ggabriel67.kanvas.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer>
{
    @Query(value = """
SELECT * FROM notifications WHERE user_id = :userId AND payload->>'invitationId' = :invitationId 
""", nativeQuery = true)
    Notification findByInvitationIdAndUserId(@Param("invitationId") Integer invitationId,
                                    @Param("userId") Integer userId);
}
