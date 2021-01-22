package net.pseudow.tools.nickname;

import java.util.UUID;

public class NickedPlayer {
    private final String realName, nickName;
    private final UUID playerId;

    /**
     *
     * @param realName - Player's real name.
     * @param nickName - New player's name.
     * @param playerId - Player's unique Id;
     */
    public NickedPlayer(String realName, String nickName, UUID playerId) {
        this.realName = realName;
        this.nickName = nickName;
        this.playerId = playerId;
    }

    public String getRealName() {
        return this.realName;
    }

    public String getNickName() {
        return this.nickName;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }
}
