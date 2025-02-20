package logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * @author erick
 */
public class WebControl{
    /**
     * @param url
     * @return
     */
    public boolean checkConnection(String url){
        UtilityClass uc=new UtilityClass();
        boolean flag=false;
        String sanitizedUrl=url.replaceAll("^https?://","");
        try{
            InetAddress ia=InetAddress.getByName(sanitizedUrl);
            flag=ia.isReachable(3000);
        }catch(UnknownHostException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }catch(IOException x){
            uc.log(Level.SEVERE,x.getMessage(),x);
        }
        return flag;
    }

    /**
     * Makes requests to the WG's public API
     * @param link
     * @return data from WG's API
     */
    protected String getData(String link){
        UtilityClass uc=new UtilityClass();
        String line="";
        try{
            URL u=new URL(link);
            HttpURLConnection huc=(HttpURLConnection)u.openConnection();
            huc.setRequestMethod("GET");
            huc.setRequestProperty("Accept","application/json");
            if(huc.getResponseCode()==HttpURLConnection.HTTP_OK){
                try(InputStream is=huc.getInputStream();
                        InputStreamReader isr=new InputStreamReader(is,StandardCharsets.UTF_8);
                        BufferedReader br=new BufferedReader(isr)){
                    for(String line2;(line2=br.readLine())!=null;){
                        line=line2;
                    }
                }
            }
        }catch(ProtocolException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }catch(MalformedURLException x){
            uc.log(Level.SEVERE,x.getMessage(),x);
        }catch(IOException s){
            uc.log(Level.SEVERE,s.getMessage(),s);
        }
        return line;
    }
}