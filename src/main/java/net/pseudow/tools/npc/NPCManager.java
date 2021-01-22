package net.pseudow.tools.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import io.netty.channel.*;
import net.pseudow.tools.reflection.Reflection;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

public class NPCManager implements Listener {
    private final Class<?> packetPlayInUseEntityClass;

    private final HashSet<CustomNPC> customNPCs;
    private final JavaPlugin javaPlugin;

    private int distance;

    /**
     * A class which allows you to create custom npc.
     *
     * @param javaPlugin The main instance of you plugin.
     * @author Pseudow
     */
    public NPCManager(JavaPlugin javaPlugin) {
        this.customNPCs = new HashSet<>();
        this.javaPlugin = javaPlugin;
        this.distance = 2500;

        this.packetPlayInUseEntityClass = Objects.requireNonNull(Reflection.getNMSClass("PacketPlayInUseEntity"));

        this.javaPlugin.getServer().getPluginManager().registerEvents(this, javaPlugin);
    }

    public CustomNPC createNPC(String npcName, Location location) {
        return this.createNPC(npcName, location, new GameProfile(UUID.randomUUID(), npcName));
    }

    /**
     * @param npcName  The name of the npc
     * @param location The location you want to spawn it
     * @param playerId The player's id of the skin and cape
     * @return a new NPC
     * @apiNote PLEASE USE THIS METHOD ASYNCHRONOUSLY
     */
    public CustomNPC createNPC(String npcName, Location location, UUID playerId) {
        final GameProfile gameProfile = this.fillGameProfile(playerId);
        Reflection.setField(Reflection.getField(GameProfile.class, "name"), gameProfile, npcName);
        Reflection.setField(Reflection.getField(GameProfile.class, "id"), gameProfile, UUID.randomUUID());
        return this.createNPC(npcName, location, gameProfile);
    }

    public CustomNPC createNPC(String npcName, Location location, GameProfile gameProfile) {
        final CustomNPC customNPC = new CustomNPC(this.javaPlugin, npcName, location, gameProfile);
        this.customNPCs.add(customNPC);
        return customNPC;
    }

    public CustomNPC getNPCByName(String npcName) {
        for (CustomNPC customNPC : this.customNPCs)
            if (customNPC.getNPCName().equalsIgnoreCase(npcName))
                return customNPC;
        return null;
    }

    public boolean exists(String npcName) {
        return this.customNPCs.stream().anyMatch(npc -> npc.getNPCName().equalsIgnoreCase(npcName));
    }

    public void removeNPC(CustomNPC customNPC) {
        this.javaPlugin.getServer().getOnlinePlayers().stream().filter(player -> player.getWorld() == customNPC.getCurrentWorld())
                .forEach(player -> customNPC.getDestroyPackets().forEach(packet -> Reflection.sendPacket(player, packet)));
        customNPC.getHologram().fullDestroy();
        this.customNPCs.remove(customNPC);
    }

    public void onDisable() {
        for (CustomNPC customNPC : this.customNPCs) {
            this.removeNPC(customNPC);
        }

    }

    @EventHandler(priority = EventPriority.LOW)
    private void onPlayerMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        for (CustomNPC customNPC : this.customNPCs) {
            switch (this.canBeDisplayed(player, event.getFrom(), event.getTo(), customNPC)) {
                case 1:
                    customNPC.getSpawnPackets().forEach(packet -> Reflection.sendPacket(player, packet));
                    customNPC.getHologram().sendLines(player);
                    break;
                case 2:
                    customNPC.getDestroyPackets().forEach(packet -> Reflection.sendPacket(player, packet));
                    customNPC.getHologram().removeLines(player);
                    break;
                default:
                    break;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onPlayerTeleport(PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        for (CustomNPC customNPC : this.customNPCs) {
            switch (this.canBeDisplayed(player, event.getFrom(), event.getTo(), customNPC)) {
                case 1:
                    customNPC.getSpawnPackets().forEach(packet -> Reflection.sendPacket(player, packet));
                    customNPC.getHologram().sendLines(player);
                    break;
                case 2:
                    customNPC.getDestroyPackets().forEach(packet -> Reflection.sendPacket(player, packet));
                    customNPC.getHologram().removeLines(player);
                    break;
                default:
                    break;
            }
        }
    }

    private int canBeDisplayed(Player player, Location from, Location to, CustomNPC customNPC) {
        if (!customNPC.isReceiver(player))
            return 2;
        if (customNPC.getCurrentWorld() == to.getWorld())
            if (from.distanceSquared(customNPC.getLocation()) > this.distance &&
                    to.distanceSquared(customNPC.getLocation()) < this.distance)
                return 1;
            else if (from.distanceSquared(customNPC.getLocation()) < this.distance &&
                    to.distanceSquared(customNPC.getLocation()) > this.distance)
                return 2;
        return 0;
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        this.injectPlayer(player);

        this.customNPCs.stream().filter(CustomNPC::isVisible).forEach(customNPC -> customNPC.addReceiver(player));
        this.customNPCs.stream().filter(customNPC -> customNPC.isReceiver(player)).forEach(customNPC ->
                customNPC.getSpawnPackets().forEach(packet -> Reflection.sendPacket(player, packet)));
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final Channel channel = ((Channel) Reflection.invokeField(Reflection.invokeField(Reflection.invokeField(Reflection.getHandle(player), "playerConnection"), "networkManager"), "channel"));
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(player.getName());
        });

        this.customNPCs.stream().filter(customNPC -> customNPC.isVisible() || customNPC.isReceiver(player)).forEach(customNPC ->
                customNPC.getDestroyPackets().forEach(packet -> Reflection.sendPacket(player, packet)));
    }

    private void injectPlayer(Player player) {
        final ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {

            @Override
            public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
                if (packet.getClass() == packetPlayInUseEntityClass) {
                    final int entityId = (int) Reflection.invokeField(packet, "a");
                    customNPCs.stream().filter(customNPC -> customNPC.isReceiver(player) && customNPC.getId() == entityId).findFirst().ifPresent(customNPC -> {
                        if (customNPC.getInteractCallBack() != null) {
                            final Object action = Reflection.invokeField(packet, "action");

                            if(Reflection.getEnumValue((Class<Enum>) Reflection.getNMSClass("PacketPlayInUseEntity$EnumEntityUseAction"), "INTERACT").equals(action))
                                customNPC.getInteractCallBack().fire(true, player);
                            else
                                customNPC.getInteractCallBack().fire(false, player);
                        }
                    });
                }
                super.channelRead(channelHandlerContext, packet);
            }

            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
                super.write(channelHandlerContext, packet, channelPromise);
            }
        };

        final ChannelPipeline pipeline = ((Channel) Reflection.invokeField(Reflection.invokeField(Reflection.invokeField(Reflection.getHandle(player), "playerConnection"), "networkManager"), "channel")).pipeline();
        pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);
    }

    public void setViewDistance(int distance) {
        this.distance = distance;
    }

    private GameProfile fillGameProfile(UUID playerId) {
        final Class<?> minecraftServerClass = Reflection.getNMSClass("MinecraftServer");
        final Object minecraftServer = Objects.requireNonNull(Reflection.invokeStaticMethod(minecraftServerClass, "getServer"));
        final MinecraftSessionService service = (MinecraftSessionService) Objects.requireNonNull(Reflection.invokeMethod(minecraftServer, "aD"));
        return service.fillProfileProperties(new GameProfile(playerId, null), true);
    }
}
