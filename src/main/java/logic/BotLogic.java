package logic;

import dbconnection.DeleteData;
import dbconnection.GetData;
import dbconnection.InsertData;
import interfaces.Interfaces;
import mvc.Mvc1;
import mvc.Mvc2;

import org.jetbrains.annotations.NotNull;
import io.github.cdimascio.dotenv.Dotenv;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import java.util.logging.Level;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;

/**
 * @author erick
 */
public class BotLogic{
    ShardManager manager;
    public void run(){
        EventListeners evt=new EventListeners();
        try{
            Dotenv token=Dotenv.configure().directory("data").load();
            DefaultShardManagerBuilder builder=DefaultShardManagerBuilder.createDefault(token.get("TOKEN"));
            builder.setStatus(OnlineStatus.ONLINE);
            builder.setActivity(Activity.playing("Testing"));
            builder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
            manager=builder.build();
            manager.addEventListener(evt);
            evt.clearCommands();
        }catch(Exception e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }
    
    private final DeleteData dd=new DeleteData();
    private final GetData gd=new GetData();
    private final InsertData id=new InsertData();
    private final BotActions ba=new BotActions();
    private final UtilityClass uc=new UtilityClass();
    private final JsonHandler jh=new JsonHandler();
    private final BotCommandActions bca=new BotCommandActions();
    private final CommandActions ca=new CommandActions();

    private static final String MESSAGE="You are not the leader of this team";
    private static final String MESSAGE_2="Cannot be NULL";
    private static final String MESSAGE_3="No data";
    public static final String MESSAGE_4="Something gone wrong";
    private static final String MESSAGE_5="Results";
    private static final String MESSAGE_6="Write valid data";
    private static final String MESSAGE_7="Internal failure";
    private static final String MESSAGE_8=" has been created!";
    
    private static final String HEADER_1="add-existing-player";
    private static final String HEADER_2="check-player";
    private static final String HEADER_3="create-new-team";
    private static final String HEADER_4="formula-stats";
    private static final String HEADER_5="freeup-roster";
    private static final String HEADER_6="help";
    private static final String HEADER_16="ingame-roster";
    private static final String HEADER_7="ingame-team-leaderboard";
    private static final String HEADER_8="personal-stats-tier10";
    private static final String HEADER_9="register-player";
    private static final String HEADER_10="remove-from-roster";
    private static final String HEADER_11="roster";
    private static final String HEADER_12="seed-teams";
    private static final String HEADER_13="team-leaderboard";
    private static final String HEADER_14="team-registration";
    private static final String HEADER_15="team-stats";
    
    private static final String FIELD_HEADER_1_1="player";
    private static final String FIELD_HEADER_1_2="clantag";
    private static final String FIELD_HEADER_1_3="server";
    
    private static final String FIELD_HEADER_2_1="player2";
    
    private static final String FIELD_HEADER_3_1="clantag3";
    private static final String FIELD_HEADER_3_2="server3";
    private static final String FIELD_HEADER_3_3="player3";
    
    private static final String FIELD_HEADER_4_1="player4";
    
    private static final String FIELD_HEADER_5_1="clantag5";
    private static final String FIELD_HEADER_5_2="server5";
    
    private static final String FIELD_HEADER_16_1="clantag16";
    private static final String FIELD_HEADER_16_2="realm16";
    
    private static final String FIELD_HEADER_7_1="tourney_id";
    private static final String FIELD_HEADER_7_2="server7";
    
    private static final String FIELD_HEADER_8_1="player8";
    
    private static final String FIELD_HEADER_9_1="player9";
    private static final String FIELD_HEADER_9_2="server9";
    
    private static final String FIELD_HEADER_10_1="nickname10";
    private static final String FIELD_HEADER_10_2="clantag10";
    private static final String FIELD_HEADER_10_3="server10";
    
    private static final String FIELD_HEADER_11_1="clantag11";
    private static final String FIELD_HEADER_11_2="server11";
    
    private static final String FIELD_HEADER_12_1="tourney_id";
    private static final String FIELD_HEADER_12_2="server12";
    
    private static final String FIELD_HEADER_13_1="server13";
    
    private static final String FIELD_HEADER_14_1="clantag14";
    private static final String FIELD_HEADER_14_2="server14";
    private static final String FIELD_HEADER_14_3="player1";
    private static final String FIELD_HEADER_14_4="player2";
    private static final String FIELD_HEADER_14_5="player3";
    private static final String FIELD_HEADER_14_6="player4";
    private static final String FIELD_HEADER_14_7="player5";
    private static final String FIELD_HEADER_14_8="player6";
    private static final String FIELD_HEADER_14_9="player7";
    private static final String FIELD_HEADER_14_10="player8";
    private static final String FIELD_HEADER_14_11="player9";
    private static final String FIELD_HEADER_14_12="player10";
    
    private static final String FIELD_HEADER_15_1="clantag15";
    private static final String FIELD_HEADER_15_2="server15";
    
    private class EventListeners extends net.dv8tion.jda.api.hooks.ListenerAdapter{
        @Override
        public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent evt){
            CommandMethods cm=new CommandMethods(evt,evt.getUser().getId());
            switch(evt.getName()){
                case HEADER_1->{
                    cm.addExistingPlayer();
                }
                case HEADER_2->{
                    cm.checkPlayer();
                }
                case HEADER_3->{
                    cm.createNewTeam();
                }
                case HEADER_4->{
                    cm.formulaStats();
                }
                case HEADER_5->{
                    cm.freeupRoster();
                }
                case HEADER_6->{
                    cm.help();
                }
                case HEADER_16->{
                    cm.ingameRoster();
                }
                case HEADER_7->{
                    cm.ingameTeamLeaderboard();
                }
                case HEADER_8->{
                    cm.personalStatsTier10();
                }
                case HEADER_9->{
                    cm.registerPlayer();
                }
                case HEADER_10->{
                    cm.removeFromRoster();
                }
                case HEADER_11->{
                    cm.rosterCommand();
                }
                case HEADER_12->{
                    cm.seedTeams();
                }
                case HEADER_13->{
                    cm.teamLeaderboard();
                }
                case HEADER_14->{
                    cm.teamRegistrationCommand();
                }
                case HEADER_15->{
                    cm.teamStatsCommand();
                }
                default->{}
            }
        }

        @Override
        public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent evt){
            switch(evt.getName()){
                case HEADER_1->{
                    switch(evt.getFocusedOption().getName()){
                        case FIELD_HEADER_1_1->{
                            setChoices(evt,Concurrency.clanlessPlayerChoices);
                        }
                        case FIELD_HEADER_1_2->{
                            setChoices(evt,Concurrency.clanChoices);
                        }
                        default->{}
                    }
                }
                case HEADER_2->{
                    if(evt.getFocusedOption().getName().equals(FIELD_HEADER_2_1)){
                        setChoices(evt,Concurrency.playerChoices);
                    }
                }
                case HEADER_4->{
                    if(evt.getFocusedOption().getName().equals(FIELD_HEADER_4_1)){
                        setChoices(evt,Concurrency.tbPlayerChoices);
                    }
                }
                case HEADER_5->{
                    if(evt.getFocusedOption().getName().equals(FIELD_HEADER_5_1)){
                        setChoices(evt,Concurrency.clanChoices);
                    }
                }
                case HEADER_16->{
                    if(evt.getFocusedOption().getName().equals(FIELD_HEADER_16_1)){
                        setChoices(evt,Concurrency.ingameTeamChoices);
                    }
                }
                case HEADER_7->{
                    if(evt.getFocusedOption().getName().equals(FIELD_HEADER_7_1)){
                        setChoices(evt,Concurrency.tournamentChoices);
                    }
                }
                case HEADER_8->{
                    if(evt.getFocusedOption().getName().equals(FIELD_HEADER_8_1)){
                        setChoices(evt,Concurrency.playerChoices);
                    }
                }
                case HEADER_10->{
                    switch(evt.getFocusedOption().getName()){
                        case FIELD_HEADER_10_1->{
                            setChoices(evt,Concurrency.notNullPlayerChoices);
                        }
                        case FIELD_HEADER_10_2->{
                            setChoices(evt,Concurrency.clanChoices);
                        }
                        default->{}
                    }
                }
                case HEADER_11->{
                    if(evt.getFocusedOption().getName().equals(FIELD_HEADER_11_1)){
                        setChoices(evt,Concurrency.clanChoices);
                    }
                }
                case HEADER_12->{
                    if(evt.getFocusedOption().getName().equals(FIELD_HEADER_12_1)){
                        setChoices(evt,Concurrency.tournamentChoices);
                    }
                }
                case HEADER_15->{
                    if(evt.getFocusedOption().getName().equals(FIELD_HEADER_15_1)){
                        setChoices(evt,Concurrency.clanChoices);
                    }
                }
                default->{}
            }
        }

        @Override
        public void onButtonInteraction(@NotNull ButtonInteractionEvent evt){
            evt.deferEdit().queue(hook->{
                try{
                    String value=evt.getButton().getId().trim();
                    String[] val=value.split(":");
                    switch(val[0]){
                        case "refresh-formula-stats"->{
                            hook.editOriginalEmbeds(ca.playerWeight(Integer.parseInt(val[1]))).queue();
                        }
                        case "refresh-personal-stats"->{
                            hook.editOriginalEmbeds(bca.getTier10Stats(Integer.parseInt(val[1]))).queue();
                        }
                        default->{}
                    }
                }catch(Exception e){
                    hook.editOriginalEmbeds(new EmbedBuilder().setColor(Color.RED).addField(MESSAGE_4,MESSAGE_7,true).build()).queue();
                    uc.log(Level.SEVERE,e.getMessage(),e);
                }
            });
        }

        private void setChoices(CommandAutoCompleteInteraction evt,List<Command.Choice> choices){
            String focused=evt.getFocusedOption().getValue().toLowerCase();
            List<Command.Choice> choices2=choices.stream()
                    .filter(choice->choice.getName().toLowerCase().startsWith(focused))
                    .limit(5)
                    .collect(Collectors.toList());
            Collections.shuffle(choices2);
            evt.replyChoices(choices2).queue();
        }

        private void clearCommands(){
            manager.getShards().forEach(jda->
                jda.retrieveCommands().queue(commands->{
                    for(Command command:commands){
                        jda.deleteCommandById(command.getId()).queue();
                    }
                }));

            manager.getGuildCache().forEach(guild->{
                guild.retrieveCommands().queue(commands->{
                    for(Command command:commands){
                        guild.deleteCommandById(command.getId()).queue();
                    }
                });
                
                registerCommands(guild);
            });
        }

        @Override
        public void onGuildJoin(@NotNull GuildJoinEvent evt){
            registerCommands(evt.getGuild());
            //uc.log(Level.INFO,evt.getGuild().getName());
        }

        @Override
        public void onGuildReady(@NotNull GuildReadyEvent evt){
            registerCommands(evt.getGuild());
            //uc.log(Level.INFO,evt.getGuild().getName());
        }

        private void registerCommands(Guild guild){
            List<CommandData> commands=new ArrayList<>();

            List<Command.Choice> serverChoices=Arrays.asList(
                    new Command.Choice("NA","NA"),
                    new Command.Choice("EU","EU"),
                    new Command.Choice("ASIA","ASIA")
            );

            commands.add(Commands.slash(HEADER_1,"Adds a clanless player to a team roster.")
                    .addOptions(
                            new OptionData(OptionType.STRING,FIELD_HEADER_1_1,"Player to be added",true).setAutoComplete(true),
                            new OptionData(OptionType.STRING,FIELD_HEADER_1_2,"Team clantag",true).setAutoComplete(true),
                            new OptionData(OptionType.STRING,FIELD_HEADER_1_3,"Player and team region",true).addChoices(serverChoices)
                    ));

            commands.add(Commands.slash(HEADER_2,"Checks a specific player in the bot's registry.")
                    .addOptions(
                            new OptionData(OptionType.STRING,FIELD_HEADER_2_1,"Player to be checked",true).setAutoComplete(true)
                    ));

            commands.add(Commands.slash(HEADER_3,"Creates a new team without using the team-registration command.")
                    .addOptions(
                            new OptionData(OptionType.STRING,FIELD_HEADER_3_1,"Team clantag",true).setMaxLength(5),
                            new OptionData(OptionType.STRING,FIELD_HEADER_3_2,"Team region",true).addChoices(serverChoices),
                            new OptionData(OptionType.STRING,FIELD_HEADER_3_3,"Player nickname",true)
                    ));

            commands.add(Commands.slash(HEADER_4,"Provides an approximate value of the seeding; not definitive and may vary.")
                    .addOptions(
                            new OptionData(OptionType.STRING,FIELD_HEADER_4_1,"Player",true).setAutoComplete(true)
                    ));

            commands.add(Commands.slash(HEADER_5,"Clears a team roster but keeps player entries for stat tracking.")
                    .addOptions(
                            new OptionData(OptionType.STRING,FIELD_HEADER_5_1,"Team clantag",true).setAutoComplete(true),
                            new OptionData(OptionType.STRING,FIELD_HEADER_5_2,"Team region",true).addChoices(serverChoices)
                    ));

            commands.add(Commands.slash(HEADER_6,"yes"));
            
            commands.add(Commands.slash(HEADER_16,"(Still under development, only testing purpose)")
                    .addOptions(new OptionData(OptionType.STRING,FIELD_HEADER_16_1,"Team clantag",true).setAutoComplete(true),
                            new OptionData(OptionType.STRING,FIELD_HEADER_16_2,"Team region",true).addChoices(serverChoices)));
            
            commands.add(Commands.slash(HEADER_7,"(Still under development, only testing purpose)")
                    .addOptions(
                            new OptionData(OptionType.STRING,FIELD_HEADER_7_1,"Tournament ID",true).setAutoComplete(true),
                            new OptionData(OptionType.STRING,FIELD_HEADER_7_2,"Teams region",true).addChoices(serverChoices)
                    ));

            commands.add(Commands.slash(HEADER_8,"Gets a player's tier 10 statistics.")
                    .addOptions(
                            new OptionData(OptionType.STRING,FIELD_HEADER_8_1,"Player nickname",true).setAutoComplete(true)
                    ));

            commands.add(Commands.slash(HEADER_9,"Registers a clanless player.")
                    .addOptions(
                            new OptionData(OptionType.STRING,FIELD_HEADER_9_1,"Player nickname",true),
                            new OptionData(OptionType.STRING,FIELD_HEADER_9_2,"Team region",true).addChoices(serverChoices)
                    ));

            commands.add(Commands.slash(HEADER_10,"Removes a player from a team roster.")
                    .addOptions(
                            new OptionData(OptionType.STRING,FIELD_HEADER_10_1,"Player nickname",true).setAutoComplete(true),
                            new OptionData(OptionType.STRING,FIELD_HEADER_10_2,"Team clantag",true).setAutoComplete(true),
                            new OptionData(OptionType.STRING,FIELD_HEADER_10_3,"Team region",true).addChoices(serverChoices)
                    ));
            
            commands.add(Commands.slash(HEADER_11,"Displays the team structure.")
                    .addOptions(
                            new OptionData(OptionType.STRING,FIELD_HEADER_11_1,"Team clantag",true).setAutoComplete(true),
                            new OptionData(OptionType.STRING,FIELD_HEADER_11_2,"Team region",true).addChoices(serverChoices)
                    ));

            commands.add(Commands.slash(HEADER_12,"(Still under development, only testing purpose)")
                    .addOptions(
                            new OptionData(OptionType.STRING,FIELD_HEADER_12_1,"Tournament ID",true).setAutoComplete(true),
                            new OptionData(OptionType.STRING,FIELD_HEADER_12_2,"Teams region",true).addChoices(serverChoices)
                    ));

            commands.add(Commands.slash(HEADER_13,"Shows teams registered in the bot's records.")
                    .addOptions(
                            new OptionData(OptionType.STRING,FIELD_HEADER_13_1,"Team region",true).addChoices(serverChoices)
                    ));

            commands.add(Commands.slash(HEADER_14,"Registers a team for stat tracking.")
                    .addOptions(
                            new OptionData(OptionType.STRING,FIELD_HEADER_14_1,"Team clantag",true),
                            new OptionData(OptionType.STRING,FIELD_HEADER_14_2,"Team region",true).addChoices(serverChoices),
                            new OptionData(OptionType.STRING,FIELD_HEADER_14_3,"1st player of the team",true),
                            new OptionData(OptionType.STRING,FIELD_HEADER_14_4,"2nd player of the team",true),
                            new OptionData(OptionType.STRING,FIELD_HEADER_14_5,"3rd player of the team",true),
                            new OptionData(OptionType.STRING,FIELD_HEADER_14_6,"4th player of the team",true),
                            new OptionData(OptionType.STRING,FIELD_HEADER_14_7,"5th player of the team",true),
                            new OptionData(OptionType.STRING,FIELD_HEADER_14_8,"6th player of the team",true),
                            new OptionData(OptionType.STRING,FIELD_HEADER_14_9,"7th player of the team",true),
                            new OptionData(OptionType.STRING,FIELD_HEADER_14_10,"8th player of the team",false),
                            new OptionData(OptionType.STRING,FIELD_HEADER_14_11,"9th player of the team",false),
                            new OptionData(OptionType.STRING,FIELD_HEADER_14_12,"10th player of the team",false)
                    ));

            commands.add(Commands.slash(HEADER_15,"Gets a team's statistics.")
                    .addOptions(
                            new OptionData(OptionType.STRING,FIELD_HEADER_15_1,"Team clantag",true).setAutoComplete(true),
                            new OptionData(OptionType.STRING,FIELD_HEADER_15_2,"Team region",true).addChoices(serverChoices)
                    ));
            guild.updateCommands().addCommands(commands).queue();
        }
    }
    
    protected class CommandMethods{
        String callerId;
        SlashCommandInteractionEvent evt;
        public CommandMethods(@NotNull SlashCommandInteractionEvent evt,String userId){
            this.evt=evt;
            this.callerId=userId;
        }

        private void addExistingPlayer(){
            EmbedBuilder eb=new EmbedBuilder();
            evt.deferReply(false).queue(hook->{
                try{
                    int clantag=evt.getOption(FIELD_HEADER_1_2).getAsInt();
                    long player=evt.getOption(FIELD_HEADER_1_1).getAsLong();
                    String realm=evt.getOption(FIELD_HEADER_1_3).getAsString().toUpperCase();

                    if(gd.checkCallerDiscordId(callerId,clantag,realm)){
                        id.teamRegistration(clantag,player,callerId,realm);
                        eb.setColor(Color.GREEN).addField(MESSAGE_5,gd.checkPlayerByID(player)+" has been added to "+gd.checkClantagByID(clantag,realm)+" roster",false);
                    }else{
                        eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE,false);
                    }
                }catch(Exception e){
                    eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6+" or register first your account using register-player command",false);
                    uc.log(Level.SEVERE,e.getMessage(),e);
                }
                hook.sendMessageEmbeds(eb.build()).queue();
            });
        }

        private void checkPlayer(){
            EmbedBuilder eb=new EmbedBuilder();
            evt.deferReply(false).queue(hook->{
                try{
                    long player=evt.getOption(FIELD_HEADER_2_1).getAsLong();
                    hook.sendMessageEmbeds(bca.checkPlayer(player)).queue();
                }catch(Exception e){
                    hook.sendMessageEmbeds(eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6+" or register first your account using register-player command",false).build()).queue();
                    uc.log(Level.SEVERE,e.getMessage(),e);
                }
            });
        }

        private void createNewTeam(){
            EmbedBuilder eb=new EmbedBuilder();
            evt.deferReply(false).queue(hook->{
                MessageEmbed me=null;
                try{
                    String clantag=evt.getOption(FIELD_HEADER_3_1).getAsString().toUpperCase();
                    String player=evt.getOption(FIELD_HEADER_3_3).getAsString().trim();
                    String realm=evt.getOption(FIELD_HEADER_3_2).getAsString().toUpperCase();
                    if(clantag!=null&&!clantag.isEmpty()&&!clantag.equalsIgnoreCase("NULL")){
                        Mvc1 data=bca.checkDiscordInput(player,realm);
                        me=ca.createTeam(data,jh.getClanData(clantag,realm),callerId);
                    }else{
                        me=eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_2,false).build();
                    }
                }catch(Exception e){
                    me=eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6,false).build();
                    uc.log(Level.SEVERE,e.getMessage(),e);
                }
                hook.sendMessageEmbeds(me).queue();
            });
        }

        private void formulaStats(){
            EmbedBuilder eb=new EmbedBuilder();
            evt.deferReply(false).queue(hook->{
                try{
                    long player=evt.getOption(FIELD_HEADER_4_1).getAsLong();
                    hook.sendMessageEmbeds(ca.playerWeight(player)).addActionRow(Button.primary("refresh-formula-stats:"+player,"Refresh")).queue();
                }catch(Exception e){
                    hook.sendMessageEmbeds(eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6+" or register first your account for stat tracking using register-player command",false).build()).queue();
                    uc.log(Level.SEVERE,e.getMessage(),e);
                }
            });
        }

        private void freeupRoster(){
            EmbedBuilder eb=new EmbedBuilder();
            evt.deferReply(false).queue(hook->{
                try{
                    int clantag=evt.getOption(FIELD_HEADER_5_1).getAsInt();
                    String realm=evt.getOption(FIELD_HEADER_5_2).getAsString();
                    if(gd.checkCallerDiscordId(callerId,clantag,realm)){
                        dd.freeupRoster(clantag,realm);
                        eb.setColor(Color.GREEN).addField(MESSAGE_5,gd.checkClantagByID(clantag,realm)+" roster has been removed from team list", false);
                    }else{
                        eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE,false);
                    }
                }catch(Exception e){
                    eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6,false);
                    uc.log(Level.SEVERE,e.getMessage(),e);
                }
                hook.sendMessageEmbeds(eb.build()).queue();
            });
        }

        private void help(){
            EmbedBuilder eb=new EmbedBuilder();
            evt.deferReply(false).queue(hook->{
                eb.setColor(Color.PINK);
                for(Map.Entry<String,String> entry:jh.getHelpCommandData().entrySet()){
                    eb.addField(entry.getKey(),entry.getValue(),false);
                }
                hook.sendMessageEmbeds(eb.build()).queue();
            });
        }

        private void ingameRoster(){
            EmbedBuilder eb=new EmbedBuilder();
            evt.deferReply(false).queue(hook->{
                try{
                    int clanId=evt.getOption(FIELD_HEADER_16_1).getAsInt();
                    String realm=evt.getOption(FIELD_HEADER_16_2).getAsString().toUpperCase();
                    if(clanId!=0){
                        double team=ba.getIngameTeamWinrate(clanId);
                        String team2=ba.getIngameTeamRoster(clanId,realm);
                        if(!team2.isEmpty()){
                            eb.setColor(Color.GREEN)
                                    .addField("Original:",team+"%",false)
                                    .addField("Wargaming's page:",Math.round(team)+"%",false)
                                    .addField("Roster:",team2,false);
                        }else{
                            eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_3,false);
                        }
                    }else{
                        eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6,false);
                    }
                }catch(Exception e){
                    eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6,false);
                    uc.log(Level.SEVERE,e.getMessage(),e);
                }
                hook.sendMessageEmbeds(eb.build()).queue();
            });
        }

        private void ingameTeamLeaderboard(){
            EmbedBuilder eb=new EmbedBuilder();
            evt.deferReply(false).queue(hook->{
                try{
                    int tourneyId=evt.getOption(FIELD_HEADER_7_1).getAsInt();
                    String realm=evt.getOption(FIELD_HEADER_7_2).getAsString().toUpperCase();

                    List<Interfaces.LeaderboardEntry> teams=ba.getIngameLeaderboardData(tourneyId,realm);

                    eb.setTitle(MESSAGE_5).setColor(Color.GREEN);

                    if(teams.isEmpty()){
                        eb.setDescription("No teams were found with registered battles for this tournament.");
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
                    eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6,false);
                    uc.log(Level.SEVERE,e.getMessage(),e);
                }
                hook.sendMessageEmbeds(eb.build()).queue();
            });
        }

        private void personalStatsTier10(){
            EmbedBuilder eb=new EmbedBuilder();
            evt.deferReply(false).queue(hook->{
                try{
                    long player=evt.getOption(FIELD_HEADER_8_1).getAsLong();
                    hook.sendMessageEmbeds(bca.getTier10Stats(player)).addActionRow(Button.primary("refresh-personal-stats:"+player,"Refresh")).queue();
                }catch(Exception e){
                    hook.sendMessageEmbeds(eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6,false).build()).queue();
                    uc.log(Level.SEVERE,e.getMessage(),e);
                }
            });
        }

        private void registerPlayer(){
            EmbedBuilder eb=new EmbedBuilder();
            evt.deferReply(false).queue(hook->{
                try{
                    String player=evt.getOption(FIELD_HEADER_9_1).getAsString().trim();
                    String realm=evt.getOption(FIELD_HEADER_9_2).getAsString().toUpperCase();
                    Mvc1 data=bca.checkDiscordInput(player,realm);

                    long accId=data.getAcoountId();
                    String nickname=data.getNickname();
                    if(accId!=0){
                        if(!gd.checkUserData(accId,realm)){
                            id.registerPlayer(accId,nickname,realm,data.getLastBattleTime(),data.getUpdatedAt());
                            eb.setColor(Color.GREEN).addField(MESSAGE_5,nickname+" has been registered successfully!",false);
                        }else{
                            eb.setColor(Color.YELLOW).addField(MESSAGE_4,nickname+" is already registered",false);
                        }
                    }else{
                        eb.setColor(Color.RED).addField(MESSAGE_4,MESSAGE_6,false);
                    }
                }catch(Exception e){
                    eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6,false);
                    uc.log(Level.SEVERE,e.getMessage(),e);
                }
                hook.sendMessageEmbeds(eb.build()).queue();
            });
        }

        private void removeFromRoster(){
            EmbedBuilder eb=new EmbedBuilder();
            evt.deferReply(false).queue(hook->{
                try{
                    long player=evt.getOption(FIELD_HEADER_10_1).getAsLong();
                    int clantag=evt.getOption(FIELD_HEADER_10_2).getAsInt();
                    String realm=evt.getOption(FIELD_HEADER_10_3).getAsString();
                    if(gd.checkCallerDiscordId(callerId,clantag,realm)){
                        dd.removeFromRoster(player,clantag);
                        eb.setColor(Color.GREEN).addField(MESSAGE_5,gd.checkPlayerByID(player)+" has been removed from the team roster",false);
                    }else{
                        eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE,false);
                    }
                }catch(Exception e){
                    eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6,false);
                    uc.log(Level.SEVERE,e.getMessage(),e);
                }
                hook.sendMessageEmbeds(eb.build()).queue();
            });
        }

        private void rosterCommand(){
            EmbedBuilder eb=new EmbedBuilder();
            evt.deferReply(false).queue(hook->{
                try{
                    int clanId=evt.getOption(FIELD_HEADER_11_1).getAsInt();
                    String realm=evt.getOption(FIELD_HEADER_11_2).getAsString().toUpperCase();
                    if(clanId!=0){
                        double team=ba.getTeamWinrate(clanId,realm);
                        String team2=ba.getTeamRoster(clanId,realm);

                        if(!team2.isEmpty()){
                            eb.setColor(Color.GREEN)
                                    .addField("Original:",team+"%",false)
                                    .addField("Wargaming's page:",Math.round(team)+"%",false)
                                    .addField("Roster:",team2,false);
                        }else{
                            eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_3,false);
                        }
                    }else{
                        eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6,false);
                    }
                }catch(Exception e){
                    eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6+" or register first your team using team-registration or create-new-team commands",false);
                    uc.log(Level.SEVERE,e.getMessage(),e);
                }
                hook.sendMessageEmbeds(eb.build()).queue();
            });
        }

        private void seedTeams(){
            EmbedBuilder eb=new EmbedBuilder();
            evt.deferReply().queue(hook->{
                int tourneyId=evt.getOption(FIELD_HEADER_12_1).getAsInt();
                String realm=evt.getOption(FIELD_HEADER_12_2).getAsString();
                try{
                    List<MessageEmbed> embeds=bca.seedTeams(tourneyId,realm);
                    if(!embeds.isEmpty()){
                        hook.sendMessageEmbeds(embeds).queue();
                    }else{
                        hook.sendMessage("No se pudo generar ningún grupo.").queue();
                    }
                }catch(Exception e){
                    uc.log(Level.SEVERE,e.getMessage(),e);
                    hook.sendMessageEmbeds(eb.setColor(Color.RED).addField(MESSAGE_4,MESSAGE_7,false).build()).queue();
                }
            });
        }

        private void teamLeaderboard(){
            EmbedBuilder eb=new EmbedBuilder();
            evt.deferReply(false).queue(hook->{
                try{
                    String realm=evt.getOption(FIELD_HEADER_13_1).getAsString().toUpperCase();
                    eb.setColor(Color.GREEN).addField(MESSAGE_5,ba.teamLeaderboard(realm),false);
                }catch(Exception e){
                    eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6,false);
                    uc.log(Level.SEVERE,e.getMessage(),e);
                }
                hook.sendMessageEmbeds(eb.build()).queue();
            });
        }

        private void teamRegistrationCommand(){
            EmbedBuilder eb=new EmbedBuilder();
            evt.deferReply(false).queue(hook->{
                try{
                    OptionMapping[] values={
                        evt.getOption(FIELD_HEADER_14_3),
                        evt.getOption(FIELD_HEADER_14_4),
                        evt.getOption(FIELD_HEADER_14_5),
                        evt.getOption(FIELD_HEADER_14_6),
                        evt.getOption(FIELD_HEADER_14_7),
                        evt.getOption(FIELD_HEADER_14_8),
                        evt.getOption(FIELD_HEADER_14_9),
                        evt.getOption(FIELD_HEADER_14_10),
                        evt.getOption(FIELD_HEADER_14_11),
                        evt.getOption(FIELD_HEADER_14_12)
                    };
                    String clantag=evt.getOption(FIELD_HEADER_14_1).getAsString().toUpperCase();
                    String realm=evt.getOption(FIELD_HEADER_14_2).getAsString().toUpperCase();
                    if(clantag!=null&&!clantag.isEmpty()&&!clantag.equalsIgnoreCase("NULL")){
                        for(OptionMapping value:values){
                            if(value!=null){
                                Mvc1 data=bca.checkDiscordInput(value.getAsString(),realm);
                                Mvc2 data2=jh.getClanData(clantag,realm);
                                MessageEmbed singleResult=ca.createTeam(data,data2,callerId);
                                if(singleResult!=null&&!singleResult.getFields().isEmpty()){
                                    for(MessageEmbed.Field field:singleResult.getFields()){
                                        eb.setColor(Color.GREEN).addField(field);
                                    }
                                }
                            }
                        }
                    }else{
                        eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_2,false);
                    }
                }catch(Exception e){
                    eb.setColor(Color.RED).addField(MESSAGE_4,MESSAGE_7,false);
                    uc.log(Level.SEVERE,e.getMessage(),e);
                }
                hook.sendMessageEmbeds(eb.build()).queue();
            });
        }

        private void teamStatsCommand(){
            EmbedBuilder eb=new EmbedBuilder();
            evt.deferReply(false).queue(hook->{
                try{
                    int clanId=evt.getOption(FIELD_HEADER_15_1).getAsInt();
                    String realm=evt.getOption(FIELD_HEADER_15_2).getAsString().toUpperCase();
                    
                    if(clanId!=0){
                        double val=ba.getTeamWinrate(clanId,realm);
                        if(val!=0.0){
                            eb.setColor(Color.GREEN)
                                    .setTitle("Your team win rate is:")
                                    .addField("Original:",val+"%",false)
                                    .addField("Wargaming's page:",Math.round(val)+"%",false);
                        }else{
                            eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_3,false);
                        }
                    }else{
                        eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6,false);
                    }
                }catch(Exception e){
                    eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6+" or register first your team using team-registration or create-new-team commands",false);
                    uc.log(Level.SEVERE,e.getMessage(),e);
                }
                hook.sendMessageEmbeds(eb.build()).queue();
            });
        }
    }

    protected class CommandActions{
        private MessageEmbed createTeam(Mvc1 data,Mvc2 data2,String callerId){
            EmbedBuilder eb=new EmbedBuilder();
            try{
                long accId=data.getAcoountId();
                String player=data.getNickname();
                long lastBattleTime=data.getLastBattleTime();
                long updatedAt=data.getUpdatedAt();

                int clanId=data2.getClanId();
                String clantag=data2.getClantag();
                String realm=data2.getRealm();
                long updatedAt2=data2.getUpdatedAt();

                if(accId!=0&&clanId!=0){
                    boolean userExists=gd.checkUserData(accId,realm);
                    boolean clanExists=gd.checkClanData(clanId,realm);
                    boolean teammateExists=gd.checkTeamPlayer(accId,realm);

                    if(!userExists){
                        id.registerPlayer(accId,player,realm,lastBattleTime,updatedAt);
                    }
                    if(!clanExists){
                        id.setClanInfo(clanId,clantag,realm,updatedAt2);
                    }
                    if(!teammateExists){
                        id.teamRegistration(clanId,accId,callerId,realm);
                        eb.setColor(Color.GREEN).addField(MESSAGE_5,gd.checkPlayerByID(accId)+" added to the "+clantag+"'s team roster",false);
                    }else{
                        eb.setColor(Color.YELLOW).addField(MESSAGE_4,gd.checkPlayerByID(accId)+" is on another team",false);
                    }
                    eb.setColor(Color.GREEN).addField(MESSAGE_5,clantag+MESSAGE_8,false);
                }else{
                    eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6,false);
                }
            }catch(Exception e){
                eb.setColor(Color.RED).addField(MESSAGE_4,MESSAGE_7,false);
                uc.log(Level.SEVERE,e.getMessage(),e);
            }
            return eb.build();
        }

        private MessageEmbed playerWeight(long accId){
            EmbedBuilder eb=new EmbedBuilder();
            try{
                if(accId!=0){
                    eb.setColor(Color.GREEN).addField("Your win rate using Wargaming's tournament formula is:",ba.calculatePlayerWeight(accId)+"%",false);
                }else{
                    eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6,false);
                }
            }catch(Exception e){
                eb.setColor(Color.RED).addField(MESSAGE_4,MESSAGE_7,false);
                uc.log(Level.SEVERE,e.getMessage(),e);
            }
            return eb.build();
        }
    }
}
