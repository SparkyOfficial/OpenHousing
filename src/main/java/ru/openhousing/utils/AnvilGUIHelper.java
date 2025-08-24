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
 * Профессиональная утилита для ввода текста через AnvilGUI или чат
 */
public class AnvilGUIHelper {

    // Статические переменные для проверки доступности AnvilGUI
    private static boolean isAnvilGUIAvailable = false;
    private static boolean checkedAnvilGUI = false;

    /**
     * Проверяет доступность AnvilGUI при первой загрузке
     */
    private static void checkAnvilGUIAvailability(OpenHousing plugin) {
        if (checkedAnvilGUI) {
            return;
        }

        try {
            // Это должно инициировать статическую инициализацию класса AnvilGUI
            Class.forName("net.wesjd.anvilgui.AnvilGUI");
            isAnvilGUIAvailable = true;
            plugin.getLogger().info("[DEBUG] AnvilGUI class initialized successfully. VersionMatcher passed.");
        } catch (Throwable t) {
            // Throwable для перехвата Error и Exception
            plugin.getLogger().severe("[CRITICAL ERROR] AnvilGUI class initialization failed at startup! Error: " + t.getMessage());
            plugin.getLogger().severe("This means AnvilGUI is not compatible. Using chat fallback permanently.");
            // Выводим stack trace для диагностики
            t.printStackTrace();
            isAnvilGUIAvailable = false;
        } finally {
            checkedAnvilGUI = true;
        }
    }
    
    /**
     * Проверяет доступность AnvilGUI (публичный метод для тестов)
     */
    public static boolean isAnvilGUIAvailable() {
        return isAnvilGUIAvailable;
    }
    
    /**
     * Проверяет доступность AnvilGUI (публичный метод для тестов)
     */
    public static void checkAnvilGUIAvailability() {
        // Для тестов просто возвращаем текущее состояние
        // В реальном использовании нужно передавать plugin
    }

    /**
     * Открыть красивый GUI для ввода текста
     */
    public static void openTextInput(OpenHousing plugin, Player player, String title, String defaultText, Consumer<String> callback) {
        checkAnvilGUIAvailability(plugin);

        if (isAnvilGUIAvailable) {
            try {
                // Если AnvilGUI доступен, пробуем его открыть
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
                        Bukkit.getScheduler().runTask(plugin, () -> callback.accept(input.trim()));
                        return AnvilGUI.Response.close();
                    })
                    .onClose(player1 -> {})
                    .text(defaultText != null ? defaultText : "")
                    .title(title.length() > 30 ? title.substring(0, 30) : title)
                    .itemLeft(new ItemStack(Material.PAPER))
                    .plugin(plugin)
                    .open(player);
                plugin.getLogger().info("[DEBUG] AnvilGUI opened for " + player.getName() + ": " + title);
            } catch (Exception e) {
                // Этот catch для Runtime ошибок при создании AnvilGUI после его статической инициализации
                plugin.getLogger().warning("[WARNING] AnvilGUI failed during runtime, falling back to chat: " + e.getMessage());
                fallbackToChatInput(plugin, player, title, defaultText, callback);
            }
        } else {
            // AnvilGUI несовместим или сломан, сразу переходим к чату
            plugin.getLogger().info("[DEBUG] AnvilGUI not available, using chat fallback directly for " + player.getName() + ": " + title);
            fallbackToChatInput(plugin, player, title, defaultText, callback);
        }
    }

    /**
     * Открыть красивый GUI для ввода чисел
     */
    public static void openNumberInput(OpenHousing plugin, Player player, String title, String defaultValue, Consumer<Double> callback) {
        checkAnvilGUIAvailability(plugin);

        if (isAnvilGUIAvailable) {
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
                            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(number));
                            return AnvilGUI.Response.close();
                        } catch (NumberFormatException e) {
                            player.sendMessage("§cНеверный формат числа! Введите корректное число.");
                            return AnvilGUI.Response.text(state.getText());
                        }
                    })
                    .onClose(player1 -> {})
                    .text(defaultValue != null ? defaultValue : "0")
                    .title(title.length() > 30 ? title.substring(0, 30) : title)
                    .itemLeft(new ItemStack(Material.GOLD_NUGGET))
                    .plugin(plugin)
                    .open(player);
                plugin.getLogger().info("[DEBUG] AnvilGUI number input opened for " + player.getName() + ": " + title);
            } catch (Exception e) {
                plugin.getLogger().warning("[WARNING] AnvilGUI number input failed during runtime, falling back to chat: " + e.getMessage());
                fallbackToNumberChatInput(plugin, player, title, defaultValue, callback);
            }
        } else {
            plugin.getLogger().info("[DEBUG] AnvilGUI not available, using chat fallback for number input: " + player.getName() + ": " + title);
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

    // --- Методы-заглушки для fallback через чат ---
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
