package me.cdrx.geofactions;

import org.bukkit.Chunk;

import java.util.UUID;

public class TownCache {
    private String townName;
    private UUID owner;
    private UUID[] admins;
    private UUID[] treasurers;
    private UUID[] residents;
    private Chunk[] claimedChunks;
    private Chunk[] primaryChunks;

    public TownCache(String townName1, UUID owner1, UUID[] admins1, UUID[] treasurers1, UUID[] residents1, Chunk[] claimedChunks1, Chunk[] primaryChunks1){
        this.townName = townName1;
        this.owner = owner1;
        this.admins = admins1;
        this.treasurers = treasurers1;
        this.residents = residents1;
        this.claimedChunks = claimedChunks1;
        this.primaryChunks = primaryChunks1;
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
        return claimedChunks;
    }

    public void setClaimedChunks(Chunk[] claimedChunks) {
        this.claimedChunks = claimedChunks;
    }

    public Chunk[] getPrimaryChunks() {
        return primaryChunks;
    }

    public void setPrimaryChunks(Chunk[] primaryChunks) {
        this.primaryChunks = primaryChunks;
    }

}
