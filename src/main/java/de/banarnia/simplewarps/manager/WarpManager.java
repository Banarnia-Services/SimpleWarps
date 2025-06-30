package de.banarnia.simplewarps.manager;

import co.aikar.commands.CommandIssuer;
import de.banarnia.api.config.YamlConfig;
import de.banarnia.simplewarps.SimpleWarps;
import de.banarnia.simplewarps.config.WarpConfig;
import de.banarnia.simplewarps.events.WarpEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class WarpManager {

    private SimpleWarps plugin;
    private WarpConfig config;
    private YamlConfig warpConfig;

    private HashMap<String, Warp> warps = new HashMap<>();

    private HashMap<UUID, WarpEvent> warpWarmups = new HashMap<>();
    private HashMap<UUID, Long> warpCooldowns = new HashMap<>();

    public WarpManager(SimpleWarps plugin, WarpConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.warpConfig = YamlConfig.of(plugin.getDataFolder(), "warps.yml");

        loadWarps();
    }

    // ~~~~~ Teleport ~~~~~

    /**
     * Warps a player to the given warp.
     * @param target Target player to warp.
     * @param warp Destination.
     */
    public void warpPlayer(Player target, Warp warp) {
        warpPlayer((CommandIssuer) target, target, warp);
    }

    /**
     * Warps a player to the given warp.
     * @param issuer Command issues.
     * @param target Target player to warp.
     * @param warp Destination.
     */
    public void warpPlayer(CommandIssuer issuer, Player target, Warp warp) {
        // Check for NULL pointer.
        if (issuer == null || target == null || warp == null)
            return;

        // Check if warp is enabled and location is loaded.
        if (!warp.isEnabled() || !warp.isLocationLoaded())
            return;

        // Check if issuer is target.
        boolean issuerIsTarget = issuer.isPlayer() && issuer.getUniqueId().equals(target.getUniqueId());

        // Check if cooldown is running.
        if (!issuerIsTarget && hasCooldown(target) && !target.hasPermission("simplewarps.cooldown.bypass")) {
            // ToDo message.
            return;
        }

        // Check if warmup has to be applied.
        double warmupTime = config.getWarmupTime();
        if (!issuerIsTarget || target.hasPermission("simplewarps.warmup.bypass"))
            warmupTime = 0;

        // Warp the player.
        WarpEvent event = new WarpEvent(target, warp, warmupTime);
        event.callEvent();
        event.execute(success -> {
            // ToDo message.
        });
    }

    // ~~~~~ Warmup ~~~~~

    /**
     * Check if a player has a warp warmup.
     * @param player Player to check.
     * @return True, if player is in warmup phase, else false.
     */
    public boolean hasWarmup(Player player) {
        return warpWarmups.containsKey(player.getUniqueId());
    }

    /**
     * Cancel a pending warp warmup.
     * @param player Player instance.
     */
    public void cancelWarmup(Player player) {
        // Check if player is in warmup phase.
        if (!hasWarmup(player))
            return;

        // Cancel event.
        warpWarmups.get(player.getUniqueId()).setCancelled(true);
    }

    public List<WarpEvent> getWarmupWarps(Warp warp) {
        return this.warpWarmups.values().stream()
                .filter(warmupWarp -> warmupWarp.getWarp().equals(warp))
                .collect(Collectors.toList());
    }

    // ~~~~~ Cooldown ~~~~~

    /**
     * Check if a player has a warp cooldown.
     * @param player Player to check.
     * @return True, if player has warp cooldown, else false.
     */
    public boolean hasCooldown(Player player) {
        return warpCooldowns.containsKey(player.getUniqueId());
    }

    /**
     * Set the warp cooldown timer, before the player can execute the next warp command.
     * @param player Player to aplpy the cooldown to.
     */
    public void startCooldown(Player player) {
        double cooldown = config.getCooldownTime();
        if (cooldown <= 0 || player.hasPermission("simplewarps.cooldown.bypass"))
            return;

        warpCooldowns.put(player.getUniqueId(), (System.currentTimeMillis() + (long) (cooldown * 1000.0)));
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> warpCooldowns.remove(player.getUniqueId()), (long) (20.0 * cooldown));
    }

    /**
     * Reset a players warp cooldown timer.
     * @param player Player instance.
     */
    public void resetCooldown(Player player) {
        warpCooldowns.remove(player.getUniqueId());
    }

    // ~~~~~ Warps ~~~~~

    /**
     * Load all warps from the warp config.
     */
    private void loadWarps() {
        // Check if warps exist.
        if (!warpConfig.isConfigurationSection("warps"))
            return;

        // Reset list.
        warps.clear();

        // Read all saved warps.
        ConfigurationSection section = warpConfig.getConfigurationSection("warps");
        for (String warpName : section.getKeys(false)) {
            Warp warp = section.getSerializable(warpName, Warp.class);
            if (warp == null)
                continue;

            warps.put(warpName, warp);
        }
    }

    /**
     * Check if a warp with the given name exists.
     * @param warpName Warps name.
     * @return True, if warp exists, else false.
     */
    public boolean warpExists(String warpName) {
        return warps.containsKey(warpName);
    }

    /**
     * Retrieve a warp by its name.
     * @param warpName Warps name.
     * @return Warp instance, if warp exists, else null.
     */
    public Warp getWarp(String warpName) {
        return warps.get(warpName);
    }

    /**
     * Create a new warp and save it in the config.
     * @param warp Warp instance.
     * @return True, if warp with given name does not exist, else false.
     */
    public boolean createWarp(Warp warp) {
        // Check if warp exists.
        if (warp == null || warpExists(warp.getName()))
            return false;

        // Save warp in config.
        warpConfig.set("warps." + warp.getName(), warp);
        warpConfig.save();

        // Add to list.
        warps.put(warp.getName(), warp);

        return true;
    }

    /**
     * Update a warp in the config.
     * @param warp Warp to update.
     * @return True, if warp was updated, else false.
     */
    public boolean updateWarp(Warp warp) {
        // Check if warp exists.
        if (warp == null || !warpExists(warp.getName()))
            return false;

        // Save warp in config.
        warpConfig.set("warps." + warp.getName(), warp);
        warpConfig.save();

        return true;
    }

    /**
     * Delete a warp and cancel all warmups for this wap.
     * @param warp Warp instance.
     * @return True, if warp was deleted, else false.
     */
    public boolean deleteWarp(Warp warp) {
        // Check if warp exists.
        if (warp == null || !getWarp(warp.getName()).equals(warp))
            return false;

        // Cancel pending warp warmups.
        getWarmupWarps(warp).forEach(event -> event.setCancelled(true));

        // Remove from config.
        warpConfig.set("warps." + warp.getName(), null);
        warpConfig.save();

        // Remove from list.
        warps.remove(warp.getName());

        return true;
    }

    /**
     * Reload the general config.
     */
    public void reloadConfig() {
        config.reload();
    }

    /**
     * Get a collection of all registered warps.
     * @return Collection of registered warps.
     */
    public Collection<Warp> getWarps() {
        return warps.values();
    }

}
