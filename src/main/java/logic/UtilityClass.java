package logic;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

public class UtilityClass{
    public String getPath(String file){
        String name=FilenameUtils.getBaseName(file);
        String ext=FilenameUtils.getExtension(file);

        File f=new File("data/json",name+"."+ext);
        for(int i=0;f.exists();i++){
            f=new File(f.getParent(),name+"-("+i+")"+"."+ext);
        }

        return f.getPath();
    }
}