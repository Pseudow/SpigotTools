package net.pseudow.tools.npc;

import com.mojang.authlib.GameProfile;
import net.pseudow.tools.Hologram;
import net.pseudow.tools.nickname.NickNameManager;
import net.pseudow.tools.reflection.Reflection;
import net.pseudow.tools.reflection.entities.EntityLiving;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.util.*;

public class CustomNPC {
    // OTHERS
    private final HashMap<EntityLiving.EnumItemSlot, ItemStack> itemStacks;
    private final Set<OfflinePlayer> receivers;
    private Hologram hologram;
    private boolean visible;

    // INSTANCES
    private final JavaPlugin javaPlugin;

    // REFLECTION
    private final Constructor<?> packetPlayOutEntityTeleport;
    private final Constructor<?> packetPlayOutEntityDestroyConstructor;
    private final Constructor<?> packetPlayOutNamedEntitySpawnConstructor;
    private final Constructor<?> packetPlayOutEntityHeadRotation;
    private final Constructor<?> packetPlayOutEntityMetadata;
    private final Class<?> craftItemStackClass;
    private final Class<?> itemStackClass;

    // EVENTS
    private NPCInteractCallBack playerInteraction;

    // ENTITY INFORMATION
    private final Object entityPlayer;
    private World currentWorld;
    private Location location;
    private String npcName;

    /**
     * CustomNPC is a class using NMS by Reflection to allow multi-versioning.
     * This class has been created by Pseudow and it his under developement,
     * that means that there will be some other modifications.
     *
     * @author Pseudow
     *
     * @param javaPlugin The main instance of you plugin
     * @param npcName The name which will be displayed in the tab.
     * @param location The location you want to spawn it.
     * @param gameProfile The profile of the new player
     */
    public CustomNPC(JavaPlugin javaPlugin, String npcName, Location location, GameProfile gameProfile) {
        this.itemStacks = new HashMap<>();
        this.receivers = new HashSet<>();
        this.javaPlugin = javaPlugin;
        this.npcName = npcName;
        this.visible = true;

        // GET THE NMS OBJECTS
        final Class<?> entityLivingClass = Reflection.getNMSClass("EntityLiving");
        this.craftItemStackClass = Reflection.getOBCClass("inventory.CraftItemStack");
        this.itemStackClass = Reflection.getNMSClass("ItemStack");
        this.packetPlayOutEntityDestroyConstructor = Reflection.getConstructor(Objects.requireNonNull(Reflection.getNMSClass("PacketPlayOutEntityDestroy")), int[].class);
        this.packetPlayOutNamedEntitySpawnConstructor = Reflection.getConstructor(Objects.requireNonNull(Reflection.getNMSClass("PacketPlayOutNamedEntitySpawn")), Reflection.getNMSClass("EntityHuman"));
        this.packetPlayOutEntityTeleport = Reflection.getConstructor(Objects.requireNonNull(Reflection.getNMSClass("PacketPlayOutEntityTeleport")), entityLivingClass);
        this.packetPlayOutEntityHeadRotation = Reflection.getConstructor(Objects.requireNonNull(Reflection.getNMSClass("PacketPlayOutEntityHeadRotation")), Reflection.getNMSClass("Entity"), byte.class);
        this.packetPlayOutEntityMetadata = Reflection.getConstructor(Objects.requireNonNull(Reflection.getNMSClass("PacketPlayOutEntityMetadata")),
                int.class, Reflection.getNMSClass("DataWatcher"), boolean.class);

        // PREPARE THE FAKE PLAYER
        final Class<?> entityPlayerClass = Objects.requireNonNull(Reflection.getNMSClass("EntityPlayer")),
                minecraftServerClass = Objects.requireNonNull(Reflection.getNMSClass("MinecraftServer")),
                playerInteractManagerClass = Objects.requireNonNull(Reflection.getNMSClass("PlayerInteractManager"));
        final Constructor<?> playerInteractManagerConstructor = Reflection.getConstructor(playerInteractManagerClass, Reflection.getNMSClass("World"));
        final Constructor<?> entityPlayerConstructor = Reflection.getConstructor(entityPlayerClass, minecraftServerClass, Reflection.getNMSClass("WorldServer"), GameProfile.class, playerInteractManagerClass);
        final Object minecraftServer = Reflection.invokeStaticMethod(Reflection.getNMSClass("MinecraftServer"), "getServer");
        // CREATE FAKE PLAYER
        this.entityPlayer = Reflection.callConstructor(entityPlayerConstructor, minecraftServer, Reflection.getHandle(location.getWorld()),
                gameProfile, Reflection.callConstructor(playerInteractManagerConstructor, Reflection.getHandle(location.getWorld())));
        this.setLocation(location);
    }

    public CustomNPC setHologram(String... lines) {
        if(this.hologram == null) {
            this.hologram = new Hologram(this.javaPlugin, true, lines);
        }
        else this.hologram.change(lines);
        this.hologram.generateLines(this.location);
        this.receivers.forEach(receiver -> this.hologram.addReceiver(receiver));
        this.hologram.sendLinesForPlayers();
        return this;
    }

    public CustomNPC setInteractEvent(NPCInteractCallBack event) {
        this.playerInteraction = event;
        return this;
    }

    public CustomNPC setLocation(Location location) {
        Validate.notNull(location, "Error, the location cannot be null!");

        this.location = location;
        this.currentWorld = location.getWorld();
        Reflection.invokeMethod(entityPlayer, "setLocation", location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        Bukkit.getOnlinePlayers().forEach(player -> Reflection.sendPacket(player, this.getTeleportPacket()));

        if(this.hologram != null) {
            this.hologram.generateLines(this.location);
            this.hologram.sendLinesForPlayers();
        }

        return this;
    }

    public CustomNPC setNameVisible(boolean visible) {
        Reflection.invokeMethod(entityPlayer, "setCustomNameVisible", visible);
        return this;
    }

    public void setNpcName(String name) {
        NickNameManager.setNameInGameInfo(entityPlayer, name);
    }

    public CustomNPC setEquipment(EntityLiving.EnumItemSlot itemSlot, ItemStack itemStack) {
        this.itemStacks.put(itemSlot, itemStack);
        Bukkit.getOnlinePlayers().forEach(player -> Reflection.sendPacket(player, this.getEquipmentPacket(itemSlot, itemStack)));
        return this;
    }

    public CustomNPC setVisible(boolean visible) {
        if(visible)
            Bukkit.getOnlinePlayers().forEach(this::addReceiver);
        else Bukkit.getOnlinePlayers().forEach(this::removeReceiver);

        this.visible = visible;
        return this;
    }

    public CustomNPC addReceiver(@Nonnull OfflinePlayer player) {

        this.receivers.add(player.getPlayer());

        if(this.hologram != null)
            this.hologram.addReceiver(player);

        this.getSpawnPackets().forEach(packet -> Reflection.sendPacket(player.getPlayer(), packet));
        return this;
    }

    public CustomNPC removeReceiver(@Nonnull OfflinePlayer player) {
        this.receivers.remove(player.getPlayer());

        if(this.hologram != null) {
            this.hologram.removeLines(player.getPlayer());
            this.hologram.removeReceiver(player);
        }

        this.getDestroyPackets().forEach(packet -> Reflection.sendPacket(player.getPlayer(), packet));
        return this;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean isReceiver(Player player) {
        return this.receivers.contains(player);
    }

    public int getId() {
        return (Integer) Objects.requireNonNull(Reflection.invokeMethod(entityPlayer, "getId"));
    }

    protected ArrayList<Object> getSpawnPackets() {
        ArrayList<Object> packets = new ArrayList<>();
        packets.add(Reflection.callConstructor(NickNameManager.getPacketPlayOutPlayerInfoConstructor(),
                Reflection.getEnumValue(NickNameManager.getEnumPlayerInfoAction(), "ADD_PLAYER"),
                Collections.singletonList(entityPlayer)));
        packets.add(Reflection.callConstructor(packetPlayOutNamedEntitySpawnConstructor, entityPlayer));
        this.itemStacks.forEach((itemSlot, itemStack) -> packets.add(this.getEquipmentPacket(itemSlot, itemStack)));
        return packets;
    }

    protected ArrayList<Object> getDestroyPackets() {
        ArrayList<Object> packets = new ArrayList<>();
        packets.add(Reflection.callConstructor(NickNameManager.getPacketPlayOutPlayerInfoConstructor(),
                Reflection.getEnumValue(NickNameManager.getEnumPlayerInfoAction(), "REMOVE_PLAYER"),
                Collections.singletonList(entityPlayer)));
        packets.add(Reflection.callConstructor(packetPlayOutEntityDestroyConstructor, (Object) new int[]{getId()}));
        return packets;
    }

    private Object getTeleportPacket() {
        return Reflection.callConstructor(packetPlayOutEntityTeleport, entityPlayer);
    }

    private Object getHeadPacket(byte yaw) {
        return Reflection.callConstructor(packetPlayOutEntityHeadRotation, this.entityPlayer, yaw);
    }

    private Object getMetadataPacket() {
        return Reflection.callConstructor(packetPlayOutEntityMetadata, getId(), Reflection.invokeMethod(this.entityPlayer, "getDataWatcher"), true);
    }

    private Object getEquipmentPacket(EntityLiving.EnumItemSlot slot, Object itemStack) {
        Object packet = null;

        try {
            final Constructor<?> packetPlayOutEntityEquipment = Reflection.getConstructor(Objects.requireNonNull(Reflection.getNMSClass("PacketPlayOutEntityEquipment")), int.class, int.class, this.itemStackClass);
            packet = Reflection.callConstructor(packetPlayOutEntityEquipment, getId(), slot.getSlot(), Reflection.invokeStaticMethod(craftItemStackClass, "asNMSCopy", itemStack));
        } catch (Exception exception) {
            final Constructor<?> packetPlayOutEntityEquipment = Reflection.getConstructor(Objects.requireNonNull(Reflection.getNMSClass("PacketPlayOutEntityEquipment")), int.class, Reflection.getNMSClass("EnumItemSlot"), this.itemStackClass);
            packet = Reflection.callConstructor(packetPlayOutEntityEquipment, getId(), slot.getEnum(), Reflection.invokeStaticMethod(craftItemStackClass, "asNMSCopy", itemStack));
        }

        return packet;
    }

    public Hologram getHologram() {
        return this.hologram;
    }

    public NPCInteractCallBack getInteractCallBack() {
        return this.playerInteraction;
    }

    public Location getLocation() {
        return this.location;
    }

    public Object getEntityPlayer() {
        return this.entityPlayer;
    }

    public World getCurrentWorld() {
        return this.currentWorld;
    }

    public String getNPCName() {
        return this.npcName;
    }
}
