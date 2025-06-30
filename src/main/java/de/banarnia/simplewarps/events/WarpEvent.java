package de.banarnia.simplewarps.events;

import de.banarnia.api.events.BanarniaEvent;
import de.banarnia.simplewarps.SimpleWarps;
import de.banarnia.simplewarps.manager.Warp;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import java.util.function.Consumer;

public class WarpEvent extends BanarniaEvent implements Cancellable {

    private Player player;
    private Warp warp;
    private double warmupTime;

    private Consumer<Boolean> afterTeleport;
    private int warmupTaskId;
    private boolean warmupRunning;

    private boolean cancelled;

    public WarpEvent(Player player, Warp warp, double warmupTime) {
        this.player     = player;
        this.warp       = warp;
        this.warmupTime = warmupTime;
    }

    // ~~~~~ Teleport ~~~~~

    /**
     * Execute the teleport task. Applies warmup, if configured.
     * @param afterTeleport True, if player was teleported, else false.
     */
    public void execute(Consumer<Boolean> afterTeleport) {
        // Check if warmup is configured.
        if (warmupTime <= 0) {
            afterTeleport.accept(warp.teleport(player));
            return;
        }

        // Teleport player after warmup time.
        this.warmupTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(SimpleWarps.getInstance(), () -> {
            afterTeleport.accept(warp.teleport(player));
        }, (long) (20.0 * warmupTime));

        this.afterTeleport = afterTeleport;
        this.warmupRunning = true;
    }

    // ~~~~~ Cancel ~~~~~

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;

        // Cancel warmup task.
        if (this.cancelled) {
            Bukkit.getScheduler().cancelTask(this.warmupTaskId);
            this.warmupRunning = false;
            this.afterTeleport.accept(false);
        }
    }

    // ~~~~~ Getter & Setter ~~~~~


    public Warp getWarp() {
        return warp;
    }
}
