package net.pseudow.tools;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class PlayerUtils {
    /**
     * Set a {@link Player} like if it just joined the server.
     *
     * @author Pseudow
     *
     * @param player - The player you want to clean
     * @param gameMode - The gamemode you want to set to it
     */
    public static void cleanPlayer(Player player, GameMode gameMode) {
        player.setGameMode(gameMode);

        player.setAllowFlight(false);
        player.setFlying(false);
        player.setTotalExperience(0);
        player.setExp(0);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);

        player.getInventory().clear();
        player.getInventory().setBoots(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setHelmet(null);
    }
}
