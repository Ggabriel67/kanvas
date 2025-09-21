package io.github.ggabriel67.kanvas.message.board;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BoardMessage<T>
{
    private BoardMessageType type;
    private T payload;
}
