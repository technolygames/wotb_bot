package logic;

import dbconnection.DbConnection;
import dbconnection.GetData;
import interfaces.Interfaces;
import mvc.Mvc3;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashMap;

import java.util.logging.Level;

/**
 * @author erick
 */
public class BotActions{
    private final GetData gd=new GetData();
    private final UtilityClass uc=new UtilityClass();

    /**
     * @param tourneyId
     * @param realm
     * @return
     */
    public Map<String,List<Interfaces.LeaderboardEntry>> seedTeams(int tourneyId,String realm){
        Map<String,List<Interfaces.LeaderboardEntry>> groups=new LinkedHashMap<>();
        boolean strongestVsWeakest=false;
        int teamsPerGroup=4;

        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select title,seed_type,teams_confirmed from tournament_data where tournament_id=?")){
            List<Interfaces.LeaderboardEntry> teams=getIngameLeaderboardData(tourneyId,realm);

            ps.setInt(1,tourneyId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    strongestVsWeakest=rs.getString("seed_type").equalsIgnoreCase("strongest-weakest");
                    int teamsConfirmed=rs.getInt("teams_confirmed");
                    if(!rs.getString("title").contains("Prove your skill")&&(teamsConfirmed>0&&teamsConfirmed<32)){
                        teamsPerGroup=8;
                    }
                }
            }

            if(strongestVsWeakest){
                int totalGroups=(int)Math.ceil((double)teams.size()/teamsPerGroup);

                for(int i=0;i<totalGroups;i++){
                    List<Interfaces.LeaderboardEntry> group=new ArrayList<>();
                    if(i<teams.size()){
                        group.add(teams.get(i));
                    }

                    int weakIndex=teams.size()-1-i;
                    if(weakIndex>i&&weakIndex<teams.size()){
                        group.add(teams.get(weakIndex));
                    }

                    for(int j=1;j<teamsPerGroup/2;j++){
                        int strongIndex=i+totalGroups*j;
                        int weakIndex2=teams.size()-1-i-totalGroups*j;

                        if(strongIndex<teams.size()){
                            group.add(teams.get(strongIndex));
                        }
                        if(weakIndex2>strongIndex&&weakIndex2<teams.size()){
                            group.add(teams.get(weakIndex2));
                        }
                    }
                
                    groups.put("Group "+(i+1),group);
                }
            }else{
                int groupNumber=1;
                List<Interfaces.LeaderboardEntry> currentGroup=new ArrayList<>();

                for(int i=0;i<teams.size();i++){
                    currentGroup.add(teams.get(i));

                    if(currentGroup.size()==teamsPerGroup||i==teams.size()-1){
                        groups.put("Group "+groupNumber++,currentGroup);
                        currentGroup=new ArrayList<>();
                    }
                }
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return groups;
    }
    
    /**
     * @param teamId
     * @param realm
     * @return
     */
    public String getIngameTeamRoster(int teamId,String realm){
        String value="";
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("SELECT ud.wotb_id, ud.nickname FROM user_data ud JOIN ingame_team t ON ud.wotb_id = t.wotb_id WHERE t.team_id = ? AND ud.realm = ?")){
            Map<Long,String> teamPlayerData=new HashMap<>();

            ps.setInt(1,teamId);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    long wotbId=rs.getLong("wotb_id");
                    String nickname=rs.getString("nickname");
                    teamPlayerData.put(wotbId,nickname);
                }
            }
            value=buildRosterString(teamPlayerData);
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return value;
    }
    
    /**
     * @param tourneyId
     * @param realm
     * @return
     */
    public List<Interfaces.LeaderboardEntry> getIngameLeaderboardData(int tourneyId,String realm){
        List<Interfaces.LeaderboardEntry> teams=new ArrayList<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select distinct t.team_id,t.team_name,cd.clantag from ingame_team_data t left join clan_data cd on cd.clan_id=t.clan_id where t.tournament_id=? and t.realm=?")){

            ps.setInt(1,tourneyId);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    double winrate=getIngameTeamWinrate(rs.getInt("team_id"));
                    if(winrate!=0.0){
                        teams.add(new Interfaces.LeaderboardEntry(rs.getString("team_name"),rs.getString("clantag"),winrate));
                    }
                }
            }

            teams.sort(Comparator.comparingDouble(Interfaces.LeaderboardEntry::winrate).reversed());
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return teams;
    }
    
    /**
     * @param teamId
     * @return
     */
    public double getIngameTeamWinrate(int teamId){
        List<Long> playerIdsInTeam=new ArrayList<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement psGetPlayers=cn.prepareStatement("SELECT wotb_id FROM ingame_team WHERE team_id=?")){
            psGetPlayers.setInt(1,teamId);
            try(ResultSet rs=psGetPlayers.executeQuery()){
                while(rs.next()){
                    playerIdsInTeam.add(rs.getLong("wotb_id"));
                }
            }
            return calculateTeamWinrateFromPlayerIds(playerIdsInTeam);
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
            return 0.0;
        }
    }
    
    /**
     * @param realm
     * @return
     */
    public String teamLeaderboard(String realm){
        StringBuilder sb=new StringBuilder();
        try(Connection cn=new DbConnection().getConnection()){
            Map<Integer,List<Long>> clanToPlayerIdsMap=new HashMap<>();
            Map<Integer,String> clanDataMap=new HashMap<>();
            List<Long> allPlayerIds=new ArrayList<>();

            try(PreparedStatement ps=cn.prepareStatement("SELECT cd.clan_id, cd.clantag, t.wotb_id FROM clan_data cd JOIN team t ON cd.clan_id = t.clan_id WHERE cd.realm = ?")){
                ps.setString(1,realm);
                try(ResultSet rs=ps.executeQuery()){
                    while(rs.next()){
                        int clanId=rs.getInt("clan_id");
                        String clantag=rs.getString("clantag");
                        long wotbId=rs.getLong("wotb_id");

                        clanDataMap.put(clanId,clantag);
                        clanToPlayerIdsMap.computeIfAbsent(clanId,k->new ArrayList<>()).add(wotbId);
                        
                        if(!allPlayerIds.contains(wotbId)){
                            allPlayerIds.add(wotbId);
                        }
                    }
                }
            }

            if(allPlayerIds.isEmpty()){
                return "There's no teams to show up in this leaderboard.";
            }

            Map<Long,Mvc3> batchTier10Stats=gd.getPlayerStatsInBatch(allPlayerIds);
            Map<Long,Map<Integer,int[]>> batchLowerTierStats=gd.getLowerTierStatsInBatch(allPlayerIds);

            List<Interfaces.LeaderboardEntry> teams=new ArrayList<>();
            for(Map.Entry<Integer,List<Long>> entry:clanToPlayerIdsMap.entrySet()){
                int clanId=entry.getKey();
                List<Long> playerIdsInTeam=entry.getValue();
                
                List<Double> individualWinrates=new ArrayList<>();
                for(Long playerId:playerIdsInTeam){
                    Mvc3 playerT10Data=batchTier10Stats.getOrDefault(playerId,new Mvc3());
                    Map<Integer,int[]> playerLowerData=batchLowerTierStats.getOrDefault(playerId,Collections.emptyMap());

                    double effectiveWinrate=getThousandBattlesTier10Stats(playerT10Data,playerLowerData);
                    if(effectiveWinrate>0){
                        individualWinrates.add(effectiveWinrate);
                    }
                }
                
                if(!individualWinrates.isEmpty()){
                    individualWinrates.sort(Collections.reverseOrder());
                    double sumOfTopWinrates=0.0;
                    int playersCounted=0;
                    int limit=Math.min(individualWinrates.size(),7);

                    for(int i=0;i<limit;i++){
                        sumOfTopWinrates+=individualWinrates.get(i);
                        playersCounted++;
                    }

                    if(playersCounted>0){
                        double teamWinrate=UtilityClass.getFormattedDouble(sumOfTopWinrates/playersCounted);
                        if(teamWinrate>0.0){
                            teams.add(new Interfaces.LeaderboardEntry(null,clanDataMap.get(clanId),teamWinrate));
                        }
                    }
                }
            }

            teams.sort(Comparator.comparingDouble(team->-team.winrate()));
            int count=1;
            for(Interfaces.LeaderboardEntry team:teams){
                sb.append(count).append(". ").append(team).append("\n");
                count++;
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return sb.toString();
    }

    /**
     * @param clanId
     * @param realm
     * @return
    */
    public String getTeamRoster(int clanId,String realm){
        String value="";
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("SELECT ud.wotb_id, ud.nickname FROM team t JOIN user_data ud ON ud.wotb_id = t.wotb_id WHERE t.clan_id = ? AND t.realm = ?")){
            Map<Long,String> teamPlayerData=new HashMap<>();

            ps.setInt(1,clanId);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    long wotbId=rs.getLong("wotb_id");
                    String nickname=rs.getString("nickname");
                    teamPlayerData.put(wotbId,nickname);
                }
            }
            value=buildRosterString(teamPlayerData);
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return value;
    }

    /**
     * @param clanId
     * @param server
     * @return
     */
    public double getTeamWinrate(int clanId,String server){
        List<Long> playerIdsInTeam=new ArrayList<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement psGetPlayers=cn.prepareStatement("select wotb_id from team where clan_id=? and realm=?")){
            psGetPlayers.setInt(1,clanId);
            psGetPlayers.setString(2,server);
            try(ResultSet rs=psGetPlayers.executeQuery()){
                while(rs.next()){
                    playerIdsInTeam.add(rs.getLong("wotb_id"));
                }
            }
            return calculateTeamWinrateFromPlayerIds(playerIdsInTeam);
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
            return 0.0;
        }
    }
    
    /**
     * @param preloadedTier10Data 
     * @param preloadedLowerTierData 
     * @return
     */
    protected double getThousandBattlesTier10Stats(Mvc3 preloadedTier10Data,Map<Integer,int[]> preloadedLowerTierData){
        int battlesT10=(preloadedTier10Data!=null)?preloadedTier10Data.getBattles():0;
        int winsT10=(preloadedTier10Data!=null)?preloadedTier10Data.getWins():0;
        if(battlesT10>=UtilityClass.MAX_BATTLE_COUNT){
            return UtilityClass.getOverallWinrate(winsT10,battlesT10);
        }else{
            return calculatePlayerWeightWithPrefetchedStats(preloadedTier10Data,preloadedLowerTierData);
        }
    }

    /**
     * @param tier10Data 
     * @param lowerTierBattleData 
     * @return
     */
    public double calculatePlayerWeightWithPrefetchedStats(Mvc3 tier10Data,Map<Integer,int[]> lowerTierBattleData){
        final int TOURNAMENT_TIER=10;
        final int REQUIRED_BATTLES=UtilityClass.MAX_BATTLE_COUNT;

        int battlesT10=(tier10Data!=null)?tier10Data.getBattles():0;
        int winsT10=(tier10Data!=null)?tier10Data.getWins():0;

        if(battlesT10>=REQUIRED_BATTLES){
            return UtilityClass.getOverallWinrate(winsT10,battlesT10);
        }

        Map<Integer,int[]> battleData=new HashMap<>();
        if(lowerTierBattleData!=null){
            battleData.putAll(lowerTierBattleData);
        }
        battleData.put(TOURNAMENT_TIER,new int[]{battlesT10,winsT10});

        return getFormulaStats(battleData);
    }
    
    /**
     * @param accId
     * @return
     */
    public double calculatePlayerWeight(long accId){
        final int REQUIRED_BATTLES=UtilityClass.MAX_BATTLE_COUNT;
        double value=0.0;
        Map<Integer,int[]> battleData=new HashMap<>();

        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select sum(tb.battles) as battles,sum(tb.wins) as wins, td.tank_tier from thousand_battles tb join tank_data td on td.tank_id=tb.tank_id where tb.wotb_id=? and td.tank_tier between 5 and 9 group by td.tank_tier")){
            Mvc3 data=gd.getPlayerStats(accId);
            int battles1=data.getBattles();
            if(battles1<REQUIRED_BATTLES){
                battleData.put(10,new int[]{battles1,data.getWins()});
                ps.setLong(1,accId);
                try(ResultSet rs=ps.executeQuery()){
                    while(rs.next()){
                        int tier=rs.getInt("tank_tier");
                        int battles=rs.getInt("battles");
                        int wins=rs.getInt("wins");
                        battleData.put(tier,new int[]{battles,wins});
                    }
                }
                value=getFormulaStats(battleData);
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return value;
    }

    /**
     * @param playerIds
     * @return
     */
    private double calculateTeamWinrateFromPlayerIds(List<Long> playerIds){
        if(playerIds==null||playerIds.isEmpty()){
            return 0.0;
        }

        Map<Long,Mvc3> batchTier10Stats=gd.getPlayerStatsInBatch(playerIds);
        Map<Long,Map<Integer,int[]>> batchLowerTierStats=gd.getLowerTierStatsInBatch(playerIds);
        List<Double> individualWinrates=new ArrayList<>();

        for(Long playerId:playerIds){
            Mvc3 playerT10Data=batchTier10Stats.getOrDefault(playerId,new Mvc3());
            Map<Integer,int[]> playerLowerData=batchLowerTierStats.getOrDefault(playerId,Collections.emptyMap());
            double effectiveWinrate=getThousandBattlesTier10Stats(playerT10Data,playerLowerData);
            if(effectiveWinrate>0){
                individualWinrates.add(effectiveWinrate);
            }
        }

        if(individualWinrates.isEmpty()){
            return 0.0;
        }

        individualWinrates.sort(Collections.reverseOrder());

        double sumOfTopWinrates=0.0;
        int playersCounted=0;
        int limit=Math.min(individualWinrates.size(),7);

        for(int i=0;i<limit;i++){
            sumOfTopWinrates+=individualWinrates.get(i);
            playersCounted++;
        }

        return (playersCounted>0)?UtilityClass.getFormattedDouble(sumOfTopWinrates/playersCounted):0.0;
    }
    
    /**
     * @param teamPlayerData
     * @return
     */
    private String buildRosterString(Map<Long,String> teamPlayerData){
        if(teamPlayerData==null||teamPlayerData.isEmpty()){
            return "";
        }

        List<Long> playerIds=new ArrayList<>(teamPlayerData.keySet());
        Map<Long,Mvc3> batchTier10Stats=gd.getPlayerStatsInBatch(playerIds);
        Map<Long,Map<Integer,int[]>> batchLowerTierStats=gd.getLowerTierStatsInBatch(playerIds);

        List<Interfaces.Player> players=new ArrayList<>();
        for(Long playerId:playerIds){
            String nickname=teamPlayerData.get(playerId);
            Mvc3 playerT10Data=batchTier10Stats.getOrDefault(playerId,new Mvc3());
            Map<Integer,int[]> playerLowerData=batchLowerTierStats.getOrDefault(playerId,Collections.emptyMap());
            double winrate=getThousandBattlesTier10Stats(playerT10Data,playerLowerData);
            players.add(new Interfaces.Player(nickname,winrate));
        }

        players.sort(Comparator.comparingDouble(player->-player.winrate()));

        StringBuilder value=new StringBuilder();
        int count=1;
        for(Interfaces.Player player:players){
            value.append(count).append(". ").append(player).append("\n");
            if(count==7){
                value.append("\n");
            }
            count++;
        }
        return value.toString();
    }
    
    /**
     * @param battleData
     * @return
     */
    private double getFormulaStats(Map<Integer,int[]> battleData){
        final int TOURNAMENT_TIER=10;
        final int REQUIRED_BATTLES=UtilityClass.MAX_BATTLE_COUNT;
        int totalBattles=0;
        double totalWeightedVictories=0.0;
        final double[] penalty={1.0,0.95,0.85,0.7,0.5,0.25};

        if(battleData.containsKey(TOURNAMENT_TIER)){
            int[] stats=battleData.get(TOURNAMENT_TIER);
            int battles=stats[0];
            double winRate=battles>0?(double)stats[1]/battles:0;

            int needed=Math.min(battles,REQUIRED_BATTLES-totalBattles);
            totalWeightedVictories+=needed*winRate;
            totalBattles+=needed;
        }

        if(totalBattles<REQUIRED_BATTLES){
            for(int tier=TOURNAMENT_TIER-1;tier>=5;tier--){
                if(totalBattles>=REQUIRED_BATTLES)break;

                int tierDifference=TOURNAMENT_TIER-tier;
                double penaltyFactor=penalty[Math.min(tierDifference,penalty.length-1)];

                if(battleData.containsKey(tier)){
                    int[] stats=battleData.get(tier);
                    int battles=stats[0];
                    double winRate=battles>0?(double)stats[1]/battles:0;

                    int needed=Math.min(battles,REQUIRED_BATTLES-totalBattles);
                    totalWeightedVictories+=needed*winRate*penaltyFactor;
                    totalBattles+=needed;
                }
            }
        }

        if(totalBattles<REQUIRED_BATTLES){
            totalWeightedVictories+=(REQUIRED_BATTLES-totalBattles)*0;
            totalBattles=REQUIRED_BATTLES;
        }
        
        return UtilityClass.getOverallWinrate(totalWeightedVictories,totalBattles);
    }
}