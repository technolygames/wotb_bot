package dbconnection;

import logic.UtilityClass;
import mvc.Mvc1;
import mvc.Mvc2;

import java.sql.SQLException;
import java.sql.PreparedStatement;

/**
 *
 * @author erick
 */
public class InsertData{
    private InsertData(){}

    public static void setUserData(Mvc1 data){
        try(PreparedStatement ps=DbConnection.getConnection().prepareStatement("insert into user_data values(?,?,?,?)")){
            ps.setString(1,data.getDiscordId());
            ps.setInt(2,data.getWotbId());
            ps.setString(3,data.getWotbName());
            ps.setString(4,data.getServer());
            
            ps.execute();
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
        }
    }

    public static void setTankStats(Mvc2 data){
        try(PreparedStatement ps=DbConnection.getConnection().prepareStatement("insert into tank_stats values(?,?,?,?,?,?)")){
            ps.setInt(1,data.getPlayerId());
            ps.setInt(2,data.getTankId());
            ps.setInt(3,data.getTankTier());
            ps.setInt(4,data.getBattles());
            ps.setInt(5,data.getWins());
            ps.setInt(6,data.getLosses());

            ps.execute();
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
        }
    }

    public static void setTeam(String clantag,int wotbId,String wotbName){
        try(PreparedStatement ps=DbConnection.getConnection().prepareStatement("insert into team values(?,?,?)")){
            ps.setString(1,clantag);
            ps.setInt(2,wotbId);
            ps.setString(3,wotbName);

            ps.execute();
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
        }
    }
}