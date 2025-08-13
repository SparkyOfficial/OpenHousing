package ru.openhousing.utils;

import ru.openhousing.coding.blocks.CodeBlock;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Утилиты для блоков кода
 */
public class CodeBlockUtils {
    
    /**
     * Замена переменных в строке
     */
    public static String replaceVariables(String text, CodeBlock.ExecutionContext context) {
        if (text == null) return "";
        
        // Замена переменных вида {variable_name}
        for (String varName : context.getVariables().keySet()) {
            Object value = context.getVariable(varName);
            if (value != null) {
                text = text.replace("{" + varName + "}", value.toString());
            }
        }
        
        // Замена основных плейсхолдеров
        Player player = context.getPlayer();
        if (player != null) {
            text = text.replace("{player}", player.getName());
            text = text.replace("{world}", player.getWorld().getName());
            text = text.replace("{x}", String.valueOf(player.getLocation().getBlockX()));
            text = text.replace("{y}", String.valueOf(player.getLocation().getBlockY()));
            text = text.replace("{z}", String.valueOf(player.getLocation().getBlockZ()));
            text = text.replace("{health}", String.valueOf(player.getHealth()));
            text = text.replace("{food}", String.valueOf(player.getFoodLevel()));
            text = text.replace("{level}", String.valueOf(player.getLevel()));
        }
        
        return text;
    }
    
    /**
     * Парсинг локации из строки
     */
    public static Location parseLocation(String locationString, World world) {
        if (locationString == null || locationString.isEmpty() || world == null) {
            return null;
        }
        
        String[] parts = locationString.split(",");
        if (parts.length >= 3) {
            try {
                double x = Double.parseDouble(parts[0].trim());
                double y = Double.parseDouble(parts[1].trim());
                double z = Double.parseDouble(parts[2].trim());
                return new Location(world, x, y, z);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * Поиск ближайших игроков в доме
     */
    public static List<Player> findPlayersInHouse(Location houseLocation, int radius) {
        World world = houseLocation.getWorld();
        if (world == null) return List.of();
        
        return world.getPlayers().stream()
            .filter(player -> player.getLocation().distance(houseLocation) <= radius)
            .collect(Collectors.toList());
    }
    
    /**
     * Поиск ближайшего игрока
     */
    public static Player findNearestPlayer(Location location, List<Player> players) {
        if (players.isEmpty()) return null;
        
        Player nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Player player : players) {
            double distance = player.getLocation().distance(location);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = player;
            }
        }
        
        return nearest;
    }
    
    /**
     * Поиск ближайших сущностей
     */
    public static List<Entity> findNearestEntities(Location location, int radius, Class<? extends Entity> entityType) {
        World world = location.getWorld();
        if (world == null) return List.of();
        
        return world.getNearbyEntities(location, radius, radius, radius).stream()
            .filter(entity -> entityType.isInstance(entity))
            .collect(Collectors.toList());
    }
}
