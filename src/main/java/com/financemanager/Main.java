package com.financemanager;

import com.financemanager.service.AuthService;
import com.financemanager.service.FileService;
import com.financemanager.service.TransactionService;
import com.financemanager.service.WalletService;
import com.financemanager.exception.CategoryNotFoundException;
import com.financemanager.exception.InsufficientFundsException;
import com.financemanager.exception.UserNotFoundException;

import java.util.Scanner;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static AuthService authService;
    private static TransactionService transactionService;
    private static WalletService walletService;
    private static FileService fileService;
    private static Scanner scanner;
    private static boolean running = true;

    public static void main(String[] args) {
        try {
            initializeServices();
            scanner = new Scanner(System.in, StandardCharsets.UTF_8);

            System.out.println("=== ПРИЛОЖЕНИЕ ДЛЯ УПРАВЛЕНИЯ ЛИЧНЫМИ ФИНАНСАМИ ===");

            while (running) {
                if (authService.getCurrentUser() == null) {
                    showAuthMenu();
                } else {
                    showMainMenu();
                }
            }
        } catch (Exception e) {
            System.out.println("Критическая ошибка: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
            System.out.println("Приложение завершено. До свидания!");
        }
    }

    private static void initializeServices() {
        try {
            fileService = new FileService();
            authService = new AuthService(fileService);
            transactionService = new TransactionService();
            walletService = new WalletService(authService, transactionService, fileService);
        } catch (Exception e) {
            System.out.println("Ошибка инициализации сервисов: " + e.getMessage());
            e.printStackTrace();
            running = false;
        }
    }

    private static void showAuthMenu() {
        System.out.println("\n=== ГЛАВНОЕ МЕНЮ ===");
        System.out.println("1. Вход");
        System.out.println("2. Регистрация");
        System.out.println("3. Восстановление пароля");
        System.out.println("4. Выход");
        System.out.print("Выберите действие: ");

        try {
            String input = scanner.nextLine();
            if (input.isEmpty()) {
                System.out.println("Пожалуйста, введите число!");
                return;
            }

            int choice = Integer.parseInt(input);

            switch (choice) {
                case 1 -> login();
                case 2 -> register();
                case 3 -> resetPassword();
                case 4 -> running = false;
                default -> System.out.println("Неверный выбор! Введите число от 1 до 4.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Пожалуйста, введите корректное число!");
        } catch (Exception e) {
            System.out.println("Произошла ошибка: " + e.getMessage());
        }
    }

    private static void showMainMenu() {
        System.out.println("\n=== ОСНОВНОЕ МЕНЮ ===");
        System.out.println("Текущий пользователь: " + authService.getCurrentUser().getUsername());
        System.out.println("1. Добавить доход");
        System.out.println("2. Добавить расход");
        System.out.println("3. Показать общую статистику");
        System.out.println("4. Показать детальную статистику");
        System.out.println("5. Статистика по выбранным категориям");
        System.out.println("6. Экспорт статистики в файл");
        System.out.println("7. Создать категорию");
        System.out.println("8. Установить лимит бюджета");
        System.out.println("9. Перевод другому пользователю");
        System.out.println("10. Настройка секретного вопроса");
        System.out.println("11. Статистика за период");
        System.out.println("12. Редактировать категорию");
        System.out.println("13. Удалить категорию");
        System.out.println("14. Экспорт в CSV");
        System.out.println("15. Импорт из CSV");
        System.out.println("16. Проверить оповещения");
        System.out.println("17. Помощь");
        System.out.println("18. Выход");
        System.out.print("Выберите действие: ");

        try {
            String input = scanner.nextLine();
            if (input.isEmpty()) {
                System.out.println("Пожалуйста, введите число!");
                return;
            }

            int choice = Integer.parseInt(input);

            switch (choice) {
                case 1 -> addIncome();
                case 2 -> addExpense();
                case 3 -> showStatistics();
                case 4 -> showDetailedStatistics();
                case 5 -> showSelectedCategoriesStatistics();
                case 6 -> exportStatisticsToFile();
                case 7 -> createCategory();
                case 8 -> setBudgetLimit();
                case 9 -> transferToUser();
                case 10 -> setupSecretQuestion();
                case 11 -> showStatisticsForPeriod();
                case 12 -> editCategory();
                case 13 -> deleteCategory();
                case 14 -> exportToCSV();
                case 15 -> importFromCSV();
                case 16 -> checkAdvancedAlerts();
                case 17 -> showHelp();
                case 18 -> logout();
                default -> System.out.println("Неверный выбор! Введите число от 1 до 18.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Пожалуйста, введите корректное число!");
        } catch (Exception e) {
            System.out.println("Произошла ошибка: " + e.getMessage());
        }
    }

    private static void login() {
        try {
            System.out.print("Введите логин: ");
            String username = scanner.nextLine();

            if (!authService.validateLogin(username)) {
                return;
            }

            System.out.print("Введите пароль: ");
            String password = scanner.nextLine();

            authService.login(username, password);
        } catch (Exception e) {
            System.out.println("Ошибка при входе: " + e.getMessage());
        }
    }

    private static void register() {
        try {
            System.out.print("Введите логин: ");
            String username = scanner.nextLine();

            if (!authService.validateLogin(username)) {
                return;
            }

            if (authService.isUserExists(username)) {
                System.out.println("Пользователь с логином '" + username + "' уже существует!");
                return;
            }

            System.out.print("Введите пароль: ");
            String password = scanner.nextLine();

            if (authService.register(username, password)) {
                setupSecretQuestionAfterRegistration(username);
            }
        } catch (Exception e) {
            System.out.println("Ошибка при регистрации: " + e.getMessage());
        }
    }

    private static void setupSecretQuestionAfterRegistration(String username) {
        System.out.println("\n=== НАСТРОЙКА СЕКРЕТНОГО ВОПРОСА ===");
        System.out.println("Для обеспечения безопасности вашего аккаунта необходимо настроить секретный вопрос. Он потребуется для восстановления пароля в случае его утери.");

        setupSecretQuestionForUser(username);
    }

    private static void setupSecretQuestionForUser(String username) {
        try {
            String question = "";
            while (question.isEmpty()) {
                System.out.print("Введите секретный вопрос: ");
                question = scanner.nextLine().trim();

                if (question.isEmpty()) {
                    System.out.println("Секретный вопрос не может быть пустым! Пожалуйста, введите вопрос.");
                }
            }

            String answer = "";
            while (answer.isEmpty()) {
                System.out.print("Введите ответ на секретный вопрос: ");
                answer = scanner.nextLine().trim();

                if (answer.isEmpty()) {
                    System.out.println("Ответ на секретный вопрос не может быть пустым! Пожалуйста, введите ответ.");
                }
            }

            if (authService.setSecretQuestion(username, question, answer)) {
                System.out.println("Регистрация завершена успешно!");
            } else {
                System.out.println("Ошибка при установке секретного вопроса!");
                System.out.println("Пожалуйста, попробуйте снова.");
                setupSecretQuestionForUser(username);
            }
        } catch (Exception e) {
            System.out.println("Ошибка при настройке секретного вопроса: " + e.getMessage());
        }
    }

    private static void resetPassword() {
        try {
            System.out.println("\n=== ВОССТАНОВЛЕНИЕ ПАРОЛЯ ===");
            System.out.print("Введите логин: ");
            String username = scanner.nextLine();

            if (!authService.isUserExists(username)) {
                System.out.println("Пользователь с логином '" + username + "' не найден!");
                return;
            }

            if (!authService.hasSecretQuestion(username)) {
                System.out.println("Для данного пользователя не установлен секретный вопрос.");
                System.out.println("Восстановление пароля невозможно. Обратитесь к администратору.");
                return;
            }

            String question = authService.getSecretQuestion(username);
            System.out.println("Секретный вопрос: " + question);
            System.out.print("Введите ответ: ");
            String answer = scanner.nextLine();

            System.out.print("Введите новый пароль: ");
            String newPassword = scanner.nextLine();

            authService.resetPassword(username, answer, newPassword);
        } catch (Exception e) {
            System.out.println("Ошибка при восстановлении пароля: " + e.getMessage());
        }
    }

    private static void setupSecretQuestion() {
        try {
            String username = authService.getCurrentUser().getUsername();

            if (authService.hasSecretQuestion(username)) {
                System.out.println("Секретный вопрос уже установлен.");
                System.out.print("Хотите изменить его? (y/n): ");
                String response = scanner.nextLine().trim().toLowerCase();

                if (!response.equals("y") && !response.equals("yes")) {
                    System.out.println("Изменение отменено.");
                    return;
                }
            }

            System.out.println("\n=== ИЗМЕНЕНИЕ СЕКРЕТНОГО ВОПРОСА ===");
            setupSecretQuestionForUser(username);
        } catch (Exception e) {
            System.out.println("Ошибка при настройке секретного вопроса: " + e.getMessage());
        }
    }

    private static void logout() {
        try {
            authService.logout();
        } catch (Exception e) {
            System.out.println("Ошибка при выходе: " + e.getMessage());
        }
    }

    private static void addIncome() {
        try {
            System.out.print("Введите сумму дохода: ");
            String amountInput = scanner.nextLine().trim();
            if (amountInput.isEmpty()) {
                System.out.println("Сумма не может быть пустой!");
                return;
            }

            double amount = Double.parseDouble(amountInput);
            if (amount <= 0) {
                System.out.println("Сумма должна быть положительной!");
                return;
            }

            System.out.print("Введите категорию: ");
            String category = scanner.nextLine().trim();
            if (category.isEmpty()) {
                System.out.println("Категория не может быть пустой!");
                return;
            }

            System.out.print("Введите описание: ");
            String description = scanner.nextLine().trim();

            transactionService.addIncome(authService.getCurrentUser().getWallet(),
                    amount, category, description);
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат суммы! Используйте числа (например: 1000.50)");
        } catch (CategoryNotFoundException e) {
            System.out.println(e.getMessage());
            System.out.println("Сначала создайте категорию через меню (пункт 7)");
        } catch (Exception e) {
            System.out.println("Ошибка при добавлении дохода: " + e.getMessage());
        }
    }

    private static void addExpense() {
        try {
            System.out.print("Введите сумму расхода: ");
            String amountInput = scanner.nextLine().trim();
            if (amountInput.isEmpty()) {
                System.out.println("Сумма не может быть пустой!");
                return;
            }

            double amount = Double.parseDouble(amountInput);
            if (amount <= 0) {
                System.out.println("Сумма должна быть положительной!");
                return;
            }

            System.out.print("Введите категорию: ");
            String category = scanner.nextLine().trim();
            if (category.isEmpty()) {
                System.out.println("Категория не может быть пустой!");
                return;
            }

            System.out.print("Введите описание: ");
            String description = scanner.nextLine().trim();

            transactionService.addExpense(authService.getCurrentUser().getWallet(),
                    amount, category, description);
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат суммы! Используйте числа (например: 1000.50)");
        } catch (CategoryNotFoundException | InsufficientFundsException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("Ошибка при добавлении расхода: " + e.getMessage());
        }
    }

    private static void showStatistics() {
        try {
            walletService.showStatistics();
        } catch (Exception e) {
            System.out.println("Ошибка при показе статистики: " + e.getMessage());
        }
    }

    private static void showDetailedStatistics() {
        try {
            walletService.showDetailedStatistics();
        } catch (Exception e) {
            System.out.println("Ошибка при показе детальной статистики: " + e.getMessage());
        }
    }

    private static void showSelectedCategoriesStatistics() {
        try {
            System.out.print("Введите названия категорий через запятую: ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Не введены категории!");
                return;
            }

            List<String> categories = Arrays.stream(input.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            if (categories.isEmpty()) {
                System.out.println("Не введены категории!");
                return;
            }

            walletService.showStatisticsForCategories(categories);
        } catch (Exception e) {
            System.out.println("Ошибка при показе статистики по категориям: " + e.getMessage());
        }
    }

    private static void exportStatisticsToFile() {
        try {
            System.out.print("Введите имя файла для экспорта (без расширения): ");
            String filename = scanner.nextLine().trim();
            if (filename.isEmpty()) {
                System.out.println("Имя файла не может быть пустым!");
                return;
            }

            walletService.exportStatisticsToFile(filename + ".txt");
        } catch (Exception e) {
            System.out.println("Ошибка при экспорте статистики: " + e.getMessage());
        }
    }

    private static void createCategory() {
        try {
            System.out.print("Введите название категории: ");
            String categoryName = scanner.nextLine().trim();
            if (categoryName.isEmpty()) {
                System.out.println("Название категории не может быть пустым!");
                return;
            }

            System.out.print("Введите лимит бюджета (0 если без лимита): ");
            String limitInput = scanner.nextLine().trim();
            if (limitInput.isEmpty()) {
                System.out.println("Лимит не может быть пустым! Введите 0 если без лимита.");
                return;
            }

            double budgetLimit = Double.parseDouble(limitInput);
            if (budgetLimit < 0) {
                System.out.println("Лимит не может быть отрицательным!");
                return;
            }

            walletService.createCategory(categoryName, budgetLimit);
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат лимита! Используйте числа (например: 1000.50)");
        } catch (Exception e) {
            System.out.println("Ошибка при создании категории: " + e.getMessage());
        }
    }

    private static void setBudgetLimit() {
        try {
            System.out.print("Введите название категории: ");
            String categoryName = scanner.nextLine().trim();
            if (categoryName.isEmpty()) {
                System.out.println("Название категории не может быть пустой!");
                return;
            }

            System.out.print("Введите новый лимит бюджета: ");
            String limitInput = scanner.nextLine().trim();
            if (limitInput.isEmpty()) {
                System.out.println("Лимит не может быть пустым!");
                return;
            }

            double budgetLimit = Double.parseDouble(limitInput);
            if (budgetLimit < 0) {
                System.out.println("Лимит не может быть отрицательным!");
                return;
            }

            walletService.setBudgetLimit(categoryName, budgetLimit);
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат лимита! Используйте числа (например: 1000.50)");
        } catch (CategoryNotFoundException e) {
            System.out.println(e.getMessage());
            System.out.println("Сначала создайте категорию через меню (пункт 7)");
        } catch (Exception e) {
            System.out.println("Ошибка при установке лимита: " + e.getMessage());
        }
    }

    private static void transferToUser() {
        try {
            System.out.print("Введите логин получателя: ");
            String targetUsername = scanner.nextLine().trim();
            if (targetUsername.isEmpty()) {
                System.out.println("Логин получателя не может быть пустым!");
                return;
            }

            System.out.print("Введите сумму перевода: ");
            String amountInput = scanner.nextLine().trim();
            if (amountInput.isEmpty()) {
                System.out.println("Сумма не может быть пустой!");
                return;
            }

            double amount = Double.parseDouble(amountInput);
            if (amount <= 0) {
                System.out.println("Сумма должна быть положительной!");
                return;
            }

            System.out.print("Введите описание перевода: ");
            String description = scanner.nextLine().trim();

            walletService.transferToUser(targetUsername, amount, description);
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат суммы! Используйте числа (например: 1000.50)");
        } catch (UserNotFoundException | InsufficientFundsException | CategoryNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("Ошибка при переводе: " + e.getMessage());
        }
    }

    private static void showStatisticsForPeriod() {
        try {
            System.out.print("Введите начальную дату (гггг-мм-дд): ");
            String startDate = scanner.nextLine().trim();
            System.out.print("Введите конечную дату (гггг-мм-дд): ");
            String endDate = scanner.nextLine().trim();

            walletService.showStatisticsForPeriod(startDate, endDate);
        } catch (Exception e) {
            System.out.println("Ошибка при показе статистики за период: " + e.getMessage());
        }
    }

    private static void editCategory() {
        try {
            System.out.print("Введите текущее название категории: ");
            String oldName = scanner.nextLine().trim();
            System.out.print("Введите новое название категории: ");
            String newName = scanner.nextLine().trim();
            System.out.print("Введите новый лимит бюджета: ");
            String limitInput = scanner.nextLine().trim();

            double newLimit = Double.parseDouble(limitInput);
            walletService.editCategory(oldName, newName, newLimit);
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат лимита!");
        } catch (Exception e) {
            System.out.println("Ошибка при редактировании категории: " + e.getMessage());
        }
    }

    private static void deleteCategory() {
        try {
            System.out.print("Введите название категории для удаления: ");
            String categoryName = scanner.nextLine().trim();

            walletService.deleteCategory(categoryName);
        } catch (Exception e) {
            System.out.println("Ошибка при удалении категории: " + e.getMessage());
        }
    }

    private static void exportToCSV() {
        try {
            System.out.print("Введите имя CSV файла (без расширения): ");
            String filename = scanner.nextLine().trim();
            walletService.exportToCSV(filename + ".csv");
        } catch (Exception e) {
            System.out.println("Ошибка при экспорте в CSV: " + e.getMessage());
        }
    }

    private static void importFromCSV() {
        try {
            System.out.print("Введите имя CSV файла для импорта: ");
            String filename = scanner.nextLine().trim();
            walletService.importFromCSV(filename);
        } catch (Exception e) {
            System.out.println("Ошибка при импорте из CSV: " + e.getMessage());
        }
    }

    private static void checkAdvancedAlerts() {
        try {
            walletService.checkAdvancedAlerts();
        } catch (Exception e) {
            System.out.println("Ошибка при проверке оповещений: " + e.getMessage());
        }
    }

    private static void showHelp() {
        try {
            walletService.showHelp();
        } catch (Exception e) {
            System.out.println("Ошибка при показе справки: " + e.getMessage());
        }
    }
}