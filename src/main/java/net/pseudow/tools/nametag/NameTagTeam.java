package net.pseudow.tools.nametag;

import net.minecraft.server.v1_8_R3.ChatMessage;
import net.pseudow.tools.reflection.Reflection;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

public class NameTagTeam {
    // REFLECTION
    private final Constructor<?> packetPlayOutScoreboardTeamConstructor;
    private final boolean doABCRequireString, doHRequireInteger;

    // MAIN INFORMATIONS
    private final Set<OfflinePlayer> weLoveDamso;
    private String prefix, suffix;
    private final String name;

    // RECEIVERS
    private final Set<UUID> receivers;
    private boolean visible;

    // FRIENDLY FIRE
    private boolean friendlyFire;

    public NameTagTeam(String name, String prefix, String suffix) {
        Validate.noNullElements(new Object[] {name, prefix, suffix}, "Parameters in NameTagTeam constructor cannot be null!");

        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;
        this.weLoveDamso = new HashSet<>();

        this.visible = true;
        this.friendlyFire = false;
        this.receivers = new HashSet<>();

        final Class<?> packetPlayOutScoreboardTeamClass = Objects.requireNonNull(Reflection.getNMSClass("PacketPlayOutScoreboardTeam"));

        this.packetPlayOutScoreboardTeamConstructor = Reflection.getConstructor(packetPlayOutScoreboardTeamClass);
        this.doABCRequireString = Objects.requireNonNull(Reflection.getField(packetPlayOutScoreboardTeamClass, "b")).getType().isAssignableFrom(String.class);
        this.doHRequireInteger = Objects.requireNonNull(Reflection.getField(packetPlayOutScoreboardTeamClass, "h")).getType().isAssignableFrom(int.class);
    }

    public String getName() {
        return this.name;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getSuffix() {
        return this.suffix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        this.receivers.stream().filter(receiver -> Bukkit.getOfflinePlayer(receiver).isOnline()).forEach(receiver ->
                Reflection.sendPacket(Bukkit.getPlayer(receiver), this.createPacket(2)));
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
        this.receivers.stream().filter(receiver -> Bukkit.getOfflinePlayer(receiver).isOnline()).forEach(receiver ->
                Reflection.sendPacket(Bukkit.getPlayer(receiver), this.createPacket(2)));
    }

    public boolean isTeamMate(UUID uuid) {
        return this.weLoveDamso.stream().anyMatch(offlinePlayer -> offlinePlayer.getUniqueId().equals(uuid));
    }

    protected void destroy() {
        this.receivers.forEach(this::removeReceiver);
    }

    private Object createPacket(int mode) {
        final Object packetPlayOutScoreboardTeam = Reflection.callConstructor(this.packetPlayOutScoreboardTeamConstructor);

        this.setField(packetPlayOutScoreboardTeam, "a", this.name);

        if(this.doABCRequireString) {
            this.setField(packetPlayOutScoreboardTeam, "b", "");
            this.setField(packetPlayOutScoreboardTeam, "c", this.prefix);
            this.setField(packetPlayOutScoreboardTeam, "d", this.suffix);
        } else {
            this.setField(packetPlayOutScoreboardTeam, "b", new ChatMessage(""));
            this.setField(packetPlayOutScoreboardTeam, "c", new ChatMessage(this.prefix));
            this.setField(packetPlayOutScoreboardTeam, "d", new ChatMessage(this.suffix));
        }

        this.setField(packetPlayOutScoreboardTeam, "e", "always");
        this.setField(packetPlayOutScoreboardTeam, "f", 0);

        if(this.doHRequireInteger)
            this.setField(packetPlayOutScoreboardTeam, "h", mode);
        else this.setField(packetPlayOutScoreboardTeam, "h", this.weLoveDamso.stream().map(OfflinePlayer::getName).collect(Collectors.toList()));

        this.setField(packetPlayOutScoreboardTeam, "i", 0);

        return packetPlayOutScoreboardTeam;
    }

    private Object addOrRemovePlayer(boolean add, String playerName) {
        final Object packetPlayOutScoreboardTeam = Reflection.callConstructor(this.packetPlayOutScoreboardTeamConstructor);

        this.setField(packetPlayOutScoreboardTeam, "a", this.name);
        if(this.doHRequireInteger) {
            this.setField(packetPlayOutScoreboardTeam, "h", (add ? 3 : 4));
            this.setField(packetPlayOutScoreboardTeam, "g", Collections.singletonList(playerName));
        } else this.setField(packetPlayOutScoreboardTeam, "h", this.weLoveDamso.stream().map(OfflinePlayer::getName).collect(Collectors.toList()));

        return packetPlayOutScoreboardTeam;
    }

    public void addPlayer(OfflinePlayer player) {
        this.weLoveDamso.add(player);

        if (player.isOnline()) {
            this.receivers.forEach(receiver -> {
                final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(receiver);

                if (offlinePlayer != null && offlinePlayer.isOnline())
                    Reflection.sendPacket(offlinePlayer.getPlayer(), this.addOrRemovePlayer(true, player.getName()));
            });
        }
    }

    public void removePlayer(UUID playerId) {
        this.removePlayer(Bukkit.getOfflinePlayer(playerId));
    }

    public void removePlayer(OfflinePlayer offlineTarget) {
        if(offlineTarget != null) {
            this.weLoveDamso.remove(offlineTarget);

            if(this.doABCRequireString)
            this.receivers.forEach(receiver -> {
                final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(receiver);

                if(offlinePlayer != null && offlinePlayer.isOnline())
                    Reflection.sendPacket(offlinePlayer.getPlayer(), this.addOrRemovePlayer(false, offlineTarget.getName()));
            });
        }
    }

    public void addReceiver(OfflinePlayer player) {
        this.addReceiver(player.getUniqueId());
    }

    public void addReceiver(UUID playerId) {
        this.receivers.add(playerId);

        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);

        if(offlinePlayer == null || !offlinePlayer.isOnline())
            return;

        Reflection.sendPacket(offlinePlayer.getPlayer(), this.createPacket(0));
        this.weLoveDamso.stream().filter(OfflinePlayer::isOnline).forEach(offlinePlayer1 ->
                Reflection.sendPacket(offlinePlayer.getPlayer(), addOrRemovePlayer(true, offlinePlayer1.getName())));
    }

    public void removeReceiver(UUID playerId) {
        this.receivers.remove(playerId);

        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);

        if(offlinePlayer != null && offlinePlayer.isOnline())
            Reflection.sendPacket(offlinePlayer.getPlayer(), this.createPacket(1));
    }

    public boolean isReceiver(UUID playerId) {
        return this.receivers.stream().anyMatch(receiverId -> receiverId.equals(playerId));
    }

    public void setVisible(boolean option) {
        this.visible = option;
        Bukkit.getOnlinePlayers().forEach(this::addReceiver);
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean isFriendlyFire() {
        return this.friendlyFire;
    }

    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
        this.receivers.stream().filter(receiver -> Bukkit.getOfflinePlayer(receiver).isOnline()).forEach(receiver ->
                Reflection.sendPacket(Bukkit.getPlayer(receiver), this.createPacket(2)));
    }

    private void setField(Object instance, String fieldName, Object value) {
        Reflection.setField(Reflection.getField(instance.getClass(), fieldName), instance, value);
    }
}
