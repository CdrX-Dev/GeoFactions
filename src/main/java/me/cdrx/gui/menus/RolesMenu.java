package me.cdrx.gui.menus;

import me.cdrx.Main;
import me.cdrx.Prefixes;
import me.cdrx.gui.Menu;
import me.cdrx.gui.PlayerMenuUtility;
import org.bukkit.ChatColor;
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

public class RolesMenu extends Menu {
    public RolesMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return "Role menu";
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (e.getView().getTitle().equals("Role menu")) {
            e.setCancelled(true);
            Player player = (Player) e.getWhoClicked();
            String action = "promote";
            if(e.isRightClick()) action = "demote";
            if (e.getSlot() == 11) {
                player.closeInventory();
                player.sendMessage(Prefixes.townBasicPrefix + "Type the name of the player you want to " + action + " " + ChatColor.RED + "" + ChatColor.BOLD + " Owner");
                player.sendMessage(Prefixes.townBasicPrefix + "Type 'cancel' to cancel this action!");

                HashMap<UUID, String> hashMap = Main.getTypingPlayers();
                hashMap.put(player.getUniqueId(), action + "_owner");
                Main.setTypingPlayers(hashMap);
            } else if (e.getSlot() == 13) {
                player.closeInventory();
                player.sendMessage(Prefixes.townBasicPrefix + "Type the name of the player you want to " + action + " "  + ChatColor.BLUE + "" + ChatColor.BOLD + " Admin");
                player.sendMessage(Prefixes.townBasicPrefix + "Type 'cancel' to cancel this action!");

                HashMap<UUID, String> hashMap = Main.getTypingPlayers();
                hashMap.put(player.getUniqueId(), action + "_admin");
                Main.setTypingPlayers(hashMap);
            } else if (e.getSlot() == 15) {
                player.closeInventory();
                player.sendMessage(Prefixes.townBasicPrefix + "Type the name of the player you want to " + action + " "  + ChatColor.GREEN + "" + ChatColor.BOLD + " Treasurer");
                player.sendMessage(Prefixes.townBasicPrefix + "Type 'cancel' to cancel this action!");

                HashMap<UUID, String> hashMap = Main.getTypingPlayers();
                hashMap.put(player.getUniqueId(), action + "_treasurer");
                Main.setTypingPlayers(hashMap);
            }  else if (e.getSlot() == 18) {
                player.closeInventory();
                player.performCommand("geofactions");
            }
        }
     }

    @Override
    public void setMenuItems() {
        //Contour
        int[] list = {0,1,2,3,4,5,6,7,8,9,17,19,20,21,22,23,24,25,26};
        for(int integer : list) {
            ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            inventory.setItem(integer, item);
        }
        //Back
        ItemStack bck = new ItemStack(Material.REDSTONE);
        ItemMeta metabck = bck.getItemMeta();
        metabck.setDisplayName("BACK");
        bck.setItemMeta(metabck);
        inventory.setItem(18,bck);

        //Owner
        ItemStack own = new ItemStack(Material.GOLDEN_HELMET);
        ItemMeta metaown = own.getItemMeta();
        metaown.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD+ "OWNER");
        List<String> loreown = new ArrayList<String>();
        loreown.add(ChatColor.GRAY + "Has every rights in the town");
        metaown.setLore(loreown);
        metaown.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        own.setItemMeta(metaown);
        inventory.setItem(11,own);

        //Admin
        ItemStack adm = new ItemStack(Material.IRON_SWORD);
        ItemMeta metaadm = adm.getItemMeta();
        metaadm.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD+"ADMIN");
        List<String> loreadm = new ArrayList<String>();
        loreadm.add(ChatColor.GRAY + "Has most of the rights in the town");
        metaadm.setLore(loreadm);
        metaadm.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        adm.setItemMeta(metaadm);
        inventory.setItem(13,adm);

        //Treasurer
        ItemStack tre = new ItemStack(Material.CHEST);
        ItemMeta metatre = tre.getItemMeta();
        metatre.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "TREASURER");
        List<String> loretre = new ArrayList<String>();
        loretre.add(ChatColor.GRAY + "You have rights! Not a lot but still");
        metatre.setLore(loretre);
        tre.setItemMeta(metatre);
        inventory.setItem(15,tre);


        }
}

