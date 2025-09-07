package dbconnection;

import interfaces.Interfaces;
import mvc.Mvc3;
import logic.JsonHandler;
import logic.UtilityClass;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import java.util.logging.Level;

/**
 * @author erick
 */
public class GetData{
    private final UtilityClass uc=new UtilityClass();
    private static final int BATCH_LIMIT_1=100;
    private static final int BATCH_LIMIT_2=25;

    /**
     * @param accId
     * @return
     */
    public String getRealm(long accId){
        String realm=null;
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select distinct realm from user_data where wotb_id=?")){
            ps.setLong(1,accId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    realm=uc.getRealm(rs.getString("realm"));
                }
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
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
            uc.log(Level.SEVERE,e.getMessage(),e);
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
                            if(currentTankIdList.size()==BATCH_LIMIT_1){
                                currentTankIdList=new ArrayList<>();
                                tankIdLists.add(currentTankIdList);
                            }
                        }
                    }
                }
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return tankIdLists;
    }

    /**
     * @param accId
     * @return
     */
    public Mvc3 getPlayerStats(long accId){
        new JsonHandler().dataManipulation(accId);
        Mvc3 data=new Mvc3();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select sum(wins) as wins,sum(battles) as battles from tank_stats where wotb_id=?")){
            ps.setLong(1,accId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    data.setBattles(rs.getInt("battles"));
                    data.setWins(rs.getInt("wins"));
                }
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return data;
    }
    
    /**
     * @return
     */
    public List<Interfaces.RealmSchedule> getTournamentDateInfo(){
        List<Interfaces.RealmSchedule> list=new ArrayList<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select registration_start_at,registration_end_at,realm from tournament_data");
                ResultSet rs=ps.executeQuery()){
            while(rs.next()){
                long rsa=rs.getLong("registration_start_at");
                long rea=rs.getLong("registration_end_at");
                String realm=rs.getString("realm");

                ZoneId zoneId=switch(realm){
                    case "EU"->ZoneId.of("Europe/Nicosia");
                    case "NA"->ZoneId.of("America/Chicago");
                    case "ASIA"->ZoneId.of("Asia/Singapore");
                    default->ZoneId.systemDefault();
                };

                list.add(new Interfaces.RealmSchedule(realm,rsa,rea,zoneId));
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return list;
    }
    
    public Map<String,Map<Integer,List<List<Interfaces.TeamInfo>>>> getIngameTeamIds(){
        Map<String,Map<Integer,List<List<Interfaces.TeamInfo>>>> teams=new HashMap<>();
        Map<String,Map<Integer,List<Interfaces.TeamInfo>>> tempGroupedData=new HashMap<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select clan_id,tournament_id,team_id,realm from ingame_team_data");
                ResultSet rs=ps.executeQuery()){
            while(rs.next()){
                String realm=rs.getString("realm");
                int tourneyId=rs.getInt("tournament_id");
                String teamId=rs.getString("team_id");
                int clanId=rs.getInt("clan_id");

                tempGroupedData.
                        computeIfAbsent(realm,k->new HashMap<>()).
                        computeIfAbsent(tourneyId,k->new ArrayList<>()).
                        add(new Interfaces.TeamInfo(teamId,clanId));
            }

            for(Map.Entry<String,Map<Integer,List<Interfaces.TeamInfo>>> realmEntry:tempGroupedData.entrySet()){
                String realm=realmEntry.getKey();
                
                Map<Integer,List<Interfaces.TeamInfo>> tourneysInRealm=realmEntry.getValue();
                Map<Integer,List<List<Interfaces.TeamInfo>>> finalTourneyMap=teams.computeIfAbsent(realm,k->new HashMap<>());

                for(Map.Entry<Integer,List<Interfaces.TeamInfo>> tourneyEntry:tourneysInRealm.entrySet()){
                    int tourneyId=tourneyEntry.getKey();
                    List<Interfaces.TeamInfo> allTeamsInTourney=tourneyEntry.getValue();

                    List<List<Interfaces.TeamInfo>> batches=new ArrayList<>();
                    List<Interfaces.TeamInfo> currentBatch=new ArrayList<>();

                    for(Interfaces.TeamInfo teamInfo:allTeamsInTourney){
                        currentBatch.add(teamInfo);
                        if(currentBatch.size()==BATCH_LIMIT_2){
                            batches.add(new ArrayList<>(currentBatch));
                            currentBatch.clear();
                        }
                    }

                    if(!currentBatch.isEmpty()){
                        batches.add(currentBatch);
                    }

                    finalTourneyMap.put(tourneyId,batches);
                }
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return teams;
    }

    /**
     * @param accIds
     * @return
     */
    public Map<Long,Mvc3> getPlayerStatsInBatch(List<Long> accIds){
        Map<Long,Mvc3> resultMap=new HashMap<>();
        if(accIds==null||accIds.isEmpty()){
            return resultMap;
        }

        StringBuilder placeholders=new StringBuilder();
        for(int i=0;i<accIds.size();i++){
            placeholders.append("?");
            if(i<accIds.size()-1){
                placeholders.append(",");
            }
        }

        String sql="SELECT wotb_id, SUM(wins) AS wins, SUM(battles) AS battles FROM tank_stats WHERE wotb_id IN ("+placeholders.toString()+") GROUP BY wotb_id";

        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement(sql)){
            for(int i=0;i<accIds.size();i++){
                ps.setLong(i+1,accIds.get(i));
            }

            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    Mvc3 data=new Mvc3();
                    data.setBattles(rs.getInt("battles"));
                    data.setWins(rs.getInt("wins"));
                    resultMap.put(rs.getLong("wotb_id"),data);
                }
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return resultMap;
    }

    /**
     * @param accIds
     * @return
     */
    public Map<Long,Map<Integer,int[]>> getLowerTierStatsInBatch(List<Long> accIds){
        Map<Long,Map<Integer,int[]>> resultMap=new HashMap<>();
        if(accIds==null||accIds.isEmpty()){
            return resultMap;
        }

        StringBuilder placeholders=new StringBuilder();
        for(int i=0;i<accIds.size();i++){
            placeholders.append("?");
            if(i<accIds.size()-1){
                placeholders.append(",");
            }
        }

        String sql="SELECT tb.wotb_id, td.tank_tier, SUM(tb.battles) AS battles, SUM(tb.wins) AS wins FROM thousand_battles tb JOIN tank_data td ON td.tank_id = tb.tank_id WHERE tb.wotb_id IN ("+placeholders.toString()+") AND td.tank_tier BETWEEN 5 AND 9 GROUP BY tb.wotb_id, td.tank_tier";

        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement(sql)){
            for(int i=0;i<accIds.size();i++){
                ps.setLong(i+1,accIds.get(i));
            }

            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    long wotbId=rs.getLong("wotb_id");
                    int tier=rs.getInt("tank_tier");
                    int battles=rs.getInt("battles");
                    int wins=rs.getInt("wins");
                    resultMap.computeIfAbsent(wotbId,k->new HashMap<>()).put(tier,new int[]{battles,wins});
                }
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return resultMap;
    }
    
    public Map<Long,Interfaces.UserData> getPlayerFuncData(){
        Map<Long,Interfaces.UserData> dbDataMap=new HashMap<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("SELECT u.wotb_id, u.nickname, u.last_battle_time, u.updated_at, COALESCE(ts.total_battles, 0) AS tank_stats_battles, COALESCE(tb.total_battles, 0) AS thousand_battles_battles FROM user_data u LEFT JOIN (SELECT wotb_id, SUM(battles) as total_battles FROM tank_stats GROUP BY wotb_id) ts ON u.wotb_id = ts.wotb_id LEFT JOIN (SELECT wotb_id, SUM(battles) as total_battles FROM thousand_battles GROUP BY wotb_id) tb ON u.wotb_id = tb.wotb_id");
                ResultSet rs=ps.executeQuery()){
            while(rs.next()){
                long wotbId=rs.getLong("wotb_id");
                Interfaces.UserData data=new Interfaces.UserData(
                    rs.getString("nickname"),
                    rs.getLong("last_battle_time"),
                    rs.getLong("updated_at"),
                    rs.getInt("tank_stats_battles"),
                    rs.getInt("thousand_battles_battles")
                );
                dbDataMap.put(wotbId,data);
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return dbDataMap;
    }
    
    public Map<Integer,Interfaces.ClanData> getClanFuncData(){
        Map<Integer,Interfaces.ClanData> data=new HashMap<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select clan_id,clantag,realm,updated_at from clan_data")){
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    int clanId=rs.getInt("clan_id");
                    Interfaces.ClanData cdata=new Interfaces.ClanData(
                            rs.getString("clantag"),
                            rs.getString("realm"),
                            rs.getLong("updated_at")
                    );
                    data.put(clanId,cdata);
                }
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
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

                        if(currentAccIdList.size()==BATCH_LIMIT_1){
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
            uc.log(Level.SEVERE,e.getMessage(),e);
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

        try(Connection cn=new DbConnection().getConnection();
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
                        
                        if(currentClanIdList.size()==BATCH_LIMIT_1){
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
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return clanLists;
    }

    /**
     * @return
     */
    public Map<String,List<List<String>>> getTournamentLists(){
        Map<String,List<List<String>>> tourneyLists=new HashMap<>();
        tourneyLists.put("EU",new ArrayList<>());
        tourneyLists.put("NA",new ArrayList<>());
        tourneyLists.put("ASIA",new ArrayList<>());

        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select tournament_id from tournament_data where realm=?")){
            for(Map.Entry<String,List<List<String>>> entry:tourneyLists.entrySet()){
                String realm=entry.getKey();
                List<List<String>> regionLists=entry.getValue();

                ps.setString(1,realm);
                try(ResultSet rs=ps.executeQuery()){
                    List<String> currentTourneyIdList=new ArrayList<>();
                    while(rs.next()){
                        currentTourneyIdList.add(rs.getString("tournament_id"));

                        if(currentTourneyIdList.size()==BATCH_LIMIT_2){
                            regionLists.add(new ArrayList<>(currentTourneyIdList));
                            currentTourneyIdList.clear();
                        }
                    }

                    if(!currentTourneyIdList.isEmpty()){
                        regionLists.add(new ArrayList<>(currentTourneyIdList));
                    }
                }
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return tourneyLists;
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
                if(rs.next()&&rs.getLong("discord_id_caller")==Long.parseLong(discordId)){
                    flag=true;
                }
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return flag;
    }

    /**
     * @param clanId
     * @param realm
     * @return
     */
    public boolean checkClanData(int clanId,String realm){
        boolean flag=false;
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select clan_id from clan_data where clan_id=? and realm=?")){
            ps.setInt(1,clanId);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    flag=true;
                }
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return flag;
    }

    /**
     * @param accId
     * @param realm
     * @return
    */
    public boolean checkUserData(long accId,String realm){
        boolean flag=false;
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select nickname from user_data where wotb_id=? and realm=?")){
            ps.setLong(1,accId);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    flag=true;
                }
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return flag;
    }

    /**
     * @param accId
     * @param realm
     * @return
     */
    public boolean checkTeamPlayer(long accId,String realm){
        boolean flag=false;
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select clan_id from team where wotb_id=? and realm=?")){
            ps.setLong(1,accId);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    flag=true;
                }
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
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
            uc.log(Level.SEVERE,e.getMessage(),e);
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
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return val;
    }

    /**
     * @param accId
     * @return
     */
    public String checkPlayerByID(long accId){
        String val="";
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select nickname from user_data where wotb_id=?")){
            ps.setLong(1,accId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    val=rs.getString("nickname");
                }
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return val;
    }
}