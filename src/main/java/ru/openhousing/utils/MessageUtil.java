package ru.openhousing.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Logger;

/**
 * Утилита для работы с сообщениями
 */
public class MessageUtil {
    
    private static final Logger logger = Bukkit.getLogger();
    
    /**
     * Окрашивание текста с помощью цветовых кодов
     */
    public static String colorize(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    /**
     * Окрашивание списка строк
     */
    public static List<String> colorize(List<String> messages) {
        return messages.stream()
            .map(MessageUtil::colorize)
            .toList();
    }
    
    /**
     * Удаление цветовых кодов
     */
    public static String stripColor(String message) {
        if (message == null) return "";
        return ChatColor.stripColor(colorize(message));
    }
    
    /**
     * Отправка сообщения отправителю команды
     */
    public static void send(CommandSender sender, String message) {
        if (sender != null && message != null) {
            sender.sendMessage(colorize(message));
        }
    }
    
    /**
     * Отправка сообщения игроку
     */
    public static void send(Player player, String message) {
        if (player != null && message != null) {
            player.sendMessage(colorize(message));
        }
    }
    
    /**
     * Отправка нескольких сообщений
     */
    public static void send(CommandSender sender, String... messages) {
        if (sender != null && messages != null) {
            for (String message : messages) {
                send(sender, message);
            }
        }
    }
    
    /**
     * Отправка списка сообщений
     */
    public static void send(CommandSender sender, List<String> messages) {
        if (sender != null && messages != null) {
            for (String message : messages) {
                send(sender, message);
            }
        }
    }
    
    /**
     * Отправка заголовка игроку
     */
    public static void sendTitle(Player player, String title, String subtitle) {
        sendTitle(player, title, subtitle, 10, 70, 20);
    }
    
    /**
     * Отправка заголовка игроку с настройкой времени
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player != null) {
            player.sendTitle(
                colorize(title != null ? title : ""),
                colorize(subtitle != null ? subtitle : ""),
                fadeIn, stay, fadeOut
            );
        }
    }
    
    /**
     * Отправка сообщения на панель действий
     */
    public static void sendActionBar(Player player, String message) {
        if (player != null && message != null) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                TextComponent.fromLegacyText(colorize(message)));
        }
    }
    
    /**
     * Отправка сообщения в консоль
     */
    public static void sendConsole(String message) {
        if (message != null) {
            Bukkit.getConsoleSender().sendMessage(colorize(message));
        }
    }
    
    /**
     * Логирование информации
     */
    public static void logInfo(String message) {
        if (message != null) {
            logger.info(stripColor(message));
        }
    }
    
    /**
     * Логирование предупреждения
     */
    public static void logWarning(String message) {
        if (message != null) {
            logger.warning(stripColor(message));
        }
    }
    
    /**
     * Логирование ошибки
     */
    public static void logError(String message) {
        if (message != null) {
            logger.severe(stripColor(message));
        }
    }
    
    /**
     * Логирование ошибки с исключением
     */
    public static void logError(String message, Throwable throwable) {
        if (message != null) {
            logger.severe(stripColor(message));
            if (throwable != null) {
                throwable.printStackTrace();
            }
        }
    }
    
    /**
     * Отправка сообщения всем игрокам
     */
    public static void broadcast(String message) {
        if (message != null) {
            Bukkit.broadcastMessage(colorize(message));
        }
    }
    
    /**
     * Отправка сообщения игрокам с определенным разрешением
     */
    public static void broadcast(String message, String permission) {
        if (message != null && permission != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission(permission)) {
                    send(player, message);
                }
            }
        }
    }
    
    /**
     * Замена плейсхолдеров в сообщении
     */
    public static String replacePlaceholders(String message, Object... replacements) {
        if (message == null) return "";
        
        String result = message;
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                String placeholder = String.valueOf(replacements[i]);
                String replacement = String.valueOf(replacements[i + 1]);
                result = result.replace(placeholder, replacement);
            }
        }
        
        return result;
    }
    
    /**
     * Центрирование текста (для чата)
     */
    public static String center(String message) {
        if (message == null) return "";
        
        int maxLength = 80; // Приблизительная ширина чата
        String stripped = stripColor(message);
        
        if (stripped.length() >= maxLength) {
            return message;
        }
        
        int spaces = (maxLength - stripped.length()) / 2;
        StringBuilder centered = new StringBuilder();
        
        for (int i = 0; i < spaces; i++) {
            centered.append(" ");
        }
        
        centered.append(message);
        return centered.toString();
    }
    
    /**
     * Создание разделителя
     */
    public static String createSeparator(char character, int length) {
        StringBuilder separator = new StringBuilder();
        for (int i = 0; i < length; i++) {
            separator.append(character);
        }
        return separator.toString();
    }
    
    /**
     * Создание разделителя по умолчанию
     */
    public static String createSeparator() {
        return createSeparator('-', 50);
    }
    
    /**
     * Форматирование времени
     */
    public static String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + "д " + (hours % 24) + "ч " + (minutes % 60) + "м";
        } else if (hours > 0) {
            return hours + "ч " + (minutes % 60) + "м " + (seconds % 60) + "с";
        } else if (minutes > 0) {
            return minutes + "м " + (seconds % 60) + "с";
        } else {
            return seconds + "с";
        }
    }
    
    /**
     * Форматирование числа с разделителями
     */
    public static String formatNumber(long number) {
        return String.format("%,d", number);
    }
    
    /**
     * Форматирование числа с плавающей точкой
     */
    public static String formatDecimal(double number) {
        return String.format("%.2f", number);
    }
    
    /**
     * Сокращение длинных чисел (1000 -> 1k)
     */
    public static String formatCompactNumber(long number) {
        if (number >= 1_000_000_000) {
            return String.format("%.1fб", number / 1_000_000_000.0);
        } else if (number >= 1_000_000) {
            return String.format("%.1fм", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fт", number / 1_000.0);
        } else {
            return String.valueOf(number);
        }
    }
    
    /**
     * Проверка, является ли строка числом
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Получение прогресс-бара
     */
    public static String createProgressBar(double percentage, int length, String progressChar, String emptyChar) {
        StringBuilder bar = new StringBuilder();
        int progressLength = (int) (length * (percentage / 100.0));
        
        bar.append("§a");
        for (int i = 0; i < progressLength; i++) {
            bar.append(progressChar);
        }
        
        bar.append("§7");
        for (int i = progressLength; i < length; i++) {
            bar.append(emptyChar);
        }
        
        return bar.toString();
    }
    
    /**
     * Получение прогресс-бара по умолчанию
     */
    public static String createProgressBar(double percentage) {
        return createProgressBar(percentage, 20, "█", "█");
    }
}
