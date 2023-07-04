package mvc;

public class Mvc2{
    private int tankId;
    private int battles;
    private int wins;
    private int losses;

    public int getTankId(){
        return tankId;
    }

    public void setTankId(int tankId){
        this.tankId=tankId;
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

    public int getLosses(){
        return losses;
    }

    public void setLosses(int losses){
        this.losses=losses;
    }
}