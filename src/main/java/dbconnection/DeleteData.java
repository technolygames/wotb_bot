package dbconnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import logic.UtilityClass;

/**
 *
 * @author erick
 */
public class DeleteData{
    public void removeFromRoster(int accId,int clanId){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("delete from team where wotb_id=? and clan_id=?")){
            ps.setInt(1,accId);
            ps.setInt(2,clanId);
            ps.execute();
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }
    
    public void freeupRoster(int clanId,String realm){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("delete from team where clan_id=? and realm=?")){
            ps.setInt(1,clanId);
            ps.setString(2,realm);
            ps.execute();
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }

    public void freeupThousandBattles(int accId){
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("delete from thousand_battles where wotb_id=?")){
            ps.setInt(1,accId);
            ps.execute();
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }
}