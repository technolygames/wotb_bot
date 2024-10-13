package logic;

import dbconnection.DeleteData;
import dbconnection.GetData;
import dbconnection.InsertData;

import org.jetbrains.annotations.NotNull;
import io.github.cdimascio.dotenv.Dotenv;

import java.awt.Color;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import mvc.Mvc1;
import mvc.Mvc2;

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
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;

public class BotLogic{
    ShardManager manager;
    public BotLogic() throws InterruptedException{
        Dotenv token=Dotenv.configure().directory("data").load();
        DefaultShardManagerBuilder builder=DefaultShardManagerBuilder.createDefault(token.get("TOKEN"));
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.playing("Testing"));
        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        manager=builder.build();
        EventListeners evt=new EventListeners();
        manager.addEventListener(evt);
        evt.clearCommands();
    }

    private class EventListeners extends net.dv8tion.jda.api.hooks.ListenerAdapter{
        private static final String MESSAGE="You are not the leader of this team";
        private static final String MESSAGE_2="Cannot be NULL";
        private static final String MESSAGE_3="No data";
        private static final String MESSAGE_4="Something gone wrong";
        private static final String MESSAGE_5="Results";

        @Override
        public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent evt) throws NullPointerException{
            String callerId=evt.getUser().getId();
            EmbedBuilder eb=new EmbedBuilder();
            switch(evt.getName()){
                case "add-existing-player":{
                    evt.deferReply(false).queue(m->{
                        int clantag=evt.getOption("clantag").getAsInt();
                        int player=evt.getOption("player").getAsInt();
                        String realm=evt.getOption("server").getAsString().toUpperCase();
                        if(new GetData().verifyCallerDiscordId(callerId,clantag,realm)){
                            new InsertData().teamRegistration(clantag,player,callerId,realm);
                            eb.setColor(Color.GREEN).addField(MESSAGE_5,new GetData().checkPlayerByID(player)+" has been added to "+new GetData().checkClantagByID(clantag,realm)+" roster",false);
                        }else{
                            eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE,false);
                        }
                        m.sendMessageEmbeds(eb.build()).queue();
                    });
                    break;
                }
                case "check-player":{
                    evt.deferReply(false).queue(m->{
                        int nickname=evt.getOption("player2").getAsInt();
                        eb.setColor(Color.GREEN).addField(MESSAGE_5,new BotActions().checkPlayer(nickname),false);
                        m.sendMessageEmbeds(eb.build()).queue();
                    });
                    break;
                }
                case "create-new-team":{
                    evt.deferReply(false).queue(m->{
                        String clantag=evt.getOption("clantag3").getAsString().toUpperCase();
                        var player=evt.getOption("player3");
                        var code=evt.getOption("code3");
                        String realm=evt.getOption("server3").getAsString().toUpperCase();
                        if(player!=null){
                            m.sendMessageEmbeds(createTeam(new JsonHandler().getAccountData(player.getAsString(),realm),new JsonHandler().getClanData(clantag,realm),callerId)).queue();
                        }else if(code!=null){
                            m.sendMessageEmbeds(createTeam(new JsonHandler().getAccountData(code.getAsInt(),realm),new JsonHandler().getClanData(clantag,realm),callerId)).queue();
                        }
                    });
                    
                    break;
                }
                case "formula-stats":{
                    evt.deferReply(false).queue(m->{
                        int player=evt.getOption("player11").getAsInt();
                        m.sendMessageEmbeds(playerWeight(player)).addActionRow(Button.primary("refresh-formula-stats:"+player,"Refresh")).queue();
                    });
                    break;
                }
                case "freeup-roster":{
                    evt.deferReply(false).queue(m->{
                        int clantag=evt.getOption("clantag4").getAsInt();
                        String realm=evt.getOption("server4").getAsString();
                        if(new GetData().verifyCallerDiscordId(callerId,clantag,realm)){
                            new DeleteData().freeupRoster(clantag,realm);
                            eb.setColor(Color.GREEN).addField(MESSAGE_5,new GetData().checkClantagByID(clantag,realm)+" roster has been removed from team list",false);
                        }else{
                            eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE,false);
                        }
                        m.sendMessageEmbeds(eb.build()).queue();
                    });
                    break;
                }
                case "help":{
                    evt.deferReply(false).queue(m->{
                        m.sendMessageEmbeds(help()).queue();
                    });
                    break;
                }
                case "personal-stats-tier10":{
                    evt.deferReply(false).queue(m->{
                        int player=evt.getOption("player5").getAsInt();
                        m.sendMessageEmbeds(personalStats(player)).addActionRow(Button.primary("refresh-personal-stats:"+player,"Refresh")).queue();
                    });
                    break;
                }
                case "register-player":{
                    evt.deferReply(false).queue(m->{
                        var player=evt.getOption("player6");
                        var code=evt.getOption("code6");
                        String realm=evt.getOption("server6").getAsString().toUpperCase();
                        if(player!=null){
                            m.sendMessageEmbeds(registerPlayer(new JsonHandler().getAccountData(player.getAsString(),realm),realm)).queue();
                        }else if(code!=null){
                            m.sendMessageEmbeds(registerPlayer(new JsonHandler().getAccountData(code.getAsInt(),realm),realm)).queue();
                        }
                    });
                    break;
                }
                case "remove-from-roster":{
                    evt.deferReply(false).queue(m->{
                        int player=evt.getOption("player7").getAsInt();
                        int clantag=evt.getOption("clantag7").getAsInt();
                        String realm=evt.getOption("server7").getAsString();
                        if(new GetData().verifyCallerDiscordId(callerId,clantag,realm)){
                            eb.setColor(Color.GREEN).addField(MESSAGE_5,new GetData().checkPlayerByID(player)+" has been removed from the team roster",false);
                            new DeleteData().removeFromRoster(player,clantag);
                        }else{
                            eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE,false);
                        }
                        m.sendMessageEmbeds(eb.build()).queue();
                    });
                    break;
                }
                case "roster":{
                    evt.deferReply(false).queue(m->{
                        int clanId=evt.getOption("clantag8").getAsInt();
                        String realm=evt.getOption("server8").getAsString().toUpperCase();
                        MessageEmbed val=roster(clanId,realm);
                        if(!val.getFields().get(2).getValue().equals(MESSAGE_3)){
                            m.sendMessageEmbeds(val).addActionRow(Button.primary("refresh-roster:"+clanId+":"+realm,"Refresh")).queue();
                        }else{
                            m.sendMessageEmbeds(val).queue();
                        }
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
                        String clantag=evt.getOption("clantag9").getAsString().toUpperCase();
                        String realm=evt.getOption("server9").getAsString().toUpperCase();
                        if(!clantag.equals("NULL")){
                            int clanId=new JsonHandler().getClanData(clantag,realm).getClanId();
                            if(!new GetData().checkClanData(clantag,realm)){
                                new InsertData().setClanInfo(clanId,clantag,realm);
                                for(String value:values){
                                    int wotbId=new JsonHandler().getAccountData(value,realm).getAcoountId();
                                    if(!new GetData().checkUserData(wotbId,realm)){
                                        new InsertData().registerPlayer(wotbId,value,realm);
                                        new InsertData().teamRegistration(clanId,wotbId,callerId,realm);
                                    }else{
                                        new InsertData().teamRegistration(clanId,wotbId,callerId,realm);
                                    }
                                }
                                eb.setColor(Color.GREEN).addField(MESSAGE_5,clantag+" team has been registered!",false);
                            }else{
                                for(String value:values){
                                    int wotbId=new JsonHandler().getAccountData(value,realm).getAcoountId();
                                    if(!new GetData().checkUserData(wotbId,realm)){
                                        new InsertData().registerPlayer(wotbId,value,realm);
                                        new InsertData().teamRegistration(clanId,wotbId,callerId,realm);
                                    }else{
                                        new InsertData().teamRegistration(clanId,wotbId,callerId,realm);
                                    }
                                }
                                eb.setColor(Color.GREEN).addField(MESSAGE_5,clantag+" team has been registered!",false);
                            }
                        }else{
                            eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_2,false);
                        }
                        m.sendMessageEmbeds(eb.build()).queue();
                    });
                    break;
                }
                case "team-stats":{
                    evt.deferReply(false).queue(m->{
                        int clanId=evt.getOption("clantag10").getAsInt();
                        String realm=evt.getOption("server10").getAsString().toUpperCase();
                        MessageEmbed val=teamStats(clanId,realm);
                        if(!val.getTitle().equals(MESSAGE_4)){
                            m.sendMessageEmbeds(val).addActionRow(Button.primary("refresh-team-stats:"+clanId+":"+realm,"Refresh")).queue();
                        }else{
                            m.sendMessage(MESSAGE_3).queue();
                        }
                    });
                    break;
                }
                default:break;
            }
        }

        @Override
        public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent evt){
            switch(evt.getName()){
                case "add-existing-player":{
                    switch(evt.getFocusedOption().getName()){
                        case "player":{
                            setChoices(evt,new BotActions().getClanlessPlayersAsChoices());
                            break;
                        }
                        case "clantag":{
                            setChoices(evt,new BotActions().getClanAsChoices());
                            break;
                        }
                        default:break;
                    }
                    break;
                }
                case "check-player":{
                    if(evt.getFocusedOption().getName().equals("player2")){
                        setChoices(evt,new BotActions().getPlayersAsChoices());
                    }
                    break;
                }
                case "formula-stats":{
                    if(evt.getFocusedOption().getName().equals("player11")){
                        setChoices(evt,new BotActions().getThousandBattlesPlayerAsChoice());
                    }
                    break;
                }
                case "freeup-roster":{
                    if(evt.getFocusedOption().getName().equals("clantag4")){
                        setChoices(evt,new BotActions().getClanAsChoices());
                    }
                    break;
                }
                case "personal-stats-tier10":{
                    if(evt.getFocusedOption().getName().equals("player5")){
                        setChoices(evt,new BotActions().getPlayersAsChoices());
                    }
                    break;
                }
                case "remove-from-roster":{
                    switch(evt.getFocusedOption().getName()){
                        case "player7":{
                            setChoices(evt,new BotActions().getNotNullPlayersAsChoices());
                            break;
                        }
                        case "clantag7":{
                            setChoices(evt,new BotActions().getClanAsChoices());
                            break;
                        }
                        default:break;
                    }
                    break;
                }
                case "roster":{
                    if(evt.getFocusedOption().getName().equals("clantag8")){
                        setChoices(evt,new BotActions().getClanAsChoices());
                    }
                    break;
                }
                case "team-stats":{
                    if(evt.getFocusedOption().getName().equals("clantag10")){
                        setChoices(evt,new BotActions().getClanAsChoices());
                    }
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
                case "refresh-formula-stats":{
                    evt.deferEdit().queue(s->s.editOriginalEmbeds(playerWeight(Integer.parseInt(val[1]))).queue());
                    break;
                }
                case "refresh-personal-stats":{
                    evt.deferEdit().queue(s->s.editOriginalEmbeds(personalStats(Integer.parseInt(val[1]))).queue());
                    break;
                }
                case "refresh-roster":{
                    evt.deferEdit().queue(s->s.editOriginalEmbeds(roster(Integer.parseInt(val[1]),val[2])).queue());
                    break;
                }
                case "refresh-team-stats":{
                    evt.deferEdit().queue(s->s.editOriginalEmbeds(teamStats(Integer.parseInt(val[1]),val[2])).queue());
                    break;
                }
                default:break;
            }
        }

        private void setChoices(CommandAutoCompleteInteraction evt,List<Command.Choice> choices){
            String value=evt.getFocusedOption().getValue();
            List<Command.Choice> filteredChoices=choices.stream()
                    .filter(choice->choice.getName().contains(value))
                    .collect(Collectors.toList());
            Collections.shuffle(filteredChoices);
            List<Command.Choice> choices2=filteredChoices.stream()
                    .limit(5)
                    .collect(Collectors.toList());
            evt.replyChoices(choices2).queue();
        }

        private void clearCommands(){
            manager.getShards().forEach(jda->
                jda.retrieveCommands().queue(commands->{
                    for(var command:commands){
                        jda.deleteCommandById(command.getId()).queue();
                    }
                }));

            manager.getGuildCache().forEach(guild->{
                guild.retrieveCommands().queue(commands->{
                    for(var command:commands){
                        guild.deleteCommandById(command.getId()).queue();
                    }
                });
                
                registerCommands(guild);
            });
        }

        @Override
        public void onGuildJoin(@NotNull GuildJoinEvent evt){
            registerCommands(evt.getGuild());
        }

        @Override
        public void onGuildReady(@NotNull GuildReadyEvent evt){
            registerCommands(evt.getGuild());
        }

        private void registerCommands(Guild guild){
            List<CommandData> commands=new ArrayList<>();

            List<Command.Choice> serverChoices=Arrays.asList(
                    new Command.Choice("NA","NA"),
                    new Command.Choice("EU","EU"),
                    new Command.Choice("ASIA","ASIA")
            );

            commands.add(Commands.slash("add-existing-player","Adds a clanless player to a team roster.")
                    .addOptions(
                            new OptionData(OptionType.STRING,"clantag","Team clantag",true).setAutoComplete(true),
                            new OptionData(OptionType.STRING,"player","Player to be added",true).setAutoComplete(true),
                            new OptionData(OptionType.STRING,"server","Player and team region",true).addChoices(serverChoices)
                    ));

            commands.add(Commands.slash("check-player","Checks an specific player on bot's registry.")
                    .addOptions(
                            new OptionData(OptionType.STRING,"player2","Player to be checked",true).setAutoComplete(true)
                    ));

            commands.add(Commands.slash("create-new-team","Creates a new team without using team-registration command.")
                    .addOptions(
                            new OptionData(OptionType.STRING,"clantag3","Team clantag",true).setMaxLength(5),
                            new OptionData(OptionType.STRING,"server3","Team region",true).addChoices(serverChoices),
                            new OptionData(OptionType.STRING,"player3","Player nickname",false),
                            new OptionData(OptionType.STRING,"code3","Player ID",false)
                    ));
            
            commands.add(Commands.slash("formula-stats","Es un aproximado, no es definitivo")
                    .addOptions(
                            new OptionData(OptionType.STRING,"player11","Jugador",true).setAutoComplete(true)
                    ));

            commands.add(Commands.slash("freeup-roster","Free team roster but keeps player entries for stat tracking")
                    .addOptions(
                            new OptionData(OptionType.STRING,"clantag4","Team clantag",true).setAutoComplete(true),
                            new OptionData(OptionType.STRING,"server4","Team region",true).addChoices(serverChoices)
                    ));

            commands.add(Commands.slash("help","yes"));
            
            commands.add(Commands.slash("personal-stats-tier10","Gets tier 10 player stats.")
                    .addOptions(
                            new OptionData(OptionType.STRING,"player5","Player nickname",true).setAutoComplete(true)
                    ));

            commands.add(Commands.slash("register-player","Register a new teammate.")
                    .addOptions(
                            new OptionData(OptionType.STRING,"server6","Team region",true).addChoices(serverChoices),
                            new OptionData(OptionType.STRING,"player6","Player nickname",false),
                            new OptionData(OptionType.INTEGER,"code6","Player ID",false)
                    ));

            commands.add(Commands.slash("remove-from-roster","Removes a player from a team roster.")
                    .addOptions(
                            new OptionData(OptionType.STRING,"player7","Player nickname",true).setAutoComplete(true),
                            new OptionData(OptionType.STRING,"clantag7","Team clantag",true).setAutoComplete(true),
                            new OptionData(OptionType.STRING,"server7","Team region",true).addChoices(serverChoices)
                    ));
            
            commands.add(Commands.slash("roster","Gets team structure.")
                    .addOptions(
                            new OptionData(OptionType.STRING,"clantag8","Team clantag",true).setAutoComplete(true),
                            new OptionData(OptionType.STRING,"server8","Team region",true).addChoices(serverChoices)
                    ));

            commands.add(Commands.slash("team-registration","Register a team for stat tracking.")
                    .addOptions(
                            new OptionData(OptionType.STRING,"clantag9","Team clantag",true),
                            new OptionData(OptionType.STRING,"server9","Team region",true).addChoices(serverChoices),
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

            commands.add(Commands.slash("team-stats","Gets team stats.")
                    .addOptions(
                            new OptionData(OptionType.STRING,"clantag10","Team clantag",true).setAutoComplete(true),
                            new OptionData(OptionType.STRING,"server10","Team region",true).addChoices(serverChoices)
                    ));

            guild.updateCommands().addCommands(commands).complete();
        }

        protected MessageEmbed playerWeight(int accId){
            EmbedBuilder eb=new EmbedBuilder();
            eb.setColor(Color.GREEN).addField("Your win rate using Wargaming's tournament formula is:",new BotActions().calculatePlayerWeight(accId)+"%",false);
            return eb.build();
        }

        protected MessageEmbed personalStats(int accId){
            return new BotActions().getTier10Stats(accId);
        }

        protected MessageEmbed teamStats(int clanId,String realm){
            return new BotActions().getTeamWinrate(clanId,realm);
        }

        protected MessageEmbed roster(int clanId,String realm){
            return new BotActions().getRoster(clanId,realm);
        }

        protected MessageEmbed registerPlayer(Mvc1 data,String realm){
            int accId=data.getAcoountId();
            String nickname=data.getNickname();
            EmbedBuilder eb=new EmbedBuilder();
            if(!new GetData().checkUserData(accId,realm)){
                new InsertData().registerPlayer(accId,nickname,realm);
                eb.setColor(Color.GREEN).addField(MESSAGE_5,nickname+" has been registered successfully!",false);
            }else{
                eb.setColor(Color.YELLOW).addField(MESSAGE_4,nickname+" is already registered",false);
            }
            return eb.build();
        }
        
        protected MessageEmbed createTeam(Mvc1 data,Mvc2 data2,String callerId){
            int wotbId=data.getAcoountId();
            String player=data.getNickname();
            
            int clanId=data2.getClanId();
            String clantag=data2.getClantag();
            String realm=data2.getRealm();
            
            EmbedBuilder eb=new EmbedBuilder();
            if(!clantag.equals("NULL")){
                GetData val=new GetData();
                boolean userExists=val.checkUserData(wotbId,realm);
                boolean clanExists=val.checkClanData(clantag,realm);
                int value=val.checkClanIdByTag(clantag,realm);
                boolean teamExists=val.checkTeam(value,realm);

                InsertData val2=new InsertData();
                if(!userExists&&!clanExists){
                    val2.setClanInfo(clanId,clantag,realm);
                    val2.registerPlayer(wotbId,player,realm);
                    val2.teamRegistration(clanId,wotbId,callerId,realm);
                    eb.setColor(Color.GREEN).addField(MESSAGE_5,clantag+" has been created!",false);
                }else if(!clanExists){
                    val2.setClanInfo(clanId,clantag,realm);
                    val2.teamRegistration(clanId,wotbId,callerId,realm);
                    eb.setColor(Color.GREEN).addField(MESSAGE_5,clantag+" has been created!",false);
                }else if(!teamExists){
                    val2.teamRegistration(clanId,wotbId,callerId,realm);
                }else{
                    eb.setColor(Color.YELLOW).addField(MESSAGE_4,clantag+" already exists",false);
                }
            }else{
                eb.setColor(Color.RED).addField(MESSAGE_4,MESSAGE_2,false);
            }
            return eb.build();
        }

        protected MessageEmbed help(){
            EmbedBuilder eb=new EmbedBuilder();
            eb.setColor(Color.ORANGE)
            .addField("/add-existing-player","adds a player without a clan to a team roster. If he is not registered, you must use the /register-player command.",false)
            .addField("/check-player","check the data of a specific player in the bot's records.",false)
            .addField("/create-new-team","creates a new team without the need to enter the remaining 9 players. This command can be used in conjunction with the /add-existing-player command.",false)
            .addField("/formula-stats","check the statistics of a player with less than the number of games requested by the game to calculate the weight of a player on a team.",false)
            .addField("/freeup-roster","clears the record of a team. Clan data is kept in the bot logs.",false)
            .addField("/personal-stats-tier10","check a player's tier 10 statistics.",false)
            .addField("/register-player","registers a player in the bot database. The player's nickname or ID can be used at the time of registration.",false)
            .addField("/remove-from-roster","removes a player from a team roster.",false)
            .addField("/roster","check a team's statistics. The numbers shown may vary and may be approximate, not definitive.",false)
            .addField("/team-registration","registers a team with a base of 7 players, it can also be all 10 players.",false)
            .addField("/team-stats","check the win rate of a team. This is an approximate, not a definitive value that can be displayed on the Wargaming website.",false)
            .addField("Notes","The commands that are for managing a team (add-existing-player,freeup-roster,remove-from-roster), these can only be used if the team exists in the bot's records.\nThis bot uses the values provided from the Wargaming public API. It does not use external APIs (such as BlitzStars) to perform the calculations nor does it directly query the Wargaming site for a team's win rate.",false);
            return eb.build();
        }
    }
}