package logic;

import java.io.IOException;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.Properties;

import java.util.logging.Logger;

import io.github.cdimascio.dotenv.Dotenv;

/**
 *
 * @author erick
 */
public class UtilityClass{
    private UtilityClass(){}

    public static final Logger LOGGER=Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public static final String APP_ID=UtilityClass.getAppID();
    
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
     * 
     * @param value
     * @return
     */
    public static double getFormattedDouble(double value){
        return Double.parseDouble(new DecimalFormat("##.##").format(value).replace(",","."));
    }

    /**
     * 
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

    private static String getAppID(){
        return Dotenv.configure().directory("data").load().get("APP_ID");
    }

    public static String getRealm(String realm){
        Properties p=new Properties();
        try(FileInputStream fis=new FileInputStream("data/realms.properties")){
            p.load(fis);
            return "https://"+p.getProperty(realm);
        }catch(IOException e){
            LOGGER.severe(e.fillInStackTrace().toString());
            return "NA";
        }
    }
}