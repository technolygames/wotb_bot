package interfaces;

import java.time.ZoneId;

/**
 * @author erick
 */
public class Interfaces{
    public record TankStats(int battles,int wins){}
    public record RealmSchedule(String realmName,long start,long end,ZoneId timeZone){}
    public record TeamInfo(String teamId,int clanId){}
    public record UserData(String nickname,long lastBattleTime,long updatedAt,int tankStatsBattles,int thousandBattlesBattles){}
    public record ClanData(String clantag,String realm,long updatedAt){}
    public record TourneyInfo(String string,long regStart,long regEnd,int teams){}

    public record LeaderboardEntry(String teamName,String clantag,double winrate){
        @Override
        public String toString(){
            boolean hasClantag=clantag!=null&&!clantag.isEmpty();
            boolean hasTeamName=teamName!=null&&!teamName.isEmpty();
            String name;

            if(hasTeamName&&hasClantag){
                name=String.format("[%s] %s",clantag,teamName);
            }else if(hasTeamName){
                name=teamName;
            }else{
                name=String.format("[%s]",clantag);
            }
            
            return String.format("%s - %d%%",name,Math.round(winrate));
        }
    }

    public record Player(String nickname,double winrate){
        @Override
        public String toString(){
            return String.format("%s - %.2f%%",nickname,winrate);
        }
    }
}