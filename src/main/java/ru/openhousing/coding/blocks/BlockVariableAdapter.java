package ru.openhousing.coding.blocks;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import ru.openhousing.coding.variables.VariableType;
import ru.openhousing.coding.variables.DynamicVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Адаптер для интеграции системы "переменных в руке" с существующей системой переменных
 * Позволяет использовать существующие переменные как предметы для drag-n-drop
 */
public class BlockVariableAdapter {
    
    // Кэш для быстрого доступа к переменным
    private static final Map<String, BlockVariable> variableCache = new ConcurrentHashMap<>();
    private static final Map<String, DynamicVariable> dynamicVariableCache = new ConcurrentHashMap<>();
    
    /**
     * Создает BlockVariable из существующей переменной
     */
    public static BlockVariable createFromExistingVariable(String name, Object value, VariableType existingType) {
        // Проверяем кэш
        if (variableCache.containsKey(name)) {
            BlockVariable cached = variableCache.get(name);
            cached.setValue(value);
            return cached;
        }
        
        // Создаем новую переменную с соответствующим типом
        BlockVariable.VariableType newType = mapExistingTypeToNewType(existingType);
        String description = getDescriptionForExistingType(existingType);
        
        BlockVariable variable = new BlockVariable(name, description, newType, value);
        variableCache.put(name, variable);
        
        return variable;
    }
    
    /**
     * Создает BlockVariable из DynamicVariable
     */
    public static BlockVariable createFromDynamicVariable(DynamicVariable dynamicVar) {
        String name = dynamicVar.getVariableName();
        
        if (variableCache.containsKey(name)) {
            return variableCache.get(name);
        }
        
        // Создаем переменную типа DYNAMIC
        BlockVariable variable = new BlockVariable(
            name, 
            "Динамическая переменная с плейсхолдерами", 
            BlockVariable.VariableType.STRING, 
            dynamicVar.getDefaultValue()
        );
        
        variableCache.put(name, variable);
        dynamicVariableCache.put(name, dynamicVar);
        
        return variable;
    }
    
    /**
     * Маппинг существующих типов переменных в новые
     */
    private static BlockVariable.VariableType mapExistingTypeToNewType(VariableType existingType) {
        switch (existingType) {
            case TEXT:
                return BlockVariable.VariableType.STRING;
            case NUMBER:
                return BlockVariable.VariableType.DOUBLE;
            case LOCATION:
                return BlockVariable.VariableType.LOCATION;
            case DYNAMIC:
                return BlockVariable.VariableType.STRING;
            case GAME_VALUE:
                return BlockVariable.VariableType.STRING;
            case POTION_EFFECT:
                return BlockVariable.VariableType.STRING;
            case PARTICLE_EFFECT:
                return BlockVariable.VariableType.STRING;
            default:
                return BlockVariable.VariableType.STRING;
        }
    }
    
    /**
     * Получение описания для существующего типа
     */
    private static String getDescriptionForExistingType(VariableType existingType) {
        switch (existingType) {
            case TEXT:
                return "Текстовая переменная";
            case NUMBER:
                return "Числовая переменная";
            case LOCATION:
                return "Координаты (ПКМ - записать позицию, Shift+ЛКМ - телепорт)";
            case DYNAMIC:
                return "Динамическая переменная с плейсхолдерами";
            case GAME_VALUE:
                return "Системная переменная (только чтение)";
            case POTION_EFFECT:
                return "Эффект зелья (настройка: '4:30' и '3')";
            case PARTICLE_EFFECT:
                return "Эффект частиц";
            default:
                return "Переменная";
        }
    }
    
    /**
     * Создает ItemStack для переменной с улучшенным отображением
     */
    public static ItemStack createEnhancedItemStack(BlockVariable variable) {
        ItemStack item = variable.createItemStack();
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Добавляем информацию о существующей системе
            List<String> enhancedLore = new ArrayList<>(meta.getLore());
            enhancedLore.add("");
            enhancedLore.add(ChatColor.GOLD + "§6Интеграция с существующей системой:");
            enhancedLore.add(ChatColor.GREEN + "§a✓ Совместима с VAR_SET, VAR_ADD и др.");
            enhancedLore.add(ChatColor.GREEN + "§a✓ Поддерживает плейсхолдеры");
            enhancedLore.add(ChatColor.GREEN + "§a✓ Автосохранение в контексте");
            enhancedLore.add("");
            enhancedLore.add(ChatColor.YELLOW + "§eИспользование:");
            enhancedLore.add(ChatColor.WHITE + "§f1. Держите в руке");
            enhancedLore.add(ChatColor.WHITE + "§f2. Напишите в чат новое значение");
            enhancedLore.add(ChatColor.WHITE + "§f3. Используйте в блоках VAR_SET");
            
            meta.setLore(enhancedLore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Синхронизирует значение с существующей системой переменных
     */
    public static void syncWithExistingSystem(BlockVariable variable, String playerName) {
        String name = variable.getName();
        Object value = variable.getValue();
        
        // Если это динамическая переменная, обновляем её
        if (dynamicVariableCache.containsKey(name)) {
            DynamicVariable dynamicVar = dynamicVariableCache.get(name);
            // TODO: Обновить значение в существующей системе
        }
        
        // Обновляем кэш
        variableCache.put(name, variable);
    }
    
    /**
     * Получает все доступные переменные для игрока
     */
    public static List<BlockVariable> getAllAvailableVariables() {
        List<BlockVariable> variables = new ArrayList<>();
        
        // Добавляем кэшированные переменные
        variables.addAll(variableCache.values());
        
        // Добавляем системные переменные
        variables.add(new BlockVariable("player_name", "Имя игрока", BlockVariable.VariableType.STRING, "Unknown"));
        variables.add(new BlockVariable("world_name", "Название мира", BlockVariable.VariableType.WORLD, "world"));
        variables.add(new BlockVariable("online_players", "Игроков онлайн", BlockVariable.VariableType.INTEGER, 0));
        variables.add(new BlockVariable("server_tps", "TPS сервера", BlockVariable.VariableType.DOUBLE, 20.0));
        
        return variables;
    }
    
    /**
     * Создает переменную по умолчанию для часто используемых случаев
     */
    public static BlockVariable createDefaultVariable(String name, String description, BlockVariable.VariableType type) {
        Object defaultValue = getDefaultValueForType(type);
        BlockVariable variable = new BlockVariable(name, description, type, defaultValue);
        variableCache.put(name, variable);
        return variable;
    }
    
    /**
     * Получает значение по умолчанию для типа
     */
    private static Object getDefaultValueForType(BlockVariable.VariableType type) {
        switch (type) {
            case BOOLEAN:
                return false;
            case INTEGER:
                return 0;
            case DOUBLE:
                return 0.0;
            case STRING:
                return "";
            case LIST:
                return new ArrayList<>();
            case LOCATION:
                return "0,0,0,world";
            case PLAYER:
                return "";
            case WORLD:
                return "world";
            case PERMISSION:
                return "";
            case GROUP:
                return "";
            default:
                return null;
        }
    }
    
    /**
     * Проверяет, является ли переменная системной
     */
    public static boolean isSystemVariable(String name) {
        return name.startsWith("system_") || 
               name.equals("player_name") || 
               name.equals("world_name") || 
               name.equals("online_players") || 
               name.equals("server_tps");
    }
    
    /**
     * Получает переменную по имени
     */
    public static BlockVariable getVariableByName(String name) {
        return variableCache.get(name);
    }
    
    /**
     * Очищает кэш переменных
     */
    public static void clearCache() {
        variableCache.clear();
        dynamicVariableCache.clear();
    }
    
    /**
     * Получает количество кэшированных переменных
     */
    public static int getCachedVariableCount() {
        return variableCache.size();
    }
}
