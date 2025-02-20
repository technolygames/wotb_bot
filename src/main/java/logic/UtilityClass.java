package logic;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.Properties;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author erick
 */
public class UtilityClass{
    Logger logger;
    public UtilityClass(){
        logger=Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    public static final String APP_ID=new UtilityClass().getAppID();
    public static final int MAX_BATTLE_COUNT=new UtilityClass().getBattleCount("max_count");

    /**
     * @param file
     * @return 
     */
    protected String exists(File file){
        String parent=file.getParent();

        File f=new File(parent);
        if(!f.exists()){
            f.mkdir();
        }

        String filename=file.getName();

        String name=FilenameUtils.getBaseName(filename);
        String extension=FilenameUtils.getExtension(filename);

        file=new File(parent,name+"."+extension);
        for(int i=1;file.exists();i++){
            file=new File(parent,name+"-("+i+")."+extension);
        }

        return file.getPath();
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
    public String getRealm(String realm){
        Properties p=new Properties();
        try(InputStream is=ClassLoader.getSystemClassLoader().getResourceAsStream("realms.properties")){
            p.load(is);
            return "https://"+p.getProperty(realm);
        }catch(IOException e){
            log(Level.SEVERE,e.getMessage(),e);
            return null;
        }
    }

    /**
     * @param count
     * @return
     */
    protected int getBattleCount(String count){
        Properties p=new Properties();
        try(FileInputStream fis=new FileInputStream("data/generic.properties")){
            p.load(fis);
            return Integer.parseInt(p.getProperty(count));
        }catch(IOException e){
            log(Level.SEVERE,e.getMessage(),e);
            return 0;
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
        try{
            FileHandler fh=new FileHandler(exists(new File("data/exceptions/"+thrown.getClass().getSimpleName()+".log")));
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.log(level,message,thrown);
            
            fh.flush();
            fh.close();
        }catch(IOException e){
            log(Level.SEVERE,e.getMessage());
        }
    }
}