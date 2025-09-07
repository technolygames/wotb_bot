package logic;

import dbconnection.GetData;
import interfaces.Interfaces;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import net.dv8tion.jda.api.interactions.commands.Command;

/**
 * @author erick
 */
public class Concurrency{
    private final List<ZoneId> zoneIds=List.of(
        ZoneId.of("Asia/Singapore"),
        ZoneId.of("Europe/Nicosia"),
        ZoneId.of("America/Chicago")
    );

    private final List<Integer> targetHours=List.of(10,12,18);

    private final ScheduledExecutorService scheduler=Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    
    private final UtilityClass uc=new UtilityClass();
    private final JsonHandler jh=new JsonHandler();
    
    public static List<Command.Choice> clanChoices=new ArrayList<>();
    public static List<Command.Choice> tournamentChoices=new ArrayList<>();
    public static List<Command.Choice> playerChoices=new ArrayList<>();
    public static List<Command.Choice> tbPlayerChoices=new ArrayList<>();
    public static List<Command.Choice> notNullPlayerChoices=new ArrayList<>();
    public static List<Command.Choice> clanlessPlayerChoices=new ArrayList<>();
    public static List<Command.Choice> ingameTeamChoices=new ArrayList<>();

    /**
     */
    public void run(){
        scheduler.scheduleAtFixedRate(()->{
            try{
                jh.playerProfile();
                jh.clanProfile();
            }catch(Exception e){
                uc.log(Level.SEVERE,e.getMessage(),e);
            }
        },0,15,TimeUnit.MINUTES);

        for(ZoneId zone:zoneIds){
            for(int hour:targetHours){
                scheduleTask(zone,hour);
            }
        }

        List<Interfaces.RealmSchedule> schedules=new GetData().getTournamentDateInfo();

        for(Interfaces.RealmSchedule schedule:schedules){
            scheduleRealmDataCollection(schedule);
        }
        
        scheduler.scheduleAtFixedRate(()->{
            try{
                refreshCaches();
            }catch(Exception e){
                uc.log(Level.SEVERE,"Failed to refresh autocomplete cache",e);
            }
        },1,5,TimeUnit.MINUTES);
    }

    /**
     * @param schedule
     */
    private void scheduleRealmDataCollection(Interfaces.RealmSchedule schedule){
        ZonedDateTime nowInRealm=ZonedDateTime.now(schedule.timeZone());

        ZonedDateTime startTime=ZonedDateTime.ofInstant(Instant.ofEpochSecond(schedule.start()),schedule.timeZone());
        ZonedDateTime endTime=ZonedDateTime.ofInstant(Instant.ofEpochSecond(schedule.end()),schedule.timeZone());

        ZonedDateTime periodicTaskShutdownTime=endTime.plusSeconds(1);
        
        Runnable dataCollectionTask=()->runDataCollection();

        if(nowInRealm.isBefore(periodicTaskShutdownTime)){
            long initialDelay=0;
            if(nowInRealm.isBefore(startTime)){
                initialDelay=Duration.between(nowInRealm,startTime).toMillis();
            }

            final long TEN_MINUTES_IN_MILLIS=10L*60*1000;

            ScheduledFuture<?> taskFuture=scheduler.scheduleAtFixedRate(dataCollectionTask,initialDelay,TEN_MINUTES_IN_MILLIS,TimeUnit.MILLISECONDS);

            long shutdownDelay=Duration.between(nowInRealm,periodicTaskShutdownTime).toMillis();
            scheduler.schedule(()->taskFuture.cancel(false),shutdownDelay,TimeUnit.MILLISECONDS);
        }

        ZonedDateTime finalCheckTime=endTime.plusMinutes(1);

        if(nowInRealm.isBefore(finalCheckTime)){
            long finalCheckDelay=Duration.between(nowInRealm,finalCheckTime).toMillis();

            scheduler.schedule(dataCollectionTask,finalCheckDelay,TimeUnit.MILLISECONDS);
        }
    }

    /**
     */
    private void runDataCollection(){
        try{
            jh.updateTournamentData();
            jh.manipulateTeams();
            jh.synchronizeTeamRosters();
            jh.validateTeams();
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }
    
    /**
     */
    private void refreshCaches(){
        BotCommandActions bca=new BotCommandActions();
        clanChoices=bca.getClanAsChoices();
        tournamentChoices=bca.getTournamentsAsChoices();
        playerChoices=bca.getPlayersAsChoices();
        tbPlayerChoices=bca.getThousandBattlesPlayerAsChoice();
        notNullPlayerChoices=bca.getNotNullPlayersAsChoices();
        clanlessPlayerChoices=bca.getClanlessPlayersAsChoices();
        ingameTeamChoices=bca.getIngameTeamAsChoice();
    }
    
    /**
     * @param zone
     * @param hour
     */
    private void scheduleTask(ZoneId zone,int hour){
        long delayInSeconds=calculateDelayInSeconds(zone,hour);
        scheduler.schedule(()->{
            try{
                jh.getTournamentData();
                scheduleTask(zone,hour);
            }catch(Exception e){
                uc.log(Level.SEVERE,e.getMessage(),e);
            }
        },delayInSeconds,TimeUnit.SECONDS);
    }

    /**
     * @param targetZone
     * @param targetHour
     * @return
     */
    private long calculateDelayInSeconds(ZoneId targetZone,int targetHour){
        ZonedDateTime nowSystem=ZonedDateTime.now();
        ZonedDateTime nowTarget=nowSystem.withZoneSameInstant(targetZone);

        ZonedDateTime nextRunInTarget=nowTarget.withHour(targetHour).withMinute(0).withSecond(0).withNano(0);
        if(!nextRunInTarget.isAfter(nowTarget)){
            nextRunInTarget=nextRunInTarget.plusDays(1);
        }

        ZonedDateTime nextRunInSystem=nextRunInTarget.withZoneSameInstant(ZoneId.systemDefault());
        return Duration.between(nowSystem,nextRunInSystem).getSeconds();
    }
}