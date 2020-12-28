package net.pseudow.tools.npc;

import com.mojang.authlib.GameProfile;
import net.pseudow.tools.nickname.NickNameManager;
import net.pseudow.tools.reflection.Reflection;
import net.pseudow.tools.reflection.entities.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class CustomNPC {
    private final HashMap<EntityLiving.EnumItemSlot, ItemStack> itemStacks;
    private final Class<?> craftItemStackClass;
    private final Class<?> entityLivingClass;
    private final Class<?> itemStackClass;
    private final Object entityPlayer;
    private final World currentWorld;

    /**
     * CustomNPC is a class using NMS by Reflection to allow multi-versionning.
     * This class has been created by Pseudow and it his under developpement,
     * that means that there will be some other modifications.
     *
     * @author Pseudow
     *
     * @param npcName - The name which will be displayed in the tab.
     * @param location - The location you want to spawn it.
     * @param skinOwner - The owner of the npc.
     * @param skinUrl - The url of the skin.
     * @param playerInteractManager - What will happen when we will interact.
     */
    public CustomNPC(String npcName, Location location, UUID skinOwner, String skinUrl, Object playerInteractManager) {
        this.itemStacks = new HashMap<>();
        this.entityLivingClass = Reflection.getNMSClass("EntityLiving");
        this.craftItemStackClass = Reflection.getOBCClass("inventory.CraftItemStack");
        this.itemStackClass = Reflection.getNMSClass("ItemStack");
        this.currentWorld = location.getWorld();
        final Class<?> entityPlayerClass = Objects.requireNonNull(Reflection.getNMSClass("EntityPlayer")),
                minecraftServerClass = Objects.requireNonNull(Reflection.getNMSClass("MinecraftServer")),
                playerInteractManagerClass = Objects.requireNonNull(Reflection.getNMSClass("PlayerInteractManager"));
        final Constructor<?> entityPlayerConstructor = Reflection.getConstructor(entityPlayerClass, minecraftServerClass, Reflection.getNMSClass("WorldServer"), GameProfile.class, playerInteractManagerClass);
        final Object minecraftServer = Reflection.invokeStaticMethod(Reflection.getNMSClass("MinecraftServer"), "getServer");

        this.entityPlayer = Reflection.callConstructor(entityPlayerConstructor, minecraftServer, Reflection.getHandle(location.getWorld()),
                GameProfileBuilder.getProfile(skinOwner, npcName, skinUrl), playerInteractManager);

        this.setLocation(location);
    }

    public void setLocation(Location location) {
        Reflection.invokeMethod(entityPlayer, "setLocation", location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public void setNpcName(String name) {
        NickNameManager.setNameInGameInfo(entityPlayer, name);
    }

    private int getId() {
        return (Integer) Objects.requireNonNull(Reflection.invokeMethod(entityPlayer, "getId"));
    }

    public ArrayList<Object> getSpawnPackets() {
        Constructor<?> packetPlayOutSpawnEntityLiving = Reflection.getConstructor(Objects.requireNonNull(Reflection.getNMSClass("PacketPlayOutSpawnEntityLiving")), entityLivingClass);
        ArrayList<Object> packets = new ArrayList<>();
        packets.add(Reflection.callConstructor(packetPlayOutSpawnEntityLiving, entityPlayer));
        this.itemStacks.forEach((itemSlot, itemStack) -> packets.add(this.getEquipmentPacket(itemSlot, itemStack)));
        return packets;
    }

    public Object getDestroyPacket() {
        Constructor<?> packetPlayOutEntityDestroy = Reflection.getConstructor(Objects.requireNonNull(Reflection.getNMSClass("PacketPlayOutEntityDestroy")), int[].class);
        return Reflection.callConstructor(packetPlayOutEntityDestroy, (Object) new int[]{getId()});
    }

    public Object getTeleportPacket() {
        Constructor<?> packetPlayOutEntityTeleport = Reflection.getConstructor(Objects.requireNonNull(Reflection.getNMSClass("PacketPlayOutEntityTeleport")), entityLivingClass);
        return Reflection.callConstructor(packetPlayOutEntityTeleport, entityPlayer);
    }

    private Object getEquipmentPacket(EntityLiving.EnumItemSlot slot, Object itemStack) {
        Object packet = null;

        if (Reflection.getVersion().contains("8")) {
            Constructor<?> packetPlayOutEntityEquipment = Reflection.getConstructor(Objects.requireNonNull(Reflection.getNMSClass("PacketPlayOutEntityEquipment")), int.class, int.class, this.itemStackClass);
            packet = Reflection.callConstructor(packetPlayOutEntityEquipment, getId(), slot.getSlot(), Reflection.invokeStaticMethod(craftItemStackClass, "asNMSCopy", itemStack));
        } else if (Reflection.getVersion().contains("12")) {
            Constructor<?> packetPlayOutEntityEquipment = Reflection.getConstructor(Objects.requireNonNull(Reflection.getNMSClass("PacketPlayOutEntityEquipment")), int.class, Reflection.getNMSClass("EnumItemSlot"), this.itemStackClass);
            packet = Reflection.callConstructor(packetPlayOutEntityEquipment, getId(), slot.getEnum(), Reflection.invokeStaticMethod(craftItemStackClass, "asNMSCopy", itemStack));

        }
        return packet;
    }

    public void setEquipment(EntityLiving.EnumItemSlot itemSlot, ItemStack itemStack) {
        this.itemStacks.put(itemSlot, itemStack);
        Bukkit.getOnlinePlayers().forEach(player -> Reflection.sendPacket(player, this.getEquipmentPacket(itemSlot, itemStack)));
    }

    public Object getEntityPlayer() {
        return this.entityPlayer;
    }

    public World getCurrentWorld() {
        return this.currentWorld;
    }
}
