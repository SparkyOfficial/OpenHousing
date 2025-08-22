package ru.openhousing.coding.script;

import ru.openhousing.coding.blocks.CodeBlock;

import java.util.*;

/**
 * Код игрока с системой строк
 */
public class CodeScript {
    
    private final UUID playerId;
    private String playerName;
    private final Map<Integer, CodeLine> lines; // Номер строки -> строка
    private final Map<String, Object> globalVariables;
    private final Map<String, CodeBlock> functions;
    private long lastModified;
    private boolean enabled;
    private int nextLineNumber;
    private String boundWorld; // мир/дом, к которому привязан код
    
    public CodeScript(UUID playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.lines = new LinkedHashMap<>();
        this.globalVariables = new HashMap<>();
        this.functions = new HashMap<>();
        this.lastModified = System.currentTimeMillis();
        this.enabled = true;
        this.nextLineNumber = 1;
        this.boundWorld = null;
    }
    
    /**
     * Выполнение кода
     */
    public CodeBlock.ExecutionResult execute(CodeBlock.ExecutionContext context) {
        if (!enabled) {
            System.out.println("[OpenHousing] Код отключен для игрока: " + playerName);
            return CodeBlock.ExecutionResult.error("Код отключен");
        }
        
        System.out.println("[OpenHousing] Начинаем выполнение кода игрока: " + playerName);
        System.out.println("[OpenHousing] Количество строк кода: " + lines.size());
        System.out.println("[OpenHousing] Количество блоков: " + getAllBlocks().size());
        
        // Добавляем глобальные переменные в контекст
        context.getVariables().putAll(globalVariables);
        System.out.println("[OpenHousing] Загружено глобальных переменных: " + globalVariables.size());
        
        // Добавляем функции в контекст
        context.getFunctions().putAll(functions);
        System.out.println("[OpenHousing] Загружено функций: " + functions.size());
        
        try {
            int executedLines = 0;
            int executedBlocks = 0;
            
            // Выполняем все строки в порядке их номеров
            for (CodeLine line : lines.values()) {
                if (line.isEnabled()) {
                    System.out.println("[OpenHousing] Выполняем строку " + line.getLineNumber() + ": " + line.getName());
                    System.out.println("[OpenHousing] Блоков в строке: " + line.getBlocks().size());
                    
                    line.execute(context);
                    executedLines++;
                    executedBlocks += line.getBlocks().size();
                    
                    System.out.println("[OpenHousing] Строка " + line.getLineNumber() + " выполнена успешно");
                } else {
                    System.out.println("[OpenHousing] Строка " + line.getLineNumber() + " отключена, пропускаем");
                }
            }
            
            // Сохраняем обновленные глобальные переменные
            globalVariables.putAll(context.getVariables());
            
            System.out.println("[OpenHousing] Выполнение завершено успешно!");
            System.out.println("[OpenHousing] Выполнено строк: " + executedLines);
            System.out.println("[OpenHousing] Выполнено блоков: " + executedBlocks);
            System.out.println("[OpenHousing] Итоговых переменных: " + context.getVariables().size());
            
            return CodeBlock.ExecutionResult.success();
        } catch (Exception e) {
            System.out.println("[OpenHousing] КРИТИЧЕСКАЯ ОШИБКА при выполнении кода: " + e.getMessage());
            e.printStackTrace();
            return CodeBlock.ExecutionResult.error("Ошибка выполнения кода: " + e.getMessage());
        }
    }
    
    /**
     * Создание новой строки
     */
    public CodeLine createLine(String name) {
        CodeLine line = new CodeLine(nextLineNumber++, name);
        lines.put(line.getLineNumber(), line);
        updateModified();
        return line;
    }
    
    /**
     * Создание новой строки с автоматическим именем
     */
    public CodeLine createLine() {
        return createLine("Строка " + nextLineNumber);
    }
    
    /**
     * Получение строки по номеру
     */
    public CodeLine getLine(int lineNumber) {
        return lines.get(lineNumber);
    }
    
    /**
     * Удаление строки
     */
    public boolean removeLine(int lineNumber) {
        CodeLine removed = lines.remove(lineNumber);
        if (removed != null) {
            updateModified();
            return true;
        }
        return false;
    }
    
    /**
     * Добавление блока в строку
     */
    public boolean addBlockToLine(int lineNumber, CodeBlock block) {
        CodeLine line = lines.get(lineNumber);
        if (line != null) {
            line.addBlock(block);
            updateModified();
            
            // Если это функция, добавляем в карту функций
            if (block.getType() == ru.openhousing.coding.blocks.BlockType.FUNCTION) {
                String functionName = (String) block.getParameter("name");
                if (functionName != null && !functionName.isEmpty()) {
                    functions.put(functionName, block);
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * Добавление блока (создает новую строку если нет)
     */
    public void addBlock(CodeBlock block) {
        if (lines.isEmpty()) {
            createLine();
        }
        
        // Добавляем в последнюю строку
        CodeLine lastLine = lines.values().stream()
            .reduce((first, second) -> second)
            .orElse(null);
            
        if (lastLine != null) {
            addBlockToLine(lastLine.getLineNumber(), block);
        }
    }
    
    /**
     * Удаление блока из строки
     */
    public boolean removeBlockFromLine(int lineNumber, CodeBlock block) {
        CodeLine line = lines.get(lineNumber);
        if (line != null) {
            boolean removed = line.removeBlock(block);
            if (removed) {
                updateModified();
                
                // Если это функция, удаляем из карты функций
                if (block.getType() == ru.openhousing.coding.blocks.BlockType.FUNCTION) {
                    String functionName = (String) block.getParameter("name");
                    if (functionName != null) {
                        functions.remove(functionName);
                    }
                }
            }
            return removed;
        }
        return false;
    }
    
    /**
     * Получение всех блоков из всех строк
     */
    public List<CodeBlock> getAllBlocks() {
        List<CodeBlock> allBlocks = new ArrayList<>();
        for (CodeLine line : lines.values()) {
            allBlocks.addAll(line.getBlocks());
        }
        return allBlocks;
    }
    
    /**
     * Получение списка всех строк
     */
    public List<CodeLine> getLines() {
        return new ArrayList<>(lines.values());
    }
    
    /**
     * Поиск блока по ID во всех строках
     */
    public CodeBlock findBlock(UUID blockId) {
        for (CodeLine line : lines.values()) {
            for (CodeBlock block : line.getBlocks()) {
                if (block.getId().equals(blockId)) {
                    return block;
                }
                // Поиск в дочерних блоках
                CodeBlock found = findBlockRecursive(block.getChildBlocks(), blockId);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
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
     * Очистка всех строк и блоков
     */
    public void clear() {
        lines.clear();
        functions.clear();
        nextLineNumber = 1;
        updateModified();
    }
    
    /**
     * Валидация кода
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        
        for (CodeLine line : lines.values()) {
            List<CodeBlock> lineBlocks = line.getBlocks();
            for (int i = 0; i < lineBlocks.size(); i++) {
                CodeBlock block = lineBlocks.get(i);
                if (!block.validate()) {
                    errors.add("Строка " + line.getLineNumber() + ", блок " + (i + 1) + 
                             " (" + block.getType().getDisplayName() + ") содержит ошибки");
                }
                
                // Валидация дочерних блоков
                validateChildBlocks(block, errors, "Строка " + line.getLineNumber() + "." + (i + 1));
            }
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
     * Получение статистики кода
     */
    public ScriptStats getStats() {
        return new ScriptStats(this);
    }
    
    /**
     * Клонирование кода
     */
    public CodeScript clone() {
        CodeScript clone = new CodeScript(this.playerId, this.playerName);
        clone.globalVariables.putAll(this.globalVariables);
        clone.enabled = this.enabled;
        clone.boundWorld = this.boundWorld;
        
        // Клонирование строк (поверхностное)
        clone.lines.putAll(this.lines);
        clone.functions.putAll(this.functions);
        clone.nextLineNumber = this.nextLineNumber;
        
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
        return getAllBlocks();
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
        return getAllBlocks().size();
    }
    
    public boolean isEmpty() {
        return lines.isEmpty() || getAllBlocks().isEmpty();
    }

    // Привязка к миру/дому
    public void setBoundWorld(String worldName) {
        this.boundWorld = worldName;
        updateModified();
    }
    public String getBoundWorld() { return boundWorld; }
    
    /**
     * Статистика кода
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
        
        public ScriptStats(CodeScript script) {            List<CodeBlock> allBlocks = script.getAllBlocks();
            this.totalBlocks = countTotalBlocks(allBlocks);
            this.eventBlocks = countBlocksByCategory(allBlocks, ru.openhousing.coding.blocks.BlockType.BlockCategory.EVENT);
            this.conditionBlocks = countBlocksByCategory(allBlocks, ru.openhousing.coding.blocks.BlockType.BlockCategory.CONDITION);
            this.actionBlocks = countBlocksByCategory(allBlocks, ru.openhousing.coding.blocks.BlockType.BlockCategory.ACTION);
            this.functionBlocks = countBlocksByCategory(allBlocks, ru.openhousing.coding.blocks.BlockType.BlockCategory.FUNCTION);
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
    
    /**
     * Получение использованных переменных
     */
    public Set<String> getUsedVariables() {
        Set<String> variables = new HashSet<>();
        
        for (CodeLine line : lines.values()) {
            for (CodeBlock block : line.getBlocks()) {
                // Анализируем параметры блока на предмет переменных
                // Переменные обычно имеют формат {variable_name}
                Map<String, Object> params = block.getParameters();
                for (Object value : params.values()) {
                    if (value instanceof String) {
                        String str = (String) value;
                        extractVariables(str, variables);
                    }
                }
            }
        }
        
        return variables;
    }
    
    /**
     * Извлечение переменных из строки
     */
    private void extractVariables(String text, Set<String> variables) {
        if (text == null) return;
        
        int start = text.indexOf('{');
        while (start != -1) {
            int end = text.indexOf('}', start);
            if (end != -1) {
                String variable = text.substring(start + 1, end);
                if (!variable.isEmpty()) {
                    variables.add(variable);
                }
                start = text.indexOf('{', end);
            } else {
                break;
            }
        }
    }
    
    /**
     * Вставить строку в определенную позицию
     */
    public void insertLine(int position, CodeLine line) {
        // Сдвигаем все строки с номерами >= position на 1 вверх
        Map<Integer, CodeLine> newLines = new LinkedHashMap<>();
        
        for (Map.Entry<Integer, CodeLine> entry : lines.entrySet()) {
            int lineNumber = entry.getKey();
            CodeLine codeLine = entry.getValue();
            
            if (lineNumber >= position) {
                newLines.put(lineNumber + 1, codeLine);
            } else {
                newLines.put(lineNumber, codeLine);
            }
        }
        
        // Добавляем новую строку
        newLines.put(position, line);
        
        // Заменяем карту строк
        lines.clear();
        lines.putAll(newLines);
        
        // Обновляем nextLineNumber
        nextLineNumber = Math.max(nextLineNumber, lines.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1);
        lastModified = System.currentTimeMillis();
    }
    
    /**
     * Переместить строку вверх
     */
    public boolean moveLineUp(int lineNumber) {
        if (lineNumber <= 1 || !lines.containsKey(lineNumber)) {
            return false;
        }
        
        CodeLine currentLine = lines.get(lineNumber);
        CodeLine previousLine = lines.get(lineNumber - 1);
        
        if (previousLine == null) {
            return false;
        }
        
        // Меняем местами в карте
        lines.put(lineNumber - 1, currentLine);
        lines.put(lineNumber, previousLine);
        
        lastModified = System.currentTimeMillis();
        return true;
    }
    
    /**
     * Переместить строку вниз
     */
    public boolean moveLineDown(int lineNumber) {
        if (!lines.containsKey(lineNumber) || !lines.containsKey(lineNumber + 1)) {
            return false;
        }
        
        CodeLine currentLine = lines.get(lineNumber);
        CodeLine nextLine = lines.get(lineNumber + 1);
        
        // Меняем местами в карте
        lines.put(lineNumber, nextLine);
        lines.put(lineNumber + 1, currentLine);
        
        lastModified = System.currentTimeMillis();
        return true;
    }
    
    /**
     * Получить количество строк
     */
    public int getLineCount() {
        return lines.size();
    }
    
    /**
     * Получить следующий доступный номер строки
     */
    public int getNextAvailableLineNumber() {
        return nextLineNumber;
    }
    
    /**
     * Добавить строку в скрипт
     */
    public void addLine(CodeLine line) {
        lines.put(line.getLineNumber(), line);
        if (line.getLineNumber() >= nextLineNumber) {
            nextLineNumber = line.getLineNumber() + 1;
        }
        updateModified();
    }
}
