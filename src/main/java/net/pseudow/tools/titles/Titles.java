package net.pseudow.tools.titles;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.pseudow.tools.reflection.Reflection;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Titles {

    /**
     * Please use this method in 1.8.8, in a version above, Spigot has already
     * methods to use instead of this one.
     *
     * @author SamaGames
     *
     * @param player - The player you want to display the title.
     * @param fadeIn - The time the title will fade in.
     * @param stay - The time the title will stay.
     * @param fadeOut - The time the title will disappear.
     * @param title - The title you want to send.
     * @param subtitle - The subtitle you want to send.
     */
    public static void sendTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
        Reflection.sendPacket(player, new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, null, fadeIn, stay, fadeOut));

        if (subtitle != null) {
            subtitle = subtitle.replaceAll("%player%", player.getDisplayName());
            subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);

            Reflection.sendPacket(player, new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + subtitle + "\"}")));
        }

        if (title != null) {
            title = title.replaceAll("%player%", player.getDisplayName());
            title = ChatColor.translateAlternateColorCodes('&', title);

            Reflection.sendPacket(player, new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + title + "\"}")));
        }
    }

    /**
     * Please use this method in 1.8.8, in a version above, Spigot has already
     * methods to use instead of this one.
     *
     * @author SamaGames
     *
     * @param player - The player you want to display the tab.
     * @param header - The text which will be displayed in top of the tab.
     * @param footer - The text which will be displayed below of the tab.
     */
    public static void sendTabTitle(Player player, String header, String footer) {
        if (header == null) header = "";
        header = ChatColor.translateAlternateColorCodes('&', header);

        if (footer == null) footer = "";
        footer = ChatColor.translateAlternateColorCodes('&', footer);

        header = header.replaceAll("%player%", player.getDisplayName());
        footer = footer.replaceAll("%player%", player.getDisplayName());

        try {
            PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
            Reflection.setField(packet.getClass().getDeclaredField("a"), packet, IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + header + "\"}"));
            Reflection.setField(packet.getClass().getDeclaredField("b"), packet, IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + footer + "\"}"));

            Reflection.sendPacket(player, packet);
        }
        catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
