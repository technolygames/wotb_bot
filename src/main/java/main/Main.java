package main;

import dbconnection.GetData;
import dbconnection.UpdateData;
import logic.BotActions;
import logic.BotLogic;
import logic.JsonHandler;

/**
 * Main method
 * 
 * @author erick
 */
public class Main{
    protected Main(){}

    public static void main(String[] args){
        //System.out.println(BotActions.getTeamWinrate("NEXO", "NA"));
        //JsonHandler.dataManipulation(1026412385);
        //JsonHandler.updatePlayerNickname(1026412385);
        new BotLogic();
        //System.out.println(BotActions.getTeamWinrate("-EXE-","NA"));
        //JsonHandler.getTankInfo();
        //UpdateData.updateTierTank();
        //System.out.println(GetData.getTankStats(1018737583).getTankTier());
        //BotActions.dataManipulation(1018737583);
        //System.out.println(GetData.getTierTenWinRate(1018737583));
        //JsonHandler.getAccTankData(1030271250);
        //System.out.println(GetData.getTierTenWinRate(1030271250));
        //System.out.println(TournamentWinRate.getTournamentWinRate());
    }
}