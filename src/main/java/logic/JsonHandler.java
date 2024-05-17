package logic;

import dbconnection.DbConnection;
import dbconnection.UpdateData;
import threads.Thread2;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

/**
 *
 * @author erick
 */
public class JsonHandler{
    private JsonHandler(){}

    private static Gson gson;
    private static JsonElement je;

    /** 
     * Gets account data from Wg's api.<br>
     * Data returned from Wg's api is in-game name and user ID.<br>
     * Method used to get this values is getNickname().
     * 
     * @return in-game name and user ID used to get info from the api.
    */
    public static JsonObject getAccountData(String nickname){
        try{
            je=JsonParser.parseString(new Thread2().thread(ApiRequest.getData("https://api.wotblitz.com/wotb/account/list/?application_id="+UtilityClass.APP_ID+"&search="+nickname)));
            gson=new GsonBuilder().create();

            return gson.toJsonTree(je).
            getAsJsonObject().
            getAsJsonArray("data").get(0).
            getAsJsonObject();
        }catch(IllegalStateException n){
            UtilityClass.LOGGER.severe(n.fillInStackTrace().toString());
            return null;
        }
    }

    /**
     * Gets tank info from the requesting account.
     * @param accId
     */
    public static void getAccTankData(int accId){
        try(var cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("insert into tank_stats values(?,?,?,?,?,?)");
        PreparedStatement ps2=cn.prepareStatement("select * from tank_stats where player_id=? and tank_id=?");
        PreparedStatement ps3=cn.prepareStatement("select * from tank_list");
        PreparedStatement ps4=cn.prepareStatement("update tank_stats set tank_tier=? where tank_id=?");
        PreparedStatement ps5=cn.prepareStatement("delete from tank_stats where tank_tier<=0")){
            je=JsonParser.parseString(new Thread2().thread(ApiRequest.getData("https://api.wotblitz.com/wotb/tanks/stats/?application_id="+UtilityClass.APP_ID+"&account_id="+accId+"&fields=all%2Ctank_id%2Clast_battle_time")));
            gson=new GsonBuilder().create();

            new Thread(()->{
                var text=gson.toJsonTree(je).getAsJsonObject().getAsJsonObject("data");
                var val=UtilityClass.getJsonKeyName(text.keySet());
                var text2=text.getAsJsonArray(val);
                for(var val2:text2){
                    var val3=val2.getAsJsonObject();
                    var val4=val3.getAsJsonObject("all");

                    String lit1="tank_id";
                    var tankId=val3.get(lit1).getAsInt();
                    try{
                        ps2.setInt(1,accId);
                        ps2.setInt(2,tankId);
                        ResultSet rs=ps2.executeQuery();
                        ResultSet rs2=ps3.executeQuery();
                        if(!rs.next()){
                            ps.setInt(1,Integer.parseInt(val));
                            ps.setInt(2,tankId);
                            ps.setInt(3,0);
                            ps.setInt(4,val4.get("battles").getAsInt());
                            ps.setInt(5,val4.get("wins").getAsInt());
                            ps.setInt(6,val4.get("losses").getAsInt());
                            ps.execute();

                            while(rs2.next()){
                                ps4.setInt(1,rs2.getInt("tank_tier"));
                                ps4.setInt(2,rs2.getInt(lit1));
                                ps4.execute();
                            }

                            ps5.execute();
                        }
                    }catch(SQLException e){
                        UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
                    }
                }
            });
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
        }catch(IllegalStateException x){
            UtilityClass.LOGGER.severe(x.fillInStackTrace().toString());
        }
    }

    /**
     * 
     * @param accId
     * @return
     */
    public static void dataManipulation(int accId){
        try(var cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select * from tank_stats where player_id=?");
        PreparedStatement ps2=cn.prepareStatement("update tank_stats set battles=battles+?, wins=wins+?, losses=losses+? where player_id=? and tank_id=?");
        PreparedStatement ps3=cn.prepareStatement("insert into tank_stats values(?,?,?,?,?,?)")){
            ps.setInt(1,accId);
            ResultSet rs=ps.executeQuery();

            je=JsonParser.parseString(new Thread2().thread(ApiRequest.getData("https://api.wotblitz.com/wotb/tanks/stats/?application_id="+UtilityClass.APP_ID+"&account_id="+accId+"&fields=all%2Ctank_id%2Clast_battle_time")));
            gson=new GsonBuilder().create();
            getAccTankData(accId);

            JsonObject data=gson.toJsonTree(je).getAsJsonObject().getAsJsonObject("data");
            String key=UtilityClass.getJsonKeyName(data.keySet());
            JsonArray text2=data.getAsJsonArray(key);

            while(rs.next()){
                for(JsonElement val2:text2){
                    JsonObject val3=val2.getAsJsonObject();
                    JsonObject val4=val3.getAsJsonObject("all");

                    String lit1="tank_id";

                    int v1=rs.getInt("battles");
                    int v2=rs.getInt("wins");
                    int v3=rs.getInt("losses");

                    int value1=val4.get("battles").getAsInt();
                    int value2=val4.get("wins").getAsInt();
                    int value3=val4.get("losses").getAsInt();
                    int value4=val3.get(lit1).getAsInt();

                    if(rs.getInt(lit1)==value4&&v1!=value1){
                        if(value2>=v2&&value3>=v3){
                            ps2.setInt(1,UtilityClass.calculateDifference(value1,v1));
                            ps2.setInt(2,UtilityClass.calculateDifference(value2,v2));
                            ps2.setInt(3,UtilityClass.calculateDifference(value3,v3));
                            ps2.setInt(4,accId);
                            ps2.setInt(5,value4);

                            ps2.execute();
                        }
                        break;
                    }
                }
            }
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
        }catch(IllegalStateException x){
            UtilityClass.LOGGER.severe(x.fillInStackTrace().toString());
        }
    }

    public static void updatePlayerNickname(int wotbId){
        try(var cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select wotb_name from user_data where wotb_id=?");
        PreparedStatement ps2=cn.prepareStatement("select wotb_name from team where wotb_id=?")){
            ps.setInt(1,wotbId);
            ps2.setInt(1,wotbId);
            ResultSet rs=ps.executeQuery();
            ResultSet rs2=ps2.executeQuery();
            je=JsonParser.parseString(new Thread2().thread(ApiRequest.getData("https://api.wotblitz.com/wotb/account/info/?application_id="+UtilityClass.APP_ID+"&fields=nickname&account_id="+wotbId)));
            gson=new GsonBuilder().create();

            var data1=gson.toJsonTree(je).
            getAsJsonObject().
            getAsJsonObject("data").
            getAsJsonObject();

            var data2=data1.
            getAsJsonObject(UtilityClass.getJsonKeyName(data1.keySet())).
            getAsJsonObject();

            if(rs.next()){
                String apiNickname=data2.get("nickname").getAsString();
                if(!rs.getString("wotb_name").equals(apiNickname)){
                    UpdateData.updateNicknameFromUserData(apiNickname,wotbId);
                }
            }
            if(rs2.next()){
                String apiNickname=data2.get("nickname").getAsString();
                if(!rs2.getString("wotb_name").equals(apiNickname)){
                    UpdateData.updateNicknameFromTeamData(apiNickname,wotbId);
                }
            }
            
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
        }catch(IllegalStateException x){
            UtilityClass.LOGGER.severe(x.fillInStackTrace().toString());
        }
    }

    public static void getTankInfo(){
        try(var cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("insert into tank_list values(?,?,?,?)");
        PreparedStatement ps2=cn.prepareStatement("select * from tank_list where tank_id=?")){
            je=JsonParser.parseString(new Thread2().thread(ApiRequest.getData("https://api.wotblitz.com/wotb/encyclopedia/vehicles/?application_id="+UtilityClass.APP_ID+"&fields=tank_id%2Ctier%2Cnation%2Cname")));
            gson=new GsonBuilder().create();

            var data1=gson.toJsonTree(je).
            getAsJsonObject().
            getAsJsonObject("data");
            
            for(String string1:data1.keySet()){
                var data2=data1.
                getAsJsonObject().
                getAsJsonObject(string1).
                getAsJsonObject();

                int tankId=data2.get("tank_id").getAsInt();
                int tankTier=data2.get("tier").getAsInt();
                ps2.setInt(1,tankId);
                ResultSet rs=ps2.executeQuery();
                if(!rs.next()&&tankTier>4){
                    ps.setInt(1,tankId);
                    ps.setString(2,data2.get("name").getAsString());
                    ps.setString(3,data2.get("nation").getAsString());
                    ps.setInt(4,tankTier);

                    ps.execute();
                }
            }
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
        }catch(IllegalStateException x){
            UtilityClass.LOGGER.severe(x.fillInStackTrace().toString());
        }
    }

    protected class ApiRequest{
        private ApiRequest(){}

        /**
         * Makes requests to the WG's public API
         * @param link
         * @return data from WG's API
         */
        public static InputStream getData(String link){
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
                UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
                return null;
            }catch(MalformedURLException x){
                UtilityClass.LOGGER.severe(x.fillInStackTrace().toString());
                return null;
            }catch(IOException s){
                UtilityClass.LOGGER.severe(s.fillInStackTrace().toString());
                return null;
            }
        }
    }
}