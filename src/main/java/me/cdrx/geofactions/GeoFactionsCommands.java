package me.cdrx.geofactions;

import me.cdrx.Main;
import me.cdrx.Prefixes;
import me.cdrx.gui.PlayerMenuUtility;
import me.cdrx.gui.menus.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GeoFactionsCommands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender,Command cmd,String cmdName,String[] args) {
        if(args.length == 0){
            if(sender instanceof Player){
                Player p = (Player) sender;
                PlayerMenuUtility menuUtility = Main.getPlayerMenuUtility(p);
                if(!Logics.isPlayerResidentOfATown(p)){
                    CreatingTownMenu inv = new CreatingTownMenu(menuUtility);
                    inv.open();
                }else{
                    p.sendMessage(Prefixes.townBasicPrefix + "You do have a town broo!");
                    if(isPlayerTownOwner(p)){
                        TownOwnerMenu inv = new TownOwnerMenu(menuUtility);
                        inv.open();
                    }else if(isPlayerTownAdmin(p)){
                        TownAdminMenu inv = new TownAdminMenu(menuUtility);
                        inv.open();
                    }else if(isPlayerTownTreasurer(p)){
                        TownTreasurerMenu inv = new TownTreasurerMenu(menuUtility);
                        inv.open();
                    }else{
                        TownHabMenu inv = new TownHabMenu(menuUtility);
                        inv.open();
                    }
                }
            }else{
                sender.sendMessage(Prefixes.townBasicPrefix + "This command are only for human beign!");
            }
        }else{
            if(args[0].equals("inviteAccept")){
                Player player = Bukkit.getPlayer(UUID.fromString(args[1]));
                String townName = args[2];

                Logics.addPlayerAsResident(player, townName);
            }else if(args[0].equals("inviteDeny")) {
                Player player = Bukkit.getPlayer(UUID.fromString(args[1]));
                Player sender1 = Bukkit.getPlayer(UUID.fromString(args[2]));

                player.sendMessage(Prefixes.townBasicPrefix + "You did the right choice buddy!");
                sender1.sendMessage(Prefixes.townBasicPrefix + player.getName() + " denied your request!");
            }else if(args[0].equals("deleteTown")) {
                UUID playerUUID = UUID.fromString(args[1]);
                for(TownCache tc : Main.getTownCache()){
                    for(UUID uuid : tc.getResidents()){
                        if(uuid.equals(playerUUID)){
                            Logics.deleteTown(tc.getTownName());
                        }
                    }
                }
            }else if(args[0].equals("dontDeleteTown")){
                Player player = Bukkit.getPlayer(UUID.fromString(args[1]));
                player.sendMessage(Prefixes.townBasicPrefix + "Hooo, so you finally decided to not delete your town.");
            }else{
                sender.sendMessage(Prefixes.townBasicPrefix + "This command does not exists yet!");
            }
        }
        return true;
    }


    private boolean isPlayerTownTreasurer(Player p) {
        for(TownCache tc : Main.getTownCache()){
            for(UUID uuid : tc.getTreasurers()){
                if(uuid.equals(p.getUniqueId())){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPlayerTownAdmin(Player p) {
        for(TownCache tc : Main.getTownCache()){
            for(UUID uuid : tc.getAdmins()){
                if(uuid.equals(p.getUniqueId())){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPlayerTownOwner(Player p) {
        for(TownCache tc : Main.getTownCache()){
            if(tc.getOwner().equals(p.getUniqueId())){
                return true;
            }
        }
        return false;
    }
}
