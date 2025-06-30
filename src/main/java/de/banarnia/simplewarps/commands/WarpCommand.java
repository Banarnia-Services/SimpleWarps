package de.banarnia.simplewarps.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import de.banarnia.simplewarps.manager.Warp;
import de.banarnia.simplewarps.manager.WarpManager;
import org.bukkit.entity.Player;

@CommandAlias("warp")
public class WarpCommand extends BaseCommand {

    private WarpManager manager;

    public WarpCommand(WarpManager manager) {
        this.manager = manager;
    }

    @Default
    public void openGui(Player sender) {
        // Todo: GUI
    }

    @Subcommand("list")
    public void listWarps(CommandIssuer sender) {
        // Todo message.
    }

    @Default
    @CommandCompletion("@enabledWarps")
    public void warp(Player sender, Warp warp) {
        manager.warpPlayer(sender, warp);
    }

    @Default
    @CommandPermission("simplewarps.warp.others")
    @CommandCompletion("@players @enabledWarps")
    public void warpPlayer(CommandIssuer sender, Player target, Warp warp) {
        manager.warpPlayer(sender, target, warp);
    }

    @Subcommand("reload")
    @CommandPermission("simplewarps.reload")
    public void reload(CommandIssuer sender) {
        manager.reloadConfig();
        // ToDo: message.
    }

}
