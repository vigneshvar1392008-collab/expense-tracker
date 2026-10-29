import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import org.sqlite.SQLiteDataSource;
public class ExpenseTracker {
    SQLiteDataSource ds = new SQLiteDataSource();

    public ExpenseTracker() {
    connectDatabase();
    initDatabase();
    createGUI();
 }


    private void connectDatabase() {

        ds.setUrl("jdbc:sqlite:ExpensesDB.db");

        try {
            Connection conn = ds.getConnection();

            System.out.println("Database connected successfully!");

            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void initDatabase() {

    String accountTable = """
        CREATE TABLE IF NOT EXISTS accounts (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL
        )
        """;

    String expenseTable = """
        CREATE TABLE IF NOT EXISTS expenses (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            account_id INTEGER NOT NULL,
            date TEXT NOT NULL,
            description TEXT NOT NULL,
            amount REAL NOT NULL,
            FOREIGN KEY (account_id) REFERENCES accounts(id)
        )
        """;

    try {
        Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement();

        stmt.execute(accountTable);
        stmt.execute(expenseTable);

        System.out.println("Tables created successfully!");

        stmt.close();
        conn.close();

    } catch (SQLException e) {
        e.printStackTrace();
    }
}
private void addAccount(String accountName) {

    String sql = "INSERT INTO accounts (name) VALUES (?)";

    try {
        Connection conn = ds.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);

        pstmt.setString(1, accountName);
        pstmt.executeUpdate();

        System.out.println("Account added successfully!");

        pstmt.close();
        conn.close();

    } catch (SQLException e) {
        e.printStackTrace();
    }
}
private void loadAccounts() {

    String sql = "SELECT * FROM accounts";

    try {
        Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            System.out.println(
                rs.getInt("id") + " - " +
                rs.getString("name")
            );
        }

        rs.close();
        stmt.close();
        conn.close();

    } catch (SQLException e) {
        e.printStackTrace();
    }
}
private void addExpense(int accountId, String date, String description, double amount) {

    String sql = "INSERT INTO expenses " +
                 "(account_id, date, description, amount) " +
                 "VALUES (?, ?, ?, ?)";

    try {
        Connection conn = ds.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);

        pstmt.setInt(1, accountId);
        pstmt.setString(2, date);
        pstmt.setString(3, description);
        pstmt.setDouble(4, amount);

        pstmt.executeUpdate();

        System.out.println("Expense added successfully!");

        pstmt.close();
        conn.close();

    } catch (SQLException e) {
        e.printStackTrace();
    }
}
private void loadExpenses(DefaultTableModel tableModel) {
    String sql = "SELECT id, date, description, amount FROM expenses";

    try {
        Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        tableModel.setRowCount(0);

        while (rs.next()) {
            Object[] row = {
                rs.getInt("id"),
                rs.getString("date"),
                rs.getString("description"),
                rs.getDouble("amount")
            };

            tableModel.addRow(row);
        }

        rs.close();
        stmt.close();
        conn.close();

    } catch (SQLException e) {
        e.printStackTrace();
    }
}
private void deleteExpense(int expenseId) {
    String sql = "DELETE FROM expenses WHERE id = ?";

    try {
        Connection conn = ds.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);

        pstmt.setInt(1, expenseId);

        int rowsDeleted = pstmt.executeUpdate();

        System.out.println("Rows deleted: " + rowsDeleted);

        pstmt.close();
        conn.close();

    } catch (SQLException e) {
        e.printStackTrace();
    }
}
private double calculateTotal() {
    String sql = "SELECT SUM(amount) AS total FROM expenses";

    try {
        Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        double total = 0;

        if (rs.next()) {
            total = rs.getDouble("total");
        }

        rs.close();
        stmt.close();
        conn.close();

        return total;

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return 0;
}
private void loadAccountNames(JComboBox<String> accountComboBox) {

    String sql = "SELECT name FROM accounts";

    try {
        Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery(sql);

        accountComboBox.removeAllItems();

        while (rs.next()) {
            accountComboBox.addItem(rs.getString("name"));
        }

        rs.close();
        stmt.close();
        conn.close();

    } catch (SQLException e) {
        e.printStackTrace();
    }
}

private int getAccountId(String accountName) {

    String sql = "SELECT id FROM accounts WHERE name = ?";

    try {
        Connection conn = ds.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);

        pstmt.setString(1, accountName);

        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            int id = rs.getInt("id");

            rs.close();
            pstmt.close();
            conn.close();

            return id;
        }

        rs.close();
        pstmt.close();
        conn.close();

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return -1;
}
private void createGUI() {

    JFrame frame = new JFrame("Expense Tracker");

    frame.setSize(700, 500);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new FlowLayout());

    JLabel title = new JLabel("Expense Tracker");

    JTextField accountField = new JTextField(15);
    JButton addAccountButton = new JButton("Add Account");

    JComboBox<String> accountComboBox = new JComboBox<>();

    JTextField dateField = new JTextField(10);
    JTextField descriptionField = new JTextField(15);
    JTextField amountField = new JTextField(10);

    JButton addExpenseButton = new JButton("Add Expense");
    JButton deleteExpenseButton = new JButton("Delete Expense");

    JLabel totalLabel = new JLabel("Total Expense: Rs.0.0");

    // JTable
    String[] columns = {
        "ID", "Date", "Description", "Amount"
    };

    DefaultTableModel tableModel =
        new DefaultTableModel(columns, 0);

    JTable expenseTable =
        new JTable(tableModel);

    JScrollPane scrollPane =
        new JScrollPane(expenseTable);

    scrollPane.setPreferredSize(
        new Dimension(650, 200)
    );

    // Add components
    frame.add(title);

    frame.add(new JLabel("Account Name:"));
    frame.add(accountField);
    frame.add(addAccountButton);

    frame.add(new JLabel("Select Account:"));
    frame.add(accountComboBox);

    frame.add(new JLabel("Date:"));
    frame.add(dateField);

    frame.add(new JLabel("Description:"));
    frame.add(descriptionField);

    frame.add(new JLabel("Amount:"));
    frame.add(amountField);

    frame.add(addExpenseButton);
    frame.add(deleteExpenseButton);

    frame.add(totalLabel);
    frame.add(scrollPane);

    // Load existing data
    loadAccountNames(accountComboBox);
    loadExpenses(tableModel);

    totalLabel.setText(
        "Total Expense: Rs." + calculateTotal()
    );

    // Add Account
    addAccountButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {

            String accountName =
                accountField.getText();

            if (!accountName.isEmpty()) {

                addAccount(accountName);

                loadAccountNames(accountComboBox);

                accountField.setText("");

                JOptionPane.showMessageDialog(
                    frame,
                    "Account added successfully!"
                );
            }
        }
    });

    // Add Expense
    addExpenseButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {

            String date = dateField.getText();
            String description =
                descriptionField.getText();
            String amountText =
                amountField.getText();

            if (accountComboBox.getSelectedItem() == null ||
                date.isEmpty() ||
                description.isEmpty() ||
                amountText.isEmpty()) {

                JOptionPane.showMessageDialog(
                    frame,
                    "Please fill all fields!"
                );

                return;
            }

            try {

                double amount =
                    Double.parseDouble(amountText);

                if (amount <= 0) {

                    JOptionPane.showMessageDialog(
                        frame,
                        "Amount must be greater than 0!"
                    );

                    return;
                }

                String accountName =
                    accountComboBox
                        .getSelectedItem()
                        .toString();

                int accountId =
                    getAccountId(accountName);

                addExpense(
                    accountId,
                    date,
                    description,
                    amount
                );

                loadExpenses(tableModel);

                totalLabel.setText(
                    "Total Expense: Rs."
                    + calculateTotal()
                );

                dateField.setText("");
                descriptionField.setText("");
                amountField.setText("");

                JOptionPane.showMessageDialog(
                    frame,
                    "Expense added successfully!"
                );

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                    frame,
                    "Enter a valid amount!"
                );
            }
        }
    });

    // Delete Expense
    deleteExpenseButton.addActionListener(
        new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                int selectedRow =
                    expenseTable.getSelectedRow();

                if (selectedRow == -1) {

                    JOptionPane.showMessageDialog(
                        frame,
                        "Please select an expense to delete!"
                    );

                    return;
                }

                int expenseId =
                    Integer.parseInt(
                        tableModel
                            .getValueAt(selectedRow, 0)
                            .toString()
                    );

                deleteExpense(expenseId);

                loadExpenses(tableModel);

                totalLabel.setText(
                    "Total Expense: Rs."
                    + calculateTotal()
                );

                JOptionPane.showMessageDialog(
                    frame,
                    "Expense deleted successfully!"
                );
            }
        }
    );

    frame.setVisible(true);
}

   public static void main(String[] args) {

    SwingUtilities.invokeLater(() -> {
        new ExpenseTracker();
    });

}
}