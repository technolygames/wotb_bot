package mvc;

/**
 * @author erick
 */
public class Mvc1{
    private String nickname;
    private int acoountId;
    private long lastBattleTime;
    private long updatedAt;

    public String getNickname(){
        return nickname;
    }

    public void setNickname(String nickname){
        this.nickname=nickname;
    }

    public int getAcoountId(){
        return acoountId;
    }

    public void setAcoountId(int acoountId){
        this.acoountId=acoountId;
    }

    public long getLastBattleTime(){
        return lastBattleTime;
    }

    public void setLastBattleTime(long lastBattleTime){
        this.lastBattleTime=lastBattleTime;
    }

    public long getUpdatedAt(){
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt){
        this.updatedAt=updatedAt;
    }
}