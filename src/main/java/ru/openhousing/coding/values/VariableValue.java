package ru.openhousing.coding.values;

import org.bukkit.entity.Player;
import ru.openhousing.coding.blocks.CodeBlock;

/**
 * Значение переменной
 */
public class VariableValue extends Value {
    
    public VariableValue(String rawValue) {
        super(ValueType.VARIABLE, rawValue != null ? rawValue : "");
    }
    
    @Override
    public Object getValue(Player player, CodeBlock.ExecutionContext context) {
        if (context == null) {
            return null;
        }
        
        String variableName = rawValue;
        
        // Удаляем префикс % если есть
        if (variableName.startsWith("%") && variableName.endsWith("%") && variableName.length() > 2) {
            variableName = variableName.substring(1, variableName.length() - 1);
        }
        
        return context.getVariable(variableName);
    }
    
    @Override
    public String getDisplayValue() {
        if (!rawValue.startsWith("%") || !rawValue.endsWith("%")) {
            return "%" + rawValue + "%";
        }
        return rawValue;
    }
    
    @Override
    public boolean isValid() {
        return rawValue != null && !rawValue.trim().isEmpty();
    }
    
    /**
     * Получение имени переменной
     */
    public String getVariableName() {
        String name = rawValue;
        if (name.startsWith("%") && name.endsWith("%") && name.length() > 2) {
            name = name.substring(1, name.length() - 1);
        }
        return name;
    }
}
