package ru.openhousing.coding.blocks.actions;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.OpenHousing;

import java.util.Arrays;
import java.util.List;

/**
 * Блок действий мира
 */
public class WorldActionBlock extends CodeBlock {
    
    public enum WorldActionType {
        SET_BLOCK("Установить блок", "Устанавливает блок в указанной позиции"),
        BREAK_BLOCK("Разрушить блок", "Разрушает блок в указанной позиции"),
        FILL_AREA("Заполнить область", "Заполняет область блоками"),
        REPLACE_BLOCKS("Заменить блоки", "Заменяет блоки в области"),
        
        SET_TIME("Установить время", "Устанавливает время в мире"),
        ADD_TIME("Добавить время", "Добавляет время к текущему"),
        SET_WEATHER("Установить погоду", "Устанавливает погоду"),
        SET_STORM("Установить грозу", "Включает/выключает грозу"),
        STRIKE_LIGHTNING("Ударить молнией", "Ударяет молнией в точку"),
        
        PLAY_SOUND("Воспроизвести звук", "Воспроизводит звук в мире"),
        PLAY_SOUND_ALL("Звук для всех", "Воспроизводит звук для всех игроков"),
        STOP_SOUND("Остановить звук", "Останавливает воспроизведение звука"),
        
        SPAWN_PARTICLE("Создать частицы", "Создает частицы в точке"),
        SPAWN_PARTICLE_LINE("Линия частиц", "Создает линию из частиц"),
        SPAWN_PARTICLE_CIRCLE("Круг частиц", "Создает круг из частиц"),
        SPAWN_PARTICLE_SPHERE("Сфера частиц", "Создает сферу из частиц"),
        
        CREATE_EXPLOSION("Создать взрыв", "Создает взрыв в точке"),
        
        SEND_MESSAGE("Отправить сообщение", "Отправляет сообщение игроку"),
        SEND_MESSAGE_ALL("Сообщение всем", "Отправляет сообщение всем игрокам"),
        SEND_TITLE("Отправить титул", "Отправляет титул игроку"),
        SEND_ACTIONBAR("Отправить actionbar", "Отправляет actionbar сообщение"),
        
        TELEPORT_PLAYER("Телепорт игрока", "Телепортирует игрока"),
        HEAL_PLAYER("Лечить игрока", "Восстанавливает здоровье игрока"),
        FEED_PLAYER("Накормить игрока", "Восстанавливает голод игрока"),
        GIVE_ITEM("Дать предмет", "Дает предмет игроку"),
        TAKE_ITEM("Забрать предмет", "Забирает предмет у игрока"),
        CLEAR_INVENTORY("Очистить инвентарь", "Очищает инвентарь игрока"),
        
        SET_GAMEMODE("Установить режим", "Устанавливает игровой режим"),
        SET_FLY("Установить полет", "Включает/выключает полет"),
        SET_GOD_MODE("Режим бога", "Включает/выключает неуязвимость"),
        
        KICK_PLAYER("Кикнуть игрока", "Кикает игрока с сервера"),
        BAN_PLAYER("Забанить игрока", "Банит игрока"),
        
        RUN_COMMAND("Выполнить команду", "Выполняет команду от имени игрока"),
        RUN_COMMAND_CONSOLE("Команда консоли", "Выполняет команду от имени консоли"),
        
        SET_SPAWN("Установить спавн", "Устанавливает точку спавна мира"),
        LOAD_CHUNK("Загрузить чанк", "Загружает чанк"),
        UNLOAD_CHUNK("Выгрузить чанк", "Выгружает чанк"),
        
        SAVE_WORLD("Сохранить мир", "Сохраняет мир"),
        
        SET_DIFFICULTY("Установить сложность", "Устанавливает сложность мира"),
        SET_GAME_RULE("Установить правило", "Устанавливает игровое правило"),
        
        CREATE_FIREWORK("Создать фейерверк", "Запускает фейерверк"),
        
        FREEZE_PLAYER("Заморозить игрока", "Замораживает игрока"),
        UNFREEZE_PLAYER("Разморозить игрока", "Размораживает игрока"),
        
        SET_WALKSPEED("Скорость ходьбы", "Устанавливает скорость ходьбы"),
        SET_FLYSPEED("Скорость полета", "Устанавливает скорость полета"),
        
        PUSH_PLAYER("Толкнуть игрока", "Толкает игрока в направлении"),
        LAUNCH_PLAYER("Запустить игрока", "Запускает игрока в воздух"),
        
        HIDE_PLAYER("Скрыть игрока", "Скрывает игрока от других"),
        SHOW_PLAYER("Показать игрока", "Показывает скрытого игрока"),
        
        SET_EXPERIENCE("Установить опыт", "Устанавливает уровень опыта"),
        GIVE_EXPERIENCE("Дать опыт", "Дает опыт игроку"),
        TAKE_EXPERIENCE("Забрать опыт", "Забирает опыт у игрока"),
        
        WAIT("Ждать", "Приостанавливает выполнение на время"),
        
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
        super(BlockType.WORLD_ACTION);
        setParameter("actionType", WorldActionType.SET_BLOCK);
        setParameter("value", "");
        setParameter("location", "");
        setParameter("extra", "");
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        WorldActionType actionType = (WorldActionType) getParameter(ru.openhousing.coding.constants.BlockParams.ACTION_TYPE);
        String value = replaceVariables((String) getParameter(ru.openhousing.coding.constants.BlockParams.VALUE), context);
        String location = replaceVariables((String) getParameter(ru.openhousing.coding.constants.BlockParams.LOCATION), context);
        String extra = replaceVariables((String) getParameter(ru.openhousing.coding.constants.BlockParams.EXTRA), context);
        
        if (actionType == null) {
            return ExecutionResult.error("Не указан тип действия");
        }
        
        try {
            executeWorldAction(context, actionType, value, location, extra);
            return ExecutionResult.success();
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения действия: " + e.getMessage());
        }
    }
    
    private void executeWorldAction(ExecutionContext context, WorldActionType actionType,
                                  String value, String location, String extra) {
        
        Player player = context.getPlayer();
        World world = player != null ? player.getWorld() : null;
        
        switch (actionType) {
            case SET_BLOCK:
                setBlock(world, location, value);
                break;
                
            case BREAK_BLOCK:
                breakBlock(world, location);
                break;
                
            case FILL_AREA:
                fillArea(world, location, value, extra);
                break;
                
            case SET_TIME:
                if (world != null) {
                    try {
                        long time = parseTime(value);
                        world.setTime(time);
                    } catch (NumberFormatException e) {
                        // Игнорируем ошибку
                    }
                }
                break;
                
            case ADD_TIME:
                if (world != null) {
                    try {
                        long addTime = Long.parseLong(value);
                        world.setTime(world.getTime() + addTime);
                    } catch (NumberFormatException e) {
                        // Игнорируем ошибку
                    }
                }
                break;
                
            case SET_WEATHER:
                if (world != null) {
                    boolean storm = value.equalsIgnoreCase("storm") || 
                                  value.equalsIgnoreCase("rain") || 
                                  Boolean.parseBoolean(value);
                    world.setStorm(storm);
                }
                break;
                
            case SET_STORM:
                if (world != null) {
                    world.setThundering(Boolean.parseBoolean(value));
                }
                break;
                
            case STRIKE_LIGHTNING:
                strikeLightning(world, location, Boolean.parseBoolean(extra));
                break;
                
            case PLAY_SOUND:
                playSound(player, value, location, extra);
                break;
                
            case PLAY_SOUND_ALL:
                playSoundAll(world, value, location, extra);
                break;
                
            case SPAWN_PARTICLE:
                spawnParticle(world, location, value, extra);
                break;
                
            case SPAWN_PARTICLE_LINE:
                spawnParticleLine(world, location, extra, value);
                break;
                
            case SPAWN_PARTICLE_CIRCLE:
                spawnParticleCircle(world, location, value, extra);
                break;
                
            case CREATE_EXPLOSION:
                createExplosion(world, location, value, extra);
                break;
                
            case SEND_MESSAGE:
                if (player != null) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', value));
                }
                break;
                
            case SEND_MESSAGE_ALL:
                if (world != null) {
                    String message = ChatColor.translateAlternateColorCodes('&', value);
                    for (Player p : world.getPlayers()) {
                        p.sendMessage(message);
                    }
                }
                break;
                
            case SEND_TITLE:
                sendTitle(player, value, extra);
                break;
                
            case SEND_ACTIONBAR:
                sendActionBar(player, value);
                break;
                
            case TELEPORT_PLAYER:
                teleportPlayer(player, location);
                break;
                
            case HEAL_PLAYER:
                healPlayer(player, value);
                break;
                
            case FEED_PLAYER:
                if (player != null) {
                    player.setFoodLevel(20);
                    player.setSaturation(20);
                }
                break;
                
            case GIVE_ITEM:
                giveItem(player, value, extra, null); // name is not used in this case
                break;
                
            case CLEAR_INVENTORY:
                if (player != null) {
                    player.getInventory().clear();
                }
                break;
                
            case SET_GAMEMODE:
                setGameMode(player, value);
                break;
                
            case SET_FLY:
                if (player != null) {
                    boolean canFly = Boolean.parseBoolean(value);
                    player.setAllowFlight(canFly);
                    player.setFlying(canFly);
                }
                break;
                
            case SET_GOD_MODE:
                if (player != null) {
                    player.setInvulnerable(Boolean.parseBoolean(value));
                }
                break;
                
            case RUN_COMMAND:
                if (player != null) {
                    player.performCommand(value);
                }
                break;
                
            case RUN_COMMAND_CONSOLE:
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), value);
                break;
                
            case SET_SPAWN:
                setSpawn(world, location);
                break;
                
            case SAVE_WORLD:
                if (world != null) {
                    world.save();
                }
                break;
                
            case SET_DIFFICULTY:
                setDifficulty(world, value);
                break;
                
            case SET_GAME_RULE:
                setGameRule(world, value, extra);
                break;
                
            case CREATE_FIREWORK:
                createFirework(world, location, value);
                break;
                
            case SET_WALKSPEED:
                if (player != null) {
                    try {
                        float speed = Float.parseFloat(value);
                        player.setWalkSpeed(Math.max(-1.0f, Math.min(1.0f, speed)));
                    } catch (NumberFormatException e) {
                        // Игнорируем ошибку
                    }
                }
                break;
                
            case SET_FLYSPEED:
                if (player != null) {
                    try {
                        float speed = Float.parseFloat(value);
                        player.setFlySpeed(Math.max(-1.0f, Math.min(1.0f, speed)));
                    } catch (NumberFormatException e) {
                        // Игнорируем ошибку
                    }
                }
                break;
                
            case PUSH_PLAYER:
                pushPlayer(player, value, extra);
                break;
                
            case LAUNCH_PLAYER:
                if (player != null) {
                    try {
                        double power = Double.parseDouble(value);
                        player.setVelocity(new Vector(0, power, 0));
                    } catch (NumberFormatException e) {
                        player.setVelocity(new Vector(0, 1.0, 0));
                        player.sendMessage("§c[OpenHousing] Неверная сила запуска: '" + value + "'. Используется значение по умолчанию: 1.0");
                    }
                }
                break;
                
            case SET_EXPERIENCE:
                setExperience(player, value);
                break;
                
            case GIVE_EXPERIENCE:
                if (player != null) {
                    try {
                        int exp = Integer.parseInt(value);
                        player.giveExp(exp);
                    } catch (NumberFormatException e) {
                        player.sendMessage("§c[OpenHousing] Неверное количество опыта: '" + value + "'. Опыт не выдан.");
                    }
                }
                break;
                
            case WAIT:
                // Реализация ожидания через планировщик
                waitAction(context, value);
                break;
                
            case REPLACE_BLOCKS:
                // TODO: Реализовать логику замены блоков
                break;
                
            case STOP_SOUND:
                // TODO: Реализовать логику остановки звука
                break;
                
            case SPAWN_PARTICLE_SPHERE:
                // TODO: Реализовать логику создания сферы из частиц
                break;
                
            case TAKE_ITEM:
                // TODO: Реализовать логику забора предмета
                break;
                
            case KICK_PLAYER:
                // TODO: Реализовать логику кика игрока
                break;
                
            case BAN_PLAYER:
                // TODO: Реализовать логику бана игрока
                break;
                
            case LOAD_CHUNK:
                // TODO: Реализовать логику загрузки чанка
                break;
                
            case UNLOAD_CHUNK:
                // TODO: Реализовать логику выгрузки чанка
                break;
                
            case FREEZE_PLAYER:
                // TODO: Реализовать логику заморозки игрока
                break;
                
            case UNFREEZE_PLAYER:
                // TODO: Реализовать логику разморозки игрока
                break;
                
            case HIDE_PLAYER:
                // TODO: Реализовать логику скрытия игрока
                break;
                
            case SHOW_PLAYER:
                // TODO: Реализовать логику показа игрока
                break;
                
            case TAKE_EXPERIENCE:
                // TODO: Реализовать логику забора опыта
                break;
                
            case SEND_TO_SERVER:
                // TODO: Реализовать логику отправки на другой сервер
                break;
        }
    }
    
    /**
     * Установка блока
     */
    private void setBlock(World world, String location, String blockType) {
        if (world == null) return;
        
        Location loc = parseLocation(location, world);
        if (loc != null) {
            try {
                Material material = Material.valueOf(blockType.toUpperCase());
                loc.getBlock().setType(material);
            } catch (IllegalArgumentException e) {
                // Неверный тип блока - логируем для администраторов
                if (OpenHousing.getInstance() != null) {
                    OpenHousing.getInstance().getLogger().warning("Неверный тип блока: " + blockType + " в локации " + location);
                }
                // Отправляем сообщение игроку, если он в мире
                for (Player player : world.getPlayers()) {
                    if (player.getLocation().distance(loc) < 50) { // В радиусе 50 блоков
                        player.sendMessage("§c[OpenHousing] Неверный тип блока: '" + blockType + "'. Примеры: STONE, DIAMOND_ORE, GLASS");
                    }
                }
            }
        }
    }
    
    /**
     * Разрушение блока
     */
    private void breakBlock(World world, String location) {
        if (world == null) return;
        
        Location loc = parseLocation(location, world);
        if (loc != null) {
            Block block = loc.getBlock();
            block.breakNaturally();
        }
    }
    
    /**
     * Заполнение области
     */
    private void fillArea(World world, String location, String blockType, String size) {
        if (world == null) return;
        
        Location loc = parseLocation(location, world);
        if (loc == null) return;
        
        try {
            Material material = Material.valueOf(blockType.toUpperCase());
            String[] sizeParts = size.split(",");
            
            int sizeX = 3, sizeY = 3, sizeZ = 3;
            if (sizeParts.length >= 3) {
                try {
                    sizeX = Integer.parseInt(sizeParts[0].trim());
                    sizeY = Integer.parseInt(sizeParts[1].trim());
                    sizeZ = Integer.parseInt(sizeParts[2].trim());
                    
                    // Проверяем разумность размеров
                    if (sizeX > 50 || sizeY > 50 || sizeZ > 50) {
                        for (Player player : world.getPlayers()) {
                            if (player.getLocation().distance(loc) < 100) {
                                player.sendMessage("§c[OpenHousing] Слишком большие размеры области: " + sizeX + "x" + sizeY + "x" + sizeZ + ". Максимум: 50x50x50");
                            }
                        }
                        return;
                    }
                } catch (NumberFormatException e) {
                    // Используем значения по умолчанию
                    if (OpenHousing.getInstance() != null) {
                        OpenHousing.getInstance().getLogger().warning("Неверные размеры области: " + size + ". Используются значения по умолчанию: 3x3x3");
                    }
                    for (Player player : world.getPlayers()) {
                        if (player.getLocation().distance(loc) < 100) {
                            player.sendMessage("§c[OpenHousing] Неверные размеры области: '" + size + "'. Используются значения по умолчанию: 3x3x3");
                        }
                    }
                }
            }
            
            for (int x = 0; x < sizeX; x++) {
                for (int y = 0; y < sizeY; y++) {
                    for (int z = 0; z < sizeZ; z++) {
                        Block block = world.getBlockAt(loc.getBlockX() + x, 
                                                     loc.getBlockY() + y, 
                                                     loc.getBlockZ() + z);
                        block.setType(material);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            // Неверный тип блока
            for (Player player : world.getPlayers()) {
                if (player.getLocation().distance(loc) < 100) {
                    player.sendMessage("§c[OpenHousing] Неверный тип блока: '" + blockType + "'. Примеры: STONE, DIAMOND_ORE, GLASS");
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
    }
    
    /**
     * Удар молнии
     */
    private void strikeLightning(World world, String location, boolean effect) {
        if (world == null) return;
        
        Location loc = parseLocation(location, world);
        if (loc != null) {
            if (effect) {
                world.strikeLightningEffect(loc);
            } else {
                world.strikeLightning(loc);
            }
        }
    }
    
    /**
     * Воспроизведение звука
     */
    private void playSound(Player player, String soundName, String location, String volume) {
        if (player == null) return;
        
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            float vol = 1.0f;
            float pitch = 1.0f;
            
            try {
                vol = Float.parseFloat(volume);
                if (vol < 0.0f || vol > 10.0f) {
                    player.sendMessage("§c[OpenHousing] Громкость звука должна быть от 0.0 до 10.0. Используется значение по умолчанию: 1.0");
                    vol = 1.0f;
                }
            } catch (NumberFormatException e) {
                // Используем значение по умолчанию
                player.sendMessage("§c[OpenHousing] Неверная громкость звука: '" + volume + "'. Используется значение по умолчанию: 1.0");
            }
            
            if (location.isEmpty()) {
                player.playSound(player.getLocation(), sound, vol, pitch);
            } else {
                Location loc = parseLocation(location, player.getWorld());
                if (loc != null) {
                    player.playSound(loc, sound, vol, pitch);
                } else {
                    player.sendMessage("§c[OpenHousing] Неверная локация для звука: '" + location + "'. Формат: x,y,z или world,x,y,z");
                }
            }
        } catch (IllegalArgumentException e) {
            // Неверное имя звука
            player.sendMessage("§c[OpenHousing] Неверное имя звука: '" + soundName + "'. Примеры: BLOCK_STONE_BREAK, ENTITY_PLAYER_LEVELUP, MUSIC_DISC_13");
        }
    }
    
    /**
     * Воспроизведение звука для всех
     */
    private void playSoundAll(World world, String soundName, String location, String volume) {
        if (world == null) return;
        
        for (Player player : world.getPlayers()) {
            playSound(player, soundName, location, volume);
        }
    }
    
    /**
     * Создание частиц
     */
    private void spawnParticle(World world, String location, String particleName, String count) {
        if (world == null) return;
        
        Location loc = parseLocation(location, world);
        if (loc == null) return;
        
        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase());
            int particleCount = 10;
            
            try {
                particleCount = Integer.parseInt(count);
                if (particleCount < 1 || particleCount > 1000) {
                    for (Player player : world.getPlayers()) {
                        if (player.getLocation().distance(loc) < 100) {
                            player.sendMessage("§c[OpenHousing] Количество частиц должно быть от 1 до 1000. Используется значение по умолчанию: 10");
                        }
                    }
                    particleCount = 10;
                }
            } catch (NumberFormatException e) {
                // Используем значение по умолчанию
                for (Player player : world.getPlayers()) {
                    if (player.getLocation().distance(loc) < 100) {
                        player.sendMessage("§c[OpenHousing] Неверное количество частиц: '" + count + "'. Используется значение по умолчанию: 10");
                    }
                }
            }
            
            world.spawnParticle(particle, loc, particleCount, 0.5, 0.5, 0.5);
        } catch (IllegalArgumentException e) {
            // Неверное имя частицы
            for (Player player : world.getPlayers()) {
                if (player.getLocation().distance(loc) < 100) {
                    player.sendMessage("§c[OpenHousing] Неверное имя частицы: '" + particleName + "'. Примеры: EXPLOSION_NORMAL, FLAME, HEART, NOTE");
                }
            }
        }
    }
    
    /**
     * Линия частиц
     */
    private void spawnParticleLine(World world, String startLoc, String endLoc, String particleName) {
        if (world == null) return;
        
        Location start = parseLocation(startLoc, world);
        Location end = parseLocation(endLoc, world);
        
        if (start == null || end == null) return;
        
        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase());
            Vector direction = end.toVector().subtract(start.toVector());
            double distance = direction.length();
            direction.normalize();
            
            for (double i = 0; i < distance; i += 0.5) {
                Location particleLoc = start.clone().add(direction.clone().multiply(i));
                world.spawnParticle(particle, particleLoc, 1, 0, 0, 0);
            }
        } catch (IllegalArgumentException e) {
            // Неверное имя частицы
        }
    }
    
    /**
     * Круг частиц
     */
    private void spawnParticleCircle(World world, String location, String particleName, String radius) {
        if (world == null) return;
        
        Location center = parseLocation(location, world);
        if (center == null) return;
        
        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase());
            double r = 3.0;
            
            try {
                r = Double.parseDouble(radius);
            } catch (NumberFormatException e) {
                // Используем значение по умолчанию
            }
            
            for (int i = 0; i < 360; i += 10) {
                double angle = Math.toRadians(i);
                double x = center.getX() + r * Math.cos(angle);
                double z = center.getZ() + r * Math.sin(angle);
                Location particleLoc = new Location(world, x, center.getY(), z);
                world.spawnParticle(particle, particleLoc, 1, 0, 0, 0);
            }
        } catch (IllegalArgumentException e) {
            // Неверное имя частицы
        }
    }
    
    /**
     * Создание взрыва
     */
    private void createExplosion(World world, String location, String power, String breakBlocks) {
        if (world == null) return;
        
        Location loc = parseLocation(location, world);
        if (loc == null) return;
        
        float explosionPower = 4.0f;
        boolean setFire = false;
        boolean destroyBlocks = Boolean.parseBoolean(breakBlocks);
        
        try {
            explosionPower = Float.parseFloat(power);
        } catch (NumberFormatException e) {
            // Используем значение по умолчанию
        }
        
        world.createExplosion(loc, explosionPower, setFire, destroyBlocks);
    }
    
    /**
     * Отправка титула
     */
    private void sendTitle(Player player, String title, String subtitle) {
        if (player == null) return;
        
        String titleText = ChatColor.translateAlternateColorCodes('&', title);
        String subtitleText = subtitle != null ? ChatColor.translateAlternateColorCodes('&', subtitle) : "";
        
        player.sendTitle(titleText, subtitleText, 20, 60, 20);
    }
    
    /**
     * Отправка action bar
     */
    private void sendActionBar(Player player, String message) {
        if (player == null) return;
        
        String text = ChatColor.translateAlternateColorCodes('&', message);
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                                  new net.md_5.bungee.api.chat.TextComponent(text));
    }
    
    /**
     * Телепортация игрока
     */
    private void teleportPlayer(Player player, String location) {
        if (player == null) return;
        
        Location loc = parseLocation(location, player.getWorld());
        if (loc != null) {
            player.teleport(loc);
        }
    }
    
    /**
     * Лечение игрока
     */
    private void healPlayer(Player player, String amount) {
        if (player == null) return;
        
        try {
            if (amount.isEmpty()) {
                player.setHealth(player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue());
            } else {
                double healAmount = Double.parseDouble(amount);
                if (healAmount < 0) {
                    player.sendMessage("§c[OpenHousing] Количество здоровья не может быть отрицательным. Используется полное исцеление.");
                    healAmount = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
                }
                double newHealth = Math.min(player.getHealth() + healAmount,
                                          player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue());
                player.setHealth(newHealth);
            }
        } catch (NumberFormatException e) {
            // Полное исцеление при ошибке
            player.sendMessage("§c[OpenHousing] Неверное количество здоровья: '" + amount + "'. Используется полное исцеление.");
            player.setHealth(player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue());
        }
    }
    
    /**
     * Выдача предмета
     */
    private void giveItem(Player player, String itemType, String amount, String name) {
        if (player == null) return;
        
        try {
            Material material = Material.valueOf(itemType.toUpperCase());
            int itemAmount = 1;
            
            try {
                itemAmount = Integer.parseInt(amount);
                if (itemAmount < 1) {
                    player.sendMessage("§c[OpenHousing] Количество предметов должно быть больше 0. Используется значение по умолчанию: 1");
                    itemAmount = 1;
                } else if (itemAmount > 64) {
                    player.sendMessage("§c[OpenHousing] Количество предметов не может быть больше 64. Используется значение по умолчанию: 1");
                    itemAmount = 1;
                }
            } catch (NumberFormatException e) {
                // Используем значение по умолчанию
                player.sendMessage("§c[OpenHousing] Неверное количество предметов: '" + amount + "'. Используется значение по умолчанию: 1");
            }
            
            org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(material, itemAmount);
            
            if (name != null && !name.isEmpty()) {
                org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
                    item.setItemMeta(meta);
                }
            }
            
            player.getInventory().addItem(item);
        } catch (IllegalArgumentException e) {
            // Неверный тип предмета
            player.sendMessage("§c[OpenHousing] Неверный тип предмета: '" + itemType + "'. Примеры: DIAMOND, STONE, APPLE, IRON_SWORD");
        }
    }
    
    /**
     * Установка игрового режима
     */
    private void setGameMode(Player player, String mode) {
        if (player == null) return;
        
        try {
            GameMode gameMode = GameMode.valueOf(mode.toUpperCase());
            player.setGameMode(gameMode);
        } catch (IllegalArgumentException e) {
            // Неверный режим игры
            player.sendMessage("§c[OpenHousing] Неверный режим игры: '" + mode + "'. Доступные: SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR");
        }
    }
    
    /**
     * Установка спавна
     */
    private void setSpawn(World world, String location) {
        if (world == null) return;
        
        Location loc = parseLocation(location, world);
        if (loc != null) {
            world.setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
    }
    
    /**
     * Установка сложности
     */
    private void setDifficulty(World world, String difficulty) {
        if (world == null) return;
        
        try {
            Difficulty diff = Difficulty.valueOf(difficulty.toUpperCase());
            world.setDifficulty(diff);
        } catch (IllegalArgumentException e) {
            // Неверная сложность
            for (Player player : world.getPlayers()) {
                player.sendMessage("§c[OpenHousing] Неверная сложность: '" + difficulty + "'. Доступные: PEACEFUL, EASY, NORMAL, HARD");
            }
        }
    }
    
    /**
     * Установка игрового правила
     */
    private void setGameRule(World world, String ruleName, String value) {
        if (world == null) return;
        
        try {
            GameRule<?> gameRule = GameRule.getByName(ruleName);
            if (gameRule != null) {
                if (gameRule.getType() == Boolean.class) {
                    @SuppressWarnings("unchecked")
                    GameRule<Boolean> boolRule = (GameRule<Boolean>) gameRule;
                    world.setGameRule(boolRule, Boolean.parseBoolean(value));
                } else if (gameRule.getType() == Integer.class) {
                    @SuppressWarnings("unchecked")
                    GameRule<Integer> intRule = (GameRule<Integer>) gameRule;
                    world.setGameRule(intRule, Integer.parseInt(value));
                }
            } else {
                for (Player player : world.getPlayers()) {
                    player.sendMessage("§c[OpenHousing] Неверное игровое правило: '" + ruleName + "'. Примеры: doDaylightCycle, keepInventory, naturalRegeneration");
                }
            }
        } catch (Exception e) {
            // Ошибка в значении правила
            for (Player player : world.getPlayers()) {
                player.sendMessage("§c[OpenHousing] Неверное значение для правила '" + ruleName + "': '" + value + "'");
            }
        }
    }
    
    /**
     * Создание фейерверка
     */
    private void createFirework(World world, String location, String colors) {
        if (world == null) return;
        
        Location loc = parseLocation(location, world);
        if (loc != null) {
            org.bukkit.entity.Firework firework = world.spawn(loc, org.bukkit.entity.Firework.class);
            org.bukkit.inventory.meta.FireworkMeta meta = firework.getFireworkMeta();
            
            org.bukkit.FireworkEffect.Builder effectBuilder = org.bukkit.FireworkEffect.builder();
            effectBuilder.with(org.bukkit.FireworkEffect.Type.BALL);
            effectBuilder.withColor(org.bukkit.Color.RED, org.bukkit.Color.BLUE, org.bukkit.Color.GREEN);
            effectBuilder.withFade(org.bukkit.Color.YELLOW);
            
            meta.addEffect(effectBuilder.build());
            meta.setPower(1);
            firework.setFireworkMeta(meta);
        }
    }
    
    /**
     * Толчок игрока
     */
    private void pushPlayer(Player player, String direction, String force) {
        if (player == null) return;
        
        double pushForce = 1.0;
        try {
            pushForce = Double.parseDouble(force);
            if (pushForce < 0.1 || pushForce > 10.0) {
                player.sendMessage("§c[OpenHousing] Сила толчка должна быть от 0.1 до 10.0. Используется значение по умолчанию: 1.0");
                pushForce = 1.0;
            }
        } catch (NumberFormatException e) {
            // Используем значение по умолчанию
            player.sendMessage("§c[OpenHousing] Неверная сила толчка: '" + force + "'. Используется значение по умолчанию: 1.0");
        }
        
        Vector pushVector = new Vector(0, 0, 0);
        
        switch (direction.toLowerCase()) {
            case "forward":
                pushVector = player.getLocation().getDirection().multiply(pushForce);
                break;
            case "backward":
                pushVector = player.getLocation().getDirection().multiply(-pushForce);
                break;
            case "up":
                pushVector = new Vector(0, pushForce, 0);
                break;
            case "down":
                pushVector = new Vector(0, -pushForce, 0);
                break;
            case "left":
                pushVector = player.getLocation().getDirection().rotateAroundY(Math.PI/2).multiply(pushForce);
                break;
            case "right":
                pushVector = player.getLocation().getDirection().rotateAroundY(-Math.PI/2).multiply(pushForce);
                break;
            default:
                player.sendMessage("§c[OpenHousing] Неверное направление толчка: '" + direction + "'. Доступные: forward, backward, up, down, left, right");
                return;
        }
        
        player.setVelocity(pushVector);
    }
    
    /**
     * Установка опыта
     */
    private void setExperience(Player player, String value) {
        if (player == null) return;
        
        try {
            int level = Integer.parseInt(value);
            if (level < 0) {
                player.sendMessage("§c[OpenHousing] Уровень опыта не может быть отрицательным. Устанавливается 0.");
                level = 0;
            } else if (level > 1000) {
                player.sendMessage("§c[OpenHousing] Уровень опыта слишком высокий: " + level + ". Максимум: 1000");
                level = 1000;
            }
            player.setLevel(level);
            player.setExp(0);
        } catch (NumberFormatException e) {
            // Игнорируем ошибку
            player.sendMessage("§c[OpenHousing] Неверный уровень опыта: '" + value + "'. Ожидается число от 0 до 1000");
        }
    }
    
    /**
     * Ожидание
     */
    private void waitAction(ExecutionContext context, String seconds) {
        try {
            int delay = Integer.parseInt(seconds);
            if (delay < 1) {
                if (context.getPlayer() != null) {
                    context.getPlayer().sendMessage("§c[OpenHousing] Время ожидания должно быть больше 0. Используется 1 секунда.");
                }
                delay = 1;
            } else if (delay > 300) {
                if (context.getPlayer() != null) {
                    context.getPlayer().sendMessage("§c[OpenHousing] Время ожидания слишком большое: " + delay + " сек. Максимум: 300 сек (5 мин)");
                }
                delay = 300;
            }
            // Используем Bukkit Scheduler вместо Thread.sleep
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                ru.openhousing.OpenHousing.getInstance(),
                () -> {
                    // Здесь можно добавить логику, которая должна выполниться после задержки
                    if (context.getPlayer() != null) {
                        context.getPlayer().sendMessage("§aОжидание завершено!");
                    }
                },
                delay * 20L // Конвертируем секунды в тики (20 тиков = 1 секунда)
            );
        } catch (NumberFormatException e) {
            // Игнорируем ошибки
            if (context.getPlayer() != null) {
                context.getPlayer().sendMessage("§c[OpenHousing] Неверное время ожидания: '" + seconds + "'. Ожидается число от 1 до 300");
            }
        }
    }
    
    /**
     * Парсинг времени
     */
    private long parseTime(String timeString) {
        switch (timeString.toLowerCase()) {
            case "dawn":
            case "sunrise":
                return 0;
            case "day":
            case "noon":
                return 6000;
            case "dusk":
            case "sunset":
                return 12000;
            case "night":
            case "midnight":
                return 18000;
            default:
                return Long.parseLong(timeString);
        }
    }
    
    /**
     * Парсинг локации
     */
    private Location parseLocation(String locationString, World world) {
        return ru.openhousing.utils.CodeBlockUtils.parseLocation(locationString, world);
    }
    
    /**
     * Замена переменных в строке
     */
    private String replaceVariables(String text, ExecutionContext context) {
        return ru.openhousing.utils.CodeBlockUtils.replaceVariables(text, context);
    }
    
    @Override
    public boolean validate() {
        return getParameter(ru.openhousing.coding.constants.BlockParams.ACTION_TYPE) != null;
    }
    
    @Override
    public List<String> getDescription() {
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
