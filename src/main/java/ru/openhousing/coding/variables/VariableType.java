package ru.openhousing.coding.variables;

import org.bukkit.Material;

/**
 * Типы переменных согласно википедии
 */
public enum VariableType {
    TEXT("Текст", Material.PAPER, "Переменная, позволяющая хранить текст"),
    NUMBER("Число", Material.REDSTONE, "Может хранить только числа (включая Pi и e)"),
    LOCATION("Местоположение", Material.COMPASS, "Хранит координаты. ПКМ - записать позицию, Shift+ЛКМ - телепорт"),
    DYNAMIC("Динамическая переменная", Material.CHEST, "Хранит данные с плейсхолдерами. Shift+ПКМ - сохранить навсегда"),
    GAME_VALUE("Игровое значение", Material.KNOWLEDGE_BOOK, "Системная переменная, которую нельзя изменить"),
    POTION_EFFECT("Эффекты зелья", Material.POTION, "Выбор зелья. Настройка через чат: '4:30' и '3'"),
    PARTICLE_EFFECT("Эффект частиц", Material.FIREWORK_ROCKET, "Выбор эффекта частиц для использования");
    
    private final String displayName;
    private final Material material;
    private final String description;
    
    VariableType(String displayName, Material material, String description) {
        this.displayName = displayName;
        this.material = material;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public Material getMaterial() { return material; }
    public String getDescription() { return description; }
}
