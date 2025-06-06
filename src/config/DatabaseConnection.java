package config;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final Dotenv dotenv = Dotenv.load();

    private static final String URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");

    public static Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Kết nối MySQL thành công!");
            return connection;
        } catch (SQLException e) {
            System.out.println("Kết nối MySQL không thành công!");
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}