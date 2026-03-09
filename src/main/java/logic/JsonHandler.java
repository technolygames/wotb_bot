package logic;

import dbconnection.GetData;
import dbconnection.UpdateData;
import dbconnection.DbConnection;
import dbconnection.DeleteData;
import dbconnection.InsertData;
import interfaces.Interfaces;

import java.io.Reader;
import java.io.FileReader;
import java.io.StringReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * @author erick
 */
public class JsonHandler{
    private final WebControl wc=new WebControl();
    private final UtilityClass uc=new UtilityClass();
    private final InsertData id=new InsertData();
    private final GetData gd=new GetData();
    private final UpdateData ud=new UpdateData();
    private final DeleteData dd=new DeleteData();

    /** 
     * @param nickname
     * @param realm
     * @return
    */
    public Interfaces.UserData2 getAccountData(String nickname,String realm){
        Interfaces.UserData2 model=null;
        String apiResponse=wc.getData(uc.getRealm(realm)+"/wotb/account/list/?application_id="+UtilityClass.APP_ID+"&search="+nickname+"&fields=account_id,nickname");
        if(apiResponse!=null&&!apiResponse.isEmpty()){
            try(Reader r=new StringReader(apiResponse)){
                JsonElement data=JsonParser.parseReader(r).getAsJsonObject().get("data");
                if(data!=null&&!data.isJsonNull()&&data.isJsonArray()){
                    for(JsonElement player:data.getAsJsonArray()){
                        JsonObject data2=player.getAsJsonObject();
                        long apiUserId=data2.get("account_id").getAsLong();
                        String apiNickname=data2.get("nickname").getAsString();
                        if(apiUserId!=0&&apiNickname.equals(nickname)){
                            model=getAccountData(apiUserId,realm);
                            break;
                        }
                    }
                }
            }catch(Exception e){
                uc.log(Level.SEVERE,e.getMessage(),e);
            }
        }
        return model;
    }

    /**
     * @param accId
     * @param realm
     * @return
     */
    public Interfaces.UserData2 getAccountData(long accId,String realm){
        Interfaces.UserData2 model=null;
        String apiResponse=wc.getData(uc.getRealm(realm)+"/wotb/account/info/?application_id="+UtilityClass.APP_ID+"&account_id="+accId+"&fields=nickname,last_battle_time,updated_at");
        if(apiResponse!=null&&!apiResponse.isEmpty()){
            try(Reader r=new StringReader(apiResponse)){
                JsonElement data=JsonParser.parseReader(r).getAsJsonObject().get("data");
                if(data!=null&&!data.isJsonNull()&&data.isJsonObject()){
                    JsonObject player=data.getAsJsonObject();
                    if(player!=null&&!player.isEmpty()){
                        for(String playerId:player.keySet()){
                            JsonElement val=player.get(playerId);
                            if(val!=null&&!val.isJsonNull()&&val.isJsonObject()){
                                JsonObject values=val.getAsJsonObject();
                                model=new Interfaces.UserData2(
                                        Long.parseLong(playerId),
                                        values.get("nickname").getAsString(),
                                        values.get("last_battle_time").getAsLong(),
                                        values.get("updated_at").getAsLong()
                                );
                            }
                        }
                    }
                }
            }catch(Exception e){
                uc.log(Level.SEVERE,e.getMessage(),e);
            }
        }
        return model;
    }

    /**
     * @param clantag
     * @param realm
     * @return
     */
    public Interfaces.ClanData2 getClanData(String clantag,String realm){
        Interfaces.ClanData2 model=null;
        String apiResponse=wc.getData(uc.getRealm(realm)+"/wotb/clans/list/?application_id="+UtilityClass.APP_ID+"&search="+clantag+"&fields=clan_id,tag");
        if(apiResponse!=null&&!apiResponse.isEmpty()){
            try(Reader r=new StringReader(apiResponse)){
                JsonElement data=JsonParser.parseReader(r).getAsJsonObject().get("data");
                if(data!=null&&!data.isJsonNull()&&data.isJsonArray()){
                    for(JsonElement clan:data.getAsJsonArray()){
                        JsonObject val=clan.getAsJsonObject();
                        int apiClanId=val.get("clan_id").getAsInt();
                        String apiClantag=val.get("tag").getAsString();
                        if(apiClanId!=0&&apiClantag.equals(clantag)){
                            model=getClanData(apiClanId,realm);
                            break;
                        }
                    }
                }
            }catch(Exception e){
                uc.log(Level.SEVERE,e.getMessage(),e);
            }
        }
        return model;
    }

    /**
     * @param clanId
     * @param realm
     * @return
     */
    protected Interfaces.ClanData2 getClanData(int clanId,String realm){
        Interfaces.ClanData2 model=null;
        String apiResponse=wc.getData(uc.getRealm(realm)+"/wotb/clans/info/?application_id="+UtilityClass.APP_ID+"&clan_id="+clanId+"&fields=tag,updated_at");
        if(apiResponse!=null&&!apiResponse.isEmpty()){
            try(Reader r=new StringReader(apiResponse)){
                JsonElement data=JsonParser.parseReader(r).getAsJsonObject().get("data");
                if(data!=null&&!data.isJsonNull()&&data.isJsonObject()){
                    JsonObject clan=data.getAsJsonObject();
                    if(clan!=null&&!clan.isEmpty()){
                        for(String apiClanId:clan.keySet()){
                            JsonElement val=clan.get(apiClanId);
                            if(val!=null&&!val.isJsonNull()&&val.isJsonObject()){
                                JsonObject values=val.getAsJsonObject();
                                model=new Interfaces.ClanData2(
                                        Integer.parseInt(apiClanId),
                                        values.get("tag").getAsString(),
                                        realm,
                                        values.get("updated_at").getAsLong()
                                );
                            }
                        }
                    }
                }
            }catch(Exception e){
                uc.log(Level.SEVERE,e.getMessage(),e);
            }
        }
        return model;
    }

    /**
     */
    public void getTournamentData(){
        Map<String,List<String>> tournamentLists=new HashMap<>();
        tournamentLists.put("NA",new ArrayList<>());
        tournamentLists.put("EU",new ArrayList<>());
        tournamentLists.put("ASIA",new ArrayList<>());
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select tournament_id from tournament_data");
                PreparedStatement ps2=cn.prepareStatement("insert into tournament_data values(?,?,?,?,?,?,?,?,?)")){
            Set<Integer> tourneys=new HashSet<>();
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    tourneys.add(rs.getInt("tournament_id"));
                }
            }
            
            for(Map.Entry<String,List<String>> entry:tournamentLists.entrySet()){
                String realms=entry.getKey();
                List<String> lists=entry.getValue();
                String apiResponse=wc.getData(uc.getRealm(realms)+"/wotb/tournaments/list/?application_id="+UtilityClass.APP_ID+"&limit=25&fields=tournament_id");
                if(apiResponse!=null&&!apiResponse.isEmpty()){
                    try(Reader r=new StringReader(apiResponse)){
                        JsonElement data=JsonParser.parseReader(r).getAsJsonObject().get("data");
                        if(data!=null&&!data.isJsonNull()&&data.isJsonArray()){
                            for(JsonElement values2:data.getAsJsonArray()){
                                JsonObject val=values2.getAsJsonObject();
                                lists.add(val.get("tournament_id").getAsString());
                            }
                        }
                    }
                }
            }

            for(Map.Entry<String,List<String>> entry:tournamentLists.entrySet()){
                String realms=entry.getKey();
                List<String> lists=entry.getValue();
                if(!lists.isEmpty()){
                    String apiResponse=wc.getData(uc.getRealm(realms)+"/wotb/tournaments/info/?application_id="+UtilityClass.APP_ID+"&tournament_id="+String.join(",",lists)+"&fields=max_players_count,registration_start_at,registration_end_at,title,tournament_id,end_at,teams.confirmed,start_at,description");
                    if(apiResponse!=null&&!apiResponse.isEmpty()){
                        try(Reader r=new StringReader(apiResponse)){
                            JsonElement data=JsonParser.parseReader(r).getAsJsonObject().get("data");
                            if(data!=null&&!data.isJsonNull()&&data.isJsonObject()){
                                JsonObject tourney=data.getAsJsonObject();
                                if(tourney!=null&&!tourney.isEmpty()){
                                    for(String keys:tourney.keySet()){
                                        JsonElement v=tourney.get(keys);
                                        if(v!=null&&!v.isJsonNull()&&v.isJsonObject()){
                                            JsonObject values2=v.getAsJsonObject();
                                            JsonObject teamsObj=values2.getAsJsonObject("teams");

                                            JsonElement teamsConfirmed=teamsObj.get("confirmed");
                                            int confirmed=UtilityClass.tersin(teamsConfirmed);

                                            int tourneyId=values2.get("tournament_id").getAsInt();
                                            int maxPlayers=values2.get("max_players_count").getAsInt();
                                            if(maxPlayers==10){
                                                String title=values2.get("title").getAsString();
                                                List<String> titlesMatches=Arrays.asList("10vs10".toLowerCase(),"Professionals".toLowerCase(),"Challengers".toLowerCase(),"Lower".toLowerCase());
                                                String titleVal=title.toLowerCase();

                                                boolean forbidTitle=titlesMatches.stream().anyMatch(titleVal::contains);

                                                if(!forbidTitle){
                                                    String description=values2.get("description").getAsString();
                                                    if(!tourneys.contains(tourneyId)){
                                                        String seedingType=UtilityClass.parseSeedingType(description);
                                                        if(!seedingType.equalsIgnoreCase("unknown")){
                                                            ps2.setInt(1,tourneyId);
                                                            ps2.setString(2,title);
                                                            ps2.setString(3,seedingType);
                                                            ps2.setLong(4,values2.get("registration_start_at").getAsLong());
                                                            ps2.setLong(5,values2.get("registration_end_at").getAsLong());
                                                            ps2.setInt(6,confirmed);
                                                            ps2.setLong(7,values2.get("start_at").getAsLong());
                                                            ps2.setLong(8,values2.get("end_at").getAsLong());
                                                            ps2.setString(9,realms);
                                                            ps2.executeUpdate();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     */
    public void manipulateTeams(){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("SELECT team_id FROM ingame_team_data WHERE tournament_id=?");
                PreparedStatement ps2=cn.prepareStatement("delete from ingame_team where wotb_id=? and team_id=?");
                PreparedStatement ps3=cn.prepareStatement("insert ignore into ingame_team values(?,?)")){
            ZoneId sd=ZoneId.systemDefault();
            Map<Integer,List<Long>> dbMapTeam=gd.getIngameTeamInfo();
            for(Map.Entry<Integer,Interfaces.TourneyInfo> entry:gd.getTourneyFuncData().entrySet()){
                int tourneyId=entry.getKey();
                
                Map<Integer,List<Long>> apiMapTeam=new HashMap<>();
                
                Set<Integer> dbTeamIds=new HashSet<>();
                ps.setInt(1,tourneyId);
                try(ResultSet rs=ps.executeQuery()){
                    while(rs.next()){
                        dbTeamIds.add(rs.getInt("team_id"));
                    }
                }

                Interfaces.TourneyInfo td=entry.getValue();
                String realm=td.string();
                int teamsConfirmed=td.teams();

                ZonedDateTime systemTime=ZonedDateTime.now(sd);
                ZonedDateTime dbRegStart=Instant.ofEpochSecond(td.regStart()).atZone(sd);
                ZonedDateTime dbStartAt=Instant.ofEpochSecond(td.startAt()).atZone(sd);
                if(systemTime.isAfter(dbRegStart)&&systemTime.isBefore(dbStartAt)){
                    int totalPages=UtilityClass.calculateTotalPages(teamsConfirmed);
                    for(int pageNo=1;pageNo<=totalPages;pageNo++){
                        String apiResponse=wc.getData(uc.getRealm(realm)+"/wotb/tournaments/teams/?application_id="+UtilityClass.APP_ID+"&tournament_id="+tourneyId+"&fields=clan_id,team_id,title,players.account_id&status=confirmed&page_no="+pageNo+"&limit=100");
                        if(apiResponse!=null&&!apiResponse.isEmpty()){
                            try(Reader r=new StringReader(apiResponse)){
                                JsonElement data=JsonParser.parseReader(r).getAsJsonObject().get("data");
                                if(data!=null&&data.isJsonArray()){
                                    for(JsonElement team:data.getAsJsonArray()){
                                        JsonObject teamObj=team.getAsJsonObject();
                                        JsonElement playersElement=teamObj.get("players");
                                        JsonElement clanIdElement=teamObj.get("clan_id");

                                        int apiTeamId=teamObj.get("team_id").getAsInt();
                                        String apiTeamName=teamObj.get("title").getAsString();
                                        int apiClanId=UtilityClass.tersin(clanIdElement);

                                        if(apiClanId==0)continue;

                                        Interfaces.ClanData2 clanApiData=(!gd.checkClanData(apiClanId))?getClanData(apiClanId,realm):null;

                                        if(clanApiData!=null&&apiClanId!=0&&!gd.checkClanData(apiClanId)){
                                            id.setClanInfo(clanApiData.clanId(),clanApiData.clantag(),realm,clanApiData.updatedAt());
                                        }

                                        if(!dbTeamIds.contains(apiTeamId)){
                                            id.ingameTeamDataRegistration(apiTeamId,apiClanId,tourneyId,apiTeamName,realm);
                                        }

                                        for(JsonElement playerElement:playersElement.getAsJsonArray()){
                                            JsonObject playerObj=playerElement.getAsJsonObject();

                                            long accId=playerObj.get("account_id").getAsLong();
                                            apiMapTeam.computeIfAbsent(apiTeamId,k->new ArrayList<>()).add(accId);

                                            Interfaces.UserData2 userData=(!gd.checkUserData(accId))?getAccountData(accId,realm):null;

                                            if(userData!=null&&!gd.checkUserData(accId)){
                                                id.registerPlayer(accId,userData.nickname(),realm,userData.lastBattleTime(),userData.updatedAt());
                                            }

                                            if(gd.checkIngameTeamRegistry(apiTeamId)&&gd.checkUserData(accId)&&!gd.checkIngameTeamPlayerRegistry(apiTeamId,accId)){
                                                ps3.setInt(1,apiTeamId);
                                                ps3.setLong(2,accId);
                                                ps3.addBatch();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    for(Map.Entry<Integer,List<Long>> entry2:apiMapTeam.entrySet()){
                        int teamId=entry2.getKey();
                        Set<Long> apiPlayers=new HashSet<>(entry2.getValue());
                        if(dbMapTeam.containsKey(teamId)){
                            List<Long> dbPlayers=dbMapTeam.get(teamId);
                            
                            for(long dbAccId:dbPlayers){
                                if(!apiPlayers.contains(dbAccId)){
                                    ps2.setLong(1,dbAccId);
                                    ps2.setInt(2,teamId);
                                    ps2.addBatch();
                                }
                            }
                        }
                    }
                    ps2.executeBatch();
                    ps3.executeBatch();
                }
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     */
    public void validateTeams(){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("update ingame_team_data set clan_id=? where team_id=?");
                PreparedStatement ps2=cn.prepareStatement("update ingame_team_data set team_name=? where team_id=?");
                PreparedStatement ps3=cn.prepareStatement("delete from ingame_team_data where team_id=?")){
            ZoneId sd=ZoneId.systemDefault();
            Map<Integer,Map<Integer,Interfaces.TourneyTeamInfo>> dbTeamsByTourney=gd.getIngameTeamData();
            for(Map.Entry<Integer,Interfaces.TourneyInfo> entry:gd.getTourneyFuncData().entrySet()){
                int tourneyId=entry.getKey();
                Interfaces.TourneyInfo tourney=entry.getValue();
                String realm=tourney.string();
                
                ZonedDateTime systemTime=ZonedDateTime.now(sd);
                ZonedDateTime dbRegStart=Instant.ofEpochSecond(tourney.regStart()).atZone(sd);
                ZonedDateTime dbEndAt=Instant.ofEpochSecond(tourney.endAt()).atZone(sd);
                if(systemTime.isAfter(dbRegStart)&&systemTime.isBefore(dbEndAt)){
                    Map<Integer,Interfaces.TourneyTeamInfo> apiTeams=new HashMap<>();
                    int totalPages=UtilityClass.calculateTotalPages(tourney.teams());
                    for(int pageNo=1;pageNo<=totalPages;pageNo++){
                        String apiResponse=wc.getData(uc.getRealm(realm)+"/wotb/tournaments/teams/?application_id="+UtilityClass.APP_ID+"&tournament_id="+tourneyId+"&limit=100&page_no="+pageNo+"&status=confirmed&fields=clan_id,team_id,title");
                        if(apiResponse!=null&&!apiResponse.isEmpty()){
                            try(Reader r=new StringReader(apiResponse)){
                                JsonElement data=JsonParser.parseReader(r).getAsJsonObject().get("data");
                                if(data!=null&&data.isJsonArray()){
                                    for(JsonElement team:data.getAsJsonArray()){
                                        JsonObject teamObj=team.getAsJsonObject();
                                        
                                        JsonElement clanIdElement=teamObj.get("clan_id");
                                        String apiTeamName=teamObj.get("title").getAsString();
                                        int apiClanId=UtilityClass.tersin(clanIdElement);
                                        
                                        apiTeams.put(teamObj.get("team_id").getAsInt(),new Interfaces.TourneyTeamInfo(apiClanId,apiTeamName));
                                    }
                                }
                            }
                        }
                        
                    }

                    Map<Integer,Interfaces.TourneyTeamInfo> dbTeams=dbTeamsByTourney.getOrDefault(tourneyId,Collections.emptyMap());
                    
                    for(Map.Entry<Integer,Interfaces.TourneyTeamInfo> dbEntry:dbTeams.entrySet()){
                        int teamId=dbEntry.getKey();
                        Interfaces.TourneyTeamInfo dbTeamInfo=dbEntry.getValue();
                        Interfaces.TourneyTeamInfo apiTeamInfo=apiTeams.get(teamId);
                        if(apiTeamInfo!=null){
                            int apiClanId=apiTeamInfo.clanId();
                            int dbClanId=dbTeamInfo.clanId();
                            if(apiClanId!=0&&gd.checkClanData(apiClanId)){
                                if(dbClanId!=apiClanId){
                                    ps.setInt(1,apiClanId);
                                    ps.setInt(2,teamId);
                                    ps.addBatch();
                                }
                            }
                            
                            String dbTeamName=dbTeamInfo.teamName();
                            String apiTeamName=apiTeamInfo.teamName();
                            if(!dbTeamName.equals(apiTeamName)){
                                ps2.setString(1,apiTeamName);
                                ps2.setInt(2,teamId);
                                ps2.addBatch();
                            }
                        }
                    }

                    List<Integer> teamsToDelete=new ArrayList<>(dbTeams.keySet());
                    teamsToDelete.removeAll(apiTeams.keySet());
                    for(Integer teamIdToDelete:teamsToDelete){
                        ps3.setInt(1,teamIdToDelete);
                        ps3.addBatch();
                    }
                    
                    ps.executeBatch();
                    ps2.executeBatch();
                    ps3.executeBatch();
                }
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }
    
    /**
     */
    public void updateTournamentData(){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("update tournament_data set teams_confirmed=? where tournament_id=?");
                PreparedStatement ps2=cn.prepareStatement("update tournament_data set seed_type=? where tournament_id=?");){
            ZoneId sd=ZoneId.systemDefault();
            Map<Integer,Interfaces.TourneyInfo> td2=gd.getTourneyFuncData();
            for(Map.Entry<String,List<List<String>>> entry:gd.getTournamentLists().entrySet()){
                String realm=entry.getKey();
                for(List<String> values:entry.getValue()){
                    String apiResponse=wc.getData(uc.getRealm(realm)+"/wotb/tournaments/info/?application_id="+UtilityClass.APP_ID+"&tournament_id="+String.join(",",values)+"&fields=teams.confirmed,description");
                    if(apiResponse!=null&&!apiResponse.isEmpty()){
                        try(Reader r=new StringReader(apiResponse)){
                            JsonElement data=JsonParser.parseReader(r).getAsJsonObject().get("data");
                            if(data!=null&&!data.isJsonNull()&&data.isJsonObject()){
                                JsonObject tourneys=data.getAsJsonObject();
                                if(tourneys!=null&&!tourneys.isEmpty()){
                                    for(String tourneyId:tourneys.keySet()){
                                        Interfaces.TourneyInfo tourneyData=td2.get(Integer.valueOf(tourneyId));
                                        if(tourneyData!=null){
                                            ZonedDateTime systemTime=ZonedDateTime.now(sd);
                                            ZonedDateTime dbRegStart=Instant.ofEpochSecond(tourneyData.regStart()).atZone(sd);
                                            ZonedDateTime dbStartAt=Instant.ofEpochSecond(tourneyData.startAt()).atZone(sd);
                                            if(systemTime.isAfter(dbRegStart)&&systemTime.isBefore(dbStartAt)){
                                                JsonElement data2=tourneys.get(tourneyId);
                                                if(data2!=null&&!data2.isJsonNull()&&data2.isJsonObject()){
                                                    JsonObject data3=data2.getAsJsonObject();
                                                    JsonObject teamsObj=data3.getAsJsonObject("teams");
                                                    
                                                    JsonElement teamsConfirmed=teamsObj.get("confirmed");
                                                    
                                                    String description=data3.get("description").getAsString();
                                                    int confirmed=UtilityClass.tersin(teamsConfirmed);

                                                    if(confirmed!=tourneyData.teams()){
                                                        ps.setInt(1,confirmed);
                                                        ps.setInt(2,Integer.parseInt(tourneyId));
                                                        ps.executeUpdate();
                                                    }

                                                    String seedingType=UtilityClass.parseSeedingType(description);
                                                    if(tourneyData.string().equalsIgnoreCase("unknown")){
                                                        ps2.setString(1,seedingType);
                                                        ps2.setInt(2,Integer.parseInt(tourneyId));
                                                        ps2.executeUpdate();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     * Gets tank info from the requesting account.
     * @param accId
     */
    public void getAccTankData(long accId){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("insert into tank_stats values(?,?,?,?)");
                PreparedStatement ps2=cn.prepareStatement("select 1 from tank_stats where wotb_id=? and tank_id=?")){
            for(List<String> tankIdLists:gd.getTierTenTankList()){
                String apiResponse=wc.getData(gd.getRealm(accId)+"/wotb/tanks/stats/?application_id="+UtilityClass.APP_ID+"&account_id="+accId+"&tank_id="+String.join(",",tankIdLists)+"&fields=tank_id,all.battles,all.wins");
                if(apiResponse!=null&&!apiResponse.isEmpty()){
                    try(Reader r=new StringReader(apiResponse)){
                        JsonElement data=JsonParser.parseReader(r).getAsJsonObject().get("data");
                        if(data!=null&&!data.isJsonNull()&&data.isJsonObject()){
                            JsonObject player=data.getAsJsonObject();
                            if(player!=null&&!player.isEmpty()){
                                for(String playerId:player.keySet()){
                                    JsonElement values=player.get(playerId);
                                    if(values!=null&&!values.isJsonNull()&&values.isJsonArray()){
                                        for(JsonElement val2:values.getAsJsonArray()){
                                            JsonObject val3=val2.getAsJsonObject();
                                            JsonObject allObj=val3.getAsJsonObject("all");

                                            int tankId=val3.get("tank_id").getAsInt();
                                            int battles=allObj.get("battles").getAsInt();
                                            int wins=allObj.get("wins").getAsInt();
                                            if(battles!=0){
                                                ps2.setLong(1,accId);
                                                ps2.setInt(2,tankId);
                                                try(ResultSet rs=ps2.executeQuery()){
                                                    if(!rs.next()){
                                                        ps.setLong(1,accId);
                                                        ps.setInt(2,tankId);
                                                        ps.setInt(3,battles);
                                                        ps.setInt(4,wins);
                                                        ps.addBatch();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            ps.executeBatch();
                        }
                    }
                }
            }
            dataValidation(accId);
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     * @param accId
     */
    protected void dataValidation(long accId){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("insert into thousand_battles values(?,?,?,?)");
                PreparedStatement ps2=cn.prepareStatement("select 1 from thousand_battles where wotb_id=? and tank_id=?")){
            int battles=gd.getPlayerStats2(accId).battles();

            if(battles<UtilityClass.MAX_BATTLE_COUNT){
                for(List<String> tankIdList:gd.getTankLists()){
                    String apiResponse=wc.getData(gd.getRealm(accId)+"/wotb/tanks/stats/?application_id="+UtilityClass.APP_ID+"&account_id="+accId+"&fields=tank_id,all.battles,all.wins&tank_id="+String.join(",",tankIdList));
                    if(apiResponse!=null&&!apiResponse.isEmpty()){
                        try(Reader r=new StringReader(apiResponse)){
                            JsonElement data=JsonParser.parseReader(r).getAsJsonObject().get("data");
                            if(data!=null&&!data.isJsonNull()&&data.isJsonObject()){
                                JsonObject player=data.getAsJsonObject();
                                if(player!=null&&!player.isEmpty()){
                                    for(String playerId:player.keySet()){
                                        JsonElement values=player.get(playerId);
                                        if(values!=null&&!values.isJsonNull()&&values.isJsonArray()){
                                            for(JsonElement val2:values.getAsJsonArray()){
                                                JsonObject val3=val2.getAsJsonObject();
                                                JsonObject allObj=val3.getAsJsonObject("all");

                                                int tankId=val3.get("tank_id").getAsInt();
                                                int battles2=allObj.get("battles").getAsInt();
                                                int wins=allObj.get("wins").getAsInt();

                                                if(battles2!=0){
                                                    ps2.setLong(1,accId);
                                                    ps2.setInt(2,tankId);
                                                    try(ResultSet rs3=ps2.executeQuery()){
                                                        if(!rs3.next()){
                                                            ps.setLong(1,accId);
                                                            ps.setInt(2,tankId);
                                                            ps.setInt(3,battles2);
                                                            ps.setInt(4,wins);
                                                            ps.addBatch();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                ps.executeBatch();
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     * @param accId
     */
    public void dataManipulation(long accId){
        Map<Integer,Interfaces.TankStats> dbTankStatsMap=new HashMap<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select tank_id,battles,wins from tank_stats where wotb_id=?");
                PreparedStatement ps2=cn.prepareStatement("update tank_stats set battles=?, wins=? where wotb_id=? and tank_id=?");
                PreparedStatement ps3=cn.prepareStatement("insert into tank_stats values(?,?,?,?)")){
            ps.setLong(1,accId);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    dbTankStatsMap.put(rs.getInt("tank_id"),new Interfaces.TankStats(rs.getInt("battles"),rs.getInt("wins")));
                }
            }

            for(List<String> tankIdLists:gd.getTierTenTankList()){
                String apiResponse=wc.getData(gd.getRealm(accId)+"/wotb/tanks/stats/?application_id="+UtilityClass.APP_ID+"&fields=tank_id,all.battles,all.wins&tank_id="+String.join(",",tankIdLists)+"&account_id="+accId);
                if(apiResponse!=null&&!apiResponse.isEmpty()){
                    try(Reader r=new StringReader(apiResponse)){
                        JsonElement data=JsonParser.parseReader(r).getAsJsonObject().get("data");
                        if(data!=null&&!data.isJsonNull()&&data.isJsonObject()){
                            JsonObject player=data.getAsJsonObject();
                            if(player!=null&&!player.isEmpty()){
                                for(String playerId:player.keySet()){
                                    JsonElement values=player.get(playerId);
                                    if(values!=null&&!values.isJsonNull()&&values.isJsonArray()){
                                        for(JsonElement val2:values.getAsJsonArray()){
                                            JsonObject val3=val2.getAsJsonObject();
                                            JsonObject allObj=val3.getAsJsonObject("all");

                                            int apiBattles=allObj.get("battles").getAsInt();
                                            int apiWins=allObj.get("wins").getAsInt();
                                            int apiTankId=val3.get("tank_id").getAsInt();

                                            if(apiBattles==0)continue;

                                            Interfaces.TankStats dbStats=dbTankStatsMap.get(apiTankId);
                                            if(dbStats==null){
                                                uc.log(Level.INFO,accId+" tiene tanque nuevo: "+apiTankId);
                                                ps3.setLong(1,accId);
                                                ps3.setInt(2,apiTankId);
                                                ps3.setInt(3,apiBattles);
                                                ps3.setInt(4,apiWins);
                                                ps3.addBatch();
                                            }else{
                                                int dbBattles=dbStats.battles();
                                                int dbWins=dbStats.wins();
                                                if(apiBattles!=dbBattles||apiWins!=dbWins){
                                                    uc.log(Level.INFO,"si hay cambios de "+accId+", del tanque "+apiTankId);
                                                    ps2.setInt(1,apiBattles);
                                                    ps2.setInt(2,apiWins);
                                                    ps2.setLong(3,accId);
                                                    ps2.setInt(4,apiTankId);
                                                    ps2.addBatch();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ps2.executeBatch();
            ps3.executeBatch();
            thousandBattlesDataManipulation(accId);
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     * @param accId
     */
    protected void thousandBattlesDataManipulation(long accId){
        Map<Integer,Interfaces.TankStats> dbTankStatsMap=new HashMap<>();
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select tank_id,battles,wins from thousand_battles where wotb_id=?");
                PreparedStatement ps2=cn.prepareStatement("insert into thousand_battles values(?,?,?,?)");
                PreparedStatement ps3=cn.prepareStatement("update thousand_battles set battles=?, wins=? where wotb_id=? and tank_id=?")){
            int battles=gd.getPlayerStats2(accId).battles();

            ps.setLong(1,accId);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    dbTankStatsMap.put(rs.getInt("tank_id"),new Interfaces.TankStats(rs.getInt("battles"),rs.getInt("wins")));
                }
            }

            if(battles<=UtilityClass.MAX_BATTLE_COUNT-1){
                for(List<String> tankIdList:gd.getTankLists()){
                    String apiResponse=wc.getData(gd.getRealm(accId)+"/wotb/tanks/stats/?application_id="+UtilityClass.APP_ID+"&account_id="+accId+"&fields=tank_id,all.battles,all.wins&tank_id="+String.join(",",tankIdList));
                    if(apiResponse!=null&&!apiResponse.isEmpty()){
                        try(Reader r=new StringReader(apiResponse)){
                            JsonElement data=JsonParser.parseReader(r).getAsJsonObject().get("data");
                            if(data!=null&&!data.isJsonNull()&&data.isJsonObject()){
                                JsonObject player=data.getAsJsonObject();
                                if(player!=null&&!player.isEmpty()){
                                    for(String playerId:player.keySet()){
                                        JsonElement values=player.get(playerId);
                                        if(values!=null&&!values.isJsonNull()&&values.isJsonArray()){
                                            for(JsonElement val2:values.getAsJsonArray()){
                                                JsonObject val3=val2.getAsJsonObject();
                                                JsonObject allObj=val3.getAsJsonObject("all");

                                                int apiBattles=allObj.get("battles").getAsInt();
                                                int apiWins=allObj.get("wins").getAsInt();
                                                int apiTankId=val3.get("tank_id").getAsInt();

                                                if(apiBattles==0)continue;

                                                Interfaces.TankStats dbStats=dbTankStatsMap.get(apiTankId);
                                                if(dbStats==null){
                                                    uc.log(Level.INFO,accId+" tiene tanque nuevo: "+apiTankId);
                                                    ps2.setLong(1,accId);
                                                    ps2.setInt(2,apiTankId);
                                                    ps2.setInt(3,apiBattles);
                                                    ps2.setInt(4,apiWins);
                                                    ps2.addBatch();
                                                }else{
                                                    int dbBattles=dbStats.battles();
                                                    int dbWins=dbStats.wins();
                                                    if(apiBattles!=dbBattles||apiWins!=dbWins){
                                                        uc.log(Level.INFO,"si hay cambios de "+accId+", del tanque "+apiTankId);
                                                        ps3.setInt(1,apiBattles);
                                                        ps3.setInt(2,apiWins);
                                                        ps3.setLong(3,accId);
                                                        ps3.setInt(4,apiTankId);
                                                        ps3.addBatch();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                ps2.executeBatch();
                ps3.executeBatch();
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }
    
    /**
     */
    public void playerProfile(){
        try{
            ZoneId sd=ZoneId.systemDefault();
            List<Long> playerIds=new ArrayList<>();
            Map<Long,Interfaces.UserData3> pd=gd.getPlayerFuncData();
            for(Map.Entry<String,List<List<String>>> entry:gd.getPlayersLists().entrySet()){
                String realm=entry.getKey();
                for(List<String> val:entry.getValue()){
                    if(!val.isEmpty()){
                        String apiResponse=wc.getData(uc.getRealm(realm)+"/wotb/account/info/?application_id="+UtilityClass.APP_ID+"&account_id="+String.join(",",val)+"&fields=nickname,last_battle_time,updated_at");
                        if(apiResponse!=null&&!apiResponse.isEmpty()){
                            try(Reader r=new StringReader(apiResponse)){
                                JsonElement data=JsonParser.parseReader(r).getAsJsonObject().get("data");
                                if(data!=null&&!data.isJsonNull()&&data.isJsonObject()){
                                    JsonObject players=data.getAsJsonObject();
                                    if(players!=null&&!players.isEmpty()){
                                        for(String playerId:players.keySet()){
                                            JsonElement values=players.get(playerId);
                                            if(values!=null&&!values.isJsonNull()&&values.isJsonObject()){
                                                JsonObject playerData=values.getAsJsonObject();
                                                long apiUser=Long.parseLong(playerId);

                                                String apiNickname=playerData.get("nickname").getAsString();
                                                long apiTimestamp1=playerData.get("last_battle_time").getAsLong();
                                                long apiTimestamp2=playerData.get("updated_at").getAsLong();

                                                playerIds.add(apiUser);

                                                ZonedDateTime apiTime1=Instant.ofEpochSecond(apiTimestamp1).atZone(sd);
                                                ZonedDateTime apiTime2=Instant.ofEpochSecond(apiTimestamp2).atZone(sd);
                                                Interfaces.UserData3 playerValues=pd.get(apiUser);
                                                if(playerValues!=null){
                                                    ZonedDateTime dbTime1=Instant.ofEpochSecond(playerValues.lastBattleTime()).atZone(sd);
                                                    ZonedDateTime dbTime2=Instant.ofEpochSecond(playerValues.updatedAt()).atZone(sd);
                                                    if(dbTime1.isBefore(apiTime1)||dbTime2.isBefore(apiTime2)){
                                                        if(!playerValues.nickname().equals(apiNickname)){
                                                            ud.updateNickname(apiNickname,apiUser);
                                                        }

                                                        dataManipulation(apiUser);
                                                        ud.updateUserTimestamps(apiTimestamp1,apiTimestamp2,apiUser);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Map<Long,Interfaces.TankStats> t10Stats=gd.getPlayerStatsInBatch(playerIds);
            Map<Long,Map<Integer,Interfaces.TankStats>> ltStats=gd.getLowerTierStatsInBatch(playerIds);
            
            for(Long players:playerIds){
                Interfaces.TankStats t10=t10Stats.get(players);
                Map<Integer,Interfaces.TankStats> lt=ltStats.get(players);
                
                if(t10==null||lt==null)continue;
                
                int battles=0;
                for(Map.Entry<Integer,Interfaces.TankStats> tier:lt.entrySet()){
                    battles+=tier.getValue().battles();
                }
                
                if(t10.battles()>=UtilityClass.MAX_BATTLE_COUNT&&battles!=0&&battles>0){
                    dd.freeupThousandBattles(players);
                }
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }
    
    /**
     */
    public void clanProfile(){
        try{
            ZoneId sd=ZoneId.systemDefault();
            Map<Integer,Interfaces.ClanData> cdata=gd.getClanFuncData();
            for(Map.Entry<String,List<List<String>>> entry:gd.getClanLists().entrySet()){
                String realm=entry.getKey();
                for(List<String> val:entry.getValue()){
                    String apiResponse=wc.getData(uc.getRealm(realm)+"/wotb/clans/info/?application_id="+UtilityClass.APP_ID+"&clan_id="+String.join(",",val)+"&fields=tag,updated_at");
                    if(apiResponse!=null&&!apiResponse.isEmpty()){
                        try(Reader r=new StringReader(apiResponse)){
                            JsonElement data=JsonParser.parseReader(r).getAsJsonObject().get("data");
                            if(data!=null&&!data.isJsonNull()&&data.isJsonObject()){
                                JsonObject clans=data.getAsJsonObject();
                                if(clans!=null&&!clans.isEmpty()){
                                    for(String clanIds:clans.keySet()){
                                        JsonElement values=clans.get(clanIds);
                                        if(values!=null&&!values.isJsonNull()&&values.isJsonObject()){
                                            JsonObject clanData=values.getAsJsonObject();
                                            int clanId=Integer.parseInt(clanIds);

                                            String apiClantag=clanData.get("tag").getAsString();
                                            long apiTimestamp=clanData.get("updated_at").getAsLong();
                                            
                                            ZonedDateTime apiTime=Instant.ofEpochSecond(apiTimestamp).atZone(sd);

                                            Interfaces.ClanData cd=cdata.get(clanId);
                                            if(cd!=null){
                                                ZonedDateTime dbTime=Instant.ofEpochSecond(cd.updatedAt()).atZone(sd);
                                                if(dbTime.isBefore(apiTime)){
                                                    if(!cd.clantag().equals(apiClantag)){
                                                        ud.updateClantag(apiClantag,clanId);
                                                    }
                                                    ud.updateClanTimestamp(apiTimestamp,clanId);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }
}