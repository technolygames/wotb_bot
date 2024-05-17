package logic;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        Dotenv token=Dotenv.configure().directory("data/.env").load();
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
                case "register-user"->{
                    String player=BotActions.registerPlayer(evt.getUser().getId(),evt.getOption("player").getAsString(),evt.getOption("server").getAsString());
                    reply(evt,player);
                }
                case "team-stats"->{
                    reply(evt,"Processing...");
                    sendMessage(evt.getGuild(),evt.getChannel(),String.valueOf(BotActions.getTeamWinrate(evt.getOption("clan").getAsString(),evt.getOption("server").getAsString())+"%"));
                }
                case "team-registration"->{
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
                    OptionMapping om=evt.getOption("clan");
                    OptionMapping om2=evt.getOption("realm");
                    for(OptionMapping optionMapping:arrayOm){
                        int wotbId=JsonHandler.getAccountData(optionMapping.getAsString()).get("account_id").getAsInt();
                        BotActions.teamRegistration(om.getAsString(),wotbId,optionMapping.getAsString(),om2.getAsString());
                        JsonHandler.getAccTankData(wotbId);
                    }
                    reply(evt,om.getAsString()+"' team, of the "+om2.getAsString()+" server, has been registered!");
                }
                case "personal-stats-tier10"->{
                    OptionMapping om=evt.getOption("player");
                    double val=BotActions.getPersonalTier10Stats(om.getAsString());
                    if(val!=0.0){
                        reply(evt,String.valueOf(val+"%"));
                    }else{
                        reply(evt,"No data");
                    }
                }
                case "single-teammate-register"->{
                    OptionMapping om=evt.getOption("player");
                    OptionMapping om2=evt.getOption("clan");
                    OptionMapping om3=evt.getOption("realm");
                    int wotbId=JsonHandler.getAccountData(om.getAsString()).get("account_id").getAsInt();
                    BotActions.teamRegistration(om2.getAsString(),wotbId,om.getAsString(),om3.getAsString());
                    JsonHandler.getAccTankData(wotbId);
                    reply(evt,om.getAsString()+" of the "+om2.getAsString()+" team, from "+om3.getAsString()+" server, has been registered!");
                }
                case "delete-teammate-register"->{
                    OptionMapping om=evt.getOption("player");
                    OptionMapping om2=evt.getOption("clan");
                    OptionMapping om3=evt.getOption("realm");
                    int wotbId=JsonHandler.getAccountData(om.getAsString()).get("account_id").getAsInt();
                    BotActions.teamRegistration(om2.getAsString(),wotbId,om.getAsString(),om3.getAsString());
                    JsonHandler.getAccTankData(wotbId);
                    reply(evt,om.getAsString()+" of the "+om2.getAsString()+" team, from "+om3.getAsString()+" server, has been registered!");
                }
                default->{break;}
            }
        }

        @Override
        public void onGuildReady(@NotNull GuildReadyEvent evt){
            List<CommandData> cd=new ArrayList<>();
            List<OptionData> odl=new ArrayList<>();

            odl.add(new OptionData(OptionType.STRING,"player1","Set player 1st of the team",true));
            odl.add(new OptionData(OptionType.STRING,"player2","Set player 2nd of the team",true));
            odl.add(new OptionData(OptionType.STRING,"player3","Set player 3rd of the team",true));
            odl.add(new OptionData(OptionType.STRING,"player4","Set player 4th of the team",true));
            odl.add(new OptionData(OptionType.STRING,"player5","Set player 5th of the team",true));
            odl.add(new OptionData(OptionType.STRING,"player6","Set player 6th of the team",true));
            odl.add(new OptionData(OptionType.STRING,"player7","Set player 7th of the team",true));
            odl.add(new OptionData(OptionType.STRING,"player8","Set player 8th of the team",true));
            odl.add(new OptionData(OptionType.STRING,"player9","Set player 9th of the team",true));
            odl.add(new OptionData(OptionType.STRING,"player10","Set player 10th of the team",true));
            odl.add(new OptionData(OptionType.STRING,"clan","Set clantag of the team",true));
            odl.add(new OptionData(OptionType.STRING,"realm","Set team server",true));
            cd.add(Commands.slash("team-registration","Register 10 players of a team").addOptions(odl));

            odl=new ArrayList<>();
            odl.add(new OptionData(OptionType.STRING,"clan","Team which will get stats",true));
            odl.add(new OptionData(OptionType.STRING,"server","Server where team is created",true));
            cd.add(Commands.slash("team-stats","Gets team stats").addOptions(odl));

            odl=new ArrayList<>();
            odl.add(new OptionData(OptionType.STRING,"player","Set  player to be register",true));
            odl.add(new OptionData(OptionType.STRING,"clan","Set clantag of the team",true));
            odl.add(new OptionData(OptionType.STRING,"realm","Set team server",true));
            cd.add(Commands.slash("single-teammate-register","Register a single teammate on a team").addOptions(odl));

            odl=new ArrayList<>();
            odl.add(new OptionData(OptionType.STRING,"player","Set  player to be register",true));
            odl.add(new OptionData(OptionType.STRING,"clan","Set clantag of the team",true));
            odl.add(new OptionData(OptionType.STRING,"realm","Set team server",true));
            cd.add(Commands.slash("delete-teammate-register","Delete a teammate register from team").addOptions(odl));

            cd.add(Commands.slash("personal-stats-tier10","Gets player stats of tier 10").addOption(OptionType.STRING,"player","Gets player stats of tier 10",true));
            
            odl=new ArrayList<>();
            odl.add(new OptionData(OptionType.STRING,"player","Register the user to record data",true));
            odl.add(new OptionData(OptionType.STRING,"server","Set user server",true));
            cd.add(Commands.slash("register-user","Register the user to record data").addOptions(odl));
            evt.getGuild().updateCommands().addCommands(cd).complete();
        }

        private void sendMessage(Guild evt,MessageChannelUnion channel,String message){
            evt.getTextChannelById(channel.getIdLong()).sendMessage(message).queue();
        }

        private void reply(SlashCommandInteraction evt,String message){
            evt.reply(message).setEphemeral(false).queue();
        }
    }
}