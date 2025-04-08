public class Transaction {
    String sender;
    String receiver;
    double amount;
    String timestamp;

    public Transaction(String sender, String receiver, double amount, String timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return sender + " -> " + receiver + " | Amount: " + amount + " | Time: " + timestamp;
    }
}