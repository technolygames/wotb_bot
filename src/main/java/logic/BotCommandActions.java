package logic;

import dbconnection.DbConnection;
import dbconnection.GetData;
import interfaces.Interfaces;
import mvc.Mvc3;
import mvc.Mvc1;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.logging.Level;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.Command;

/**
 * @author erick
 */
public class BotCommandActions{
    private final UtilityClass uc=new UtilityClass();
    private final JsonHandler jh=new JsonHandler();
    private final BotActions ba=new BotActions();
    private final GetData gd=new GetData();
    
    /**
     * @param value
     * @param realm
     * @return
     */
    public Mvc1 checkDiscordInput(String value,String realm){
        Mvc1 data=new Mvc1();
        String value2=value.trim();
        if(value2.contains("/")){
            String[] parts=value2.split("/");
            if(parts.length==2&&parts[1].matches("\\d+")){
                data=jh.getAccountData(Integer.parseInt(parts[1]),realm);
            }
        }else if(value2.matches("\\d+")){
            data=jh.getAccountData(Integer.parseInt(value2),realm);
        }else{
            data=jh.getAccountData(value2,realm);
        }
        return data;
    }
    
    /**
     * @param accId
     * @return
     */
    public MessageEmbed getTier10Stats(long accId){
        EmbedBuilder eb=new EmbedBuilder();
        try{
            Mvc3 data=gd.getPlayerStats(accId);
            int wins=data.getWins();
            int battles=data.getBattles();
            eb.setColor(Color.GREEN).
                    addField("Your tier X win rate is:",UtilityClass.getOverallWinrate(wins,battles)+"%",false).
                    addField("Battles:",String.valueOf(battles),false).
                    addField("Wins:",String.valueOf(wins),false);
        }catch(Exception e){
            eb.setColor(Color.RED).addField(BotLogic.MESSAGE_4,"Internal failure",false);
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return eb.build();
    }

    /**
     * @param accId
     * @return
     */
    public MessageEmbed checkPlayer(long accId){
        EmbedBuilder eb=new EmbedBuilder();
        Mvc3 data=gd.getPlayerStats(accId);
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select coalesce(t.clan_id,0) as clan_id, ud.nickname, ud.realm as realm from user_data ud left join team t on t.wotb_id=ud.wotb_id where ud.wotb_id=?");
                PreparedStatement ps2=cn.prepareStatement("select sum(tb.battles) as battles,sum(tb.wins) as wins,td.tank_tier from thousand_battles tb join tank_data td on td.tank_id=tb.tank_id where tb.wotb_id=? and td.tank_tier between 5 and 9 group by td.tank_tier")){
            ps.setLong(1,accId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    int battles=data.getBattles();
                    double stats=UtilityClass.getOverallWinrate(data.getWins(),battles);
                    if(stats!=0.0){
                        String nickname=rs.getString("nickname");
                        String realm=rs.getString("realm");
                        int clanId=rs.getInt("clan_id");
                        eb.setColor(Color.GREEN).addField("Nickname:",nickname,false);
                        if(clanId!=0){
                            eb.addField("Clan:","["+gd.checkClantagByID(clanId,realm)+"]",false);
                        }else{
                            eb.addField("Clan:","No clan",false);
                        }
                        eb.addField("Server:",realm,false);
                        if(battles<UtilityClass.MAX_BATTLE_COUNT){
                            eb.addField("Seeding:",ba.calculatePlayerWeight(accId)+"%",false);
                            ps2.setLong(1,accId);
                            try(ResultSet rs2=ps2.executeQuery()){
                                while(rs2.next()){
                                    eb.addField("Tier "+rs2.getInt("tank_tier")+" stats:",UtilityClass.getOverallWinrate(rs2.getInt("wins"),rs2.getInt("battles"))+"%",false);
                                }
                            }
                        }
                        eb.addField("Tier 10 stats:",stats+"%",false);
                    }
                }
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
            eb.setColor(Color.RED).addField(BotLogic.MESSAGE_4,"Internal failure",false);
        }
        return eb.build();
    }

    /**
     * @param tourneyId
     * @param realm
     * @return
     */
    public List<MessageEmbed> seedTeams(int tourneyId,String realm){
        List<MessageEmbed> embedsToSend=new ArrayList<>();
        Map<String,List<Interfaces.LeaderboardEntry>> teamsData=ba.seedTeams(tourneyId,realm);
        EmbedBuilder eb=new EmbedBuilder();

        if(teamsData.isEmpty()){
            eb.setColor(Color.ORANGE).setDescription("No se encontraron equipos para generar los grupos.");
            embedsToSend.add(eb.build());
            return embedsToSend;
        }

        eb.setColor(Color.GREEN);
        int fieldCount=0;

        for(Map.Entry<String,List<Interfaces.LeaderboardEntry>> entry:teamsData.entrySet()){
            String groupName=entry.getKey();
            List<Interfaces.LeaderboardEntry> teamList=entry.getValue();

            StringBuilder groupSb=new StringBuilder();
            for(Interfaces.LeaderboardEntry team:teamList){
                groupSb.append(team).append("\n");
            }

            eb.addField(groupName,groupSb.toString(),false);
            fieldCount++;

            if(fieldCount==25){
                embedsToSend.add(eb.build());
                eb=new EmbedBuilder().setColor(Color.GREEN);
                fieldCount=0;
            }
        }

        if(fieldCount>0){
            embedsToSend.add(eb.build());
        }

        return embedsToSend;
    }
    
    /**
     * @return
     */
    public List<Command.Choice> getIngameTeamAsChoice(){
        List<Command.Choice> choices=new ArrayList<>();
        
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("SELECT itd.team_id, itd.team_name, itd.realm, cd.clantag, td.title, td.end_at FROM ingame_team_data itd LEFT JOIN clan_data cd ON cd.clan_id = itd.clan_id JOIN tournament_data td ON td.tournament_id = itd.tournament_id");
                ResultSet rs=ps.executeQuery()){
            while(rs.next()){
                String clantag=rs.getString("clantag");
                String val;
                String teamName=rs.getString("team_name");
                String realm=rs.getString("realm");
                
                String date=UtilityClass.getFormattedDate(rs.getLong("end_at"));
                String tourneyName=rs.getString("title");
                if(clantag!=null){
                    val=teamName+" - ["+clantag+"] - "+tourneyName+" - ("+date+") - ("+realm+")";
                }else{
                    val=teamName+" - "+tourneyName+" - ("+date+") - ("+realm+")";
                }
                choices.add(new Command.Choice(val,rs.getString("team_id")));
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return choices;
    }

    /**
     * @return
     */
    public List<Command.Choice> getClanAsChoices(){
        List<Command.Choice> choices=new ArrayList<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select distinct cd.clantag,cd.clan_id,cd.realm from clan_data cd join team t on cd.clan_id=t.clan_id");
                ResultSet rs=ps.executeQuery()){
            while(rs.next()){
                choices.add(new Command.Choice(rs.getString("clantag")+" - ("+rs.getString("realm")+")",rs.getString("clan_id")));
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return choices;
    }
    
    /**
     * @return 
     */
    public List<Command.Choice> getTournamentsAsChoices(){
        List<Command.Choice> choices=new ArrayList<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select tournament_id,title,start_at,realm from tournament_data");
                ResultSet rs=ps.executeQuery()){
            while(rs.next()){
                choices.add(new Command.Choice(rs.getString("title")+" - ("+UtilityClass.getFormattedDate(rs.getLong("start_at"))+") - ("+rs.getString("realm")+")",rs.getString("tournament_id")));
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return choices;
    }

    /**
     * @return
     */
    public List<Command.Choice> getThousandBattlesPlayerAsChoice(){
        List<Command.Choice> choices=new ArrayList<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select sum(ts.battles) as battles,ud.wotb_id,ud.nickname,ud.realm from user_data ud join tank_stats ts on ts.wotb_id=ud.wotb_id group by ud.wotb_id");
                ResultSet rs=ps.executeQuery()){
            while(rs.next()){
                if(rs.getInt("battles")<UtilityClass.MAX_BATTLE_COUNT){
                    choices.add(new Command.Choice(rs.getString("nickname")+"- ("+rs.getString("realm")+")",rs.getString("wotb_id")));
                }
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return choices;
    }

    /**
     * @return
     */
    public List<Command.Choice> getClanlessPlayersAsChoices(){
        List<Command.Choice> choices=new ArrayList<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select ud.nickname,ud.wotb_id,ud.realm from user_data ud left join team t on ud.wotb_id=t.wotb_id where t.wotb_id is null");
                ResultSet rs=ps.executeQuery()){
            while(rs.next()){
                choices.add(new Command.Choice(rs.getString("nickname")+" - ("+rs.getString("realm")+")",rs.getString("wotb_id")));
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return choices;
    }

    /**
     * @return
     */
    public List<Command.Choice> getPlayersAsChoices(){
        List<Command.Choice> choices=new ArrayList<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select nickname,wotb_id,realm from user_data");
                ResultSet rs=ps.executeQuery()){
            while(rs.next()){
                choices.add(new Command.Choice(rs.getString("nickname")+" - ("+rs.getString("realm")+")",rs.getString("wotb_id")));
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return choices;
    }

    /**
     * @return
     */
    public List<Command.Choice> getNotNullPlayersAsChoices(){
        List<Command.Choice> choices=new ArrayList<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select t.clan_id,ud.nickname,ud.wotb_id,ud.realm from user_data ud join team t on t.wotb_id=ud.wotb_id");
                ResultSet rs=ps.executeQuery()){
            while(rs.next()){
                String realm=rs.getString("realm");
                String clantag=gd.checkClantagByID(rs.getInt("clan_id"),realm);
                if(clantag!=null){
                    choices.add(new Command.Choice(rs.getString("nickname")+" - ("+realm+")",rs.getString("wotb_id")));
                }
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return choices;
    }
}