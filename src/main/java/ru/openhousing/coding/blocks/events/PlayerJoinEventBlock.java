package ru.openhousing.coding.blocks.events;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок события входа игрока на сервер
 */
public class PlayerJoinEventBlock extends CodeBlock {
    
    public PlayerJoinEventBlock() {
        super(BlockType.PLAYER_JOIN);
        setParameter("welcomeMessage", "Добро пожаловать, %player_name%!");
        setParameter("showTitle", true);
        setParameter("titleText", "Добро пожаловать!");
        setParameter("subtitleText", "Наслаждайтесь игрой!");
        setParameter("playSound", true);
        setParameter("soundType", "ENTITY_PLAYER_LEVELUP");
        setParameter("giveStarterItems", false);
        setParameter("teleportToSpawn", false);
        setParameter("setGamemode", "SURVIVAL");
        setParameter("clearInventory", false);
        setParameter("setHealth", 20.0);
        setParameter("setFood", 20);
        setParameter("setExperience", 0);
        setParameter("setLevel", 0);
        setParameter("addEffects", false);
        setParameter("effects", "SPEED:30:1,REGENERATION:60:1");
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден");
        }
        
        try {
            // Приветственное сообщение
            String welcomeMessage = replaceVariables((String) getParameter("welcomeMessage"), context);
            if (welcomeMessage != null && !welcomeMessage.trim().isEmpty()) {
                player.sendMessage(welcomeMessage);
            }
            
            // Показать заголовок
            if ((Boolean) getParameter("showTitle")) {
                String titleText = replaceVariables((String) getParameter("titleText"), context);
                String subtitleText = replaceVariables((String) getParameter("subtitleText"), context);
                player.sendTitle(titleText, subtitleText, 10, 70, 20);
            }
            
            // Воспроизвести звук
            if ((Boolean) getParameter("playSound")) {
                String soundType = (String) getParameter("soundType");
                if (soundType != null) {
                    try {
                        org.bukkit.Sound sound = org.bukkit.Sound.valueOf(soundType);
                        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                    } catch (IllegalArgumentException e) {
                        // Звук не найден, игнорируем
                    }
                }
            }
            
            // Дать стартовые предметы
            if ((Boolean) getParameter("giveStarterItems")) {
                giveStarterItems(player);
            }
            
            // Телепортировать на спавн
            if ((Boolean) getParameter("teleportToSpawn")) {
                player.teleport(player.getWorld().getSpawnLocation());
            }
            
            // Установить режим игры
            String gamemode = (String) getParameter("setGamemode");
            if (gamemode != null) {
                try {
                    org.bukkit.GameMode mode = org.bukkit.GameMode.valueOf(gamemode.toUpperCase());
                    player.setGameMode(mode);
                } catch (IllegalArgumentException e) {
                    // Режим игры не найден, игнорируем
                }
            }
            
            // Очистить инвентарь
            if ((Boolean) getParameter("clearInventory")) {
                player.getInventory().clear();
            }
            
            // Установить здоровье
            Double health = (Double) getParameter("setHealth");
            if (health != null) {
                player.setHealth(Math.min(health, player.getMaxHealth()));
            }
            
            // Установить голод
            Integer food = (Integer) getParameter("setFood");
            if (food != null) {
                player.setFoodLevel(Math.min(food, 20));
            }
            
            // Установить опыт
            Integer exp = (Integer) getParameter("setExperience");
            if (exp != null) {
                player.setExp(0);
                player.setLevel(exp);
            }
            
            // Установить уровень
            Integer level = (Integer) getParameter("setLevel");
            if (level != null) {
                player.setLevel(level);
            }
            
            // Добавить эффекты
            if ((Boolean) getParameter("addEffects")) {
                String effects = (String) getParameter("effects");
                if (effects != null) {
                    addEffects(player, effects);
                }
            }
            
            // Добавляем информацию о событии в контекст
            context.setVariable("join_time", System.currentTimeMillis());
            context.setVariable("player_first_join", !player.hasPlayedBefore());
            context.setVariable("player_ip", player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "unknown");
            
            return ExecutionResult.success();
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения события входа: " + e.getMessage());
        }
    }
    
    /**
     * Дать стартовые предметы
     */
    private void giveStarterItems(Player player) {
        org.bukkit.inventory.ItemStack stone = new org.bukkit.inventory.ItemStack(org.bukkit.Material.STONE, 64);
        org.bukkit.inventory.ItemStack wood = new org.bukkit.inventory.ItemStack(org.bukkit.Material.OAK_LOG, 64);
        org.bukkit.inventory.ItemStack bread = new org.bukkit.inventory.ItemStack(org.bukkit.Material.BREAD, 16);
        org.bukkit.inventory.ItemStack pickaxe = new org.bukkit.inventory.ItemStack(org.bukkit.Material.STONE_PICKAXE, 1);
        org.bukkit.inventory.ItemStack axe = new org.bukkit.inventory.ItemStack(org.bukkit.Material.STONE_AXE, 1);
        
        player.getInventory().addItem(stone, wood, bread, pickaxe, axe);
    }
    
    /**
     * Добавить эффекты зелья
     */
    private void addEffects(Player player, String effectsStr) {
        String[] effects = effectsStr.split(",");
        for (String effect : effects) {
            String[] parts = effect.trim().split(":");
            if (parts.length >= 2) {
                try {
                    org.bukkit.potion.PotionEffectType type = org.bukkit.potion.PotionEffectType.getByName(parts[0].toUpperCase());
                    int duration = Integer.parseInt(parts[1]) * 20; // Конвертируем в тики
                    int amplifier = parts.length > 2 ? Integer.parseInt(parts[2]) - 1 : 0;
                    
                    if (type != null) {
                        player.addPotionEffect(new org.bukkit.potion.PotionEffect(type, duration, amplifier));
                    }
                } catch (NumberFormatException e) {
                    // Игнорируем некорректные значения
                }
            }
        }
    }
    
    @Override
    public boolean validate() {
        return getParameter("welcomeMessage") != null;
    }
    
    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "§7Срабатывает при входе игрока на сервер",
            "",
            "§eПараметры:",
            "§7• Приветственное сообщение",
            "§7• Показать заголовок",
            "§7• Воспроизвести звук",
            "§7• Дать стартовые предметы",
            "§7• Телепортировать на спавн",
            "§7• Установить режим игры",
            "§7• Настроить здоровье/голод",
            "§7• Добавить эффекты зелья"
        );
    }
    
    @Override
    public boolean matchesEvent(Object event) {
        return event instanceof PlayerJoinEvent;
    }
    
    @Override
    public ExecutionContext createContextFromEvent(Object event) {
        if (event instanceof PlayerJoinEvent) {
            return new ExecutionContext(((PlayerJoinEvent) event).getPlayer());
        }
        return null;
    }
}
