package logic;

import mvc.Mvc1;
import mvc.Mvc2;
import dbconnection.GetData;
import dbconnection.UpdateData;
import dbconnection.DbConnection;
import dbconnection.DeleteData;

import java.util.Map;
import java.util.List;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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
        Mvc1 model=new Mvc1();
        WebControl wc=new WebControl();
        UtilityClass uc=new UtilityClass();
        try{
            String api=uc.getRealm(realm);
            if(wc.checkConnection(api)){
                JsonElement je=new GsonBuilder().create().toJsonTree(JsonParser.parseString(wc.getData(api+"/wotb/account/list/?application_id="+UtilityClass.APP_ID+"&search="+nickname)));
                JsonArray data=je.getAsJsonObject().getAsJsonArray("data");
                for(int i=0;i<data.size();i++){
                    JsonObject val=data.get(i).getAsJsonObject();
                    String val2=val.get("nickname").getAsString();
                    if(val2.equals(nickname)){
                        model.setNickname(val2);
                        model.setAcoountId(val.get("account_id").getAsInt());
                    }
                }
            }
        }catch(IllegalStateException|IndexOutOfBoundsException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return model;
    }

    /**
     * @param accId
     * @param realm
     * @return
     */
    public Mvc1 getAccountData(int accId,String realm){
        Mvc1 model=new Mvc1();
        WebControl wc=new WebControl();
        UtilityClass uc=new UtilityClass();
        try{
            String api=uc.getRealm(realm);
            if(wc.checkConnection(api)){
                JsonElement je=new GsonBuilder().create().toJsonTree(JsonParser.parseString(wc.getData(api+"/wotb/account/info/?application_id="+UtilityClass.APP_ID+"&account_id="+accId+"&fields=nickname")));
                JsonObject data=je.getAsJsonObject().getAsJsonObject("data");
                for(String keyValues:data.keySet()){
                    JsonObject val=data.getAsJsonObject(keyValues);
                    model.setNickname(val.get("nickname").getAsString());
                    model.setAcoountId(accId);
                }
            }
        }catch(IllegalStateException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return model;
    }

    /**
     * @param clantag
     * @param realm
     * @return
     */
    public Mvc2 getClanData(String clantag,String realm){
        Mvc2 model=new Mvc2();
        WebControl wc=new WebControl();
        UtilityClass uc=new UtilityClass();
        try{
            String api=uc.getRealm(realm);
            if(wc.checkConnection(api)){
                JsonElement je=new GsonBuilder().create().toJsonTree(JsonParser.parseString(wc.getData(api+"/wotb/clans/list/?application_id=fd14e112652ef853caa088328cd5a67d&search="+clantag+"&fields=clan_id%2Ctag")));
                JsonArray data=je.getAsJsonObject().getAsJsonArray("data");
                for(int i=0;i<data.size();i++){
                    JsonObject val=data.get(i).getAsJsonObject();
                    String apiClantag=val.get("tag").getAsString();
                    if(apiClantag.equals(clantag)){
                        model.setClanId(val.get("clan_id").getAsInt());
                        model.setClantag(apiClantag);
                        model.setRealm(realm);
                    }
                }
            }
        }catch(IllegalStateException|IndexOutOfBoundsException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return model;
    }

    /**
     * Gets tank info from the requesting account.
     * @param accId
     */
    public void getAccTankData(int accId){
        WebControl wc=new WebControl();
        GetData gd=new GetData();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("insert into tank_stats values(?,?,?,?)");
                PreparedStatement ps2=cn.prepareStatement("select tank_id from tank_stats where wotb_id=?")){
            String api=gd.getRealm(accId);
            if(wc.checkConnection(api)){
                JsonElement je=new GsonBuilder().create().toJsonTree(JsonParser.parseString(wc.getData(api+"/wotb/tanks/stats/?application_id="+UtilityClass.APP_ID+"&account_id="+accId+"&tank_id="+gd.getTierTenTankList()+"&fields=tank_id%2Call.battles%2Call.wins")));
                JsonObject data=je.getAsJsonObject().getAsJsonObject("data");
                if(!data.isEmpty()){
                    for(String keyValues:data.keySet()){
                        for(JsonElement val2:data.getAsJsonArray(keyValues)){
                            JsonObject val3=val2.getAsJsonObject();
                            JsonObject val4=val3.getAsJsonObject("all");

                            ps2.setInt(1,accId);
                            try(ResultSet rs=ps2.executeQuery()){
                                if(!rs.next()){
                                    int battles=val4.get("battles").getAsInt();
                                    int wins=val4.get("wins").getAsInt();
                                    if(battles!=0&&wins!=0){
                                        ps.setInt(1,accId);
                                        ps.setInt(2,val3.get("tank_id").getAsInt());
                                        ps.setInt(3,battles);
                                        ps.setInt(4,wins);
                                        ps.executeUpdate();
                                    }
                                }
                            }
                        }
                    }
                    dataValidation(accId);
                }
            }
        }catch(SQLException|IllegalStateException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     * @param accId
     */
    protected void dataValidation(int accId){
        WebControl wc=new WebControl();
        GetData gd=new GetData();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select sum(battles) as battles from tank_stats where wotb_id=?");
                PreparedStatement ps2=cn.prepareStatement("insert into thousand_battles values(?,?,?,?)");
                PreparedStatement ps3=cn.prepareStatement("select tank_id from thousand_battles where wotb_id=? and tank_id=?")){
            int battles=0;

            ps.setInt(1,accId);
            try(ResultSet rs2=ps.executeQuery()){
                if(rs2.next()){
                    battles=rs2.getInt("battles");
                }
            }

            if(battles<UtilityClass.MAX_BATTLE_COUNT){
                for(List<String> tankIdList:gd.getTankLists()){
                    String api=gd.getRealm(accId);
                    if(wc.checkConnection(api)){
                        JsonElement je=new GsonBuilder().create().toJsonTree(JsonParser.parseString(wc.getData(api+"/wotb/tanks/stats/?application_id="+UtilityClass.APP_ID+"&account_id="+accId+"&fields=tank_id%2Call.battles%2Call.wins&tank_id="+String.join(",",tankIdList))));
                        JsonObject data=je.getAsJsonObject().getAsJsonObject("data");
                        if(!data.isEmpty()){
                            for(String keyValues:data.keySet()){
                                for(JsonElement val2:data.getAsJsonArray(keyValues)){
                                    JsonObject val3=val2.getAsJsonObject();
                                    JsonObject val4=val3.getAsJsonObject("all");

                                    int tankId=val3.get("tank_id").getAsInt();

                                    ps3.setInt(1,accId);
                                    ps3.setInt(2,tankId);
                                    try(ResultSet rs3=ps3.executeQuery()){
                                        if(!rs3.next()){
                                            int battles2=val4.get("battles").getAsInt();
                                            int wins=val4.get("wins").getAsInt();
                                            if(battles!=0&&wins!=0){
                                                ps2.setInt(1,accId);
                                                ps2.setInt(2,tankId);
                                                ps2.setInt(3,battles2);
                                                ps2.setInt(4,wins);
                                                ps2.executeUpdate();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
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
        WebControl wc=new WebControl();
        GetData gd=new GetData();
        UtilityClass uc=new UtilityClass();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select tank_id,battles,wins from tank_stats where wotb_id=?");
                PreparedStatement ps2=cn.prepareStatement("update tank_stats set battles=battles+?, wins=wins+? where wotb_id=? and tank_id=?");
                PreparedStatement ps3=cn.prepareStatement("insert into tank_stats values(?,?,?,?)");
                PreparedStatement ps4=cn.prepareStatement("select tank_id from tank_stats where wotb_id=? and tank_id=?")){
            String api=gd.getRealm(accId);
            if(wc.checkConnection(api)){
                JsonElement je=new GsonBuilder().create().toJsonTree(JsonParser.parseString(wc.getData(api+"/wotb/tanks/stats/?application_id="+UtilityClass.APP_ID+"&fields=tank_id%2Call.battles%2Call.wins&tank_id="+gd.getTierTenTankList()+"&account_id="+accId)));
                JsonObject data=je.getAsJsonObject().getAsJsonObject("data");
                if(!data.isEmpty()){
                    for(String keyValues:data.keySet()){
                        for(JsonElement val2:data.getAsJsonArray(keyValues)){
                            JsonObject val3=val2.getAsJsonObject();
                            JsonObject val4=val3.getAsJsonObject("all");

                            int apiBattles=val4.get("battles").getAsInt();
                            int apiWins=val4.get("wins").getAsInt();
                            int apiTankId=val3.get("tank_id").getAsInt();

                            if(apiBattles!=0&&apiWins!=0){
                                ps4.setInt(1,accId);
                                ps4.setInt(2,apiTankId);
                                try(ResultSet rs=ps4.executeQuery()){
                                    if(!rs.next()){
                                        uc.log(Level.INFO,accId+" tiene tanque nuevo: "+apiTankId);
                                        ps3.setInt(1,accId);
                                        ps3.setInt(2,apiTankId);
                                        ps3.setInt(3,apiBattles);
                                        ps3.setInt(4,apiWins);
                                        ps3.executeUpdate();
                                    }
                                }
                            }

                            ps.setInt(1,accId);
                            try(ResultSet rs2=ps.executeQuery()){
                                while(rs2.next()){
                                    int dbBattles=rs2.getInt("battles");
                                    int dbWins=rs2.getInt("wins");
                                    int dbTankId=rs2.getInt("tank_id");
                                    if(dbTankId==apiTankId&&(apiBattles!=dbBattles||apiWins!=dbWins)){
                                        uc.log(Level.INFO,"si hay cambios de "+accId+", del tanque "+apiTankId);
                                        ps2.setInt(1,UtilityClass.calculateDifference(apiBattles,dbBattles));
                                        ps2.setInt(2,UtilityClass.calculateDifference(apiWins,dbWins));
                                        ps2.setInt(3,accId);
                                        ps2.setInt(4,apiTankId);
                                        ps2.executeUpdate();
                                    }
                                }
                            }
                        }
                    }
                    thousandBattlesDataManipulation(accId);
                }
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     * @param accId
     */
    protected void thousandBattlesDataManipulation(int accId){
        WebControl wc=new WebControl();
        GetData gd=new GetData();
        UtilityClass uc=new UtilityClass();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("insert into thousand_battles values(?,?,?,?)");
                PreparedStatement ps2=cn.prepareStatement("select tank_id from thousand_battles where wotb_id=? and tank_id=?");
                PreparedStatement ps3=cn.prepareStatement("update thousand_battles set battles=battles+?, wins=wins+? where wotb_id=? and tank_id=?");
                PreparedStatement ps4=cn.prepareStatement("select tank_id,battles,wins from thousand_battles where wotb_id=?");
                PreparedStatement ps5=cn.prepareStatement("select sum(battles) as battles from tank_stats where wotb_id=?")){
            int battles=0;
            
            ps5.setInt(1,accId);
            try(ResultSet rs2=ps5.executeQuery()){
                if(rs2.next()){
                    battles=rs2.getInt("battles");
                }
            }
            
            if(battles<UtilityClass.MAX_BATTLE_COUNT){
                for(List<String> tankIdList:gd.getTankLists()){
                    String api=gd.getRealm(accId);
                    if(wc.checkConnection(api)){
                        JsonElement je=new GsonBuilder().create().toJsonTree(JsonParser.parseString(wc.getData(api+"/wotb/tanks/stats/?application_id="+UtilityClass.APP_ID+"&account_id="+accId+"&fields=tank_id%2Call.battles%2Call.wins&tank_id="+String.join(",",tankIdList))));
                        JsonObject data=je.getAsJsonObject().getAsJsonObject("data");
                        if(!data.isEmpty()){
                            for(String keyValues:data.keySet()){
                                for(JsonElement val2:data.getAsJsonArray(keyValues)){
                                    JsonObject val3=val2.getAsJsonObject();
                                    JsonObject val4=val3.getAsJsonObject("all");

                                    int apiBattles=val4.get("battles").getAsInt();
                                    int apiWins=val4.get("wins").getAsInt();
                                    int apiTankId=val3.get("tank_id").getAsInt();

                                    if(apiBattles!=0&&apiWins!=0){
                                        ps2.setInt(1,accId);
                                        ps2.setInt(2,apiTankId);
                                        try(ResultSet rs=ps2.executeQuery()){
                                            if(!rs.next()){
                                                uc.log(Level.INFO,accId+" tiene tanque nuevo: "+apiTankId);
                                                ps.setInt(1,accId);
                                                ps.setInt(2,apiTankId);
                                                ps.setInt(3,apiBattles);
                                                ps.setInt(4,apiWins);
                                                ps.executeUpdate();
                                            }   
                                        }
                                    }

                                    ps4.setInt(1,accId);
                                    try(ResultSet rs2=ps4.executeQuery()){
                                        while(rs2.next()){
                                            int dbBattles=rs2.getInt("battles");
                                            int dbWins=rs2.getInt("wins");
                                            int dbTankId=rs2.getInt("tank_id");

                                            if(dbTankId==apiTankId&&(apiBattles!=dbBattles||apiWins!=dbWins)){
                                                uc.log(Level.INFO,"si hay cambios de "+accId+", del tanque "+apiTankId);
                                                ps3.setInt(1,UtilityClass.calculateDifference(apiBattles,dbBattles));
                                                ps3.setInt(2,UtilityClass.calculateDifference(apiWins,dbWins));
                                                ps3.setInt(3,accId);
                                                ps3.setInt(4,apiTankId);
                                                ps3.executeUpdate();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }else{
                new DeleteData().freeupThousandBattles(accId);
            }
        }catch(SQLException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
    */
    protected void updatePlayerNickname(){
        WebControl wc=new WebControl();
        UtilityClass uc=new UtilityClass();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select nickname from user_data where wotb_id=?")){
            for(Map.Entry<String,List<String>> entry:new GetData().getPlayersLists().entrySet()){
                List<String> list=entry.getValue();
                if(!list.isEmpty()){
                    String api=uc.getRealm(entry.getKey());
                    if(wc.checkConnection(api)){
                        JsonElement je=new GsonBuilder().create().toJsonTree(JsonParser.parseString(wc.getData(api+"/wotb/account/info/?application_id="+UtilityClass.APP_ID+"&fields=nickname&account_id="+String.join(",",list))));
                        JsonObject data=je.getAsJsonObject().getAsJsonObject("data");
                        if(data!=null&&!data.isEmpty()){
                            for(String keyValue:data.keySet()){
                                int user=Integer.parseInt(keyValue);
                                ps.setInt(1,user);
                                try(ResultSet rs=ps.executeQuery()){
                                    if(rs.next()){
                                        JsonObject playerData=data.getAsJsonObject(keyValue);
                                        String apiNickname=playerData.get("nickname").getAsString();
                                        if(!rs.getString("nickname").equals(apiNickname)){
                                            new UpdateData().updateNickname(apiNickname,user);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }catch(SQLException|IllegalStateException|NullPointerException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     */
    protected void updateClantag(){
        WebControl wc=new WebControl();
        UtilityClass uc=new UtilityClass();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select clantag,realm from clan_data where clan_id=?")){
            for(Map.Entry<String,List<String>> entry:new GetData().getClanLists().entrySet()){
                List<String> list=entry.getValue();
                if(!list.isEmpty()){
                    String api=uc.getRealm(entry.getKey());
                    if(wc.checkConnection(api)){
                        JsonElement je=new GsonBuilder().create().toJsonTree(JsonParser.parseString(wc.getData(api+"/wotb/clans/info/?application_id="+UtilityClass.APP_ID+"&clan_id="+String.join(",",list)+"&fields=tag")));
                        JsonObject data=je.getAsJsonObject().getAsJsonObject("data");
                        if(data!=null&&!data.isEmpty()){
                            for(String keyValue:data.keySet()){
                                int clanId=Integer.parseInt(keyValue);
                                ps.setInt(1,clanId);
                                try(ResultSet rs=ps.executeQuery()){
                                    if(rs.next()){
                                        JsonObject clanData=data.getAsJsonObject(keyValue);
                                        String apiClantag=clanData.get("tag").getAsString();
                                        if(!rs.getString("clantag").equals(apiClantag)){
                                            new UpdateData().updateClantag(apiClantag,clanId,rs.getString("realm"));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }catch(SQLException|IllegalStateException|NullPointerException e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }
}