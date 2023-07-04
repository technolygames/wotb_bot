/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main;

import java.util.ArrayList;
import java.util.List;

import logic.BotLogic;
import mvc.Mvc2;

/**
 *
 * @author erick
 */
public class Main{
    public static void main(String[] args){
        List<Mvc2> data1=new ArrayList<>();
        List<Mvc2> data2=new ArrayList<>();
        data1.add(new BotLogic().retrieveFromApi());
        data2.add(new BotLogic().retrieveFromDatabase());
        

        new BotLogic().dataManipulation(data1,data2);

        /*new ApiRequest().test("https://api.wotblitz.com/wotb/account/list/?application_id=fd14e112652ef853caa088328cd5a67d&search=OaxacoGameplays");
        
        var data=new BotLogic().retrieveFromApi();

        System.out.println(data.getTankId()+" battles: "+data.getBattles()+" wins: "+data.getWins()+" losses: "+data.getLosses());

        Mvc1 datos=new Mvc1();
        datos.setDiscordId("336722242887090178");
        datos.setWotbId(1018737583);
        datos.setWotbName("OaxacoGameplays");

        new DbConnection().setUserData(datos);

        var id=new DbConnection().getUserData(String.valueOf(336722242887090178l));

        System.out.println(id.getWotbId()+" "+id.getWotbName()+" es de la base de datos "+Calendar.HOUR);*/
    }
}