package ru.openhousing.coding.blocks.functions;

import org.bukkit.entity.Player;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок функции для создания переиспользуемого кода
 */
public class FunctionBlock extends CodeBlock {
    
    public FunctionBlock() {
        super(BlockType.FUNCTION);
        setParameter("functionName", "myFunction");
        setParameter("description", "Моя функция");
        setParameter("parameters", "");
        setParameter("returnValue", "");
        setParameter("isPublic", true);
        setParameter("isStatic", false);
        setParameter("maxCalls", 100);
        setParameter("callTimeout", 5000);
        setParameter("saveToDatabase", false);
        setParameter("shareGlobally", false);
        setParameter("requirePermission", false);
        setParameter("permissionNode", "openhousing.function");
        setParameter("logCalls", false);
        setParameter("showDebug", false);
        setParameter("cacheResult", false);
        setParameter("cacheTimeout", 300);
        setParameter("addEffects", false);
        setParameter("effects", "GLOWING:30:1");
        setParameter("spawnParticles", false);
        setParameter("particleType", "ENCHANTMENT_TABLE");
        setParameter("particleCount", 10);
        setParameter("playSound", false);
        setParameter("soundType", "ENTITY_PLAYER_LEVELUP");
        setParameter("broadcastCall", false);
        setParameter("broadcastMessage", "§e%player_name% вызвал функцию %function_name%");
        setParameter("saveCallHistory", false);
        setParameter("maxHistorySize", 100);
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден");
        }
        
        try {
            String functionName = (String) getParameter("functionName");
            String description = (String) getParameter("description");
            String parameters = (String) getParameter("parameters");
            String returnValue = (String) getParameter("returnValue");
            boolean isPublic = (Boolean) getParameter("isPublic");
            boolean isStatic = (Boolean) getParameter("isStatic");
            Integer maxCalls = (Integer) getParameter("maxCalls");
            Integer callTimeout = (Integer) getParameter("callTimeout");
            boolean saveToDatabase = (Boolean) getParameter("saveToDatabase");
            boolean shareGlobally = (Boolean) getParameter("shareGlobally");
            boolean requirePermission = (Boolean) getParameter("requirePermission");
            String permissionNode = (String) getParameter("permissionNode");
            boolean logCalls = (Boolean) getParameter("logCalls");
            boolean showDebug = (Boolean) getParameter("showDebug");
            boolean cacheResult = (Boolean) getParameter("cacheResult");
            Integer cacheTimeout = (Integer) getParameter("cacheTimeout");
            boolean addEffects = (Boolean) getParameter("addEffects");
            String effects = (String) getParameter("effects");
            boolean spawnParticles = (Boolean) getParameter("spawnParticles");
            String particleType = (String) getParameter("particleType");
            Integer particleCount = (Integer) getParameter("particleCount");
            boolean playSound = (Boolean) getParameter("playSound");
            String soundType = (String) getParameter("soundType");
            boolean broadcastCall = (Boolean) getParameter("broadcastCall");
            String broadcastMessage = (String) getParameter("broadcastMessage");
            boolean saveCallHistory = (Boolean) getParameter("saveCallHistory");
            Integer maxHistorySize = (Integer) getParameter("maxHistorySize");
            
            // Проверка разрешений
            if (requirePermission && permissionNode != null) {
                if (!player.hasPermission(permissionNode)) {
                    player.sendMessage("§cУ вас нет разрешения на использование этой функции!");
                    return ExecutionResult.error("Недостаточно прав");
                }
            }
            
            // Проверка лимита вызовов
            if (maxCalls != null && maxCalls > 0) {
                String callCountVar = "function_calls_" + functionName;
                Object currentCalls = context.getVariable(callCountVar);
                int calls = currentCalls instanceof Number ? ((Number) currentCalls).intValue() : 0;
                
                if (calls >= maxCalls) {
                    player.sendMessage("§cДостигнут лимит вызовов функции: " + maxCalls);
                    return ExecutionResult.error("Превышен лимит вызовов");
                }
                
                context.setVariable(callCountVar, calls + 1);
            }
            
            // Логирование вызова
            if (logCalls) {
                logFunctionCall(player, functionName, parameters);
            }
            
            // Показ отладочной информации
            if (showDebug) {
                showDebugInfo(player, functionName, parameters, returnValue);
            }
            
            // Трансляция вызова
            if (broadcastCall && broadcastMessage != null) {
                String message = replaceVariables(broadcastMessage, context);
                message = message.replace("%function_name%", functionName);
                player.getServer().broadcastMessage(message);
            }
            
            // Сохранение истории вызовов
            if (saveCallHistory && maxHistorySize != null) {
                saveCallHistory(player, functionName, parameters, maxHistorySize);
            }
            
            // Добавление эффектов
            if (addEffects && effects != null) {
                addFunctionEffects(player, effects);
            }
            
            // Создание частиц
            if (spawnParticles && particleType != null && particleCount != null) {
                spawnFunctionParticles(player, particleType, particleCount);
            }
            
            // Воспроизведение звука
            if (playSound && soundType != null) {
                playFunctionSound(player, soundType);
            }
            
            // Выполнение дочерних блоков (тело функции)
            long startTime = System.currentTimeMillis();
            ExecutionResult childResult = executeChildren(context);
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Проверка таймаута
            if (callTimeout != null && executionTime > callTimeout) {
                player.sendMessage("§cФункция выполнялась слишком долго: " + executionTime + "ms");
                return ExecutionResult.error("Превышен таймаут выполнения");
            }
            
            // Сохранение в базу данных
            if (saveToDatabase) {
                saveFunctionToDatabase(player, functionName, description, parameters, returnValue, isPublic, shareGlobally);
            }
            
            // Кэширование результата
            if (cacheResult && cacheTimeout != null) {
                cacheFunctionResult(player, functionName, childResult, cacheTimeout);
            }
            
            // Возврат значения
            if (returnValue != null && !returnValue.trim().isEmpty()) {
                Object returnObj = evaluateReturnValue(returnValue, context);
                return ExecutionResult.returnValue(returnObj);
            }
            
            return childResult;
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения функции: " + e.getMessage());
        }
    }
    
    /**
     * Логировать вызов функции
     */
    private void logFunctionCall(Player player, String functionName, String parameters) {
        String logMessage = String.format(
            "[FUNCTION] Player %s called function %s with parameters: %s",
            player.getName(),
            functionName,
            parameters
        );
        
        System.out.println(logMessage);
    }
    
    /**
     * Показать отладочную информацию
     */
    private void showDebugInfo(Player player, String functionName, String parameters, String returnValue) {
        String debugMessage = String.format(
            "§8[DEBUG] Функция: §7%s §8| Параметры: §7%s §8| Возврат: §7%s",
            functionName,
            parameters,
            returnValue
        );
        player.sendMessage(debugMessage);
    }
    
    /**
     * Сохранить историю вызовов
     */
    private void saveCallHistory(Player player, String functionName, String parameters, Integer maxHistorySize) {
        String historyVar = "function_history_" + functionName;
        Object history = player.getServer().getPluginManager().getPlugin("OpenHousing")
            .getConfig().get(historyVar);
        
        // Здесь можно добавить логику сохранения истории
        System.out.println("[FUNCTION_HISTORY] Saving call history for " + functionName);
    }
    
    /**
     * Добавить эффекты функции
     */
    private void addFunctionEffects(Player player, String effectsStr) {
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
     * Создать частицы функции
     */
    private void spawnFunctionParticles(Player player, String particleType, Integer particleCount) {
        try {
            org.bukkit.Particle particle = org.bukkit.Particle.valueOf(particleType);
            player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1, 0), particleCount);
        } catch (IllegalArgumentException e) {
            // Тип частиц не найден, игнорируем
        }
    }
    
    /**
     * Воспроизвести звук функции
     */
    private void playFunctionSound(Player player, String soundType) {
        try {
            org.bukkit.Sound sound = org.bukkit.Sound.valueOf(soundType);
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            // Звук не найден, игнорируем
        }
    }
    
    /**
     * Сохранить функцию в базу данных
     */
    private void saveFunctionToDatabase(Player player, String functionName, String description, String parameters, String returnValue, boolean isPublic, boolean shareGlobally) {
        // Здесь можно добавить сохранение в базу данных
        System.out.println("[FUNCTION_DB] Saving function: " + functionName + " by " + player.getName());
    }
    
    /**
     * Кэшировать результат функции
     */
    private void cacheFunctionResult(Player player, String functionName, ExecutionResult result, Integer cacheTimeout) {
        // Здесь можно добавить кэширование результата
        System.out.println("[FUNCTION_CACHE] Caching result for function: " + functionName);
    }
    
    /**
     * Вычислить возвращаемое значение
     */
    private Object evaluateReturnValue(String returnValue, ExecutionContext context) {
        // Простая замена переменных
        return replaceVariables(returnValue, context);
    }
    
    /**
     * Получить количество параметров функции
     */
    public int getParameterCount() {
        String parameters = (String) getParameter("parameters");
        if (parameters == null || parameters.trim().isEmpty()) {
            return 0;
        }
        return parameters.split(",").length;
    }
    
    /**
     * Получить список параметров функции
     */
    public String[] getFunctionParameters() {
        String parameters = (String) getParameter("parameters");
        if (parameters == null || parameters.trim().isEmpty()) {
            return new String[0];
        }
        return parameters.split(",");
    }
    
    @Override
    public boolean validate() {
        return getParameter("functionName") != null;
    }
    
    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "§7Создает переиспользуемую функцию",
            "",
            "§eПараметры:",
            "§7• Имя функции",
            "§7• Описание",
            "§7• Параметры (через запятую)",
            "§7• Возвращаемое значение",
            "§7• Публичная/приватная",
            "§7• Статическая/динамическая",
            "§7• Лимит вызовов",
            "§7• Таймаут выполнения",
            "§7• Сохранение в БД",
            "§7• Глобальное распространение",
            "§7• Проверка разрешений",
            "§7• Логирование вызовов",
            "§7• Отладочная информация",
            "§7• Кэширование результата",
            "§7• Эффекты и частицы",
            "§7• Звуки",
            "§7• Трансляция вызовов",
            "§7• История вызовов"
        );
    }
    
    @Override
    public boolean matchesEvent(Object event) {
        return false; // Функции не привязаны к событиям
    }
    
    @Override
    public ExecutionContext createContextFromEvent(Object event) {
        return null; // Функции не создают контекст из событий
    }
}
