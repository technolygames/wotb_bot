package main;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import dbconnection.DbConnection;
import dbconnection.GetData;
import dbconnection.InsertData;
import logic.ApiRequest;
import logic.BotLogic;
import logic.JsonHandler;
import logic.UtilityClass;
import mvc.Mvc1;
import mvc.Mvc2;

/**
 *
 * @author erick
 */
public class Main{
    public static void main(String[] args) throws IOException,SQLException{
        List<Mvc2> data1=new ArrayList<>();
        List<Mvc2> data2=new ArrayList<>();

        var tankId=GetData.getTankList();
        while(tankId.next()){
            System.out.println(JsonHandler.getTankStats(new ApiRequest().getTankData(JsonHandler.getAccountData(new ApiRequest().getNickname("ArepaMan")).get("account_id").getAsInt(),tankId.getInt("tank_id"))));
            //System.out.println(new BotLogic().retrieveFromApi(tankId.getInt("tank_id"),"Kaposzta3"));
            //System.out.println(new BotLogic().retrieveFromDatabase(tankId.getInt("tank_id")));
        }
        //new BotLogic().dataManipulation(data1,data2);

        /*new BotLogic().registerPlayer(1026412385,"Kaposzta3");

        UtilityClass.saveData(new ApiRequest().getData("https://api.wotblitz.com/wotb/account/list/?application_id=fd14e112652ef853caa088328cd5a67d&search=Kaposzta3"),"test.json");
        
        var data=new BotLogic().retrieveFromApi(tankId);

        System.out.println("tank_id: "+data.getTankId()+", battles: "+data.getBattles()+", wins: "+data.getWins()+", losses: "+data.getLosses());

        Mvc1 datos=new Mvc1();
        datos.setDiscordId("336722242887090178");
        datos.setWotbId(1018737583);
        datos.setWotbName("OaxacoGameplays");

        new DbConnection().setUserData(datos);

        var id=new DbConnection().getUserData(String.valueOf(336722242887090178l));

        System.out.println(id.getWotbId()+" "+id.getWotbName()+" es de la base de datos "+Calendar.HOUR);*/
    }
}