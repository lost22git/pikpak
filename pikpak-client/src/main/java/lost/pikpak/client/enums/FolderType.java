package lost.pikpak.client.enums;

import io.avaje.jsonb.Json;

public enum FolderType {
    UNKNOWN("UNKNOWN"),
    DOWNLOAD("DOWNLOAD"),
    NORMAL("NORMAL"),
    ;

    final String value;

    FolderType(String value) {
        this.value = value;
    }

    public static FolderType parse(String value) {
        for (FolderType folderType : FolderType.values()) {
            if (folderType.value.equals(value)) {
                return folderType;
            }
        }
        return FolderType.UNKNOWN;
    }

    @Json.Value
    public String getValue() {
        return value;
    }
}
