package dbconnection;

import logic.UtilityClass;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.logging.Level;

/**
 * @author erick
 */
public class DeleteData{
    private final UtilityClass uc=new UtilityClass();

    /**
     * @param accId
     * @param teamId
     */
    public void removeFromIngameRoster(long accId,int teamId){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("delete from ingame_team where wotb_id=? and team_id=?")){
            ps.setLong(1,accId);
            ps.setInt(2,teamId);
            ps.executeUpdate();
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     * @param teamId
     */
    public void removeIngameTeam(int teamId){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("delete from ingame_team_data where team_id=?")){
            ps.setInt(1,teamId);
            ps.executeUpdate();
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }
    
    /**
     * @param accId
     * @param clanId
     */
    public void removeFromRoster(long accId,int clanId){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("delete from team where wotb_id=? and clan_id=?")){
            ps.setLong(1,accId);
            ps.setInt(2,clanId);
            ps.executeUpdate();
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }
    
    /**
     * @param clanId
     * @param realm
     */
    public void freeupRoster(int clanId,String realm){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("delete from team where clan_id=? and realm=?")){
            ps.setInt(1,clanId);
            ps.setString(2,realm);
            ps.executeUpdate();
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     * @param accId
     */
    public void freeupThousandBattles(long accId){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("delete from thousand_battles where wotb_id=?")){
            ps.setLong(1,accId);
            ps.executeUpdate();
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }
}