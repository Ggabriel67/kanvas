package io.github.ggabriel67.kanvas.event;

public class Event<T>
{
    private String eventType;
    private T payload;

    public Event() {
    }

    public Event(String eventType, T payload) {
        this.eventType = eventType;
        this.payload = payload;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public String getEventType() {
        return eventType;
    }

    public T getPayload() {
        return payload;
    }
}
