package ru.openhousing.coding.blocks.conditions;

import org.bukkit.entity.Player;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок условия проверки переменной
 */
public class IfVariableConditionBlock extends CodeBlock {
    
    public enum ComparisonType {
        EQUALS("Равно", "=="),
        NOT_EQUALS("Не равно", "!="),
        GREATER_THAN("Больше", ">"),
        LESS_THAN("Меньше", "<"),
        GREATER_EQUALS("Больше или равно", ">="),
        LESS_EQUALS("Меньше или равно", "<="),
        CONTAINS("Содержит", "contains"),
        NOT_CONTAINS("Не содержит", "!contains"),
        STARTS_WITH("Начинается с", "starts"),
        ENDS_WITH("Заканчивается на", "ends"),
        IS_EMPTY("Пустое", "empty"),
        IS_NOT_EMPTY("Не пустое", "!empty"),
        IS_NULL("Null", "null"),
        IS_NOT_NULL("Не null", "!null"),
        IS_NUMBER("Число", "number"),
        IS_STRING("Строка", "string"),
        IS_BOOLEAN("Булево", "boolean");
        
        private final String displayName;
        private final String symbol;
        
        ComparisonType(String displayName, String symbol) {
            this.displayName = displayName;
            this.symbol = symbol;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getSymbol() {
            return symbol;
        }
    }
    
    public IfVariableConditionBlock() {
        super(BlockType.IF_VARIABLE);
        setParameter("variableName", "test_var");
        setParameter("comparisonType", ComparisonType.EQUALS);
        setParameter("compareValue", "test_value");
        setParameter("caseSensitive", true);
        setParameter("useRegex", false);
        setParameter("regexPattern", "");
        setParameter("checkGlobal", false);
        setParameter("checkLocal", true);
        setParameter("checkSystem", false);
        setParameter("defaultValue", "");
        setParameter("negateResult", false);
        setParameter("customMessage", "");
        setParameter("showDebug", false);
        setParameter("logResult", false);
        setParameter("saveResult", false);
        setParameter("resultVariable", "condition_result");
        setParameter("executeOnTrue", true);
        setParameter("executeOnFalse", false);
        setParameter("breakOnFalse", false);
        setParameter("continueOnFalse", false);
        setParameter("returnOnFalse", false);
        setParameter("returnValue", null);
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден");
        }
        
        try {
            // Получаем параметры
            String variableName = (String) getParameter("variableName");
            ComparisonType comparisonType = (ComparisonType) getParameter("comparisonType");
            Object compareValue = getParameter("compareValue");
            boolean caseSensitive = (Boolean) getParameter("caseSensitive");
            boolean useRegex = (Boolean) getParameter("useRegex");
            String regexPattern = (String) getParameter("regexPattern");
            boolean checkGlobal = (Boolean) getParameter("checkGlobal");
            boolean checkLocal = (Boolean) getParameter("checkLocal");
            boolean checkSystem = (Boolean) getParameter("checkSystem");
            Object defaultValue = getParameter("defaultValue");
            boolean negateResult = (Boolean) getParameter("negateResult");
            String customMessage = (String) getParameter("customMessage");
            boolean showDebug = (Boolean) getParameter("showDebug");
            boolean logResult = (Boolean) getParameter("logResult");
            boolean saveResult = (Boolean) getParameter("saveResult");
            String resultVariable = (String) getParameter("resultVariable");
            boolean executeOnTrue = (Boolean) getParameter("executeOnTrue");
            boolean executeOnFalse = (Boolean) getParameter("executeOnFalse");
            boolean breakOnFalse = (Boolean) getParameter("breakOnFalse");
            boolean continueOnFalse = (Boolean) getParameter("continueOnFalse");
            boolean returnOnFalse = (Boolean) getParameter("returnOnFalse");
            Object returnValue = getParameter("returnValue");
            
            // Получаем значение переменной
            Object variableValue = getVariableValue(context, variableName, checkGlobal, checkLocal, checkSystem, defaultValue);
            
            // Выполняем сравнение
            boolean result = performComparison(variableValue, compareValue, comparisonType, caseSensitive, useRegex, regexPattern);
            
            // Инвертируем результат если нужно
            if (negateResult) {
                result = !result;
            }
            
            // Показываем отладочную информацию
            if (showDebug) {
                showDebugInfo(player, variableName, variableValue, compareValue, comparisonType, result);
            }
            
            // Логируем результат
            if (logResult) {
                logConditionResult(player, variableName, variableValue, compareValue, comparisonType, result);
            }
            
            // Сохраняем результат в переменную
            if (saveResult && resultVariable != null) {
                context.setVariable(resultVariable, result);
            }
            
            // Показываем кастомное сообщение
            if (customMessage != null && !customMessage.trim().isEmpty()) {
                String message = replaceVariables(customMessage, context);
                message = message.replace("%result%", result ? "истинно" : "ложно");
                message = message.replace("%variable%", variableName);
                message = message.replace("%value%", String.valueOf(variableValue));
                message = message.replace("%compare%", String.valueOf(compareValue));
                player.sendMessage(message);
            }
            
            // Обрабатываем результат
            if (result) {
                if (executeOnTrue) {
                    return ExecutionResult.success();
                } else {
                    return ExecutionResult.continueLoop();
                }
            } else {
                if (executeOnFalse) {
                    return ExecutionResult.success();
                } else if (breakOnFalse) {
                    return ExecutionResult.breakLoop();
                } else if (continueOnFalse) {
                    return ExecutionResult.continueLoop();
                } else if (returnOnFalse) {
                    return ExecutionResult.returnValue(returnValue);
                } else {
                    return ExecutionResult.success();
                }
            }
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения условия переменной: " + e.getMessage());
        }
    }
    
    /**
     * Получить значение переменной
     */
    private Object getVariableValue(ExecutionContext context, String variableName, boolean checkGlobal, boolean checkLocal, boolean checkSystem, Object defaultValue) {
        Object value = null;
        
        // Проверяем локальные переменные
        if (checkLocal) {
            value = context.getVariable(variableName);
        }
        
        // Проверяем глобальные переменные
        if (value == null && checkGlobal) {
            value = context.getGlobalVariables().get(variableName);
        }
        
        // Проверяем системные переменные
        if (value == null && checkSystem) {
            value = context.getSystemVariables().get(variableName);
        }
        
        // Возвращаем значение по умолчанию если переменная не найдена
        if (value == null) {
            value = defaultValue;
        }
        
        return value;
    }
    
    /**
     * Выполнить сравнение
     */
    private boolean performComparison(Object variableValue, Object compareValue, ComparisonType comparisonType, boolean caseSensitive, boolean useRegex, String regexPattern) {
        if (variableValue == null && compareValue == null) {
            return comparisonType == ComparisonType.EQUALS || comparisonType == ComparisonType.IS_NULL;
        }
        
        if (variableValue == null) {
            return comparisonType == ComparisonType.IS_NULL || comparisonType == ComparisonType.IS_EMPTY;
        }
        
        if (compareValue == null) {
            return comparisonType == ComparisonType.IS_NOT_NULL || comparisonType == ComparisonType.IS_NOT_EMPTY;
        }
        
        String varStr = String.valueOf(variableValue);
        String compareStr = String.valueOf(compareValue);
        
        switch (comparisonType) {
            case EQUALS:
                if (useRegex && regexPattern != null) {
                    return varStr.matches(regexPattern);
                }
                if (!caseSensitive) {
                    return varStr.equalsIgnoreCase(compareStr);
                }
                return varStr.equals(compareStr);
                
            case NOT_EQUALS:
                if (useRegex && regexPattern != null) {
                    return !varStr.matches(regexPattern);
                }
                if (!caseSensitive) {
                    return !varStr.equalsIgnoreCase(compareStr);
                }
                return !varStr.equals(compareStr);
                
            case GREATER_THAN:
                return compareNumbers(variableValue, compareValue) > 0;
                
            case LESS_THAN:
                return compareNumbers(variableValue, compareValue) < 0;
                
            case GREATER_EQUALS:
                return compareNumbers(variableValue, compareValue) >= 0;
                
            case LESS_EQUALS:
                return compareNumbers(variableValue, compareValue) <= 0;
                
            case CONTAINS:
                if (!caseSensitive) {
                    return varStr.toLowerCase().contains(compareStr.toLowerCase());
                }
                return varStr.contains(compareStr);
                
            case NOT_CONTAINS:
                if (!caseSensitive) {
                    return !varStr.toLowerCase().contains(compareStr.toLowerCase());
                }
                return !varStr.contains(compareStr);
                
            case STARTS_WITH:
                if (!caseSensitive) {
                    return varStr.toLowerCase().startsWith(compareStr.toLowerCase());
                }
                return varStr.startsWith(compareStr);
                
            case ENDS_WITH:
                if (!caseSensitive) {
                    return varStr.toLowerCase().endsWith(compareStr.toLowerCase());
                }
                return varStr.endsWith(compareStr);
                
            case IS_EMPTY:
                return varStr.trim().isEmpty();
                
            case IS_NOT_EMPTY:
                return !varStr.trim().isEmpty();
                
            case IS_NULL:
                return variableValue == null;
                
            case IS_NOT_NULL:
                return variableValue != null;
                
            case IS_NUMBER:
                try {
                    Double.parseDouble(varStr);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
                
            case IS_STRING:
                return variableValue instanceof String;
                
            case IS_BOOLEAN:
                return variableValue instanceof Boolean || 
                       "true".equalsIgnoreCase(varStr) || 
                       "false".equalsIgnoreCase(varStr);
                
            default:
                return false;
        }
    }
    
    /**
     * Сравнить числа
     */
    private int compareNumbers(Object value1, Object value2) {
        try {
            double num1 = Double.parseDouble(String.valueOf(value1));
            double num2 = Double.parseDouble(String.valueOf(value2));
            return Double.compare(num1, num2);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Показать отладочную информацию
     */
    private void showDebugInfo(Player player, String variableName, Object variableValue, Object compareValue, ComparisonType comparisonType, boolean result) {
        String debugMessage = String.format(
            "§8[DEBUG] Условие переменной: §7%s %s %s §8= §7%s",
            variableName,
            comparisonType.getSymbol(),
            compareValue,
            result ? "§aистинно" : "§cложно"
        );
        player.sendMessage(debugMessage);
        
        String valueInfo = String.format(
            "§8[DEBUG] Значение переменной: §7%s §8(тип: §7%s§8)",
            variableValue,
            variableValue != null ? variableValue.getClass().getSimpleName() : "null"
        );
        player.sendMessage(valueInfo);
    }
    
    /**
     * Логировать результат условия
     */
    private void logConditionResult(Player player, String variableName, Object variableValue, Object compareValue, ComparisonType comparisonType, boolean result) {
        String logMessage = String.format(
            "[CONDITION] Player %s: Variable %s %s %s = %s (Value: %s)",
            player.getName(),
            variableName,
            comparisonType.getSymbol(),
            compareValue,
            result,
            variableValue
        );
        
        System.out.println(logMessage);
    }
    
    @Override
    public boolean validate() {
        return getParameter("variableName") != null && getParameter("comparisonType") != null;
    }
    
    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "§7Проверяет условие для переменной",
            "",
            "§eПараметры:",
            "§7• Имя переменной",
            "§7• Тип сравнения",
            "§7• Значение для сравнения",
            "§7• Регистрозависимость",
            "§7• Использование regex",
            "§7• Область поиска (локальная/глобальная/системная)",
            "§7• Значение по умолчанию",
            "§7• Инвертировать результат",
            "§7• Кастомное сообщение",
            "§7• Отладочная информация",
            "§7• Логирование результата",
            "§7• Сохранение результата",
            "§7• Действия при истине/лжи",
            "§7• Управление потоком выполнения"
        );
    }
    
    @Override
    public boolean matchesEvent(Object event) {
        return false; // Условия не привязаны к событиям
    }
    
    @Override
    public ExecutionContext createContextFromEvent(Object event) {
        return null; // Условия не создают контекст из событий
    }
}
