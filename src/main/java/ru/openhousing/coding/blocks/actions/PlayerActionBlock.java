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
        PlayerActionType actionType = (PlayerActionType) getParameter(ru.openhousing.coding.constants.BlockParams.ACTION_TYPE);
        String value = replaceVariables((String) getParameter(ru.openhousing.coding.constants.BlockParams.VALUE), context);
        String extra1 = replaceVariables((String) getParameter(ru.openhousing.coding.constants.BlockParams.EXTRA1), context);
        String extra2 = replaceVariables((String) getParameter(ru.openhousing.coding.constants.BlockParams.EXTRA2), context);
        
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
                try {
                    String[] itemParts = value.split(":");
                    if (itemParts.length == 0 || itemParts[0].trim().isEmpty()) {
                        throw new IllegalArgumentException("Не указан материал предмета");
                    }
                    
                    Material material;
                    try {
                        material = Material.valueOf(itemParts[0].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Неверный материал: '" + itemParts[0] + "'. Примеры: DIAMOND, STONE, APPLE");
                    }
                    
                    int amount = 1;
                    if (itemParts.length > 1) {
                        try {
                            amount = Integer.parseInt(itemParts[1].trim());
                            if (amount <= 0) {
                                throw new IllegalArgumentException("Количество должно быть больше 0");
                            }
                            if (amount > 64) {
                                throw new IllegalArgumentException("Количество не может быть больше 64");
                            }
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Неверное количество: '" + itemParts[1] + "'. Ожидается число");
                        }
                    }
                    
                    player.getInventory().addItem(new ItemStack(material, amount));
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Ошибка в GIVE_ITEM: " + e.getMessage());
                }
                break;
                
            case TAKE_ITEM:
                try {
                    String[] takeParts = value.split(":");
                    if (takeParts.length == 0 || takeParts[0].trim().isEmpty()) {
                        throw new IllegalArgumentException("Не указан материал предмета");
                    }
                    
                    Material takeMaterial;
                    try {
                        takeMaterial = Material.valueOf(takeParts[0].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Неверный материал: '" + takeParts[0] + "'. Примеры: DIAMOND, STONE, APPLE");
                    }
                    
                    int takeAmount = 1;
                    if (takeParts.length > 1) {
                        try {
                            takeAmount = Integer.parseInt(takeParts[1].trim());
                            if (takeAmount <= 0) {
                                throw new IllegalArgumentException("Количество должно быть больше 0");
                            }
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Неверное количество: '" + takeParts[1] + "'. Ожидается число");
                        }
                    }
                    
                    ItemStack takeItem = new ItemStack(takeMaterial, takeAmount);
                    player.getInventory().removeItem(takeItem);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Ошибка в TAKE_ITEM: " + e.getMessage());
                }
                break;
                
            case SET_HEALTH:
                try {
                    double health = Double.parseDouble(value);
                    if (health < 0 || health > 20) {
                        throw new IllegalArgumentException("Здоровье должно быть от 0 до 20");
                    }
                    player.setHealth(health);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Ошибка в SET_HEALTH: '" + value + "' не является числом");
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Ошибка в SET_HEALTH: " + e.getMessage());
                }
                break;
                
            case SET_FOOD:
                try {
                    int food = Integer.parseInt(value);
                    if (food < 0 || food > 20) {
                        throw new IllegalArgumentException("Уровень еды должен быть от 0 до 20");
                    }
                    player.setFoodLevel(food);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Ошибка в SET_FOOD: '" + value + "' не является числом");
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Ошибка в SET_FOOD: " + e.getMessage());
                }
                break;
                
            case SET_LEVEL:
                try {
                    int level = Integer.parseInt(value);
                    if (level < 0) {
                        throw new IllegalArgumentException("Уровень не может быть отрицательным");
                    }
                    player.setLevel(level);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Ошибка в SET_LEVEL: '" + value + "' не является числом");
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Ошибка в SET_LEVEL: " + e.getMessage());
                }
                break;
                
            case ADD_EXPERIENCE:
                try {
                    int exp = Integer.parseInt(value);
                    if (exp < 0) {
                        throw new IllegalArgumentException("Опыт не может быть отрицательным");
                    }
                    player.giveExp(exp);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Ошибка в ADD_EXPERIENCE: '" + value + "' не является числом");
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Ошибка в ADD_EXPERIENCE: " + e.getMessage());
                }
                break;
                
            case SET_GAMEMODE:
                try {
                    GameMode gameMode = GameMode.valueOf(value.toUpperCase());
                    player.setGameMode(gameMode);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Ошибка в SET_GAMEMODE: '" + value + "' не является допустимым режимом игры. Допустимые: SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR");
                }
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
        return ru.openhousing.utils.CodeBlockUtils.replaceVariables(text, context);
    }
    
    @Override
    public boolean validate() {
        return getParameter(ru.openhousing.coding.constants.BlockParams.ACTION_TYPE) != null && getParameter(ru.openhousing.coding.constants.BlockParams.VALUE) != null;
    }
    
    @Override
    public List<String> getDescription() {
        PlayerActionType actionType = (PlayerActionType) getParameter(ru.openhousing.coding.constants.BlockParams.ACTION_TYPE);
        String value = (String) getParameter(ru.openhousing.coding.constants.BlockParams.VALUE);
        
        return Arrays.asList(
            "§6Действие игрока",
            "§7Тип: §f" + (actionType != null ? actionType.getDisplayName() : "Не выбран"),
            "§7Значение: §f" + (value != null && !value.isEmpty() ? value : "Не указано"),
            "",
            "§8" + (actionType != null ? actionType.getDescription() : "")
        );
    }
}
