package me.cdrx.geofactions;

import me.cdrx.Main;
import me.cdrx.Prefixes;
import me.cdrx.gui.Menu;
import me.cdrx.gui.PlayerMenuUtility;
import me.cdrx.gui.menus.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GeoFactionsCommands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender,Command cmd,String cmdName,String[] args) {
        if(args.length == 0){
            if(sender instanceof Player){
                Player p = (Player) sender;
                PlayerMenuUtility menuUtility = Main.getPlayerMenuUtility(p);
                if(!isTownResident(p)){
                    Menu inv = new CreatingTownMenu(menuUtility);
                    inv.open();
                }else{
                    p.sendMessage(Prefixes.townBasicPrefix + "You do have a town broo!");
                    if(isPlayerTownOwner(p)){
                        Menu inv = new TownOwnerMenu(menuUtility);
                        inv.open();
                    }else if(isPlayerTownAdmin(p)){
                        Menu inv = new TownAdminMenu(menuUtility);
                        inv.open();
                    }else if(isPlayerTownTreasurer(p)){
                        Menu inv = new TownTreasurerMenu(menuUtility);
                        inv.open();
                    }else{
                        Menu inv = new TownHabMenu(menuUtility);
                        inv.open();
                    }
                }
            }else{
                sender.sendMessage(Prefixes.townBasicPrefix + "This command are only for human beign!");
            }
        }else{
            sender.sendMessage(Prefixes.townBasicPrefix + "This command does not exists yet!");
        }
        return true;
    }

    private boolean isTownResident(Player p) {
        if(Main.getResidentsTown().containsKey(p.getUniqueId())){
            return true;
        }else{
            try{
                //TODO: Requete au serveur SQL
            }catch (Exception e){
                return false;
            }
        }
        return false;
    }

    private boolean isPlayerTownTreasurer(Player p) {
        return false;
    }

    private boolean isPlayerTownAdmin(Player p) {
        return false;
    }

    private boolean isPlayerTownOwner(Player p) {
        return false;
    }
}
