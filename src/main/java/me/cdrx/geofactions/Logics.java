package me.cdrx.geofactions;

import me.cdrx.Main;
import me.cdrx.Prefixes;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Logics {
    public static void createTown(UUID uuid, String townName){
        Player p = Bukkit.getPlayer(uuid);
        Chunk chunk = p.getChunk();
        if(!isChunkClaimed(chunk)){
            p.sendMessage(Prefixes.townBasicPrefix + "You can't claim this chunk because it is already owned by antoher town");
        }else{
            if(playerHasMoneyToCreateTown(p)){
                claimChunk(chunk, townName);
                setChunkAsPrimary(chunk, townName);
                p.sendMessage(Prefixes.townBasicPrefix + "You successfully created you town " + ChatColor.BLUE + townName + " and claimed this chunk!");
                p.sendMessage(Prefixes.townBasicPrefix + "You can now do '/geofactions' to open your owner menu!");
            }else{
                p.sendMessage(Prefixes.townBasicPrefix + "You do not have enough ressource to create your town! You need 10 iron ingots and 10 gold ingots.");
            }
        }
        //TODO: Procedure pour créer la ville.
    }

    public static boolean isChunkClaimed(Chunk chunk) {
        for(TownCache townCache : Main.getTownCache()){
            for(Chunk chunk1 : townCache.getClaimedChunks()){
                if(chunk.equals(chunk1)) return true;
            }
        }
        return false;
    }

    public static boolean isChunkClaimedByTown(Chunk chunk, String townName){
        TownCache townCache = null;
        for(TownCache tc : Main.getTownCache()){
            if(tc.getTownName().equals(townName)) townCache = tc;
        }

        for(Chunk chunk1 : townCache.getClaimedChunks()){
            if(chunk.equals(chunk1)) return true;
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

    public static void claimChunk(Chunk chunk, String townName){
        //TODO: Faire la procedure pour claim un chunk!
        //TODO: Trouver une manière de faire claim un chunk!
    }

    public static void addPlayerAsResident(Player player, String townName){
        //TODO: Faire la procedure pour ajouter le joueur dans le résidents.
        //TODO: Faire un systeme d'invite que les admins et owner d'une town peuvent inviter des gens dans leur ville et ca demande une confirmation au gars pour la join.
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
        resetTownsClaimsCache();
    }

    public static int processPrice(){
        //X dans la fonction (en pourcentage)
        double claimedLand = processClaimedLandPercent(3000, 3000) * 100;

        //Fonction
        double fonction = (Math.pow(claimedLand, 75/100) + 10);

        //Valeur de retour
        return (int) Math.ceil(fonction);
    }

    public static double processClaimedLandPercent(int mapX, int mapY){
        double totalChunksCount = (mapX * mapY)/(16*16);
        double totalChunksClaimed = Main.getTownsClaims().size();

        return totalChunksClaimed / totalChunksCount;
    }

    private static void resetTownsClaimsCache() {
        HashMap<Chunk, String> map = Main.getTownsClaims();
        map.clear();
        try{
            final Connection connection = Main.getDatabaseManager().getInfoConnection().getConnection();
            final PreparedStatement ps = connection.prepareStatement("SELECT * FROM towns_claims");
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                String town = rs.getString("town");
                String world = rs.getString("world");
                int x = rs.getInt("x");
                int z = rs.getInt("z");
                World w = Bukkit.getWorld(world);
                Chunk chunk = w.getChunkAt(x, z);
                map.put(chunk, town);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        Main.setTownsClaims(map);

    }

    public static boolean isTownOfPlayer(Player player, String townName) {
        HashMap<UUID, String> map = Main.getResidentsTown();
        if(map.containsKey(player.getUniqueId())){
            return map.get(player.getUniqueId()).equals(townName);
        }else{
            return false;
        }
    }

    private static boolean playerHasMoneyToCreateTown(Player p) {
        Inventory inv = p.getInventory();
        return inv.contains(Material.IRON_INGOT, 10) && inv.contains(Material.GOLD_INGOT, 10);
    }

    public static void initializeCache() {
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

            Bukkit.getServer().getLogger().info();

        }catch (SQLException e){
            e.printStackTrace();
            Bukkit.getServer().getLogger().info(Prefixes.severeError + "Something herribly wrong happened while loading database info in cache on server!");
        }
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
                    if(uuid1.equals(cache.getOwner()); return "owner";
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
    }
}
