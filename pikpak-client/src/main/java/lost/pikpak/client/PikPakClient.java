package lost.pikpak.client;

import lost.pikpak.client.context.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.lang.System.Logger.Level.DEBUG;

public class PikPakClient {
    private static final System.Logger LOG = System.getLogger(PikPakClient.class.getName());
    private final Config config;
    private final Map<String, Context> contexts = new HashMap<>();

    private PikPakClient(Config config) {
        this.config = config == null ? Config.createDefault() : config;
        loadContextsFromConfig();
    }

    public static PikPakClient create() {
        return new PikPakClient(null);
    }

    public static PikPakClient create(Config config) {
        return new PikPakClient(config);
    }

    private void loadContextsFromConfig() {
        LOG.log(DEBUG, "loadContextsFromConfig start...");
        for (var e : this.config.users().entrySet()) {
            var username = e.getKey();
            var user = e.getValue();
            if (user == null) continue;
            var context = Context.create(this, user);
            this.contexts.put(username, context);
        }
        LOG.log(DEBUG, "loadContextsFromConfig end...");
    }

    public Config config() {
        return this.config;
    }

    public Optional<Context> context(String username) {
        return Optional.ofNullable(contexts.getOrDefault(username, null));
    }

    public PikPakClient addContext(Config.User user) {
        Objects.requireNonNull(user);
        this.config.addUser(user);
        var context = Context.create(this, user);
        this.contexts.put(user.username(), context);
        return this;
    }

}
