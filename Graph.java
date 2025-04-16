import java.util.*;

public class Graph {
    private LinkedHashMap<String, List<Transaction>> adjList;
    private double fraudThreshold = 10000;
    private OutputHandler outputHandler;

    // Interface for handling output
    public interface OutputHandler {
        void handleOutput(String message);
    }

    public Graph() {
        this.adjList = new LinkedHashMap<>();
    }

    // Set the output handler to redirect console output to GUI
    public void setOutputHandler(OutputHandler handler) {
        this.outputHandler = handler;
    }

    // Helper method to output messages
    private void output(String message) {
        if (outputHandler != null) {
            outputHandler.handleOutput(message);
        } else {
            System.out.println(message); // Fallback to console
        }
    }

    public void addTransaction(String sender, String receiver, double amount, String timestamp) {
        Transaction transaction = new Transaction(sender, receiver, amount, timestamp);
        adjList.putIfAbsent(sender, new ArrayList<>());
        adjList.putIfAbsent(receiver, new ArrayList<>());
        adjList.get(sender).add(transaction);

        if (amount > fraudThreshold) {
            output("ALERT: Suspicious transaction detected! " + transaction);
        }
    }

    public void displayTransactions() {
        for (String user : adjList.keySet()) {
            output("User: " + user);
            for (Transaction t : adjList.get(user)) {
                output("   " + t);
            }
        }
    }

    public void detectFraudBFS(String startUser) {
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        boolean fraudFound = false;

        queue.add(startUser);
        visited.add(startUser);

        while (!queue.isEmpty()) {
            String user = queue.poll();
            if (adjList.containsKey(user)) {
                for (Transaction t : adjList.get(user)) {
                    if (t.amount > fraudThreshold) {
                        output("⚠ Fraud Alert (BFS): Suspicious Transaction - " + t);
                        fraudFound = true;
                    }

                    if (!visited.contains(t.receiver)) {
                        queue.add(t.receiver);
                        visited.add(t.receiver);
                    }
                }
            }
        }

        if (!fraudFound) {
            output("No suspicious transactions found in BFS from user " + startUser + ".");
        }
    }

    public boolean detectCycleDFS(String user, Set<String> visited, Set<String> stack, List<String> path) {
        if (stack.contains(user)) {
            path.add(user); // add the node where the cycle was found
            output("Fraud Alert (DFS): Cycle detected in transactions!");
            
            // Print cycle path
            int start = path.indexOf(user);
            StringBuilder cyclePath = new StringBuilder("Cycle Path: ");
            for (int i = start; i < path.size(); i++) {
                cyclePath.append(path.get(i));
                if (i < path.size() - 1) cyclePath.append(" -> ");
            }
            output(cyclePath.toString());
            return true;
        }

        if (visited.contains(user)) return false;

        visited.add(user);
        stack.add(user);
        path.add(user);

        if (adjList.containsKey(user)) {
            for (Transaction t : adjList.get(user)) {
                if (detectCycleDFS(t.receiver, visited, stack, path)) {
                    return true;
                }
            }
        }

        stack.remove(user);
        path.remove(path.size() - 1); // backtrack
        return false;
    }

    public void detectFraudDFS() {
        Set<String> visited = new HashSet<>();
        Set<String> stack = new HashSet<>();

        for (String user : adjList.keySet()) {
            if (!visited.contains(user)) {
                List<String> path = new ArrayList<>();
                if (detectCycleDFS(user, visited, stack, path)) {
                    return;
                }
            }
        }

        output("No suspicious cycles detected.");
    }

    public void displayGraphAsEdges() {
        output("\nTransaction Graph (Text View):");
        for (String user : adjList.keySet()) {
            for (Transaction t : adjList.get(user)) {
                output(user + " -> " + t.receiver + " | ₹" + t.amount);
            }
        }
    }

    public void setFraudThreshold(double newThreshold) {
        this.fraudThreshold = newThreshold;
        output("Fraud threshold updated to ₹" + newThreshold);
    }
}
