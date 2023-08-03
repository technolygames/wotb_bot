package logic;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import dbconnection.GetData;
import dbconnection.InsertData;
import dbconnection.UpdateData;
import mvc.Mvc1;
import mvc.Mvc2;
import mvc.Mvc3;

/**
 *
 * @author erick
 */
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

    public static double getWinRateValue(String json){
        double wins=JsonHandler.getStatData(json).get("wins").getAsInt()+.0;
        double battles=JsonHandler.getStatData(json).get("battles").getAsInt()+.0;

        return Double.parseDouble(new DecimalFormat("##.##").format((wins/battles)*100));
    }

    public void registerPlayer(int wotbId,String nickname){
        if(!GetData.existUser(wotbId)&&!GetData.existTankRegister(wotbId)){
            var apiRequest=new ApiRequest();
            JsonObject json=JsonHandler.getAccountData(apiRequest.getNickname(nickname));
            JsonObject json2=JsonHandler.getTankStats(apiRequest.getTankData(json.get("account_id").getAsInt(),20257));

            Mvc1 mvc=new Mvc1();
            Mvc2 mvc2=new Mvc2();

            mvc.setDiscordId("336722242887090178");
            mvc.setWotbId(json.get("account_id").getAsInt());
            mvc.setWotbName(json.get("nickname").getAsString());

            mvc2.setPlayerId(mvc.getWotbId());
            mvc2.setTankId(20257);
            mvc2.setBattles(json2.get("battles").getAsInt());
            mvc2.setWins(json2.get("wins").getAsInt());
            mvc2.setLosses(json2.get("losses").getAsInt());

            InsertData.setUserData(mvc);
            InsertData.setTankStats(mvc2);

            System.out.println("desde ahora, se registrarán los datos");
        }
    }

    public void setTeamPlayers(){
        
    }

    public void dataManipulation(List<Mvc2> dataFromApi,List<Mvc2> dataFromDatabase){
        Mvc3 data=new Mvc3();
        boolean found=false;

        for(Mvc2 dataPoint1:dataFromApi){
            int value1=dataPoint1.getBattles();
            int value2=dataPoint1.getWins();
            for(Mvc2 dataPoint2:dataFromDatabase){
                if(dataPoint2.getBattles()!=value1){
                    found=true;
                    if(value2>=dataPoint2.getWins()&&dataPoint1.getLosses()>=dataPoint2.getLosses()){
                        data.setTankId(dataFromApi.get(0).getTankId());
                        data.setBattleDifference(calculateDifference(value1,dataPoint2.getBattles()));
                        data.setWinDifference(calculateDifference(value2,dataPoint2.getWins()));
                        data.setLossDifference(calculateDifference(dataPoint1.getLosses(),dataPoint2.getLosses()));
                        System.out.println("se detectaron cambios");
                    }
                    break;
                }
            }
            if(found){
                UpdateData.updateData(data);
            }
        }
    }

    private List<Mvc2> filterData(List<Mvc2> data){
        List<Mvc2> filteredData=new ArrayList<>();
        int count=1300;
        for(Mvc2 dataPoint:data){
            int battles=dataPoint.getBattles();
            int tier=dataPoint.getTier();

            if(count<battles){
                if(tier>=8&&tier<=10){
                    filteredData.add(dataPoint);
                    count++;
                }
            }else{
                break;
            }
        }
        return filteredData;
    }

    public int calculateDifference(int fromApi,int fromDatabase){
        return fromApi-fromDatabase;
    }

    public Mvc2 retrieveFromApi(int tankId,String nickname){
        Mvc2 data=new Mvc2();
        var id=JsonHandler.getAccountData(new ApiRequest().getNickname(nickname)).get("account_id").getAsInt();
        
        var stats=JsonHandler.getTankStats(new ApiRequest().getTankData(id,tankId));

        data.setPlayerId(id);
        data.setTankId(tankId);
        data.setBattles(stats.get("battles").getAsInt());
        data.setWins(stats.get("wins").getAsInt());
        data.setLosses(stats.get("losses").getAsInt());

        return data;
    }

    public Mvc2 retrieveFromDatabase(int tankId){
        var dbData=GetData.getTankStats(tankId);
        Mvc2 data=new Mvc2();

        data.setPlayerId(dbData.getPlayerId());
        data.setTankId(dbData.getTankId());
        data.setTier(dbData.getTier());
        data.setBattles(dbData.getBattles());
        data.setWins(dbData.getWins());
        data.setLosses(dbData.getLosses());

        return data;
    }
}