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
    
    public static void deletePlayerFromTeamList(String nickname,String clantag){
        try(PreparedStatement ps=DbConnection.getConnection().prepareStatement("delete from team where wotb_name=? and clantag=?")){
            ps.setString(1,nickname);
            ps.setString(2,clantag);

            ps.execute();
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
        }
    }

    public static void deleteTeam(String clantag,String realm){
        try(PreparedStatement ps=DbConnection.getConnection().prepareStatement("delete from team where clantag=? and realm=?")){
            ps.setString(1,clantag);
            ps.setString(2,realm);

            ps.execute();
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
        }
    }
}