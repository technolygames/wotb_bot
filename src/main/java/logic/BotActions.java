package logic;

import dbconnection.DbConnection;
import dbconnection.GetData;
import interfaces.Interfaces;

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
     * @return
     */
    public Map<String,List<Interfaces.LeaderboardEntry>> seedTeams(int tourneyId){
        Map<String,List<Interfaces.LeaderboardEntry>> groups=new LinkedHashMap<>();
        try{
            var val=gd.getTourneyValues(0,tourneyId);
            boolean strongestVsWeakest=val.seedType().equalsIgnoreCase("strongest-weakest");
            int teamsPerGroup=val.teamsPerGroup();
            int totalGroups=val.maxGroups();

            List<Interfaces.LeaderboardEntry> teams=gd.getIngameLeaderboardData(tourneyId);
            int totalTeams=teams.size();

            if(strongestVsWeakest){
                for(int i=0;i<totalGroups;i++){
                    List<Interfaces.LeaderboardEntry> group=new ArrayList<>();
                    
                    if(i<totalGroups){
                        group.add(teams.get(i));
                    }
                    
                    int weakIndex=totalTeams-1-i;
                    if(weakIndex>i&&weakIndex<totalTeams){
                        group.add(teams.get(weakIndex));
                    }
                    
                    for(int j=1;j<UtilityClass.getDivision(teamsPerGroup,2);j++){
                        int strongIndex=i+(totalGroups*j);
                        weakIndex=totalTeams-1-i-(totalGroups*j);

                        if(strongIndex<totalTeams){
                            group.add(teams.get(strongIndex));
                        }
                        if(weakIndex>strongIndex&&weakIndex<totalTeams){
                            group.add(teams.get(weakIndex));
                        }
                    }
                
                    groups.put("Group "+(i+1),group);
                }
            }else{
                int groupNumber=1;
                List<Interfaces.LeaderboardEntry> currentGroup=new ArrayList<>();

                for(int i=0;i<totalTeams;i++){
                    currentGroup.add(teams.get(i));

                    if(currentGroup.size()==teamsPerGroup||i==totalTeams-1){
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
     * @return
     */
    public String getIngameTeamRoster(int teamId){
        String value="";
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("SELECT ud.wotb_id, ud.nickname FROM user_data ud JOIN ingame_team t ON ud.wotb_id = t.wotb_id WHERE t.team_id = ?")){
            Map<Long,String> teamPlayerData=new HashMap<>();
            
            var val=gd.getTourneyValues(teamId,0);
            
            ps.setInt(1,teamId);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    long wotbId=rs.getLong("wotb_id");
                    String nickname=rs.getString("nickname");
                    teamPlayerData.put(wotbId,nickname);
                }
            }
            value=buildRosterString(teamPlayerData,val.requiredBattles());
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return value;
    }
    
    /**
     * @param teamId
     * @return
     */
    public double getIngameTeamWinrate(int teamId){
        double winrate=0.0;
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("SELECT wotb_id FROM ingame_team WHERE team_id=?")){
            var val=gd.getTourneyValues(teamId,0);
            List<Long> playerIdsInTeam=new ArrayList<>();
            ps.setInt(1,teamId);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    playerIdsInTeam.add(rs.getLong("wotb_id"));
                }
            }
            winrate=calculateTeamWinrateFromPlayerIds(playerIdsInTeam,val.requiredBattles());
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return winrate;
    }
    
    /**
     * @param clanId
     * @return
     */
    public Interfaces.TeamProfile getTeamProfile(int clanId){
        Map<Long,String> playerNicknameMap=gd.getTeamData(clanId);
        List<Long> playerIds=playerNicknameMap.keySet().stream().toList();
        
        int requiredBattles=UtilityClass.MAX_BATTLE_COUNT;
        
        double winrate=calculateTeamWinrateFromPlayerIds(playerIds,requiredBattles);
        String roster=buildRosterString(playerNicknameMap,requiredBattles);
        
        return new Interfaces.TeamProfile(winrate,roster);
    }

    /**
     * @param clanId
     * @return
     */
    public double getTeamWinrate(int clanId){
        List<Long> playerIdsInTeam=new ArrayList<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select wotb_id from team where clan_id=?")){
            ps.setInt(1,clanId);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    playerIdsInTeam.add(rs.getLong("wotb_id"));
                }
            }
            return calculateTeamWinrateFromPlayerIds(playerIdsInTeam,UtilityClass.MAX_BATTLE_COUNT);
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
            return 0.0;
        }
    }
    
    /**
     * @param teamPlayerData
     * @param requiredBattles 
     * @return
     */
    protected String buildRosterString(Map<Long,String> teamPlayerData,int requiredBattles){
        if(teamPlayerData==null||teamPlayerData.isEmpty()){
            return "";
        }

        List<Long> playerIds=teamPlayerData.keySet().stream().toList();
        Map<Long,Interfaces.TankStats> batchTier10Stats=gd.getPlayerStatsInBatch(playerIds);
        Map<Long,Map<Integer,Interfaces.TankStats>> batchLowerTierStats=gd.getLowerTierStatsInBatch(playerIds);

        List<Interfaces.Player> players=new ArrayList<>();
        for(Long playerId:playerIds){
            String nickname=teamPlayerData.get(playerId);
            Interfaces.TankStats playerT10Data=batchTier10Stats.get(playerId);
            Map<Integer,Interfaces.TankStats> playerLowerData=batchLowerTierStats.get(playerId);
            
            double winrate=getThousandBattlesTier10Stats(playerT10Data,playerLowerData,requiredBattles);
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
     * @param playerIds
     * @param requiredBattles 
     * @return
     */
    protected double calculateTeamWinrateFromPlayerIds(List<Long> playerIds,int requiredBattles){
        if(playerIds==null||playerIds.isEmpty()){
            return 0.0;
        }

        Map<Long,Interfaces.TankStats> batchTier10Stats=gd.getPlayerStatsInBatch(playerIds);
        Map<Long,Map<Integer,Interfaces.TankStats>> batchLowerTierStats=gd.getLowerTierStatsInBatch(playerIds);
        List<Double> individualWinrates=new ArrayList<>();

        for(Long playerId:playerIds){
            Interfaces.TankStats playerT10Data=batchTier10Stats.get(playerId);
            Map<Integer,Interfaces.TankStats> playerLowerData=batchLowerTierStats.get(playerId);
            
            double effectiveWinrate=getThousandBattlesTier10Stats(playerT10Data,playerLowerData,requiredBattles);
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
     * @param preloadedTier10Data 
     * @param preloadedLowerTierData 
     * @param requiredBattles 
     * @return
     */
    protected double getThousandBattlesTier10Stats(Interfaces.TankStats preloadedTier10Data,Map<Integer,Interfaces.TankStats> preloadedLowerTierData,int requiredBattles){
        int battlesT10=(preloadedTier10Data!=null)?preloadedTier10Data.battles():0;
        int winsT10=(preloadedTier10Data!=null)?preloadedTier10Data.wins():0;
        if(battlesT10>=requiredBattles){
            return UtilityClass.getOverallWinrate(winsT10,battlesT10);
        }else{
            return calculatePlayerWeightWithPrefetchedStats(preloadedTier10Data,preloadedLowerTierData,requiredBattles);
        }
    }
    
    /**
     * @param tier10Data 
     * @param lowerTierBattleData 
     * @param requiredBattles 
     * @return
     */
    public double calculatePlayerWeightWithPrefetchedStats(Interfaces.TankStats tier10Data,Map<Integer,Interfaces.TankStats> lowerTierBattleData,int requiredBattles){
        final int TOURNAMENT_TIER=10;
        int totalBattles=0;
        double totalWeightedVictories=0.0;
        
        Map<Integer,Interfaces.TankStats> battleData=new HashMap<>();
        
        if(tier10Data!=null){
            battleData.put(10,new Interfaces.TankStats(tier10Data.battles(),tier10Data.wins()));
        }

        if(lowerTierBattleData!=null){
            battleData.putAll(lowerTierBattleData);
        }
        
        for(int currentTier=TOURNAMENT_TIER;currentTier<=10;currentTier++){
            if(totalBattles>=requiredBattles)break;
            
            Interfaces.TankStats stats=battleData.get(TOURNAMENT_TIER);
            if(stats!=null){
                int battles=stats.battles();
                double winrate=(battles>0)?UtilityClass.getDivision(stats.wins(),battles):0;

                int needed=Math.min(battles,requiredBattles-totalBattles);
                totalWeightedVictories+=needed*winrate;
                totalBattles+=needed;
            }
        }

        if(totalBattles<requiredBattles){
            for(int lowerTier=TOURNAMENT_TIER-1;lowerTier>=5;lowerTier--){
                if(totalBattles>=requiredBattles)break;

                int tierDifference=TOURNAMENT_TIER-lowerTier;
                double penaltyFactor=switch(tierDifference){
                    case 1->0.95;
                    case 2->0.85;
                    case 3->0.70;
                    case 4->0.50;
                    case 5->0.25;
                    default->0.0;
                };
                
                Interfaces.TankStats stats=battleData.get(lowerTier);
                if(stats!=null){
                    int battles=stats.battles();
                    double winrate=(battles>0)?UtilityClass.getDivision(stats.wins(),battles):0;

                    int needed=Math.min(battles,requiredBattles-totalBattles);
                    totalWeightedVictories+=needed*winrate*penaltyFactor;
                    totalBattles+=needed;
                }
            }
        }

        if(totalBattles<requiredBattles){
            totalWeightedVictories+=(requiredBattles-totalBattles)*0;
            totalBattles=requiredBattles;
        }
        
        return (totalBattles>0)?UtilityClass.getOverallWinrate((int)totalWeightedVictories,totalBattles):0.0;
    }
}