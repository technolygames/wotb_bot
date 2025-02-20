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
    /**
     * @param accId
     * @param clanId
     */
    public void removeFromRoster(int accId,int clanId){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("delete from team where wotb_id=? and clan_id=?")){
            ps.setInt(1,accId);
            ps.setInt(2,clanId);
            ps.execute();
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
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
            ps.execute();
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     * @param accId
     */
    public void freeupThousandBattles(int accId){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("delete from thousand_battles where wotb_id=?")){
            ps.setInt(1,accId);
            ps.execute();
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }
}