package ru.openhousing.coding.blocks.text;

import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.values.TextValue;
import ru.openhousing.coding.values.Value;
import ru.openhousing.coding.values.ValueType;

import java.util.Arrays;
import java.util.List;

/**
 * Блок операций с текстом
 */
public class TextOperationBlock extends CodeBlock {
    
    public enum TextOperation {
        CONCATENATE("Объединение", "Объединить строки"),
        SUBSTRING("Подстрока", "Извлечь часть строки"),
        REPLACE("Замена", "Заменить текст"),
        UPPERCASE("Верхний регистр", "Преобразовать в верхний регистр"),
        LOWERCASE("Нижний регистр", "Преобразовать в нижний регистр"),
        TRIM("Обрезка", "Убрать пробелы в начале и конце"),
        LENGTH("Длина", "Получить длину строки"),
        SPLIT("Разделение", "Разделить строку по разделителю"),
        CONTAINS("Содержит", "Проверить, содержит ли строка подстроку"),
        STARTS_WITH("Начинается с", "Проверить, начинается ли строка с подстроки"),
        ENDS_WITH("Заканчивается на", "Проверить, заканчивается ли строка на подстроку");
        
        private final String displayName;
        private final String description;
        
        TextOperation(String displayName, String description) {
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
    
    private TextOperation operation;
    private Value text1;
    private Value text2; // Второй текст для операций типа CONCATENATE, REPLACE
    private String resultVariable;
    
    public TextOperationBlock() {
        super(BlockType.TEXT_OPERATION);
        this.operation = TextOperation.CONCATENATE;
        this.text1 = new TextValue("");
        this.text2 = new TextValue("");
        this.resultVariable = "result";
        
        setParameter("operation", operation.name());
        setParameter("text1", text1.getRawValue());
        setParameter("text2", text2.getRawValue());
        setParameter("resultVariable", resultVariable);
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        try {
            if (operation == null) {
                return ExecutionResult.error("Операция с текстом не выбрана");
            }
            
            if (text1 == null) {
                return ExecutionResult.error("Первый текстовый параметр не указан");
            }
            
            if ((operation == TextOperation.CONCATENATE || operation == TextOperation.REPLACE) && text2 == null) {
                return ExecutionResult.error("Второй текстовый параметр не указан для операции " + operation.getDisplayName());
            }
            
            if (resultVariable == null || resultVariable.trim().isEmpty()) {
                return ExecutionResult.error("Переменная для результата не указана");
            }
            
            String str1 = getStringValue(text1, context);
            String str2 = "";
            
            if (text2 != null) {
                str2 = getStringValue(text2, context);
            }
            
            // Проверяем разумность входных данных
            if (str1.length() > 10000) {
                if (context.getPlayer() != null) {
                    context.getPlayer().sendMessage("§c[OpenHousing] Первый текст слишком длинный: " + str1.length() + " символов. Максимум: 10,000");
                }
                str1 = str1.substring(0, 10000);
            }
            
            if (str2.length() > 1000) {
                if (context.getPlayer() != null) {
                    context.getPlayer().sendMessage("§c[OpenHousing] Второй текст слишком длинный: " + str2.length() + " символов. Максимум: 1,000");
                }
                str2 = str2.substring(0, 1000);
            }
            
            String result = performTextOperation(str1, str2);
            
            // Проверяем разумность результата
            if (result.length() > 50000) {
                if (context.getPlayer() != null) {
                    context.getPlayer().sendMessage("§c[OpenHousing] Результат операции слишком длинный: " + result.length() + " символов. Обрезан до 50,000");
                }
                result = result.substring(0, 50000);
            }
            
            context.setVariable(resultVariable, result);
            
            if (context.getPlayer() != null) {
                context.getPlayer().sendMessage("§a[OpenHousing] Результат операции " + operation.getDisplayName() + ": " + 
                    (result.length() > 100 ? result.substring(0, 100) + "..." : result));
            }
            
            return ExecutionResult.success();
            
        } catch (Exception e) {
            String errorMsg = "Ошибка выполнения текстовой операции: " + e.getMessage();
            if (context.getPlayer() != null) {
                context.getPlayer().sendMessage("§c[OpenHousing] " + errorMsg);
            }
            return ExecutionResult.error(errorMsg);
        }
    }

    /**
     * Выполнение текстовой операции
     */
    private String performTextOperation(String str1, String str2) {
        switch (operation) {
            case CONCATENATE:
                return str1 + str2;
            case REPLACE:
                if (str2.isEmpty()) {
                    return str1; // Если заменяющая строка пустая, возвращаем исходный текст
                }
                return str1.replace(str2, "");
            case UPPERCASE:
                return str1.toUpperCase();
            case LOWERCASE:
                return str1.toLowerCase();
            case TRIM:
                return str1.trim();
            case LENGTH:
                return String.valueOf(str1.length());
            case SPLIT:
                // str1 = текст, str2 = разделитель
                if (str2.isEmpty()) {
                    return str1; // Если разделитель пустой, возвращаем исходный текст
                }
                String[] splitResult = str1.split(str2);
                return String.join(", ", splitResult);
            case CONTAINS:
                return String.valueOf(str1.contains(str2));
            case STARTS_WITH:
                return String.valueOf(str1.startsWith(str2));
            case ENDS_WITH:
                return String.valueOf(str1.endsWith(str2));
            default:
                return str1;
        }
    }
    
    /**
     * Получение строкового значения из Value
     */
    private String getStringValue(Value value, ExecutionContext context) throws Exception {
        Object result = value.getValue(context.getPlayer(), context);
        
        if (result instanceof String) {
            return (String) result;
        } else {
            return String.valueOf(result);
        }
    }
    
    @Override
    public boolean validate() {
        return operation != null && text1 != null && 
               resultVariable != null && !resultVariable.trim().isEmpty();
    }
    
    @Override
    public List<String> getDescription() {
        String description = "§6" + operation.getDisplayName();
        
        if (operation == TextOperation.CONCATENATE || operation == TextOperation.REPLACE) {
            description += "\n§7" + text1.getDisplayValue() + " + " + text2.getDisplayValue();
        } else {
            description += "\n§7" + text1.getDisplayValue();
        }
        
        description += "\n§7Результат в: §f%" + resultVariable + "%";
        
        return Arrays.asList(description.split("\n"));
    }
    
    // Сеттеры
    public void setOperation(TextOperation operation) {
        this.operation = operation;
        setParameter("operation", operation.name());
    }
    
    public void setText1(Value text1) {
        this.text1 = text1;
        setParameter("text1", text1.getRawValue());
    }
    
    public void setText2(Value text2) {
        this.text2 = text2;
        setParameter("text2", text2.getRawValue());
    }
    
    public void setResultVariable(String resultVariable) {
        this.resultVariable = resultVariable;
        setParameter("resultVariable", resultVariable);
    }
    
    // Геттеры
    public TextOperation getOperation() {
        return operation;
    }
    
    public Value getText1() {
        return text1;
    }
    
    public Value getText2() {
        return text2;
    }
    
    public String getResultVariable() {
        return resultVariable;
    }
}
