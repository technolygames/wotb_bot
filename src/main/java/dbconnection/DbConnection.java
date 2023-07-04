package dbconnection;

import java.io.IOException;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import logic.UtilityClass;
import mvc.Mvc1;
import mvc.Mvc2;

public class DbConnection{
    protected Properties p;

    protected PreparedStatement ps;
    protected ResultSet rs;
    
    public Connection getConnection(){
        p=new Properties();
        try{
            p.load(new FileInputStream("data/config.properties"));
            String database=p.getProperty("database");
            String server=p.getProperty("server");
            String port=p.getProperty("port");
            String user=p.getProperty("user");
            String pass=p.getProperty("pass");

            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection("jdbc:mysql://"+server+":"+port+"/"+database+"?serverTimezone=UTC",user,pass);
        }catch(SQLException e){
            UtilityClass.LOGGER.config(e.fillInStackTrace().toString());
            return null;
        }catch(IOException x){
            UtilityClass.LOGGER.config(x.fillInStackTrace().toString());
            return null;
        }catch(ClassNotFoundException n){
            UtilityClass.LOGGER.config(n.fillInStackTrace().toString());
            return null;
        }
    }

    public Mvc1 getUserData(String discordId){
        try{
            ps=getConnection().prepareStatement("select * from user_data where discord_id=?");
            ps.setString(1, discordId);
            rs=ps.executeQuery();

            Mvc1 data=new Mvc1();
            while(rs.next()){
                data.setDiscordId(rs.getString("discord_id"));
                data.setWotbId(rs.getInt("wotb_id"));
                data.setWotbName(rs.getString("wotb_name"));
            }

            rs.close();
            ps.close();

            return data;
        }catch(SQLException e){
            UtilityClass.LOGGER.config(e.fillInStackTrace().toString());
            return null;
        }
    }

    public void setUserData(Mvc1 data){
        try{
            ps=getConnection().prepareStatement("insert into user_data values(?,?,?)");
            ps.setString(1,data.getDiscordId());
            ps.setInt(2,data.getWotbId());
            ps.setString(3,data.getWotbName());
            
            ps.execute();

            ps.close();
        }catch(SQLException e){
            UtilityClass.LOGGER.config(e.fillInStackTrace().toString());
        }
    }

    public Mvc2 getTankStats(int tankId){
        try{
            ps=getConnection().prepareStatement("select * from tank_stats where tank_id=?");
            ps.setInt(1,tankId);
            rs=ps.executeQuery();

            Mvc2 data=new Mvc2();
            while(rs.next()){
                data.setTankId(rs.getInt("tank_id"));
                data.setBattles(rs.getInt("battles"));
                data.setWins(rs.getInt("wins"));
                data.setLosses(rs.getInt("losses"));
            }

            ps.close();
            rs.close();

            return data;
        }catch(SQLException e){
            UtilityClass.LOGGER.config(e.fillInStackTrace().toString());
            return null;
        }
    }

    public void setTankStats(Mvc2 data){
        try{
            ps=getConnection().prepareStatement("insert into tank_stats values(?,?,?,?)");
            ps.setInt(1, data.getTankId());
            ps.setInt(2, data.getBattles());
            ps.setInt(3, data.getWins());
            ps.setInt(4, data.getLosses());

            ps.execute();
        }catch(SQLException e){
            UtilityClass.LOGGER.config(e.fillInStackTrace().toString());
        }
    }
}