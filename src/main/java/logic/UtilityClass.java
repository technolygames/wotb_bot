package logic;

import com.google.gson.JsonElement;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Properties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static final int MIN_BATTLE_COUNT=new UtilityClass().getBattleCount("min_count");
    public static final int MAX_BATTLE_COUNT=new UtilityClass().getBattleCount("max_count");

    /**
     * Structure:<br>
     * <ul>
     * <li>EU</li>
     * <li>NA</li>
     * <li>ASIA</li>
     * </ul>
     * @return
     */
    public static Map<String,List<List<String>>> mapList(){
        Map<String,List<List<String>>> lists=new HashMap<>();
        lists.put("EU",new ArrayList<>());
        lists.put("NA",new ArrayList<>());
        lists.put("ASIA",new ArrayList<>());
        return lists;
    }
    
    public static String getSqlPlaceholders(List<Long> accIds){
        StringBuilder placeholders=new StringBuilder();
        for(int i=0;i<accIds.size();i++){
            placeholders.append("?");
            if(i<accIds.size()-1){
                placeholders.append(",");
            }
        }
        return placeholders.toString();
    }
    
    public static boolean isDigit(String str){
        if(str==null||str.isEmpty())return false;
        for(int i=0;i<str.length();i++){
            if(!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }
    
    /**
     * ternary sintaxis for battles<br>
     * <ul>
     * <li>value=0 - "0 battles"</li>
     * <li>value=1 - "1 battle"</li>
     * <li>value>=2 - "2 battles"</li>
     * </ul>
     * @param value
     * @return
     */
    public static String tersin(int value){
        return value+((value==1)?" battle":" battles");
    }
    
    /**
     * ternary sintaxis for json values that returns integer value or zero
     * @param value
     * @return
     */
    public static int tersin(JsonElement value){
        return (!value.isJsonNull())?value.getAsInt():0;
    }
    
    /**
     * @param confirmed
     * @param perGroup 
     * @return
     */
    public static int getMaxGroups(int confirmed,int perGroup){
        return (int)Math.ceil(UtilityClass.getDivision(confirmed,perGroup));
    }
    
    /**
     * @param totalTeams
     * @return 
     */
    public static int calculateTotalPages(int totalTeams){
        return (int)Math.ceil(getDivision(totalTeams,100));
    }

    /**
     * @param value
     * @return
     */
    public static double getFormattedDouble(double value){
        return getDivision(Math.round(value*100.0),(int)100.0);
    }

    /**
     * Format: MM/dd/yyyy
     * @param timestamp
     * @return
     */
    public static String getFormattedDate(long timestamp){
        return Instant.ofEpochSecond(timestamp).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }

    /**
     * Gets account overall win rate.
     * @param wins
     * @param battles
     * @return account win rate.
     */
    public static double getOverallWinrate(int wins,int battles){
        return getFormattedDouble(getDivision(wins,battles)*100);
    }
    
    /**
     * a/b
     * @param a dividend
     * @param b divisor
     * @return
     */
    public static double getDivision(double a,int b){
        return (a/b);
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
        try(InputStream is=getClass().getResourceAsStream("/realms.properties")){
            p.load(is);
            value="https://"+p.getProperty(realm.toUpperCase());
        }catch(IOException e){
            log(Level.SEVERE,e.getMessage(),e);
        }
        return value;
    }

    /**
     * @param input
     * @return
     */
    public static boolean isValidLocalizedDecimal(String input){
        if(input==null||input.trim().isEmpty()){
            return false;
        }

        NumberFormat formatter=NumberFormat.getInstance();
        formatter.setGroupingUsed(false);
        try{
            formatter.parse(input.trim());
            return true;
        }catch(ParseException e){
            return false;
        }
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
        String unknownSeeding="Unknown";
        String seeding="";
        
        String[] keywords={
            "Seeding principle in each group:",
            "Seeding within groups follows the",
        };
        
        if(description==null||description.isEmpty()){
            return unknownSeeding;
        }

        String lowerDesc=description.toLowerCase();
        
        for(String key:keywords){
            int keywordIndex=lowerDesc.indexOf(key.toLowerCase());
            if(keywordIndex!=-1){
                int valueStartIndex=keywordIndex+key.length();
                int endOfLineIndex=description.indexOf('\n',valueStartIndex);
                if(endOfLineIndex==-1){
                    endOfLineIndex=description.length();
                }
                String seedingLine=description.substring(valueStartIndex,endOfLineIndex).trim().toLowerCase();
                seeding=switch(seedingLine){
                    case String s when s.contains("strongest")->"Strongest-Weakest";
                    case String s when s.contains("death")->"Groups of Death";
                    default->unknownSeeding;
                };
            }
        }
        return seeding;
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
    public synchronized void log(Level level,String message,Throwable thrown){
        LogRecord logger2=new LogRecord(level,message);
        logger2.setThrown(thrown);
        
        String formattedMessage=new SimpleFormatter().format(logger2);
        
        String parentDir="data/exceptions/";
        String baseName=thrown.getClass().getSimpleName();
        String extension=".log";

        String finalFilePath=FileHandler.exists(new File(parentDir,baseName+extension));

        try(FileWriter fw=new FileWriter(finalFilePath);
                BufferedWriter writer=new BufferedWriter(fw)){
            writer.write(formattedMessage);
        }catch(IOException e){
            log(Level.SEVERE,e.toString());
        }
    }
}