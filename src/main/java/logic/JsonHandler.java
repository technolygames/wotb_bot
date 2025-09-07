package logic;

import mvc.Mvc1;
import mvc.Mvc2;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
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
import java.util.stream.Collectors;

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
     * Gets account data from Wg api.<br>
     * Data obtained is in-game name and user ID.
     * @param nickname
     * @param realm
     * @return in-game name and user ID used to get info from the api.
    */
    public Mvc1 getAccountData(String nickname,String realm){
        Mvc1 model=new Mvc1();
        String apiResponse=wc.getData(uc.getRealm(realm)+"/wotb/account/list/?application_id="+UtilityClass.APP_ID+"&search="+nickname+"&fields=account_id%2Cnickname");
        if(apiResponse!=null&&!apiResponse.isEmpty()){
            try(Reader r=new StringReader(apiResponse)){
                JsonElement values=JsonParser.parseReader(r).getAsJsonObject().get("data");
                if(values!=null&&!values.isJsonNull()&&values.isJsonArray()){
                    for(JsonElement data:values.getAsJsonArray()){
                        JsonObject data2=data.getAsJsonObject();
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
    public Mvc1 getAccountData(long accId,String realm){
        Mvc1 model=new Mvc1();
        String apiResponse=wc.getData(uc.getRealm(realm)+"/wotb/account/info/?application_id="+UtilityClass.APP_ID+"&account_id="+accId+"&fields=nickname%2Clast_battle_time%2Cupdated_at");
        if(apiResponse!=null&&!apiResponse.isEmpty()){
            try(Reader r=new StringReader(apiResponse)){
                JsonElement values=JsonParser.parseReader(r).getAsJsonObject().get("data");
                if(values!=null&&!values.isJsonNull()&&values.isJsonObject()){
                    JsonObject data=values.getAsJsonObject();
                    if(!data.isEmpty()){
                        JsonObject val=data.getAsJsonObject(String.valueOf(accId));
                        model.setNickname(val.get("nickname").getAsString());
                        model.setAcoountId(accId);
                        model.setLastBattleTime(val.get("last_battle_time").getAsLong());
                        model.setUpdatedAt(val.get("updated_at").getAsLong());
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
    public Mvc2 getClanData(String clantag,String realm){
        Mvc2 model=new Mvc2();
        String apiResponse=wc.getData(uc.getRealm(realm)+"/wotb/clans/list/?application_id="+UtilityClass.APP_ID+"&search="+clantag+"&fields=clan_id%2Ctag");
        if(apiResponse!=null&&!apiResponse.isEmpty()){
            try(Reader r=new StringReader(apiResponse)){
                JsonElement values=JsonParser.parseReader(r).getAsJsonObject().get("data");
                if(values!=null&&!values.isJsonNull()&&values.isJsonArray()){
                    for(JsonElement data:values.getAsJsonArray()){
                        JsonObject val=data.getAsJsonObject();
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
    protected Mvc2 getClanData(int clanId,String realm){
        Mvc2 model=new Mvc2();
        String apiResponse=wc.getData(uc.getRealm(realm)+"/wotb/clans/info/?application_id="+UtilityClass.APP_ID+"&clan_id="+clanId+"&fields=clan_id%2Ctag%2Cupdated_at");
        if(apiResponse!=null&&!apiResponse.isEmpty()){
            try(Reader r=new StringReader(apiResponse)){
                JsonElement values=JsonParser.parseReader(r).getAsJsonObject().get("data");
                if(values!=null&&!values.isJsonNull()&&values.isJsonObject()){
                    JsonObject data=values.getAsJsonObject();
                    if(!data.isEmpty()){
                        JsonObject val=data.getAsJsonObject(String.valueOf(clanId));
                        model.setClanId(val.get("clan_id").getAsInt());
                        model.setClantag(val.get("tag").getAsString());
                        model.setRealm(realm);
                        model.setUpdatedAt(val.get("updated_at").getAsLong());
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
                PreparedStatement ps=cn.prepareStatement("insert into tournament_data values(?,?,?,?,?,?,?,?,?)");
                PreparedStatement ps2=cn.prepareStatement("select tournament_id from tournament_data")){
            for(Map.Entry<String,List<String>> entry:tournamentLists.entrySet()){
                String realms=entry.getKey();
                List<String> lists=entry.getValue();
                String apiResponse=wc.getData(uc.getRealm(realms)+"/wotb/tournaments/list/?application_id="+UtilityClass.APP_ID+"&limit=25&fields=tournament_id");
                if(apiResponse!=null&&!apiResponse.isEmpty()){
                    try(Reader r=new StringReader(apiResponse)){
                        JsonElement values=JsonParser.parseReader(r).getAsJsonObject().get("data");
                        if(values!=null&&!values.isJsonNull()&&values.isJsonArray()){
                            for(JsonElement values2:values.getAsJsonArray()){
                                JsonObject val=values2.getAsJsonObject();
                                String tourneyId=val.get("tournament_id").getAsString();
                                lists.add(tourneyId);
                            }
                        }
                    }
                }
            }
            
            Set<Integer> tourneys=new HashSet<>();
            try(ResultSet rs=ps2.executeQuery()){
                while(rs.next()){
                    tourneys.add(rs.getInt("tournament_id"));
                }
            }

            for(Map.Entry<String,List<String>> entry:tournamentLists.entrySet()){
                String realms=entry.getKey();
                List<String> lists=entry.getValue();
                if(!lists.isEmpty()){
                    String apiResponse=wc.getData(uc.getRealm(realms)+"/wotb/tournaments/info/?application_id="+UtilityClass.APP_ID+"&tournament_id="+String.join(",",lists)+"&fields=max_players_count%2Cregistration_start_at%2Cregistration_end_at%2Ctitle%2Ctournament_id%2Cend_at%2Cteams.max%2Cteams.confirmed%2Cstart_at%2Cdescription");
                    if(apiResponse!=null&&!apiResponse.isEmpty()){
                        try(Reader r=new StringReader(apiResponse)){
                            JsonElement vs=JsonParser.parseReader(r).getAsJsonObject().get("data");
                            if(vs!=null&&!vs.isJsonNull()&&vs.isJsonObject()){
                                JsonObject data=vs.getAsJsonObject();
                                if(data!=null&&!data.isEmpty()){
                                    for(String keys:data.keySet()){
                                        JsonObject values2=data.getAsJsonObject(keys);
                                        JsonObject values3=values2.getAsJsonObject("teams");

                                        JsonElement val=values3.get("confirmed");
                                        int max=values3.get("max").getAsInt();
                                        int confirmed=0;
                                        if(val!=null&&!val.isJsonNull()){
                                            confirmed=val.getAsInt();
                                        }

                                        int tourneyId=values2.get("tournament_id").getAsInt();
                                        int maxPlayers=values2.get("max_players_count").getAsInt();
                                        if(maxPlayers==10){
                                            String title=values2.get("title").getAsString();
                                            List<String> titlesMatches=Arrays.asList("10vs10".toLowerCase(),"Professionals".toLowerCase(),"Challengers".toLowerCase(),"Lower".toLowerCase());
                                            String titleVal=title.toLowerCase();
                                            
                                            boolean forbidTitle=titlesMatches.stream().anyMatch(titleVal::contains);
                                            
                                            if(!forbidTitle&&(max!=8&&max!=16)){
                                                JsonElement descriptionElement=values2.get("description");
                                                String description=descriptionElement!=null&&!descriptionElement.isJsonNull()?descriptionElement.getAsString():"";
                                                String seedingType=UtilityClass.parseSeedingType(description);
                                                
                                                if(!tourneys.contains(tourneyId)){
                                                    ps.setInt(1,tourneyId);
                                                    ps.setString(2,title);
                                                    ps.setString(3,seedingType);
                                                    ps.setLong(4,values2.get("registration_start_at").getAsLong());
                                                    ps.setLong(5,values2.get("registration_end_at").getAsLong());
                                                    ps.setInt(6,confirmed);
                                                    ps.setLong(7,values2.get("start_at").getAsLong());
                                                    ps.setLong(8,values2.get("end_at").getAsLong());
                                                    ps.setString(9,realms);
                                                    ps.executeUpdate();
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
        LocalDateTime systemTime=LocalDateTime.now(ZoneId.systemDefault());
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("SELECT tournament_id, teams_confirmed, realm, registration_start_at, registration_end_at FROM tournament_data");
                PreparedStatement ps2=cn.prepareStatement("SELECT team_id FROM ingame_team_data WHERE tournament_id=?")){
            Map<Integer,Interfaces.TourneyInfo> tdata=new HashMap<>();
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    int tourneyId=rs.getInt("tournament_id");
                    Interfaces.TourneyInfo td=new Interfaces.TourneyInfo(
                            rs.getString("realm"),
                            rs.getLong("registration_start_at"),
                            rs.getLong("registration_end_at"),
                            rs.getInt("teams_confirmed")
                    );
                    tdata.put(tourneyId,td);
                }
            }
            
            for(Map.Entry<Integer,Interfaces.TourneyInfo> data:tdata.entrySet()){
                int tourneyId=data.getKey();
                Interfaces.TourneyInfo td=data.getValue();
                String realm=td.string();
                LocalDateTime dbRegStart=Instant.ofEpochSecond(td.regStart()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                LocalDateTime dbRegEnd=Instant.ofEpochSecond(td.regEnd()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                int teamsConfirmed=td.teams();
                
                if(systemTime.isAfter(dbRegStart)&&systemTime.isBefore(dbRegEnd)){
                    int totalPages=UtilityClass.calculateTotalPages(teamsConfirmed);
                    
                    Set<Integer> existingTeamIds=new HashSet<>();
                    ps2.setInt(1,tourneyId);
                    try(ResultSet rs=ps2.executeQuery()){
                        while(rs.next()){
                            existingTeamIds.add(rs.getInt("team_id"));
                        }
                    }
                    
                    for(int pageNo=1;pageNo<=totalPages;pageNo++){
                        String apiResponse=wc.getData(uc.getRealm(realm)+"/wotb/tournaments/teams/?application_id="+UtilityClass.APP_ID+"&tournament_id="+tourneyId+"&fields=clan_id%2Cteam_id%2Ctitle%2Ctournament_id%2Cplayers%2Cplayers.account_id&status=confirmed&page_no="+pageNo+"&limit=100");
                        if(apiResponse!=null&&!apiResponse.isEmpty()){
                            try(Reader r=new StringReader(apiResponse)){
                                JsonElement dataElement=JsonParser.parseReader(r).getAsJsonObject().get("data");
                                if(dataElement!=null&&dataElement.isJsonArray()){
                                    for(JsonElement teamElement:dataElement.getAsJsonArray()){
                                        JsonObject teamObj=teamElement.getAsJsonObject();

                                        int apiTeamId=teamObj.get("team_id").getAsInt();
                                        String apiTeamName=teamObj.get("title").getAsString();
                                        int apiClanId=teamObj.get("clan_id").isJsonNull()?0:teamObj.get("clan_id").getAsInt();

                                        if(apiClanId!=0&&!gd.checkClanData(apiClanId,realm)){
                                            Mvc2 clanApiData=getClanData(apiClanId,realm);
                                            if(clanApiData!=null&&clanApiData.getClanId()==apiClanId&&clanApiData.getClantag()!=null&&!clanApiData.getClantag().isEmpty()){
                                                id.setClanInfo(clanApiData.getClanId(),clanApiData.getClantag(),realm,clanApiData.getUpdatedAt());
                                            }else{
                                                continue;
                                            }
                                        }

                                        boolean currentTeamDataOperationSuccess=false;
                                        if(!existingTeamIds.contains(apiTeamId)){
                                            id.ingameTeamDataRegistration(apiTeamId,apiClanId,tourneyId,apiTeamName,realm);
                                            currentTeamDataOperationSuccess=true;
                                        }

                                        if(currentTeamDataOperationSuccess){
                                            if(teamObj.has("players")&&teamObj.get("players").isJsonArray()){
                                                for(JsonElement pElement:teamObj.getAsJsonArray("players")){
                                                    JsonObject pObj=pElement.getAsJsonObject();
                                                    long accId=pObj.get("account_id").getAsLong();

                                                    if(!gd.checkUserData(accId,realm)){
                                                        Mvc1 userData=getAccountData(accId,realm);
                                                        if(userData!=null&&userData.getAcoountId()==accId){
                                                            id.registerPlayer(accId,userData.getNickname(),realm,userData.getLastBattleTime(),userData.getUpdatedAt());
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
    public void synchronizeTeamRosters(){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("SELECT itd.team_id, itd.realm FROM ingame_team_data itd JOIN tournament_data td ON itd.tournament_id = td.tournament_id WHERE NOW() BETWEEN FROM_UNIXTIME(td.registration_start_at) AND FROM_UNIXTIME(td.registration_end_at)")){
            Map<String,Map<Integer,List<Integer>>> teamsByRealmAndTourney=new HashMap<>();
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    String realm=rs.getString("realm");
                    int tourneyId=rs.getInt("tournament_id");
                    int teamId=rs.getInt("team_id");

                    teamsByRealmAndTourney.
                            computeIfAbsent(realm,k->new HashMap<>()).
                            computeIfAbsent(tourneyId,k->new ArrayList<>()).
                            add(teamId);
                }
            }

            for(Map.Entry<String,Map<Integer,List<Integer>>> realmEntry:teamsByRealmAndTourney.entrySet()){
                String realm=realmEntry.getKey();
                for(Map.Entry<Integer,List<Integer>> tourneyEntry:realmEntry.getValue().entrySet()){
                    int tourneyId=tourneyEntry.getKey();
                    List<Integer> allTeamsInTourney=tourneyEntry.getValue();
                    List<Integer> currentBatch=new ArrayList<>();
                    for(Integer teamId:allTeamsInTourney){
                        currentBatch.add(teamId);
                        if(currentBatch.size()==25){
                            computeTeams(currentBatch,tourneyId,realm);
                            currentBatch.clear();
                        }
                    }
                    if (!currentBatch.isEmpty()){
                        computeTeams(currentBatch,tourneyId,realm);
                    }
                }
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }
    
    protected void computeTeams(List<Integer> currentBatch,int tourneyId,String realm){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps2=cn.prepareStatement("SELECT wotb_id FROM ingame_team WHERE team_id = ?")){
            String teamIdsForQuery=currentBatch.stream().map(String::valueOf).collect(Collectors.joining(","));
            String apiResponse=wc.getData(uc.getRealm(realm)+"/wotb/tournaments/teams/?tournament_id="+tourneyId+"&application_id="+UtilityClass.APP_ID+"&team_id="+teamIdsForQuery+"&fields=team_id,players.account_id");
            if(apiResponse!=null&&!apiResponse.isEmpty()){
                try(Reader r=new StringReader(apiResponse)){
                    JsonElement data=JsonParser.parseReader(r).getAsJsonObject().get("data");
                    if(data!=null&&data.isJsonArray()){
                        for(JsonElement element:data.getAsJsonArray()){
                            JsonObject values=element.getAsJsonObject();
                            JsonElement val=values.get("players");

                            List<Long> apiPlayerIds=new ArrayList<>();
                            for(JsonElement element2:val.getAsJsonArray()){
                                long value3=element2.getAsJsonObject().getAsJsonArray("account_id").getAsLong();
                                apiPlayerIds.add(value3);
                            }

                            int teamIdFromApi=values.get("team_id").getAsInt();

                            Set<Long> dbPlayerIdsInTeam=new HashSet<>();
                            ps2.setInt(1,teamIdFromApi);
                            try(ResultSet rs2=ps2.executeQuery()){
                                while(rs2.next()){
                                    dbPlayerIdsInTeam.add(rs2.getLong("wotb_id"));
                                }
                            }

                            for(long accId:apiPlayerIds){
                                if(!dbPlayerIdsInTeam.contains(accId)&&gd.checkUserData(accId,realm)){
                                    id.ingameTeamRegistration(teamIdFromApi,accId);
                                }
                            }

                            for(long dbAccId:dbPlayerIdsInTeam){
                                if(!apiPlayerIds.contains(dbAccId)){
                                    dd.removeFromIngameRoster(dbAccId,teamIdFromApi);
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
    public void validateTeams(){
        LocalDateTime systemTime=LocalDateTime.now(ZoneId.systemDefault());
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select tournament_id,registration_start_at,registration_end_at from tournament_data");
                PreparedStatement ps2=cn.prepareStatement("update ingame_team_data set clan_id=? where team_id=?")){
            Map<Integer,long[]> tournamentDates=new HashMap<>();
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    int tourneyId=rs.getInt("tournament_id");
                    tournamentDates.put(tourneyId,new long[]{rs.getLong("registration_start_at"),rs.getLong("registration_end_at")});
                }
            }

            for(Map.Entry<String,Map<Integer,List<List<Interfaces.TeamInfo>>>> realmEntry:gd.getIngameTeamIds().entrySet()){
                String realm=realmEntry.getKey();
                for(Map.Entry<Integer,List<List<Interfaces.TeamInfo>>> tourneyEntry:realmEntry.getValue().entrySet()){
                    int tourneyId=tourneyEntry.getKey();
                    List<List<Interfaces.TeamInfo>> batches=tourneyEntry.getValue();

                    long[] dates=tournamentDates.get(tourneyId);
                    LocalDateTime dbRegStart=Instant.ofEpochSecond(dates[0]).atZone(ZoneId.systemDefault()).toLocalDateTime();
                    LocalDateTime dbRegEnd=Instant.ofEpochSecond(dates[1]).atZone(ZoneId.systemDefault()).toLocalDateTime();

                    if(systemTime.isAfter(dbRegStart)&&systemTime.isBefore(dbRegEnd)){
                        for(List<Interfaces.TeamInfo> batchOfTeams:batches){
                            String teamIdsForQuery=batchOfTeams.stream().map(Interfaces.TeamInfo::teamId).collect(Collectors.joining(","));
                            String apiResponse=wc.getData(uc.getRealm(realm)+"/wotb/tournaments/teams/?application_id="+UtilityClass.APP_ID+"&tournament_id="+tourneyId+"&team_id="+teamIdsForQuery+"&fields=team_id,status,clan_id&limit=25");
                            if(apiResponse!=null&&!apiResponse.isEmpty()){
                                try(Reader r=new StringReader(apiResponse)){
                                    Map<String,Interfaces.TeamInfo> teamInfoMap=batchOfTeams.stream().collect(Collectors.toMap(Interfaces.TeamInfo::teamId,info->info));
                                    List<String> allTeamIdsInBatch=batchOfTeams.stream().map(Interfaces.TeamInfo::teamId).toList();
                                    Set<String> foundTeamIds=new HashSet<>();

                                    JsonElement dataElement=JsonParser.parseReader(r).getAsJsonObject().get("data");
                                    if(dataElement!=null&&dataElement.isJsonArray()){
                                        for(JsonElement teamElement:dataElement.getAsJsonArray()){
                                            JsonObject teamObj=teamElement.getAsJsonObject();
                                            String status=teamObj.get("status").getAsString();
                                            int apiClanId=teamObj.get("clan_id").getAsInt();
                                            String apiTeamId=teamObj.get("team_id").getAsString();

                                            if(status.equalsIgnoreCase("confirmed")){
                                                Interfaces.TeamInfo dbTeamInfo=teamInfoMap.get(apiTeamId);
                                                if(apiClanId!=dbTeamInfo.clanId()&&gd.checkClanData(apiClanId,realm)){
                                                    ps2.setInt(1,apiClanId);
                                                    ps2.setInt(2,Integer.parseInt(apiTeamId));
                                                    ps2.executeUpdate();
                                                }

                                                foundTeamIds.add(apiTeamId);
                                            }
                                        }
                                    }

                                    List<String> teamsToDelete=new ArrayList<>(allTeamIdsInBatch);
                                    teamsToDelete.removeAll(foundTeamIds);

                                    if(!teamsToDelete.isEmpty()){
                                        for(String teamIdStr:teamsToDelete){
                                            dd.removeIngameTeam(Integer.parseInt(teamIdStr));
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
    public void updateTournamentData(){
        LocalDateTime systemTime=LocalDateTime.now(ZoneId.systemDefault());
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select tournament_id,seed_type,registration_start_at,registration_end_at,teams_confirmed from tournament_data");
                PreparedStatement ps2=cn.prepareStatement("update tournament_data set teams_confirmed=? where tournament_id=?");
                PreparedStatement ps3=cn.prepareStatement("update tournament_data set seed_type=? where tournament_id=?")){
            Map<Integer,Interfaces.TourneyInfo> tdata=new HashMap<>();
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    int tourneyId=rs.getInt("tournament_id");
                    Interfaces.TourneyInfo td=new Interfaces.TourneyInfo(
                            rs.getString("seed_type"),
                            rs.getLong("registration_start_at"),
                            rs.getLong("registration_end_at"),
                            rs.getInt("teams_confirmed")
                    );
                    tdata.put(tourneyId,td);
                }
            }
            
            for(Map.Entry<String,List<List<String>>> entry:gd.getTournamentLists().entrySet()){
                String realm=entry.getKey();
                for(List<String> values:entry.getValue()){
                    for(String tourneys:values){
                        Interfaces.TourneyInfo td=tdata.get(Integer.valueOf(tourneys));
                        if(td!=null){
                            LocalDateTime start=LocalDateTime.ofInstant(Instant.ofEpochSecond(td.regStart()),ZoneId.systemDefault());
                            LocalDateTime end=LocalDateTime.ofInstant(Instant.ofEpochSecond(td.regEnd()),ZoneId.systemDefault());

                            if(systemTime.isAfter(start)&&systemTime.isBefore(end)){
                                String apiResponse=wc.getData(uc.getRealm(realm)+"/wotb/tournaments/info/?application_id="+UtilityClass.APP_ID+"&tournament_id="+tourneys+"&fields=teams.confirmed%2Cdescription");
                                if(apiResponse!=null&&!apiResponse.isEmpty()){
                                    try(Reader r=new StringReader(apiResponse)){
                                        JsonElement vs=JsonParser.parseReader(r).getAsJsonObject().get("data");
                                        if(vs!=null&&!vs.isJsonNull()&&vs.isJsonObject()){
                                            JsonObject data=vs.getAsJsonObject();
                                            if(data!=null&&!data.isEmpty()){
                                                JsonObject data2=data.getAsJsonObject(tourneys);
                                                JsonObject values3=data2.getAsJsonObject("teams");
                                                JsonElement descriptionElement=data2.get("description");
                                                JsonElement val=values3.get("confirmed");

                                                int apiTeams=0;
                                                if(val!=null&&!val.isJsonNull()){
                                                    apiTeams=val.getAsInt();
                                                }

                                                if(apiTeams!=td.teams()){
                                                    ps2.setInt(1,apiTeams);
                                                    ps2.setInt(2,Integer.parseInt(tourneys));
                                                    ps2.executeUpdate();
                                                }

                                                String description=descriptionElement!=null&&!descriptionElement.isJsonNull()?descriptionElement.getAsString():"";
                                                String seedingType=UtilityClass.parseSeedingType(description);
                                                if(td.string().equalsIgnoreCase("unknown")){
                                                    ps3.setString(1,seedingType);
                                                    ps3.setInt(2,Integer.parseInt(tourneys));
                                                    ps3.executeUpdate();
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
            String apiResponse=wc.getData(gd.getRealm(accId)+"/wotb/tanks/stats/?application_id="+UtilityClass.APP_ID+"&account_id="+accId+"&tank_id="+gd.getTierTenTankList()+"&fields=tank_id%2Call.battles%2Call.wins");
            if(apiResponse!=null&&!apiResponse.isEmpty()){
                try(Reader r=new StringReader(apiResponse)){
                    JsonElement data=JsonParser.parseReader(r).getAsJsonObject().get("data");
                    if(data!=null&&!data.isJsonNull()&&data.isJsonObject()){
                        JsonElement values=data.getAsJsonObject().get(String.valueOf(accId));
                        if(values!=null&&!values.isJsonNull()&&values.isJsonArray()){
                            for(JsonElement val2:values.getAsJsonArray()){
                                JsonObject val3=val2.getAsJsonObject();
                                JsonObject val4=val3.getAsJsonObject("all");

                                int tankId=val3.get("tank_id").getAsInt();
                                int battles=val4.get("battles").getAsInt();
                                int wins=val4.get("wins").getAsInt();
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
                        ps.executeBatch();
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
                PreparedStatement ps=cn.prepareStatement("select sum(battles) as battles from tank_stats where wotb_id=?");
                PreparedStatement ps2=cn.prepareStatement("insert into thousand_battles values(?,?,?,?)");
                PreparedStatement ps3=cn.prepareStatement("select 1 from thousand_battles where wotb_id=? and tank_id=?")){
            int battles=0;

            ps.setLong(1,accId);
            try(ResultSet rs2=ps.executeQuery()){
                if(rs2.next()){
                    battles=rs2.getInt("battles");
                }
            }

            if(battles<UtilityClass.MAX_BATTLE_COUNT){
                for(List<String> tankIdList:gd.getTankLists()){
                    String apiResponse=wc.getData(gd.getRealm(accId)+"/wotb/tanks/stats/?application_id="+UtilityClass.APP_ID+"&account_id="+accId+"&fields=tank_id%2Call.battles%2Call.wins&tank_id="+String.join(",",tankIdList));
                    if(apiResponse!=null&&!apiResponse.isEmpty()){
                        try(Reader r=new StringReader(apiResponse)){
                            JsonElement data=JsonParser.parseReader(r).getAsJsonObject().get("data");
                            if(data!=null&&!data.isJsonNull()&&data.isJsonObject()){
                                JsonElement values=data.getAsJsonObject().get(String.valueOf(accId));
                                if(values!=null&&!values.isJsonNull()&&values.isJsonArray()){
                                    for(JsonElement val2:values.getAsJsonArray()){
                                        JsonObject val3=val2.getAsJsonObject();
                                        JsonObject val4=val3.getAsJsonObject("all");

                                        int tankId=val3.get("tank_id").getAsInt();
                                        int battles2=val4.get("battles").getAsInt();
                                        int wins=val4.get("wins").getAsInt();

                                        if(battles2!=0){
                                            ps3.setLong(1,accId);
                                            ps3.setInt(2,tankId);
                                            try(ResultSet rs3=ps3.executeQuery()){
                                                if(!rs3.next()){
                                                    ps2.setLong(1,accId);
                                                    ps2.setInt(2,tankId);
                                                    ps2.setInt(3,battles2);
                                                    ps2.setInt(4,wins);
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
                ps2.executeBatch();
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     * @param accId
     */
    public void dataManipulation(long accId){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select tank_id,battles,wins from tank_stats where wotb_id=?");
                PreparedStatement ps2=cn.prepareStatement("update tank_stats set battles=?, wins=? where wotb_id=? and tank_id=?");
                PreparedStatement ps3=cn.prepareStatement("insert into tank_stats values(?,?,?,?)")){
            Map<Integer,Interfaces.TankStats> dbTankStatsMap=new HashMap<>();
            ps.setLong(1,accId);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    dbTankStatsMap.put(rs.getInt("tank_id"),new Interfaces.TankStats(rs.getInt("battles"),rs.getInt("wins")));
                }
            }

            String apiResponse=wc.getData(gd.getRealm(accId)+"/wotb/tanks/stats/?application_id="+UtilityClass.APP_ID+"&fields=tank_id%2Call.battles%2Call.wins&tank_id="+gd.getTierTenTankList()+"&account_id="+accId);
            if(apiResponse!=null&&!apiResponse.isEmpty()){
                try(Reader r=new StringReader(apiResponse)){
                    JsonElement data=JsonParser.parseReader(r).getAsJsonObject().get("data");
                    if(data!=null&&!data.isJsonNull()&&data.isJsonObject()){
                        JsonElement values=data.getAsJsonObject().get(String.valueOf(accId));
                        if(values!=null&&!values.isJsonNull()&&values.isJsonArray()){
                            for(JsonElement val2:values.getAsJsonArray()){
                                JsonObject val3=val2.getAsJsonObject();
                                JsonObject val4=val3.getAsJsonObject("all");

                                int apiBattles=val4.get("battles").getAsInt();
                                int apiWins=val4.get("wins").getAsInt();
                                int apiTankId=val3.get("tank_id").getAsInt();

                                if(apiBattles==0)continue;

                                Interfaces.TankStats dbStats=dbTankStatsMap.get(apiTankId);
                                if(dbStats==null){
                                    uc.log(Level.INFO,accId+" tiene tanque nuevo: "+apiTankId);
                                    ps3.setLong(1,accId);
                                    ps3.setInt(2,apiTankId);
                                    ps3.setInt(3,apiBattles);
                                    ps3.setInt(4,apiWins);
                                    ps3.executeUpdate();
                                }else{
                                    int dbBattles=dbStats.battles();
                                    int dbWins=dbStats.wins();
                                    if(apiBattles!=dbBattles||apiWins!=dbWins){
                                        uc.log(Level.INFO,"si hay cambios de "+accId+", del tanque "+apiTankId);
                                        ps2.setInt(1,apiBattles);
                                        ps2.setInt(2,apiWins);
                                        ps2.setLong(3,accId);
                                        ps2.setInt(4,apiTankId);
                                        ps2.executeUpdate();
                                    }
                                }
                            }
                        }
                        thousandBattlesDataManipulation(accId);
                    }
                }
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     * @param accId
     */
    protected void thousandBattlesDataManipulation(long accId){
        try(Connection cn=new DbConnection().getConnection();
                PreparedStatement ps=cn.prepareStatement("select sum(battles) as battles from tank_stats where wotb_id=?");
                PreparedStatement ps2=cn.prepareStatement("select tank_id,battles,wins from thousand_battles where wotb_id=?");
                PreparedStatement ps3=cn.prepareStatement("insert into thousand_battles values(?,?,?,?)");
                PreparedStatement ps4=cn.prepareStatement("update thousand_battles set battles=?, wins=? where wotb_id=? and tank_id=?")){
            int battles=0;

            ps.setLong(1,accId);
            try(ResultSet rs2=ps.executeQuery()){
                if(rs2.next()){
                    battles=rs2.getInt("battles");
                }
            }
            
            Map<Integer,Interfaces.TankStats> dbTankStatsMap=new HashMap<>();
            ps2.setLong(1,accId);
            try(ResultSet rs=ps2.executeQuery()){
                while(rs.next()){
                    dbTankStatsMap.put(rs.getInt("tank_id"),new Interfaces.TankStats(rs.getInt("battles"),rs.getInt("wins")));
                }
            }

            if(battles<=UtilityClass.MAX_BATTLE_COUNT-1){
                for(List<String> tankIdList:gd.getTankLists()){
                    String apiResponse=wc.getData(gd.getRealm(accId)+"/wotb/tanks/stats/?application_id="+UtilityClass.APP_ID+"&account_id="+accId+"&fields=tank_id%2Call.battles%2Call.wins&tank_id="+String.join(",",tankIdList));
                    if(apiResponse!=null&&!apiResponse.isEmpty()){
                        try(Reader r=new StringReader(apiResponse)){
                            JsonElement data=JsonParser.parseReader(r).getAsJsonObject().get("data");
                            if(data!=null&&!data.isJsonNull()&&data.isJsonObject()){
                                JsonElement values=data.getAsJsonObject().get(String.valueOf(accId));
                                if(values!=null&&!values.isJsonNull()&&values.isJsonArray()){
                                    for(JsonElement val2:values.getAsJsonArray()){
                                        JsonObject val3=val2.getAsJsonObject();
                                        JsonObject val4=val3.getAsJsonObject("all");

                                        int apiBattles=val4.get("battles").getAsInt();
                                        int apiWins=val4.get("wins").getAsInt();
                                        int apiTankId=val3.get("tank_id").getAsInt();

                                        if(apiBattles==0)continue;

                                        Interfaces.TankStats dbStats=dbTankStatsMap.get(apiTankId);
                                        if(dbStats==null){
                                            uc.log(Level.INFO,accId+" tiene tanque nuevo: "+apiTankId);
                                            ps3.setLong(1,accId);
                                            ps3.setInt(2,apiTankId);
                                            ps3.setInt(3,apiBattles);
                                            ps3.setInt(4,apiWins);
                                            ps3.executeUpdate();
                                        }else{
                                            int dbBattles=dbStats.battles();
                                            int dbWins=dbStats.wins();
                                            if(apiBattles!=dbBattles||apiWins!=dbWins){
                                                uc.log(Level.INFO,"si hay cambios de "+accId+", del tanque "+apiTankId);
                                                ps4.setInt(1,apiBattles);
                                                ps4.setInt(2,apiWins);
                                                ps4.setLong(3,accId);
                                                ps4.setInt(4,apiTankId);
                                                ps4.executeUpdate();
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
    public void playerProfile(){
        try{
            Map<Long,Interfaces.UserData> dbDataMap=gd.getPlayerFuncData();
            for(Map.Entry<String,List<List<String>>> entry:gd.getPlayersLists().entrySet()){
                for(List<String> val:entry.getValue()){
                    for(String players:val){
                        if(!players.isEmpty()){
                            String apiResponse=wc.getData(uc.getRealm(entry.getKey())+"/wotb/account/info/?application_id="+UtilityClass.APP_ID+"&account_id="+String.join(",",players)+"&fields=nickname%2Clast_battle_time%2Cupdated_at");
                            if(apiResponse!=null&&!apiResponse.isEmpty()){
                                try(Reader r=new StringReader(apiResponse)){
                                    JsonElement data=JsonParser.parseReader(r).getAsJsonObject().get("data");
                                    if(data!=null&&!data.isJsonNull()&&data.isJsonObject()){
                                        JsonElement values=data.getAsJsonObject().get(players);
                                        if(values!=null&&!values.isJsonNull()&&values.isJsonObject()){
                                            JsonObject playerData=values.getAsJsonObject();
                                            long apiUser=Long.parseLong(players);
                                            String apiNickname=playerData.get("nickname").getAsString();
                                            long apiTimestamp1=playerData.get("last_battle_time").getAsLong();
                                            long apiTimestamp2=playerData.get("updated_at").getAsLong();

                                            LocalDateTime apiTime1=LocalDateTime.ofInstant(Instant.ofEpochSecond(apiTimestamp1),ZoneId.systemDefault());
                                            LocalDateTime apiTime2=LocalDateTime.ofInstant(Instant.ofEpochSecond(apiTimestamp2),ZoneId.systemDefault());

                                            Interfaces.UserData dbData=dbDataMap.get(apiUser);
                                            if(dbData!=null){
                                                LocalDateTime dbTime1=LocalDateTime.ofInstant(Instant.ofEpochSecond(dbData.lastBattleTime()),ZoneId.systemDefault());
                                                LocalDateTime dbTime2=LocalDateTime.ofInstant(Instant.ofEpochSecond(dbData.updatedAt()),ZoneId.systemDefault());
                                                if(dbTime2.isBefore(apiTime2)&&dbTime1.isBefore(apiTime1)&&(dbTime1.getHour()!=apiTime1.getHour()||dbTime1.getMinute()!=apiTime1.getMinute())){
                                                    if(!dbData.nickname().equals(apiNickname)){
                                                        ud.updateNickname(apiNickname,apiUser);
                                                    }
                                                    dataManipulation(apiUser);
                                                    ud.updateUserTimestamps(apiTimestamp1,apiTimestamp2,apiUser);
                                                }

                                                if(dbData.tankStatsBattles()>=UtilityClass.MAX_BATTLE_COUNT&&dbData.thousandBattlesBattles()>0){
                                                    dd.freeupThousandBattles(apiUser);
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
    public void clanProfile(){
        try{
            Map<Integer,Interfaces.ClanData> cdata=gd.getClanFuncData();
            for(Map.Entry<String,List<List<String>>> entry:gd.getClanLists().entrySet()){
                String realm=entry.getKey();
                for(List<String> val:entry.getValue()){
                    for(String clans:val){
                        if(!clans.isEmpty()){
                            String apiResponse=wc.getData(uc.getRealm(realm)+"/wotb/clans/info/?application_id="+UtilityClass.APP_ID+"&clan_id="+String.join(",",clans)+"&fields=tag%2Cupdated_at");
                            if(apiResponse!=null&&!apiResponse.isEmpty()){
                                try(Reader r=new StringReader(apiResponse)){
                                    JsonElement data=JsonParser.parseReader(r).getAsJsonObject().get("data");
                                    if(data!=null&&!data.isJsonNull()&&data.isJsonObject()){
                                        JsonElement values=data.getAsJsonObject().get(clans);
                                        if(values!=null&&!values.isJsonNull()&&values.isJsonObject()){
                                            JsonObject clanData=values.getAsJsonObject();
                                            int clanId=Integer.parseInt(clans);

                                            JsonElement tagElement=clanData.get("tag");
                                            JsonElement updatedAtElement=clanData.get("updated_at");
                                            if(tagElement!=null&&!tagElement.isJsonNull()&&tagElement.isJsonPrimitive()&&updatedAtElement!=null&&!updatedAtElement.isJsonNull()&&updatedAtElement.isJsonPrimitive()){
                                                String apiClantag=tagElement.getAsString();
                                                long apiTimestamp=updatedAtElement.getAsLong();
                                                LocalDateTime apiTime=LocalDateTime.ofInstant(Instant.ofEpochSecond(apiTimestamp),ZoneId.systemDefault());

                                                Interfaces.ClanData cd=cdata.get(clanId);
                                                if(cd!=null){
                                                    LocalDateTime dbTime=LocalDateTime.ofInstant(Instant.ofEpochSecond(cd.updatedAt()),ZoneId.systemDefault());
                                                    if(dbTime.isBefore(apiTime)&&(dbTime.getHour()!=apiTime.getHour()||dbTime.getMinute()!=apiTime.getMinute())){
                                                        if(!cd.clantag().equals(apiClantag)){
                                                            ud.updateClantag(apiClantag,clanId,cd.realm());
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
            }
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     * @return
     */
    public Map<String,String> getHelpCommandData(){
        Map<String,String> descriptions=new HashMap<>();
        Gson gson=new Gson();
        try(FileReader fr=new FileReader("data/command_data.json",StandardCharsets.UTF_8)){
            Type type=new TypeToken<Map<String,String>>(){}.getType();
            descriptions=gson.fromJson(fr,type);
        }catch(Exception e){
            uc.log(Level.SEVERE,e.getMessage(),e);
        }
        return descriptions;
    }
}