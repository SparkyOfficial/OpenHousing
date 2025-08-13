package ru.openhousing.coding.blocks.math;

import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.values.NumberValue;
import ru.openhousing.coding.values.Value;
import ru.openhousing.coding.values.ValueType;
import ru.openhousing.coding.values.VariableValue;

import java.util.Arrays;
import java.util.List;

/**
 * Блок математических операций
 */
public class MathBlock extends CodeBlock {
    
    public enum MathOperation {
        ADD("Сложение", "+", "Сложить два числа"),
        SUBTRACT("Вычитание", "-", "Вычесть второе число из первого"),
        MULTIPLY("Умножение", "*", "Умножить два числа"),
        DIVIDE("Деление", "/", "Разделить первое число на второе"),
        MODULO("Остаток", "%", "Получить остаток от деления"),
        POWER("Возведение в степень", "^", "Возвести число в степень"),
        SQUARE_ROOT("Квадратный корень", "√", "Извлечь квадратный корень"),
        ABSOLUTE("Модуль", "|x|", "Получить абсолютное значение"),
        ROUND("Округление", "round", "Округлить до ближайшего целого"),
        FLOOR("Округление вниз", "floor", "Округлить вниз"),
        CEIL("Округление вверх", "ceil", "Округлить вверх"),
        MIN("Минимум", "min", "Найти минимальное значение"),
        MAX("Максимум", "max", "Найти максимальное значение"),
        RANDOM("Случайное число", "random", "Получить случайное число в диапазоне");
        
        private final String displayName;
        private final String symbol;
        private final String description;
        
        MathOperation(String displayName, String symbol, String description) {
            this.displayName = displayName;
            this.symbol = symbol;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getSymbol() {
            return symbol;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private MathOperation operation;
    private Value operand1;
    private Value operand2; // Не используется для унарных операций
    private String resultVariable;
    
    public MathBlock() {
        super(BlockType.MATH); // Нужно добавить в BlockType
        this.operation = MathOperation.ADD;
        this.operand1 = new NumberValue("0");
        this.operand2 = new NumberValue("0");
        this.resultVariable = "result";
        
        setParameter("operation", operation.name());
        setParameter("operand1", operand1.getRawValue());
        setParameter("operand2", operand2.getRawValue());
        setParameter("resultVariable", resultVariable);
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        try {
            double result = performOperation(context);
            
            // Сохраняем результат в переменную
            context.setVariable(resultVariable, result);
            
            return ExecutionResult.success(result);
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка в математической операции: " + e.getMessage());
        }
    }
    
    /**
     * Выполнение математической операции
     */
    private double performOperation(ExecutionContext context) throws Exception {
        double value1 = getNumberValue(operand1, context);
        double value2 = isUnaryOperation() ? 0 : getNumberValue(operand2, context);
        
        switch (operation) {
            case ADD:
                return value1 + value2;
            case SUBTRACT:
                return value1 - value2;
            case MULTIPLY:
                return value1 * value2;
            case DIVIDE:
                if (value2 == 0) throw new ArithmeticException("Деление на ноль");
                return value1 / value2;
            case MODULO:
                if (value2 == 0) throw new ArithmeticException("Деление на ноль");
                return value1 % value2;
            case POWER:
                return Math.pow(value1, value2);
            case SQUARE_ROOT:
                if (value1 < 0) throw new ArithmeticException("Квадратный корень из отрицательного числа");
                return Math.sqrt(value1);
            case ABSOLUTE:
                return Math.abs(value1);
            case ROUND:
                return Math.round(value1);
            case FLOOR:
                return Math.floor(value1);
            case CEIL:
                return Math.ceil(value1);
            case MIN:
                return Math.min(value1, value2);
            case MAX:
                return Math.max(value1, value2);
            case RANDOM:
                // value1 = минимум, value2 = максимум
                if (value1 > value2) {
                    double temp = value1;
                    value1 = value2;
                    value2 = temp;
                }
                return value1 + Math.random() * (value2 - value1);
            default:
                throw new UnsupportedOperationException("Неизвестная операция: " + operation);
        }
    }
    
    /**
     * Получение числового значения из Value
     */
    private double getNumberValue(Value value, ExecutionContext context) throws Exception {
        Object result = value.getValue(context.getPlayer(), context);
        
        if (result instanceof Number) {
            return ((Number) result).doubleValue();
        } else if (result instanceof String) {
            try {
                return Double.parseDouble((String) result);
            } catch (NumberFormatException e) {
                throw new Exception("Невозможно преобразовать '" + result + "' в число");
            }
        } else {
            throw new Exception("Значение не является числом: " + result);
        }
    }
    
    /**
     * Проверка, является ли операция унарной
     */
    private boolean isUnaryOperation() {
        return operation == MathOperation.SQUARE_ROOT ||
               operation == MathOperation.ABSOLUTE ||
               operation == MathOperation.ROUND ||
               operation == MathOperation.FLOOR ||
               operation == MathOperation.CEIL;
    }
    
    @Override
    public boolean validate() {
        return operation != null && operand1 != null && 
               (isUnaryOperation() || operand2 != null) &&
               resultVariable != null && !resultVariable.trim().isEmpty();
    }
    
    @Override
    public List<String> getDescription() {
        String description = "§6" + operation.getDisplayName() + " §7(" + operation.getSymbol() + ")";
        
        if (isUnaryOperation()) {
            description += "\n§7Значение: §f" + operand1.getDisplayValue();
        } else {
            description += "\n§7" + operand1.getDisplayValue() + " " + operation.getSymbol() + " " + operand2.getDisplayValue();
        }
        
        description += "\n§7Результат в: §f%" + resultVariable + "%";
        
        return Arrays.asList(description.split("\n"));
    }
    
    // Сеттеры
    public void setOperation(MathOperation operation) {
        this.operation = operation;
        setParameter("operation", operation.name());
    }
    
    public void setOperand1(Value operand1) {
        this.operand1 = operand1;
        setParameter("operand1", operand1.getRawValue());
    }
    
    public void setOperand2(Value operand2) {
        this.operand2 = operand2;
        setParameter("operand2", operand2.getRawValue());
    }
    
    public void setResultVariable(String resultVariable) {
        this.resultVariable = resultVariable;
        setParameter("resultVariable", resultVariable);
    }
    
    // Геттеры
    public MathOperation getOperation() {
        return operation;
    }
    
    public Value getOperand1() {
        return operand1;
    }
    
    public Value getOperand2() {
        return operand2;
    }
    
    public String getResultVariable() {
        return resultVariable;
    }
}
