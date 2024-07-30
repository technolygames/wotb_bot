package dbconnection;

import logic.UtilityClass;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.logging.Level;

/**
 *
 * @author erick
 */
public class InsertData{
    public void setTeam(String clantag,int wotbId,String wotbName){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("insert into team values(?,?,?)")){
            ps.setString(1,clantag);
            ps.setInt(2,wotbId);
            ps.setString(3,wotbName);

            ps.execute();
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }
}