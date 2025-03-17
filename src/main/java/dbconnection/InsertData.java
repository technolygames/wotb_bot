package dbconnection;

import logic.JsonHandler;
import logic.UtilityClass;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.logging.Level;

/**
 * @author erick
 */
public class InsertData{
    /**
     * @param clanId
     * @param clantag
     * @param realm
     * @param updatedAt
     */
    public void setClanInfo(int clanId,String clantag,String realm,long updatedAt){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("insert into clan_data values(?,?,?,?)")){
            ps.setInt(1,clanId);
            ps.setString(2,clantag);
            ps.setString(3,realm);
            ps.setLong(4,updatedAt);
            ps.executeUpdate();
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     * @param clanId
     * @param discordId
     * @param wotbId
     * @param realm
     */
    public void teamRegistration(int clanId,int wotbId,String discordId,String realm){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("insert into team values(?,?,?,?)")){
            ps.setInt(1,clanId);
            ps.setInt(2,wotbId);
            ps.setLong(3,Long.parseLong(discordId));
            ps.setString(4,realm);
            ps.executeUpdate();
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     * @param accId
     * @param nickname
     * @param realm
     * @param lastBattleTime
     * @param updatedAt
     */
    public void registerPlayer(int accId,String nickname,String realm,long lastBattleTime,long updatedAt){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("insert into user_data values(?,?,?,?,?)")){
            ps.setInt(1,accId);
            ps.setString(2,nickname);
            ps.setString(3,realm);
            ps.setLong(4,lastBattleTime);
            ps.setLong(5,updatedAt);
            ps.executeUpdate();
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        new JsonHandler().getAccTankData(accId);
    }
}