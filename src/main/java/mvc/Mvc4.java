package mvc;

public class Mvc4{
    private int wotbId;
    private int tierTank;
    private int battles;
    private int wins;
    private double winrate;

    public int getWotbId(){
        return wotbId;
    }

    public void setWotbId(int wotbId){
        this.wotbId=wotbId;
    }

    public int getTierTank(){
        return tierTank;
    }

    public void setTierTank(int tierTank){
        this.tierTank=tierTank;
    }

    public int getBattles(){
        return battles;
    }

    public void setBattles(int battles){
        this.battles=battles;
    }

    public int getWins(){
        return wins;
    }

    public void setWins(int wins){
        this.wins=wins;
    }

    public double getWinrate(){
        return winrate;
    }

    public void setWinrate(double winrate){
        this.winrate=winrate;
    }
}