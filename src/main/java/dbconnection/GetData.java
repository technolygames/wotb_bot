package dbconnection;

import logic.UtilityClass;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.logging.Level;

/**
 *
 * @author erick
 */
public class GetData{
    public boolean verifyCallerDiscordId(String discordId,String clantag,String realm){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("select discord_id_caller from team where clantag=? and realm=?")){
            boolean flag=false;
            ps.setString(1,clantag);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    if(rs.getString("discord_id_caller").equals(discordId)){
                        flag=true;
                    }
                }
            }
            return flag;
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return false;
        }
    }

    public String getRealm(int accId){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("select realm from team where wotb_id=?")){
            String realm="";

            ps.setInt(1,accId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    realm=UtilityClass.getRealm(rs.getString("realm"));
                }
            }
            return realm;
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return null;
        }
    }

    public boolean checkClantag(String clantag,String realm){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("select clantag from team where clantag=? and realm=?")){
            ps.setString(1,clantag);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    return true;
                }
            }
            return false;
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return false;
        }
    }

    public boolean checkPlayerClantag(String nickname,String realm){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("select clantag from team where wotb_name=? and realm=?")){
            ps.setString(1,nickname);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    String val=rs.getString("clantag");
                    if(val==null){
                        return true;
                    }
                }
            }
            return false;
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return false;
        }
    }

    public boolean checkPlayerRegistry(String nickname,String realm){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("select wotb_name from team where wotb_name=? and realm=?")){
            ps.setString(1,nickname);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    return true;
                }
            }
            return false;
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return false;
        }
    }
}