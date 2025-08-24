package ru.openhousing.coding.blocks.events;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок события смерти игрока
 */
public class PlayerDeathEventBlock extends CodeBlock {
    
    public PlayerDeathEventBlock() {
        super(BlockType.PLAYER_DEATH);
        setParameter("deathMessage", "§c%player_name% погиб!");
        setParameter("showTitle", true);
        setParameter("titleText", "§cВы погибли!");
        setParameter("subtitleText", "§7Нажмите пробел для возрождения");
        setParameter("playSound", true);
        setParameter("soundType", "ENTITY_PLAYER_DEATH");
        setParameter("dropItems", true);
        setParameter("keepInventory", false);
        setParameter("dropExperience", true);
        setParameter("keepExperience", false);
        setParameter("respawnDelay", 3);
        setParameter("autoRespawn", false);
        setParameter("teleportToSpawn", true);
        setParameter("clearEffects", true);
        setParameter("setHealth", 20.0);
        setParameter("setFood", 20);
        setParameter("setExperience", 0);
        setParameter("setLevel", 0);
        setParameter("addDeathEffect", false);
        setParameter("deathEffect", "BLINDNESS:10:1,SLOW:20:1");
        setParameter("broadcastDeath", true);
        setParameter("logDeath", true);
        setParameter("deathCountVariable", "deaths");
        setParameter("incrementDeathCount", true);
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден");
        }
        
        try {
            // Сообщение о смерти
            String deathMessage = replaceVariables((String) getParameter("deathMessage"), context);
            if (deathMessage != null && !deathMessage.trim().isEmpty()) {
                player.sendMessage(deathMessage);
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
            
            // Настройки дропа предметов
            boolean dropItems = (Boolean) getParameter("dropItems");
            boolean keepInventory = (Boolean) getParameter("keepInventory");
            
            if (!dropItems) {
                player.getInventory().clear();
            }
            
            // Настройки опыта
            boolean dropExperience = (Boolean) getParameter("dropExperience");
            boolean keepExperience = (Boolean) getParameter("keepExperience");
            
            if (!dropExperience || keepExperience) {
                player.setExp(0);
                player.setLevel(0);
            }
            
            // Задержка возрождения
            Integer respawnDelay = (Integer) getParameter("respawnDelay");
            if (respawnDelay != null && respawnDelay > 0) {
                // Здесь можно добавить логику задержки возрождения
            }
            
            // Автоматическое возрождение
            if ((Boolean) getParameter("autoRespawn")) {
                scheduleRespawn(player, context);
            }
            
            // Телепортация на спавн
            if ((Boolean) getParameter("teleportToSpawn")) {
                player.teleport(player.getWorld().getSpawnLocation());
            }
            
            // Очистить эффекты
            if ((Boolean) getParameter("clearEffects")) {
                player.getActivePotionEffects().forEach(effect -> 
                    player.removePotionEffect(effect.getType()));
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
            
            // Добавить эффект смерти
            if ((Boolean) getParameter("addDeathEffect")) {
                String effects = (String) getParameter("deathEffect");
                if (effects != null) {
                    addDeathEffects(player, effects);
                }
            }
            
            // Трансляция смерти
            if ((Boolean) getParameter("broadcastDeath")) {
                String broadcastMessage = replaceVariables("§c%player_name% погиб!", context);
                player.getServer().broadcastMessage(broadcastMessage);
            }
            
            // Логирование смерти
            if ((Boolean) getParameter("logDeath")) {
                logDeath(player, context);
            }
            
            // Увеличить счетчик смертей
            if ((Boolean) getParameter("incrementDeathCount")) {
                String deathCountVar = (String) getParameter("deathCountVariable");
                if (deathCountVar != null) {
                    Object currentDeaths = context.getVariable(deathCountVar);
                    int deaths = currentDeaths instanceof Number ? ((Number) currentDeaths).intValue() : 0;
                    context.setVariable(deathCountVar, deaths + 1);
                }
            }
            
            // Добавляем информацию о событии в контекст
            context.setVariable("death_time", System.currentTimeMillis());
            context.setVariable("death_location", player.getLocation());
            context.setVariable("death_cause", "unknown");
            context.setVariable("death_killer", null);
            context.setVariable("death_damage", 0.0);
            
            return ExecutionResult.success();
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения события смерти: " + e.getMessage());
        }
    }
    
    /**
     * Запланировать возрождение
     */
    private void scheduleRespawn(Player player, ExecutionContext context) {
        Integer respawnDelay = (Integer) getParameter("respawnDelay");
        if (respawnDelay != null && respawnDelay > 0) {
            player.getServer().getScheduler().runTaskLater(
                org.bukkit.Bukkit.getPluginManager().getPlugin("OpenHousing"),
                () -> {
                    if (player.isOnline()) {
                        performRespawn(player, context);
                    }
                },
                respawnDelay * 20L // Конвертируем в тики
            );
        } else {
            performRespawn(player, context);
        }
    }
    
    /**
     * Выполнить возрождение
     */
    private void performRespawn(Player player, ExecutionContext context) {
        // Телепортация на спавн
        if ((Boolean) getParameter("teleportToSpawn")) {
            player.teleport(player.getWorld().getSpawnLocation());
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
        
        // Очистить эффекты
        if ((Boolean) getParameter("clearEffects")) {
            player.getActivePotionEffects().forEach(effect -> 
                player.removePotionEffect(effect.getType()));
        }
        
        player.sendMessage("§aВы возродились!");
    }
    
    /**
     * Добавить эффекты смерти
     */
    private void addDeathEffects(Player player, String effectsStr) {
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
    
    /**
     * Логировать смерть
     */
    private void logDeath(Player player, ExecutionContext context) {
        String logMessage = String.format(
            "[DEATH] Player %s died at %s in world %s",
            player.getName(),
            player.getLocation().toString(),
            player.getWorld().getName()
        );
        
        // Здесь можно добавить логирование в файл или базу данных
        System.out.println(logMessage);
    }
    
    @Override
    public boolean validate() {
        return getParameter("deathMessage") != null;
    }
    
    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "§7Срабатывает при смерти игрока",
            "",
            "§eПараметры:",
            "§7• Сообщение о смерти",
            "§7• Показать заголовок",
            "§7• Воспроизвести звук",
            "§7• Настройки дропа предметов",
            "§7• Настройки опыта",
            "§7• Задержка возрождения",
            "§7• Автоматическое возрождение",
            "§7• Телепортация на спавн",
            "§7• Очистка эффектов",
            "§7• Настройки здоровья/голода",
            "§7• Эффекты смерти",
            "§7• Трансляция смерти",
            "§7• Логирование",
            "§7• Счетчик смертей"
        );
    }
    
    @Override
    public boolean matchesEvent(Object event) {
        return event instanceof PlayerDeathEvent;
    }
    
    @Override
    public ExecutionContext createContextFromEvent(Object event) {
        if (event instanceof PlayerDeathEvent) {
            return new ExecutionContext(((PlayerDeathEvent) event).getEntity());
        }
        return null;
    }
}
