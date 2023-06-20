package lost.pikpak.client.enums;

import io.avaje.jsonb.Json;

public enum Kind {
    UNKNOWN("Unknown"),
    FOLDER("drive#folder"),
    FILE_LIST("drive#fileList"),

    FILE("drive#file"),

    URL("upload#url"),

    TASK("drive#task"),
    ;

    final String value;

    Kind(String value) {
        this.value = value;
    }

    public static Kind parse(String value) {
        for (Kind kind : Kind.values()) {
            if (kind.value.equals(value)) {
                return kind;
            }
        }
        return Kind.UNKNOWN;
    }

    @Json.Value
    public String getValue() {
        return value;
    }
}
