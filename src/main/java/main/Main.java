/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main;

import logic.ApiRequest;

/**
 *
 * @author erick
 */
public class Main{
    public static void main(String[] args){
        new ApiRequest().test("https://api.wotblitz.com/wotb/account/list/?application_id=fd14e112652ef853caa088328cd5a67d&search=OaxacoGameplays");
        //System.out.println(new BotLogic().getTournamentWinRate());
        //System.out.println(new BotLogic().getWinRateValue("data/json/test-(12).json","data/json/test-(11).json"));
    }
}