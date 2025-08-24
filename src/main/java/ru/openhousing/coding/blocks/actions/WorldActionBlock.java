package ru.openhousing.coding.blocks.actions;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
import java.util.Arrays;

/**
 * Блок действий с миром
 * Выполняет различные действия с миром: изменение блоков, создание взрывов, спавн существ
 */
public class WorldActionBlock extends CodeBlock {
    
    /**
     * Типы действий с миром
     */
    public enum WorldActionType {
        SET_BLOCK("Установить блок", "Устанавливает блок в указанном месте"),
        BREAK_BLOCK("Сломать блок", "Ломает блок в указанном месте"),
        FILL_AREA("Заполнить область", "Заполняет область блоками"),
        REPLACE_BLOCKS("Заменить блоки", "Заменяет блоки одного типа на другой"),
        SET_TIME("Установить время", "Устанавливает время в мире"),
        ADD_TIME("Добавить время", "Добавляет время к текущему"),
        SET_WEATHER("Установить погоду", "Устанавливает погоду в мире"),
        SET_STORM("Установить шторм", "Устанавливает шторм в мире"),
        STRIKE_LIGHTNING("Удар молнии", "Создает удар молнии"),
        PLAY_SOUND("Воспроизвести звук", "Воспроизводит звук в точке"),
        PLAY_SOUND_ALL("Воспроизвести звук всем", "Воспроизводит звук всем игрокам"),
        STOP_SOUND("Остановить звук", "Останавливает воспроизведение звука"),
        SPAWN_PARTICLE("Создать частицы", "Создает частицы в точке"),
        SPAWN_PARTICLE_LINE("Частицы линией", "Создает частицы по линии"),
        SPAWN_PARTICLE_CIRCLE("Частицы кругом", "Создает частицы по кругу"),
        SPAWN_PARTICLE_SPHERE("Частицы сферой", "Создает частицы в форме сферы"),
        CREATE_EXPLOSION("Создать взрыв", "Создает взрыв в точке"),
        SEND_MESSAGE("Отправить сообщение", "Отправляет сообщение игроку"),
        SEND_MESSAGE_ALL("Отправить всем", "Отправляет сообщение всем игрокам"),
        SEND_TITLE("Отправить заголовок", "Отправляет заголовок игроку"),
        SEND_ACTIONBAR("Отправить в actionbar", "Отправляет сообщение в actionbar"),
        TELEPORT_PLAYER("Телепортировать игрока", "Телепортирует игрока"),
        HEAL_PLAYER("Исцелить игрока", "Восстанавливает здоровье игрока"),
        FEED_PLAYER("Накормить игрока", "Восстанавливает голод игрока"),
        GIVE_ITEM("Дать предмет", "Дает предмет игроку"),
        TAKE_ITEM("Забрать предмет", "Забирает предмет у игрока"),
        CLEAR_INVENTORY("Очистить инвентарь", "Очищает инвентарь игрока"),
        SET_GAMEMODE("Установить режим", "Устанавливает режим игры"),
        SET_FLY("Установить полет", "Включает/выключает полет"),
        SET_GOD_MODE("Режим бога", "Включает/выключает режим бога"),
        KICK_PLAYER("Кикнуть игрока", "Кикает игрока с сервера"),
        BAN_PLAYER("Забанить игрока", "Банит игрока на сервере"),
        RUN_COMMAND("Выполнить команду", "Выполняет команду от имени игрока"),
        RUN_COMMAND_CONSOLE("Команда консоли", "Выполняет команду от имени консоли"),
        SET_SPAWN("Установить спавн", "Устанавливает точку спавна"),
        LOAD_CHUNK("Загрузить чанк", "Загружает чанк в память"),
        UNLOAD_CHUNK("Выгрузить чанк", "Выгружает чанк из памяти"),
        SAVE_WORLD("Сохранить мир", "Сохраняет мир на диск"),
        SET_DIFFICULTY("Установить сложность", "Устанавливает сложность мира"),
        SET_GAME_RULE("Установить правило", "Устанавливает правило игры"),
        CREATE_FIREWORK("Создать фейерверк", "Создает фейерверк"),
        FREEZE_PLAYER("Заморозить игрока", "Замораживает игрока"),
        UNFREEZE_PLAYER("Разморозить игрока", "Размораживает игрока"),
        SET_WALKSPEED("Установить скорость ходьбы", "Устанавливает скорость ходьбы"),
        SET_FLYSPEED("Установить скорость полета", "Устанавливает скорость полета"),
        PUSH_PLAYER("Толкнуть игрока", "Толкает игрока в указанном направлении"),
        LAUNCH_PLAYER("Запустить игрока", "Запускает игрока в воздух"),
        HIDE_PLAYER("Скрыть игрока", "Скрывает игрока от других"),
        SHOW_PLAYER("Показать игрока", "Показывает игрока другим"),
        SET_EXPERIENCE("Установить опыт", "Устанавливает уровень опыта"),
        GIVE_EXPERIENCE("Дать опыт", "Дает опыт игроку"),
        TAKE_EXPERIENCE("Забрать опыт", "Забирает опыт у игрока"),
        WAIT("Ожидание", "Ожидает указанное время"),
        SEND_TO_SERVER("Отправить на сервер", "Отправляет игрока на другой сервер");
        
        private final String displayName;
        private final String description;
        
        WorldActionType(String displayName, String description) {
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
    
    public WorldActionBlock() {
        super(BlockType.GAME_SET_BLOCK);
        initializeDefaultParameters();
    }

    private void initializeDefaultParameters() {
        setParameter("enabled", true);
        setParameter("actionType", "SET_BLOCK");
        
        // Действия с блоками
        setParameter("setBlock", false);
        setParameter("blockType", "STONE");
        setParameter("blockX", "0");
        setParameter("blockY", "64");
        setParameter("blockZ", "0");
        setParameter("blockWorld", "world");
        setParameter("breakBlock", false);
        setParameter("breakX", "0");
        setParameter("breakY", "64");
        setParameter("breakZ", "0");
        setParameter("breakWorld", "world");
        setParameter("dropItems", true);
        
        // Действия с существами
        setParameter("spawnEntity", false);
        setParameter("entityType", "ZOMBIE");
        setParameter("entityX", "0");
        setParameter("entityY", "64");
        setParameter("entityZ", "0");
        setParameter("entityWorld", "world");
        setParameter("entityCount", "1");
        setParameter("entityName", "");
        setParameter("entityCustomName", false);
        setParameter("entityInvulnerable", false);
        setParameter("entitySilent", false);
        setParameter("entityGlowing", false);
        setParameter("entityGravity", true);
        setParameter("entityAI", true);
        
        setParameter("removeEntity", false);
        setParameter("removeEntityType", "ZOMBIE");
        setParameter("removeRadius", "10");
        setParameter("removeAllEntities", false);
        
        // Действия с взрывами
        setParameter("createExplosion", false);
        setParameter("explosionX", "0");
        setParameter("explosionY", "64");
        setParameter("explosionZ", "0");
        setParameter("explosionWorld", "world");
        setParameter("explosionPower", "4.0");
        setParameter("explosionFire", false);
        setParameter("explosionBreakBlocks", true);
        
        // Действия с частицами
        setParameter("spawnParticles", false);
        setParameter("particleType", "CLOUD");
        setParameter("particleX", "0");
        setParameter("particleY", "64");
        setParameter("particleZ", "0");
        setParameter("particleWorld", "world");
        setParameter("particleCount", "10");
        setParameter("particleOffsetX", "0.5");
        setParameter("particleOffsetY", "0.5");
        setParameter("particleOffsetZ", "0.5");
        setParameter("particleSpeed", "0.1");
        
        // Действия со звуками
        setParameter("playSound", false);
        setParameter("soundType", "ENTITY_PLAYER_LEVELUP");
        setParameter("soundX", "0");
        setParameter("soundY", "64");
        setParameter("soundZ", "0");
        setParameter("soundWorld", "world");
        setParameter("soundVolume", "1.0");
        setParameter("soundPitch", "1.0");
        
        // Действия с погодой
        setParameter("setWeather", false);
        setParameter("weatherType", "CLEAR");
        setParameter("weatherDuration", "6000");
        
        // Действия со временем
        setParameter("setTime", false);
        setParameter("timeValue", "0");
        setParameter("timeType", "TICKS");
        
        // Действия с молнией
        setParameter("strikeLightning", false);
        setParameter("lightningX", "0");
        setParameter("lightningY", "64");
        setParameter("lightningZ", "0");
        setParameter("lightningWorld", "world");
        setParameter("lightningNoFire", true);
        
        // Действия с телепортацией
        setParameter("teleportEntity", false);
        setParameter("entityToTeleport", "NEAREST_PLAYER");
        setParameter("teleportX", "0");
        setParameter("teleportY", "64");
        setParameter("teleportZ", "0");
        setParameter("teleportWorld", "world");
        
        // Действия с эффектами
        setParameter("addEffectToNearby", false);
        setParameter("effectType", "SPEED");
        setParameter("effectDuration", "60");
        setParameter("effectAmplifier", "1");
        setParameter("effectRadius", "10");
        setParameter("effectTargetPlayers", true);
        setParameter("effectTargetEntities", false);
        
        // Действия с сообщениями
        setParameter("broadcastMessage", false);
        setParameter("broadcastText", "Мир изменился!");
        setParameter("broadcastToWorld", false);
        setParameter("broadcastToNearby", false);
        setParameter("broadcastRadius", "50");
        
        // Логирование
        setParameter("logAction", false);
        setParameter("logFormat", "[{time}] World action executed: {action} at {location}");
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

            String actionType = (String) getParameter("actionType");
            
            // Выполняем действия в зависимости от типа
            switch (actionType) {
                case "SET_BLOCK":
                    if ((Boolean) getParameter("setBlock")) {
                        executeSetBlock(context);
                }
                break;
                case "BREAK_BLOCK":
                    if ((Boolean) getParameter("breakBlock")) {
                        executeBreakBlock(context);
                }
                break;
                case "SPAWN_ENTITY":
                    if ((Boolean) getParameter("spawnEntity")) {
                        executeSpawnEntity(context);
                }
                break;
                case "REMOVE_ENTITY":
                    if ((Boolean) getParameter("removeEntity")) {
                        executeRemoveEntity(context);
                }
                break;
                case "CREATE_EXPLOSION":
                    if ((Boolean) getParameter("createExplosion")) {
                        executeCreateExplosion(context);
                }
                break;
                case "SPAWN_PARTICLES":
                    if ((Boolean) getParameter("spawnParticles")) {
                        executeSpawnParticles(context);
                }
                break;
                case "PLAY_SOUND":
                    if ((Boolean) getParameter("playSound")) {
                        executePlaySound(context);
                }
                break;
                case "SET_WEATHER":
                    if ((Boolean) getParameter("setWeather")) {
                        executeSetWeather(context);
                }
                break;
                case "SET_TIME":
                    if ((Boolean) getParameter("setTime")) {
                        executeSetTime(context);
                }
                break;
                case "STRIKE_LIGHTNING":
                    if ((Boolean) getParameter("strikeLightning")) {
                        executeStrikeLightning(context);
                }
                break;
                case "TELEPORT_ENTITY":
                    if ((Boolean) getParameter("teleportEntity")) {
                        executeTeleportEntity(context);
                }
                break;
                case "ADD_EFFECT":
                    if ((Boolean) getParameter("addEffectToNearby")) {
                        executeAddEffectToNearby(context);
                }
                break;
                case "BROADCAST":
                    if ((Boolean) getParameter("broadcastMessage")) {
                        executeBroadcastMessage(context);
                }
                break;
            }

            // Логируем действие
            if ((Boolean) getParameter("logAction")) {
                logAction(context, actionType);
            }

            return ExecutionResult.success();
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения WorldActionBlock: " + e.getMessage());
        }
    }

    private void executeSetBlock(ExecutionContext context) {
        try {
            String blockType = (String) getParameter("blockType");
            double x = Double.parseDouble((String) getParameter("blockX"));
            double y = Double.parseDouble((String) getParameter("blockY"));
            double z = Double.parseDouble((String) getParameter("blockZ"));
            String worldName = (String) getParameter("blockWorld");
            
            World world = context.getPlayer().getServer().getWorld(worldName);
            if (world != null) {
                Location location = new Location(world, x, y, z);
                Material material = Material.valueOf(blockType);
                location.getBlock().setType(material);
            }
        } catch (Exception e) {
            // Игнорируем ошибки установки блока
        }
    }

    private void executeBreakBlock(ExecutionContext context) {
        try {
            double x = Double.parseDouble((String) getParameter("breakX"));
            double y = Double.parseDouble((String) getParameter("breakY"));
            double z = Double.parseDouble((String) getParameter("breakZ"));
            String worldName = (String) getParameter("breakWorld");
            boolean dropItems = (Boolean) getParameter("dropItems");
            
            World world = context.getPlayer().getServer().getWorld(worldName);
            if (world != null) {
                Location location = new Location(world, x, y, z);
                if (dropItems) {
                    location.getBlock().breakNaturally();
                } else {
                    location.getBlock().setType(Material.AIR);
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки ломания блока
        }
    }

    private void executeSpawnEntity(ExecutionContext context) {
        try {
            String entityType = (String) getParameter("entityType");
            double x = Double.parseDouble((String) getParameter("entityX"));
            double y = Double.parseDouble((String) getParameter("entityY"));
            double z = Double.parseDouble((String) getParameter("entityZ"));
            String worldName = (String) getParameter("entityWorld");
            int count = Integer.parseInt((String) getParameter("entityCount"));
            String name = (String) getParameter("entityName");
            boolean customName = (Boolean) getParameter("entityCustomName");
            boolean invulnerable = (Boolean) getParameter("entityInvulnerable");
            boolean silent = (Boolean) getParameter("entitySilent");
            boolean glowing = (Boolean) getParameter("entityGlowing");
            boolean gravity = (Boolean) getParameter("entityGravity");
            boolean ai = (Boolean) getParameter("entityAI");
            
            World world = context.getPlayer().getServer().getWorld(worldName);
            if (world != null) {
                Location location = new Location(world, x, y, z);
                EntityType type = EntityType.valueOf(entityType);
                
                for (int i = 0; i < count; i++) {
                    Entity entity = world.spawnEntity(location, type);
                    
                    if (customName && !name.isEmpty()) {
                        entity.setCustomName(name);
                        entity.setCustomNameVisible(true);
                    }
                    
                    entity.setInvulnerable(invulnerable);
                    entity.setSilent(silent);
                    entity.setGlowing(glowing);
                    entity.setGravity(gravity);
                    
                    if (entity instanceof org.bukkit.entity.Mob) {
                        org.bukkit.entity.Mob mob = (org.bukkit.entity.Mob) entity;
                        mob.setAI(ai);
                    }
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки спавна существ
        }
    }

    private void executeRemoveEntity(ExecutionContext context) {
        try {
            if ((Boolean) getParameter("removeAllEntities")) {
                double x = Double.parseDouble((String) getParameter("entityX"));
                double y = Double.parseDouble((String) getParameter("entityY"));
                double z = Double.parseDouble((String) getParameter("entityZ"));
                String worldName = (String) getParameter("entityWorld");
                double radius = Double.parseDouble((String) getParameter("removeRadius"));
                
                World world = context.getPlayer().getServer().getWorld(worldName);
                if (world != null) {
                    Location center = new Location(world, x, y, z);
                    for (Entity entity : world.getEntities()) {
                        if (entity.getLocation().distance(center) <= radius) {
                            entity.remove();
                        }
                    }
                }
            } else {
                String entityType = (String) getParameter("removeEntityType");
                double x = Double.parseDouble((String) getParameter("entityX"));
                double y = Double.parseDouble((String) getParameter("entityY"));
                double z = Double.parseDouble((String) getParameter("entityZ"));
                String worldName = (String) getParameter("entityWorld");
                double radius = Double.parseDouble((String) getParameter("removeRadius"));
                
                World world = context.getPlayer().getServer().getWorld(worldName);
                if (world != null) {
                    Location center = new Location(world, x, y, z);
                    EntityType type = EntityType.valueOf(entityType);
                    
                    for (Entity entity : world.getEntities()) {
                        if (entity.getType() == type && entity.getLocation().distance(center) <= radius) {
                            entity.remove();
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки удаления существ
        }
    }

    private void executeCreateExplosion(ExecutionContext context) {
        try {
            double x = Double.parseDouble((String) getParameter("explosionX"));
            double y = Double.parseDouble((String) getParameter("explosionY"));
            double z = Double.parseDouble((String) getParameter("explosionZ"));
            String worldName = (String) getParameter("explosionWorld");
            float power = Float.parseFloat((String) getParameter("explosionPower"));
            boolean fire = (Boolean) getParameter("explosionFire");
            boolean breakBlocks = (Boolean) getParameter("explosionBreakBlocks");
            
            World world = context.getPlayer().getServer().getWorld(worldName);
            if (world != null) {
                Location location = new Location(world, x, y, z);
                world.createExplosion(location, power, fire, breakBlocks);
            }
        } catch (Exception e) {
            // Игнорируем ошибки создания взрыва
        }
    }

    private void executeSpawnParticles(ExecutionContext context) {
        try {
            String particleType = (String) getParameter("particleType");
            double x = Double.parseDouble((String) getParameter("particleX"));
            double y = Double.parseDouble((String) getParameter("particleY"));
            double z = Double.parseDouble((String) getParameter("particleZ"));
            String worldName = (String) getParameter("particleWorld");
            int count = Integer.parseInt((String) getParameter("particleCount"));
            double offsetX = Double.parseDouble((String) getParameter("particleOffsetX"));
            double offsetY = Double.parseDouble((String) getParameter("particleOffsetY"));
            double offsetZ = Double.parseDouble((String) getParameter("particleOffsetZ"));
            double speed = Double.parseDouble((String) getParameter("particleSpeed"));
            
            World world = context.getPlayer().getServer().getWorld(worldName);
            if (world != null) {
                Location location = new Location(world, x, y, z);
                org.bukkit.Particle particle = org.bukkit.Particle.valueOf(particleType);
                world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
            }
        } catch (Exception e) {
            // Игнорируем ошибки создания частиц
        }
    }

    private void executePlaySound(ExecutionContext context) {
        try {
            String soundType = (String) getParameter("soundType");
            double x = Double.parseDouble((String) getParameter("soundX"));
            double y = Double.parseDouble((String) getParameter("soundY"));
            double z = Double.parseDouble((String) getParameter("soundZ"));
            String worldName = (String) getParameter("soundWorld");
            float volume = Float.parseFloat((String) getParameter("soundVolume"));
            float pitch = Float.parseFloat((String) getParameter("soundPitch"));
            
            World world = context.getPlayer().getServer().getWorld(worldName);
            if (world != null) {
                Location location = new Location(world, x, y, z);
                Sound sound = Sound.valueOf(soundType);
                world.playSound(location, sound, volume, pitch);
                        }
                    } catch (Exception e) {
            // Игнорируем ошибки воспроизведения звука
        }
    }

    private void executeSetWeather(ExecutionContext context) {
        try {
            String weatherType = (String) getParameter("weatherType");
            int duration = Integer.parseInt((String) getParameter("weatherDuration"));
            String worldName = (String) getParameter("entityWorld");
            
            World world = context.getPlayer().getServer().getWorld(worldName);
            if (world != null) {
                switch (weatherType) {
                    case "CLEAR":
                        world.setStorm(false);
                        world.setThundering(false);
                        break;
                    case "RAIN":
                        world.setStorm(true);
                        world.setThundering(false);
                        break;
                    case "THUNDER":
                        world.setStorm(true);
                        world.setThundering(true);
                        break;
                }
                
                if (duration > 0) {
                    // Устанавливаем длительность погоды
                    world.setWeatherDuration(duration);
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки установки погоды
        }
    }

    private void executeSetTime(ExecutionContext context) {
        try {
            String timeValue = (String) getParameter("timeValue");
            String timeType = (String) getParameter("timeType");
            String worldName = (String) getParameter("entityWorld");
            
            World world = context.getPlayer().getServer().getWorld(worldName);
            if (world != null) {
                long time;
                if (timeType.equals("TICKS")) {
                    time = Long.parseLong(timeValue);
            } else {
                    // Предполагаем, что это время дня (0-24000)
                    time = Long.parseLong(timeValue);
                }
                world.setTime(time);
            }
        } catch (Exception e) {
            // Игнорируем ошибки установки времени
        }
    }

    private void executeStrikeLightning(ExecutionContext context) {
        try {
            double x = Double.parseDouble((String) getParameter("lightningX"));
            double y = Double.parseDouble((String) getParameter("lightningY"));
            double z = Double.parseDouble((String) getParameter("lightningZ"));
            String worldName = (String) getParameter("lightningWorld");
            boolean noFire = (Boolean) getParameter("lightningNoFire");
            
            World world = context.getPlayer().getServer().getWorld(worldName);
            if (world != null) {
                Location location = new Location(world, x, y, z);
                if (noFire) {
                    world.strikeLightningEffect(location);
            } else {
                    world.strikeLightning(location);
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки удара молнии
        }
    }

    private void executeTeleportEntity(ExecutionContext context) {
        try {
            String entityToTeleport = (String) getParameter("entityToTeleport");
            double x = Double.parseDouble((String) getParameter("teleportX"));
            double y = Double.parseDouble((String) getParameter("teleportY"));
            double z = Double.parseDouble((String) getParameter("teleportZ"));
            String worldName = (String) getParameter("teleportWorld");
            
            World world = context.getPlayer().getServer().getWorld(worldName);
            if (world != null) {
                Location destination = new Location(world, x, y, z);
                
                if (entityToTeleport.equals("NEAREST_PLAYER")) {
                    Player nearestPlayer = null;
                    double minDistance = Double.MAX_VALUE;
                    
                    for (Player player : world.getPlayers()) {
                        double distance = player.getLocation().distance(context.getPlayer().getLocation());
                        if (distance < minDistance) {
                            minDistance = distance;
                            nearestPlayer = player;
                        }
                    }
                    
                    if (nearestPlayer != null) {
                        nearestPlayer.teleport(destination);
                    }
                } else {
                    // Телепортируем всех игроков в мире
                for (Player player : world.getPlayers()) {
                        player.teleport(destination);
                    }
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки телепортации
        }
    }

    private void executeAddEffectToNearby(ExecutionContext context) {
        try {
            String effectType = (String) getParameter("effectType");
            int duration = Integer.parseInt((String) getParameter("effectDuration"));
            int amplifier = Integer.parseInt((String) getParameter("effectAmplifier"));
            double radius = Double.parseDouble((String) getParameter("effectRadius"));
            boolean targetPlayers = (Boolean) getParameter("effectTargetPlayers");
            boolean targetEntities = (Boolean) getParameter("effectTargetEntities");
            
            Location center = context.getPlayer().getLocation();
            PotionEffectType type = PotionEffectType.getByName(effectType);
            
            if (type != null) {
                PotionEffect effect = new PotionEffect(type, duration, amplifier);
                
                if (targetPlayers) {
                    for (Player player : center.getWorld().getPlayers()) {
                        if (player.getLocation().distance(center) <= radius) {
                            player.addPotionEffect(effect);
                        }
                    }
                }
                
                if (targetEntities) {
                    for (Entity entity : center.getWorld().getEntities()) {
                        if (entity.getLocation().distance(center) <= radius && entity instanceof Player) {
                            ((Player) entity).addPotionEffect(effect);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки добавления эффектов
        }
    }

    private void executeBroadcastMessage(ExecutionContext context) {
        try {
            String message = (String) getParameter("broadcastText");
            boolean toWorld = (Boolean) getParameter("broadcastToWorld");
            boolean toNearby = (Boolean) getParameter("broadcastToNearby");
            double radius = Double.parseDouble((String) getParameter("broadcastRadius"));
            
            message = replacePlaceholders(message, context.getPlayer(), context);
            
            if (toWorld) {
                String worldName = (String) getParameter("entityWorld");
                World world = context.getPlayer().getServer().getWorld(worldName);
                if (world != null) {
            for (Player player : world.getPlayers()) {
                        player.sendMessage(message);
                    }
                }
            } else if (toNearby) {
                Location center = context.getPlayer().getLocation();
                for (Player player : center.getWorld().getPlayers()) {
                    if (player.getLocation().distance(center) <= radius) {
                        player.sendMessage(message);
                    }
                }
            } else {
                // Отправляем всем игрокам на сервере
                context.getPlayer().getServer().broadcastMessage(message);
            }
        } catch (Exception e) {
            // Игнорируем ошибки отправки сообщений
        }
    }

    private void logAction(ExecutionContext context, String actionType) {
        try {
            String format = (String) getParameter("logFormat");
            String log = replacePlaceholders(format, context.getPlayer(), context)
                .replace("{action}", actionType);
            // TODO: Записать в лог
        } catch (Exception e) {
            // Игнорируем ошибки логирования
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
            .replace("{location}", String.format("%.1f,%.1f,%.1f", 
                player.getLocation().getX(), 
                player.getLocation().getY(), 
                player.getLocation().getZ()));
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("enabled", getParameter("enabled"));
        params.put("actionType", getParameter("actionType"));
        params.put("setBlock", getParameter("setBlock"));
        params.put("blockType", getParameter("blockType"));
        params.put("blockX", getParameter("blockX"));
        params.put("blockY", getParameter("blockY"));
        params.put("blockZ", getParameter("blockZ"));
        params.put("blockWorld", getParameter("blockWorld"));
        params.put("breakBlock", getParameter("breakBlock"));
        params.put("breakX", getParameter("breakX"));
        params.put("breakY", getParameter("breakY"));
        params.put("breakZ", getParameter("breakZ"));
        params.put("breakWorld", getParameter("breakWorld"));
        params.put("dropItems", getParameter("dropItems"));
        params.put("spawnEntity", getParameter("spawnEntity"));
        params.put("entityType", getParameter("entityType"));
        params.put("entityX", getParameter("entityX"));
        params.put("entityY", getParameter("entityY"));
        params.put("entityZ", getParameter("entityZ"));
        params.put("entityWorld", getParameter("entityWorld"));
        params.put("entityCount", getParameter("entityCount"));
        params.put("entityName", getParameter("entityName"));
        params.put("entityCustomName", getParameter("entityCustomName"));
        params.put("entityInvulnerable", getParameter("entityInvulnerable"));
        params.put("entitySilent", getParameter("entitySilent"));
        params.put("entityGlowing", getParameter("entityGlowing"));
        params.put("entityGravity", getParameter("entityGravity"));
        params.put("entityAI", getParameter("entityAI"));
        params.put("removeEntity", getParameter("removeEntity"));
        params.put("removeEntityType", getParameter("removeEntityType"));
        params.put("removeRadius", getParameter("removeRadius"));
        params.put("removeAllEntities", getParameter("removeAllEntities"));
        params.put("createExplosion", getParameter("createExplosion"));
        params.put("explosionX", getParameter("explosionX"));
        params.put("explosionY", getParameter("explosionY"));
        params.put("explosionZ", getParameter("explosionZ"));
        params.put("explosionWorld", getParameter("explosionWorld"));
        params.put("explosionPower", getParameter("explosionPower"));
        params.put("explosionFire", getParameter("explosionFire"));
        params.put("explosionBreakBlocks", getParameter("explosionBreakBlocks"));
        params.put("spawnParticles", getParameter("spawnParticles"));
        params.put("particleType", getParameter("particleType"));
        params.put("particleX", getParameter("particleX"));
        params.put("particleY", getParameter("particleY"));
        params.put("particleZ", getParameter("particleZ"));
        params.put("particleWorld", getParameter("particleWorld"));
        params.put("particleCount", getParameter("particleCount"));
        params.put("particleOffsetX", getParameter("particleOffsetX"));
        params.put("particleOffsetY", getParameter("particleOffsetY"));
        params.put("particleOffsetZ", getParameter("particleOffsetZ"));
        params.put("particleSpeed", getParameter("particleSpeed"));
        params.put("playSound", getParameter("playSound"));
        params.put("soundType", getParameter("soundType"));
        params.put("soundX", getParameter("soundX"));
        params.put("soundY", getParameter("soundY"));
        params.put("soundZ", getParameter("soundZ"));
        params.put("soundWorld", getParameter("soundWorld"));
        params.put("soundVolume", getParameter("soundVolume"));
        params.put("soundPitch", getParameter("soundPitch"));
        params.put("setWeather", getParameter("setWeather"));
        params.put("weatherType", getParameter("weatherType"));
        params.put("weatherDuration", getParameter("weatherDuration"));
        params.put("setTime", getParameter("setTime"));
        params.put("timeValue", getParameter("timeValue"));
        params.put("timeType", getParameter("timeType"));
        params.put("strikeLightning", getParameter("strikeLightning"));
        params.put("lightningX", getParameter("lightningX"));
        params.put("lightningY", getParameter("lightningY"));
        params.put("lightningZ", getParameter("lightningZ"));
        params.put("lightningWorld", getParameter("lightningWorld"));
        params.put("lightningNoFire", getParameter("lightningNoFire"));
        params.put("teleportEntity", getParameter("teleportEntity"));
        params.put("entityToTeleport", getParameter("entityToTeleport"));
        params.put("teleportX", getParameter("teleportX"));
        params.put("teleportY", getParameter("teleportY"));
        params.put("teleportZ", getParameter("teleportZ"));
        params.put("teleportWorld", getParameter("teleportWorld"));
        params.put("addEffectToNearby", getParameter("addEffectToNearby"));
        params.put("effectType", getParameter("effectType"));
        params.put("effectDuration", getParameter("effectDuration"));
        params.put("effectAmplifier", getParameter("effectAmplifier"));
        params.put("effectRadius", getParameter("effectRadius"));
        params.put("effectTargetPlayers", getParameter("effectTargetPlayers"));
        params.put("effectTargetEntities", getParameter("effectTargetEntities"));
        params.put("broadcastMessage", getParameter("broadcastMessage"));
        params.put("broadcastText", getParameter("broadcastText"));
        params.put("broadcastToWorld", getParameter("broadcastToWorld"));
        params.put("broadcastToNearby", getParameter("broadcastToNearby"));
        params.put("broadcastRadius", getParameter("broadcastRadius"));
        params.put("logAction", getParameter("logAction"));
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
        description.add("Блок действий с миром");
        description.add("Выполняет различные действия:");
        description.add("- Установка и ломание блоков");
        description.add("- Спавн и удаление существ");
        description.add("- Создание взрывов и молний");
        description.add("- Создание частиц и звуков");
        description.add("- Изменение погоды и времени");
        description.add("- Телепортация существ");
        description.add("- Добавление эффектов");
        description.add("- Отправка сообщений");
        WorldActionType actionType = (WorldActionType) getParameter(ru.openhousing.coding.constants.BlockParams.ACTION_TYPE);
        String value = (String) getParameter(ru.openhousing.coding.constants.BlockParams.VALUE);
        String location = (String) getParameter(ru.openhousing.coding.constants.BlockParams.LOCATION);
        
        return Arrays.asList(
            "§6Действие мира",
            "§7Тип: §f" + (actionType != null ? actionType.getDisplayName() : "Не выбран"),
            "§7Значение: §f" + (value != null && !value.isEmpty() ? value : "Не указано"),
            "§7Локация: §f" + (location != null && !location.isEmpty() ? location : "Не указана"),
            "",
            "§8" + (actionType != null ? actionType.getDescription() : "")
        );
    }
}
