package net.pseudow.tools.nickname;

import java.util.UUID;

public class NickedPlayer {
    private final String realName;
    private final UUID playerId;
    private String nickName;

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

    public void setNickName(String nickName) {
        this.nickName = nickName;
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
