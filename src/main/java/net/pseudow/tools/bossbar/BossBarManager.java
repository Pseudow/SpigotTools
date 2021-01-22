package net.pseudow.tools.bossbar;

import net.minecraft.server.v1_8_R3.*;
import net.pseudow.tools.reflection.Reflection;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class BossBarManager implements Runnable {
    private final HashMap<Player, EntityWither> bossBars;

    private final HashMap<UUID, AtomicInteger> actualMessages;
    private final HashMap<UUID, List<String>> messages;

    private final List<String> defaultMessages;

    private final int delay;
    private int cooldown;

    /**
     * This class allows you to create bossbars in minecraft using Spigot-NMS.
     * You shouldn't use this class in a version above 1.8 because Spigot already
     * implement BossBar utilities. Also, be careful this boss bar is visible when
     * you are in Spectator mode!
     *
     * @author Pseudow
     *
     * @param plugin The main of your current plugin.
     * @param defaultMessages The message which will be displayed to the players.
     * @param delay The delay between messages. Be careful, this delay is in seconds.
     *
     */
    public BossBarManager(JavaPlugin plugin, List<String> defaultMessages, int delay) {
        this.defaultMessages = defaultMessages;
        this.delay = delay;
        this.cooldown = 0;

        this.bossBars = new HashMap<>();
        this.actualMessages = new HashMap<>();
        this.messages = new HashMap<>();

        plugin.getServer().getScheduler().runTaskTimer(plugin, this, 0, 20);
    }

    /**
     * Use this method in your plugin disable method!
     */
    public void onDisable() {
        for(Player player : bossBars.keySet())
            onLogout(player);
    }

    /**
     * This method allows you to display the bossbar to a player.
     *
     * @author Pseudow
     *
     * @param player The player you want to make boss bar visible.
     */
    public void onLogin(Player player) {
        World world = ((CraftWorld) player.getWorld()).getHandle();

        EntityWither entityWither = new EntityWither(world);
        Location l = getWitherLocation(player.getLocation());

        entityWither.setInvisible(true);
        entityWither.setCustomNameVisible(true);
        entityWither.setCustomName("");
        entityWither.setLocation(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());

        Reflection.sendPacket(player, new PacketPlayOutSpawnEntityLiving(entityWither));

        this.bossBars.put(player, entityWither);
        this.messages.put(player.getUniqueId(), this.defaultMessages);
        this.actualMessages.put(player.getUniqueId(), new AtomicInteger(0));
    }

    /**
     * This method allows you to remove the bossbar's visibility to a  player.
     *
     * @author Pseudow
     *
     * @param player The player you want to make boss bar invisible.
     */
    public void onLogout(Player player) {
        EntityWither entityWither = this.bossBars.remove(player);
        this.messages.remove(player.getUniqueId());
        this.actualMessages.remove(player.getUniqueId());

        if(entityWither == null) return;

        Reflection.sendPacket(player, new PacketPlayOutEntityDestroy(entityWither.getId()));
    }

    /**
     * Replace old message by a new.
     *
     * @author Pseudow
     *
     * @param player Change the boss bar title.
     * @param newMessages The title list you want to have now.
     */
    public void setTitle(Player player, List<String> newMessages) {
        this.messages.put(player.getUniqueId(), newMessages);
        this.actualMessages.put(player.getUniqueId(), new AtomicInteger(0));
    }

    /**
     * Set the boss bar level for a player.
     *
     * @author Pseudow
     *
     * @param player The player you want to change its bossbar's progress.
     * @param progress The progress number. 1 is the hole bossbar, 0.8 is 80% of the bossbar.
     *
     * @deprecated Be careful, if the boss bar progress reaches its half pv, it will display
     * a "shield" around it. I can remove it but i can't put the code here sorry so please don't dm
     * me if you want to know how to solve this problem.
     */
    @Deprecated
    public void setProgress(Player player, double progress) {
        EntityWither entityWither = this.bossBars.get(player);
        entityWither.setHealth((float) progress * entityWither.getMaxHealth());
        Reflection.sendPacket(player, new PacketPlayOutEntityMetadata(entityWither.getId(), entityWither.getDataWatcher(), true));
    }

    @Override
    public void run() {
        this.bossBars.forEach((player, entityWither) -> {
            Location l = getWitherLocation(player.getLocation());
            entityWither.setLocation(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
            Reflection.sendPacket(player, new PacketPlayOutEntityTeleport(entityWither));

        });

        this.cooldown += 1;
        if(this.cooldown == this.delay) {
            this.bossBars.forEach((player, entityWither) -> {
                List<String> messages = this.messages.get(player.getUniqueId());

                if (messages == null)
                    messages = this.defaultMessages;

                AtomicInteger atomicInteger = this.actualMessages.get(player.getUniqueId());

                entityWither.setCustomName(messages.get(atomicInteger.get()));
                entityWither.setCustomNameVisible(true);

                Reflection.sendPacket(player, new PacketPlayOutEntityMetadata(entityWither.getId(), entityWither.getDataWatcher(), true));

                if (atomicInteger.addAndGet(1) >= messages.size())
                    atomicInteger.set(0);
            });
            this.cooldown = 0;
        }
    }

    private Location getWitherLocation(Location l) {
        return l.add(l.getDirection().multiply(32));
    }
}
