package mvc;

/**
 *
 * @author erick
 */
public class Mvc2{
    private int wotbId;
    private int tankId;
    private int battleDifference;
    private int winDifference;
    private int lossDifference;

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

    public int getBattleDifference(){
        return battleDifference;
    }

    public void setBattleDifference(int battleDifference){
        this.battleDifference=battleDifference;
    }

    public int getWinDifference(){
        return winDifference;
    }

    public void setWinDifference(int winDifference){
        this.winDifference=winDifference;
    }

    public int getLossDifference(){
        return lossDifference;
    }

    public void setLossDifference(int lossDifference){
        this.lossDifference=lossDifference;
    }
    
    @Override
    public String toString(){
        return "Mvc3 [accountId="+wotbId+", tankId="+tankId+", battleDifference="+battleDifference+", winDifference="+winDifference+", lossDifference="+lossDifference+"]";
    }
}