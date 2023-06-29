/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logic;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.MalformedURLException;

import java.util.logging.Logger;

import threads.Thread1;

/**
 *
 * @author erick
 */
public class ApiRequest{
    public static final Logger LOGGER=Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

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
            LOGGER.config(e.fillInStackTrace().toString());
            return null;
        }catch(MalformedURLException x){
            LOGGER.config(x.fillInStackTrace().toString());
            return null;
        }catch(IOException s){
            LOGGER.config(s.fillInStackTrace().toString());
            return null;
        }
    }

    public String getNickname(String nickname){
        try{
            InputStream is=getData("https://api.wotblitz.com/wotb/account/list/?application_id=fd14e112652ef853caa088328cd5a67d&search="+nickname);
            String path=new UtilityClass().getPath("acc-data.json");
            FileOutputStream fos=new FileOutputStream(path);
            new Thread1(is,fos).run();

            return path;
        }catch(IOException e){
            LOGGER.config(e.fillInStackTrace().toString());
            return null;
        }catch(NullPointerException x){
            LOGGER.config(x.fillInStackTrace().toString());
            return null;
        }
    }

    public String getTankData(int accId,int tankId){
        try{
            InputStream is=getData("https://api.wotblitz.com/wotb/account/tankstats/?application_id=fd14e112652ef853caa088328cd5a67d&tank_id="+tankId+"&account_id="+accId);
            String path=new UtilityClass().getPath("tank-data.json");
            FileOutputStream fos=new FileOutputStream(path);
            new Thread1(is,fos).run();

            return path;
        }catch(IOException e){
            LOGGER.config(e.fillInStackTrace().toString());
            return null;
        }catch(NullPointerException x){
            LOGGER.config(x.fillInStackTrace().toString());
            return null;
        }
    }

    public String getTankIdData(){
        try{
            InputStream is=getData("https://api.wotblitz.com/wotb/encyclopedia/vehicles/?application_id=fd14e112652ef853caa088328cd5a67d&fields=name%2Ctank_id%2Ctier");
            String path=new UtilityClass().getPath("tankid-data.json");
            FileOutputStream fos=new FileOutputStream(path);
            new Thread1(is,fos).run();

            return path;
        }catch(IOException e){
            LOGGER.config(e.fillInStackTrace().toString());
            return null;
        }catch(NullPointerException x){
            LOGGER.config(x.fillInStackTrace().toString());
            return null;
        }
    }

    public String test(String link){
        try{
            InputStream is=getData(link);
            String path=new UtilityClass().getPath("test.json");
            FileOutputStream fos=new FileOutputStream(path);
            new Thread1(is,fos).run();

            return path;
        }catch(IOException e){
            LOGGER.config(e.fillInStackTrace().toString());
            return null;
        }catch(NullPointerException x){
            LOGGER.config(x.fillInStackTrace().toString());
            return null;
        }
    }
}