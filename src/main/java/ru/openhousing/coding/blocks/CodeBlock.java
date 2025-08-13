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
        private final Map<String, Object> variables;
        private final Map<String, CodeBlock> functions;
        private Object target;
        
        public ExecutionContext(Player player) {
            this.player = player;
            this.variables = new HashMap<>();
            this.functions = new HashMap<>();
        }
        
        public Player getPlayer() {
            return player;
        }
        
        public Map<String, Object> getVariables() {
            return variables;
        }
        
        public Object getVariable(String name) {
            return variables.get(name);
        }
        
        public void setVariable(String name, Object value) {
            variables.put(name, value);
        }
        
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
        
        /**
         * Создание дочернего контекста для функций
         */
        public ExecutionContext createChildContext() {
            ExecutionContext child = new ExecutionContext(player);
            child.variables.putAll(this.variables); // Копируем переменные
            child.functions.putAll(this.functions); // Копируем функции
            child.target = this.target; // Копируем цель
            return child;
        }
    }
}
