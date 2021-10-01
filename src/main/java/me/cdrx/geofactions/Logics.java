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
                    list.add(new TownCache(townName , uuid, uuids, uuids, residents, chunks, chunks, getBankFromSQL(townName)));
                    Main.setTownCache(list);

                    try{
                        final Connection connection = Main.getDatabaseManager().getInfoConnection().getConnection();
                        PreparedStatement ps = connection.prepareStatement("INSERT INTO towns_list VALUES(?)");
                        ps.setString(1, townName);
                        ps.executeUpdate();

                        ps = connection.prepareStatement("INSERT INTO towns_owner VALUES(?, ?)");
                        ps.setString(1, townName);
                        ps.setString(2, uuid.toString());
                        ps.executeUpdate();

                        ps = connection.prepareStatement("INSERT INTO town_residents VALUES(?, ?)");
                        ps.setString(1, townName);
                        ps.setString(2, uuid.toString());
                        ps.executeUpdate();

                        ps = connection.prepareStatement("INSERT INTO towns_banks VALUES(?, ?, ?, ?)");
                        ps.setString(1, townName);
                        ps.setString(2, "empty");
                        ps.setString(3, "empty");
                        ps.setString(4, "empty");
                        ps.executeUpdate();
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    claimChunk(chunk, townName, p);
                    setChunkAsPrimary(p ,chunk, townName);
                    p.sendMessage(Prefixes.townBasicPrefix + "You successfully created you town " + ChatColor.BLUE + townName + " and claimed this chunk!");
                    p.sendMessage(Prefixes.townBasicPrefix + "You can now do '/geofactions' to open your owner menu!");
                }
            }else{
                p.sendMessage(Prefixes.townBasicPrefix + "You do not have enough ressource to create your town! You need 10 iron ingots and 10 gold ingots.");
            }
        }
    }

    public static List<ItemStack> getBankFromSQL(String townName){
        List<ItemStack> inventory = new ArrayList<>();

        try{
            final Connection connection = Main.getDatabaseManager().getInfoConnection().getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM towns_banks WHERE town=?");
            ps.setString(1, townName);
            ResultSet rs = ps.executeQuery();

            String str = "empty";
            while(rs.next()){
                str = rs.getString("inventory");
                break;
            }

            if(str.equals("empty")) return inventory;

            List<String> strList = new ArrayList<>();
            strList.addAll(Arrays.asList(str.split(";")));
            for(String str1 : strList){
                String[] strList2 = str1.split(",");
                Bukkit.getLogger().info("working with database to bank substring " + Arrays.toString(strList2) + "!");
                inventory.add(new ItemStack(Material.getMaterial(strList2[0]), Integer.parseInt(strList2[1].trim())));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }

        return inventory;
    }

    public static void pushBankToSQL(String townName, List<ItemStack> bank){
        try{
            final Connection connection = Main.getDatabaseManager().getInfoConnection().getConnection();
            PreparedStatement ps = connection.prepareStatement("DELETE FROM towns_banks WHERE town=?");
            ps.setString(1, townName);
            ps.executeUpdate();

            ps = connection.prepareStatement("INSERT INTO towns_banks VALUES(?, ?, ?, ?)");
            ps.setString(1, townName);
            ps.setString(2, bankToString(bank));
            ps.setString(3, "empty");
            ps.setString(4, "empty");
            ps.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static boolean isChunkPrimary(Chunk chunk){
        for(TownCache tc : Main.getTownCache()){
            for(Chunk c : tc.getPrimaryChunks()){
                if(c.equals(chunk)) return true;
            }
        }
        return false;
    }

    public static String bankToString(List<ItemStack> bank) {
        String bankStr = "";
        if(bank.size() == 0) return "empty";
        for(ItemStack item : bank){
            if(item != null){
                bankStr = bankStr + item.getType() + "," + item.getAmount() + ";";
            }
        }
        if(bankStr.isEmpty()) return "empty";
        return bankStr;
    }

    public static boolean isTownNameTaken(String townName) {
        for(TownCache tc : Main.getTownCache()){
            if(tc.getTownName().equals(townName)){
                return true;
            }
        }
        return false;
    }

    public static void setTownAdmin(Player sender,UUID uuid, String townName){
        Player p = Bukkit.getPlayer(uuid);
        if(isPlayerResidentOfATown(Bukkit.getPlayer(uuid))){
            if(getTownOfPlayer(sender).equals(getTownOfPlayer(Bukkit.getPlayer(uuid)))){
                if(getPlayerRole(uuid).equals("admin")){
                    sender.sendMessage(Prefixes.townBasicPrefix + "This player is already an admin!");
                    return;
                }
                if(getPlayerRole(uuid).equals("treasurer")){
                    removeTownTreasurer(sender, uuid, townName);
                }
                UUID[] admins = getTownByName(townName).getAdmins();
                admins = Arrays.copyOf(admins, admins.length + 1);
                admins[admins.length - 1] = uuid;
                getTownByName(townName).setAdmins(admins);

                try{
                    final Connection connection = Main.getDatabaseManager().getInfoConnection().getConnection();
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO town_admins VALUES(?, ?)");
                    ps.setString(1, townName);
                    ps.setString(2, uuid.toString());
                    ps.executeUpdate();
                }catch (Exception e){
                    e.printStackTrace();
                }

                sender.sendMessage(Prefixes.townBasicPrefix + "Successfully setted this player as a town admin!");
            }else{
                sender.sendMessage(Prefixes.townBasicPrefix + "This player is not even in your town broo!");
            }
        }else{
            sender.sendMessage(Prefixes.townBasicPrefix + "This player is not even in your town broo!");
        }
    }

    public static void setTownTreasurer(Player sender, UUID uuid, String townName){
        if(isPlayerResidentOfATown(Bukkit.getPlayer(uuid))){
            if(getTownOfPlayer(sender).equals(getTownOfPlayer(Bukkit.getPlayer(uuid)))){
                if(getPlayerRole(uuid).equals("admin")){
                    sender.sendMessage(Prefixes.townBasicPrefix + "This player is already an admin!");
                    return;
                }
                if(getPlayerRole(uuid).equals("treasurer")){
                    sender.sendMessage(Prefixes.townBasicPrefix + "This player is already a town treasurer!");
                    return;
                }
                UUID[] treasurers = getTownByName(townName).getTreasurers();
                treasurers = Arrays.copyOf(treasurers, treasurers.length + 1);
                treasurers[treasurers.length - 1] = uuid;
                getTownByName(townName).setTreasurers(treasurers);

                try{
                    final Connection connection = Main.getDatabaseManager().getInfoConnection().getConnection();
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO town_treasurers VALUES(?, ?)");
                    ps.setString(1, townName);
                    ps.setString(2, uuid.toString());
                    ps.executeUpdate();
                }catch (Exception e){
                    e.printStackTrace();
                }

                sender.sendMessage(Prefixes.townBasicPrefix + "Successfully setted this player as a town treasurer!");
            }else{
                sender.sendMessage(Prefixes.townBasicPrefix + "This player is not even in your town broo!");
            }
        }else{
            sender.sendMessage(Prefixes.townBasicPrefix + "This player is not even in your town broo!");
        }
    }

    public static void removeTownAdmin(Player sender, UUID uuid, String townName){
        if(isPlayerResidentOfATown(Bukkit.getPlayer(uuid))){
            if(getTownOfPlayer(sender).equals(getTownOfPlayer(Bukkit.getPlayer(uuid)))){
                if(!getPlayerRole(uuid).equals("admin")){
                    sender.sendMessage(Prefixes.townBasicPrefix + "This player is not even a town admin!");
                    return;
                }
                List<UUID> adminsList = new LinkedList<>(Arrays.asList(getTownByName(getTownOfPlayer(sender)).getAdmins()));
                adminsList.remove(uuid);
                getTownByName(townName).setAdmins(adminsList.toArray(new UUID[adminsList.size()]));

                try{
                    final Connection connection = Main.getDatabaseManager().getInfoConnection().getConnection();
                    PreparedStatement ps = connection.prepareStatement("DELETE FROM town_admins WHERE town=? AND admins_uuid=?");
                    ps.setString(1, townName);
                    ps.setString(2, uuid.toString());
                    ps.executeUpdate();
                }catch (Exception e){
                    e.printStackTrace();
                }

                sender.sendMessage(Prefixes.townBasicPrefix + "Successfully removed this player from the admins!");
            }else{
                sender.sendMessage(Prefixes.townBasicPrefix + "This player is not even in your town broo!");
            }
        }else{
            sender.sendMessage(Prefixes.townBasicPrefix + "This player is not even in your town broo!");
        }
    }

    public static void removeTownTreasurer(Player sender, UUID uuid, String townName){
        if(isPlayerResidentOfATown(Bukkit.getPlayer(uuid))){
            if(getTownOfPlayer(sender).equals(getTownOfPlayer(Bukkit.getPlayer(uuid)))){
                if(!getPlayerRole(uuid).equals("treasurer")){
                    sender.sendMessage(Prefixes.townBasicPrefix + "This player is not even a town treasurer!");
                    return;
                }
                List<UUID> treasurersList = new LinkedList<>(Arrays.asList(getTownByName(getTownOfPlayer(sender)).getTreasurers()));
                treasurersList.remove(uuid);
                getTownByName(townName).setTreasurers(treasurersList.toArray(new UUID[treasurersList.size()]));

                try{
                    final Connection connection = Main.getDatabaseManager().getInfoConnection().getConnection();
                    PreparedStatement ps = connection.prepareStatement("DELETE FROM town_treasurers WHERE town=? AND treasurers_uuid=?");
                    ps.setString(1, townName);
                    ps.setString(2, uuid.toString());
                    ps.executeUpdate();
                }catch (Exception e){
                    e.printStackTrace();
                }

                sender.sendMessage(Prefixes.townBasicPrefix + "Successfully removed this player from the treasurers!");
            }else{
                sender.sendMessage(Prefixes.townBasicPrefix + "This player is not even in your town broo!");
            }
        }else{
            sender.sendMessage(Prefixes.townBasicPrefix + "This player is not even in your town broo!");
        }
    }

    public static TownCache getTownByName(String townName){
        for(TownCache tc : Main.getTownCache()){
            if(tc.getTownName().equals(townName)) return tc;
        }
        return null;
    }

    public static String getTownOfPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        for(TownCache tc : Main.getTownCache()){
            for(UUID uuid1 : tc.getResidents()){
                if(uuid.equals(uuid1)) return tc.getTownName();
            }
        }
        return null;
    }

    public static boolean isChunkClaimed(Chunk chunk) {
        for(TownCache townCache : Main.getTownCache()){
            for(Chunk chunk1 : townCache.getClaimedChunks()){
                if(chunk.equals(chunk1)) return true;
            }
        }
        return false;
    }

    public static void setChunkAsPrimary(Player sender, Chunk chunk, String townName) {
        TownCache town = Logics.getTownByName(townName);
        if(town.getPrimaryChunks().length != 6){
            try{
                final Connection connection = Main.getDatabaseManager().getInfoConnection().getConnection();
                PreparedStatement ps = connection.prepareStatement("INSERT INTO primary_chunks VALUES(?, ?, ?, ?)");
                ps.setInt(1, chunk.getX());
                ps.setInt(2, chunk.getZ());
                ps.setString(3, chunk.getWorld().getName());
                ps.setString(4, townName);
                ps.executeUpdate();

                List<Chunk> primaryList = new LinkedList<Chunk>(Arrays.asList(town.getPrimaryChunks()));
                primaryList.add(chunk);
                town.setPrimaryChunks(primaryList.toArray(new Chunk[primaryList.size()]));
                sender.sendMessage(Prefixes.townBasicPrefix + "Successfully added this chunk to your primary chunks!");
            }catch (SQLException ex){
                ex.printStackTrace();
            }
        }else{
            sender.sendMessage(Prefixes.townBasicPrefix + "You can't claim more than six primary chunks!");
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
                    player.sendMessage(Prefixes.townBasicPrefix + "Price is currently at : " + processPrice());
                } catch (Exception e) {
                    player.sendMessage(Prefixes.townBasicPrefix + "Something went wrong while trying to claim this chunk! Try again later.");
                    e.printStackTrace();
                }
            }
        }else{
            player.sendMessage(Prefixes.townBasicPrefix + "This chunk is already claimed by a town!");
        }
    }

    public static boolean playerCanPayChunk(Player player) {
        Inventory inv = player.getInventory();
        ItemStack stack1 = new ItemStack(Material.GOLD_INGOT, processPrice());
        ItemStack stack2 = new ItemStack(Material.IRON_INGOT, processPrice());
        if(inv.containsAtLeast(stack1,1) && inv.containsAtLeast(stack2, 1)){
            boolean gold = false;
            boolean iron = false;
            for(ItemStack itemStack : inv.getContents()){
                if(itemStack != null){
                    if(itemStack.getType().equals(Material.GOLD_INGOT)){
                        if(!gold){
                            itemStack.setAmount(itemStack.getAmount() - processPrice());
                            gold = true;
                        }
                    }

                    if(itemStack.getType().equals(Material.IRON_INGOT)){
                        if(!iron){
                            itemStack.setAmount(itemStack.getAmount() - processPrice());
                            iron = true;
                        }
                    }

                    if(iron && gold){
                        break;
                    }
                }
            }

            return true;
        }else{
            player.sendMessage(Prefixes.townBasicPrefix + "In order to claim a chunk you need to have " + processPrice() + " gold and iron ingots on you!");
            return false;
        }
    }

    public static void invitePlayerInTown(Player sender, Player player, String townName){
        HashMap<UUID, String> map = Main.getTypingPlayers();
        map.remove(sender.getUniqueId());
        Main.setTypingPlayers(map);

        if(getTownByName(getTownOfPlayer(sender.getPlayer())).getResidents().length == 28){
            sender.sendMessage(Prefixes.townBasicPrefix + "You can't invite this player, you already have reached your residents limit!");
            return;
        }

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

    public static void removeResidentFromTown(Player sender, Player player, TownCache town){
        String grade = Logics.getPlayerRole(player.getUniqueId());
        if(grade.equals("owner")){
            sender.sendMessage(Prefixes.townBasicPrefix + "You can't remove the owner from the town!");
        }else if(grade.equals("admin")){
            List<UUID> residentsList = new LinkedList<UUID>(Arrays.asList(town.getResidents()));
            List<UUID> adminsList = new LinkedList<UUID>(Arrays.asList(town.getAdmins()));
            residentsList.remove(player.getUniqueId());
            adminsList.remove(player.getUniqueId());
            town.setResidents(residentsList.toArray(new UUID[residentsList.size()]));
            town.setAdmins(adminsList.toArray(new UUID[adminsList.size()]));
            try{
                final Connection connection = Main.getDatabaseManager().getInfoConnection().getConnection();
                PreparedStatement ps = connection.prepareStatement("DELETE FROM town_admins WHERE admins_uuid=?");
                ps.setString(1, player.getUniqueId().toString());
                ps.executeUpdate();

                ps = connection.prepareStatement("DELETE FROM town_residents WHERE residents_uuid=?");
                ps.setString(1, player.getUniqueId().toString());
                ps.executeUpdate();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }else if(grade.equals("treasurer")){
            List<UUID> residentsList = new LinkedList<UUID>(Arrays.asList(town.getResidents()));
            List<UUID> treasurersList = new LinkedList<UUID>(Arrays.asList(town.getTreasurers()));
            residentsList.remove(player.getUniqueId());
            treasurersList.remove(player.getUniqueId());
            town.setResidents(residentsList.toArray(new UUID[residentsList.size()]));
            town.setTreasurers(treasurersList.toArray(new UUID[treasurersList.size()]));
            try{
                final Connection connection = Main.getDatabaseManager().getInfoConnection().getConnection();
                PreparedStatement ps = connection.prepareStatement("DELETE FROM town_treasurers WHERE treasurers_uuid=?");
                ps.setString(1, player.getUniqueId().toString());
                ps.executeUpdate();

                ps = connection.prepareStatement("DELETE FROM town_residents WHERE residents_uuid=?");
                ps.setString(1, player.getUniqueId().toString());
                ps.executeUpdate();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }else if(grade.equals("resident")){
            List<UUID> residentsList = new LinkedList<UUID>(Arrays.asList(town.getResidents()));
            residentsList.remove(player.getUniqueId());
            town.setResidents(residentsList.toArray(new UUID[residentsList.size()]));
            try{
                final Connection connection = Main.getDatabaseManager().getInfoConnection().getConnection();
                PreparedStatement ps = connection.prepareStatement("DELETE FROM town_residents WHERE residents_uuid=?");
                ps.setString(1, player.getUniqueId().toString());
                ps.executeUpdate();
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        sender.sendMessage(Prefixes.townBasicPrefix + "Successfully removed this player from the residents of this town!");
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

    public static void townRename(Player sender, String townName, String newName){
        if(!isTownNameTaken(newName)){
            try{
                final Connection connection = Main.getDatabaseManager().getInfoConnection().getConnection();
                PreparedStatement ps = connection.prepareStatement("UPDATE towns_list SET town=? WHERE town=?");
                ps.setString(1, newName);
                ps.setString(2, townName);
                ps.executeUpdate();

                ps = connection.prepareStatement("UPDATE towns_claims SET town=? WHERE town=?");
                ps.setString(1, newName);
                ps.setString(2, townName);
                ps.executeUpdate();

                ps = connection.prepareStatement("UPDATE primary_chunks SET town=? WHERE town=?");
                ps.setString(1, newName);
                ps.setString(2, townName);
                ps.executeUpdate();

                ps = connection.prepareStatement("UPDATE towns_owner SET town=? WHERE town=?");
                ps.setString(1, newName);
                ps.setString(2, townName);
                ps.executeUpdate();

                ps = connection.prepareStatement("UPDATE town_admins SET town=? WHERE town=?");
                ps.setString(1, newName);
                ps.setString(2, townName);
                ps.executeUpdate();

                ps = connection.prepareStatement("UPDATE town_residents SET town=? WHERE town=?");
                ps.setString(1, newName);
                ps.setString(2, townName);
                ps.executeUpdate();

                ps = connection.prepareStatement("UPDATE town_treasurers SET town=? WHERE town=?");
                ps.setString(1, newName);
                ps.setString(2, townName);
                ps.executeUpdate();

                for(TownCache tc : Main.getTownCache()){
                    if(tc.getTownName().equals(townName)) tc.setTownName(newName);
                }
                sender.sendMessage(Prefixes.townBasicPrefix + "The town rename was successfull!");
                townAnnoucement(newName, Prefixes.townAnnouncementPrefix + "Your town changed of name! It is now called " + newName + ".");
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            sender.sendMessage(Prefixes.townBasicPrefix + "This town name is already taken!");
        }
    }

    public static void deleteTown(String townName){
        try{
            townAnnoucement(townName, Prefixes.townBasicPrefix + "Your town is deleted!");

            final Connection connection = Main.getDatabaseManager().getInfoConnection().getConnection();
            PreparedStatement ps = connection.prepareStatement("DELETE FROM towns_list WHERE town=?");
            ps.setString(1, townName);
            ps.executeUpdate();

            ps = connection.prepareStatement("DELETE FROM towns_claims WHERE town=?");
            ps.setString(1, townName);
            ps.executeUpdate();

            ps = connection.prepareStatement("DELETE FROM primary_chunks WHERE town=?");
            ps.setString(1, townName);
            ps.executeUpdate();

            ps = connection.prepareStatement("DELETE FROM towns_owner WHERE town=?");
            ps.setString(1, townName);
            ps.executeUpdate();

            ps = connection.prepareStatement("DELETE FROM town_admins WHERE town=?");
            ps.setString(1, townName);
            ps.executeUpdate();

            ps = connection.prepareStatement("DELETE FROM town_treasurers WHERE town=?");
            ps.setString(1, townName);
            ps.executeUpdate();

            ps = connection.prepareStatement("DELETE FROM town_residents WHERE town=?");
            ps.setString(1, townName);
            ps.executeUpdate();

            List<TownCache> list = Main.getTownCache();
            for(Iterator<TownCache> iter = list.iterator() ; iter.hasNext();){
                TownCache tc = iter.next();
                if(tc.getTownName().equals(townName)){
                    iter.remove();
                    break;
                }
            }
            Main.setTownCache(list);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static int processPrice(){
        double claimedLand = processClaimedLandPercent(3000, 3000) * 100;
        double fonction = (Math.pow(claimedLand, 110.0/100.0) + 10);

        return (int) Math.ceil(fonction);
    }

    public static double processClaimedLandPercent(int mapX, int mapY){
        double totalChunksCount = (mapX * mapY)/(16*16);
        double totalChunksClaimed = processClaimedChunk();

        return totalChunksClaimed / totalChunksCount;
    }

    public static double processClaimedChunk() {
        List<Chunk> chunkList = new ArrayList<>();
        for(TownCache tc : Main.getTownCache()){
            chunkList.addAll(Arrays.asList(tc.getClaimedChunks()));
        }
        return chunkList.size();
    }

    public static boolean playerHasMoneyToCreateTown(Player p) {
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

            List<TownCache> townsCache = Main.getTownCache();
            while(rs.next()){
                String townName = rs.getString("town");
                PreparedStatement prepstatement = connection.prepareStatement("SELECT * FROM towns_owner WHERE town=?");
                prepstatement.setString(1, townName);
                ResultSet resSet = prepstatement.executeQuery();

                UUID townOwner = null;
                while (resSet.next()){
                     townOwner = UUID.fromString(resSet.getString("owner_uuid"));
                }

                prepstatement = connection.prepareStatement("SELECT * FROM town_admins WHERE town=?");
                prepstatement.setString(1, townName);
                resSet = prepstatement.executeQuery();

                List<UUID> townAdmins = new ArrayList<>();
                while (resSet.next()){
                    townAdmins.add(UUID.fromString(resSet.getString("admins_uuid")));
                }

                prepstatement = connection.prepareStatement("SELECT * FROM town_treasurers WHERE town=?");
                prepstatement.setString(1, townName);
                resSet = prepstatement.executeQuery();

                List<UUID> townTreasurers = new ArrayList<>();
                while (resSet.next()){
                    townTreasurers.add(UUID.fromString(resSet.getString("treasurers_uuid")));
                }

                prepstatement = connection.prepareStatement("SELECT * FROM town_residents WHERE town=?");
                prepstatement.setString(1, townName);
                resSet = prepstatement.executeQuery();

                List<UUID> townResidents = new ArrayList<>();
                while (resSet.next()){
                    townResidents.add(UUID.fromString(resSet.getString("residents_uuid")));
                }

                prepstatement = connection.prepareStatement("SELECT * FROM towns_claims WHERE town=?");
                prepstatement.setString(1, townName);
                resSet = prepstatement.executeQuery();

                List<Chunk> townClaims = new ArrayList<>();
                while(resSet.next()){
                    townClaims.add(Bukkit.getWorld(resSet.getString("world")).getChunkAt(resSet.getInt("x"), resSet.getInt("z")));
                }

                prepstatement = connection.prepareStatement("SELECT * FROM primary_chunks WHERE town=?");
                prepstatement.setString(1, townName);
                resSet = prepstatement.executeQuery();

                List<Chunk> townPrimary = new ArrayList<>();
                while(resSet.next()){
                    townPrimary.add(Bukkit.getWorld(resSet.getString("world")).getChunkAt(resSet.getInt("x"), resSet.getInt("z")));
                }

                UUID[] townAdmin = new UUID[townAdmins.size()];
                UUID[] townTreasurer = new UUID[townTreasurers.size()];
                UUID[] townRes = new UUID[townResidents.size()];
                Chunk[] townClaim = new Chunk[townClaims.size()];
                Chunk[] primeChunk = new Chunk[townPrimary.size()];

                townAdmin = townAdmins.toArray(townAdmin);
                townTreasurer = townTreasurers.toArray(townTreasurer);
                townRes = townResidents.toArray(townRes);
                townClaim = townClaims.toArray(townClaim);
                primeChunk = townPrimary.toArray(primeChunk);
                townsCache.add(new TownCache(townName, townOwner, townAdmin, townTreasurer, townRes, townClaim, primeChunk, getBankFromSQL(townName)));
            }

            Main.setTownCache(townsCache);
        }catch (SQLException e){
            e.printStackTrace();
            Bukkit.getLogger().info(Prefixes.severeError + "Something herribly wrong happened while loading database info in cache on server!");
        }
        Bukkit.getServer().getLogger().info(ChatColor.GREEN + "Finished cache initialization! This task took " + (Calendar.getInstance().getTimeInMillis() - startTime) + "ms!");
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

    public static boolean doesPlayerExist(String playerName){
        for(Player p : Bukkit.getOnlinePlayers()){
            if(p.getName().equals(playerName)) return true;
        }
        return false;
    }

    public static TownCache getTownByChunk(Chunk chunk) {
        for(TownCache tc : Main.getTownCache()){
            if(Arrays.stream(tc.getClaimedChunks()).toList().contains(chunk)) return tc;
        }
        return null;
    }

    public static void unclaimChunk(Player sender, Chunk chunk){
        TownCache town = Logics.getTownByChunk(chunk);
        List<Chunk> chunks = new LinkedList<Chunk>(Arrays.asList(town.getClaimedChunks()));
        chunks.remove(chunk);
        town.setClaimedChunks(chunks.toArray(new Chunk[chunks.size()]));
        try{
            final Connection connection = Main.getDatabaseManager().getInfoConnection().getConnection();
            PreparedStatement ps = connection.prepareStatement("DELETE FROM towns_claims WHERE x=? AND z=? AND world=? AND town=?");
            ps.setInt(1, chunk.getX());
            ps.setInt(2, chunk.getZ());
            ps.setString(3, chunk.getWorld().getName());
            ps.setString(4, town.getTownName());
            ps.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
        sender.sendMessage(Prefixes.townBasicPrefix + "Successfully unclaimed this chunk!");
    }

    public static void removePrimaryChunk(Player sender ,Chunk chunk){
        TownCache town = Logics.getTownByChunk(chunk);
        if(Logics.isChunkPrimary(chunk)){
            List<Chunk> primaryChunks = new LinkedList<Chunk>(Arrays.asList(town.getPrimaryChunks()));
            primaryChunks.remove(chunk);
            town.setPrimaryChunks(primaryChunks.toArray(new Chunk[primaryChunks.size()]));
            try{
                final Connection connection = Main.getDatabaseManager().getInfoConnection().getConnection();
                PreparedStatement ps = connection.prepareStatement("DELETE FROM primary_chunks WHERE x=? AND z=? AND world=? AND town=?");
                ps.setInt(1, chunk.getX());
                ps.setInt(2, chunk.getZ());
                ps.setString(3, chunk.getWorld().getName());
                ps.setString(4, town.getTownName());
                ps.executeUpdate();
            }catch (SQLException e){
                e.printStackTrace();
            }
            sender.sendMessage(Prefixes.townBasicPrefix + "Succesfully removed chunk from primary chunks!");
        }else{
            sender.sendMessage(Prefixes.townBasicPrefix + "This chunk is not a primary chunk!");
        }
    }

    public static void warBeginProcedure(Player sender, Chunk chunk) {
        TownCache targetTown = Logics.getTownByChunk(chunk);
        List<Player> onlinePlayer = new ArrayList<>();
        for(UUID uuid : targetTown.getResidents()){
            Player p = Bukkit.getPlayer(uuid);
            if(p.isOnline()){
                onlinePlayer.add(p);
            }
        }
        if(onlinePlayer.size() >= 1){
            if (!Arrays.stream(Logics.getTownByChunk(chunk).getPrimaryChunks()).toList().contains(chunk)){
                if(playerCanPayWarFee(sender)){
                    //TODO: Faire les procedures pour le temps rester dans un chunk pis toute genre pis genre le win ou le perdre.
                    TownCache townOfSender = Logics.getTownByName(getTownOfPlayer(sender));
                    townOfSender.setWarChunk(chunk);

                    townAnnoucement(townOfSender.getTownName(), Prefixes.townBasicPrefix + "You are now in war with the town " + targetTown.getTownName() + " for the chunk at (" + chunk.getX() + ";" + chunk.getZ() + ")!");
                }else {
                    sender.sendMessage(Prefixes.townBasicPrefix + "You don't have enough ressource to start a war! You need 16 diamonds and " + processPrice() + " of gold/iron ingots.");
                }
            }else{
                sender.sendMessage(Prefixes.townBasicPrefix + "This is a primary chunk, you can't start a war!");
            }
        }else{
            sender.sendMessage(Prefixes.townBasicPrefix + "The opposite town don't have enough online players to start a war. (Need at least 2 players online!)");
        }
    }

    public static boolean playerCanPayWarFee(Player p) {
        int amountOfDiamondsRequired = 16;

        Inventory inv = p.getInventory();

        ItemStack item1 = new ItemStack(Material.GOLD_INGOT, processPrice());
        ItemStack item2 = new ItemStack(Material.IRON_INGOT, processPrice());
        ItemStack item3 = new ItemStack(Material.DIAMOND, amountOfDiamondsRequired);

        if(inv.containsAtLeast(item1, processPrice()) && inv.containsAtLeast(item2, processPrice()) && inv.containsAtLeast(item3, amountOfDiamondsRequired)){
            boolean gold = false;
            boolean iron = false;
            boolean diamond = false;
            for(ItemStack itemStack : inv.getContents()) {
                if (itemStack.getType().equals(Material.GOLD_INGOT)) {
                    if (!gold) {
                        itemStack.setAmount(itemStack.getAmount() - processPrice());
                        gold = true;
                    }
                }

                if (itemStack.getType().equals(Material.IRON_INGOT)) {
                    if (!iron) {
                        itemStack.setAmount(itemStack.getAmount() - processPrice());
                        iron = true;
                    }
                }

                if (itemStack.getType().equals(Material.DIAMOND)) {
                    if (!diamond) {
                        itemStack.setAmount(itemStack.getAmount() - amountOfDiamondsRequired);
                        diamond = true;
                    }
                }
                if (iron && gold && diamond) {
                    return true;
                }
            }
            return false;
        }else{
            return false;
        }
    }

    public static boolean townContainsPlayerInChunk(TownCache town, Chunk chunk) {
        for(UUID uuid : town.getResidents()){
            Player p = Bukkit.getPlayer(uuid);
            if(p.getChunk().equals(chunk)) return true;
        }
        return false;
    }

    //Quand les attaquants win.
    public static void endWarProcessAsAttackers(TownCache attackers, Chunk warChunk) {
        //Faire la procedure pour donner le chunk a la ville et l'enlever du cache.
        attackers.setSecondsInWarChunk(0);
        attackers.setWarChunk(null);

        try{
            final Connection connection = Main.getDatabaseManager().getInfoConnection().getConnection();
            PreparedStatement ps = connection.prepareStatement("DELETE FROM towns_claims WHERE x=? AND y=? AND world=?");
            ps.setInt(1, warChunk.getX());
            ps.setInt(2, warChunk.getZ());
            ps.setString(3, warChunk.getWorld().getName());
            ps.executeUpdate();

            ps = connection.prepareStatement("INSERT INTO towns_claims VALUES(?, ?, ?, ?)");
            ps.setInt(1, warChunk.getX());
            ps.setInt(2, warChunk.getZ());
            ps.setString(3, warChunk.getWorld().getName());
            ps.setString(4, attackers.getTownName());
            ps.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }

        HashMap<String, String> hashMap = Main.getChunksWars();

        TownCache defenders = Logics.getTownByName(hashMap.get(attackers.getTownName()));
        townAnnoucement(defenders.getTownName(), Prefixes.townBasicPrefix + "Your town losed the current chunk war. What a shame!");
        List<Chunk> defendersChunks = Arrays.asList(defenders.getClaimedChunks());
        defendersChunks.remove(warChunk);
        defenders.setClaimedChunks(defendersChunks.toArray(new Chunk[defendersChunks.size()]));
        defenders.setSecondsInWarChunk(0);

        List<Chunk> attackersChunks = Arrays.asList(attackers.getClaimedChunks());
        attackersChunks.add(warChunk);
        attackers.setClaimedChunks(attackersChunks.toArray(new Chunk[attackersChunks.size()]));

        hashMap.remove(attackers.getTownName());
        Main.setChunksWars(hashMap);
        //Fin de la procedure ici

        //Donner la mise dans le milieu du chunk.
        World world = warChunk.getWorld();
        int ChunkX = warChunk.getX() * 16 + 8;
        int ChunkZ = warChunk.getZ() * 16 + 8;

        int yCoos = 80;
        while(!new Location(world, ChunkX, yCoos,ChunkZ).getBlock().getType().equals(Material.AIR)){
            yCoos = yCoos + 1;
        }

        Location loc = new Location(world, ChunkX, yCoos, ChunkZ);
        ItemStack iron = new ItemStack(Material.IRON_INGOT, processPrice());
        ItemStack gold = new ItemStack(Material.GOLD_INGOT, processPrice());
        ItemStack diamond = new ItemStack(Material.DIAMOND, 16);

        loc.getWorld().dropItemNaturally(loc, iron);
        loc.getWorld().dropItemNaturally(loc, gold);
        loc.getWorld().dropItemNaturally(loc, diamond);

        townAnnoucement(attackers.getTownName(), Prefixes.townBasicPrefix + "Your town successfully winned the battle for chunk in world " + warChunk.getWorld().getName() + " at " + warChunk.getX() + ";" + warChunk.getZ() + "!");
        townAnnoucement(attackers.getTownName(), Prefixes.townBasicPrefix + "Your rewards for winning the chunk has spawned in " + loc.getWorld().getName() + " at " + loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + "! Go take them!");
    }

    //Quand les defendeurs win.
    public static void endWarProcessAsDefenders(TownCache defenders, TownCache attackers, Chunk warChunk) {
        //Faire la procedure pour donner le chunk a la ville et l'enlever du cache.
        HashMap<String, String> hashMap = Main.getChunksWars();
        defenders.setSecondsInWarChunk(0);
        attackers.setSecondsInWarChunk(0);
        attackers.setWarChunk(null);
        townAnnoucement(attackers.getTownName(), Prefixes.townBasicPrefix + "Your town losed the current chunk war. What a shame!");
        hashMap.remove(attackers.getTownName());
        Main.setChunksWars(hashMap);
        //Fin de la procedure ici

        //Donner la mise dans le milieu du chunk.
        World world = warChunk.getWorld();
        int ChunkX = warChunk.getX() * 16 + 8;
        int ChunkZ = warChunk.getZ() * 16 + 8;

        int yCoos = 80;
        while(!new Location(world, ChunkX, yCoos,ChunkZ).getBlock().getType().equals(Material.AIR)){
            yCoos = yCoos + 1;
        }

        Location loc = new Location(world, ChunkX, yCoos, ChunkZ);
        ItemStack iron = new ItemStack(Material.IRON_INGOT, processPrice());
        ItemStack gold = new ItemStack(Material.GOLD_INGOT, processPrice());
        ItemStack diamond = new ItemStack(Material.DIAMOND, 16);

        loc.getWorld().dropItemNaturally(loc, iron);
        loc.getWorld().dropItemNaturally(loc, gold);
        loc.getWorld().dropItemNaturally(loc, diamond);

        townAnnoucement(defenders.getTownName(), Prefixes.townBasicPrefix + "Your town successfully winned the battle for chunk in world " + warChunk.getWorld().getName() + " at " + warChunk.getX() + ";" + warChunk.getZ() + "!");
        townAnnoucement(defenders.getTownName(), Prefixes.townBasicPrefix + "Your rewards for winning the chunk has spawned in " + loc.getWorld().getName() + " at " + loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + "! Go take them!");
    }

    public static boolean isPlayerOfTownAlreadyInBankInventory(TownCache town){
        for(UUID uuid : town.getResidents()){
            OfflinePlayer offP = Bukkit.getOfflinePlayer(uuid);
            if(offP.isOnline()){
                Player p = Bukkit.getPlayer(uuid);
                if(p.getOpenInventory().getTitle().equals("Bank")){
                    return true;
                }
            }
        }
        return false;
    }

}
