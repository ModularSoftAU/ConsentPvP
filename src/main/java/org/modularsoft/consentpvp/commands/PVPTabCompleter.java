package org.modularsoft.consentpvp.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PVPTabCompleter implements TabCompleter {

    private static final String[] SUBCOMMANDS = { "enable", "disable", "bypass", "death", "status", "reload" };

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList(SUBCOMMANDS), new ArrayList<>());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("bypass") && sender.hasPermission("consentpvp.admin")) {
            List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
            return StringUtil.copyPartialMatches(args[1], playerNames, new ArrayList<>());
        }
        return null;
    }
}
