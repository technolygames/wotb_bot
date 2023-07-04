package logic;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class JsonHandler{
    private JsonHandler(){}
    public static JsonObject getAccountData(String json){
        try{
            JsonElement je=JsonParser.parseString(json);
            Gson gson=new GsonBuilder().create();

            return gson.toJsonTree(je).
            getAsJsonObject().
            getAsJsonArray("data").get(0).
            getAsJsonObject();
        }catch(IllegalStateException n){
            UtilityClass.LOGGER.config(n.fillInStackTrace().toString());
            return null;
        }
    }

    public static JsonObject getStatData(String json,String user){
        try{
            JsonElement je=JsonParser.parseString(json);
            Gson gson=new GsonBuilder().create();

            return gson.toJsonTree(je).
            getAsJsonObject().
            getAsJsonObject("data").
            getAsJsonObject(user).
            getAsJsonObject("statistics").
            getAsJsonObject("all");
        }catch(IllegalStateException n){
            UtilityClass.LOGGER.config(n.fillInStackTrace().toString());
            return null;
        }
    }

    public static JsonObject getTankStats(String json,String user){
        try{
            JsonElement je=JsonParser.parseString(json);
            Gson gson=new GsonBuilder().create();

            return gson.toJsonTree(je).
            getAsJsonObject().
            getAsJsonObject("data").
            getAsJsonObject(user).
            getAsJsonObject("all");
        }catch(IllegalStateException n){
            UtilityClass.LOGGER.config(n.fillInStackTrace().toString());
            return null;
        }
    }
}