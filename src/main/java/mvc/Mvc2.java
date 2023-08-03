package mvc;

/**
 *
 * @author erick
 */
public class Mvc2{
    private int playerId;
    private int tankId;
    private int tier;
    private int battles;
    private int wins;
    private int losses;

    public int getPlayerId(){
        return playerId;
    }

    public void setPlayerId(int playerId){
        this.playerId=playerId;
    }

    public int getTankId(){
        return tankId;
    }

    public void setTankId(int tankId){
        this.tankId=tankId;
    }

    public int getTier(){
        return tier;
    }

    public void setTier(int tier){
        this.tier=tier;
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