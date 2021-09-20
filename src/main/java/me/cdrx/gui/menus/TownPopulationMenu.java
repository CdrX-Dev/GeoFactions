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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class TownPopulationMenu extends Menu {
    public TownPopulationMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return null;
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {

    }

    @Override
    public void setMenuItems() {
        Player p = playerMenuUtility.getPlayer();
        Player[] playersList = getTownPlayers();
        int[] intList = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,31,37,38,39,40,41,42,43};
        for(int i = 0 ; i < intList.length ; i++){
            Player player = playersList[i];
            int integer = intList[i];
            String role = Logics.getPlayerRole(player.getUniqueId());
            ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            skullMeta.setOwner(player.getName());
            skullMeta.setDisplayName(ChatColor.GREEN + player.getName());
            List<String> lorehead = new ArrayList<String>();
            lorehead.add(role);
            skullMeta.setLore(lorehead);
            head.setItemMeta(skullMeta);
            inventory.setItem(integer, head);
        }

    }

    private Player[] getTownPlayers() {
        //
        //TODO: logique de la methode pour get les vrais joueurs de la town
        //
        Player[] players = (Player[]) Bukkit.getOnlinePlayers().toArray();
        return players;
    }
}
