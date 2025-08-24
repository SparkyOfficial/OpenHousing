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
 * Блок события движения игрока
 * Обрабатывает различные события движения: ходьба, бег, прыжки, полет
 */
public class PlayerMoveEventBlock extends CodeBlock {

    public PlayerMoveEventBlock() {
        super(BlockType.PLAYER_MOVE);
        initializeDefaultParameters();
    }

    private void initializeDefaultParameters() {
        setParameter("enabled", true);
        setParameter("checkDistance", true);
        setParameter("minDistance", "1.0");
        setParameter("checkSpeed", false);
        setParameter("minSpeed", "0.1");
        setParameter("checkYMovement", false);
        setParameter("checkJumping", false);
        setParameter("checkFlying", false);
        setParameter("checkSneaking", false);
        setParameter("checkSprinting", false);
        
        // Действия при движении
        setParameter("sendMessage", false);
        setParameter("message", "Вы двигаетесь!");
        setParameter("playSound", false);
        setParameter("soundType", "ENTITY_PLAYER_LEVELUP");
        setParameter("soundVolume", "1.0");
        setParameter("soundPitch", "1.0");
        setParameter("showTitle", false);
        setParameter("title", "Движение");
        setParameter("subtitle", "Вы перемещаетесь");
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
        
        // Телепортация
        setParameter("teleportToLocation", false);
        setParameter("teleportX", "0");
        setParameter("teleportY", "64");
        setParameter("teleportZ", "0");
        setParameter("teleportWorld", "world");
        
        // Ограничения
        setParameter("cancelMovement", false);
        setParameter("setWalkSpeed", false);
        setParameter("walkSpeed", "0.2");
        setParameter("setFlySpeed", false);
        setParameter("flySpeed", "0.1");
        
        // Статистика
        setParameter("trackDistance", false);
        setParameter("trackSteps", false);
        setParameter("trackTime", false);
        setParameter("saveToDatabase", false);
        
        // Уведомления
        setParameter("notifyOwner", false);
        setParameter("notifyMessage", "Игрок {player} двигается в вашем доме");
        setParameter("broadcastToHouse", false);
        setParameter("broadcastMessage", "Игрок {player} перемещается");
        
        // Логирование
        setParameter("logMovement", false);
        setParameter("logFormat", "[{time}] {player} moved from {from} to {to}");
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

            // Проверяем расстояние
            if ((Boolean) getParameter("checkDistance")) {
                double minDistance = Double.parseDouble((String) getParameter("minDistance"));
                Object fromObj = context.getVariable("fromLocation");
                Object toObj = context.getVariable("toLocation");
                
                if (fromObj instanceof Location && toObj instanceof Location) {
                    Location from = (Location) fromObj;
                    Location to = (Location) toObj;
                    double distance = from.distance(to);
                    if (distance < minDistance) {
                        return ExecutionResult.success(); // Слишком маленькое движение
                    }
                }
            }

            // Проверяем скорость
            if ((Boolean) getParameter("checkSpeed")) {
                double minSpeed = Double.parseDouble((String) getParameter("minSpeed"));
                Object speedObj = context.getVariable("movementSpeed");
                if (speedObj instanceof Double) {
                    Double speed = (Double) speedObj;
                    if (speed < minSpeed) {
                        return ExecutionResult.success(); // Слишком медленное движение
                    }
                }
            }

            // Проверяем тип движения
            if ((Boolean) getParameter("checkJumping")) {
                Object jumpObj = context.getVariable("isJumping");
                if (!(jumpObj instanceof Boolean) || !(Boolean) jumpObj) {
                    return ExecutionResult.success(); // Не прыжок
                }
            }

            if ((Boolean) getParameter("checkFlying")) {
                Object flyObj = context.getVariable("isFlying");
                if (!(flyObj instanceof Boolean) || !(Boolean) flyObj) {
                    return ExecutionResult.success(); // Не полет
                }
            }

            if ((Boolean) getParameter("checkSneaking")) {
                Object sneakObj = context.getVariable("isSneaking");
                if (!(sneakObj instanceof Boolean) || !(Boolean) sneakObj) {
                    return ExecutionResult.success(); // Не крадется
                }
            }

            if ((Boolean) getParameter("checkSprinting")) {
                Object sprintObj = context.getVariable("isSprinting");
                if (!(sprintObj instanceof Boolean) || !(Boolean) sprintObj) {
                    return ExecutionResult.success(); // Не бежит
                }
            }

            // Выполняем действия
            executeActions(player, context);

            return ExecutionResult.success();
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения PlayerMoveEventBlock: " + e.getMessage());
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

        // Телепортируем
        if ((Boolean) getParameter("teleportToLocation")) {
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

        // Устанавливаем скорость
        if ((Boolean) getParameter("setWalkSpeed")) {
            float speed = Float.parseFloat((String) getParameter("walkSpeed"));
            player.setWalkSpeed(speed);
        }

        if ((Boolean) getParameter("setFlySpeed")) {
            float speed = Float.parseFloat((String) getParameter("flySpeed"));
            player.setFlySpeed(speed);
        }

        // Отменяем движение
        if ((Boolean) getParameter("cancelMovement")) {
            context.setVariable("cancelMovement", true);
        }

        // Сохраняем статистику
        if ((Boolean) getParameter("trackDistance")) {
            Object fromObj = context.getVariable("fromLocation");
            Object toObj = context.getVariable("toLocation");
            if (fromObj instanceof Location && toObj instanceof Location) {
                Location from = (Location) fromObj;
                Location to = (Location) toObj;
                double distance = from.distance(to);
                String key = "totalDistance_" + player.getUniqueId();
                Object totalDistanceObj = context.getVariable(key);
                Double totalDistance = (totalDistanceObj instanceof Double) ? (Double) totalDistanceObj : 0.0;
                context.setVariable(key, totalDistance + distance);
            }
        }

        if ((Boolean) getParameter("trackSteps")) {
            String key = "totalSteps_" + player.getUniqueId();
            Object stepsObj = context.getVariable(key);
            Integer steps = (stepsObj instanceof Integer) ? (Integer) stepsObj : 0;
            context.setVariable(key, steps + 1);
        }

        if ((Boolean) getParameter("trackTime")) {
            String key = "movementTime_" + player.getUniqueId();
            Object startTimeObj = context.getVariable(key);
            if (!(startTimeObj instanceof Long)) {
                context.setVariable(key, System.currentTimeMillis());
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
        if ((Boolean) getParameter("logMovement")) {
            String format = (String) getParameter("logFormat");
            String log = replacePlaceholders(format, player, context);
            // TODO: Записать в лог
        }
    }

    private String replacePlaceholders(String text, Player player, ExecutionContext context) {
        if (text == null) return "";
        
        return text
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
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("enabled", getParameter("enabled"));
        params.put("checkDistance", getParameter("checkDistance"));
        params.put("minDistance", getParameter("minDistance"));
        params.put("checkSpeed", getParameter("checkSpeed"));
        params.put("minSpeed", getParameter("minSpeed"));
        params.put("checkYMovement", getParameter("checkYMovement"));
        params.put("checkJumping", getParameter("checkJumping"));
        params.put("checkFlying", getParameter("checkFlying"));
        params.put("checkSneaking", getParameter("checkSneaking"));
        params.put("checkSprinting", getParameter("checkSprinting"));
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
        params.put("teleportToLocation", getParameter("teleportToLocation"));
        params.put("teleportX", getParameter("teleportX"));
        params.put("teleportY", getParameter("teleportY"));
        params.put("teleportZ", getParameter("teleportZ"));
        params.put("teleportWorld", getParameter("teleportWorld"));
        params.put("cancelMovement", getParameter("cancelMovement"));
        params.put("setWalkSpeed", getParameter("setWalkSpeed"));
        params.put("walkSpeed", getParameter("walkSpeed"));
        params.put("setFlySpeed", getParameter("setFlySpeed"));
        params.put("flySpeed", getParameter("flySpeed"));
        params.put("trackDistance", getParameter("trackDistance"));
        params.put("trackSteps", getParameter("trackSteps"));
        params.put("trackTime", getParameter("trackTime"));
        params.put("saveToDatabase", getParameter("saveToDatabase"));
        params.put("notifyOwner", getParameter("notifyOwner"));
        params.put("notifyMessage", getParameter("notifyMessage"));
        params.put("broadcastToHouse", getParameter("broadcastToHouse"));
        params.put("broadcastMessage", getParameter("broadcastMessage"));
        params.put("logMovement", getParameter("logMovement"));
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
        description.add("Блок события движения игрока");
        description.add("Обрабатывает различные события движения:");
        description.add("- Ходьба и бег");
        description.add("- Прыжки и полет");
        description.add("- Крадущееся движение");
        description.add("- Отслеживание расстояния");
        description.add("- Статистика движения");
        return description;
    }
}
