package dbconnection;

import logic.UtilityClass;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.logging.Level;

/**
 *
 * @author erick
 */
public class DeleteData{
    public void deletePlayerFromTeamList(String nickname,String clantag){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("delete from team where wotb_name=? and clantag=?")){
            ps.setString(1,nickname);
            ps.setString(2,clantag);

            ps.execute();
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }

    public void deletePlayerFromList(String nickname,String realm){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("delete from team where wotb_name=? and realm=?")){
            ps.setString(1,nickname);
            ps.setString(2,realm);

            ps.execute();
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }

    public void deleteTeam(String clantag,String realm){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("delete from team where clantag=? and realm=?")){
            ps.setString(1,clantag);
            ps.setString(2,realm);

            ps.execute();
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }
}