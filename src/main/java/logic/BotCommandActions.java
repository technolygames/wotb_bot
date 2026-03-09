package logic;

import dbconnection.DbConnection;
import dbconnection.GetData;
import dbconnection.InsertData;
import interfaces.Interfaces;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
    public static List<Command.Choice> clanChoices=new ArrayList<>();
    public static List<Command.Choice> tournamentChoices=new ArrayList<>();
    public static List<Command.Choice> playerChoices=new ArrayList<>();
    public static List<Command.Choice> tbPlayerChoices=new ArrayList<>();
    public static List<Command.Choice> notNullPlayerChoices=new ArrayList<>();
    public static List<Command.Choice> clanlessPlayerChoices=new ArrayList<>();
    public static List<Command.Choice> ingameTeamChoices=new ArrayList<>();
    
    private final UtilityClass uc=new UtilityClass();
    private final JsonHandler jh=new JsonHandler();
    private final BotActions ba=new BotActions();
    private final GetData gd=new GetData();
    private final InsertData id=new InsertData();
    
    /**
     * @param value
     * @param realm
     * @return
     */
    public Interfaces.UserData2 checkDiscordInput(String value,String realm){
        String regex="\\d+";
        String dash="/";
        
        Interfaces.UserData2 data=null;
        String value2=value.trim();
        switch(value2){
            case String s when s.contains(dash)->{
                String[] parts=s.split(dash);
                if(parts.length==2&&parts[1].matches(regex)){
                    data=jh.getAccountData(Integer.parseInt(parts[1]),realm);
                }
            }
            case String s when s.matches(regex)->{
                data=jh.getAccountData(Integer.parseInt(s),realm);
            }
            default->{
                data=jh.getAccountData(value2,realm);
            }
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
            Interfaces.TankStats stats=gd.getPlayerStats(accId);
            Interfaces.UserData3 player=gd.getPlayerFuncData().get(accId);
            
            long lbt=player.lastBattleTime();
            String nickname=player.nickname();

            int wins=stats.wins();
            int battles=stats.battles();
            eb.setColor(Color.GREEN).
                    addField("Player:",nickname+"/"+accId,false).
                    addField("Tier 10 win rate:",UtilityClass.getOverallWinrate(wins,battles)+"%",false).
                    addField("Battles:",String.valueOf(battles),false).
                    addField("Wins:",String.valueOf(wins),false).
                    addField("Last battle played:",String.valueOf(UtilityClass.getFormattedDate(lbt)),false);
        }catch(Exception e){
            eb.setColor(Color.RED).addField(BotLogic.MESSAGE_4,"Internal failure",false);
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return eb.build();
    }
    
    public MessageEmbed getOverallGoalAccWinrate(long accId,double goalWinrate){
        DecimalFormat df=new DecimalFormat("00.00000");
        EmbedBuilder eb=new EmbedBuilder();
        try{
            if(UtilityClass.isValidLocalizedDecimal(String.valueOf(goalWinrate))){
                Interfaces.TankStats stats=gd.getPlayerStats(accId);
                int battles=stats.battles();
                int wins=stats.wins();

                double actualWr=UtilityClass.getDivision(wins,battles);
                
                double gain=UtilityClass.getDivision((wins+1),(battles+1))*100.0;
                double current=actualWr*100.0;
                double gainPerWin=gain-current;
                
                int goalBattles=(gainPerWin>0)?(int)Math.ceil(0.01/gainPerWin):0;

                double goal=UtilityClass.getDivision(goalWinrate,(int)100.0);
                int target=(int) Math.ceil(UtilityClass.getDivision((battles*goal-wins),(int)(1.0-goal)));

                String targetDisplay=(target<=0)?"Target reached":UtilityClass.tersin(target);
                
                eb.setColor(Color.GREEN).
                        addField("Player:",gd.checkPlayerByID(accId)+"/"+accId,false).
                        addField("Current win rate (in tier 10):",UtilityClass.getOverallWinrate(wins,battles)+"%",false).
                        addField("Gain per win:",df.format(gainPerWin)+"%",false).
                        addField("To +00.01%, you need to win (in tier 10):",UtilityClass.tersin(goalBattles),false).
                        addField("To reach "+goalWinrate+"%, you need to win:",targetDisplay,false);
            }else{
                eb.setColor(Color.YELLOW).addField(BotLogic.MESSAGE_4,"Write a valid target win rate (i.e. 60.10, without percentage symbol).",false);
            }
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
        Interfaces.TankStats data=gd.getPlayerStats(accId);
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select nickname,realm,last_battle_time,updated_at from user_data where wotb_id=?");
                PreparedStatement ps2=cn.prepareStatement("select sum(tb.battles) as battles,sum(tb.wins) as wins,td.tank_tier from thousand_battles tb join tank_data td on td.tank_id=tb.tank_id where tb.wotb_id=? and td.tank_tier between 5 and 9 group by td.tank_tier order by td.tank_tier asc")){
            ps.setLong(1,accId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    int battles=data.battles();
                    double stats=UtilityClass.getOverallWinrate(data.wins(),battles);
                    eb.setColor(Color.GREEN);
                    eb.addField("Nickname:",rs.getString("nickname"),false);
                    eb.addField("Player ID:",String.valueOf(accId),false);
                    eb.addField("Server:",rs.getString("realm"),false);
                    if(battles<UtilityClass.MAX_BATTLE_COUNT){
                        List<Long> val=Arrays.asList(accId);
                        Interfaces.TankStats batchTier10Stats=gd.getPlayerStatsInBatch(val).get(accId);
                        Map<Integer,Interfaces.TankStats> batchLowerTierStats=gd.getLowerTierStatsInBatch(val).get(accId);
                        eb.addField("Seeding (2500):",ba.calculatePlayerWeightWithPrefetchedStats(batchTier10Stats,batchLowerTierStats,UtilityClass.MAX_BATTLE_COUNT)+"%",false);
                        eb.addField("Seeding (2000):",ba.calculatePlayerWeightWithPrefetchedStats(batchTier10Stats,batchLowerTierStats,UtilityClass.MIN_BATTLE_COUNT)+"%",false);
                        ps2.setLong(1,accId);
                        try(ResultSet rs2=ps2.executeQuery()){
                            while(rs2.next()){
                                eb.addField("Tier "+rs2.getInt("tank_tier")+" stats:",UtilityClass.getOverallWinrate(rs2.getInt("wins"),rs2.getInt("battles"))+"%",false);
                            }
                        }
                    }
                    eb.addField("Tier 10 stats:",stats+"%",false);
                    eb.addField("Last battle played:",UtilityClass.getFormattedDate(rs.getLong("last_battle_time")),false);
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
     * @return
     */
    public List<MessageEmbed> seedTeams(int tourneyId){
        EmbedBuilder eb=new EmbedBuilder();
        List<MessageEmbed> embedsToSend=new ArrayList<>();
        try{
            Map<String,List<Interfaces.LeaderboardEntry>> teamsData=ba.seedTeams(tourneyId);

            if(teamsData.isEmpty()){
                eb.setColor(Color.ORANGE).setDescription("No teams were found.");
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
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return embedsToSend;
    }
    
    /**
     */
    public void refreshCaches(){
        clanChoices=getClanAsChoices();
        tournamentChoices=getTournamentsAsChoices();
        playerChoices=getPlayersAsChoices();
        tbPlayerChoices=getThousandBattlesPlayerAsChoice();
        notNullPlayerChoices=getNotNullPlayersAsChoices();
        clanlessPlayerChoices=getClanlessPlayersAsChoices();
        ingameTeamChoices=getIngameTeamAsChoice();
    }
    
    public MessageEmbed createTeam(Interfaces.UserData2 data,Interfaces.ClanData2 data2,String callerId){
        EmbedBuilder eb=new EmbedBuilder();
        try{
            long accId=data.accId();
            String player=data.nickname();
            long lastBattleTime=data.lastBattleTime();
            long updatedAt=data.updatedAt();

            int clanId=data2.clanId();
            String clantag=data2.clantag();
            String realm=data2.realm();
            long updatedAt2=data2.updatedAt();

            if(accId!=0&&clanId!=0){
                boolean userExists=gd.checkUserData(accId);
                boolean clanExists=gd.checkClanData(clanId);
                boolean teammateExists=gd.checkTeamPlayer(accId,clanId);

                if(!userExists){
                    id.registerPlayer(accId,player,realm,lastBattleTime,updatedAt);
                }
                if(!clanExists){
                    id.setClanInfo(clanId,clantag,realm,updatedAt2);
                }
                if(!teammateExists){
                    id.teamRegistration(clanId,accId,callerId,realm);
                    eb.setColor(Color.GREEN)
                            .addField(BotLogic.MESSAGE_5,gd.checkPlayerByID(accId)+" added to the "+clantag+"'s roster",false)
                            .addField("Success",clantag+BotLogic.MESSAGE_8,false);
                }else{
                    eb.setColor(Color.YELLOW).addField(BotLogic.MESSAGE_4,gd.checkPlayerByID(accId)+" is already on the team",false);
                }
            }else{
                eb.setColor(Color.YELLOW).addField(BotLogic.MESSAGE_4,BotLogic.MESSAGE_6,false);
            }
        }catch(Exception e){
            eb.setColor(Color.RED).addField(BotLogic.MESSAGE_4,BotLogic.MESSAGE_7,false);
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return eb.build();
    }

    public MessageEmbed playerWeight(long accId,int requiredBattles){
        EmbedBuilder eb=new EmbedBuilder();
        try{
            if(accId!=0){
                int battles=(requiredBattles==0)?UtilityClass.MAX_BATTLE_COUNT:requiredBattles;
                List<Long> val=Arrays.asList(accId);
                Interfaces.TankStats batchTier10Stats=gd.getPlayerStatsInBatch(val).get(accId);
                Map<Integer,Interfaces.TankStats> batchLowerTierStats=gd.getLowerTierStatsInBatch(val).get(accId);
                eb.setColor(Color.GREEN).
                        addField("Player:",gd.checkPlayerByID(accId)+"/"+accId,false).
                        addField("Win rate using Wargaming's tournament formula:",ba.calculatePlayerWeightWithPrefetchedStats(batchTier10Stats,batchLowerTierStats,battles)+"%",false).
                        addField("Control number of tier 10 battles:",String.valueOf(battles),false);
            }else{
                eb.setColor(Color.YELLOW).addField(BotLogic.MESSAGE_4,BotLogic.MESSAGE_6,false);
            }
        }catch(Exception e){
            eb.setColor(Color.RED).addField(BotLogic.MESSAGE_4,BotLogic.MESSAGE_7,false);
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return eb.build();
    }

    public MessageEmbed teamLeaderboard(List<Interfaces.LeaderboardEntry> teams){
        EmbedBuilder eb=new EmbedBuilder();
        try{
            eb.setTitle(BotLogic.MESSAGE_5).setColor(Color.GREEN);

            if(teams.isEmpty()){
                eb.setDescription("No teams were found.");
            }else{
                StringBuilder fieldValue=new StringBuilder();
                int fieldCount=1;
                int rank=1;

                for(Interfaces.LeaderboardEntry team:teams){
                    String line=String.format("%d. %s\n",rank,team.toString());

                    if(fieldValue.length()+line.length()>1024){
                        eb.addField("Position from "+(rank-fieldValue.toString().lines().count())+" to "+(rank-1),fieldValue.toString(),false);
                        fieldValue.setLength(0);
                        fieldCount++;
                    }

                    if(fieldCount>25)break;

                    fieldValue.append(line);
                    rank++;
                }

                if(fieldValue.length()>0){
                    eb.addField("Position from "+(rank-fieldValue.toString().lines().count())+" to "+(rank-1),fieldValue.toString(),false);
                }
            }
        }catch(Exception e){
            eb.setColor(Color.YELLOW).addField(BotLogic.MESSAGE_4,BotLogic.MESSAGE_6,false);
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return eb.build();
    }
    
    /**
     * @return
     */
    protected List<Command.Choice> getIngameTeamAsChoice(){
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
    protected List<Command.Choice> getClanAsChoices(){
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
    protected List<Command.Choice> getTournamentsAsChoices(){
        List<Command.Choice> choices=new ArrayList<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select tournament_id,title,end_at,realm from tournament_data");
                ResultSet rs=ps.executeQuery()){
            while(rs.next()){
                choices.add(new Command.Choice(rs.getString("title")+" - ("+UtilityClass.getFormattedDate(rs.getLong("end_at"))+") - ("+rs.getString("realm")+")",rs.getString("tournament_id")));
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return choices;
    }

    /**
     * @return
     */
    protected List<Command.Choice> getThousandBattlesPlayerAsChoice(){
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
    protected List<Command.Choice> getClanlessPlayersAsChoices(){
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
    protected List<Command.Choice> getPlayersAsChoices(){
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
                PreparedStatement ps=cn.prepareStatement("select t.clan_id,ud.nickname,ud.wotb_id,ud.realm from team t join user_data ud on ud.wotb_id=t.wotb_id");
                ResultSet rs=ps.executeQuery()){
            while(rs.next()){
                String clantag=gd.checkClantagByID(rs.getInt("clan_id"));
                if(clantag!=null){
                    choices.add(new Command.Choice(rs.getString("nickname")+" - ("+rs.getString("realm")+")",rs.getString("wotb_id")));
                }
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return choices;
    }
}