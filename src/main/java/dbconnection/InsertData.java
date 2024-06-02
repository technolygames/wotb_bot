package dbconnection;

import logic.UtilityClass;

import java.sql.SQLException;
import java.sql.PreparedStatement;

/**
 *
 * @author erick
 */
public class InsertData{
    private InsertData(){}

    public static void setTeam(String clantag,int wotbId,String wotbName){
        try(PreparedStatement ps=DbConnection.getConnection().prepareStatement("insert into team values(?,?,?)")){
            ps.setString(1,clantag);
            ps.setInt(2,wotbId);
            ps.setString(3,wotbName);

            ps.execute();
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
        }
    }
}