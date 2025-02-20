package logic;

import dbconnection.DbConnection;
import dbconnection.GetData;
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
import java.util.StringJoiner;

import java.util.logging.Level;

/**
 * @author erick
 */
public class BotActions{
    /**
     * @param realm
     * @return
     */
    public String teamLeaderboard(String realm){
        StringJoiner sb=new StringJoiner("\n");
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select distinct cd.clan_id,cd.clantag from clan_data cd join team t on cd.clan_id=t.clan_id where cd.realm=?")){
            List<Team> teams=new ArrayList<>();
            int count=1;

            ps.setString(1,realm);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    teams.add(new Team(rs.getString("clantag"),getTeamWinrate(rs.getInt("clan_id"),realm)));
                }
            }

            Collections.sort(teams,Comparator.comparingDouble(team->-team.winrate));
            for(Team team:teams){
                if(team!=null){
                    sb.add(count+". "+team);
                    count++;
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        return sb.toString();
    }

    /**
     * @param clanId
     * @param realm
     * @return
    */
    public String getTeamRoster(int clanId,String realm){
        StringBuilder value=new StringBuilder();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select t.wotb_id,ud.nickname from team t join user_data ud on t.wotb_id=ud.wotb_id where t.clan_id=? and t.realm=?")){
            List<Player> players=new ArrayList<>();
            int count=1;
            
            ps.setInt(1,clanId);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    players.add(new Player(rs.getString("nickname"),getThousandBattlesTier10Stats(rs.getInt("wotb_id"))));
                }
            }

            Collections.sort(players,Comparator.comparingDouble(player->-player.winrate));
            for(Player player:players){
                value.append(count).append(". ").append(player).append("\n");
                count++;
                if(count==8){
                    value.append("\n");
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        return value.toString();
    }
    
    /**
     * @param clanId
     * @param server
     * @return
     */
    public double getTeamWinrate(int clanId,String server){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select wotb_id from team where clan_id=? and realm=?")){
            List<Double> winrates=new ArrayList<>();
            int count=0;
            double value=0.0;

            ps.setInt(1,clanId);
            ps.setString(2,server);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    winrates.add(getThousandBattlesTier10Stats(rs.getInt("wotb_id")));
                }
            }

            Collections.sort(winrates,Collections.reverseOrder());
            for(Double winrate:winrates){
                if(count==7){
                    break;
                }
                value+=winrate;
                count++;
            }

            return value==0?0.0:UtilityClass.getFormattedDouble(value/count);
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return 0.0;
        }
    }

    /**
     * @param accId
     * @return
     */
    protected double getThousandBattlesTier10Stats(int accId){
        Mvc3 data=new GetData().getPlayerStats(accId);
        int battles=data.getBattles();
        if(battles>=UtilityClass.MAX_BATTLE_COUNT){
            return UtilityClass.getOverallWinrate(data.getWins(),battles);
        }else{
            return calculatePlayerWeight(accId);
        }
    }

    /**
     * @param accId
     * @return
     */
    public double calculatePlayerWeight(int accId){
        Map<Integer,int[]> battleData=new HashMap<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select sum(tb.battles) as battles, sum(tb.wins) as wins, td.tank_tier from thousand_battles tb join tank_data td on td.tank_id=tb.tank_id where tb.wotb_id=? and td.tank_tier=?")){
            Mvc3 data=new GetData().getPlayerStats(accId);
            battleData.put(10,new int[]{data.getBattles(),data.getWins()});

            ps.setInt(1,accId);
            for(int i=9;i>=5;i--){
                ps.setInt(2,i);
                try(ResultSet rs=ps.executeQuery()){
                    if(rs.next()){
                        int dbWins=rs.getInt("wins");
                        int dbBattles=rs.getInt("battles");
                        int dbTier=rs.getInt("tank_tier");
                        battleData.put(dbTier,new int[]{dbBattles,dbWins});
                    }
                }
            }

            int minTier=10;
            int totalBattles=0;
            int totalWeightedVictories=0;
            int requiredBattles=UtilityClass.MAX_BATTLE_COUNT;
            double[] penalty={1.0,0.95,0.85,0.7,0.5,0.25};
            for(int tier=10;tier>=minTier-5;tier--){
                int tierOffset=Math.max(minTier-tier,0);
                if(battleData.containsKey(tier)){
                    int battles=battleData.get(tier)[0];
                    int wins=battleData.get(tier)[1];
                    double winrate=(battles>0)?(double)wins/battles:0.0;

                    if(totalBattles<requiredBattles){
                        int remainingBattles=requiredBattles-totalBattles;
                        if(battles<=remainingBattles){
                            totalWeightedVictories+=penalty[tierOffset]*battles*winrate;
                            totalBattles+=battles;
                        }else{
                            totalWeightedVictories+=penalty[tierOffset]*remainingBattles*winrate;
                            totalBattles=requiredBattles;
                        }
                    }else{
                        totalWeightedVictories+=penalty[tierOffset]*battles*winrate;
                        totalBattles+=battles;
                        break;
                    }
                }
            }

            if(totalBattles<requiredBattles){
                int remainingBattles=requiredBattles-totalBattles;
                int lowestTier=battleData.keySet().stream().min(Integer::compare).orElse(10);
                int[] lowestTierData=battleData.getOrDefault(lowestTier,new int[]{0,0});
                int lowestTierBattles=lowestTierData[0];
                int lowestTierWins=lowestTierData[1];
                double winrate=(lowestTierBattles>0)?(double)lowestTierWins/lowestTierBattles:0.0;
                double penaltyFactor=penalty[Math.min(Math.max(minTier-lowestTier,0),penalty.length-1)];
                totalWeightedVictories+=penaltyFactor*remainingBattles*winrate;
                totalBattles=requiredBattles;
            }

            return UtilityClass.getOverallWinrate(totalWeightedVictories,totalBattles);
        }catch(SQLException|ArrayIndexOutOfBoundsException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return 0.0;
        }
    }

    /**
     * @param accId
     * @return
     */
    public String checkPlayer(int accId){
        GetData gd=new GetData();
        Mvc3 data=gd.getPlayerStats(accId);
        String val="No data";
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select coalesce(t.clan_id,0) as clan_id, ud.nickname, ud.realm as realm from user_data ud left join team t on t.wotb_id=ud.wotb_id where ud.wotb_id=?")){
            ps.setInt(1,accId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    double stats=UtilityClass.getOverallWinrate(data.getWins(),data.getBattles());
                    if(stats!=0.0){
                        String realm=rs.getString("realm");
                        int clanId=rs.getInt("clan_id");
                        if(clanId!=0){
                            val="("+realm+") - ["+gd.checkClantagByID(clanId,realm)+"] "+rs.getString("nickname")+" ("+stats+"%)";
                        }else{
                            val="("+realm+") - [Clanless] "+rs.getString("nickname")+" ("+stats+"%)";
                        }
                    }
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        return val;
    }

    private class Player{
        String name;
        double winrate;

        Player(String name,double winrate){
            this.name=name;
            this.winrate=winrate;
        }

        @Override
        public String toString(){
            return name+" ("+String.format("%.2f",winrate)+"%)";
        }
    }

    private class Team implements Comparable<Team>{
        String clantag;
        double winrate;

        public Team(String clantag,double winrate){
            this.clantag=clantag;
            this.winrate=winrate;
        }

        @Override
        public int compareTo(Team other){
            return Double.compare(this.winrate,other.winrate);
        }

        @Override
        public String toString(){
            return clantag+" - "+String.format("%d",Math.round(winrate))+"%";
        }
    }
}