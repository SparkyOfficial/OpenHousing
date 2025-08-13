package ru.openhousing.coding.blocks.actions;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок действий игрока
 */
public class PlayerActionBlock extends CodeBlock {
    
    public enum PlayerActionType {
        SEND_MESSAGE("Отправить сообщение", "Отправляет сообщение игроку"),
        TELEPORT("Телепортировать", "Телепортирует игрока"),
        GIVE_ITEM("Дать предмет", "Дает предмет игроку"),
        TAKE_ITEM("Забрать предмет", "Забирает предмет у игрока"),
        SET_HEALTH("Установить здоровье", "Устанавливает здоровье игрока"),
        SET_FOOD("Установить голод", "Устанавливает уровень голода"),
        SET_LEVEL("Установить уровень", "Устанавливает уровень опыта"),
        ADD_EXPERIENCE("Добавить опыт", "Добавляет опыт игроку"),
        SET_GAMEMODE("Установить режим", "Устанавливает режим игры"),
        PLAY_SOUND("Воспроизвести звук", "Воспроизводит звук для игрока"),
        SHOW_TITLE("Показать заголовок", "Показывает заголовок игроку"),
        SEND_ACTIONBAR("Отправить панель действий", "Отправляет сообщение на панель действий"),
        KICK_PLAYER("Кикнуть игрока", "Кикает игрока с сервера"),
        ADD_POTION_EFFECT("Добавить эффект", "Добавляет эффект зелья"),
        REMOVE_POTION_EFFECT("Убрать эффект", "Убирает эффект зелья"),
        CLEAR_INVENTORY("Очистить инвентарь", "Очищает инвентарь игрока"),
        OPEN_INVENTORY("Открыть инвентарь", "Открывает инвентарь"),
        CLOSE_INVENTORY("Закрыть инвентарь", "Закрывает инвентарь"),
        SET_FLY("Установить полет", "Включает/выключает полет"),
        SPAWN_PARTICLE("Создать частицы", "Создает частицы вокруг игрока"),
        STRIKE_LIGHTNING("Ударить молнией", "Ударяет молнией по игроку"),
        SET_FIRE("Поджечь", "Поджигает игрока"),
        EXTINGUISH("Потушить", "Тушит игрока"),
        HIDE_PLAYER("Скрыть игрока", "Скрывает игрока от других"),
        SHOW_PLAYER("Показать игрока", "Показывает скрытого игрока"),
        GIVE_MONEY("Дать деньги", "Дает деньги игроку"),
        TAKE_MONEY("Забрать деньги", "Забирает деньги у игрока"),
        SEND_TO_SERVER("Отправить на сервер", "Отправляет игрока на другой сервер");
        
        private final String displayName;
        private final String description;
        
        PlayerActionType(String displayName, String description) {
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
    
    public PlayerActionBlock() {
        super(BlockType.PLAYER_ACTION);
        setParameter("actionType", PlayerActionType.SEND_MESSAGE);
        setParameter("value", "");
        setParameter("extra1", "");
        setParameter("extra2", "");
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден");
        }
        
        try {
            executeAction(player, context);
            return ExecutionResult.success();
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения действия: " + e.getMessage());
        }
    }
    
    private void executeAction(Player player, ExecutionContext context) {
        PlayerActionType actionType = (PlayerActionType) getParameter("actionType");
        String value = replaceVariables((String) getParameter("value"), context);
        String extra1 = replaceVariables((String) getParameter("extra1"), context);
        String extra2 = replaceVariables((String) getParameter("extra2"), context);
        
        if (actionType == null) return;
        
        switch (actionType) {
            case SEND_MESSAGE:
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', value));
                break;
                
            case TELEPORT:
                // Формат: world,x,y,z или x,y,z (текущий мир)
                String[] coords = value.split(",");
                if (coords.length >= 3) {
                    World world = coords.length >= 4 ? 
                        Bukkit.getWorld(coords[0]) : player.getWorld();
                    if (world != null) {
                        int offset = coords.length >= 4 ? 1 : 0;
                        double x = Double.parseDouble(coords[offset]);
                        double y = Double.parseDouble(coords[offset + 1]);
                        double z = Double.parseDouble(coords[offset + 2]);
                        player.teleport(new Location(world, x, y, z));
                    }
                }
                break;
                
            case GIVE_ITEM:
                // Формат: MATERIAL:количество или MATERIAL
                String[] itemParts = value.split(":");
                Material material = Material.valueOf(itemParts[0].toUpperCase());
                int amount = itemParts.length > 1 ? Integer.parseInt(itemParts[1]) : 1;
                player.getInventory().addItem(new ItemStack(material, amount));
                break;
                
            case TAKE_ITEM:
                String[] takeParts = value.split(":");
                Material takeMaterial = Material.valueOf(takeParts[0].toUpperCase());
                int takeAmount = takeParts.length > 1 ? Integer.parseInt(takeParts[1]) : 1;
                ItemStack takeItem = new ItemStack(takeMaterial, takeAmount);
                player.getInventory().removeItem(takeItem);
                break;
                
            case SET_HEALTH:
                double health = Math.min(20.0, Math.max(0.0, Double.parseDouble(value)));
                player.setHealth(health);
                break;
                
            case SET_FOOD:
                int food = Math.min(20, Math.max(0, Integer.parseInt(value)));
                player.setFoodLevel(food);
                break;
                
            case SET_LEVEL:
                player.setLevel(Integer.parseInt(value));
                break;
                
            case ADD_EXPERIENCE:
                player.giveExp(Integer.parseInt(value));
                break;
                
            case SET_GAMEMODE:
                GameMode gameMode = GameMode.valueOf(value.toUpperCase());
                player.setGameMode(gameMode);
                break;
                
            case PLAY_SOUND:
                // Формат: SOUND:volume:pitch
                String[] soundParts = value.split(":");
                Sound sound = Sound.valueOf(soundParts[0].toUpperCase());
                float volume = soundParts.length > 1 ? Float.parseFloat(soundParts[1]) : 1.0f;
                float pitch = soundParts.length > 2 ? Float.parseFloat(soundParts[2]) : 1.0f;
                player.playSound(player.getLocation(), sound, volume, pitch);
                break;
                
            case SHOW_TITLE:
                // value = title, extra1 = subtitle, extra2 = fadeIn,stay,fadeOut
                String title = ChatColor.translateAlternateColorCodes('&', value);
                String subtitle = ChatColor.translateAlternateColorCodes('&', extra1);
                
                int fadeIn = 10, stay = 70, fadeOut = 20;
                if (!extra2.isEmpty()) {
                    String[] timings = extra2.split(",");
                    if (timings.length >= 3) {
                        fadeIn = Integer.parseInt(timings[0]);
                        stay = Integer.parseInt(timings[1]);
                        fadeOut = Integer.parseInt(timings[2]);
                    }
                }
                
                player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
                break;
                
            case SEND_ACTIONBAR:
                player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    net.md_5.bungee.api.chat.TextComponent.fromLegacyText(
                        ChatColor.translateAlternateColorCodes('&', value)));
                break;
                
            case KICK_PLAYER:
                player.kickPlayer(ChatColor.translateAlternateColorCodes('&', value));
                break;
                
            case ADD_POTION_EFFECT:
                // Формат: EFFECT:duration:amplifier
                String[] effectParts = value.split(":");
                PotionEffectType effectType = PotionEffectType.getByName(effectParts[0].toUpperCase());
                int duration = effectParts.length > 1 ? Integer.parseInt(effectParts[1]) * 20 : 200;
                int amplifier = effectParts.length > 2 ? Integer.parseInt(effectParts[2]) : 0;
                
                if (effectType != null) {
                    player.addPotionEffect(new PotionEffect(effectType, duration, amplifier));
                }
                break;
                
            case REMOVE_POTION_EFFECT:
                PotionEffectType removeType = PotionEffectType.getByName(value.toUpperCase());
                if (removeType != null) {
                    player.removePotionEffect(removeType);
                }
                break;
                
            case CLEAR_INVENTORY:
                player.getInventory().clear();
                break;
                
            case CLOSE_INVENTORY:
                player.closeInventory();
                break;
                
            case SET_FLY:
                boolean canFly = Boolean.parseBoolean(value);
                player.setAllowFlight(canFly);
                player.setFlying(canFly);
                break;
                
            case SPAWN_PARTICLE:
                // Формат: PARTICLE:count:offsetX,offsetY,offsetZ
                String[] particleParts = value.split(":");
                try {
                    Particle particle = Particle.valueOf(particleParts[0].toUpperCase());
                    int count = particleParts.length > 1 ? Integer.parseInt(particleParts[1]) : 10;
                    
                    double offsetX = 0, offsetY = 0, offsetZ = 0;
                    if (particleParts.length > 2) {
                        String[] offsets = particleParts[2].split(",");
                        if (offsets.length >= 3) {
                            offsetX = Double.parseDouble(offsets[0]);
                            offsetY = Double.parseDouble(offsets[1]);
                            offsetZ = Double.parseDouble(offsets[2]);
                        }
                    }
                    
                    player.getWorld().spawnParticle(particle, player.getLocation(), count, offsetX, offsetY, offsetZ);
                } catch (Exception e) {
                    // Ignore invalid particle
                }
                break;
                
            case STRIKE_LIGHTNING:
                player.getWorld().strikeLightning(player.getLocation());
                break;
                
            case SET_FIRE:
                int fireTicks = Integer.parseInt(value);
                player.setFireTicks(fireTicks);
                break;
                
            case EXTINGUISH:
                player.setFireTicks(0);
                break;
                
            case GIVE_MONEY:
                try {
                    ru.openhousing.OpenHousing plugin = ru.openhousing.OpenHousing.getInstance();
                    if (plugin.getEconomy() != null) {
                        double amount_money = Double.parseDouble(value);
                        plugin.getEconomy().depositPlayer(player, amount_money);
                    }
                } catch (Exception e) {
                    // Economy not available
                }
                break;
                
            case TAKE_MONEY:
                try {
                    ru.openhousing.OpenHousing plugin = ru.openhousing.OpenHousing.getInstance();
                    if (plugin.getEconomy() != null) {
                        double amount_money = Double.parseDouble(value);
                        plugin.getEconomy().withdrawPlayer(player, amount_money);
                    }
                } catch (Exception e) {
                    // Economy not available
                }
                break;
        }
    }
    
    /**
     * Замена переменных в строке
     */
    private String replaceVariables(String text, ExecutionContext context) {
        if (text == null) return "";
        
        // Замена переменных вида {variable_name}
        for (String varName : context.getVariables().keySet()) {
            Object value = context.getVariable(varName);
            if (value != null) {
                text = text.replace("{" + varName + "}", value.toString());
            }
        }
        
        // Замена основных плейсхолдеров
        Player player = context.getPlayer();
        if (player != null) {
            text = text.replace("{player}", player.getName());
            text = text.replace("{world}", player.getWorld().getName());
            text = text.replace("{x}", String.valueOf(player.getLocation().getBlockX()));
            text = text.replace("{y}", String.valueOf(player.getLocation().getBlockY()));
            text = text.replace("{z}", String.valueOf(player.getLocation().getBlockZ()));
            text = text.replace("{health}", String.valueOf(player.getHealth()));
            text = text.replace("{food}", String.valueOf(player.getFoodLevel()));
            text = text.replace("{level}", String.valueOf(player.getLevel()));
        }
        
        return text;
    }
    
    @Override
    public boolean validate() {
        return getParameter("actionType") != null && getParameter("value") != null;
    }
    
    @Override
    public List<String> getDescription() {
        PlayerActionType actionType = (PlayerActionType) getParameter("actionType");
        String value = (String) getParameter("value");
        
        return Arrays.asList(
            "§6Действие игрока",
            "§7Тип: §f" + (actionType != null ? actionType.getDisplayName() : "Не выбран"),
            "§7Значение: §f" + (value != null && !value.isEmpty() ? value : "Не указано"),
            "",
            "§8" + (actionType != null ? actionType.getDescription() : "")
        );
    }
}
