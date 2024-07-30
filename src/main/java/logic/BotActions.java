package logic;

import dbconnection.DbConnection;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

import java.util.logging.Level;

/**
 * 
 * @author erick
 */
public class BotActions{
    /**
     * @param clantag
     * @param discordId
     * @param wotbId
     * @param nickname
     * @param realm
     */
    public void teamRegistration(String clantag,String discordId,int wotbId,String nickname,String realm){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("insert into team values(?,?,?,?,?)")){
            ps.setString(1,clantag);
            ps.setString(2,discordId);
            ps.setInt(3,wotbId);
            ps.setString(4,nickname);
            ps.setString(5,realm);
            ps.addBatch();
            ps.executeBatch();
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     * Gets team tournament win rate.<br>
     * This win rate can be see on WoTBlitz official webpage.
     * @param clantag clantag of the team.
     * @param server server where are from the team.
     * @return tournament win rate.
     */
    public double getTeamWinrate(String clantag,String server){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("select wotb_name from team where clantag=? and realm=?")){
            double value=0.0;
            List<Double> winrates=new ArrayList<>();

            ps.setString(1,clantag);
            ps.setString(2,server);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    winrates.add(getTier10Stats(rs.getString("wotb_name"),server));
                }
            }

            Collections.sort(winrates,Collections.reverseOrder());
            int count=Math.min(7,winrates.size());
            for(int i=0;i<count;i++){
                value+=winrates.get(i);
            }

            return count==0?0.0:UtilityClass.getFormattedDouble(value/count);
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return 0.0;
        }
    }

    /**
     * 
     * @param clantag
     * @param realm
     * @return
    */
    public String getRoster(String clantag,String realm){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("select wotb_name from team where clantag=? and realm=?")){
            StringBuilder value=new StringBuilder();
            List<Player> players=new ArrayList<>();
            int count=1;
            
            ps.setString(1,clantag);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    String val=rs.getString("wotb_name");
                    players.add(new Player(val,getTier10Stats(val,realm)));
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
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return "No data";
        }
    }
    
    /**
     * @param nickname
     * @param realm
     * @return
     */
    public double getTier10Stats(String nickname,String realm){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("SELECT t.wotb_id, SUM(ts.wins) AS wins, SUM(ts.battles) AS battles FROM tank_stats ts JOIN team t ON ts.wotb_id = t.wotb_id WHERE t.wotb_name = ? AND t.realm = ? GROUP BY t.wotb_id;")){
            double wins=0.0;

            ps.setString(1,nickname);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    new JsonHandler().dataManipulation(rs.getInt("wotb_id"));
                    int val=rs.getInt("battles");
                    if(val>=1000){
                        wins=UtilityClass.getOverallWinrate(rs.getInt("wins"),val);
                    }else{
                        wins=0.0;
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
     * @param nickname
     * @param realm
     * @return
     */
    public double getPersonalTier10Stats(String nickname,String realm){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("SELECT t.wotb_id, SUM(ts.wins) AS wins, SUM(ts.battles) AS battles FROM tank_stats ts JOIN team t ON ts.wotb_id = t.wotb_id WHERE t.wotb_name = ? AND t.realm = ? GROUP BY t.wotb_id;")){
            double wins=0.0;

            ps.setString(1,nickname);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                new JsonHandler().dataManipulation(rs.getInt("wotb_id"));
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

    public String checkPlayer(String nickname,String realm){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("select clantag from team where wotb_name=? and realm=?")){
            String val="";
            ps.setString(1,nickname);
            ps.setString(2,realm);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    double stats=getTier10Stats(nickname,realm);
                    if(stats!=0.0){
                        val="("+realm+") - ["+rs.getString("clantag")+"] "+nickname+" ("+stats+"%)";
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