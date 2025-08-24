package ru.openhousing.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Утилита для работы с сообщениями
 */
public class MessageUtil {

    private static volatile Logger logger = null;
    private static volatile Plugin pluginInstance = null; // Для сохранения ссылки на плагин

    /**
     * Инициализация MessageUtil с плагином
     * Должна вызываться из OpenHousing#onEnable.
     */
    public static void initialize(Plugin plugin) {
        if (pluginInstance == null) { // Инициализируем только один раз
            pluginInstance = plugin;
            logger = plugin.getLogger();
            // Optional: for development, log a message here when initialized
            if (logger != null) {
                logger.info("MessageUtil initialized with plugin: " + plugin.getName());
            }
        }
    }

    /**
     * Получение логгера с fallback для использования в тестовой среде.
     */
    private static Logger getLoggerOrFallback() {
        if (logger != null) {
            return logger;
        }
        // Этот блок выполняется, если плагин не инициализирован (в юнит-тестах или до onEnable)
        try {
            // Если Bukkit.getServer() доступен, значит есть какое-то окружение Bukkit (хотя бы mock)
            if (Bukkit.getServer() != null) {
                return Bukkit.getLogger();
            }
        } catch (NoClassDefFoundError | Exception ignored) {
            // Игнорируем ошибки при доступе к Bukkit API вне его среды
        }
        // Используем стандартный Java-логгер как окончательный фоллбэк
        return Logger.getLogger("OpenHousingFallbackLogger");
    }

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
        if (messages == null) return new ArrayList<>(); // Возвращаем пустой список, а не null
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
     * Удаление цветовых кодов (alias для stripColor)
     */
    public static String stripColors(String message) {
        return stripColor(message);
    }

    /**
     * Окрашивание списка строк (alias для colorize)
     */
    public static List<String> colorizeList(List<String> messages) {
        return colorize(messages);
    }

    /**
     * Удаление цветовых кодов из списка строк
     */
    public static List<String> stripColorsList(List<String> messages) {
        if (messages == null) return new ArrayList<>(); // Возвращаем пустой список, а не null
        return messages.stream()
            .map(MessageUtil::stripColors)
            .toList();
    }

    /**
     * Форматирование сообщения с параметрами
     */
    public static String format(String template, String... params) {
        if (template == null) return "";
        String result = template;
        for (int i = 0; i < params.length; i++) {
            result = result.replace("{" + i + "}", params[i] != null ? params[i] : "");
        }
        return colorize(result);
    }
    
    // Остальные перегрузки format() теперь будут автоматически использовать основной format(String, String...)
    // Их можно упростить до одного вызова.
    public static String format(String name, String value) {
        return format("&e{0}: &f{1}", name, value);
    }

    public static String format(String name, String value, String info) {
        return format("&e{0}: &f{1} &7({2})", name, value, info);
    }

    public static String format(String name, String value, String info, int number) {
        return format("&e{0}: &f{1} &7({2}) &6[{3}]", name, value, info, String.valueOf(number));
    }

    public static String format(String name, String value, String info, String extra) {
        return format("&e{0}: &f{1} &7({2}) &a{3}", name, value, info, extra);
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
            try {
                // Если Bukkit.getServer() доступен, значит есть какое-то окружение Bukkit (хотя бы mock)
                if (Bukkit.getServer() != null) {
                    player.sendTitle(
                        colorize(title != null ? title : ""),
                        colorize(subtitle != null ? subtitle : ""),
                        fadeIn, stay, fadeOut
                    );
                } else {
                    // Fallback для тестов
                    getLoggerOrFallback().info("[TITLE] " + player.getName() + ": " + stripColor(title) + " | " + stripColor(subtitle));
                }
            } catch (NoClassDefFoundError | Exception e) {
                getLoggerOrFallback().warning("Failed to send title to " + player.getName() + " (error in test or missing API): " + e.getMessage());
            }
        }
    }

    /**
     * Отправка сообщения на панель действий
     */
    public static void sendActionBar(Player player, String message) {
        if (player != null && message != null) {
            try {
                if (Bukkit.getServer() != null) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText(colorize(message)));
                } else {
                    getLoggerOrFallback().info("[ACTION BAR] " + player.getName() + ": " + stripColor(message));
                }
            } catch (NoClassDefFoundError | Exception e) {
                getLoggerOrFallback().warning("Failed to send action bar to " + player.getName() + " (error in test or missing API): " + e.getMessage());
            }
        }
    }

    /**
     * Отправка сообщения в консоль
     */
    public static void sendConsole(String message) {
        if (message != null) {
            try {
                if (Bukkit.getServer() != null && Bukkit.getConsoleSender() != null) {
                    Bukkit.getConsoleSender().sendMessage(colorize(message));
                } else {
                    getLoggerOrFallback().info("[CONSOLE] " + stripColor(message));
                }
            } catch (NoClassDefFoundError | Exception e) {
                getLoggerOrFallback().info("[CONSOLE] " + stripColor(message));
            }
        }
    }

    /**
     * Логирование информации
     */
    public static void logInfo(String message) {
        if (message != null) {
            Logger currentLogger = getLoggerOrFallback();
            if (currentLogger != null) {
                currentLogger.info(stripColor(message));
            }
        }
    }

    /**
     * Логирование предупреждения
     */
    public static void logWarning(String message) {
        if (message != null) {
            Logger currentLogger = getLoggerOrFallback();
            if (currentLogger != null) {
                currentLogger.warning(stripColor(message));
            }
        }
    }

    /**
     * Логирование ошибки
     */
    public static void logError(String message) {
        if (message != null) {
            Logger currentLogger = getLoggerOrFallback();
            if (currentLogger != null) {
                currentLogger.severe(stripColor(message));
            }
        }
    }

    /**
     * Логирование ошибки с исключением
     */
    public static void logError(String message, Throwable throwable) {
        if (message != null) {
            Logger currentLogger = getLoggerOrFallback();
            if (currentLogger != null) {
                currentLogger.severe(stripColor(message));
                if (throwable != null) {
                    throwable.printStackTrace();
                }
            }
        }
    }

    /**
     * Отправка сообщения всем игрокам
     */
    public static void broadcast(String message) {
        if (message != null) {
            try {
                if (Bukkit.getServer() != null) {
                    Bukkit.broadcastMessage(colorize(message));
                } else {
                    getLoggerOrFallback().info("[BROADCAST] " + stripColor(message));
                }
            } catch (NoClassDefFoundError | Exception e) {
                getLoggerOrFallback().info("[BROADCAST] " + stripColor(message));
            }
        }
    }

    /**
     * Отправка сообщения игрокам с определенным разрешением
     */
    public static void broadcast(String message, String permission) {
        if (message != null && permission != null) {
            try {
                if (Bukkit.getServer() != null) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.hasPermission(permission)) {
                            send(player, message);
                        }
                    }
                } else {
                    getLoggerOrFallback().info("[BROADCAST w/PERMISSION] " + stripColor(message) + " (Perm: " + permission + ")");
                }
            } catch (NoClassDefFoundError | Exception e) {
                getLoggerOrFallback().info("[BROADCAST w/PERMISSION] " + stripColor(message) + " (Perm: " + permission + ")");
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
