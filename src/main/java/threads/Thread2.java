package threads;

import logic.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;

/**
 *
 * @author erick
 */
public class Thread2{
    public String thread(InputStream is){
        try(BufferedReader bis=new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8))){
            String lineString="";
            for(String line;(line=bis.readLine())!=null;){
                lineString=line;
            }
            return lineString;
        }catch(IOException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
            return null;
        }
    }
}