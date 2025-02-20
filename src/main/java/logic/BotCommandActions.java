package logic;

import dbconnection.DbConnection;
import dbconnection.GetData;
import mvc.Mvc3;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import java.util.logging.Level;

/**
 * @author erick
 */
public class BotCommandActions{
    /**
     * @param accId
     * @return
     */
    public MessageEmbed getTier10Stats(int accId){
        EmbedBuilder eb=new EmbedBuilder();
        try{
            Mvc3 data=new GetData().getPlayerStats(accId);
            int wins=data.getWins();
            int battles=data.getBattles();
            eb.setColor(Color.GREEN).
                    addField("Your tier X win rate is:",UtilityClass.getOverallWinrate(wins,battles)+"%",false).
                    addField("Battles:",String.valueOf(battles),false).
                    addField("Wins:",String.valueOf(wins),false);
        }catch(Exception e){
            eb.setColor(Color.RED).addField(BotLogic.EventListeners.MESSAGE_4,"Internal failure",false);
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        return eb.build();
    }

    /**
     * @return
     */
    public List<Command.Choice> getClanAsChoices(){
        List<Command.Choice> choices=new ArrayList<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select distinct cd.clantag,cd.clan_id from clan_data cd join team t on cd.clan_id=t.clan_id")){
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    choices.add(new Command.Choice(rs.getString("clantag"),rs.getString("clan_id")));
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        return choices;
    }

    /**
     * @return
     */
    public List<Command.Choice> getThousandBattlesPlayerAsChoice(){
        List<Command.Choice> choices=new ArrayList<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select sum(ts.battles) as battles,ud.wotb_id,ud.nickname from user_data ud join tank_stats ts on ts.wotb_id=ud.wotb_id group by ud.wotb_id")){
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    if(rs.getInt("battles")<UtilityClass.MAX_BATTLE_COUNT){
                        choices.add(new Command.Choice(rs.getString("nickname"),rs.getString("wotb_id")));
                    }
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        return choices;
    }

    /**
     * @return
     */
    public List<Command.Choice> getClanlessPlayersAsChoices(){
        List<Command.Choice> choices=new ArrayList<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select ud.nickname,ud.wotb_id from user_data ud left join team t on ud.wotb_id=t.wotb_id where t.wotb_id is null")){
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    choices.add(new Command.Choice(rs.getString("nickname"),rs.getString("wotb_id")));
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        return choices;
    }

    /**
     * @return
     */
    public List<Command.Choice> getPlayersAsChoices(){
        List<Command.Choice> choices=new ArrayList<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select nickname,wotb_id from user_data")){
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    choices.add(new Command.Choice(rs.getString("nickname"),rs.getString("wotb_id")));
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        return choices;
    }

    /**
     * @return
     */
    public List<Command.Choice> getNotNullPlayersAsChoices(){
        List<Command.Choice> choices=new ArrayList<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select t.clan_id,ud.nickname,ud.wotb_id,ud.realm from user_data ud join team t on t.wotb_id=ud.wotb_id")){
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    String realm=rs.getString("realm");
                    String clantag=new GetData().checkClantagByID(rs.getInt("clan_id"),realm);
                    if(clantag!=null){
                        choices.add(new Command.Choice(rs.getString("nickname"),rs.getString("wotb_id")));
                    }
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
        return choices;
    }
}