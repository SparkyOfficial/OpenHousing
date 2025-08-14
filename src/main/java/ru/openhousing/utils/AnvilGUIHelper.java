package ru.openhousing.utils;

import org.bukkit.entity.Player;
import ru.openhousing.OpenHousing;

import java.util.function.Consumer;

/**
 * Утилита для ввода текста (пока без AnvilGUI)
 */
public class AnvilGUIHelper {

    /**
     * Открыть ввод текста через чат
     */
    public static void openTextInput(OpenHousing plugin, Player player, String title, String defaultText, Consumer<String> callback) {
        player.sendMessage("§6" + title);
        player.sendMessage("§7Введите значение в чат:");
        if (defaultText != null && !defaultText.isEmpty()) {
            player.sendMessage("§7Текущее значение: §e" + defaultText);
        }
        player.sendMessage("§7Напишите 'cancel' для отмены");
        
        // TODO: Добавить обработку ввода в чат через ChatListener
        // Пока просто показываем сообщение
    }

    /**
     * Открыть ввод числа через чат
     */
    public static void openNumberInput(OpenHousing plugin, Player player, String title, String defaultValue, Consumer<Double> callback) {
        player.sendMessage("§6" + title);
        player.sendMessage("§7Введите число в чат:");
        if (defaultValue != null && !defaultValue.isEmpty()) {
            player.sendMessage("§7Текущее значение: §e" + defaultValue);
        }
        player.sendMessage("§7Напишите 'cancel' для отмены");
        
        // TODO: Добавить обработку ввода в чат через ChatListener
        // Пока просто показываем сообщение
    }

    /**
     * Открыть ввод целого числа через чат
     */
    public static void openIntegerInput(OpenHousing plugin, Player player, String title, String defaultValue, Consumer<Integer> callback) {
        openNumberInput(plugin, player, title, defaultValue, (number) -> {
            callback.accept(number.intValue());
        });
    }
}
