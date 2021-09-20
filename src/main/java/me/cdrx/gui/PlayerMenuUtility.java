package me.cdrx.gui;

import org.bukkit.entity.Player;

public class PlayerMenuUtility {
    //Classe (Objet) attribué a chaque joueurs dans une Collection dans la classe Main (Permet de stocker des infos sur les menus pour chaque joeuurs)

    private Player player;

    public PlayerMenuUtility(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
