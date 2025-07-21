package org.modularsoft.consentpvp.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PVPTabCompleter implements TabCompleter {

    private static final String[] SUBCOMMANDS = { "enable", "disable", "death", "status" };

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList(SUBCOMMANDS), new ArrayList<>());
        }
        return null;
    }
}
