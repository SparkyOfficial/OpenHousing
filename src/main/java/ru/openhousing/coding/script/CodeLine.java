package ru.openhousing.coding.script;

import ru.openhousing.coding.blocks.CodeBlock;

import java.util.*;

/**
 * Строка кода - содержит блоки кода в определенном порядке
 */
public class CodeLine {
    
    private final int lineNumber;
    private String name;
    private String description;
    private final List<CodeBlock> blocks;
    private boolean enabled;
    private final Map<String, Object> metadata;
    
    public CodeLine(int lineNumber) {
        this.lineNumber = lineNumber;
        this.name = "Строка " + lineNumber;
        this.description = "";
        this.blocks = new ArrayList<>();
        this.enabled = true;
        this.metadata = new HashMap<>();
    }
    
    public CodeLine(int lineNumber, String name) {
        this.lineNumber = lineNumber;
        this.name = name;
        this.description = "";
        this.blocks = new ArrayList<>();
        this.enabled = true;
        this.metadata = new HashMap<>();
    }
    
    /**
     * Добавление блока в строку
     */
    public void addBlock(CodeBlock block) {
        blocks.add(block);
    }
    
    /**
     * Добавление блока в определенную позицию
     */
    public void addBlock(int index, CodeBlock block) {
        if (index >= 0 && index <= blocks.size()) {
            blocks.add(index, block);
        }
    }
    
    /**
     * Удаление блока
     */
    public boolean removeBlock(CodeBlock block) {
        return blocks.remove(block);
    }
    
    /**
     * Удаление блока по индексу
     */
    public CodeBlock removeBlock(int index) {
        if (index >= 0 && index < blocks.size()) {
            return blocks.remove(index);
        }
        return null;
    }
    
    /**
     * Перемещение блока
     */
    public boolean moveBlock(int fromIndex, int toIndex) {
        if (fromIndex >= 0 && fromIndex < blocks.size() && 
            toIndex >= 0 && toIndex < blocks.size()) {
            CodeBlock block = blocks.remove(fromIndex);
            blocks.add(toIndex, block);
            return true;
        }
        return false;
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
     * Замена блока по индексу
     */
    public boolean replaceBlock(int index, CodeBlock newBlock) {
        if (index >= 0 && index < blocks.size()) {
            blocks.set(index, newBlock);
            return true;
        }
        return false;
    }
    
    /**
     * Очистка всех блоков
     */
    public void clearBlocks() {
        blocks.clear();
    }
    
    /**
     * Проверка валидности строки
     */
    public boolean isValid() {
        // Проверяем все блоки в строке
        for (CodeBlock block : blocks) {
            if (!block.validate()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Выполнение всех блоков в строке с поддержкой IF-ELSE логики
     */
    public void execute(CodeBlock.ExecutionContext context) {
        if (!enabled) {
            return;
        }
        
        boolean lastConditionResult = false;
        
        for (int i = 0; i < blocks.size(); i++) {
            CodeBlock block = blocks.get(i);
            
            try {
                // Специальная логика для ELSE блоков
                if (block.getType() == ru.openhousing.coding.blocks.BlockType.ELSE) {
                    // ELSE выполняется только если предыдущее условие было ложным
                    if (!lastConditionResult) {
                        CodeBlock.ExecutionResult result = block.execute(context);
                        if (result.getType() == CodeBlock.ExecutionResult.Type.ERROR) {
                            break;
                        }
                    }
                    lastConditionResult = false; // Сбрасываем флаг
                    continue;
                }
                
                CodeBlock.ExecutionResult result = block.execute(context);
                
                // Запоминаем результат условных блоков
                if (isConditionalBlock(block.getType())) {
                    lastConditionResult = (result.getType() == CodeBlock.ExecutionResult.Type.SUCCESS);
                } else {
                    lastConditionResult = false;
                }
                
                // Если блок запросил остановку выполнения
                if (result.getType() == CodeBlock.ExecutionResult.Type.ERROR) {
                    break;
                }
            } catch (Exception e) {
                // Логируем ошибку и продолжаем
                System.err.println("Ошибка выполнения блока в строке " + lineNumber + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Проверяет, является ли блок условным
     */
    private boolean isConditionalBlock(ru.openhousing.coding.blocks.BlockType blockType) {
        return blockType == ru.openhousing.coding.blocks.BlockType.IF_PLAYER ||
               blockType == ru.openhousing.coding.blocks.BlockType.IF_ENTITY ||
               blockType == ru.openhousing.coding.blocks.BlockType.IF_VARIABLE ||
               blockType == ru.openhousing.coding.blocks.BlockType.ITEM_CHECK;
    }
    

    
    // Геттеры и сеттеры
    public int getLineNumber() {
        return lineNumber;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<CodeBlock> getBlocks() {
        return new ArrayList<>(blocks);
    }
    
    public int getBlockCount() {
        return blocks.size();
    }
    
    public boolean isEmpty() {
        return blocks.isEmpty();
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }
    
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    public void removeMetadata(String key) {
        metadata.remove(key);
    }
    
    @Override
    public String toString() {
        return "CodeLine{" +
                "number=" + lineNumber +
                ", name='" + name + '\'' +
                ", blocks=" + blocks.size() +
                ", enabled=" + enabled +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeLine codeLine = (CodeLine) o;
        return lineNumber == codeLine.lineNumber;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(lineNumber);
    }
    
    /**
     * Создание копии строки
     */
    public CodeLine clone() {
        CodeLine copy = new CodeLine(this.lineNumber + 1000, this.name + " (копия)"); // Временный номер
        copy.setDescription(this.description);
        copy.setEnabled(this.enabled);
        
        // Копируем блоки
        for (CodeBlock block : this.blocks) {
            // TODO: Реализовать клонирование блоков когда будет метод clone() в CodeBlock
            // copy.addBlock(block.clone());
        }
        
        // Копируем метаданные
        copy.metadata.putAll(this.metadata);
        
        return copy;
    }
}
