package ru.openhousing.coding.values;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.openhousing.coding.blocks.CodeBlock;

/**
 * Значение местоположения
 */
public class LocationValue extends Value {
    
    private String worldName;
    private double x, y, z;
    private float yaw, pitch;
    
    public LocationValue(String rawValue) {
        super(ValueType.LOCATION, rawValue != null ? rawValue : "world,0,64,0,0,0");
        parseLocation();
    }
    
    public LocationValue(Location location) {
        super(ValueType.LOCATION, locationToString(location));
        if (location != null) {
            this.worldName = location.getWorld().getName();
            this.x = location.getX();
            this.y = location.getY();
            this.z = location.getZ();
            this.yaw = location.getYaw();
            this.pitch = location.getPitch();
        }
    }
    
    private void parseLocation() {
        try {
            String[] parts = rawValue.split(",");
            if (parts.length >= 4) {
                worldName = parts[0].trim();
                x = Double.parseDouble(parts[1].trim());
                y = Double.parseDouble(parts[2].trim());
                z = Double.parseDouble(parts[3].trim());
                yaw = parts.length > 4 ? Float.parseFloat(parts[4].trim()) : 0.0f;
                pitch = parts.length > 5 ? Float.parseFloat(parts[5].trim()) : 0.0f;
            } else {
                // Значения по умолчанию
                worldName = "world";
                x = y = z = 0;
                yaw = pitch = 0;
            }
        } catch (Exception e) {
            // Значения по умолчанию при ошибке парсинга
            worldName = "world";
            x = y = z = 0;
            yaw = pitch = 0;
        }
    }
    
    @Override
    public Object getValue(Player player, CodeBlock.ExecutionContext context) {
        // Обработка специальных значений
        String processedWorldName = worldName;
        double processedX = x, processedY = y, processedZ = z;
        
        // Замена переменных
        if (context != null) {
            String rawX = String.valueOf(x);
            String rawY = String.valueOf(y);
            String rawZ = String.valueOf(z);
            
            for (String variable : context.getVariables().keySet()) {
                Object value = context.getVariable(variable);
                if (value != null) {
                    String strValue = String.valueOf(value);
                    processedWorldName = processedWorldName.replace("%" + variable + "%", strValue);
                    rawX = rawX.replace("%" + variable + "%", strValue);
                    rawY = rawY.replace("%" + variable + "%", strValue);
                    rawZ = rawZ.replace("%" + variable + "%", strValue);
                }
            }
            
            try {
                processedX = Double.parseDouble(rawX);
                processedY = Double.parseDouble(rawY);
                processedZ = Double.parseDouble(rawZ);
            } catch (NumberFormatException ignored) {}
        }
        
        // Специальные значения
        if (processedWorldName.equals("@player") && player != null) {
            return player.getLocation();
        }
        
        World world = Bukkit.getWorld(processedWorldName);
        if (world == null && player != null) {
            world = player.getWorld();
        }
        
        if (world != null) {
            return new Location(world, processedX, processedY, processedZ, yaw, pitch);
        }
        
        return null;
    }
    
    @Override
    public String getDisplayValue() {
        return String.format("%s: %.1f, %.1f, %.1f", worldName, x, y, z);
    }
    
    @Override
    public boolean isValid() {
        try {
            String[] parts = rawValue.split(",");
            if (parts.length >= 4) {
                Double.parseDouble(parts[1].trim());
                Double.parseDouble(parts[2].trim());
                Double.parseDouble(parts[3].trim());
                return true;
            }
        } catch (Exception e) {
            // Проверяем на переменные
            return rawValue.contains("%");
        }
        return false;
    }
    
    private static String locationToString(Location location) {
        if (location == null) return "world,0,64,0,0,0";
        return String.format("%s,%.2f,%.2f,%.2f,%.2f,%.2f",
            location.getWorld().getName(),
            location.getX(), location.getY(), location.getZ(),
            location.getYaw(), location.getPitch());
    }
}
