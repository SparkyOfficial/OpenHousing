package ru.openhousing.coding.blocks.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок действий с инвентарем
 */
public class InventoryActionBlock extends CodeBlock {
    
    public enum InventoryAction {
        GIVE_ITEM("Дать предмет", "Дает предмет игроку"),
        TAKE_ITEM("Забрать предмет", "Забирает предмет у игрока"),
        CLEAR_INVENTORY("Очистить инвентарь", "Очищает весь инвентарь"),
        CLEAR_HOTBAR("Очистить панель", "Очищает панель быстрого доступа"),
        SET_ITEM("Установить предмет", "Устанавливает предмет в определенный слот"),
        CHECK_SPACE("Проверить место", "Проверяет наличие свободного места"),
        COUNT_ITEMS("Подсчитать предметы", "Подсчитывает количество определенных предметов");
        
        private final String displayName;
        private final String description;
        
        InventoryAction(String displayName, String description) {
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
    
    public InventoryActionBlock() {
        super(BlockType.INVENTORY_ACTION);
        setParameter("action", InventoryAction.GIVE_ITEM.name());
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
            InventoryAction action = InventoryAction.valueOf((String) getParameter("action"));
            String itemStr = replaceVariables((String) getParameter("item"), context);
            String amountStr = replaceVariables((String) getParameter("amount"), context);
            String slotStr = replaceVariables((String) getParameter("slot"), context);
            
            switch (action) {
                case GIVE_ITEM:
                    Material material = Material.valueOf(itemStr.toUpperCase());
                    int amount = Integer.parseInt(amountStr);
                    player.getInventory().addItem(new ItemStack(material, amount));
                    break;
                    
                case TAKE_ITEM:
                    Material takeMaterial = Material.valueOf(itemStr.toUpperCase());
                    int takeAmount = Integer.parseInt(amountStr);
                    player.getInventory().removeItem(new ItemStack(takeMaterial, takeAmount));
                    break;
                    
                case CLEAR_INVENTORY:
                    player.getInventory().clear();
                    break;
                    
                case CLEAR_HOTBAR:
                    for (int i = 0; i < 9; i++) {
                        player.getInventory().setItem(i, null);
                    }
                    break;
                    
                case SET_ITEM:
                    Material setMaterial = Material.valueOf(itemStr.toUpperCase());
                    int setAmount = Integer.parseInt(amountStr);
                    int slot = Integer.parseInt(slotStr);
                    if (slot >= 0 && slot < 36) {
                        player.getInventory().setItem(slot, new ItemStack(setMaterial, setAmount));
                    }
                    break;
                    
                case CHECK_SPACE:
                    int freeSlots = 0;
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item == null) freeSlots++;
                    }
                    context.setVariable("free_slots", freeSlots);
                    break;
                    
                case COUNT_ITEMS:
                    Material countMaterial = Material.valueOf(itemStr.toUpperCase());
                    int totalCount = 0;
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null && item.getType() == countMaterial) {
                            totalCount += item.getAmount();
                        }
                    }
                    context.setVariable("item_count", totalCount);
                    break;
            }
            
            return ExecutionResult.success();
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения действия с инвентарем: " + e.getMessage());
        }
    }
    
    @Override
    public boolean validate() {
        return getParameter("action") != null && getParameter("item") != null;
    }
    
    @Override
    public List<String> getDescription() {
        String action = (String) getParameter("action");
        String item = (String) getParameter("item");
        String amount = (String) getParameter("amount");
        
        return Arrays.asList(
            "§6Действие с инвентарем",
            "§7Действие: §f" + action,
            "§7Предмет: §f" + item,
            "§7Количество: §f" + amount
        );
    }
}
