package dbconnection;

import logic.UtilityClass;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

import java.util.logging.Level;
import logic.FileHandler;

/**
 * @author erick
 */
public class DeleteData{
    private final UtilityClass uc=new UtilityClass();

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
     */
    public void freeupRoster(int clanId){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("delete from team where clan_id=?")){
            ps.setInt(1,clanId);
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
    
    /**
     * @param dir
     */
    public void accountDeletionRequest(String dir){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps2=cn.prepareStatement("delete from user_data where wotb_id=?")){
            for(Map.Entry<String,Map<String,List<Long>>> entry:new FileHandler().getCsvData(dir).entrySet()){
                for(Map.Entry<String,List<Long>> entry2:entry.getValue().entrySet()){
                    List<Long> accIds=entry2.getValue();
                    if(accIds.isEmpty())continue;
                    for(long accId:accIds){
                        ps2.setLong(1,accId);
                        ps2.addBatch();
                    }
                }
            }
            ps2.executeBatch();
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }
}