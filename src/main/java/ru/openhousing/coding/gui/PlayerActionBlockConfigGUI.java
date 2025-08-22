package ru.openhousing.coding.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.actions.PlayerActionBlock;

import java.util.Arrays;

/**
 * Специализированный конфигуратор для PlayerAction блоков
 */
public class PlayerActionBlockConfigGUI extends BaseBlockConfigGUI {
    
    // Слоты для типов действий
    private static final int ACTION_TYPE_SLOT = 13;
    private static final int VALUE_SLOT = 20;
    private static final int EXTRA1_SLOT = 21;
    private static final int EXTRA2_SLOT = 22;
    
    public PlayerActionBlockConfigGUI(OpenHousing plugin, Player player, CodeBlock block, Runnable onSaveCallback) {
        super(plugin, player, block, onSaveCallback);
    }
    
    @Override
    public void setupInventory() {
        inventory = Bukkit.createInventory(null, 54, ChatColor.DARK_BLUE + "Настройка действия игрока");
        
        setupNavigationItems();
        setupActionTypeSelector();
        setupParameterSlots();
    }
    
    private void setupActionTypeSelector() {
        Object currentActionType = block.getParameter("actionType");
        Material material = Material.PLAYER_HEAD;
        String displayName = ChatColor.GOLD + "Тип действия";
        String currentTypeName = "Не выбран";
        
        if (currentActionType instanceof PlayerActionBlock.PlayerActionType) {
            PlayerActionBlock.PlayerActionType type = (PlayerActionBlock.PlayerActionType) currentActionType;
            currentTypeName = getActionTypeDisplayName(type);
            material = getActionTypeMaterial(type);
        }
        
        ItemStack actionTypeItem = new ItemStack(material);
        ItemMeta meta = actionTypeItem.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Выберите тип действия для выполнения",
            ChatColor.GREEN + "Текущий тип: " + ChatColor.WHITE + currentTypeName,
            ChatColor.YELLOW + "Нажмите для изменения"
        ));
        actionTypeItem.setItemMeta(meta);
        inventory.setItem(ACTION_TYPE_SLOT, actionTypeItem);
    }
    
    private void setupParameterSlots() {
        Object actionType = block.getParameter("actionType");
        
        if (actionType instanceof PlayerActionBlock.PlayerActionType) {
            PlayerActionBlock.PlayerActionType type = (PlayerActionBlock.PlayerActionType) actionType;
            
            switch (type) {
                case SEND_MESSAGE:
                    setupVariableSlot(VALUE_SLOT, "value", "Сообщение", "Текст сообщения для отправки игроку");
                    setupVariableSlot(EXTRA1_SLOT, "extra1", "Заголовок", "Заголовок сообщения (опционально)");
                    break;
                    
                case TELEPORT:
                    setupVariableSlot(VALUE_SLOT, "value", "Координаты", "Координаты телепортации (x,y,z или локация)");
                    setupVariableSlot(EXTRA1_SLOT, "extra1", "Мир", "Название мира (опционально)");
                    break;
                    
                case GIVE_ITEM:
                    setupVariableSlot(VALUE_SLOT, "value", "Предмет", "Тип предмета (например: DIAMOND_SWORD)");
                    setupVariableSlot(EXTRA1_SLOT, "extra1", "Количество", "Количество предметов (по умолчанию: 1)");
                    setupVariableSlot(EXTRA2_SLOT, "extra2", "NBT данные", "Дополнительные данные предмета (опционально)");
                    break;
                    
                case PLAY_SOUND:
                    setupVariableSlot(VALUE_SLOT, "value", "Звук", "Название звука (например: ENTITY_PLAYER_LEVELUP)");
                    setupVariableSlot(EXTRA1_SLOT, "extra1", "Громкость", "Громкость звука (0.0-1.0, по умолчанию: 1.0)");
                    setupVariableSlot(EXTRA2_SLOT, "extra2", "Высота тона", "Высота тона (0.5-2.0, по умолчанию: 1.0)");
                    break;
                    
                case ADD_POTION_EFFECT:
                    setupVariableSlot(VALUE_SLOT, "value", "Эффект", "Тип эффекта (например: SPEED)");
                    setupVariableSlot(EXTRA1_SLOT, "extra1", "Длительность", "Длительность в тиках (20 тиков = 1 секунда)");
                    setupVariableSlot(EXTRA2_SLOT, "extra2", "Сила", "Сила эффекта (0-255, по умолчанию: 0)");
                    break;
                    
                case SET_GAMEMODE:
                    setupVariableSlot(VALUE_SLOT, "value", "Режим игры", "Режим игры (SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR)");
                    break;
                    
                case KICK:
                    setupVariableSlot(VALUE_SLOT, "value", "Причина", "Причина кика (опционально)");
                    break;
                    
                case HEAL:
                    setupVariableSlot(VALUE_SLOT, "value", "Количество", "Количество здоровья для восстановления (опционально)");
                    break;
                    
                case FEED:
                    setupVariableSlot(VALUE_SLOT, "value", "Количество", "Количество голода для восстановления (опционально)");
                    break;
                    
                case SET_LEVEL:
                    setupVariableSlot(VALUE_SLOT, "value", "Уровень", "Новый уровень игрока");
                    break;
                    
                case ADD_EXPERIENCE:
                    setupVariableSlot(VALUE_SLOT, "value", "Опыт", "Количество опыта для выдачи");
                    break;
                    
                case SHOW_TITLE:
                    setupVariableSlot(VALUE_SLOT, "value", "Заголовок", "Основной заголовок");
                    setupVariableSlot(EXTRA1_SLOT, "extra1", "Подзаголовок", "Подзаголовок (опционально)");
                    setupVariableSlot(EXTRA2_SLOT, "extra2", "Время", "Время показа в тиках (опционально)");
                    break;
                    
                case SEND_ACTIONBAR:
                    setupVariableSlot(VALUE_SLOT, "value", "Сообщение", "Текст для отображения в action bar");
                    break;
                    
                case CLEAR_INVENTORY:
                    // Нет параметров
                    break;
                    
                case KICK_PLAYER:
                    setupVariableSlot(VALUE_SLOT, "value", "Причина", "Причина кика (опционально)");
                    break;
            }
        }
    }
    
    private String getActionTypeDisplayName(PlayerActionBlock.PlayerActionType type) {
        switch (type) {
            case SEND_MESSAGE: return "Отправить сообщение";
            case TELEPORT: return "Телепортировать";
            case GIVE_ITEM: return "Выдать предмет";
            case PLAY_SOUND: return "Воспроизвести звук";
            case ADD_POTION_EFFECT: return "Добавить эффект";
            case SET_GAMEMODE: return "Установить режим игры";
            case KICK_PLAYER: return "Кикнуть игрока";
            case SET_HEALTH: return "Установить здоровье";
            case SET_FOOD: return "Установить голод";
            case SET_LEVEL: return "Установить уровень";
            case ADD_EXPERIENCE: return "Выдать опыт";
            case SHOW_TITLE: return "Отправить заголовок";
            case SEND_ACTIONBAR: return "Отправить action bar";
            case CLEAR_INVENTORY: return "Очистить инвентарь";
            default: return type.toString();
        }
    }
    
    private Material getActionTypeMaterial(PlayerActionBlock.PlayerActionType type) {
        switch (type) {
            case SEND_MESSAGE: return Material.PAPER;
            case TELEPORT: return Material.ENDER_PEARL;
            case GIVE_ITEM: return Material.CHEST;
            case PLAY_SOUND: return Material.NOTE_BLOCK;
            case ADD_POTION_EFFECT: return Material.POTION;
            case SET_GAMEMODE: return Material.COMMAND_BLOCK;
            case KICK_PLAYER: return Material.BARRIER;
            case SET_HEALTH: return Material.GOLDEN_APPLE;
            case SET_FOOD: return Material.BREAD;
            case SET_LEVEL: return Material.EXPERIENCE_BOTTLE;
            case ADD_EXPERIENCE: return Material.EXPERIENCE_BOTTLE;
            case SHOW_TITLE: return Material.BOOK;
            case SEND_ACTIONBAR: return Material.OAK_SIGN;
            case CLEAR_INVENTORY: return Material.BUCKET;
            default: return Material.PLAYER_HEAD;
        }
    }
    
    @Override
    protected String getParameterPrompt(String parameterName) {
        Object actionType = block.getParameter("actionType");
        if (!(actionType instanceof PlayerActionBlock.PlayerActionType)) {
            return "Введите значение:";
        }
        
        PlayerActionBlock.PlayerActionType type = (PlayerActionBlock.PlayerActionType) actionType;
        
        switch (parameterName) {
            case "value":
                return getValuePrompt(type);
            case "extra1":
                return getExtra1Prompt(type);
            case "extra2":
                return getExtra2Prompt(type);
            default:
                return "Введите значение:";
        }
    }
    
    private String getValuePrompt(PlayerActionBlock.PlayerActionType type) {
        switch (type) {
            case SEND_MESSAGE: return "Введите текст сообщения:";
            case TELEPORT: return "Введите координаты (x,y,z) или имя локации:";
            case GIVE_ITEM: return "Введите тип предмета (например: DIAMOND_SWORD):";
            case PLAY_SOUND: return "Введите название звука:";
            case ADD_POTION_EFFECT: return "Введите тип эффекта (например: SPEED):";
            case SET_GAMEMODE: return "Введите режим игры (SURVIVAL/CREATIVE/ADVENTURE/SPECTATOR):";
            case KICK_PLAYER: return "Введите причину кика:";
            case SET_HEALTH: return "Введите количество здоровья (или оставьте пустым для полного восстановления):";
            case SET_FOOD: return "Введите количество голода (или оставьте пустым для полного восстановления):";
            case SET_LEVEL: return "Введите новый уровень:";
            case ADD_EXPERIENCE: return "Введите количество опыта:";
            case SHOW_TITLE: return "Введите заголовок:";
            case SEND_ACTIONBAR: return "Введите текст для action bar:";
            default: return "Введите значение:";
        }
    }
    
    private String getExtra1Prompt(PlayerActionBlock.PlayerActionType type) {
        switch (type) {
            case SEND_MESSAGE: return "Введите заголовок сообщения (опционально):";
            case TELEPORT: return "Введите название мира (опционально):";
            case GIVE_ITEM: return "Введите количество предметов:";
            case PLAY_SOUND: return "Введите громкость (0.0-1.0):";
            case ADD_POTION_EFFECT: return "Введите длительность в тиках:";
            case SHOW_TITLE: return "Введите подзаголовок:";
            default: return "Введите дополнительный параметр:";
        }
    }
    
    private String getExtra2Prompt(PlayerActionBlock.PlayerActionType type) {
        switch (type) {
            case GIVE_ITEM: return "Введите NBT данные (опционально):";
            case PLAY_SOUND: return "Введите высоту тона (0.5-2.0):";
            case ADD_POTION_EFFECT: return "Введите силу эффекта (0-255):";
            case SHOW_TITLE: return "Введите время показа в тиках:";
            default: return "Введите дополнительный параметр:";
        }
    }
    
    @Override
    protected void handleSpecificClick(int slot, boolean isRightClick, boolean isShiftClick) {
        if (slot == ACTION_TYPE_SLOT) {
            openActionTypeSelector();
        }
    }
    
    private void openActionTypeSelector() {
        HandlerList.unregisterAll(this);
        
        // Создаем GUI для выбора типа действия
        org.bukkit.inventory.Inventory actionSelector = Bukkit.createInventory(null, 54, 
            ChatColor.DARK_BLUE + "Выберите тип действия");
        
        PlayerActionBlock.PlayerActionType[] types = PlayerActionBlock.PlayerActionType.values();
        for (int i = 0; i < types.length && i < 45; i++) {
            PlayerActionBlock.PlayerActionType type = types[i];
            ItemStack item = new ItemStack(getActionTypeMaterial(type));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + getActionTypeDisplayName(type));
            meta.setLore(Arrays.asList(ChatColor.GRAY + "Нажмите для выбора"));
            item.setItemMeta(meta);
            actionSelector.setItem(i, item);
        }
        
        // Кнопка назад
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.YELLOW + "Назад");
        backItem.setItemMeta(backMeta);
        actionSelector.setItem(53, backItem);
        
        player.openInventory(actionSelector);
        
        // Регистрируем временный обработчик
        new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
                if (!event.getInventory().equals(actionSelector) || !event.getWhoClicked().equals(player)) {
                    return;
                }
                
                event.setCancelled(true);
                int clickedSlot = event.getSlot();
                
                if (clickedSlot == 53) {
                    // Назад
                    HandlerList.unregisterAll(this);
                    PlayerActionBlockConfigGUI.this.open();
                    return;
                }
                
                if (clickedSlot < types.length) {
                    PlayerActionBlock.PlayerActionType selectedType = types[clickedSlot];
                    block.setParameter("actionType", selectedType);
                    
                    HandlerList.unregisterAll(this);
                    PlayerActionBlockConfigGUI.this.open();
                }
            }
            
            @org.bukkit.event.EventHandler
            public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
                if (event.getInventory().equals(actionSelector) && event.getPlayer().equals(player)) {
                    HandlerList.unregisterAll(this);
                }
            }
        };
        
        Bukkit.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
                if (!event.getInventory().equals(actionSelector) || !event.getWhoClicked().equals(player)) {
                    return;
                }
                
                event.setCancelled(true);
                int clickedSlot = event.getSlot();
                
                if (clickedSlot == 53) {
                    // Назад
                    HandlerList.unregisterAll(this);
                    PlayerActionBlockConfigGUI.this.open();
                    return;
                }
                
                if (clickedSlot < types.length) {
                    PlayerActionBlock.PlayerActionType selectedType = types[clickedSlot];
                    block.setParameter("actionType", selectedType);
                    
                    HandlerList.unregisterAll(this);
                    PlayerActionBlockConfigGUI.this.open();
                }
            }
            
            @org.bukkit.event.EventHandler
            public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
                if (event.getInventory().equals(actionSelector) && event.getPlayer().equals(player)) {
                    HandlerList.unregisterAll(this);
                }
            }
        }, plugin);
    }
}
