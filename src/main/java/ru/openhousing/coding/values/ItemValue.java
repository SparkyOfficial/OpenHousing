package ru.openhousing.coding.values;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.utils.ItemBuilder;

/**
 * Значение предмета
 */
public class ItemValue extends Value {
    
    private Material material;
    private int amount;
    private String displayName;
    
    public ItemValue(String rawValue) {
        super(ValueType.ITEM, rawValue != null ? rawValue : "DIAMOND,1");
        parseItem();
    }
    
    public ItemValue(ItemStack item) {
        super(ValueType.ITEM, itemToString(item));
        if (item != null) {
            this.material = item.getType();
            this.amount = item.getAmount();
            this.displayName = item.hasItemMeta() && item.getItemMeta().hasDisplayName() 
                ? item.getItemMeta().getDisplayName() : null;
        }
    }
    
    private void parseItem() {
        try {
            String[] parts = rawValue.split(",");
            if (parts.length >= 1) {
                material = Material.valueOf(parts[0].trim().toUpperCase());
                amount = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 1;
                displayName = parts.length > 2 ? parts[2].trim() : null;
            } else {
                material = Material.DIAMOND;
                amount = 1;
                displayName = null;
            }
        } catch (Exception e) {
            material = Material.DIAMOND;
            amount = 1;
            displayName = null;
        }
    }
    
    @Override
    public Object getValue(Player player, CodeBlock.ExecutionContext context) {
        if (material == null) {
            return null;
        }
        
        int processedAmount = amount;
        String processedDisplayName = displayName;
        
        // Обработка переменных
        if (context != null) {
            String amountStr = String.valueOf(amount);
            
            for (String variable : context.getVariables().keySet()) {
                Object value = context.getVariable(variable);
                if (value != null) {
                    String strValue = String.valueOf(value);
                    amountStr = amountStr.replace("%" + variable + "%", strValue);
                    if (processedDisplayName != null) {
                        processedDisplayName = processedDisplayName.replace("%" + variable + "%", strValue);
                    }
                }
            }
            
            try {
                processedAmount = Integer.parseInt(amountStr);
            } catch (NumberFormatException ignored) {}
        }
        
        ItemBuilder builder = new ItemBuilder(material, processedAmount);
        
        if (processedDisplayName != null && !processedDisplayName.trim().isEmpty()) {
            builder.name(processedDisplayName.replace("&", "§"));
        }
        
        return builder.build();
    }
    
    @Override
    public String getDisplayValue() {
        if (material != null) {
            String name = material.name();
            if (amount > 1) {
                name += " x" + amount;
            }
            if (displayName != null && !displayName.trim().isEmpty()) {
                name += " (\"" + displayName + "\")";
            }
            return name;
        }
        return rawValue;
    }
    
    @Override
    public boolean isValid() {
        try {
            String[] parts = rawValue.split(",");
            if (parts.length >= 1) {
                Material.valueOf(parts[0].trim().toUpperCase());
                if (parts.length > 1) {
                    Integer.parseInt(parts[1].trim());
                }
                return true;
            }
        } catch (Exception e) {
            return rawValue.contains("%");
        }
        return false;
    }
    
    private static String itemToString(ItemStack item) {
        if (item == null) return "DIAMOND,1";
        
        StringBuilder sb = new StringBuilder();
        sb.append(item.getType().name()).append(",").append(item.getAmount());
        
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            sb.append(",").append(item.getItemMeta().getDisplayName());
        }
        
        return sb.toString();
    }
}
