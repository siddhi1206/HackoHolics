import java.sql.*;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:transactions.db";

    public static void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS transactions (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                         "sender TEXT," +
                         "receiver TEXT," +
                         "amount REAL," +
                         "timestamp TEXT)";
            stmt.execute(sql);
            System.out.println(" Database initialized.");
        } catch (SQLException e) {
            System.out.println(" Database error: " + e.getMessage());
        }
    }

    public static void insertTransaction(String sender, String receiver, double amount, String timestamp) {
        String sql = "INSERT INTO transactions (sender, receiver, amount, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sender);
            pstmt.setString(2, receiver);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, timestamp);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(" Insert error: " + e.getMessage());
        }
    }

    public static void showAllTransactions() {
        String sql = "SELECT * FROM transactions";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println(" Transactions from DB:");
            while (rs.next()) {
                System.out.printf("%s -> %s | ₹%.2f | %s%n",
                        rs.getString("sender"),
                        rs.getString("receiver"),
                        rs.getDouble("amount"),
                        rs.getString("timestamp"));
            }
        } catch (SQLException e) {
            System.out.println(" Fetch error: " + e.getMessage());
        }
    }
}
