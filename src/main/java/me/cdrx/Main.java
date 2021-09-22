package me.cdrx;

import me.cdrx.geofactions.GeoFactionsCommands;
import me.cdrx.geofactions.Logics;
import me.cdrx.geofactions.TownCache;
import me.cdrx.gui.Listeners.MenuListener;
import me.cdrx.gui.PlayerMenuUtility;
import me.cdrx.sql.DbManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Main extends JavaPlugin {
    private static DbManager dbManager;
    private static final HashMap<Player, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();
    public static List<TownCache> townCache = new ArrayList<>();
    private static HashMap<UUID, String> typingPlayers = new HashMap<>();
    private static List<UUID> claimsView = new ArrayList<>();

    @Override
    public void onEnable() {
        dbManager =  new DbManager();
        getServer().getPluginManager().registerEvents(new Listeners(), this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        this.getCommand("geofactions").setExecutor(new GeoFactionsCommands());
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("GeoFactions");

        Logics.initializeCache();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                List<UUID> list = Main.getClaimsView();
                for(UUID uuid : list){
                    Player p = Bukkit.getPlayer(uuid);
                    double playerYLevel = p.getLocation().getY();
                    Chunk chunk = p.getChunk();
                    World world = chunk.getWorld();
                    int chunkX = chunk.getX();
                    int chunkZ = chunk.getZ();

                    int cornerX = chunkX * 16;
                    int cornerZ = chunkZ * 16;

                    List<Location> locationsList = new ArrayList<>();
                    for(int i = 0 ; i < 16 ; i++){
                        locationsList.add(new Location(world, cornerX + i, playerYLevel + 1, cornerZ));
                        locationsList.add(new Location(world, cornerX + i, playerYLevel + 1, cornerZ + 16));

                        locationsList.add(new Location(world, cornerX, playerYLevel + 1, cornerZ + i));
                        locationsList.add(new Location(world, cornerX + 16, playerYLevel + 1, cornerZ + i));
                    }

                    Particle particlesType = Particle.REDSTONE;
                    Particle.DustOptions dustOptions = new Particle.DustOptions(Color.GRAY, 1.5F);

                    if(Logics.isChunkClaimed(chunk)){
                        if(Logics.isChunkClaimedWithPlayerTown(p, chunk)){
                            dustOptions = new Particle.DustOptions(Color.GREEN, 1.5F);
                        }else{
                            dustOptions = new Particle.DustOptions(Color.RED, 1.5F);
                        }
                    }
                    for(Location loc : locationsList){
                        p.spawnParticle(particlesType, loc, 1);
                    }
                }
            }
        }, 0L, 30L); //0 Tick initial delay, 20 Tick (1 Second) between repeats
    }

    @Override
    public void onDisable() {
        Logics.closeCacheProcedure();
        dbManager.close();
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
    public static List<UUID> getClaimsView() {
        return claimsView;
    }
    public static void setClaimsView(List<UUID> claimsView) {
        Main.claimsView = claimsView;
    }
}
