package ru.openhousing.coding.blocks.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок проверки предметов
 */
public class ItemCheckBlock extends CodeBlock {
    
    public enum CheckType {
        HAS_ITEM("Имеет предмет", "Проверяет наличие предмета"),
        HAS_AMOUNT("Имеет количество", "Проверяет точное количество"),
        HAS_AT_LEAST("Имеет минимум", "Проверяет минимальное количество"),
        HAS_SPACE("Имеет место", "Проверяет наличие свободного места"),
        SLOT_CONTAINS("Слот содержит", "Проверяет содержимое определенного слота"),
        INVENTORY_FULL("Инвентарь полон", "Проверяет заполненность инвентаря"),
        INVENTORY_EMPTY("Инвентарь пуст", "Проверяет пустоту инвентаря");
        
        private final String displayName;
        private final String description;
        
        CheckType(String displayName, String description) {
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
    
    public ItemCheckBlock() {
        super(BlockType.ITEM_CHECK);
        setParameter("checkType", CheckType.HAS_ITEM.name());
        setParameter("item", "DIAMOND");
        setParameter("amount", "1");
        setParameter("slot", "0");
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден");
        }
        
        try {
            CheckType checkType = CheckType.valueOf((String) getParameter("checkType"));
            String itemStr = replaceVariables((String) getParameter("item"), context);
            String amountStr = replaceVariables((String) getParameter("amount"), context);
            String slotStr = replaceVariables((String) getParameter("slot"), context);
            
            boolean result = false;
            
            switch (checkType) {
                case HAS_ITEM:
                    Material material = Material.valueOf(itemStr.toUpperCase());
                    result = player.getInventory().contains(material);
                    break;
                    
                case HAS_AMOUNT:
                    Material amountMaterial = Material.valueOf(itemStr.toUpperCase());
                    int requiredAmount = Integer.parseInt(amountStr);
                    int actualAmount = 0;
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null && item.getType() == amountMaterial) {
                            actualAmount += item.getAmount();
                        }
                    }
                    result = actualAmount == requiredAmount;
                    break;
                    
                case HAS_AT_LEAST:
                    Material minMaterial = Material.valueOf(itemStr.toUpperCase());
                    int minAmount = Integer.parseInt(amountStr);
                    int totalAmount = 0;
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null && item.getType() == minMaterial) {
                            totalAmount += item.getAmount();
                        }
                    }
                    result = totalAmount >= minAmount;
                    break;
                    
                case HAS_SPACE:
                    int freeSlots = 0;
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item == null) freeSlots++;
                    }
                    result = freeSlots > 0;
                    break;
                    
                case SLOT_CONTAINS:
                    int slot = Integer.parseInt(slotStr);
                    if (slot >= 0 && slot < 36) {
                        ItemStack slotItem = player.getInventory().getItem(slot);
                        if (slotItem != null) {
                            Material slotMaterial = Material.valueOf(itemStr.toUpperCase());
                            result = slotItem.getType() == slotMaterial;
                        }
                    }
                    break;
                    
                case INVENTORY_FULL:
                    result = player.getInventory().firstEmpty() == -1;
                    break;
                    
                case INVENTORY_EMPTY:
                    result = true;
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null) {
                            result = false;
                            break;
                        }
                    }
                    break;
            }
            
            // Сохраняем результат проверки в переменную
            context.setVariable("check_result", result);
            
            // Если условие выполнено, выполняем дочерние блоки
            if (result) {
                return executeChildren(context);
            }
            
            return ExecutionResult.success();
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка проверки предмета: " + e.getMessage());
        }
    }
    
    @Override
    public boolean validate() {
        return getParameter("checkType") != null && getParameter("item") != null;
    }
    
    @Override
    public List<String> getDescription() {
        String checkType = (String) getParameter("checkType");
        String item = (String) getParameter("item");
        String amount = (String) getParameter("amount");
        
        return Arrays.asList(
            "§6Проверка предмета",
            "§7Тип: §f" + checkType,
            "§7Предмет: §f" + item,
            "§7Количество: §f" + amount,
            "",
            "§8Дочерних блоков: " + childBlocks.size()
        );
    }
}
