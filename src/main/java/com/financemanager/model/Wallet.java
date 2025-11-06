package com.financemanager.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Wallet {
    private double balance;
    private List<Transaction> transactions;
    private Map<String, Category> categories;

    public Wallet() {
        this.balance = 0.0;
        this.transactions = new ArrayList<>();
        this.categories = new HashMap<>();
        initializeDefaultCategories();
    }

    private void initializeDefaultCategories() {
        // Русские категории по умолчанию
        categories.put("Еда", new Category("Еда", 0.0));
        categories.put("Транспорт", new Category("Транспорт", 0.0));
        categories.put("Развлечения", new Category("Развлечения", 0.0));
        categories.put("Здоровье", new Category("Здоровье", 0.0));
        categories.put("Образование", new Category("Образование", 0.0));
        categories.put("Одежда", new Category("Одежда", 0.0));
        categories.put("Жилье", new Category("Жилье", 0.0));
        categories.put("Связь", new Category("Связь", 0.0));
        categories.put("Переводы", new Category("Переводы", 0.0));
        categories.put("Зарплата", new Category("Зарплата", 0.0));
        categories.put("Прочее", new Category("Прочее", 0.0));
    }

    // Getters
    public double getBalance() { return balance; }
    public List<Transaction> getTransactions() { return transactions; }
    public Map<String, Category> getCategories() { return categories; }

    public void setBalance(double balance) { this.balance = balance; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
    public void setCategories(Map<String, Category> categories) { this.categories = categories; }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }
}