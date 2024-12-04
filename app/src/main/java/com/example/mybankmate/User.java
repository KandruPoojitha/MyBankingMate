package com.example.mybankmate;

public class User {
    private String uid;
    private String email;
    private String checkingAccountNumber;
    private String savingsAccountNumber;

    public User() {}

    public User(String uid, String email, String checkingAccountNumber, String savingsAccountNumber) {
        this.uid = uid;
        this.email = email;
        this.checkingAccountNumber = checkingAccountNumber;
        this.savingsAccountNumber = savingsAccountNumber;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCheckingAccountNumber() {
        return checkingAccountNumber;
    }

    public void setCheckingAccountNumber(String checkingAccountNumber) {
        this.checkingAccountNumber = checkingAccountNumber;
    }

    public String getSavingsAccountNumber() {
        return savingsAccountNumber;
    }

    public void setSavingsAccountNumber(String savingsAccountNumber) {
        this.savingsAccountNumber = savingsAccountNumber;
    }
}
