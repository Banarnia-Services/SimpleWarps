package de.banarnia.simplewarps.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import de.banarnia.simplewarps.manager.Warp;
import de.banarnia.simplewarps.manager.WarpManager;
import org.bukkit.entity.Player;

@CommandAlias("setwarp")
@CommandPermission("simplewarps.setwarp")
public class SetwarpCommand extends BaseCommand {

    private WarpManager manager;

    public SetwarpCommand(WarpManager manager) {
        this.manager = manager;
    }

    @Default
    @CommandCompletion("@warps")
    public void setWarp(Player sender, @Single String warpName) {
        Warp warp = manager.getWarp(warpName);

        // Update location, if warp already exists.
        if (warp != null) {
            warp.updateLocation(sender.getLocation(), System.currentTimeMillis());
            manager.updateWarp(warp);
            // ToDo message.
            return;
        }

        // Create new warp.
        warp = new Warp(warpName, sender.getLocation());

        // Add the warp.
        manager.createWarp(warp);

        // Todo message.
    }

}
