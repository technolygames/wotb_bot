package dbconnection;

import logic.UtilityClass;

import java.io.IOException;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 *
 * @author erick
 */
public class DbConnection{
    private DbConnection(){}

    private static HikariDataSource ds;

    static{
        Properties p=new Properties();
        HikariConfig config=new HikariConfig();
        try{
            p.load(new FileInputStream("data/config.properties"));
            config.setJdbcUrl("jdbc:mysql://"+p.getProperty("server")+":"+p.getProperty("port")+"/"+p.getProperty("database")+"?serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8");
            config.setUsername(p.getProperty("user"));
            config.setPassword(p.getProperty("pass"));
            config.addDataSourceProperty("cachePrepStmts","true");
            config.addDataSourceProperty("prepStmtCacheSize","256");
            config.addDataSourceProperty("prepStmtCacheSqlLimit","2048");
            config.setMaximumPoolSize(600);
            ds=new HikariDataSource(config);
        }catch(IOException x){
            UtilityClass.LOGGER.severe(x.fillInStackTrace().toString());
        }
    }

    public static Connection getConnection(){
        try{
            return ds.getConnection();
        }catch(SQLException e){
            UtilityClass.LOGGER.severe(e.fillInStackTrace().toString());
            return null;
        }catch(NullPointerException s){
            UtilityClass.LOGGER.severe(s.fillInStackTrace().toString());
            return null;
        }catch(Exception n){
            UtilityClass.LOGGER.severe(n.fillInStackTrace().toString());
            return null;
        }
    }
}