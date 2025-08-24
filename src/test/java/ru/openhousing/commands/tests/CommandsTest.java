package ru.openhousing.commands.tests;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.openhousing.OpenHousing;
import ru.openhousing.commands.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для системы команд
 */
@ExtendWith(MockitoExtension.class)
class CommandsTest {

    @Mock
    private OpenHousing plugin;
    
    @Mock
    private CommandSender sender;
    
    @Mock
    private Player player;
    
    @Mock
    private Command command;

    private HousingCommand housingCommand;
    private CodeCommand codeCommand;
    private HouseModeCommand houseModeCommand;

    @BeforeEach
    void setUp() {
        when(player.getName()).thenReturn("TestPlayer");
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.hasPermission(anyString())).thenReturn(true);
        
        housingCommand = new HousingCommand(plugin);
        codeCommand = new CodeCommand(plugin);
        houseModeCommand = new HouseModeCommand(plugin);
    }

    @Test
    void testHousingCommandCreation() {
        // Arrange & Act
        HousingCommand command = new HousingCommand(plugin);
        
        // Assert
        assertNotNull(command);
    }

    @Test
    void testHousingCommandHelp() {
        // Arrange
        String[] args = {"help"};
        
        // Act
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        
        // Assert
        assertTrue(result);
        verify(player, atLeastOnce()).sendMessage(contains("§6=== OpenHousing Команды ==="));
    }

    @Test
    void testHousingCommandCreate() {
        // Arrange
        String[] args = {"create", "Test House"};
        
        // Act
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        
        // Assert
        assertTrue(result);
        // Note: Actual house creation would require more complex mocking
    }

    @Test
    void testHousingCommandList() {
        // Arrange
        String[] args = {"list"};
        
        // Act
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void testHousingCommandInfo() {
        // Arrange
        String[] args = {"info", "TestHouse"};
        
        // Act
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void testHousingCommandDelete() {
        // Arrange
        String[] args = {"delete", "TestHouse"};
        
        // Act
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void testHousingCommandTeleport() {
        // Arrange
        String[] args = {"tp", "TestHouse"};
        
        // Act
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void testHousingCommandSettings() {
        // Arrange
        String[] args = {"settings", "TestHouse"};
        
        // Act
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void testHousingCommandInvite() {
        // Arrange
        String[] args = {"invite", "TestHouse", "OtherPlayer"};
        
        // Act
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void testHousingCommandKick() {
        // Arrange
        String[] args = {"kick", "TestHouse", "OtherPlayer"};
        
        // Act
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void testHousingCommandBan() {
        // Arrange
        String[] args = {"ban", "TestHouse", "OtherPlayer"};
        
        // Act
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void testHousingCommandUnban() {
        // Arrange
        String[] args = {"unban", "TestHouse", "OtherPlayer"};
        
        // Act
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void testHousingCommandPublic() {
        // Arrange
        String[] args = {"public", "TestHouse"};
        
        // Act
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void testHousingCommandPrivate() {
        // Arrange
        String[] args = {"private", "TestHouse"};
        
        // Act
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void testHousingCommandInvalidSubcommand() {
        // Arrange
        String[] args = {"invalid"};
        
        // Act
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        
        // Assert
        assertTrue(result);
        verify(player, atLeastOnce()).sendMessage(contains("§cНеизвестная подкоманда"));
    }

    @Test
    void testHousingCommandNoPermission() {
        // Arrange
        when(player.hasPermission(anyString())).thenReturn(false);
        String[] args = {"create", "Test House"};
        
        // Act
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        
        // Assert
        assertTrue(result);
        verify(player, atLeastOnce()).sendMessage(contains("§cУ вас нет разрешения"));
    }

    @Test
    void testCodeCommandCreation() {
        // Arrange & Act
        CodeCommand command = new CodeCommand(plugin);
        
        // Assert
        assertNotNull(command);
    }

    @Test
    void testCodeCommandHelp() {
        // Arrange
        String[] args = {"help"};
        
        // Act
        boolean result = codeCommand.onCommand(player, command, "code", args);
        
        // Assert
        assertTrue(result);
        verify(player, atLeastOnce()).sendMessage(contains("§6=== Команды кода ==="));
    }

    @Test
    void testCodeCommandEditor() {
        // Arrange
        String[] args = {"editor"};
        
        // Act
        boolean result = codeCommand.onCommand(player, command, "code", args);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void testCodeCommandExecute() {
        // Arrange
        String[] args = {"execute"};
        
        // Act
        boolean result = codeCommand.onCommand(player, command, "code", args);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void testCodeCommandSave() {
        // Arrange
        String[] args = {"save"};
        
        // Act
        boolean result = codeCommand.onCommand(player, command, "code", args);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void testCodeCommandLoad() {
        // Arrange
        String[] args = {"load"};
        
        // Act
        boolean result = codeCommand.onCommand(player, command, "code", args);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void testCodeCommandDebug() {
        // Arrange
        String[] args = {"debug"};
        
        // Act
        boolean result = codeCommand.onCommand(player, command, "code", args);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void testCodeCommandShare() {
        // Arrange
        String[] args = {"share"};
        
        // Act
        boolean result = codeCommand.onCommand(player, command, "code", args);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void testCodeCommandImport() {
        // Arrange
        String[] args = {"import", "test-code"};
        
        // Act
        boolean result = codeCommand.onCommand(player, command, "code", args);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void testCodeCommandInvalidSubcommand() {
        // Arrange
        String[] args = {"invalid"};
        
        // Act
        boolean result = codeCommand.onCommand(player, command, "code", args);
        
        // Assert
        assertTrue(result);
        verify(player, atLeastOnce()).sendMessage(contains("§cНеизвестная подкоманда"));
    }

    @Test
    void testCodeCommandNoPermission() {
        // Arrange
        when(player.hasPermission(anyString())).thenReturn(false);
        String[] args = {"editor"};
        
        // Act
        boolean result = codeCommand.onCommand(player, command, "code", args);
        
        // Assert
        assertTrue(result);
        verify(player, atLeastOnce()).sendMessage(contains("§cУ вас нет разрешения"));
    }

    @Test
    void testHouseModeCommandCreation() {
        // Arrange & Act
        HouseModeCommand command = new HouseModeCommand(plugin);
        
        // Assert
        assertNotNull(command);
    }

    @Test
    void testHouseModeCommandEnable() {
        // Arrange
        String[] args = {"enable"};
        
        // Act
        boolean result = houseModeCommand.onCommand(player, command, "housemode", args);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void testHouseModeCommandDisable() {
        // Arrange
        String[] args = {"disable"};
        
        // Act
        boolean result = houseModeCommand.onCommand(player, command, "housemode", args);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void testHouseModeCommandStatus() {
        // Arrange
        String[] args = {"status"};
        
        // Act
        boolean result = houseModeCommand.onCommand(player, command, "housemode", args);
        
        // Assert
        assertTrue(result);
    }

    @Test
    void testHouseModeCommandInvalidSubcommand() {
        // Arrange
        String[] args = {"invalid"};
        
        // Act
        boolean result = houseModeCommand.onCommand(player, command, "housemode", args);
        
        // Assert
        assertTrue(result);
        verify(player, atLeastOnce()).sendMessage(contains("§cНеизвестная подкоманда"));
    }

    @Test
    void testHouseModeCommandNoPermission() {
        // Arrange
        when(player.hasPermission(anyString())).thenReturn(false);
        String[] args = {"enable"};
        
        // Act
        boolean result = houseModeCommand.onCommand(player, command, "housemode", args);
        
        // Assert
        assertTrue(result);
        verify(player, atLeastOnce()).sendMessage(contains("§cУ вас нет разрешения"));
    }

    @Test
    void testCommandSenderNotPlayer() {
        // Arrange
        String[] args = {"help"};
        
        // Act
        boolean result = housingCommand.onCommand(sender, command, "housing", args);
        
        // Assert
        assertTrue(result);
        verify(sender, atLeastOnce()).sendMessage(contains("§cЭта команда только для игроков"));
    }

    @Test
    void testCommandTabCompletion() {
        // Arrange
        String[] args = {"create"};
        
        // Act
        java.util.List<String> completions = housingCommand.onTabComplete(player, command, "housing", args);
        
        // Assert
        assertNotNull(completions);
        // Note: Actual tab completion would require more complex setup
    }

    @Test
    void testCommandUsage() {
        // Arrange
        String[] args = {};
        
        // Act
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        
        // Assert
        assertTrue(result);
        verify(player, atLeastOnce()).sendMessage(contains("§6Использование"));
    }

    @Test
    void testCommandInvalidArguments() {
        // Arrange
        String[] args = {"create"}; // Missing house name
        
        // Act
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        
        // Assert
        assertTrue(result);
        verify(player, atLeastOnce()).sendMessage(contains("§cНеверное количество аргументов"));
    }

    @Test
    void testCommandHouseNotFound() {
        // Arrange
        String[] args = {"info", "NonExistentHouse"};
        
        // Act
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        
        // Assert
        assertTrue(result);
        verify(player, atLeastOnce()).sendMessage(contains("§cДом не найден"));
    }

    @Test
    void testCommandPlayerNotFound() {
        // Arrange
        String[] args = {"invite", "TestHouse", "NonExistentPlayer"};
        
        // Act
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        
        // Assert
        assertTrue(result);
        verify(player, atLeastOnce()).sendMessage(contains("§cИгрок не найден"));
    }

    @Test
    void testCommandNotOwner() {
        // Arrange
        String[] args = {"delete", "TestHouse"};
        
        // Act
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        
        // Assert
        assertTrue(result);
        verify(player, atLeastOnce()).sendMessage(contains("§cВы не являетесь владельцем"));
    }

    @Test
    void testCommandAlreadyExists() {
        // Arrange
        String[] args = {"create", "ExistingHouse"};
        
        // Act
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        
        // Assert
        assertTrue(result);
        verify(player, atLeastOnce()).sendMessage(contains("§cДом с таким именем уже существует"));
    }

    @Test
    void testCommandInsufficientFunds() {
        // Arrange
        String[] args = {"create", "ExpensiveHouse"};
        
        // Act
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        
        // Assert
        assertTrue(result);
        verify(player, atLeastOnce()).sendMessage(contains("§cНедостаточно средств"));
    }

    @Test
    void testCommandSuccess() {
        // Arrange
        String[] args = {"create", "NewHouse"};
        
        // Act
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        
        // Assert
        assertTrue(result);
        verify(player, atLeastOnce()).sendMessage(contains("§aДом успешно создан"));
    }
}
