package lost.pikpak.client.enums;

public enum HttpHeader {
    CONTENT_TYPE("content-type"),
    USER_AGENT("user-agent"),
    REFERER("referer"),
    AUTHORIZATION("Authorization"),
    CAPTCHA_TOKEN("x-captcha-token"),
    DEVICE_ID("x-device-id"),
    DEVICE_MODEL("x-device-model"),
    DEVICE_NAME("x-device-name"),
    DEVICE_SIGN("x-device-sign"),
    CLIENT_ID("x-client-id"),
    CLIENT_VERSION("x-client-version"),
    NETWORK_TYPE("x-net-work-type"),
    OS_VERSION("x-os-version"),
    PLATFORM_VERSION("x-platform-version"),
    PROTOCOL_VERSION("x-protocol-version"),
    SDK_VERSION("x-sdk-version"),
    PROVIDER_NAME("x-provider-name"),

    ;

    final String value;

    HttpHeader(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
