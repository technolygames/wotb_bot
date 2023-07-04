package logic;

import java.io.File;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

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
}