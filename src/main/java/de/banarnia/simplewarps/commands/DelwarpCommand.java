package de.banarnia.simplewarps.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import de.banarnia.simplewarps.manager.Warp;
import de.banarnia.simplewarps.manager.WarpManager;

@CommandAlias("delwarp")
@CommandPermission("simplewarps.admin")
public class DelwarpCommand extends BaseCommand {

    private WarpManager manager;

    public DelwarpCommand(WarpManager manager) {
        this.manager = manager;
    }

    @Default
    @CommandCompletion("@warps")
    public void deleteWarp(CommandIssuer sender, Warp warp) {
        manager.executeDelwarp(sender.getIssuer(), warp);
    }

}
