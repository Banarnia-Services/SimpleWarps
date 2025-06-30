package de.banarnia.simplewarps.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import de.banarnia.simplewarps.manager.WarpManager;
import org.bukkit.entity.Player;

@CommandAlias("setwarp")
@CommandPermission("simplewarps.admin")
public class SetwarpCommand extends BaseCommand {

    private WarpManager manager;

    public SetwarpCommand(WarpManager manager) {
        this.manager = manager;
    }

    @Default
    @CommandCompletion("@warps")
    public void setWarp(Player sender, @Single String warpName) {
        manager.executeSetwarp(sender, warpName);
    }

}
