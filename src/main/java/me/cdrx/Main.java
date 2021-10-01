package me.cdrx;

import me.cdrx.geofactions.GeoFactionsCommands;
import me.cdrx.geofactions.Logics;
import me.cdrx.geofactions.TownCache;
import me.cdrx.gui.Listeners.MenuListener;
import me.cdrx.gui.PlayerMenuUtility;
import me.cdrx.sql.DbManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
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
    private static HashMap<Player, String> lastActionBarState = new HashMap<>();
    private static HashMap<String, String> chunksWars = new HashMap<>();

    @Override
    public void onEnable() {
        //Register des recipes
        ItemStack shulker = new ItemStack(Material.SHULKER_SHELL);
        NamespacedKey shulkerKey = new NamespacedKey(this, "shulker_shell");
        ShapedRecipe shulkerRecipe = new ShapedRecipe(shulkerKey, shulker);
        shulkerRecipe.shape("B", "B", "B",
                            "B", "A", "B",
                            "B", "B", "B");
        shulkerRecipe.setIngredient('A', Material.PHANTOM_MEMBRANE);
        shulkerRecipe.setIngredient('B', Material.DIAMOND);
        Bukkit.addRecipe(shulkerRecipe);
        //
        dbManager =  new DbManager();
        getServer().getPluginManager().registerEvents(new Listeners(), this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        this.getCommand("geofactions").setExecutor(new GeoFactionsCommands());
        this.getCommand("claimsList").setExecutor(new ClaimsListCommands());
        this.getCommand("pluginBreakdown").setExecutor(new PluginBreakdownCommands());
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("GeoFactions");

        Logics.initializeCache();
        //Loop les joueurs qui sont en claims vision.
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
                        locationsList.add(new Location(world, cornerX + i, playerYLevel + 2, cornerZ));
                        locationsList.add(new Location(world, cornerX + i, playerYLevel + 2, cornerZ + 16));
                        locationsList.add(new Location(world, cornerX, playerYLevel + 2, cornerZ + i));
                        locationsList.add(new Location(world, cornerX + 16, playerYLevel + 2, cornerZ + i));
                    }

                    Particle particlesType = Particle.REDSTONE;
                    Particle.DustOptions dustOptions = new Particle.DustOptions(Color.GRAY, 1.5F);

                    if(Logics.isChunkClaimed(chunk)){
                        if(Logics.isChunkClaimedWithPlayerTown(p, chunk)){
                            if(Logics.isChunkPrimary(chunk)){
                                dustOptions = new Particle.DustOptions(Color.PURPLE, 1.5F);
                            }else{
                                dustOptions = new Particle.DustOptions(Color.LIME, 1.5F);
                            }
                        }else{
                            dustOptions = new Particle.DustOptions(Color.RED, 1.5F);
                        }
                    }
                    for(Location loc : locationsList){
                        p.spawnParticle(particlesType, loc, 1, dustOptions);
                    }
                }
            }
        }, 0L, 30L);

        //Loop a travers les joueurs et tout pour genre leur envoyer une action bar quand ils changent de type de chunk.
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                for(Player p : Bukkit.getOnlinePlayers()){
                    String bar;

                    if(Logics.isChunkClaimed(p.getChunk())){
                        if(Logics.isChunkClaimedWithPlayerTown(p, p.getChunk())){
                            bar = ChatColor.GREEN + "Home";
                        }else{
                            bar = ChatColor.RED + "Claimed: " + Logics.getTownByChunk(p.getChunk()).getTownName();
                        }
                    }else{
                        bar = ChatColor.GRAY + "Unclaimed";
                    }
                    if(lastActionBarState.containsKey(p)){
                        if(!lastActionBarState.get(p).equals(bar)){
                            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(bar));
                            lastActionBarState.remove(p);
                            lastActionBarState.put(p, bar);
                        }
                    }else{
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(bar));
                        lastActionBarState.put(p, bar);
                    }
                }
            }
        }, 0L, 10L);

        //Pour loop a travers les war chunks et tout pour la guerre
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                int winSeconds = 90;
                HashMap<String, String> map = Main.getChunksWars();
                for(String tName : map.keySet()){
                    TownCache attackers = Logics.getTownByName(tName);
                    TownCache defenders = Logics.getTownByName(map.get(tName));
                    Chunk warChunk = attackers.getWarChunk();

                    if(attackers.getSecondsInWarChunk() == winSeconds){
                        Logics.endWarProcessAsAttackers(attackers, warChunk);
                    }else if(defenders.getSecondsInWarChunk() == winSeconds){
                        Logics.endWarProcessAsDefenders(defenders, attackers, warChunk);
                    }

                    if(Logics.townContainsPlayerInChunk(attackers, warChunk)){
                        if(Logics.townContainsPlayerInChunk(defenders, warChunk)){
                            attackers.setSecondsInWarChunk(attackers.getSecondsInWarChunk() + 1);
                        }
                    }else{
                        attackers.setSecondsInWarChunk(0);
                    }

                    if(Logics.townContainsPlayerInChunk(defenders, warChunk)){
                        if(!Logics.townContainsPlayerInChunk(attackers, warChunk)){
                            defenders.setSecondsInWarChunk(defenders.getSecondsInWarChunk() + 1);
                        }
                    }else{
                        defenders.setSecondsInWarChunk(0);
                    }

                    for(UUID uuid : attackers.getResidents()){
                        Player p = Bukkit.getPlayer(uuid);
                        p.sendMessage(Prefixes.townBasicPrefix + "You town is now at " + (60 - attackers.getSecondsInWarChunk()) + " seconds away from winning this chunk!");
                        Chunk chunk = attackers.getWarChunk();
                        World world = chunk.getWorld();
                        int chunkX = chunk.getX();
                        int chunkZ = chunk.getZ();

                        int cornerX = chunkX * 16;
                        int cornerZ = chunkZ * 16;

                        List<Location> locationsList = new ArrayList<>();
                        for(int i = 0 ; i < 16 ; i++){
                            locationsList.add(new Location(world, cornerX + i, 40, cornerZ));
                            locationsList.add(new Location(world, cornerX + i, 40, cornerZ + 16));
                            locationsList.add(new Location(world, cornerX, 40, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + 16, 40, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + i, 50, cornerZ));
                            locationsList.add(new Location(world, cornerX + i, 50, cornerZ + 16));
                            locationsList.add(new Location(world, cornerX, 50, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + 16, 50, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + i, 60, cornerZ));
                            locationsList.add(new Location(world, cornerX + i, 60, cornerZ + 16));
                            locationsList.add(new Location(world, cornerX, 60, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + 16, 60, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + i, 70, cornerZ));
                            locationsList.add(new Location(world, cornerX + i, 70, cornerZ + 16));
                            locationsList.add(new Location(world, cornerX, 70, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + 16, 70, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + i, 80, cornerZ));
                            locationsList.add(new Location(world, cornerX + i, 80, cornerZ + 16));
                            locationsList.add(new Location(world, cornerX, 80, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + 16, 80, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + i, 90, cornerZ));
                            locationsList.add(new Location(world, cornerX + i, 90, cornerZ + 16));
                            locationsList.add(new Location(world, cornerX, 90, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + 16, 90, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + i, 100, cornerZ));
                            locationsList.add(new Location(world, cornerX + i, 100, cornerZ + 16));
                            locationsList.add(new Location(world, cornerX, 100, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + 16, 100, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + i, 120, cornerZ));
                            locationsList.add(new Location(world, cornerX + i, 120, cornerZ + 16));
                            locationsList.add(new Location(world, cornerX, 120, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + 16, 120, cornerZ + i));
                        }

                        Particle particlesType = Particle.REDSTONE;
                        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.ORANGE, 1.65F);

                        for(Location loc : locationsList){
                            p.spawnParticle(particlesType, loc, 1, dustOptions);
                        }
                    }
                    for(UUID uuid : defenders.getResidents()){
                        Player p = Bukkit.getPlayer(uuid);
                        p.sendMessage(Prefixes.townBasicPrefix + "You town is now at " + (60 - defenders.getSecondsInWarChunk()) + " seconds away from winning this chunk!");
                        Chunk chunk = attackers.getWarChunk();
                        World world = chunk.getWorld();
                        int chunkX = chunk.getX();
                        int chunkZ = chunk.getZ();

                        int cornerX = chunkX * 16;
                        int cornerZ = chunkZ * 16;

                        List<Location> locationsList = new ArrayList<>();
                        for(int i = 0 ; i < 16 ; i++){
                            locationsList.add(new Location(world, cornerX + i, 40, cornerZ));
                            locationsList.add(new Location(world, cornerX + i, 40, cornerZ + 16));
                            locationsList.add(new Location(world, cornerX, 40, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + 16, 40, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + i, 50, cornerZ));
                            locationsList.add(new Location(world, cornerX + i, 50, cornerZ + 16));
                            locationsList.add(new Location(world, cornerX, 50, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + 16, 50, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + i, 60, cornerZ));
                            locationsList.add(new Location(world, cornerX + i, 60, cornerZ + 16));
                            locationsList.add(new Location(world, cornerX, 60, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + 16, 60, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + i, 70, cornerZ));
                            locationsList.add(new Location(world, cornerX + i, 70, cornerZ + 16));
                            locationsList.add(new Location(world, cornerX, 70, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + 16, 70, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + i, 80, cornerZ));
                            locationsList.add(new Location(world, cornerX + i, 80, cornerZ + 16));
                            locationsList.add(new Location(world, cornerX, 80, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + 16, 80, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + i, 90, cornerZ));
                            locationsList.add(new Location(world, cornerX + i, 90, cornerZ + 16));
                            locationsList.add(new Location(world, cornerX, 90, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + 16, 90, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + i, 100, cornerZ));
                            locationsList.add(new Location(world, cornerX + i, 100, cornerZ + 16));
                            locationsList.add(new Location(world, cornerX, 100, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + 16, 100, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + i, 120, cornerZ));
                            locationsList.add(new Location(world, cornerX + i, 120, cornerZ + 16));
                            locationsList.add(new Location(world, cornerX, 120, cornerZ + i));
                            locationsList.add(new Location(world, cornerX + 16, 120, cornerZ + i));
                        }

                        Particle particlesType = Particle.REDSTONE;
                        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.ORANGE, 1.65F);

                        for(Location loc : locationsList){
                            p.spawnParticle(particlesType, loc, 1, dustOptions);
                        }
                    }
                }
            }
        }, 0L, 20L);
    }

    @Override
    public void onDisable() {
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
    public static HashMap<Player, String> getLastActionBarState() {
        return lastActionBarState;
    }
    public static void setLastActionBarState(HashMap<Player, String> lastActionBarState) {
        Main.lastActionBarState = lastActionBarState;
    }
    public static HashMap<String, String> getChunksWars() {
        return chunksWars;
    }
    public static void setChunksWars(HashMap<String, String> chunksWars) {
        Main.chunksWars = chunksWars;
    }
}
