package ru.openhousing.coding.blocks.events;

import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок событий игрока
 */
public class PlayerEventBlock extends CodeBlock {
    
    public enum PlayerEventType {
        JOIN("Подключение", "Когда игрок заходит на сервер"),
        QUIT("Отключение", "Когда игрок покидает сервер"),
        CHAT("Сообщение в чат", "Когда игрок пишет в чат"),
        MOVE("Движение", "Когда игрок двигается"),
        INTERACT("Взаимодействие", "Когда игрок взаимодействует с блоком"),
        INTERACT_ENTITY("Взаимодействие с существом", "Когда игрок взаимодействует с существом"),
        DAMAGE("Получение урона", "Когда игрок получает урон"),
        DEATH("Смерть", "Когда игрок умирает"),
        RESPAWN("Возрождение", "Когда игрок возрождается"),
        DROP_ITEM("Выбрасывание предмета", "Когда игрок выбрасывает предмет"),
        PICKUP_ITEM("Подбор предмета", "Когда игрок подбирает предмет"),
        INVENTORY_CLICK("Клик в инвентаре", "Когда игрок кликает в инвентаре"),
        COMMAND("Команда", "Когда игрок использует команду"),
        TELEPORT("Телепортация", "Когда игрок телепортируется"),
        WORLD_CHANGE("Смена мира", "Когда игрок меняет мир"),
        SNEAK("Присесть", "Когда игрок приседает"),
        JUMP("Прыжок", "Когда игрок прыгает"),
        LEFT_CLICK("Левый клик", "Когда игрок кликает левой кнопкой"),
        RIGHT_CLICK("Правый клик", "Когда игрок кликает правой кнопкой"),
        BREAK_BLOCK("Разрушение блока", "Когда игрок ломает блок"),
        PLACE_BLOCK("Установка блока", "Когда игрок ставит блок"),
        CHANGE_GAMEMODE("Смена режима игры", "Когда игрок меняет режим игры"),
        LEVEL_CHANGE("Изменение уровня", "Когда игрок получает/теряет уровень"),
        FOOD_CHANGE("Изменение голода", "Когда меняется уровень голода игрока"),
        ITEM_CONSUME("Потребление предмета", "Когда игрок съедает/выпивает предмет"),
        FISH("Рыбалка", "Когда игрок ловит рыбу"),
        ENCHANT_ITEM("Зачарование предмета", "Когда игрок зачаровывает предмет"),
        ANVIL_USE("Использование наковальни", "Когда игрок использует наковальню"),
        CRAFT_ITEM("Создание предмета", "Когда игрок создает предмет"),
        PORTAL_USE("Использование портала", "Когда игрок использует портал");
        
        private final String displayName;
        private final String description;
        
        PlayerEventType(String displayName, String description) {
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
    
    public PlayerEventBlock() {
        super(BlockType.PLAYER_EVENT);
        setParameter("eventType", PlayerEventType.JOIN);
        setParameter("conditions", ""); // Дополнительные условия
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        // События выполняются автоматически при регистрации
        // Этот метод вызывается когда событие действительно происходит
        return executeChildren(context);
    }
    
    @Override
    public boolean validate() {
        return getParameter(ru.openhousing.coding.constants.BlockParams.EVENT_TYPE) != null;
    }
    
    @Override
    public List<String> getDescription() {
        Object eventTypeParam = getParameter(ru.openhousing.coding.constants.BlockParams.EVENT_TYPE);
        PlayerEventType eventType = null;
        
        if (eventTypeParam instanceof PlayerEventType) {
            eventType = (PlayerEventType) eventTypeParam;
        } else if (eventTypeParam instanceof String) {
            try {
                eventType = PlayerEventType.valueOf((String) eventTypeParam);
            } catch (IllegalArgumentException e) {
                // Игнорируем неверные значения
            }
        }
        
        return Arrays.asList(
            "§6Событие игрока",
            "§7Тип: §f" + (eventType != null ? eventType.getDisplayName() : "Не выбран"),
            "§7Описание: §f" + (eventType != null ? eventType.getDescription() : ""),
            "",
            "§8Дочерних блоков: " + childBlocks.size()
        );
    }
    
    @Override
    public boolean matchesEvent(Object event) {
        if (!(event instanceof org.bukkit.event.player.PlayerEvent)) {
            return false;
        }
        
        Object eventTypeParam = getParameter(ru.openhousing.coding.constants.BlockParams.EVENT_TYPE);
        PlayerEventType eventType = null;
        
        if (eventTypeParam instanceof PlayerEventType) {
            eventType = (PlayerEventType) eventTypeParam;
        } else if (eventTypeParam instanceof String) {
            try {
                eventType = PlayerEventType.valueOf((String) eventTypeParam);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        
        if (eventType == null) return false;
        
        return matchesEventType(event, eventType);
    }
    
    @Override
    public ExecutionContext createContextFromEvent(Object event) {
        if (!(event instanceof org.bukkit.event.player.PlayerEvent)) {
            return null;
        }
        
        org.bukkit.event.player.PlayerEvent playerEvent = (org.bukkit.event.player.PlayerEvent) event;
        ExecutionContext context = new ExecutionContext(playerEvent.getPlayer());
        
        // Добавляем специфичные для события переменные
        addEventVariables(context, event);
        
        return context;
    }
    
    /**
     * Проверка соответствия типа события
     */
    private boolean matchesEventType(Object event, PlayerEventType eventType) {
        switch (eventType) {
            case JOIN:
                return event instanceof org.bukkit.event.player.PlayerJoinEvent;
            case QUIT:
                return event instanceof org.bukkit.event.player.PlayerQuitEvent;
            case CHAT:
                return event instanceof org.bukkit.event.player.AsyncPlayerChatEvent;
            case MOVE:
                return event instanceof org.bukkit.event.player.PlayerMoveEvent;
            case INTERACT:
                return event instanceof org.bukkit.event.player.PlayerInteractEvent;
            case DAMAGE:
                return event instanceof org.bukkit.event.entity.EntityDamageEvent && 
                       ((org.bukkit.event.entity.EntityDamageEvent) event).getEntity() instanceof org.bukkit.entity.Player;
            case DEATH:
                return event instanceof org.bukkit.event.entity.PlayerDeathEvent;
            case SNEAK:
                return event instanceof org.bukkit.event.player.PlayerToggleSneakEvent;
            case JUMP:
                return event instanceof org.bukkit.event.player.PlayerMoveEvent && 
                       isJumpEvent((org.bukkit.event.player.PlayerMoveEvent) event);
            default:
                return false;
        }
    }
    
    /**
     * Добавление переменных события в контекст
     */
    private void addEventVariables(ExecutionContext context, Object event) {
        if (event instanceof org.bukkit.event.player.PlayerJoinEvent) {
            org.bukkit.event.player.PlayerJoinEvent joinEvent = (org.bukkit.event.player.PlayerJoinEvent) event;
            context.setVariable("join_message", joinEvent.getJoinMessage());
            
        } else if (event instanceof org.bukkit.event.player.PlayerQuitEvent) {
            org.bukkit.event.player.PlayerQuitEvent quitEvent = (org.bukkit.event.player.PlayerQuitEvent) event;
            context.setVariable("quit_message", quitEvent.getQuitMessage());
            
        } else if (event instanceof org.bukkit.event.player.AsyncPlayerChatEvent) {
            org.bukkit.event.player.AsyncPlayerChatEvent chatEvent = (org.bukkit.event.player.AsyncPlayerChatEvent) event;
            context.setVariable("chat_message", chatEvent.getMessage());
            context.setVariable("chat_format", chatEvent.getFormat());
            
        } else if (event instanceof org.bukkit.event.player.PlayerMoveEvent) {
            org.bukkit.event.player.PlayerMoveEvent moveEvent = (org.bukkit.event.player.PlayerMoveEvent) event;
            context.setVariable("from_x", moveEvent.getFrom().getX());
            context.setVariable("from_y", moveEvent.getFrom().getY());
            context.setVariable("from_z", moveEvent.getFrom().getZ());
            context.setVariable("to_x", moveEvent.getTo().getX());
            context.setVariable("to_y", moveEvent.getTo().getY());
            context.setVariable("to_z", moveEvent.getTo().getZ());
        }
    }
    
    /**
     * Проверка, является ли движение прыжком
     */
    private boolean isJumpEvent(org.bukkit.event.player.PlayerMoveEvent moveEvent) {
        return moveEvent.getTo().getY() > moveEvent.getFrom().getY() + 0.1;
    }
    
    /**
     * Проверка соответствия события (старый метод для совместимости)
     */
    public boolean matchesEvent(Class<?> eventClass, Object... params) {
        Object eventTypeParam = getParameter(ru.openhousing.coding.constants.BlockParams.EVENT_TYPE);
        PlayerEventType eventType = null;
        
        if (eventTypeParam instanceof PlayerEventType) {
            eventType = (PlayerEventType) eventTypeParam;
        } else if (eventTypeParam instanceof String) {
            try {
                eventType = PlayerEventType.valueOf((String) eventTypeParam);
            } catch (IllegalArgumentException e) {
                // Игнорируем неверные значения
                return false;
            }
        }
        
        if (eventType == null) return false;
        
        switch (eventType) {
            case JOIN:
                return eventClass == PlayerJoinEvent.class;
            case QUIT:
                return eventClass == PlayerQuitEvent.class;
            case CHAT:
                return eventClass == AsyncPlayerChatEvent.class;
            case MOVE:
                return eventClass == PlayerMoveEvent.class;
            case INTERACT:
                return eventClass == PlayerInteractEvent.class;
            case INTERACT_ENTITY:
                return eventClass == PlayerInteractEntityEvent.class;
            case DAMAGE:
                return eventClass == org.bukkit.event.entity.EntityDamageEvent.class;
            case DEATH:
                return eventClass == PlayerDeathEvent.class;
            case RESPAWN:
                return eventClass == PlayerRespawnEvent.class;
            case DROP_ITEM:
                return eventClass == PlayerDropItemEvent.class;
            case PICKUP_ITEM:
                return eventClass == org.bukkit.event.player.PlayerPickupItemEvent.class;
            case INVENTORY_CLICK:
                return eventClass == org.bukkit.event.inventory.InventoryClickEvent.class;
            case COMMAND:
                return eventClass == PlayerCommandPreprocessEvent.class;
            case TELEPORT:
                return eventClass == PlayerTeleportEvent.class;
            case WORLD_CHANGE:
                return eventClass == PlayerChangedWorldEvent.class;
            case BREAK_BLOCK:
                return eventClass == org.bukkit.event.block.BlockBreakEvent.class;
            case PLACE_BLOCK:
                return eventClass == org.bukkit.event.block.BlockPlaceEvent.class;
            case SNEAK:
                return eventClass == PlayerToggleSneakEvent.class;
            case JUMP:
                // Jump событие можно отловить через PlayerMoveEvent с проверкой Y координаты
                return eventClass == PlayerMoveEvent.class;
            case LEFT_CLICK:
                return eventClass == PlayerInteractEvent.class;
            case RIGHT_CLICK:
                return eventClass == PlayerInteractEvent.class;
            case CHANGE_GAMEMODE:
                return eventClass == PlayerGameModeChangeEvent.class;
            case LEVEL_CHANGE:
                return eventClass == PlayerLevelChangeEvent.class;
            case FOOD_CHANGE:
                return eventClass == org.bukkit.event.entity.FoodLevelChangeEvent.class;
            case ITEM_CONSUME:
                return eventClass == PlayerItemConsumeEvent.class;
            case FISH:
                return eventClass == PlayerFishEvent.class;
            case ENCHANT_ITEM:
                return eventClass == org.bukkit.event.enchantment.EnchantItemEvent.class;
            case ANVIL_USE:
                return eventClass == org.bukkit.event.inventory.InventoryClickEvent.class;
            case CRAFT_ITEM:
                return eventClass == org.bukkit.event.inventory.CraftItemEvent.class;
            case PORTAL_USE:
                return eventClass == PlayerPortalEvent.class;
            default:
                return false;
        }
    }
    
    /**
     * Создание контекста выполнения из события
     */
    public ExecutionContext createContextFromEvent(Player player, Object event) {
        ExecutionContext context = new ExecutionContext(player);
        
        // Добавляем переменные в зависимости от типа события
        Object eventTypeParam = getParameter(ru.openhousing.coding.constants.BlockParams.EVENT_TYPE);
        PlayerEventType eventType = null;
        
        if (eventTypeParam instanceof PlayerEventType) {
            eventType = (PlayerEventType) eventTypeParam;
        } else if (eventTypeParam instanceof String) {
            try {
                eventType = PlayerEventType.valueOf((String) eventTypeParam);
            } catch (IllegalArgumentException e) {
                // Игнорируем неверные значения
                return context;
            }
        }
        
        if (eventType != null) {
            switch (eventType) {
                case CHAT:
                    if (event instanceof AsyncPlayerChatEvent) {
                        context.setVariable("message", ((AsyncPlayerChatEvent) event).getMessage());
                    }
                    break;
                case DAMAGE:
                    if (event instanceof org.bukkit.event.entity.EntityDamageEvent) {
                        context.setVariable("damage", ((org.bukkit.event.entity.EntityDamageEvent) event).getDamage());
                        context.setVariable("cause", ((org.bukkit.event.entity.EntityDamageEvent) event).getCause().name());
                    }
                    break;
                case COMMAND:
                    if (event instanceof PlayerCommandPreprocessEvent) {
                        context.setVariable("command", ((PlayerCommandPreprocessEvent) event).getMessage());
                    }
                    break;
                case INTERACT:
                    if (event instanceof PlayerInteractEvent) {
                        context.setVariable("action", ((PlayerInteractEvent) event).getAction().name());
                        if (((PlayerInteractEvent) event).getClickedBlock() != null) {
                            context.setVariable("block", ((PlayerInteractEvent) event).getClickedBlock());
                        }
                    }
                    break;
                case INTERACT_ENTITY:
                    if (event instanceof PlayerInteractEntityEvent) {
                        context.setVariable("entity", ((PlayerInteractEntityEvent) event).getRightClicked());
                    }
                    break;
                case SNEAK:
                    if (event instanceof PlayerToggleSneakEvent) {
                        PlayerToggleSneakEvent sneakEvent = (PlayerToggleSneakEvent) event;
                        context.setVariable("is_sneaking", sneakEvent.isSneaking());
                    }
                    break;
                case JUMP:
                    if (event instanceof PlayerMoveEvent) {
                        PlayerMoveEvent moveEvent = (PlayerMoveEvent) event;
                        // Проверяем, что игрок прыгнул (Y координата увеличилась)
                        if (moveEvent.getTo() != null && moveEvent.getFrom() != null) {
                            double yDiff = moveEvent.getTo().getY() - moveEvent.getFrom().getY();
                            if (yDiff > 0.4) {
                                context.setVariable("jump_height", yDiff);
                            }
                        }
                    }
                    break;
                case LEFT_CLICK:
                    if (event instanceof PlayerInteractEvent) {
                        PlayerInteractEvent interactEvent = (PlayerInteractEvent) event;
                        if (interactEvent.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_AIR || 
                            interactEvent.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK) {
                            context.setVariable("clicked_block", interactEvent.getClickedBlock());
                            context.setVariable("item_in_hand", interactEvent.getItem());
                        }
                    }
                    break;
                case RIGHT_CLICK:
                    if (event instanceof PlayerInteractEvent) {
                        PlayerInteractEvent interactEvent = (PlayerInteractEvent) event;
                        if (interactEvent.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR || 
                            interactEvent.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                            context.setVariable("clicked_block", interactEvent.getClickedBlock());
                            context.setVariable("item_in_hand", interactEvent.getItem());
                        }
                    }
                    break;
                case CHANGE_GAMEMODE:
                    if (event instanceof PlayerGameModeChangeEvent) {
                        PlayerGameModeChangeEvent gamemodeEvent = (PlayerGameModeChangeEvent) event;
                        context.setVariable("old_gamemode", gamemodeEvent.getPlayer().getGameMode().name());
                        context.setVariable("new_gamemode", gamemodeEvent.getNewGameMode().name());
                    }
                    break;
                case LEVEL_CHANGE:
                    if (event instanceof PlayerLevelChangeEvent) {
                        PlayerLevelChangeEvent levelEvent = (PlayerLevelChangeEvent) event;
                        context.setVariable("old_level", levelEvent.getOldLevel());
                        context.setVariable("new_level", levelEvent.getNewLevel());
                    }
                    break;
                case FOOD_CHANGE:
                    if (event instanceof org.bukkit.event.entity.FoodLevelChangeEvent) {
                        org.bukkit.event.entity.FoodLevelChangeEvent foodEvent = (org.bukkit.event.entity.FoodLevelChangeEvent) event;
                        context.setVariable("old_food", ((Player) foodEvent.getEntity()).getFoodLevel());
                        context.setVariable("new_food", foodEvent.getFoodLevel());
                    }
                    break;
                case ITEM_CONSUME:
                    if (event instanceof PlayerItemConsumeEvent) {
                        PlayerItemConsumeEvent consumeEvent = (PlayerItemConsumeEvent) event;
                        context.setVariable("item", consumeEvent.getItem());
                    }
                    break;
                case FISH:
                    if (event instanceof PlayerFishEvent) {
                        PlayerFishEvent fishEvent = (PlayerFishEvent) event;
                        context.setVariable("state", fishEvent.getState().name());
                        if (fishEvent.getCaught() != null) {
                            context.setVariable("caught", fishEvent.getCaught());
                        }
                    }
                    break;
                case ENCHANT_ITEM:
                    if (event instanceof org.bukkit.event.enchantment.EnchantItemEvent) {
                        org.bukkit.event.enchantment.EnchantItemEvent enchantEvent = (org.bukkit.event.enchantment.EnchantItemEvent) event;
                        context.setVariable("item", enchantEvent.getItem());
                        context.setVariable("enchants", enchantEvent.getEnchantsToAdd());
                        context.setVariable("cost", enchantEvent.getExpLevelCost());
                    }
                    break;
                case CRAFT_ITEM:
                    if (event instanceof org.bukkit.event.inventory.CraftItemEvent) {
                        org.bukkit.event.inventory.CraftItemEvent craftEvent = (org.bukkit.event.inventory.CraftItemEvent) event;
                        context.setVariable("result", craftEvent.getRecipe().getResult());
                    }
                    break;
                case PORTAL_USE:
                    if (event instanceof PlayerPortalEvent) {
                        PlayerPortalEvent portalEvent = (PlayerPortalEvent) event;
                        context.setVariable("from_world", portalEvent.getFrom().getWorld().getName());
                        if (portalEvent.getTo() != null) {
                            context.setVariable("to_world", portalEvent.getTo().getWorld().getName());
                        }
                    }
                    break;
            }
        }
        
        return context;
    }
}
