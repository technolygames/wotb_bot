package mvc;

/**
 *
 * @author erick
 */
public class Mvc1{
    String discordId;
    int wotbId;
    String wotbName;

    public String getDiscordId(){
        return discordId;
    }

    public void setDiscordId(String discordId){
        this.discordId=discordId;
    }

    public int getWotbId(){
        return wotbId;
    }

    public void setWotbId(int wotbId){
        this.wotbId=wotbId;
    }

    public String getWotbName(){
        return wotbName;
    }

    public void setWotbName(String wotbName){
        this.wotbName=wotbName;
    }

    @Override
    public String toString(){
        return "Mvc1 [discordId=" + discordId + ", wotbId=" + wotbId + ", wotbName=" + wotbName + "]";
    }
}