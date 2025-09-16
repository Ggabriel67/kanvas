package io.github.ggabriel67.kanvas.kafka.producer.board;

public record MemberRemoved(
        Integer memberId,
        Integer userId,
        Integer boardId
) {
}
