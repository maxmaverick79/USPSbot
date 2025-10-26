package org.example;
import java.sql.*;
import java.time.LocalDateTime;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1212";

    // Prevent instantiation
    private DatabaseConnection() {}

    /**
     * Get a database connection
     */
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Insert a load into the database if it doesn't already exist.
     *
     * @return true if inserted successfully, false otherwise
     */
    public static boolean insertLoad(
            int loadId,
            int totalMiles,
            String pickup,
            String delivery,
            String originLocationCity,
            String originLocationState,
            String destinationLocationCity,
            String destinationLocationState
    ) {
        if (checkLoadIdExists(loadId)) {
            System.out.println("Error: Load with ID " + loadId + " already exists. Insertion aborted.-- ");
            System.out.print(LocalDateTime.now());
            return false;
        }

        String sql = """
            INSERT INTO loadList 
            (loadId, totalMiles, pickup, delivery, originLocationCity, originLocationState, destinationLocationCity, destinationLocationState)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, loadId);
            stmt.setInt(2, totalMiles);
            stmt.setString(3, pickup);
            stmt.setString(4, delivery);
            stmt.setString(5, originLocationCity);
            stmt.setString(6, originLocationState);
            stmt.setString(7, destinationLocationCity);
            stmt.setString(8, destinationLocationState);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("✅ Load inserted successfully! loadId=" + loadId+" -- ");
                System.out.print(LocalDateTime.now());

                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ SQL error during load insertion: " + e.getMessage());
        }
        return false;
    }

    /**
     * Check if a loadId already exists in the database.
     */
    public static boolean checkLoadIdExists(int loadId) {
        String sql = "SELECT COUNT(*) FROM loadList WHERE loadId = ?";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, loadId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ SQL error checking load ID existence: " + e.getMessage());
        }
        return false;
    }
}