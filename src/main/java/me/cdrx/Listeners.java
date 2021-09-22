package me.cdrx;

import me.cdrx.geofactions.Logics;
import me.cdrx.geofactions.TownCache;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerChatEvent;

import java.util.HashMap;
import java.util.UUID;

public class Listeners implements Listener {

    //Quand le joueur tape dans le chat, tu check si il est pas en train de taper le nom de sa ville. Le coquin!
    @EventHandler
    public void onMessageEvent(PlayerChatEvent e){
        HashMap<UUID, String> list = Main.getTypingPlayers();
        if(list.containsKey(e.getPlayer().getUniqueId())){
            String statement = list.get(e.getPlayer().getUniqueId());
            if(statement.equals("create_town")){
                e.setCancelled(true);

                //Methode pour créer la ville
                String townName = e.getMessage();
                if(!townName.equalsIgnoreCase("cancel")) Logics.createTown(e.getPlayer().getUniqueId(), townName);

                //Enlever le joueur de la liste de joueurs qui tapent leur nom de leur ville dans le chat.
                list.remove(e.getPlayer().getUniqueId());
                Main.setTypingPlayers(list);
            }else if(statement.equals("")){
                //TODO: Check si on est pas en train de rajouer une grade au joueur dans une town!
            }else if(statement.equals("invite_player")){
                e.setCancelled(true);
                String playerName = e.getMessage();
                Player player = e.getPlayer();
                String townName = null;
                for(TownCache tc : Main.getTownCache()){
                    for(UUID uuid : tc.getResidents()){
                        if(uuid.equals(player.getUniqueId())){
                            townName = tc.getTownName();
                        }
                    }
                }
                if(!playerName.equalsIgnoreCase("cancel")){
                    if(Logics.doesPlayerExist(playerName)){
                        Logics.invitePlayerInTown(e.getPlayer(), Bukkit.getPlayer(playerName), townName);
                    }else{
                        player.sendMessage(Prefixes.townBasicPrefix + "Could not invite player " + playerName + ". Is it really a player name?");
                    }
                }else{
                    HashMap<UUID, String> map = Main.getTypingPlayers();
                    map.remove(player.getUniqueId());
                    Main.setTypingPlayers(map);

                    player.sendMessage(Prefixes.townBasicPrefix + "Ok, cancelling right now!");
                }
            }
        }
    }


    //Pour les events en relation avec les claims
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        Chunk chunk = e.getBlock().getChunk();
        if(!Logics.isChunkClaimed(chunk)) return;
        if(!Logics.isChunkClaimedWithPlayerTown(e.getPlayer(), chunk)){
            e.setCancelled(true);
            e.getPlayer().sendMessage(Prefixes.townBasicPrefix + "This is not your town! You can't do that here!");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e){
        Chunk chunk = e.getBlock().getChunk();
        if(!Logics.isChunkClaimed(chunk)) return;
        if(!Logics.isChunkClaimedWithPlayerTown(e.getPlayer(), chunk)){
            e.setCancelled(true);
            e.getPlayer().sendMessage(Prefixes.townBasicPrefix + "This is not your town! You can't do that here!");
        }
    }
}

