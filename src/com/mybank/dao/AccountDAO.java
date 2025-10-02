package com.mybank.dao;

import com.mybank.model.Account;
import com.mybank.util.DBUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccountDAO {

    private static final Logger logger = Logger.getLogger(AccountDAO.class.getName());

    public Account getAccountByNumber(String accountNumber) {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return extractAccountFromResultSet(rs);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching account", e);
        }

        return null;
    }

    public Account getAccountByUserId(int userId) {
        String sql = "SELECT * FROM accounts WHERE user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return extractAccountFromResultSet(rs);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching account by user ID", e);
        }

        return null;
    }

    public boolean updateBalance(String accountNumber, BigDecimal newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, newBalance);
            ps.setString(2, accountNumber);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating account balance", e);
        }

        return false;
    }

    private Account extractAccountFromResultSet(ResultSet rs) throws SQLException {
        return new Account(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("account_number"),
                rs.getString("account_type"),
                rs.getBigDecimal("balance")
        );
    }

    public void deposit(String accNum,BigDecimal balance){
        Account acc = getAccountByNumber(accNum);
        if (acc != null) {
            BigDecimal newBalance = acc.getBalance().add(balance);
                boolean updated = updateBalance(accNum, newBalance);
                if (updated) {
                    System.out.println("Deposit successful. New balance: Rs." + newBalance);
                } else {
                    System.out.println("Deposit failed.");
                }
        } else {
            System.out.println("Account not found.");
        }
        return;
    }

    public BigDecimal getBalanceByAccountNumber(String accountNumber) {
        String sql = "SELECT balance FROM accounts WHERE account_number = ?";

        try (Connection conn = DBUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal("balance");
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching account balance", e);
        }

        return null; 
    }

}
