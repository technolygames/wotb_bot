package threads;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;

import logic.ApiRequest;
import logic.UtilityClass;

public class Thread2{
    public String thread(InputStream is){
        String lineString="";
        try(BufferedReader bis=new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8))){
            for(String line;(line=bis.readLine())!=null;){
                lineString=line;
            }
            return lineString;
        }catch(IOException e){
            UtilityClass.LOGGER.config(e.fillInStackTrace().toString());
            return null;
        }
    }
}
