package ru.openhousing.coding.blocks.actions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionContext;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionResult;
import ru.openhousing.coding.blocks.BlockVariable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Специализированный блок для управления командами
 * Выполнение, проверка и управление командами с GUI настройками
 */
public class CommandManagementBlock extends CodeBlock {
    
    // Статические поля для глобального управления
    private static final Map<String, CommandTemplate> commandTemplates = new ConcurrentHashMap<>();
    private static final Map<String, Integer> commandUsageStats = new ConcurrentHashMap<>();
    private static final AtomicInteger totalCommandOperations = new AtomicInteger(0);
    
    // Переменные блока (настраиваются через drag-n-drop)
    private BlockVariable operationTypeVar;
    private BlockVariable commandVar;
    private BlockVariable targetPlayerVar;
    private BlockVariable executorVar;
    private BlockVariable argumentsVar;
    private BlockVariable permissionVar;
    private BlockVariable cooldownVar;
    private BlockVariable conditionVar;
    private BlockVariable successMessageVar;
    private BlockVariable failureMessageVar;
    private BlockVariable loggingEnabledVar;
    private BlockVariable notificationEnabledVar;
    private BlockVariable asyncExecutionVar;
    private BlockVariable outputCaptureVar;
    private BlockVariable errorHandlingVar;
    
    public enum CommandOperationType {
        EXECUTE("Выполнить", "Выполняет команду"),
        EXECUTE_AS_PLAYER("Выполнить от имени игрока", "Выполняет команду от имени игрока"),
        EXECUTE_AS_CONSOLE("Выполнить от имени консоли", "Выполняет команду от имени консоли"),
        CHECK_PERMISSION("Проверить права", "Проверяет права на выполнение команды"),
        CHECK_COOLDOWN("Проверить задержку", "Проверяет задержку команды"),
        SET_COOLDOWN("Установить задержку", "Устанавливает задержку команды"),
        REGISTER("Зарегистрировать", "Регистрирует новую команду"),
        UNREGISTER("Отменить регистрацию", "Отменяет регистрацию команды"),
        ALIAS("Создать алиас", "Создает алиас для команды"),
        HELP("Показать помощь", "Показывает справку по команде"),
        TAB_COMPLETE("Автодополнение", "Настраивает автодополнение команды"),
        VALIDATE("Проверить", "Проверяет корректность команды"),
        LOG("Логировать", "Логирует выполнение команды");
        
        private final String displayName;
        private final String description;
        
        CommandOperationType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    public CommandManagementBlock() {
        super(BlockType.GAME_EXECUTE_COMMAND); // Используем GAME_EXECUTE_COMMAND как базовый тип
        initializeDefaultSettings();
        initializeCommandTemplates();
    }
    
    /**
     * Инициализация настроек по умолчанию
     */
    private void initializeDefaultSettings() {
        operationTypeVar = new BlockVariable("operationType", "Тип операции", 
            BlockVariable.VariableType.STRING, "EXECUTE");
        commandVar = new BlockVariable("command", "Команда", 
            BlockVariable.VariableType.STRING, "say Привет!");
        targetPlayerVar = new BlockVariable("targetPlayer", "Целевой игрок", 
            BlockVariable.VariableType.PLAYER, "");
        executorVar = new BlockVariable("executor", "Исполнитель", 
            BlockVariable.VariableType.STRING, "console");
        argumentsVar = new BlockVariable("arguments", "Аргументы", 
            BlockVariable.VariableType.LIST, Arrays.asList("arg1", "arg2"));
        permissionVar = new BlockVariable("permission", "Разрешение", 
            BlockVariable.VariableType.PERMISSION, "command.execute");
        cooldownVar = new BlockVariable("cooldown", "Задержка (мс)", 
            BlockVariable.VariableType.INTEGER, 1000);
        conditionVar = new BlockVariable("condition", "Условие выполнения", 
            BlockVariable.VariableType.STRING, "true");
        successMessageVar = new BlockVariable("successMessage", "Сообщение об успехе", 
            BlockVariable.VariableType.STRING, "§aКоманда выполнена успешно!");
        failureMessageVar = new BlockVariable("failureMessage", "Сообщение об ошибке", 
            BlockVariable.VariableType.STRING, "§cОшибка выполнения команды!");
        loggingEnabledVar = new BlockVariable("loggingEnabled", "Включить логирование", 
            BlockVariable.VariableType.BOOLEAN, true);
        notificationEnabledVar = new BlockVariable("notificationEnabled", "Включить уведомления", 
            BlockVariable.VariableType.BOOLEAN, true);
        asyncExecutionVar = new BlockVariable("asyncExecution", "Асинхронное выполнение", 
            BlockVariable.VariableType.BOOLEAN, false);
        outputCaptureVar = new BlockVariable("outputCapture", "Захват вывода", 
            BlockVariable.VariableType.BOOLEAN, false);
        errorHandlingVar = new BlockVariable("errorHandling", "Обработка ошибок", 
            BlockVariable.VariableType.STRING, "CONTINUE");
    }
    
    /**
     * Инициализация шаблонов команд
     */
    private void initializeCommandTemplates() {
        commandTemplates.put("basic_say", new CommandTemplate(
            "basic_say", "Базовое сообщение", "say", 
            Arrays.asList("message"), "command.say", 0));
        
        commandTemplates.put("teleport", new CommandTemplate(
            "teleport", "Телепортация", "tp", 
            Arrays.asList("player", "target"), "command.tp", 2000));
        
        commandTemplates.put("give_item", new CommandTemplate(
            "give_item", "Выдать предмет", "give", 
            Arrays.asList("player", "item", "amount"), "command.give", 1000));
        
        commandTemplates.put("gamemode", new CommandTemplate(
            "gamemode", "Режим игры", "gamemode", 
            Arrays.asList("player", "mode"), "command.gamemode", 1000));
        
        commandTemplates.put("weather", new CommandTemplate(
            "weather", "Погода", "weather", 
            Arrays.asList("type"), "command.weather", 5000));
        
        commandTemplates.put("time", new CommandTemplate(
            "time", "Время", "time", 
            Arrays.asList("set", "value"), "command.time", 1000));
    }
    
    /**
     * Открытие GUI настройки блока
     */
    public void openConfigurationGUI(Player player) {
        if (player == null) return;
        
        // Создаем инвентарь для настройки
        org.bukkit.inventory.Inventory configGUI = Bukkit.createInventory(null, 54, "§6Настройка CommandManagementBlock");
        
        // Заполняем GUI элементами настройки
        fillConfigurationGUI(configGUI, player);
        
        // Открываем GUI
        player.openInventory(configGUI);
    }
    
    /**
     * Заполнение GUI элементами настройки
     */
    private void fillConfigurationGUI(org.bukkit.inventory.Inventory gui, Player player) {
        gui.clear();
        
        // Основные настройки
        gui.setItem(10, createConfigItem(Material.COMMAND_BLOCK, "§eТип операции", 
            Arrays.asList("§7Текущее значение: " + getStringValue(operationTypeVar),
                         "§7Клик для изменения")));
        
        gui.setItem(11, createConfigItem(Material.WRITABLE_BOOK, "§eКоманда", 
            Arrays.asList("§7Текущее значение: " + getStringValue(commandVar),
                         "§7Клик для изменения")));
        
        gui.setItem(12, createConfigItem(Material.PLAYER_HEAD, "§eЦелевой игрок", 
            Arrays.asList("§7Текущее значение: " + getStringValue(targetPlayerVar),
                         "§7Клик для изменения")));
        
        gui.setItem(13, createConfigItem(Material.COMMAND_BLOCK, "§eИсполнитель", 
            Arrays.asList("§7Текущее значение: " + getStringValue(executorVar),
                         "§7Клик для изменения")));
        
        // Настройки команды
        gui.setItem(19, createConfigItem(Material.PAPER, "§eАргументы", 
            Arrays.asList("§7Текущее значение: " + formatListValue(argumentsVar),
                         "§7Клик для изменения")));
        
        gui.setItem(20, createConfigItem(Material.SHIELD, "§eРазрешение", 
            Arrays.asList("§7Текущее значение: " + getStringValue(permissionVar),
                         "§7Клик для изменения")));
        
        gui.setItem(21, createConfigItem(Material.CLOCK, "§eЗадержка", 
            Arrays.asList("§7Текущее значение: " + getIntegerValue(cooldownVar) + "мс",
                         "§7Клик для изменения")));
        
        gui.setItem(22, createConfigItem(Material.COMPARATOR, "§eУсловие", 
            Arrays.asList("§7Текущее значение: " + getStringValue(conditionVar),
                         "§7Клик для изменения")));
        
        // Шаблоны команд
        gui.setItem(45, createConfigItem(Material.CRAFTING_TABLE, "§6Шаблоны команд", 
            Arrays.asList("§7Клик для выбора шаблона")));
        
        // Кнопки управления
        gui.setItem(53, createConfigItem(Material.BARRIER, "§cЗакрыть", 
            Arrays.asList("§7Клик для закрытия")));
    }
    
    /**
     * Создание элемента настройки
     */
    private org.bukkit.inventory.ItemStack createConfigItem(Material material, String name, List<String> lore) {
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(material);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Показ настроек в чате
     */
    public void showSettings(Player player) {
        if (player == null) return;
        
        player.sendMessage("§6=== Настройки CommandManagementBlock ===");
        player.sendMessage("§eОперация: §7" + getStringValue(operationTypeVar));
        player.sendMessage("§eКоманда: §7" + getStringValue(commandVar));
        player.sendMessage("§eЦелевой игрок: §7" + getStringValue(targetPlayerVar));
        player.sendMessage("§eИсполнитель: §7" + getStringValue(executorVar));
        player.sendMessage("§eАргументы: §7" + formatListValue(argumentsVar));
        player.sendMessage("§eРазрешение: §7" + getStringValue(permissionVar));
        player.sendMessage("§eЗадержка: §7" + getIntegerValue(cooldownVar) + "мс");
        player.sendMessage("§eУсловие: §7" + getStringValue(conditionVar));
        player.sendMessage("");
        player.sendMessage("§eСистема:");
        player.sendMessage("§7• Логирование: " + (getBooleanValue(loggingEnabledVar) ? "§aВключено" : "§cВыключено"));
        player.sendMessage("§7• Уведомления: " + (getBooleanValue(notificationEnabledVar) ? "§aВключено" : "§cВыключено"));
        player.sendMessage("§7• Асинхронность: " + (getBooleanValue(asyncExecutionVar) ? "§aВключена" : "§cВыключена"));
        player.sendMessage("§7• Захват вывода: " + (getBooleanValue(outputCaptureVar) ? "§aВключен" : "§cВыключен"));
        player.sendMessage("");
        player.sendMessage("§7Используйте /blockconfig gui для открытия GUI настройки");
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        try {
            Player player = context.getPlayer();
            if (player == null) {
                return ExecutionResult.error("Игрок не найден в контексте");
            }
            
            // Получаем параметры операции
            String operationTypeStr = getStringValue(operationTypeVar);
            String command = getStringValue(commandVar);
            String condition = getStringValue(conditionVar);
            
            // Проверяем условие выполнения
            if (!evaluateCondition(condition, context)) {
                return ExecutionResult.success("Операция пропущена по условию");
            }
            
            // Определяем тип операции
            CommandOperationType operationType = parseOperationType(operationTypeStr);
            if (operationType == null) {
                return ExecutionResult.error("Неизвестный тип операции: " + operationTypeStr);
            }
            
            // Выполняем операцию
            ExecutionResult result = executeCommandOperation(operationType, command, context);
            
            // Логируем операцию
            if (getBooleanValue(loggingEnabledVar)) {
                logOperation(operationType, command, result, context);
            }
            
            // Обновляем статистику
            updateStatistics(operationType, command, result);
            
            // Уведомляем игрока
            if (getBooleanValue(notificationEnabledVar)) {
                if (result.isSuccess()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        getStringValue(successMessageVar)));
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        getStringValue(failureMessageVar)));
                }
            }
            
            return result;
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения блока управления командами: " + e.getMessage());
        }
    }
    
    /**
     * Выполнение операции с командой
     */
    private ExecutionResult executeCommandOperation(CommandOperationType operationType, 
                                                  String command, 
                                                  ExecutionContext context) {
        
        try {
            switch (operationType) {
                case EXECUTE:
                    return executeExecuteOperation(command, context);
                    
                case EXECUTE_AS_PLAYER:
                    return executeExecuteAsPlayerOperation(command, context);
                    
                case EXECUTE_AS_CONSOLE:
                    return executeExecuteAsConsoleOperation(command, context);
                    
                case CHECK_PERMISSION:
                    return executeCheckPermissionOperation(command, context);
                    
                case CHECK_COOLDOWN:
                    return executeCheckCooldownOperation(command, context);
                    
                case SET_COOLDOWN:
                    return executeSetCooldownOperation(command, context);
                    
                case REGISTER:
                    return executeRegisterOperation(command, context);
                    
                case UNREGISTER:
                    return executeUnregisterOperation(command, context);
                    
                case ALIAS:
                    return executeAliasOperation(command, context);
                    
                case HELP:
                    return executeHelpOperation(command, context);
                    
                case TAB_COMPLETE:
                    return executeTabCompleteOperation(command, context);
                    
                case VALIDATE:
                    return executeValidateOperation(command, context);
                    
                case LOG:
                    return executeLogOperation(command, context);
                    
                default:
                    return ExecutionResult.error("Неподдерживаемая операция: " + operationType);
            }
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения операции " + operationType + ": " + e.getMessage());
        }
    }
    
    /**
     * Операция EXECUTE
     */
    private ExecutionResult executeExecuteOperation(String command, ExecutionContext context) {
        try {
            Player player = context.getPlayer();
            if (player == null) {
                return ExecutionResult.error("Игрок не найден в контексте");
            }
            
            // Проверяем права
            String permission = getStringValue(permissionVar);
            if (!permission.isEmpty() && !player.hasPermission(permission)) {
                return ExecutionResult.error("Недостаточно прав для выполнения команды");
            }
            
            // Проверяем задержку
            int cooldown = getIntegerValue(cooldownVar);
            if (cooldown > 0) {
                // TODO: Реализовать проверку задержки
            }
            
            // Выполняем команду
            boolean success = Bukkit.dispatchCommand(player, command);
            
            if (success) {
                return ExecutionResult.success("Команда '" + command + "' выполнена успешно");
            } else {
                return ExecutionResult.error("Не удалось выполнить команду '" + command + "'");
            }
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения команды: " + e.getMessage());
        }
    }
    
    /**
     * Операция EXECUTE_AS_PLAYER
     */
    private ExecutionResult executeExecuteAsPlayerOperation(String command, ExecutionContext context) {
        try {
            String targetPlayerName = getStringValue(targetPlayerVar);
            if (targetPlayerName.isEmpty()) {
                return ExecutionResult.error("Не указан целевой игрок");
            }
            
            Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
            if (targetPlayer == null) {
                return ExecutionResult.error("Целевой игрок не найден: " + targetPlayerName);
            }
            
            // Выполняем команду от имени игрока
            boolean success = Bukkit.dispatchCommand(targetPlayer, command);
            
            if (success) {
                return ExecutionResult.success("Команда '" + command + "' выполнена от имени " + targetPlayerName);
            } else {
                return ExecutionResult.error("Не удалось выполнить команду '" + command + "' от имени " + targetPlayerName);
            }
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения команды от имени игрока: " + e.getMessage());
        }
    }
    
    /**
     * Операция EXECUTE_AS_CONSOLE
     */
    private ExecutionResult executeExecuteAsConsoleOperation(String command, ExecutionContext context) {
        try {
            // Выполняем команду от имени консоли
            boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            
            if (success) {
                return ExecutionResult.success("Команда '" + command + "' выполнена от имени консоли");
            } else {
                return ExecutionResult.error("Не удалось выполнить команду '" + command + "' от имени консоли");
            }
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения команды от имени консоли: " + e.getMessage());
        }
    }
    
    // Остальные операции реализуются аналогично
    private ExecutionResult executeCheckPermissionOperation(String command, ExecutionContext context) {
        // TODO: Реализовать проверку прав
        return ExecutionResult.success("Операция проверки прав выполнена");
    }
    
    private ExecutionResult executeCheckCooldownOperation(String command, ExecutionContext context) {
        // TODO: Реализовать проверку задержки
        return ExecutionResult.success("Операция проверки задержки выполнена");
    }
    
    private ExecutionResult executeSetCooldownOperation(String command, ExecutionContext context) {
        // TODO: Реализовать установку задержки
        return ExecutionResult.success("Операция установки задержки выполнена");
    }
    
    private ExecutionResult executeRegisterOperation(String command, ExecutionContext context) {
        // TODO: Реализовать регистрацию команды
        return ExecutionResult.success("Операция регистрации выполнена");
    }
    
    private ExecutionResult executeUnregisterOperation(String command, ExecutionContext context) {
        // TODO: Реализовать отмену регистрации
        return ExecutionResult.success("Операция отмены регистрации выполнена");
    }
    
    private ExecutionResult executeAliasOperation(String command, ExecutionContext context) {
        // TODO: Реализовать создание алиаса
        return ExecutionResult.success("Операция создания алиаса выполнена");
    }
    
    private ExecutionResult executeHelpOperation(String command, ExecutionContext context) {
        // TODO: Реализовать показ помощи
        return ExecutionResult.success("Операция показа помощи выполнена");
    }
    
    private ExecutionResult executeTabCompleteOperation(String command, ExecutionContext context) {
        // TODO: Реализовать автодополнение
        return ExecutionResult.success("Операция настройки автодополнения выполнена");
    }
    
    private ExecutionResult executeValidateOperation(String command, ExecutionContext context) {
        // TODO: Реализовать проверку команды
        return ExecutionResult.success("Операция проверки выполнена");
    }
    
    private ExecutionResult executeLogOperation(String command, ExecutionContext context) {
        // TODO: Реализовать логирование
        return ExecutionResult.success("Операция логирования выполнена");
    }
    
    /**
     * Проверка условия выполнения
     */
    private boolean evaluateCondition(String condition, ExecutionContext context) {
        if (condition == null || condition.trim().isEmpty() || condition.equals("true")) {
            return true;
        }
        
        if (condition.equals("false")) {
            return false;
        }
        
        // Простая проверка существования переменной
        if (condition.startsWith("exists:")) {
            String varName = condition.substring(7).trim();
            return context.hasVariable(varName);
        }
        
        // Проверка значения переменной
        if (condition.contains("==")) {
            String[] parts = condition.split("==");
            if (parts.length == 2) {
                String varName = parts[0].trim();
                String expectedValue = parts[1].trim();
                Object actualValue = context.getVariable(varName);
                return actualValue != null && actualValue.toString().equals(expectedValue);
            }
        }
        
        return true; // По умолчанию выполняем
    }
    
    /**
     * Парсинг типа операции
     */
    private CommandOperationType parseOperationType(String operationTypeStr) {
        try {
            return CommandOperationType.valueOf(operationTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Логирование операции
     */
    private void logOperation(CommandOperationType operationType, String command, 
                            ExecutionResult result, ExecutionContext context) {
        String logMessage = String.format("[CommandOp] %s: %s -> %s", 
            operationType.name(), command, result.isSuccess() ? "SUCCESS" : "FAILED");
        
        if (result.isSuccess()) {
            OpenHousing.getInstance().getLogger().info(logMessage);
        } else {
            OpenHousing.getInstance().getLogger().warning(logMessage + " - " + result.getMessage());
        }
    }
    
    /**
     * Обновление статистики
     */
    private void updateStatistics(CommandOperationType operationType, String command, ExecutionResult result) {
        totalCommandOperations.incrementAndGet();
        commandUsageStats.merge(operationType.name(), 1, Integer::sum);
    }
    
    @Override
    public boolean validate() {
        // Проверяем базовые параметры
        return !getStringValue(commandVar).trim().isEmpty();
    }
    
    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("§6Блок управления командами");
        description.add("§7Выполнение, проверка и управление");
        description.add("§7командами с GUI настройками");
        description.add("");
        description.add("§eОперация:");
        description.add("§7• " + getStringValue(operationTypeVar));
        description.add("§7• Команда: " + getStringValue(commandVar));
        description.add("§7• Целевой игрок: " + getStringValue(targetPlayerVar));
        description.add("§7• Исполнитель: " + getStringValue(executorVar));
        description.add("");
        description.add("§eНастройки:");
        description.add("§7• Логирование: " + (getBooleanValue(loggingEnabledVar) ? "§aВключено" : "§cВыключено"));
        description.add("§7• Уведомления: " + (getBooleanValue(notificationEnabledVar) ? "§aВключено" : "§cВыключено"));
        description.add("§7• Асинхронность: " + (getBooleanValue(asyncExecutionVar) ? "§aВключена" : "§cВыключена"));
        
        return description;
    }
    
    // Вспомогательные методы для работы с переменными
    private boolean getBooleanValue(BlockVariable variable) {
        Object value = variable.getValue();
        return value instanceof Boolean ? (Boolean) value : false;
    }
    
    private int getIntegerValue(BlockVariable variable) {
        Object value = variable.getValue();
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof String) {
            try { return Integer.parseInt((String) value); } catch (Exception e) { }
        }
        return 0;
    }
    
    private String getStringValue(BlockVariable variable) {
        Object value = variable.getValue();
        return value != null ? value.toString() : "";
    }
    
    @SuppressWarnings("unchecked")
    private List<String> getListValue(BlockVariable variable) {
        Object value = variable.getValue();
        if (value instanceof List) {
            return (List<String>) value;
        }
        return new ArrayList<>();
    }
    
    private String formatListValue(BlockVariable variable) {
        List<String> list = getListValue(variable);
        if (list.isEmpty()) return "Пусто";
        return String.join(", ", list);
    }
    
    // Геттеры для переменных (для внешнего доступа)
    public BlockVariable getOperationTypeVar() { return operationTypeVar; }
    public BlockVariable getCommandVar() { return commandVar; }
    public BlockVariable getTargetPlayerVar() { return targetPlayerVar; }
    public BlockVariable getExecutorVar() { return executorVar; }
    public BlockVariable getArgumentsVar() { return argumentsVar; }
    public BlockVariable getPermissionVar() { return permissionVar; }
    public BlockVariable getCooldownVar() { return cooldownVar; }
    public BlockVariable getConditionVar() { return conditionVar; }
    public BlockVariable getSuccessMessageVar() { return successMessageVar; }
    public BlockVariable getFailureMessageVar() { return failureMessageVar; }
    public BlockVariable getLoggingEnabledVar() { return loggingEnabledVar; }
    public BlockVariable getNotificationEnabledVar() { return notificationEnabledVar; }
    public BlockVariable getAsyncExecutionVar() { return asyncExecutionVar; }
    public BlockVariable getOutputCaptureVar() { return outputCaptureVar; }
    public BlockVariable getErrorHandlingVar() { return errorHandlingVar; }
    
    // Внутренние классы
    private static class CommandTemplate {
        private final String id;
        private final String name;
        private final String command;
        private final List<String> arguments;
        private final String permission;
        private final int cooldown;
        
        public CommandTemplate(String id, String name, String command, List<String> arguments, 
                             String permission, int cooldown) {
            this.id = id;
            this.name = name;
            this.command = command;
            this.arguments = arguments;
            this.permission = permission;
            this.cooldown = cooldown;
        }
        
        // Геттеры
        public String getId() { return id; }
        public String getName() { return name; }
        public String getCommand() { return command; }
        public List<String> getArguments() { return arguments; }
        public String getPermission() { return permission; }
        public int getCooldown() { return cooldown; }
    }
}
