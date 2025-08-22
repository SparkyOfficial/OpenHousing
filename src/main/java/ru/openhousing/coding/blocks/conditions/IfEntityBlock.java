package ru.openhousing.coding.blocks.conditions;

import org.bukkit.entity.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.potion.PotionEffectType;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок условия "Если существо"
 */
public class IfEntityBlock extends CodeBlock {
    
    public enum EntityConditionType {
        TYPE_IS("Тип существа", "Проверяет тип существа"),
        HEALTH_ABOVE("Здоровье больше", "Проверяет уровень здоровья"),
        HEALTH_BELOW("Здоровье меньше", "Проверяет уровень здоровья"),
        HEALTH_PERCENT_ABOVE("Здоровье % больше", "Проверяет процент здоровья"),
        HEALTH_PERCENT_BELOW("Здоровье % меньше", "Проверяет процент здоровья"),
        IS_ALIVE("Живое", "Проверяет, живо ли существо"),
        IS_DEAD("Мертвое", "Проверяет, мертво ли существо"),
        IS_ADULT("Взрослое", "Проверяет, взрослое ли существо"),
        IS_BABY("Детеныш", "Проверяет, детеныш ли это"),
        IS_TAMED("Прирученное", "Проверяет, приручено ли существо"),
        IS_LEASHED("На поводке", "Проверяет, на поводке ли существо"),
        IS_SITTING("Сидит", "Проверяет, сидит ли существо"),
        IS_STANDING("Стоит", "Проверяет, стоит ли существо"),
        IS_SLEEPING("Спит", "Проверяет, спит ли существо"),
        IS_AGGRESSIVE("Агрессивное", "Проверяет, агрессивно ли существо"),
        IS_PEACEFUL("Мирное", "Проверяет, мирное ли существо"),
        IS_UNDEAD("Нежить", "Проверяет, является ли существо нежитью"),
        IS_BOSS("Босс", "Проверяет, является ли существо боссом"),
        IS_FLYING("Летает", "Проверяет, летает ли существо"),
        IS_SWIMMING("Плавает", "Проверяет, плавает ли существо"),
        IS_ON_FIRE("Горит", "Проверяет, горит ли существо"),
        IS_IN_WATER("В воде", "Проверяет, находится ли существо в воде"),
        IS_IN_LAVA("В лаве", "Проверяет, находится ли существо в лаве"),
        IS_ON_GROUND("На земле", "Проверяет, на земле ли существо"),
        IS_GLOWING("Светится", "Проверяет, светится ли существо"),
        IS_INVISIBLE("Невидимое", "Проверяет, невидимо ли существо"),
        IS_SILENT("Безмолвное", "Проверяет, безмолвно ли существо"),
        HAS_GRAVITY("Есть гравитация", "Проверяет, действует ли гравитация"),
        HAS_POTION_EFFECT("Есть эффект зелья", "Проверяет наличие эффекта зелья"),
        HAS_TARGET("Есть цель", "Проверяет, есть ли у существа цель"),
        TARGET_IS_PLAYER("Цель - игрок", "Проверяет, является ли цель игроком"),
        DISTANCE_FROM_SPAWN("Расстояние от спавна", "Проверяет расстояние от точки спавна"),
        DISTANCE_FROM_PLAYER("Расстояние от игрока", "Проверяет расстояние до игрока"),
        IN_WORLD("В мире", "Проверяет нахождение в определенном мире"),
        NAME_EQUALS("Имя равно", "Проверяет имя существа"),
        NAME_CONTAINS("Имя содержит", "Проверяет часть имени существа"),
        HAS_CUSTOM_NAME("Есть кастомное имя", "Проверяет наличие кастомного имени"),
        IS_CUSTOM_NAME_VISIBLE("Имя видимо", "Проверяет видимость кастомного имени"),
        AGE_ABOVE("Возраст больше", "Проверяет возраст существа"),
        AGE_BELOW("Возраст меньше", "Проверяет возраст существа"),
        IS_BREEDING("Размножается", "Проверяет, может ли существо размножаться"),
        LOVE_MODE("В режиме любви", "Проверяет, в режиме ли любви существо"),
        IS_PASSENGER("Пассажир", "Проверяет, является ли существо пассажиром"),
        HAS_PASSENGER("Есть пассажир", "Проверяет, есть ли у существа пассажир");
        
        private final String displayName;
        private final String description;
        
        EntityConditionType(String displayName, String description) {
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
    
    public IfEntityBlock() {
        super(BlockType.IF_ENTITY_EXISTS); // Используем любое условие существа как базовое
        setParameter(ru.openhousing.coding.constants.BlockParams.CONDITION_TYPE, EntityConditionType.TYPE_IS);
        setParameter(ru.openhousing.coding.constants.BlockParams.VALUE, "");
        setParameter(ru.openhousing.coding.constants.BlockParams.COMPARE_VALUE, "");
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        Object target = context.getTarget();
        
        if (!(target instanceof Entity)) {
            // Если цель не установлена, пытаемся найти ближайшее существо к игроку
            if (context.getPlayer() != null) {
                target = findNearestEntity(context);
            }
            
            if (!(target instanceof Entity)) {
                return ExecutionResult.error("Существо не найдено");
            }
        }
        
        Entity entity = (Entity) target;
        boolean conditionMet = checkCondition(entity, context);
        
        if (conditionMet) {
            return executeChildren(context);
        }
        
        return ExecutionResult.success();
    }
    
    private boolean checkCondition(Entity entity, ExecutionContext context) {
        EntityConditionType conditionType = (EntityConditionType) getParameter(ru.openhousing.coding.constants.BlockParams.CONDITION_TYPE);
        String value = replaceVariables((String) getParameter(ru.openhousing.coding.constants.BlockParams.VALUE), context);
        String compareValue = replaceVariables((String) getParameter(ru.openhousing.coding.constants.BlockParams.COMPARE_VALUE), context);
        
        if (conditionType == null) {
            return false;
        }
        
        try {
            switch (conditionType) {
                case TYPE_IS:
                    return entity.getType().name().equalsIgnoreCase(value);
                    
                case IS_ALIVE:
                    return !entity.isDead();
                    
                case IS_DEAD:
                    return entity.isDead();
                    
                case HEALTH_ABOVE:
                    if (entity instanceof Damageable) {
                        try {
                            double healthThreshold = Double.parseDouble(value);
                            if (healthThreshold < 0) {
                                if (context.getPlayer() != null) {
                                    context.getPlayer().sendMessage("§c[OpenHousing] Порог здоровья не может быть отрицательным. Используется 0.");
                                }
                                healthThreshold = 0;
                            }
                            return ((Damageable) entity).getHealth() > healthThreshold;
                        } catch (NumberFormatException e) {
                            if (context.getPlayer() != null) {
                                context.getPlayer().sendMessage("§c[OpenHousing] Неверный порог здоровья: '" + value + "'. Ожидается число");
                            }
                        }
                    }
                    break;
                    
                case HEALTH_BELOW:
                    if (entity instanceof Damageable) {
                        try {
                            double healthThreshold = Double.parseDouble(value);
                            if (healthThreshold < 0) {
                                if (context.getPlayer() != null) {
                                    context.getPlayer().sendMessage("§c[OpenHousing] Порог здоровья не может быть отрицательным. Используется 0.");
                                }
                                healthThreshold = 0;
                            }
                            return ((Damageable) entity).getHealth() < healthThreshold;
                        } catch (NumberFormatException e) {
                            if (context.getPlayer() != null) {
                                context.getPlayer().sendMessage("§c[OpenHousing] Неверный порог здоровья: '" + value + "'. Ожидается число");
                            }
                        }
                    }
                    break;
                    
                case HEALTH_PERCENT_ABOVE:
                    if (entity instanceof LivingEntity) {
                        LivingEntity living = (LivingEntity) entity;
                        AttributeInstance maxHealth = living.getAttribute(Attribute.MAX_HEALTH);
                        if (maxHealth != null) {
                            try {
                                double percentThreshold = Double.parseDouble(value);
                                if (percentThreshold < 0 || percentThreshold > 100) {
                                    if (context.getPlayer() != null) {
                                        context.getPlayer().sendMessage("§c[OpenHousing] Процент здоровья должен быть от 0 до 100. Используется значение по умолчанию: 50");
                                    }
                                    percentThreshold = Math.min(Math.max(percentThreshold, 0), 100);
                                }
                                double percent = (living.getHealth() / maxHealth.getValue()) * 100;
                                return percent > percentThreshold;
                            } catch (NumberFormatException e) {
                                if (context.getPlayer() != null) {
                                    context.getPlayer().sendMessage("§c[OpenHousing] Неверный процент здоровья: '" + value + "'. Ожидается число от 0 до 100");
                                }
                            }
                        }
                    }
                    break;
                    
                case HEALTH_PERCENT_BELOW:
                    if (entity instanceof LivingEntity) {
                        LivingEntity living = (LivingEntity) entity;
                        AttributeInstance maxHealth = living.getAttribute(Attribute.MAX_HEALTH);
                        if (maxHealth != null) {
                            try {
                                double percentThreshold = Double.parseDouble(value);
                                if (percentThreshold < 0 || percentThreshold > 100) {
                                    if (context.getPlayer() != null) {
                                        context.getPlayer().sendMessage("§c[OpenHousing] Процент здоровья должен быть от 0 до 100. Используется значение по умолчанию: 50");
                                    }
                                    percentThreshold = Math.min(Math.max(percentThreshold, 0), 100);
                                }
                                double percent = (living.getHealth() / maxHealth.getValue()) * 100;
                                return percent < percentThreshold;
                            } catch (NumberFormatException e) {
                                if (context.getPlayer() != null) {
                                    context.getPlayer().sendMessage("§c[OpenHousing] Неверный процент здоровья: '" + value + "'. Ожидается число от 0 до 100");
                                }
                            }
                        }
                    }
                    break;
                    
                case IS_ADULT:
                    if (entity instanceof Ageable) {
                        return ((Ageable) entity).isAdult();
                    }
                    break;
                    
                case IS_BABY:
                    if (entity instanceof Ageable) {
                        return !((Ageable) entity).isAdult();
                    }
                    break;
                    
                case IS_TAMED:
                    if (entity instanceof Tameable) {
                        return ((Tameable) entity).isTamed();
                    }
                    break;
                    
                case IS_LEASHED:
                    if (entity instanceof LivingEntity) {
                        return ((LivingEntity) entity).isLeashed();
                    }
                    return false;
                    
                case IS_SITTING:
                    if (entity instanceof Sittable) {
                        return ((Sittable) entity).isSitting();
                    }
                    break;
                    
                case IS_SLEEPING:
                    if (entity instanceof Fox) {
                        return ((Fox) entity).isSleeping();
                    }
                    break;
                    
                case IS_AGGRESSIVE:
                    if (entity instanceof Monster) {
                        return true; // Монстры агрессивны по умолчанию
                    }
                    if (entity instanceof Wolf) {
                        return ((Wolf) entity).isAngry();
                    }
                    break;
                    
                case IS_PEACEFUL:
                    return entity instanceof Animals && !(entity instanceof Monster);
                    
                case IS_UNDEAD:
                    return entity instanceof Zombie || entity instanceof Skeleton || 
                           entity instanceof WitherSkeleton || entity instanceof Wither;
                    
                case IS_BOSS:
                    return entity instanceof Boss;
                    
                case IS_FLYING:
                    if (entity instanceof Flying) {
                        return true;
                    }
                    if (entity instanceof LivingEntity) {
                        return ((LivingEntity) entity).isGliding();
                    }
                    return false;
                    
                case IS_SWIMMING:
                    if (entity instanceof LivingEntity) {
                        return ((LivingEntity) entity).isSwimming();
                    }
                    return false;
                    
                case IS_ON_FIRE:
                    return entity.getFireTicks() > 0;
                    
                case IS_IN_WATER:
                    return entity.isInWater();
                    
                case IS_IN_LAVA:
                    return entity.isInLava();
                    
                case IS_ON_GROUND:
                    return entity.isOnGround();
                    
                case IS_GLOWING:
                    return entity.isGlowing();
                    
                case IS_INVISIBLE:
                    return entity.isInvisible();
                    
                case IS_SILENT:
                    return entity.isSilent();
                    
                case HAS_GRAVITY:
                    return entity.hasGravity();
                    
                case HAS_POTION_EFFECT:
                    if (entity instanceof LivingEntity) {
                        LivingEntity living = (LivingEntity) entity;
                        PotionEffectType effectType = PotionEffectType.getByName(value.toUpperCase());
                        return effectType != null && living.hasPotionEffect(effectType);
                    }
                    break;
                    
                case HAS_TARGET:
                    if (entity instanceof Mob) {
                        return ((Mob) entity).getTarget() != null;
                    }
                    break;
                    
                case TARGET_IS_PLAYER:
                    if (entity instanceof Mob) {
                        LivingEntity target = ((Mob) entity).getTarget();
                        return target instanceof Player;
                    }
                    break;
                    
                case DISTANCE_FROM_PLAYER:
                    if (context.getPlayer() != null) {
                        double distance = entity.getLocation().distance(context.getPlayer().getLocation());
                        double maxDistance = Double.parseDouble(value);
                        return distance <= maxDistance;
                    }
                    break;
                    
                case IN_WORLD:
                    return entity.getWorld().getName().equalsIgnoreCase(value);
                    
                case NAME_EQUALS:
                    String entityName = entity.getCustomName();
                    return entityName != null && entityName.equals(value);
                    
                case NAME_CONTAINS:
                    entityName = entity.getCustomName();
                    return entityName != null && entityName.toLowerCase().contains(value.toLowerCase());
                    
                case HAS_CUSTOM_NAME:
                    return entity.getCustomName() != null;
                    
                case IS_CUSTOM_NAME_VISIBLE:
                    return entity.isCustomNameVisible();
                    
                case AGE_ABOVE:
                    if (entity instanceof Ageable) {
                        return ((Ageable) entity).getAge() > Integer.parseInt(value);
                    }
                    break;
                    
                case AGE_BELOW:
                    if (entity instanceof Ageable) {
                        return ((Ageable) entity).getAge() < Integer.parseInt(value);
                    }
                    break;
                    
                case IS_BREEDING:
                    if (entity instanceof Animals) {
                        return ((Animals) entity).canBreed();
                    }
                    break;
                    
                case LOVE_MODE:
                    if (entity instanceof Animals) {
                        return ((Animals) entity).isLoveMode();
                    }
                    break;
                    
                case IS_PASSENGER:
                    return entity.isInsideVehicle();
                    
                case HAS_PASSENGER:
                    return !entity.getPassengers().isEmpty();
            }
        } catch (Exception e) {
            return false;
        }
        
        return false;
    }
    
    /**
     * Поиск ближайшего существа к игроку
     */
    private Entity findNearestEntity(ExecutionContext context) {
        if (context.getPlayer() == null) {
            return null;
        }
        
        // Используем оптимизированный поиск из CodeBlockUtils
        List<Entity> nearbyEntities = ru.openhousing.utils.CodeBlockUtils.findNearestEntities(
            context.getPlayer().getLocation(), 
            10, 
            Entity.class
        );
        
        if (nearbyEntities.isEmpty()) return null;
        
        // Находим ближайшее существо
        Entity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player) continue;
            
            double distance = entity.getLocation().distance(context.getPlayer().getLocation());
            if (distance < nearestDistance) {
                nearest = entity;
                nearestDistance = distance;
            }
        }
        
        return nearest;
    }
    
    /**
     * Замена переменных в строке
     */

    
    @Override
    public boolean validate() {
        return getParameter(ru.openhousing.coding.constants.BlockParams.CONDITION_TYPE) != null;
    }
    
    @Override
    public List<String> getDescription() {
        EntityConditionType conditionType = (EntityConditionType) getParameter(ru.openhousing.coding.constants.BlockParams.CONDITION_TYPE);
        String value = (String) getParameter(ru.openhousing.coding.constants.BlockParams.VALUE);
        
        return Arrays.asList(
            "§6Если существо",
            "§7Условие: §f" + (conditionType != null ? conditionType.getDisplayName() : "Не выбрано"),
            "§7Значение: §f" + (value != null && !value.isEmpty() ? value : "Не указано"),
            "",
            "§8Дочерних блоков: " + childBlocks.size()
        );
    }
}
