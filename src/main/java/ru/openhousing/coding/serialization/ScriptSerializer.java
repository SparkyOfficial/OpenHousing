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
            
            return jsonObject;
        }
        
        @Override
        public CodeBlock deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            
            // Получаем тип блока
            String typeName = jsonObject.get("type").getAsString();
            BlockType blockType = BlockType.valueOf(typeName);
            
            // Создаем блок через фабрику
            CodeBlock block = ru.openhousing.coding.blocks.CodeBlockFactory.createBlock(blockType);
            if (block == null) {
                throw new JsonParseException("Unknown block type: " + typeName);
            }
            
            // ID генерируется автоматически при создании блока
            
            // Устанавливаем параметры
            if (jsonObject.has("parameters")) {
                JsonObject parameters = jsonObject.getAsJsonObject("parameters");
                for (Map.Entry<String, JsonElement> entry : parameters.entrySet()) {
                    String key = entry.getKey();
                    JsonElement value = entry.getValue();
                    
                    // Десериализуем enum значения
                    Object deserializedValue = deserializeEnumValue(key, value, blockType, context);
                    if (deserializedValue != null) {
                        block.setParameter(key, deserializedValue);
                    } else {
                        // Обычная десериализация
                        block.setParameter(key, context.deserialize(value, Object.class));
                    }
                }
            }
            
            // Устанавливаем дочерние блоки
            if (jsonObject.has("children")) {
                JsonArray children = jsonObject.getAsJsonArray("children");
                for (JsonElement childElement : children) {
                    CodeBlock child = context.deserialize(childElement, CodeBlock.class);
                    if (child != null) {
                        block.addChild(child);
                    }
                }
            }
            
            return block;
        }
        
        /**
         * Десериализация enum значений на основе ключа и типа блока
         */
        private Object deserializeEnumValue(String key, JsonElement value, BlockType blockType, JsonDeserializationContext context) {
            try {
                String stringValue = value.getAsString();
                
                // Обработка действий игрока
                if (blockType.name().startsWith("PLAYER_") && blockType.getCategory() == BlockType.BlockCategory.ACTION) {
                    if (key.equals("actionType")) {
                        return ru.openhousing.coding.blocks.actions.PlayerActionBlock.PlayerActionType.valueOf(stringValue);
                    }
                }
                
                // Обработка действий мира
                if (blockType.name().startsWith("GAME_") && blockType.getCategory() == BlockType.BlockCategory.ACTION) {
                    if (key.equals("actionType")) {
                        return ru.openhousing.coding.blocks.actions.WorldActionBlock.WorldActionType.valueOf(stringValue);
                    }
                }
                
                // Обработка событий игрока
                if (blockType.name().startsWith("PLAYER_") && blockType.getCategory() == BlockType.BlockCategory.EVENT) {
                    if (key.equals("eventType")) {
                        return ru.openhousing.coding.blocks.events.PlayerEventBlock.PlayerEventType.valueOf(stringValue);
                    }
                }
                
                // Обработка событий существ
                if (blockType.name().startsWith("ENTITY_") && blockType.getCategory() == BlockType.BlockCategory.EVENT) {
                    if (key.equals("eventType")) {
                        return ru.openhousing.coding.blocks.events.EntityEventBlock.EntityEventType.valueOf(stringValue);
                    }
                }
                
                // Обработка событий мира
                if (blockType.name().startsWith("WORLD_") && blockType.getCategory() == BlockType.BlockCategory.EVENT) {
                    if (key.equals("eventType")) {
                        return ru.openhousing.coding.blocks.events.WorldEventBlock.WorldEventType.valueOf(stringValue);
                    }
                }
                
                // Обработка условий игрока
                if (blockType.name().startsWith("IF_PLAYER")) {
                    if (key.equals("conditionType")) {
                        return ru.openhousing.coding.blocks.conditions.IfPlayerBlock.PlayerConditionType.valueOf(stringValue);
                    }
                }
                
                // Обработка условий существ
                if (blockType.name().startsWith("IF_ENTITY")) {
                    if (key.equals("conditionType")) {
                        return ru.openhousing.coding.blocks.conditions.IfEntityBlock.EntityConditionType.valueOf(stringValue);
                    }
                }
                
                // Обработка условий переменных
                if (blockType.name().startsWith("IF_VARIABLE")) {
                    if (key.equals("conditionType")) {
                        return ru.openhousing.coding.blocks.conditions.IfVariableBlock.VariableConditionType.valueOf(stringValue);
                    }
                }
                
                // Обработка действий переменных
                if (blockType.name().startsWith("VAR_")) {
                    if (key.equals("actionType")) {
                        return ru.openhousing.coding.blocks.variables.VariableActionBlock.VariableActionType.valueOf(stringValue);
                    }
                }
                
                // Обработка математических операций
                if (blockType == BlockType.MATH) {
                    if (key.equals("operation")) {
                        return ru.openhousing.coding.blocks.math.MathBlock.MathOperation.valueOf(stringValue);
                    }
                }
                
                // Обработка текстовых операций
                if (blockType == BlockType.TEXT_OPERATION) {
                    if (key.equals("operation")) {
                        return ru.openhousing.coding.blocks.text.TextOperationBlock.TextOperation.valueOf(stringValue);
                    }
                }
                
                // Обработка повторений (AsyncRepeatBlock)
                if (blockType == BlockType.REPEAT) {
                    if (key.equals("repeatType")) {
                        return ru.openhousing.coding.blocks.control.AsyncRepeatBlock.RepeatType.valueOf(stringValue);
                    }
                }
                
                // Обработка целей
                if (blockType == BlockType.TARGET) {
                    if (key.equals("targetType")) {
                        return ru.openhousing.coding.blocks.control.TargetBlock.TargetType.valueOf(stringValue);
                    }
                }
                
                // Обработка функций
                if (blockType == BlockType.FUNCTION) {
                    if (key.equals("functionType")) {
                        // TODO: Добавить FunctionType когда будет создан
                        return stringValue; // Пока возвращаем как строку
                    }
                }
                
                // Обработка вызовов функций
                if (blockType == BlockType.CALL_FUNCTION) {
                    if (key.equals("callType")) {
                        // TODO: Добавить CallType когда будет создан
                        return stringValue; // Пока возвращаем как строку
                    }
                }
                
                // Обработка контроля кода
                if (blockType == BlockType.CODE_CONTROL) {
                    if (key.equals("controlType")) {
                        // TODO: Добавить ControlType когда будет создан
                        return stringValue; // Пока возвращаем как строку
                    }
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
            
            // Строки кода
            if (!src.getLines().isEmpty()) {
                JsonArray lines = new JsonArray();
                for (ru.openhousing.coding.script.CodeLine line : src.getLines()) {
                    JsonObject lineObject = new JsonObject();
                    lineObject.addProperty("name", line.getName());
                    lineObject.addProperty("description", line.getDescription());
                    lineObject.addProperty("enabled", line.isEnabled());
                    lineObject.addProperty("lineNumber", line.getLineNumber());
                    
                    // Блоки в строке
                    if (!line.getBlocks().isEmpty()) {
                        JsonArray blocks = new JsonArray();
                        for (CodeBlock block : line.getBlocks()) {
                            blocks.add(context.serialize(block, CodeBlock.class));
                        }
                        lineObject.add("blocks", blocks);
                    }
                    
                    lines.add(lineObject);
                }
                jsonObject.add("lines", lines);
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
            
            // Строки кода
            if (jsonObject.has("lines")) {
                JsonArray lines = jsonObject.getAsJsonArray("lines");
                for (JsonElement lineElement : lines) {
                    JsonObject lineObject = lineElement.getAsJsonObject();
                    
                    String lineName = lineObject.get("name").getAsString();
                    String description = lineObject.has("description") ? lineObject.get("description").getAsString() : "";
                    boolean enabled = lineObject.has("enabled") ? lineObject.get("enabled").getAsBoolean() : true;
                    int lineNumber = lineObject.has("lineNumber") ? lineObject.get("lineNumber").getAsInt() : 0;
                    
                    ru.openhousing.coding.script.CodeLine line = script.createLine(lineName);
                    line.setDescription(description);
                    line.setEnabled(enabled);
                    
                    // Блоки в строке
                    if (lineObject.has("blocks")) {
                        JsonArray blocks = lineObject.getAsJsonArray("blocks");
                        for (JsonElement blockElement : blocks) {
                            CodeBlock block = context.deserialize(blockElement, CodeBlock.class);
                            if (block != null) {
                                line.addBlock(block);
                            }
                        }
                    }
                }
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
            
            return script;
        }
    }
}
