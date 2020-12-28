package net.pseudow.tools.titles;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.pseudow.tools.reflection.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ActionBar {
    private String text;

    /**
     * Please, don't use this class in 1.9.4 and above versions.
     * This class might work but you have an other method in
     * Bukkit API {@link Player} which is better.
     *
     * @param text - basic text which will be send by default to players.
     */
    public ActionBar(String text) {
        this.text = text;
    }

    private PacketPlayOutChat toPacket(String text) {
        return new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + text + "\"}"), (byte) 2);
    }

    public void setText(String text) {
        this.text = text;
    }

    public void sendToPlayer(Player p) {
        sendToPlayer(p, text);
    }

    public void sendToPlayer(Player p, String text) {
        Reflection.sendPacket(p, toPacket(text));
    }

    public void sendToAll() {
        sendToAll(text);
    }

    public void sendToAll(String text) {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            sendToPlayer(p, text);
        }
    }

}

