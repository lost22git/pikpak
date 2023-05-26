package lost.pikpak.client.enums;

import io.avaje.jsonb.Json;

public enum UploadType {
    UNKNOWN("UNKNOWN"),
    UPLOAD_TYPE_URL("UPLOAD_TYPE_URL"),

    ;

    final String value;

    UploadType(String value) {
        this.value = value;
    }

    public static UploadType parse(String value) {
        for (UploadType uploadType : UploadType.values()) {
            if (uploadType.value.equals(value)) {
                return uploadType;
            }
        }
        return UploadType.UNKNOWN;
    }

    @Json.Value
    public String getValue() {
        return value;
    }
}
