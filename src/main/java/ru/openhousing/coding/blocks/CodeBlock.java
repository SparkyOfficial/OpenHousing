package ru.openhousing.coding.blocks;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Базовый класс для блока кода
 */
public abstract class CodeBlock {
    
    protected final UUID id;
    protected final BlockType type;
    protected final Map<String, Object> parameters;
    protected final List<CodeBlock> childBlocks;
    protected CodeBlock parentBlock;
    protected Location location;
    
    public CodeBlock(BlockType type) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.parameters = new HashMap<>();
        this.childBlocks = new ArrayList<>();
    }
    
    /**
     * Выполнение блока
     */
    public abstract ExecutionResult execute(ExecutionContext context);
    
    /**
     * Проверка корректности блока
     */
    public abstract boolean validate();
    
    /**
     * Получение описания блока для GUI
     */
    public abstract List<String> getDescription();
    
    /**
     * Проверка, соответствует ли событие этому блоку
     */
    public boolean matchesEvent(Object event) {
        return false; // По умолчанию не соответствует
    }
    
    /**
     * Клонирование блока
     */
    public CodeBlock clone() {
        try {
            // Создаем новый экземпляр того же типа
            CodeBlock cloned = this.getClass().getDeclaredConstructor().newInstance();
            
            // Копируем параметры
            cloned.parameters.putAll(this.parameters);
            
            // Копируем дочерние блоки
            for (CodeBlock child : this.childBlocks) {
                CodeBlock clonedChild = child.clone();
                clonedChild.parentBlock = cloned;
                cloned.childBlocks.add(clonedChild);
            }
            
            // Копируем местоположение
            if (this.location != null) {
                cloned.location = this.location.clone();
            }
            
            return cloned;
        } catch (Exception e) {
            // Fallback: создаем простую копию без дочерних блоков
            return createSimpleCopy();
        }
    }
    
    /**
     * Создание простой копии блока без дочерних элементов
     */
    protected CodeBlock createSimpleCopy() {
        try {
            CodeBlock copy = this.getClass().getDeclaredConstructor().newInstance();
            copy.parameters.putAll(this.parameters);
            return copy;
        } catch (Exception e) {
            throw new RuntimeException("Не удалось клонировать блок " + this.getClass().getSimpleName(), e);
        }
    }
    
    /**
     * Создание контекста выполнения из события
     */
    public ExecutionContext createContextFromEvent(Object event) {
        if (event instanceof org.bukkit.event.player.PlayerEvent) {
            org.bukkit.event.player.PlayerEvent playerEvent = (org.bukkit.event.player.PlayerEvent) event;
            return new ExecutionContext(playerEvent.getPlayer());
        }
        return null; // По умолчанию не создаем контекст
    }
    
    /**
     * Добавление дочернего блока
     */
    public void addChild(CodeBlock child) {
        childBlocks.add(child);
        child.setParent(this);
    }
    
    /**
     * Удаление дочернего блока
     */
    public void removeChild(CodeBlock child) {
        childBlocks.remove(child);
        child.setParent(null);
    }
    
    /**
     * Выполнение всех дочерних блоков
     */
    protected ExecutionResult executeChildren(ExecutionContext context) {
        for (CodeBlock child : childBlocks) {
            ExecutionResult result = child.execute(context);
            
            // Обработка результатов
            switch (result.getType()) {
                case BREAK:
                case RETURN:
                case ERROR:
                    return result;
                case CONTINUE:
                    continue;
                case SUCCESS:
                default:
                    break;
            }
        }
        return ExecutionResult.success();
    }
    
    // Геттеры и сеттеры
    public UUID getId() {
        return id;
    }
    
    public BlockType getType() {
        return type;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public Object getParameter(String key) {
        return parameters.get(key);
    }
    
    public void setParameter(String key, Object value) {
        parameters.put(key, value);
    }
    
    public List<CodeBlock> getChildBlocks() {
        return new ArrayList<>(childBlocks);
    }
    
    public CodeBlock getParentBlock() {
        return parentBlock;
    }
    
    public void setParent(CodeBlock parent) {
        this.parentBlock = parent;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
    
    /**
     * Замена переменных в строке
     */
    protected String replaceVariables(String text, ExecutionContext context) {
        if (text == null || context == null) {
            return text;
        }
        
        String result = text;
        
        // Заменяем переменные в формате %variableName%
        for (String varName : context.getVariables().keySet()) {
            Object value = context.getVariable(varName);
            String placeholder = "%" + varName + "%";
            if (result.contains(placeholder)) {
                result = result.replace(placeholder, value != null ? value.toString() : "");
            }
        }
        
        return result;
    }
    
    /**
     * Результат выполнения блока
     */
    public static class ExecutionResult {
        public enum Type {
            SUCCESS,    // Успешное выполнение
            ERROR,      // Ошибка
            BREAK,      // Прерывание цикла
            CONTINUE,   // Продолжение цикла
            RETURN      // Возврат из функции
        }
        
        private final Type type;
        private final String message;
        private final Object value;
        
        private ExecutionResult(Type type, String message, Object value) {
            this.type = type;
            this.message = message;
            this.value = value;
        }
        
        public static ExecutionResult success() {
            return new ExecutionResult(Type.SUCCESS, null, null);
        }
        
        public static ExecutionResult success(Object value) {
            return new ExecutionResult(Type.SUCCESS, null, value);
        }
        
        public static ExecutionResult error(String message) {
            return new ExecutionResult(Type.ERROR, message, null);
        }
        
        public static ExecutionResult breakLoop() {
            return new ExecutionResult(Type.BREAK, null, null);
        }
        
        public static ExecutionResult continueLoop() {
            return new ExecutionResult(Type.CONTINUE, null, null);
        }
        
        public static ExecutionResult returnValue(Object value) {
            return new ExecutionResult(Type.RETURN, null, value);
        }
        
        public Type getType() {
            return type;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Object getValue() {
            return value;
        }
        
        public boolean isSuccess() {
            return type == Type.SUCCESS;
        }
        
        public boolean isError() {
            return type == Type.ERROR;
        }
    }
    
    /**
     * Контекст выполнения кода
     */
    public static class ExecutionContext {
        private final Player player;
        private final Map<String, Object> localVariables;      // Локальные переменные (только для текущего контекста)
        private final Map<String, Object> globalVariables;     // Глобальные переменные (сохраняются между вызовами)
        private final Map<String, Object> systemVariables;     // Системные переменные (read-only)
        private final Map<String, CodeBlock> functions;
        private final List<String> executionLog;              // Лог выполнения для отладки
        private final long startTime;                         // Время начала выполнения
        private Object target;
        private boolean debugMode = false;
        private int executionDepth = 0;                       // Глубина вложенности вызовов
        private String currentBlockId = "unknown";            // ID текущего выполняемого блока
        
        public ExecutionContext(Player player) {
            this.player = player;
            this.localVariables = new HashMap<>();
            this.globalVariables = new HashMap<>();
            this.systemVariables = new HashMap<>();
            this.functions = new HashMap<>();
            this.executionLog = new ArrayList<>();
            this.startTime = System.currentTimeMillis();
            
            // Инициализируем системные переменные
            initializeSystemVariables();
        }
        
        /**
         * Инициализация системных переменных (read-only)
         */
        private void initializeSystemVariables() {
            systemVariables.put("player_name", player.getName() != null ? player.getName() : "Unknown");
            systemVariables.put("player_uuid", player.getUniqueId() != null ? player.getUniqueId().toString() : "unknown");
            systemVariables.put("world_name", player.getWorld() != null ? player.getWorld().getName() : "unknown");
            systemVariables.put("execution_time", startTime);
            systemVariables.put("tps", 20.0); // Будет обновляться
        }
        
        public Player getPlayer() {
            return player;
        }
        
        // === УПРАВЛЕНИЕ ПЕРЕМЕННЫМИ ===
        
        /**
         * Получение переменной с приоритетом: локальная -> глобальная -> системная
         */
        public Object getVariable(String name) {
            if (localVariables.containsKey(name)) {
                return localVariables.get(name);
            }
            if (globalVariables.containsKey(name)) {
                return globalVariables.get(name);
            }
            if (systemVariables.containsKey(name)) {
                return systemVariables.get(name);
            }
            return null;
        }
        
        /**
         * Установка локальной переменной
         */
        public void setLocalVariable(String name, Object value) {
            localVariables.put(name, value);
            if (debugMode) {
                logExecution("SET_LOCAL", name + " = " + value);
            }
        }
        
        /**
         * Установка глобальной переменной
         */
        public void setGlobalVariable(String name, Object value) {
            globalVariables.put(name, value);
            if (debugMode) {
                logExecution("SET_GLOBAL", name + " = " + value);
            }
        }
        
        /**
         * Проверка существования переменной
         */
        public boolean hasVariable(String name) {
            return localVariables.containsKey(name) || 
                   globalVariables.containsKey(name) || 
                   systemVariables.containsKey(name);
        }
        
        /**
         * Удаление локальной переменной
         */
        public void removeLocalVariable(String name) {
            if (localVariables.remove(name) != null && debugMode) {
                logExecution("REMOVE_LOCAL", name);
            }
        }
        
        /**
         * Удаление глобальной переменной
         */
        public void removeGlobalVariable(String name) {
            if (globalVariables.remove(name) != null && debugMode) {
                logExecution("REMOVE_GLOBAL", name);
            }
        }
        
        /**
         * Получение всех переменных с их типами
         */
        public Map<String, VariableInfo> getAllVariables() {
            Map<String, VariableInfo> allVars = new HashMap<>();
            
            // Локальные переменные
            localVariables.forEach((name, value) -> 
                allVars.put(name, new VariableInfo(name, value, VariableScope.LOCAL)));
            
            // Глобальные переменные
            globalVariables.forEach((name, value) -> 
                allVars.put(name, new VariableInfo(name, value, VariableScope.GLOBAL)));
            
            // Системные переменные
            systemVariables.forEach((name, value) -> 
                allVars.put(name, new VariableInfo(name, value, VariableScope.SYSTEM)));
            
            return allVars;
        }
        
        // === DEBUG MODE ===
        
        /**
         * Логирование выполнения для отладки
         */
        public void logExecution(String action, String details) {
            if (!debugMode) return;
            
            String timestamp = String.format("[%dms]", System.currentTimeMillis() - startTime);
            String depth = "  ".repeat(executionDepth);
            String logEntry = String.format("%s%s %s: %s", depth, timestamp, action, details);
            
            executionLog.add(logEntry);
            
            // Отправляем в чат игрока, если включен debug mode
            if (player != null && player.isOnline()) {
                player.sendMessage("§7[DEBUG] " + logEntry);
            }
        }
        
        /**
         * Получение лога выполнения
         */
        public List<String> getExecutionLog() {
            return new ArrayList<>(executionLog);
        }
        
        /**
         * Очистка лога выполнения
         */
        public void clearExecutionLog() {
            executionLog.clear();
        }
        
        /**
         * Получение статистики выполнения
         */
        public ExecutionStats getExecutionStats() {
            long currentTime = System.currentTimeMillis();
            return new ExecutionStats(
                currentTime - startTime,
                executionDepth,
                localVariables.size(),
                globalVariables.size(),
                systemVariables.size(),
                executionLog.size()
            );
        }
        
        // === УПРАВЛЕНИЕ КОНТЕКСТОМ ===
        
        /**
         * Увеличение глубины выполнения (при входе в блок)
         */
        public void enterBlock(String blockId) {
            executionDepth++;
            currentBlockId = blockId;
            if (debugMode) {
                logExecution("ENTER_BLOCK", blockId + " (depth: " + executionDepth + ")");
            }
        }
        
        /**
         * Уменьшение глубины выполнения (при выходе из блока)
         */
        public void exitBlock(String blockId) {
            if (debugMode) {
                logExecution("EXIT_BLOCK", blockId + " (depth: " + executionDepth + ")");
            }
            executionDepth = Math.max(0, executionDepth - 1);
            if (executionDepth == 0) {
                currentBlockId = "unknown";
            }
        }
        
        /**
         * Создание дочернего контекста для функций
         */
        public ExecutionContext createChildContext() {
            ExecutionContext child = new ExecutionContext(player);
            child.globalVariables.putAll(this.globalVariables); // Копируем глобальные переменные
            child.systemVariables.putAll(this.systemVariables); // Копируем системные переменные
            child.functions.putAll(this.functions);            // Копируем функции
            child.target = this.target;                       // Копируем цель
            child.debugMode = this.debugMode;                 // Копируем режим отладки
            child.executionDepth = this.executionDepth + 1;   // Увеличиваем глубину
            return child;
        }
        
        // === СТАРЫЕ МЕТОДЫ ДЛЯ ОБРАТНОЙ СОВМЕСТИМОСТИ ===
        
        @Deprecated
        public Map<String, Object> getVariables() {
            return localVariables; // Возвращаем только локальные для совместимости
        }
        
        @Deprecated
        public void setVariable(String name, Object value) {
            setLocalVariable(name, value); // По умолчанию устанавливаем как локальную
        }
        
        // === ОСТАЛЬНЫЕ МЕТОДЫ ===
        
        public Map<String, CodeBlock> getFunctions() {
            return functions;
        }
        
        public CodeBlock getFunction(String name) {
            return functions.get(name);
        }
        
        public void setFunction(String name, CodeBlock function) {
            functions.put(name, function);
        }
        
        public Object getTarget() {
            return target;
        }
        
        public void setTarget(Object target) {
            this.target = target;
        }
        
        public boolean isDebugMode() {
            return debugMode;
        }
        
        public void setDebugMode(boolean debugMode) {
            this.debugMode = debugMode;
            if (debugMode) {
                logExecution("DEBUG_MODE", "Включен");
            }
        }
        
        public int getExecutionDepth() {
            return executionDepth;
        }
        
        public String getCurrentBlockId() {
            return currentBlockId;
        }
        
        public long getStartTime() {
            return startTime;
        }
        
        // === ГЕТТЕРЫ ДЛЯ ПЕРЕМЕННЫХ ===
        
        public Map<String, Object> getLocalVariables() {
            return new HashMap<>(localVariables);
        }
        
        public Map<String, Object> getGlobalVariables() {
            return new HashMap<>(globalVariables);
        }
        
        public Map<String, Object> getSystemVariables() {
            return new HashMap<>(systemVariables);
        }
        
        /**
         * Информация о переменной
         */
        public static class VariableInfo {
            private final String name;
            private final Object value;
            private final VariableScope scope;
            private final long lastModified;
            
            public VariableInfo(String name, Object value, VariableScope scope) {
                this.name = name;
                this.value = value;
                this.scope = scope;
                this.lastModified = System.currentTimeMillis();
            }
            
            public String getName() { return name; }
            public Object getValue() { return value; }
            public VariableScope getScope() { return scope; }
            public long getLastModified() { return lastModified; }
            public String getValueType() { return value != null ? value.getClass().getSimpleName() : "null"; }
        }
        
        /**
         * Область видимости переменной
         */
        public enum VariableScope {
            LOCAL("Локальная"),
            GLOBAL("Глобальная"),
            SYSTEM("Системная");
            
            private final String displayName;
            
            VariableScope(String displayName) {
                this.displayName = displayName;
            }
            
            public String getDisplayName() {
                return displayName;
            }
        }
        
        /**
         * Статистика выполнения
         */
        public static class ExecutionStats {
            private final long executionTime;
            private final int maxDepth;
            private final int localVarCount;
            private final int globalVarCount;
            private final int systemVarCount;
            private final int logEntries;
            
            public ExecutionStats(long executionTime, int maxDepth, int localVarCount, 
                                int globalVarCount, int systemVarCount, int logEntries) {
                this.executionTime = executionTime;
                this.maxDepth = maxDepth;
                this.localVarCount = localVarCount;
                this.globalVarCount = globalVarCount;
                this.systemVarCount = systemVarCount;
                this.logEntries = logEntries;
            }
            
            public long getExecutionTime() { return executionTime; }
            public int getMaxDepth() { return maxDepth; }
            public int getLocalVarCount() { return localVarCount; }
            public int getGlobalVarCount() { return globalVarCount; }
            public int getSystemVarCount() { return systemVarCount; }
            public int getLogEntries() { return logEntries; }
            public int getTotalVarCount() { return localVarCount + globalVarCount + systemVarCount; }
        }
    }
}
