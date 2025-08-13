package ru.openhousing.coding.values;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.openhousing.coding.blocks.CodeBlock;

/**
 * Значение эффекта зелья
 */
public class PotionEffectValue extends Value {
    
    private PotionEffectType effectType;
    private int duration;
    private int amplifier;
    private boolean ambient;
    private boolean particles;
    
    public PotionEffectValue(String rawValue) {
        super(ValueType.POTION_EFFECT, rawValue != null ? rawValue : "SPEED,600,0,false,true");
        parseEffect();
    }
    
    public PotionEffectValue(PotionEffectType type, int duration, int amplifier) {
        super(ValueType.POTION_EFFECT, 
            type.getName() + "," + duration + "," + amplifier + ",false,true");
        this.effectType = type;
        this.duration = duration;
        this.amplifier = amplifier;
        this.ambient = false;
        this.particles = true;
    }
    
    private void parseEffect() {
        try {
            String[] parts = rawValue.split(",");
            if (parts.length >= 3) {
                effectType = PotionEffectType.getByName(parts[0].trim().toUpperCase());
                duration = Integer.parseInt(parts[1].trim());
                amplifier = Integer.parseInt(parts[2].trim());
                ambient = parts.length > 3 ? Boolean.parseBoolean(parts[3].trim()) : false;
                particles = parts.length > 4 ? Boolean.parseBoolean(parts[4].trim()) : true;
            } else {
                // Значения по умолчанию
                effectType = PotionEffectType.SPEED;
                duration = 600;
                amplifier = 0;
                ambient = false;
                particles = true;
            }
        } catch (Exception e) {
            // Значения по умолчанию при ошибке
            effectType = PotionEffectType.SPEED;
            duration = 600;
            amplifier = 0;
            ambient = false;
            particles = true;
        }
    }
    
    @Override
    public Object getValue(Player player, CodeBlock.ExecutionContext context) {
        if (effectType == null) {
            return null;
        }
        
        int processedDuration = duration;
        int processedAmplifier = amplifier;
        
        // Обработка переменных
        if (context != null) {
            String durationStr = String.valueOf(duration);
            String amplifierStr = String.valueOf(amplifier);
            
            for (String variable : context.getVariables().keySet()) {
                Object value = context.getVariable(variable);
                if (value != null) {
                    String strValue = String.valueOf(value);
                    durationStr = durationStr.replace("%" + variable + "%", strValue);
                    amplifierStr = amplifierStr.replace("%" + variable + "%", strValue);
                }
            }
            
            try {
                processedDuration = Integer.parseInt(durationStr);
                processedAmplifier = Integer.parseInt(amplifierStr);
            } catch (NumberFormatException ignored) {}
        }
        
        return new PotionEffect(effectType, processedDuration, processedAmplifier, ambient, particles);
    }
    
    @Override
    public String getDisplayValue() {
        if (effectType != null) {
            return String.format("%s %d (%ds)", 
                effectType.getName(), amplifier + 1, duration / 20);
        }
        return rawValue;
    }
    
    @Override
    public boolean isValid() {
        try {
            String[] parts = rawValue.split(",");
            if (parts.length >= 3) {
                PotionEffectType.getByName(parts[0].trim().toUpperCase());
                Integer.parseInt(parts[1].trim());
                Integer.parseInt(parts[2].trim());
                return true;
            }
        } catch (Exception e) {
            return rawValue.contains("%");
        }
        return false;
    }
}
