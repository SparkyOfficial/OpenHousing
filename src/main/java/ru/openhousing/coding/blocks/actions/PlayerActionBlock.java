package ru.openhousing.coding.blocks.actions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.actions.PlayerActionBlock.PlayerActionType;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
                try {
                    PotionEffectType removeType = PotionEffectType.getByName(value.toUpperCase());
                    if (removeType != null) {
                        player.removePotionEffect(removeType);
                    } else {
                        player.sendMessage("§c[OpenHousing] Неверный тип эффекта для удаления: '" + value + "'. Примеры: SPEED, JUMP, STRENGTH, INVISIBILITY");
                    }
                } catch (Exception e) {
                    player.sendMessage("§c[OpenHousing] Ошибка при удалении эффекта: '" + value + "'");
                }
                break;
                
            case CLEAR_INVENTORY:
                player.getInventory().clear();
                break;
                
            case OPEN_INVENTORY:
                // Реализована логика открытия инвентаря
                if (extra1 != null && !extra1.isEmpty()) {
                    try {
                        // Пытаемся найти игрока по имени
                        Player targetPlayer = Bukkit.getPlayer(extra1);
                        if (targetPlayer != null) {
                            player.openInventory(targetPlayer.getInventory());
                            player.sendMessage("§a[OpenHousing] Открыт инвентарь игрока: " + targetPlayer.getName());
                        } else {
                            player.sendMessage("§c[OpenHousing] Игрок не найден: " + extra1);
                        }
                    } catch (Exception e) {
                        player.sendMessage("§c[OpenHousing] Ошибка при открытии инвентаря: " + e.getMessage());
                    }
                } else {
                    // Открываем собственный инвентарь
                    player.openInventory(player.getInventory());
                    player.sendMessage("§a[OpenHousing] Открыт ваш инвентарь!");
                }
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
                    
                    // Проверяем разумность количества частиц
                    if (count < 1 || count > 1000) {
                        player.sendMessage("§c[OpenHousing] Количество частиц должно быть от 1 до 1000. Используется значение по умолчанию: 10");
                        count = 10;
                    }
                    
                    double offsetX = 0, offsetY = 0, offsetZ = 0;
                    if (particleParts.length > 2) {
                        String[] offsets = particleParts[2].split(",");
                        if (offsets.length >= 3) {
                            try {
                                offsetX = Double.parseDouble(offsets[0]);
                                offsetY = Double.parseDouble(offsets[1]);
                                offsetZ = Double.parseDouble(offsets[2]);
                                
                                // Проверяем разумность смещений
                                if (Math.abs(offsetX) > 5.0 || Math.abs(offsetY) > 5.0 || Math.abs(offsetZ) > 5.0) {
                                    player.sendMessage("§c[OpenHousing] Смещения частиц слишком большие. Максимум: ±5.0. Используются значения по умолчанию: 0,0,0");
                                    offsetX = offsetY = offsetZ = 0;
                                }
                            } catch (NumberFormatException e) {
                                player.sendMessage("§c[OpenHousing] Неверные значения смещений частиц: '" + particleParts[2] + "'. Используются значения по умолчанию: 0,0,0");
                                offsetX = offsetY = offsetZ = 0;
                            }
                        }
                    }
                    
                    player.getWorld().spawnParticle(particle, player.getLocation(), count, offsetX, offsetY, offsetZ);
                } catch (IllegalArgumentException e) {
                    // Неверное имя частицы
                    player.sendMessage("§c[OpenHousing] Неверное имя частицы: '" + particleParts[0] + "'. Примеры: EXPLOSION_NORMAL, FLAME, HEART, NOTE");
                } catch (Exception e) {
                    player.sendMessage("§c[OpenHousing] Ошибка при создании частиц: " + e.getMessage());
                }
                break;
                
            case STRIKE_LIGHTNING:
                player.getWorld().strikeLightning(player.getLocation());
                break;
                
            case SET_FIRE:
                try {
                    int fireTicks = Integer.parseInt(value);
                    if (fireTicks < 0) {
                        player.sendMessage("§c[OpenHousing] Время горения не может быть отрицательным. Устанавливается 0.");
                        fireTicks = 0;
                    } else if (fireTicks > 600) { // 30 секунд максимум
                        player.sendMessage("§c[OpenHousing] Время горения слишком большое: " + fireTicks + " тиков. Максимум: 600 (30 сек)");
                        fireTicks = 600;
                    }
                    player.setFireTicks(fireTicks);
                } catch (NumberFormatException e) {
                    player.sendMessage("§c[OpenHousing] Неверное время горения: '" + value + "'. Ожидается число от 0 до 600");
                }
                break;
                
            case EXTINGUISH:
                player.setFireTicks(0);
                break;
                
            case GIVE_MONEY:
                try {
                    ru.openhousing.OpenHousing plugin = ru.openhousing.OpenHousing.getInstance();
                    if (plugin.getEconomy() != null) {
                        double amount_money = Double.parseDouble(value);
                        if (amount_money < 0) {
                            player.sendMessage("§c[OpenHousing] Сумма денег не может быть отрицательной.");
                            return;
                        } else if (amount_money > 1000000) {
                            player.sendMessage("§c[OpenHousing] Слишком большая сумма: " + amount_money + ". Максимум: 1,000,000");
                            return;
                        }
                        plugin.getEconomy().depositPlayer(player, amount_money);
                        player.sendMessage("§a[OpenHousing] Получено " + amount_money + " денег!");
                    } else {
                        player.sendMessage("§c[OpenHousing] Экономика недоступна на этом сервере");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("§c[OpenHousing] Неверная сумма денег: '" + value + "'. Ожидается число");
                } catch (Exception e) {
                    player.sendMessage("§c[OpenHousing] Ошибка при выдаче денег: " + e.getMessage());
                }
                break;
                
            case TAKE_MONEY:
                try {
                    ru.openhousing.OpenHousing plugin = ru.openhousing.OpenHousing.getInstance();
                    if (plugin.getEconomy() != null) {
                        double amount_money = Double.parseDouble(value);
                        if (amount_money < 0) {
                            player.sendMessage("§c[OpenHousing] Сумма денег не может быть отрицательной.");
                            return;
                        } else if (amount_money > 1000000) {
                            player.sendMessage("§c[OpenHousing] Слишком большая сумма: " + amount_money + ". Максимум: 1,000,000");
                            return;
                        }
                        
                        double balance = plugin.getEconomy().getBalance(player);
                        if (balance < amount_money) {
                            player.sendMessage("§c[OpenHousing] Недостаточно денег! Баланс: " + balance + ", требуется: " + amount_money);
                            return;
                        }
                        
                        plugin.getEconomy().withdrawPlayer(player, amount_money);
                        player.sendMessage("§a[OpenHousing] Списано " + amount_money + " денег. Новый баланс: " + plugin.getEconomy().getBalance(player));
                    } else {
                        player.sendMessage("§c[OpenHousing] Экономика недоступна на этом сервере");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("§c[OpenHousing] Неверная сумма денег: '" + value + "'. Ожидается число");
                } catch (Exception e) {
                    player.sendMessage("§c[OpenHousing] Ошибка при списании денег: " + e.getMessage());
                }
                break;
                
            case HIDE_PLAYER:
                // Реализована логика скрытия игрока
                if (extra1 != null && !extra1.isEmpty()) {
                    try {
                        Player targetPlayer = Bukkit.getPlayer(extra1);
                        if (targetPlayer != null) {
                            player.hidePlayer(OpenHousing.getInstance(), targetPlayer);
                            player.sendMessage("§a[OpenHousing] Игрок " + targetPlayer.getName() + " скрыт от вас!");
                        } else {
                            player.sendMessage("§c[OpenHousing] Игрок не найден: " + extra1);
                        }
                    } catch (Exception e) {
                        player.sendMessage("§c[OpenHousing] Ошибка при скрытии игрока: " + e.getMessage());
                    }
                } else {
                    player.sendMessage("§c[OpenHousing] Укажите имя игрока для скрытия!");
                }
                break;
                
            case SHOW_PLAYER:
                // Реализована логика показа игрока
                if (extra1 != null && !extra1.isEmpty()) {
                    try {
                        Player targetPlayer = Bukkit.getPlayer(extra1);
                        if (targetPlayer != null) {
                            player.showPlayer(OpenHousing.getInstance(), targetPlayer);
                            player.sendMessage("§a[OpenHousing] Игрок " + targetPlayer.getName() + " снова видим для вас!");
                        } else {
                            player.sendMessage("§c[OpenHousing] Игрок не найден: " + extra1);
                        }
                    } catch (Exception e) {
                        player.sendMessage("§c[OpenHousing] Ошибка при показе игрока: " + e.getMessage());
                    }
                } else {
                    player.sendMessage("§c[OpenHousing] Укажите имя игрока для показа!");
                }
                break;
                
            case SEND_TO_SERVER:
                // Реализована логика отправки на другой сервер (BungeeCord)
                if (extra1 != null && !extra1.isEmpty()) {
                    try {
                        // Проверяем, поддерживается ли BungeeCord
                        if (player.getClass().getMethod("sendPluginMessage", String.class, byte[].class) != null) {
                            // Отправляем команду на BungeeCord
                            String command = "connect " + extra1;
                            player.performCommand(command);
                            player.sendMessage("§a[OpenHousing] Отправка на сервер: " + extra1);
                        } else {
                            player.sendMessage("§c[OpenHousing] BungeeCord не поддерживается на этом сервере!");
                        }
                    } catch (Exception e) {
                        player.sendMessage("§c[OpenHousing] Ошибка при отправке на сервер: " + e.getMessage());
                    }
                } else {
                    player.sendMessage("§c[OpenHousing] Укажите название сервера для перехода!");
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
