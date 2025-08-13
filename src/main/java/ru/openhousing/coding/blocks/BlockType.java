package ru.openhousing.coding.blocks;

import org.bukkit.Material;

/**
 * Типы блоков для визуального программирования
 */
public enum BlockType {
    
    // События
    PLAYER_EVENT("Событие игрока", Material.PLAYER_HEAD, BlockCategory.EVENT, 
                "Запускается при определенном действии игрока"),
    ENTITY_EVENT("Событие существа", Material.ZOMBIE_HEAD, BlockCategory.EVENT,
                "Запускается при определенном действии существа"),
    WORLD_EVENT("Событие мира", Material.GRASS_BLOCK, BlockCategory.EVENT,
               "Запускается при определенном событии в мире"),
    
    // Условия
    IF_PLAYER("Если игрок", Material.COMPASS, BlockCategory.CONDITION,
             "Проверяет условие для игрока"),
    IF_ENTITY("Если существо", Material.SPIDER_EYE, BlockCategory.CONDITION,
             "Проверяет условие для существа"),
    IF_GAME("Если игра", Material.DIAMOND, BlockCategory.CONDITION,
           "Проверяет условие игры"),
    IF_VARIABLE("Если переменная", Material.BOOK, BlockCategory.CONDITION,
               "Проверяет значение переменной"),
    
    // Действия
    PLAYER_ACTION("Действие игрока", Material.GOLDEN_SWORD, BlockCategory.ACTION,
                 "Выполняет действие с игроком"),
    ENTITY_ACTION("Действие существа", Material.BONE, BlockCategory.ACTION,
                 "Выполняет действие с существом"),
    GAME_ACTION("Действие игры", Material.EMERALD, BlockCategory.ACTION,
               "Выполняет действие в игре"),
    VARIABLE_ACTION("Действие переменной", Material.WRITABLE_BOOK, BlockCategory.ACTION,
                   "Изменяет значение переменной"),
    WORLD_ACTION("Действие мира", Material.GRASS_BLOCK, BlockCategory.ACTION,
                "Выполняет действие в мире"),
    
    // Функции и управление
    FUNCTION("Функция", Material.COMMAND_BLOCK, BlockCategory.FUNCTION,
            "Создает переиспользуемую функцию"),
    CALL_FUNCTION("Вызвать функцию", Material.REPEATING_COMMAND_BLOCK, BlockCategory.FUNCTION,
                 "Вызывает созданную функцию"),
    REPEAT("Повторение", Material.CLOCK, BlockCategory.CONTROL,
          "Повторяет код определенное количество раз"),
    TARGET("Цель", Material.TARGET, BlockCategory.CONTROL,
          "Определяет цель для действий"),
            CODE_CONTROL("Контроль кода", Material.LEVER, BlockCategory.CONTROL,
                    "Управляет выполнением кода"),
        ELSE("Иначе", Material.REDSTONE_TORCH, BlockCategory.CONTROL,
            "Выполняется, если условие ложно"),
        
        // Математика и текст
        MATH("Математика", Material.REDSTONE_BLOCK, BlockCategory.UTILITY,
            "Выполняет математические операции"),
        TEXT_OPERATION("Операции с текстом", Material.WRITABLE_BOOK, BlockCategory.UTILITY,
                      "Операции с текстовыми строками"),
        
        // Инвентарь и предметы
        INVENTORY_ACTION("Действие с инвентарем", Material.CHEST, BlockCategory.ACTION,
                        "Работа с инвентарем игрока"),
        ITEM_CHECK("Проверка предмета", Material.ITEM_FRAME, BlockCategory.CONDITION,
                  "Проверяет предметы в инвентаре");
    
    private final String displayName;
    private final Material material;
    private final BlockCategory category;
    private final String description;
    
    BlockType(String displayName, Material material, BlockCategory category, String description) {
        this.displayName = displayName;
        this.material = material;
        this.category = category;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public BlockCategory getCategory() {
        return category;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Категории блоков
     */
    public enum BlockCategory {
        EVENT("События", Material.REDSTONE, "Блоки событий"),
        CONDITION("Условия", Material.COMPARATOR, "Блоки условий"),
        ACTION("Действия", Material.PISTON, "Блоки действий"),
        FUNCTION("Функции", Material.COMMAND_BLOCK, "Блоки функций"),
        CONTROL("Управление", Material.REDSTONE, "Блоки управления кодом"),
        UTILITY("Утилиты", Material.CRAFTING_TABLE, "Вспомогательные блоки");
        
        private final String displayName;
        private final Material material;
        private final String description;
        
        BlockCategory(String displayName, Material material, String description) {
            this.displayName = displayName;
            this.material = material;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public Material getMaterial() {
            return material;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
