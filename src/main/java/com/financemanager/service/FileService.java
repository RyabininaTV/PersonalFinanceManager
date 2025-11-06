package com.financemanager.service;

import com.financemanager.model.User;
import com.financemanager.model.Wallet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FileService {
    private static final String USERS_DATA_FILE = "data/users.json";
    private static final String WALLETS_DATA_DIR = "data/wallets/";
    private Gson gson;

    public FileService() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .setLenient()
                .create();
        createDataDirectories();
    }

    private void createDataDirectories() {
        new File("data").mkdirs();
        new File(WALLETS_DATA_DIR).mkdirs();
    }

    public void saveUsers(Map<String, User> users) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(USERS_DATA_FILE), StandardCharsets.UTF_8)) {
            gson.toJson(users, writer);
            writer.flush();
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении данных пользователей: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Map<String, User> loadUsers() {
        File file = new File(USERS_DATA_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }

        if (file.length() == 0) {
            return new HashMap<>();
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(USERS_DATA_FILE), StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<String, User>>(){}.getType();
            Map<String, User> users = gson.fromJson(reader, type);

            if (users == null) {
                return new HashMap<>();
            }

            return users;
        } catch (Exception e) {
            System.out.println("Ошибка при загрузке данных пользователей: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public void saveWallet(Wallet wallet, String username) {
        String walletFile = WALLETS_DATA_DIR + username + ".json";
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(walletFile), StandardCharsets.UTF_8)) {
            gson.toJson(wallet, writer);
            writer.flush();
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении кошелька пользователя " + username + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Wallet loadWallet(String username) {
        String walletFile = WALLETS_DATA_DIR + username + ".json";
        File file = new File(walletFile);
        if (!file.exists()) {
            return new Wallet();
        }

        if (file.length() == 0) {
            return new Wallet();
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(walletFile), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, Wallet.class);
        } catch (Exception e) {
            System.out.println("Ошибка при загрузке кошелька пользователя " + username + ": " + e.getMessage());
            e.printStackTrace();
            return new Wallet();
        }
    }

    public void exportStatisticsToFile(String statistics, String filename) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8)) {
            writer.write(statistics);
            writer.flush();
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении статистики в файл: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Не удалось экспортировать статистику", e);
        }
    }
}