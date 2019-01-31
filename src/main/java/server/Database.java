package server;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class Database {
    private String  url = "jdbc:mysql://localhost/ftp";
    private String username = "root";
    private String password = "12345";

    private Connection connection;
    private Statement statement;

    public Database(){
        try {
            Class.forName("com.MySQL.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection(url, username, password);
            statement = connection.createStatement();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
