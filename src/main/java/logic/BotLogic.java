package logic;

import java.text.DecimalFormat;
import java.util.List;

import dbconnection.DbConnection;
import mvc.Mvc2;

public class BotLogic{
    public double getTournamentWinRate(){
        int t10=400;
        double w10=0.68;
        int t9=400;
        double w9=0.64;
        int t8=200;
        double w8=0.69;
        int t7=200;
        double w7=0.63;
        int mloss=100;
        double wloss=0;

        int max=1300;

        var val1=t10*w10;
        var val2=0.95*t9*w9;
        var val3=0.85*t8*w8;
        var val4=0.75*t7*w7;
        var val5=mloss*wloss;

        var result1=val1+val2+val3+val4+val5/max;

        return result1/10;
    }

    public double getWinRateValue(String dir1,String dir2){
        String acc=String.valueOf(JsonHandler.getAccountData(dir1).get("account_id").getAsInt());
        
        double wins=JsonHandler.getStatData(dir2,acc).get("wins").getAsInt()+.0;
        double battles=JsonHandler.getStatData(dir2,acc).get("battles").getAsInt()+.0;

        return Double.parseDouble(new DecimalFormat("##.##").format((wins/battles)*100));
    }

    public void dataManipulation(List<Mvc2> dataFromApi,List<Mvc2> dataFromDatabase){
        for(Mvc2 dataPoint1:dataFromApi){
            int value1=dataPoint1.getBattles();
            int value2=dataPoint1.getWins();

            boolean found=false;
            for(Mvc2 dataPoint2:dataFromDatabase){
                if(dataPoint2.getBattles()==value1){
                    found=true;
                    int oldValue=dataPoint2.getWins();
                    if(value2!=oldValue){
                        System.out.println("hay cambios");
                    }
                    break;
                }
            }
            if(!found){
                System.out.println("test found");
            }
        }
    }

    public Mvc2 retrieveFromApi(){
        Mvc2 data=new Mvc2();
        var id=JsonHandler.getAccountData(new ApiRequest().getNickname("OaxacoGameplays")).get("account_id").getAsInt();
        var tankId=20257;
        
        var stats=JsonHandler.getTankStats(new ApiRequest().getTankData(id,tankId),String.valueOf(id));

        data.setTankId(tankId);
        data.setBattles(stats.get("battles").getAsInt());
        data.setWins(stats.get("wins").getAsInt());
        data.setLosses(stats.get("losses").getAsInt());

        return data;
    }

    public Mvc2 retrieveFromDatabase(){
        var tankId=20257;
        var dbData=new DbConnection().getTankStats(tankId);
        Mvc2 data=new Mvc2();
        
        data.setTankId(dbData.getTankId());
        data.setBattles(dbData.getBattles());
        data.setWins(dbData.getWins());
        data.setLosses(dbData.getLosses());

        return data;
    }
}