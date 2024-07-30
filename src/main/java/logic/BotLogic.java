package logic;

import dbconnection.DeleteData;
import dbconnection.GetData;
import dbconnection.UpdateData;

import org.jetbrains.annotations.NotNull;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

public class BotLogic{
    ShardManager manager;
    public BotLogic() throws InterruptedException{
        Dotenv token=Dotenv.configure().directory("data").load();
        DefaultShardManagerBuilder builder=DefaultShardManagerBuilder.createDefault(token.get("TOKEN"));
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.playing("Testing"));
        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        manager=builder.build();
        manager.addEventListener(new EventListeners());
        new EventListeners().clearCommands();
    }

    private String mess;
    private static final String MESSAGE="You are not the leader of this team";
    private static final String MESSAGE_2="Cannot be NULL";
    
    private class EventListeners extends net.dv8tion.jda.api.hooks.ListenerAdapter{
        @Override
        public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent evt){
            String callerId=evt.getUser().getId();
            switch(evt.getName()){
                case "add-existing-player":{
                    evt.deferReply(false).queue(m->{
                        String clantag=evt.getOption("clantag").getAsString().toUpperCase();
                        String player=evt.getOption("player").getAsString();
                        String realm=evt.getOption("server").getAsString().toUpperCase();
                        if(new GetData().verifyCallerDiscordId(callerId,clantag,realm)){
                            new UpdateData().updatePlayerRegistry(callerId,clantag,player,realm);
                            mess=player+" has been added to "+clantag+" roster";
                        }else{
                            mess=MESSAGE;
                        }
                        m.sendMessage(mess).queue();
                    });
                    break;
                }
                case "change-team-clantag":{
                    evt.deferReply(false).queue(m->{
                        String oldClantag=evt.getOption("old").getAsString().toUpperCase();
                        String newClantag=evt.getOption("new").getAsString().toUpperCase();
                        String realm=evt.getOption("server2").getAsString().toUpperCase();
                        if(!oldClantag.equals("NULL")||!newClantag.equals("NULL")){
                            if(new GetData().verifyCallerDiscordId(callerId,oldClantag,realm)){
                                new UpdateData().updateTeamClantag(oldClantag,newClantag,realm);
                                mess=oldClantag+" has changed it's clantag to "+newClantag;
                            }else{
                                mess=MESSAGE;
                            }
                        }else{
                            mess=MESSAGE_2;
                        }
                        m.sendMessage(mess).queue();
                    });
                    break;
                }
                case "change-teammate-clantag":{
                    evt.deferReply(false).queue(m->{
                        String oldClantag=evt.getOption("old").getAsString().toUpperCase();
                        String newClantag=evt.getOption("new").getAsString().toUpperCase();
                        String player=evt.getOption("player2").getAsString();
                        String realm=evt.getOption("server2").getAsString().toUpperCase();
                        if(!oldClantag.equals("NULL")||!newClantag.equals("NULL")){
                            if(new GetData().verifyCallerDiscordId(callerId,oldClantag,realm)){
                                new UpdateData().updatePlayerClantag(newClantag,player,realm);
                                mess=player+" has changed it's clantag from "+oldClantag+" to "+newClantag;
                            }else{
                                mess=MESSAGE;
                            }
                        }else{
                            mess="Use /remove-from-roster instead of this command";
                        }
                        m.sendMessage(mess).queue();
                    });
                    break;
                }
                case "check-player":{
                    evt.deferReply(false).queue(m->{
                        String nickname=evt.getOption("player3").getAsString();
                        String realm=evt.getOption("server3").getAsString().toUpperCase();
                        m.sendMessage(new BotActions().checkPlayer(nickname,realm)).queue();
                    });
                    break;
                }
                case "create-new-teaam":{
                    evt.deferReply(false).queue(m->{
                        String clantag=evt.getOption("clantag4").getAsString().toUpperCase();
                        String player=evt.getOption("player4").getAsString();
                        String realm=evt.getOption("server4").getAsString().toUpperCase();
                        if(!clantag.equals("NULL")){
                            if(new GetData().checkClantag(clantag,realm)){
                                int wotbId=new JsonHandler().getAccountData(player,realm).getAcoountId();
                                new BotActions().teamRegistration(clantag,callerId,wotbId,player,realm);
                                mess=clantag+" has been created!";
                            }else{
                                mess=clantag+" already exist";
                            }
                        }else{
                            mess=MESSAGE_2;
                        }
                        m.sendMessage(mess).queue();
                    });
                    break;
                }
                case "delete-team-registry":{
                    evt.deferReply(false).queue(m->{
                        String clantag=evt.getOption("clantag5").getAsString().toUpperCase();
                        String realm=evt.getOption("server5").getAsString().toUpperCase();
                        if(!clantag.equals("NULL")){
                            if(new GetData().verifyCallerDiscordId(callerId,clantag,realm)){
                                new DeleteData().deleteTeam(clantag,realm);
                                mess=clantag+" team has been deleted!";
                            }else{
                                mess=MESSAGE;
                            }
                        }else{
                            mess=MESSAGE_2;
                        }
                        m.sendMessage(mess).queue();
                    });
                    break;
                }
                case "delete-teammate-registry":{
                    evt.deferReply(false).queue(m->{
                        String player=evt.getOption("player5").getAsString();
                        String clantag=evt.getOption("clantag5").getAsString().toUpperCase();
                        String realm=evt.getOption("server5").getAsString().toUpperCase();
                        if(clantag.equals("NULL")){
                            new DeleteData().deletePlayerFromList(player,realm);
                            mess=player+" has been deleted!";
                        }else{
                            if(new GetData().verifyCallerDiscordId(callerId,clantag,realm)){
                                new DeleteData().deletePlayerFromTeamList(player,clantag);
                                mess=player+" has been deleted!";
                            }else{
                                mess=MESSAGE;
                            }
                        }
                        m.sendMessage(mess).queue();
                    });
                    break;
                }
                case "personal-stats-tier10":{
                    evt.deferReply(false).queue(m->{
                        String player=evt.getOption("player6").getAsString();
                        String realm=evt.getOption("server6").getAsString().toUpperCase();
                        m.sendMessage(personalStats(player,realm)).addActionRow(Button.primary("refresh-personal-stats:"+player+":"+realm,"Refresh")).queue();
                    });
                    break;
                }
                case "remove-from-roster":{
                    evt.deferReply(false).queue(m->{
                        String player=evt.getOption("player7").getAsString();
                        String clantag=evt.getOption("clantag7").getAsString();
                        String realm=evt.getOption("server7").getAsString();
                        if(new GetData().verifyCallerDiscordId(callerId,clantag,realm)){
                            new UpdateData().updatePlayerRegistry(null,null,player,realm);
                            m.sendMessage(player+" has been removed from the team roster").queue();
                        }
                    });
                    break;
                }
                case "roster":{
                    evt.deferReply(false).queue(m->{
                        String realm=evt.getOption("server8").getAsString().toUpperCase();
                        String clantag=evt.getOption("clantag8").getAsString().toUpperCase();
                        if(!clantag.equals("NULL")){
                            m.sendMessage(roster(clantag,realm)).addActionRow(Button.primary("refresh-roster:"+clantag+":"+realm,"Refresh")).queue();
                        }else{
                            m.sendMessage(MESSAGE_2).queue();
                        }
                    });
                    break;
                }
                case "single-teammate-registration":{
                    evt.deferReply(false).queue(m->{
                        String player=evt.getOption("player9").getAsString();
                        String clantag=evt.getOption("clantag9").getAsString().toUpperCase();
                        String realm=evt.getOption("server9").getAsString().toUpperCase();
                        if(!clantag.equals("NULL")){
                            if(new GetData().verifyCallerDiscordId(callerId,clantag,realm)){
                                if(!new GetData().checkPlayerRegistry(player,realm)){
                                    int wotbId=new JsonHandler().getAccountData(player,realm).getAcoountId();
                                    new BotActions().teamRegistration(clantag,callerId,wotbId,player,realm);
                                    new JsonHandler().getAccTankData(wotbId);
                                    mess=player+" has been added to "+clantag+" roster!";
                                }else{
                                    if(new GetData().checkPlayerClantag(player,realm)){
                                        new UpdateData().updatePlayerRegistry(callerId,clantag,player,realm);
                                        mess=player+" has been added to "+clantag+" roster";
                                    }else{
                                        mess=player+" is on another team";
                                    }
                                }
                            }else{
                                mess=MESSAGE;
                            }
                        }else{
                            mess="To track your tier 10 stats, use /personal-stats-tier10";
                        }
                        m.sendMessage(mess).queue();
                    });
                    break;
                }
                case "team-registration":{
                    evt.deferReply(false).queue(m->{
                        String[] values={
                            evt.getOption("player1").getAsString(),
                            evt.getOption("player2").getAsString(),
                            evt.getOption("player3").getAsString(),
                            evt.getOption("player4").getAsString(),
                            evt.getOption("player5").getAsString(),
                            evt.getOption("player6").getAsString(),
                            evt.getOption("player7").getAsString(),
                            evt.getOption("player8").getAsString(),
                            evt.getOption("player9").getAsString(),
                            evt.getOption("player10").getAsString()
                        };
                        String clantag=evt.getOption("clantag10").getAsString().toUpperCase();
                        String realm=evt.getOption("server10").getAsString().toUpperCase();
                        if(!clantag.equals("NULL")){
                            if(!new GetData().checkClantag(clantag,realm)){
                                mess=clantag+" team has been registered!";
                                for(String value:values){
                                    if(!new GetData().checkPlayerRegistry(value,realm)){
                                        int wotbId=new JsonHandler().getAccountData(value,realm).getAcoountId();
                                        new BotActions().teamRegistration(clantag,callerId,wotbId,value,realm);
                                        new JsonHandler().getAccTankData(wotbId);
                                    }else{
                                        if(new GetData().checkPlayerClantag(value,realm)){
                                            new UpdateData().updatePlayerRegistry(callerId,clantag,value,realm);
                                        }
                                    }
                                }
                            }else{
                                mess=clantag+" already exist";
                            }
                        }else{
                            mess=MESSAGE_2;
                        }
                        m.sendMessage(mess).queue();
                    });
                    break;
                }
                case "team-stats":{
                    evt.deferReply(false).queue(m->{
                        String clantag=evt.getOption("clantag11").getAsString().toUpperCase();
                        String realm=evt.getOption("server11").getAsString().toUpperCase();
                        if(!clantag.equals("NULL")){
                            m.sendMessage(teamStats(clantag,realm)).addActionRow(Button.primary("refresh-team-stats:"+clantag+":"+realm,"Refresh")).queue();
                        }else{
                            m.sendMessage(MESSAGE_2).queue();
                        }
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
                case "refresh-personal-stats":{
                    evt.deferEdit().queue(s->s.editOriginal(personalStats(val[1],val[2])).queue());
                    break;
                }
                case "refresh-roster":{
                    evt.deferEdit().queue(s->s.editOriginal(roster(val[1],val[2])).queue());
                    break;
                }
                case "refresh-team-stats":{
                    evt.deferEdit().queue(s->s.editOriginal(teamStats(val[1],val[2])).queue());
                    break;
                }
                default:break;
            }
        }

        private void clearCommands(){
            manager.getShards().forEach(jda->{
                jda.retrieveCommands().queue(commands->{
                    for(var command:commands){
                        jda.deleteCommandById(command.getId()).queue();
                    }
                });
            });
            manager.getGuildCache().forEach(guild->{
                guild.retrieveCommands().queue(commands->{
                    for(var command:commands){
                        guild.deleteCommandById(command.getId()).queue();
                    }
                });
                guild.updateCommands().addCommands(getNewCommands()).complete();
            });
        }

        @Override
        public void onGuildJoin(@NotNull GuildJoinEvent evt){
            List<CommandData> newCommands=getNewCommands();
            evt.getGuild().updateCommands().addCommands(newCommands).complete();
        }

        @Override
        public void onGuildReady(@NotNull GuildReadyEvent evt){
            registerCommands(evt.getGuild());
        }

        private void registerCommands(Guild guild){
            List<CommandData> newCommands=getNewCommands();
            guild.updateCommands().addCommands(newCommands).complete();
        }

        private List<CommandData> getNewCommands(){
            List<CommandData> commands=new ArrayList<>();

            List<Command.Choice> serverChoices=Arrays.asList(
                    new Command.Choice("NA","NA"),
                    new Command.Choice("EU","EU"),
                    new Command.Choice("ASIA","ASIA")
            );

            commands.add(Commands.slash("add-existing-player","Adds an existing player into the bot database to a team roster")
                    .addOptions(
                            new OptionData(OptionType.STRING,"clantag","Team clantag",true),
                            new OptionData(OptionType.STRING,"player","Player to be added",true),
                            new OptionData(OptionType.STRING,"server","Player and team region",true).addChoices(serverChoices)
                    ));

            commands.add(Commands.slash("change-team-clantag","Change team clantag")
                    .addOptions(
                            new OptionData(OptionType.STRING,"old","Old clantag",true),
                            new OptionData(OptionType.STRING,"new","New clantag",true),
                            new OptionData(OptionType.STRING,"server2", "Team region",true).addChoices(serverChoices)
                    ));

            commands.add(Commands.slash("change-teammate-clantag","Change teammate clantag")
                    .addOptions(
                            new OptionData(OptionType.STRING,"old","Old clantag",true),
                            new OptionData(OptionType.STRING,"new","New clantag",true),
                            new OptionData(OptionType.STRING,"player2","Player nickname",true),
                            new OptionData(OptionType.STRING,"server2","Player region",true).addChoices(serverChoices)
                    ));

            commands.add(Commands.slash("check-player","Checks if a player exist on bot's registry")
                    .addOptions(
                            new OptionData(OptionType.STRING,"player3","Player to be checked",true),
                            new OptionData(OptionType.STRING,"server3","Player region",true).addChoices(serverChoices)
                    ));

            commands.add(Commands.slash("create-new-team","Creates a new team without using team-registration command")
                    .addOptions(
                            new OptionData(OptionType.STRING,"clantag4","Team clantag",true),
                            new OptionData(OptionType.STRING,"player4","Player to be register",true),
                            new OptionData(OptionType.STRING,"server4","Team region",true).addChoices(serverChoices)
                    ));

            commands.add(Commands.slash("delete-team-registry","Delete a team registry")
                    .addOptions(
                            new OptionData(OptionType.STRING,"clantag5","Team clantag",true),
                            new OptionData(OptionType.STRING,"server5","Team region",true).addChoices(serverChoices)
                    ));

            commands.add(Commands.slash("delete-teammate-registry","Delete a teammate register")
                    .addOptions(
                            new OptionData(OptionType.STRING,"player5","Player nickname",true),
                            new OptionData(OptionType.STRING,"clantag5","Team clantag",true),
                            new OptionData(OptionType.STRING,"server5","Player region",true).addChoices(serverChoices)
                    ));

            commands.add(Commands.slash("personal-stats-tier10","Gets player stats of tier 10")
                    .addOptions(
                            new OptionData(OptionType.STRING,"player6","Player nickname",true),
                            new OptionData(OptionType.STRING,"server6","Player region",true).addChoices(serverChoices)
                    ));

            commands.add(Commands.slash("remove-from-roster","Remove a teammate from team roster")
                    .addOptions(
                            new OptionData(OptionType.STRING,"player7","Player nickname",true),
                            new OptionData(OptionType.STRING,"clantag7","Team clantag",true),
                            new OptionData(OptionType.STRING,"server7","Team region",true).addChoices(serverChoices)
                    ));
            
            commands.add(Commands.slash("roster","Get team structure")
                    .addOptions(
                            new OptionData(OptionType.STRING,"clantag8","Team clantag",true),
                            new OptionData(OptionType.STRING,"server8","Team region",true).addChoices(serverChoices)
                    ));

            commands.add(Commands.slash("single-teammate-registration","Register a new teammate")
                    .addOptions(
                            new OptionData(OptionType.STRING,"player9","Player nickname",true),
                            new OptionData(OptionType.STRING,"clantag9","Team clantag",true),
                            new OptionData(OptionType.STRING,"server9","Team region",true).addChoices(serverChoices)
                    ));

            commands.add(Commands.slash("team-registration","Register a team for statistic tracking")
                    .addOptions(
                            new OptionData(OptionType.STRING,"clantag10","Team clantag",true),
                            new OptionData(OptionType.STRING,"server10","Team region",true).addChoices(serverChoices),
                            new OptionData(OptionType.STRING,"player1","1st player of the team",true),
                            new OptionData(OptionType.STRING,"player2","2nd player of the team",true),
                            new OptionData(OptionType.STRING,"player3","3rd player of the team",true),
                            new OptionData(OptionType.STRING,"player4","4th player of the team",true),
                            new OptionData(OptionType.STRING,"player5","5th player of the team",true),
                            new OptionData(OptionType.STRING,"player6","6th player of the team",true),
                            new OptionData(OptionType.STRING,"player7","7th player of the team",true),
                            new OptionData(OptionType.STRING,"player8","8th player of the team",false),
                            new OptionData(OptionType.STRING,"player9","9th player of the team",false),
                            new OptionData(OptionType.STRING,"player10","10th player of the team",false)
                    ));

            commands.add(Commands.slash("team-stats","Gets team stats")
                    .addOptions(
                            new OptionData(OptionType.STRING,"clantag11","Team clantag",true),
                            new OptionData(OptionType.STRING,"server11","Team region",true).addChoices(serverChoices)
                    ));

            return commands;
        }

        protected String personalStats(String player,String realm){
            double val=new BotActions().getTier10Stats(player,realm);
            if(val!=0.0){
                return "Your tier X win rate is: "+val+"%";
            }else{
                return "No data or is under 1000 battles";
            }
        }

        protected String teamStats(String clantag,String realm){
            double val=new BotActions().getTeamWinrate(clantag,realm);
            return "Original: "+val+"%, Wargaming's page: "+Math.round(val)+"%";
        }

        protected String roster(String clantag,String realm){
            double val=new BotActions().getTeamWinrate(clantag,realm);
            return "Team Roster (original: "+val+"%, Wargaming's page: "+Math.round(val)+"%): \n"+new BotActions().getRoster(clantag,realm);
        }
    }
}