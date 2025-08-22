package ru.openhousing.coding.blocks.conditions;

import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок условия "Если переменная"
 */
public class IfVariableBlock extends CodeBlock {
    
    public enum VariableConditionType {
        EQUALS("Равна", "Проверяет, равна ли переменная значению"),
        NOT_EQUALS("Не равна", "Проверяет, не равна ли переменная значению"),
        GREATER("Больше", "Проверяет, больше ли переменная значения"),
        LESS("Меньше", "Проверяет, меньше ли переменная значения"),
        GREATER_OR_EQUAL("Больше или равна", "Проверяет, больше ли переменная значения или равна ему"),
        LESS_OR_EQUAL("Меньше или равна", "Проверяет, меньше ли переменная значения или равна ему"),
        CONTAINS("Содержит", "Проверяет, содержит ли переменная текст"),
        NOT_CONTAINS("Не содержит", "Проверяет, не содержит ли переменная текст"),
        STARTS_WITH("Начинается с", "Проверяет, начинается ли переменная с текста"),
        ENDS_WITH("Заканчивается на", "Проверяет, заканчивается ли переменная на текст"),
        IS_NUMBER("Является числом", "Проверяет, является ли переменная числом"),
        IS_TEXT("Является текстом", "Проверяет, является ли переменная текстом"),
        IS_EMPTY("Пустая", "Проверяет, пустая ли переменная"),
        NOT_EMPTY("Не пустая", "Проверяет, не пустая ли переменная"),
        EXISTS("Существует", "Проверяет, существует ли переменная"),
        NOT_EXISTS("Не существует", "Проверяет, не существует ли переменная"),
        BETWEEN("Между", "Проверяет, находится ли переменная между двумя значениями");
        
        private final String displayName;
        private final String description;
        
        VariableConditionType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public IfVariableBlock() {
        super(BlockType.IF_VARIABLE_EQUALS); // Используем любое условие переменной как базовое
        setParameter(ru.openhousing.coding.constants.BlockParams.CONDITION_TYPE, VariableConditionType.EQUALS);
        setParameter(ru.openhousing.coding.constants.BlockParams.VARIABLE_NAME, "myVariable");
        setParameter(ru.openhousing.coding.constants.BlockParams.VALUE, "");
        setParameter(ru.openhousing.coding.constants.BlockParams.SECOND_VALUE, "");
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        String variableName = (String) getParameter(ru.openhousing.coding.constants.BlockParams.VARIABLE_NAME);
        
        if (variableName == null || variableName.isEmpty()) {
            return ExecutionResult.error("Не указано имя переменной");
        }
        
        boolean conditionMet = checkCondition(context, variableName);
        
        if (conditionMet) {
            return executeChildren(context);
        }
        
        return ExecutionResult.success();
    }
    
    private boolean checkCondition(ExecutionContext context, String variableName) {
        VariableConditionType conditionType = (VariableConditionType) getParameter(ru.openhousing.coding.constants.BlockParams.CONDITION_TYPE);
        String value = replaceVariables((String) getParameter(ru.openhousing.coding.constants.BlockParams.VALUE), context);
        String secondValue = replaceVariables((String) getParameter(ru.openhousing.coding.constants.BlockParams.SECOND_VALUE), context);
        
        if (conditionType == null) {
            return false;
        }
        
        Object variableValue = context.getVariable(variableName);
        
        try {
            switch (conditionType) {
                case EXISTS:
                    return variableValue != null;
                    
                case NOT_EXISTS:
                    return variableValue == null;
                    
                case IS_EMPTY:
                    return variableValue == null || variableValue.toString().isEmpty();
                    
                case NOT_EMPTY:
                    return variableValue != null && !variableValue.toString().isEmpty();
                    
                case IS_NUMBER:
                    if (variableValue == null) return false;
                    return isNumeric(variableValue.toString());
                    
                case IS_TEXT:
                    return variableValue instanceof String;
                    
                case EQUALS:
                    if (variableValue == null) return value == null || value.isEmpty();
                    return variableValue.toString().equals(value);
                    
                case NOT_EQUALS:
                    if (variableValue == null) return value != null && !value.isEmpty();
                    return !variableValue.toString().equals(value);
                    
                case CONTAINS:
                    if (variableValue == null) return false;
                    return variableValue.toString().toLowerCase().contains(value.toLowerCase());
                    
                case NOT_CONTAINS:
                    if (variableValue == null) return true;
                    return !variableValue.toString().toLowerCase().contains(value.toLowerCase());
                    
                case STARTS_WITH:
                    if (variableValue == null) return false;
                    return variableValue.toString().toLowerCase().startsWith(value.toLowerCase());
                    
                case ENDS_WITH:
                    if (variableValue == null) return false;
                    return variableValue.toString().toLowerCase().endsWith(value.toLowerCase());
                    
                case GREATER:
                    return compareNumbers(variableValue, value) > 0;
                    
                case LESS:
                    return compareNumbers(variableValue, value) < 0;
                    
                case GREATER_OR_EQUAL:
                    return compareNumbers(variableValue, value) >= 0;
                    
                case LESS_OR_EQUAL:
                    return compareNumbers(variableValue, value) <= 0;
                    
                case BETWEEN:
                    if (secondValue == null || secondValue.isEmpty()) return false;
                    double varNum = parseNumber(variableValue);
                    double minNum = parseNumber(value);
                    double maxNum = parseNumber(secondValue);
                    return varNum >= minNum && varNum <= maxNum;
                    
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Сравнение чисел
     */
    private int compareNumbers(Object variableValue, String compareValue) {
        try {
            double varNum = parseNumber(variableValue);
            double compareNum = parseNumber(compareValue);
            return Double.compare(varNum, compareNum);
        } catch (Exception e) {
            // Если не удается сравнить как числа, сравниваем как строки
            String varStr = variableValue != null ? variableValue.toString() : "";
            return varStr.compareTo(compareValue);
        }
    }
    
    /**
     * Парсинг числа из объекта
     */
    private double parseNumber(Object value) {
        if (value == null) return 0;
        
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot parse as number: " + value);
        }
    }
    
    /**
     * Парсинг числа из строки
     */
    private double parseNumber(String value) {
        if (value == null || value.isEmpty()) return 0;
        
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot parse as number: " + value);
        }
    }
    
    /**
     * Проверка, является ли строка числом
     */
    private boolean isNumeric(String str) {
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
     * Замена переменных в строке
     */

    
    @Override
    public boolean validate() {
        String variableName = (String) getParameter(ru.openhousing.coding.constants.BlockParams.VARIABLE_NAME);
        return variableName != null && !variableName.trim().isEmpty();
    }
    
    @Override
    public List<String> getDescription() {
        VariableConditionType conditionType = (VariableConditionType) getParameter(ru.openhousing.coding.constants.BlockParams.CONDITION_TYPE);
        String variableName = (String) getParameter(ru.openhousing.coding.constants.BlockParams.VARIABLE_NAME);
        String value = (String) getParameter(ru.openhousing.coding.constants.BlockParams.VALUE);
        
        return Arrays.asList(
            "§6Если переменная",
            "§7Переменная: §f" + (variableName != null ? variableName : "Не указана"),
            "§7Условие: §f" + (conditionType != null ? conditionType.getDisplayName() : "Не выбрано"),
            "§7Значение: §f" + (value != null && !value.isEmpty() ? value : "Не указано"),
            "",
            "§8Дочерних блоков: " + childBlocks.size()
        );
    }
}
