package dbconnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import logic.UtilityClass;

/**
 *
 * @author erick
 */
public class UpdateData{
    protected UpdateData(){}

    public static void updateNicknameFromTeamData(String nickname,int accId){
        try(PreparedStatement ps=DbConnection.getConnection().prepareStatement("update team set wotb_name=? where wotb_id=?")){
            ps.setString(1,nickname);
            ps.setInt(2,accId);

            ps.execute();
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
        }
    }
}