package lost.pikpak.client.enums;

import io.avaje.jsonb.Json;

public enum ThumbnailSize {
    UNKNOWN("UNKNOWN"),
    SIZE_MEDIUM("SIZE_MEDIUM"),
    ;

    final String value;

    ThumbnailSize(String value) {
        this.value = value;
    }

    public static ThumbnailSize parse(String value) {
        for (ThumbnailSize thumbnailSize : ThumbnailSize.values()) {
            if (thumbnailSize.value.equals(value)) {
                return thumbnailSize;
            }
        }
        return ThumbnailSize.UNKNOWN;
    }

    @Json.Value
    public String getValue() {
        return value;
    }
}
