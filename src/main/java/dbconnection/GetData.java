package dbconnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import logic.UtilityClass;
import mvc.Mvc1;

/**
 *
 * @author erick
 */
public class GetData{
    private GetData(){}

    public static Mvc1 getUserData(String wotbId){
        try(PreparedStatement ps=DbConnection.getConnection().prepareStatement("select * from user_data where wotb_id=?")){
            ps.setString(1,wotbId);
            ResultSet rs=ps.executeQuery();

            Mvc1 data=new Mvc1();
            while(rs.next()){
                data.setDiscordId(rs.getString("discord_id"));
                data.setWotbId(rs.getInt("wotb_id"));
                data.setWotbName(rs.getString("wotb_name"));
                data.setServer(rs.getString("realm"));
            }

            rs.close();

            return data;
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
            return null;
        }
    }

    public static double getTierTenWinRate(int wotbId){
        try(PreparedStatement ps=DbConnection.getConnection().prepareStatement("select sum(battles) as battles,sum(wins) as wins from tank_stats where tank_tier=10 and player_id=?")){
            ps.setInt(1,wotbId);
            ResultSet rs=ps.executeQuery();
            double wr=0;
            while(rs.next()){
                wr=UtilityClass.getOverallWinrate(rs.getInt("wins"),rs.getInt("battles"));
            }
            return wr;
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
            return 0.0;
        }
    }

    public static boolean isUpToOneThousandAndThreeHundredBattles(int wotbId){
        try(PreparedStatement ps=DbConnection.getConnection().prepareStatement("select sum(battles) as battles from tank_stats where tank_tier=10 and player_id=?")){
            ps.setInt(1,wotbId);
            ResultSet rs=ps.executeQuery();
            while(rs.next()){
                if(rs.getInt("battles")>1300){
                    return true;
                }
            }
            return false;
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
            return false;
        }
    }

    public static boolean existUser(int wotbId){
        try(PreparedStatement ps=DbConnection.getConnection().prepareStatement("select * from user_data where wotb_id=?")){
            ps.setInt(1,wotbId);
            ResultSet rs=ps.executeQuery();
            return rs.next();
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
            return false;
        }
    }

    public static boolean existTankRegister(int wotbId){
        try(PreparedStatement ps=DbConnection.getConnection().prepareStatement("select * from tank_stats where player_id=?")){
            ps.setInt(1,wotbId);
            ResultSet rs=ps.executeQuery();
            return rs.next();
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
            return false;
        }
    }

    public static boolean existTankData(int wotbId,int tankId){
        try(PreparedStatement ps=DbConnection.getConnection().prepareStatement("select * from tank_stats where player_id=? and tank_id=?")){
            ps.setInt(1,wotbId);
            ps.setInt(2,tankId);
            ResultSet rs=ps.executeQuery();
            return rs.next();
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
            return false;
        }
    }
}