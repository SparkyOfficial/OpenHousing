package ru.openhousing.utils;

import org.bukkit.entity.Player;
import ru.openhousing.OpenHousing;
import ru.openhousing.listeners.ChatListener;

import java.util.function.Consumer;

/**
 * Утилита для ввода текста через чат
 */
public class AnvilGUIHelper {

    /**
     * Открыть ввод текста через чат
     */
    public static void openTextInput(OpenHousing plugin, Player player, String title, String defaultText, Consumer<String> callback) {
        ChatListener chatListener = getChatListener(plugin);
        if (chatListener != null) {
            String prompt = title + (defaultText != null && !defaultText.isEmpty() ? " (текущее: " + defaultText + ")" : "");
            chatListener.expectInput(player, prompt, callback);
        } else {
            // Fallback если ChatListener недоступен
            player.sendMessage("§6" + title);
            player.sendMessage("§7Введите значение в чат:");
            if (defaultText != null && !defaultText.isEmpty()) {
                player.sendMessage("§7Текущее значение: §e" + defaultText);
            }
            player.sendMessage("§7Напишите 'cancel' для отмены");
        }
    }

    /**
     * Открыть ввод числа через чат
     */
    public static void openNumberInput(OpenHousing plugin, Player player, String title, String defaultValue, Consumer<Double> callback) {
        ChatListener chatListener = getChatListener(plugin);
        if (chatListener != null) {
            String prompt = title + (defaultValue != null && !defaultValue.isEmpty() ? " (текущее: " + defaultValue + ")" : "");
            chatListener.expectNumberInput(player, prompt, callback);
        } else {
            // Fallback если ChatListener недоступен
            player.sendMessage("§6" + title);
            player.sendMessage("§7Введите число в чат:");
            if (defaultValue != null && !defaultValue.isEmpty()) {
                player.sendMessage("§7Текущее значение: §e" + defaultValue);
            }
            player.sendMessage("§7Напишите 'cancel' для отмены");
        }
    }

    /**
     * Открыть ввод целого числа через чат
     */
    public static void openIntegerInput(OpenHousing plugin, Player player, String title, String defaultValue, Consumer<Integer> callback) {
        ChatListener chatListener = getChatListener(plugin);
        if (chatListener != null) {
            String prompt = title + (defaultValue != null && !defaultValue.isEmpty() ? " (текущее: " + defaultValue + ")" : "");
            chatListener.expectIntegerInput(player, prompt, callback);
        } else {
            openNumberInput(plugin, player, title, defaultValue, (number) -> {
                callback.accept(number.intValue());
            });
        }
    }

    /**
     * Получить ChatListener из плагина
     */
    private static ChatListener getChatListener(OpenHousing plugin) {
        return plugin.getChatListener();
    }
}
