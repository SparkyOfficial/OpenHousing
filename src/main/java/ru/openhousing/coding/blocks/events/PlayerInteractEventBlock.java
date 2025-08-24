package ru.openhousing.coding.blocks.events;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionContext;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionResult;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Блок события взаимодействия игрока с блоками
 * Обрабатывает клики по блокам, использование предметов, открытие инвентарей
 */
public class PlayerInteractEventBlock extends CodeBlock {

    public PlayerInteractEventBlock() {
        super(BlockType.PLAYER_INTERACT);
        initializeDefaultParameters();
    }

    private void initializeDefaultParameters() {
        setParameter("enabled", true);
        setParameter("checkBlockType", false);
        setParameter("blockType", "STONE");
        setParameter("checkItemType", false);
        setParameter("itemType", "STONE");
        setParameter("checkAction", false);
        setParameter("actionType", "RIGHT_CLICK");
        setParameter("checkHand", false);
        setParameter("handType", "MAIN_HAND");
        setParameter("checkPermission", false);
        setParameter("permission", "openhousing.interact");
        
        // Действия при взаимодействии
        setParameter("sendMessage", false);
        setParameter("message", "Вы взаимодействуете с блоком!");
        setParameter("playSound", false);
        setParameter("soundType", "BLOCK_STONE_PLACE");
        setParameter("soundVolume", "1.0");
        setParameter("soundPitch", "1.0");
        setParameter("showTitle", false);
        setParameter("title", "Взаимодействие");
        setParameter("subtitle", "Блок активирован");
        setParameter("titleFadeIn", "10");
        setParameter("titleStay", "20");
        setParameter("titleFadeOut", "10");
        
        // Эффекты
        setParameter("addPotionEffect", false);
        setParameter("potionEffectType", "SPEED");
        setParameter("potionEffectDuration", "60");
        setParameter("potionEffectAmplifier", "1");
        setParameter("spawnParticles", false);
        setParameter("particleType", "CLOUD");
        setParameter("particleCount", "10");
        
        // Изменение блока
        setParameter("changeBlock", false);
        setParameter("newBlockType", "AIR");
        setParameter("dropOriginal", true);
        setParameter("breakBlock", false);
        setParameter("giveItem", false);
        setParameter("itemToGive", "STONE");
        setParameter("itemAmount", "1");
        
        // Телепортация
        setParameter("teleportPlayer", false);
        setParameter("teleportX", "0");
        setParameter("teleportY", "64");
        setParameter("teleportZ", "0");
        setParameter("teleportWorld", "world");
        
        // Ограничения
        setParameter("cancelInteraction", false);
        setParameter("requireItem", false);
        setParameter("requiredItem", "STONE");
        setParameter("consumeItem", false);
        setParameter("cooldown", false);
        setParameter("cooldownTime", "5");
        
        // Статистика
        setParameter("trackInteractions", false);
        setParameter("trackBlockType", false);
        setParameter("saveToDatabase", false);
        
        // Уведомления
        setParameter("notifyOwner", false);
        setParameter("notifyMessage", "Игрок {player} взаимодействует с блоком в вашем доме");
        setParameter("broadcastToHouse", false);
        setParameter("broadcastMessage", "Игрок {player} активировал блок");
        
        // Логирование
        setParameter("logInteraction", false);
        setParameter("logFormat", "[{time}] {player} interacted with {block} at {location}");
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден");
        }

        try {
            // Получаем параметры
            boolean enabled = (Boolean) getParameter("enabled");
            if (!enabled) {
                return ExecutionResult.success();
            }

            // Проверяем тип блока
            if ((Boolean) getParameter("checkBlockType")) {
                String requiredBlockType = (String) getParameter("blockType");
                Object blockTypeObj = context.getVariable("blockType");
                if (blockTypeObj instanceof Material) {
                    Material blockType = (Material) blockTypeObj;
                    if (!blockType.name().equals(requiredBlockType)) {
                        return ExecutionResult.success(); // Неправильный тип блока
                    }
                }
            }

            // Проверяем тип предмета
            if ((Boolean) getParameter("checkItemType")) {
                String requiredItemType = (String) getParameter("itemType");
                Object itemTypeObj = context.getVariable("itemType");
                if (itemTypeObj instanceof Material) {
                    Material itemType = (Material) itemTypeObj;
                    if (!itemType.name().equals(requiredItemType)) {
                        return ExecutionResult.success(); // Неправильный тип предмета
                    }
                }
            }

            // Проверяем действие
            if ((Boolean) getParameter("checkAction")) {
                String requiredAction = (String) getParameter("actionType");
                Object actionObj = context.getVariable("actionType");
                if (actionObj instanceof String) {
                    String action = (String) actionObj;
                    if (!action.equals(requiredAction)) {
                        return ExecutionResult.success(); // Неправильное действие
                    }
                }
            }

            // Проверяем руку
            if ((Boolean) getParameter("checkHand")) {
                String requiredHand = (String) getParameter("handType");
                Object handObj = context.getVariable("handType");
                if (handObj instanceof String) {
                    String hand = (String) handObj;
                    if (!hand.equals(requiredHand)) {
                        return ExecutionResult.success(); // Неправильная рука
                    }
                }
            }

            // Проверяем права
            if ((Boolean) getParameter("checkPermission")) {
                String permission = (String) getParameter("permission");
                if (!player.hasPermission(permission)) {
                    return ExecutionResult.error("Недостаточно прав");
                }
            }

            // Проверяем кулдаун
            if ((Boolean) getParameter("cooldown")) {
                int cooldownTime = Integer.parseInt((String) getParameter("cooldownTime"));
                String cooldownKey = "interactCooldown_" + player.getUniqueId();
                Object lastInteractionObj = context.getVariable(cooldownKey);
                
                if (lastInteractionObj instanceof Long) {
                    Long lastInteraction = (Long) lastInteractionObj;
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastInteraction < cooldownTime * 1000) {
                        return ExecutionResult.error("Подождите перед следующим взаимодействием");
                    }
                }
                
                context.setVariable(cooldownKey, System.currentTimeMillis());
            }

            // Выполняем действия
            executeActions(player, context);

            return ExecutionResult.success();
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения PlayerInteractEventBlock: " + e.getMessage());
        }
    }

    private void executeActions(Player player, ExecutionContext context) {
        // Отправляем сообщение
        if ((Boolean) getParameter("sendMessage")) {
            String message = (String) getParameter("message");
            message = replacePlaceholders(message, player, context);
            player.sendMessage(message);
        }

        // Воспроизводим звук
        if ((Boolean) getParameter("playSound")) {
            try {
                String soundType = (String) getParameter("soundType");
                float volume = Float.parseFloat((String) getParameter("soundVolume"));
                float pitch = Float.parseFloat((String) getParameter("soundPitch"));
                
                Sound sound = Sound.valueOf(soundType);
                player.playSound(player.getLocation(), sound, volume, pitch);
            } catch (Exception e) {
                // Игнорируем ошибки звука
            }
        }

        // Показываем заголовок
        if ((Boolean) getParameter("showTitle")) {
            String title = (String) getParameter("title");
            String subtitle = (String) getParameter("subtitle");
            int fadeIn = Integer.parseInt((String) getParameter("titleFadeIn"));
            int stay = Integer.parseInt((String) getParameter("titleStay"));
            int fadeOut = Integer.parseInt((String) getParameter("titleFadeOut"));
            
            title = replacePlaceholders(title, player, context);
            subtitle = replacePlaceholders(subtitle, player, context);
            
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }

        // Добавляем эффект зелья
        if ((Boolean) getParameter("addPotionEffect")) {
            try {
                String effectType = (String) getParameter("potionEffectType");
                int duration = Integer.parseInt((String) getParameter("potionEffectDuration"));
                int amplifier = Integer.parseInt((String) getParameter("potionEffectAmplifier"));
                
                PotionEffectType type = PotionEffectType.getByName(effectType);
                if (type != null) {
                    PotionEffect effect = new PotionEffect(type, duration, amplifier);
                    player.addPotionEffect(effect);
                }
            } catch (Exception e) {
                // Игнорируем ошибки эффектов
            }
        }

        // Создаем частицы
        if ((Boolean) getParameter("spawnParticles")) {
            try {
                String particleType = (String) getParameter("particleType");
                int count = Integer.parseInt((String) getParameter("particleCount"));
                
                org.bukkit.Particle particle = org.bukkit.Particle.valueOf(particleType);
                player.getWorld().spawnParticle(particle, player.getLocation(), count);
            } catch (Exception e) {
                // Игнорируем ошибки частиц
            }
        }

        // Изменяем блок
        if ((Boolean) getParameter("changeBlock")) {
            try {
                String newBlockType = (String) getParameter("newBlockType");
                boolean dropOriginal = (Boolean) getParameter("dropOriginal");
                
                Object locationObj = context.getVariable("interactionLocation");
                if (locationObj instanceof Location) {
                    Location location = (Location) locationObj;
                    Material newMaterial = Material.valueOf(newBlockType);
                    location.getBlock().setType(newMaterial);
                    
                    if (dropOriginal) {
                        Object originalBlockObj = context.getVariable("originalBlockType");
                        if (originalBlockObj instanceof Material) {
                            Material originalBlock = (Material) originalBlockObj;
                            location.getWorld().dropItemNaturally(location, new org.bukkit.inventory.ItemStack(originalBlock));
                        }
                    }
                }
            } catch (Exception e) {
                // Игнорируем ошибки изменения блока
            }
        }

        // Ломаем блок
        if ((Boolean) getParameter("breakBlock")) {
            try {
                Object locationObj = context.getVariable("interactionLocation");
                if (locationObj instanceof Location) {
                    Location location = (Location) locationObj;
                    location.getBlock().setType(Material.AIR);
                }
            } catch (Exception e) {
                // Игнорируем ошибки ломания блока
            }
        }

        // Даем предмет
        if ((Boolean) getParameter("giveItem")) {
            try {
                String itemType = (String) getParameter("itemToGive");
                int amount = Integer.parseInt((String) getParameter("itemAmount"));
                
                Material material = Material.valueOf(itemType);
                org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(material, amount);
                player.getInventory().addItem(item);
            } catch (Exception e) {
                // Игнорируем ошибки выдачи предметов
            }
        }

        // Телепортируем игрока
        if ((Boolean) getParameter("teleportPlayer")) {
            try {
                double x = Double.parseDouble((String) getParameter("teleportX"));
                double y = Double.parseDouble((String) getParameter("teleportY"));
                double z = Double.parseDouble((String) getParameter("teleportZ"));
                String worldName = (String) getParameter("teleportWorld");
                
                org.bukkit.World world = player.getServer().getWorld(worldName);
                if (world != null) {
                    Location location = new Location(world, x, y, z);
                    player.teleport(location);
                }
            } catch (Exception e) {
                // Игнорируем ошибки телепортации
            }
        }

        // Потребляем предмет
        if ((Boolean) getParameter("consumeItem")) {
            try {
                org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    player.getInventory().setItemInMainHand(null);
                }
            } catch (Exception e) {
                // Игнорируем ошибки потребления предметов
            }
        }

        // Отменяем взаимодействие
        if ((Boolean) getParameter("cancelInteraction")) {
            context.setVariable("cancelInteraction", true);
        }

        // Сохраняем статистику
        if ((Boolean) getParameter("trackInteractions")) {
            String key = "totalInteractions_" + player.getUniqueId();
            Object interactionsObj = context.getVariable(key);
            Integer interactions = (interactionsObj instanceof Integer) ? (Integer) interactionsObj : 0;
            context.setVariable(key, interactions + 1);
        }

        if ((Boolean) getParameter("trackBlockType")) {
            Object blockTypeObj = context.getVariable("blockType");
            if (blockTypeObj instanceof Material) {
                Material blockType = (Material) blockTypeObj;
                String key = "interactionsWith_" + blockType.name() + "_" + player.getUniqueId();
                Object countObj = context.getVariable(key);
                Integer count = (countObj instanceof Integer) ? (Integer) countObj : 0;
                context.setVariable(key, count + 1);
            }
        }

        // Уведомляем владельца
        if ((Boolean) getParameter("notifyOwner")) {
            String message = (String) getParameter("notifyMessage");
            message = replacePlaceholders(message, player, context);
            // TODO: Найти владельца дома и отправить сообщение
        }

        // Вещаем в дом
        if ((Boolean) getParameter("broadcastToHouse")) {
            String message = (String) getParameter("broadcastMessage");
            message = replacePlaceholders(message, player, context);
            // TODO: Отправить сообщение всем игрокам в доме
        }

        // Логируем
        if ((Boolean) getParameter("logInteraction")) {
            String format = (String) getParameter("logFormat");
            String log = replacePlaceholders(format, player, context);
            // TODO: Записать в лог
        }
    }

    private String replacePlaceholders(String text, Player player, ExecutionContext context) {
        if (text == null) return "";
        
        String result = text
            .replace("{player}", player.getName())
            .replace("{uuid}", player.getUniqueId().toString())
            .replace("{world}", player.getWorld().getName())
            .replace("{x}", String.valueOf(player.getLocation().getBlockX()))
            .replace("{y}", String.valueOf(player.getLocation().getBlockY()))
            .replace("{z}", String.valueOf(player.getLocation().getBlockZ()))
            .replace("{time}", String.valueOf(System.currentTimeMillis()))
            .replace("{gamemode}", player.getGameMode().name())
            .replace("{health}", String.valueOf(player.getHealth()))
            .replace("{maxHealth}", String.valueOf(player.getMaxHealth()))
            .replace("{food}", String.valueOf(player.getFoodLevel()))
            .replace("{level}", String.valueOf(player.getLevel()))
            .replace("{exp}", String.valueOf(player.getExp()));
        
        // Добавляем специфичные для взаимодействия плейсхолдеры
        Object blockTypeObj = context.getVariable("blockType");
        if (blockTypeObj instanceof Material) {
            result = result.replace("{block}", ((Material) blockTypeObj).name());
        }
        
        Object locationObj = context.getVariable("interactionLocation");
        if (locationObj instanceof Location) {
            Location location = (Location) locationObj;
            result = result.replace("{location}", 
                String.format("%.1f,%.1f,%.1f", location.getX(), location.getY(), location.getZ()));
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("enabled", getParameter("enabled"));
        params.put("checkBlockType", getParameter("checkBlockType"));
        params.put("blockType", getParameter("blockType"));
        params.put("checkItemType", getParameter("checkItemType"));
        params.put("itemType", getParameter("itemType"));
        params.put("checkAction", getParameter("checkAction"));
        params.put("actionType", getParameter("actionType"));
        params.put("checkHand", getParameter("checkHand"));
        params.put("handType", getParameter("handType"));
        params.put("checkPermission", getParameter("checkPermission"));
        params.put("permission", getParameter("permission"));
        params.put("sendMessage", getParameter("sendMessage"));
        params.put("message", getParameter("message"));
        params.put("playSound", getParameter("playSound"));
        params.put("soundType", getParameter("soundType"));
        params.put("soundVolume", getParameter("soundVolume"));
        params.put("soundPitch", getParameter("soundPitch"));
        params.put("showTitle", getParameter("showTitle"));
        params.put("title", getParameter("title"));
        params.put("subtitle", getParameter("subtitle"));
        params.put("titleFadeIn", getParameter("titleFadeIn"));
        params.put("titleStay", getParameter("titleStay"));
        params.put("titleFadeOut", getParameter("titleFadeOut"));
        params.put("addPotionEffect", getParameter("addPotionEffect"));
        params.put("potionEffectType", getParameter("potionEffectType"));
        params.put("potionEffectDuration", getParameter("potionEffectDuration"));
        params.put("potionEffectAmplifier", getParameter("potionEffectAmplifier"));
        params.put("spawnParticles", getParameter("spawnParticles"));
        params.put("particleType", getParameter("particleType"));
        params.put("particleCount", getParameter("particleCount"));
        params.put("changeBlock", getParameter("changeBlock"));
        params.put("newBlockType", getParameter("newBlockType"));
        params.put("dropOriginal", getParameter("dropOriginal"));
        params.put("breakBlock", getParameter("breakBlock"));
        params.put("giveItem", getParameter("giveItem"));
        params.put("itemToGive", getParameter("itemToGive"));
        params.put("itemAmount", getParameter("itemAmount"));
        params.put("teleportPlayer", getParameter("teleportPlayer"));
        params.put("teleportX", getParameter("teleportX"));
        params.put("teleportY", getParameter("teleportY"));
        params.put("teleportZ", getParameter("teleportZ"));
        params.put("teleportWorld", getParameter("teleportWorld"));
        params.put("cancelInteraction", getParameter("cancelInteraction"));
        params.put("requireItem", getParameter("requireItem"));
        params.put("requiredItem", getParameter("requiredItem"));
        params.put("consumeItem", getParameter("consumeItem"));
        params.put("cooldown", getParameter("cooldown"));
        params.put("cooldownTime", getParameter("cooldownTime"));
        params.put("trackInteractions", getParameter("trackInteractions"));
        params.put("trackBlockType", getParameter("trackBlockType"));
        params.put("saveToDatabase", getParameter("saveToDatabase"));
        params.put("notifyOwner", getParameter("notifyOwner"));
        params.put("notifyMessage", getParameter("notifyMessage"));
        params.put("broadcastToHouse", getParameter("broadcastToHouse"));
        params.put("broadcastMessage", getParameter("broadcastMessage"));
        params.put("logInteraction", getParameter("logInteraction"));
        params.put("logFormat", getParameter("logFormat"));
        return params;
    }

    @Override
    public boolean validate() {
        return true; // Базовая валидация
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("Блок события взаимодействия игрока");
        description.add("Обрабатывает взаимодействие с блоками:");
        description.add("- Клики по блокам");
        description.add("- Использование предметов");
        description.add("- Открытие инвентарей");
        description.add("- Изменение блоков");
        description.add("- Телепортация");
        description.add("- Кулдауны и ограничения");
        return description;
    }
}
