package ru.openhousing.coding.blocks.control;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок "Цель" - определяет цель для последующих действий
 */
public class TargetBlock extends CodeBlock {
    
    public enum TargetType {
        CURRENT_PLAYER("Текущий игрок", "Игрок, который запустил код"),
        ALL_PLAYERS("Все игроки", "Все игроки на сервере"),
        PLAYERS_IN_HOUSE("Игроки в доме", "Все игроки в текущем доме"),
        NEAREST_PLAYER("Ближайший игрок", "Ближайший к текущему игрок"),
        RANDOM_PLAYER("Случайный игрок", "Случайный онлайн игрок"),
        NEAREST_ENTITY("Ближайшее существо", "Ближайшее к игроку существо"),
        ALL_ENTITIES("Все существа", "Все существа вокруг"),
        SPECIFIC_PLAYER("Конкретный игрок", "Игрок по имени"),
        VARIABLE_TARGET("Цель из переменной", "Цель, сохраненная в переменной");
        
        private final String displayName;
        private final String description;
        
        TargetType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private TargetType targetType;
    private String targetValue; // Имя игрока, имя переменной, и т.д.
    private double radius; // Радиус для поиска
    
    public TargetBlock() {
        super(BlockType.TARGET);
        this.targetType = TargetType.CURRENT_PLAYER;
        this.targetValue = "";
        this.radius = 10.0;
        
        setParameter("targetType", targetType.name());
        setParameter("targetValue", targetValue);
        setParameter("radius", radius);
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        try {
            Object target = findTarget(context);
            context.setTarget(target);
            
            // Выполняем дочерние блоки с установленной целью
            return executeChildren(context);
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка при установке цели: " + e.getMessage());
        }
    }
    
    /**
     * Поиск цели в зависимости от типа
     */
    private Object findTarget(ExecutionContext context) {
        Player player = context.getPlayer();
        
        switch (targetType) {
            case CURRENT_PLAYER:
                return player;
                
            case ALL_PLAYERS:
                return org.bukkit.Bukkit.getOnlinePlayers();
                
            case PLAYERS_IN_HOUSE:
                // Получаем всех игроков в текущем доме
                // Это требует интеграции с HousingManager
                return org.bukkit.Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.getWorld().equals(player.getWorld()))
                    .toArray();
                
            case NEAREST_PLAYER:
                return org.bukkit.Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !p.equals(player))
                    .filter(p -> p.getLocation().distance(player.getLocation()) <= radius)
                    .min((p1, p2) -> Double.compare(
                        p1.getLocation().distance(player.getLocation()),
                        p2.getLocation().distance(player.getLocation())
                    ))
                    .orElse(null);
                
            case RANDOM_PLAYER:
                List<? extends Player> onlinePlayers = (List<? extends Player>) org.bukkit.Bukkit.getOnlinePlayers();
                if (onlinePlayers.isEmpty()) return null;
                return onlinePlayers.get((int) (Math.random() * onlinePlayers.size()));
                
            case NEAREST_ENTITY:
                return player.getNearbyEntities(radius, radius, radius).stream()
                    .filter(entity -> !(entity instanceof Player))
                    .min((e1, e2) -> Double.compare(
                        e1.getLocation().distance(player.getLocation()),
                        e2.getLocation().distance(player.getLocation())
                    ))
                    .orElse(null);
                
            case ALL_ENTITIES:
                return player.getNearbyEntities(radius, radius, radius).stream()
                    .filter(entity -> !(entity instanceof Player))
                    .toArray();
                
            case SPECIFIC_PLAYER:
                if (targetValue != null && !targetValue.trim().isEmpty()) {
                    // Обработка переменных в имени
                    String processedName = targetValue;
                    for (String variable : context.getVariables().keySet()) {
                        Object value = context.getVariable(variable);
                        if (value != null) {
                            processedName = processedName.replace("%" + variable + "%", String.valueOf(value));
                        }
                    }
                    return org.bukkit.Bukkit.getPlayer(processedName);
                }
                return null;
                
            case VARIABLE_TARGET:
                if (targetValue != null && !targetValue.trim().isEmpty()) {
                    return context.getVariable(targetValue);
                }
                return null;
                
            default:
                return player;
        }
    }
    
    @Override
    public boolean validate() {
        return targetType != null;
    }
    
    @Override
    public List<String> getDescription() {
        String description = "§6Цель: §f" + targetType.getDisplayName();
        
        if (targetType == TargetType.SPECIFIC_PLAYER || targetType == TargetType.VARIABLE_TARGET) {
            description += " §7(" + (targetValue.isEmpty() ? "не указано" : targetValue) + ")";
        }
        
        if (targetType == TargetType.NEAREST_PLAYER || targetType == TargetType.NEAREST_ENTITY || 
            targetType == TargetType.ALL_ENTITIES) {
            description += " §7(радиус: " + radius + ")";
        }
        
        return Arrays.asList(description, "§7" + targetType.getDescription());
    }
    
    /**
     * Установка типа цели
     */
    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
        setParameter("targetType", targetType.name());
    }
    
    /**
     * Установка значения цели
     */
    public void setTargetValue(String targetValue) {
        this.targetValue = targetValue;
        setParameter("targetValue", targetValue);
    }
    
    /**
     * Установка радиуса поиска
     */
    public void setRadius(double radius) {
        this.radius = radius;
        setParameter("radius", radius);
    }
    
    // Геттеры
    public TargetType getTargetType() {
        return targetType;
    }
    
    public String getTargetValue() {
        return targetValue;
    }
    
    public double getRadius() {
        return radius;
    }
}
