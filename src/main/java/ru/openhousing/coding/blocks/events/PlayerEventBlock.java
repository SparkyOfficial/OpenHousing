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
        PLACE_BLOCK("Установка блока", "Когда игрок ставит блок");
        
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
        return getParameter("eventType") != null;
    }
    
    @Override
    public List<String> getDescription() {
        PlayerEventType eventType = (PlayerEventType) getParameter("eventType");
        return Arrays.asList(
            "§6Событие игрока",
            "§7Тип: §f" + (eventType != null ? eventType.getDisplayName() : "Не выбран"),
            "§7Описание: §f" + (eventType != null ? eventType.getDescription() : ""),
            "",
            "§8Дочерних блоков: " + childBlocks.size()
        );
    }
    
    /**
     * Проверка соответствия события
     */
    public boolean matchesEvent(Class<?> eventClass, Object... params) {
        PlayerEventType eventType = (PlayerEventType) getParameter("eventType");
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
        PlayerEventType eventType = (PlayerEventType) getParameter("eventType");
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
            }
        }
        
        return context;
    }
}
