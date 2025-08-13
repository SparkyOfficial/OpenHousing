package ru.openhousing.coding.blocks.inventory;

import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок проверки предметов
 */
public class ItemCheckBlock extends CodeBlock {
    
    public ItemCheckBlock() {
        super(BlockType.ITEM_CHECK);
        setParameter("item", "DIAMOND");
        setParameter("amount", "1");
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        // Заглушка - будет реализовано позже
        return ExecutionResult.success("Item check placeholder");
    }
    
    @Override
    public boolean validate() {
        return true;
    }
    
    @Override
    public List<String> getDescription() {
        return Arrays.asList("§6Проверка предмета", "§7В разработке");
    }
}
