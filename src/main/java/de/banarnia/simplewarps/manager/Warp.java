package de.banarnia.simplewarps.manager;

import de.banarnia.api.UtilString;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Warp implements ConfigurationSerializable {

    private final String name;
    private boolean enabled = true;
    private long created;
    private String icon;
    private boolean requiresPermission;

    private String worldName;
    private double x,y,z;
    private float yaw,pitch;

    public Warp(String name, Location location) {
        this(name, true, System.currentTimeMillis(), Material.GRASS_BLOCK.toString(), false,
             location.getWorld().getName(), location.getX(), location.getY(), location.getZ(),
             location.getYaw(), location.getPitch());
    }

    public Warp(String name, boolean enabled, long created, String icon, boolean requiresPermission,
                String worldName, double x, double y, double z, float yaw, float pitch) {
        this.name               = name;
        this.enabled            = enabled;
        this.created            = created;
        this.icon               = icon;
        this.requiresPermission = requiresPermission;
        this.worldName          = worldName;
        this.x                  = x;
        this.y                  = y;
        this.z                  = z;
        this.yaw                = yaw;
        this.pitch              = pitch;
    }

    // ~~~~~ Teleport ~~~~~

    /**
     * Teleports the player to the warp location, if it is loaded.
     * @param player Player to teleport.
     * @param cause Teleport cause.
     * @return True if player was teleported, else false.
     */
    public boolean teleport(Player player, PlayerTeleportEvent.TeleportCause cause) {
        if (!isEnabled() || !isLocationLoaded())
            return false;

        return cause != null ? player.teleport(getLocation(), cause) : player.teleport(getLocation());
    }

    /**
     * Teleports the player to the warp location, if it is loaded.
     * @param player Player to teleport.
     * @return True if player was teleported, else false.
     */
    public boolean teleport(Player player) {
        return teleport(player, null);
    }

    // ~~~~~ Location ~~~~~

    /**
     * Check if the warp is usable.
     * @return True if the warp location is loaded, else false.
     */
    public boolean isLocationLoaded() {
        return getLocation() != null;
    }

    /**
     * Get the world instance the warp is located in.
     * @return Warp world.
     */
    public World getWorld() {
        return Bukkit.getWorld(this.worldName);
    }

    /**
     * Get location of the warp if the world is loaded.
     * @return Location if the world is loaded, else null.
     */
    public Location getLocation() {
        World world = getWorld();
        if (world == null)
            return null;

        return new Location(world, this.x, this.y, this.z, this.yaw, this.pitch);
    }

    /**
     * Get the World Environment of the warp.
     * @return Warp world environment.
     */
    public World.Environment getWorldEnvironment() {
        World world = getWorld();
        if (world != null && world.getEnvironment() != null)
            return world.getEnvironment();

        if (UtilString.containsIgnoreCase(worldName, "nether"))
            return World.Environment.NETHER;
        if (UtilString.containsIgnoreCase(worldName, "end"))
            return World.Environment.THE_END;

        return World.Environment.NORMAL;
    }

    /**
     * Update the warp location.
     * @param newLocation New location of the warp.
     * @param timestamp New creation timestamp.
     * @return True, if warps location was changed, else false.
     */
    public boolean updateLocation(Location newLocation, long timestamp) {
        if (newLocation == null || newLocation.getWorld() == null)
            return false;

        this.worldName  = newLocation.getWorld().getName();
        this.x          = newLocation.getX();
        this.y          = newLocation.getY();
        this.z          = newLocation.getZ();
        this.yaw        = newLocation.getYaw();
        this.pitch      = newLocation.getPitch();

        this.created    = timestamp;

        return true;
    }

    // ~~~~~ Serialize ~~~~~

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> result = new LinkedHashMap<>();
        result.put("Name", name);
        result.put("Enabled", enabled);
        result.put("Created", created);
        if (icon != null)
            result.put("Icon", icon);
        result.put("RequiresPermission", requiresPermission);

        result.put("World", worldName);
        result.put("X", x);
        result.put("Y", y);
        result.put("Z", z);
        result.put("Yaw", yaw);
        result.put("Pitch", pitch);

        return result;
    }

    public static Warp deserialize(Map<String, Object> map) {
        String name     = (String) map.get("Name");
        boolean enabled = (boolean) map.get("Enabled");
        long created    = (long) map.get("Created");
        String icon     = null;
        if (map.containsKey("Icon"))
            icon = (String) map.get("Icon");
        boolean requiresPermission = (boolean) map.get("RequiresPermission");

        String worldName    = (String) map.get("World");
        double x            = (double) map.get("X");
        double y            = (double) map.get("Y");
        double z            = (double) map.get("Z");
        float yaw           = (float) ((double) map.get("Yaw"));
        float pitch         = (float) ((double) map.get("Pitch"));

        return new Warp(name, enabled, created, icon, requiresPermission, worldName, x, y, z, yaw, pitch);
    }

    // ~~~~~ Getter & Setter ~~~~~


    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public long getCreated() {
        return created;
    }

    public String getIcon() {
        return icon;
    }

    public String getWorldName() {
        return worldName;
    }
}
