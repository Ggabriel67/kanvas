package io.github.ggabriel67.kanvas.kafka.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Event<T>
{
    private String eventType;
    private T payload;
}
