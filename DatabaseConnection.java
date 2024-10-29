import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://ep-dawn-paper-a85j3gnq.eastus2.azure.neon.tech/neondb";
    private static final String USER = "Arulkon";
    private static final String PASSWORD = "9KPscB3RvHQd";
    private static final String SSL_MODE = "require";

    public static Connection connect() {
        try {
            System.out.println("Connected to the database.");
            return DriverManager.getConnection(URL + "?sslmode=" + SSL_MODE, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database.");
            e.printStackTrace();
            return null;
        }
    }

    public static void disconnect(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Disconnected from the database.");
            } catch (SQLException e) {
                System.out.println("Failed to disconnect from the database.");
                e.printStackTrace();
            }
        }
    }
}




