package io.github.ggabriel67.kanvas.notification;

import java.util.List;

public record ReadNotificationsRequest(
        List<Integer> ids
) {
}
