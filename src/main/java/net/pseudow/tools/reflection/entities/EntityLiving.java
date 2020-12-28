package net.pseudow.tools.reflection.entities;

import net.pseudow.tools.reflection.Reflection;
import org.bukkit.Location;

import java.lang.reflect.Constructor;
import java.util.Objects;

public class EntityLiving {
    private final Class<?> craftItemStackClass;
    private final Class<?> entityLivingClass;
    private final Class<?> itemStack;
    private final Object entityLiving;

    /**
     * Create a NMS {@link net.minecraft.server.v1_8_R3.Entity} with multi-versioning using {@link Reflection}.
     * For example if you want to spawn a EntityWolf, use new EntityLiving(Reflection.getNMSClass("EntityWolf"), Location);
     *
     * @param entityClass - The entity nms class.
     * @param location    - the location you want to spawn it.
     * @author Pseudow
     */

    public EntityLiving(Class<?> entityClass, Location location) {
        this.entityLivingClass = Reflection.getNMSClass("EntityLiving");
        this.craftItemStackClass = Reflection.getOBCClass("inventory.CraftItemStack");
        this.itemStack = Reflection.getNMSClass("ItemStack");

        Object craftWorldObject = Objects.requireNonNull(Reflection.getOBCClass("CraftWorld")).cast(location.getWorld());
        Object nmsWorld = Reflection.getHandle(craftWorldObject);

        assert nmsWorld != null;

        Constructor<?> constructor = Reflection.getConstructor(entityClass, nmsWorld.getClass());

        this.entityLiving = Reflection.callConstructor(constructor, nmsWorld);
    }

    private int getId() {
        return (Integer) Objects.requireNonNull(Reflection.invokeMethod(entityLiving, "getId"));
    }

    public void setLocation(Location loc) {
        Reflection.invokeMethod(entityLiving, "setLocation", loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public boolean isInvisible() {
        return (boolean) Objects.requireNonNull(Reflection.invokeMethod(entityLiving, "isInvisible"));
    }

    public void setInvisible(boolean option) {
        Reflection.invokeMethod(entityLiving, "setInvisible", option);
    }

    public String getCustomName() {
        return (String) Objects.requireNonNull(Reflection.invokeMethod(entityLiving, "getCustomName"));
    }

    public void setCustomName(String customName) {
        Reflection.invokeMethod(entityLiving, "setCustomName", customName);
    }

    public void setCustomNameVisible(boolean option) {
        Reflection.invokeMethod(entityLiving, "setCustomNameVisible", option);
    }

    public Object getTeleportPacket() {
        Constructor<?> packetPlayOutEntityTeleport = Reflection.getConstructor(Objects.requireNonNull(Reflection.getNMSClass("PacketPlayOutEntityTeleport")), entityLivingClass);
        return Reflection.callConstructor(packetPlayOutEntityTeleport, entityLiving);
    }

    public Object getHeadPacket(byte yaw) {
        Constructor<?> packetPlayOutEntityHeadRotation = Reflection.getConstructor(Objects.requireNonNull(Reflection.getNMSClass("PacketPlayOutEntityHeadRotation")), Reflection.getNMSClass("Entity"), byte.class);
        return Reflection.callConstructor(packetPlayOutEntityHeadRotation, entityLiving, yaw);
    }

    public Object getMetadataPacket() {
        Constructor<?> packetPlayOutEntityMetadata = Reflection.getConstructor(Objects.requireNonNull(Reflection.getNMSClass("PacketPlayOutEntityMetadata")),
                int.class, Reflection.getNMSClass("DataWatcher"), boolean.class);
        return Reflection.callConstructor(packetPlayOutEntityMetadata, getId(), Reflection.invokeMethod(entityLiving, "getDataWatcher"), true);
    }

    public Object getEquipmentPacket(EnumItemSlot slot, Object itemStack) {
        Object packet = null;

        if (Reflection.getVersion().contains("8")) {
            Constructor<?> packetPlayOutEntityEquipment = Reflection.getConstructor(Objects.requireNonNull(Reflection.getNMSClass("PacketPlayOutEntityEquipment")), int.class, int.class, this.itemStack);
            packet = Reflection.callConstructor(packetPlayOutEntityEquipment, getId(), slot.getSlot(), Reflection.invokeStaticMethod(craftItemStackClass, "asNMSCopy", itemStack));
        } else if (Reflection.getVersion().contains("12")) {
            Constructor<?> packetPlayOutEntityEquipment = Reflection.getConstructor(Objects.requireNonNull(Reflection.getNMSClass("PacketPlayOutEntityEquipment")), int.class, Reflection.getNMSClass("EnumItemSlot"), this.itemStack);
            packet = Reflection.callConstructor(packetPlayOutEntityEquipment, getId(), slot.getEnum(), Reflection.invokeStaticMethod(craftItemStackClass, "asNMSCopy", itemStack));

        }
        return packet;
    }

    public Object getSpawnPacket() {
        Constructor<?> packetPlayOutSpawnEntityLiving = Reflection.getConstructor(Objects.requireNonNull(Reflection.getNMSClass("PacketPlayOutSpawnEntityLiving")), entityLivingClass);
        return Reflection.callConstructor(packetPlayOutSpawnEntityLiving, entityLiving);
    }

    public Object getDestroyPacket() {
        Constructor<?> packetPlayOutEntityDestroy = Reflection.getConstructor(Objects.requireNonNull(Reflection.getNMSClass("PacketPlayOutEntityDestroy")), int[].class);
        return Reflection.callConstructor(packetPlayOutEntityDestroy, (Object) new int[]{getId()});
    }

    public Object getEntityLiving() {
        return entityLiving;
    }

    public enum EnumItemSlot {
        MAIN_HAND("mainhand", 0),
        OFF_HAND("offhand", 5),
        BOOTS("feet", 4),
        LEGGINS("legs", 3),
        CHESTPLATE("chest", 2),
        HELMET("head", 1);

        private final String name;
        private final int slot;

        EnumItemSlot(String name, int slot) {
            this.name = name;
            this.slot = slot;
        }

        public Object getEnum() {
            return Reflection.invokeStaticMethod(Reflection.getNMSClass("EnumItemSlot"), "a", name);
        }

        public int getSlot() {
            return slot;
        }
    }
}