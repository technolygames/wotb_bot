package dbconnection;

import logic.UtilityClass;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import java.util.logging.Level;

/**
 *
 * @author erick
 */
public class GetData{
    public String getRealm(int accId){
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select realm from user_data where wotb_id=?")){
            ps.setInt(1,accId);
            String realm="";
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

    public String getTierTenTankList(){
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select tank_id from tank_data where tank_tier=10")){
            StringJoiner tankIdList=new StringJoiner(",");
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    tankIdList.add(String.valueOf(rs.getInt("tank_id")));
                }
            }
            return tankIdList.toString().replaceAll(",$","");
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return null;
        }
    }

    public List<List<String>> getTankLists(int accId){
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select min(td.tank_tier) as tank_tier from thousand_battles tb join tank_data td on td.tank_id=tb.tank_id where tb.wotb_id=?");
        PreparedStatement ps2=cn.prepareStatement("select tank_id from tank_data where tank_tier=?")){
            List<List<String>> tankIdLists=new ArrayList<>();
            List<String> currentTankIdList=new ArrayList<>();
            tankIdLists.add(currentTankIdList);
            ps.setInt(1,accId);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    int tier=rs.getInt("tank_tier");
                    for(int i=9;i>=tier;i--){
                        ps2.setInt(1,i);
                        try(ResultSet rs2=ps2.executeQuery()){
                            while(rs2.next()){
                                String tankId=rs2.getString("tank_id");
                                currentTankIdList.add(tankId);
                                if(currentTankIdList.size()==100){
                                    currentTankIdList=new ArrayList<>();
                                    tankIdLists.add(currentTankIdList);
                                }
                            }
                        }
                    }
                }
            }
            return tankIdLists;
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return new ArrayList<>();
        }
    }

    public boolean verifyCallerDiscordId(String discordId,int clanId,String realm){
        boolean flag=false;
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select discord_id_caller from team where clan_id=? and realm=?")){
            ps.setInt(1,clanId);
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
            return flag;
        }
    }

    public boolean checkClanData(String clantag,String realm){
        boolean flag=false;
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select clan_id from clan_data where clantag=? and realm=?")){
            ps.setString(1,clantag);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    flag=true;
                }
            }
            return flag;
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return flag;
        }
    }

    public boolean checkTeam(int clanId,String realm){
        boolean flag=false;
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select clan_id from team where clan_id=? and realm=?")){
            ps.setInt(1,clanId);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    flag=true;
                }
            }
            return flag;
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return flag;
        }
    }

    public boolean checkUserData(int accId,String realm){
        boolean flag=false;
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select wotb_id from user_data where wotb_id=? and realm=?")){
            ps.setInt(1,accId);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    flag=true;
                }
            }
            return flag;
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return flag;
        }
    }

    public boolean checkTeamPlayer(int accId,String realm){
        boolean flag=false;
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select clan_id from team where wotb_id=? and realm=?")){
            ps.setInt(1,accId);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    flag=true;
                }
            }
            return flag;
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return flag;
        }
    }

    public String checkClantagByID(int clanId,String realm){
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select clantag from clan_data where clan_id=? and realm=?")){
            ps.setInt(1,clanId);
            ps.setString(2,realm);
            String val="";
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    val=rs.getString("clantag");
                }
            }
            return val;
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return null;
        }
    }
    
    public int checkClanIdByTag(String clantag,String realm){
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select clan_id from clan_data where clantag=? and realm=?")){
            ps.setString(1,clantag);
            ps.setString(2,realm);
            int val=0;
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    val=rs.getInt("clan_id");
                }
            }
            return val;
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return 0;
        }
    }

    public String checkPlayerByID(int accId){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("select ud.nickname from team t join user_data ud on t.wotb_id=ud.wotb_id where t.wotb_id=?")){
            ps.setInt(1,accId);
            String val="";
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    val=rs.getString("nickname");
                }
            }
            return val;
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return null;
        }
    }
}