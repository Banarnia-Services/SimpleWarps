package de.banarnia.simplewarps.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import de.banarnia.simplewarps.gui.WarpGUI;
import de.banarnia.simplewarps.lang.Message;
import de.banarnia.simplewarps.manager.Warp;
import de.banarnia.simplewarps.manager.WarpManager;
import org.bukkit.entity.Player;

import java.util.Collection;

@CommandAlias("warp")
public class WarpCommand extends BaseCommand {

    private WarpManager manager;

    public WarpCommand(WarpManager manager) {
        this.manager = manager;
    }

    @Default
    public void openGui(Player sender) {
        new WarpGUI(manager, sender).open(sender);
    }

    @Subcommand("list")
    public void listWarps(CommandIssuer sender) {
        boolean isAdmin = sender.hasPermission("simplewarps.admin");

        Collection<Warp> warps = isAdmin ? manager.getWarps() : manager.getUsableWarps(sender.getIssuer());
        sender.sendMessage("§6Warps §8[§e" + warps.size() + "§8]");

        warps.forEach(warp -> {
            String enabled = warp.isEnabled() ? "§2✔" : "§4✖";
            String message = "§7- " +
                    (isAdmin ? "§8[" + enabled + "§8] "  : "") +
                    "§e" + warp.getName();
            sender.sendMessage(message);
        });
    }

    @Default
    @CommandCompletion("@enabledWarps @players")
    public void warp(CommandIssuer sender, Warp warp, @Optional OnlinePlayer target) {
        manager.executeWarp(sender.getIssuer(), target != null ? target.getPlayer() : sender.getIssuer(), warp);
    }

    @Subcommand("reload")
    @CommandPermission("simplewarps.reload")
    public void reload(CommandIssuer sender) {
        manager.reloadConfig();
        sender.sendMessage(Message.COMMAND_INFO_WARP_CONFIG_RELOADED.get());
    }

}
