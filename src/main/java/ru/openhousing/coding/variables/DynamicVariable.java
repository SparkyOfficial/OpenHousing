package ru.openhousing.coding.variables;

import org.bukkit.entity.Player;
import ru.openhousing.coding.placeholders.PlaceholderProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Динамическая переменная с поддержкой плейсхолдеров
 * Как в википедии: "похожа на коробку" с плейсхолдерами внутри
 */
public class DynamicVariable {
    
    private String variableName;
    private String placeholderKey; // %player%_kills, %player%_deaths и т.д.
    private Object defaultValue;
    private boolean isSaved; // СОХРАНЕНО - через Shift + ПКМ
    private VariableScope scope; // Глобальная или локальная
    
    // Хранилище значений
    private static final Map<String, Object> globalVariables = new HashMap<>();
    private static final Map<String, Map<UUID, Object>> playerVariables = new HashMap<>();
    
    public DynamicVariable(String variableName, String placeholderKey) {
        this.variableName = variableName;
        this.placeholderKey = placeholderKey;
        this.defaultValue = 0;
        this.isSaved = false;
        this.scope = VariableScope.LOCAL;
    }
    
    /**
     * Получение значения переменной для игрока
     */
    public Object getValue(Player player) {
        if (scope == VariableScope.GLOBAL) {
            return globalVariables.getOrDefault(placeholderKey, defaultValue);
        } else {
            return playerVariables
                .computeIfAbsent(placeholderKey, k -> new HashMap<>())
                .getOrDefault(player.getUniqueId(), defaultValue);
        }
    }
    
    /**
     * Установка значения переменной
     */
    public void setValue(Player player, Object value) {
        if (scope == VariableScope.GLOBAL) {
            globalVariables.put(placeholderKey, value);
        } else {
            playerVariables
                .computeIfAbsent(placeholderKey, k -> new HashMap<>())
                .put(player.getUniqueId(), value);
        }
    }
    
    /**
     * Увеличение числового значения (для операции +)
     */
    public void addValue(Player player, Number amount) {
        Object current = getValue(player);
        double currentNum = current instanceof Number ? ((Number) current).doubleValue() : 0;
        setValue(player, currentNum + amount.doubleValue());
    }
    
    /**
     * Обработка плейсхолдера в тексте
     */
    public String processInText(String text, Player player) {
        if (text == null || !text.contains(placeholderKey)) {
            return text;
        }
        
        Object value = getValue(player);
        return text.replace(placeholderKey, value.toString());
    }
    
    /**
     * Создание динамической переменной из плейсхолдера
     */
    public static DynamicVariable fromPlaceholder(String placeholder) {
        // Примеры: %player%_kills, %victim%_deaths
        String variableName = placeholder.replace("%", "").replace("_", " ");
        return new DynamicVariable(variableName, placeholder);
    }
    
    /**
     * Сохранение переменной (Shift + ПКМ)
     */
    public DynamicVariable makeSaved() {
        this.isSaved = true;
        return this;
    }
    
    /**
     * Получение отображаемого имени для GUI
     */
    public String getDisplayName() {
        String name = variableName;
        if (isSaved) {
            name += " §8СОХРАНЕНО";
        }
        return name;
    }
    
    /**
     * Получение всех сохраненных переменных игрока
     */
    public static Map<String, Object> getSavedVariables(Player player) {
        Map<String, Object> saved = new HashMap<>();
        
        // Только сохраненные переменные
        for (Map.Entry<String, Map<UUID, Object>> entry : playerVariables.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue().get(player.getUniqueId());
            if (value != null) {
                saved.put(key, value);
            }
        }
        
        return saved;
    }
    
    /**
     * Команда /plot vars - показать все переменные
     */
    public static Map<String, Object> getAllVariablesForPlayer(Player player) {
        Map<String, Object> allVars = new HashMap<>();
        
        // Глобальные переменные
        allVars.putAll(globalVariables);
        
        // Локальные переменные игрока
        for (Map.Entry<String, Map<UUID, Object>> entry : playerVariables.entrySet()) {
            Object value = entry.getValue().get(player.getUniqueId());
            if (value != null) {
                allVars.put(entry.getKey(), value);
            }
        }
        
        return allVars;
    }
    
    // Геттеры и сеттеры
    public String getVariableName() { return variableName; }
    public void setVariableName(String variableName) { this.variableName = variableName; }
    
    public String getPlaceholderKey() { return placeholderKey; }
    public void setPlaceholderKey(String placeholderKey) { this.placeholderKey = placeholderKey; }
    
    public Object getDefaultValue() { return defaultValue; }
    public void setDefaultValue(Object defaultValue) { this.defaultValue = defaultValue; }
    
    public boolean isSaved() { return isSaved; }
    public void setSaved(boolean saved) { isSaved = saved; }
    
    public VariableScope getScope() { return scope; }
    public void setScope(VariableScope scope) { this.scope = scope; }
    
    /**
     * Область видимости переменной
     */
    public enum VariableScope {
        GLOBAL("Глобальная", "Единственная в своём роде. Количество игроков онлайн, голосов и т.д."),
        LOCAL("Локальная", "Используется с плейсхолдерами. Убийства, смерти, очки игрока");
        
        private final String displayName;
        private final String description;
        
        VariableScope(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
}
