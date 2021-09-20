package me.cdrx.sql;

public class DbCredentials {

    private String host;
    private String user;
    private String pass;
    private String dbName;
    private String port;

    public DbCredentials(String host, String user, String pass, String dbName, String port){
        this.dbName = dbName;
        this.port = port;
        this.host = host;
        this.pass = pass;
        this.user = user;
    }

    public String toURI(){
        final StringBuilder sb = new StringBuilder();
        sb.append("jdbc:mysql://")
                .append(host)
                .append(":")
                .append(port)
                .append("/")
                .append(dbName);
        return sb.toString();
    }

    public String getUser() {
        return user;
    }

    public String getPass(){
        return pass;
    }
}
