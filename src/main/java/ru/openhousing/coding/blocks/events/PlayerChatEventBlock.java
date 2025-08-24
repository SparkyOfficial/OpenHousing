package ru.openhousing.coding.blocks.events;

import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок события отправки сообщения в чат
 */
public class PlayerChatEventBlock extends CodeBlock {
    
    public PlayerChatEventBlock() {
        super(BlockType.PLAYER_CHAT);
        setParameter("messageFilter", "ANY");
        setParameter("filterPattern", "");
        setParameter("caseSensitive", false);
        setParameter("responseMessage", "§aСообщение получено!");
        setParameter("showTitle", false);
        setParameter("titleText", "§aСообщение отправлено!");
        setParameter("subtitleText", "§7%message%");
        setParameter("playSound", false);
        setParameter("soundType", "ENTITY_EXPERIENCE_ORB_PICKUP");
        setParameter("cancelMessage", false);
        setParameter("modifyMessage", false);
        setParameter("newMessage", "%player_name%: %message%");
        setParameter("addPrefix", false);
        setParameter("prefix", "§7[Чат] ");
        setParameter("addSuffix", false);
        setParameter("suffix", " §7[%time%]");
        setParameter("filterWords", false);
        setParameter("filteredWords", "bad,word,spam");
        setParameter("replaceFiltered", true);
        setParameter("replacementChar", "*");
        setParameter("checkPermissions", false);
        setParameter("permissionNode", "openhousing.chat");
        setParameter("denyMessage", "§cУ вас нет разрешения отправлять сообщения!");
        setParameter("rateLimit", false);
        setParameter("rateLimitSeconds", 3);
        setParameter("rateLimitMessage", "§cПодождите %seconds% секунд перед отправкой следующего сообщения!");
        setParameter("trackStatistics", true);
        setParameter("messageCountVariable", "messages_sent");
        setParameter("lastMessageVariable", "last_message");
        setParameter("lastMessageTimeVariable", "last_message_time");
        setParameter("messageLengthVariable", "message_length");
        setParameter("broadcastToStaff", false);
        setParameter("staffMessage", "§e[Staff] %player_name%: %message%");
        setParameter("staffPermission", "openhousing.staff");
        setParameter("logChat", false);
        setParameter("saveToDatabase", false);
        setParameter("addEffects", false);
        setParameter("effects", "GLOWING:30:1");
        setParameter("spawnParticles", false);
        setParameter("particleType", "HEART");
        setParameter("particleCount", 5);
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден");
        }
        
        try {
            // Получаем сообщение из контекста
            String message = (String) context.getVariable("message");
            if (message == null) {
                return ExecutionResult.error("Сообщение не найдено в контексте");
            }
            
            // Проверка разрешений
            if ((Boolean) getParameter("checkPermissions")) {
                String permissionNode = (String) getParameter("permissionNode");
                if (permissionNode != null && !player.hasPermission(permissionNode)) {
                    String denyMessage = replaceVariables((String) getParameter("denyMessage"), context);
                    player.sendMessage(denyMessage);
                    return ExecutionResult.error("Недостаточно прав");
                }
            }
            
            // Проверка фильтра сообщений
            String messageFilter = (String) getParameter("messageFilter");
            String filterPattern = (String) getParameter("filterPattern");
            boolean caseSensitive = (Boolean) getParameter("caseSensitive");
            
            if (!"ANY".equals(messageFilter) && filterPattern != null) {
                String compareMessage = caseSensitive ? message : message.toLowerCase();
                String comparePattern = caseSensitive ? filterPattern : filterPattern.toLowerCase();
                
                if (!compareMessage.contains(comparePattern)) {
                    return ExecutionResult.success(); // Сообщение не подходит под фильтр
                }
            }
            
            // Фильтрация слов
            if ((Boolean) getParameter("filterWords")) {
                message = filterBadWords(message);
            }
            
            // Модификация сообщения
            if ((Boolean) getParameter("modifyMessage")) {
                String newMessageTemplate = (String) getParameter("newMessage");
                if (newMessageTemplate != null) {
                    message = replaceVariables(newMessageTemplate, context);
                }
            }
            
            // Добавление префикса
            if ((Boolean) getParameter("addPrefix")) {
                String prefix = replaceVariables((String) getParameter("prefix"), context);
                if (prefix != null) {
                    message = prefix + message;
                }
            }
            
            // Добавление суффикса
            if ((Boolean) getParameter("addSuffix")) {
                String suffix = replaceVariables((String) getParameter("suffix"), context);
                if (suffix != null) {
                    message = message + suffix;
                }
            }
            
            // Проверка ограничения скорости
            if ((Boolean) getParameter("rateLimit")) {
                if (!checkRateLimit(player, context)) {
                    return ExecutionResult.error("Превышен лимит сообщений");
                }
            }
            
            // Отмена сообщения
            if ((Boolean) getParameter("cancelMessage")) {
                // Сообщение будет отменено, но мы все равно выполняем остальные действия
            }
            
            // Ответное сообщение
            String responseMessage = replaceVariables((String) getParameter("responseMessage"), context);
            if (responseMessage != null && !responseMessage.trim().isEmpty()) {
                player.sendMessage(responseMessage);
            }
            
            // Показать заголовок
            if ((Boolean) getParameter("showTitle")) {
                String titleText = replaceVariables((String) getParameter("titleText"), context);
                String subtitleText = replaceVariables((String) getParameter("subtitleText"), context);
                player.sendTitle(titleText, subtitleText, 10, 70, 20);
            }
            
            // Воспроизвести звук
            if ((Boolean) getParameter("playSound")) {
                String soundType = (String) getParameter("soundType");
                if (soundType != null) {
                    try {
                        org.bukkit.Sound sound = org.bukkit.Sound.valueOf(soundType);
                        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                    } catch (IllegalArgumentException e) {
                        // Звук не найден, игнорируем
                    }
                }
            }
            
            // Добавить эффекты
            if ((Boolean) getParameter("addEffects")) {
                String effects = (String) getParameter("effects");
                if (effects != null) {
                    addChatEffects(player, effects);
                }
            }
            
            // Создать частицы
            if ((Boolean) getParameter("spawnParticles")) {
                String particleType = (String) getParameter("particleType");
                Integer particleCount = (Integer) getParameter("particleCount");
                
                if (particleType != null && particleCount != null) {
                    try {
                        org.bukkit.Particle particle = org.bukkit.Particle.valueOf(particleType);
                        player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1, 0), particleCount);
                    } catch (IllegalArgumentException e) {
                        // Тип частиц не найден, игнорируем
                    }
                }
            }
            
            // Отслеживание статистики
            if ((Boolean) getParameter("trackStatistics")) {
                trackChatStatistics(player, context, message);
            }
            
            // Трансляция персоналу
            if ((Boolean) getParameter("broadcastToStaff")) {
                broadcastToStaff(player, context, message);
            }
            
            // Логирование чата
            if ((Boolean) getParameter("logChat")) {
                logChatMessage(player, context, message);
            }
            
            // Сохранение в базу данных
            if ((Boolean) getParameter("saveToDatabase")) {
                saveChatToDatabase(player, context, message);
            }
            
            // Добавляем информацию о событии в контекст
            context.setVariable("chat_time", System.currentTimeMillis());
            context.setVariable("chat_message", message);
            context.setVariable("chat_length", message.length());
            context.setVariable("chat_contains_links", message.contains("http") || message.contains("www"));
            context.setVariable("chat_contains_caps", message.equals(message.toUpperCase()) && message.length() > 3);
            
            return ExecutionResult.success();
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения события чата: " + e.getMessage());
        }
    }
    
    /**
     * Фильтрация плохих слов
     */
    private String filterBadWords(String message) {
        String filteredWords = (String) getParameter("filteredWords");
        boolean replaceFiltered = (Boolean) getParameter("replaceFiltered");
        String replacementChar = (String) getParameter("replacementChar");
        
        if (filteredWords != null) {
            String[] badWords = filteredWords.split(",");
            for (String badWord : badWords) {
                String word = badWord.trim();
                if (!word.isEmpty()) {
                    if (replaceFiltered && replacementChar != null) {
                        String replacement = replacementChar.repeat(word.length());
                        message = message.replaceAll("(?i)" + word, replacement);
                    } else {
                        message = message.replaceAll("(?i)" + word, "");
                    }
                }
            }
        }
        
        return message;
    }
    
    /**
     * Проверка ограничения скорости
     */
    private boolean checkRateLimit(Player player, ExecutionContext context) {
        Integer rateLimitSeconds = (Integer) getParameter("rateLimitSeconds");
        if (rateLimitSeconds == null || rateLimitSeconds <= 0) {
            return true;
        }
        
        Object lastMessageTime = context.getVariable("last_message_time");
        if (lastMessageTime instanceof Number) {
            long lastTime = ((Number) lastMessageTime).longValue();
            long currentTime = System.currentTimeMillis();
            long timeDiff = (currentTime - lastTime) / 1000; // Конвертируем в секунды
            
            if (timeDiff < rateLimitSeconds) {
                String rateLimitMessage = replaceVariables((String) getParameter("rateLimitMessage"), context);
                rateLimitMessage = rateLimitMessage.replace("%seconds%", String.valueOf(rateLimitSeconds - timeDiff));
                player.sendMessage(rateLimitMessage);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Добавить эффекты чата
     */
    private void addChatEffects(Player player, String effectsStr) {
        String[] effects = effectsStr.split(",");
        for (String effect : effects) {
            String[] parts = effect.trim().split(":");
            if (parts.length >= 2) {
                try {
                    org.bukkit.potion.PotionEffectType type = org.bukkit.potion.PotionEffectType.getByName(parts[0].toUpperCase());
                    int duration = Integer.parseInt(parts[1]) * 20; // Конвертируем в тики
                    int amplifier = parts.length > 2 ? Integer.parseInt(parts[2]) - 1 : 0;
                    
                    if (type != null) {
                        player.addPotionEffect(new org.bukkit.potion.PotionEffect(type, duration, amplifier));
                    }
                } catch (NumberFormatException e) {
                    // Игнорируем некорректные значения
                }
            }
        }
    }
    
    /**
     * Отслеживание статистики чата
     */
    private void trackChatStatistics(Player player, ExecutionContext context, String message) {
        // Общий счетчик сообщений
        String messageCountVariable = (String) getParameter("messageCountVariable");
        if (messageCountVariable != null) {
            Object currentMessages = context.getVariable(messageCountVariable);
            int messages = currentMessages instanceof Number ? ((Number) currentMessages).intValue() : 0;
            context.setVariable(messageCountVariable, messages + 1);
        }
        
        // Последнее сообщение
        String lastMessageVariable = (String) getParameter("lastMessageVariable");
        if (lastMessageVariable != null) {
            context.setVariable(lastMessageVariable, message);
        }
        
        // Время последнего сообщения
        String lastMessageTimeVariable = (String) getParameter("lastMessageTimeVariable");
        if (lastMessageTimeVariable != null) {
            context.setVariable(lastMessageTimeVariable, System.currentTimeMillis());
        }
        
        // Длина сообщения
        String messageLengthVariable = (String) getParameter("messageLengthVariable");
        if (messageLengthVariable != null) {
            context.setVariable(messageLengthVariable, message.length());
        }
        
        // Статистика по символам
        context.setVariable("message_char_count", message.length());
        context.setVariable("message_word_count", message.split("\\s+").length);
        context.setVariable("message_contains_emojis", message.matches(".*[\\p{So}\\p{Emoji}].*"));
    }
    
    /**
     * Трансляция персоналу
     */
    private void broadcastToStaff(Player player, ExecutionContext context, String message) {
        String staffMessage = replaceVariables((String) getParameter("staffMessage"), context);
        String staffPermission = (String) getParameter("staffPermission");
        
        if (staffMessage != null && staffPermission != null) {
            for (Player onlinePlayer : player.getServer().getOnlinePlayers()) {
                if (onlinePlayer.hasPermission(staffPermission)) {
                    onlinePlayer.sendMessage(staffMessage);
                }
            }
        }
    }
    
    /**
     * Логировать сообщение чата
     */
    private void logChatMessage(Player player, ExecutionContext context, String message) {
        String logMessage = String.format(
            "[CHAT] Player %s: %s (World: %s, Location: %s)",
            player.getName(),
            message,
            player.getWorld().getName(),
            player.getLocation().toString()
        );
        
        // Здесь можно добавить логирование в файл или базу данных
        System.out.println(logMessage);
    }
    
    /**
     * Сохранить сообщение в базу данных
     */
    private void saveChatToDatabase(Player player, ExecutionContext context, String message) {
        // Здесь можно добавить сохранение в базу данных
        // Например, через DatabaseManager
        System.out.println("[CHAT_DB] Saving message from " + player.getName() + ": " + message);
    }
    
    @Override
    public boolean validate() {
        return getParameter("messageFilter") != null;
    }
    
    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "§7Срабатывает при отправке сообщения в чат",
            "",
            "§eПараметры:",
            "§7• Фильтр сообщений",
            "§7• Фильтрация слов",
            "§7• Модификация сообщения",
            "§7• Префикс/суффикс",
            "§7• Ограничение скорости",
            "§7• Отмена сообщения",
            "§7• Ответное сообщение",
            "§7• Показать заголовок",
            "§7• Воспроизвести звук",
            "§7• Добавить эффекты",
            "§7• Создать частицы",
            "§7• Отслеживание статистики",
            "§7• Трансляция персоналу",
            "§7• Логирование",
            "§7• Сохранение в БД",
            "§7• Проверка разрешений"
        );
    }
    
    @Override
    public boolean matchesEvent(Object event) {
        return event instanceof AsyncPlayerChatEvent;
    }
    
    @Override
    public ExecutionContext createContextFromEvent(Object event) {
        if (event instanceof AsyncPlayerChatEvent) {
            AsyncPlayerChatEvent chatEvent = (AsyncPlayerChatEvent) event;
            ExecutionContext context = new ExecutionContext(chatEvent.getPlayer());
            context.setVariable("message", chatEvent.getMessage());
            context.setVariable("recipients", chatEvent.getRecipients());
            context.setVariable("format", chatEvent.getFormat());
            return context;
        }
        return null;
    }
}
