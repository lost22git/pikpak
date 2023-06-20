package lost.pikpak.client.enums;

import io.avaje.jsonb.Json;

public enum FileCategory {
    UNKNOWN("UNKNOWN"),
    VIDEO("VIDEO"),
    OTHER("OTHER"),
    ;

    final String value;

    FileCategory(String value) {
        this.value = value;
    }

    public static FileCategory parse(String value) {
        for (FileCategory fileCategory : FileCategory.values()) {
            if (fileCategory.value.equals(value)) {
                return fileCategory;
            }
        }
        return FileCategory.UNKNOWN;
    }

    @Json.Value
    public String getValue() {
        return value;
    }
}
