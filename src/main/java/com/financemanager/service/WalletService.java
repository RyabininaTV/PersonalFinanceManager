package com.financemanager.service;

import com.financemanager.model.User;
import com.financemanager.model.Wallet;
import com.financemanager.model.Category;
import com.financemanager.model.Transaction;
import com.financemanager.exception.CategoryNotFoundException;
import com.financemanager.exception.InsufficientFundsException;
import com.financemanager.exception.UserNotFoundException;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class WalletService {
    private AuthService authService;
    private TransactionService transactionService;
    private FileService fileService;

    public WalletService(AuthService authService, TransactionService transactionService, FileService fileService) {
        this.authService = authService;
        this.transactionService = transactionService;
        this.fileService = fileService;
    }

    public void showStatistics() {
        User currentUser = authService.getCurrentUser();
        Wallet wallet = currentUser.getWallet();

        double totalIncome = transactionService.getTotalIncome(wallet);
        double totalExpenses = transactionService.getTotalExpenses(wallet);
        double balance = wallet.getBalance();

        System.out.println("\n=== ОБЩАЯ СТАТИСТИКА ===");
        System.out.printf("Общий доход: %.2f%n", totalIncome);
        System.out.printf("Общие расходы: %.2f%n", totalExpenses);
        System.out.printf("Текущий баланс: %.2f%n", balance);

        if (totalExpenses > totalIncome) {
            System.out.println("⚠️  ВНИМАНИЕ: Расходы превышают доходы!");
        }
        if (balance < 0) {
            System.out.println("⚠️  ВНИМАНИЕ: Отрицательный баланс!");
        }
    }

    public void showDetailedStatistics() {
        User currentUser = authService.getCurrentUser();
        Wallet wallet = currentUser.getWallet();

        double totalIncome = transactionService.getTotalIncome(wallet);
        double totalExpenses = transactionService.getTotalExpenses(wallet);

        System.out.println("\n=== ДЕТАЛЬНАЯ СТАТИСТИКА ===");
        System.out.printf("Общий доход: %.2f%n", totalIncome);
        System.out.printf("Общие расходы: %.2f%n", totalExpenses);
        System.out.printf("Текущий баланс: %.2f%n", wallet.getBalance());

        boolean hasIncome = false;
        for (String category : wallet.getCategories().keySet()) {
            double income = transactionService.getIncomeByCategory(wallet, category);
            if (income > 0) {
                System.out.printf("%s: %.2f%n", category, income);
                hasIncome = true;
            }
        }
        if (!hasIncome) {
            System.out.println("Нет данных о доходах");
        }

        System.out.println("\n--- БЮДЖЕТ ПО КАТЕГОРИЯМ ---");
        for (Category category : wallet.getCategories().values()) {
            double expenses = transactionService.getExpensesByCategory(wallet, category.getName());
            double budgetLimit = category.getBudgetLimit();
            double remaining = budgetLimit - expenses;

            if (budgetLimit > 0) {
                System.out.printf("%s: Лимит: %.2f, Потрачено: %.2f, Остаток: %.2f%n",
                        category.getName(), budgetLimit, expenses, remaining);

                if (remaining < 0) {
                    System.out.printf("   🚨 ПРЕВЫШЕН БЮДЖЕТ на: %.2f%n", Math.abs(remaining));
                } else if (remaining < budgetLimit * 0.1) {
                    System.out.printf("   ⚠️  Внимание: Осталось менее 10%% бюджета%n");
                }
            }
        }

        System.out.println("\n--- ФИНАНСОВОЕ ЗДОРОВЬЕ ---");
        if (totalExpenses > totalIncome) {
            System.out.println("🚨 КРИТИЧЕСКОЕ: Расходы превышают доходы!");
        } else if (totalExpenses > totalIncome * 0.8) {
            System.out.println("⚠️  ВНИМАНИЕ: Расходы составляют более 80% от доходов");
        } else {
            System.out.println("✅ ХОРОШО: Финансы в норме");
        }

        if (wallet.getBalance() < 0) {
            System.out.println("🚨 КРИТИЧЕСКОЕ: Отрицательный баланс!");
        } else if (wallet.getBalance() < totalExpenses) {
            System.out.println("⚠️  ВНИМАНИЕ: Небольшой запас средств");
        }
    }

    public void showStatisticsForCategories(List<String> categories) throws CategoryNotFoundException {
        User currentUser = authService.getCurrentUser();
        Wallet wallet = currentUser.getWallet();

        for (String category : categories) {
            if (!wallet.getCategories().containsKey(category)) {
                throw new CategoryNotFoundException("Категория '" + category + "' не найдена!");
            }
        }

        System.out.println("\n=== СТАТИСТИКА ПО ВЫБРАННЫМ КАТЕГОРИЯМ ===");

        double totalIncome = 0;
        double totalExpenses = 0;

        for (String category : categories) {
            double income = transactionService.getIncomeByCategory(wallet, category);
            double expenses = transactionService.getExpensesByCategory(wallet, category);
            double budgetLimit = wallet.getCategories().get(category).getBudgetLimit();
            double remaining = budgetLimit - expenses;

            System.out.printf("\n--- %s ---%n", category);
            System.out.printf("Доходы: %.2f%n", income);
            System.out.printf("Расходы: %.2f%n", expenses);
            if (budgetLimit > 0) {
                System.out.printf("Лимит бюджета: %.2f%n", budgetLimit);
                System.out.printf("Остаток бюджета: %.2f%n", remaining);

                if (remaining < 0) {
                    System.out.printf("🚨 Превышение: %.2f%n", Math.abs(remaining));
                }
            }

            totalIncome += income;
            totalExpenses += expenses;
        }

        System.out.printf("\n--- ИТОГО ПО ВЫБРАННЫМ КАТЕГОРИЯМ ---%n");
        System.out.printf("Общий доход: %.2f%n", totalIncome);
        System.out.printf("Общие расходы: %.2f%n", totalExpenses);
        System.out.printf("Чистый результат: %.2f%n", totalIncome - totalExpenses);
    }

    public void exportStatisticsToFile(String filename) {
        try {
            User currentUser = authService.getCurrentUser();
            Wallet wallet = currentUser.getWallet();

            double totalIncome = transactionService.getTotalIncome(wallet);
            double totalExpenses = transactionService.getTotalExpenses(wallet);

            StringBuilder statistics = new StringBuilder();
            statistics.append("=== ЭКСПОРТ СТАТИСТИКИ ===\n");
            statistics.append("Пользователь: ").append(currentUser.getUsername()).append("\n");
            statistics.append(String.format("Общий доход: %.2f%n", totalIncome));
            statistics.append(String.format("Общие расходы: %.2f%n", totalExpenses));
            statistics.append(String.format("Текущий баланс: %.2f%n", wallet.getBalance()));

            statistics.append("\n--- ДОХОДЫ ПО КАТЕГОРИЯМ ---\n");
            for (String category : wallet.getCategories().keySet()) {
                double income = transactionService.getIncomeByCategory(wallet, category);
                if (income > 0) {
                    statistics.append(String.format("%s: %.2f%n", category, income));
                }
            }

            statistics.append("\n--- РАСХОДЫ И БЮДЖЕТЫ ---\n");
            for (Category category : wallet.getCategories().values()) {
                double expenses = transactionService.getExpensesByCategory(wallet, category.getName());
                double budgetLimit = category.getBudgetLimit();
                if (expenses > 0 || budgetLimit > 0) {
                    statistics.append(String.format("%s: Лимит: %.2f, Потрачено: %.2f, Остаток: %.2f%n",
                            category.getName(), budgetLimit, expenses, budgetLimit - expenses));
                }
            }

            fileService.exportStatisticsToFile(statistics.toString(), filename);
            System.out.println("Статистика успешно экспортирована в файл: " + filename);
        } catch (Exception e) {
            System.out.println("Ошибка при экспорте статистики: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void createCategory(String categoryName, double budgetLimit) {
        User currentUser = authService.getCurrentUser();
        Wallet wallet = currentUser.getWallet();

        if (wallet.getCategories().containsKey(categoryName)) {
            System.out.println("Категория '" + categoryName + "' уже существует!");
            return;
        }

        Category newCategory = new Category(categoryName, budgetLimit);
        wallet.getCategories().put(categoryName, newCategory);

        fileService.saveWallet(wallet, currentUser.getUsername());

        System.out.println("Категория '" + categoryName + "' успешно создана!");
        if (budgetLimit > 0) {
            System.out.println("Установлен лимит бюджета: " + budgetLimit);
        }
    }

    public void setBudgetLimit(String categoryName, double budgetLimit) throws CategoryNotFoundException {
        User currentUser = authService.getCurrentUser();
        Wallet wallet = currentUser.getWallet();

        if (!wallet.getCategories().containsKey(categoryName)) {
            throw new CategoryNotFoundException("Категория '" + categoryName + "' не найдена!");
        }

        Category category = wallet.getCategories().get(categoryName);
        category.setBudgetLimit(budgetLimit);

        fileService.saveWallet(wallet, currentUser.getUsername());

        System.out.println("Лимит бюджета для категории '" + categoryName + "' установлен: " + budgetLimit);

        double currentExpenses = transactionService.getExpensesByCategory(wallet, categoryName);
        if (budgetLimit > 0 && currentExpenses > budgetLimit) {
            System.out.println("⚠️  ВНИМАНИЕ: Текущие расходы уже превышают новый лимит!");
            System.out.printf("   Расходы: %.2f, Лимит: %.2f%n", currentExpenses, budgetLimit);
        }
    }

    public void transferToUser(String targetUsername, double amount, String description)
            throws UserNotFoundException, InsufficientFundsException, CategoryNotFoundException {

        User currentUser = authService.getCurrentUser();
        User targetUser = authService.getUser(targetUsername);

        if (targetUser == null) {
            throw new UserNotFoundException("Пользователь '" + targetUsername + "' не найден!");
        }

        if (currentUser.getWallet().getBalance() < amount) {
            throw new InsufficientFundsException("Недостаточно средств для перевода!");
        }

        transactionService.addExpense(currentUser.getWallet(), amount, "Переводы",
                "Перевод пользователю: " + targetUsername + ". " + description);

        transactionService.addIncome(targetUser.getWallet(), amount, "Переводы",
                "Перевод от пользователя: " + currentUser.getUsername() + ". " + description);

        fileService.saveWallet(currentUser.getWallet(), currentUser.getUsername());
        fileService.saveWallet(targetUser.getWallet(), targetUsername);

        System.out.println("Перевод успешно выполнен!");
        System.out.printf("Переведено: %.2f пользователю: %s%n", amount, targetUsername);
    }

    public void showStatisticsForPeriod(String startDate, String endDate) {
        try {
            User currentUser = authService.getCurrentUser();
            Wallet wallet = currentUser.getWallet();

            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");

            List<Transaction> periodTransactions = wallet.getTransactions().stream()
                    .filter(t -> {
                        LocalDateTime transactionDate = LocalDateTime.parse(t.getDate(), formatter);
                        return !transactionDate.isBefore(start) && !transactionDate.isAfter(end);
                    })
                    .toList();

            if (periodTransactions.isEmpty()) {
                System.out.println("Нет операций за указанный период: " + startDate + " - " + endDate);
                return;
            }

            double periodIncome = periodTransactions.stream()
                    .filter(t -> t.getType() == com.financemanager.model.TransactionType.INCOME)
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            double periodExpenses = periodTransactions.stream()
                    .filter(t -> t.getType() == com.financemanager.model.TransactionType.EXPENSE)
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            System.out.println("\n=== СТАТИСТИКА ЗА ПЕРИОД " + startDate + " - " + endDate + " ===");
            System.out.printf("Доходы за период: %.2f%n", periodIncome);
            System.out.printf("Расходы за период: %.2f%n", periodExpenses);
            System.out.printf("Баланс за период: %.2f%n", periodIncome - periodExpenses);
            System.out.printf("Количество операций: %d%n", periodTransactions.size());

        } catch (Exception e) {
            System.out.println("Ошибка при расчете статистики за период: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void editCategory(String oldName, String newName, double newBudgetLimit) throws CategoryNotFoundException {
        User currentUser = authService.getCurrentUser();
        Wallet wallet = currentUser.getWallet();

        if (!wallet.getCategories().containsKey(oldName)) {
            throw new CategoryNotFoundException("Категория '" + oldName + "' не найдена!");
        }

        if (!oldName.equals(newName) && wallet.getCategories().containsKey(newName)) {
            throw new IllegalArgumentException("Категория '" + newName + "' уже существует!");
        }

        Category category = wallet.getCategories().get(oldName);
        if (!oldName.equals(newName)) {
            wallet.getCategories().remove(oldName);
            category.setName(newName);
            wallet.getCategories().put(newName, category);

            wallet.getTransactions().stream()
                    .filter(t -> t.getCategory().equals(oldName))
                    .forEach(t -> t.setCategory(newName));
        }

        category.setBudgetLimit(newBudgetLimit);
        fileService.saveWallet(wallet, currentUser.getUsername());

        System.out.println("Категория успешно обновлена!");
        System.out.println("Новое название: " + newName);
        System.out.println("Новый лимит: " + newBudgetLimit);
    }

    public void deleteCategory(String categoryName) throws CategoryNotFoundException {
        User currentUser = authService.getCurrentUser();
        Wallet wallet = currentUser.getWallet();

        if (!wallet.getCategories().containsKey(categoryName)) {
            throw new CategoryNotFoundException("Категория '" + categoryName + "' не найдена!");
        }

        long transactionsCount = wallet.getTransactions().stream()
                .filter(t -> t.getCategory().equals(categoryName))
                .count();

        if (transactionsCount > 0) {
            System.out.println("Внимание: В категории '" + categoryName + "' есть " + transactionsCount + " транзакций.");
            System.out.println("Они будут перемещены в категорию 'Прочее'.");

            wallet.getTransactions().stream()
                    .filter(t -> t.getCategory().equals(categoryName))
                    .forEach(t -> t.setCategory("Прочее"));
        }

        wallet.getCategories().remove(categoryName);
        fileService.saveWallet(wallet, currentUser.getUsername());

        System.out.println("Категория '" + categoryName + "' успешно удалена!");
    }

    public void importFromCSV(String filename) {
        System.out.println("Импорт из CSV файла: " + filename);
        System.out.println("Функциональность в разработке...");
    }

    public void exportToCSV(String filename) {
        try {
            User currentUser = authService.getCurrentUser();
            Wallet wallet = currentUser.getWallet();

            StringBuilder csv = new StringBuilder();
            csv.append("ID,Дата,Тип,Категория,Сумма,Описание\n");

            for (Transaction transaction : wallet.getTransactions()) {
                csv.append(String.format("%s,%s,%s,%s,%.2f,%s\n",
                        transaction.getId(),
                        transaction.getDate(),
                        transaction.getType(),
                        transaction.getCategory(),
                        transaction.getAmount(),
                        transaction.getDescription()));
            }

            fileService.exportStatisticsToFile(csv.toString(), filename);
            System.out.println("Данные успешно экспортированы в CSV файл: " + filename);

        } catch (Exception e) {
            System.out.println("Ошибка при экспорте в CSV: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void checkAdvancedAlerts() {
        User currentUser = authService.getCurrentUser();
        Wallet wallet = currentUser.getWallet();

        double totalIncome = transactionService.getTotalIncome(wallet);
        double totalExpenses = transactionService.getTotalExpenses(wallet);
        double balance = wallet.getBalance();

        boolean hasAlerts = false;

        for (Category category : wallet.getCategories().values()) {
            double budgetLimit = category.getBudgetLimit();
            if (budgetLimit > 0) {
                double expenses = transactionService.getExpensesByCategory(wallet, category.getName());
                double usagePercentage = (expenses / budgetLimit) * 100;

                if (usagePercentage >= 80 && usagePercentage < 100) {
                    System.out.println("⚠️  ВНИМАНИЕ: Категория '" + category.getName() + "' использована на " +
                            String.format("%.1f", usagePercentage) + "%");
                    System.out.printf("   Лимит: %.2f, Потрачено: %.2f%n", budgetLimit, expenses);
                    hasAlerts = true;
                }
            }
        }

        if (balance == 0) {
            System.out.println("ℹ️  ИНФОРМАЦИЯ: Баланс равен нулю");
            hasAlerts = true;
        }

        if (totalExpenses > 0 && balance < (totalExpenses * 0.1)) {
            System.out.println("⚠️  ВНИМАНИЕ: Низкий баланс (" + String.format("%.2f", balance) + ")");
            System.out.println("   Рекомендуется пополнить счет");
            hasAlerts = true;
        }

        if (!hasAlerts) {
            System.out.println("✅ Нет критических оповещений");
        }
    }

    public void showHelp() {
        System.out.println("\n=== СПРАВКА ПО КОМАНДАМ ===");
        System.out.println("1. Добавить доход - Внесение денежных поступлений");
        System.out.println("2. Добавить расход - Учет трат по категориям");
        System.out.println("3. Общая статистика - Основные финансовые показатели");
        System.out.println("4. Детальная статистика - Подробный анализ по категориям");
        System.out.println("5. Статистика по категориям - Анализ выбранных категорий");
        System.out.println("6. Экспорт статистики - Сохранение отчета в файл");
        System.out.println("7. Создать категорию - Добавление новой категории расходов/доходов");
        System.out.println("8. Установить лимит - Настройка бюджетных ограничений");
        System.out.println("9. Перевод - Перевод средств другому пользователю");
        System.out.println("10. Секретный вопрос - Настройка восстановления пароля");
        System.out.println("11. Статистика за период - Анализ за выбранный период времени");
        System.out.println("12. Редактировать категорию - Изменение названия и лимита категории");
        System.out.println("13. Удалить категорию - Удаление категории с переносом транзакций");
        System.out.println("14. Экспорт в CSV - Экспорт данных в CSV формат");
        System.out.println("15. Импорт из CSV - Импорт данных из CSV файла");
        System.out.println("16. Проверить оповещения - Проверка финансовых предупреждений");
        System.out.println("17. Помощь - Показать эту справку");
        System.out.println("\nПримеры использования:");
        System.out.println("- Для анализа питания и транспорта: выберите пункт 5 и введите 'Еда, Транспорт'");
        System.out.println("- Для установки месячного лимита на развлечения: 8000");
        System.out.println("- Для перевода: укажите логин получателя и сумму");
    }
}