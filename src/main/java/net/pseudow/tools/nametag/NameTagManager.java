package net.pseudow.tools.nametag;

import org.apache.commons.lang3.Validate;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;

import javax.persistence.EntityExistsException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class NameTagManager implements Listener {
    private final Set<NameTagTeam> nameTagTeams;

    /**
     * This manager allows you to add or remove prefix/suffix (which is displayed either on a player's head
     * or in the tab) to a certain player or create a team which do the same for multiple players.
     *
     * @author Pseudow
     *
     * @param javaPlugin The main instance of your plugin
     */
    public NameTagManager(JavaPlugin javaPlugin) {
        Validate.notNull(javaPlugin, "Error, Java Plugin instance cannot be null!");

        this.nameTagTeams = new HashSet<>();

        javaPlugin.getServer().getPluginManager().registerEvents(this, javaPlugin);
    }

    public void onDisable() {
        this.nameTagTeams.forEach(team -> this.removeTeamByName(team.getName()));
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        this.nameTagTeams.stream().filter(nameTagTeam -> nameTagTeam.isVisible() || nameTagTeam.isReceiver(player.getUniqueId()))
                .forEach(nameTagTeam -> nameTagTeam.addReceiver(player));
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        this.nameTagTeams.stream().filter(nameTagTeam -> nameTagTeam.isVisible() || nameTagTeam.isReceiver(player.getUniqueId()))
                .forEach(nameTagTeam -> nameTagTeam.removePlayer(player));
    }

    @EventHandler
    private void onDamage(EntityDamageByEntityEvent event) {
        final Entity attacked = event.getEntity(),
                    attacker = event.getDamager();

        if(attacked instanceof Player) {
            Player victim = (Player) attacked,
                            demoniacPlayer = null;

            if(attacker instanceof Player) {
                victim = (Player) attacker;

            } else if(attacker instanceof Arrow) {
                final Arrow arrow = (Arrow) attacker;
                final ProjectileSource source = arrow.getShooter();

                if(source instanceof Player)
                    victim = (Player) source;

            } else return;


            NameTagTeam victimTeam = this.getPlayerTeam(victim.getUniqueId()),
                    playerTeam = this.getPlayerTeam(demoniacPlayer.getUniqueId());

            if(victimTeam != null && victimTeam == playerTeam && victimTeam.isFriendlyFire())
                event.setCancelled(true);
        }

    }

    public NameTagTeam createNewTeam(String prefix) {
        String teamName = Integer.toString(ThreadLocalRandom.current().nextInt(999));

        while(this.getTeamByName(teamName) != null) {
            teamName = Integer.toString(ThreadLocalRandom.current().nextInt(999));
        }

        return this.createNewTeam(teamName, prefix);
    }

    public NameTagTeam createNewTeam(String name, String prefix) {
        return this.createNewTeam(name, prefix, "");
    }

    public NameTagTeam createNewTeam(String name, String prefix, String suffix) {
        return this.createNewTeam(name, prefix, suffix, true);
    }

    /**
     * Create a new team which allows you to set a prefix/suffix to (a) player(s) and display it
     * to players you want to display.
     *
     * @exception javax.persistence.EntityExistsException If the name is already existing! So please don't use player's uuid as name there is
     * some chance to be already used!
     *
     * @param name The name of the team. (It won't be displayed)
     * @param prefix The prefix which will be displayed before the player's name.
     * @param suffix The suffix which will be displayed after the player's name.
     * @param visible If the team will be displayed to everyone.
     *
     * @return A new team to allow you to modify it or add players or destroy it and so on...
     */
    public NameTagTeam createNewTeam(String name, String prefix, String suffix, boolean visible) {
        if(this.getTeamByName(name) != null)
            throw new EntityExistsException("Error! The team name is already used!");

        final NameTagTeam nameTagTeam = new NameTagTeam(name, prefix, suffix);
        nameTagTeam.setVisible(visible);

        this.nameTagTeams.add(nameTagTeam);

        return nameTagTeam;
    }

    /**
     * Remove a team players inside this team won't have the name tag after doing this!
     *
     * @param parameter By default, we start by checking the name.
     */
    public void removeTeamByName(String parameter) {
        for(NameTagTeam nameTagTeam : this.nameTagTeams)
            if(nameTagTeam.getName().equals(parameter)) {
                this.nameTagTeams.remove(nameTagTeam);
                nameTagTeam.destroy();
            }
    }

    /**
     * Obtain a team by checking with the parameter. It will returns null if the team hasn't been found!
     *
     * @param parameter By default, we start by checking the name.
     *
     * @return The team found!
     */
    public NameTagTeam getTeamByName(String parameter) {
        return this.nameTagTeams.stream().filter(nameTagTeam ->
                nameTagTeam.getName().equals(parameter)).findFirst().orElse(null);
    }

    public NameTagTeam getPlayerTeam(UUID playerId) {
        return this.nameTagTeams.stream().filter(nameTagTeam -> nameTagTeam.isTeamMate(playerId))
                .findFirst().orElse(null);
    }
}
