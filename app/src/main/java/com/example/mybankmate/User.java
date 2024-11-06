package com.example.mybankmate;

public class User {
    private String email;
    private String accountNumber;

    public User() { }

    public User(String email, String accountNumber) {
        this.email = email;
        this.accountNumber = accountNumber;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
}
