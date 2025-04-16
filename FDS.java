import java.util.Scanner;

public class FDS {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Graph graph = new Graph();

        // Prompt the user to set a fraud threshold or use default
        System.out.print("Enter fraud threshold (default is ₹10000): ");
        String input = scanner.nextLine();
        if (!input.isEmpty()) {
            try {
                double threshold = Double.parseDouble(input);
                graph.setFraudThreshold(threshold);
            } catch (NumberFormatException e) {
                System.out.println("⚠ Invalid input. Using default ₹10000.");
            }
        }

        // Main menu loop
        while (true) {
            System.out.println("\n===== Fraud Detection Menu =====");
            System.out.println("1. Add Transaction");
            System.out.println("2. Display Transactions");
            System.out.println("3. Run BFS Fraud Detection");
            System.out.println("4. Run DFS Cycle Detection");
            System.out.println("5. Load Sample Fraud Dataset");
            System.out.println("6. Set Fraud Threshold");
            System.out.println("7. Display Graph Connections");
            System.out.println("8. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    // Manual transaction input
                    System.out.print("Enter Sender: ");
                    String sender = scanner.nextLine();
                    System.out.print("Enter Receiver: ");
                    String receiver = scanner.nextLine();
                    System.out.print("Enter Amount: ");
                    double amount = scanner.nextDouble();
                    scanner.nextLine(); // consume newline
                    System.out.print("Enter Timestamp (YYYY-MM-DD HH:MM:SS): ");
                    String timestamp = scanner.nextLine();
                    graph.addTransaction(sender, receiver, amount, timestamp);
                    System.out.println("Transaction added.");
                    break;

                case 2:
                    // View all recorded transactions
                    System.out.println("Transaction History:");
                    graph.displayTransactions();
                    break;

                case 3:
                    // Run BFS from a specified user to check for high-value suspicious paths
                    System.out.print("Enter user to start BFS fraud check: ");
                    String userBFS = scanner.nextLine();
                    graph.detectFraudBFS(userBFS);
                    break;

                case 4:
                    // DFS cycle detection — useful for identifying circular money movement (possible laundering)
                    graph.detectFraudDFS();
                    break;

                case 5:
                    // Add pre-defined test data with suspicious activity
                    loadSampleData(graph);
                    break;

                case 6:
                    // Update the fraud threshold manually
                    System.out.print("Enter new fraud threshold (₹): ");
                    double newThreshold = scanner.nextDouble();
                    scanner.nextLine(); // consume newline
                    graph.setFraudThreshold(newThreshold);
                    break;

                case 7:
                    // Print graph as edges to visualize the network of transactions
                    graph.displayGraphAsEdges();
                    break;

                case 8:
                    // Exit the program
                    System.out.println("Exiting...");
                    scanner.close();
                    return;

                default:
                    // Catch invalid menu options
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // Sample data for testing fraud detection algorithms (includes cycles + high-value txns)
    public static void loadSampleData(Graph graph) {
        graph.addTransaction("A", "B", 2000, "2025-04-01 10:00:00");
        graph.addTransaction("B", "C", 3000, "2025-04-01 10:01:00");
        graph.addTransaction("C", "D", 4000, "2025-04-01 10:02:00");
        graph.addTransaction("D", "A", 5000, "2025-04-01 10:03:00"); // cycle

        graph.addTransaction("X", "Y", 15000, "2025-04-01 11:00:00"); // high-value
        graph.addTransaction("X", "Y", 10000, "2025-04-01 11:00:00"); // high-value
        graph.addTransaction("P", "Q", 700, "2025-04-01 12:00:00");   // normal

        System.out.println("Sample dataset loaded.");
    }
}
