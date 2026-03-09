package dbconnection;

import interfaces.Interfaces;
import logic.JsonHandler;
import logic.UtilityClass;
import logic.BotActions;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    
    public Interfaces.TourneyValues getTourneyValues(int teamId,int tourneyId){
        Interfaces.TourneyValues values=null;
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select td.title,td.teams_confirmed,td.seed_type,td.realm from tournament_data td join ingame_team_data itd on td.tournament_id=itd.tournament_id where itd.team_id=? or itd.tournament_id=?")){
            ps.setInt(1,teamId);
            ps.setInt(2,tourneyId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    String seedType=rs.getString("seed_type");
                    Integer teamsConfirmed=rs.getInt("teams_confirmed");
                    String realm=rs.getString("realm").toLowerCase();
                    String title=rs.getString("title").toLowerCase();

                    int teamsPerGroup=4;
                    int maxGroups=UtilityClass.getMaxGroups(teamsConfirmed,teamsPerGroup);
                    int requiredBattles=UtilityClass.MAX_BATTLE_COUNT;

                    switch(title){
                        case String s when s.contains("leader competition tourney")->{
                            if(realm.equalsIgnoreCase("na")||realm.equalsIgnoreCase("asia")){
                                if(teamsConfirmed<=64){
                                    teamsPerGroup=8;
                                    maxGroups=UtilityClass.getMaxGroups(teamsConfirmed,teamsPerGroup);
                                    requiredBattles=UtilityClass.MIN_BATTLE_COUNT;
                                }
                            }
                            if(realm.equalsIgnoreCase("eu")){
                                if(teamsConfirmed<=256){
                                    teamsPerGroup=16;
                                    maxGroups=UtilityClass.getMaxGroups(teamsConfirmed,teamsPerGroup);
                                    requiredBattles=UtilityClass.MIN_BATTLE_COUNT;
                                }
                            }
                        }
                        case String s when s.contains("arena competition")->{
                            if(realm.equalsIgnoreCase("na")||realm.equalsIgnoreCase("asia")){
                                switch(teamsConfirmed){
                                    case Integer i when (i<=32)->maxGroups=8;
                                    case Integer i when (i<=64)->maxGroups=16;
                                    case Integer i when (i<=128)->{
                                        maxGroups=16;
                                        teamsPerGroup=8;
                                        requiredBattles=UtilityClass.MIN_BATTLE_COUNT;
                                    }
                                    default->{}
                                }
                            }
                            if(realm.equalsIgnoreCase("eu")){
                                if(teamsConfirmed<=256){
                                    maxGroups=32;
                                    teamsPerGroup=8;
                                    requiredBattles=UtilityClass.MAX_BATTLE_COUNT;
                                }
                            }
                        }
                        case String s when s.contains("prove your skill")->{
                            if(realm.equalsIgnoreCase("na")||realm.equalsIgnoreCase("asia")){
                                if(teamsConfirmed<=64||teamsConfirmed<=32){
                                    teamsPerGroup=4;
                                    maxGroups=UtilityClass.getMaxGroups(teamsConfirmed,teamsPerGroup);
                                    requiredBattles=UtilityClass.MAX_BATTLE_COUNT;
                                }
                            }
                            if(realm.equalsIgnoreCase("eu")){
                                if(teamsConfirmed<=256||teamsConfirmed<=512){
                                    teamsPerGroup=8;
                                    maxGroups=UtilityClass.getMaxGroups(teamsConfirmed,teamsPerGroup);
                                    requiredBattles=UtilityClass.MAX_BATTLE_COUNT;
                                }
                                if(teamsConfirmed<=128){
                                    teamsPerGroup=4;
                                    maxGroups=UtilityClass.getMaxGroups(teamsConfirmed,teamsPerGroup);
                                    requiredBattles=UtilityClass.MAX_BATTLE_COUNT;
                                }
                            }
                        }
                        default->{
                            teamsPerGroup=4;
                            maxGroups=UtilityClass.getMaxGroups(teamsConfirmed,teamsPerGroup);
                            requiredBattles=UtilityClass.MAX_BATTLE_COUNT;
                        }
                    }
                    values=new Interfaces.TourneyValues(teamsPerGroup,maxGroups,requiredBattles,seedType);
                }
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return values;
    }
    
    /**
     * @param tourneyId
     * @return
     */
    public List<Interfaces.LeaderboardEntry> getIngameLeaderboardData(int tourneyId){
        List<Interfaces.LeaderboardEntry> teams=new ArrayList<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select distinct t.team_id,t.team_name,cd.clantag from ingame_team_data t left join clan_data cd on cd.clan_id=t.clan_id where t.tournament_id=?")){
            ps.setInt(1,tourneyId);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    double winrate=new BotActions().getIngameTeamWinrate(rs.getInt("team_id"));
                    teams.add(new Interfaces.LeaderboardEntry(rs.getString("team_name"),rs.getString("clantag"),winrate));
                }
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return teams.stream().sorted(Comparator.comparingDouble(Interfaces.LeaderboardEntry::winrate).reversed()).toList();
    }
    
    /**
     * @param realm
     * @return
     */
    public List<Interfaces.LeaderboardEntry> teamLeaderboard(String realm){
        Set<Interfaces.LeaderboardEntry> teams=new HashSet<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("SELECT cd.clan_id, cd.clantag, t.wotb_id FROM team t JOIN clan_data cd ON cd.clan_id = t.clan_id WHERE cd.realm = ?")){
            ps.setString(1,realm);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    double winrate=new BotActions().getTeamWinrate(rs.getInt("clan_id"));
                    teams.add(new Interfaces.LeaderboardEntry(null,rs.getString("clantag"),winrate));
                }
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return teams.stream().sorted(Comparator.comparingDouble(Interfaces.LeaderboardEntry::winrate).reversed()).toList();
    }
    
    /**
     * @param clanId
     * @return
     */
    public Map<Long,String> getTeamData(int clanId){
        Map<Long,String> playerNicknameMap=new HashMap<>();
        try(Connection cn=new DbConnection().getConnection();
             PreparedStatement ps=cn.prepareStatement("SELECT ud.wotb_id, ud.nickname FROM team t JOIN user_data ud ON ud.wotb_id = t.wotb_id WHERE t.clan_id = ?")){
            ps.setInt(1,clanId);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    playerNicknameMap.put(rs.getLong("wotb_id"),rs.getString("nickname"));
                }
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return playerNicknameMap;
    }

    /**
     * Only for API usage.
     * @return
     */
    public List<List<String>> getTierTenTankList(){
        List<List<String>> list=new ArrayList<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select tank_id from tank_data where tank_tier=10");
                ResultSet rs=ps.executeQuery()){
            List<String> currentList=new ArrayList<>();
            while(rs.next()){
                currentList.add(rs.getString("tank_id"));
                if(currentList.size()==BATCH_LIMIT_1){
                    list.add(new ArrayList<>(currentList));
                    currentList.clear();
                }
            }
            if(!currentList.isEmpty()){
                list.add(new ArrayList<>(currentList));
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return list;
    }

    /**
     * Only for API usage.
     * @return
     */
    public List<List<String>> getTankLists(){
        List<List<String>> tankIdLists=new ArrayList<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select tank_id from tank_data where tank_tier between 5 and 9 order by tank_tier desc")){
            List<String> allTankIdsForTier=new ArrayList<>();
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    allTankIdsForTier.add(rs.getString("tank_id"));
                    if(allTankIdsForTier.size()==BATCH_LIMIT_1){
                        tankIdLists.add(new ArrayList<>(allTankIdsForTier));
                        allTankIdsForTier.clear();
                    }
                }
            }
            if(!allTankIdsForTier.isEmpty()){
                tankIdLists.add(new ArrayList<>(allTankIdsForTier));
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
    public Interfaces.TankStats getPlayerStats(long accId){
        new JsonHandler().dataManipulation(accId);
        Interfaces.TankStats data=null;
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select sum(wins) as wins,sum(battles) as battles from tank_stats where wotb_id=?")){
            ps.setLong(1,accId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    data=new Interfaces.TankStats(
                            rs.getInt("battles"),
                            rs.getInt("wins")
                    );
                }
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return data;
    }

    /**
     * @param accId
     * @return
     */
    public Interfaces.TankStats getPlayerStats2(long accId){
        Interfaces.TankStats data=null;
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select sum(wins) as wins,sum(battles) as battles from tank_stats where wotb_id=?")){
            ps.setLong(1,accId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    data=new Interfaces.TankStats(
                            rs.getInt("battles"),
                            rs.getInt("wins")
                    );
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
    public Map<Integer,List<Long>> getIngameTeamInfo(){
        Map<Integer,List<Long>> dbMapTeam=new HashMap<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select wotb_id,team_id from ingame_team")){
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    dbMapTeam.computeIfAbsent(rs.getInt("team_id"),l->new ArrayList<>()).add(rs.getLong("wotb_id"));
                }
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return dbMapTeam;
    }
    
    /**
     * @return
     */
    public Map<Integer,Map<Integer,Interfaces.TourneyTeamInfo>> getIngameTeamData(){
        Map<Integer,Map<Integer,Interfaces.TourneyTeamInfo>> data=new HashMap<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select tournament_id,team_id,clan_id,team_name from ingame_team_data")){
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    data.computeIfAbsent(rs.getInt("tournament_id"),k->new HashMap<>()).
                            put(rs.getInt("team_id"),new Interfaces.TourneyTeamInfo(rs.getInt("clan_id"),rs.getString("team_name")));
                }
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return data;
    }

    /**
     * @param accIds
     * @return
     */
    public Map<Long,Interfaces.TankStats> getPlayerStatsInBatch(List<Long> accIds){
        Map<Long,Interfaces.TankStats> resultMap=new HashMap<>();
        if(accIds==null||accIds.isEmpty()){
            return resultMap;
        }

        String sql="SELECT wotb_id, SUM(wins) AS wins, SUM(battles) AS battles FROM tank_stats WHERE wotb_id IN ("+UtilityClass.getSqlPlaceholders(accIds)+") GROUP BY wotb_id";

        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement(sql)){
            for(int i=0;i<accIds.size();i++){
                ps.setLong(i+1,accIds.get(i));
            }

            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    resultMap.put(rs.getLong("wotb_id"),
                            new Interfaces.TankStats(
                                    rs.getInt("battles"),
                                    rs.getInt("wins")
                            ));
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
    public Map<Long,Map<Integer,Interfaces.TankStats>> getLowerTierStatsInBatch(List<Long> accIds){
        Map<Long,Map<Integer,Interfaces.TankStats>> resultMap=new HashMap<>();
        if(accIds==null||accIds.isEmpty()){
            return resultMap;
        }

        String sql="SELECT tb.wotb_id, td.tank_tier, SUM(tb.battles) AS battles, SUM(tb.wins) AS wins FROM thousand_battles tb JOIN tank_data td ON td.tank_id = tb.tank_id WHERE tb.wotb_id IN ("+UtilityClass.getSqlPlaceholders(accIds)+") AND td.tank_tier BETWEEN 5 AND 9 GROUP BY tb.wotb_id, td.tank_tier";

        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement(sql)){
            for(int i=0;i<accIds.size();i++){
                ps.setLong(i+1,accIds.get(i));
            }

            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    resultMap.computeIfAbsent(rs.getLong("wotb_id"),k->new HashMap<>()).put(rs.getInt("tank_tier"),
                            new Interfaces.TankStats(
                                    rs.getInt("battles"),
                                    rs.getInt("wins")
                            ));
                }
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return resultMap;
    }
    
    /**
     * @return
     */
    public Map<Long,Interfaces.UserData3> getPlayerFuncData(){
        Map<Long,Interfaces.UserData3> playerMap=new HashMap<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select wotb_id,nickname,last_battle_time,updated_at from user_data");
                ResultSet rs=ps.executeQuery()){
            while(rs.next()){
                playerMap.put(rs.getLong("wotb_id"),
                        new Interfaces.UserData3(
                                rs.getString("nickname"),
                                rs.getLong("last_battle_time"),
                                rs.getLong("updated_at")
                        ));
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return playerMap;
    }
    
    /**
     * @return
     */
    public Map<Integer,Interfaces.ClanData> getClanFuncData(){
        Map<Integer,Interfaces.ClanData> data=new HashMap<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select clan_id,clantag,realm,updated_at from clan_data")){
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    data.put(rs.getInt("clan_id"),
                            new Interfaces.ClanData(
                                    rs.getString("clantag"),
                                    rs.getString("realm"),
                                    rs.getLong("updated_at")
                            ));
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
    public Map<Integer,Interfaces.TourneyInfo> getTourneyFuncData(){
        Map<Integer,Interfaces.TourneyInfo> tourneyData=new HashMap<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("SELECT tournament_id, teams_confirmed, realm, registration_start_at, registration_end_at,start_at,end_at FROM tournament_data")){
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    tourneyData.put(rs.getInt("tournament_id"),
                            new Interfaces.TourneyInfo(
                                    rs.getString("realm"),
                                    rs.getLong("registration_start_at"),
                                    rs.getLong("registration_end_at"),
                                    rs.getInt("teams_confirmed"),
                                    rs.getInt("start_at"),
                                    rs.getInt("end_at")
                            ));
                }
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return tourneyData;
    }
    
    /**
     * Only for API usage.
     * @return
     */
    public Map<String,List<List<String>>> getPlayersLists(){
        Map<String,List<List<String>>> accIdLists=UtilityClass.mapList();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select wotb_id from user_data where realm=?")){
            for(Map.Entry<String,List<List<String>>> entry:accIdLists.entrySet()){
                String realm=entry.getKey();
                List<List<String>> regionLists=entry.getValue();

                ps.setString(1,realm);
                try(ResultSet rs=ps.executeQuery()){
                    List<String> currentAccIdList=new ArrayList<>();
                    while(rs.next()){
                        currentAccIdList.add(rs.getString("wotb_id"));

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
     * Only for API usage.
     * @return
     */
    public Map<String,List<List<String>>> getClanLists(){
        Map<String,List<List<String>>> clanLists=UtilityClass.mapList();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select clan_id from clan_data where realm=?")){
            for(Map.Entry<String,List<List<String>>> entry:clanLists.entrySet()){
                String realm=entry.getKey();
                List<List<String>> regionLists=entry.getValue();

                ps.setString(1,realm);
                try(ResultSet rs=ps.executeQuery()){
                    List<String> currentClanIdList=new ArrayList<>();
                    while(rs.next()){
                        currentClanIdList.add(rs.getString("clan_id"));
                        
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
     * Only for API usage.
     * @return
     */
    public Map<String,List<List<String>>> getTournamentLists(){
        Map<String,List<List<String>>> tourneyLists=UtilityClass.mapList();
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
     * @return
     */
    public boolean checkCallerDiscordId(String discordId,int clanId){
        boolean flag=false;
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select discord_id_caller from team where clan_id=?")){
            ps.setInt(1,clanId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    flag=rs.getLong("discord_id_caller")==Long.parseLong(discordId);
                }
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return flag;
    }

    /**
     * @param clanId
     * @return
     */
    public boolean checkClanData(int clanId){
        boolean flag=false;
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select 1 from clan_data where clan_id=?")){
            ps.setInt(1,clanId);
            try(ResultSet rs=ps.executeQuery()){
                flag=rs.next();
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return flag;
    }

    /**
     * @param accId
     * @return
    */
    public boolean checkUserData(long accId){
        boolean flag=false;
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select 1 from user_data where wotb_id=?")){
            ps.setLong(1,accId);
            try(ResultSet rs=ps.executeQuery()){
                flag=rs.next();
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return flag;
    }

    /**
     * @param accId
     * @param clanId
     * @return
     */
    public boolean checkTeamPlayer(long accId,int clanId){
        boolean flag=false;
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select 1 from team where wotb_id=? and clan_id=?")){
            ps.setLong(1,accId);
            ps.setInt(2,clanId);
            try(ResultSet rs=ps.executeQuery()){
                flag=rs.next();
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return flag;
    }

    /**
     * @param teamId
     * @return
     */
    public boolean checkIngameTeamRegistry(int teamId){
        boolean flag=false;
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select 1 from ingame_team_data where team_id=?")){
            ps.setInt(1,teamId);
            try(ResultSet rs=ps.executeQuery()){
                flag=rs.next();
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return flag;
    }
    
    /**
     * @param teamId
     * @param accId
     * @return 
     */
    public boolean checkIngameTeamPlayerRegistry(int teamId,long accId){
        boolean flag=false;
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select 1 from ingame_team where team_id=? and wotb_id=?")){
            ps.setInt(1,teamId);
            ps.setLong(2,accId);
            try(ResultSet rs=ps.executeQuery()){
                flag=rs.next();
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return flag;
    }
    
    /**
     * @param clanId
     * @return
     */
    public String checkClantagByID(int clanId){
        String val=null;
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select clantag from clan_data where clan_id=?")){
            ps.setInt(1,clanId);
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
     * @param accId
     * @return
     */
    public String checkPlayerByID(long accId){
        String val=null;
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