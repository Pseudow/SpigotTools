package net.pseudow.tools;

import net.pseudow.tools.reflection.Reflection;
import net.pseudow.tools.reflection.entities.EntityArmorStand;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Hologram {
    private static final double distance = 0.24D;

    private final HashMap<OfflinePlayer, Boolean> receivers;
    private final HashMap<Integer, EntityArmorStand> entities;
    private List<String> lines;
    private Location location;
    private final BukkitTask taskID;
    private final double rangeView = 60;
    private boolean linesChanged;

    /**
     * This class is inspired by a SamaGames class
     * You can find it in github: https://github.com/SamaGames/SamaGamesAPI.
     * I modified this class to make it available for multi-versioning.
     *
     * @author SamaGames
     *
     * @param lines Hologram's lines
     */
    public Hologram(JavaPlugin plugin, String... lines) {
        this.receivers = new HashMap<>();
        this.entities = new HashMap<>();

        this.lines = new ArrayList<>();
        this.lines.addAll(Arrays.asList(lines));

        this.linesChanged = true;

        this.taskID = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::sendLinesForPlayers, 10L, 10L);
    }

    /**
     * Add hologram's receiver to this hologram.
     *
     * @param offlinePlayer Player
     * @return {@code true} is success
     */
    public boolean addReceiver(OfflinePlayer offlinePlayer) {
        if (!offlinePlayer.isOnline())
            return false;

        Player p = offlinePlayer.getPlayer();
        boolean inRange = false;

        if (p.getLocation().getWorld() == this.location.getWorld() && p.getLocation().distance(this.location) <= this.rangeView) {
            inRange = true;
            this.sendLines(offlinePlayer.getPlayer());
        }

        this.receivers.put(offlinePlayer, inRange);

        return true;
    }

    /**
     * Remove hologram's receiver to this hologram.
     *
     * @param offlinePlayer Player
     * @return {@code true} is success
     */
    public boolean removeReceiver(OfflinePlayer offlinePlayer) {
        if (!offlinePlayer.isOnline())
            return false;

        this.receivers.remove(offlinePlayer);
        this.removeLines(offlinePlayer.getPlayer());

        return true;
    }

    /**
     * Remove a given line to a given player
     *
     * @param p    Player
     * @param line Line number
     * @return {@code true} is success
     */
    public boolean removeLineForPlayer(Player p, int line) {
        EntityArmorStand entity = this.entities.get(line);

        if (entity == null)
            return false;

        Reflection.sendPacket(p, entity.getDestroyPacket());

        return true;
    }

    /**
     * Remove lines to all players
     */
    public void removeLinesForPlayers() {
        for (OfflinePlayer offlinePlayer : this.receivers.keySet()) {
            if (!offlinePlayer.isOnline())
                continue;

            this.removeLines(offlinePlayer.getPlayer());
        }
    }

    /**
     * Destroy the hologram
     */
    public void destroy() {
        this.removeLinesForPlayers();

        this.clearEntities();
        this.clearLines();

        this.location = null;
    }

    /**
     * Destroy the hologram, it can't be
     * used anymore
     */
    public void fullDestroy() {
        this.destroy();
        this.receivers.clear();
        this.taskID.cancel();
    }

    /**
     * Update hologram's lines
     *
     * @param lines Hologram's lines
     */
    public void change(String... lines) {
        this.removeLinesForPlayers();

        this.clearEntities();
        this.clearLines();

        this.lines = new ArrayList<>();
        this.lines.addAll(Arrays.asList(lines));
        this.linesChanged = true;

        this.generateLines(this.location);
    }

    /**
     * Set hologram location
     *
     * @param location Location
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Generate hologram in world
     */
    public void generateLines() {
        this.generateLines(this.location);
    }

    /**
     * Generate hologram in world at the given
     * location
     *
     * @param loc Hologram's location
     */
    public void generateLines(Location loc) {
        Location first = loc.clone().add(0, (this.lines.size() / 2) * distance, 0);

        for (int i = 0; i < this.lines.size(); i++) {
            this.entities.put(i, generateEntitiesForLine(first.clone(), this.lines.get(i)));
            first.subtract(0, distance, 0);
        }

        this.location = loc;
    }

    /**
     * Send hologram's lines to all players
     */
    public void sendLinesForPlayers() {
        for (OfflinePlayer offlinePlayer : this.receivers.keySet()) {
            if (!offlinePlayer.isOnline())
                continue;

            Player p = offlinePlayer.getPlayer();
            boolean wasInRange = this.receivers.get(offlinePlayer);
            boolean inRange = false;

            if (p.getLocation().getWorld() == this.location.getWorld() && p.getLocation().distance(this.location) <= this.rangeView)
                inRange = true;

            if (this.linesChanged && inRange) {
                this.sendLines(p);
                this.linesChanged = false;
            } else if (wasInRange == inRange) {
                continue;
            } else if (wasInRange) {
                this.removeLines(p);
            } else {
                this.sendLines(p);
            }

            this.receivers.put(offlinePlayer, inRange);
        }
    }

    /**
     * Send hologram's lines to a given player
     *
     * @param p Player
     */
    public void sendLines(Player p) {
        for (int i = 0; i < this.lines.size(); i++)
            this.sendPacketForLine(p, i);
    }

    /**
     * Remove hologram's lines to a given player
     *
     * @param p Player
     */
    public void removeLines(Player p) {
        for (int i = 0; i < this.lines.size(); i++)
            this.removeLineForPlayer(p, i);
    }

    /**
     * Clear hologram's entities who permits
     * all the system to work
     */
    public void clearEntities() {
        this.entities.clear();
    }

    /**
     * Clear hologram's lines
     */
    public void clearLines() {
        this.lines.clear();
    }

    /**
     * Get hologram location
     *
     * @return Location
     */
    public Location getLocation() {
        return this.location;
    }

    private static EntityArmorStand generateEntitiesForLine(Location loc, String text) {
        EntityArmorStand entity = new EntityArmorStand(loc);
        entity.setSize(0.00001F, 0.00001F);
        entity.setInvisible(true);
        entity.setGravity(false);
        entity.setCustomName(text);
        entity.setCustomNameVisible(true);
        entity.setLocation(new Location(loc.getWorld(), loc.getX(), loc.getY() - 2, loc.getZ(), 0, 0));

        return entity;
    }

    private boolean sendPacketForLine(Player p, int line) {
        EntityArmorStand entity = this.entities.get(line);

        if (entity == null)
            return false;

        Reflection.sendPacket(p, entity.getSpawnPacket());
        Reflection.sendPacket(p, entity.getMetadataPacket());

        return true;
    }
}
