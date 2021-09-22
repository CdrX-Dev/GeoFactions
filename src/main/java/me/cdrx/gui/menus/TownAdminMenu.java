package me.cdrx.gui.menus;

import me.cdrx.Main;
import me.cdrx.Prefixes;
import me.cdrx.geofactions.TownCache;
import me.cdrx.gui.Menu;
import me.cdrx.gui.PlayerMenuUtility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TownAdminMenu extends Menu {
    public TownAdminMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return "Town Admin Menu";
    }

    @Override
    public int getSlots() {
        return 45;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (e.getView().getTitle().equals("Town Admin Menu")) {
            e.setCancelled(true);
            Player player = (Player) e.getWhoClicked();

            //Habitants
            if (e.getSlot() == 10) {
                Menu inv = new TownPopulationMenu(playerMenuUtility);
                inv.open();
            }
            //Invite
            else if(e.getSlot() == 34) {
                player.closeInventory();
                player.sendMessage(Prefixes.townBasicPrefix + "Type the name of the new member");
                player.sendMessage(Prefixes.townBasicPrefix + "Type 'cancel' to cancel this action!");
                HashMap<UUID, String> map = Main.getTypingPlayers();
                map.put(player.getUniqueId(), "invite_player");
                Main.setTypingPlayers(map);
            }
            //banque
            else if (e.getSlot() ==  28) {
                Menu inv = new TownBankMenu(playerMenuUtility);
                inv.open();
            }
            //claim
            else if (e.getSlot() == 16) {
                List<UUID> list = Main.getClaimsView();
                if(list.contains(player.getUniqueId())){
                    list.remove(player.getUniqueId());
                } else{
                    list.add(player.getUniqueId());
                }
                Main.setClaimsView(list);
            }

        }
    }

    @Override
    public void setMenuItems() {
        Player p = playerMenuUtility.getOwner();

        //Contour
        int[] list = {0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,37,38,39,40,41,42,43,44};
        for(int integer : list) {
            ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            inventory.setItem(integer, item);
        }
        //Habitants
        ItemStack pop = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta metapop = pop.getItemMeta();
        metapop.setDisplayName(ChatColor.GRAY + "POPULATION");
        List<String> lorepop = new ArrayList<String>();
        lorepop.add("Your population");
        metapop.setLore(lorepop);
        pop.setItemMeta(metapop);
        inventory.setItem(10,pop);

        //Banque
        ItemStack bnk = new ItemStack(Material.CHEST);
        ItemMeta metabnk = bnk.getItemMeta();
        metabnk.setDisplayName(ChatColor.GRAY + "BANK");
        List<String> lorebnk = new ArrayList<String>();
        lorebnk.add("Your town bank");
        metabnk.setLore(lorebnk);
        bnk.setItemMeta(metabnk);
        inventory.setItem(28,bnk);

        //Claim
        ItemStack clm = new ItemStack(Material.IRON_AXE);
        ItemMeta metaclm = clm.getItemMeta();
        metaclm.setDisplayName(ChatColor.GRAY + "CLAIM");
        List<String> loreclm = new ArrayList<String>();
        loreclm.add("See your claims whit a map");
        metaclm.setLore(loreclm);
        clm.setItemMeta(metaclm);
        inventory.setItem(16,clm);

        //Recap
        List<TownCache> list1 = Main.getTownCache();
        String townName = null;
        String owner = null;
        int population = 0;
        int claimedchunk = 0;
        for(TownCache cache: list1){
            for(UUID uuid : cache.getResidents()){
                if(uuid.equals(p.getUniqueId())){
                    townName = cache.getTownName();
                    population = cache.getResidents().length;
                    claimedchunk = cache.getClaimedChunks().length;
                    owner = Bukkit.getPlayer(cache.getOwner()).getName();
                    break;
                }
            }
        }

        ItemStack rcp = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta metarcp = rcp.getItemMeta();
        metarcp.setDisplayName(ChatColor.GRAY + townName);
        List<String> lorercp = new ArrayList<String>();
        lorercp.add("-----------------------------");
        lorercp.add("Residents: " + population);
        lorercp.add("Claimed chunk: " + claimedchunk);
        lorercp.add("Owner: " + owner);
        //TODO: Status de guerre
        lorercp.add("-----------------------------");
        metarcp.setLore(lorercp);
        rcp.setItemMeta(metarcp);
        inventory.setItem(22,rcp);

        //Invite a player
        ItemStack inv = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
        ItemMeta metainv = inv.getItemMeta();
        metainv.setDisplayName(ChatColor.GRAY + "Invite a player");
        List<String> loreinv = new ArrayList<String>();
        loreinv.add("Invite a friend or a servant!");
        metainv.setLore(loreinv);
        inv.setItemMeta(metainv);
        inventory.setItem(34,inv);
    }
}
