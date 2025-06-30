package de.banarnia.simplewarps.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import de.banarnia.simplewarps.lang.Message;
import de.banarnia.simplewarps.manager.Warp;
import de.banarnia.simplewarps.manager.WarpManager;
import org.bukkit.Bukkit;

@CommandAlias("warpall")
@CommandPermission("simplewarps.warpall")
public class WarpallCommand extends BaseCommand {

    private WarpManager manager;

    public WarpallCommand(WarpManager manager) {
        this.manager = manager;
    }

    @Default
    @CommandCompletion("@enabledWarps")
    public void warpAll(CommandIssuer sender, Warp warp) {
        if (!warp.isEnabled()) {
            sender.sendMessage(Message.COMMAND_ERROR_WARP_DISABLED.get());
            return;
        }

        Bukkit.getOnlinePlayers().forEach(player -> manager.executeWarp(sender.getIssuer(), player, warp));
    }

}
