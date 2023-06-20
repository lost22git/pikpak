package lost.pikpak.client.enums;

import io.avaje.jsonb.Json;

public enum AuditStatus {
    UNKNOWN("UNKNOWN"),
    STATUS_OK("STATUS_OK"),
    ;

    final String value;

    AuditStatus(String value) {
        this.value = value;
    }

    public static AuditStatus parse(String value) {
        for (AuditStatus auditStatus : AuditStatus.values()) {
            if (auditStatus.value.equals(value)) {
                return auditStatus;
            }
        }
        return AuditStatus.UNKNOWN;
    }

    @Json.Value
    public String getValue() {
        return value;
    }
}
