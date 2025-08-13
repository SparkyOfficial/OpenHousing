package ru.openhousing.coding.values;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import ru.openhousing.coding.blocks.CodeBlock;

/**
 * Значение частицы
 */
public class ParticleValue extends Value {
    
    private Particle particle;
    private int count;
    private double offsetX, offsetY, offsetZ;
    private double extra;
    private Object data;
    
    public ParticleValue(String rawValue) {
        super(ValueType.PARTICLE, rawValue != null ? rawValue : "FLAME,10,0.5,0.5,0.5,0.1");
        parseParticle();
    }
    
    public ParticleValue(Particle particle, int count, double offsetX, double offsetY, double offsetZ, double extra) {
        super(ValueType.PARTICLE, 
            particle.name() + "," + count + "," + offsetX + "," + offsetY + "," + offsetZ + "," + extra);
        this.particle = particle;
        this.count = count;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.extra = extra;
    }
    
    private void parseParticle() {
        try {
            String[] parts = rawValue.split(",");
            if (parts.length >= 6) {
                particle = Particle.valueOf(parts[0].trim().toUpperCase());
                count = Integer.parseInt(parts[1].trim());
                offsetX = Double.parseDouble(parts[2].trim());
                offsetY = Double.parseDouble(parts[3].trim());
                offsetZ = Double.parseDouble(parts[4].trim());
                extra = Double.parseDouble(parts[5].trim());
                
                // Обработка специальных данных для некоторых частиц
                if (parts.length > 6 && particle == Particle.DUST) {
                    // Цветная пыль: R,G,B,size
                    try {
                        String[] colorParts = parts[6].split(":");
                        if (colorParts.length >= 3) {
                            int r = Integer.parseInt(colorParts[0]);
                            int g = Integer.parseInt(colorParts[1]);
                            int b = Integer.parseInt(colorParts[2]);
                            float size = colorParts.length > 3 ? Float.parseFloat(colorParts[3]) : 1.0f;
                            data = new Particle.DustOptions(Color.fromRGB(r, g, b), size);
                        }
                    } catch (Exception e) {
                        data = new Particle.DustOptions(Color.RED, 1.0f);
                    }
                }
            } else {
                // Значения по умолчанию
                particle = Particle.FLAME;
                count = 10;
                offsetX = offsetY = offsetZ = 0.5;
                extra = 0.1;
            }
        } catch (Exception e) {
            // Значения по умолчанию при ошибке
            particle = Particle.FLAME;
            count = 10;
            offsetX = offsetY = offsetZ = 0.5;
            extra = 0.1;
        }
    }
    
    @Override
    public Object getValue(Player player, CodeBlock.ExecutionContext context) {
        if (particle == null) {
            return null;
        }
        
        int processedCount = count;
        double processedOffsetX = offsetX, processedOffsetY = offsetY, processedOffsetZ = offsetZ;
        double processedExtra = extra;
        
        // Обработка переменных
        if (context != null) {
            String countStr = String.valueOf(count);
            String offsetXStr = String.valueOf(offsetX);
            String offsetYStr = String.valueOf(offsetY);
            String offsetZStr = String.valueOf(offsetZ);
            String extraStr = String.valueOf(extra);
            
            for (String variable : context.getVariables().keySet()) {
                Object value = context.getVariable(variable);
                if (value != null) {
                    String strValue = String.valueOf(value);
                    countStr = countStr.replace("%" + variable + "%", strValue);
                    offsetXStr = offsetXStr.replace("%" + variable + "%", strValue);
                    offsetYStr = offsetYStr.replace("%" + variable + "%", strValue);
                    offsetZStr = offsetZStr.replace("%" + variable + "%", strValue);
                    extraStr = extraStr.replace("%" + variable + "%", strValue);
                }
            }
            
            try {
                processedCount = Integer.parseInt(countStr);
                processedOffsetX = Double.parseDouble(offsetXStr);
                processedOffsetY = Double.parseDouble(offsetYStr);
                processedOffsetZ = Double.parseDouble(offsetZStr);
                processedExtra = Double.parseDouble(extraStr);
            } catch (NumberFormatException ignored) {}
        }
        
        return new ParticleData(particle, processedCount, 
            processedOffsetX, processedOffsetY, processedOffsetZ, processedExtra, data);
    }
    
    @Override
    public String getDisplayValue() {
        if (particle != null) {
            return String.format("%s x%d", particle.name(), count);
        }
        return rawValue;
    }
    
    @Override
    public boolean isValid() {
        try {
            String[] parts = rawValue.split(",");
            if (parts.length >= 6) {
                Particle.valueOf(parts[0].trim().toUpperCase());
                Integer.parseInt(parts[1].trim());
                Double.parseDouble(parts[2].trim());
                Double.parseDouble(parts[3].trim());
                Double.parseDouble(parts[4].trim());
                Double.parseDouble(parts[5].trim());
                return true;
            }
        } catch (Exception e) {
            return rawValue.contains("%");
        }
        return false;
    }
    
    /**
     * Класс для хранения данных частицы
     */
    public static class ParticleData {
        public final Particle particle;
        public final int count;
        public final double offsetX, offsetY, offsetZ;
        public final double extra;
        public final Object data;
        
        public ParticleData(Particle particle, int count, double offsetX, double offsetY, double offsetZ, double extra, Object data) {
            this.particle = particle;
            this.count = count;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.extra = extra;
            this.data = data;
        }
    }
}
