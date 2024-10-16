package logic;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.Properties;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author erick
 */
public class UtilityClass{
    Logger logger;
    public UtilityClass(){
        logger=Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    public static final String APP_ID=new UtilityClass().getAppID();
    public static final int MAX_BATTLE_COUNT=2000;
    
    /**
     * 
     * @param keySet
     * @return
     */
    public static String getJsonKeyName(Set<String> keySet){
        String val1="";
        for (String val2:keySet){
            val1=val2;
        }
        return val1;
    }

    /**
     * @param value
     * @return
     */
    public static double getFormattedDouble(double value){
        return Double.parseDouble(new DecimalFormat("##.##").format(value).replace(",","."));
    }

    /**
     * @param fromApi
     * @param fromDatabase
     * @return
     */
    public static int calculateDifference(int fromApi,int fromDatabase){
        return fromApi-fromDatabase;
    }

    /**
     * Gets account overall win rate.
     * @param wins
     * @param battles
     * @return account win rate.
     */
    public static double getOverallWinrate(int wins,int battles){
        double wins2=wins+.0;
        double battles2=battles+.0;

        return getFormattedDouble((wins2/battles2)*100);
    }

    /**
     * @return
     */
    protected String getAppID(){
        return Dotenv.configure().directory("data").load().get("APP_ID");
    }

    /**
     * @param realm
     * @return
     */
    public static String getRealm(String realm){
        Properties p=new Properties();
        try(InputStream fis=ClassLoader.getSystemClassLoader().getResourceAsStream("realms.properties")){
            p.load(fis);
            return "https://"+p.getProperty(realm);
        }catch(IOException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return "NA";
        }
    }

    /**
     * @param level
     * @param message
     */
    public void log(Level level,String message){
        logger.log(level,message);
    }
    
    /**
     * @param level
     * @param message
     * @param thrown
     */
    public void log(Level level,String message,Throwable thrown){
        logger.log(level,message,thrown);
    }
}