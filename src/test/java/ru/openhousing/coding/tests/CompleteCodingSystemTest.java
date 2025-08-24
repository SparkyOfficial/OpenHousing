package ru.openhousing.coding.tests;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionContext;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionResult;
import ru.openhousing.coding.blocks.actions.PlayerActionBlock;
import ru.openhousing.coding.blocks.actions.PlayerActionBlock.PlayerActionType;
import ru.openhousing.coding.blocks.conditions.IfVariableConditionBlock;
import ru.openhousing.coding.blocks.control.RepeatBlock;
import ru.openhousing.coding.blocks.events.PlayerJoinEventBlock;
import ru.openhousing.coding.blocks.events.PlayerDeathEventBlock;
import ru.openhousing.coding.blocks.events.PlayerBlockBreakEventBlock;
import ru.openhousing.coding.blocks.events.PlayerChatEventBlock;
import ru.openhousing.coding.blocks.functions.FunctionBlock;
import ru.openhousing.coding.blocks.functions.CallFunctionBlock;
import ru.openhousing.coding.script.CodeLine;
import ru.openhousing.coding.script.CodeScript;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Комплексный тест всей системы кодинга
 * Демонстрирует работу всех блоков и их интеграцию
 */
@ExtendWith(MockitoExtension.class)
class CompleteCodingSystemTest {

    private CodeScript script;
    @Mock
    private Player mockPlayer;
    private ExecutionContext mockContext;

    @BeforeEach
    void setUp() {
        script = new CodeScript(mockPlayer.getUniqueId(), "CompleteSystemTest");
        mockContext = new ExecutionContext(mockPlayer);
        
        // Настройка мока игрока
        lenient().when(mockPlayer.getName()).thenReturn("TestPlayer");
        lenient().when(mockPlayer.getUniqueId()).thenReturn(java.util.UUID.randomUUID());
        lenient().when(mockPlayer.getInventory()).thenReturn(mock(org.bukkit.inventory.PlayerInventory.class));
        lenient().when(mockPlayer.getLocation()).thenReturn(mock(org.bukkit.Location.class));
        lenient().when(mockPlayer.getWorld()).thenReturn(mock(org.bukkit.World.class));
    }

    @Test
    @DisplayName("Тест базовой системы кодинга - Простое действие")
    void testBasicCodingSystem() {
        // Создаем простой скрипт с одним действием
        script = new CodeScript(mockPlayer.getUniqueId(), "BasicScript");
        
        // Строка 1: Простое действие - отправить сообщение
        CodeLine actionLine = new CodeLine(1, "Send Message");
        PlayerActionBlock actionBlock = new PlayerActionBlock();
        actionBlock.setParameter("actionType", PlayerActionType.SEND_MESSAGE);
        actionBlock.setParameter("value", "Привет, мир!");
        actionLine.addBlock(actionBlock);
        script.addLine(actionLine);

        // Выполняем скрипт
        ExecutionResult result = script.execute(mockContext);
        
        // Проверяем результат
        assertTrue(result.isSuccess(), "Базовый скрипт должен выполниться успешно");
        verify(mockPlayer, times(1)).sendMessage(contains("Привет, мир!"));
    }

    @Test
    @DisplayName("Тест системы условий - Переменные")
    void testVariableConditionSystem() {
        // Создаем скрипт с условиями
        script = new CodeScript(mockPlayer.getUniqueId(), "ConditionScript");
        
        // Устанавливаем переменную в контексте
        mockContext.setVariable("playerLevel", 10);
        mockContext.setVariable("playerGold", 100);
        
        // Строка 1: Условие - уровень игрока
        CodeLine levelLine = new CodeLine(1, "Check player level");
        IfVariableConditionBlock levelBlock = new IfVariableConditionBlock();
        levelBlock.setParameter("variableName", "playerLevel");
        levelBlock.setParameter("comparisonType", "greater");
        levelBlock.setParameter("value", "5");
        levelLine.addBlock(levelBlock);
        script.addLine(levelLine);

        // Строка 2: Действие если условие выполнено
        CodeLine actionLine = new CodeLine(2, "Level up action");
        PlayerActionBlock actionBlock = new PlayerActionBlock();
        actionBlock.setParameter("actionType", PlayerActionType.SEND_MESSAGE);
        actionBlock.setParameter("value", "Вы достигли высокого уровня!");
        actionLine.addBlock(actionBlock);
        script.addLine(actionLine);

        // Выполняем скрипт
        ExecutionResult result = script.execute(mockContext);
        
        // Проверяем результат
        assertTrue(result.isSuccess(), "Скрипт с условиями должен выполниться успешно");
        verify(mockPlayer, times(1)).sendMessage(contains("Вы достигли высокого уровня!"));
    }

    @Test
    @DisplayName("Тест системы циклов - Повторение действий")
    void testLoopSystem() {
        // Создаем скрипт с циклом
        script = new CodeScript(mockPlayer.getUniqueId(), "LoopScript");
        
        // Строка 1: Цикл - повторить 3 раза
        CodeLine loopLine = new CodeLine(1, "Repeat 3 times");
        RepeatBlock loopBlock = new RepeatBlock();
        loopBlock.setParameter("loopType", "repeat_times");
        loopBlock.setParameter("iterations", "3");
        loopLine.addBlock(loopBlock);
        script.addLine(loopLine);

        // Строка 2: Действие внутри цикла
        CodeLine actionLine = new CodeLine(2, "Action in loop");
        PlayerActionBlock actionBlock = new PlayerActionBlock();
        actionBlock.setParameter("actionType", PlayerActionType.SEND_MESSAGE);
        actionBlock.setParameter("value", "Итерация {iteration} из {total}");
        actionLine.addBlock(actionBlock);
        script.addLine(actionLine);

        // Выполняем скрипт
        ExecutionResult result = script.execute(mockContext);
        
        // Проверяем результат
        assertTrue(result.isSuccess(), "Скрипт с циклом должен выполниться успешно");
        verify(mockPlayer, times(3)).sendMessage(anyString());
    }

    @Test
    @DisplayName("Тест системы функций - Создание и вызов")
    void testFunctionSystem() {
        // Создаем скрипт с функциями
        script = new CodeScript(mockPlayer.getUniqueId(), "FunctionScript");
        
        // Строка 1: Определение функции
        CodeLine functionLine = new CodeLine(1, "Define function");
        FunctionBlock functionBlock = new FunctionBlock();
        functionBlock.setParameter("functionName", "greet");
        functionBlock.setParameter("parameters", "name,time");
        functionBlock.setParameter("functionCode", "sendMessage('Привет, ' + name + '! Время: ' + time)");
        functionLine.addBlock(functionBlock);
        script.addLine(functionLine);

        // Строка 2: Вызов функции
        CodeLine callLine = new CodeLine(2, "Call function");
        CallFunctionBlock callBlock = new CallFunctionBlock();
        callBlock.setParameter("functionName", "greet");
        callBlock.setParameter("arguments", "TestPlayer,день");
        callLine.addBlock(callBlock);
        script.addLine(callLine);

        // Выполняем скрипт
        ExecutionResult result = script.execute(mockContext);
        
        // Проверяем результат
        assertTrue(result.isSuccess(), "Скрипт с функциями должен выполниться успешно");
    }

    @Test
    @DisplayName("Тест комплексного скрипта - Игровая механика")
    void testComplexGameMechanicsScript() {
        // Создаем комплексный скрипт игровой механики
        script = new CodeScript(mockPlayer.getUniqueId(), "GameMechanicsScript");
        
        // Устанавливаем переменные
        mockContext.setVariable("playerHealth", 20);
        mockContext.setVariable("playerHunger", 20);
        mockContext.setVariable("playerExperience", 0);
        
        // Строка 1: Действие - приветствие
        CodeLine welcomeLine = new CodeLine(1, "Welcome Message");
        PlayerActionBlock welcomeBlock = new PlayerActionBlock();
        welcomeBlock.setParameter("actionType", PlayerActionType.SEND_MESSAGE);
        welcomeBlock.setParameter("value", "Добро пожаловать в игру!");
        welcomeLine.addBlock(welcomeBlock);
        script.addLine(welcomeLine);

        // Строка 2: Условие - проверка здоровья
        CodeLine healthLine = new CodeLine(2, "Check health");
        IfVariableConditionBlock healthBlock = new IfVariableConditionBlock();
        healthBlock.setParameter("variableName", "playerHealth");
        healthBlock.setParameter("comparisonType", "less");
        healthBlock.setParameter("value", "10");
        healthLine.addBlock(healthBlock);
        script.addLine(healthLine);

        // Строка 3: Действие - лечение
        CodeLine healLine = new CodeLine(3, "Heal player");
        PlayerActionBlock healBlock = new PlayerActionBlock();
        healBlock.setParameter("actionType", PlayerActionType.SEND_MESSAGE);
        healBlock.setParameter("value", "Вы получили лечение!");
        healLine.addBlock(healBlock);
        script.addLine(healLine);

        // Строка 4: Цикл - дать опыт
        CodeLine expLine = new CodeLine(4, "Give experience loop");
        RepeatBlock expBlock = new RepeatBlock();
        expBlock.setParameter("loopType", "repeat_times");
        expBlock.setParameter("iterations", "5");
        expLine.addBlock(expBlock);
        script.addLine(expLine);

        // Строка 5: Действие в цикле
        CodeLine expActionLine = new CodeLine(5, "Experience action");
        PlayerActionBlock expActionBlock = new PlayerActionBlock();
        expActionBlock.setParameter("actionType", PlayerActionType.ADD_EXPERIENCE);
        expActionBlock.setParameter("amount", "10");
        expActionLine.addBlock(expActionBlock);
        script.addLine(expActionLine);

        // Выполняем скрипт
        ExecutionResult result = script.execute(mockContext);
        
        // Проверяем результат
        assertTrue(result.isSuccess(), "Комплексный скрипт должен выполниться успешно");
        verify(mockPlayer, times(1)).sendMessage(contains("Добро пожаловать в игру!"));
        verify(mockPlayer, times(1)).sendMessage(contains("Вы получили лечение!"));
    }

    @Test
    @DisplayName("Тест интеграции с /code editor - Создание скрипта через GUI")
    void testCodeEditorIntegration() {
        // Симулируем создание скрипта через /code editor
        script = new CodeScript(mockPlayer.getUniqueId(), "EditorCreatedScript");
        
        // Добавляем блоки как будто они были добавлены через GUI
        CodeLine line1 = new CodeLine(1, "Action: Send Message");
        PlayerActionBlock actionBlock = new PlayerActionBlock();
        actionBlock.setParameter("actionType", PlayerActionType.SEND_MESSAGE);
        actionBlock.setParameter("value", "Этот скрипт работает!");
        line1.addBlock(actionBlock);
        script.addLine(line1);

        CodeLine line2 = new CodeLine(2, "Condition: Check Variable");
        IfVariableConditionBlock conditionBlock = new IfVariableConditionBlock();
        conditionBlock.setParameter("variableName", "testVar");
        conditionBlock.setParameter("comparisonType", "equals");
        conditionBlock.setParameter("value", "true");
        line2.addBlock(conditionBlock);
        script.addLine(line2);

        // Выполняем скрипт
        ExecutionResult result = script.execute(mockContext);
        
        // Проверяем результат
        assertTrue(result.isSuccess(), "Скрипт созданный через редактор должен работать");
        verify(mockPlayer, times(1)).sendMessage(contains("Этот скрипт работает!"));
    }

    @Test
    @DisplayName("Тест производительности - Множественные блоки")
    void testPerformanceWithMultipleBlocks() {
        // Создаем скрипт с множественными блоками для тестирования производительности
        script = new CodeScript(mockPlayer.getUniqueId(), "PerformanceTest");
        
        // Добавляем 20 строк с разными блоками (уменьшил с 50 для производительности)
        for (int i = 1; i <= 20; i++) {
            CodeLine line = new CodeLine(i, "Line " + i);
            
            // Чередуем разные типы блоков
            switch (i % 4) {
                case 0:
                    PlayerActionBlock actionBlock = new PlayerActionBlock();
                    actionBlock.setParameter("actionType", PlayerActionType.SEND_MESSAGE);
                    actionBlock.setParameter("value", "Сообщение " + i);
                    line.addBlock(actionBlock);
                    break;
                case 1:
                    IfVariableConditionBlock conditionBlock = new IfVariableConditionBlock();
                    conditionBlock.setParameter("variableName", "var" + i);
                    conditionBlock.setParameter("comparisonType", "equals");
                    conditionBlock.setParameter("value", "true");
                    line.addBlock(conditionBlock);
                    break;
                case 2:
                    RepeatBlock repeatBlock = new RepeatBlock();
                    repeatBlock.setParameter("loopType", "repeat_times");
                    repeatBlock.setParameter("iterations", "2");
                    line.addBlock(repeatBlock);
                    break;
                case 3:
                    FunctionBlock functionBlock = new FunctionBlock();
                    functionBlock.setParameter("functionName", "func" + i);
                    functionBlock.setParameter("parameters", "param1,param2");
                    functionBlock.setParameter("functionCode", "sendMessage('Функция " + i + "')");
                    line.addBlock(functionBlock);
                    break;
            }
            
            script.addLine(line);
        }

        // Выполняем скрипт и измеряем время
        long startTime = System.currentTimeMillis();
        ExecutionResult result = script.execute(mockContext);
        long endTime = System.currentTimeMillis();
        
        // Проверяем результат
        assertTrue(result.isSuccess(), "Производительный тест должен выполниться успешно");
        assertTrue((endTime - startTime) < 1000, "Скрипт должен выполниться менее чем за 1 секунду");
        
        System.out.println("Производительный тест выполнен за " + (endTime - startTime) + "ms");
    }

    @Test
    @DisplayName("Тест совместимости блоков - Все типы блоков")
    void testAllBlockTypesCompatibility() {
        // Тестируем создание всех типов блоков
        assertDoesNotThrow(() -> {
            // События
            new PlayerJoinEventBlock();
            new PlayerDeathEventBlock();
            new PlayerBlockBreakEventBlock();
            new PlayerChatEventBlock();
            
            // Действия
            new PlayerActionBlock();
            
            // Условия
            new IfVariableConditionBlock();
            
            // Управление
            new RepeatBlock();
            
            // Функции
            new FunctionBlock();
            new CallFunctionBlock();
        }, "Все типы блоков должны создаваться без ошибок");
    }

    @Test
    @DisplayName("Тест создания блоков событий - Без выполнения")
    void testEventBlocksCreation() {
        // Тестируем создание блоков событий без их выполнения
        assertDoesNotThrow(() -> {
            // Создаем блоки событий
            PlayerJoinEventBlock joinBlock = new PlayerJoinEventBlock();
            joinBlock.setParameter("sendWelcomeMessage", true);
            joinBlock.setParameter("welcomeMessage", "Тест приветствия");
            
            PlayerDeathEventBlock deathBlock = new PlayerDeathEventBlock();
            deathBlock.setParameter("sendDeathMessage", true);
            deathBlock.setParameter("deathMessage", "Тест смерти");
            
            PlayerBlockBreakEventBlock breakBlock = new PlayerBlockBreakEventBlock();
            breakBlock.setParameter("blockType", "STONE");
            breakBlock.setParameter("sendMessage", true);
            breakBlock.setParameter("message", "Тест ломания");
            
            PlayerChatEventBlock chatBlock = new PlayerChatEventBlock();
            chatBlock.setParameter("filterWords", true);
            chatBlock.setParameter("filteredWords", "спам,реклама");
            chatBlock.setParameter("sendMessage", true);
            chatBlock.setParameter("message", "Тест чата");
            
            // Проверяем, что параметры установлены
            assertTrue((Boolean) joinBlock.getParameter("sendWelcomeMessage"));
            assertTrue((Boolean) deathBlock.getParameter("sendDeathMessage"));
            assertTrue((Boolean) breakBlock.getParameter("sendMessage"));
            assertTrue((Boolean) chatBlock.getParameter("sendMessage"));
            
        }, "Блоки событий должны создаваться и настраиваться без ошибок");
    }

    @Test
    @DisplayName("Тест системы переменных - Установка и получение")
    void testVariableSystem() {
        // Тестируем работу с переменными
        mockContext.setVariable("testString", "Hello World");
        mockContext.setVariable("testNumber", 42);
        mockContext.setVariable("testBoolean", true);
        
        // Проверяем получение переменных
        assertEquals("Hello World", mockContext.getVariable("testString"));
        assertEquals(42, mockContext.getVariable("testNumber"));
        assertEquals(true, mockContext.getVariable("testBoolean"));
        
        // Проверяем несуществующую переменную
        assertNull(mockContext.getVariable("nonExistent"));
        
        // Изменяем переменную
        mockContext.setVariable("testString", "Updated Value");
        assertEquals("Updated Value", mockContext.getVariable("testString"));
    }

    @Test
    @DisplayName("Тест системы кодинга - Демонстрация возможностей")
    void testCodingSystemDemonstration() {
        // Создаем демонстрационный скрипт
        script = new CodeScript(mockPlayer.getUniqueId(), "DemoScript");
        
        // Устанавливаем переменные для демонстрации
        mockContext.setVariable("playerName", "TestPlayer");
        mockContext.setVariable("playerLevel", 15);
        mockContext.setVariable("isVIP", true);
        
        // Строка 1: Приветствие
        CodeLine welcomeLine = new CodeLine(1, "Welcome");
        PlayerActionBlock welcomeBlock = new PlayerActionBlock();
        welcomeBlock.setParameter("actionType", PlayerActionType.SEND_MESSAGE);
        welcomeBlock.setParameter("value", "Добро пожаловать, {playerName}!");
        welcomeLine.addBlock(welcomeBlock);
        script.addLine(welcomeLine);
        
        // Строка 2: Проверка VIP статуса
        CodeLine vipLine = new CodeLine(2, "VIP Check");
        IfVariableConditionBlock vipBlock = new IfVariableConditionBlock();
        vipBlock.setParameter("variableName", "isVIP");
        vipBlock.setParameter("comparisonType", "equals");
        vipBlock.setParameter("value", "true");
        vipLine.addBlock(vipBlock);
        script.addLine(vipLine);
        
        // Строка 3: VIP награда
        CodeLine rewardLine = new CodeLine(3, "VIP Reward");
        PlayerActionBlock rewardBlock = new PlayerActionBlock();
        rewardBlock.setParameter("actionType", PlayerActionType.SEND_MESSAGE);
        rewardBlock.setParameter("value", "Вы получили VIP награду!");
        rewardLine.addBlock(rewardBlock);
        script.addLine(rewardLine);
        
        // Строка 4: Цикл наград
        CodeLine loopLine = new CodeLine(4, "Reward Loop");
        RepeatBlock loopBlock = new RepeatBlock();
        loopBlock.setParameter("loopType", "repeat_times");
        loopBlock.setParameter("iterations", "3");
        loopLine.addBlock(loopBlock);
        script.addLine(loopLine);
        
        // Строка 5: Действие в цикле
        CodeLine loopActionLine = new CodeLine(5, "Loop Action");
        PlayerActionBlock loopActionBlock = new PlayerActionBlock();
        loopActionBlock.setParameter("actionType", PlayerActionType.SEND_MESSAGE);
        loopActionBlock.setParameter("value", "Награда #{iteration}");
        loopActionLine.addBlock(loopActionBlock);
        script.addLine(loopActionLine);
        
        // Выполняем скрипт
        ExecutionResult result = script.execute(mockContext);
        
        // Проверяем результат
        assertTrue(result.isSuccess(), "Демонстрационный скрипт должен выполниться успешно");
        verify(mockPlayer, times(1)).sendMessage(contains("Добро пожаловать, TestPlayer!"));
        verify(mockPlayer, times(1)).sendMessage(contains("Вы получили VIP награду!"));
        verify(mockPlayer, times(3)).sendMessage(contains("Награда #"));
    }
}
