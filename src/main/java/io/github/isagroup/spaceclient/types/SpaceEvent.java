package io.github.isagroup.spaceclient.types;

/**
 * Space events that can be listened to
 */
public enum SpaceEvent {
    SYNCHRONIZED("synchronized"),
    PRICING_CREATED("pricing_created"),
    PRICING_ARCHIVED("pricing_archived"),
    PRICING_ACTIVED("pricing_actived"),
    SERVICE_DISABLED("service_disabled"),
    ERROR("error");

    private final String eventName;

    SpaceEvent(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }

    public static SpaceEvent fromString(String text) {
        for (SpaceEvent event : SpaceEvent.values()) {
            if (event.eventName.equalsIgnoreCase(text)) {
                return event;
            }
        }
        return null;
    }
}
