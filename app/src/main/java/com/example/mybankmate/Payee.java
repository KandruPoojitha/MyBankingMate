package com.example.mybankmate;

public class Payee {
    private String id;
    private String name;
    private String accountId;

    public Payee() { }

    public Payee(String id, String name, String accountId) {
        this.id = id;
        this.name = name;
        this.accountId = accountId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
