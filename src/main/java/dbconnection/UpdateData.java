package dbconnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import logic.UtilityClass;
import mvc.Mvc3;

/**
 *
 * @author erick
 */
public class UpdateData{
    private UpdateData(){}

    private static PreparedStatement ps;

    public static void updateData(Mvc3 data){
        try{
            ps=DbConnection.getConnection().prepareStatement("update tank_stats set battles=battles+?, wins=wins+?, losses=losses+? where tank_id=?;");
            ps.setInt(1,data.getBattleDifference());
            ps.setInt(2,data.getWinDifference());
            ps.setInt(3,data.getLossDifference());
            ps.setInt(4,data.getTankId());

            ps.execute();

            ps.close();
        }catch(SQLException e){
            UtilityClass.LOGGER.info(e.fillInStackTrace().toString());
        }
    }
}