package lost.pikpak.client.enums;

import io.avaje.jsonb.Json;

public enum SortOrder {

    UNKNOWN("UNKNOWN"),
    MODIFY_TIME_DESC("MODIFY_TIME_DESC"),
    ;

    final String value;

    SortOrder(String value) {
        this.value = value;
    }

    public static SortOrder parse(String value) {
        for (SortOrder sortOrder : SortOrder.values()) {
            if (sortOrder.value.equals(value)) {
                return sortOrder;
            }
        }
        return SortOrder.UNKNOWN;
    }

    @Json.Value
    public String getValue() {
        return value;
    }
}
