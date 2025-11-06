package com.financemanager.service;

import com.financemanager.model.Transaction;
import com.financemanager.model.TransactionType;
import com.financemanager.model.Wallet;
import com.financemanager.exception.CategoryNotFoundException;
import com.financemanager.exception.InsufficientFundsException;

import java.util.List;
import java.util.stream.Collectors;

public class TransactionService {

    public void addIncome(Wallet wallet, double amount, String category, String description)
            throws CategoryNotFoundException {

        validateAmount(amount);
        validateCategory(wallet, category);

        String transactionId = "INC_" + System.currentTimeMillis();
        Transaction transaction = new Transaction(transactionId, amount,
                TransactionType.INCOME, category, description);

        wallet.addTransaction(transaction);
        wallet.setBalance(wallet.getBalance() + amount);

        System.out.println("Доход добавлен: +" + amount + " в категорию '" + category + "'");
        checkFinancialHealth(wallet);
    }

    public void addExpense(Wallet wallet, double amount, String category, String description)
            throws CategoryNotFoundException, InsufficientFundsException {

        validateAmount(amount);
        validateCategory(wallet, category);

        if (wallet.getBalance() < amount) {
            throw new InsufficientFundsException("Недостаточно средств на счете! Доступно: " + wallet.getBalance() + ", требуется: " + amount);
        }

        String transactionId = "EXP_" + System.currentTimeMillis();
        Transaction transaction = new Transaction(transactionId, amount,
                TransactionType.EXPENSE, category, description);

        wallet.addTransaction(transaction);
        wallet.setBalance(wallet.getBalance() - amount);

        System.out.println("Расход добавлен: -" + amount + " в категорию '" + category + "'");
        checkBudgetExceeded(wallet, category, amount);
        checkFinancialHealth(wallet);
    }

    private void validateAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной!");
        }
    }

    private void validateCategory(Wallet wallet, String category) throws CategoryNotFoundException {
        if (!wallet.getCategories().containsKey(category)) {
            throw new CategoryNotFoundException("Категория '" + category + "' не найдена!");
        }
    }

    private void checkBudgetExceeded(Wallet wallet, String category, double expenseAmount) {
        double budgetLimit = wallet.getCategories().get(category).getBudgetLimit();
        if (budgetLimit > 0) {
            double categoryExpenses = getExpensesByCategory(wallet, category);
            double remaining = budgetLimit - categoryExpenses;

            if (remaining < 0) {
                System.out.println("🚨 ПРЕВЫШЕН БЮДЖЕТ в категории '" + category + "'!");
                System.out.printf("   Лимит: %.2f, Потрачено: %.2f, Превышение: %.2f%n",
                        budgetLimit, categoryExpenses, Math.abs(remaining));
            } else if (remaining < budgetLimit * 0.1) {
                System.out.println("⚠️  Внимание: В категории '" + category + "' осталось менее 10% бюджета");
                System.out.printf("   Лимит: %.2f, Потрачено: %.2f, Остаток: %.2f%n",
                        budgetLimit, categoryExpenses, remaining);
            }
        }
    }

    private void checkFinancialHealth(Wallet wallet) {
        double totalIncome = getTotalIncome(wallet);
        double totalExpenses = getTotalExpenses(wallet);

        if (totalExpenses > totalIncome) {
            System.out.println("🚨 КРИТИЧЕСКОЕ: Расходы превышают доходы!");
            System.out.printf("   Доходы: %.2f, Расходы: %.2f%n", totalIncome, totalExpenses);
        } else if (totalExpenses > totalIncome * 0.8) {
            System.out.println("⚠️  ВНИМАНИЕ: Расходы составляют более 80% от доходов");
        }

        if (wallet.getBalance() < 0) {
            System.out.println("🚨 КРИТИЧЕСКОЕ: Отрицательный баланс!");
        } else if (wallet.getBalance() < totalExpenses) {
            System.out.println("⚠️  ВНИМАНИЕ: Небольшой запас средств");
        }
    }

    public double getTotalIncome(Wallet wallet) {
        return wallet.getTransactions().stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalExpenses(Wallet wallet) {
        return wallet.getTransactions().stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getIncomeByCategory(Wallet wallet, String category) {
        return wallet.getTransactions().stream()
                .filter(t -> t.getType() == TransactionType.INCOME &&
                        t.getCategory().equals(category))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getExpensesByCategory(Wallet wallet, String category) {
        return wallet.getTransactions().stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE &&
                        t.getCategory().equals(category))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getExpensesByCategories(Wallet wallet, List<String> categories) throws CategoryNotFoundException {
        double total = 0;
        for (String category : categories) {
            if (!wallet.getCategories().containsKey(category)) {
                throw new CategoryNotFoundException("Категория '" + category + "' не найдена!");
            }
            total += getExpensesByCategory(wallet, category);
        }
        return total;
    }
}