package ru.openhousing.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.openhousing.OpenHousing;
import ru.openhousing.utils.MessageUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Обработчик ввода в чат для настройки значений
 */
public class ChatListener implements Listener {

    private final OpenHousing plugin;
    
    // Хранилище ожидающих ввода игроков
    private final Map<UUID, ChatInputSession> awaitingInput = new HashMap<>();

    public ChatListener(OpenHousing plugin) {
        this.plugin = plugin;
    }

    /**
     * Установить ожидание ввода от игрока
     */
    public void expectInput(Player player, String prompt, Consumer<String> callback) {
        awaitingInput.put(player.getUniqueId(), new ChatInputSession(prompt, callback));
        MessageUtil.send(player, "&6" + prompt);
        MessageUtil.send(player, "&7Введите значение в чат или 'cancel' для отмены");
    }

    /**
     * Установить ожидание числового ввода от игрока
     */
    public void expectNumberInput(Player player, String prompt, Consumer<Double> callback) {
        expectInput(player, prompt, (input) -> {
            try {
                double number = Double.parseDouble(input);
                callback.accept(number);
            } catch (NumberFormatException e) {
                MessageUtil.send(player, "&cНеверный формат числа! Попробуйте еще раз.");
                expectNumberInput(player, prompt, callback);
            }
        });
    }

    /**
     * Установить ожидание целочисленного ввода от игрока
     */
    public void expectIntegerInput(Player player, String prompt, Consumer<Integer> callback) {
        expectNumberInput(player, prompt, (number) -> {
            callback.accept(number.intValue());
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Проверяем, ожидаем ли мы ввод от этого игрока
        if (!awaitingInput.containsKey(playerId)) {
            return;
        }

        event.setCancelled(true); // Отменяем обычный чат

        String message = event.getMessage().trim();
        ChatInputSession session = awaitingInput.get(playerId);

        // Обработка отмены
        if (message.equalsIgnoreCase("cancel") || message.equalsIgnoreCase("отмена")) {
            awaitingInput.remove(playerId);
            MessageUtil.send(player, "&7Ввод отменен");
            return;
        }

        // Обработка пустого ввода
        if (message.isEmpty()) {
            MessageUtil.send(player, "&cВвод не может быть пустым! Попробуйте еще раз или напишите 'cancel'");
            return;
        }

        // Выполняем callback
        try {
            awaitingInput.remove(playerId);
            session.callback.accept(message);
            MessageUtil.send(player, "&aЗначение принято: &e" + message);
        } catch (Exception e) {
            MessageUtil.send(player, "&cОшибка обработки ввода: " + e.getMessage());
            plugin.getLogger().warning("Error processing chat input from " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Проверить, ожидаем ли мы ввод от игрока
     */
    public boolean isAwaitingInput(Player player) {
        return awaitingInput.containsKey(player.getUniqueId());
    }

    /**
     * Отменить ожидание ввода
     */
    public void cancelInput(Player player) {
        UUID playerId = player.getUniqueId();
        if (awaitingInput.containsKey(playerId)) {
            awaitingInput.remove(playerId);
            MessageUtil.send(player, "&7Ожидание ввода отменено");
        }
    }

    /**
     * Очистить все ожидания при выходе игрока
     */
    public void clearPlayerInput(Player player) {
        awaitingInput.remove(player.getUniqueId());
    }

    /**
     * Сессия ввода в чат
     */
    private static class ChatInputSession {
        private final String prompt;
        private final Consumer<String> callback;
        private final long timestamp;

        public ChatInputSession(String prompt, Consumer<String> callback) {
            this.prompt = prompt;
            this.callback = callback;
            this.timestamp = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > 300000; // 5 минут
        }
    }

    /**
     * Очистка устаревших сессий
     */
    public void cleanupExpiredSessions() {
        awaitingInput.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
