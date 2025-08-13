package ru.openhousing.coding.script;

import ru.openhousing.coding.blocks.CodeBlock;

import java.util.*;

/**
 * Скрипт кода игрока
 */
public class CodeScript {
    
    private final UUID playerId;
    private String playerName;
    private final List<CodeBlock> blocks;
    private final Map<String, Object> globalVariables;
    private final Map<String, CodeBlock> functions;
    private long lastModified;
    private boolean enabled;
    
    public CodeScript(UUID playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.blocks = new ArrayList<>();
        this.globalVariables = new HashMap<>();
        this.functions = new HashMap<>();
        this.lastModified = System.currentTimeMillis();
        this.enabled = true;
    }
    
    /**
     * Выполнение скрипта
     */
    public CodeBlock.ExecutionResult execute(CodeBlock.ExecutionContext context) {
        if (!enabled) {
            return CodeBlock.ExecutionResult.error("Скрипт отключен");
        }
        
        // Добавляем глобальные переменные в контекст
        context.getVariables().putAll(globalVariables);
        
        // Добавляем функции в контекст
        context.getFunctions().putAll(functions);
        
        try {
            for (CodeBlock block : blocks) {
                CodeBlock.ExecutionResult result = block.execute(context);
                
                // Обработка результатов
                if (result.getType() == CodeBlock.ExecutionResult.Type.ERROR) {
                    return result;
                } else if (result.getType() == CodeBlock.ExecutionResult.Type.RETURN) {
                    return result;
                }
            }
            
            // Сохраняем обновленные глобальные переменные
            globalVariables.putAll(context.getVariables());
            
            return CodeBlock.ExecutionResult.success();
        } catch (Exception e) {
            return CodeBlock.ExecutionResult.error("Ошибка выполнения скрипта: " + e.getMessage());
        }
    }
    
    /**
     * Добавление блока
     */
    public void addBlock(CodeBlock block) {
        blocks.add(block);
        updateModified();
        
        // Если это функция, добавляем в карту функций
        if (block.getType() == ru.openhousing.coding.blocks.BlockType.FUNCTION) {
            String functionName = (String) block.getParameter("name");
            if (functionName != null && !functionName.isEmpty()) {
                functions.put(functionName, block);
            }
        }
    }
    
    /**
     * Удаление блока
     */
    public void removeBlock(CodeBlock block) {
        blocks.remove(block);
        updateModified();
        
        // Если это функция, удаляем из карты функций
        if (block.getType() == ru.openhousing.coding.blocks.BlockType.FUNCTION) {
            String functionName = (String) block.getParameter("name");
            if (functionName != null) {
                functions.remove(functionName);
            }
        }
    }
    
    /**
     * Вставка блока в определенную позицию
     */
    public void insertBlock(int index, CodeBlock block) {
        blocks.add(index, block);
        updateModified();
        
        // Если это функция, добавляем в карту функций
        if (block.getType() == ru.openhousing.coding.blocks.BlockType.FUNCTION) {
            String functionName = (String) block.getParameter("name");
            if (functionName != null && !functionName.isEmpty()) {
                functions.put(functionName, block);
            }
        }
    }
    
    /**
     * Перемещение блока
     */
    public void moveBlock(int fromIndex, int toIndex) {
        if (fromIndex >= 0 && fromIndex < blocks.size() && 
            toIndex >= 0 && toIndex < blocks.size() && 
            fromIndex != toIndex) {
            
            CodeBlock block = blocks.remove(fromIndex);
            blocks.add(toIndex, block);
            updateModified();
        }
    }
    
    /**
     * Получение блока по индексу
     */
    public CodeBlock getBlock(int index) {
        if (index >= 0 && index < blocks.size()) {
            return blocks.get(index);
        }
        return null;
    }
    
    /**
     * Поиск блока по ID
     */
    public CodeBlock findBlock(UUID blockId) {
        return findBlockRecursive(blocks, blockId);
    }
    
    private CodeBlock findBlockRecursive(List<CodeBlock> searchBlocks, UUID blockId) {
        for (CodeBlock block : searchBlocks) {
            if (block.getId().equals(blockId)) {
                return block;
            }
            
            // Поиск в дочерних блоках
            CodeBlock found = findBlockRecursive(block.getChildBlocks(), blockId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
    
    /**
     * Очистка всех блоков
     */
    public void clear() {
        blocks.clear();
        functions.clear();
        updateModified();
    }
    
    /**
     * Валидация скрипта
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        
        for (int i = 0; i < blocks.size(); i++) {
            CodeBlock block = blocks.get(i);
            if (!block.validate()) {
                errors.add("Блок " + (i + 1) + " (" + block.getType().getDisplayName() + ") содержит ошибки");
            }
            
            // Валидация дочерних блоков
            validateChildBlocks(block, errors, String.valueOf(i + 1));
        }
        
        return errors;
    }
    
    private void validateChildBlocks(CodeBlock parent, List<String> errors, String path) {
        List<CodeBlock> children = parent.getChildBlocks();
        for (int i = 0; i < children.size(); i++) {
            CodeBlock child = children.get(i);
            String childPath = path + "." + (i + 1);
            
            if (!child.validate()) {
                errors.add("Блок " + childPath + " (" + child.getType().getDisplayName() + ") содержит ошибки");
            }
            
            validateChildBlocks(child, errors, childPath);
        }
    }
    
    /**
     * Получение статистики скрипта
     */
    public ScriptStats getStats() {
        return new ScriptStats(this);
    }
    
    /**
     * Клонирование скрипта
     */
    public CodeScript clone() {
        CodeScript clone = new CodeScript(this.playerId, this.playerName);
        clone.globalVariables.putAll(this.globalVariables);
        clone.enabled = this.enabled;
        
        // Клонирование блоков (поверхностное)
        clone.blocks.addAll(this.blocks);
        clone.functions.putAll(this.functions);
        
        return clone;
    }
    
    private void updateModified() {
        this.lastModified = System.currentTimeMillis();
    }
    
    // Геттеры и сеттеры
    public UUID getPlayerId() {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public List<CodeBlock> getBlocks() {
        return new ArrayList<>(blocks);
    }
    
    public Map<String, Object> getGlobalVariables() {
        return new HashMap<>(globalVariables);
    }
    
    public void setGlobalVariable(String name, Object value) {
        globalVariables.put(name, value);
        updateModified();
    }
    
    public Object getGlobalVariable(String name) {
        return globalVariables.get(name);
    }
    
    public void removeGlobalVariable(String name) {
        globalVariables.remove(name);
        updateModified();
    }
    
    public Map<String, CodeBlock> getFunctions() {
        return new HashMap<>(functions);
    }
    
    public long getLastModified() {
        return lastModified;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        updateModified();
    }
    
    public int getBlockCount() {
        return blocks.size();
    }
    
    public boolean isEmpty() {
        return blocks.isEmpty();
    }
    
    /**
     * Статистика скрипта
     */
    public static class ScriptStats {
        private final int totalBlocks;
        private final int eventBlocks;
        private final int conditionBlocks;
        private final int actionBlocks;
        private final int functionBlocks;
        private final int variableCount;
        private final int functionCount;
        private final boolean hasErrors;
        
        public ScriptStats(CodeScript script) {
            this.totalBlocks = countTotalBlocks(script.blocks);
            this.eventBlocks = countBlocksByCategory(script.blocks, ru.openhousing.coding.blocks.BlockType.BlockCategory.EVENT);
            this.conditionBlocks = countBlocksByCategory(script.blocks, ru.openhousing.coding.blocks.BlockType.BlockCategory.CONDITION);
            this.actionBlocks = countBlocksByCategory(script.blocks, ru.openhousing.coding.blocks.BlockType.BlockCategory.ACTION);
            this.functionBlocks = countBlocksByCategory(script.blocks, ru.openhousing.coding.blocks.BlockType.BlockCategory.FUNCTION);
            this.variableCount = script.globalVariables.size();
            this.functionCount = script.functions.size();
            this.hasErrors = !script.validate().isEmpty();
        }
        
        private int countTotalBlocks(List<CodeBlock> blocks) {
            int count = blocks.size();
            for (CodeBlock block : blocks) {
                count += countTotalBlocks(block.getChildBlocks());
            }
            return count;
        }
        
        private int countBlocksByCategory(List<CodeBlock> blocks, ru.openhousing.coding.blocks.BlockType.BlockCategory category) {
            int count = 0;
            for (CodeBlock block : blocks) {
                if (block.getType().getCategory() == category) {
                    count++;
                }
                count += countBlocksByCategory(block.getChildBlocks(), category);
            }
            return count;
        }
        
        // Геттеры
        public int getTotalBlocks() { return totalBlocks; }
        public int getEventBlocks() { return eventBlocks; }
        public int getConditionBlocks() { return conditionBlocks; }
        public int getActionBlocks() { return actionBlocks; }
        public int getFunctionBlocks() { return functionBlocks; }
        public int getVariableCount() { return variableCount; }
        public int getFunctionCount() { return functionCount; }
        public boolean hasErrors() { return hasErrors; }
    }
}
