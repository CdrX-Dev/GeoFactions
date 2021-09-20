package me.cdrx.gui.menus;

import me.cdrx.Main;
import me.cdrx.Prefixes;
import me.cdrx.gui.Menu;
import me.cdrx.gui.PlayerMenuUtility;
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

public class CreatingTownMenu extends Menu {

    public CreatingTownMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return "Create a town";
    }

    @Override
    public int getSlots() {
        return 27;
    }
    
    @Override
    public void handleMenu(InventoryClickEvent e) {
        if(e.getView().getTitle().equals("Create a town")) {
            e.setCancelled(true);
            if (e.getSlot() == 12) {
                //inventaire se ferme
                Player player = (Player) e.getWhoClicked();
                player.closeInventory();

                //message de écrire nom de la ville dans le chat
                player.sendMessage(Prefixes.townBasicPrefix + "Type the name of your city");
                player.sendMessage(Prefixes.townBasicPrefix + "Type 'cancel' to cancel this action!");

                //reconnaitre nom de la ville écris dans le chat
                HashMap<UUID, String> list = Main.getTypingPlayers();
                list.put(e.getWhoClicked().getUniqueId(), "create_town");
                Main.setTypingPlayers(list);
            } else if (e.getSlot() == 14) {
                e.getWhoClicked().closeInventory();
            }
        }
    }

    @Override
    public void setMenuItems() {
        Player p = playerMenuUtility.getPlayer();

        //Bouton pour accepter
        ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "YES" );
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Create a new town");
        meta.setLore(lore);
        item.setItemMeta(meta);
        inventory.setItem(12, item);

        //Bouton pour refuser
        ItemStack itemNo = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta metaNo = itemNo.getItemMeta();
        metaNo.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "NO" );
        List<String> loreNo = new ArrayList<>();
        loreNo.add(ChatColor.GRAY + "Cancel");
        metaNo.setLore(loreNo);
        itemNo.setItemMeta(metaNo);
        inventory.setItem(14, itemNo);


    }
}
