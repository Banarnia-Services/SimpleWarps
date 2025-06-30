package de.banarnia.simplewarps.manager;

import de.banarnia.api.config.YamlConfig;
import de.banarnia.simplewarps.SimpleWarps;
import de.banarnia.simplewarps.config.WarpConfig;
import de.banarnia.simplewarps.events.WarpEvent;
import de.banarnia.simplewarps.lang.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
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

    // ~~~~~ Services ~~~~~

    /**
     * Executes a command to warp a specific player to a warp location.
     * @param issuer Command issuer.
     * @param target Player to warp.
     * @param warp Warp instance.
     */
    public void executeWarp(CommandSender issuer, Player target, Warp warp) {
        // Check for NULL pointer.
        if (issuer == null || warp == null || target == null)
            return;

        // Check if warp is enabled and location is loaded.
        if (!warp.isEnabled()) {
            issuer.sendMessage(Message.COMMAND_ERROR_WARP_DISABLED.get());
            return;
        }

        // Check if world is loaded.
        if (!warp.isLocationLoaded()) {
            issuer.sendMessage(Message.COMMAND_ERROR_WARP_LOCATION_NOT_LOADED.get());
            return;
        }

        // Check if issuer is target.
        boolean issuerIsTarget = issuer instanceof Player && ((Player) issuer).getUniqueId().equals(target.getUniqueId());

        // Check if permission is required.
        if (issuerIsTarget && warp.requiresPermission() && !target.hasPermission("simplewarps.warp." + warp.getName())) {
            issuer.sendMessage(Message.COMMAND_ERROR_PERMISSION.get());
            return;
        }

        // Check if permission to warp others is required.
        if (!issuerIsTarget && !issuer.hasPermission("simplewarps.others")) {
            issuer.sendMessage(Message.COMMAND_ERROR_PERMISSION.get());
            return;
        }

        // Check if cooldown is running.
        if (issuerIsTarget && hasCooldown(target) && !target.hasPermission("simplewarps.cooldown.bypass")) {
            String cooldown = String.valueOf(getRemainingCooldown(target));
            target.sendMessage(Message.COMMAND_ERROR_WARP_COOLDOWN.replace("%time%", cooldown));
            return;
        }

        // Check if warmup is running.
        if (hasWarmup(target))
            cancelWarmup(target);

        // Start cooldown.
        if (issuerIsTarget)
            startCooldown(target);

        // Check if warmup has to be applied.
        double warmupTime = config.getWarmupTime();
        if (!issuerIsTarget || target.hasPermission("simplewarps.warmup.bypass"))
            warmupTime = 0;

        if (warmupTime > 0)
            target.sendMessage(Message.COMMAND_INFO_WARP_WARMUP
                                      .replace("%warp%", warp.getName())
                                      .replace("%time%", String.valueOf(warmupTime)));

        // Warp the player.
        WarpEvent event = new WarpEvent(target, warp, warmupTime);
        if (warmupTime > 0)
            warpWarmups.put(target.getUniqueId(), event);
        event.callEvent();
        Player finalTarget = target;
        event.execute(success -> {
            if (success)
                finalTarget.sendMessage(Message.COMMAND_INFO_WARP_TELEPORT.replace("%warp%", warp.getName()));
            else  {
                String message = warp.isLocationLoaded() ? Message.COMMAND_ERROR_WARP_WARMUP_ABORT.get()
                                                         : Message.COMMAND_ERROR_WARP_LOCATION_NOT_LOADED.get();
                finalTarget.sendMessage(message);
            }

            if (!issuerIsTarget)
                issuer.sendMessage(success ? Message.COMMAND_INFO_WARP_OTHERS
                                                    .replace("%player%", finalTarget.getName())
                                                    .replace("%warp%", warp.getName())
                                           : Message.COMMAND_ERROR_WARP_FAILED.get()
                );

            warpWarmups.remove(target.getUniqueId());
            });
    }

    /**
     * Execute a command to create a warp or updating an existing warps location.
     * @param sender Command issuer.
     * @param warpName Name of the warp.
     */
    public void executeSetwarp(Player sender, String warpName) {
        Warp warp = getWarp(warpName);

        // Update location, if warp already exists.
        if (warp != null) {
            warp.updateLocation(sender.getLocation(), System.currentTimeMillis());
            updateWarp(warp);
            sender.sendMessage(Message.COMMAND_INFO_SETWARP_UPDATED.replace("%warp%", warpName));
            return;
        }

        // Create new warp.
        warp = new Warp(warpName, sender.getLocation());

        // Add the warp.
        createWarp(warp);

        sender.sendMessage(Message.COMMAND_INFO_SETWARP_CREATED.replace("%warp%", warpName));
    }

    /**
     * Execute a command to delete a specific warp.
     * @param sender Command issuer.
     * @param warp Warp to delete.
     */
    public void executeDelwarp(CommandSender sender, Warp warp) {
        deleteWarp(warp);
        sender.sendMessage(Message.COMMAND_INFO_DELWARP_FINISHED.replace("%warp%", warp.getName()));
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
     * @param player Player to apply the cooldown to.
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

    /**
     * Get remaining seconds for the warp cooldown.
     * @param player Player instance.
     * @return Amount of seconds left.
     */
    public long getRemainingCooldown(Player player) {
        return (warpCooldowns.getOrDefault(player.getUniqueId(), 0L) - System.currentTimeMillis()) / 1000;
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

    /**
     * Get a collection of all enabled warps.
     * @return Collection of enabled warps.
     */
    public Collection<Warp> getEnabledWarps() {
        return warps.values().stream().filter(Warp::isEnabled).collect(Collectors.toList());
    }

    /**
     * Get a collection of all warps that are enabled and the player has permission to use.
     * @param player Player instance.
     * @return Collection of usable warps.
     */
    public Collection<Warp> getUsableWarps(Player player) {
        return warps.values().stream()
                .filter(Warp::isEnabled)
                .filter(warp -> !warp.requiresPermission() || player.hasPermission("simplewarps.warp." + warp.getName()))
                .collect(Collectors.toList());
    }

    // ~~~~~ Getter & Setter ~~~~~


    public WarpConfig getConfig() {
        return config;
    }
}
