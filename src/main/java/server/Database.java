package server;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Database {
    private Dotenv dotenv = Dotenv.configure().directory("./").load();

    private String url = dotenv.get("URL");
    private String username = dotenv.get("USERNAME");
    private String password = dotenv.get("PASSWORD");

    private Connection connection;
    private Statement statement;

    public Database() {
        try {
            Class.forName("com.MySQL.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection(url, username, password);
            statement = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
