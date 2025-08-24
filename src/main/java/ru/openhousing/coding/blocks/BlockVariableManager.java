package ru.openhousing.coding.blocks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import ru.openhousing.OpenHousing;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер переменных блоков
 * Обрабатывает чат для настройки переменных и управляет их состоянием
 */
public class BlockVariableManager implements Listener {
    
    private final OpenHousing plugin;
    private final Map<UUID, BlockVariable> pendingVariables = new ConcurrentHashMap<>();
    private final Map<String, BlockVariable> globalVariables = new ConcurrentHashMap<>();
    
    public BlockVariableManager(OpenHousing plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Обработка чата для настройки переменных
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // Проверяем, есть ли у игрока переменная в руке
        BlockVariable variable = getVariableInHand(player);
        if (variable != null) {
            event.setCancelled(true);
            
            // Обрабатываем значение переменной
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (variable.parseValue(message)) {
                    player.sendMessage("§a✓ Переменная '" + variable.getName() + "' обновлена!");
                    player.sendMessage("§7Новое значение: " + formatVariableValue(variable));
                    
                    // Обновляем предмет в руке
                    updateVariableInHand(player, variable);
                } else {
                    player.sendMessage("§c✗ Неверный формат значения для переменной '" + variable.getName() + "'");
                    player.sendMessage("§7Ожидаемый формат: " + variable.getType().getExample());
                }
            });
        }
    }
    
    /**
     * Получает переменную, которую игрок держит в руке
     */
    private BlockVariable getVariableInHand(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack mainHand = inventory.getItemInMainHand();
        ItemStack offHand = inventory.getItemInOffHand();
        
        // Проверяем основную руку
        BlockVariable variable = findVariableByItem(mainHand);
        if (variable != null) {
            return variable;
        }
        
        // Проверяем вторую руку
        return findVariableByItem(offHand);
    }
    
    /**
     * Находит переменную по предмету
     */
    private BlockVariable findVariableByItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        
        // Проверяем все переменные
        for (BlockVariable variable : globalVariables.values()) {
            if (isVariableItem(item, variable)) {
                return variable;
            }
        }
        
        return null;
    }
    
    /**
     * Проверяет, является ли предмет переменной
     */
    private boolean isVariableItem(ItemStack item, BlockVariable variable) {
        if (item.getType() != variable.getMaterial()) {
            return false;
        }
        
        if (!item.hasItemMeta()) {
            return false;
        }
        
        String displayName = item.getItemMeta().getDisplayName();
        return displayName != null && displayName.contains(variable.getName());
    }
    
    /**
     * Обновляет переменную в руке игрока
     */
    private void updateVariableInHand(Player player, BlockVariable variable) {
        PlayerInventory inventory = player.getInventory();
        ItemStack mainHand = inventory.getItemInMainHand();
        ItemStack offHand = inventory.getItemInOffHand();
        
        // Обновляем основную руку
        if (isVariableItem(mainHand, variable)) {
            inventory.setItemInMainHand(variable.createItemStack());
            return;
        }
        
        // Обновляем вторую руку
        if (isVariableItem(offHand, variable)) {
            inventory.setItemInOffHand(variable.createItemStack());
        }
    }
    
    /**
     * Форматирует значение переменной для отображения
     */
    private String formatVariableValue(BlockVariable variable) {
        Object value = variable.getValue();
        if (value == null) return "§cНе задано";
        
        switch (variable.getType()) {
            case BOOLEAN:
                return (Boolean) value ? "§aВключено" : "§cВыключено";
            case INTEGER:
            case DOUBLE:
                return "§e" + value.toString();
            case STRING:
                return "§f" + value.toString();
            case LIST:
                if (value instanceof java.util.List) {
                    java.util.List<?> list = (java.util.List<?>) value;
                    return "§f" + String.join(", ", list.stream().map(Object::toString).toArray(String[]::new));
                }
                return "§f" + value.toString();
            default:
                return "§f" + value.toString();
        }
    }
    
    /**
     * Регистрирует глобальную переменную
     */
    public void registerGlobalVariable(BlockVariable variable) {
        globalVariables.put(variable.getName(), variable);
    }
    
    /**
     * Получает глобальную переменную по имени
     */
    public BlockVariable getGlobalVariable(String name) {
        return globalVariables.get(name);
    }
    
    /**
     * Создает переменную для блока
     */
    public BlockVariable createBlockVariable(String name, String description, 
                                          BlockVariable.VariableType type, Object defaultValue) {
        BlockVariable variable = new BlockVariable(name, description, type, defaultValue);
        registerGlobalVariable(variable);
        return variable;
    }
    
    /**
     * Выдает переменную игроку
     */
    public void giveVariableToPlayer(Player player, String variableName) {
        BlockVariable variable = globalVariables.get(variableName);
        if (variable != null) {
            player.getInventory().addItem(variable.createItemStack());
            player.sendMessage("§a✓ Вы получили переменную '" + variableName + "'");
            player.sendMessage("§7Держите её в руке и напишите в чат новое значение");
        } else {
            player.sendMessage("§c✗ Переменная '" + variableName + "' не найдена");
        }
    }
    
    /**
     * Показывает все доступные переменные
     */
    public void showAvailableVariables(Player player) {
        if (globalVariables.isEmpty()) {
            player.sendMessage("§7Нет доступных переменных");
            return;
        }
        
        player.sendMessage("§6=== Доступные переменные ===");
        for (BlockVariable variable : globalVariables.values()) {
            player.sendMessage(String.format("§e%s §7(%s): %s", 
                variable.getName(), 
                variable.getType().getDisplayName(),
                formatVariableValue(variable)));
        }
        player.sendMessage("");
        player.sendMessage("§7Используйте: /blockvar give <имя_переменной>");
    }
    
    /**
     * Удаляет переменную
     */
    public void removeVariable(String name) {
        globalVariables.remove(name);
    }
    
    /**
     * Очищает все переменные
     */
    public void clearAllVariables() {
        globalVariables.clear();
    }
    
    /**
     * Получает количество переменных
     */
    public int getVariableCount() {
        return globalVariables.size();
    }
}
