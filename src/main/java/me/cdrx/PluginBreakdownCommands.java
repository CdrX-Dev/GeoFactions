package me.cdrx;

import me.cdrx.geofactions.Logics;
import me.cdrx.geofactions.TownCache;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;

public class PluginBreakdownCommands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args) {
        sender.sendMessage("This command causes extreme lag! (Plus it is bugged and does not work properly.)");

        MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
        MemoryUsage currentHeapUsage = mbean.getHeapMemoryUsage();
        List<TownCache> emptyList = new ArrayList<>();
        Main.setTownCache(emptyList);
        MemoryUsage beforeTownCacheHeapUsage = mbean.getHeapMemoryUsage();
        Logics.initializeCache();

        sender.sendMessage("The TownCache system is currently using " + (currentHeapUsage.getUsed() - beforeTownCacheHeapUsage.getUsed()) + " of RAM for " + Main.getTownCache().size() + " towns instancied!");
        return true;
    }
}
