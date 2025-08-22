package ru.openhousing.coding.serialization;

import com.google.gson.*;
import ru.openhousing.coding.blocks.*;
import ru.openhousing.coding.blocks.actions.*;
import ru.openhousing.coding.blocks.conditions.*;
import ru.openhousing.coding.blocks.control.*;
import ru.openhousing.coding.blocks.events.*;
import ru.openhousing.coding.blocks.functions.*;
import ru.openhousing.coding.blocks.variables.*;
import ru.openhousing.coding.script.CodeScript;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

/**
 * Сериализатор кода в JSON
 */
public class ScriptSerializer {
    
    private final Gson gson;
    
    public ScriptSerializer() {
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(CodeBlock.class, new CodeBlockAdapter())
            .registerTypeAdapter(CodeScript.class, new CodeScriptAdapter())
            .create();
    }
    
    /**
     * Сериализация кода в JSON
     */
    public String serialize(CodeScript script) {
        try {
            return gson.toJson(script);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }
    
    /**
     * Десериализация кода из JSON
     */
    public CodeScript deserialize(String json, UUID playerId, String playerName) {
        try {
            if (json == null || json.trim().isEmpty() || json.equals("{}")) {
                return new CodeScript(playerId, playerName);
            }
            
            CodeScript script = gson.fromJson(json, CodeScript.class);
            if (script == null) {
                return new CodeScript(playerId, playerName);
            }
            
            // Обновляем информацию об игроке
            script.setPlayerName(playerName);
            
            return script;
        } catch (Exception e) {
            e.printStackTrace();
            return new CodeScript(playerId, playerName);
        }
    }
    
    /**
     * Адаптер для сериализации CodeBlock
     */
    private static class CodeBlockAdapter implements JsonSerializer<CodeBlock>, JsonDeserializer<CodeBlock> {
        
        @Override
        public JsonElement serialize(CodeBlock src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            
            // Основная информация
            jsonObject.addProperty("id", src.getId().toString());
            jsonObject.addProperty("type", src.getType().name());
            
            // Параметры
            JsonObject parameters = new JsonObject();
            for (Map.Entry<String, Object> entry : src.getParameters().entrySet()) {
                if (entry.getValue() != null) {
                    parameters.add(entry.getKey(), context.serialize(entry.getValue()));
                }
            }
            jsonObject.add("parameters", parameters);
            
            // Дочерние блоки
            if (!src.getChildBlocks().isEmpty()) {
                JsonArray children = new JsonArray();
                for (CodeBlock child : src.getChildBlocks()) {
                    children.add(context.serialize(child, CodeBlock.class));
                }
                jsonObject.add("children", children);
            }
            
            // Локация (если есть)
            if (src.getLocation() != null) {
                JsonObject location = new JsonObject();
                location.addProperty("world", src.getLocation().getWorld().getName());
                location.addProperty("x", src.getLocation().getX());
                location.addProperty("y", src.getLocation().getY());
                location.addProperty("z", src.getLocation().getZ());
                jsonObject.add("location", location);
            }
            
            return jsonObject;
        }
        
        @Override
        public CodeBlock deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            
            // Получаем тип блока
            String typeString = jsonObject.get("type").getAsString();
            BlockType blockType;
            
            try {
                blockType = BlockType.valueOf(typeString);
            } catch (IllegalArgumentException e) {
                throw new JsonParseException("Unknown block type: " + typeString);
            }
            
            // Создаем блок
            CodeBlock block = createBlockInstance(blockType);
            if (block == null) {
                throw new JsonParseException("Cannot create block of type: " + typeString);
            }
            
            // Загружаем параметры
            if (jsonObject.has("parameters")) {
                JsonObject parameters = jsonObject.getAsJsonObject("parameters");
                for (Map.Entry<String, JsonElement> entry : parameters.entrySet()) {
                    String key = entry.getKey();
                    JsonElement value = entry.getValue();
                    
                    if (value.isJsonPrimitive()) {
                        JsonPrimitive primitive = value.getAsJsonPrimitive();
                        if (primitive.isString()) {
                            block.setParameter(key, primitive.getAsString());
                        } else if (primitive.isNumber()) {
                            if (primitive.getAsString().contains(".")) {
                                block.setParameter(key, primitive.getAsDouble());
                            } else {
                                block.setParameter(key, primitive.getAsInt());
                            }
                        } else if (primitive.isBoolean()) {
                            block.setParameter(key, primitive.getAsBoolean());
                        }
                    } else {
                        // Для сложных объектов (например, enum)
                        try {
                            // Специальная обработка для enum значений
                            if (key.equals("actionType") || key.equals("eventType") || key.equals("conditionType") || 
                                key.equals("operation") || key.equals("targetType") || key.equals("repeatType")) {
                                // Определяем правильный enum тип на основе ключа и типа блока
                                Object enumValue = deserializeEnumValue(key, value, blockType, context);
                                if (enumValue != null) {
                                    block.setParameter(key, enumValue);
                                } else {
                                    // Если не удалось десериализовать, сохраняем как строку
                                    block.setParameter(key, value.getAsString());
                                }
                            } else {
                                Object deserializedValue = context.deserialize(value, Object.class);
                                block.setParameter(key, deserializedValue);
                            }
                        } catch (Exception e) {
                            // Игнорируем ошибки десериализации параметров
                        }
                    }
                }
            }
            
            // Загружаем дочерние блоки
            if (jsonObject.has("children")) {
                JsonArray children = jsonObject.getAsJsonArray("children");
                for (JsonElement childElement : children) {
                    try {
                        CodeBlock child = context.deserialize(childElement, CodeBlock.class);
                        if (child != null) {
                            block.addChild(child);
                        }
                    } catch (Exception e) {
                        // Игнорируем ошибки десериализации дочерних блоков
                    }
                }
            }
            
            // Загружаем локацию
            if (jsonObject.has("location")) {
                try {
                    JsonObject location = jsonObject.getAsJsonObject("location");
                    String worldName = location.get("world").getAsString();
                    double x = location.get("x").getAsDouble();
                    double y = location.get("y").getAsDouble();
                    double z = location.get("z").getAsDouble();
                    
                    org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
                    if (world != null) {
                        block.setLocation(new org.bukkit.Location(world, x, y, z));
                    }
                } catch (Exception e) {
                    // Игнорируем ошибки загрузки локации
                }
            }
            
            return block;
        }
        
        /**
         * Создание экземпляра блока по типу
         */
        private CodeBlock createBlockInstance(BlockType blockType) {
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
                
                // Действия мира
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
                    return new RepeatBlock();
                case ELSE:
                    return new ElseBlock();
                case TARGET:
                    return new ru.openhousing.coding.blocks.control.TargetBlock();
            
                // Математика и утилиты
                case MATH:
                    return new ru.openhousing.coding.blocks.math.MathBlock();
                case TEXT_OPERATION:
                    return new ru.openhousing.coding.blocks.text.TextOperationBlock();
                case INVENTORY_ACTION:
                    return new ru.openhousing.coding.blocks.inventory.InventoryActionBlock();
                case ITEM_CHECK:
                    return new ru.openhousing.coding.blocks.inventory.ItemCheckBlock();
            
                default:
                    return null;
            }
        }
        
        /**
         * Десериализация enum значений на основе ключа и типа блока
         */
        private Object deserializeEnumValue(String key, JsonElement value, BlockType blockType, JsonDeserializationContext context) {
            try {
                String stringValue = value.getAsString();
                
                switch (blockType) {
                    // Обработка действий игрока
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
                        if (key.equals("actionType")) {
                            return ru.openhousing.coding.blocks.actions.PlayerActionBlock.PlayerActionType.valueOf(stringValue);
                        }
                        break;
                        
                    // Обработка действий мира
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
                        if (key.equals("actionType")) {
                            return ru.openhousing.coding.blocks.actions.WorldActionBlock.WorldActionType.valueOf(stringValue);
                        }
                        break;
                        
                    // Обработка событий игрока
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
                        if (key.equals("eventType")) {
                            return ru.openhousing.coding.blocks.events.PlayerEventBlock.PlayerEventType.valueOf(stringValue);
                        }
                        break;
                        
                    // Обработка событий существ
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
                        if (key.equals("eventType")) {
                            return ru.openhousing.coding.blocks.events.EntityEventBlock.EntityEventType.valueOf(stringValue);
                        }
                        break;
                        
                    // Обработка событий мира
                    case WORLD_WEATHER_CHANGE:
                    case WORLD_TIME_CHANGE:
                    case WORLD_CHUNK_LOAD:
                    case WORLD_CHUNK_UNLOAD:
                    case WORLD_STRUCTURE_GROW:
                    case WORLD_EXPLOSION:
                    case WORLD_PORTAL_CREATE:
                        if (key.equals("eventType")) {
                            return ru.openhousing.coding.blocks.events.WorldEventBlock.WorldEventType.valueOf(stringValue);
                        }
                        break;
                        
                    // Обработка условий игрока
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
                        if (key.equals("conditionType")) {
                            return ru.openhousing.coding.blocks.conditions.IfPlayerBlock.PlayerConditionType.valueOf(stringValue);
                        }
                        break;
                        
                    // Обработка условий существ
                    case IF_ENTITY_EXISTS:
                    case IF_ENTITY_TYPE:
                    case IF_ENTITY_HEALTH:
                        if (key.equals("conditionType")) {
                            return ru.openhousing.coding.blocks.conditions.IfEntityBlock.EntityConditionType.valueOf(stringValue);
                        }
                        break;
                        
                    // Обработка условий переменных
                    case IF_VARIABLE_EQUALS:
                    case IF_VARIABLE_GREATER:
                    case IF_VARIABLE_LESS:
                    case IF_VARIABLE_CONTAINS:
                    case IF_VARIABLE_EXISTS:
                    case IF_VARIABLE_SAVED:
                    case IF_VARIABLE_TYPE:
                        if (key.equals("conditionType")) {
                            return ru.openhousing.coding.blocks.conditions.IfVariableBlock.VariableConditionType.valueOf(stringValue);
                        }
                        break;
                    case MATH:
                        if (key.equals("operation")) {
                            return ru.openhousing.coding.blocks.math.MathBlock.MathOperation.valueOf(stringValue);
                        }
                        break;
                    case TEXT_OPERATION:
                        if (key.equals("operation")) {
                            return ru.openhousing.coding.blocks.text.TextOperationBlock.TextOperation.valueOf(stringValue);
                        }
                        break;
                    case REPEAT:
                        if (key.equals("repeatType")) {
                            return ru.openhousing.coding.blocks.control.RepeatBlock.RepeatType.valueOf(stringValue);
                        }
                        break;
                    case TARGET:
                        if (key.equals("targetType")) {
                            return ru.openhousing.coding.blocks.control.TargetBlock.TargetType.valueOf(stringValue);
                        }
                        break;
                    // Обработка действий переменных
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
                        if (key.equals("actionType")) {
                            return ru.openhousing.coding.blocks.variables.VariableActionBlock.VariableActionType.valueOf(stringValue);
                        }
                        break;
                }
            } catch (IllegalArgumentException e) {
                // Если enum значение не найдено, возвращаем null
                return null;
            }
            
            return null;
        }
    }
    
    /**
     * Адаптер для сериализации CodeScript
     */
    private static class CodeScriptAdapter implements JsonSerializer<CodeScript>, JsonDeserializer<CodeScript> {
        
        @Override
        public JsonElement serialize(CodeScript src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            
            // Основная информация
            jsonObject.addProperty("playerId", src.getPlayerId().toString());
            jsonObject.addProperty("playerName", src.getPlayerName());
            jsonObject.addProperty("enabled", src.isEnabled());
            jsonObject.addProperty("lastModified", src.getLastModified());
            if (src.getBoundWorld() != null) {
                jsonObject.addProperty("boundWorld", src.getBoundWorld());
            }
            
            // Блоки
            if (!src.getBlocks().isEmpty()) {
                JsonArray blocks = new JsonArray();
                for (CodeBlock block : src.getBlocks()) {
                    blocks.add(context.serialize(block, CodeBlock.class));
                }
                jsonObject.add("blocks", blocks);
            }
            
            // Глобальные переменные
            if (!src.getGlobalVariables().isEmpty()) {
                JsonObject variables = new JsonObject();
                for (Map.Entry<String, Object> entry : src.getGlobalVariables().entrySet()) {
                    if (entry.getValue() != null) {
                        variables.add(entry.getKey(), context.serialize(entry.getValue()));
                    }
                }
                jsonObject.add("globalVariables", variables);
            }
            
            // Функции (сохраняем только их имена, так как они уже есть в блоках)
            if (!src.getFunctions().isEmpty()) {
                JsonArray functions = new JsonArray();
                for (String functionName : src.getFunctions().keySet()) {
                    functions.add(functionName);
                }
                jsonObject.add("functionNames", functions);
            }
            
            return jsonObject;
        }
        
        @Override
        public CodeScript deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            
            // Основная информация
            UUID playerId = UUID.fromString(jsonObject.get("playerId").getAsString());
            String playerName = jsonObject.get("playerName").getAsString();
            
            CodeScript script = new CodeScript(playerId, playerName);
            
            // Статус
            if (jsonObject.has("enabled")) {
                script.setEnabled(jsonObject.get("enabled").getAsBoolean());
            }
            if (jsonObject.has("boundWorld")) {
                script.setBoundWorld(jsonObject.get("boundWorld").getAsString());
            }
            
            // Глобальные переменные
            if (jsonObject.has("globalVariables")) {
                JsonObject variables = jsonObject.getAsJsonObject("globalVariables");
                for (Map.Entry<String, JsonElement> entry : variables.entrySet()) {
                    String key = entry.getKey();
                    JsonElement value = entry.getValue();
                    
                    if (value.isJsonPrimitive()) {
                        JsonPrimitive primitive = value.getAsJsonPrimitive();
                        if (primitive.isString()) {
                            script.setGlobalVariable(key, primitive.getAsString());
                        } else if (primitive.isNumber()) {
                            if (primitive.getAsString().contains(".")) {
                                script.setGlobalVariable(key, primitive.getAsDouble());
                            } else {
                                script.setGlobalVariable(key, primitive.getAsInt());
                            }
                        } else if (primitive.isBoolean()) {
                            script.setGlobalVariable(key, primitive.getAsBoolean());
                        }
                    }
                }
            }
            
            // Блоки
            if (jsonObject.has("blocks")) {
                JsonArray blocks = jsonObject.getAsJsonArray("blocks");
                for (JsonElement blockElement : blocks) {
                    try {
                        CodeBlock block = context.deserialize(blockElement, CodeBlock.class);
                        if (block != null) {
                            script.addBlock(block);
                        }
                    } catch (Exception e) {
                        // Игнорируем ошибки десериализации блоков
                    }
                }
            }
            
            return script;
        }
    }
}
