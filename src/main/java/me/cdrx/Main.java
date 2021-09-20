package me.cdrx;

import me.cdrx.geofactions.GeoFactionsCommands;
import me.cdrx.geofactions.Logics;
import me.cdrx.geofactions.TownCache;
import me.cdrx.gui.PlayerMenuUtility;
import me.cdrx.sql.DbManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Main extends JavaPlugin {
    static DbManager dbManager;
    private static final HashMap<Player, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();
    public static List<TownCache> townCache = new ArrayList<>();
    private static HashMap<UUID, String> typingPlayers = new HashMap<>();

    @Override
    public void onEnable() {
        Logics.initializeCache();
        dbManager =  new DbManager();
        getServer().getPluginManager().registerEvents(new Listeners(), this);
        this.getCommand("geofactions").setExecutor(new GeoFactionsCommands());
    }

    @Override
    public void onDisable() {
    }

    public static DbManager getDatabaseManager(){
        return dbManager;
    }
    public static PlayerMenuUtility getPlayerMenuUtility(Player p){
        PlayerMenuUtility playerMenuUtility;
        if(playerMenuUtilityMap.containsKey(p)){
            return playerMenuUtilityMap.get(p);
        }else{
            playerMenuUtility = new PlayerMenuUtility(p);
            playerMenuUtilityMap.put(p, playerMenuUtility);

            return playerMenuUtility;
        }
    }
    public static HashMap<UUID, String> getTypingPlayers(){
        return typingPlayers;
    }
    public static void setTypingPlayers(HashMap<UUID, String> list){
        typingPlayers = list;
    }
    public static List<TownCache> getTownCache() {
        return townCache;
    }
    public static void setTownCache(List<TownCache> townCache) {
        Main.townCache = townCache;
    }
}
