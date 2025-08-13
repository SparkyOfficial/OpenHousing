package ru.openhousing.coding.blocks.actions;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок действий с существами
 */
public class EntityActionBlock extends CodeBlock {
    
    public enum EntityActionType {
        SPAWN("Создать существо", "Спавнит новое существо"),
        REMOVE("Удалить существо", "Удаляет существо"),
        KILL("Убить существо", "Убивает существо"),
        DAMAGE("Нанести урон", "Наносит урон существу"),
        HEAL("Лечить", "Восстанавливает здоровье"),
        SET_HEALTH("Установить здоровье", "Устанавливает здоровье"),
        SET_MAX_HEALTH("Установить макс. здоровье", "Устанавливает максимальное здоровье"),
        TELEPORT("Телепортировать", "Телепортирует существо"),
        TELEPORT_TO_PLAYER("Телепорт к игроку", "Телепортирует существо к игроку"),
        SET_VELOCITY("Установить скорость", "Устанавливает скорость движения"),
        PUSH("Толкнуть", "Толкает существо в направлении"),
        LAUNCH("Запустить", "Запускает существо в воздух"),
        SET_FIRE("Поджечь", "Поджигает существо"),
        EXTINGUISH("Потушить", "Тушит существо"),
        SET_NAME("Установить имя", "Устанавливает кастомное имя"),
        SET_NAME_VISIBLE("Показать имя", "Делает имя видимым/невидимым"),
        SET_GLOWING("Свечение", "Включает/выключает свечение"),
        SET_INVISIBLE("Невидимость", "Делает существо невидимым/видимым"),
        SET_SILENT("Безмолвие", "Делает существо безмолвным"),
        SET_GRAVITY("Гравитация", "Включает/выключает гравитацию"),
        SET_INVULNERABLE("Неуязвимость", "Делает существо неуязвимым"),
        ADD_POTION_EFFECT("Добавить эффект", "Добавляет эффект зелья"),
        REMOVE_POTION_EFFECT("Убрать эффект", "Убирает эффект зелья"),
        CLEAR_EFFECTS("Очистить эффекты", "Убирает все эффекты"),
        SET_TARGET("Установить цель", "Устанавливает цель для атаки"),
        CLEAR_TARGET("Очистить цель", "Убирает цель"),
        SET_ANGRY("Разозлить", "Делает существо агрессивным"),
        SET_PEACEFUL("Успокоить", "Делает существо мирным"),
        TAME("Приручить", "Приручает существо"),
        UNTAME("Отучить", "Отучает существо"),
        SIT("Посадить", "Заставляет существо сидеть"),
        STAND("Поставить", "Заставляет существо встать"),
        SET_BABY("Сделать детенышем", "Превращает в детеныша"),
        SET_ADULT("Сделать взрослым", "Превращает во взрослого"),
        SET_AGE("Установить возраст", "Устанавливает возраст"),
        BREED("Размножить", "Запускает размножение"),
        SHEAR("Постричь", "Стрижет овцу"),
        MILK("Подоить", "Доит корову"),
        LEASH("Взять на поводок", "Берет существо на поводок"),
        UNLEASH("Отпустить с поводка", "Отпускает с поводка"),
        MOUNT_PLAYER("Посадить игрока", "Сажает игрока на существо"),
        DISMOUNT_PLAYER("Снять игрока", "Снимает игрока с существа"),
        EXPLODE("Взорвать", "Взрывает существо"),
        LIGHTNING_STRIKE("Ударить молнией", "Ударяет молнией по существу"),
        PLAY_SOUND("Воспроизвести звук", "Воспроизводит звук существа"),
        SPAWN_PARTICLES("Создать частицы", "Создает частицы вокруг существа"),
        DROP_ITEM("Выбросить предмет", "Заставляет существо выбросить предмет"),
        GIVE_ITEM("Дать предмет", "Дает предмет существу"),
        SET_EQUIPMENT("Установить экипировку", "Устанавливает экипировку"),
        FOLLOW_PLAYER("Следовать за игроком", "Заставляет следовать за игроком"),
        STOP_FOLLOWING("Прекратить следование", "Прекращает следование"),
        FACE_PLAYER("Повернуться к игроку", "Поворачивает существо к игроку"),
        FACE_LOCATION("Повернуться к точке", "Поворачивает к определенной точке"),
        SET_AI("Установить ИИ", "Включает/выключает ИИ существа"),
        SET_COLLIDABLE("Столкновения", "Включает/выключает столкновения"),
        FREEZE("Заморозить", "Замораживает существо"),
        UNFREEZE("Разморозить", "Размораживает существо");
        
        private final String displayName;
        private final String description;
        
        EntityActionType(String displayName, String description) {
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
    
    public EntityActionBlock() {
        super(BlockType.ENTITY_ACTION);
        setParameter("actionType", EntityActionType.SPAWN);
        setParameter("value", "");
        setParameter("extra1", "");
        setParameter("extra2", "");
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        Object target = context.getTarget();
        EntityActionType actionType = (EntityActionType) getParameter(ru.openhousing.coding.constants.BlockParams.ACTION_TYPE);
        String value = replaceVariables((String) getParameter(ru.openhousing.coding.constants.BlockParams.VALUE), context);
        String extra1 = replaceVariables((String) getParameter(ru.openhousing.coding.constants.BlockParams.EXTRA1), context);
        String extra2 = replaceVariables((String) getParameter(ru.openhousing.coding.constants.BlockParams.EXTRA2), context);
        
        if (actionType == null) {
            return ExecutionResult.error("Не указан тип действия");
        }
        
        // Для некоторых действий не нужно существо (например, SPAWN)
        if (actionType != EntityActionType.SPAWN && !(target instanceof Entity)) {
            if (context.getPlayer() != null) {
                target = findNearestEntity(context);
            }
            
            if (!(target instanceof Entity)) {
                return ExecutionResult.error("Существо не найдено");
            }
        }
        
        try {
            executeEntityAction(context, (Entity) target, actionType, value, extra1, extra2);
            return ExecutionResult.success();
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения действия: " + e.getMessage());
        }
    }
    
    private void executeEntityAction(ExecutionContext context, Entity entity, EntityActionType actionType,
                                   String value, String extra1, String extra2) {
        
        switch (actionType) {
            case SPAWN:
                spawnEntity(context, value, extra1, extra2);
                break;
                
            case REMOVE:
                if (entity != null) {
                    entity.remove();
                }
                break;
                
            case KILL:
                if (entity instanceof LivingEntity) {
                    ((LivingEntity) entity).setHealth(0);
                }
                break;
                
            case DAMAGE:
                if (entity instanceof Damageable) {
                    double damage = Double.parseDouble(value);
                    ((Damageable) entity).damage(damage);
                }
                break;
                
            case HEAL:
                if (entity instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity) entity;
                    double healAmount = Double.parseDouble(value);
                    double newHealth = Math.min(living.getHealth() + healAmount, 
                                              living.getAttribute(Attribute.MAX_HEALTH).getValue());
                    living.setHealth(newHealth);
                }
                break;
                
            case SET_HEALTH:
                if (entity instanceof LivingEntity) {
                    double health = Double.parseDouble(value);
                    LivingEntity living = (LivingEntity) entity;
                    double maxHealth = living.getAttribute(Attribute.MAX_HEALTH).getValue();
                    living.setHealth(Math.min(health, maxHealth));
                }
                break;
                
            case SET_MAX_HEALTH:
                if (entity instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity) entity;
                    AttributeInstance health = living.getAttribute(Attribute.MAX_HEALTH);
                    if (health != null) {
                        health.setBaseValue(Double.parseDouble(value));
                    }
                }
                break;
                
            case TELEPORT:
                teleportEntity(entity, value);
                break;
                
            case TELEPORT_TO_PLAYER:
                if (context.getPlayer() != null) {
                    entity.teleport(context.getPlayer().getLocation());
                }
                break;
                
            case SET_VELOCITY:
                setVelocity(entity, value);
                break;
                
            case PUSH:
                pushEntity(entity, value, context);
                break;
                
            case LAUNCH:
                double power = Double.parseDouble(value);
                entity.setVelocity(new Vector(0, power, 0));
                break;
                
            case SET_FIRE:
                int fireTicks = Integer.parseInt(value);
                entity.setFireTicks(fireTicks);
                break;
                
            case EXTINGUISH:
                entity.setFireTicks(0);
                break;
                
            case SET_NAME:
                entity.setCustomName(ChatColor.translateAlternateColorCodes('&', value));
                break;
                
            case SET_NAME_VISIBLE:
                entity.setCustomNameVisible(Boolean.parseBoolean(value));
                break;
                
            case SET_GLOWING:
                entity.setGlowing(Boolean.parseBoolean(value));
                break;
                
            case SET_INVISIBLE:
                entity.setInvisible(Boolean.parseBoolean(value));
                break;
                
            case SET_SILENT:
                entity.setSilent(Boolean.parseBoolean(value));
                break;
                
            case SET_GRAVITY:
                entity.setGravity(Boolean.parseBoolean(value));
                break;
                
            case SET_INVULNERABLE:
                entity.setInvulnerable(Boolean.parseBoolean(value));
                break;
                
            case ADD_POTION_EFFECT:
                addPotionEffect(entity, value, extra1, extra2);
                break;
                
            case REMOVE_POTION_EFFECT:
                if (entity instanceof LivingEntity) {
                    PotionEffectType effectType = PotionEffectType.getByName(value.toUpperCase());
                    if (effectType != null) {
                        ((LivingEntity) entity).removePotionEffect(effectType);
                    }
                }
                break;
                
            case CLEAR_EFFECTS:
                if (entity instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity) entity;
                    for (PotionEffect effect : living.getActivePotionEffects()) {
                        living.removePotionEffect(effect.getType());
                    }
                }
                break;
                
            case SET_TARGET:
                setTarget(entity, value, context);
                break;
                
            case CLEAR_TARGET:
                if (entity instanceof Mob) {
                    ((Mob) entity).setTarget(null);
                }
                break;
                
            case SET_ANGRY:
                setAngry(entity, Boolean.parseBoolean(value));
                break;
                
            case TAME:
                if (entity instanceof Tameable && context.getPlayer() != null) {
                    Tameable tameable = (Tameable) entity;
                    tameable.setTamed(true);
                    tameable.setOwner(context.getPlayer());
                }
                break;
                
            case UNTAME:
                if (entity instanceof Tameable) {
                    Tameable tameable = (Tameable) entity;
                    tameable.setTamed(false);
                    tameable.setOwner(null);
                }
                break;
                
            case SIT:
                if (entity instanceof Sittable) {
                    ((Sittable) entity).setSitting(true);
                }
                break;
                
            case STAND:
                if (entity instanceof Sittable) {
                    ((Sittable) entity).setSitting(false);
                }
                break;
                
            case SET_BABY:
                if (entity instanceof Ageable) {
                    ((Ageable) entity).setBaby();
                }
                break;
                
            case SET_ADULT:
                if (entity instanceof Ageable) {
                    ((Ageable) entity).setAdult();
                }
                break;
                
            case SET_AGE:
                if (entity instanceof Ageable) {
                    ((Ageable) entity).setAge(Integer.parseInt(value));
                }
                break;
                
            case EXPLODE:
                explodeEntity(entity, value);
                break;
                
            case LIGHTNING_STRIKE:
                entity.getWorld().strikeLightning(entity.getLocation());
                break;
                
            case PLAY_SOUND:
                playEntitySound(entity, value, extra1);
                break;
                
            case SPAWN_PARTICLES:
                spawnParticles(entity, value, extra1, extra2);
                break;
                
            case SET_AI:
                if (entity instanceof Mob) {
                    ((Mob) entity).setAware(Boolean.parseBoolean(value));
                }
                break;
                
            case SET_COLLIDABLE:
                if (entity instanceof LivingEntity) {
                    ((LivingEntity) entity).setCollidable(Boolean.parseBoolean(value));
                }
                break;
        }
    }
    
    /**
     * Спавн существа
     */
    private void spawnEntity(ExecutionContext context, String entityType, String location, String extra) {
        if (context.getPlayer() == null) return;
        
        try {
            EntityType type = EntityType.valueOf(entityType.toUpperCase());
            Location spawnLoc = parseLocation(location, context);
            if (spawnLoc == null) {
                spawnLoc = context.getPlayer().getLocation();
            }
            
            Entity spawned = spawnLoc.getWorld().spawnEntity(spawnLoc, type);
            
            // Устанавливаем spawned entity как новую цель
            context.setTarget(spawned);
            
        } catch (IllegalArgumentException e) {
            // Неверный тип существа
        }
    }
    
    /**
     * Телепортация существа
     */
    private void teleportEntity(Entity entity, String location) {
        Location loc = parseLocationString(location, entity.getWorld());
        if (loc != null) {
            entity.teleport(loc);
        }
    }
    
    /**
     * Установка скорости
     */
    private void setVelocity(Entity entity, String velocityString) {
        String[] parts = velocityString.split(",");
        if (parts.length >= 3) {
            try {
                double x = Double.parseDouble(parts[0].trim());
                double y = Double.parseDouble(parts[1].trim());
                double z = Double.parseDouble(parts[2].trim());
                entity.setVelocity(new Vector(x, y, z));
            } catch (NumberFormatException e) {
                // Игнорируем ошибку
            }
        }
    }
    
    /**
     * Толчок существа
     */
    private void pushEntity(Entity entity, String direction, ExecutionContext context) {
        Vector pushVector = new Vector(0, 0, 0);
        double force = 1.0;
        
        String[] parts = direction.split(":");
        if (parts.length > 1) {
            try {
                force = Double.parseDouble(parts[1]);
            } catch (NumberFormatException e) {
                force = 1.0;
            }
        }
        
        switch (parts[0].toLowerCase()) {
            case "forward":
                pushVector = entity.getLocation().getDirection().multiply(force);
                break;
            case "backward":
                pushVector = entity.getLocation().getDirection().multiply(-force);
                break;
            case "up":
                pushVector = new Vector(0, force, 0);
                break;
            case "down":
                pushVector = new Vector(0, -force, 0);
                break;
            case "left":
                pushVector = entity.getLocation().getDirection().rotateAroundY(Math.PI/2).multiply(force);
                break;
            case "right":
                pushVector = entity.getLocation().getDirection().rotateAroundY(-Math.PI/2).multiply(force);
                break;
            case "player":
                if (context.getPlayer() != null) {
                    Vector dir = context.getPlayer().getLocation().subtract(entity.getLocation()).toVector().normalize();
                    pushVector = dir.multiply(force);
                }
                break;
        }
        
        entity.setVelocity(pushVector);
    }
    
    /**
     * Добавление эффекта зелья
     */
    private void addPotionEffect(Entity entity, String effectName, String duration, String amplifier) {
        if (!(entity instanceof LivingEntity)) return;
        
        PotionEffectType effectType = PotionEffectType.getByName(effectName.toUpperCase());
        if (effectType == null) return;
        
        int dur = 200; // 10 секунд по умолчанию
        int amp = 0;
        
        try {
            if (!duration.isEmpty()) {
                dur = Integer.parseInt(duration) * 20; // Конвертируем секунды в тики
            }
            if (!amplifier.isEmpty()) {
                amp = Integer.parseInt(amplifier);
            }
        } catch (NumberFormatException e) {
            // Используем значения по умолчанию
        }
        
        PotionEffect effect = new PotionEffect(effectType, dur, amp);
        ((LivingEntity) entity).addPotionEffect(effect);
    }
    
    /**
     * Установка цели
     */
    private void setTarget(Entity entity, String targetType, ExecutionContext context) {
        if (!(entity instanceof Mob)) return;
        
        Mob mob = (Mob) entity;
        
        switch (targetType.toLowerCase()) {
            case "player":
                if (context.getPlayer() != null) {
                    mob.setTarget(context.getPlayer());
                }
                break;
            case "nearest_player":
                Player nearest = null;
                double nearestDistance = Double.MAX_VALUE;
                for (Player player : entity.getWorld().getPlayers()) {
                    double distance = player.getLocation().distance(entity.getLocation());
                    if (distance < nearestDistance) {
                        nearest = player;
                        nearestDistance = distance;
                    }
                }
                if (nearest != null) {
                    mob.setTarget(nearest);
                }
                break;
            case "none":
            case "null":
                mob.setTarget(null);
                break;
        }
    }
    
    /**
     * Установка агрессивности
     */
    private void setAngry(Entity entity, boolean angry) {
        if (entity instanceof Wolf) {
            ((Wolf) entity).setAngry(angry);
        } else if (entity instanceof PigZombie) {
            if (angry) {
                ((PigZombie) entity).setAngry(true);
                ((PigZombie) entity).setAnger(400); // 20 секунд
            } else {
                ((PigZombie) entity).setAngry(false);
            }
        }
    }
    
    /**
     * Взрыв существа
     */
    private void explodeEntity(Entity entity, String power) {
        float explosionPower = 4.0f;
        try {
            explosionPower = Float.parseFloat(power);
        } catch (NumberFormatException e) {
            // Используем значение по умолчанию
        }
        
        entity.getWorld().createExplosion(entity.getLocation(), explosionPower, false, false);
    }
    
    /**
     * Воспроизведение звука существа
     */
    private void playEntitySound(Entity entity, String soundName, String volume) {
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            float vol = 1.0f;
            try {
                vol = Float.parseFloat(volume);
            } catch (NumberFormatException e) {
                // Используем значение по умолчанию
            }
            
            entity.getWorld().playSound(entity.getLocation(), sound, vol, 1.0f);
        } catch (IllegalArgumentException e) {
            // Неверное имя звука
        }
    }
    
    /**
     * Создание частиц
     */
    private void spawnParticles(Entity entity, String particleName, String count, String offset) {
        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase());
            int particleCount = 10;
            double offsetValue = 0.5;
            
            try {
                particleCount = Integer.parseInt(count);
            } catch (NumberFormatException e) {
                // Используем значение по умолчанию
                if (entity instanceof Player) {
                    ((Player) entity).sendMessage("§c[OpenHousing] Неверное количество частиц: '" + count + "'. Используется значение по умолчанию: 10");
                }
            }
            
            try {
                offsetValue = Double.parseDouble(offset);
            } catch (NumberFormatException e) {
                // Используем значение по умолчанию
                if (entity instanceof Player) {
                    ((Player) entity).sendMessage("§c[OpenHousing] Неверное смещение частиц: '" + offset + "'. Используется значение по умолчанию: 0.5");
                }
            }
            
            entity.getWorld().spawnParticle(particle, entity.getLocation(), particleCount, 
                                          offsetValue, offsetValue, offsetValue);
        } catch (IllegalArgumentException e) {
            // Неверное имя частицы
            if (entity instanceof Player) {
                ((Player) entity).sendMessage("§c[OpenHousing] Неверное имя частицы: '" + particleName + "'. Используйте одно из доступных значений частиц.");
            }
        }
    }
    
    /**
     * Поиск ближайшего существа
     */
    private Entity findNearestEntity(ExecutionContext context) {
        if (context.getPlayer() == null) return null;
        
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
     * Парсинг локации
     */
    private Location parseLocation(String locationString, ExecutionContext context) {
        return ru.openhousing.utils.CodeBlockUtils.parseLocation(locationString, context.getPlayer().getWorld());
    }
    
    /**
     * Парсинг локации из строки
     */
    private Location parseLocationString(String locationString, World world) {
        return ru.openhousing.utils.CodeBlockUtils.parseLocation(locationString, world);
    }
    
    /**
     * Замена переменных в строке
     */
    private String replaceVariables(String text, ExecutionContext context) {
        return ru.openhousing.utils.CodeBlockUtils.replaceVariables(text, context);
    }
    
    @Override
    public boolean validate() {
        return getParameter(ru.openhousing.coding.constants.BlockParams.ACTION_TYPE) != null;
    }
    
    @Override
    public List<String> getDescription() {
        EntityActionType actionType = (EntityActionType) getParameter(ru.openhousing.coding.constants.BlockParams.ACTION_TYPE);
        String value = (String) getParameter(ru.openhousing.coding.constants.BlockParams.VALUE);
        
        return Arrays.asList(
            "§6Действие существа",
            "§7Тип: §f" + (actionType != null ? actionType.getDisplayName() : "Не выбран"),
            "§7Значение: §f" + (value != null && !value.isEmpty() ? value : "Не указано"),
            "",
            "§8" + (actionType != null ? actionType.getDescription() : "")
        );
    }
}
