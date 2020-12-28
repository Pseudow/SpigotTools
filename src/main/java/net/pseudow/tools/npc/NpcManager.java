package net.pseudow.tools.npc;

import net.pseudow.tools.nickname.NickNameManager;
import net.pseudow.tools.reflection.Reflection;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.shanerx.mojang.Mojang;
import org.shanerx.mojang.PlayerProfile;

import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class NpcManager implements Listener {
    private final HashSet<CustomNPC> customNPCs;
    private final JavaPlugin javaPlugin;
    private final Mojang api;

    /**
     * @author Pseudow
     * @param javaPlugin
     *
     * @deprecated This class doesn't work yet! # Soon
     */
    @Deprecated
    public NpcManager(JavaPlugin javaPlugin) {
        this.api = new Mojang().connect();
        this.customNPCs = new HashSet<>();
        this.javaPlugin = javaPlugin;

        this.javaPlugin.getServer().getPluginManager().registerEvents(this, javaPlugin);
        this.javaPlugin.getServer().getScheduler().runTaskTimer(javaPlugin, () -> {
            this.javaPlugin.getServer().getOnlinePlayers().forEach(player ->
                    this.customNPCs.stream().filter(customNPC -> customNPC.getCurrentWorld() == player.getWorld())
                            .forEach(customNPC -> Reflection.sendPacket(player, customNPC.getTeleportPacket())));
        }, 0, 20);
    }

    public CustomNPC createNPC(String npcName, Location location, UUID skinOwner, Object playerInteractManager) {
        PlayerProfile playerProfile = this.api.getPlayerProfile(skinOwner.toString().replaceAll("-", ""));
        AtomicReference<String> skinUrl = new AtomicReference<>();

        playerProfile.getTextures().flatMap(PlayerProfile.TexturesProperty::getSkin).ifPresent(skin -> skinUrl.set(skin.toString()));
        CustomNPC customNPC = new CustomNPC(npcName, location, skinOwner, skinUrl.get(), playerInteractManager);

        this.customNPCs.add(customNPC);
        this.javaPlugin.getServer().getOnlinePlayers().stream().filter(player -> player.getWorld() == location.getWorld())
                .forEach(player -> this.spawnNPCToPlayer(customNPC, player));

        return customNPC;
    }

    public void removeNPC(CustomNPC customNPC) {
        this.javaPlugin.getServer().getOnlinePlayers().stream().filter(player -> player.getWorld() == customNPC.getCurrentWorld())
                .forEach(player -> this.destroyNPCToPlayer(customNPC, player));
        this.customNPCs.remove(customNPC);
    }

    public void onDisable() {
        this.customNPCs.forEach(this::removeNPC);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        this.customNPCs.stream().filter(customNPC -> customNPC.getCurrentWorld() == player.getWorld())
                .forEach(customNPC -> this.spawnNPCToPlayer(customNPC, player));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        this.customNPCs.stream().filter(customNPC -> customNPC.getCurrentWorld() == player.getWorld())
                .forEach(customNPC -> this.destroyNPCToPlayer(customNPC, player));
    }

    @EventHandler
    public void onSwitchWorld(PlayerChangedWorldEvent event) {
        final Player player = event.getPlayer();
        this.customNPCs.forEach(customNPC -> {
            if(customNPC.getCurrentWorld() == player.getWorld()) {
                this.spawnNPCToPlayer(customNPC, player);
            } else {
                this.destroyNPCToPlayer(customNPC, player);
            }
        });
    }

    private void spawnNPCToPlayer(CustomNPC customNPC, Player player) {
        customNPC.getSpawnPackets().forEach(packet -> Reflection.sendPacket(player, packet));

        Object packet = Reflection.callConstructor(NickNameManager.getPacketPlayOutPlayerInfoConstructor(),
                Reflection.getEnumValue(NickNameManager.getEnumPlayerInfoAction(), "ADD_PLAYER"),
                Collections.singletonList(customNPC.getEntityPlayer()));
        Reflection.sendPacket(player, packet);
    }

    private void destroyNPCToPlayer(CustomNPC customNPC, Player player) {
        Reflection.sendPacket(player, customNPC.getDestroyPacket());

        Object packet = Reflection.callConstructor(NickNameManager.getPacketPlayOutPlayerInfoConstructor(),
                Reflection.getEnumValue(NickNameManager.getEnumPlayerInfoAction(), "REMOVE_PLAYER"),
                Collections.singletonList(customNPC.getEntityPlayer()));
        Reflection.sendPacket(player, packet);
    }
}
