package me.cdrx.geofactions;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TownCache {
    private String townName;
    private UUID owner;
    private UUID[] admins;
    private UUID[] treasurers;
    private UUID[] residents;
    private HashMap<int[], String> claimedChunksUpdate = new HashMap<>();
    private HashMap<int[], String> primaryChunksUpdate = new HashMap<>();
    private List<ItemStack> townBank;
    private Chunk warChunk;
    private int secondsInWarChunk;

    public TownCache(String townName1, UUID owner1, UUID[] admins1, UUID[] treasurers1, UUID[] residents1, Chunk[] claimedChunks1, Chunk[] primaryChunks1, List<ItemStack> bank1){
        this.warChunk = null;
        this.secondsInWarChunk = 0;
        this.townName = townName1;
        this.owner = owner1;
        this.admins = admins1;
        this.treasurers = treasurers1;
        this.residents = residents1;
        setClaimedChunks(claimedChunks1);
        setPrimaryChunks(primaryChunks1);
        this.townBank = bank1;
    }

    public String getTownName() {
        return townName;
    }

    public void setTownName(String townName) {
        this.townName = townName;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public UUID[] getAdmins() {
        return admins;
    }

    public void setAdmins(UUID[] admins) {
        this.admins = admins;
    }

    public UUID[] getTreasurers() {
        return treasurers;
    }

    public void setTreasurers(UUID[] treasurers) {
        this.treasurers = treasurers;
    }

    public UUID[] getResidents() {
        return residents;
    }

    public void setResidents(UUID[] residents) {
        this.residents = residents;
    }

    public Chunk[] getClaimedChunks() {
        Chunk[] listChunk = {};
        for (int[] ints : claimedChunksUpdate.keySet()) {
            listChunk = Arrays.copyOf(listChunk, listChunk.length + 1);
            listChunk[listChunk.length - 1] = Bukkit.getWorld(claimedChunksUpdate.get(ints)).getChunkAt(ints[0], ints[1]);
        }
        return listChunk;
    }

    public void setClaimedChunks(Chunk[] claimedChunks) {
        this.claimedChunksUpdate.clear();
        for(Chunk c : claimedChunks){
            int[] ints = {c.getX(), c.getZ()};
            this.claimedChunksUpdate.put(ints, c.getWorld().getName());
        }
    }

    public Chunk[] getPrimaryChunks() {
        Chunk[] listChunk = {};
        for(int[] ints : primaryChunksUpdate.keySet()){
            listChunk = Arrays.copyOf(listChunk, listChunk.length + 1);
            listChunk[listChunk.length - 1] = Bukkit.getWorld(primaryChunksUpdate.get(ints)).getChunkAt(ints[0], ints[1]);
        }
        return listChunk;
    }

    public void setPrimaryChunks(Chunk[] primaryChunks) {
        this.primaryChunksUpdate.clear();
        for(Chunk c : primaryChunks){
            int[] ints = {c.getX(), c.getZ()};
            this.primaryChunksUpdate.put(ints, c.getWorld().getName());
        }
    }

    public List<ItemStack> getTownBank() {
        return townBank;
    }

    public void setTownBank(List<ItemStack> townBank) {
        this.townBank = townBank;
    }

    public Chunk getWarChunk() {
        return warChunk;
    }

    public void setWarChunk(Chunk warChunk) {
        this.warChunk = warChunk;
    }

    public int getSecondsInWarChunk() {
        return secondsInWarChunk;
    }

    public void setSecondsInWarChunk(int secondsInWarChunk) {
        this.secondsInWarChunk = secondsInWarChunk;
    }
}
