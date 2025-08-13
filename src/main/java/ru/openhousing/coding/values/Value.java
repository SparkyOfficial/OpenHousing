package ru.openhousing.coding.values;

import org.bukkit.entity.Player;
import ru.openhousing.coding.blocks.CodeBlock;

/**
 * Базовый класс для значений в системе кодинга
 */
public abstract class Value {
    
    protected String rawValue;
    protected ValueType type;
    
    public Value(ValueType type, String rawValue) {
        this.type = type;
        this.rawValue = rawValue;
    }
    
    /**
     * Получение обработанного значения
     */
    public abstract Object getValue(Player player, CodeBlock.ExecutionContext context);
    
    /**
     * Получение строкового представления для GUI
     */
    public abstract String getDisplayValue();
    
    /**
     * Проверка валидности значения
     */
    public abstract boolean isValid();
    
    /**
     * Получение типа значения
     */
    public ValueType getType() {
        return type;
    }
    
    /**
     * Получение сырого значения
     */
    public String getRawValue() {
        return rawValue;
    }
    
    /**
     * Установка сырого значения
     */
    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }
    
    /**
     * Создание значения по типу и строке
     */
    public static Value create(ValueType type, String rawValue) {
        switch (type) {
            case TEXT:
                return new TextValue(rawValue);
            case NUMBER:
                return new NumberValue(rawValue);
            case VARIABLE:
                return new VariableValue(rawValue);
            case LOCATION:
                return new LocationValue(rawValue);
            case POTION_EFFECT:
                return new PotionEffectValue(rawValue);
            case PARTICLE:
                return new ParticleValue(rawValue);
            case ITEM:
                return new ItemValue(rawValue);
            case SOUND:
                return new SoundValue(rawValue);
            default:
                return new TextValue(rawValue);
        }
    }
}
