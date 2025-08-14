package ru.openhousing.utils;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.OpenHousing;
import ru.openhousing.listeners.ChatListener;

import java.util.function.Consumer;

/**
 * Профессиональная утилита для ввода текста через AnvilGUI
 */
public class AnvilGUIHelper {

    /**
     * Открыть красивый GUI для ввода текста
     */
    public static void openTextInput(OpenHousing plugin, Player player, String title, String defaultText, Consumer<String> callback) {
        try {
            new AnvilGUI.Builder()
                .onComplete((player1, text) -> {
                    String input = text.trim();
                    
                    // Проверка на пустой ввод
                    if (input.isEmpty()) {
                        player.sendMessage("§cВвод не может быть пустым!");
                        return AnvilGUI.Response.close();
                    }
                    
                    // Вызываем callback и закрываем GUI
                    callback.accept(input);
                    return AnvilGUI.Response.close();
                })
                .onClose(player1 -> {
                    // Игрок закрыл GUI без ввода - ничего не делаем
                })
                .text(defaultText != null ? defaultText : "")
                .title(title.length() > 30 ? title.substring(0, 30) : title) // Ограничиваем длину заголовка
                .itemLeft(new ItemStack(Material.PAPER))
                .plugin(plugin)
                .open(player);
                
        } catch (Exception e) {
            // Fallback к чату если AnvilGUI не работает
            plugin.getLogger().warning("AnvilGUI failed, using chat fallback: " + e.getMessage());
            fallbackToChatInput(plugin, player, title, defaultText, callback);
        }
    }

    /**
     * Открыть красивый GUI для ввода чисел
     */
    public static void openNumberInput(OpenHousing plugin, Player player, String title, String defaultValue, Consumer<Double> callback) {
        try {
            new AnvilGUI.Builder()
                .onComplete((player1, text) -> {
                    String input = text.trim();
                    
                    if (input.isEmpty()) {
                        player.sendMessage("§cВвод не может быть пустым!");
                        return AnvilGUI.Response.close();
                    }
                    
                    try {
                        double number = Double.parseDouble(input);
                        callback.accept(number);
                        return AnvilGUI.Response.close();
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cНеверный формат числа! Введите корректное число.");
                        return AnvilGUI.Response.close();
                    }
                })
                .onClose(player1 -> {
                    // Игрок закрыл GUI без ввода
                })
                .text(defaultValue != null ? defaultValue : "0")
                .title(title.length() > 30 ? title.substring(0, 30) : title)
                .itemLeft(new ItemStack(Material.GOLD_NUGGET))
                .plugin(plugin)
                .open(player);
                
        } catch (Exception e) {
            // Fallback к чату если AnvilGUI не работает
            plugin.getLogger().warning("AnvilGUI failed, using chat fallback: " + e.getMessage());
            fallbackToNumberChatInput(plugin, player, title, defaultValue, callback);
        }
    }

    /**
     * Открыть красивый GUI для ввода целых чисел
     */
    public static void openIntegerInput(OpenHousing plugin, Player player, String title, String defaultValue, Consumer<Integer> callback) {
        openNumberInput(plugin, player, title, defaultValue, (number) -> {
            callback.accept(number.intValue());
        });
    }

    /**
     * Fallback к вводу через чат если AnvilGUI недоступен
     */
    private static void fallbackToChatInput(OpenHousing plugin, Player player, String title, String defaultText, Consumer<String> callback) {
        ChatListener chatListener = plugin.getChatListener();
        if (chatListener != null) {
            String prompt = title + (defaultText != null && !defaultText.isEmpty() ? " (текущее: " + defaultText + ")" : "");
            chatListener.expectInput(player, prompt, callback);
        } else {
            // Последний fallback - просто сообщения
            player.sendMessage("§6" + title);
            player.sendMessage("§7Введите значение в чат:");
            if (defaultText != null && !defaultText.isEmpty()) {
                player.sendMessage("§7Текущее значение: §e" + defaultText);
            }
            player.sendMessage("§7Напишите 'cancel' для отмены");
        }
    }

    /**
     * Fallback к вводу чисел через чат если AnvilGUI недоступен
     */
    private static void fallbackToNumberChatInput(OpenHousing plugin, Player player, String title, String defaultValue, Consumer<Double> callback) {
        ChatListener chatListener = plugin.getChatListener();
        if (chatListener != null) {
            String prompt = title + (defaultValue != null && !defaultValue.isEmpty() ? " (текущее: " + defaultValue + ")" : "");
            chatListener.expectNumberInput(player, prompt, callback);
        } else {
            // Последний fallback - просто сообщения
            player.sendMessage("§6" + title);
            player.sendMessage("§7Введите число в чат:");
            if (defaultValue != null && !defaultValue.isEmpty()) {
                player.sendMessage("§7Текущее значение: §e" + defaultValue);
            }
            player.sendMessage("§7Напишите 'cancel' для отмены");
        }
    }
}
