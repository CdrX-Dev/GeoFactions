package me.cdrx.sql;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class DbManager {
    private DbConnection infoConnection;

    public DbManager(){
        this.infoConnection = new DbConnection(new DbCredentials("na05-sql.pebblehost.com","customer_206734_geofactions","AUtNrx2$s@b0inF@KJnE","customer_206734_geofactions","3306"));
    }

    public DbConnection getInfoConnection(){
        return this.infoConnection;
    }

    //Ferme toutes les connections
    public void close(){
        try{
            this.infoConnection.close();
            Bukkit.getServer().getLogger().info(ChatColor.AQUA + "Sucessfully closed connections to databases!");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
