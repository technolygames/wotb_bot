package dbconnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import logic.UtilityClass;
import mvc.Mvc1;
import mvc.Mvc2;

/**
 *
 * @author erick
 */
public class GetData{
    private GetData(){}

    private static PreparedStatement ps;
    private static ResultSet rs;

    public static Mvc1 getUserData(String wotbId){
        try{
            ps=DbConnection.getConnection().prepareStatement("select * from user_data where wotb_id=?");
            ps.setString(1,wotbId);
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
            UtilityClass.LOGGER.info(e.fillInStackTrace().toString());
            return null;
        }
    }

    public static Mvc2 getTankStats(int tankId){
        try{
            ps=DbConnection.getConnection().prepareStatement("select * from tank_stats where tank_id=?");
            ps.setInt(1,tankId);
            rs=ps.executeQuery();

            Mvc2 data=new Mvc2();
            while(rs.next()){
                data.setPlayerId(rs.getInt("player_id"));
                data.setTankId(rs.getInt("tank_id"));
                data.setBattles(rs.getInt("battles"));
                data.setWins(rs.getInt("wins"));
                data.setLosses(rs.getInt("losses"));
            }

            ps.close();
            rs.close();

            return data;
        }catch(SQLException e){
            UtilityClass.LOGGER.info(e.fillInStackTrace().toString());
            return null;
        }
    }

    public static boolean existUser(int wotbId){
        try{
            ps=DbConnection.getConnection().prepareStatement("select * from user_data where wotb_id=?");
            ps.setInt(1,wotbId);
            rs=ps.executeQuery();
            return rs.next();
        }catch(SQLException e){
            UtilityClass.LOGGER.info(e.fillInStackTrace().toString());
            return false;
        }
    }

    public static boolean existTankRegister(int wotbId){
        try{
            ps=DbConnection.getConnection().prepareStatement("select * from tank_stats where player_id=?");
            ps.setInt(1,wotbId);
            rs=ps.executeQuery();
            return rs.next();
        }catch(SQLException e){
            UtilityClass.LOGGER.info(e.fillInStackTrace().toString());
            return false;
        }
    }

    public static ResultSet getTankList(){
        try{
            ps=DbConnection.getConnection().prepareStatement("select tank_id from tank_list where tank_tier=10;");
            return ps.executeQuery();
        }catch(SQLException e){
            UtilityClass.LOGGER.info(e.fillInStackTrace().toString());
            return null;
        }
    }
}