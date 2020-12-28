package net.pseudow.tools;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.pseudow.tools.reflection.Reflection;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Constructor;
import java.util.Objects;

public class ItemUtils {
    /**
     * Open the given written book
     *
     * @author Pseudow
     *
     * @param book - Written book
     * @param player - Player
     */
    public static void openWrittenBook(ItemStack book, Player player) {
        if (book.getType() != Material.WRITTEN_BOOK)
            return;

        ItemStack previous = player.getInventory().getItemInHand();
        player.getInventory().setItemInHand(book);

        ByteBuf buffer = Unpooled.buffer(256);
        buffer.setByte(0, (byte) 1);
        buffer.writerIndex(1);

        Constructor<?> packetPlayOutCustomPlayloadConstructor = Reflection.getConstructor(
                Objects.requireNonNull(Reflection.getNMSClass("PacketPlayOutCustomPayload")),
                String.class, Reflection.getNMSClass("PacketDataSerializer"));
        Constructor<?> packetDataSerializerConstructor = Reflection.getConstructor(
                Objects.requireNonNull(Reflection.getNMSClass("PacketDataSerializer")),
                ByteBuf.class);

        Reflection.sendPacket(player, Reflection.callConstructor(packetPlayOutCustomPlayloadConstructor,
                "MC|BOpen", Reflection.callConstructor(packetDataSerializerConstructor, buffer)));

        player.getInventory().setItemInHand(previous);
    }

    /**
     * Format a given item stack into a formatted string
     *
     * @author Pseudow
     *
     * @param stack - Stack
     * @return Formatted string
     */
    public static String stackToStr(ItemStack stack) {
        return stack.getType().name() + ", " + stack.getAmount() + ", " + stack.getDurability();
    }

    /**
     * Format a given formatted string into itemstack
     *
     * @author Pseudow
     *
     * @param string - Formatted string
     * @return Stack
     */
    public static ItemStack strToStack(String string) {
        String[] data = string.split(", ");
        return new ItemStack(Material.matchMaterial(data[0]), Integer.parseInt(data[1]), Short.parseShort(data[2]));
    }

    /**
     * Hide all the special attributes of an itemstack
     *
     * @author Pseudow
     *
     * @param itemStack - The stack
     * @return Cleaned stack
     */
    public static ItemStack hideAllAttributes(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();

        for (ItemFlag itemFlag : ItemFlag.values())
            if (itemFlag.name().startsWith("HIDE_"))
                meta.addItemFlags(itemFlag);

        itemStack.setItemMeta(meta);

        return itemStack;
    }

    /**
     * Get the given player's username head
     *
     * @author Pseudow
     *
     * @param player - Player's username
     * @return Player's head
     */
    public static ItemStack getPlayerHead(String player) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwner(player);
        head.setItemMeta(meta);

        return head;
    }

}
