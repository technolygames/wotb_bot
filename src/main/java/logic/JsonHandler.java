package logic;

import dbconnection.DbConnection;
import dbconnection.UpdateData;

import java.util.StringJoiner;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.ProtocolException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.GsonBuilder;
import dbconnection.GetData;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import mvc.Mvc1;

/**
 *
 * @author erick
 */
public class JsonHandler{
    /** 
     * Gets account data from Wg api.<br>
     * Data obtained is in-game name and user ID.
     * @param nickname
     * @param realm
     * @return in-game name and user ID used to get info from the api.
    */
    public Mvc1 getAccountData(String nickname,String realm){
        try{
            JsonElement je=JsonParser.parseString(getData(UtilityClass.getRealm(realm)+"/wotb/account/list/?application_id="+UtilityClass.APP_ID+"&search="+nickname));
            JsonObject data=new GsonBuilder().create().toJsonTree(je).getAsJsonObject().getAsJsonArray("data").get(0).getAsJsonObject();
            Mvc1 model=new Mvc1();
            model.setNickname(data.get("nickname").getAsString());
            model.setAcoountId(data.get("account_id").getAsInt());
            return model;
        }catch(IllegalStateException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return null;
        }
    }

    /**
     * Gets tank info from the requesting account.
     * @param accId
     */
    public void getAccTankData(int accId){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("insert into tank_stats values(?,?,?,?,?,?,?)");
                PreparedStatement ps2=cn.prepareStatement("select tank_id from tank_stats where wotb_id=?");
                PreparedStatement ps3=cn.prepareStatement("select tank_id,tank_tier from tank_list where tank_id=?")){
            JsonElement je=JsonParser.parseString(getData(new GetData().getRealm(accId)+"/wotb/tanks/stats/?application_id="+UtilityClass.APP_ID+"&account_id="+accId+"&fields=tank_id%2Clast_battle_time%2Call.battles%2Call.wins%2Call.losses"));
            JsonObject data=new GsonBuilder().create().toJsonTree(je).getAsJsonObject().getAsJsonObject("data");
            if(!data.isEmpty()){
                for(var val2:data.getAsJsonArray(UtilityClass.getJsonKeyName(data.keySet()))){
                    var val3=val2.getAsJsonObject();
                    var val4=val3.getAsJsonObject("all");
                    var tankId=val3.get("tank_id").getAsInt();

                    ps2.setInt(1,accId);
                    ps3.setInt(1,tankId);
                    try(ResultSet rs=ps2.executeQuery();
                            ResultSet rs2=ps3.executeQuery()){
                        while(rs2.next()&&!rs.next()){
                            if(rs2.getInt("tank_id")==tankId){
                                ps.setInt(1,accId);
                                ps.setInt(2,tankId);
                                ps.setInt(3,rs2.getInt("tank_tier"));
                                ps.setInt(4,val4.get("battles").getAsInt());
                                ps.setInt(5,val4.get("wins").getAsInt());
                                ps.setInt(6,val4.get("losses").getAsInt());
                                ps.setString(7,UtilityClass.getMatchDate(val3.get("last_battle_time").getAsLong()));
                                ps.addBatch();
                            }
                        }
                    }
                }
                ps.executeBatch();
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }catch(IllegalStateException x){
            new UtilityClass().log(Level.SEVERE,x.getMessage(),x);
        }catch(NullPointerException s){
            new UtilityClass().log(Level.SEVERE,s.getMessage(),s);
        }
    }

    /**
     * 
     * @param accId
     */
    public void dataManipulation(int accId){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("select tank_id,tank_tier from tank_list");
                PreparedStatement ps2=cn.prepareStatement("select tank_id,battles,wins,losses,last_time_battle from tank_stats where wotb_id=?");
                PreparedStatement ps3=cn.prepareStatement("update tank_stats set battles=battles+?, wins=wins+?, losses=losses+?, last_time_battle=? where wotb_id=? and tank_id=?");
                PreparedStatement ps4=cn.prepareStatement("insert into tank_stats value(?,?,?,?,?,?,?)");
                PreparedStatement ps5=cn.prepareStatement("select last_time_battle from tank_stats where wotb_id=? and tank_id=?")){
            updatePlayerNickname(accId);
            StringJoiner tankIdList=new StringJoiner(",");
            int tier=0;

            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    tankIdList.add(String.valueOf(rs.getInt("tank_id")));
                    tier=rs.getInt("tank_tier");
                }
            }

            if(tankIdList.length()==0){
                return;
            }

            JsonElement je=JsonParser.parseString(getData(new GetData().getRealm(accId)+"/wotb/tanks/stats/?application_id="+UtilityClass.APP_ID+"&fields=tank_id%2Clast_battle_time%2Call.battles%2Call.wins%2Call.losses&tank_id="+tankIdList.toString()+"&account_id="+accId));
            JsonObject data=new GsonBuilder().create().toJsonTree(je).getAsJsonObject().getAsJsonObject("data");
            if(!data.isEmpty()){
                for(JsonElement val2:data.getAsJsonArray(UtilityClass.getJsonKeyName(data.keySet()))){
                    JsonObject val3=val2.getAsJsonObject();
                    JsonObject val4=val3.getAsJsonObject("all");

                    int value1=val4.get("battles").getAsInt();
                    int value2=val4.get("wins").getAsInt();
                    int value3=val4.get("losses").getAsInt();
                    int value4=val3.get("tank_id").getAsInt();
                    String value5=UtilityClass.getMatchDate(val3.get("last_battle_time").getAsLong());

                    ps5.setInt(1,accId);
                    ps5.setInt(2,value4);
                    try(ResultSet rs2=ps5.executeQuery()){
                        if(!rs2.next()){
                            System.out.println("hay tanque nuevo: "+value4);
                            ps4.setInt(1,accId);
                            ps4.setInt(2,value4);
                            ps4.setInt(3,tier);
                            ps4.setInt(4,value1);
                            ps4.setInt(5,value2);
                            ps4.setInt(6,value3);
                            ps4.setString(7,value5);
                            ps4.executeUpdate();
                        }
                    }

                    ps2.setInt(1,accId);
                    try(ResultSet rs3=ps2.executeQuery()){
                        while(rs3.next()){
                            int v1=rs3.getInt("battles");
                            int v2=rs3.getInt("wins");
                            int v3=rs3.getInt("losses");
                            int v4=rs3.getInt("tank_id");
                            String v5=rs3.getString("last_time_battle");

                            if(v4==value4&&(value1!=v1||value2!=v2||value3!=v3)){
                                System.out.println("si hay cambios de: "+accId+", del tanque: "+value4);
                                ps3.setInt(1,UtilityClass.calculateDifference(value1,v1));
                                ps3.setInt(2,UtilityClass.calculateDifference(value2,v2));
                                ps3.setInt(3,UtilityClass.calculateDifference(value3,v3));
                                ps3.setString(4,value5);
                                ps3.setInt(5,accId);
                                ps3.setInt(6,value4);

                                ps3.addBatch();
                            }
                        }
                    }
                }
                ps3.executeBatch();
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }catch(IllegalStateException x){
            new UtilityClass().log(Level.SEVERE,x.getMessage(),x);
        }
    }

    /**
     * @param wotbId
    */
    protected void updatePlayerNickname(int wotbId){
        try(Connection cn=DbConnection.getConnection();
                PreparedStatement ps=cn.prepareStatement("select wotb_name,realm from team where wotb_id=?")){
            ps.setInt(1,wotbId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    JsonElement je=JsonParser.parseString(getData(UtilityClass.getRealm(rs.getString("realm"))+"/wotb/account/info/?application_id="+UtilityClass.APP_ID+"&fields=nickname&account_id="+wotbId));
                    JsonObject data=new GsonBuilder().create().toJsonTree(je).getAsJsonObject().getAsJsonObject("data");
                    if(!data.isEmpty()){
                        JsonObject playerData=data.getAsJsonObject(UtilityClass.getJsonKeyName(data.keySet()));
                        String apiNickname=playerData.get("nickname").getAsString();
                        if(!rs.getString("wotb_name").equals(apiNickname)){
                            new UpdateData().updateNicknameFromTeamData(apiNickname,wotbId);
                        }
                    }
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }catch(IllegalStateException x){
            new UtilityClass().log(Level.SEVERE,x.getMessage(),x);
        }catch(NullPointerException s){
            new UtilityClass().log(Level.SEVERE,s.getMessage(),s);
        }
    }

    /**
     * Makes requests to the WG's public API
     * @param link
     * @return data from WG's API
     */
    protected String getData(String link){
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
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return null;
        }catch(MalformedURLException x){
            new UtilityClass().log(Level.SEVERE,x.getMessage(),x);
            return null;
        }catch(IOException s){
            new UtilityClass().log(Level.SEVERE,s.getMessage(),s);
            return null;
        }
    }
}