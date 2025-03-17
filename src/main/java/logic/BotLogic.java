package logic;

import dbconnection.DeleteData;
import dbconnection.GetData;
import dbconnection.InsertData;
import mvc.Mvc1;
import mvc.Mvc2;

import org.jetbrains.annotations.NotNull;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

import java.awt.Color;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

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
        }catch(DotenvException|IllegalArgumentException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }

    public class EventListeners extends net.dv8tion.jda.api.hooks.ListenerAdapter{
        private static final String MESSAGE="You are not the leader of this team";
        private static final String MESSAGE_2="Cannot be NULL";
        private static final String MESSAGE_3="No data";
        public static final String MESSAGE_4="Something gone wrong";
        private static final String MESSAGE_5="Results";
        private static final String MESSAGE_6="Write valid data";
        private static final String MESSAGE_7="Internal failure";
        private static final String MESSAGE_8=" has been created!";
        
        @Override
        public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent evt){
            String callerId=evt.getUser().getId();
            EmbedBuilder eb=new EmbedBuilder();
            JsonHandler jh=new JsonHandler();
            UtilityClass uc=new UtilityClass();
            switch(evt.getName()){
                case "add-existing-player":{
                    evt.deferReply(false).queue(m->{
                        try{
                            int clantag=evt.getOption("clantag").getAsInt();
                            int player=evt.getOption("player").getAsInt();
                            String realm=evt.getOption("server").getAsString().toUpperCase();
                            if(new GetData().checkCallerDiscordId(callerId,clantag,realm)){
                                new InsertData().teamRegistration(clantag,player,callerId,realm);
                                eb.setColor(Color.GREEN).addField(MESSAGE_5,new GetData().checkPlayerByID(player)+" has been added to "+new GetData().checkClantagByID(clantag,realm)+" roster",false);
                            }else{
                                eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE,false);
                            }
                        }catch(Exception e){
                            eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6+" or register first your account using register-player command",false);
                            uc.log(Level.SEVERE,e.getMessage(),e);
                        }
                        m.sendMessageEmbeds(eb.build()).queue();
                    });
                    break;
                }
                case "check-player":{
                    evt.deferReply(false).queue(m->{
                        try{
                            int player=evt.getOption("player2").getAsInt();
                            eb.setColor(Color.GREEN).addField(MESSAGE_5,new BotActions().checkPlayer(player),false);
                        }catch(Exception e){
                            eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6+" or register first your account using register-player command",false);
                            uc.log(Level.SEVERE,e.getMessage(),e);
                        }
                        m.sendMessageEmbeds(eb.build()).queue();
                    });
                    break;
                }
                case "create-new-team":{
                    evt.deferReply(false).queue(m->{
                        MessageEmbed me=null;
                        try{
                            String clantag=evt.getOption("clantag3").getAsString().toUpperCase();
                            var player=evt.getOption("player3");
                            var code=evt.getOption("code3");
                            String realm=evt.getOption("server3").getAsString().toUpperCase();

                            if(!clantag.equals("NULL")){
                                if(player!=null){
                                    me=createTeam(jh.getAccountData(player.getAsString(),realm),jh.getClanData(clantag,realm),callerId);
                                }else if(code!=null){
                                    me=createTeam(jh.getAccountData(code.getAsInt(),realm),jh.getClanData(clantag,realm),callerId);
                                }
                            }else{
                                me=eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_2,false).build();
                            }
                        }catch(Exception e){
                            me=eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6,false).build();
                            uc.log(Level.SEVERE,e.getMessage(),e);
                        }
                        m.sendMessageEmbeds(me).queue();
                    });
                    
                    break;
                }
                case "formula-stats":{
                    evt.deferReply(false).queue(m->{
                        try{
                            int player=evt.getOption("player11").getAsInt();
                            m.sendMessageEmbeds(playerWeight(player)).addActionRow(Button.primary("refresh-formula-stats:"+player,"Refresh")).queue();
                        }catch(Exception e){
                            m.sendMessageEmbeds(eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6+" or register first your account for stat tracking using register-player command",false).build()).queue();
                            uc.log(Level.SEVERE,e.getMessage(),e);
                        }
                    });
                    break;
                }
                case "freeup-roster":{
                    evt.deferReply(false).queue(m->{
                        try{
                            int clantag=evt.getOption("clantag4").getAsInt();
                            String realm=evt.getOption("server4").getAsString();
                            if(new GetData().checkCallerDiscordId(callerId,clantag,realm)){
                                new DeleteData().freeupRoster(clantag,realm);
                                eb.setColor(Color.GREEN).addField(MESSAGE_5,new GetData().checkClantagByID(clantag,realm)+" roster has been removed from team list",false);
                            }else{
                                eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE,false);
                            }
                        }catch(Exception e){
                            eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6,false);
                            uc.log(Level.SEVERE,e.getMessage(),e);
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
                        try{
                            int player=evt.getOption("player5").getAsInt();
                            m.sendMessageEmbeds(new BotCommandActions().getTier10Stats(player)).addActionRow(Button.primary("refresh-personal-stats:"+player,"Refresh")).queue();
                        }catch(Exception e){
                            m.sendMessageEmbeds(eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6,false).build()).queue();
                            uc.log(Level.SEVERE,e.getMessage(),e);
                        }
                    });
                    break;
                }
                case "register-player":{
                    evt.deferReply(false).queue(m->{
                        MessageEmbed val=null;
                        try{
                            var player=evt.getOption("nickname6");
                            var code=evt.getOption("code6");
                            String realm=evt.getOption("server6").getAsString().toUpperCase();
                            if(player!=null){
                                val=registerPlayer(jh.getAccountData(player.getAsString(),realm),realm);
                            }else if(code!=null){
                                val=registerPlayer(jh.getAccountData(code.getAsInt(),realm),realm);
                            }
                        }catch(Exception e){
                            val=eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6,false).build();
                            uc.log(Level.SEVERE,e.getMessage(),e);
                        }
                        m.sendMessageEmbeds(val).queue();
                    });
                    break;
                }
                case "remove-from-roster":{
                    evt.deferReply(false).queue(m->{
                        try{
                            int player=evt.getOption("nickname7").getAsInt();
                            int clantag=evt.getOption("clantag7").getAsInt();
                            String realm=evt.getOption("server7").getAsString();
                            if(new GetData().checkCallerDiscordId(callerId,clantag,realm)){
                                eb.setColor(Color.GREEN).addField(MESSAGE_5,new GetData().checkPlayerByID(player)+" has been removed from the team roster",false);
                                new DeleteData().removeFromRoster(player,clantag);
                            }else{
                                eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE,false);
                            }
                        }catch(Exception e){
                            eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6,false);
                            uc.log(Level.SEVERE,e.getMessage(),e);
                        }
                        m.sendMessageEmbeds(eb.build()).queue();
                    });
                    break;
                }
                case "roster":{
                    evt.deferReply(false).queue(m->{
                        try{
                            int clanId=evt.getOption("clantag8").getAsInt();
                            String realm=evt.getOption("server8").getAsString().toUpperCase();
                            MessageEmbed val=roster(clanId,realm);
                            if(!val.getFields().get(2).getValue().equals(MESSAGE_3)){
                                m.sendMessageEmbeds(val).addActionRow(Button.primary("refresh-roster:"+clanId+":"+realm,"Refresh")).queue();
                            }else{
                                m.sendMessageEmbeds(val).queue();
                            }
                        }catch(Exception e){
                            m.sendMessageEmbeds(eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6+" or register first your team using team-registration or create-new-team commands",false).build()).queue();
                            uc.log(Level.SEVERE,e.getMessage(),e);
                        }
                    });
                    break;
                }
                case "team-leaderboard":{
                    evt.deferReply(false).queue(m->{
                        try{
                            String realm=evt.getOption("server12").getAsString().toUpperCase();
                            eb.setColor(Color.GREEN).addField(MESSAGE_5,new BotActions().teamLeaderboard(realm),false);
                        }catch(Exception e){
                            eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6,false);
                            uc.log(Level.SEVERE,e.getMessage(),e);
                        }
                        m.sendMessageEmbeds(eb.build()).queue();
                    });
                    break;
                }
                case "team-registration":{
                    evt.deferReply(false).queue(m->{
                        MessageEmbed me=null;
                        try{
                            OptionMapping[] values={
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
                            String clantag=evt.getOption("clantag9").getAsString().toUpperCase();
                            String realm=evt.getOption("server9").getAsString().toUpperCase();
                            if(!clantag.equals("NULL")){
                                me=teamRegistration(values,jh.getClanData(clantag,realm),callerId,realm);
                            }else{
                                me=eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_2,false).build();
                            }
                        }catch(Exception e){
                            me=eb.setColor(Color.RED).addField(MESSAGE_4,MESSAGE_7,false).build();
                            uc.log(Level.SEVERE,e.getMessage(),e);
                        }
                        m.sendMessageEmbeds(me).queue();
                    });
                    break;
                }
                case "team-stats":{
                    evt.deferReply(false).queue(m->{
                        try{
                            int clanId=evt.getOption("clantag10").getAsInt();
                            String realm=evt.getOption("server10").getAsString().toUpperCase();
                            MessageEmbed val=teamStats(clanId,realm);
                            if(!val.getTitle().equals(MESSAGE_4)){
                                m.sendMessageEmbeds(val).addActionRow(Button.primary("refresh-team-stats:"+clanId+":"+realm,"Refresh")).queue();
                            }else{
                                m.sendMessageEmbeds(eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_3,false).build()).queue();
                            }
                        }catch(Exception e){
                            m.sendMessageEmbeds(eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6+" or register first your team using team-registration or create-new-team commands",false).build()).queue();
                            uc.log(Level.SEVERE,e.getMessage(),e);
                        }
                    });
                    break;
                }
                default:break;
            }
        }

        @Override
        public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent evt){
            BotCommandActions bca=new BotCommandActions();
            switch(evt.getName()){
                case "add-existing-player":{
                    switch(evt.getFocusedOption().getName()){
                        case "player":{
                            setChoices(evt,bca.getClanlessPlayersAsChoices());
                            break;
                        }
                        case "clantag":{
                            setChoices(evt,bca.getClanAsChoices());
                            break;
                        }
                        default:break;
                    }
                    break;
                }
                case "check-player":{
                    if(evt.getFocusedOption().getName().equals("player2")){
                        setChoices(evt,bca.getPlayersAsChoices());
                    }
                    break;
                }
                case "formula-stats":{
                    if(evt.getFocusedOption().getName().equals("player11")){
                        setChoices(evt,bca.getThousandBattlesPlayerAsChoice());
                    }
                    break;
                }
                case "freeup-roster":{
                    if(evt.getFocusedOption().getName().equals("clantag4")){
                        setChoices(evt,bca.getClanAsChoices());
                    }
                    break;
                }
                case "personal-stats-tier10":{
                    if(evt.getFocusedOption().getName().equals("player5")){
                        setChoices(evt,bca.getPlayersAsChoices());
                    }
                    break;
                }
                case "remove-from-roster":{
                    switch(evt.getFocusedOption().getName()){
                        case "nickname7":{
                            setChoices(evt,bca.getNotNullPlayersAsChoices());
                            break;
                        }
                        case "clantag7":{
                            setChoices(evt,bca.getClanAsChoices());
                            break;
                        }
                        default:break;
                    }
                    break;
                }
                case "roster":{
                    if(evt.getFocusedOption().getName().equals("clantag8")){
                        setChoices(evt,bca.getClanAsChoices());
                    }
                    break;
                }
                case "team-stats":{
                    if(evt.getFocusedOption().getName().equals("clantag10")){
                        setChoices(evt,bca.getClanAsChoices());
                    }
                    break;
                }
                default:break;
            }
        }

        @Override
        public void onButtonInteraction(@NotNull ButtonInteractionEvent evt){
            try{
                String[] val=evt.getButton().getId().split(":");
                switch(val[0]){
                    case "refresh-formula-stats":{
                        evt.deferEdit().queue(s->s.editOriginalEmbeds(playerWeight(Integer.parseInt(val[1]))).queue());
                        break;
                    }
                    case "refresh-personal-stats":{
                        evt.deferEdit().queue(s->s.editOriginalEmbeds(new BotCommandActions().getTier10Stats(Integer.parseInt(val[1]))).queue());
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
            }catch(Exception e){
                evt.deferReply(true).queue(m->m.sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).addField(MESSAGE_4,MESSAGE_7,true).build()).queue());
                new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            }
        }

        private void setChoices(CommandAutoCompleteInteraction evt,List<Command.Choice> choices){
            List<Command.Choice> choices2=choices.stream()
                    .filter(choice->choice.getName().startsWith(evt.getFocusedOption().getValue()))
                    .limit(5)
                    .collect(Collectors.toList());
            Collections.shuffle(choices2);
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
                            new OptionData(OptionType.STRING,"player","Player to be added",true).setAutoComplete(true),
                            new OptionData(OptionType.STRING,"clantag","Team clantag",true).setAutoComplete(true),
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
                            new OptionData(OptionType.INTEGER,"code3","Player ID",false)
                    ));

            commands.add(Commands.slash("formula-stats","Is an approximate value, it's not definitive. It may vary.")
                    .addOptions(
                            new OptionData(OptionType.STRING,"player11","Player",true).setAutoComplete(true)
                    ));

            commands.add(Commands.slash("freeup-roster","Free team roster but keeps player entries for stat tracking.")
                    .addOptions(
                            new OptionData(OptionType.STRING,"clantag4","Team clantag",true).setAutoComplete(true),
                            new OptionData(OptionType.STRING,"server4","Team region",true).addChoices(serverChoices)
                    ));

            commands.add(Commands.slash("help","yes"));
            
            commands.add(Commands.slash("personal-stats-tier10","Gets tier 10 player stats.")
                    .addOptions(
                            new OptionData(OptionType.STRING,"player5","Player nickname",true).setAutoComplete(true)
                    ));

            commands.add(Commands.slash("register-player","Register a clanless player.")
                    .addOptions(
                            new OptionData(OptionType.STRING,"server6","Team region",true).addChoices(serverChoices),
                            new OptionData(OptionType.STRING,"nickname6","Player nickname",false),
                            new OptionData(OptionType.INTEGER,"code6","Player ID",false)
                    ));

            commands.add(Commands.slash("remove-from-roster","Removes a player from a team roster.")
                    .addOptions(
                            new OptionData(OptionType.STRING,"nickname7","Player nickname",true).setAutoComplete(true),
                            new OptionData(OptionType.STRING,"clantag7","Team clantag",true).setAutoComplete(true),
                            new OptionData(OptionType.STRING,"server7","Team region",true).addChoices(serverChoices)
                    ));
            
            commands.add(Commands.slash("roster","Gets team structure.")
                    .addOptions(
                            new OptionData(OptionType.STRING,"clantag8","Team clantag",true).setAutoComplete(true),
                            new OptionData(OptionType.STRING,"server8","Team region",true).addChoices(serverChoices)
                    ));

            commands.add(Commands.slash("team-leaderboard","Gets teams registered into bot's records.")
                    .addOptions(
                            new OptionData(OptionType.STRING,"server12","Team region",true).addChoices(serverChoices)
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

        private MessageEmbed teamRegistration(OptionMapping[] values,Mvc2 data,String callerId,String realm){
            MessageEmbed val=null;
            for(OptionMapping value:values){
                if(value!=null){
                    Mvc1 data2=new JsonHandler().getAccountData(value.getAsString(),realm);
                    val=createTeam(data2,data,callerId);
                }
            }
            return val;
        }
        
        private MessageEmbed createTeam(Mvc1 data,Mvc2 data2,String callerId){
            EmbedBuilder eb=new EmbedBuilder();
            try{
                int accId=data.getAcoountId();
                String player=data.getNickname();
                long lastBattleTime=data.getLastBattleTime();
                long updatedAt=data.getUpdatedAt();

                int clanId=data2.getClanId();
                String clantag=data2.getClantag();
                String realm=data2.getRealm();
                long updatedAt2=data2.getUpdatedAt();

                if(accId!=0||clanId!=0){
                    GetData val=new GetData();
                    boolean userExists=val.checkUserData(accId,realm);
                    boolean clanExists=val.checkClanData(clantag,realm);
                    boolean teamExists=val.checkTeam(clanId,realm);
                    
                    InsertData val2=new InsertData();
                    if(!userExists&&!clanExists){
                        val2.setClanInfo(clanId,clantag,realm,updatedAt2);
                        val2.registerPlayer(accId,player,realm,lastBattleTime,updatedAt);
                        val2.teamRegistration(clanId,accId,callerId,realm);
                        eb.setColor(Color.GREEN).addField(MESSAGE_5,clantag+MESSAGE_8,false);
                    }else if(!clanExists){
                        val2.setClanInfo(clanId,clantag,realm,updatedAt2);
                        val2.teamRegistration(clanId,accId,callerId,realm);
                        eb.setColor(Color.GREEN).addField(MESSAGE_5,clantag+MESSAGE_8,false);
                    }else if(!userExists&&!teamExists){
                        val2.registerPlayer(accId,player,realm,lastBattleTime,updatedAt);
                        val2.teamRegistration(clanId,accId,callerId,realm);
                        eb.setColor(Color.GREEN).addField(MESSAGE_5,clantag+MESSAGE_8,false);
                    }else if(!teamExists){
                        val2.teamRegistration(clanId,accId,callerId,realm);
                        eb.setColor(Color.GREEN).addField(MESSAGE_5,clantag+MESSAGE_8,false);
                    }else{
                        eb.setColor(Color.YELLOW).addField(MESSAGE_4,clantag+" already exists",false);
                    }
                }else{
                    eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6,false);
                }
            }catch(Exception e){
                eb.setColor(Color.RED).addField(MESSAGE_4,MESSAGE_7,false);
                new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            }
            return eb.build();
        }

        private MessageEmbed playerWeight(int accId){
            EmbedBuilder eb=new EmbedBuilder();
            try{
                if(accId!=0){
                    eb.setColor(Color.GREEN).addField("Your win rate using Wargaming's tournament formula is:",new BotActions().calculatePlayerWeight(accId)+"%",false);
                }else{
                    eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6,false);
                }
            }catch(Exception e){
                eb.setColor(Color.RED).addField(MESSAGE_4,MESSAGE_7,false);
                new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            }
            return eb.build();
        }

        private MessageEmbed teamStats(int clanId,String realm){
            EmbedBuilder eb=new EmbedBuilder();
            try{
                if(clanId!=0){
                    double val=new BotActions().getTeamWinrate(clanId,realm);
                    if(val!=0.0){
                        eb.setColor(Color.GREEN).
                                setTitle("Your team win rate is:").
                                addField("Original: ",val+"%",false).
                                addField("Wargaming's page:",Math.round(val)+"%",false);
                    }else{
                        eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_3,false);
                    }
                }else{
                    eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_6,false);
                }
            }catch(Exception e){
                eb.setColor(Color.RED).addField(MESSAGE_4,MESSAGE_7,false);
                new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            }
            return eb.build();
        }

        private MessageEmbed roster(int clanId,String realm){
            EmbedBuilder eb=new EmbedBuilder();
            try{
                BotActions val=new BotActions();
                double team=val.getTeamWinrate(clanId,realm);
                String team2=val.getTeamRoster(clanId,realm);
                if(clanId!=0){
                    if(!team2.isEmpty()||!team2.isBlank()){
                        eb.setColor(Color.GREEN).
                                addField("Original:",team+"%",false).
                                addField("Wargaming's page:",Math.round(team)+"%",false).
                                addField("Roster:",team2,false);
                    }else{
                        eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_3,false);
                    }
                }else{
                    eb.setColor(Color.YELLOW).addField(MESSAGE_4,MESSAGE_3,false);
                }
            }catch(Exception e){
                eb.setColor(Color.RED).addField(MESSAGE_4,MESSAGE_7,false);
                new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            }
            return eb.build();
        }

        private MessageEmbed registerPlayer(Mvc1 data,String realm){
            EmbedBuilder eb=new EmbedBuilder();
            try{
                int accId=data.getAcoountId();
                String nickname=data.getNickname();
                long lastBattleTime=data.getLastBattleTime();
                long updatedAt=data.getUpdatedAt();
                
                if(accId!=0){
                    if(!new GetData().checkUserData(accId,realm)){
                        new InsertData().registerPlayer(accId,nickname,realm,lastBattleTime,updatedAt);
                        eb.setColor(Color.GREEN).addField(MESSAGE_5,nickname+" has been registered successfully!",false);
                    }else{
                        eb.setColor(Color.YELLOW).addField(MESSAGE_4,nickname+" is already registered",false);
                    }
                }else{
                    eb.setColor(Color.RED).addField(MESSAGE_4,MESSAGE_6,false);
                }
            }catch(Exception e){
                eb.setColor(Color.RED).addField(MESSAGE_4,MESSAGE_7,false);
                new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            }
            return eb.build();
        }

        private MessageEmbed help(){
            EmbedBuilder eb=new EmbedBuilder();
            eb.setColor(Color.ORANGE)
                    .addField("/add-existing-player","Adds a player without a clan to a team roster.\nValues:\nclantag - if you registered a team using team-registration or create-new-team, it will appear in the clan list;\nplayer - if you registered a player using the register-player command, it will appear in the list;\nserver - list of available servers.",false)
                    .addField("/check-player","Check the data of a specific player in the bot's records.\nValues:\nplayer2 - if you registered a player using the register-player or team-registration commands, it will appear in the list.",false)
                    .addField("/create-new-team","Creates a new team without the need to enter the remaining 9 players.\nValues, it has 2 optional fields:\nclantag3;\nserver3 - list of available servers;\nOptional fields (only one of the two optional fields can be used):\nplayer3 - nickname of the player to be register;\ncode3 - player ID that wargaming gives when you click on the nickname field in the game.",false)
                    .addField("/formula-stats","Check the statistics of a player with less than the number of battles requested by the game to calculate the weight of a player on a team.\nValues:\nplayer11 - if you registered a player using the register-player or team-registration commands, it will appear in the list.",false)
                    .addField("/freeup-roster","Clears the record of a team. Clan data is kept in the bot logs.\nValues:\nclantag4 - if you registered a team using team-registration or create-new-team, it will appear in the clan list;\nserver4 - list of available servers.",false)
                    .addField("/personal-stats-tier10","Check a player's tier 10 statistics.\nValues:\nplayer5 - if you registered a player using the register-player or team-registration commands, it will appear in the list.",false)
                    .addField("/register-player","Registers a player in the bot database.\nValues, it has 2 optional fields:\nserver - list of available servers;\nOptional fields (only one of the two optional fields can be used):\nnickname6 - nickname of the player to be register;\ncode6 - player ID that wargaming gives when you click on the nickname field in the game.",false)
                    .addField("/remove-from-roster","Removes a player from a team roster.\nValues:\nnickname7 - if you registered a player using the register-player or team-registration commands, it will appear in the list;\nclantag7 - if you registered a team using team-registration or create-new-team, it will appear in the clan list;\nserver7 - list of available servers.",false)
                    .addField("/roster","Check a team's statistics. The numbers shown may vary and may be approximate, not definitive.\nValues:\nclantag8 - if you registered a team using team-registration or create-new-team, it will appear in the clan list;\nserver8 - list of available servers.",false)
                    .addField("/team-registration","Registers a team with a base of 7 players, it can also be all 10 players.\nValues:\nclantag9;\nserver9 - list of available servers;\nplayer1...player10 - each field only accepts nicknames.",false)
                    .addField("/team-stats","Check the win rate of a team. This is an approximate, not a definitive value that can be displayed on the Wargaming website.\nValues:\nclantag10 - if you registered a team using team-registration or create-new-team, it will appear in the clan list;\nserver10 - list of available servers.",false)
                    .addField("Notes","The commands that are for managing a team (add-existing-player,freeup-roster,remove-from-roster), these can only be used if the team exists in the bot's records.\nThis bot uses the values provided from the Wargaming public API. It does not use external APIs (such as BlitzStars) to perform the calculations nor does it directly query the Wargaming website for a team's win rate.\nThe bot is not responsible for sending a tournament team registration request to the game. Bot team registration and game registration are different things.\nIn case a player has changed its nickname, this change will be reflected in 10 minutes in the bot logs.\nIn some cases, in particular if it's your first time using the bot, it may display a message that the team is registered. This is a false positive.",false);
            return eb.build();
        }
    }
}