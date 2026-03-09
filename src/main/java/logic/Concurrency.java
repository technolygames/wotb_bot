package logic;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * @author erick
 */
public class Concurrency implements Runnable{
    private final List<Integer> targetHours=List.of(5,10,12,18);

    private final ScheduledExecutorService scheduler=Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    
    private final UtilityClass uc=new UtilityClass();
    private final JsonHandler jh=new JsonHandler();

    /**
     */
    @Override
    public void run(){
        scheduler.scheduleAtFixedRate(()->{
            try{
                jh.playerProfile();
                jh.clanProfile();
            }catch(Exception e){
                uc.log(Level.SEVERE,e.getMessage(),e);
            }
        },0,20,TimeUnit.MINUTES);

        for(int hour:targetHours){
            scheduleTask(hour);
        }

        scheduler.scheduleAtFixedRate(()->runDataCollection(),0,10,TimeUnit.MINUTES);
        
        scheduler.scheduleAtFixedRate(()->{
            try{
                new BotCommandActions().refreshCaches();
            }catch(Exception e){
                uc.log(Level.SEVERE,e.getMessage(),e);
            }
        },0,5,TimeUnit.MINUTES);
    }

    /**
     * @param zone
     * @param hour
     */
    private void scheduleTask(int hour){
        long delayInSeconds=calculateDelayInSeconds(hour);
        scheduler.schedule(()->{
            try{
                jh.getTournamentData();
                scheduleTask(hour);
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
    private long calculateDelayInSeconds(int targetHour){
        ZoneId sd=ZoneId.systemDefault();
        ZonedDateTime nowSystem=ZonedDateTime.now(sd);

        ZonedDateTime nextRunInTarget=nowSystem.withHour(targetHour).withMinute(0).withSecond(0).withNano(0);
        if(!nowSystem.isAfter(nextRunInTarget)){
            nextRunInTarget=nextRunInTarget.plusDays(1);
        }

        return Duration.between(nowSystem,nextRunInTarget).getSeconds();
    }
    
    /**
     */
    public void runDataCollection(){
        try{
            jh.updateTournamentData();
            jh.manipulateTeams();
            jh.validateTeams();
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
     }
}