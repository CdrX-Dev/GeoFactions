package me.cdrx.gui.menus;

import me.cdrx.Main;
import me.cdrx.Prefixes;
import me.cdrx.geofactions.TownCache;
import me.cdrx.gui.Menu;
import me.cdrx.gui.PlayerMenuUtility;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
        return 45;
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
            //Invite
            else if (e.getSlot() == 25) {
                player.closeInventory();
                player.sendMessage(Prefixes.townBasicPrefix + "Type the name of the new member");
                player.sendMessage(Prefixes.townBasicPrefix + "Type 'cancel' to cancel this action!");

                HashMap<UUID, String> map = Main.getTypingPlayers();
                map.put(player.getUniqueId(), "invite_player");
                Main.setTypingPlayers(map);
            }
            //Banque
            else if (e.getSlot() == 28) {
                Menu inv = new TownBankMenu(playerMenuUtility);
                inv.open();
            }
            //Back
            else if (e.getSlot() == 45) {
                player.closeInventory();
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
                message.setColor(net.md_5.bungee.api.ChatColor.RED);
                message.setBold(true);
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Think twice before clicking").color(net.md_5.bungee.api.ChatColor.GRAY).italic(true).create()));
                //-------------------------------------------------------------------------
                //TODO: Créer cette commande la (geofactions dontDeleteTown [player UUID])
                //-------------------------------------------------------------------------
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/geofactions dontDeleteTown " + player.getUniqueId()));

                player.spigot().sendMessage(message);
                player.spigot().sendMessage(message1);
            }
            //claim
            else if (e.getSlot() == 16){
                //-------------------------------------------------------------------------
                //TODO: CED OCCUPE TOI DE ÇA
                //-------------------------------------------------------------------------
            }
        }
    }

    @Override
    public void setMenuItems() {
        Player p = playerMenuUtility.getPlayer();

        //Contour
        int[] list = {0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,46,47,48,49,50,51,52,53};
        for(int integer : list) {
            ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            inventory.setItem(integer, item);
        }

        //Back
        ItemStack bck = new ItemStack(Material.REDSTONE);
        ItemMeta metabck = bck.getItemMeta();
        metabck.setDisplayName(ChatColor.GRAY + "Leave");
        inventory.setItem(45,bck);

        //Habitants
        ItemStack pop = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta metapop = pop.getItemMeta();
        metapop.setDisplayName(ChatColor.GRAY + "POPULATION");
        List<String> lorepop = new ArrayList<String>();
        lorepop.add("Your population");
        metapop.setLore(lorepop);
        inventory.setItem(10,pop);

        //Rôles
        ItemStack rol = new ItemStack(Material.STICK);
        ItemMeta metarol = rol.getItemMeta();
        metarol.setDisplayName(ChatColor.GRAY + "ROLES");
        List<String> lorerol = new ArrayList<String>();
        lorerol.add("You can see the roles here");
        metarol.setLore(lorerol);
        inventory.setItem(19,rol);

        //Banque
        ItemStack bnk = new ItemStack(Material.GOLD_INGOT);
        ItemMeta metabnk = bnk.getItemMeta();
        metabnk.setDisplayName(ChatColor.GRAY + "BANK");
        List<String> lorebnk = new ArrayList<String>();
        //TO DO syncroniser inventaire
        lorebnk.add("Your money");
        metabnk.setLore(lorebnk);
        inventory.setItem(28,bnk);

        //Claim
        ItemStack clm = new ItemStack(Material.IRON_AXE);
        ItemMeta metaclm = clm.getItemMeta();
        metaclm.setDisplayName(ChatColor.GRAY + "CLAIM");
        List<String> loreclm = new ArrayList<String>();
        loreclm.add("See your claims whit a map");
        metaclm.setLore(loreclm);
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
        inventory.setItem(22,rcp);




        //Delete
        ItemStack del = new ItemStack(Material.BARRIER);
        ItemMeta metadel = del.getItemMeta();
        metadel.setDisplayName(ChatColor.GRAY + "DELETE");
        List<String> loredel = new ArrayList<String>();
        loredel.add("Delete your town" + ChatColor.BOLD + ChatColor.RED + "FOREVER");
        metadel.setLore(loredel);
        inventory.setItem(37,del);

        //Invite a player
        ItemStack inv = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
        ItemMeta metainv = inv.getItemMeta();
        metainv.setDisplayName(ChatColor.GRAY + "Invite a player");
        inventory.setItem(25,inv);
    }
}
