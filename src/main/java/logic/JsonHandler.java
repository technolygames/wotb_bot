package logic;

import dbconnection.DbConnection;
import dbconnection.UpdateData;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.ProtocolException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;

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
     * @param nickname
     * @param realm
     * @return in-game name and user ID used to get info from the api.
    */
    public static JsonObject getAccountData(String nickname,String realm){
        try{
            je=JsonParser.parseString(getData(UtilityClass.getRealm(realm)+"/wotb/account/list/?application_id="+UtilityClass.APP_ID+"&search="+nickname));
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
        PreparedStatement ps2=cn.prepareStatement("select tank_id from tank_stats where wotb_id=? and tank_id=?");
        PreparedStatement ps3=cn.prepareStatement("select tank_tier from tank_list where tank_id=?");
        PreparedStatement ps4=cn.prepareStatement("update tank_stats set tank_tier=? where tank_id=?");
        PreparedStatement ps5=cn.prepareStatement("delete from tank_stats where tank_tier<=0");
        PreparedStatement ps6=cn.prepareStatement("select realm from team where wotb_id=?")){
            ps6.setInt(1,accId);
            updatePlayerNickname(accId);
            ResultSet rs=ps6.executeQuery();
            String realm="";
            while(rs.next()){
                realm=UtilityClass.getRealm(rs.getString("realm"));
            }
            je=JsonParser.parseString(getData(realm+"/wotb/tanks/stats/?application_id="+UtilityClass.APP_ID+"&account_id="+accId+"&fields=all%2Ctank_id%2Clast_battle_time"));
            gson=new GsonBuilder().create();

            var text=gson.toJsonTree(je).getAsJsonObject().getAsJsonObject("data");
            var text2=text.getAsJsonArray(UtilityClass.getJsonKeyName(text.keySet()));
            for(var val2:text2){
                var val3=val2.getAsJsonObject();
                var val4=val3.getAsJsonObject("all");
                var tankId=val3.get("tank_id").getAsInt();

                ps2.setInt(1,accId);
                ps2.setInt(2,tankId);
                ps3.setInt(1,tankId);
                ResultSet rs2=ps2.executeQuery();
                ResultSet rs3=ps3.executeQuery();
                if(!rs2.next()){
                    ps.setInt(1,accId);
                    ps.setInt(2,tankId);
                    ps.setInt(3,0);
                    ps.setInt(4,val4.get("battles").getAsInt());
                    ps.setInt(5,val4.get("wins").getAsInt());
                    ps.setInt(6,val4.get("losses").getAsInt());
                    ps.addBatch();
                    ps.executeBatch();

                    while(rs3.next()){
                        ps4.setInt(1,rs3.getInt("tank_tier"));
                        ps4.setInt(2,tankId);
                        ps4.addBatch();
                        ps4.executeBatch();
                    }

                    ps5.execute();
                }
            }
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
        }catch(IllegalStateException x){
            UtilityClass.LOGGER.severe(x.fillInStackTrace().toString());
        }
    }

    /**
     * 
     * @param accId
     */
    public static void dataManipulation(int accId){
        try(var cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select tank_id,battles,wins,losses from tank_stats where wotb_id=?");
        PreparedStatement ps2=cn.prepareStatement("update tank_stats set battles=battles+?, wins=wins+?, losses=losses+? where wotb_id=? and tank_id=?");
        PreparedStatement ps3=cn.prepareStatement("select realm from team where wotb_id=?")){
            ps3.setInt(1,accId);
            updatePlayerNickname(accId);
            ResultSet rs=ps3.executeQuery();
            String realm="";
            while(rs.next()){
                realm=UtilityClass.getRealm(rs.getString("realm"));
            }

            ps.setInt(1,accId);
            ResultSet rs2=ps.executeQuery();

            je=JsonParser.parseString(getData(realm+"/wotb/tanks/stats/?application_id="+UtilityClass.APP_ID+"&account_id="+accId+"&fields=all%2Ctank_id%2Clast_battle_time"));
            gson=new GsonBuilder().create();
            getAccTankData(accId);

            JsonObject data=gson.toJsonTree(je).getAsJsonObject().getAsJsonObject("data");
            String key=UtilityClass.getJsonKeyName(data.keySet());
            JsonArray text2=data.getAsJsonArray(key);

            while(rs2.next()){
                for(JsonElement val2:text2){
                    JsonObject val3=val2.getAsJsonObject();
                    JsonObject val4=val3.getAsJsonObject("all");

                    int v1=rs2.getInt("battles");
                    int v2=rs2.getInt("wins");
                    int v3=rs2.getInt("losses");

                    int value1=val4.get("battles").getAsInt();
                    int value2=val4.get("wins").getAsInt();
                    int value3=val4.get("losses").getAsInt();
                    int value4=val3.get("tank_id").getAsInt();

                    if(rs2.getInt("tank_id")==value4&&v1!=value1){
                        if(value2>=v2&&value3>=v3){
                            ps2.setInt(1,UtilityClass.calculateDifference(value1,v1));
                            ps2.setInt(2,UtilityClass.calculateDifference(value2,v2));
                            ps2.setInt(3,UtilityClass.calculateDifference(value3,v3));
                            ps2.setInt(4,accId);
                            ps2.setInt(5,value4);

                            ps2.addBatch();
                            ps2.executeBatch();
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

    /**
     * @param wotbId
    */
    protected static void updatePlayerNickname(int wotbId){
        try(var cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select wotb_name,realm from team where wotb_id=?")){
            ps.setInt(1,wotbId);
            ResultSet rs=ps.executeQuery();
            
            if(rs.next()){
                je=JsonParser.parseString(getData(UtilityClass.getRealm(rs.getString("realm"))+"/wotb/account/info/?application_id="+UtilityClass.APP_ID+"&fields=nickname&account_id="+wotbId));
                gson=new GsonBuilder().create();

                var data1=gson.toJsonTree(je).
                getAsJsonObject().
                getAsJsonObject("data").
                getAsJsonObject();

                var data2=data1.
                getAsJsonObject(UtilityClass.getJsonKeyName(data1.keySet())).
                getAsJsonObject();
                String apiNickname=data2.get("nickname").getAsString();
                if(!rs.getString("wotb_name").equals(apiNickname)){
                    UpdateData.updateNicknameFromTeamData(apiNickname,wotbId);
                }
            }
            
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
        }catch(IllegalStateException x){
            UtilityClass.LOGGER.severe(x.fillInStackTrace().toString());
        }
    }

    /**
     * 
     */
    public static void getTankInfo(){
        try(var cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("insert into tank_list values(?,?,?,?)");
        PreparedStatement ps2=cn.prepareStatement("select tank_name from tank_list where tank_id=?")){
            je=JsonParser.parseString(getData("https://api.wotblitz.com/wotb/encyclopedia/vehicles/?application_id="+UtilityClass.APP_ID+"&fields=tank_id%2Ctier%2Cnation%2Cname"));
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
                ResultSet rs2=ps2.executeQuery();
                if(!rs2.next()&&tankTier>4){
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

    /**
     * Makes requests to the WG's public API
     * @param link
     * @return data from WG's API
     */
    protected static String getData(String link){
        try{
            URL u=new URL(link);
            HttpURLConnection u2=(HttpURLConnection)u.openConnection();
            u2.setRequestMethod("GET");
            u2.setRequestProperty("Accept","application/json");
            if(u2.getResponseCode()==HttpURLConnection.HTTP_OK){
                try(BufferedReader br=new BufferedReader(new InputStreamReader(u2.getInputStream(),StandardCharsets.UTF_8))){
                    String line="";
                    for(String line2;(line2=br.readLine())!=null;){
                        line=line2;
                    }
                    return line;
                }
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