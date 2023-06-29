package dbconnection;

import java.io.IOException;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.util.Properties;

import logic.ApiRequest;

public class DbConnection{
    protected Properties p;
    
    public Connection getConnection(){
        p=new Properties();
        try{
            p.load(new FileInputStream("data/config.properties"));
            String database=p.getProperty("database");
            String server=p.getProperty("server");
            String port=p.getProperty("port");
            String user=p.getProperty("user");
            String pass=p.getProperty("pass");

            return DriverManager.getConnection("jdbc:mysql://"+server+":"+port+"/"+database+"?serverTimezone=UTC",user,pass);
        }catch(SQLException e){
            ApiRequest.LOGGER.config(e.fillInStackTrace().toString());
            return null;
        }catch(IOException x){
            ApiRequest.LOGGER.config(x.fillInStackTrace().toString());
            return null;
        }
    }
}