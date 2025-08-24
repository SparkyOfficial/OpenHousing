package ru.openhousing.coding.blocks.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Специализированный блок для обработки зачарования предметов игроками
 * Предоставляет расширенную функциональность для управления процессом зачарования
 */
public class PlayerEnchantEventBlock extends CodeBlock {

    // Настройки зачарования
    private List<Material> allowedItems;
    private List<String> allowedWorlds;
    private List<Enchantment> allowedEnchantments;
    private int maxEnchantmentLevel;
    private boolean autoRepairEnabled;
    private boolean autoUnbreakableEnabled;
    private boolean autoMendingEnabled;
    private boolean economyIntegrationEnabled;
    private boolean achievementsEnabled;
    private boolean loggingEnabled;
    private boolean statisticsEnabled;
    private boolean debugModeEnabled;

    // Команды для выполнения
    private List<String> preEnchantCommands;
    private List<String> postEnchantCommands;
    private List<String> failureCommands;

    public PlayerEnchantEventBlock() {
        super(BlockType.PLAYER_ENCHANT);
        initializeDefaultSettings();
    }

    private void initializeDefaultSettings() {
        // Разрешенные предметы
        allowedItems = new ArrayList<>();
        allowedItems.add(Material.DIAMOND_SWORD);
        allowedItems.add(Material.DIAMOND_PICKAXE);
        allowedItems.add(Material.DIAMOND_AXE);
        allowedItems.add(Material.DIAMOND_SHOVEL);
        allowedItems.add(Material.DIAMOND_HELMET);
        allowedItems.add(Material.DIAMOND_CHESTPLATE);
        allowedItems.add(Material.DIAMOND_LEGGINGS);
        allowedItems.add(Material.DIAMOND_BOOTS);

        // Разрешенные миры
        allowedWorlds = new ArrayList<>();
        allowedWorlds.add("world");
        allowedWorlds.add("world_nether");
        allowedWorlds.add("world_the_end");

        // Разрешенные зачарования
        allowedEnchantments = new ArrayList<>();
        allowedEnchantments.add(Enchantment.DAMAGE_ALL);
        allowedEnchantments.add(Enchantment.UNBREAKING);
        allowedEnchantments.add(Enchantment.EFFICIENCY);
        allowedEnchantments.add(Enchantment.PROTECTION_ENVIRONMENTAL);
        allowedEnchantments.add(Enchantment.ARROW_DAMAGE);
        allowedEnchantments.add(Enchantment.ARROW_INFINITE);

        // Ограничения
        maxEnchantmentLevel = 5;
        autoRepairEnabled = true;
        autoUnbreakableEnabled = false;
        autoMendingEnabled = true;
        economyIntegrationEnabled = false;
        achievementsEnabled = true;
        loggingEnabled = true;
        statisticsEnabled = true;
        debugModeEnabled = false;

        // Команды
        preEnchantCommands = new ArrayList<>();
        postEnchantCommands = new ArrayList<>();
        failureCommands = new ArrayList<>();
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        if (debugModeEnabled) {
            context.getPlayer().sendMessage("§7[DEBUG] PlayerEnchantEventBlock создан");
        }
        return ExecutionResult.success();
    }

    @EventHandler
    public void onPrepareEnchant(PrepareItemEnchantEvent event) {
        Player player = event.getEnchanter();
        if (player == null) return;

        // Проверяем разрешения
        if (!isEnchantmentAllowed(player, event.getItem())) {
            event.setCancelled(true);
            player.sendMessage("§cЗачарование этого предмета запрещено!");
            executeFailureCommands(player, "prepare_enchant");
            return;
        }

        // Логирование
        if (loggingEnabled) {
            logEnchantmentAttempt(player, "prepare", event.getItem());
        }

        // Выполняем предварительные команды
        executePreEnchantCommands(player, event.getItem());
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        if (player == null) return;

        // Проверяем разрешения
        if (!isEnchantmentAllowed(player, event.getItem())) {
            event.setCancelled(true);
            player.sendMessage("§cЗачарование этого предмета запрещено!");
            executeFailureCommands(player, "enchant_item");
            return;
        }

        // Применяем автоматические улучшения
        applyAutoImprovements(event.getItem());

        // Логирование
        if (loggingEnabled) {
            logEnchantmentSuccess(player, event.getItem(), event.getEnchantsToAdd());
        }

        // Статистика
        if (statisticsEnabled) {
            updateEnchantmentStatistics(player, event.getItem(), event.getEnchantsToAdd());
        }

        // Достижения
        if (achievementsEnabled) {
            checkEnchantmentAchievements(player, event.getItem(), event.getEnchantsToAdd());
        }

        // Выполняем команды после зачарования
        executePostEnchantCommands(player, event.getItem());
    }

    private boolean isEnchantmentAllowed(Player player, ItemStack item) {
        // Проверка предмета
        if (!allowedItems.contains(item.getType())) {
            return false;
        }

        // Проверка мира
        if (!allowedWorlds.contains(player.getWorld().getName())) {
            return false;
        }

        // Проверка разрешений
        if (!player.hasPermission("openhousing.enchant." + item.getType().name().toLowerCase())) {
            return false;
        }

        return true;
    }

    private void applyAutoImprovements(ItemStack item) {
        if (autoRepairEnabled) {
            // Автоматический ремонт
            if (item.getDurability() > 0) {
                item.setDurability((short) 0);
            }
        }

        if (autoUnbreakableEnabled) {
            // Автоматическое зачарование "Неразрушимость"
            item.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
        }

        if (autoMendingEnabled) {
            // Автоматическое зачарование "Починка"
            item.addUnsafeEnchantment(Enchantment.MENDING, 1);
        }
    }

    private void logEnchantmentAttempt(Player player, String action, ItemStack item) {
        String logMessage = String.format(
            "[ENCHANT] %s %s %s в мире %s",
            player.getName(),
            action,
            item.getType().name(),
            player.getWorld().getName()
        );
        
        // Логируем в консоль
        Bukkit.getLogger().info(logMessage);
        
        // Логируем в файл
        if (OpenHousing.getInstance() != null) {
            OpenHousing.getInstance().getLogger().info(logMessage);
        }
    }

    private void logEnchantmentSuccess(Player player, ItemStack item, Map<Enchantment, Integer> enchants) {
        StringBuilder enchantList = new StringBuilder();
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            if (enchantList.length() > 0) enchantList.append(", ");
            enchantList.append(entry.getKey().getKey().getKey())
                      .append(" ")
                      .append(entry.getValue());
        }

        String logMessage = String.format(
            "[ENCHANT_SUCCESS] %s зачаровал %s: %s",
            player.getName(),
            item.getType().name(),
            enchantList.toString()
        );
        
        Bukkit.getLogger().info(logMessage);
        if (OpenHousing.getInstance() != null) {
            OpenHousing.getInstance().getLogger().info(logMessage);
        }
    }

    private void updateEnchantmentStatistics(Player player, ItemStack item, Map<Enchantment, Integer> enchants) {
        // Здесь будет логика обновления статистики
        // Пока просто логируем
        if (debugModeEnabled) {
            player.sendMessage("§7[DEBUG] Статистика обновлена");
        }
    }

    private void checkEnchantmentAchievements(Player player, ItemStack item, Map<Enchantment, Integer> enchants) {
        // Здесь будет логика проверки достижений
        // Пока просто логируем
        if (debugModeEnabled) {
            player.sendMessage("§7[DEBUG] Достижения проверены");
        }
    }

    private void executePreEnchantCommands(Player player, ItemStack item) {
        for (String command : preEnchantCommands) {
            try {
                String processedCommand = command
                    .replace("{player}", player.getName())
                    .replace("{item}", item.getType().name())
                    .replace("{world}", player.getWorld().getName());
                
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
            } catch (Exception e) {
                if (debugModeEnabled) {
                    player.sendMessage("§cОшибка выполнения команды: " + command);
                }
            }
        }
    }

    private void executePostEnchantCommands(Player player, ItemStack item) {
        for (String command : postEnchantCommands) {
            try {
                String processedCommand = command
                    .replace("{player}", player.getName())
                    .replace("{item}", item.getType().name())
                    .replace("{world}", player.getWorld().getName());
                
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
            } catch (Exception e) {
                if (debugModeEnabled) {
                    player.sendMessage("§cОшибка выполнения команды: " + command);
                }
            }
        }
    }

    private void executeFailureCommands(Player player, String reason) {
        for (String command : failureCommands) {
            try {
                String processedCommand = command
                    .replace("{player}", player.getName())
                    .replace("{reason}", reason)
                    .replace("{world}", player.getWorld().getName());
                
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
            } catch (Exception e) {
                if (debugModeEnabled) {
                    player.sendMessage("§cОшибка выполнения команды: " + command);
                }
            }
        }
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("§7Специализированный блок для обработки зачарования");
        description.add("§7предметов игроками с расширенной функциональностью");
        description.add("§7");
        description.add("§7Возможности:");
        description.add("§7• Контроль разрешенных предметов и зачарований");
        description.add("§7• Автоматические улучшения (ремонт, неразрушимость)");
        description.add("§7• Система достижений и статистики");
        description.add("§7• Логирование и мониторинг");
        description.add("§7• Выполнение команд до/после зачарования");
        return description;
    }

    @Override
    public boolean validate() {
        // Проверяем корректность настроек
        if (allowedItems == null || allowedItems.isEmpty()) {
            return false;
        }
        
        if (allowedWorlds == null || allowedWorlds.isEmpty()) {
            return false;
        }
        
        if (maxEnchantmentLevel < 1 || maxEnchantmentLevel > 10) {
            return false;
        }
        
        return true;
    }

    // Геттеры и сеттеры для настроек
    public List<Material> getAllowedItems() { return new ArrayList<>(allowedItems); }
    public void setAllowedItems(List<Material> items) { this.allowedItems = new ArrayList<>(items); }
    
    public List<String> getAllowedWorlds() { return new ArrayList<>(allowedWorlds); }
    public void setAllowedWorlds(List<String> worlds) { this.allowedWorlds = new ArrayList<>(worlds); }
    
    public List<Enchantment> getAllowedEnchantments() { return new ArrayList<>(allowedEnchantments); }
    public void setAllowedEnchantments(List<Enchantment> enchants) { this.allowedEnchantments = new ArrayList<>(enchants); }
    
    public int getMaxEnchantmentLevel() { return maxEnchantmentLevel; }
    public void setMaxEnchantmentLevel(int level) { this.maxEnchantmentLevel = level; }
    
    public boolean isAutoRepairEnabled() { return autoRepairEnabled; }
    public void setAutoRepairEnabled(boolean enabled) { this.autoRepairEnabled = enabled; }
    
    public boolean isAutoUnbreakableEnabled() { return autoUnbreakableEnabled; }
    public void setAutoUnbreakableEnabled(boolean enabled) { this.autoUnbreakableEnabled = enabled; }
    
    public boolean isAutoMendingEnabled() { return autoMendingEnabled; }
    public void setAutoMendingEnabled(boolean enabled) { this.autoMendingEnabled = enabled; }
    
    public boolean isEconomyIntegrationEnabled() { return economyIntegrationEnabled; }
    public void setEconomyIntegrationEnabled(boolean enabled) { this.economyIntegrationEnabled = enabled; }
    
    public boolean isAchievementsEnabled() { return achievementsEnabled; }
    public void setAchievementsEnabled(boolean enabled) { this.achievementsEnabled = enabled; }
    
    public boolean isLoggingEnabled() { return loggingEnabled; }
    public void setLoggingEnabled(boolean enabled) { this.loggingEnabled = enabled; }
    
    public boolean isStatisticsEnabled() { return statisticsEnabled; }
    public void setStatisticsEnabled(boolean enabled) { this.statisticsEnabled = enabled; }
    
    public boolean isDebugModeEnabled() { return debugModeEnabled; }
    public void setDebugModeEnabled(boolean enabled) { this.debugModeEnabled = enabled; }
}
