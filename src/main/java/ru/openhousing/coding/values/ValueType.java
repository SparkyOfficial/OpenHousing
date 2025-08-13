package ru.openhousing.coding.values;

import org.bukkit.Material;

/**
 * Типы значений в системе кодинга
 */
public enum ValueType {
    TEXT("Текст", Material.PAPER, "Текстовое значение"),
    NUMBER("Число", Material.REDSTONE, "Числовое значение"),
    VARIABLE("Переменная", Material.BOOK, "Динамическая переменная с плейсхолдерами"),
    LOCATION("Местоположение", Material.COMPASS, "Координаты в мире"),
    POTION_EFFECT("Эффект зелья", Material.POTION, "Эффект зелья с настройками"),
    PARTICLE("Частица", Material.FIREWORK_STAR, "Частица с параметрами"),
    ITEM("Предмет", Material.DIAMOND, "Предмет с NBT данными"),
    SOUND("Звук", Material.NOTE_BLOCK, "Звук с громкостью и тоном"),
    ENTITY("Существо", Material.EGG, "Ссылка на существо"),
    PLAYER("Игрок", Material.PLAYER_HEAD, "Ссылка на игрока"),
    BLOCK("Блок", Material.STONE, "Тип блока"),
    MATERIAL("Материал", Material.COBBLESTONE, "Тип материала");
    
    private final String displayName;
    private final Material icon;
    private final String description;
    
    ValueType(String displayName, Material icon, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public Material getIcon() {
        return icon;
    }
    
    public String getDescription() {
        return description;
    }
}
