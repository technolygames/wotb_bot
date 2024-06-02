package logic;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import dbconnection.DeleteData;
import dbconnection.GetData;
import io.github.cdimascio.dotenv.Dotenv;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
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

    private class EventListeners extends net.dv8tion.jda.api.hooks.ListenerAdapter{
        @Override
        public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent evt){
            switch(evt.getName()){
                case "personal-stats-tier10"->{
                    reply(evt);
                    OptionMapping om=evt.getOption("player2");
                    double val=BotActions.getPersonalTier10Stats(om.getAsString());
                    if(val!=0.0){
                        sendMessage(evt.getGuild(),evt.getChannel(),"Your tier X win rate is: "+val+"%");
                    }else{
                        sendMessage(evt.getGuild(),evt.getChannel(),"No data");
                    }
                }
                case "team-stats"->{
                    reply(evt);
                    double val=BotActions.getTeamWinrate(evt.getOption("clantag3").getAsString(),evt.getOption("realm3").getAsString());
                    sendMessage(evt.getGuild(),evt.getChannel(),String.valueOf("Original: "+val+"%, Wargaming's page: "+Math.round(val)+"%"));
                }
                case "roster"->{
                    reply(evt);
                    String clantag=evt.getOption("clantag").getAsString();
                    String realm=evt.getOption("realm").getAsString();
                    double val=BotActions.getTeamWinrate(clantag,realm);
                    sendMessage(evt.getGuild(),evt.getChannel(),"Team Roster (original: "+val+"%, Wargaming's page: "+Math.round(val)+"%): \n"+BotActions.getRoster(clantag,realm));
                }
                case "team-registration"->{
                    reply(evt);
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
                    OptionMapping om=evt.getOption("clantag4");
                    OptionMapping om2=evt.getOption("realm4");
                    sendMessage(evt.getGuild(),evt.getChannel(),om.getAsString()+"' team, of the "+om2.getAsString()+" server, has been registered!");
                    for(OptionMapping optionMapping:arrayOm){
                        int wotbId=JsonHandler.getAccountData(optionMapping.getAsString(),om2.getAsString()).get("account_id").getAsInt();
                        BotActions.teamRegistration(om.getAsString(),evt.getUser().getId(),wotbId,optionMapping.getAsString(),om2.getAsString());
                        JsonHandler.getAccTankData(wotbId);
                    }
                }
                case "single-teammate-registration"->{
                    reply(evt);
                    OptionMapping om=evt.getOption("player5");
                    OptionMapping om2=evt.getOption("clantag5");
                    OptionMapping om3=evt.getOption("server5");
                    if(GetData.verifyCallerDiscordId(evt.getUser().getId(),om2.getAsString(),om3.getAsString())){
                        int wotbId=JsonHandler.getAccountData(om.getAsString(),om3.getAsString()).get("account_id").getAsInt();
                        BotActions.teamRegistration(om2.getAsString(),evt.getUser().getId(),wotbId,om.getAsString(),om3.getAsString());
                        JsonHandler.getAccTankData(wotbId);
                        sendMessage(evt.getGuild(),evt.getChannel(),om.getAsString()+" of the "+om2.getAsString()+" team, from "+om3.getAsString()+" server, has been registered!");
                    }else{
                        sendMessage(evt.getGuild(),evt.getChannel(),"You are not team leader, you can't register a new teammate without autorization of team caller/leader");
                    }
                }
                case "delete-team-registry"->{
                    reply(evt);
                    String clantag=evt.getOption("clantag6").getAsString();
                    String realm=evt.getOption("server6").getAsString();
                    if(GetData.verifyCallerDiscordId(evt.getUser().getId(),clantag,realm)){
                        DeleteData.deleteTeam(clantag,realm);
                        sendMessage(evt.getGuild(),evt.getChannel(),clantag+" team, from "+realm+" server, has been deleted!");
                    }else{
                        sendMessage(evt.getGuild(),evt.getChannel(),"You are not team leader, you can't delete team's registry without autorization of team caller/leader");
                    }
                }
                case "delete-teammate-registry"->{
                    reply(evt);
                    OptionMapping om=evt.getOption("player7");
                    String clantag=evt.getOption("clantag7").getAsString();
                    String realm=evt.getOption("server7").getAsString();
                    if(GetData.verifyCallerDiscordId(evt.getUser().getId(),clantag,realm)){
                        DeleteData.deletePlayerFromTeamList(om.getAsString(),clantag);
                        sendMessage(evt.getGuild(),evt.getChannel(),om.getAsString()+" has been deleted successfully!");
                    }else{
                        sendMessage(evt.getGuild(),evt.getChannel(),"You are not team leader, you can't delete a teammate registry without autorization of team caller/leader");
                    }
                }
                default->{break;}
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

        private void sendMessage(Guild evt,MessageChannelUnion channel,String message){
            evt.getTextChannelById(channel.getIdLong()).sendMessage(message).queue();
        }

        private void reply(SlashCommandInteraction evt){
            evt.reply("Processing...").setEphemeral(true).queue();
        }
    }
}