package net.pseudow.tools.scoreboard;

import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.entity.Player;

public abstract class IScoreboard {
    private FastBoard fastBoard;

    public IScoreboard(Player player, String displayName) {
        this.fastBoard = new FastBoard(player);
        this.fastBoard.updateTitle(displayName);

        this.reloadData();
    }

    public void setDisplayName(String displayName) {
        this.fastBoard.updateTitle(displayName);
    }

    public void setLine(int index, String line) {
        this.fastBoard.updateLine(index, line);
    }

    public void setLines(String... lines) {
        this.fastBoard.updateLines(lines);
    }

    public void removeLine(int index) {
        this.fastBoard.removeLine(index);
    }

    public abstract void reloadData();
    public abstract void setLines(String ip);

    protected FastBoard getFastBoard() {
        return fastBoard;
    }
}
