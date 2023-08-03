package logic;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

/**
 *
 * @author erick
 */
public class JsonHandler{
    private JsonHandler(){}

    private static Gson gson;

    public static JsonObject getAccountData(String json){
        try{
            JsonElement je=JsonParser.parseString(json);
            gson=new GsonBuilder().create();

            return gson.toJsonTree(je).
            getAsJsonObject().
            getAsJsonArray("data").get(0).
            getAsJsonObject();
        }catch(IllegalStateException n){
            UtilityClass.LOGGER.info(n.fillInStackTrace().toString());
            return null;
        }
    }

    public static JsonObject getStatData(String json){
        try{
            JsonElement je=JsonParser.parseString(json);
            gson=new GsonBuilder().create();
            var val1=UtilityClass.getJsonKeyName(gson.toJsonTree(je).getAsJsonObject().getAsJsonObject("data").keySet());

            return gson.toJsonTree(je).
            getAsJsonObject().
            getAsJsonObject("data").
            getAsJsonObject(val1).
            getAsJsonObject("statistics").
            getAsJsonObject("all");
        }catch(IllegalStateException n){
            UtilityClass.LOGGER.info(n.fillInStackTrace().toString());
            return null;
        }
    }

    public static JsonObject getTankInfo(String json){
        try{
            JsonElement je=JsonParser.parseString(json);
            gson=new GsonBuilder().create();
            return gson.toJsonTree(je).getAsJsonObject().getAsJsonObject("data");
        }catch(IllegalStateException e){
            UtilityClass.LOGGER.info(e.fillInStackTrace().toString());
            return null;
        }catch(NullPointerException x){
            UtilityClass.LOGGER.info(x.fillInStackTrace().toString());
            return null;
        }
    }

    public static JsonObject getTankStats(String json){
        try{
            var val=getTankInfo(json);
            var val1=UtilityClass.getJsonKeyName(val.keySet());

            return val.
            getAsJsonObject(val1).
            getAsJsonObject("all");
        }catch(IllegalStateException n){
            UtilityClass.LOGGER.info(n.fillInStackTrace().toString());
            return null;
        }catch(NullPointerException x){
            return new JsonObject();
        }
    }
}