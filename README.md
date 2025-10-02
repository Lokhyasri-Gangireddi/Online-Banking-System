Online Banking System (Java)

A simple, console-based online banking system built in Java using JDBC, allowing users to register, log in, deposit, check balances, and transfer funds securely.

Features

-  User Registration & Login
  - Secure password hashing with BCrypt
  - Unique account number generation

-  Account Management
  - View current account balance
  - Deposit funds
  - Transfer funds to other accounts

-  Security
  - Passwords are hashed (not stored in plain text)
  - Transactions use JDBC transactions to ensure consistency

---

Tech Stack
- Java (JDK 8+)
- JDBC (Java Database Connectivity)
- MySQL / SQLite
- BCrypt (`org.mindrot.jbcrypt.BCrypt`) for password hashing

---

Database Schema

`users` Table
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255),
    email VARCHAR(150)
);

`accounts` Table
CREATE TABLE accounts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    account_number VARCHAR(20) UNIQUE,
    account_type VARCHAR(20),
    balance DECIMAL(15,2),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

`transactions` Table
CREATE TABLE transactions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    from_account_id INT,
    to_account_id INT,
    amount DECIMAL(15,2),
    type VARCHAR(20),
    created_at DATETIME,
    FOREIGN KEY (from_account_id) REFERENCES accounts(id),
    FOREIGN KEY (to_account_id) REFERENCES accounts(id)
);


How to Run

Clone the repository:

git clone https://github.com/Lokhyasri-Gangireddi/online-banking-system.git
cd online-banking-system


Set up your database:

Use MySQL, SQLite, or another JDBC-compatible RDBMS

Run the SQL schema provided above

Configure DB connection in DBUtil.java:
private static final String URL = "jdbc:mysql://localhost:3306/your_db";
private static final String USER = "your_username";
private static final String PASSWORD = "your_password";


Build & Run the project:

Compile and run using an IDE

Or from terminal:

javac com/mybank/Main.java
java com.mybank.Main



