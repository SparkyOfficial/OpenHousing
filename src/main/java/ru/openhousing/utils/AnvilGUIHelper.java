package ru.openhousing.utils;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
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
                .onClick((slot, state) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) { // Если клик не по слоту вывода, оставляем GUI открытым
                        return AnvilGUI.Response.text(state.getText());
                    }

                    // Если клик по слоту вывода (пользователь нажал на "готово")
                    String input = state.getText();
                    if (input == null || input.trim().isEmpty()) {
                        player.sendMessage("§cВвод не может быть пустым!");
                        return AnvilGUI.Response.text(state.getText()); // Можно вернуть текущий текст или Response.openInventory(player.getOpenInventory()) для ре-опен
                    }

                    // Важно: выполнение колбэка должно быть в главном потоке Bukkit
                    // после закрытия GUI, чтобы избежать ConcurrentModificationException
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        callback.accept(input.trim());
                    });
                    return AnvilGUI.Response.close(); // Закрываем GUI
                })
                .onClose(player1 -> {
                    // Опционально: можно добавить логику, если игрок закрыл GUI без ввода
                })
                .text(defaultText != null ? defaultText : "")
                .title(title.length() > 30 ? title.substring(0, 30) : title) // Ограничиваем длину заголовка
                .itemLeft(new ItemStack(Material.PAPER))
                .plugin(plugin)
                .open(player);
                
        } catch (Exception e) {
            // Fallback к чату если AnvilGUI не работает (или ошибка)
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
                .onClick((slot, state) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) {
                        return AnvilGUI.Response.text(state.getText());
                    }

                    String input = state.getText();
                    if (input == null || input.trim().isEmpty()) {
                        player.sendMessage("§cВвод не может быть пустым!");
                        return AnvilGUI.Response.text(state.getText());
                    }
                    
                    try {
                        double number = Double.parseDouble(input);
                        // Важно: выполнение колбэка должно быть в главном потоке Bukkit
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            callback.accept(number);
                        });
                        return AnvilGUI.Response.close();
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cНеверный формат числа! Введите корректное число.");
                        return AnvilGUI.Response.text(state.getText()); // Если ошибка, оставляем текст в AnvilGUI
                    }
                })
                .onClose(player1 -> {
                    // Опционально: можно добавить логику, если игрок закрыл GUI без ввода
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

    // --- Методы-заглушки для fallback через чат (остаются без изменений) ---
    private static void fallbackToChatInput(OpenHousing plugin, Player player, String title, String defaultText, Consumer<String> callback) {
        ChatListener chatListener = plugin.getChatListener();
        if (chatListener != null) {
            String prompt = title + (defaultText != null && !defaultText.isEmpty() ? " (текущее: " + defaultText + ")" : "");
            chatListener.expectInput(player, prompt, callback);
        } else {
            player.sendMessage("§6" + title);
            player.sendMessage("§7Введите значение в чат:");
            if (defaultText != null && !defaultText.isEmpty()) {
                player.sendMessage("§7Текущее значение: §e" + defaultText);
            }
            player.sendMessage("§7Напишите 'cancel' для отмены");
        }
    }

    private static void fallbackToNumberChatInput(OpenHousing plugin, Player player, String title, String defaultValue, Consumer<Double> callback) {
        ChatListener chatListener = plugin.getChatListener();
        if (chatListener != null) {
            String prompt = title + (defaultValue != null && !defaultValue.isEmpty() ? " (текущее: " + defaultValue + ")" : "");
            chatListener.expectNumberInput(player, prompt, callback);
        } else {
            player.sendMessage("§6" + title);
            player.sendMessage("§7Введите число в чат:");
            if (defaultValue != null && !defaultValue.isEmpty()) {
                player.sendMessage("§7Текущее значение: §e" + defaultValue);
            }
            player.sendMessage("§7Напишите 'cancel' для отмены");
        }
    }
}
