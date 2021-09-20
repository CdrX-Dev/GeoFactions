package me.cdrx.gui;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class Menu implements InventoryHolder {
    //Classe abstraite donnant l'heritage aux menus (Non-Dumbs)

    protected Inventory inventory;

    protected PlayerMenuUtility playerMenuUtility;

    public Menu(PlayerMenuUtility playerMenuUtility){
        this.playerMenuUtility = playerMenuUtility;
    }

    public abstract String getMenuName();

    public abstract int getSlots();

    public abstract void handleMenu(InventoryClickEvent e);

    public abstract void setMenuItems();

    public void open(){
        inventory = Bukkit.createInventory(null, getSlots(), getMenuName());
        this.setMenuItems();
        playerMenuUtility.getPlayer().openInventory(inventory);
    }

    @Override
    public Inventory getInventory(){
        return inventory;
    }
}
