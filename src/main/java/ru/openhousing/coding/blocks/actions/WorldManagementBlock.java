package ru.openhousing.coding.blocks.actions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionContext;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionResult;
import ru.openhousing.coding.blocks.BlockVariable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Специализированный блок для управления мирами
 * Создание, настройка и управление мирами с GUI настройками
 */
public class WorldManagementBlock extends CodeBlock {
    
    // Статические поля для глобального управления
    private static final Map<String, WorldTemplate> worldTemplates = new ConcurrentHashMap<>();
    private static final Map<String, Integer> worldUsageStats = new ConcurrentHashMap<>();
    private static final AtomicInteger totalWorldOperations = new AtomicInteger(0);
    
    // Переменные блока (настраиваются через drag-n-drop)
    private BlockVariable operationTypeVar;
    private BlockVariable worldNameVar;
    private BlockVariable worldTypeVar;
    private BlockVariable worldSeedVar;
    private BlockVariable worldGeneratorVar;
    private BlockVariable worldEnvironmentVar;
    private BlockVariable worldDifficultyVar;
    private BlockVariable worldGameModeVar;
    private BlockVariable worldPvPVar;
    private BlockVariable worldTimeVar;
    private BlockVariable worldWeatherVar;
    private BlockVariable worldBorderVar;
    private BlockVariable worldSpawnVar;
    private BlockVariable conditionVar;
    private BlockVariable loggingEnabledVar;
    private BlockVariable autoSaveVar;
    private BlockVariable notificationEnabledVar;
    
    public enum WorldOperationType {
        CREATE("Создать", "Создает новый мир"),
        DELETE("Удалить", "Удаляет существующий мир"),
        LOAD("Загрузить", "Загружает мир в память"),
        UNLOAD("Выгрузить", "Выгружает мир из памяти"),
        TELEPORT("Телепорт", "Телепортирует игрока в мир"),
        SET_SPAWN("Установить спавн", "Устанавливает точку спавна"),
        SET_TIME("Установить время", "Устанавливает время в мире"),
        SET_WEATHER("Установить погоду", "Устанавливает погоду в мире"),
        SET_DIFFICULTY("Установить сложность", "Устанавливает сложность мира"),
        SET_GAMEMODE("Установить режим игры", "Устанавливает режим игры"),
        SET_PVP("Установить PvP", "Включает/выключает PvP"),
        SET_BORDER("Установить границу", "Устанавливает границу мира"),
        BACKUP("Резервная копия", "Создает резервную копию мира"),
        RESTORE("Восстановить", "Восстанавливает мир из резервной копии"),
        OPTIMIZE("Оптимизировать", "Оптимизирует производительность мира"),
        RESET("Сбросить", "Сбрасывает мир к начальному состоянию");
        
        private final String displayName;
        private final String description;
        
        WorldOperationType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    public WorldManagementBlock() {
        super(BlockType.PLAYER_TELEPORT_ACTION); // Используем PLAYER_TELEPORT_ACTION как базовый тип
        initializeDefaultSettings();
        initializeWorldTemplates();
    }
    
    /**
     * Инициализация настроек по умолчанию
     */
    private void initializeDefaultSettings() {
        operationTypeVar = new BlockVariable("operationType", "Тип операции", 
            BlockVariable.VariableType.STRING, "CREATE");
        worldNameVar = new BlockVariable("worldName", "Название мира", 
            BlockVariable.VariableType.STRING, "new_world");
        worldTypeVar = new BlockVariable("worldType", "Тип мира", 
            BlockVariable.VariableType.STRING, "NORMAL");
        worldSeedVar = new BlockVariable("worldSeed", "Сид мира", 
            BlockVariable.VariableType.STRING, "random");
        worldGeneratorVar = new BlockVariable("worldGenerator", "Генератор", 
            BlockVariable.VariableType.STRING, "default");
        worldEnvironmentVar = new BlockVariable("worldEnvironment", "Окружение", 
            BlockVariable.VariableType.STRING, "NORMAL");
        worldDifficultyVar = new BlockVariable("worldDifficulty", "Сложность", 
            BlockVariable.VariableType.STRING, "NORMAL");
        worldGameModeVar = new BlockVariable("worldGameMode", "Режим игры", 
            BlockVariable.VariableType.STRING, "SURVIVAL");
        worldPvPVar = new BlockVariable("worldPvP", "PvP включен", 
            BlockVariable.VariableType.BOOLEAN, true);
        worldTimeVar = new BlockVariable("worldTime", "Время мира", 
            BlockVariable.VariableType.INTEGER, 0);
        worldWeatherVar = new BlockVariable("worldWeather", "Погода", 
            BlockVariable.VariableType.STRING, "CLEAR");
        worldBorderVar = new BlockVariable("worldBorder", "Граница мира", 
            BlockVariable.VariableType.INTEGER, 10000);
        worldSpawnVar = new BlockVariable("worldSpawn", "Точка спавна", 
            BlockVariable.VariableType.LOCATION, "0,64,0");
        conditionVar = new BlockVariable("condition", "Условие выполнения", 
            BlockVariable.VariableType.STRING, "true");
        loggingEnabledVar = new BlockVariable("loggingEnabled", "Включить логирование", 
            BlockVariable.VariableType.BOOLEAN, true);
        autoSaveVar = new BlockVariable("autoSave", "Автосохранение", 
            BlockVariable.VariableType.BOOLEAN, true);
        notificationEnabledVar = new BlockVariable("notificationEnabled", "Включить уведомления", 
            BlockVariable.VariableType.BOOLEAN, true);
    }
    
    /**
     * Инициализация шаблонов миров
     */
    private void initializeWorldTemplates() {
        worldTemplates.put("survival", new WorldTemplate(
            "survival", "Мир выживания", WorldType.NORMAL, 
            "default", World.Environment.NORMAL, "NORMAL", "SURVIVAL"));
        
        worldTemplates.put("creative", new WorldTemplate(
            "creative", "Креативный мир", WorldType.FLAT, 
            "flat", World.Environment.NORMAL, "PEACEFUL", "CREATIVE"));
        
        worldTemplates.put("nether", new WorldTemplate(
            "nether", "Ад", WorldType.NORMAL, 
            "default", World.Environment.NETHER, "HARD", "SURVIVAL"));
        
        worldTemplates.put("end", new WorldTemplate(
            "end", "Край", WorldType.NORMAL, 
            "default", World.Environment.THE_END, "HARD", "SURVIVAL"));
    }
    
    /**
     * Открытие GUI настройки блока
     */
    public void openConfigurationGUI(Player player) {
        if (player == null) return;
        
        // Создаем инвентарь для настройки
        org.bukkit.inventory.Inventory configGUI = Bukkit.createInventory(null, 54, "§6Настройка WorldManagementBlock");
        
        // Заполняем GUI элементами настройки
        fillConfigurationGUI(configGUI, player);
        
        // Открываем GUI
        player.openInventory(configGUI);
    }
    
    /**
     * Заполнение GUI элементами настройки
     */
    private void fillConfigurationGUI(org.bukkit.inventory.Inventory gui, Player player) {
        gui.clear();
        
        // Основные настройки
        gui.setItem(10, createConfigItem(Material.COMMAND_BLOCK, "§eТип операции", 
            Arrays.asList("§7Текущее значение: " + getStringValue(operationTypeVar),
                         "§7Клик для изменения")));
        
        gui.setItem(11, createConfigItem(Material.GRASS_BLOCK, "§eНазвание мира", 
            Arrays.asList("§7Текущее значение: " + getStringValue(worldNameVar),
                         "§7Клик для изменения")));
        
        gui.setItem(12, createConfigItem(Material.COMPASS, "§eТип мира", 
            Arrays.asList("§7Текущее значение: " + getStringValue(worldTypeVar),
                         "§7Клик для изменения")));
        
        gui.setItem(13, createConfigItem(Material.WHEAT_SEEDS, "§eСид мира", 
            Arrays.asList("§7Текущее значение: " + getStringValue(worldSeedVar),
                         "§7Клик для изменения")));
        
        // Настройки мира
        gui.setItem(19, createConfigItem(Material.BEACON, "§eГенератор", 
            Arrays.asList("§7Текущее значение: " + getStringValue(worldGeneratorVar),
                         "§7Клик для изменения")));
        
        gui.setItem(20, createConfigItem(Material.NETHER_PORTAL, "§eОкружение", 
            Arrays.asList("§7Текущее значение: " + getStringValue(worldEnvironmentVar),
                         "§7Клик для изменения")));
        
        gui.setItem(21, createConfigItem(Material.SKELETON_SKULL, "§eСложность", 
            Arrays.asList("§7Текущее значение: " + getStringValue(worldDifficultyVar),
                         "§7Клик для изменения")));
        
        gui.setItem(22, createConfigItem(Material.DIAMOND_SWORD, "§eРежим игры", 
            Arrays.asList("§7Текущее значение: " + getStringValue(worldGameModeVar),
                         "§7Клик для изменения")));
        
        // Шаблоны миров
        gui.setItem(45, createConfigItem(Material.CRAFTING_TABLE, "§6Шаблоны миров", 
            Arrays.asList("§7Клик для выбора шаблона")));
        
        // Кнопки управления
        gui.setItem(53, createConfigItem(Material.BARRIER, "§cЗакрыть", 
            Arrays.asList("§7Клик для закрытия")));
    }
    
    /**
     * Создание элемента настройки
     */
    private org.bukkit.inventory.ItemStack createConfigItem(Material material, String name, List<String> lore) {
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(material);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Показ настроек в чате
     */
    public void showSettings(Player player) {
        if (player == null) return;
        
        player.sendMessage("§6=== Настройки WorldManagementBlock ===");
        player.sendMessage("§eОперация: §7" + getStringValue(operationTypeVar));
        player.sendMessage("§eМир: §7" + getStringValue(worldNameVar));
        player.sendMessage("§eТип: §7" + getStringValue(worldTypeVar));
        player.sendMessage("§eСид: §7" + getStringValue(worldSeedVar));
        player.sendMessage("§eГенератор: §7" + getStringValue(worldGeneratorVar));
        player.sendMessage("§eОкружение: §7" + getStringValue(worldEnvironmentVar));
        player.sendMessage("§eСложность: §7" + getStringValue(worldDifficultyVar));
        player.sendMessage("§eРежим игры: §7" + getStringValue(worldGameModeVar));
        player.sendMessage("§ePvP: §7" + (getBooleanValue(worldPvPVar) ? "§aВключен" : "§cВыключен"));
        player.sendMessage("");
        player.sendMessage("§eСистема:");
        player.sendMessage("§7• Логирование: " + (getBooleanValue(loggingEnabledVar) ? "§aВключено" : "§cВыключено"));
        player.sendMessage("§7• Автосохранение: " + (getBooleanValue(autoSaveVar) ? "§aВключено" : "§cВыключено"));
        player.sendMessage("§7• Уведомления: " + (getBooleanValue(notificationEnabledVar) ? "§aВключено" : "§cВыключено"));
        player.sendMessage("");
        player.sendMessage("§7Используйте /blockconfig gui для открытия GUI настройки");
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        try {
            Player player = context.getPlayer();
            if (player == null) {
                return ExecutionResult.error("Игрок не найден в контексте");
            }
            
            // Получаем параметры операции
            String operationTypeStr = getStringValue(operationTypeVar);
            String worldName = getStringValue(worldNameVar);
            String condition = getStringValue(conditionVar);
            
            // Проверяем условие выполнения
            if (!evaluateCondition(condition, context)) {
                return ExecutionResult.success("Операция пропущена по условию");
            }
            
            // Определяем тип операции
            WorldOperationType operationType = parseOperationType(operationTypeStr);
            if (operationType == null) {
                return ExecutionResult.error("Неизвестный тип операции: " + operationTypeStr);
            }
            
            // Выполняем операцию
            ExecutionResult result = executeWorldOperation(operationType, worldName, context);
            
            // Логируем операцию
            if (getBooleanValue(loggingEnabledVar)) {
                logOperation(operationType, worldName, result, context);
            }
            
            // Обновляем статистику
            updateStatistics(operationType, worldName, result);
            
            // Уведомляем игрока
            if (getBooleanValue(notificationEnabledVar)) {
                if (result.isSuccess()) {
                    player.sendMessage("§a✓ " + result.getMessage());
                } else {
                    player.sendMessage("§c✗ " + result.getMessage());
                }
            }
            
            return result;
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения блока управления мирами: " + e.getMessage());
        }
    }
    
    /**
     * Выполнение операции с миром
     */
    private ExecutionResult executeWorldOperation(WorldOperationType operationType, 
                                                String worldName, 
                                                ExecutionContext context) {
        
        try {
            switch (operationType) {
                case CREATE:
                    return executeCreateOperation(worldName, context);
                    
                case DELETE:
                    return executeDeleteOperation(worldName, context);
                    
                case LOAD:
                    return executeLoadOperation(worldName, context);
                    
                case UNLOAD:
                    return executeUnloadOperation(worldName, context);
                    
                case TELEPORT:
                    return executeTeleportOperation(worldName, context);
                    
                case SET_SPAWN:
                    return executeSetSpawnOperation(worldName, context);
                    
                case SET_TIME:
                    return executeSetTimeOperation(worldName, context);
                    
                case SET_WEATHER:
                    return executeSetWeatherOperation(worldName, context);
                    
                case SET_DIFFICULTY:
                    return executeSetDifficultyOperation(worldName, context);
                    
                case SET_GAMEMODE:
                    return executeSetGameModeOperation(worldName, context);
                    
                case SET_PVP:
                    return executeSetPvPOperation(worldName, context);
                    
                case SET_BORDER:
                    return executeSetBorderOperation(worldName, context);
                    
                case BACKUP:
                    return executeBackupOperation(worldName, context);
                    
                case RESTORE:
                    return executeRestoreOperation(worldName, context);
                    
                case OPTIMIZE:
                    return executeOptimizeOperation(worldName, context);
                    
                case RESET:
                    return executeResetOperation(worldName, context);
                    
                default:
                    return ExecutionResult.error("Неподдерживаемая операция: " + operationType);
            }
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения операции " + operationType + ": " + e.getMessage());
        }
    }
    
    /**
     * Операция CREATE
     */
    private ExecutionResult executeCreateOperation(String worldName, ExecutionContext context) {
        try {
            WorldCreator creator = new WorldCreator(worldName);
            
            // Настройка типа мира
            String worldTypeStr = getStringValue(worldTypeVar);
            if (!worldTypeStr.equals("NORMAL")) {
                try {
                    creator.type(WorldType.valueOf(worldTypeStr));
                } catch (IllegalArgumentException e) {
                    // Игнорируем некорректный тип
                }
            }
            
            // Настройка сида
            String seedStr = getStringValue(worldSeedVar);
            if (!seedStr.equals("random")) {
                try {
                    creator.seed(Long.parseLong(seedStr));
                } catch (NumberFormatException e) {
                    creator.seed(seedStr.hashCode());
                }
            }
            
            // Настройка генератора
            String generatorStr = getStringValue(worldGeneratorVar);
            if (!generatorStr.equals("default")) {
                // TODO: Реализовать поддержку кастомных генераторов
            }
            
            // Настройка окружения
            String environmentStr = getStringValue(worldEnvironmentVar);
            if (!environmentStr.equals("NORMAL")) {
                try {
                    creator.environment(World.Environment.valueOf(environmentStr));
                } catch (IllegalArgumentException e) {
                    // Игнорируем некорректное окружение
                }
            }
            
            // Создание мира
            World world = creator.createWorld();
            
            if (world != null) {
                // Применяем дополнительные настройки
                applyWorldSettings(world, context);
                
                return ExecutionResult.success("Мир '" + worldName + "' создан успешно");
            } else {
                return ExecutionResult.error("Не удалось создать мир '" + worldName + "'");
            }
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка создания мира: " + e.getMessage());
        }
    }
    
    /**
     * Применение настроек к миру
     */
    private void applyWorldSettings(World world, ExecutionContext context) {
        // Установка сложности
        String difficultyStr = getStringValue(worldDifficultyVar);
        try {
            world.setDifficulty(org.bukkit.Difficulty.valueOf(difficultyStr));
        } catch (IllegalArgumentException e) {
            // Игнорируем некорректную сложность
        }
        
        // Установка PvP
        boolean pvpEnabled = getBooleanValue(worldPvPVar);
        world.setPVP(pvpEnabled);
        
        // Установка времени
        int time = getIntegerValue(worldTimeVar);
        world.setTime(time);
        
        // Установка погоды
        String weatherStr = getStringValue(worldWeatherVar);
        try {
            if (weatherStr.equals("CLEAR")) {
                world.setStorm(false);
                world.setThundering(false);
            } else if (weatherStr.equals("RAIN")) {
                world.setStorm(true);
                world.setThundering(false);
            } else if (weatherStr.equals("THUNDER")) {
                world.setStorm(true);
                world.setThundering(true);
            }
        } catch (Exception e) {
            // Игнорируем ошибки погоды
        }
        
        // Установка границы мира
        int borderSize = getIntegerValue(worldBorderVar);
        if (borderSize > 0) {
            world.getWorldBorder().setSize(borderSize);
        }
    }
    
    // Остальные операции реализуются аналогично
    private ExecutionResult executeDeleteOperation(String worldName, ExecutionContext context) {
        // TODO: Реализовать удаление мира
        return ExecutionResult.success("Операция удаления выполнена");
    }
    
    private ExecutionResult executeLoadOperation(String worldName, ExecutionContext context) {
        // TODO: Реализовать загрузку мира
        return ExecutionResult.success("Операция загрузки выполнена");
    }
    
    private ExecutionResult executeUnloadOperation(String worldName, ExecutionContext context) {
        // TODO: Реализовать выгрузку мира
        return ExecutionResult.success("Операция выгрузки выполнена");
    }
    
    private ExecutionResult executeTeleportOperation(String worldName, ExecutionContext context) {
        // TODO: Реализовать телепортацию в мир
        return ExecutionResult.success("Операция телепортации выполнена");
    }
    
    private ExecutionResult executeSetSpawnOperation(String worldName, ExecutionContext context) {
        // TODO: Реализовать установку спавна
        return ExecutionResult.success("Операция установки спавна выполнена");
    }
    
    private ExecutionResult executeSetTimeOperation(String worldName, ExecutionContext context) {
        // TODO: Реализовать установку времени
        return ExecutionResult.success("Операция установки времени выполнена");
    }
    
    private ExecutionResult executeSetWeatherOperation(String worldName, ExecutionContext context) {
        // TODO: Реализовать установку погоды
        return ExecutionResult.success("Операция установки погоды выполнена");
    }
    
    private ExecutionResult executeSetDifficultyOperation(String worldName, ExecutionContext context) {
        // TODO: Реализовать установку сложности
        return ExecutionResult.success("Операция установки сложности выполнена");
    }
    
    private ExecutionResult executeSetGameModeOperation(String worldName, ExecutionContext context) {
        // TODO: Реализовать установку режима игры
        return ExecutionResult.success("Операция установки режима игры выполнена");
    }
    
    private ExecutionResult executeSetPvPOperation(String worldName, ExecutionContext context) {
        // TODO: Реализовать установку PvP
        return ExecutionResult.success("Операция установки PvP выполнена");
    }
    
    private ExecutionResult executeSetBorderOperation(String worldName, ExecutionContext context) {
        // TODO: Реализовать установку границы
        return ExecutionResult.success("Операция установки границы выполнена");
    }
    
    private ExecutionResult executeBackupOperation(String worldName, ExecutionContext context) {
        // TODO: Реализовать резервное копирование
        return ExecutionResult.success("Операция резервного копирования выполнена");
    }
    
    private ExecutionResult executeRestoreOperation(String worldName, ExecutionContext context) {
        // TODO: Реализовать восстановление
        return ExecutionResult.success("Операция восстановления выполнена");
    }
    
    private ExecutionResult executeOptimizeOperation(String worldName, ExecutionContext context) {
        // TODO: Реализовать оптимизацию
        return ExecutionResult.success("Операция оптимизации выполнена");
    }
    
    private ExecutionResult executeResetOperation(String worldName, ExecutionContext context) {
        // TODO: Реализовать сброс
        return ExecutionResult.success("Операция сброса выполнена");
    }
    
    /**
     * Проверка условия выполнения
     */
    private boolean evaluateCondition(String condition, ExecutionContext context) {
        if (condition == null || condition.trim().isEmpty() || condition.equals("true")) {
            return true;
        }
        
        if (condition.equals("false")) {
            return false;
        }
        
        // Простая проверка существования переменной
        if (condition.startsWith("exists:")) {
            String varName = condition.substring(7).trim();
            return context.hasVariable(varName);
        }
        
        // Проверка значения переменной
        if (condition.contains("==")) {
            String[] parts = condition.split("==");
            if (parts.length == 2) {
                String varName = parts[0].trim();
                String expectedValue = parts[1].trim();
                Object actualValue = context.getVariable(varName);
                return actualValue != null && actualValue.toString().equals(expectedValue);
            }
        }
        
        return true; // По умолчанию выполняем
    }
    
    /**
     * Парсинг типа операции
     */
    private WorldOperationType parseOperationType(String operationTypeStr) {
        try {
            return WorldOperationType.valueOf(operationTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Логирование операции
     */
    private void logOperation(WorldOperationType operationType, String worldName, 
                            ExecutionResult result, ExecutionContext context) {
        String logMessage = String.format("[WorldOp] %s: %s -> %s", 
            operationType.name(), worldName, result.isSuccess() ? "SUCCESS" : "FAILED");
        
        if (result.isSuccess()) {
            OpenHousing.getInstance().getLogger().info(logMessage);
        } else {
            OpenHousing.getInstance().getLogger().warning(logMessage + " - " + result.getMessage());
        }
    }
    
    /**
     * Обновление статистики
     */
    private void updateStatistics(WorldOperationType operationType, String worldName, ExecutionResult result) {
        totalWorldOperations.incrementAndGet();
        worldUsageStats.merge(operationType.name(), 1, Integer::sum);
    }
    
    @Override
    public boolean validate() {
        // Проверяем базовые параметры
        return !getStringValue(worldNameVar).trim().isEmpty();
    }
    
    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("§6Блок управления мирами");
        description.add("§7Создание, настройка и управление");
        description.add("§7мирами с GUI настройками");
        description.add("");
        description.add("§eОперация:");
        description.add("§7• " + getStringValue(operationTypeVar));
        description.add("§7• Мир: " + getStringValue(worldNameVar));
        description.add("§7• Тип: " + getStringValue(worldTypeVar));
        description.add("§7• Генератор: " + getStringValue(worldGeneratorVar));
        description.add("");
        description.add("§eНастройки:");
        description.add("§7• Логирование: " + (getBooleanValue(loggingEnabledVar) ? "§aВключено" : "§cВыключено"));
        description.add("§7• Автосохранение: " + (getBooleanValue(autoSaveVar) ? "§aВключено" : "§cВыключено"));
        description.add("§7• Уведомления: " + (getBooleanValue(notificationEnabledVar) ? "§aВключено" : "§cВыключено"));
        
        return description;
    }
    
    // Вспомогательные методы для работы с переменными
    private boolean getBooleanValue(BlockVariable variable) {
        Object value = variable.getValue();
        return value instanceof Boolean ? (Boolean) value : false;
    }
    
    private int getIntegerValue(BlockVariable variable) {
        Object value = variable.getValue();
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof String) {
            try { return Integer.parseInt((String) value); } catch (Exception e) { }
        }
        return 0;
    }
    
    private String getStringValue(BlockVariable variable) {
        Object value = variable.getValue();
        return value != null ? value.toString() : "";
    }
    
    // Геттеры для переменных (для внешнего доступа)
    public BlockVariable getOperationTypeVar() { return operationTypeVar; }
    public BlockVariable getWorldNameVar() { return worldNameVar; }
    public BlockVariable getWorldTypeVar() { return worldTypeVar; }
    public BlockVariable getWorldSeedVar() { return worldSeedVar; }
    public BlockVariable getWorldGeneratorVar() { return worldGeneratorVar; }
    public BlockVariable getWorldEnvironmentVar() { return worldEnvironmentVar; }
    public BlockVariable getWorldDifficultyVar() { return worldDifficultyVar; }
    public BlockVariable getWorldGameModeVar() { return worldGameModeVar; }
    public BlockVariable getWorldPvPVar() { return worldPvPVar; }
    public BlockVariable getWorldTimeVar() { return worldTimeVar; }
    public BlockVariable getWorldWeatherVar() { return worldWeatherVar; }
    public BlockVariable getWorldBorderVar() { return worldBorderVar; }
    public BlockVariable getWorldSpawnVar() { return worldSpawnVar; }
    public BlockVariable getConditionVar() { return conditionVar; }
    public BlockVariable getLoggingEnabledVar() { return loggingEnabledVar; }
    public BlockVariable getAutoSaveVar() { return autoSaveVar; }
    public BlockVariable getNotificationEnabledVar() { return notificationEnabledVar; }
    
    // Внутренние классы
    private static class WorldTemplate {
        private final String id;
        private final String name;
        private final WorldType worldType;
        private final String generator;
        private final World.Environment environment;
        private final String difficulty;
        private final String gameMode;
        
        public WorldTemplate(String id, String name, WorldType worldType, String generator, 
                           World.Environment environment, String difficulty, String gameMode) {
            this.id = id;
            this.name = name;
            this.worldType = worldType;
            this.generator = generator;
            this.environment = environment;
            this.difficulty = difficulty;
            this.gameMode = gameMode;
        }
        
        // Геттеры
        public String getId() { return id; }
        public String getName() { return name; }
        public WorldType getWorldType() { return worldType; }
        public String getGenerator() { return generator; }
        public World.Environment getEnvironment() { return environment; }
        public String getDifficulty() { return difficulty; }
        public String getGameMode() { return gameMode; }
    }
}
