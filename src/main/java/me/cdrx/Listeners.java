package me.cdrx;

import me.cdrx.geofactions.Logics;
import me.cdrx.geofactions.TownCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

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
            }else if(statement.equals("promote_owner")) {
                e.setCancelled(true);
                String playerName = e.getMessage();
                Player sender = e.getPlayer();
                String townName = Logics.getTownOfPlayer(sender);

                if(Bukkit.getPlayer(playerName).isOnline()){
                    Player player = Bukkit.getPlayer(playerName);
                    if(Logics.isPlayerResidentOfATown(player)){
                        if(townName.equals(Logics.getTownOfPlayer(player))){
                            TownCache town = Logics.getTownByName(townName);
                            town.setOwner(player.getUniqueId());

                            try{
                                final Connection connection = Main.getDatabaseManager().getInfoConnection().getConnection();
                                PreparedStatement ps = connection.prepareStatement("DELETE FROM towns_owner WHERE town=?");
                                ps.setString(1, townName);
                                ps.executeUpdate();

                                ps = connection.prepareStatement("INSERT INTO towns_owner VALUES(?, ?)");
                                ps.setString(1, townName);
                                ps.setString(2, player.getUniqueId().toString());
                                ps.executeUpdate();
                            }catch (SQLException ex){
                                ex.printStackTrace();
                            }
                        }else{
                            sender.sendMessage(Prefixes.townBasicPrefix + "This player is not in your town. You can't promote him!");
                        }
                    }else{
                        sender.sendMessage(Prefixes.townBasicPrefix + "This player is not in your town. You can't promote him!");
                    }
                }else{
                    sender.sendMessage(Prefixes.townBasicPrefix + "This player does not seem to be online right now.");
                }
            }else if(statement.equals("promote_admin")) {
                e.setCancelled(true);
                String playerName = e.getMessage();
                Player sender = e.getPlayer();

                if(Bukkit.getPlayer(playerName).isOnline()){
                    Player player = Bukkit.getPlayer(playerName);
                    Logics.setTownAdmin(sender, player.getUniqueId(), Logics.getTownOfPlayer(sender));
                }else{
                    sender.sendMessage(Prefixes.townBasicPrefix + "This player does not seem to be online right now.");
                }

                HashMap<UUID, String> map = Main.getTypingPlayers();
                map.remove(sender.getUniqueId());
                Main.setTypingPlayers(map);
            }else if(statement.equals("promote_treasurer")) {
                e.setCancelled(true);
                String playerName = e.getMessage();
                Player sender = e.getPlayer();

                if(Bukkit.getPlayer(playerName).isOnline()){
                    Player player = Bukkit.getPlayer(playerName);
                    Logics.setTownTreasurer(sender, player.getUniqueId(), Logics.getTownOfPlayer(sender));
                }else{
                    sender.sendMessage(Prefixes.townBasicPrefix + "This player does not seem to be online right now.");
                }

                HashMap<UUID, String> map = Main.getTypingPlayers();
                map.remove(sender.getUniqueId());
                Main.setTypingPlayers(map);
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
            }else if(statement.equals("demote_admin")){
                e.setCancelled(true);
                if(Bukkit.getPlayer(e.getMessage()).isOnline()){
                    UUID playerUUID = Bukkit.getPlayer(e.getMessage()).getUniqueId();
                    Logics.removeTownAdmin(e.getPlayer(), playerUUID, Logics.getTownByName(Logics.getTownOfPlayer(e.getPlayer())).getTownName());
                }else{
                    e.getPlayer().sendMessage(Prefixes.townBasicPrefix + "This player does not seems to be on the server right now!");
                }

                HashMap<UUID, String> map = Main.getTypingPlayers();
                map.remove(e.getPlayer().getUniqueId());
                Main.setTypingPlayers(map);
            }else if(statement.equals("demote_treasurer")){
                e.setCancelled(true);
                if(Bukkit.getPlayer(e.getMessage()).isOnline()){
                    UUID playerUUID = Bukkit.getPlayer(e.getMessage()).getUniqueId();
                    Logics.removeTownTreasurer(e.getPlayer(), playerUUID, Logics.getTownByName(Logics.getTownOfPlayer(e.getPlayer())).getTownName());
                }else{
                    e.getPlayer().sendMessage(Prefixes.townBasicPrefix + "This player does not seems to be on the server right now!");
                }

                HashMap<UUID, String> map = Main.getTypingPlayers();
                map.remove(e.getPlayer().getUniqueId());
                Main.setTypingPlayers(map);
            }else if(statement.equals("demote_owner")){
                e.setCancelled(true);
                e.getPlayer().sendMessage(Prefixes.townBasicPrefix + "You can't demote an owner!");
                HashMap<UUID, String> map = Main.getTypingPlayers();
                map.remove(e.getPlayer().getUniqueId());
                Main.setTypingPlayers(map);
            }else if(statement.equals("demote_resident")){
                e.setCancelled(true);
                if(Bukkit.getPlayer(e.getMessage()).isOnline()){
                    Player p = Bukkit.getPlayer(e.getMessage());
                    if(Logics.isPlayerResidentOfATown(p)){
                        if(Logics.getTownOfPlayer(e.getPlayer()).equals(Logics.getTownOfPlayer(p))){
                            Logics.removeResidentFromTown(e.getPlayer(), p, Logics.getTownByName(Logics.getTownOfPlayer(e.getPlayer())));
                        }else{
                            e.getPlayer().sendMessage(Prefixes.townBasicPrefix + "This player is not in your town!");
                        }
                    }else{
                        e.getPlayer().sendMessage(Prefixes.townBasicPrefix + "This player is not in your town!");
                    }
                }else{
                    e.getPlayer().sendMessage(Prefixes.townBasicPrefix + "This player does not seems to be on the server right now!");
                }
                HashMap<UUID, String> map = Main.getTypingPlayers();
                map.remove(e.getPlayer().getUniqueId());
                Main.setTypingPlayers(map);
            }
        }
    }

    @EventHandler
    public void onChatMessage(PlayerChatEvent e){
        Player p = e.getPlayer();
        e.setCancelled(true);
        if(Logics.isPlayerResidentOfATown(p)){
            Bukkit.broadcastMessage(ChatColor.GRAY + "[" + ChatColor.RED + Logics.getTownOfPlayer(p) + ChatColor.GRAY + "] " + ChatColor.BLUE + e.getPlayer().getName() + ChatColor.GRAY +" : " + e.getMessage());
        }else{
            Bukkit.broadcastMessage(ChatColor.BLUE + e.getPlayer().getName() + ChatColor.GRAY +" : " + e.getMessage());
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

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent e){
        if(!e.getAction().isRightClick()) return;
        if(!Logics.isChunkClaimed(e.getPlayer().getChunk())) return;
        if(Logics.isChunkClaimedWithPlayerTown(e.getPlayer(), e.getPlayer().getChunk())) return;
        e.setCancelled(true);
        e.getPlayer().sendMessage(Prefixes.townBasicPrefix + "This is not your town! You can't do that here!");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        if(Main.getClaimsView().contains(e.getPlayer().getUniqueId())){
            List<UUID> list = Main.getClaimsView();
            list.remove(e.getPlayer().getUniqueId());
            Main.setClaimsView(list);
        }
        if(Main.getLastActionBarState().containsKey(e.getPlayer())){
            HashMap<Player, String> map = Main.getLastActionBarState();
            map.remove(e.getPlayer());
            Main.setLastActionBarState(map);
        }
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent e){
        if(!Logics.isPlayerResidentOfATown((Player) e.getPlayer())) return;
        List<ItemStack> townBank = new ArrayList<>();
        townBank = Arrays.asList(e.getInventory().getContents());
        if(e.getView().getTitle().equals("Bank")){
            TownCache town = Logics.getTownByName(Logics.getTownOfPlayer(((Player) e.getPlayer())));
            assert town != null;
            town.setTownBank(townBank);
            Logics.pushBankToSQL(town.getTownName(), town.getTownBank());
        }
    }

    @EventHandler
    public void onExplosion(BlockExplodeEvent e){
        if(Logics.isChunkPrimary(e.getBlock().getChunk())) e.setCancelled(true);
    }
}

