package me.cdrx.gui.menus;

import me.cdrx.geofactions.Logics;
import me.cdrx.gui.Menu;
import me.cdrx.gui.PlayerMenuUtility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TownPopulationMenu extends Menu {
    public TownPopulationMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return "Town Population Menu";
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (e.getView().getTitle().equals("Town Population Menu")) {
            e.setCancelled(true);
            Player player = (Player) e.getWhoClicked();
            if(e.getSlot() == 45){
                player.closeInventory();
                player.performCommand("geofactions");
            }

        }
    }

    @Override
    public void setMenuItems(){
        //Contour
        int[] list = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 46, 47, 48, 49, 50, 51, 52, 53};
        for (int integer : list) {
            ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            inventory.setItem(integer, item);

            Player p = playerMenuUtility.getOwner();
            UUID[] playersList = Logics.getTownByName(Logics.getTownOfPlayer(p)).getResidents();
            int[] intList = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 31, 37, 38, 39, 40, 41, 42, 43};
            for (int i = 0; i < playersList.length; i++) {
                UUID playerUUID = playersList[i];
                Player player = Bukkit.getPlayer(playerUUID);
                int integer1 = intList[i];
                String role = Logics.getPlayerRole(player.getUniqueId());
                ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
                SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
                skullMeta.setOwner(player.getName());
                skullMeta.setDisplayName(ChatColor.GREEN + player.getName());
                List<String> lorehead = new ArrayList<String>();
                lorehead.add(role);
                skullMeta.setLore(lorehead);
                head.setItemMeta(skullMeta);
                inventory.setItem(integer1, head);
            }
        }
        //Back
        ItemStack bck = new ItemStack(Material.REDSTONE);
        ItemMeta metabck = bck.getItemMeta();
        metabck.setDisplayName("BACK");
        bck.setItemMeta(metabck);
        inventory.setItem(45,bck);
    }

}
