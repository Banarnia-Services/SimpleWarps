package de.banarnia.simplewarps;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.CommandManager;
import de.banarnia.api.lang.LanguageHandler;
import de.banarnia.simplewarps.manager.WarpManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleWarps extends JavaPlugin {

    private static SimpleWarps instance;

    private CommandManager commandManager;
    private LanguageHandler languageHandler;

    private WarpManager manager;

    @Override
    public void onLoad() {
        instance = this;

        super.onLoad();
    }

    @Override
    public void onEnable() {
        super.onEnable();

        // BStats
        int pluginId = 26311;
        Metrics metrics = new Metrics(this, pluginId);

        // Command manager.
        this.commandManager = new BukkitCommandManager(this);
        commandManager.usePerIssuerLocale(true);

        // ToDo
        // Language handler.
        //File langFolder = new File(getDataFolder(), "lang");
        //YamlConfig.fromResource(this, "lang/en.yml", langFolder, "en.yml");
        //YamlConfig.fromResource(this, "lang/de.yml", langFolder, "de.yml");
        //this.languageHandler = new LanguageHandler(this, homeConfig.getLanguage());
        //this.languageHandler.register(Message.class);

        // Manager.
        this.manager = new WarpManager();
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(Player::closeInventory);
    }

    public static SimpleWarps getInstance() {
        return instance;
    }
}
