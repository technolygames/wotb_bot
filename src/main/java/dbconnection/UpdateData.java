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
public class UpdateData{
    public void updateNicknameFromTeamData(String nickname,int accId){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("update team set wotb_name=? where wotb_id=?")){
            ps.setString(1,nickname);
            ps.setInt(2,accId);

            ps.execute();
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }

    public void updatePlayerRegistry(String discordId,String clantag,String nickname,String realm){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("update team set discord_id_caller=?,clantag=? where wotb_name=? and realm=?")){
            ps.setString(1,discordId);
            ps.setString(2,clantag);
            ps.setString(3,nickname);
            ps.setString(4,realm);

            ps.execute();
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }

    public void updateTeamClantag(String oldClantag,String newClantag,String realm){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("update team set clantag=? where clantag=? and realm=?")){
            ps.setString(1,newClantag);
            ps.setString(2,oldClantag);
            ps.setString(3,realm);

            ps.execute();
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }

    public void updatePlayerClantag(String clantag,String nickname,String realm){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("update team set clantag=? where wotb_name=? and realm=?")){
            ps.setString(1,clantag);
            ps.setString(2,nickname);
            ps.setString(3,realm);

            ps.execute();
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }
}