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
    private final UtilityClass uc=new UtilityClass();

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
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     * @param clanId
     * @param discordId
     * @param accId
     * @param realm
     */
    public void teamRegistration(int clanId,long accId,String discordId,String realm){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("insert into team values(?,?,?,?)")){
            ps.setInt(1,clanId);
            ps.setLong(2,accId);
            ps.setLong(3,Long.parseLong(discordId));
            ps.setString(4,realm);
            ps.executeUpdate();
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     * @param accId
     * @param nickname
     * @param realm
     * @param lastBattleTime
     * @param updatedAt
     */
    public void registerPlayer(long accId,String nickname,String realm,long lastBattleTime,long updatedAt){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("insert into user_data values(?,?,?,?,?)")){
            ps.setLong(1,accId);
            ps.setString(2,nickname);
            ps.setString(3,realm);
            ps.setLong(4,lastBattleTime);
            ps.setLong(5,updatedAt);
            ps.executeUpdate();
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        new JsonHandler().getAccTankData(accId);
    }
    
    /**
     * @param teamId
     * @param clanId
     * @param tourneyId
     * @param teamName
     * @param realm
     */
    public void ingameTeamDataRegistration(int teamId,int clanId,int tourneyId,String teamName,String realm){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("insert into ingame_team_data values(?,?,?,?,?)")){
            ps.setInt(1,teamId);
            ps.setInt(2,clanId);
            ps.setInt(3,tourneyId);
            ps.setString(4,teamName);
            ps.setString(5,realm);
            ps.executeUpdate();
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }
    
    /**
     * @param teamId
     * @param accId
     */
    public void ingameTeamRegistration(int teamId,long accId){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("insert into ingame_team values(?,?)")){
            ps.setInt(1,teamId);
            ps.setLong(2,accId);
            ps.executeUpdate();
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }
}