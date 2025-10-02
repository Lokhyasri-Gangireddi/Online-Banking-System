package com.mybank.dao;
import com.mybank.model.Account;
import com.mybank.model.Transaction;
import com.mybank.util.DBUtil;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionDAO {

    private static final Logger logger = Logger.getLogger(TransactionDAO.class.getName());

    public boolean saveTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (from_account_id, to_account_id, amount, type, created_at) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, transaction.getFromAccountId());
            ps.setInt(2, transaction.getToAccountId());
            ps.setBigDecimal(3, transaction.getAmount());
            ps.setString(4, transaction.getType());
            ps.setTimestamp(5, Timestamp.valueOf(transaction.getCreatedAt()));

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error saving transaction", e);
        }

        return false;
    }

    public List<Transaction> getTransactionsForAccount(int accountId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE from_account_id = ? OR to_account_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            ps.setInt(2, accountId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching transactions", e);
        }

        return transactions;
    }

    private Transaction extractTransactionFromResultSet(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getInt("id"),
                rs.getInt("from_account_id"),
                rs.getInt("to_account_id"),
                rs.getBigDecimal("amount"),
                rs.getString("type"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }

    public boolean transfer(String fromAccNumber, String toAccNumber, BigDecimal amount) {
    String updateBalanceSQL = "UPDATE accounts SET balance = ? WHERE id = ?";
    String insertTxSQL = "INSERT INTO transactions (from_account_id, to_account_id, amount, type, created_at) VALUES (?, ?, ?, ?, ?)";

    try (Connection conn = DBUtil.getConnection()) {
        conn.setAutoCommit(false);

        AccountDAO accDAO = new AccountDAO();
        Account fromAcc = accDAO.getAccountByNumber(fromAccNumber);
        Account toAcc = accDAO.getAccountByNumber(toAccNumber);

        if (fromAcc == null || toAcc == null) {
            System.out.println("One of the accounts does not exist.");
            return false;
        }

        if (fromAcc.getBalance().compareTo(amount) < 0) {
            System.out.println("Insufficient balance.");
            return false;
        }

        BigDecimal newFromBalance = fromAcc.getBalance().subtract(amount);
        BigDecimal newToBalance = toAcc.getBalance().add(amount);

        try (PreparedStatement psUpdate = conn.prepareStatement(updateBalanceSQL)) {
            psUpdate.setBigDecimal(1, newFromBalance);
            psUpdate.setInt(2, fromAcc.getId());
            psUpdate.executeUpdate();

            psUpdate.setBigDecimal(1, newToBalance);
            psUpdate.setInt(2, toAcc.getId());
            psUpdate.executeUpdate();
        }

        try (PreparedStatement psTx = conn.prepareStatement(insertTxSQL)) {
            psTx.setInt(1, fromAcc.getId());
            psTx.setInt(2, toAcc.getId());
            psTx.setBigDecimal(3, amount);
            psTx.setString(4, "TRANSFER");
            psTx.setTimestamp(5, Timestamp.valueOf(java.time.LocalDateTime.now()));
            psTx.executeUpdate();
        }

        conn.commit();
        System.out.println("Transfer successful!");
        return true;

    } catch (Exception e) {
        e.printStackTrace(); 
        return false;
    }
}

}
