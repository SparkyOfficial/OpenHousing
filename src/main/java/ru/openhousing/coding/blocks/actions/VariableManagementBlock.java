package ru.openhousing.coding.blocks.actions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionContext;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionResult;
import ru.openhousing.coding.blocks.BlockVariable;
import ru.openhousing.coding.blocks.BlockVariableAdapter;
import ru.openhousing.coding.blocks.BlockVariableManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Специализированный блок для управления переменными
 * Интегрирует существующую систему переменных с новой системой "переменных в руке"
 * 
 * @author OpenHousing Team
 * @version 1.0.0
 */
public class VariableManagementBlock extends CodeBlock {
    
    // Статические поля для глобального управления
    private static final Map<String, VariableOperation> operationHistory = new ConcurrentHashMap<>();
    private static final Map<String, Integer> variableUsageStats = new ConcurrentHashMap<>();
    private static final AtomicInteger totalOperations = new AtomicInteger(0);
    private static final AtomicInteger successfulOperations = new AtomicInteger(0);
    
    // Переменные блока (настраиваются через drag-n-drop)
    private BlockVariable operationTypeVar;
    private BlockVariable targetVariableVar;
    private BlockVariable sourceVariableVar;
    private BlockVariable valueVar;
    private BlockVariable conditionVar;
    private BlockVariable loggingEnabledVar;
    private BlockVariable validationEnabledVar;
    private BlockVariable rollbackEnabledVar;
    private BlockVariable maxHistorySizeVar;
    private BlockVariable autoBackupVar;
    private BlockVariable notificationEnabledVar;
    private BlockVariable performanceModeVar;
    private BlockVariable cacheEnabledVar;
    private BlockVariable syncIntervalVar;
    private BlockVariable errorHandlingVar;
    
    // Внутренние кэши и состояния
    private final Map<String, VariableBackup> variableBackups = new ConcurrentHashMap<>();
    private final Queue<VariableOperation> pendingOperations = new LinkedList<>();
    private final Map<String, Long> lastSyncTime = new ConcurrentHashMap<>();
    
    public enum VariableOperationType {
        SET("Установить", "Устанавливает значение переменной"),
        ADD("Прибавить", "Прибавляет к переменной"),
        MULTIPLY("Умножить", "Умножает переменную"),
        DIVIDE("Разделить", "Делит переменную"),
        APPEND("Добавить текст", "Добавляет текст к переменной"),
        REMOVE("Удалить", "Удаляет переменную"),
        COPY("Копировать", "Копирует значение одной переменной в другую"),
        SWAP("Поменять местами", "Меняет значения двух переменных местами"),
        MERGE("Объединить", "Объединяет значения нескольких переменных"),
        SPLIT("Разделить", "Разделяет переменную на части"),
        VALIDATE("Проверить", "Проверяет корректность значения"),
        BACKUP("Резервная копия", "Создает резервную копию переменной"),
        RESTORE("Восстановить", "Восстанавливает переменную из резервной копии"),
        SYNC("Синхронизировать", "Синхронизирует с внешними системами"),
        OPTIMIZE("Оптимизировать", "Оптимизирует хранение переменной"),
        MIGRATE("Мигрировать", "Перемещает переменную между системами");
        
        private final String displayName;
        private final String description;
        
        VariableOperationType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    public VariableManagementBlock() {
        super(BlockType.VAR_SET); // Используем VAR_SET как базовый тип
        initializeDefaultSettings();
    }
    
    /**
     * Инициализация настроек по умолчанию
     */
    private void initializeDefaultSettings() {
        // Создаем переменные с значениями по умолчанию
        operationTypeVar = new BlockVariable("operationType", "Тип операции", 
            BlockVariable.VariableType.STRING, "SET");
        targetVariableVar = new BlockVariable("targetVariable", "Целевая переменная", 
            BlockVariable.VariableType.STRING, "myVariable");
        sourceVariableVar = new BlockVariable("sourceVariable", "Исходная переменная", 
            BlockVariable.VariableType.STRING, "");
        valueVar = new BlockVariable("value", "Значение", 
            BlockVariable.VariableType.STRING, "");
        conditionVar = new BlockVariable("condition", "Условие выполнения", 
            BlockVariable.VariableType.STRING, "true");
        loggingEnabledVar = new BlockVariable("loggingEnabled", "Включить логирование", 
            BlockVariable.VariableType.BOOLEAN, true);
        validationEnabledVar = new BlockVariable("validationEnabled", "Включить валидацию", 
            BlockVariable.VariableType.BOOLEAN, true);
        rollbackEnabledVar = new BlockVariable("rollbackEnabled", "Включить откат", 
            BlockVariable.VariableType.BOOLEAN, false);
        maxHistorySizeVar = new BlockVariable("maxHistorySize", "Максимальный размер истории", 
            BlockVariable.VariableType.INTEGER, 100);
        autoBackupVar = new BlockVariable("autoBackup", "Автоматическое резервное копирование", 
            BlockVariable.VariableType.BOOLEAN, true);
        notificationEnabledVar = new BlockVariable("notificationEnabled", "Включить уведомления", 
            BlockVariable.VariableType.BOOLEAN, true);
        performanceModeVar = new BlockVariable("performanceMode", "Режим производительности", 
            BlockVariable.VariableType.BOOLEAN, false);
        cacheEnabledVar = new BlockVariable("cacheEnabled", "Включить кэширование", 
            BlockVariable.VariableType.BOOLEAN, true);
        syncIntervalVar = new BlockVariable("syncInterval", "Интервал синхронизации (мс)", 
            BlockVariable.VariableType.INTEGER, 5000);
        errorHandlingVar = new BlockVariable("errorHandling", "Обработка ошибок", 
            BlockVariable.VariableType.STRING, "CONTINUE");
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
            String targetVariableName = getStringValue(targetVariableVar);
            String sourceVariableName = getStringValue(sourceVariableVar);
            Object value = getValue(valueVar);
            String condition = getStringValue(conditionVar);
            
            // Проверяем условие выполнения
            if (!evaluateCondition(condition, context)) {
                return ExecutionResult.success("Операция пропущена по условию");
            }
            
            // Определяем тип операции
            VariableOperationType operationType = parseOperationType(operationTypeStr);
            if (operationType == null) {
                return ExecutionResult.error("Неизвестный тип операции: " + operationTypeStr);
            }
            
            // Выполняем операцию
            ExecutionResult result = executeVariableOperation(operationType, targetVariableName, 
                sourceVariableName, value, context);
            
            // Логируем операцию
            if (getBooleanValue(loggingEnabledVar)) {
                logOperation(operationType, targetVariableName, result, context);
            }
            
            // Обновляем статистику
            updateStatistics(operationType, targetVariableName, result);
            
            // Синхронизируем с внешними системами
            if (getBooleanValue(cacheEnabledVar)) {
                syncWithExternalSystems(targetVariableName, context);
            }
            
            return result;
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения блока управления переменными: " + e.getMessage());
        }
    }
    
    /**
     * Выполнение операции с переменной
     */
    private ExecutionResult executeVariableOperation(VariableOperationType operationType, 
                                                   String targetVariableName, 
                                                   String sourceVariableName, 
                                                   Object value, 
                                                   ExecutionContext context) {
        
        try {
            switch (operationType) {
                case SET:
                    return executeSetOperation(targetVariableName, value, context);
                    
                case ADD:
                    return executeAddOperation(targetVariableName, value, context);
                    
                case MULTIPLY:
                    return executeMultiplyOperation(targetVariableName, value, context);
                    
                case DIVIDE:
                    return executeDivideOperation(targetVariableName, value, context);
                    
                case APPEND:
                    return executeAppendOperation(targetVariableName, value, context);
                    
                case REMOVE:
                    return executeRemoveOperation(targetVariableName, context);
                    
                case COPY:
                    return executeCopyOperation(targetVariableName, sourceVariableName, context);
                    
                case SWAP:
                    return executeSwapOperation(targetVariableName, sourceVariableName, context);
                    
                case MERGE:
                    return executeMergeOperation(targetVariableName, sourceVariableName, context);
                    
                case SPLIT:
                    return executeSplitOperation(targetVariableName, context);
                    
                case VALIDATE:
                    return executeValidateOperation(targetVariableName, context);
                    
                case BACKUP:
                    return executeBackupOperation(targetVariableName, context);
                    
                case RESTORE:
                    return executeRestoreOperation(targetVariableName, context);
                    
                case SYNC:
                    return executeSyncOperation(targetVariableName, context);
                    
                case OPTIMIZE:
                    return executeOptimizeOperation(targetVariableName, context);
                    
                case MIGRATE:
                    return executeMigrateOperation(targetVariableName, context);
                    
                default:
                    return ExecutionResult.error("Неподдерживаемая операция: " + operationType);
            }
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения операции " + operationType + ": " + e.getMessage());
        }
    }
    
    /**
     * Операция SET
     */
    private ExecutionResult executeSetOperation(String variableName, Object value, ExecutionContext context) {
        // Создаем резервную копию перед изменением
        if (getBooleanValue(autoBackupVar)) {
            createBackup(variableName, context);
        }
        
        // Устанавливаем значение
        context.setVariable(variableName, value);
        
        // Уведомляем игрока
        if (getBooleanValue(notificationEnabledVar)) {
            Player player = context.getPlayer();
            if (player != null) {
                player.sendMessage("§a✓ Переменная '" + variableName + "' установлена в: " + formatValue(value));
            }
        }
        
        return ExecutionResult.success("Переменная '" + variableName + "' установлена");
    }
    
    /**
     * Операция ADD
     */
    private ExecutionResult executeAddOperation(String variableName, Object value, ExecutionContext context) {
        Object currentValue = context.getVariable(variableName);
        if (currentValue == null) {
            currentValue = 0.0;
        }
        
        if (currentValue instanceof Number && value instanceof Number) {
            double result = ((Number) currentValue).doubleValue() + ((Number) value).doubleValue();
            context.setVariable(variableName, result);
            return ExecutionResult.success("К переменной '" + variableName + "' добавлено: " + value);
        } else if (currentValue instanceof String && value instanceof String) {
            String result = currentValue.toString() + value.toString();
            context.setVariable(variableName, result);
            return ExecutionResult.success("К переменной '" + variableName + "' добавлен текст: " + value);
        } else {
            return ExecutionResult.error("Неподдерживаемые типы для операции ADD");
        }
    }
    
    /**
     * Операция MULTIPLY
     */
    private ExecutionResult executeMultiplyOperation(String variableName, Object value, ExecutionContext context) {
        Object currentValue = context.getVariable(variableName);
        if (currentValue == null || !(currentValue instanceof Number) || !(value instanceof Number)) {
            return ExecutionResult.error("Обе переменные должны быть числами для операции MULTIPLY");
        }
        
        double result = ((Number) currentValue).doubleValue() * ((Number) value).doubleValue();
        context.setVariable(variableName, result);
        return ExecutionResult.success("Переменная '" + variableName + "' умножена на: " + value);
    }
    
    /**
     * Операция DIVIDE
     */
    private ExecutionResult executeDivideOperation(String variableName, Object value, ExecutionContext context) {
        Object currentValue = context.getVariable(variableName);
        if (currentValue == null || !(currentValue instanceof Number) || !(value instanceof Number)) {
            return ExecutionResult.error("Обе переменные должны быть числами для операции DIVIDE");
        }
        
        double divisor = ((Number) value).doubleValue();
        if (divisor == 0) {
            return ExecutionResult.error("Деление на ноль невозможно");
        }
        
        double result = ((Number) currentValue).doubleValue() / divisor;
        context.setVariable(variableName, result);
        return ExecutionResult.success("Переменная '" + variableName + "' разделена на: " + value);
    }
    
    /**
     * Операция APPEND
     */
    private ExecutionResult executeAppendOperation(String variableName, Object value, ExecutionContext context) {
        Object currentValue = context.getVariable(variableName);
        if (currentValue == null) {
            currentValue = "";
        }
        
        String result = currentValue.toString() + value.toString();
        context.setVariable(variableName, result);
        return ExecutionResult.success("К переменной '" + variableName + "' добавлен текст: " + value);
    }
    
    /**
     * Операция REMOVE
     */
    private ExecutionResult executeRemoveOperation(String variableName, ExecutionContext context) {
        Object removedValue = context.getVariable(variableName);
        context.removeLocalVariable(variableName);
        return ExecutionResult.success("Переменная '" + variableName + "' удалена (было: " + formatValue(removedValue) + ")");
    }
    
    /**
     * Операция COPY
     */
    private ExecutionResult executeCopyOperation(String targetVariableName, String sourceVariableName, ExecutionContext context) {
        Object sourceValue = context.getVariable(sourceVariableName);
        if (sourceValue == null) {
            return ExecutionResult.error("Исходная переменная '" + sourceVariableName + "' не найдена");
        }
        
        context.setVariable(targetVariableName, sourceValue);
        return ExecutionResult.success("Значение скопировано из '" + sourceVariableName + "' в '" + targetVariableName + "'");
    }
    
    /**
     * Операция SWAP
     */
    private ExecutionResult executeSwapOperation(String variable1Name, String variable2Name, ExecutionContext context) {
        Object value1 = context.getVariable(variable1Name);
        Object value2 = context.getVariable(variable2Name);
        
        context.setVariable(variable1Name, value2);
        context.setVariable(variable2Name, value1);
        
        return ExecutionResult.success("Значения переменных '" + variable1Name + "' и '" + variable2Name + "' поменяны местами");
    }
    
    /**
     * Операция MERGE
     */
    private ExecutionResult executeMergeOperation(String targetVariableName, String sourceVariableName, ExecutionContext context) {
        Object targetValue = context.getVariable(targetVariableName);
        Object sourceValue = context.getVariable(sourceVariableName);
        
        if (targetValue instanceof List && sourceValue instanceof List) {
            List<Object> mergedList = new ArrayList<>((List<?>) targetValue);
            mergedList.addAll((List<?>) sourceValue);
            context.setVariable(targetVariableName, mergedList);
            return ExecutionResult.success("Списки переменных объединены");
        } else if (targetValue instanceof String && sourceValue instanceof String) {
            String mergedString = targetValue.toString() + " " + sourceValue.toString();
            context.setVariable(targetVariableName, mergedString);
            return ExecutionResult.success("Строки переменных объединены");
        } else {
            return ExecutionResult.error("Операция MERGE поддерживается только для списков и строк");
        }
    }
    
    /**
     * Операция SPLIT
     */
    private ExecutionResult executeSplitOperation(String variableName, ExecutionContext context) {
        Object value = context.getVariable(variableName);
        if (value instanceof String) {
            String[] parts = value.toString().split("\\s+");
            List<String> splitList = Arrays.asList(parts);
            context.setVariable(variableName + "_split", splitList);
            return ExecutionResult.success("Переменная '" + variableName + "' разделена на " + parts.length + " частей");
        } else {
            return ExecutionResult.error("Операция SPLIT поддерживается только для строк");
        }
    }
    
    /**
     * Операция VALIDATE
     */
    private ExecutionResult executeValidateOperation(String variableName, ExecutionContext context) {
        Object value = context.getVariable(variableName);
        if (value == null) {
            return ExecutionResult.error("Переменная '" + variableName + "' не существует");
        }
        
        // Простая валидация
        boolean isValid = true;
        String validationMessage = "Переменная '" + variableName + "' валидна";
        
        if (value instanceof String && value.toString().length() > 1000) {
            isValid = false;
            validationMessage = "Строка слишком длинная (максимум 1000 символов)";
        } else if (value instanceof Number) {
            double numValue = ((Number) value).doubleValue();
            if (numValue < -1e6 || numValue > 1e6) {
                isValid = false;
                validationMessage = "Число слишком большое (допустимый диапазон: ±1,000,000)";
            }
        }
        
        if (isValid) {
            return ExecutionResult.success(validationMessage);
        } else {
            return ExecutionResult.error(validationMessage);
        }
    }
    
    /**
     * Операция BACKUP
     */
    private ExecutionResult executeBackupOperation(String variableName, ExecutionContext context) {
        Object value = context.getVariable(variableName);
        if (value == null) {
            return ExecutionResult.error("Переменная '" + variableName + "' не существует");
        }
        
        createBackup(variableName, context);
        return ExecutionResult.success("Создана резервная копия переменной '" + variableName + "'");
    }
    
    /**
     * Операция RESTORE
     */
    private ExecutionResult executeRestoreOperation(String variableName, ExecutionContext context) {
        VariableBackup backup = variableBackups.get(variableName);
        if (backup == null) {
            return ExecutionResult.error("Резервная копия переменной '" + variableName + "' не найдена");
        }
        
        context.setVariable(variableName, backup.getValue());
        return ExecutionResult.success("Переменная '" + variableName + "' восстановлена из резервной копии");
    }
    
    /**
     * Операция SYNC
     */
    private ExecutionResult executeSyncOperation(String variableName, ExecutionContext context) {
        // Синхронизация с внешними системами
        long currentTime = System.currentTimeMillis();
        lastSyncTime.put(variableName, currentTime);
        
        // TODO: Реализовать реальную синхронизацию
        return ExecutionResult.success("Переменная '" + variableName + "' синхронизирована");
    }
    
    /**
     * Операция OPTIMIZE
     */
    private ExecutionResult executeOptimizeOperation(String variableName, ExecutionContext context) {
        Object value = context.getVariable(variableName);
        if (value == null) {
            return ExecutionResult.error("Переменная '" + variableName + "' не существует");
        }
        
        // Простая оптимизация
        if (value instanceof String && value.toString().length() > 100) {
            String optimized = value.toString().substring(0, 100) + "...";
            context.setVariable(variableName, optimized);
            return ExecutionResult.success("Переменная '" + variableName + "' оптимизирована (обрезана)");
        }
        
        return ExecutionResult.success("Переменная '" + variableName + "' уже оптимизирована");
    }
    
    /**
     * Операция MIGRATE
     */
    private ExecutionResult executeMigrateOperation(String variableName, ExecutionContext context) {
        Object value = context.getVariable(variableName);
        if (value == null) {
            return ExecutionResult.error("Переменная '" + variableName + "' не существует");
        }
        
        // Миграция в глобальную область
        context.setGlobalVariable(variableName, value);
        context.removeLocalVariable(variableName);
        
        return ExecutionResult.success("Переменная '" + variableName + "' мигрирована в глобальную область");
    }
    
    /**
     * Создание резервной копии
     */
    private void createBackup(String variableName, ExecutionContext context) {
        Object value = context.getVariable(variableName);
        if (value != null) {
            VariableBackup backup = new VariableBackup(variableName, value, System.currentTimeMillis());
            variableBackups.put(variableName, backup);
        }
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
    private VariableOperationType parseOperationType(String operationTypeStr) {
        try {
            return VariableOperationType.valueOf(operationTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Логирование операции
     */
    private void logOperation(VariableOperationType operationType, String variableName, 
                            ExecutionResult result, ExecutionContext context) {
        String logMessage = String.format("[VariableOp] %s: %s -> %s", 
            operationType.name(), variableName, result.isSuccess() ? "SUCCESS" : "FAILED");
        
        if (result.isSuccess()) {
            OpenHousing.getInstance().getLogger().info(logMessage);
        } else {
            OpenHousing.getInstance().getLogger().warning(logMessage + " - " + result.getMessage());
        }
    }
    
    /**
     * Обновление статистики
     */
    private void updateStatistics(VariableOperationType operationType, String variableName, ExecutionResult result) {
        totalOperations.incrementAndGet();
        
        if (result.isSuccess()) {
            successfulOperations.incrementAndGet();
        }
        
        variableUsageStats.merge(variableName, 1, Integer::sum);
        
        // Сохраняем историю операций
        VariableOperation operation = new VariableOperation(operationType, variableName, 
            result.isSuccess(), System.currentTimeMillis());
        operationHistory.put(operationType.name() + "_" + variableName + "_" + System.currentTimeMillis(), operation);
    }
    
    /**
     * Синхронизация с внешними системами
     */
    private void syncWithExternalSystems(String variableName, ExecutionContext context) {
        // Синхронизация с BlockVariableAdapter
        BlockVariable variable = BlockVariableAdapter.getVariableByName(variableName);
        if (variable != null) {
            Object value = context.getVariable(variableName);
            if (value != null) {
                variable.setValue(value);
            }
        }
    }
    
    /**
     * Форматирование значения для отображения
     */
    private String formatValue(Object value) {
        if (value == null) return "null";
        if (value instanceof String && value.toString().length() > 50) {
            return value.toString().substring(0, 47) + "...";
        }
        return value.toString();
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
    
    private Object getValue(BlockVariable variable) {
        return variable.getValue();
    }
    
    @Override
    public boolean validate() {
        // Проверяем базовые параметры
        return getIntegerValue(maxHistorySizeVar) >= 0 && 
               getIntegerValue(syncIntervalVar) >= 0;
    }
    
    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("§6Блок управления переменными");
        description.add("§7Интегрирует существующую систему с drag-n-drop");
        description.add("");
        description.add("§eОперации:");
        description.add("§7• " + getStringValue(operationTypeVar));
        description.add("§7• Цель: " + getStringValue(targetVariableVar));
        description.add("§7• Значение: " + getStringValue(valueVar));
        description.add("");
        description.add("§eНастройки:");
        description.add("§7• Логирование: " + (getBooleanValue(loggingEnabledVar) ? "§aВключено" : "§cВыключено"));
        description.add("§7• Валидация: " + (getBooleanValue(validationEnabledVar) ? "§aВключена" : "§cВыключена"));
        description.add("§7• Резервное копирование: " + (getBooleanValue(autoBackupVar) ? "§aВключено" : "§cВыключено"));
        description.add("§7• Кэширование: " + (getBooleanValue(cacheEnabledVar) ? "§aВключено" : "§cВыключено"));
        
        return description;
    }
    
    // Геттеры для переменных (для внешнего доступа)
    public BlockVariable getOperationTypeVar() { return operationTypeVar; }
    public BlockVariable getTargetVariableVar() { return targetVariableVar; }
    public BlockVariable getSourceVariableVar() { return sourceVariableVar; }
    public BlockVariable getValueVar() { return valueVar; }
    public BlockVariable getConditionVar() { return conditionVar; }
    public BlockVariable getLoggingEnabledVar() { return loggingEnabledVar; }
    public BlockVariable getValidationEnabledVar() { return validationEnabledVar; }
    public BlockVariable getRollbackEnabledVar() { return rollbackEnabledVar; }
    public BlockVariable getMaxHistorySizeVar() { return maxHistorySizeVar; }
    public BlockVariable getAutoBackupVar() { return autoBackupVar; }
    public BlockVariable getNotificationEnabledVar() { return notificationEnabledVar; }
    public BlockVariable getPerformanceModeVar() { return performanceModeVar; }
    public BlockVariable getCacheEnabledVar() { return cacheEnabledVar; }
    public BlockVariable getSyncIntervalVar() { return syncIntervalVar; }
    public BlockVariable getErrorHandlingVar() { return errorHandlingVar; }
    
    // Внутренние классы для статистики и кэширования
    private static class VariableOperation {
        private final VariableOperationType type;
        private final String variableName;
        private final boolean success;
        private final long timestamp;
        
        public VariableOperation(VariableOperationType type, String variableName, boolean success, long timestamp) {
            this.type = type;
            this.variableName = variableName;
            this.success = success;
            this.timestamp = timestamp;
        }
        
        // Геттеры
        public VariableOperationType getType() { return type; }
        public String getVariableName() { return variableName; }
        public boolean isSuccess() { return success; }
        public long getTimestamp() { return timestamp; }
    }
    
    private static class VariableBackup {
        private final String variableName;
        private final Object value;
        private final long timestamp;
        
        public VariableBackup(String variableName, Object value, long timestamp) {
            this.variableName = variableName;
            this.value = value;
            this.timestamp = timestamp;
        }
        
        // Геттеры
        public String getVariableName() { return variableName; }
        public Object getValue() { return value; }
        public long getTimestamp() { return timestamp; }
    }
}
