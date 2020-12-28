package net.pseudow;

import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.pseudow.tools.NumberUtils;
import net.pseudow.tools.PlayerUtils;
import net.pseudow.tools.bossbar.BossBarManager;
import net.pseudow.tools.nickname.NickNameManager;
import net.pseudow.tools.npc.NpcManager;
import net.pseudow.tools.scoreboard.IScoreboard;
import net.pseudow.tools.scoreboard.ScoreboardManager;
import net.pseudow.tools.titles.Titles;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class Hub extends JavaPlugin implements Listener {
    private ScoreboardManager scoreboardManager;
    private BossBarManager bossBarManager;
    private NickNameManager nickNameManager;
    private NpcManager npcManager;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);

        this.scoreboardManager = new ScoreboardManager(this, "mc.pseudow.fr");
        this.bossBarManager = new BossBarManager(this, Arrays.asList("§fBienvenue sur §e§lPseudowMC §f! Amusez-vous bien!", "§e§lAutre message: Pseudow cherche l'amour si vous voulez."), 4);
        this.nickNameManager = new NickNameManager(this);
        this.npcManager = new NpcManager(this);
    }

    @Override
    public void onDisable() {
        this.scoreboardManager.onDisable();
        this.bossBarManager.onDisable();
        this.nickNameManager.onDisable();
        this.npcManager.onDisable();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Titles.sendTitle(player, 10, 10, 10, "PseudowMC", "§eUn serveur où l'on mange des pâtes!");
        PlayerUtils.cleanPlayer(player, GameMode.ADVENTURE);

        this.scoreboardManager.onLogin(player, new HubScoreboard(player));
        this.bossBarManager.onLogin(player);
        this.nickNameManager.setNickName(player, "PseudowFanN1");
        //NPCs DONT WORK YET! # SOON
        this.npcManager.createNPC("ILovePseudow", player.getLocation(), player.getUniqueId(), new PlayerInteractManager(((CraftWorld) player.getWorld()).getHandle()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        this.scoreboardManager.onLogout(player);
        this.bossBarManager.onLogout(player);
        this.nickNameManager.removeNickName(player);
    }

    private static class HubScoreboard extends IScoreboard {
        private final Player player;
        private int coins;

        public HubScoreboard(Player player) {
            super(player);

            this.player = player;
            getFastBoard().updateTitle("§e§lHUB");
        }

        @Override
        public void reloadData() {
            this.coins = 124827738;
        }

        @Override
        public void setLines(String ip) {
            getFastBoard().updateLines("§fCoins: " + NumberUtils.format(this.coins, 3, ','),
                    "§fPseudo: " + player.getName(),
                    "§b",
                    ip);
        }
    }
}