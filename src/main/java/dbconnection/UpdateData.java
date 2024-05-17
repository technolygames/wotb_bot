package dbconnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import logic.UtilityClass;

/**
 *
 * @author erick
 */
public class UpdateData{
    protected UpdateData(){}

    public static void updateNicknameFromUserData(String nickname,int accId){
        try(PreparedStatement ps=DbConnection.getConnection().prepareStatement("update user_data set wotb_name=? where wotb_id=?")){
            ps.setString(1,nickname);
            ps.setInt(2,accId);

            ps.execute();
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
        }
    }

    public static void updateNicknameFromTeamData(String nickname,int accId){
        try(PreparedStatement ps=DbConnection.getConnection().prepareStatement("update team set wotb_name=? where wotb_id=?")){
            ps.setString(1,nickname);
            ps.setInt(2,accId);

            ps.execute();
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
        }
    }

    public static void updateTierTank(){
        try(var cn=DbConnection.getConnection();
        PreparedStatement ps1=cn.prepareStatement("select * from tank_list");
        PreparedStatement ps2=cn.prepareStatement("update tank_stats set tank_tier=? where tank_id=?")){
            ResultSet rs=ps1.executeQuery();
            while(rs.next()){
                ps2.setInt(1,rs.getInt("tank_tier"));
                ps2.setInt(2,rs.getInt("tank_id"));
                ps2.execute();
            }
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
        }
    }
}