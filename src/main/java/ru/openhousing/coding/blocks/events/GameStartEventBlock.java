package ru.openhousing.coding.blocks.events;

import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import java.util.Arrays;
import java.util.List;

/**
 * Блок события начала игры
 * Срабатывает при запуске игры командой /play
 */
public class GameStartEventBlock extends CodeBlock {
    
    public GameStartEventBlock() {
        super(BlockType.GAME_START);
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        // Выполняем дочерние блоки
        return executeChildren(context);
    }
    
    @Override
    public boolean validate() {
        // Этот блок всегда валиден
        return true;
    }
    
    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "§6Начало игры",
            "§7Срабатывает при запуске игры командой /play",
            "",
            "§8Дочерних блоков: " + childBlocks.size()
        );
    }
    
    @Override
    public boolean matchesEvent(Object event) {
        // Этот блок не реагирует на Bukkit события, 
        // а запускается вручную командой /play
        return false;
    }
}