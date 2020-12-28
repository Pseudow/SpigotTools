package net.pseudow.tools.scoreboard;

import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.entity.Player;

public abstract class IScoreboard {
    private FastBoard fastBoard;

    public IScoreboard(Player player) {
        this.fastBoard = new FastBoard(player);
    }

    public abstract void reloadData();
    public abstract void setLines(String ip);

    public FastBoard getFastBoard() {
        return fastBoard;
    }
}
