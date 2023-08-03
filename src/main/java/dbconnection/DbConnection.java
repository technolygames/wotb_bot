package dbconnection;

import java.io.IOException;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.util.Properties;

import logic.UtilityClass;

/**
 *
 * @author erick
 */
public class DbConnection{
    private DbConnection(){}

    public static Connection getConnection(){
        Properties p=new Properties();
        try{
            p.load(new FileInputStream("data/config.properties"));
            String database=p.getProperty("database");
            String server=p.getProperty("server");
            String port=p.getProperty("port");
            String user=p.getProperty("user");
            String pass=p.getProperty("pass");

            return DriverManager.getConnection("jdbc:mysql://"+server+":"+port+"/"+database+"?serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8",user,pass);
        }catch(SQLException e){
            UtilityClass.LOGGER.info(e.fillInStackTrace().toString());
            return null;
        }catch(IOException x){
            UtilityClass.LOGGER.info(x.fillInStackTrace().toString());
            return null;
        }catch(NullPointerException s){
            UtilityClass.LOGGER.info(s.fillInStackTrace().toString());
            return null;
        }
    }
}