import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public static Connection connect() {
        Connection conn = null;
        try {
            // Update with your database URL, user, and password
            String url = "jdbc:postgresql://ep-dawn-paper-a85j3gnq.eastus2.azure.neon.tech/neondb";
            String user =  "Arulkon";
            String password = "9KPscB3RvHQd";
            System.out.println("Connecting to database.....");
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static void disconnect(Connection conn) {
        if (conn != null) {
            try {
            	System.out.println("Connection Complete");
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
