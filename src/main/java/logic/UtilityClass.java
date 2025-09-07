package logic;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.BufferedWriter;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Properties;

import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author erick
 */
public class UtilityClass{
    public static final String APP_ID=new UtilityClass().getAppID();
    public static final int MAX_BATTLE_COUNT=new UtilityClass().getBattleCount("max_count");
    private static final Object logFileLock=new Object();

    /**
     * @param file
     * @return 
     */
    protected String exists(File file){
        String parent=file.getParent();

        File f=new File(parent);
        if(!f.exists()){
            f.mkdirs();
        }

        String filename=file.getName();

        String name=FilenameUtils.getBaseName(filename);
        String extension=FilenameUtils.getExtension(filename);

        int i=1;
        File f2=new File(parent,name+"."+extension);
        while(f2.exists()){
            f2=new File(parent,name+"-("+i+")."+extension);
            i++;
        }

        return f2.getPath();
    }

    /**
     * @param totalTeams
     * @return 
     */
    public static int calculateTotalPages(int totalTeams){
        return (int)Math.ceil((double)totalTeams/100);
    }

    /**
     * @param value
     * @return
     */
    public static double getFormattedDouble(double value){
        return Math.round(value*100.0)/100.0;
    }

    /**
     * @param timestamp
     * @return
     */
    public static String getFormattedDate(long timestamp){
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp),ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
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
        return getFormattedDouble(((double)wins/battles)*100);
    }
    
    /**
     * Gets account overall win rate.
     * @param wins
     * @param battles
     * @return account win rate.
     */
    public static double getOverallWinrate(double wins,int battles){
        return getFormattedDouble((wins/battles)*100);
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
    public String getRealm(String realm){
        String value=null;
        Properties p=new Properties();
        try(InputStream is=UtilityClass.class.getResourceAsStream("/realms.properties")){
            p.load(is);
            value="https://"+p.getProperty(realm.toUpperCase());
        }catch(IOException e){
            log(Level.SEVERE,e.getMessage(),e);
        }
        return value;
    }

    /**
     * @param count
     * @return
     */
    protected int getBattleCount(String count){
        int value=0;
        Properties p=new Properties();
        try(FileInputStream fis=new FileInputStream("data/generic.properties")){
            p.load(fis);
            value=Integer.parseInt(p.getProperty(count));
        }catch(IOException e){
            log(Level.SEVERE,e.getMessage(),e);
        }
        return value;
    }

    /**
     * @param description
     * @return
    */
    public static String parseSeedingType(String description){
        final String UNKNOWN_SEEDING="Unknown";
        final String KEYWORD="Seeding principle in each group:";

        if(description==null||description.isEmpty()){
            return UNKNOWN_SEEDING;
        }

        int keywordIndex=description.indexOf(KEYWORD);
        if(keywordIndex==-1){
            return UNKNOWN_SEEDING;
        }

        int valueStartIndex=keywordIndex+KEYWORD.length();
        int endOfLineIndex=description.indexOf('\n',valueStartIndex);

        if(endOfLineIndex==-1){
            endOfLineIndex=description.length();
        }

        String seedingValue=description.substring(valueStartIndex,endOfLineIndex).trim();

        String lowerCaseSeedingValue=seedingValue.toLowerCase();

        if(lowerCaseSeedingValue.startsWith("strongest-weakest")){
            return "Strongest-Weakest";
        }else if(lowerCaseSeedingValue.startsWith("groups of death")){
            return "Groups of Death";
        }else{
            return UNKNOWN_SEEDING;
        }
    }

    /**
     * @param level
     * @param message
     */
    public void log(Level level,String message){
        Logger logger=Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        logger.log(level,message);
    }

    /**
     * @param level
     * @param message
     * @param thrown
     */
    public void log(Level level,String message,Throwable thrown){
        LogRecord logger2=new LogRecord(level,message);
        logger2.setThrown(thrown);
        String formattedMessage=new SimpleFormatter().format(logger2);

        String finalFilePath;
        synchronized(logFileLock){
            String parentDir="data/exceptions/";
            String baseName=thrown.getClass().getSimpleName();
            String extension=".log";

            File logFile=new File(parentDir,baseName+extension);

            int i=1;
            while(logFile.exists()){
                logFile=new File(parentDir,baseName+"-("+i+")"+extension);
                i++;
            }

            File parent=logFile.getParentFile();
            if(parent!=null&&!parent.exists()){
                parent.mkdirs();
            }

            finalFilePath=logFile.getAbsolutePath();
        }

        try(FileWriter fw=new FileWriter(finalFilePath);
                BufferedWriter writer=new BufferedWriter(fw)){
            writer.write(formattedMessage);
        }catch(IOException e){
            log(Level.SEVERE,e.toString());
        }
    }
}