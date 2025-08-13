package ru.openhousing.coding.blocks.inventory;

import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок действий с инвентарем
 */
public class InventoryActionBlock extends CodeBlock {
    
    public InventoryActionBlock() {
        super(BlockType.INVENTORY_ACTION);
        setParameter("action", "GIVE_ITEM");
        setParameter("item", "DIAMOND");
        setParameter("amount", "1");
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        // Заглушка - будет реализовано позже
        return ExecutionResult.success("Inventory action placeholder");
    }
    
    @Override
    public boolean validate() {
        return true;
    }
    
    @Override
    public List<String> getDescription() {
        return Arrays.asList("§6Действие с инвентарем", "§7В разработке");
    }
}
