package ru.openhousing.coding.blocks;

import ru.openhousing.coding.blocks.actions.*;
import ru.openhousing.coding.blocks.conditions.*;
import ru.openhousing.coding.blocks.control.*;
import ru.openhousing.coding.blocks.events.*;
import ru.openhousing.coding.blocks.functions.*;
import ru.openhousing.coding.blocks.inventory.*;
import ru.openhousing.coding.blocks.math.*;
import ru.openhousing.coding.blocks.text.*;
import ru.openhousing.coding.blocks.variables.*;

/**
 * Фабрика для создания блоков кода
 * Централизует логику создания блоков и устраняет дублирование кода
 */
public class CodeBlockFactory {
    
    /**
     * Создает экземпляр блока по его типу
     * 
     * @param blockType тип блока
     * @return экземпляр блока или null, если тип не поддерживается
     */
    public static CodeBlock createBlock(BlockType blockType) {
        if (blockType == null) {
            return null;
        }
        
        switch (blockType) {
            // События игрока
            case PLAYER_JOIN:
            case PLAYER_QUIT:
            case PLAYER_CHAT:
            case PLAYER_COMMAND:
            case PLAYER_MOVE:
            case PLAYER_TELEPORT:
            case PLAYER_DEATH:
            case PLAYER_RESPAWN:
            case PLAYER_DAMAGE:
            case PLAYER_HEAL:
            case PLAYER_FOOD_CHANGE:
            case PLAYER_EXP_CHANGE:
            case PLAYER_LEVEL_UP:
            case PLAYER_INVENTORY_CLICK:
            case PLAYER_ITEM_DROP:
            case PLAYER_ITEM_PICKUP:
            case PLAYER_ITEM_CONSUME:
            case PLAYER_ITEM_BREAK:
            case PLAYER_BLOCK_BREAK:
            case PLAYER_BLOCK_PLACE:
            case PLAYER_INTERACT:
            case PLAYER_INTERACT_ENTITY:
            case PLAYER_FISH:
            case PLAYER_ENCHANT:
            case PLAYER_CRAFT:
            case PLAYER_SMELT:
            case PLAYER_TRADE:
            case PLAYER_SNEAK:
                return new PlayerEventBlock();
                
            // События мира
            case WORLD_WEATHER_CHANGE:
            case WORLD_TIME_CHANGE:
            case WORLD_CHUNK_LOAD:
            case WORLD_CHUNK_UNLOAD:
            case WORLD_STRUCTURE_GROW:
            case WORLD_EXPLOSION:
            case WORLD_PORTAL_CREATE:
                return new WorldEventBlock();
                
            // События существ
            case ENTITY_SPAWN:
            case ENTITY_DEATH:
            case ENTITY_DAMAGE:
            case ENTITY_TARGET:
            case ENTITY_TAME:
            case ENTITY_BREED:
            case ENTITY_EXPLODE:
            case ENTITY_INTERACT:
            case ENTITY_MOUNT:
            case ENTITY_DISMOUNT:
            case ENTITY_LEASH:
            case ENTITY_UNLEASH:
            case ENTITY_SHEAR:
            case ENTITY_MILK:
            case ENTITY_TRANSFORM:
                return new EntityEventBlock();
            
            // Условия игрока
            case IF_PLAYER_ONLINE:
            case IF_PLAYER_PERMISSION:
            case IF_PLAYER_GAMEMODE:
            case IF_PLAYER_WORLD:
            case IF_PLAYER_FLYING:
            case IF_PLAYER_SNEAKING:
            case IF_PLAYER_BLOCKING:
            case IF_PLAYER_ITEM:
            case IF_PLAYER_HEALTH:
            case IF_PLAYER_FOOD:
                return new IfPlayerBlock();
                
            // Условия существ
            case IF_ENTITY_EXISTS:
            case IF_ENTITY_TYPE:
            case IF_ENTITY_HEALTH:
                return new IfEntityBlock();
                
            // Условия переменных
            case IF_VARIABLE_EQUALS:
            case IF_VARIABLE_GREATER:
            case IF_VARIABLE_LESS:
            case IF_VARIABLE_CONTAINS:
            case IF_VARIABLE_EXISTS:
            case IF_VARIABLE_SAVED:
            case IF_VARIABLE_TYPE:
                return new IfVariableBlock();
                
            // Условия игры
            case IF_GAME_TIME:
            case IF_GAME_WEATHER:
            case IF_GAME_DIFFICULTY:
            case IF_GAME_PLAYERS_ONLINE:
            case IF_GAME_TPS:
                return new IfPlayerBlock(); // Временно используем IfPlayerBlock
                
            // Действия игрока
            case PLAYER_SEND_MESSAGE:
            case PLAYER_SEND_TITLE:
            case PLAYER_SEND_ACTIONBAR:
            case PLAYER_TELEPORT_ACTION:
            case PLAYER_GIVE_ITEM:
            case PLAYER_REMOVE_ITEM:
            case PLAYER_CLEAR_INVENTORY:
            case PLAYER_SET_HEALTH:
            case PLAYER_SET_FOOD:
            case PLAYER_SET_EXP:
            case PLAYER_GIVE_EFFECT:
            case PLAYER_REMOVE_EFFECT:
            case PLAYER_PLAY_SOUND:
            case PLAYER_STOP_SOUND:
            case PLAYER_SPAWN_PARTICLE:
            case PLAYER_SET_GAMEMODE:
            case PLAYER_KICK:
            case PLAYER_BAN:
            case PLAYER_WHITELIST_ADD:
            case PLAYER_WHITELIST_REMOVE:
            case PLAYER_SET_DISPLAY_NAME:
            case PLAYER_RESET_DISPLAY_NAME:
            case PLAYER_SEND_PLUGIN_MESSAGE:
                return new PlayerActionBlock();
                
            // Действия переменных
            case VAR_SET:
            case VAR_ADD:
            case VAR_SUBTRACT:
            case VAR_MULTIPLY:
            case VAR_DIVIDE:
            case VAR_APPEND_TEXT:
            case VAR_REPLACE_TEXT:
            case VAR_UPPERCASE:
            case VAR_LOWERCASE:
            case VAR_REVERSE:
            case VAR_LENGTH:
            case VAR_SUBSTRING:
            case VAR_RANDOM_NUMBER:
            case VAR_ROUND:
            case VAR_ABS:
            case VAR_MIN:
            case VAR_MAX:
            case VAR_SAVE:
            case VAR_DELETE:
            case VAR_COPY:
                return new VariableActionBlock();
                
            // Действия игры
            case GAME_SET_TIME:
            case GAME_SET_WEATHER:
            case GAME_SET_DIFFICULTY:
            case GAME_BROADCAST:
            case GAME_EXECUTE_COMMAND:
            case GAME_STOP_SERVER:
            case GAME_RESTART_SERVER:
            case GAME_SAVE_WORLD:
            case GAME_LOAD_WORLD:
            case GAME_CREATE_EXPLOSION:
            case GAME_SPAWN_ENTITY:
            case GAME_REMOVE_ENTITY:
            case GAME_SET_BLOCK:
            case GAME_BREAK_BLOCK:
            case GAME_SEND_PACKET:
                return new WorldActionBlock();
                
            // Функции
            case FUNCTION:
                return new FunctionBlock();
            case CALL_FUNCTION:
                return new CallFunctionBlock();
            
            // Управление
            case REPEAT:
                return new AsyncRepeatBlock(); // Используем асинхронную версию для предотвращения server lag
            case ELSE:
                return new ElseBlock();
            case TARGET:
                return new TargetBlock();
            
            // Математика и утилиты
            case MATH:
                return new MathBlock();
            case TEXT_OPERATION:
                return new TextOperationBlock();
            case INVENTORY_ACTION:
                return new InventoryActionBlock();
            case ITEM_CHECK:
                return new ItemCheckBlock();
            
            default:
                return null;
        }
    }
    
    /**
     * Проверяет, поддерживается ли тип блока
     * 
     * @param blockType тип блока
     * @return true, если тип поддерживается
     */
    public static boolean isSupported(BlockType blockType) {
        return createBlock(blockType) != null;
    }
    
    /**
     * Получает список поддерживаемых типов блоков для категории
     * 
     * @param category категория блоков
     * @return массив поддерживаемых типов
     */
    public static BlockType[] getSupportedTypes(BlockType.BlockCategory category) {
        if (category == null) {
            return new BlockType[0];
        }
        
        return java.util.Arrays.stream(BlockType.values())
            .filter(type -> type.getCategory() == category && isSupported(type))
            .toArray(BlockType[]::new);
    }
    
    /**
     * Получает список поддерживаемых типов блоков для категории с фильтром
     * 
     * @param category категория блоков
     * @param filter префикс для фильтрации (например, "IF_PLAYER")
     * @return массив поддерживаемых типов
     */
    public static BlockType[] getSupportedTypes(BlockType.BlockCategory category, String filter) {
        if (category == null) {
            return new BlockType[0];
        }
        
        return java.util.Arrays.stream(BlockType.values())
            .filter(type -> type.getCategory() == category && 
                          type.name().startsWith(filter) && 
                          isSupported(type))
            .toArray(BlockType[]::new);
    }
}
