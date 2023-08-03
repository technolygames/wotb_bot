package mvc;

public class Mvc5{
    private int tankId;
    private String tankName;
    private String nation;
    private int tankTier;

    public int getTankId(){
        return tankId;
    }

    public void setTankId(int tankId){
        this.tankId=tankId;
    }

    public String getTankName(){
        return tankName;
    }

    public void setTankName(String tankName){
        this.tankName=tankName;
    }

    public String getNation(){
        return nation;
    }

    public void setNation(String nation){
        this.nation=nation;
    }

    public int getTankTier(){
        return tankTier;
    }

    public void setTankTier(int tankTier){
        this.tankTier=tankTier;
    }
}