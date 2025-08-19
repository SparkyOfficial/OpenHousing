package ru.openhousing.coding.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Система фильтрации событий для повышения производительности
 * Позволяет задавать условия выполнения событий
 */
public class EventFilter {
    
    private final Map<String, Object> conditions = new HashMap<>();
    
    public EventFilter() {}
    
    /**
     * Добавление условия фильтрации
     */
    public EventFilter addCondition(String key, Object value) {
        conditions.put(key, value);
        return this;
    }
    
    /**
     * Проверка соответствия события фильтру
     */
    public boolean matches(Event event) {
        if (conditions.isEmpty()) {
            return true; // Нет условий - пропускаем все
        }
        
        for (Map.Entry<String, Object> condition : conditions.entrySet()) {
            if (!checkCondition(event, condition.getKey(), condition.getValue())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Проверка конкретного условия
     */
    private boolean checkCondition(Event event, String key, Object expectedValue) {
        Object actualValue = getEventValue(event, key);
        
        if (actualValue == null && expectedValue == null) {
            return true;
        }
        
        if (actualValue == null || expectedValue == null) {
            return false;
        }
        
        // Поддержка регулярных выражений для строк
        if (expectedValue instanceof String && actualValue instanceof String) {
            String expected = (String) expectedValue;
            String actual = (String) actualValue;
            
            if (expected.startsWith("regex:")) {
                return actual.matches(expected.substring(6));
            }
            
            if (expected.startsWith("contains:")) {
                return actual.toLowerCase().contains(expected.substring(9).toLowerCase());
            }
            
            return expected.equalsIgnoreCase(actual);
        }
        
        // Числовые сравнения
        if (expectedValue instanceof Number && actualValue instanceof Number) {
            double expected = ((Number) expectedValue).doubleValue();
            double actual = ((Number) actualValue).doubleValue();
            return Math.abs(expected - actual) < 0.001;
        }
        
        return expectedValue.equals(actualValue);
    }
    
    /**
     * Извлечение значения из события по ключу
     */
    private Object getEventValue(Event event, String key) {
        return switch (key.toLowerCase()) {
            case "player_name" -> {
                if (event instanceof PlayerEvent) {
                    yield ((PlayerEvent) event).getPlayer().getName();
                }
                yield null;
            }
            case "player_world" -> {
                if (event instanceof PlayerEvent) {
                    yield ((PlayerEvent) event).getPlayer().getWorld().getName();
                }
                yield null;
            }
            case "entity_type" -> {
                if (event instanceof EntityEvent) {
                    yield ((EntityEvent) event).getEntityType().name();
                }
                yield null;
            }
            case "entity_world" -> {
                if (event instanceof EntityEvent) {
                    yield ((EntityEvent) event).getEntity().getWorld().getName();
                }
                yield null;
            }
            default -> null;
        };
    }
    
    /**
     * Создание фильтра для конкретного игрока
     */
    public static EventFilter forPlayer(String playerName) {
        return new EventFilter().addCondition("player_name", playerName);
    }
    
    /**
     * Создание фильтра для конкретного мира
     */
    public static EventFilter forWorld(String worldName) {
        return new EventFilter().addCondition("player_world", worldName);
    }
    
    /**
     * Создание фильтра для типа существа
     */
    public static EventFilter forEntityType(String entityType) {
        return new EventFilter().addCondition("entity_type", entityType);
    }
    
    /**
     * Комбинированный фильтр
     */
    public static EventFilter combine(EventFilter... filters) {
        EventFilter combined = new EventFilter();
        for (EventFilter filter : filters) {
            combined.conditions.putAll(filter.conditions);
        }
        return combined;
    }
}
