package com.mybank;

import com.mybank.dao.*;

import java.math.BigDecimal;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        UserDAO userDAO = new UserDAO();
        AccountDAO accDAO = new AccountDAO();
        TransactionDAO txDAO = new TransactionDAO();

        while (true) {
            System.out.println("\n=== Banking Menu ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose: ");
            int choice = sc.nextInt();
            sc.nextLine(); 

            if (choice == 1) {
                System.out.print("Username: ");
                String username = sc.nextLine();
                System.out.print("Password: ");
                String password = sc.nextLine();
                System.out.print("Email: ");
                String email = sc.nextLine();

                String result = userDAO.register(username, password, email);
                System.out.println(result);

            } else if (choice == 2) {
                System.out.print("Username: ");
                String username = sc.nextLine();
                System.out.print("Password: ");
                String password = sc.nextLine();

                if (userDAO.login(username, password)) {
                    System.out.println("Login successful!");

                    while (true) {
                        System.out.println("\n--- Actions ---");
                        System.out.println("1. Check Balance");
                        System.out.println("2. Deposit Funds");
                        System.out.println("3. Transfer Funds");
                        System.out.println("4. Logout");
                        System.out.print("Choose: ");
                        int action = sc.nextInt();
                        sc.nextLine(); 

                        if (action == 1) {
                            System.out.print("Enter your account number: ");
                            String accNum = sc.nextLine();
                            BigDecimal balance = accDAO.getBalanceByAccountNumber(accNum);
                            if (balance != null) {
                                System.out.println("Your balance is: Rs." + balance);
                            } else {
                                System.out.println("Account not found.");
                            }

                        } else if (action == 2) {
                            System.out.print("Enter your account number: ");
                            String accNum = sc.nextLine();
                            System.out.print("Enter amount to deposit: ");
                            BigDecimal depositAmount = sc.nextBigDecimal();
                            sc.nextLine();

                            accDAO.deposit(accNum, depositAmount);

                        } else if (action == 3) {
                            System.out.print("From Account Number: ");
                            String fromAcc = sc.nextLine();
                            System.out.print("To Account Number: ");
                            String toAcc = sc.nextLine();
                            System.out.print("Amount to transfer: ");
                            BigDecimal amount = sc.nextBigDecimal();
                            sc.nextLine();

                            boolean success = txDAO.transfer(fromAcc, toAcc, amount);
                            if (!success) {
                                System.out.println("Transfer failed.");
                            }

                        } else if (action == 4) {
                            System.out.println("Logged out.");
                            break;
                        } else {
                            System.out.println("Invalid option.");
                        }
                    }

                } else {
                    System.out.println("Invalid login credentials.");
                }

            } else if (choice == 3) {
                System.out.println("Exiting. Goodbye!");
                break;
            } else {
                System.out.println("Invalid choice.");
            }
        }

        sc.close();
    }
}
