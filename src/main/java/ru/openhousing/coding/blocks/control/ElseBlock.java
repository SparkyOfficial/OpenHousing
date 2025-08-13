package ru.openhousing.coding.blocks.control;

import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок "Иначе" - выполняется если предыдущее условие было ложным
 */
public class ElseBlock extends CodeBlock {
    
    public ElseBlock() {
        super(BlockType.ELSE);
        setParameter("description", "Выполняется если предыдущее условие ложно");
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        // Блок "Иначе" выполняется только если он идет после блока условия
        // и это условие было ложным. Логика выполнения должна быть реализована
        // в родительском блоке или системе выполнения скриптов.
        
        // Для простоты сейчас просто выполняем дочерние блоки
        return executeChildren(context);
    }
    
    @Override
    public boolean validate() {
        // Блок "Иначе" всегда валиден
        return true;
    }
    
    @Override
    public List<String> getDescription() {
        String description = (String) getParameter("description");
        
        return Arrays.asList(
            "§6Иначе",
            "§7" + (description != null ? description : "Альтернативная ветка выполнения"),
            "",
            "§8Дочерних блоков: " + childBlocks.size(),
            "§8Выполняется если предыдущее условие было ложным"
        );
    }
}
