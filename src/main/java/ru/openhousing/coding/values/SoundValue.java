package ru.openhousing.coding.values;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.openhousing.coding.blocks.CodeBlock;

/**
 * Значение звука
 */
public class SoundValue extends Value {
    
    private Sound sound;
    private float volume;
    private float pitch;
    
    public SoundValue(String rawValue) {
        super(ValueType.SOUND, rawValue != null ? rawValue : "ENTITY_EXPERIENCE_ORB_PICKUP,1.0,1.0");
        parseSound();
    }
    
    public SoundValue(Sound sound, float volume, float pitch) {
        super(ValueType.SOUND, sound.toString() + "," + volume + "," + pitch);
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }
    
    private void parseSound() {
        try {
            String[] parts = rawValue.split(",");
            if (parts.length >= 1) {
                sound = Sound.valueOf(parts[0].trim().toUpperCase());
                volume = parts.length > 1 ? Float.parseFloat(parts[1].trim()) : 1.0f;
                pitch = parts.length > 2 ? Float.parseFloat(parts[2].trim()) : 1.0f;
            } else {
                sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
                volume = 1.0f;
                pitch = 1.0f;
            }
        } catch (Exception e) {
            sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
            volume = 1.0f;
            pitch = 1.0f;
        }
    }
    
    @Override
    public Object getValue(Player player, CodeBlock.ExecutionContext context) {
        if (sound == null) {
            return null;
        }
        
        float processedVolume = volume;
        float processedPitch = pitch;
        
        // Обработка переменных
        if (context != null) {
            String volumeStr = String.valueOf(volume);
            String pitchStr = String.valueOf(pitch);
            
            for (String variable : context.getVariables().keySet()) {
                Object value = context.getVariable(variable);
                if (value != null) {
                    String strValue = String.valueOf(value);
                    volumeStr = volumeStr.replace("%" + variable + "%", strValue);
                    pitchStr = pitchStr.replace("%" + variable + "%", strValue);
                }
            }
            
            try {
                processedVolume = Float.parseFloat(volumeStr);
                processedPitch = Float.parseFloat(pitchStr);
            } catch (NumberFormatException ignored) {}
        }
        
        return new SoundData(sound, processedVolume, processedPitch);
    }
    
    @Override
    public String getDisplayValue() {
        if (sound != null) {
            return String.format("%s (%.1f, %.1f)", sound.toString(), volume, pitch);
        }
        return rawValue;
    }
    
    @Override
    public boolean isValid() {
        try {
            String[] parts = rawValue.split(",");
            if (parts.length >= 1) {
                Sound.valueOf(parts[0].trim().toUpperCase());
                if (parts.length > 1) {
                    Float.parseFloat(parts[1].trim());
                }
                if (parts.length > 2) {
                    Float.parseFloat(parts[2].trim());
                }
                return true;
            }
        } catch (Exception e) {
            return rawValue.contains("%");
        }
        return false;
    }
    
    /**
     * Класс для хранения данных звука
     */
    public static class SoundData {
        public final Sound sound;
        public final float volume;
        public final float pitch;
        
        public SoundData(Sound sound, float volume, float pitch) {
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
        }
    }
}
