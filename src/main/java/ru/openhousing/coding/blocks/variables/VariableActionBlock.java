package ru.openhousing.coding.blocks.variables;

import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок действий с переменными
 */
public class VariableActionBlock extends CodeBlock {
    
    public enum VariableActionType {
        SET("Установить", "Устанавливает значение переменной"),
        ADD("Прибавить", "Прибавляет к переменной число"),
        SUBTRACT("Отнять", "Отнимает от переменной число"),
        MULTIPLY("Умножить", "Умножает переменную на число"),
        DIVIDE("Разделить", "Делит переменную на число"),
        APPEND("Присоединить", "Присоединяет текст к переменной"),
        INCREMENT("Увеличить на 1", "Увеличивает переменную на 1"),
        DECREMENT("Уменьшить на 1", "Уменьшает переменную на 1"),
        CLEAR("Очистить", "Удаляет переменную"),
        COPY("Копировать", "Копирует значение одной переменной в другую"),
        RANDOM_NUMBER("Случайное число", "Устанавливает случайное число"),
        RANDOM_TEXT("Случайный текст", "Выбирает случайный текст из списка"),
        CONVERT_TO_NUMBER("Преобразовать в число", "Преобразует переменную в число"),
        CONVERT_TO_TEXT("Преобразовать в текст", "Преобразует переменную в текст"),
        GET_PLAYER_INFO("Информация об игроке", "Получает информацию об игроке");
        
        private final String displayName;
        private final String description;
        
        VariableActionType(String displayName, String description) {
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
    
    public VariableActionBlock() {
        super(BlockType.VARIABLE_ACTION);
        setParameter("actionType", VariableActionType.SET);
        setParameter("variableName", "myVariable");
        setParameter("value", "");
        setParameter("secondVariable", "");
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        VariableActionType actionType = (VariableActionType) getParameter(ru.openhousing.coding.constants.BlockParams.ACTION_TYPE);
        String variableName = (String) getParameter(ru.openhousing.coding.constants.BlockParams.VARIABLE_NAME);
        String value = replaceVariables((String) getParameter(ru.openhousing.coding.constants.BlockParams.VALUE), context);
        String secondVariable = (String) getParameter(ru.openhousing.coding.constants.BlockParams.SECOND_VARIABLE);
        
        if (actionType == null || variableName == null || variableName.isEmpty()) {
            return ExecutionResult.error("Не указано имя переменной");
        }
        
        try {
            executeVariableAction(context, actionType, variableName, value, secondVariable);
            return ExecutionResult.success();
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения действия с переменной: " + e.getMessage());
        }
    }
    
    private void executeVariableAction(ExecutionContext context, VariableActionType actionType, 
                                     String variableName, String value, String secondVariable) {
        
        switch (actionType) {
            case SET:
                context.setVariable(variableName, parseValue(value));
                break;
                
            case ADD:
                Object currentValue = context.getVariable(variableName);
                if (currentValue instanceof Number && isNumeric(value)) {
                    double current = ((Number) currentValue).doubleValue();
                    double add = Double.parseDouble(value);
                    context.setVariable(variableName, current + add);
                }
                break;
                
            case SUBTRACT:
                currentValue = context.getVariable(variableName);
                if (currentValue instanceof Number && isNumeric(value)) {
                    double current = ((Number) currentValue).doubleValue();
                    double subtract = Double.parseDouble(value);
                    context.setVariable(variableName, current - subtract);
                }
                break;
                
            case MULTIPLY:
                currentValue = context.getVariable(variableName);
                if (currentValue instanceof Number && isNumeric(value)) {
                    double current = ((Number) currentValue).doubleValue();
                    double multiply = Double.parseDouble(value);
                    context.setVariable(variableName, current * multiply);
                }
                break;
                
            case DIVIDE:
                currentValue = context.getVariable(variableName);
                if (currentValue instanceof Number && isNumeric(value)) {
                    double current = ((Number) currentValue).doubleValue();
                    double divide = Double.parseDouble(value);
                    if (divide != 0) {
                        context.setVariable(variableName, current / divide);
                    }
                }
                break;
                
            case APPEND:
                currentValue = context.getVariable(variableName);
                String currentText = currentValue != null ? currentValue.toString() : "";
                context.setVariable(variableName, currentText + value);
                break;
                
            case INCREMENT:
                currentValue = context.getVariable(variableName);
                if (currentValue instanceof Number) {
                    double current = ((Number) currentValue).doubleValue();
                    context.setVariable(variableName, current + 1);
                } else {
                    context.setVariable(variableName, 1);
                }
                break;
                
            case DECREMENT:
                currentValue = context.getVariable(variableName);
                if (currentValue instanceof Number) {
                    double current = ((Number) currentValue).doubleValue();
                    context.setVariable(variableName, current - 1);
                } else {
                    context.setVariable(variableName, -1);
                }
                break;
                
            case CLEAR:
                context.getVariables().remove(variableName);
                break;
                
            case COPY:
                if (secondVariable != null && !secondVariable.isEmpty()) {
                    Object sourceValue = context.getVariable(secondVariable);
                    context.setVariable(variableName, sourceValue);
                }
                break;
                
            case RANDOM_NUMBER:
                String[] rangeParts = value.split("-");
                if (rangeParts.length == 2) {
                    try {
                        int min = Integer.parseInt(rangeParts[0].trim());
                        int max = Integer.parseInt(rangeParts[1].trim());
                        int randomValue = min + (int) (Math.random() * (max - min + 1));
                        context.setVariable(variableName, randomValue);
                    } catch (NumberFormatException e) {
                        context.setVariable(variableName, (int) (Math.random() * 100));
                    }
                } else {
                    context.setVariable(variableName, (int) (Math.random() * 100));
                }
                break;
                
            case RANDOM_TEXT:
                String[] textOptions = value.split(",");
                if (textOptions.length > 0) {
                    int randomIndex = (int) (Math.random() * textOptions.length);
                    context.setVariable(variableName, textOptions[randomIndex].trim());
                }
                break;
                
            case CONVERT_TO_NUMBER:
                currentValue = context.getVariable(variableName);
                if (currentValue != null) {
                    try {
                        double number = Double.parseDouble(currentValue.toString());
                        context.setVariable(variableName, number);
                    } catch (NumberFormatException e) {
                        context.setVariable(variableName, 0);
                    }
                }
                break;
                
            case CONVERT_TO_TEXT:
                currentValue = context.getVariable(variableName);
                if (currentValue != null) {
                    context.setVariable(variableName, currentValue.toString());
                }
                break;
                
            case GET_PLAYER_INFO:
                if (context.getPlayer() != null) {
                    switch (value.toLowerCase()) {
                        case "name":
                            context.setVariable(variableName, context.getPlayer().getName());
                            break;
                        case "health":
                            context.setVariable(variableName, context.getPlayer().getHealth());
                            break;
                        case "food":
                            context.setVariable(variableName, context.getPlayer().getFoodLevel());
                            break;
                        case "level":
                            context.setVariable(variableName, context.getPlayer().getLevel());
                            break;
                        case "x":
                            context.setVariable(variableName, context.getPlayer().getLocation().getX());
                            break;
                        case "y":
                            context.setVariable(variableName, context.getPlayer().getLocation().getY());
                            break;
                        case "z":
                            context.setVariable(variableName, context.getPlayer().getLocation().getZ());
                            break;
                        case "world":
                            context.setVariable(variableName, context.getPlayer().getWorld().getName());
                            break;
                        default:
                            context.setVariable(variableName, "unknown");
                            break;
                    }
                }
                break;
        }
    }
    
    /**
     * Парсинг значения
     */
    private Object parseValue(String value) {
        if (value == null) return null;
        
        // Попытка парсинга как число
        if (isNumeric(value)) {
            try {
                if (value.contains(".")) {
                    return Double.parseDouble(value);
                } else {
                    return Integer.parseInt(value);
                }
            } catch (NumberFormatException e) {
                // Игнорируем ошибку и возвращаем как строку
            }
        }
        
        // Парсинг как булево значение
        if (value.equalsIgnoreCase("true")) {
            return true;
        } else if (value.equalsIgnoreCase("false")) {
            return false;
        }
        
        // Возвращаем как строку
        return value;
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
        VariableActionType actionType = (VariableActionType) getParameter(ru.openhousing.coding.constants.BlockParams.ACTION_TYPE);
        String variableName = (String) getParameter(ru.openhousing.coding.constants.BlockParams.VARIABLE_NAME);
        String value = (String) getParameter(ru.openhousing.coding.constants.BlockParams.VALUE);
        
        return Arrays.asList(
            "§6Действие переменной",
            "§7Действие: §f" + (actionType != null ? actionType.getDisplayName() : "Не выбрано"),
            "§7Переменная: §f" + (variableName != null ? variableName : "Не указана"),
            "§7Значение: §f" + (value != null && !value.isEmpty() ? value : "Не указано"),
            "",
            "§8" + (actionType != null ? actionType.getDescription() : "")
        );
    }
}
