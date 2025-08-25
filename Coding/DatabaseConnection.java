package miniProj;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public static Connection getConnection() {
        try {
            // Assuming you're using MySQL
            String url = "jdbc:mysql://localhost:3306/playlist_db";
            String user = "root";
            String password = "$Jesus25";

            // Establish connection
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}