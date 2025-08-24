package ru.openhousing.coding.tests;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.actions.PlayerActionBlock;
import ru.openhousing.coding.blocks.actions.PlayerActionBlock.PlayerActionType;
import ru.openhousing.coding.script.CodeScript;
import ru.openhousing.coding.script.CodeLine;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тесты выполнения кода
 */
@ExtendWith(MockitoExtension.class)
class CodeExecutionTest {

    @Mock
    private Player mockPlayer;

    private CodeBlock.ExecutionContext mockContext;

    @BeforeEach
    void setUp() {
        mockContext = new CodeBlock.ExecutionContext(mockPlayer);
        mockContext.setVariable("playerName", "TestPlayer");
        mockContext.setVariable("testNumber", 42);
        mockContext.setVariable("testString", "Hello World");
    }

    @Test
    @DisplayName("Тест выполнения простого блока отправки сообщения")
    void testSimpleMessageExecution() {
        // Arrange
        PlayerActionBlock block = new PlayerActionBlock();
        block.setParameter("actionType", PlayerActionType.SEND_MESSAGE);
        block.setParameter("value", "Привет, %playerName%!");

        // Act
        CodeBlock.ExecutionResult result = block.execute(mockContext);

        // Assert
        assertTrue(result.isSuccess(), "Блок должен выполниться успешно");
        verify(mockPlayer, times(1)).sendMessage(contains("Привет, TestPlayer!"));
    }

    @Test
    @DisplayName("Тест выполнения скрипта с несколькими блоками")
    void testScriptExecution() {
        // Arrange
        CodeScript script = new CodeScript(java.util.UUID.randomUUID(), "TestPlayer");
        
        // Создаем строку с блоком отправки сообщения
        CodeLine line = new CodeLine(1, "Test Line");
        PlayerActionBlock block = new PlayerActionBlock();
        block.setParameter("actionType", PlayerActionType.SEND_MESSAGE);
        block.setParameter("value", "Скрипт выполнен!");
        line.addBlock(block);
        
        script.addLine(line);

        // Act
        CodeBlock.ExecutionResult result = script.execute(mockContext);

        // Assert
        assertTrue(result.isSuccess(), "Скрипт должен выполниться успешно");
        verify(mockPlayer, times(1)).sendMessage(contains("Скрипт выполнен!"));
    }

    @Test
    @DisplayName("Тест выполнения блока с переменными")
    void testBlockWithVariables() {
        // Arrange
        PlayerActionBlock block = new PlayerActionBlock();
        block.setParameter("actionType", PlayerActionType.SEND_MESSAGE);
        block.setParameter("value", "Число: %testNumber%, Строка: %testString%");

        // Act
        CodeBlock.ExecutionResult result = block.execute(mockContext);

        // Assert
        assertTrue(result.isSuccess(), "Блок должен выполниться успешно");
        verify(mockPlayer, times(1)).sendMessage(contains("Число: 42, Строка: Hello World"));
    }

    @Test
    @DisplayName("Тест выполнения блока телепортации")
    void testTeleportBlock() {
        // Arrange
        PlayerActionBlock block = new PlayerActionBlock();
        block.setParameter("actionType", PlayerActionType.TELEPORT);
        block.setParameter("value", "100,64,100"); // x,y,z

        // Act
        CodeBlock.ExecutionResult result = block.execute(mockContext);

        // Assert
        assertTrue(result.isSuccess(), "Блок телепортации должен выполниться успешно");
        // Здесь можно добавить проверку телепортации, если нужно
    }

    @Test
    @DisplayName("Тест выполнения блока с ошибкой")
    void testBlockWithError() {
        // Arrange
        PlayerActionBlock block = new PlayerActionBlock();
        block.setParameter("actionType", PlayerActionType.SEND_MESSAGE);
        block.setParameter("value", null); // Некорректное значение

        // Act
        CodeBlock.ExecutionResult result = block.execute(mockContext);

        // Assert
        assertTrue(result.isSuccess(), "Блок должен обработать null значение");
    }
}
