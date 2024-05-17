package mvc;

/**
 *
 * @author erick
 */
public class Mvc3{
    private int accountId;
    private int tankId;
    private int battleDifference;
    private int winDifference;
    private int lossDifference;

    public int getAccountId(){
        return accountId;
    }

    public void setAccountId(int accountId){
        this.accountId=accountId;
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
        return "Mvc3 [accountId="+accountId+", tankId="+tankId+", battleDifference="+battleDifference+", winDifference="+winDifference+", lossDifference="+lossDifference+"]";
    }
}