package interfaces;

/**
 * @author erick
 */
public class Interfaces{
    public record ClanData(String clantag,String realm,long updatedAt){}
    
    public record ClanData2(int clanId,String clantag,String realm,long updatedAt){
        @Override
        public String toString(){
            return "Mvc2={\n\tclanId="+clanId+",\n\tclantag="+clantag+",\n\trealm="+realm+"\n}";
        }
    }
    
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
    
    public record RealmSchedule(int id,String realmName,long start,long end){}
    public record TankStats(int battles,int wins){}
    public record TeamInfo(String teamId,int clanId){}
    public record TeamProfile(double winrate,String roster){}
    public record TourneyInfo(String string,long regStart,long regEnd,int teams,long startAt,long endAt){}
    public record TourneyValues(int teamsPerGroup,int maxGroups,int requiredBattles,String seedType){}
    public record TourneyTeamInfo(int clanId,String teamName){}
    public record UserData(String nickname,String realm,long lastBattleTime,long updatedAt,int tankStatsBattles,int thousandBattlesBattles){}
    public record UserData2(long accId,String nickname,long lastBattleTime,long updatedAt){}
    public record UserData3(String nickname,long lastBattleTime,long updatedAt){}
}