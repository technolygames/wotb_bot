package dbconnection;

import logic.UtilityClass;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import java.util.logging.Level;
import logic.JsonHandler;
import mvc.Mvc3;

/**
 * @author erick
 */
public class GetData{
    /**
     * @param accId
     * @return
     */
    public String getRealm(int accId){
        String realm="";
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select realm from user_data where wotb_id=?")){
            ps.setInt(1,accId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    realm=new UtilityClass().getRealm(rs.getString("realm"));
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        return realm;
    }

    /**
     * @return
     */
    public String getTierTenTankList(){
        StringJoiner tankIdList=new StringJoiner(",");
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select tank_id from tank_data where tank_tier=10")){
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    tankIdList.add(String.valueOf(rs.getInt("tank_id")));
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        return tankIdList.toString().replaceAll(",$","");
    }

    /**
     * @return
     */
    public List<List<String>> getTankLists(){
        List<List<String>> tankIdLists=new ArrayList<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select tank_id,tank_tier from tank_data where tank_tier=?")){
            for(int i=9;i>=5;i--){
                List<String> currentTankIdList=new ArrayList<>();
                tankIdLists.add(currentTankIdList);
                ps.setInt(1,i);
                try(ResultSet rs=ps.executeQuery()){
                    while(rs.next()){
                        String tankId=rs.getString("tank_id");
                        int tier=rs.getInt("tank_tier");
                        if(i==tier){
                            currentTankIdList.add(tankId);
                            if(currentTankIdList.size()==100){
                                currentTankIdList=new ArrayList<>();
                                tankIdLists.add(currentTankIdList);
                            }
                        }
                    }
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        return tankIdLists;
    }

    /**
     * @param accId
     * @return
     */
    public Mvc3 getPlayerStats(int accId){
        new JsonHandler().dataManipulation(accId);
        Mvc3 data=new Mvc3();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select sum(wins) as wins, sum(battles) as battles from tank_stats where wotb_id=?")){
            ps.setInt(1,accId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    data.setBattles(rs.getInt("battles"));
                    data.setWins(rs.getInt("wins"));
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        return data;
    }

    /**
     * @return
     */
    public Map<String,List<List<String>>> getPlayersLists(){
        Map<String, List<List<String>>> accIdLists=new HashMap<>();
        accIdLists.put("EU",new ArrayList<>());
        accIdLists.put("NA",new ArrayList<>());
        accIdLists.put("ASIA",new ArrayList<>());
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select wotb_id from user_data where realm=?")){
            for(Map.Entry<String,List<List<String>>> entry:accIdLists.entrySet()){
                String realm=entry.getKey();
                List<List<String>> regionLists=entry.getValue();

                ps.setString(1,realm);
                try(ResultSet rs=ps.executeQuery()){
                    List<String> currentAccIdList=new ArrayList<>();
                    while(rs.next()){
                        String accId=rs.getString("wotb_id");
                        currentAccIdList.add(accId);

                        if(currentAccIdList.size()==100){
                            regionLists.add(new ArrayList<>(currentAccIdList));
                            currentAccIdList.clear();
                        }
                    }

                    if(!currentAccIdList.isEmpty()){
                        regionLists.add(new ArrayList<>(currentAccIdList));
                    }
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        return accIdLists;
    }

    /**
     * @return
     */
    public Map<String,List<List<String>>> getClanLists(){
        Map<String,List<List<String>>> clanLists=new HashMap<>();
        clanLists.put("EU",new ArrayList<>());
        clanLists.put("NA",new ArrayList<>());
        clanLists.put("ASIA",new ArrayList<>());

        try (Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select clan_id from clan_data where realm=?")){
            for(Map.Entry<String,List<List<String>>> entry:clanLists.entrySet()){
                String realm=entry.getKey();
                List<List<String>> regionLists=entry.getValue();

                ps.setString(1,realm);
                try(ResultSet rs=ps.executeQuery()){
                    List<String> currentClanIdList=new ArrayList<>();
                    while(rs.next()){
                        String clanId=rs.getString("clan_id");
                        currentClanIdList.add(clanId);
                        
                        if(currentClanIdList.size()==100){
                            regionLists.add(new ArrayList<>(currentClanIdList));
                            currentClanIdList.clear();
                        }
                    }

                    if(!currentClanIdList.isEmpty()){
                        regionLists.add(new ArrayList<>(currentClanIdList));
                    }
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        return clanLists;
    }

    /**
     * @param discordId
     * @param clanId
     * @param realm
     * @return
     */
    public boolean checkCallerDiscordId(String discordId,int clanId,String realm){
        boolean flag=false;
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select discord_id_caller from team where clan_id=? and realm=?")){
            ps.setInt(1,clanId);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    if(rs.getLong("discord_id_caller")!=Long.parseLong(discordId)){
                        flag=true;
                    }
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        return flag;
    }

    /**
     * @param clantag
     * @param realm
     * @return
     */
    public boolean checkClanData(String clantag,String realm){
        boolean flag=false;
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select clan_id from clan_data where clantag=? and realm=?")){
            ps.setString(1,clantag);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    flag=true;
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        return flag;
    }

    /**
     * @param clanId
     * @param realm
     * @return
     */
    public boolean checkTeam(int clanId,String realm){
        boolean flag=false;
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select clan_id from team where clan_id=? and realm=?")){
            ps.setInt(1,clanId);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    flag=true;
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        return flag;
    }

    /**
     * @param accId
     * @param realm
     * @return
    */
    public boolean checkUserData(int accId,String realm){
        boolean flag=false;
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select wotb_id from user_data where wotb_id=? and realm=?")){
            ps.setInt(1,accId);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    flag=true;
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        return flag;
    }

    /**
     * @param accId
     * @param realm
     * @return
     */
    public boolean checkTeamPlayer(int accId,String realm){
        boolean flag=false;
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select clan_id from team where wotb_id=? and realm=?")){
            ps.setInt(1,accId);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    flag=true;
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        return flag;
    }

    /**
     * @param clanId
     * @param realm
     * @return
     */
    public String checkClantagByID(int clanId,String realm){
        String val="";
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select clantag from clan_data where clan_id=? and realm=?")){
            ps.setInt(1,clanId);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    val=rs.getString("clantag");
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        return val;
    }

    /**
     * @param clantag
     * @param realm
     * @return
     */
    public int checkClanIdByTag(String clantag,String realm){
        int val=0;
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select clan_id from clan_data where clantag=? and realm=?")){
            ps.setString(1,clantag);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    val=rs.getInt("clan_id");
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        return val;
    }

    /**
     * @param accId
     * @return
     */
    public String checkPlayerByID(int accId){
        String val="";
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select ud.nickname from team t join user_data ud on t.wotb_id=ud.wotb_id where t.wotb_id=?")){
            ps.setInt(1,accId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    val=rs.getString("nickname");
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        return val;
    }
}