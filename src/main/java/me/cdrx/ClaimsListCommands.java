package me.cdrx;

import me.cdrx.geofactions.Logics;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClaimsListCommands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender,Command cmd,String name,String[] args) {
        if(sender instanceof Player p){
            p.sendMessage(Prefixes.townBasicPrefix + "Here is a list of your town's claimed chunks!");
            for(Chunk c : Logics.getTownByName(Logics.getTownOfPlayer(p)).getClaimedChunks()){
                p.sendMessage(Prefixes.townBasicPrefix + "Chunk at :" + c.getX() + ";" + c.getZ() + " in world '" + c.getWorld().getName() + "'!");
            }
        }
        return true;
    }
}
