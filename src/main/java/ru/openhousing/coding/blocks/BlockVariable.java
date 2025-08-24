package ru.openhousing.coding.blocks;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Переменная блока, которую можно держать в руке и настраивать
 * Поддерживает drag-n-drop интерфейс
 */
public class BlockVariable {
    
    private final String name;
    private final String description;
    private final VariableType type;
    private Object value;
    private final Material material;
    private final List<String> lore;
    
    public enum VariableType {
        BOOLEAN("Логическое значение", Material.LEVER, "true/false"),
        INTEGER("Целое число", Material.IRON_INGOT, "0, 1, 2..."),
        DOUBLE("Дробное число", Material.GOLD_INGOT, "0.5, 1.0..."),
        STRING("Текст", Material.PAPER, "любой текст"),
        LIST("Список", Material.BOOK, "элемент1,элемент2..."),
        LOCATION("Координаты", Material.COMPASS, "x,y,z"),
        PLAYER("Игрок", Material.PLAYER_HEAD, "имя игрока"),
        WORLD("Мир", Material.GRASS_BLOCK, "название мира"),
        PERMISSION("Разрешение", Material.SHIELD, "permission.node"),
        GROUP("Группа", Material.NAME_TAG, "название группы");
        
        private final String displayName;
        private final Material icon;
        private final String example;
        
        VariableType(String displayName, Material icon, String example) {
            this.displayName = displayName;
            this.icon = icon;
            this.example = example;
        }
        
        public String getDisplayName() { return displayName; }
        public Material getIcon() { return icon; }
        public String getExample() { return example; }
    }
    
    public BlockVariable(String name, String description, VariableType type, Object defaultValue) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.value = defaultValue;
        this.material = type.getIcon();
        this.lore = new ArrayList<>();
        updateLore();
    }
    
    /**
     * Создает ItemStack для переменной, которую можно держать в руке
     */
    public ItemStack createItemStack() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "§6" + name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Обновляет описание переменной
     */
    private void updateLore() {
        lore.clear();
        lore.add(ChatColor.GRAY + "§7" + description);
        lore.add("");
        lore.add(ChatColor.YELLOW + "§eТип: " + ChatColor.WHITE + type.getDisplayName());
        lore.add(ChatColor.YELLOW + "§eПример: " + ChatColor.WHITE + type.getExample());
        lore.add("");
        lore.add(ChatColor.GREEN + "§aТекущее значение:");
        lore.add(ChatColor.WHITE + "§f" + formatValue());
        lore.add("");
        lore.add(ChatColor.AQUA + "§bНапишите в чат новое значение");
        lore.add(ChatColor.AQUA + "§bдля настройки этой переменной");
    }
    
    /**
     * Форматирует значение для отображения
     */
    private String formatValue() {
        if (value == null) return "§cНе задано";
        
        switch (type) {
            case BOOLEAN:
                return (Boolean) value ? "§aВключено" : "§cВыключено";
            case INTEGER:
            case DOUBLE:
                return "§e" + value.toString();
            case STRING:
                return "§f" + value.toString();
            case LIST:
                if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    return "§f" + String.join(", ", list.stream().map(Object::toString).toArray(String[]::new));
                }
                return "§f" + value.toString();
            case LOCATION:
                return "§f" + value.toString();
            case PLAYER:
                return "§f" + value.toString();
            case WORLD:
                return "§f" + value.toString();
            case PERMISSION:
                return "§f" + value.toString();
            case GROUP:
                return "§f" + value.toString();
            default:
                return "§f" + value.toString();
        }
    }
    
    /**
     * Парсит значение из чата
     */
    public boolean parseValue(String input) {
        try {
            switch (type) {
                case BOOLEAN:
                    if (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("да") || input.equals("1")) {
                        value = true;
                        return true;
                    } else if (input.equalsIgnoreCase("false") || input.equalsIgnoreCase("нет") || input.equals("0")) {
                        value = false;
                        return true;
                    }
                    return false;
                    
                case INTEGER:
                    value = Integer.parseInt(input.trim());
                    return true;
                    
                case DOUBLE:
                    value = Double.parseDouble(input.trim());
                    return true;
                    
                case STRING:
                    value = input.trim();
                    return true;
                    
                case LIST:
                    String[] items = input.split(",");
                    List<String> list = new ArrayList<>();
                    for (String item : items) {
                        list.add(item.trim());
                    }
                    value = list;
                    return true;
                    
                case LOCATION:
                    // Формат: x,y,z или x,y,z,world
                    String[] coords = input.split(",");
                    if (coords.length >= 3) {
                        double x = Double.parseDouble(coords[0].trim());
                        double y = Double.parseDouble(coords[1].trim());
                        double z = Double.parseDouble(coords[2].trim());
                        String world = coords.length > 3 ? coords[3].trim() : "world";
                        value = String.format("%.1f,%.1f,%.1f,%s", x, y, z, world);
                        return true;
                    }
                    return false;
                    
                case PLAYER:
                case WORLD:
                case PERMISSION:
                case GROUP:
                    value = input.trim();
                    return true;
                    
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        } finally {
            updateLore();
        }
    }
    
    /**
     * Проверяет, можно ли парсить значение
     */
    public boolean canParse(String input) {
        try {
            switch (type) {
                case BOOLEAN:
                    return input.equalsIgnoreCase("true") || input.equalsIgnoreCase("false") ||
                           input.equalsIgnoreCase("да") || input.equalsIgnoreCase("нет") ||
                           input.equals("1") || input.equals("0");
                case INTEGER:
                    Integer.parseInt(input.trim());
                    return true;
                case DOUBLE:
                    Double.parseDouble(input.trim());
                    return true;
                case STRING:
                case PLAYER:
                case WORLD:
                case PERMISSION:
                case GROUP:
                    return !input.trim().isEmpty();
                case LIST:
                    return !input.trim().isEmpty();
                case LOCATION:
                    String[] coords = input.split(",");
                    if (coords.length >= 3) {
                        Double.parseDouble(coords[0].trim());
                        Double.parseDouble(coords[1].trim());
                        Double.parseDouble(coords[2].trim());
                        return true;
                    }
                    return false;
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    // Геттеры и сеттеры
    public String getName() { return name; }
    public String getDescription() { return description; }
    public VariableType getType() { return type; }
    public Object getValue() { return value; }
    public Material getMaterial() { return material; }
    public List<String> getLore() { return new ArrayList<>(lore); }
    
    public void setValue(Object value) {
        this.value = value;
        updateLore();
    }
    
    @Override
    public String toString() {
        return "BlockVariable{name='" + name + "', type=" + type + ", value=" + value + "}";
    }
}
