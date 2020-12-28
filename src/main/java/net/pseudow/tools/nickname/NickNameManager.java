package net.pseudow.tools.nickname;

import net.pseudow.tools.reflection.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.util.*;

public class NickNameManager {
    private static Constructor<?> packetPlayOutPlayerInfoConstructor;
    private static Class<Enum> enumPlayerInfoAction;

    private final HashSet<NickedPlayer> nickedPlayers;
    private final JavaPlugin javaPlugin;

    /**
     * Default constructor to use NickNameManager. Be careful, this
     * class' methods have to work together. You can't set nickname
     * by another way and remove the player's nickname here.
     *
     * @author Pseudow
     *
     * @param javaPlugin - An instance of you main class.
     */
    public NickNameManager(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
        this.nickedPlayers = new HashSet<>();

        enumPlayerInfoAction = (Class<Enum>) Reflection.getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
        packetPlayOutPlayerInfoConstructor = Reflection.getConstructor(Objects.requireNonNull(Reflection.getNMSClass("PacketPlayOutPlayerInfo")),
                enumPlayerInfoAction, Iterable.class);
    } static {
        if(enumPlayerInfoAction == null) {
            enumPlayerInfoAction = (Class<Enum>) Reflection.getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
        }

        if(packetPlayOutPlayerInfoConstructor == null) {
            packetPlayOutPlayerInfoConstructor = Reflection.getConstructor(Objects.requireNonNull(Reflection.getNMSClass("PacketPlayOutPlayerInfo")),
                    enumPlayerInfoAction, Iterable.class);
        }
    }

    /**
     * This method is used to set a change player name to another name.
     * Even thought a player has already a custom nickname you can use
     * this method. {@link com.mojang.authlib.GameProfile}
     *
     * @author Pseudow
     *
     * @param player - The player you want to change name.
     * @param nickName - The new name which will be displayed.
     */
    public void setNickName(Player player, String nickName) {
        this.removeNickName(player);
        this.nickedPlayers.add(new NickedPlayer(player.getName(), nickName, player.getUniqueId()));
        setNameInGameInfo(Reflection.getHandle(player), nickName);
    }

    /**
     * This method is used to remove player's nickname.
     *
     * @author Pseudow
     *
     * @param player - The player you want to remove old nickname.
     */
    public void removeNickName(Player player) {
        for (NickedPlayer nickedPlayer : this.nickedPlayers)
            if(nickedPlayer.getPlayerId().equals(player.getUniqueId())) {
                setNameInGameInfo(Reflection.getHandle(player), nickedPlayer.getRealName());
                this.nickedPlayers.remove(nickedPlayer);
            }
    }

    /**
     * Use this method when in your onDisable method in your main class.
     *
     * @author Pseudow
     */
    public void onDisable() {
        this.javaPlugin.getServer().getOnlinePlayers().forEach(this::removeNickName);
    }

    public static void setNameInGameInfo(Object entityPlayer, String name) {
        if(entityPlayer instanceof Player)
            entityPlayer = Reflection.getHandle(entityPlayer);

        // REMOVE PLAYER
        Object packetRemove = Reflection.callConstructor(packetPlayOutPlayerInfoConstructor, Reflection.getEnumValue(enumPlayerInfoAction, "REMOVE_PLAYER"), Collections.singletonList(entityPlayer));
        Bukkit.getServer().getOnlinePlayers().forEach(p ->
                Reflection.sendPacket(p, packetRemove));

        // SET PLAYER NEW NAME
        Object gameProfile = Objects.requireNonNull(Reflection.invokeMethod(entityPlayer, "getProfile"));
        Reflection.setField(Reflection.getField(gameProfile.getClass(), "name"), gameProfile, name);

        // ADD PLAYER

        Object packetAdd = Reflection.callConstructor(packetPlayOutPlayerInfoConstructor, Reflection.getEnumValue(enumPlayerInfoAction, "ADD_PLAYER"), Collections.singletonList(entityPlayer));
        Bukkit.getServer().getOnlinePlayers().forEach(p ->
                Reflection.sendPacket(p, packetAdd));
    }

    /**
     * Be careful, it only works if you have set a nickname by
     * this class, if you don't it will return false!
     *
     * @author Pseudow
     *
     * @param uniqueId - The player's unique id.
     * @return true if the player has a nickname, false if it doesn't.
     */
    public boolean isNicked(UUID uniqueId) {
        return nickedPlayers.stream().anyMatch(nickedPlayer ->
                nickedPlayer.getPlayerId().equals(uniqueId));
    }

    /**
     * Be careful, it only works if you have set a nickname by
     * this class, if you don't it will return false!
     *
     * @author Pseudow
     *
     * @param nickName - The player's current nickname.
     * @return true if the player has a nickname, false if it doesn't.
     */
    public boolean isNicked(String nickName) {
        return nickedPlayers.stream().anyMatch(nickedPlayer ->
                nickedPlayer.getNickName().equals(nickName));
    }

    public static Constructor<?> getPacketPlayOutPlayerInfoConstructor() {
        return packetPlayOutPlayerInfoConstructor;
    }

    public static Class<Enum> getEnumPlayerInfoAction() {
        return enumPlayerInfoAction;
    }
}
