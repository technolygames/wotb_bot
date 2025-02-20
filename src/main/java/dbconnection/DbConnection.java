package dbconnection;

import logic.UtilityClass;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import java.util.logging.Level;

/**
 * @author erick
 */
public class DbConnection{
    protected static HikariDataSource ds;

    static{
        Properties p=new Properties();
        HikariConfig config=new HikariConfig();
        try(FileInputStream fis=new FileInputStream("data/config.properties")){
            p.load(fis);
            config.setJdbcUrl("jdbc:mysql://"+p.getProperty("server")+":"+p.getProperty("port")+"/"+p.getProperty("database")+"?serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8");
            config.setUsername(p.getProperty("user"));
            config.setPassword(p.getProperty("pass"));
            config.addDataSourceProperty("cachePrepStmts","true");
            config.addDataSourceProperty("prepStmtCacheSize","256");
            config.addDataSourceProperty("prepStmtCacheSqlLimit","2048");
            config.setMaximumPoolSize(50);
            config.setMinimumIdle(5);
            ds=new HikariDataSource(config);
        }catch(IOException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     * @return 
     */
    public Connection getConnection(){
        try{
            return ds.getConnection();
        }catch(SQLException|NullPointerException e){
            new UtilityClass().log(Level.SEVERE,e.getMessage(),e);
            return null;
        }
    }
}