package logic;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author erick
 */
public class Concurrency{
    /**
     */
    public void run(){
        ScheduledExecutorService ses=Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(scheduleUpdateData(),0,10,TimeUnit.MINUTES);
    }

    /**
     * @return
     */
    protected Runnable scheduleUpdateData(){
        JsonHandler jh=new JsonHandler();
        return ()->{
            jh.playerProfile();
            jh.clanProfile();
        };
    }
}