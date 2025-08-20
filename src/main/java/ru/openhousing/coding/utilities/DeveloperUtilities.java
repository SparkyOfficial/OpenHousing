package ru.openhousing.coding.utilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.script.CodeLine;
import ru.openhousing.coding.script.CodeScript;
import ru.openhousing.utils.ItemBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * Утилиты разработки: Стрелка НЕ и Перемещатель кода 3000
 * Согласно википедии
 */
public class DeveloperUtilities {
    
    /**
     * Создание Стрелки НЕ
     * Инвертирует результат условия
     */
    public static ItemStack createNotArrow() {
        return new ItemBuilder(Material.SPECTRAL_ARROW)
            .name("§cСтрелка НЕ")
            .lore(Arrays.asList(
                "§7Утилита разработки",
                "§7Инвертирует результат условия",
                "",
                "§7Поместите перед условием чтобы",
                "§7получить противоположный результат",
                "",
                "§eПример: НЕ (игрок онлайн) = игрок оффлайн"
            ))
            .build();
    }
    
    /**
     * Создание Перемещателя кода 3000
     * Перемещает блоки кода между строками
     */
    public static ItemStack createCodeMover3000() {
        return new ItemBuilder(Material.PISTON)
            .name("§6Перемещатель кода 3000")
            .lore(Arrays.asList(
                "§7Утилита разработки",
                "§7Перемещает блоки кода между строками",
                "",
                "§7ЛКМ - выбрать блок для перемещения",
                "§7ПКМ - поместить блок в новое место",
                "§7Shift+ЛКМ - копировать блок",
                "",
                "§eПомогает организовать код"
            ))
            .build();
    }
    
    /**
     * Применение Стрелки НЕ к условию
     */
    public static boolean applyNotArrow(CodeBlock conditionBlock, boolean originalResult) {
        // Проверяем, есть ли метка НЕ у блока
        Object notFlag = conditionBlock.getParameter("not_inverted");
        if (notFlag != null && (Boolean) notFlag) {
            return !originalResult; // Инвертируем результат
        }
        return originalResult;
    }
    
    /**
     * Перемещение блока между строками (Перемещатель кода 3000)
     */
    public static boolean moveCodeBlock(CodeScript script, int fromLine, int blockIndex, int toLine, Player player) {
        try {
            List<CodeLine> lines = script.getLines();
            
            if (fromLine < 0 || fromLine >= lines.size() || toLine < 0 || toLine >= lines.size()) {
                player.sendMessage("§cНеверные номера строк!");
                return false;
            }
            
            CodeLine sourceLine = lines.get(fromLine);
            CodeLine targetLine = lines.get(toLine);
            
            if (blockIndex < 0 || blockIndex >= sourceLine.getBlocks().size()) {
                player.sendMessage("§cБлок не найден в исходной строке!");
                return false;
            }
            
            // Получаем блок для перемещения
            CodeBlock blockToMove = sourceLine.getBlocks().get(blockIndex);
            
            // Удаляем из исходной строки
            sourceLine.removeBlock(blockToMove);
            
            // Добавляем в целевую строку
            targetLine.addBlock(blockToMove);
            
            player.sendMessage("§aБлок перемещен из строки " + (fromLine + 1) + " в строку " + (toLine + 1));
            player.sendMessage("§7Блок: §f" + blockToMove.getType().getDisplayName());
            
            return true;
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка при перемещении блока: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Копирование блока (Shift+ЛКМ с Перемещателем кода 3000)
     */
    public static boolean copyCodeBlock(CodeScript script, int fromLine, int blockIndex, int toLine, Player player) {
        try {
            List<CodeLine> lines = script.getLines();
            
            if (fromLine < 0 || fromLine >= lines.size() || toLine < 0 || toLine >= lines.size()) {
                player.sendMessage("§cНеверные номера строк!");
                return false;
            }
            
            CodeLine sourceLine = lines.get(fromLine);
            CodeLine targetLine = lines.get(toLine);
            
            if (blockIndex < 0 || blockIndex >= sourceLine.getBlocks().size()) {
                player.sendMessage("§cБлок не найден в исходной строке!");
                return false;
            }
            
            // Получаем блок для копирования
            CodeBlock blockToCopy = sourceLine.getBlocks().get(blockIndex);
            
            // Клонируем блок
            CodeBlock clonedBlock = blockToCopy.clone();
            
            // Добавляем в целевую строку
            targetLine.addBlock(clonedBlock);
            
            player.sendMessage("§aБлок скопирован из строки " + (fromLine + 1) + " в строку " + (toLine + 1));
            player.sendMessage("§7Блок: §f" + blockToCopy.getType().getDisplayName());
            
            return true;
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка при копировании блока: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Получение статистики использования утилит
     */
    public static String getUtilitiesStats(Player player) {
        // Можно добавить счетчики использования утилит
        return "§6=== Статистика утилит разработки ===\n" +
               "§7Стрелка НЕ: §fИспользована для инвертирования условий\n" +
               "§7Перемещатель кода 3000: §fПомогает организовать код\n" +
               "§7Доступно в категории 'Утилиты'";
    }
    
    /**
     * Проверка, является ли предмет утилитой разработки
     */
    public static boolean isDeveloperUtility(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        String name = item.getItemMeta().getDisplayName();
        return name != null && (name.contains("Стрелка НЕ") || name.contains("Перемещатель кода 3000"));
    }
    
    /**
     * Получение типа утилиты
     */
    public static UtilityType getUtilityType(ItemStack item) {
        if (!isDeveloperUtility(item)) return null;
        
        String name = item.getItemMeta().getDisplayName();
        if (name.contains("Стрелка НЕ")) {
            return UtilityType.NOT_ARROW;
        } else if (name.contains("Перемещатель кода 3000")) {
            return UtilityType.CODE_MOVER_3000;
        }
        
        return null;
    }
    
    /**
     * Типы утилит разработки
     */
    public enum UtilityType {
        NOT_ARROW("Стрелка НЕ", "Инвертирует результат условия"),
        CODE_MOVER_3000("Перемещатель кода 3000", "Перемещает и копирует блоки кода");
        
        private final String displayName;
        private final String description;
        
        UtilityType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
}
