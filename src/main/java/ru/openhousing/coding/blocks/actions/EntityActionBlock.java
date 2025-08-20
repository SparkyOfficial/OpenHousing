package ru.openhousing.coding.blocks.actions;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.actions.EntityActionBlock.EntityActionType;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
                    try {
                        double damage = Double.parseDouble(value);
                        if (damage < 0) {
                            if (context.getPlayer() != null) {
                                context.getPlayer().sendMessage("§c[OpenHousing] Урон не может быть отрицательным!");
                            }
                            return;
                        }
                        ((Damageable) entity).damage(damage);
                    } catch (NumberFormatException e) {
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendMessage("§c[OpenHousing] Неверное значение урона: '" + value + "'. Используйте числа!");
                        }
                    }
                }
                break;
                
            case HEAL:
                if (entity instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity) entity;
                    try {
                        double healAmount = Double.parseDouble(value);
                        if (healAmount < 0) {
                            if (context.getPlayer() != null) {
                                context.getPlayer().sendMessage("§c[OpenHousing] Лечение не может быть отрицательным!");
                            }
                            return;
                        }
                        double newHealth = Math.min(living.getHealth() + healAmount, 
                                                  living.getAttribute(Attribute.MAX_HEALTH).getValue());
                        living.setHealth(newHealth);
                    } catch (NumberFormatException e) {
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendMessage("§c[OpenHousing] Неверное значение лечения: '" + value + "'. Используйте числа!");
                        }
                    }
                }
                break;
                
            case SET_HEALTH:
                if (entity instanceof LivingEntity) {
                    try {
                        double health = Double.parseDouble(value);
                        if (health < 0) {
                            if (context.getPlayer() != null) {
                                context.getPlayer().sendMessage("§c[OpenHousing] Здоровье не может быть отрицательным!");
                            }
                            return;
                        }
                        LivingEntity living = (LivingEntity) entity;
                        double maxHealth = living.getAttribute(Attribute.MAX_HEALTH).getValue();
                        living.setHealth(Math.min(health, maxHealth));
                    } catch (NumberFormatException e) {
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendMessage("§c[OpenHousing] Неверное значение здоровья: '" + value + "'. Используйте числа!");
                        }
                    }
                }
                break;
                
            case SET_MAX_HEALTH:
                if (entity instanceof LivingEntity) {
                    try {
                        double maxHealth = Double.parseDouble(value);
                        if (maxHealth <= 0) {
                            if (context.getPlayer() != null) {
                                context.getPlayer().sendMessage("§c[OpenHousing] Максимальное здоровье должно быть больше 0!");
                            }
                            return;
                        }
                        LivingEntity living = (LivingEntity) entity;
                        AttributeInstance health = living.getAttribute(Attribute.MAX_HEALTH);
                        if (health != null) {
                            health.setBaseValue(maxHealth);
                        }
                    } catch (NumberFormatException e) {
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendMessage("§c[OpenHousing] Неверное значение здоровья: '" + value + "'. Используйте числа!");
                        }
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
                try {
                    double power = Double.parseDouble(value);
                    if (power < 0 || power > 10) {
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendMessage("§c[OpenHousing] Сила запуска должна быть от 0 до 10!");
                        }
                        return;
                    }
                    entity.setVelocity(new Vector(0, power, 0));
                } catch (NumberFormatException e) {
                    if (context.getPlayer() != null) {
                        context.getPlayer().sendMessage("§c[OpenHousing] Неверная сила запуска: '" + value + "'. Используйте числа от 0 до 10!");
                    }
                }
                break;
                
            case SET_FIRE:
                try {
                    int fireTicks = Integer.parseInt(value);
                    if (fireTicks < 0) {
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendMessage("§c[OpenHousing] Время горения не может быть отрицательным!");
                        }
                        return;
                    }
                    entity.setFireTicks(fireTicks);
                } catch (NumberFormatException e) {
                    if (context.getPlayer() != null) {
                        context.getPlayer().sendMessage("§c[OpenHousing] Неверное время горения: '" + value + "'. Используйте целые числа!");
                    }
                }
                break;
                
            case EXTINGUISH:
                entity.setFireTicks(0);
                break;
                
            case SET_NAME:
                if (value != null && !value.trim().isEmpty()) {
                    entity.setCustomName(net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', value));
                } else {
                    entity.setCustomName(null);
                }
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
                
            case SET_PEACEFUL:
                if (entity instanceof Mob) {
                    Mob mob = (Mob) entity;
                    boolean peaceful = Boolean.parseBoolean(value);
                    
                    if (peaceful) {
                        // Успокаиваем моба
                        mob.setTarget(null); // Убираем цель
                        mob.setAware(false); // Отключаем ИИ временно
                        
                        // Убираем агрессивность для специфичных мобов
                        if (mob instanceof Wolf) {
                            ((Wolf) mob).setAngry(false);
                        } else if (mob instanceof org.bukkit.entity.PigZombie) {
                            ((org.bukkit.entity.PigZombie) mob).setAngry(false);
                        } else if (mob instanceof Enderman) {
                            ((Enderman) mob).setScreaming(false);
                        }
                        
                        // Добавляем эффект регенерации для успокоения
                        if (mob instanceof LivingEntity) {
                            ((LivingEntity) mob).addPotionEffect(
                                new PotionEffect(PotionEffectType.REGENERATION, 100, 0, true, false)
                            );
                        }
                        
                        // Включаем ИИ обратно через небольшую задержку
                        org.bukkit.Bukkit.getScheduler().runTaskLater(
                            ru.openhousing.OpenHousing.getInstance(), 
                            () -> mob.setAware(true), 
                            20L // 1 секунда
                        );
                        
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendMessage("§a[OpenHousing] Существо успокоено!");
                        }
                    } else {
                        // Возвращаем агрессивность
                        mob.setAware(true);
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendMessage("§a[OpenHousing] Существо больше не успокоено!");
                        }
                    }
                }
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
                
            case BREED:
                // Реализована логика размножения
                if (entity instanceof Animals) {
                    Animals animal = (Animals) entity;
                    if (context.getPlayer() != null) {
                        // Проверяем, есть ли рядом партнер для размножения
                        List<Entity> nearbyEntities = entity.getNearbyEntities(5, 5, 5);
                        Animals partner = null;
                        
                        for (Entity nearby : nearbyEntities) {
                            if (nearby instanceof Animals && nearby.getType() == entity.getType() && nearby != entity) {
                                Animals nearbyAnimal = (Animals) nearby;
                                if (nearbyAnimal.canBreed() && animal.canBreed()) {
                                    partner = nearbyAnimal;
                                    break;
                                }
                            }
                        }
                        
                        if (partner != null) {
                            // Устанавливаем режим любви для обоих животных
                            animal.setLoveModeTicks(600); // 30 секунд
                            partner.setLoveModeTicks(600);
                            
                            if (context.getPlayer() != null) {
                                context.getPlayer().sendMessage("§a[OpenHousing] Животные готовы к размножению!");
                            }
                        } else {
                            // Если партнера нет, просто устанавливаем режим любви
                            animal.setLoveModeTicks(600);
                            if (context.getPlayer() != null) {
                                context.getPlayer().sendMessage("§a[OpenHousing] Животное готово к размножению!");
                            }
                        }
                    }
                }
                break;
                
            case SHEAR:
                // Реализована логика стрижки
                if (entity instanceof Sheep) {
                    Sheep sheep = (Sheep) entity;
                    if (Boolean.parseBoolean(value)) {
                        sheep.setSheared(true);
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendMessage("§a[OpenHousing] Овца пострижена!");
                        }
                    } else {
                        sheep.setSheared(false);
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendMessage("§a[OpenHousing] Шерсть овцы восстановлена!");
                        }
                    }
                } else if (entity instanceof MushroomCow) {
                    MushroomCow mooshroom = (MushroomCow) entity;
                    if (Boolean.parseBoolean(value)) {
                        // Превращаем в обычную корову
                        mooshroom.setAware(false);
                        mooshroom.getWorld().spawnEntity(mooshroom.getLocation(), EntityType.COW);
                        mooshroom.remove();
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendMessage("§a[OpenHousing] Мухомор пострижен и превращен в корову!");
                        }
                    }
                } else if (context.getPlayer() != null) {
                    context.getPlayer().sendMessage("§c[OpenHousing] Это существо нельзя стричь!");
                }
                break;
                
            case MILK:
                // Реализована логика доения
                if (entity instanceof Cow || entity instanceof Goat) {
                    if (context.getPlayer() != null) {
                        Player player = context.getPlayer();
                        
                        // Проверяем, есть ли ведро в инвентаре
                        if (player.getInventory().containsAtLeast(new ItemStack(Material.BUCKET), 1)) {
                            // Убираем ведро и даем молоко
                            player.getInventory().removeItem(new ItemStack(Material.BUCKET, 1));
                            player.getInventory().addItem(new ItemStack(Material.MILK_BUCKET, 1));
                            
                            player.sendMessage("§a[OpenHousing] Вы подоили животное! Получено ведро молока.");
                        } else {
                            player.sendMessage("§c[OpenHousing] Для доения нужно ведро в инвентаре!");
                        }
                    }
                } else if (context.getPlayer() != null) {
                    context.getPlayer().sendMessage("§c[OpenHousing] Это существо нельзя доить!");
                }
                break;
                
            case LEASH:
                // Реализована логика поводка
                if (entity instanceof LivingEntity && context.getPlayer() != null) {
                    LivingEntity living = (LivingEntity) entity;
                    Player player = context.getPlayer();
                    
                    // Проверяем, есть ли поводок в инвентаре
                    if (player.getInventory().containsAtLeast(new ItemStack(Material.LEAD), 1)) {
                        // Убираем поводок и привязываем существо
                        player.getInventory().removeItem(new ItemStack(Material.LEAD, 1));
                        
                        // Создаем поводок и привязываем к игроку
                        if (living.isLeashed()) {
                            living.setLeashHolder(null);
                        }
                        
                        // Привязываем к игроку
                        living.setLeashHolder(player);
                        
                        player.sendMessage("§a[OpenHousing] Существо привязано к вам на поводок!");
                    } else {
                        player.sendMessage("§c[OpenHousing] Для привязки нужен поводок в инвентаре!");
                    }
                }
                break;
                
            case UNLEASH:
                // Реализована логика отпускания с поводка
                if (entity instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity) entity;
                    if (living.isLeashed()) {
                        living.setLeashHolder(null);
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendMessage("§a[OpenHousing] Существо отпущено с поводка!");
                        }
                    }
                }
                break;
                
            case MOUNT_PLAYER:
                // Реализована логика посадки игрока
                if (entity instanceof Vehicle && context.getPlayer() != null) {
                    Vehicle vehicle = (Vehicle) entity;
                    Player player = context.getPlayer();
                    
                    if (vehicle.getPassengers().isEmpty()) {
                        vehicle.addPassenger(player);
                        player.sendMessage("§a[OpenHousing] Вы сели на транспорт!");
                    } else {
                        player.sendMessage("§c[OpenHousing] Транспорт уже занят!");
                    }
                }
                break;
                
            case DISMOUNT_PLAYER:
                // Реализована логика снятия игрока
                if (entity instanceof Vehicle && context.getPlayer() != null) {
                    Vehicle vehicle = (Vehicle) entity;
                    Player player = context.getPlayer();
                    
                    if (vehicle.getPassengers().contains(player)) {
                        vehicle.removePassenger(player);
                        player.sendMessage("§a[OpenHousing] Вы слезли с транспорта!");
                    }
                }
                break;
                
            case DROP_ITEM:
                // Реализована логика выбрасывания предмета
                if (entity instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity) entity;
                    
                    try {
                        // Парсим предмет из строки (формат: MATERIAL:количество:имя)
                        String[] itemParts = value.split(":");
                        if (itemParts.length > 0) {
                            Material material = Material.valueOf(itemParts[0].toUpperCase());
                            int amount = 1;
                            String customName = "";
                            
                            if (itemParts.length > 1) {
                                try {
                                    amount = Integer.parseInt(itemParts[1]);
                                } catch (NumberFormatException e) {
                                    if (context.getPlayer() != null) {
                                        context.getPlayer().sendMessage("§c[OpenHousing] Неверное количество предметов: '" + itemParts[1] + "'. Используется 1");
                                    }
                                }
                            }
                            
                            if (itemParts.length > 2) {
                                customName = itemParts[2];
                            }
                            
                            ItemStack item = new ItemStack(material, amount);
                            
                            if (!customName.isEmpty()) {
                                ItemMeta meta = item.getItemMeta();
                                if (meta != null) {
                                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', customName));
                                    item.setItemMeta(meta);
                                }
                            }
                            
                            // Выбрасываем предмет в мире
                            living.getWorld().dropItemNaturally(living.getLocation(), item);
                            
                            if (context.getPlayer() != null) {
                                context.getPlayer().sendMessage("§a[OpenHousing] Предмет выброшен: " + material.name());
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendMessage("§c[OpenHousing] Неверный тип предмета: '" + value + "'. Примеры: DIAMOND:1:Алмаз, APPLE:5");
                        }
                    }
                }
                break;
                
            case GIVE_ITEM:
                // Реализована логика выдачи предмета
                if (entity instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity) entity;
                    
                    try {
                        // Парсим предмет из строки (формат: MATERIAL:количество:имя)
                        String[] itemParts = value.split(":");
                        if (itemParts.length > 0) {
                            Material material = Material.valueOf(itemParts[0].toUpperCase());
                            int amount = 1;
                            String customName = "";
                            
                            if (itemParts.length > 1) {
                                try {
                                    amount = Integer.parseInt(itemParts[1]);
                                } catch (NumberFormatException e) {
                                    if (context.getPlayer() != null) {
                                        context.getPlayer().sendMessage("§c[OpenHousing] Неверное количество предметов: '" + itemParts[1] + "'. Используется 1");
                                    }
                                }
                            }
                            
                            if (itemParts.length > 2) {
                                customName = itemParts[2];
                            }
                            
                            ItemStack item = new ItemStack(material, amount);
                            
                            if (!customName.isEmpty()) {
                                ItemMeta meta = item.getItemMeta();
                                if (meta != null) {
                                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', customName));
                                    item.setItemMeta(meta);
                                }
                            }
                            
                            // Если это игрок, даем в инвентарь
                            if (living instanceof Player) {
                                Player player = (Player) living;
                                player.getInventory().addItem(item);
                                player.sendMessage("§a[OpenHousing] Получен предмет: " + material.name());
                            } else {
                                // Если это моб, выбрасываем рядом
                                living.getWorld().dropItemNaturally(living.getLocation(), item);
                            }
                            
                            if (context.getPlayer() != null) {
                                context.getPlayer().sendMessage("§a[OpenHousing] Предмет выдан: " + material.name());
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendMessage("§c[OpenHousing] Неверный тип предмета: '" + value + "'. Примеры: DIAMOND:1:Алмаз, APPLE:5");
                        }
                    }
                }
                break;
                
            case SET_EQUIPMENT:
                // Реализована логика установки экипировки
                if (entity instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity) entity;
                    
                    try {
                        // Парсим экипировку из строки (формат: SLOT:MATERIAL:имя)
                        String[] equipParts = value.split(":");
                        if (equipParts.length >= 2) {
                            EquipmentSlot slot = EquipmentSlot.valueOf(equipParts[0].toUpperCase());
                            Material material = Material.valueOf(equipParts[1].toUpperCase());
                            String customName = equipParts.length > 2 ? equipParts[2] : "";
                            
                            ItemStack item = new ItemStack(material, 1);
                            
                            if (!customName.isEmpty()) {
                                ItemMeta meta = item.getItemMeta();
                                if (meta != null) {
                                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', customName));
                                    item.setItemMeta(meta);
                                }
                            }
                            
                            // Устанавливаем экипировку
                            living.getEquipment().setItem(slot, item);
                            
                            if (context.getPlayer() != null) {
                                context.getPlayer().sendMessage("§a[OpenHousing] Экипировка установлена: " + slot.name() + " - " + material.name());
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendMessage("§c[OpenHousing] Неверные параметры экипировки: '" + value + "'. Примеры: HEAD:DIAMOND_HELMET:Шлем, MAINHAND:IRON_SWORD:Меч");
                        }
                    }
                }
                break;
                
            case FOLLOW_PLAYER:
                // Реализована логика следования за игроком
                if (entity instanceof Tameable && context.getPlayer() != null) {
                    Tameable tameable = (Tameable) entity;
                    Player player = context.getPlayer();
                    
                    if (tameable.isTamed() && tameable.getOwner().equals(player)) {
                        // Используем PathfinderGoal для следования
                        if (entity instanceof Mob) {
                            Mob mob = (Mob) entity;
                            // Устанавливаем цель следования
                            mob.setTarget(player);
                            
                            player.sendMessage("§a[OpenHousing] Питомец следует за вами!");
                        }
                    } else {
                        player.sendMessage("§c[OpenHousing] Существо не приручено вами!");
                    }
                }
                break;
                
            case STOP_FOLLOWING:
                // Реализована логика прекращения следования
                if (entity instanceof Tameable) {
                    Tameable tameable = (Tameable) entity;
                    
                    if (tameable.isTamed()) {
                        if (entity instanceof Mob) {
                            Mob mob = (Mob) entity;
                            // Убираем цель
                            mob.setTarget(null);
                            
                            if (context.getPlayer() != null) {
                                context.getPlayer().sendMessage("§a[OpenHousing] Питомец перестал следовать!");
                            }
                        }
                    }
                }
                break;
                
            case FACE_PLAYER:
                // Логика поворота к игроку уже реализована
                if (context.getPlayer() != null) {
                    Location playerLoc = context.getPlayer().getLocation();
                    Location entityLoc = entity.getLocation();
                    Vector direction = playerLoc.subtract(entityLoc).toVector();
                    float yaw = (float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
                    float pitch = (float) Math.toDegrees(Math.asin(-direction.getY() / direction.length()));
                    entity.setRotation(yaw, pitch);
                    
                    if (context.getPlayer() != null) {
                        context.getPlayer().sendMessage("§a[OpenHousing] Существо повернулось к вам!");
                    }
                }
                break;
                
            case FACE_LOCATION:
                // Логика поворота к точке уже реализована
                Location targetLoc = parseLocationString(value, entity.getWorld());
                if (targetLoc != null) {
                    Location entityLoc = entity.getLocation();
                    Vector direction = targetLoc.subtract(entityLoc).toVector();
                    float yaw = (float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
                    float pitch = (float) Math.toDegrees(Math.asin(-direction.getY() / direction.length()));
                    entity.setRotation(yaw, pitch);
                    
                    if (context.getPlayer() != null) {
                        context.getPlayer().sendMessage("§a[OpenHousing] Существо повернулось к указанной точке!");
                    }
                } else if (context.getPlayer() != null) {
                    context.getPlayer().sendMessage("§c[OpenHousing] Неверная локация для поворота: '" + value + "'");
                }
                break;
                
            case FREEZE:
                // Реализована логика заморозки
                if (entity instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity) entity;
                    living.setFreezeTicks(Integer.MAX_VALUE);
                    
                    if (context.getPlayer() != null) {
                        context.getPlayer().sendMessage("§a[OpenHousing] Существо заморожено!");
                    }
                }
                break;
                
            case UNFREEZE:
                // Реализована логика разморозки
                if (entity instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity) entity;
                    living.setFreezeTicks(0);
                    
                    if (context.getPlayer() != null) {
                        context.getPlayer().sendMessage("§a[OpenHousing] Существо разморожено!");
                    }
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
            context.getPlayer().sendMessage("§c[OpenHousing] Неверный тип существа: '" + entityType + "'. Примеры: ZOMBIE, SKELETON, CREEPER, VILLAGER");
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
                
                // Проверяем разумность значений
                if (Math.abs(x) > 10.0 || Math.abs(y) > 10.0 || Math.abs(z) > 10.0) {
                    if (entity instanceof Player) {
                        ((Player) entity).sendMessage("§c[OpenHousing] Слишком большая скорость: " + x + "," + y + "," + z + ". Максимум: ±10.0");
                    }
                    return;
                }
                
                entity.setVelocity(new Vector(x, y, z));
            } catch (NumberFormatException e) {
                // Игнорируем ошибку
                if (entity instanceof Player) {
                    ((Player) entity).sendMessage("§c[OpenHousing] Неверные значения скорости: '" + velocityString + "'. Формат: x,y,z (числа)");
                }
            }
        } else {
            if (entity instanceof Player) {
                ((Player) entity).sendMessage("§c[OpenHousing] Неверный формат скорости: '" + velocityString + "'. Ожидается: x,y,z");
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
                if (force < 0.1 || force > 10.0) {
                    if (entity instanceof Player) {
                        ((Player) entity).sendMessage("§c[OpenHousing] Сила толчка должна быть от 0.1 до 10.0. Используется значение по умолчанию: 1.0");
                    }
                    force = 1.0;
                }
            } catch (NumberFormatException e) {
                force = 1.0;
                if (entity instanceof Player) {
                    ((Player) entity).sendMessage("§c[OpenHousing] Неверная сила толчка: '" + parts[1] + "'. Используется значение по умолчанию: 1.0");
                }
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
            default:
                if (entity instanceof Player) {
                    ((Player) entity).sendMessage("§c[OpenHousing] Неверное направление толчка: '" + parts[0] + "'. Доступные: forward, backward, up, down, left, right, player");
                }
                return;
        }
        
        entity.setVelocity(pushVector);
    }
    
    /**
     * Добавление эффекта зелья
     */
    private void addPotionEffect(Entity entity, String effectName, String duration, String amplifier) {
        if (!(entity instanceof LivingEntity)) return;
        
        PotionEffectType effectType = PotionEffectType.getByName(effectName.toUpperCase());
        if (effectType == null) {
            if (entity instanceof Player) {
                ((Player) entity).sendMessage("§c[OpenHousing] Неверный тип эффекта: '" + effectName + "'. Примеры: SPEED, JUMP, STRENGTH, INVISIBILITY");
            }
            return;
        }
        
        int dur = 200; // 10 секунд по умолчанию
        int amp = 0;
        
        try {
            if (!duration.isEmpty()) {
                dur = Integer.parseInt(duration) * 20; // Конвертируем секунды в тики
                if (dur < 20 || dur > 24000) { // От 1 секунды до 20 минут
                    if (entity instanceof Player) {
                        ((Player) entity).sendMessage("§c[OpenHousing] Длительность эффекта должна быть от 1 до 1200 секунд. Используется значение по умолчанию: 10 сек");
                    }
                    dur = 200;
                }
            }
            if (!amplifier.isEmpty()) {
                amp = Integer.parseInt(amplifier);
                if (amp < 0 || amp > 255) {
                    if (entity instanceof Player) {
                        ((Player) entity).sendMessage("§c[OpenHousing] Усиление эффекта должно быть от 0 до 255. Используется значение по умолчанию: 0");
                    }
                    amp = 0;
                }
            }
        } catch (NumberFormatException e) {
            // Используем значения по умолчанию
            if (entity instanceof Player) {
                ((Player) entity).sendMessage("§c[OpenHousing] Неверные значения для эффекта. Используются значения по умолчанию: длительность 10 сек, усиление 0");
            }
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
        } else if (entity instanceof org.bukkit.entity.PigZombie) {
            if (angry) {
                ((org.bukkit.entity.PigZombie) entity).setAngry(true);
                ((org.bukkit.entity.PigZombie) entity).setAnger(400); // 20 секунд
            } else {
                ((org.bukkit.entity.PigZombie) entity).setAngry(false);
            }
        } else {
            // Для других существ отправляем сообщение
            if (entity instanceof Player) {
                ((Player) entity).sendMessage("§c[OpenHousing] Установка агрессивности доступна только для волков и зомби-свиней");
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
            if (explosionPower < 0.1f || explosionPower > 10.0f) {
                if (entity instanceof Player) {
                    ((Player) entity).sendMessage("§c[OpenHousing] Сила взрыва должна быть от 0.1 до 10.0. Используется значение по умолчанию: 4.0");
                }
                explosionPower = 4.0f;
            }
        } catch (NumberFormatException e) {
            // Используем значение по умолчанию
            if (entity instanceof Player) {
                ((Player) entity).sendMessage("§c[OpenHousing] Неверная сила взрыва: '" + power + "'. Используется значение по умолчанию: 4.0");
            }
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
                if (vol < 0.0f || vol > 10.0f) {
                    if (entity instanceof Player) {
                        ((Player) entity).sendMessage("§c[OpenHousing] Громкость звука должна быть от 0.0 до 10.0. Используется значение по умолчанию: 1.0");
                    }
                    vol = 1.0f;
                }
            } catch (NumberFormatException e) {
                // Используем значение по умолчанию
                if (entity instanceof Player) {
                    ((Player) entity).sendMessage("§c[OpenHousing] Неверная громкость звука: '" + volume + "'. Используется значение по умолчанию: 1.0");
                }
            }
            
            entity.getWorld().playSound(entity.getLocation(), sound, vol, 1.0f);
        } catch (IllegalArgumentException e) {
            // Неверное имя звука
            if (entity instanceof Player) {
                ((Player) entity).sendMessage("§c[OpenHousing] Неверное имя звука: '" + soundName + "'. Примеры: BLOCK_STONE_BREAK, ENTITY_PLAYER_LEVELUP, MUSIC_DISC_13");
            }
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
                if (particleCount < 1 || particleCount > 1000) {
                    if (entity instanceof Player) {
                        ((Player) entity).sendMessage("§c[OpenHousing] Количество частиц должно быть от 1 до 1000. Используется значение по умолчанию: 10");
                    }
                    particleCount = 10;
                }
            } catch (NumberFormatException e) {
                // Используем значение по умолчанию
                if (entity instanceof Player) {
                    ((Player) entity).sendMessage("§c[OpenHousing] Неверное количество частиц: '" + count + "'. Используется значение по умолчанию: 10");
                }
            }
            
            try {
                offsetValue = Double.parseDouble(offset);
                if (offsetValue < 0.1 || offsetValue > 5.0) {
                    if (entity instanceof Player) {
                        ((Player) entity).sendMessage("§c[OpenHousing] Смещение частиц должно быть от 0.1 до 5.0. Используется значение по умолчанию: 0.5");
                    }
                    offsetValue = 0.5;
                }
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
     * Поиск ближайшего существа (оптимизированный)
     */
    private Entity findNearestEntity(ExecutionContext context) {
        if (context.getPlayer() == null) return null;
        
        Location playerLoc = context.getPlayer().getLocation();
        World world = playerLoc.getWorld();
        
        // Оптимизированный поиск с ограниченным радиусом
        Entity nearest = null;
        double nearestDistanceSquared = 100.0; // 10 блоков в квадрате для оптимизации
        
        // Используем getNearbyEntities для более эффективного поиска
        for (Entity entity : world.getNearbyEntities(playerLoc, 10, 10, 10)) {
            if (entity instanceof Player) continue;
            
            // Используем distanceSquared для избежания вычисления квадратного корня
            double distanceSquared = entity.getLocation().distanceSquared(playerLoc);
            if (distanceSquared < nearestDistanceSquared) {
                nearest = entity;
                nearestDistanceSquared = distanceSquared;
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
