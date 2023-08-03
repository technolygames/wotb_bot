package logic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

import threads.Thread1;

/**
 *
 * @author erick
 */
public class UtilityClass{
    private UtilityClass(){}

    public static final Logger LOGGER=Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    
    public static String getPath(String file){
        String name=FilenameUtils.getBaseName(file);
        String ext=FilenameUtils.getExtension(file);

        File f=new File("data/json",name+"."+ext);
        for(int i=0;f.exists();i++){
            f=new File(f.getParent(),name+"-("+i+")"+"."+ext);
        }

        return f.getPath();
    }

    public static String saveData(InputStream is,String filename) throws IOException{
        String path=getPath(filename);
        FileOutputStream fos=new FileOutputStream(path);
        new Thread1(is,fos).run();
        return path;
    }
    
    public static String getJsonKeyName(Set<String> keySet){
        String val1="";
        for (String val2:keySet){
            val1=val2;
        }
        return val1;
    }

    public static String getMatchDate(long epochTime){
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochTime),ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

}