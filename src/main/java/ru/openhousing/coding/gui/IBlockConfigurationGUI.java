package ru.openhousing.coding.gui;

import org.bukkit.event.Listener;
import ru.openhousing.coding.blocks.CodeBlock;

/**
 * Интерфейс для модульных конфигурационных GUI блоков кода
 */
public interface IBlockConfigurationGUI extends Listener {
    
    /**
     * Открыть GUI для игрока
     */
    void open();
    
    /**
     * Настроить инвентарь с элементами конфигурации
     */
    void setupInventory();
    
    /**
     * Получить блок, который конфигурируется
     */
    CodeBlock getBlock();
    
    /**
     * Закрыть GUI и выполнить callback сохранения
     */
    void close();
    
    /**
     * Проверить, принадлежит ли данный инвентарь этому GUI
     */
    boolean isValidInventory(org.bukkit.inventory.Inventory inventory);
}
