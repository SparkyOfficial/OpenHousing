package ru.openhousing.coding.events;

import org.bukkit.entity.Player;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.script.CodeScript;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реестр событий для быстрого поиска и оптимизации
 * Индексирует события по типам для максимальной производительности
 */
public class EventRegistry {
    
    // Индекс событий по классам для O(1) поиска
    private final Map<Class<?>, Set<RegisteredEvent>> eventIndex = new ConcurrentHashMap<>();
    
    // Кэш активных игроков для быстрой проверки
    private final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    
    // Статистика использования событий
    private final Map<String, Integer> eventUsageStats = new ConcurrentHashMap<>();
    
    /**
     * Регистрация события из кода игрока
     */
    public void registerEvent(UUID playerId, CodeBlock eventBlock, Class<?> eventClass) {
        RegisteredEvent registeredEvent = new RegisteredEvent(playerId, eventBlock);
        
        eventIndex.computeIfAbsent(eventClass, k -> ConcurrentHashMap.newKeySet())
                  .add(registeredEvent);
        
        activePlayers.add(playerId);
        
        // Обновляем статистику
        eventUsageStats.merge(eventClass.getSimpleName(), 1, Integer::sum);
    }
    
    /**
     * Отмена регистрации всех событий игрока
     */
    public void unregisterPlayer(UUID playerId) {
        activePlayers.remove(playerId);
        
        // Удаляем все события игрока из индекса
        eventIndex.values().forEach(events -> 
            events.removeIf(event -> event.playerId.equals(playerId))
        );
    }
    
    /**
     * Получение всех зарегистрированных событий для класса
     */
    public Set<RegisteredEvent> getEventsForClass(Class<?> eventClass) {
        return eventIndex.getOrDefault(eventClass, Collections.emptySet());
    }
    
    /**
     * Проверка активности игрока
     */
    public boolean isPlayerActive(UUID playerId) {
        return activePlayers.contains(playerId);
    }
    
    /**
     * Получение статистики использования событий
     */
    public Map<String, Integer> getUsageStats() {
        return new HashMap<>(eventUsageStats);
    }
    
    /**
     * Очистка неактивных событий
     */
    public void cleanup() {
        eventIndex.values().forEach(events -> 
            events.removeIf(event -> !activePlayers.contains(event.playerId))
        );
    }
    
    /**
     * Получение общего количества зарегистрированных событий
     */
    public int getTotalEvents() {
        return eventIndex.values().stream()
                         .mapToInt(Set::size)
                         .sum();
    }
    
    /**
     * Класс для хранения зарегистрированного события
     */
    public static class RegisteredEvent {
        public final UUID playerId;
        public final CodeBlock eventBlock;
        public final long registrationTime;
        
        public RegisteredEvent(UUID playerId, CodeBlock eventBlock) {
            this.playerId = playerId;
            this.eventBlock = eventBlock;
            this.registrationTime = System.currentTimeMillis();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof RegisteredEvent)) return false;
            RegisteredEvent other = (RegisteredEvent) obj;
            return playerId.equals(other.playerId) && eventBlock.equals(other.eventBlock);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(playerId, eventBlock);
        }
    }
}
