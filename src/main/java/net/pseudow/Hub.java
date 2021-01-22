package net.pseudow;

import net.pseudow.tools.ItemBuilder;
import net.pseudow.tools.NumberUtils;
import net.pseudow.tools.bossbar.BossBarManager;
import net.pseudow.tools.command.CommandHandler;
import net.pseudow.tools.command.CommandManager;
import net.pseudow.tools.nametag.NameTagManager;
import net.pseudow.tools.nametag.NameTagTeam;
import net.pseudow.tools.nickname.NickNameManager;
import net.pseudow.tools.npc.NPCManager;
import net.pseudow.tools.reflection.entities.EntityLiving;
import net.pseudow.tools.scoreboard.IScoreboard;
import net.pseudow.tools.scoreboard.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class Hub extends JavaPlugin implements Listener {
    // MANAGERS
    private ScoreboardManager scoreboardManager;
    private NickNameManager nickNameManager;
    private BossBarManager bossBarManager;
    private NameTagManager nameTagManager;
    private NPCManager npcManager;

    private NameTagTeam adminTeam;
    
    @Override
    public void onEnable() {
        this.getLogger().info("Hub Plugin Enabled!");

        this.nickNameManager = new NickNameManager(this);
        this.nameTagManager = new NameTagManager(this);
        this.npcManager = new NPCManager(this);
        this.scoreboardManager = new ScoreboardManager(this, "mc.pseudow.fr");
        this.bossBarManager = new BossBarManager(this, Arrays.asList("§c1st message", "§d2nd message"), 4);

        this.adminTeam = this.nameTagManager.createNewTeam("0admin", "§c[ADMIN] ", "", true);

        // OR JUST new CommandManager().registerCommand(this);
        new CommandManager().setArgumentsTooLong("Wrong usage of the command! We expected %args_length% in the command %command%!")
                .setPermissionMissingMessage(ChatColor.RED + "Sorry, you don't have the permission to execute this command!")
                .registerCommand(this);

        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        this.nickNameManager.onDisable();
        this.nameTagManager.onDisable();
        this.scoreboardManager.onDisable();
        this.bossBarManager.onDisable();
        this.npcManager.onDisable();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        this.bossBarManager.onLogin(player);
        this.scoreboardManager.onLogin(player, new HubScoreboard(player));
        this.nickNameManager.setNickName(player, "WeLovePseudow");
        this.adminTeam.addPlayer(player);

        // ASYNCHRONOUS BECAUSE WE LOOK FOR THE GAME PROFILE VIA THE ID IF YOU DON'T PLEASE DON'T RUN THIS TASK ASYNCHRONOUSLY
        this.getServer().getScheduler().runTaskAsynchronously(this, () ->
                this.npcManager.createNPC("Tutorial", player.getLocation(), player.getUniqueId())
                        .setVisible(true).setEquipment(EntityLiving.EnumItemSlot.BOOTS, new ItemBuilder(Material.DIAMOND_BOOTS)
                        .enchant(Enchantment.ARROW_DAMAGE, 1).build()).setEquipment(EntityLiving.EnumItemSlot.MAIN_HAND,
                        new ItemStack(Material.DIAMOND_HOE)).setInteractEvent((rightClicked, p) ->
                        p.sendMessage("You hits the Tutorial NPC! (RightClick: " + rightClicked + ")")).setHologram("§cWelcome to our server!",
                        "§eClick on the NPC to receive a message!", "§e§lCLICK HERE!").setNameVisible(false));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        this.bossBarManager.onLogout(player);
        this.scoreboardManager.onLogout(player);
        this.nickNameManager.removeNickName(player);
    }

    @CommandHandler(name = "nickname", description = "A simple description", usage = "/nickname <player> <nickname>",
        aliases = {"nickn", "nick", "nname"}, argsLength = 2, author = "Pseudow", requirePlayer = true)
    public boolean onNickNameCommand(CommandSender sender, String label, String[] arguments) {
        // WE ARE SURE THIS IS A PLAYER BECAUSE requirePlayer = true;
        final Player player = (Player) sender;
        final Player target = Bukkit.getPlayer(arguments[0]);

        if(target == null)
            return false;

        final String oldName = target.getName();
        this.nickNameManager.setNickName(target, arguments[1]);
        final String newName = target.getName();

        player.sendMessage("You have changed the " + oldName + "'s name to " + newName + "!");
        target.sendMessage("Your name has been changed into: " + newName + "!");

        return true;
    }

    private static class HubScoreboard extends IScoreboard {
        private String rank;
        private int coins;

        public HubScoreboard(Player player) {
            super(player, "§e§lHUB");
        }

        @Override
        public void reloadData() {
            this.coins = 3627193;
            this.rank = "§cAdmin";
        }

        @Override
        public void setLines(String ip) {
            this.setLines(ChatColor.DARK_AQUA + "",
                    "§fRank: " + this.rank,
                    "§fCoins: " + NumberUtils.format(this.coins, 3, ' '),
                    ChatColor.RED + "",
                    ip);
        }
    }
}
