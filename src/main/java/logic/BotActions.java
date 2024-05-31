package logic;

import dbconnection.DbConnection;
import dbconnection.GetData;
import dbconnection.InsertData;
import mvc.Mvc1;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.sql.PreparedStatement;

import com.google.gson.JsonObject;

/**
 * 
 * @author erick
 */
public class BotActions{
    protected BotActions(){}

    /**
     * Gets team tournament win rate.<br>
     * This win rate can be see on WoTBlitz official webpage.
     * @param clantag clantag of the team.
     * @param server server where are from the team.
     * @return tournament win rate.
     */
    public static double getTeamWinrate(String clantag,String server){
        try(var cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select wotb_id from team where clantag=? and realm=?");
        PreparedStatement ps2=cn.prepareStatement("select wotb_id, tank_tier, sum(battles) as battles, sum(wins) as wins from tank_stats where wotb_id=?")){
            ps.setString(1,clantag);
            ps.setString(2,server);

            double value=0.0;
            List<Double> winrates=new ArrayList<>();

            ResultSet rs=ps.executeQuery();
            while(rs.next()){
                int id=rs.getInt("wotb_id");
                JsonHandler.dataManipulation(id);
                JsonHandler.updatePlayerNickname(id);
                ps2.setInt(1,id);
                ResultSet rs2=ps2.executeQuery();
                while(rs2.next()){
                    int battles=rs2.getInt("battles");
                    if(battles>1000){
                        double winrate=UtilityClass.getOverallWinrate(rs2.getInt("wins"),battles);
                        winrates.add(winrate);
                    }
                }
            }

            Collections.sort(winrates,Collections.reverseOrder());

            for(int i=0;i<Math.min(7,winrates.size());i++){
                value+=winrates.get(i);
            }

            return UtilityClass.getFormattedDouble(value/7);
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
            return 0.0;
        }
    }
    
    /**
     * Register a player into the database from the API.
     * @param discordId ID retrieved by JDA.
     * @param nickname of the player to request to the API.
     * @param server where the user is located.
     * @return returns a string if a player is registered into the database or not.
    */
    public static String registerPlayer(String discordId,String nickname,String server){
        JsonObject json=JsonHandler.getAccountData(nickname);
        
        Mvc1 mvc=new Mvc1();
        
        int wotbId=json.get("account_id").getAsInt();
        mvc.setDiscordId(discordId);
        mvc.setWotbId(wotbId);
        mvc.setWotbName(json.get("nickname").getAsString());
        mvc.setServer(server);

        if(!GetData.existUser(mvc.getWotbId())){
            InsertData.setUserData(mvc);
            JsonHandler.getAccTankData(wotbId);
            return "Player registered";
        }else{
            return "Player is already registered";
        }
    }

    /**
     * @param clantag
     * @param wotbId
     * @param nickname
     * @param realm
     */
    public static void teamRegistration(String clantag,int wotbId,String nickname,String realm){
        try(PreparedStatement ps=DbConnection.getConnection().prepareStatement("insert into team values(?,?,?,?)")){
            ps.setString(1,clantag);
            ps.setInt(2,wotbId);
            ps.setString(3,nickname);
            ps.setString(4,realm);
            ps.addBatch();
            ps.executeBatch();
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
        }
    }

    /**
     * @param nickname
     * @return
     */
    public static double getPersonalTier10Stats(String nickname){
        try (var cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select wotb_id from user_data where wotb_name=? union select wotb_id from team where wotb_name=?");
        PreparedStatement ps2=cn.prepareStatement("select sum(wins) as wins, sum(battles) as battles from tank_stats where wotb_id=?")) {
            ps.setString(1,nickname);
            ps.setString(2,nickname);
            ResultSet rs=ps.executeQuery();
            double wins=0.0;
            while(rs.next()){
                int id=rs.getInt("wotb_id");
                JsonHandler.getAccTankData(id);
                JsonHandler.updatePlayerNickname(id);
                ps2.setInt(1,id);
                ResultSet rs2=ps2.executeQuery();
                if(rs2.next()){
                    wins=UtilityClass.getOverallWinrate(rs2.getInt("wins"),rs2.getInt("battles"));
                }
            }
            return wins;
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
            return 0.0;
        }
    }

    /**
     * 
     * @param clantag
     * @param realm
     * @return
    */
    public static String getRoster(String clantag,String realm){
        try(PreparedStatement ps=DbConnection.getConnection().prepareStatement("select wotb_id,wotb_name from team where clantag=? and realm=?")){
            ps.setString(1,clantag);
            ps.setString(2,realm);
            ResultSet rs=ps.executeQuery();

            StringBuilder value=new StringBuilder();
            List<Player> players=new ArrayList<>();
            int count=1;

            while(rs.next()){
                String val=rs.getString("wotb_name");
                JsonHandler.updatePlayerNickname(rs.getInt("wotb_id"));

                players.add(new Player(val,getPersonalTier10Stats(val)));
            }

            Collections.sort(players,Comparator.comparingDouble(player->-player.winrate));

            for (int i=0;i<players.size();i++){
                Player player=players.get(i);
                value.append(count).append(". ").append(player).append("\n");
                count++;
                if(count==8){
                    value.append("\n");
                }
            }

            return value.toString();
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
            return "No data";
        }
    }

    public static class Player{
        String name;
        double winrate;

        Player(String name,double winrate){
            this.name=name;
            this.winrate=winrate;
        }

        @Override
        public String toString() {
            return name+" ("+String.format("%.2f", winrate)+"%)";
        }
    }
}