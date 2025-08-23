package ru.openhousing.coding.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.control.AsyncRepeatBlock;
import ru.openhousing.coding.blocks.control.TargetBlock;
import ru.openhousing.coding.blocks.math.MathBlock;
import ru.openhousing.coding.blocks.math.MathBlock.MathOperation;
import ru.openhousing.coding.blocks.control.AsyncRepeatBlock.RepeatType;
import ru.openhousing.coding.blocks.control.TargetBlock.TargetType;
import ru.openhousing.coding.script.CodeScript;
import ru.openhousing.coding.serialization.ScriptSerializer;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для ScriptSerializer
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ScriptSerializerTest {
    
    private ScriptSerializer serializer;
    private UUID testPlayerId;
    private String testPlayerName;
    
    @Mock
    private org.bukkit.entity.Player mockPlayer;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        serializer = new ScriptSerializer();
        testPlayerId = UUID.randomUUID();
        testPlayerName = "TestPlayer";
    }
    
    @Test
    void testAsyncRepeatBlockSerialization() {
        // Создаем AsyncRepeatBlock с параметрами
        AsyncRepeatBlock repeatBlock = new AsyncRepeatBlock();
        repeatBlock.setParameter("repeatType", RepeatType.TIMES);
        repeatBlock.setParameter("value", "10");
        repeatBlock.setParameter("max_iterations", "1000");
        repeatBlock.setParameter("delay_ticks", "5");
        
        // Создаем скрипт и добавляем блок
        CodeScript script = new CodeScript(testPlayerId, testPlayerName);
        script.createLine("Test Line").addBlock(repeatBlock);
        
        // Сериализуем
        String json = serializer.serialize(script);
        assertNotNull(json);
        assertTrue(json.contains("REPEAT"));
        assertTrue(json.contains("TIMES"));
        assertTrue(json.contains("10"));
        assertTrue(json.contains("1000"));
        assertTrue(json.contains("5"));
        
        // Десериализуем
        CodeScript deserializedScript = serializer.deserialize(json, testPlayerId, testPlayerName);
        assertNotNull(deserializedScript);
        assertEquals(1, deserializedScript.getLines().size());
        
        CodeBlock deserializedBlock = deserializedScript.getLines().get(0).getBlocks().get(0);
        assertNotNull(deserializedBlock);
        assertEquals(BlockType.REPEAT, deserializedBlock.getType());
        assertEquals("10", deserializedBlock.getParameter("value"));
        assertEquals("1000", deserializedBlock.getParameter("max_iterations"));
        assertEquals("5", deserializedBlock.getParameter("delay_ticks"));
    }
    
    @Test
    void testTargetBlockSerialization() {
        // Создаем TargetBlock с параметрами
        TargetBlock targetBlock = new TargetBlock();
        targetBlock.setParameter("targetType", TargetType.NEAREST_PLAYER);
        targetBlock.setParameter("radius", "15.0");
        
        // Создаем скрипт и добавляем блок
        CodeScript script = new CodeScript(testPlayerId, testPlayerName);
        script.createLine("Target Line").addBlock(targetBlock);
        
        // Сериализуем
        String json = serializer.serialize(script);
        assertNotNull(json);
        assertTrue(json.contains("TARGET"));
        assertTrue(json.contains("NEAREST_PLAYER"));
        assertTrue(json.contains("15.0"));
        
        // Десериализуем
        CodeScript deserializedScript = serializer.deserialize(json, testPlayerId, testPlayerName);
        assertNotNull(deserializedScript);
        assertEquals(1, deserializedScript.getLines().size());
        
        CodeBlock deserializedBlock = deserializedScript.getLines().get(0).getBlocks().get(0);
        assertNotNull(deserializedBlock);
        assertEquals(BlockType.TARGET, deserializedBlock.getType());
        assertEquals("15.0", deserializedBlock.getParameter("radius"));
    }
    
    @Test
    void testMathBlockSerialization() {
        // Создаем MathBlock с параметрами
        MathBlock mathBlock = new MathBlock();
        mathBlock.setParameter("operation", MathOperation.ADD);
        mathBlock.setParameter("operand1", "5");
        mathBlock.setParameter("operand2", "3");
        mathBlock.setParameter("resultVariable", "sum");
        
        // Создаем скрипт и добавляем блок
        CodeScript script = new CodeScript(testPlayerId, testPlayerName);
        script.createLine("Math Line").addBlock(mathBlock);
        
        // Сериализуем
        String json = serializer.serialize(script);
        assertNotNull(json);
        assertTrue(json.contains("MATH"));
        assertTrue(json.contains("ADD"));
        assertTrue(json.contains("5"));
        assertTrue(json.contains("3"));
        assertTrue(json.contains("sum"));
        
        // Десериализуем
        CodeScript deserializedScript = serializer.deserialize(json, testPlayerId, testPlayerName);
        assertNotNull(deserializedScript);
        assertEquals(1, deserializedScript.getLines().size());
        
        CodeBlock deserializedBlock = deserializedScript.getLines().get(0).getBlocks().get(0);
        assertNotNull(deserializedBlock);
        assertEquals(BlockType.MATH, deserializedBlock.getType());
        assertEquals("5", deserializedBlock.getParameter("operand1"));
        assertEquals("3", deserializedBlock.getParameter("operand2"));
        assertEquals("sum", deserializedBlock.getParameter("resultVariable"));
    }
    
    @Test
    void testComplexScriptSerialization() {
        // Создаем сложный скрипт с несколькими блоками
        CodeScript script = new CodeScript(testPlayerId, testPlayerName);
        
        // Добавляем несколько строк с блоками
        var line1 = script.createLine("Repeat Line");
        AsyncRepeatBlock repeatBlock = new AsyncRepeatBlock();
        repeatBlock.setParameter("repeatType", RepeatType.WHILE);
        repeatBlock.setParameter("value", "condition");
        line1.addBlock(repeatBlock);
        
        var line2 = script.createLine("Math Line");
        MathBlock mathBlock = new MathBlock();
        mathBlock.setParameter("operation", MathOperation.MULTIPLY);
        mathBlock.setParameter("operand1", "10");
        mathBlock.setParameter("operand2", "2");
        line2.addBlock(mathBlock);
        
        // Устанавливаем глобальные переменные
        script.setGlobalVariable("counter", 0);
        script.setGlobalVariable("message", "Hello World");
        
        // Сериализуем
        String json = serializer.serialize(script);
        assertNotNull(json);
        assertTrue(json.contains("REPEAT"));
        assertTrue(json.contains("MATH"));
        assertTrue(json.contains("WHILE"));
        assertTrue(json.contains("MULTIPLY"));
        assertTrue(json.contains("counter"));
        assertTrue(json.contains("Hello World"));
        
        // Десериализуем
        CodeScript deserializedScript = serializer.deserialize(json, testPlayerId, testPlayerName);
        assertNotNull(deserializedScript);
        assertEquals(2, deserializedScript.getLines().size());
        assertEquals(0, deserializedScript.getGlobalVariables().get("counter"));
        assertEquals("Hello World", deserializedScript.getGlobalVariables().get("message"));
    }
    
    @Test
    void testEmptyScriptSerialization() {
        // Тест с пустым скриптом
        CodeScript emptyScript = new CodeScript(testPlayerId, testPlayerName);
        
        String json = serializer.serialize(emptyScript);
        assertNotNull(json);
        assertTrue(json.contains("TestPlayer"));
        assertTrue(json.contains(testPlayerId.toString()));
        
        CodeScript deserializedScript = serializer.deserialize(json, testPlayerId, testPlayerName);
        assertNotNull(deserializedScript);
        assertEquals(0, deserializedScript.getLines().size());
        assertEquals(testPlayerName, deserializedScript.getPlayerName());
        assertEquals(testPlayerId, deserializedScript.getPlayerId());
    }
    
    @Test
    void testInvalidJsonDeserialization() {
        // Тест с некорректным JSON
        String invalidJson = "{ invalid json }";
        
        CodeScript deserializedScript = serializer.deserialize(invalidJson, testPlayerId, testPlayerName);
        assertNotNull(deserializedScript);
        assertEquals(testPlayerName, deserializedScript.getPlayerName());
        assertEquals(testPlayerId, deserializedScript.getPlayerId());
        assertEquals(0, deserializedScript.getLines().size());
    }
}
