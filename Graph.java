import java.util.*;

public class Graph {
	// The graph structure: stores each user and their list of outgoing transactions
    	private LinkedHashMap<String, List<Transaction>> adjList;
   	private double fraudThreshold = 10000;

	// Constructor
    	public Graph() {
    		this.adjList = new LinkedHashMap<>();
    	}

	// Adds a new transaction to the graph
   	public void addTransaction(String sender, String receiver, double amount, String timestamp) {
        	Transaction transaction = new Transaction(sender, receiver, amount, timestamp);
		
		// Ensure both sender and receiver exist in the graph
        	adjList.putIfAbsent(sender, new ArrayList<>());
        	adjList.putIfAbsent(receiver, new ArrayList<>());
		
		// Add transaction to the sender's list
        	adjList.get(sender).add(transaction);

		// Printing alert if amount exceeds fraud threshold
        	if (amount > fraudThreshold) {
            		System.out.println("ALERT: Suspicious transaction detected! " + transaction);
        	}
    	}

	// Displays all transactions grouped by user
    	public void displayTransactions() {
        	for (String user : adjList.keySet()) {
           		System.out.println("User: " + user);
            		for (Transaction t : adjList.get(user)) {
                		System.out.println("   " + t);
            		}
        	}
   	}
	// Performs BFS from a given user to find suspicious transactions
	public void detectFraudBFS(String startUser) {
    		Set<String> visited = new HashSet<>();
    		Queue<String> queue = new LinkedList<>();
    		boolean fraudFound = false;

   		queue.add(startUser);
    		visited.add(startUser);

    		while (!queue.isEmpty()) {
        		String user = queue.poll();

			// Traverse all transactions from the current user
        		if (adjList.containsKey(user)) {
            			for (Transaction t : adjList.get(user)) {

					// If transaction is suspicious, flag it
                			if (t.amount > fraudThreshold) {
    						System.out.println("Fraud Alert (BFS): Suspicious Transaction - " + t);
    						fraudFound = true;
					}

					// Add the receiver to the queue if not visited
					if (!visited.contains(t.receiver)) {
   						queue.add(t.receiver);
    						visited.add(t.receiver);
					}

            			}
        		}
    		}

	 	// If no suspicious transactions were found
    		if (!fraudFound) {
        		System.out.println("No suspicious transactions found in BFS from user " + startUser + ".");
    		}
	}

	// DFS helper method to detect cycles and print the cycle path
	public boolean detectCycleDFS(String user, Set<String> visited, Set<String> stack, List<String> path) {
		// Cycle detected: user is already on the stack
    		if (stack.contains(user)) {
        		path.add(user); // add the node where the cycle was found
        		System.out.println("Fraud Alert (DFS): Cycle detected in transactions!");
        
        		// Print cycle path
        		int start = path.indexOf(user);
        		System.out.print("Cycle Path: ");
        		for (int i = start; i < path.size(); i++) {
            			System.out.print(path.get(i));
            			if (i < path.size() - 1) System.out.print(" -> ");
        		}
        		System.out.println();
        		return true;
    		}

		// Skip already processed users
    		if (visited.contains(user)) return false;
	
		// Mark as visited and part of current DFS stack
    		visited.add(user);
    		stack.add(user);
    		path.add(user);

		// Recurse on each receiver (child)
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

	// Starts DFS from all users to detect cycles
    	public void detectFraudDFS() {
    		Set<String> visited = new HashSet<>();
    		Set<String> stack = new HashSet<>();

    		for (String user : adjList.keySet()) {
       		 	if (!visited.contains(user)) {
            			List<String> path = new ArrayList<>();
            			if (detectCycleDFS(user, visited, stack, path)) {
                			return; // Stop after the first cycle is found
            			}
        		}
    		}

    		System.out.println("No suspicious cycles detected.");
	}

	// Displays a text-based view of the graph's edges (who sent money to whom)
	public void displayGraphAsEdges() {
    		System.out.println("\nTransaction Graph (Text View):");
    		for (String user : adjList.keySet()) {
        		for (Transaction t : adjList.get(user)) {
            			System.out.println(user + " -> " + t.receiver + " | Rs." + t.amount);
        		}
    		}
	}

	// Allows updating the fraud threshold
	public void setFraudThreshold(double newThreshold) {
    		this.fraudThreshold = newThreshold;
    		System.out.println("Fraud threshold updated to Rs." + newThreshold);
	}

	// Runs BFS fraud detection from all users in the graph
	public void detectFraudBFSAll() {
    		System.out.println("Running BFS fraud check for all users:");
    		for (String user : adjList.keySet()) {
        		System.out.println("\nFrom user: " + user);
        		detectFraudBFS(user);
    		}
	}

}
