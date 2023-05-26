import io.avaje.jsonb.Types;
import io.avaje.jsonb.stream.JsonStream;
import lost.pikpak.client.Config;
import lost.pikpak.client.enums.TokenType;
import lost.pikpak.client.model.*;
import lost.pikpak.client.token.Token;
import lost.pikpak.client.util.Util;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonTest {

    static EasyRandom easyRandom;

    static {
        var params = new EasyRandomParameters()
            .objectPoolSize(100)
            .randomizationDepth(3)
            .collectionSizeRange(3, 5);
        easyRandom = new EasyRandom(params);
    }

    static Stream<Arguments> testDataClasses() {
        return Stream.of(
            Arguments.of(InitInfoResult.class),
            Arguments.of(RefreshTokenResult.class),
            Arguments.of(FileListResult.class),
            Arguments.of(FileDetailsResult.class),
            Arguments.of(FileAddResult.class),
            Arguments.of(SignInParam.class),
            Arguments.of(SignInResult.class),
            Arguments.of(Config.class)
        );
    }

    @ParameterizedTest
    @MethodSource("testDataClasses")
    <T> void serdeData(Class<T> cls) {
        var data = easyRandom.nextObject(cls);
        var json = Util.toJsonPretty(data);

        var newData = Util.fromJson(json, cls);

        System.out.println(cls.getSimpleName() + " " + "json = " + json);

        assertThat(newData).isNotNull();
        assertThat(newData).isEqualTo(data);
    }


    @Test
    void serdeOffsetDateTime() {
        var time = Map.of("time", OffsetDateTime.now());
        var json = Util.toJsonPretty(time);
        System.out.println("json = " + json);

        HashMap<String, OffsetDateTime> newTime = Util.fromJson(json, Types.mapOf(OffsetDateTime.class));

        assertThat(newTime).isNotNull();
        assertThat(newTime).isEqualTo(time);
        System.out.println("newTime.get(\"time\") = " + newTime.get("time"));

    }

    @Test
    void derOffsetDateTime() {
        var timeStr = "2023-04-01T08:23:11.842+08:00";
        var json = "";
        try (var writer = JsonStream.builder().build().bufferedWriter()) {
            writer.pretty(true);
            writer.beginObject();
            writer.name("time");
            writer.value(timeStr);
            writer.endObject();
            json = writer.result();
        }

        var newTime = Util.fromJson(json, Types.mapOf(OffsetDateTime.class));

        assertThat(newTime).isNotNull();
        assertThat(newTime)
            .extracting("time")
            .extracting(Object::toString)
            .isEqualTo(timeStr);
    }

    @Test
    void serdeURI() {
        var uri = List.of(URI.create("http://localhost:8888/"));
        var json = Util.toJson(uri);
        System.out.println("json = " + json);

        var uri2 = Util.fromJson(json, Types.listOf(URI.class));
        assertThat(uri).isEqualTo(uri2);
    }

    @Test
    void serdeToken() {
        var refreshToken = new Token.RefreshToken("refresh_xxx", TokenType.Bearer);
        var accessToken = new Token.AccessToken(
            refreshToken,
            "access_xxx",
            TokenType.Bearer,
            "sub_xxx",
            OffsetDateTime.now());

        var json = Util.toJsonPretty(accessToken);

//        System.out.println("json = " + json);

        assertThat(json).contains("access_xxx").contains("sub_xxx").contains("refresh_xxx")
            .doesNotContain("tokenString");

        var newAccessToken = Util.fromJson(json, Token.AccessToken.class);

        assertThat(accessToken).isEqualTo(newAccessToken);
    }

    @Test
    void serdeConfig() {
        var json = "{}";
        Config config = Util.fromJson(json, Config.class);
        assertThat(config).isNotNull();
        assertThat(config.users()).hasSize(0);

        var config2 = Config.createDefault();
        var json2 = Util.toJsonPretty(config2);
        System.out.println("json2 = " + json2);
        var config22 = Util.fromJson(json2, Config.class);
        assertThat(config2).isEqualTo(config22);
    }
}
