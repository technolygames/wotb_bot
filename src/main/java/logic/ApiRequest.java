package logic;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.MalformedURLException;

import threads.Thread2;

/**
 *
 * @author erick
 */
public class ApiRequest{
    public InputStream getData(String link){
        try{
            URL u=new URL(link);
            HttpURLConnection u2=(HttpURLConnection)u.openConnection();
            u2.setRequestMethod("GET");
            u2.setRequestProperty("Accept","application/json");
            if(u2.getResponseCode()==HttpURLConnection.HTTP_OK){
                return u2.getInputStream();
            }else{
                return null;
            }
        }catch(ProtocolException e){
            UtilityClass.LOGGER.info(e.fillInStackTrace().toString());
            return null;
        }catch(MalformedURLException x){
            UtilityClass.LOGGER.info(x.fillInStackTrace().toString());
            return null;
        }catch(IOException s){
            UtilityClass.LOGGER.info(s.fillInStackTrace().toString());
            return null;
        }
    }

    public String getNickname(String nickname){
        try{
            return new Thread2().thread(getData("https://api.wotblitz.com/wotb/account/list/?application_id=fd14e112652ef853caa088328cd5a67d&search="+nickname));
        }catch(NullPointerException x){
            UtilityClass.LOGGER.info(x.fillInStackTrace().toString());
            return null;
        }
    }

    public String getStats(int wotbId){
        try{
            return new Thread2().thread(getData("https://api.wotblitz.com/wotb/account/info/?account_id="+wotbId+"&application_id=fd14e112652ef853caa088328cd5a67d&fields=statistics.all"));
        }catch(NullPointerException e){
            UtilityClass.LOGGER.info(e.fillInStackTrace().toString());
            return null;
        }
    }

    public String getTankData(int accId,int tankId){
        try{
            return new Thread2().thread(getData("https://api.wotblitz.com/wotb/account/tankstats/?application_id=fd14e112652ef853caa088328cd5a67d&tank_id="+tankId+"&account_id="+accId));
        }catch(NullPointerException x){
            UtilityClass.LOGGER.info(x.fillInStackTrace().toString());
            return null;
        }
    }

    public String getTankIdData(){
        try{
            return new Thread2().thread(getData("https://api.wotblitz.com/wotb/encyclopedia/vehicles/?application_id=fd14e112652ef853caa088328cd5a67d&fields=tank_id%2Ctier%2Cname%2Cnation&nation=other"));
        }catch(NullPointerException x){
            UtilityClass.LOGGER.info(x.fillInStackTrace().toString());
            return null;
        }
    }
}