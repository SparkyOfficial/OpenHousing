package ru.openhousing.coding.blocks.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок событий существ
 */
public class EntityEventBlock extends CodeBlock {
    
    public enum EntityEventType {
        SPAWN("Спавн существа", "Когда существо появляется в мире"),
        DEATH("Смерть существа", "Когда существо умирает"),
        DAMAGE("Получение урона", "Когда существо получает урон"),
        DAMAGE_ENTITY("Нанесение урона", "Когда существо наносит урон другому"),
        TARGET("Выбор цели", "Когда существо выбирает цель для атаки"),
        INTERACT("Взаимодействие", "Когда игрок взаимодействует с существом"),
        TAME("Приручение", "Когда существо приручается"),
        BREED("Размножение", "Когда существа размножаются"),
        EXPLODE("Взрыв", "Когда существо взрывается"),
        TRANSFORM("Превращение", "Когда существо превращается в другое"),
        TELEPORT("Телепортация", "Когда существо телепортируется"),
        PICKUP_ITEM("Подбор предмета", "Когда существо подбирает предмет"),
        DROP_ITEM("Выпадение предмета", "Когда из существа выпадает предмет"),
        CHANGE_BLOCK("Изменение блока", "Когда существо меняет блок"),
        ENTER_PORTAL("Вход в портал", "Когда существо входит в портал"),
        REGAIN_HEALTH("Восстановление здоровья", "Когда существо восстанавливает здоровье"),
        POTION_EFFECT("Эффект зелья", "Когда на существо накладывается эффект"),
        COMBUSTION("Возгорание", "Когда существо загорается"),
        FREEZE("Замерзание", "Когда существо замерзает"),
        MOUNT("Сесть верхом", "Когда игрок садится на существо"),
        DISMOUNT("Слезть", "Когда игрок слезает с существа");
        
        private final String displayName;
        private final String description;
        
        EntityEventType(String displayName, String description) {
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
    
    public EntityEventBlock() {
        super(BlockType.ENTITY_EVENT);
        setParameter("eventType", EntityEventType.SPAWN);
        setParameter("entityType", ""); // Тип существа (или "любое")
        setParameter("conditions", ""); // Дополнительные условия
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        // События выполняются автоматически при регистрации
        return executeChildren(context);
    }
    
    @Override
    public boolean validate() {
        return getParameter("eventType") != null;
    }
    
    @Override
    public List<String> getDescription() {
        Object eventTypeParam = getParameter("eventType");
        EntityEventType eventType = null;
        
        if (eventTypeParam instanceof EntityEventType) {
            eventType = (EntityEventType) eventTypeParam;
        } else if (eventTypeParam instanceof String) {
            try {
                eventType = EntityEventType.valueOf((String) eventTypeParam);
            } catch (IllegalArgumentException e) {
                // Игнорируем неверные значения
            }
        }
        
        String entityType = (String) getParameter("entityType");
        
        return Arrays.asList(
            "§6Событие существа",
            "§7Тип события: §f" + (eventType != null ? eventType.getDisplayName() : "Не выбран"),
            "§7Тип существа: §f" + (entityType != null && !entityType.isEmpty() ? entityType : "Любое"),
            "§7Описание: §f" + (eventType != null ? eventType.getDescription() : ""),
            "",
            "§8Дочерних блоков: " + childBlocks.size()
        );
    }
    
    /**
     * Проверка соответствия события
     */
    public boolean matchesEvent(Class<?> eventClass, Object... params) {
        Object eventTypeParam = getParameter("eventType");
        EntityEventType eventType = null;
        
        if (eventTypeParam instanceof EntityEventType) {
            eventType = (EntityEventType) eventTypeParam;
        } else if (eventTypeParam instanceof String) {
            try {
                eventType = EntityEventType.valueOf((String) eventTypeParam);
            } catch (IllegalArgumentException e) {
                // Игнорируем неверные значения
                return false;
            }
        }
        
        if (eventType == null) return false;
        
        switch (eventType) {
            case SPAWN:
                return eventClass == EntitySpawnEvent.class || 
                       eventClass == CreatureSpawnEvent.class;
            case DEATH:
                return eventClass == EntityDeathEvent.class;
            case DAMAGE:
                return eventClass == EntityDamageEvent.class;
            case DAMAGE_ENTITY:
                return eventClass == EntityDamageByEntityEvent.class;
            case TARGET:
                return eventClass == EntityTargetEvent.class;
            case INTERACT:
                return eventClass == PlayerInteractEntityEvent.class;
            case TAME:
                return eventClass == EntityTameEvent.class;
            case BREED:
                return eventClass == EntityBreedEvent.class;
            case EXPLODE:
                return eventClass == EntityExplodeEvent.class;
            case TRANSFORM:
                return eventClass == EntityTransformEvent.class;
            case TELEPORT:
                return eventClass == EntityTeleportEvent.class;
            case PICKUP_ITEM:
                return eventClass == EntityPickupItemEvent.class;
            case DROP_ITEM:
                return eventClass == EntityDropItemEvent.class;
            case CHANGE_BLOCK:
                return eventClass == EntityChangeBlockEvent.class;
            case ENTER_PORTAL:
                return eventClass == EntityPortalEvent.class;
            case REGAIN_HEALTH:
                return eventClass == EntityRegainHealthEvent.class;
            case POTION_EFFECT:
                return eventClass == EntityPotionEffectEvent.class;
            case COMBUSTION:
                return eventClass == EntityCombustEvent.class;
            case MOUNT:
                // EntityMountEvent не существует в стандартном API
                return false;
            case DISMOUNT:
                // EntityDismountEvent не существует в стандартном API
                return false;
            default:
                return false;
        }
    }
    
    /**
     * Создание контекста выполнения из события
     */
    public ExecutionContext createContextFromEvent(Player player, Object event) {
        ExecutionContext context = new ExecutionContext(player);
        
        Object eventTypeParam = getParameter("eventType");
        EntityEventType eventType = null;
        
        if (eventTypeParam instanceof EntityEventType) {
            eventType = (EntityEventType) eventTypeParam;
        } else if (eventTypeParam instanceof String) {
            try {
                eventType = EntityEventType.valueOf((String) eventTypeParam);
            } catch (IllegalArgumentException e) {
                // Игнорируем неверные значения
                return context;
            }
        }
        
        if (eventType != null && event instanceof EntityEvent) {
            EntityEvent entityEvent = (EntityEvent) event;
            Entity entity = entityEvent.getEntity();
            
            // Добавляем информацию о существе
            context.setVariable("entity_type", entity.getType().name());
            context.setVariable("entity_name", entity.getName());
            context.setVariable("entity_world", entity.getWorld().getName());
            context.setVariable("entity_x", entity.getLocation().getX());
            context.setVariable("entity_y", entity.getLocation().getY());
            context.setVariable("entity_z", entity.getLocation().getZ());
            context.setVariable("entity_health", getEntityHealth(entity));
            context.setVariable("entity_max_health", getEntityMaxHealth(entity));
            
            // Специфичные переменные для разных событий
            switch (eventType) {
                case DAMAGE:
                    if (event instanceof EntityDamageEvent) {
                        EntityDamageEvent damageEvent = (EntityDamageEvent) event;
                        context.setVariable("damage_amount", damageEvent.getDamage());
                        context.setVariable("damage_cause", damageEvent.getCause().name());
                        context.setVariable("damage_final", damageEvent.getFinalDamage());
                    }
                    break;
                    
                case DAMAGE_ENTITY:
                    if (event instanceof EntityDamageByEntityEvent) {
                        EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
                        Entity damager = damageEvent.getDamager();
                        context.setVariable("damager_type", damager.getType().name());
                        context.setVariable("damager_name", damager.getName());
                        context.setVariable("damage_amount", damageEvent.getDamage());
                    }
                    break;
                    
                case TARGET:
                    if (event instanceof EntityTargetEvent) {
                        EntityTargetEvent targetEvent = (EntityTargetEvent) event;
                        Entity target = targetEvent.getTarget();
                        if (target != null) {
                            context.setVariable("target_type", target.getType().name());
                            context.setVariable("target_name", target.getName());
                        }
                        context.setVariable("target_reason", targetEvent.getReason().name());
                    }
                    break;
                    
                case DEATH:
                    if (event instanceof EntityDeathEvent) {
                        EntityDeathEvent deathEvent = (EntityDeathEvent) event;
                        context.setVariable("death_drops", deathEvent.getDrops().size());
                        context.setVariable("death_exp", deathEvent.getDroppedExp());
                    }
                    break;
                    
                case EXPLODE:
                    if (event instanceof EntityExplodeEvent) {
                        EntityExplodeEvent explodeEvent = (EntityExplodeEvent) event;
                        context.setVariable("explosion_power", explodeEvent.getYield());
                        context.setVariable("blocks_affected", explodeEvent.blockList().size());
                    }
                    break;
                    
                case SPAWN:
                    if (event instanceof CreatureSpawnEvent) {
                        CreatureSpawnEvent spawnEvent = (CreatureSpawnEvent) event;
                        context.setVariable("spawn_reason", spawnEvent.getSpawnReason().name());
                    }
                    break;
            }
        }
        
        return context;
    }
    
    /**
     * Получение здоровья существа
     */
    private double getEntityHealth(Entity entity) {
        if (entity instanceof org.bukkit.entity.Damageable) {
            return ((org.bukkit.entity.Damageable) entity).getHealth();
        }
        return 0;
    }
    
    /**
     * Получение максимального здоровья существа
     */
    private double getEntityMaxHealth(Entity entity) {
        if (entity instanceof org.bukkit.attribute.Attributable) {
            org.bukkit.attribute.Attributable attributable = (org.bukkit.attribute.Attributable) entity;
            org.bukkit.attribute.AttributeInstance health = attributable.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
            if (health != null) {
                return health.getValue();
            }
        }
        return 0;
    }
}
