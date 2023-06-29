package logic;

import java.text.DecimalFormat;

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
        String acc=String.valueOf(new JsonHandler().getAccountData(dir1).get("account_id").getAsInt());
        
        double wins=new JsonHandler().getStatData(dir2,acc).get("wins").getAsInt()+.0;
        double battles=new JsonHandler().getStatData(dir2,acc).get("battles").getAsInt()+.0;

        return Double.parseDouble(new DecimalFormat("##.##").format((wins/battles)*100));
    }
}
