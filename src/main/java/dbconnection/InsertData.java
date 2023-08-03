package dbconnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import logic.UtilityClass;
import mvc.Mvc1;
import mvc.Mvc2;
import mvc.Mvc4;

/**
 *
 * @author erick
 */
public class InsertData{
    private InsertData(){}

    private static PreparedStatement ps;

    public static void setUserData(Mvc1 data){
        try{
            ps=DbConnection.getConnection().prepareStatement("insert into user_data values(?,?,?)");
            ps.setString(1,data.getDiscordId());
            ps.setInt(2,data.getWotbId());
            ps.setString(3,data.getWotbName());
            
            ps.execute();

            ps.close();
        }catch(SQLException e){
            UtilityClass.LOGGER.info(e.fillInStackTrace().toString());
        }
    }

    public static void setTankStats(Mvc2 data){
        try{
            ps=DbConnection.getConnection().prepareStatement("insert into tank_stats values(?,?,?,?,?,?)");
            ps.setInt(1,data.getPlayerId());
            ps.setInt(2,data.getTankId());
            ps.setInt(3,data.getBattles());
            ps.setInt(4,data.getWins());
            ps.setInt(5,data.getLosses());

            ps.execute();

            ps.close();
        }catch(SQLException e){
            UtilityClass.LOGGER.info(e.fillInStackTrace().toString());
        }
    }

    public static void setTeam(int wotbId,String wotbName){
        try{
            ps=DbConnection.getConnection().prepareStatement("insert into team values(?,?)");
            ps.setInt(1,wotbId);
            ps.setString(2,wotbName);

            ps.execute();

            ps.close();
        }catch(SQLException e){
            UtilityClass.LOGGER.info(e.fillInStackTrace().toString());
        }
    }

    public static void setHundredBattles(Mvc4 data){
        try{
            ps=DbConnection.getConnection().prepareStatement("insert into hundred_battles values(?,?,?,?,?)");
            ps.setInt(2,data.getTierTank());
            ps.setInt(3,data.getBattles());
            ps.setInt(4,data.getWins());
            ps.setDouble(5,data.getWinrate());

            ps.execute();

            ps.close();
        }catch(SQLException e){
            UtilityClass.LOGGER.info(e.fillInStackTrace().toString());
        }
    }

    public static void setTankInfo(int tankId,String tankName,String nation,int tankTier){
        try{
            ps=DbConnection.getConnection().prepareStatement("insert into tank_list values(?,?,?,?)");
            ps.setInt(1,tankId);
            ps.setString(2, tankName);
            ps.setString(3, nation);
            ps.setInt(4, tankTier);
            ps.execute();
            ps.close();
        }catch(SQLException e){
            UtilityClass.LOGGER.info(e.fillInStackTrace().toString());
        }
    }
}