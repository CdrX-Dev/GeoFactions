package me.cdrx.geofactions;

import me.cdrx.Main;
import me.cdrx.Prefixes;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Logics {
    public static void createTown(UUID uuid, String townName){
        Player p = Bukkit.getPlayer(uuid);
        Chunk chunk = p.getChunk();
        if(isChunkClaimed(chunk)){
            p.sendMessage(Prefixes.townBasicPrefix + "You can't claim this chunk because it is already owned by antoher town");
        }else{
            if(playerHasMoneyToCreateTown(p)){
                if(isTownNameTaken(townName)){
                    p.sendMessage(Prefixes.townBasicPrefix + "This town name is already taken! You can't create a town with this name.");
                }else{
                    UUID[] uuids = {};
                    UUID[] residents = {uuid};
                    Chunk[] chunks = {};

                    List<TownCache> list = Main.getTownCache();
                    list.add(new TownCache(townName , uuid, uuids, uuids, residents, chunks, chunks));
                    Main.setTownCache(list);

                    claimChunk(chunk, townName, p);
                    setChunkAsPrimary(chunk, townName);
                    p.sendMessage(Prefixes.townBasicPrefix + "You successfully created you town " + ChatColor.BLUE + townName + " and claimed this chunk!");
                    p.sendMessage(Prefixes.townBasicPrefix + "You can now do '/geofactions' to open your owner menu!");
                }
            }else{
                p.sendMessage(Prefixes.townBasicPrefix + "You do not have enough ressource to create your town! You need 10 iron ingots and 10 gold ingots.");
            }
        }
    }

    private static boolean isTownNameTaken(String townName) {
        for(TownCache tc : Main.getTownCache()){
            if(tc.getTownName().equals(townName)){
                return true;
            }
        }
        return false;
    }

    public static boolean isChunkClaimed(Chunk chunk) {
        for(TownCache townCache : Main.getTownCache()){
            for(Chunk chunk1 : townCache.getClaimedChunks()){
                if(chunk.equals(chunk1)) return true;
            }
        }
        return false;
    }

    public static void setChunkAsPrimary(Chunk chunk, String townName) {
        try {
            final Connection connection = Main.getDatabaseManager().getInfoConnection().getConnection();
            PreparedStatement ps = connection.prepareStatement("INSERT INTO primary_chunks VALUES(?, ?, ?, ?)");
            ps.setInt(1, chunk.getX());
            ps.setInt(2, chunk.getZ());
            ps.setString(3, chunk.getWorld().getName());
            ps.setString(4, townName);
            ps.executeUpdate();

            //Check pour la bonne classe qui représente la ville.
            TownCache townCache = null;
            for(TownCache tc : Main.getTownCache()){
                if(tc.getTownName().equals(townName)) townCache = tc;
            }
            
            Chunk[] listPrimaryChunks = {};

            //Loop a travers les resultats de retour
            ps = connection.prepareStatement("SELECT * FROM primary_chunks");
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                String tName = rs.getString("town");
                if(tName.equals(townName)){
                    World world = Bukkit.getWorld(rs.getString("world"));
                    int x = rs.getInt("x");
                    int z = rs.getInt("z");
                    listPrimaryChunks = Arrays.copyOf(listPrimaryChunks, listPrimaryChunks.length + 1);
                    listPrimaryChunks[listPrimaryChunks.length - 1] = world.getChunkAt(x,z);
                }
            }
            //Tu set le primarys chunk de la ville
            townCache.setPrimaryChunks(listPrimaryChunks);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void claimChunk(Chunk chunk, String townName, Player player){
        if(!isChunkClaimed(chunk)){
            if(playerCanPayChunk(player)) {
                try {
                    final Connection connection = Main.getDatabaseManager().getInfoConnection().getConnection();
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO towns_claims VALUES(?, ?, ?, ?)");
                    ps.setInt(1, chunk.getX());
                    ps.setInt(2, chunk.getZ());
                    ps.setString(3, chunk.getWorld().getName());
                    ps.setString(4, townName);
                    ps.executeUpdate();

                    for (TownCache tc : Main.getTownCache()) {
                        if (tc.getTownName().equals(townName)) {
                            Chunk[] chunkList = tc.getClaimedChunks();
                            chunkList = Arrays.copyOf(chunkList, chunkList.length + 1);
                            chunkList[chunkList.length - 1] = chunk;
                            tc.setClaimedChunks(chunkList);
                            break;
                        }
                    }

                    player.sendMessage(Prefixes.townBasicPrefix + "You successfully claimed this chunk!");
                } catch (Exception e) {
                    player.sendMessage(Prefixes.townBasicPrefix + "Something went wrong while trying to claim this chunk! Try again later.");
                    e.printStackTrace();
                }
            }
        }else{
            player.sendMessage(Prefixes.townBasicPrefix + "This chunk is already claimed by a town!");
        }
    }

    private static boolean playerCanPayChunk(Player player) {
        Inventory inv = player.getInventory();
        ItemStack stack1 = new ItemStack(Material.GOLD_INGOT, processPrice());
        ItemStack stack2 = new ItemStack(Material.IRON_INGOT, processPrice());
        if(inv.containsAtLeast(stack1,1) && inv.containsAtLeast(stack2, 1)){
            inv.remove(stack1);
            inv.remove(stack1);
            return true;
        }else{
            player.sendMessage(Prefixes.townBasicPrefix + "In order to claim a chunk you need to have " + processPrice() + " gold and iron ingots on you!");
            return false;
        }
    }

    public static void invitePlayerInTown(Player sender, Player player, String townName){
        if(!isPlayerResidentOfATown(player)){
            net.md_5.bungee.api.chat.TextComponent acceptRequest = new TextComponent("[YES]");
            acceptRequest.setColor(net.md_5.bungee.api.ChatColor.GREEN);
            acceptRequest.setBold(true);
            acceptRequest.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Join " + townName + "?").color(net.md_5.bungee.api.ChatColor.BLUE).italic(true).create()));
            acceptRequest.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/geofactions inviteAccept " + player.getUniqueId() + " " + townName));

            net.md_5.bungee.api.chat.TextComponent denyRequest = new TextComponent("[NO]");
            denyRequest.setColor(net.md_5.bungee.api.ChatColor.RED);
            denyRequest.setBold(true);
            denyRequest.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Deny " + townName + " invitation?").color(net.md_5.bungee.api.ChatColor.BLUE).italic(true).create()));
            denyRequest.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/geofactions inviteDeny " + player.getUniqueId() + " " + sender.getUniqueId()));

            player.sendMessage(Prefixes.townBasicPrefix + "Would you want to join " + townName + "? (Request sent by " + sender.getName() + ")");
            player.spigot().sendMessage(acceptRequest);
            player.spigot().sendMessage(denyRequest);
        }else{
            sender.sendMessage(Prefixes.townBasicPrefix + "Can't invite the player " + player.getName() + " because he is already resident of a town!");
        }
    }

    public static boolean isPlayerResidentOfATown(Player player) {
        for(TownCache tc : Main.getTownCache()){
            for(UUID uuid : tc.getResidents()){
                if(player.getUniqueId().equals(uuid)) return true;
            }
        }
        return false;
    }

    public static void addPlayerAsResident(Player player, String townName){
        try {
            final Connection connection = Main.getDatabaseManager().getInfoConnection().getConnection();
            PreparedStatement ps = connection.prepareStatement("INSERT INTO town_residents VALUES (?, ?)");
            ps.setString(1, townName);
            ps.setString(2, player.getUniqueId().toString());
            ps.executeUpdate();

            for(TownCache tc : Main.getTownCache()){
                if(tc.getTownName().equals(townName)){
                    UUID[] uuids = tc.getResidents();
                    uuids = Arrays.copyOf(uuids, uuids.length + 1);
                    uuids[uuids.length - 1] = player.getUniqueId();
                    tc.setResidents(uuids);
                    townAnnoucement(townName, Prefixes.townBasicPrefix + player.getName() + " just joined the town!");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void townAnnoucement(String townName, String annoucement) {
        for(TownCache tc : Main.getTownCache()){
            if(tc.getTownName().equals(townName)){
                for(UUID uuid : tc.getResidents()){
                    Player p = Bukkit.getPlayer(uuid);
                    p.sendMessage(annoucement);
                }
            }
        }
    }

    public static void townRename(String townName){
        //TODO: Remplacer dans le residents, chunks et chunks principaux le nom de la ville (Cache et SQL Database)
    }

    public static void deleteTown(String townName){
        try{
            final Connection connection = Main.getDatabaseManager().getInfoConnection().getConnection();
            final PreparedStatement ps = connection.prepareStatement("DELETE FROM towns_claims WHERE town=?");
            ps.setString(1, townName);
            ps.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static int processPrice(){
        double claimedLand = processClaimedLandPercent(3000, 3000) * 100;
        double fonction = (Math.pow(claimedLand, 75/100) + 10);

        return (int) Math.ceil(fonction);
    }

    public static double processClaimedLandPercent(int mapX, int mapY){
        double totalChunksCount = (mapX * mapY)/(16*16);
        double totalChunksClaimed = processClaimedChunk();

        return totalChunksClaimed / totalChunksCount;
    }

    private static double processClaimedChunk() {
        List<Chunk> chunkList = new ArrayList<>();
        for(TownCache tc : Main.getTownCache()){
            chunkList.addAll(Arrays.asList(tc.getClaimedChunks()));
        }
        return chunkList.size();
    }

    private static boolean playerHasMoneyToCreateTown(Player p) {
        Inventory inv = p.getInventory();
        return inv.contains(Material.IRON_INGOT, 10) && inv.contains(Material.GOLD_INGOT, 10);
    }

    public static void initializeCache() {
        Bukkit.getServer().getLogger().info(ChatColor.GREEN + "Starting cache initialization!");
        long startTime = Calendar.getInstance().getTimeInMillis();
        try{
            final Connection connection = Main.getDatabaseManager().getInfoConnection().getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM towns_list");
            ResultSet rs = ps.executeQuery();

            //Retreive all towns names from database
            List<String> townsNames = new ArrayList<>();
            while(rs.next()){
                townsNames.add(rs.getString("town"));
            }

            //La liste temporaire de TownCache classes que je vais push a la fin de la procedure
            List<TownCache> townCache = Main.getTownCache();
            for(String townName : townsNames){
                townCache.add(new TownCache(townName, null, null, null, null, null, null));
            }

            //Retreive owners from Database
            ps = connection.prepareStatement("SELECT * FROM towns_owner");
            ResultSet rs1 = ps.executeQuery();
            //TODO: add to class
            HashMap<String, UUID> ownersMap = new HashMap<>();
            while(rs1.next()){
                ownersMap.put(rs1.getString("town"), UUID.fromString(rs1.getString("owner_uuid")));
            }

            ps = connection.prepareStatement("SELECT * FROM town_admins");
            ResultSet rs2 = ps.executeQuery();
            //TODO: add to class
            HashMap<String, UUID[]> adminsMap = new HashMap<>();
            while(rs2.next()){
                UUID[] tempUUIDList = getUUIDListFromString(rs2.getString("admins_uuid"));
                adminsMap.put(rs2.getString("town"), tempUUIDList);
            }

            ps = connection.prepareStatement("SELECT * FROM town_treasurers");
            ResultSet rs3 = ps.executeQuery();
            //TODO: add to class
            HashMap<String, UUID[]> treasurersMap = new HashMap<>();
            while(rs3.next()){
                UUID[] tempTreasurersList = getUUIDListFromString(rs3.getString("treasurers_uuid"));
                treasurersMap.put(rs3.getString("town"), tempTreasurersList);
            }

            ps = connection.prepareStatement("SELECT * FROM town_treasurers");
            ResultSet rs4 = ps.executeQuery();
            //TODO: add to class
            HashMap<String, UUID[]> residentsMap = new HashMap<>();
            while(rs4.next()){
                UUID[] tempResidentsList = getUUIDListFromString(rs4.getString("residents_uuid"));
                residentsMap.put(rs4.getString("town"), tempResidentsList);
            }

            ps = connection.prepareStatement("SELECT * FROM towns_claims");
            ResultSet rs5 = ps.executeQuery();
            HashMap<String, Chunk[]> claimsMap = new HashMap<>();
            while(rs5.next()){
                String currentTown = rs5.getString("town");
                int x = rs5.getInt("x");
                int z = rs5.getInt("z");
                World world = Bukkit.getWorld(rs5.getString("world"));

                if(!claimsMap.containsKey(currentTown)){
                    Chunk[] tempArray = {world.getChunkAt(x,z)};
                    claimsMap.put(currentTown, tempArray);
                }else{
                    Chunk[] tempArray = claimsMap.get(currentTown);
                    claimsMap.remove(currentTown);
                    tempArray = Arrays.copyOf(tempArray, tempArray.length + 1);
                    tempArray[tempArray.length - 1] = world.getChunkAt(x,z);
                    claimsMap.put(currentTown, tempArray);
                }
            }

            ps = connection.prepareStatement("SELECT * FROM primary_chunks");
            ResultSet rs6 = ps.executeQuery();
            HashMap<String, Chunk[]> primaryClaimsMap = new HashMap<>();
            while(rs5.next()){
                String currentTown = rs5.getString("town");
                int x = rs5.getInt("x");
                int z = rs5.getInt("z");
                World world = Bukkit.getWorld(rs5.getString("world"));

                if(!primaryClaimsMap.containsKey(currentTown)){
                    Chunk[] tempArray = {world.getChunkAt(x,z)};
                    primaryClaimsMap.put(currentTown, tempArray);
                }else{
                    Chunk[] tempArray = primaryClaimsMap.get(currentTown);
                    primaryClaimsMap.remove(currentTown);
                    tempArray = Arrays.copyOf(tempArray, tempArray.length + 1);
                    tempArray[tempArray.length - 1] = world.getChunkAt(x,z);
                    primaryClaimsMap.put(currentTown, tempArray);
                }
            }

            for(TownCache townCache1 : townCache){
                String townName = townCache1.getTownName();
                townCache1.setOwner(ownersMap.get(townName));
                townCache1.setAdmins(adminsMap.get(townName));
                townCache1.setTreasurers(treasurersMap.get(townName));
                townCache1.setResidents(residentsMap.get(townName));
                townCache1.setClaimedChunks(claimsMap.get(townName));
                townCache1.setPrimaryChunks(primaryClaimsMap.get(townName));
            }

        }catch (SQLException e){
            e.printStackTrace();
            Bukkit.getLogger().info(Prefixes.severeError + "Something herribly wrong happened while loading database info in cache on server!");
        }
        Bukkit.getServer().getLogger().info(ChatColor.GREEN + "Finished cache initialization! This task took " + (Calendar.getInstance().getTimeInMillis() - startTime) + "ms!");
    }

    private static UUID[] getUUIDListFromString(String admins_uuid) {
        String[] StringUUIDList = admins_uuid.split(";");
        UUID[] TempUUIDList = {};
        for(String str : StringUUIDList){
            if(!str.isEmpty()){
                TempUUIDList = Arrays.copyOf(TempUUIDList, TempUUIDList.length + 1);
                TempUUIDList[TempUUIDList.length - 1] = UUID.fromString(str);
            }
        }
        return TempUUIDList;
    }

    public static String getPlayerRole(UUID uuid1){
        List<TownCache> list = Main.getTownCache();
        for(TownCache cache : list){
            for(UUID uuid : cache.getResidents()){
                if(uuid1.equals(uuid)){
                    if(uuid1.equals(cache.getOwner())) return "owner";
                    for(UUID uuid2 : cache.getAdmins()){
                        if(uuid2.equals(uuid1)); return "admin";
                    }
                    for(UUID uuid2 : cache.getTreasurers()){
                        if(uuid2.equals(uuid1)); return "treasurer";
                    }
                    return "resident";
                }
            }
        }
        return null;
    }

    public static boolean isChunkClaimedWithPlayerTown(Player p, Chunk chunk) {
        for(TownCache tc : Main.getTownCache()){
            for(UUID uuid1 : tc.getResidents()){
                if(p.getUniqueId().equals(uuid1)){
                    for(Chunk c1 : tc.getClaimedChunks()){
                        if(chunk.equals(c1)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static void closeCacheProcedure() {
        long start = Calendar.getInstance().getTimeInMillis();
        try{
            for(TownCache tc : Main.getTownCache()){
                //TODO: Faire le reste de la procedure.
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        Bukkit.getServer().getLogger().info("Shutdown of cache has been done in " + (Calendar.getInstance().getTimeInMillis() - start) + "ms!");
    }

    public static boolean doesPlayerExist(String playerName){
        for(Player p : Bukkit.getOnlinePlayers()){
            if(p.getName().equals(playerName)) return true;
        }
        return false;
    }
}
