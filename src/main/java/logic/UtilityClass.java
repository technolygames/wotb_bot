package logic;

import threads.Thread1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.io.FileOutputStream;
import java.time.ZoneId;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

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
     * @param file
     * @return
     */
    public static String getPath(String file){
        String name=FilenameUtils.getBaseName(file);
        String ext=FilenameUtils.getExtension(file);

        File f=new File("data/json",name+"."+ext);
        for(int i=0;f.exists();i++){
            f=new File(f.getParent(),name+"-("+i+")"+"."+ext);
        }

        return f.getPath();
    }

    /**
     * 
     * @param is
     * @param filename
     * @return
     * @throws IOException
     */
    public static String saveData(InputStream is,String filename) throws IOException{
        String path=getPath(filename);
        FileOutputStream fos=new FileOutputStream(path);
        new Thread1(is,fos).run();
        return path;
    }
    
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
     * @param epochTime
     * @return
     */
    public static String getMatchDate(long epochTime){
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochTime),ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * 
     * @param value
     * @return
     */
    public static double getFormattedDouble(double value){
        return Double.parseDouble(new DecimalFormat("##.##").format(value));
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
     * @param wotbId
     * @return account win rate.
     */
    public static double getOverallWinrate(int wins,int battles){
        double wins2=wins+.0;
        double battles2=battles+.0;

        return getFormattedDouble((wins2/battles2)*100);
    }

    private static String getAppID(){
        return Dotenv.configure().directory(".env").load().get("APP_ID");
    }
}