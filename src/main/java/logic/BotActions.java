package logic;

import dbconnection.DbConnection;
import dbconnection.GetData;
import java.awt.Color;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import java.util.logging.Level;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.Command;

/**
 * @author erick
 */
public class BotActions{
    /**
     * Gets team tournament win rate.<br>
     * This win rate can be see on WoTBlitz official webpage.
     * @param clanId clantag of the team.
     * @param server server where are from the team.
     * @return tournament win rate.
     */
    public MessageEmbed getTeamWinrate(int clanId,String server){
        EmbedBuilder eb=new EmbedBuilder();
        double val=teamWinrateStats(clanId,server);
        if(val!=0.0){
            eb.setColor(Color.GREEN).
            setTitle("Your team win rate is:").
            addField("Original: ",val+"%",false).
            addField("Wargaming's page:",Math.round(val)+"%",false);
        }else{
            eb.setColor(Color.YELLOW).
            addField("Something gone wrong","No data",false);
        }
        return eb.build();
    }

    /**
     * @param clanId
     * @param realm
     * @return
     */
    public MessageEmbed getRoster(int clanId,String realm){
        EmbedBuilder eb=new EmbedBuilder();
        double val=teamWinrateStats(clanId,realm);
        String team=getTeamRoster(clanId,realm);
        if(!team.equals("No data")){
            eb.setColor(Color.GREEN).
            addField("Original:",val+"%",false).
            addField("Wargaming's page:",Math.round(val)+"%",false).
            addField("Roster:",team,false);
        }else{
            eb.setColor(Color.YELLOW).
            addField("Something gone wrong",team,false);
        }
        return eb.build();
    }

    /**
     * @param clanId
     * @param realm
     * @return
    */
    public String getTeamRoster(int clanId,String realm){
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select t.wotb_id,ud.nickname from team t join user_data ud on t.wotb_id=ud.wotb_id where t.clan_id=? and t.realm=?")){
            StringBuilder value=new StringBuilder();
            List<Player> players=new ArrayList<>();
            int count=1;
            if(clanId!=0){
                ps.setInt(1,clanId);
                ps.setString(2,realm);
                try(ResultSet rs=ps.executeQuery()){
                    while(rs.next()){
                        String val=rs.getString("nickname");
                        players.add(new Player(val,getThousandBattlesTier10Stats(rs.getInt("wotb_id"))));
                    }
                }

                Collections.sort(players,Comparator.comparingDouble(player->-player.winrate));

                for(int i=0;i<players.size();i++){
                    Player player=players.get(i);
                    value.append(count).append(". ").append(player).append("\n");
                    count++;
                    if(count==8){
                        value.append("\n");
                    }
                }

                return value.toString();
            }else{
                return "No data";
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return "No data";
        }
    }
    
    /**
     * @param clanId
     * @param server
     * @return
     */
    private double teamWinrateStats(int clanId,String server){
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select wotb_id from team where clan_id=? and realm=?")){
            double value=0.0;
            List<Double> winrates=new ArrayList<>();
            if(clanId!=0){
                ps.setInt(1,clanId);
                ps.setString(2,server);
                try(ResultSet rs=ps.executeQuery()){
                    while(rs.next()){
                        winrates.add(getThousandBattlesTier10Stats(rs.getInt("wotb_id")));
                    }
                }

                Collections.sort(winrates,Collections.reverseOrder());
                int count=Math.min(7,winrates.size());
                for(int i=0;i<count;i++){
                    value+=winrates.get(i);
                }

                return UtilityClass.getFormattedDouble(value/count);
            }else{
                return 0.0;
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return 0.0;
        }
    }

    /**
     * @param accId
     * @return
     */
    public double getThousandBattlesTier10Stats(int accId){
        new JsonHandler().dataManipulation(accId);
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select sum(wins) as wins, sum(battles) as battles from tank_stats where wotb_id=?")){
            double wins=0.0;
            ps.setInt(1,accId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    int val=rs.getInt("battles");
                    if(val>=UtilityClass.MAX_BATTLE_COUNT){
                        wins=UtilityClass.getOverallWinrate(rs.getInt("wins"),val);
                    }else{
                        wins=calculatePlayerWeight(accId);
                    }
                }
            }
            return wins;
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return 0.0;
        }
    }
    
    /**
     * @param accId
     * @return
     */
    public double getOverallTier10Stats(int accId){
        new JsonHandler().dataManipulation(accId);
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select sum(wins) as wins, sum(battles) as battles from tank_stats where wotb_id=?")){
            double wins=0.0;

            ps.setInt(1,accId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    wins=UtilityClass.getOverallWinrate(rs.getInt("wins"),rs.getInt("battles"));
                }
            }
            return wins;
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return 0.0;
        }
    }
    
    /**
     * @param accId
     * @return
     */
    public MessageEmbed getTier10Stats(int accId){
        new JsonHandler().dataManipulation(accId);
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select sum(wins) as wins, sum(battles) as battles from tank_stats where wotb_id=?")){
            EmbedBuilder eb=new EmbedBuilder();
            ps.setInt(1,accId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    int wins=rs.getInt("wins");
                    int battles=rs.getInt("battles");
                    eb.setColor(Color.GREEN).
                    addField("Your tier X win rate is:",UtilityClass.getOverallWinrate(wins,battles)+"%",false).
                    addField("Battles:",battles+"",false).
                    addField("Wins:",wins+"",false);
                }
            }
            return eb.build();
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return null;
        }
    }
    
    /**
     * @param accId
     * @return
     */
    public double calculatePlayerWeight(int accId){
        int minTier=10;
        Map<Integer,int[]> battleData=new HashMap<>();
        double[] penalty={1.0,0.95,0.85,0.7,0.5,0.25};
        new JsonHandler().dataManipulation(accId);
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select sum(battles) as battles, sum(wins) as wins from tank_stats where wotb_id=?");
        PreparedStatement ps2=cn.prepareStatement("select sum(tb.battles) as battles, sum(tb.wins) as wins, td.tank_tier from thousand_battles tb join tank_data td on td.tank_id=tb.tank_id where tb.wotb_id=? and td.tank_tier=?")){
            ps.setInt(1,accId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    int dbWins=rs.getInt("wins");
                    int dbBattles=rs.getInt("battles");
                    if(dbBattles>0){
                        battleData.put(10,new int[]{dbBattles,dbWins});
                        minTier=10;
                    }
                }
            }

            for(int i=9;i>5;i--){
                ps2.setInt(1,accId);
                ps2.setInt(2,i);
                try(ResultSet rs2=ps2.executeQuery()){
                    if(rs2.next()){
                        int dbWins=rs2.getInt("wins");
                        int dbBattles=rs2.getInt("battles");
                        int dbTier=rs2.getInt("tank_tier");
                        if(dbBattles>0){
                            battleData.put(dbTier,new int[]{dbBattles,dbWins});
                            minTier=Math.min(minTier,dbTier);
                        }
                    }
                }
            }

            int totalWeightedVictories=0;
            int totalBattles=0;
            int requiredBattles=UtilityClass.MAX_BATTLE_COUNT;
            for(int tier=10;tier>=minTier-5;tier--){
                int tierOffset=Math.max(minTier-tier,0);
                if(battleData.containsKey(tier)){
                    int battles=battleData.get(tier)[0];
                    int wins=battleData.get(tier)[1];
                    double winRate=(double)wins/battles;
                    int battlesUsed=Math.min(requiredBattles-totalBattles,battles);
                    totalWeightedVictories+=penalty[tierOffset]*battlesUsed*winRate;
                    totalBattles+=battlesUsed;
                }
                if(totalBattles>=requiredBattles){
                    break;
                }
            }
            
            if(totalBattles<requiredBattles){
                int remainingBattles=requiredBattles-totalBattles;
                if(remainingBattles>0){
                    totalWeightedVictories+=penalty[Math.max(minTier-5,0)]*remainingBattles*0.0;
                    totalBattles=requiredBattles;
                }
            }

            return UtilityClass.getOverallWinrate(totalWeightedVictories,totalBattles);
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return 0.0;
        }
    }
    
    /**
     * @param accId
     * @return
     */
    public String checkPlayer(int accId){
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select coalesce(t.clan_id,0) as clan_id, ud.nickname, ud.realm as realm from user_data ud left join team t on t.wotb_id=ud.wotb_id where ud.wotb_id=?")){
            String val="";
            ps.setInt(1,accId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    double stats=getOverallTier10Stats(accId);
                    if(stats!=0.0){
                        String realm=rs.getString("realm");
                        int clanId=rs.getInt("clan_id");
                        if(clanId!=0){
                            val="("+realm+") - ["+new GetData().checkClantagByID(clanId,realm)+"] "+rs.getString("nickname")+" ("+stats+"%)";
                        }else{
                            val="("+realm+") - [Clanless] "+rs.getString("nickname")+" ("+stats+"%)";
                        }
                    }
                }else{
                    val="No data";
                }
            }
            return val;
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return null;
        }
    }

    /**
     * @return
     */
    public List<Command.Choice> getPlayersAsChoices(){
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select nickname,wotb_id,realm from user_data")){
            List<Command.Choice> choices=new ArrayList<>();
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    choices.add(new Command.Choice(rs.getString("nickname")+" - ("+rs.getString("realm")+")",rs.getString("wotb_id")));
                }
            }
            return choices;
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return new ArrayList<>();
        }
    }

    /**
     * @return
     */
    public List<Command.Choice> getThousandBattlesPlayerAsChoice(){
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select sum(ts.battles) as battles, ud.wotb_id, ud.realm, ud.nickname from user_data ud join tank_stats ts on ts.wotb_id=ud.wotb_id group by ud.wotb_id")){
            List<Command.Choice> choices=new ArrayList<>();
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    if(rs.getInt("battles")<UtilityClass.MAX_BATTLE_COUNT){
                        choices.add(new Command.Choice(rs.getString("nickname")+" - ("+rs.getString("realm")+")",rs.getString("wotb_id")));
                    }
                }
            }
            return choices;
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return new ArrayList<>();
        }
    }

    /**
     * @return
     */
    public List<Command.Choice> getClanAsChoices(){
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select * from clan_data")){
            List<Command.Choice> choices=new ArrayList<>();
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    choices.add(new Command.Choice(rs.getString("clantag")+" - ("+rs.getString("realm")+")",rs.getString("clan_id")));
                }
            }
            return choices;
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return new ArrayList<>();
        }
    }

    /**
     * @return
     */
    public List<Command.Choice> getNotNullPlayersAsChoices(){
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select t.clan_id,ud.nickname,ud.wotb_id,ud.realm from team t join user_data ud on t.wotb_id=ud.wotb_id")){
            List<Command.Choice> choices=new ArrayList<>();
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    String realm=rs.getString("realm");
                    String clantag=new GetData().checkClantagByID(rs.getInt("clan_id"),realm);
                    if(clantag!=null){
                        choices.add(new Command.Choice(rs.getString("nickname")+" - ("+realm+")",rs.getString("wotb_id")));
                    }
                }
            }
            return choices;
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return new ArrayList<>();
        }
    }
    
    /**
     * @return
     */
    public List<Command.Choice> getClanlessPlayersAsChoices(){
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select ud.nickname,ud.wotb_id,ud.realm from user_data ud left join team t on ud.wotb_id=t.wotb_id where t.wotb_id is null")){
            List<Command.Choice> choices=new ArrayList<>();
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    choices.add(new Command.Choice(rs.getString("nickname")+" - ("+rs.getString("realm")+")",rs.getString("wotb_id")));
                }
            }
            return choices;
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return new ArrayList<>();
        }
    }

    private static class Player{
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
}