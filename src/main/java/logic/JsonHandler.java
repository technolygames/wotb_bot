package logic;

import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;

public class JsonHandler{
    public JsonObject getAccountData(String dir){
        try{
            JsonElement je=JsonParser.parseReader(new JsonReader(new FileReader(dir,StandardCharsets.UTF_8)));
            Gson gson=new GsonBuilder().create();

            return gson.toJsonTree(je).
            getAsJsonObject().
            getAsJsonArray("data").get(0).
            getAsJsonObject();
        }catch(FileNotFoundException e){
            ApiRequest.LOGGER.config(e.fillInStackTrace().toString());
            return null;
        }catch(IOException x){
            ApiRequest.LOGGER.config(x.fillInStackTrace().toString());
            return null;
        }catch(IllegalStateException n){
            ApiRequest.LOGGER.config(n.fillInStackTrace().toString());
            return null;
        }
    }

    public JsonObject getStatData(String dir,String user){
        try{
            JsonElement je=JsonParser.parseReader(new JsonReader(new FileReader(dir,StandardCharsets.UTF_8)));
            Gson gson=new GsonBuilder().create();

            return gson.toJsonTree(je).
            getAsJsonObject().
            getAsJsonObject("data").
            getAsJsonObject(user).
            getAsJsonObject("statistics").
            getAsJsonObject("all");
        }catch(FileNotFoundException e){
            ApiRequest.LOGGER.config(e.fillInStackTrace().toString());
            return null;
        }catch(IOException x){
            ApiRequest.LOGGER.config(x.fillInStackTrace().toString());
            return null;
        }catch(IllegalStateException n){
            ApiRequest.LOGGER.config(n.fillInStackTrace().toString());
            return null;
        }
    }

    public JsonObject getTankStats(String dir,String user){
        try{
            JsonElement je=JsonParser.parseReader(new JsonReader(new FileReader(dir,StandardCharsets.UTF_8)));
            Gson gson=new GsonBuilder().create();

            return gson.toJsonTree(je).
            getAsJsonObject().
            getAsJsonObject("data").
            getAsJsonObject(user).
            getAsJsonObject("all");
        }catch(FileNotFoundException e){
            ApiRequest.LOGGER.config(e.fillInStackTrace().toString());
            return null;
        }catch(IOException x){
            ApiRequest.LOGGER.config(x.fillInStackTrace().toString());
            return null;
        }catch(IllegalStateException n){
            ApiRequest.LOGGER.config(n.fillInStackTrace().toString());
            return null;
        }
    }
}