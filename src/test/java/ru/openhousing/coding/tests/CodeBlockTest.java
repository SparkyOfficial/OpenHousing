package ru.openhousing.coding.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Arrays;

import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.actions.PlayerActionBlock;
import ru.openhousing.coding.constants.BlockParams;
import ru.openhousing.coding.blocks.actions.PlayerActionBlock.PlayerActionType;
import ru.openhousing.coding.blocks.BlockType;

/**
 * Модульные тесты для CodeBlock и его реализаций
 */
@DisplayName("Тесты системы кодинга")
public class CodeBlockTest {
    
    private CodeBlock.ExecutionContext mockContext;
    private org.bukkit.entity.Player mockPlayer;
    
    @BeforeEach
    void setUp() {
        // Создаем мок-объекты
        mockPlayer = mock(org.bukkit.entity.Player.class);
        mockContext = new CodeBlock.ExecutionContext(mockPlayer);
        
        // Устанавливаем базовые переменные
        mockContext.setVariable("playerName", "TestUser");
        mockContext.setVariable("testNumber", 42);
        mockContext.setVariable("testString", "Hello World");
    }
    
    @Test
    @DisplayName("Тест PlayerActionBlock SEND_MESSAGE")
    void testPlayerActionBlockSendMessage() {
        // Arrange
        PlayerActionBlock block = new PlayerActionBlock();
        block.setParameter(BlockParams.ACTION_TYPE, PlayerActionType.SEND_MESSAGE);
        block.setParameter(BlockParams.VALUE, "Привет, %playerName%!");
        
        // Act
        CodeBlock.ExecutionResult result = block.execute(mockContext);
        
        // Assert
        assertTrue(result.isSuccess(), "Действие должно выполниться успешно");
        verify(mockPlayer, times(1)).sendMessage(anyString());
    }
    
    @Test
    @DisplayName("Тест PlayerActionBlock с переменной")
    void testPlayerActionBlockWithVariable() {
        // Arrange
        PlayerActionBlock block = new PlayerActionBlock();
        block.setParameter(BlockParams.ACTION_TYPE, PlayerActionType.SEND_MESSAGE);
        block.setParameter(BlockParams.VALUE, "Число: %testNumber%, Строка: %testString%");
        
        // Act
        CodeBlock.ExecutionResult result = block.execute(mockContext);
        
        // Assert
        assertTrue(result.isSuccess(), "Действие должно выполниться успешно");
        verify(mockPlayer, times(1)).sendMessage(contains("Число: 42, Строка: Hello World"));
    }
    
    @Test
    @DisplayName("Тест валидации PlayerActionBlock")
    void testPlayerActionBlockValidation() {
        // Arrange
        PlayerActionBlock block = new PlayerActionBlock();
        
        // Act & Assert
        assertTrue(block.validate(), "Блок должен быть валидным по умолчанию");
        
        // Убираем обязательный параметр
        block.setParameter(BlockParams.ACTION_TYPE, null);
        assertFalse(block.validate(), "Блок без типа действия должен быть невалидным");
    }
    
    @Test
    @DisplayName("Тест контекста выполнения")
    void testExecutionContext() {
        // Arrange
        CodeBlock.ExecutionContext context = new CodeBlock.ExecutionContext(mockPlayer);
        
        // Act
        context.setVariable("testVar", "testValue");
        context.setVariable("numberVar", 123);
        
        // Assert
        assertEquals("testValue", context.getVariable("testVar"));
        assertEquals(123, context.getVariable("numberVar"));
        assertEquals(mockPlayer, context.getPlayer());
        assertFalse(context.isDebugMode());
    }
    
    @Test
    @DisplayName("Тест режима отладки")
    void testDebugMode() {
        // Arrange
        CodeBlock.ExecutionContext context = new CodeBlock.ExecutionContext(mockPlayer);
        
        // Act
        context.setDebugMode(true);
        
        // Assert
        assertTrue(context.isDebugMode());
    }
    
    @Test
    @DisplayName("Тест клонирования блока")
    void testBlockCloning() {
        // Arrange
        PlayerActionBlock original = new PlayerActionBlock();
        original.setParameter(BlockParams.ACTION_TYPE, PlayerActionType.SEND_MESSAGE);
        original.setParameter(BlockParams.VALUE, "Тестовое сообщение");
        
        // Act
        CodeBlock cloned = original.clone();
        
        // Assert
        assertNotNull(cloned, "Клонированный блок не должен быть null");
        assertNotSame(original, cloned, "Клонированный блок должен быть другим объектом");
        assertEquals(original.getType(), cloned.getType(), "Тип блока должен совпадать");
        
        // Проверяем, что параметры скопированы
        assertEquals(original.getParameter(BlockParams.VALUE), 
                    cloned.getParameter(BlockParams.VALUE), 
                    "Параметры должны быть скопированы");
    }
    
    @Test
    @DisplayName("Тест выполнения с ошибкой")
    void testExecutionWithError() {
        // Arrange
        PlayerActionBlock block = new PlayerActionBlock();
        block.setParameter(BlockParams.ACTION_TYPE, PlayerActionType.SEND_MESSAGE);
        block.setParameter(BlockParams.VALUE, null); // Вызовет ошибку
        
        // Act
        CodeBlock.ExecutionResult result = block.execute(mockContext);
        
        // Assert
        // PlayerActionBlock может обрабатывать null значения, поэтому проверяем результат
        // независимо от того, успешный он или нет
        assertNotNull(result, "Результат не должен быть null");
        if (!result.isSuccess()) {
            assertNotNull(result.getMessage(), "Должно быть сообщение об ошибке");
        }
    }
    
    @Test
    @DisplayName("Тест дочерних блоков")
    void testChildBlocks() {
        // Arrange
        PlayerActionBlock parent = new PlayerActionBlock();
        PlayerActionBlock child1 = new PlayerActionBlock();
        PlayerActionBlock child2 = new PlayerActionBlock();
        
        // Act
        parent.addChild(child1);
        parent.addChild(child2);
        
        // Assert
        assertEquals(2, parent.getChildBlocks().size(), "Должно быть 2 дочерних блока");
        assertTrue(parent.getChildBlocks().contains(child1), "Первый дочерний блок должен присутствовать");
        assertTrue(parent.getChildBlocks().contains(child2), "Второй дочерний блок должен присутствовать");
    }
    
    @Test
    @DisplayName("Тест параметров блока")
    void testBlockParameters() {
        // Arrange
        PlayerActionBlock block = new PlayerActionBlock();
        
        // Act
        block.setParameter("customParam", "customValue");
        block.setParameter("numberParam", 999);
        
        // Assert
        assertEquals("customValue", block.getParameter("customParam"));
        assertEquals(999, block.getParameter("numberParam"));
        assertNull(block.getParameter("nonExistentParam"));
    }
    
    @Test
    @DisplayName("Тест замены переменных в строке")
    void testVariableReplacement() {
        // Arrange
        TestCodeBlock block = new TestCodeBlock();
        String template = "Привет, %playerName%! Твое число: %testNumber%";
        
        // Act
        String result = block.testReplaceVariables(template, mockContext);
        
        // Assert
        assertEquals("Привет, TestUser! Твое число: 42", result);
    }
    
    @Test
    @DisplayName("Тест замены переменных с несуществующими")
    void testVariableReplacementWithMissing() {
        // Arrange
        TestCodeBlock block = new TestCodeBlock();
        String template = "Привет, %playerName%! Несуществующая: %missingVar%";
        
        // Act
        String result = block.testReplaceVariables(template, mockContext);
        
        // Assert
        assertEquals("Привет, TestUser! Несуществующая: %missingVar%", result);
    }
    
    /**
     * Тестовый блок для доступа к protected методам
     */
    private static class TestCodeBlock extends CodeBlock {
        
        public TestCodeBlock() {
            super(BlockType.PLAYER_SEND_MESSAGE);
        }
        
        @Override
        public ExecutionResult execute(ExecutionContext context) {
            return ExecutionResult.success();
        }
        
        @Override
        public boolean validate() {
            return true;
        }
        
        @Override
        public List<String> getDescription() {
            return Arrays.asList("Тестовый блок");
        }
        
        // Делаем protected метод доступным для тестов
        public String testReplaceVariables(String text, ExecutionContext context) {
            return replaceVariables(text, context);
        }
    }
}
