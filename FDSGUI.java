import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class FDSGUI {
    private static Graph graph = new Graph();
    private static JTextArea outputArea;
    private static JFrame frame;

    public static void main(String[] args) {
        frame = new JFrame("Fraud Detection System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 700); // Increased size for output area
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(255, 245, 235)); // Warm beige background

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(9, 1, 15, 15));
        buttonPanel.setBackground(new Color(255, 245, 235));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

        JButton setThresholdBtn = new JButton("Set Fraud Threshold"); // Moved to first position
        JButton addTxnBtn = new JButton("Add Transaction");
        JButton displayTxnsBtn = new JButton("Display Transactions");
        JButton bfsBtn = new JButton("Run BFS Fraud Detection");
        JButton dfsBtn = new JButton("Run DFS Cycle Detection");
        JButton loadSampleBtn = new JButton("Load Sample Dataset");
        JButton showGraphBtn = new JButton("Display Graph Connections");
        JButton showDbBtn = new JButton("Show All Transactions (DB)");
        JButton exitBtn = new JButton("Exit");

        JButton[] buttons = {
            setThresholdBtn, addTxnBtn, displayTxnsBtn, bfsBtn, dfsBtn,
            loadSampleBtn, showGraphBtn, showDbBtn, exitBtn
        };

        Color[] buttonColors = {
            new Color(255, 218, 185), // Peach Puff (Set Threshold)
            new Color(255, 182, 193), // Light Pink
            new Color(173, 216, 230), // Light Blue
            new Color(152, 251, 152), // Pale Green
            new Color(240, 230, 140), // Khaki
            new Color(221, 160, 221), // Plum
            new Color(176, 224, 230), // Powder Blue
            new Color(255, 192, 203), // Pink
            new Color(244, 164, 96)   // Sandy Brown (Exit)
        };

        Font btnFont = new Font("Segoe UI", Font.BOLD, 14);
        for (int i = 0; i < buttons.length; i++) {
            JButton btn = buttons[i];
            btn.setFont(btnFont);
            btn.setBackground(buttonColors[i]);
            btn.setForeground(Color.DARK_GRAY);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            int index = i; // for lambda access
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(buttonColors[index].darker());
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(buttonColors[index]);
                }
            });
            buttonPanel.add(btn);
        }

        // Output area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        outputArea.setBackground(new Color(250, 250, 250));
        outputArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Output"));

        // Add components to main panel
        mainPanel.add(buttonPanel, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        frame.add(mainPanel);
        frame.setVisible(true);

        // Set up graph to use our output area
        graph.setOutputHandler(message -> updateOutput(message));

        // Action Listeners
        addTxnBtn.addActionListener(e -> addTransactionDialog());
        displayTxnsBtn.addActionListener(e -> graph.displayTransactions());
        bfsBtn.addActionListener(e -> {
            String user = JOptionPane.showInputDialog(frame, "Enter user to start BFS:");
            if (user != null) graph.detectFraudBFS(user);
        });
        dfsBtn.addActionListener(e -> graph.detectFraudDFS());
        loadSampleBtn.addActionListener(e -> {
            FDS.loadSampleData(graph);
            updateOutput("Sample dataset loaded.");
        });
        setThresholdBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(frame, "Enter new fraud threshold:");
            try {
                if (input != null) {
                    double val = Double.parseDouble(input);
                    graph.setFraudThreshold(val);
                    updateOutput("Fraud threshold set to: " + val);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Invalid threshold value.");
            }
        });
        showGraphBtn.addActionListener(e -> graph.displayGraphAsEdges());
        showDbBtn.addActionListener(e -> fetchAllTransactions());
        exitBtn.addActionListener(e -> System.exit(0));
    }

    private static void addTransactionDialog() {
        JTextField senderField = new JTextField();
        JTextField receiverField = new JTextField();
        JTextField amountField = new JTextField();
        JTextField timeField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("Sender:")); panel.add(senderField);
        panel.add(new JLabel("Receiver:")); panel.add(receiverField);
        panel.add(new JLabel("Amount:")); panel.add(amountField);
        panel.add(new JLabel("Timestamp (YYYY-MM-DD HH:MM:SS):")); panel.add(timeField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Add Transaction", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String sender = senderField.getText();
                String receiver = receiverField.getText();
                double amount = Double.parseDouble(amountField.getText());
                String timestamp = timeField.getText();

                graph.addTransaction(sender, receiver, amount, timestamp);
                try {
                    DatabaseHelper.insertTransaction(sender, receiver, amount, timestamp);
                    updateOutput("Transaction added successfully: " + sender + " -> " + receiver + " | ₹" + amount);
                } catch (Exception dbEx) {
                    updateOutput("Transaction added to graph but database error: " + dbEx.getMessage());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please try again.");
            }
        }
    }

    public static void fetchAllTransactions() {
        try {
            String sql = "SELECT * FROM transactions";
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:transactions.db");
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                StringBuilder sb = new StringBuilder("Transactions from DB:\n");
                while (rs.next()) {
                    sb.append(rs.getString("sender"))
                      .append(" -> ")
                      .append(rs.getString("receiver"))
                      .append(" | ₹")
                      .append(rs.getDouble("amount"))
                      .append(" | ")
                      .append(rs.getString("timestamp"))
                      .append("\n");
                }
                updateOutput(sb.toString());
            }
        } catch (SQLException e) {
            updateOutput("DB Fetch Error: " + e.getMessage());
        }
    }
    
    public static void updateOutput(String message) {
        SwingUtilities.invokeLater(() -> {
            outputArea.append(message + "\n");
            // Auto-scroll to bottom
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }
}