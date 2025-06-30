package de.banarnia.simplewarps;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.CommandManager;
import co.aikar.commands.InvalidCommandArgument;
import de.banarnia.api.config.Config;
import de.banarnia.api.config.YamlConfig;
import de.banarnia.api.config.YamlVersionConfig;
import de.banarnia.api.lang.LanguageHandler;
import de.banarnia.simplewarps.commands.DelwarpCommand;
import de.banarnia.simplewarps.commands.SetwarpCommand;
import de.banarnia.simplewarps.commands.WarpCommand;
import de.banarnia.simplewarps.commands.WarpallCommand;
import de.banarnia.simplewarps.config.WarpConfig;
import de.banarnia.simplewarps.lang.Message;
import de.banarnia.simplewarps.listener.WarpListener;
import de.banarnia.simplewarps.manager.Warp;
import de.banarnia.simplewarps.manager.WarpManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.stream.Collectors;

public class SimpleWarps extends JavaPlugin {

    private static SimpleWarps instance;

    private CommandManager commandManager;
    private LanguageHandler languageHandler;
    private WarpManager manager;

    private WarpConfig config;

    @Override
    public void onLoad() {
        instance = this;

        super.onLoad();

        ConfigurationSerialization.registerClass(Warp.class);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        // BStats
        int pluginId = 26311;
        Metrics metrics = new Metrics(this, pluginId);

        // Load config.
        Config config = YamlVersionConfig.of(this, getDataFolder(), "config.yml",
                "config.yml", "1.0");
        this.config = new WarpConfig(this, config);

        // Language handler.
        File langFolder = new File(getDataFolder(), "lang");
        YamlConfig.fromResource(this, "lang/en.yml", langFolder, "en.yml");
        YamlConfig.fromResource(this, "lang/de.yml", langFolder, "de.yml");
        this.languageHandler = new LanguageHandler(this, this.config.getLanguage());
        this.languageHandler.register(Message.class);

        // Manager.
        this.manager = new WarpManager(this, this.config);

        // Command manager.
        this.commandManager = new BukkitCommandManager(this);
        commandManager.usePerIssuerLocale(true);
        registerCommandContext();

        // Register commands.
        commandManager.registerCommand(new WarpCommand(manager));
        commandManager.registerCommand(new WarpallCommand(manager));
        commandManager.registerCommand(new SetwarpCommand(manager));
        commandManager.registerCommand(new DelwarpCommand(manager));

        // Register listener.
        Bukkit.getPluginManager().registerEvents(new WarpListener(manager), this);
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.closeInventory();
            manager.cancelWarmup(player);
        });
    }

    public static SimpleWarps getInstance() {
        return instance;
    }

    // ~~~~~ Command Context ~~~~~

    private void registerCommandContext() {
        commandManager.getCommandContexts().registerContext(Warp.class, c -> {
            String warpName = c.popFirstArg();
            Warp warp = manager.getWarp(warpName);

            if (warp != null)
                return warp;
            else
                throw new InvalidCommandArgument(Message.COMMAND_ERROR_WARP_NOT_FOUND.replace("%warp%", warpName));
        });

        commandManager.getCommandCompletions().registerCompletion("warps", c -> {
            return manager.getWarps().stream()
                    .map(Warp::getName)
                    .collect(Collectors.toList());
        });

        commandManager.getCommandCompletions().registerCompletion("enabledWarps", c -> {
            return manager.getWarps().stream()
                    .filter(Warp::isEnabled)
                    .map(Warp::getName)
                    .collect(Collectors.toList());
        });
    }

}
