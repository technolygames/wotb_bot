package dbconnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import logic.UtilityClass;

/**
 *
 * @author erick
 */
public class DeleteData{
    protected DeleteData(){}

    public static void deleteUserData(int accId){
        try(PreparedStatement ps=DbConnection.getConnection().prepareStatement("delete from user_data where wotb_id=?")){
            ps.setInt(1,accId);

            ps.execute();
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
        }
    }

    public static void deleteTanksByTier(int tier){
        try(PreparedStatement ps=DbConnection.getConnection().prepareStatement("delete from tank_stats where tank_tier=?")){
            ps.setInt(1,tier);

            ps.execute();
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
        }
    }

    public static void deletePlayerFromTeamList(String nickname){
        try(PreparedStatement ps=DbConnection.getConnection().prepareStatement("delete from team where wotb_name=?")){
            ps.setString(1,nickname);

            ps.execute();
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
        }
    }
}