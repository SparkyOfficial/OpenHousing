package ru.openhousing.coding.blocks.events;

import org.bukkit.entity.Player;
import org.bukkit.event.block.*;
import org.bukkit.event.weather.*;
import org.bukkit.event.world.*;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок событий мира
 */
public class WorldEventBlock extends CodeBlock {
    
    public enum WorldEventType {
        BLOCK_BREAK("Разрушение блока", "Когда блок разрушается"),
        BLOCK_PLACE("Установка блока", "Когда блок устанавливается"),
        BLOCK_BURN("Горение блока", "Когда блок сгорает"),
        BLOCK_EXPLODE("Взрыв блока", "Когда блок взрывается"),
        BLOCK_FADE("Исчезновение блока", "Когда блок исчезает (лед тает)"),
        BLOCK_FORM("Образование блока", "Когда блок образуется (лед замерзает)"),
        BLOCK_GROW("Рост блока", "Когда блок растет (растения)"),
        LEAVES_DECAY("Опадание листьев", "Когда листья опадают"),
        REDSTONE_CHANGE("Изменение редстоуна", "Когда меняется сигнал редстоуна"),
        PISTON_EXTEND("Выдвижение поршня", "Когда поршень выдвигается"),
        PISTON_RETRACT("Втягивание поршня", "Когда поршень втягивается"),
        
        WEATHER_CHANGE("Смена погоды", "Когда меняется погода"),
        THUNDER_CHANGE("Изменение грозы", "Когда начинается/заканчивается гроза"),
        LIGHTNING_STRIKE("Удар молнии", "Когда ударяет молния"),
        
        TIME_SKIP("Пропуск времени", "Когда время пропускается (сон)"),
        
        WORLD_LOAD("Загрузка мира", "Когда мир загружается"),
        WORLD_UNLOAD("Выгрузка мира", "Когда мир выгружается"),
        WORLD_SAVE("Сохранение мира", "Когда мир сохраняется"),
        
        STRUCTURE_GROW("Рост структуры", "Когда растет дерево или другая структура"),
        CHUNK_LOAD("Загрузка чанка", "Когда чанк загружается"),
        CHUNK_UNLOAD("Выгрузка чанка", "Когда чанк выгружается"),
        
        PORTAL_CREATE("Создание портала", "Когда создается портал"),
        
        EXPLOSION("Взрыв", "Когда происходит взрыв"),
        
        SIGN_CHANGE("Изменение таблички", "Когда меняется текст на табличке"),
        
        WATER_LEVEL_CHANGE("Изменение уровня воды", "Когда меняется уровень воды"),
        LAVA_LEVEL_CHANGE("Изменение уровня лавы", "Когда меняется уровень лавы"),
        
        DISPENSER_DISPENSE("Раздача диспенсера", "Когда диспенсер выдает предмет"),
        DROPPER_DROP("Сброс дроппера", "Когда дроппер сбрасывает предмет"),
        HOPPER_MOVE("Движение воронки", "Когда воронка перемещает предмет");
        
        private final String displayName;
        private final String description;
        
        WorldEventType(String displayName, String description) {
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
    
    public WorldEventBlock() {
        super(BlockType.WORLD_EVENT);
        setParameter("eventType", WorldEventType.BLOCK_BREAK);
        setParameter("blockType", ""); // Тип блока (или "любой")
        setParameter("world", ""); // Мир (или "любой")
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        // События выполняются автоматически при регистрации
        return executeChildren(context);
    }
    
    @Override
    public boolean validate() {
        return getParameter("eventType") != null;
    }
    
    @Override
    public List<String> getDescription() {
        Object eventTypeParam = getParameter("eventType");
        WorldEventType eventType = null;
        
        if (eventTypeParam instanceof WorldEventType) {
            eventType = (WorldEventType) eventTypeParam;
        } else if (eventTypeParam instanceof String) {
            try {
                eventType = WorldEventType.valueOf((String) eventTypeParam);
            } catch (IllegalArgumentException e) {
                // Игнорируем неверные значения
            }
        }
        
        String blockType = (String) getParameter("blockType");
        String world = (String) getParameter("world");
        
        return Arrays.asList(
            "§6Событие мира",
            "§7Тип события: §f" + (eventType != null ? eventType.getDisplayName() : "Не выбран"),
            "§7Тип блока: §f" + (blockType != null && !blockType.isEmpty() ? blockType : "Любой"),
            "§7Мир: §f" + (world != null && !world.isEmpty() ? world : "Любой"),
            "§7Описание: §f" + (eventType != null ? eventType.getDescription() : ""),
            "",
            "§8Дочерних блоков: " + childBlocks.size()
        );
    }
    
    /**
     * Проверка соответствия события
     */
    public boolean matchesEvent(Class<?> eventClass, Object... params) {
        Object eventTypeParam = getParameter("eventType");
        WorldEventType eventType = null;
        
        if (eventTypeParam instanceof WorldEventType) {
            eventType = (WorldEventType) eventTypeParam;
        } else if (eventTypeParam instanceof String) {
            try {
                eventType = WorldEventType.valueOf((String) eventTypeParam);
            } catch (IllegalArgumentException e) {
                // Игнорируем неверные значения
                return false;
            }
        }
        
        if (eventType == null) return false;
        
        switch (eventType) {
            case BLOCK_BREAK:
                return eventClass == BlockBreakEvent.class;
            case BLOCK_PLACE:
                return eventClass == BlockPlaceEvent.class;
            case BLOCK_BURN:
                return eventClass == BlockBurnEvent.class;
            case BLOCK_EXPLODE:
                return eventClass == BlockExplodeEvent.class;
            case BLOCK_FADE:
                return eventClass == BlockFadeEvent.class;
            case BLOCK_FORM:
                return eventClass == BlockFormEvent.class;
            case BLOCK_GROW:
                return eventClass == BlockGrowEvent.class;
            case LEAVES_DECAY:
                return eventClass == LeavesDecayEvent.class;
            case REDSTONE_CHANGE:
                return eventClass == BlockRedstoneEvent.class;
            case PISTON_EXTEND:
                return eventClass == BlockPistonExtendEvent.class;
            case PISTON_RETRACT:
                return eventClass == BlockPistonRetractEvent.class;
            
            case WEATHER_CHANGE:
                return eventClass == WeatherChangeEvent.class;
            case THUNDER_CHANGE:
                return eventClass == ThunderChangeEvent.class;
            case LIGHTNING_STRIKE:
                return eventClass == LightningStrikeEvent.class;
            
            case TIME_SKIP:
                return eventClass == TimeSkipEvent.class;
            
            case WORLD_LOAD:
                return eventClass == WorldLoadEvent.class;
            case WORLD_UNLOAD:
                return eventClass == WorldUnloadEvent.class;
            case WORLD_SAVE:
                return eventClass == WorldSaveEvent.class;
            
            case STRUCTURE_GROW:
                return eventClass == StructureGrowEvent.class;
            case CHUNK_LOAD:
                return eventClass == ChunkLoadEvent.class;
            case CHUNK_UNLOAD:
                return eventClass == ChunkUnloadEvent.class;
            
            case PORTAL_CREATE:
                return eventClass == PortalCreateEvent.class;
            
            case SIGN_CHANGE:
                return eventClass == SignChangeEvent.class;
            
            case DISPENSER_DISPENSE:
                return eventClass == BlockDispenseEvent.class;
            
            case EXPLOSION:
                // TODO: Реализовать логику для взрыва
                return false;
            case WATER_LEVEL_CHANGE:
                // TODO: Реализовать логику для изменения уровня воды
                return false;
            case LAVA_LEVEL_CHANGE:
                // TODO: Реализовать логику для изменения уровня лавы
                return false;
            case DROPPER_DROP:
                // TODO: Реализовать логику для сброса дроппера
                return false;
            case HOPPER_MOVE:
                // TODO: Реализовать логику для движения воронки
                return false;
            
            default:
                return false;
        }
    }
    
    /**
     * Создание контекста выполнения из события
     */
    public ExecutionContext createContextFromEvent(Player player, Object event) {
        ExecutionContext context = new ExecutionContext(player);
        
        WorldEventType eventType = (WorldEventType) getParameter("eventType");
        if (eventType != null) {
            
            // Обработка блочных событий
            if (event instanceof BlockEvent) {
                BlockEvent blockEvent = (BlockEvent) event;
                context.setVariable("block_type", blockEvent.getBlock().getType().name());
                context.setVariable("block_x", blockEvent.getBlock().getX());
                context.setVariable("block_y", blockEvent.getBlock().getY());
                context.setVariable("block_z", blockEvent.getBlock().getZ());
                context.setVariable("block_world", blockEvent.getBlock().getWorld().getName());
                
                // Специфичные переменные для разных событий
                if (event instanceof BlockBreakEvent) {
                    BlockBreakEvent breakEvent = (BlockBreakEvent) event;
                    context.setVariable("drops", breakEvent.getBlock().getDrops().size());
                    context.setVariable("exp_drop", breakEvent.getExpToDrop());
                } else if (event instanceof BlockPlaceEvent) {
                    BlockPlaceEvent placeEvent = (BlockPlaceEvent) event;
                    context.setVariable("placed_against", placeEvent.getBlockAgainst().getType().name());
                    context.setVariable("can_build", placeEvent.canBuild());
                } else if (event instanceof BlockRedstoneEvent) {
                    BlockRedstoneEvent redstoneEvent = (BlockRedstoneEvent) event;
                    context.setVariable("old_current", redstoneEvent.getOldCurrent());
                    context.setVariable("new_current", redstoneEvent.getNewCurrent());
                }
            }
            
            // Обработка погодных событий
            if (event instanceof WeatherEvent) {
                WeatherEvent weatherEvent = (WeatherEvent) event;
                context.setVariable("weather_world", weatherEvent.getWorld().getName());
                
                if (event instanceof WeatherChangeEvent) {
                    WeatherChangeEvent changeEvent = (WeatherChangeEvent) event;
                    context.setVariable("to_weather_state", changeEvent.toWeatherState());
                } else if (event instanceof ThunderChangeEvent) {
                    ThunderChangeEvent thunderEvent = (ThunderChangeEvent) event;
                    context.setVariable("to_thunder_state", thunderEvent.toThunderState());
                } else if (event instanceof LightningStrikeEvent) {
                    LightningStrikeEvent lightningEvent = (LightningStrikeEvent) event;
                    context.setVariable("lightning_x", lightningEvent.getLightning().getLocation().getX());
                    context.setVariable("lightning_y", lightningEvent.getLightning().getLocation().getY());
                    context.setVariable("lightning_z", lightningEvent.getLightning().getLocation().getZ());
                    context.setVariable("lightning_effect", lightningEvent.getLightning().isEffect());
                }
            }
            
            // Обработка мировых событий
            if (event instanceof WorldEvent) {
                WorldEvent worldEvent = (WorldEvent) event;
                context.setVariable("event_world", worldEvent.getWorld().getName());
                context.setVariable("world_time", worldEvent.getWorld().getTime());
                context.setVariable("world_weather", worldEvent.getWorld().hasStorm());
                context.setVariable("world_thunder", worldEvent.getWorld().isThundering());
            }
            
            // Обработка событий чанков
            if (event instanceof ChunkEvent) {
                ChunkEvent chunkEvent = (ChunkEvent) event;
                context.setVariable("chunk_x", chunkEvent.getChunk().getX());
                context.setVariable("chunk_z", chunkEvent.getChunk().getZ());
                context.setVariable("chunk_world", chunkEvent.getWorld().getName());
            }
            
            // Обработка событий табличек
            if (event instanceof SignChangeEvent) {
                SignChangeEvent signEvent = (SignChangeEvent) event;
                context.setVariable("sign_line_0", signEvent.getLine(0));
                context.setVariable("sign_line_1", signEvent.getLine(1));
                context.setVariable("sign_line_2", signEvent.getLine(2));
                context.setVariable("sign_line_3", signEvent.getLine(3));
            }
            
            // Обработка событий печи удалена из-за отсутствия в API Paper 1.21.4
            
            // Обработка событий структур
            if (event instanceof StructureGrowEvent) {
                StructureGrowEvent growEvent = (StructureGrowEvent) event;
                context.setVariable("structure_type", growEvent.getSpecies().name());
                context.setVariable("blocks_count", growEvent.getBlocks().size());
                context.setVariable("bonemeal_used", growEvent.isFromBonemeal());
            }
        }
        
        return context;
    }
}
