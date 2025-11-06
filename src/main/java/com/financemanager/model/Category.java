package com.financemanager.model;

public class Category {
    private String name;
    private double budgetLimit;

    public Category(String name, double budgetLimit) {
        this.name = name;
        this.budgetLimit = budgetLimit;
    }

    // Getters and Setters
    public String getName() { return name; }
    public double getBudgetLimit() { return budgetLimit; }
    public void setName(String name) { this.name = name; }
    public void setBudgetLimit(double budgetLimit) { this.budgetLimit = budgetLimit; }
}