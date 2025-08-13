package ru.openhousing.coding.values;

import org.bukkit.entity.Player;
import ru.openhousing.coding.blocks.CodeBlock;

/**
 * Числовое значение
 */
public class NumberValue extends Value {
    
    private Double numericValue;
    
    public NumberValue(String rawValue) {
        super(ValueType.NUMBER, rawValue != null ? rawValue : "0");
        parseValue();
    }
    
    public NumberValue(double value) {
        super(ValueType.NUMBER, String.valueOf(value));
        this.numericValue = value;
    }
    
    private void parseValue() {
        try {
            this.numericValue = Double.parseDouble(rawValue);
        } catch (NumberFormatException e) {
            this.numericValue = 0.0;
        }
    }
    
    @Override
    public Object getValue(Player player, CodeBlock.ExecutionContext context) {
        // Попытка парсинга значения с переменными
        String processedValue = rawValue;
        
        // Замена переменных из контекста
        if (context != null) {
            for (String variable : context.getVariables().keySet()) {
                Object value = context.getVariable(variable);
                if (value != null) {
                    processedValue = processedValue.replace("%" + variable + "%", String.valueOf(value));
                }
            }
        }
        
        // Замена встроенных плейсхолдеров
        if (player != null) {
            processedValue = processedValue.replace("%player_x%", String.valueOf(player.getLocation().getBlockX()));
            processedValue = processedValue.replace("%player_y%", String.valueOf(player.getLocation().getBlockY()));
            processedValue = processedValue.replace("%player_z%", String.valueOf(player.getLocation().getBlockZ()));
            processedValue = processedValue.replace("%player_health%", String.valueOf(player.getHealth()));
            processedValue = processedValue.replace("%player_food%", String.valueOf(player.getFoodLevel()));
        }
        
        try {
            return Double.parseDouble(processedValue);
        } catch (NumberFormatException e) {
            return numericValue != null ? numericValue : 0.0;
        }
    }
    
    @Override
    public String getDisplayValue() {
        if (numericValue != null && numericValue == numericValue.longValue()) {
            return String.valueOf(numericValue.longValue());
        }
        return rawValue;
    }
    
    @Override
    public boolean isValid() {
        try {
            Double.parseDouble(rawValue);
            return true;
        } catch (NumberFormatException e) {
            // Проверяем, содержит ли переменные
            return rawValue.contains("%") || rawValue.matches(".*[+\\-*/].*");
        }
    }
    
    /**
     * Получение числового значения
     */
    public double getNumericValue() {
        return numericValue != null ? numericValue : 0.0;
    }
}
