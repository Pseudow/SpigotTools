package net.pseudow.tools.npc;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface NPCInteractCallBack {
    /**
     * @param rightClick If the action was with a click right
     * @param caller The player who has interacted with the npc
     */
    void fire(boolean rightClick, Player caller);
}
