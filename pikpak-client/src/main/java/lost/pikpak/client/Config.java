package lost.pikpak.client;

import io.avaje.jsonb.Json;
import lost.pikpak.client.token.Token;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@Json(naming = Json.Naming.LowerUnderscore)
public record Config(
    Data data,
    Map<String, User> users
) {
    public Config {
        // set default value
        if (data == null) data = new Data(Data.createDefault());

        // make inner users mutable
        if (users == null) users = new HashMap<>();
        else users = new HashMap<>(users);

        // set data parent
        // config.user[].data.parent=config.data
        for (var u : users.values()) {
            u.data.parent = data;
        }
    }

    public static Config createDefault() {
        return new Config(null, null);
    }

    /**
     * return Immutable map view
     *
     * @return
     */
    public Map<String, User> users() {
        return Map.copyOf(this.users);
    }

    public Optional<User> user(String username) {
        return Optional.ofNullable(this.users.getOrDefault(username, null));
    }

    public Config addUser(User user) {
        Objects.requireNonNull(user);
        Objects.requireNonNull(user.username);

        user.data.parent = this.data;
        this.users.put(user.username, user);
        return this;
    }

    @Json(naming = Json.Naming.LowerUnderscore)
    public static class User {
        private final String username;
        private final Data data;
        private String passwd;
        private Token.AccessToken accessToken;

        public User(String username,
                    String passwd,
                    Token.AccessToken accessToken,
                    Data data) {
            Objects.requireNonNull(username);
            this.username = username;
            this.passwd = passwd;
            this.accessToken = accessToken;
            this.data = data == null ? new Data() : data;
        }

        public static User create(String username) {
            return new User(username, null, null, null);
        }

        public String username() {
            return username;
        }

        public String passwd() {
            return passwd;
        }

        public Token.AccessToken accessToken() {
            return accessToken;
        }

        public Data data() {
            return data;
        }

        public User setPasswd(String passwd) {
            this.passwd = passwd;
            return this;
        }

        public User setAccessToken(Token.AccessToken accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            User user = (User) o;
            return Objects.equals(username, user.username) && Objects.equals(passwd, user.passwd) && Objects.equals(accessToken, user.accessToken) && Objects.equals(data, user.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(username, passwd, accessToken, data);
        }

        @Override
        public String toString() {
            return "User{" +
                   "username='" + username + '\'' +
                   ", passwd='" + passwd + '\'' +
                   ", accessToken=" + accessToken +
                   ", data=" + data +
                   '}';
        }
    }

    @Json(naming = Json.Naming.LowerUnderscore)
    public static class Data {
        @Json.Ignore
        private Data parent;
        private URI proxy;
        private URI httpReferer;
        private String httpUserAgent;
        private String deviceId;
        private String deviceName;
        private String deviceModel;
        private String deviceSign;
        private String clientId;
        private String clientVersion;
        private String networkType;
        private String osVersion;
        private String platformVersion;
        private String protocolVersion;
        private String sdkVersion;
        private String providerName;

        public Data() {
        }

        public Data(Data parent) {
            Objects.requireNonNull(parent);
            this.parent = parent;
        }

        private Data(
            URI proxy,
            URI httpReferer,
            String httpUserAgent,
            String deviceId,
            String deviceName,
            String deviceModel,
            String deviceSign,
            String clientId,
            String clientVersion,
            String networkType,
            String osVersion,
            String platformVersion,
            String protocolVersion,
            String sdkVersion,
            String providerName) {
            this.proxy = proxy;
            this.httpReferer = httpReferer;
            this.httpUserAgent = httpUserAgent;
            this.deviceId = deviceId;
            this.deviceName = deviceName;
            this.deviceModel = deviceModel;
            this.deviceSign = deviceSign;
            this.clientId = clientId;
            this.clientVersion = clientVersion;
            this.networkType = networkType;
            this.osVersion = osVersion;
            this.platformVersion = platformVersion;
            this.protocolVersion = protocolVersion;
            this.sdkVersion = sdkVersion;
            this.providerName = providerName;
        }

        public static Data createDefault() {
            return new Data(
                null,
                URI.create("https://mypikpak.com/"),
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36 Edg/111.0.1661.39",
                "a3b5b335867244899c17d6e497be4134",
                "PC-Chrome",
                "chrome/111.0.0.0",
                "wdi10.a3b5b335867244899c17d6e497be4134xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
                "YUMx5nI8ZU8Ap8pm",
                "1.0.0",
                "NONE",
                "Win32",
                "1",
                "301",
                "5.2.0",
                "NONE"
            );
        }


        public Optional<Data> parent() {
            return Optional.ofNullable(this.parent);
        }


        /**
         * extracting field value hierarchically
         *
         * @param extractFn
         * @param <T>
         * @return
         */
        public <T> Optional<T> extract(Function<Data, T> extractFn) {
            return Optional.ofNullable(extractFn.apply(this))
                .or(() -> parent().flatMap(e -> e.extract(extractFn)));
        }

        public URI proxy() {
            return proxy;
        }

        public URI httpReferer() {
            return httpReferer;
        }

        public String httpUserAgent() {
            return httpUserAgent;
        }

        public String deviceId() {
            return deviceId;
        }

        public String deviceName() {
            return deviceName;
        }

        public String deviceModel() {
            return deviceModel;
        }

        public String deviceSign() {
            return deviceSign;
        }

        public String clientId() {
            return clientId;
        }

        public String clientVersion() {
            return clientVersion;
        }

        public String networkType() {
            return networkType;
        }

        public String osVersion() {
            return osVersion;
        }

        public String platformVersion() {
            return platformVersion;
        }

        public String protocolVersion() {
            return protocolVersion;
        }

        public String sdkVersion() {
            return sdkVersion;
        }

        public String providerName() {
            return providerName;
        }

        public Data setProxy(URI proxy) {
            this.proxy = proxy;
            return this;
        }

        public Data setHttpReferer(URI httpReferer) {
            this.httpReferer = httpReferer;
            return this;
        }

        public Data setHttpUserAgent(String httpUserAgent) {
            this.httpUserAgent = httpUserAgent;
            return this;
        }

        public Data setDeviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Data setDeviceName(String deviceName) {
            this.deviceName = deviceName;
            return this;
        }

        public Data setDeviceModel(String deviceModel) {
            this.deviceModel = deviceModel;
            return this;
        }

        public Data setDeviceSign(String deviceSign) {
            this.deviceSign = deviceSign;
            return this;
        }

        public Data setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Data setClientVersion(String clientVersion) {
            this.clientVersion = clientVersion;
            return this;
        }

        public Data setNetworkType(String networkType) {
            this.networkType = networkType;
            return this;
        }

        public Data setOsVersion(String osVersion) {
            this.osVersion = osVersion;
            return this;
        }

        public Data setPlatformVersion(String platformVersion) {
            this.platformVersion = platformVersion;
            return this;
        }

        public Data setProtocolVersion(String protocolVersion) {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public Data setSdkVersion(String sdkVersion) {
            this.sdkVersion = sdkVersion;
            return this;
        }

        public Data setProviderName(String providerName) {
            this.providerName = providerName;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Data data = (Data) o;
            return Objects.equals(proxy, data.proxy) && Objects.equals(httpReferer, data.httpReferer) && Objects.equals(httpUserAgent, data.httpUserAgent) && Objects.equals(deviceId, data.deviceId) && Objects.equals(deviceName, data.deviceName) && Objects.equals(deviceModel, data.deviceModel) && Objects.equals(deviceSign, data.deviceSign) && Objects.equals(clientId, data.clientId) && Objects.equals(clientVersion, data.clientVersion) && Objects.equals(networkType, data.networkType) && Objects.equals(osVersion, data.osVersion) && Objects.equals(platformVersion, data.platformVersion) && Objects.equals(protocolVersion, data.protocolVersion) && Objects.equals(sdkVersion, data.sdkVersion) && Objects.equals(providerName, data.providerName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(proxy, httpReferer, httpUserAgent, deviceId, deviceName, deviceModel, deviceSign, clientId, clientVersion, networkType, osVersion, platformVersion, protocolVersion, sdkVersion, providerName);
        }

        @Override
        public String toString() {
            return "Data{" +
                   "proxy=" + proxy +
                   ", httpReferer=" + httpReferer +
                   ", httpUserAgent='" + httpUserAgent + '\'' +
                   ", deviceId='" + deviceId + '\'' +
                   ", deviceName='" + deviceName + '\'' +
                   ", deviceModel='" + deviceModel + '\'' +
                   ", deviceSign='" + deviceSign + '\'' +
                   ", clientId='" + clientId + '\'' +
                   ", clientVersion='" + clientVersion + '\'' +
                   ", networkType='" + networkType + '\'' +
                   ", osVersion='" + osVersion + '\'' +
                   ", platformVersion='" + platformVersion + '\'' +
                   ", protocolVersion='" + protocolVersion + '\'' +
                   ", sdkVersion='" + sdkVersion + '\'' +
                   ", providerName='" + providerName + '\'' +
                   '}';
        }
    }

}
