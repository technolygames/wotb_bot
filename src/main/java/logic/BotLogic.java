package logic;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import dbconnection.DeleteData;
import dbconnection.GetData;
import io.github.cdimascio.dotenv.Dotenv;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

public class BotLogic{
    private final ShardManager sm;
    public BotLogic(){
        Dotenv token=Dotenv.configure().directory("data").load();
        DefaultShardManagerBuilder builder=DefaultShardManagerBuilder.createDefault(token.get("TOKEN"));
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.playing("Testing"));
        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT,GatewayIntent.DIRECT_MESSAGES);
        sm=builder.build();
        sm.addEventListener(new EventListeners());
    }

    private String mess;
    
    private class EventListeners extends net.dv8tion.jda.api.hooks.ListenerAdapter{
        @Override
        public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent evt){
            switch(evt.getName()){
                case "personal-stats-tier10":{
                    evt.deferReply(false).queue(m->{
                        String player=evt.getOption("player2").getAsString();
                        m.sendMessage(personalStats(player)).addActionRow(Button.primary("refresh-personal-stats:"+player,"Refresh")).queue();
                    });
                    break;
                }
                case "team-stats":{
                    evt.deferReply(false).queue(m->{
                        String clantag=evt.getOption("clantag3").getAsString();
                        String realm=evt.getOption("realm3").getAsString();
                        m.sendMessage(teamStats(clantag,realm)).addActionRow(Button.primary("refresh-team-stats:"+clantag+":"+realm,"Refresh")).queue();
                    });
                    break;
                }
                case "roster":{
                    evt.deferReply(false).queue(m->{
                        String realm=evt.getOption("realm").getAsString();
                        String clantag=evt.getOption("clantag").getAsString();
                        m.sendMessage(roster(clantag,realm)).addActionRow(Button.primary("refresh-roster:"+clantag+":"+realm,"Refresh")).queue();
                    });
                    break;
                }
                case "team-registration":{
                    evt.deferReply(false).queue(m->{
                        OptionMapping[] arrayOm={
                            evt.getOption("player1"),
                            evt.getOption("player2"),
                            evt.getOption("player3"),
                            evt.getOption("player4"),
                            evt.getOption("player5"),
                            evt.getOption("player6"),
                            evt.getOption("player7"),
                            evt.getOption("player8"),
                            evt.getOption("player9"),
                            evt.getOption("player10")
                        };
                        String clantag=evt.getOption("clantag4").getAsString();
                        String realm=evt.getOption("realm4").getAsString();
                        m.sendMessage(clantag+"' team, from "+realm+" server, has been registered!").queue();
                        for(OptionMapping optionMapping:arrayOm){
                            int wotbId=JsonHandler.getAccountData(optionMapping.getAsString(),realm).get("account_id").getAsInt();
                            BotActions.teamRegistration(clantag,evt.getUser().getId(),wotbId,optionMapping.getAsString(),realm);
                            JsonHandler.getAccTankData(wotbId);
                        }
                    });
                    break;
                }
                case "single-teammate-registration":{
                    evt.deferReply(false).queue(m->{
                        String player=evt.getOption("player5").getAsString();
                        String clantag=evt.getOption("clantag5").getAsString();
                        String realm=evt.getOption("server5").getAsString();
                        if(GetData.verifyCallerDiscordId(evt.getUser().getId(),clantag,realm)){
                            int wotbId=JsonHandler.getAccountData(player,realm).get("account_id").getAsInt();
                            BotActions.teamRegistration(clantag,evt.getUser().getId(),wotbId,player,realm);
                            JsonHandler.getAccTankData(wotbId);
                            mess=player+" of the "+clantag+" team, from "+realm+" server, has been registered!";
                        }else{
                            mess="You are not team leader, you can't register a new teammate without autorization of team caller/leader";
                        }
                        m.sendMessage(mess).queue();
                    });
                    break;
                }
                case "delete-team-registry":{
                    evt.deferReply(false).queue(m->{
                        String clantag=evt.getOption("clantag6").getAsString();
                        String realm=evt.getOption("server6").getAsString();
                        if(GetData.verifyCallerDiscordId(evt.getUser().getId(),clantag,realm)){
                            DeleteData.deleteTeam(clantag,realm);
                            mess=clantag+" team, from "+realm+" server, has been deleted!";
                        }else{
                            mess="You are not team leader, you can't delete team's registry without autorization of team caller/leader";
                        }
                        m.sendMessage(mess).queue();
                    });
                    break;
                }
                case "delete-teammate-registry":{
                    evt.deferReply(false).queue(m->{
                        String player=evt.getOption("player7").getAsString();
                        String clantag=evt.getOption("clantag7").getAsString();
                        String realm=evt.getOption("server7").getAsString();
                        if(GetData.verifyCallerDiscordId(evt.getUser().getId(),clantag,realm)){
                            DeleteData.deletePlayerFromTeamList(player,clantag);
                            mess=player+" has been deleted successfully!";
                        }else{
                            mess="You are not team leader, you can't delete a teammate registry without autorization of team caller/leader";
                        }
                        m.sendMessage(mess).queue();
                    });
                    break;
                }
                default:break;
            }
        }

        @Override
        public void onButtonInteraction(@NotNull ButtonInteractionEvent evt){
            String id=evt.getButton().getId();
            String[] val=id.split(":");
            switch(val[0]){
                case "refresh-roster":{
                    evt.deferEdit().queue(s->s.editOriginal(roster(val[1],val[2])).queue());
                    break;
                }
                case "refresh-team-stats":{
                    evt.deferEdit().queue(s->s.editOriginal(teamStats(val[1],val[2])).queue());
                    break;
                }
                case "refresh-personal-stats":{
                    evt.deferEdit().queue(s->s.editOriginal(personalStats(val[1])).queue());
                    break;
                }
                default:break;
            }
        }
        
        @Override
        public void onGuildReady(@NotNull GuildReadyEvent evt){
            List<CommandData> cd=new ArrayList<>();
            List<OptionData> odl=new ArrayList<>();

            cd.add(Commands.slash("personal-stats-tier10","Gets player stats of tier 10").addOption(OptionType.STRING,"player2","Gets player stats of tier 10",true));

            odl.add(new OptionData(OptionType.STRING,"clantag3","Team which will get stats",true));
            odl.add(new OptionData(OptionType.STRING,"realm3","Server where team is created",true));
            cd.add(Commands.slash("team-stats","Gets team stats").addOptions(odl));

            odl=new ArrayList<>();
            odl.add(new OptionData(OptionType.STRING,"clantag","Team which gets team roster",true));
            odl.add(new OptionData(OptionType.STRING,"realm","Server where team is created",true));
            cd.add(Commands.slash("roster","Get team structure").addOptions(odl));

            odl=new ArrayList<>();
            odl.add(new OptionData(OptionType.STRING,"clantag4","Set clantag of the team",true));
            odl.add(new OptionData(OptionType.STRING,"realm4","Set team server",true));
            odl.add(new OptionData(OptionType.STRING,"player1","Set player 1st of the team",true));
            odl.add(new OptionData(OptionType.STRING,"player2","Set player 2nd of the team",true));
            odl.add(new OptionData(OptionType.STRING,"player3","Set player 3rd of the team",true));
            odl.add(new OptionData(OptionType.STRING,"player4","Set player 4th of the team",true));
            odl.add(new OptionData(OptionType.STRING,"player5","Set player 5th of the team",true));
            odl.add(new OptionData(OptionType.STRING,"player6","Set player 6th of the team",true));
            odl.add(new OptionData(OptionType.STRING,"player7","Set player 7th of the team",true));
            odl.add(new OptionData(OptionType.STRING,"player8","Set player 8th of the team",false));
            odl.add(new OptionData(OptionType.STRING,"player9","Set player 9th of the team",false));
            odl.add(new OptionData(OptionType.STRING,"player10","Set player 10th of the team",false));
            cd.add(Commands.slash("team-registration","Register a team for statistic tracking").addOptions(odl));

            odl=new ArrayList<>();
            odl.add(new OptionData(OptionType.STRING,"player5","Set  player to be register",true));
            odl.add(new OptionData(OptionType.STRING,"clantag5","Set clantag of the team",true));
            odl.add(new OptionData(OptionType.STRING,"server5","Set team server",true));
            cd.add(Commands.slash("single-teammate-registration","Register a single teammate on a team").addOptions(odl));

            odl=new ArrayList<>();
            odl.add(new OptionData(OptionType.STRING,"clantag6","Set clantag of the team",true));
            odl.add(new OptionData(OptionType.STRING,"server6","Set team server",true));
            cd.add(Commands.slash("delete-team-registry","Delete a team registry").addOptions(odl));

            odl=new ArrayList<>();
            odl.add(new OptionData(OptionType.STRING,"player7","Set player to be deleted",true));
            odl.add(new OptionData(OptionType.STRING,"clantag7","Set clantag of the team",true));
            odl.add(new OptionData(OptionType.STRING,"server7","Set server",true));
            cd.add(Commands.slash("delete-teammate-registry","Delete a teammate register from team").addOptions(odl));

            evt.getGuild().updateCommands().addCommands(cd).complete();
        }
        
        protected String personalStats(String player){
            double val=BotActions.getPersonalTier10Stats(player);
            if(val!=0.0){
                return "Your tier X win rate is: "+val+"%";
            }else{
                return "No data or is under 1000 battles";
            }
        }
        
        protected String teamStats(String clantag,String realm){
            double val=BotActions.getTeamWinrate(clantag,realm);
            return "Original: "+val+"%, Wargaming's page: "+Math.round(val)+"%";
        }
        
        protected String roster(String clantag,String realm){
            double val=BotActions.getTeamWinrate(clantag,realm);
            return "Team Roster (original: "+val+"%, Wargaming's page: "+Math.round(val)+"%): \n"+BotActions.getRoster(clantag,realm);
        }
    }
}