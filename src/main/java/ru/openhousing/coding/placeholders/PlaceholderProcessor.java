package ru.openhousing.coding.placeholders;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Обработчик плейсхолдеров согласно википедии
 */
public class PlaceholderProcessor {
    
    private static final Random random = new Random();
    private static final Map<String, PlaceholderInfo> placeholders = new HashMap<>();
    
    static {
        initializePlaceholders();
    }
    
    /**
     * Инициализация всех плейсхолдеров из википедии
     */
    private static void initializePlaceholders() {
        placeholders.put("%player%", new PlaceholderInfo(
            "%player%", 
            "Выбор игрока от которого исполняется код",
            "Выбирает в событии игрока по умолчанию. Поддерживает только имена игроков."
        ));
        
        placeholders.put("%damager%", new PlaceholderInfo(
            "%damager%", 
            "Выбор игрока, который ударил какую либо сущность",
            "Выбирает в событиях нанесения урона сущность, которая нанёсла урон."
        ));
        
        placeholders.put("%victim%", new PlaceholderInfo(
            "%victim%", 
            "Жертва, кого ударил %damager%",
            "Выбирает в событиях нанесения и получения урона сущность, которая получила урон."
        ));
        
        placeholders.put("%killer%", new PlaceholderInfo(
            "%killer%", 
            "Убийца из события",
            "Выбирает в событиях убийства сущность, которая убила другую."
        ));
        
        placeholders.put("%selected%", new PlaceholderInfo(
            "%selected%", 
            "Выборка, которая выбирает сущностей по отдельности",
            "Выбирает сущности, которые были выбраны блоком 'Выбрать объект'."
        ));
        
        placeholders.put("%selection%", new PlaceholderInfo(
            "%selection%", 
            "Работает как %selected%, но создаёт отдельную переменную",
            "Создает переменную с никами игроков в выборке. Используется только в названии динамической переменной."
        ));
        
        placeholders.put("%shooter%", new PlaceholderInfo(
            "%shooter%", 
            "Тот, кто выстрелил из лука в какую-либо сущность",
            "Выбирает сущность, которая в событиях стрельбы запускает снаряд."
        ));
        
        placeholders.put("%default%", new PlaceholderInfo(
            "%default%", 
            "Заменяется на имя игрока или моба, который стоит там по умолчанию",
            "Выбирает в событии сущность по умолчанию."
        ));
        
        placeholders.put("%entity%", new PlaceholderInfo(
            "%entity%", 
            "Выбирает имя энтити",
            "Выбирает в событии энтити по умолчанию. Поддерживает только имена энтити."
        ));
        
        placeholders.put("%random%", new PlaceholderInfo(
            "%random%", 
            "Выбирает рандома",
            "Выбирает случайного игрока в мире. Поддерживает только имена игроков."
        ));
    }
    
    /**
     * Обработка плейсхолдеров в тексте
     */
    public static String processPlaceholders(String text, Event event, Player defaultPlayer) {
        if (text == null) return text;
        
        String result = text;
        
        // Извлекаем контекст из события
        PlaceholderContext context = extractContextFromEvent(event, defaultPlayer);
        
        // Обрабатываем все плейсхолдеры
        for (String placeholder : placeholders.keySet()) {
            if (result.contains(placeholder)) {
                String replacement = getPlaceholderValue(placeholder, context);
                result = result.replace(placeholder, replacement);
            }
        }
        
        return result;
    }
    
    /**
     * Извлечение контекста из события
     */
    private static PlaceholderContext extractContextFromEvent(Event event, Player defaultPlayer) {
        PlaceholderContext context = new PlaceholderContext();
        context.defaultPlayer = defaultPlayer;
        
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
            context.damager = damageEvent.getDamager();
            context.victim = damageEvent.getEntity();
        }
        
        if (event instanceof EntityDeathEvent) {
            EntityDeathEvent deathEvent = (EntityDeathEvent) event;
            context.victim = deathEvent.getEntity();
            if (deathEvent.getEntity().getKiller() != null) {
                context.killer = deathEvent.getEntity().getKiller();
            }
        }
        
        if (event instanceof ProjectileHitEvent) {
            ProjectileHitEvent projectileEvent = (ProjectileHitEvent) event;
            context.shooter = projectileEvent.getEntity().getShooter();
            if (projectileEvent.getHitEntity() != null) {
                context.victim = projectileEvent.getHitEntity();
            }
        }
        
        return context;
    }
    
    /**
     * Получение значения плейсхолдера
     */
    private static String getPlaceholderValue(String placeholder, PlaceholderContext context) {
        switch (placeholder) {
            case "%player%":
                return context.defaultPlayer != null ? context.defaultPlayer.getName() : "Unknown";
                
            case "%damager%":
                return getEntityName(context.damager);
                
            case "%victim%":
                return getEntityName(context.victim);
                
            case "%killer%":
                return getEntityName(context.killer);
                
            case "%selected%":
                return getEntityName(context.selectedEntity);
                
            case "%selection%":
                return getSelectionString(context.selectedEntities);
                
            case "%shooter%":
                return getEntityName((Entity) context.shooter);
                
            case "%default%":
                return getEntityName(context.defaultEntity);
                
            case "%entity%":
                return getEntityName(context.entity);
                
            case "%random%":
                return getRandomPlayerName();
                
            default:
                return placeholder; // Возвращаем как есть, если не найден
        }
    }
    
    /**
     * Получение имени сущности
     */
    private static String getEntityName(Entity entity) {
        if (entity == null) return "Unknown";
        
        if (entity instanceof Player) {
            return ((Player) entity).getName();
        }
        
        return entity.getType().name().toLowerCase();
    }
    
    /**
     * Получение имени сущности (Object для shooter)
     */
    private static String getEntityName(Object entity) {
        if (entity instanceof Entity) {
            return getEntityName((Entity) entity);
        }
        return "Unknown";
    }
    
    /**
     * Получение строки выборки
     */
    private static String getSelectionString(List<Entity> entities) {
        if (entities == null || entities.isEmpty()) return "";
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < entities.size(); i++) {
            if (i > 0) result.append(",");
            result.append(getEntityName(entities.get(i)));
        }
        return result.toString();
    }
    
    /**
     * Получение случайного игрока
     */
    private static String getRandomPlayerName() {
        Player[] players = org.bukkit.Bukkit.getOnlinePlayers().toArray(new Player[0]);
        if (players.length == 0) return "Unknown";
        
        return players[random.nextInt(players.length)].getName();
    }
    
    /**
     * Получение всех доступных плейсхолдеров
     */
    public static Map<String, PlaceholderInfo> getAllPlaceholders() {
        return new HashMap<>(placeholders);
    }
    
    /**
     * Проверка, является ли строка плейсхолдером
     */
    public static boolean isPlaceholder(String text) {
        return text != null && text.startsWith("%") && text.endsWith("%") && placeholders.containsKey(text);
    }
    
    /**
     * Контекст для обработки плейсхолдеров
     */
    private static class PlaceholderContext {
        Player defaultPlayer;
        Entity damager;
        Entity victim;
        Entity killer;
        Entity selectedEntity;
        List<Entity> selectedEntities;
        Object shooter;
        Entity defaultEntity;
        Entity entity;
    }
    
    /**
     * Информация о плейсхолдере
     */
    public static class PlaceholderInfo {
        private final String placeholder;
        private final String name;
        private final String description;
        
        public PlaceholderInfo(String placeholder, String name, String description) {
            this.placeholder = placeholder;
            this.name = name;
            this.description = description;
        }
        
        public String getPlaceholder() { return placeholder; }
        public String getName() { return name; }
        public String getDescription() { return description; }
    }
}
