package net.pseudow.tools.scoreboard;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/*
 * This file is part of SamaGamesAPI.
 *
 * SamaGamesAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SamaGamesAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SamaGamesAPI.  If not, see <http://www.gnu.org/licenses/>.
 */
public class ScoreboardManager {
    private final Map<UUID, IScoreboard> boards;

    private final ScheduledExecutorService executorMonoThread ;
    private final ScheduledExecutorService scheduledExecutorService ;

    private final ScheduledFuture glowingTask;
    private final ScheduledFuture reloadingTask;

    private final JavaPlugin plugin;
    private final String serverIp;

    private int ipCharIndex;
    private int cooldown;

    /**
     * This class is a Scoreboard Manager which allows you to set a scoreboard with packets
     * to a player. This class is available from 1.7.10 to the last bukkit version.
     * Special thanks to SamaGames for this class and MrMicky-FR for FastBoard.
     *
     * @author SamaGames & MrMicky
     *
     * @param plugin - An instance of your main class.
     * @param serverIp - The server adress you want to display.
     */
    public ScoreboardManager(JavaPlugin plugin, String serverIp) {
        this.plugin = plugin;
        this.serverIp = serverIp;

        this.boards = new HashMap<>();
        this.ipCharIndex = 0;
        this.cooldown = 0;

        this.executorMonoThread  = Executors.newScheduledThreadPool(1);
        this.scheduledExecutorService  = Executors.newScheduledThreadPool(16);

        this.glowingTask = this.scheduledExecutorService .scheduleAtFixedRate(() -> {
            String ip = this.colorIpAt();

            for (IScoreboard boards : this.boards.values())
                this.executorMonoThread .execute(() -> boards.setLines(ip));
        }, 80, 80, TimeUnit.MILLISECONDS);

        this.reloadingTask = this.scheduledExecutorService .scheduleAtFixedRate(() ->
        {
            for (IScoreboard scoreboard : this.boards.values())
                this.executorMonoThread .execute(scoreboard::reloadData);
        }, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * Use this method when you plugin will be disabled!
     */
    public void onDisable() {
        for(UUID uuid : boards.keySet())
            if(plugin.getServer().getPlayer(uuid) != null)
                onLogout(plugin.getServer().getPlayer(uuid));

        scheduledExecutorService.shutdown();
        executorMonoThread.shutdown();

        glowingTask.cancel(true);
        reloadingTask.cancel(true);
    }

    /**
     * Send a scoreboard to a player.
     *
     * @param player - The player which will have the scoreboard to its right.
     * @param scoreboard - The scoreboard which will be displayed to the player.
     */
    public void onLogin(Player player, IScoreboard scoreboard) {
        this.boards.put(player.getUniqueId(), scoreboard);
    }

    /**
     * Remove the scoreboard to a player.
     *
     * @param player - The player which wont have the scoreboard yet.
     */
    public void onLogout(Player player) {
        IScoreboard board = this.boards.get(player.getUniqueId());

        if(board != null)
            if(board.getFastBoard() != null)
                board.getFastBoard().delete();
    }

    private String colorIpAt() {
        String ip = this.serverIp;

        if (this.cooldown > 0) {
            this.cooldown--;
            return ChatColor.YELLOW + ip;
        }

        StringBuilder formattedIp = new StringBuilder();

        if (this.ipCharIndex > 0) {
            formattedIp.append(ip.substring(0, this.ipCharIndex - 1));
            formattedIp.append(ChatColor.GOLD).append(ip.substring(this.ipCharIndex - 1, this.ipCharIndex));
        } else {
            formattedIp.append(ip.substring(0, this.ipCharIndex));
        }

        formattedIp.append(ChatColor.RED).append(ip.charAt(this.ipCharIndex));

        if (this.ipCharIndex + 1 < ip.length()) {
            formattedIp.append(ChatColor.GOLD).append(ip.charAt(this.ipCharIndex + 1));

            if (this.ipCharIndex + 2 < ip.length())
                formattedIp.append(ChatColor.YELLOW).append(ip.substring(this.ipCharIndex + 2));

            this.ipCharIndex++;
        } else {
            this.ipCharIndex = 0;
            this.cooldown = 50;
        }

        return ChatColor.YELLOW + formattedIp.toString();
    }
}
