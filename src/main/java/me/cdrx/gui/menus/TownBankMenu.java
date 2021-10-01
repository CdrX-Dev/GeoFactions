package me.cdrx.gui.menus;

import me.cdrx.geofactions.Logics;
import me.cdrx.gui.Menu;
import me.cdrx.gui.PlayerMenuUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class TownBankMenu extends Menu {
    public TownBankMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return "Bank";
    }

    @Override
    public int getSlots() {
        return 27;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Logics.getTownByName(Logics.getTownOfPlayer(p)).setTownBank(Arrays.asList(e.getInventory().getContents()));
    }

    @Override
    public void setMenuItems() {
        ItemStack[] items = new ItemStack[Logics.getTownByName(Logics.getTownOfPlayer(playerMenuUtility.getOwner())).getTownBank().size()];
        items = Logics.getTownByName(Logics.getTownOfPlayer(playerMenuUtility.getOwner())).getTownBank().toArray(items);
        inventory.setContents(items);
    }
}
