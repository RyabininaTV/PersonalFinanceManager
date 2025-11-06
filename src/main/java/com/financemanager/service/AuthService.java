package com.financemanager.service;

import com.financemanager.model.User;

import java.util.Map;
import java.util.HashMap;

public class AuthService {
    private Map<String, User> users;
    private FileService fileService;
    private User currentUser;

    public AuthService(FileService fileService) {
        this.fileService = fileService;
        try {
            this.users = fileService.loadUsers();
        } catch (Exception e) {
            System.out.println("Ошибка при загрузке пользователей: " + e.getMessage());
            e.printStackTrace();
            this.users = new HashMap<>();
        }
    }

    private boolean isValidLoginFormat(String username) {
        return username.matches("^[a-zA-Z0-9]+$");
    }

    public boolean validateLogin(String username) {
        if (username == null) {
            System.out.println("Логин не может быть null!");
            return false;
        }

        if (username.isEmpty()) {
            System.out.println("Логин не может быть пустым!");
            return false;
        }

        if (!isValidLoginFormat(username)) {
            System.out.println("Ошибка: Логин должен содержать только латинские буквы и цифры!");
            return false;
        }

        return true;
    }

    public boolean isUserExists(String username) {
        return users.containsKey(username.trim());
    }

    public User getUser(String username) {
        return users.get(username.trim());
    }

    public boolean setSecretQuestion(String username, String question, String answer) {
        User user = users.get(username.trim());
        if (user != null) {
            user.setSecretQuestion(question);
            user.setSecretAnswer(answer);
            try {
                fileService.saveUsers(users);
                return true;
            } catch (Exception e) {
                System.out.println("Ошибка при сохранении секретного вопроса: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public boolean resetPassword(String username, String answer, String newPassword) {
        User user = users.get(username.trim());
        if (user != null) {
            if (user.getSecretAnswer() != null && user.getSecretAnswer().equalsIgnoreCase(answer.trim())) {
                if (newPassword.length() < 3) {
                    System.out.println("Пароль должен содержать минимум 3 символа!");
                    return false;
                }
                user.setPassword(newPassword);
                try {
                    fileService.saveUsers(users);
                    System.out.println("Пароль успешно изменен!");
                    return true;
                } catch (Exception e) {
                    System.out.println("Ошибка при сохранении нового пароля: " + e.getMessage());
                    e.printStackTrace();
                    return false;
                }
            } else {
                System.out.println("Неверный ответ на секретный вопрос!");
                return false;
            }
        } else {
            System.out.println("Пользователь с логином '" + username + "' не найден!");
            return false;
        }
    }

    public boolean hasSecretQuestion(String username) {
        User user = users.get(username.trim());
        return user != null && user.getSecretQuestion() != null && !user.getSecretQuestion().isEmpty();
    }

    public String getSecretQuestion(String username) {
        User user = users.get(username.trim());
        if (user != null && user.getSecretQuestion() != null && !user.getSecretQuestion().isEmpty()) {
            return user.getSecretQuestion();
        }
        return null;
    }

    public boolean register(String username, String password) {
        username = username.trim();

        if (username.length() < 3 || password.length() < 3) {
            System.out.println("Логин и пароль должны содержать минимум 3 символа!");
            return false;
        }

        User newUser = new User(username, password);
        users.put(username, newUser);

        try {
            fileService.saveUsers(users);
            // Создаем отдельный файл для кошелька пользователя
            fileService.saveWallet(newUser.getWallet(), username);
            System.out.println("Пользователь '" + username + "' успешно зарегистрирован!");
            return true;
        } catch (Exception e) {
            System.out.println("Ошибка при сохранении пользователя: " + e.getMessage());
            e.printStackTrace();
            users.remove(username);
            return false;
        }
    }

    public boolean login(String username, String password) {
        username = username.trim();

        User user = users.get(username);
        if (user != null) {
            if (user.getPassword().equals(password)) {
                // Загружаем актуальный кошелек пользователя
                user.setWallet(fileService.loadWallet(username));
                currentUser = user;
                System.out.println("Добро пожаловать, " + username + "!");
                return true;
            } else {
                System.out.println("Неверный пароль!");
            }
        } else {
            System.out.println("Пользователь '" + username + "' не найден!");
        }

        return false;
    }

    public void logout() {
        if (currentUser != null) {
            try {
                // Сохраняем кошелек пользователя
                fileService.saveWallet(currentUser.getWallet(), currentUser.getUsername());
                // Сохраняем данные пользователей
                fileService.saveUsers(users);
                System.out.println("До свидания, " + currentUser.getUsername() + "!");
            } catch (Exception e) {
                System.out.println("Ошибка при сохранении данных: " + e.getMessage());
            } finally {
                currentUser = null;
            }
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Map<String, User> getUsers() {
        return users;
    }
}