package com.financemanager.model;

public class User {
    private String username;
    private String password;
    private Wallet wallet;
    private String secretQuestion;
    private String secretAnswer;

    // Конструктор по умолчанию для Gson
    public User() {
        this.wallet = new Wallet();
        this.secretQuestion = "";
        this.secretAnswer = "";
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.wallet = new Wallet();
        this.secretQuestion = "";
        this.secretAnswer = "";
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Wallet getWallet() { return wallet; }
    public String getSecretQuestion() { return secretQuestion; }
    public String getSecretAnswer() { return secretAnswer; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setWallet(Wallet wallet) { this.wallet = wallet; }
    public void setSecretQuestion(String secretQuestion) { this.secretQuestion = secretQuestion; }
    public void setSecretAnswer(String secretAnswer) { this.secretAnswer = secretAnswer; }
}