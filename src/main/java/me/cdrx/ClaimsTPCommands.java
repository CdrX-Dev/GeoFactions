package me.cdrx;

import me.cdrx.geofactions.Logics;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClaimsTPCommands implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(commandSender instanceof Player p){
            for(Chunk c : Logics.getTownByName(Logics.getTownOfPlayer(p)).getClaimedChunks()){
                p.teleport(new Location(c.getWorld(), c.getX()*16, 128,c.getZ()*16));
                return true;
            }
        }
        return true;
    }
}
