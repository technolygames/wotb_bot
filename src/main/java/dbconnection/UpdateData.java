package dbconnection;

import logic.UtilityClass;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.logging.Level;

/**
 * @author erick
 */
public class UpdateData{
    /**
     * @param nickname
     * @param accId
     */
    public void updateNickname(String nickname,int accId){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("update user_data set nickname=? where wotb_id=?")){
            ps.setString(1,nickname);
            ps.setInt(2,accId);
            ps.executeUpdate();
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     * @param clantag
     * @param clanId
     * @param realm
     */
    public void updateClantag(String clantag,int clanId,String realm){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("update clan_data set clantag=? where clan_id=? and realm=?")){
            ps.setString(1,clantag);
            ps.setInt(2,clanId);
            ps.setString(3,realm);
            ps.executeUpdate();
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }
}