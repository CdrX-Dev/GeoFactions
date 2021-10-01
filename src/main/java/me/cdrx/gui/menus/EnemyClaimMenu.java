package me.cdrx.gui.menus;

import me.cdrx.geofactions.Logics;
import me.cdrx.gui.Menu;
import me.cdrx.gui.PlayerMenuUtility;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class EnemyClaimMenu extends Menu {
    public EnemyClaimMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return "Enemy claim menu";
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = playerMenuUtility.getOwner();
        //Commence la proceduire de guerre!
        if(e.getSlot() == 13){
            Logics.warBeginProcedure(player, player.getChunk());
        }
    }

    @Override
    public void setMenuItems() {
        //Contour
        int[] list = {0,1,2,3,4,5,6,7,8,9,17,18,19,20,21,22,23,24,25,26};
        for(int integer : list) {
            ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            inventory.setItem(integer, item);

        //Déclancher guerre
        ItemStack axe = new ItemStack(Material.IRON_AXE);
        ItemMeta metaaxe = axe.getItemMeta();
        metaaxe.setDisplayName(ChatColor.GRAY + "START A WAR");
        List<String> loreaxe = new ArrayList<String>();
        loreaxe.add("Start a war against another town");
        metaaxe.setLore(loreaxe);
        axe.setItemMeta(metaaxe);
        inventory.setItem(13,axe);
        }
    }
}
