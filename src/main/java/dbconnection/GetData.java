package dbconnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import logic.UtilityClass;
import mvc.Mvc1;

/**
 *
 * @author erick
 */
public class GetData{
    private GetData(){}

    public static Mvc1 getUserData(String wotbId){
        try(PreparedStatement ps=DbConnection.getConnection().prepareStatement("select discord_id,wotb_id,wotb_name,realm from user_data where wotb_id=?")){
            ps.setString(1,wotbId);
            ResultSet rs=ps.executeQuery();

            Mvc1 data=new Mvc1();
            while(rs.next()){
                data.setDiscordId(rs.getString("discord_id"));
                data.setWotbId(rs.getInt("wotb_id"));
                data.setWotbName(rs.getString("wotb_name"));
                data.setServer(rs.getString("realm"));
            }

            rs.close();

            return data;
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
            return null;
        }
    }

    public static boolean existUser(int wotbId){
        try(PreparedStatement ps=DbConnection.getConnection().prepareStatement("select wotb_name from user_data where wotb_id=?")){
            ps.setInt(1,wotbId);
            ResultSet rs=ps.executeQuery();
            return rs.next();
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
            return false;
        }
    }
}