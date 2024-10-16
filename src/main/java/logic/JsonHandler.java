package logic;

import mvc.Mvc1;
import mvc.Mvc2;
import dbconnection.GetData;
import dbconnection.UpdateData;
import dbconnection.DbConnection;
import dbconnection.DeleteData;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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
import com.google.gson.JsonArray;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
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
            JsonElement je=new GsonBuilder().create().toJsonTree(JsonParser.parseString(getData(UtilityClass.getRealm(realm)+"/wotb/account/list/?application_id="+UtilityClass.APP_ID+"&search="+nickname)));
            JsonObject data=je.getAsJsonObject().getAsJsonArray("data").get(0).getAsJsonObject();
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
     * @param accId
     * @param realm
     * @return
     */
    public Mvc1 getAccountData(int accId,String realm){
        try{
            JsonElement je=new GsonBuilder().create().toJsonTree(JsonParser.parseString(getData(UtilityClass.getRealm(realm)+"/wotb/account/info/?application_id="+UtilityClass.APP_ID+"&account_id="+accId+"&fields=nickname")));
            JsonObject data=je.getAsJsonObject().getAsJsonObject("data");
            JsonObject data2=data.getAsJsonObject(UtilityClass.getJsonKeyName(data.keySet()));
            Mvc1 model=new Mvc1();
            model.setNickname(data2.get("nickname").getAsString());
            model.setAcoountId(accId);
            return model;
        }catch(IllegalStateException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return null;
        }
    }

    /**
     * @param clantag
     * @param realm
     * @return
     */
    public Mvc2 getClanData(String clantag,String realm){
        try{
            JsonElement je=new GsonBuilder().create().toJsonTree(JsonParser.parseString(getData(UtilityClass.getRealm(realm)+"/wotb/clans/list/?application_id=fd14e112652ef853caa088328cd5a67d&search="+clantag+"&fields=clan_id%2Ctag")));
            JsonObject data=je.getAsJsonObject().getAsJsonObject("meta");
            JsonArray data2=je.getAsJsonObject().getAsJsonArray("data");
            Mvc2 model=new Mvc2();
            for(int i=0;i<data.get("total").getAsInt();i++){
                JsonObject val=data2.get(i).getAsJsonObject();
                String val1=val.get("tag").getAsString();
                if(val1.equals(clantag)){
                    model.setClanId(val.get("clan_id").getAsInt());
                    model.setClantag(val.get("tag").getAsString());
                    model.setRealm(realm);
                }
            }
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
        PreparedStatement ps=cn.prepareStatement("insert into tank_stats values(?,?,?,?)");
        PreparedStatement ps2=cn.prepareStatement("select tank_id from tank_stats where wotb_id=?")){
            JsonElement je=new GsonBuilder().create().toJsonTree(JsonParser.parseString(getData(new GetData().getRealm(accId)+"/wotb/tanks/stats/?application_id="+UtilityClass.APP_ID+"&account_id="+accId+"&tank_id="+new GetData().getTierTenTankList()+"&fields=tank_id%2Call.battles%2Call.wins")));
            JsonObject data=je.getAsJsonObject().getAsJsonObject("data");
            if(!data.isEmpty()){
                for(var val2:data.getAsJsonArray(UtilityClass.getJsonKeyName(data.keySet()))){
                    var val3=val2.getAsJsonObject();
                    var val4=val3.getAsJsonObject("all");
                    var tankId=val3.get("tank_id").getAsInt();

                    ps2.setInt(1,accId);
                    try(ResultSet rs=ps2.executeQuery()){
                        if(!rs.next()){
                            ps.setInt(1,accId);
                            ps.setInt(2,tankId);
                            ps.setInt(3,val4.get("battles").getAsInt());
                            ps.setInt(4,val4.get("wins").getAsInt());
                            
                            ps.addBatch();
                        }
                    }
                }
                ps.executeBatch();
                dataValidation(accId);
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }catch(IllegalStateException x){
            new UtilityClass().log(Level.SEVERE,x.getMessage(),x);
        }
    }

    /**
     * @param accId
     */
    public void dataValidation(int accId){
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select sum(battles) as battles from tank_stats where wotb_id=?");
        PreparedStatement ps2=cn.prepareStatement("select tank_id from tank_data where tank_tier=?");
        PreparedStatement ps3=cn.prepareStatement("insert into thousand_battles values(?,?,?,?)");
        PreparedStatement ps4=cn.prepareStatement("select tank_id from thousand_battles where wotb_id=?")){
            List<List<String>> tankIdLists=new ArrayList<>();
            List<String> currentTankIdList=new ArrayList<>();
            tankIdLists.add(currentTankIdList);

            int currentTier=9;
            int totalWins=0;
            int maxWins=UtilityClass.MAX_BATTLE_COUNT;
            int remainingWins=maxWins;

            for(int i=9;i>=5;i--){
                ps2.setInt(1,i);
                try(ResultSet rs2=ps2.executeQuery()){
                    while(rs2.next()){
                        String tankId=rs2.getString("tank_id");
                        if(currentTankIdList.size()==100){
                            currentTankIdList=new ArrayList<>();
                            tankIdLists.add(currentTankIdList);
                        }
                        currentTankIdList.add(tankId);
                    }
                }
                currentTier=i;
            }

            for(List<String> tankIdList:tankIdLists){
                JsonElement je=new GsonBuilder().create().toJsonTree(JsonParser.parseString(getData(new GetData().getRealm(accId)+"/wotb/tanks/stats/?application_id="+UtilityClass.APP_ID+"&account_id="+accId+"&fields=tank_id%2Call.battles%2Call.wins&tank_id="+String.join(",",tankIdList))));
                JsonObject data=je.getAsJsonObject().getAsJsonObject("data");
                if(!data.isEmpty()){
                    for(JsonElement val2:data.getAsJsonArray(UtilityClass.getJsonKeyName(data.keySet()))){
                        JsonObject val3=val2.getAsJsonObject();
                        JsonObject val4=val3.getAsJsonObject("all");

                        int apiBattles=val4.get("battles").getAsInt();
                        int apiWins=val4.get("wins").getAsInt();

                        ps.setInt(1,accId);
                        ps4.setInt(1,accId);
                        try(ResultSet rs=ps.executeQuery();
                        ResultSet rs3=ps4.executeQuery()){
                            if(rs.next()||!rs3.next()){
                                int battles=rs.getInt("battles");
                                if(battles<maxWins){
                                    if(currentTier==5&&remainingWins<150){
                                        apiWins=0;
                                    }

                                    totalWins+=apiWins;
                                    remainingWins=maxWins-totalWins;

                                    ps3.setInt(1,accId);
                                    ps3.setInt(2,val3.get("tank_id").getAsInt());
                                    ps3.setInt(3,apiBattles);
                                    ps3.setInt(4,apiWins);
                                    ps3.addBatch();

                                    if(totalWins>=maxWins){
                                        ps3.executeBatch();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    ps3.executeBatch();
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     * @param accId
     */
    public void dataManipulation(int accId){
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select tank_id,battles,wins from tank_stats where wotb_id=?");
        PreparedStatement ps2=cn.prepareStatement("update tank_stats set battles=battles+?, wins=wins+? where wotb_id=? and tank_id=?");
        PreparedStatement ps3=cn.prepareStatement("insert into tank_stats values(?,?,?,?)");
        PreparedStatement ps4=cn.prepareStatement("select tank_id from tank_stats where wotb_id=? and tank_id=?")){
            updatePlayerNickname(accId);
            JsonElement je=new GsonBuilder().create().toJsonTree(JsonParser.parseString(getData(new GetData().getRealm(accId)+"/wotb/tanks/stats/?application_id="+UtilityClass.APP_ID+"&fields=tank_id%2Call.battles%2Call.wins&tank_id="+new GetData().getTierTenTankList()+"&account_id="+accId)));
            JsonObject data=je.getAsJsonObject().getAsJsonObject("data");
            int totalTier10Battles=0;
            
            if(!data.isEmpty()){
                for(JsonElement val2:data.getAsJsonArray(UtilityClass.getJsonKeyName(data.keySet()))){
                    JsonObject val3=val2.getAsJsonObject();
                    JsonObject val4=val3.getAsJsonObject("all");

                    int battles=val4.get("battles").getAsInt();
                    int wins=val4.get("wins").getAsInt();
                    int tankId=val3.get("tank_id").getAsInt();
    
                    totalTier10Battles+=battles;

                    ps4.setInt(1,accId);
                    ps4.setInt(2,tankId);
                    try(ResultSet rs=ps4.executeQuery()){
                        if(!rs.next()){
                            new UtilityClass().log(Level.INFO,accId+" tiene tanque nuevo: "+tankId);
                            ps3.setInt(1,accId);
                            ps3.setInt(2,tankId);
                            ps3.setInt(3,battles);
                            ps3.setInt(4,wins);
                            ps3.executeUpdate();
                        }
                    }

                    ps.setInt(1,accId);
                    try(ResultSet rs2=ps.executeQuery()){
                        while(rs2.next()){
                            int battlesDb=rs2.getInt("battles");
                            int winsDb=rs2.getInt("wins");
                            int tankIdDb=rs2.getInt("tank_id");
                            if(tankIdDb==tankId&&(battles!=battlesDb||wins!=winsDb)){
                                new UtilityClass().log(Level.INFO,"si hay cambios de "+accId+", del tanque "+tankId);
                                ps2.setInt(1,UtilityClass.calculateDifference(battles,battlesDb));
                                ps2.setInt(2,UtilityClass.calculateDifference(wins,winsDb));
                                ps2.setInt(3,accId);
                                ps2.setInt(4,tankId);
                                ps2.executeUpdate();
                            }
                        }
                    }
                }
            }
            thousandBattlesDataManipulation(accId,totalTier10Battles);
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     * @param accId
     * @param battles
     */
    public void thousandBattlesDataManipulation(int accId,int battles){
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("insert into thousand_battles values(?,?,?,?)");
        PreparedStatement ps2=cn.prepareStatement("select tank_id from thousand_battles where wotb_id=? and tank_id=?");
        PreparedStatement ps3=cn.prepareStatement("update thousand_battles set battles=battles+?, wins=wins+? where wotb_id=? and tank_id=?");
        PreparedStatement ps4=cn.prepareStatement("select tank_id,battles,wins from thousand_battles where wotb_id=?")){
            int battleCount=UtilityClass.MAX_BATTLE_COUNT;
            int remainingBattles=battleCount-battles;
            if(battles<battleCount){
                for(List<String> tankIdList:new GetData().getTankLists(accId)){
                    JsonElement je=new GsonBuilder().create().toJsonTree(JsonParser.parseString(getData(new GetData().getRealm(accId)+"/wotb/tanks/stats/?application_id="+UtilityClass.APP_ID+"&account_id="+accId+"&fields=tank_id%2Call.battles%2Call.wins&tank_id="+String.join(",",tankIdList))));
                    JsonObject data=je.getAsJsonObject().getAsJsonObject("data");
                    if(!data.isEmpty()){
                        for(JsonElement val2:data.getAsJsonArray(UtilityClass.getJsonKeyName(data.keySet()))){
                            JsonObject val3=val2.getAsJsonObject();
                            JsonObject val4=val3.getAsJsonObject("all");

                            int battles2=val4.get("battles").getAsInt();
                            int wins=val4.get("wins").getAsInt();
                            int tankId=val3.get("tank_id").getAsInt();
                            
                            ps2.setInt(1,accId);
                            ps2.setInt(2,tankId);
                            try(ResultSet rs=ps2.executeQuery()){
                                if(!rs.next()){
                                    new UtilityClass().log(Level.INFO,accId+" tiene tanque nuevo: "+tankId);
                                    ps.setInt(1,accId);
                                    ps.setInt(2,tankId);
                                    ps.setInt(3,battles2);
                                    ps.setInt(4,wins);
                                    ps.executeUpdate();
                                }
                            }

                            ps4.setInt(1,accId);
                            try(ResultSet rs2=ps4.executeQuery()){
                                while(rs2.next()){
                                    int battlesDb=rs2.getInt("battles");
                                    int winsDb=rs2.getInt("wins");
                                    int tankIdDb=rs2.getInt("tank_id");

                                    if(tankIdDb==tankId&&(battles2!=battlesDb||wins!=winsDb)){
                                        new UtilityClass().log(Level.INFO,"si hay cambios de "+accId+", del tanque "+tankId);
                                        ps3.setInt(1,UtilityClass.calculateDifference(battles2,battlesDb));
                                        ps3.setInt(2,UtilityClass.calculateDifference(wins,winsDb));
                                        ps3.setInt(3,accId);
                                        ps3.setInt(4,tankId);
                                        ps3.executeUpdate();
                                    }
                                }
                            }

                            remainingBattles-=Math.min(remainingBattles,battles2);
                            if(remainingBattles<=0){
                                break;
                            }
                        }
                    }
                }
            }else{
                new DeleteData().freeupThousandBattles(accId);
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     * @param wotbId
    */
    protected void updatePlayerNickname(int wotbId){
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select nickname,realm from user_data where wotb_id=?")){
            ps.setInt(1,wotbId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    JsonElement je=new GsonBuilder().create().toJsonTree(JsonParser.parseString(getData(UtilityClass.getRealm(rs.getString("realm"))+"/wotb/account/info/?application_id="+UtilityClass.APP_ID+"&fields=nickname&account_id="+wotbId)));
                    JsonObject data=je.getAsJsonObject().getAsJsonObject("data");
                    if(!data.isEmpty()){
                        JsonObject playerData=data.getAsJsonObject(UtilityClass.getJsonKeyName(data.keySet()));
                        String apiNickname=playerData.get("nickname").getAsString();
                        if(!rs.getString("nickname").equals(apiNickname)){
                            new UpdateData().updateNickname(apiNickname,wotbId);
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
     * @param clanId
     */
    protected void updateClantag(int clanId){
        try(Connection cn=DbConnection.getConnection();
        PreparedStatement ps=cn.prepareStatement("select clantag,realm from clan_data where clan_id=?")){
            ps.setInt(1,clanId);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    String realm=rs.getString("realm");
                    JsonElement je=new GsonBuilder().create().toJsonTree(JsonParser.parseString(getData(UtilityClass.getRealm(realm)+"/wotb/clans/info/?application_id="+UtilityClass.APP_ID+"&clan_id="+clanId+"&fields=tag")));
                    JsonObject data=je.getAsJsonObject().getAsJsonObject("data");
                    if(!data.isEmpty()){
                        JsonObject clanData=data.getAsJsonObject(UtilityClass.getJsonKeyName(data.keySet()));
                        String apiClantag=clanData.get("tag").getAsString();
                        if(!rs.getString("clantag").equals(apiClantag)){
                            new UpdateData().updateClantag(apiClantag,clanId,realm);
                        }
                    }
                }
            }
        }catch(SQLException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
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
            String line="";
            if(u2.getResponseCode()==HttpURLConnection.HTTP_OK){
                try(InputStream is=u2.getInputStream();
                InputStreamReader isr=new InputStreamReader(is,StandardCharsets.UTF_8);
                BufferedReader br=new BufferedReader(isr)){
                    for(String line2;(line2=br.readLine())!=null;){
                        line=line2;
                    }
                }
            }
            return line;
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