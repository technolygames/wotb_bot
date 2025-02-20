package mvc;

/**
 * @author erick
 */
public class Mvc2{
    private int clanId;
    private String clantag;
    private String realm;

    public int getClanId(){
        return clanId;
    }

    public void setClanId(int clanId){
        this.clanId=clanId;
    }

    public String getClantag(){
        return clantag;
    }

    public void setClantag(String clantag){
        this.clantag=clantag;
    }

    public String getRealm(){
        return realm;
    }

    public void setRealm(String realm){
        this.realm=realm;
    }

    @Override
    public String toString(){
        return "Mvc2{"+"clanId="+clanId+", clantag="+clantag+", realm="+realm+'}';
    }
}