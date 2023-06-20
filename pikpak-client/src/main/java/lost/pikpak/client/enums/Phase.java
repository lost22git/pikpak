package lost.pikpak.client.enums;

import io.avaje.jsonb.Json;

public enum Phase {
    UNKNOWN("UNKNOWN"),
    PHASE_TYPE_PENDING("PHASE_TYPE_PENDING"),
    PHASE_TYPE_COMPLETE("PHASE_TYPE_COMPLETE"),
    ;

    final String value;

    Phase(String value) {
        this.value = value;
    }

    public static Phase parse(String value) {
        for (Phase phase : Phase.values()) {
            if (phase.value.equals(value)) {
                return phase;
            }
        }
        return Phase.UNKNOWN;
    }

    @Json.Value
    public String getValue() {
        return value;
    }
}
