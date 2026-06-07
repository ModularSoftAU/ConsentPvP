package org.modularsoft.consentpvp.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.modularsoft.consentpvp.ConsentPVP;

public class NameTagManager {

    private final ConsentPVP plugin;
    private final Scoreboard scoreboard;
    private Component enabledPrefix;
    private Component disabledPrefix;
    private boolean force;

    private static final String TEAM_ON = "CPVP_ON";
    private static final String TEAM_OFF = "CPVP_OFF";

    public NameTagManager(ConsentPVP plugin) {
        this.plugin = plugin;
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        loadConfig();
    }

    public void loadConfig() {
        this.enabledPrefix = plugin.getMiniMessage().deserialize(
            plugin.getConfig().getString("indicators.pvp-enabled-prefix", "<green>⚔ </green>")
        );
        this.disabledPrefix = plugin.getMiniMessage().deserialize(
            plugin.getConfig().getString("indicators.pvp-disabled-prefix", "<red>⚔ </red>")
        );
        this.force = plugin.getConfig().getBoolean("indicators.force", false);

        setupTeam(TEAM_ON, enabledPrefix);
        setupTeam(TEAM_OFF, disabledPrefix);
    }

    private void setupTeam(String teamName, Component prefix) {
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }
        team.prefix(prefix);
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
    }

    public void updatePlayer(Player player) {
        if (!plugin.areIndicatorsEnabled()) {
            removePlayer(player);
            return;
        }

        Team currentTeam = scoreboard.getEntryTeam(player.getName());
        if (!force && currentTeam != null && !currentTeam.getName().startsWith("CPVP_")) {
            // Already in a non-CPVP team, and we are not forcing.
            return;
        }

        boolean hasConsent = plugin.getPVPManager().hasConsent(player.getUniqueId());
        String targetTeamName = hasConsent ? TEAM_ON : TEAM_OFF;
        String otherTeamName = hasConsent ? TEAM_OFF : TEAM_ON;

        Team targetTeam = scoreboard.getTeam(targetTeamName);
        Team otherTeam = scoreboard.getTeam(otherTeamName);

        if (otherTeam != null && otherTeam.hasEntry(player.getName())) {
            otherTeam.removeEntry(player.getName());
        }

        if (targetTeam != null && !targetTeam.hasEntry(player.getName())) {
            targetTeam.addEntry(player.getName());
        }
    }

    public void removePlayer(Player player) {
        Team teamOn = scoreboard.getTeam(TEAM_ON);
        Team teamOff = scoreboard.getTeam(TEAM_OFF);

        if (teamOn != null && teamOn.hasEntry(player.getName())) {
            teamOn.removeEntry(player.getName());
        }
        if (teamOff != null && teamOff.hasEntry(player.getName())) {
            teamOff.removeEntry(player.getName());
        }
    }

    public void updateAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayer(player);
        }
    }

    public void cleanup() {
        Team teamOn = scoreboard.getTeam(TEAM_ON);
        Team teamOff = scoreboard.getTeam(TEAM_OFF);

        if (teamOn != null) teamOn.unregister();
        if (teamOff != null) teamOff.unregister();
    }
}
