package com.mybank.dao;

import com.mybank.util.DBUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.math.BigDecimal;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO {

    private static final Logger logger = Logger.getLogger(UserDAO.class.getName());

    public String register(String username, String password, String email) {
        String insertUserSQL = "INSERT INTO users(username, password_hash, email) VALUES(?,?,?)";
        String insertAccountSQL = "INSERT INTO accounts(user_id, account_number, account_type, balance) VALUES(?,?,?,?)";

        Connection conn = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); 

            try (PreparedStatement psUser = conn.prepareStatement(insertUserSQL, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, username);
                psUser.setString(2, BCrypt.hashpw(password, BCrypt.gensalt()));
                psUser.setString(3, email);
                psUser.executeUpdate();

                ResultSet rs = psUser.getGeneratedKeys();
                if (rs.next()) {
                    int userId = rs.getInt(1);

                    String accountNumber = generateUniqueAccountNumber(conn);

                    try (PreparedStatement psAcc = conn.prepareStatement(insertAccountSQL)) {
                        psAcc.setInt(1, userId);
                        psAcc.setString(2, accountNumber);
                        psAcc.setString(3, "Savings"); 
                        psAcc.setBigDecimal(4, new BigDecimal("0.00"));
                        psAcc.executeUpdate();
                    }

                    conn.commit(); 
                    return "User registered successfully! Your Account Number is: " + accountNumber;
                } else {
                    conn.rollback();
                    return "Registration failed: Could not retrieve user ID.";
                }
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            logger.log(Level.WARNING, "Constraint violation during registration: " + e.getMessage(), e);
            return "Registration failed: Username or email already exists.";

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL error during registration", e);
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error rolling back transaction", ex);
            }
            return "Registration failed due to a database error.";

        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to reset auto-commit", e);
            }
        }
    }

    public boolean login(String username, String password) {
        String sql = "SELECT password_hash FROM users WHERE username=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hash = rs.getString("password_hash");
                return BCrypt.checkpw(password, hash);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error during login", e);
        }
        return false;
    }

    private String generateAccountNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder("ACC");
        for (int i = 0; i < 7; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String generateUniqueAccountNumber(Connection conn) throws SQLException {
        String accountNumber;
        boolean exists;

        do {
            accountNumber = generateAccountNumber();
            String checkSQL = "SELECT 1 FROM accounts WHERE account_number = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {
                checkStmt.setString(1, accountNumber);
                ResultSet rs = checkStmt.executeQuery();
                exists = rs.next();
            }
        } while (exists);

        return accountNumber;
    }
}
