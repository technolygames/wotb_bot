package dbconnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import logic.UtilityClass;

/**
 *
 * @author erick
 */
public class GetData{
    private GetData(){}

    public static boolean verifyCallerDiscordId(String discordId,String clantag,String realm){
        try(PreparedStatement ps=DbConnection.getConnection().prepareStatement("select discord_id_caller from team where clantag=? and realm=?")){
            ps.setString(1,clantag);
            ps.setString(2,realm);
            ResultSet rs=ps.executeQuery();
            boolean flag=false;
            while(rs.next()){
                if(rs.getString("discord_id_caller").equals(discordId)){
                    flag=true;
                }
            }
            return flag;
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
            return false;
        }
    }
}