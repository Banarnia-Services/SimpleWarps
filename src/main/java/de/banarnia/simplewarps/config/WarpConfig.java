package de.banarnia.simplewarps.config;

import de.banarnia.api.config.Config;
import de.banarnia.api.sql.Database;
import de.banarnia.simplewarps.SimpleWarps;

public class WarpConfig {

    private final SimpleWarps plugin;

    private final Config config;

    private Database database;

    public WarpConfig(SimpleWarps plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void reload() {
        config.loadConfig();
    }

    public String getLanguage() {
        return config.getString("language", "en");
    }

    public int getWarmupTime() {
        return config.getInt("teleport-warmup-time", 0);
    }

    public boolean cancelWarmupOnMove() { return config.getBoolean("cancel-warmup-on-move", false); }

    public int getCooldownTime() {
        return config.getInt("warp-cooldown", 10);
    }

    public boolean debugMode() {
        return config.getBoolean("debug");
    }

}
