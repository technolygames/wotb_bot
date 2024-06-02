package mvc;

/**
 *
 * @author erick
 */
public class Mvc1{
    private int wotbId;
    private int tankId;
    private int tankTier;
    private int battles;
    private int wins;
    private int losses;

    public int getWotbId(){
        return wotbId;
    }

    public void setWotbId(int wotbId){
        this.wotbId=wotbId;
    }

    public int getTankId(){
        return tankId;
    }

    public void setTankId(int tankId){
        this.tankId=tankId;
    }

    public int getTankTier(){
        return tankTier;
    }

    public void setTankTier(int tankTier){
        this.tankTier=tankTier;
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

    @Override
    public String toString(){
        return "Mvc2 [playerId="+wotbId+", tankId="+tankId+", tank_tier="+tankTier+", battles="+battles+", wins="+wins+", losses="+losses+"]";
    }
}