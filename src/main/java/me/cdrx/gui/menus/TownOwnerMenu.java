package me.cdrx.gui.menus;

import me.cdrx.Main;
import me.cdrx.Prefixes;
import me.cdrx.geofactions.Logics;
import me.cdrx.geofactions.TownCache;
import me.cdrx.gui.Menu;
import me.cdrx.gui.PlayerMenuUtility;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TownOwnerMenu extends Menu {
    public TownOwnerMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return "Town Owner Menu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (e.getView().getTitle().equals("Town Owner Menu")) {
            e.setCancelled(true);
            Player player = (Player) e.getWhoClicked();

            //Population
            if (e.getSlot() == 10) {
                Menu inv = new TownPopulationMenu(playerMenuUtility);
                inv.open();
            }
            //Role
            if (e.getSlot() == 19) {
                Menu inv = new RolesMenu(playerMenuUtility);
                inv.open();
            }

            //Invite
            else if (e.getSlot() == 34) {
                player.closeInventory();
                player.sendMessage(Prefixes.townBasicPrefix + "Type the name of the new member");
                player.sendMessage(Prefixes.townBasicPrefix + "Type 'cancel' to cancel this action!");

                HashMap<UUID, String> map = Main.getTypingPlayers();
                map.put(player.getUniqueId(), "invite_player");
                Main.setTypingPlayers(map);
            }else if(e.getSlot() == 43){
                Chunk c = player.getChunk();
                if(!Logics.isChunkClaimed(c)) return;
                if(!Logics.isChunkClaimedWithPlayerTown(player, c)){
                    Menu menu = new EnemyClaimMenu(playerMenuUtility);
                    menu.open();
                }

            //claim
            }else if(e.getSlot() == 25){
                if(e.getCurrentItem().getType().equals(Material.IRON_AXE)) {
                    Logics.claimChunk(e.getWhoClicked().getChunk(), Logics.getTownOfPlayer((Player) e.getWhoClicked()),(Player) e.getWhoClicked());
                    player.closeInventory();
                    new TownOwnerMenu(playerMenuUtility).open();
                }else if(e.getCurrentItem().getType().equals(Material.GOLDEN_AXE)){
                    player.closeInventory();
                    Logics.setChunkAsPrimary(player, player.getChunk(), Logics.getTownOfPlayer(player));
                    new TownOwnerMenu(playerMenuUtility).open();
                }
            }
            //Banque
            else if (e.getSlot() == 28) {
                if(!Logics.isPlayerOfTownAlreadyInBankInventory(Logics.getTownByName(Logics.getTownOfPlayer(player)))){
                    Menu inv = new TownBankMenu(playerMenuUtility);
                    inv.open();
                }else{
                    player.sendMessage(Prefixes.townBasicPrefix + "Someone is already in the town bank! Wait until he's not.");
                }
            //unclaim
            }else if(e.getSlot() == 24){
                if(e.getCurrentItem().getType().equals(Material.STONE_AXE)){
                    Logics.removePrimaryChunk(player, player.getChunk());
                    player.closeInventory();
                    new TownOwnerMenu(playerMenuUtility).open();
                }else if(e.getCurrentItem().getType().equals(Material.WOODEN_AXE)){
                    Logics.unclaimChunk(player, player.getChunk());
                    player.closeInventory();
                    new TownOwnerMenu(playerMenuUtility).open();
                }
            }
            //Delete
            else if (e.getSlot() == 37) {
                player.closeInventory();
                player.sendMessage("Are you sure you want to delete your town?");
                TextComponent message = new TextComponent("[YES]");
                message.setColor(net.md_5.bungee.api.ChatColor.GREEN);
                message.setBold(true);
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Think twice before clicking").color(net.md_5.bungee.api.ChatColor.GRAY).italic(true).create()));
                //-------------------------------------------------------------------------
                //TODO: Créer cette commande la (geofactions deleteTown [player UUID])
                //-------------------------------------------------------------------------
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/geofactions deleteTown " + player.getUniqueId()));
                TextComponent message1 = new TextComponent("[NO]");
                message1.setColor(net.md_5.bungee.api.ChatColor.RED);
                message1.setBold(true);
                message1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Think twice before clicking").color(net.md_5.bungee.api.ChatColor.GRAY).italic(true).create()));
                //-------------------------------------------------------------------------
                //TODO: Créer cette commande la (geofactions dontDeleteTown [player UUID])
                //-------------------------------------------------------------------------
                message1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/geofactions dontDeleteTown " + player.getUniqueId()));

                player.spigot().sendMessage(message);
                player.spigot().sendMessage(message1);
            }
            //claim vision
            else if (e.getSlot() == 16) {
                List<UUID> list = Main.getClaimsView();
                if(list.contains(player.getUniqueId())){
                    list.remove(player.getUniqueId());
                } else{
                    list.add(player.getUniqueId());
                }
                Main.setClaimsView(list);

            }
            //kick player
            else if (e.getSlot() == 33) {
                player.closeInventory();
                player.sendMessage(Prefixes.townBasicPrefix + "Type the name of the player that you want to kick from your town!");

                HashMap<UUID, String> map = Main.getTypingPlayers();
                map.put(player.getUniqueId(), "demote_resident");
                Main.setTypingPlayers(map);
            }
        }
    }

    @Override
    public void setMenuItems() {
        Player p = playerMenuUtility.getOwner();

        //Contour
        int[] list = {0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53};
        for(int integer : list) {
            ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            inventory.setItem(integer, item);
        }
        //Habitants
        ItemStack pop = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta metapop = pop.getItemMeta();
        metapop.setDisplayName(ChatColor.GRAY + "POPULATION");
        ArrayList<String> lorepop = new ArrayList<String>();
        lorepop.add("Your population");
        metapop.setLore(lorepop);
        pop.setItemMeta(metapop);
        inventory.setItem(10,pop);

        //Rôles
        ItemStack rol = new ItemStack(Material.STICK);
        ItemMeta metarol = rol.getItemMeta();
        metarol.setDisplayName(ChatColor.GRAY + "ROLES");
        ArrayList<String> lorerol = new ArrayList<String>();
        lorerol.add("You can see the roles here");
        metarol.setLore(lorerol);
        rol.setItemMeta(metarol);
        inventory.setItem(19,rol);

        //Banque
        ItemStack bnk = new ItemStack(Material.CHEST);
        ItemMeta metabnk = bnk.getItemMeta();
        metabnk.setDisplayName(ChatColor.GRAY + "BANK");
        ArrayList<String> lorebnk = new ArrayList<String>();
        //TO DO syncroniser inventaire
        lorebnk.add("Your town bank");
        metabnk.setLore(lorebnk);
        bnk.setItemMeta(metabnk);
        inventory.setItem(28,bnk);

        //Claim vision
        ItemStack clm = new ItemStack(Material.ENDER_EYE);
        ItemMeta metaclm = clm.getItemMeta();
        metaclm.setDisplayName(ChatColor.GRAY + "CLAIM VISION");
        ArrayList<String> loreclm = new ArrayList<String>();
        loreclm.add("See your claims");
        metaclm.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        metaclm.setLore(loreclm);
        clm.setItemMeta(metaclm);
        inventory.setItem(16,clm);

        //Claim
        if(!Logics.isChunkClaimed(p.getChunk())) {
            ItemStack cla = new ItemStack(Material.IRON_AXE);
            ItemMeta metacla = cla.getItemMeta();
            metacla.setDisplayName(ChatColor.GRAY + "CLAIM");
            ArrayList<String> lorecla = new ArrayList<String>();
            lorecla.add("Claim this chunk");
            metacla.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            metacla.setLore(lorecla);
            cla.setItemMeta(metacla);
            inventory.setItem(25, cla);
        }else{
            if(Logics.isChunkClaimedWithPlayerTown(p, p.getChunk())){
                ItemStack cla = new ItemStack(Material.GOLDEN_AXE);
                ItemMeta metacla = cla.getItemMeta();
                metacla.setDisplayName(ChatColor.GRAY + "CLAIM AS PRIMARY");
                ArrayList<String> lorecla = new ArrayList<String>();
                lorecla.add("Claim this chunk");
                metacla.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                metacla.setLore(lorecla);
                cla.setItemMeta(metacla);
                inventory.setItem(25, cla);
            }
        }

        //unclaim
        if(Logics.isChunkClaimedWithPlayerTown(p, p.getChunk())) {
            if(Logics.isChunkPrimary(p.getChunk())){
                ItemStack pri = new ItemStack(Material.STONE_AXE);
                ItemMeta metapri = pri.getItemMeta();
                metapri.setDisplayName(ChatColor.GRAY + "UNCLAIM A PRIMARY CHUNK");
                ArrayList<String> lorepri = new ArrayList<String>();
                lorepri.add("Unclaim this chunk");
                metapri.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                metapri.setLore(lorepri);
                pri.setItemMeta(metapri);
                inventory.setItem(24, pri);
            }else{
                ItemStack unc = new ItemStack(Material.WOODEN_AXE);
                ItemMeta metaunc = unc.getItemMeta();
                metaunc.setDisplayName(ChatColor.GRAY + "UNCLAIM");
                ArrayList<String> loreunc = new ArrayList<String>();
                loreunc.add("Unclaim this chunk");
                metaunc.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                metaunc.setLore(loreunc);
                unc.setItemMeta(metaunc);
                inventory.setItem(24, unc);
            }
        }

        //Recap
        List<TownCache> list1 = Main.getTownCache();
        String townName = null;
        String owner = null;
        int population = 0;
        int claimedchunk = 0;
        TownCache cache = Logics.getTownByName(Logics.getTownOfPlayer(p));
        townName = cache.getTownName();
        population = cache.getResidents().length;
        claimedchunk = cache.getClaimedChunks().length;
        owner = Bukkit.getOfflinePlayer(cache.getOwner()).getName();

        ItemStack rcp = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta metarcp = rcp.getItemMeta();
        metarcp.setDisplayName(ChatColor.GRAY + townName);
        List<String> lorercp = new ArrayList<String>();
        lorercp.add("-------------------------");
        lorercp.add("Residents: " + population);
        lorercp.add("Claimed chunks: " + claimedchunk);
        lorercp.add("Owner: " + owner);
        //TODO: Status de guerre
        lorercp.add("-------------------------");
        metarcp.setLore(lorercp);
        rcp.setItemMeta(metarcp);
        inventory.setItem(22,rcp);


        //Delete
        ItemStack del = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta metadel = del.getItemMeta();
        metadel.setDisplayName(ChatColor.GRAY + "DELETE");
        List<String> loredel = new ArrayList<String>();
        loredel.add("Delete your town " + ChatColor.BOLD + ChatColor.RED + "FOREVER");
        metadel.setLore(loredel);
        del.setItemMeta(metadel);
        inventory.setItem(37,del);

        //Invite a player
        ItemStack inv = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
        ItemMeta metainv = inv.getItemMeta();
        metainv.setDisplayName(ChatColor.GRAY + "INVITE A PLAYER");
        List<String> loreinv = new ArrayList<String>();
        loreinv.add("Invite a friend or a servant!");
        metainv.setLore(loreinv);
        inv.setItemMeta(metainv);
        inventory.setItem(34,inv);

        //Kick player
        ItemStack kk = new ItemStack(Material.BARRIER);
        ItemMeta metakk = kk.getItemMeta();
        metakk.setDisplayName(ChatColor.GRAY + "KICK A PLAYER");
        List<String> lorekk = new ArrayList<String>();
        lorekk.add("Kick a player from the town");
        metakk.setLore(lorekk);
        kk.setItemMeta(metakk);
        inventory.setItem(33,kk);

        /*
        //Pour mettre le bouton si le joueur est dans un chunk enemi
        if(!Logics.isChunkClaimed(p.getChunk())) return;
        if(!Logics.isChunkClaimedWithPlayerTown(p, p.getChunk())){
            ItemStack enemyClaimButton = new ItemStack(Material.RED_WOOL, 1);
            ItemMeta meta = enemyClaimButton.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "Enemy Claim");
            enemyClaimButton.setItemMeta(meta);
            inventory.setItem(43, enemyClaimButton);
        }
         */
    }
}
