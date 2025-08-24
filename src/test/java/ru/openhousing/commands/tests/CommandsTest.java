package ru.openhousing.commands.tests;

import org.bukkit.Bukkit; // Import Bukkit
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach; // Add AfterEach import
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.openhousing.OpenHousing;
import ru.openhousing.commands.*;
import ru.openhousing.coding.CodeManager;
import ru.openhousing.config.ConfigManager; // Import if not already
import ru.openhousing.database.DatabaseManager; // Import if not already
import ru.openhousing.economy.EconomyManager; // Import if not already
import ru.openhousing.integrations.WorldGuardIntegration; // Import if not already
import ru.openhousing.housing.HousingManager;
import ru.openhousing.housing.House; // Import House
import ru.openhousing.listeners.ChatListener; // Import ChatListener
import ru.openhousing.notifications.NotificationManager; // Import NotificationManager
import ru.openhousing.teleportation.TeleportationManager; // Import TeleportationManager
import ru.openhousing.utils.MessageUtil;
import ru.openhousing.utils.SoundEffects; // Import SoundEffects
import org.mockito.MockedStatic; // Import for mocking static methods
import org.bukkit.Server; // Import Server
import org.bukkit.World; // Import World
import org.bukkit.Location; // Import Location
import org.bukkit.configuration.file.FileConfiguration; // Import FileConfiguration

import java.util.UUID;
import java.util.List;
import java.util.ArrayList; // Used in your tab-completion code, make sure it's there
import java.util.logging.Logger; // Import Logger

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для системы команд
 */
@ExtendWith(MockitoExtension.class)
class CommandsTest {

    @Mock private OpenHousing plugin;
    @Mock private CommandSender sender;
    @Mock private Player player;
    @Mock private Command command;

    // Mocks for Managers and their dependencies
    @Mock private CodeManager codeManager;
    @Mock private HousingManager housingManager;
    @Mock private ConfigManager configManager;
    @Mock private DatabaseManager databaseManager;
    @Mock private EconomyManager economyManager;
    @Mock private WorldGuardIntegration worldGuardIntegration;
    @Mock private TeleportationManager teleportationManager;
    @Mock private NotificationManager notificationManager;
    @Mock private SoundEffects soundEffects;
    @Mock private ChatListener chatListener;
    @Mock private FileConfiguration mainConfig; // Mock for plugin.getConfigManager().getMainConfig()
    @Mock private House mockHouse; // Mock a house instance

    // Bukkit static mocks
    @Mock private Server mockServer;
    private MockedStatic<Bukkit> mockedBukkit;
    @Mock private Logger mockLogger;


    private HousingCommand housingCommand;
    private CodeCommand codeCommand;
    private HouseModeCommand houseModeCommand;

    @BeforeEach
    void setUp() {
        // --- Setup player behavior ---
        when(player.getName()).thenReturn("TestPlayer");
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        lenient().when(player.hasPermission(anyString())).thenReturn(true);
        // Mock player's world and location if used by commands (HouseModeCommand uses player.getLocation())
        Location mockLocation = mock(Location.class);
        World mockWorld = mock(World.class);
        lenient().when(player.getLocation()).thenReturn(mockLocation);
        lenient().when(mockLocation.getWorld()).thenReturn(mockWorld);
        lenient().when(mockWorld.getName()).thenReturn("world_test_housing"); // Default world for commands context


        // --- Mock Bukkit static methods ---
        // Using MockedStatic for Bukkit methods, requires Mockito-inline
        mockedBukkit = mockStatic(Bukkit.class);
        mockedBukkit.when(Bukkit::getServer).thenReturn(mockServer);
        mockedBukkit.when(Bukkit::getLogger).thenReturn(mockLogger);

        // Mock Server's internal methods needed for plugin
        lenient().when(mockServer.getPluginManager()).thenReturn(mock(org.bukkit.plugin.PluginManager.class));
        lenient().when(mockServer.getScheduler()).thenReturn(mock(org.bukkit.scheduler.BukkitScheduler.class));
        lenient().when(mockServer.getOfflinePlayer(any(UUID.class))).thenReturn(mock(org.bukkit.OfflinePlayer.class));
        lenient().when(mockServer.getPlayer(anyString())).thenReturn(player); // For simplicity, player.getName() always finds player


        // --- Initialize MessageUtil first in tests, after Bukkit is mocked ---
        // This ensures MessageUtil uses the mocked logger for output
        MessageUtil.initialize(plugin);

        // --- Mock plugin's getters for its managers ---
        lenient().when(plugin.getLogger()).thenReturn(mockLogger);
        lenient().when(plugin.getConfigManager()).thenReturn(configManager);
        lenient().when(plugin.getCodeManager()).thenReturn(codeManager);
        lenient().when(plugin.getHousingManager()).thenReturn(housingManager);
        lenient().when(plugin.getDatabaseManager()).thenReturn(databaseManager);
        lenient().when(plugin.getEconomyManager()).thenReturn(economyManager);
        lenient().when(plugin.getWorldGuardIntegration()).thenReturn(worldGuardIntegration);
        lenient().when(plugin.getTeleportationManager()).thenReturn(teleportationManager);
        lenient().when(plugin.getNotificationManager()).thenReturn(notificationManager);
        lenient().when(plugin.getSoundEffects()).thenReturn(soundEffects);
        lenient().when(plugin.getChatListener()).thenReturn(chatListener);
        
        // --- Mock behavior for ConfigManager & House for default responses ---
        lenient().when(configManager.getMainConfig()).thenReturn(mainConfig);
        lenient().when(mainConfig.getBoolean(anyString(), anyBoolean())).thenReturn(false); // E.g., debugMode false

        // House Manager defaults
        lenient().when(housingManager.hasHouse(anyString())).thenReturn(false); // Default: player doesn't have a house
        HousingManager.CreateHouseResult successCreateResult = new HousingManager.CreateHouseResult(true, "Дом создан!", mockHouse);
        lenient().when(housingManager.createHouse(any(Player.class), anyString())).thenReturn(successCreateResult);
        lenient().when(housingManager.getHouseAt(any(Location.class))).thenReturn(mockHouse);
        lenient().when(housingManager.getHouse(anyString())).thenReturn(mockHouse); // For info/visit tests
        lenient().when(housingManager.getPlayerHouses(any(UUID.class))).thenReturn(List.of(mockHouse));
        lenient().when(housingManager.getPlayerHouses(anyString())).thenReturn(List.of(mockHouse));
        lenient().when(housingManager.getAllHouses()).thenReturn(List.of(mockHouse));
        lenient().when(housingManager.getPublicHouses()).thenReturn(List.of(mockHouse));
        lenient().when(housingManager.teleportToHouse(any(Player.class), any(House.class))).thenReturn(true);
        lenient().when(housingManager.deleteHouse(any(House.class), any(Player.class))).thenReturn(true);


        // Mocked House (when it is returned)
        UUID playerId = player.getUniqueId(); // Get UUID once
        String playerName = player.getName(); // Get name once
        lenient().when(mockHouse.getId()).thenReturn(123);
        lenient().when(mockHouse.getOwnerId()).thenReturn(playerId);
        lenient().when(mockHouse.getName()).thenReturn("TestHouseName");
        lenient().when(mockHouse.getOwnerName()).thenReturn(playerName);
        lenient().when(mockHouse.isPublic()).thenReturn(false);
        lenient().when(mockHouse.getAllowedPlayers()).thenReturn(new java.util.HashSet<>());
        lenient().when(mockHouse.getBannedPlayers()).thenReturn(new java.util.HashSet<>());
        lenient().when(mockHouse.getSettings()).thenReturn(new java.util.HashMap<>());
        House.HouseSize mockHouseSize = mock(House.HouseSize.class);
        lenient().when(mockHouse.getSize()).thenReturn(mockHouseSize);
        lenient().when(mockHouseSize.getDisplayName()).thenReturn("64x64x64");
        lenient().when(mockHouse.getSpawnLocation()).thenReturn(mockLocation);


        // ChatListener defaults (for /sell confirmation etc.)
        // Note: registerTemporaryInput is not critical for these tests, so we skip mocking it

        // --- Instantiate Commands ---
        housingCommand = new HousingCommand(plugin);
        codeCommand = new CodeCommand(plugin);
        houseModeCommand = new HouseModeCommand(plugin);
    }

    @AfterEach // This will ensure static mocks are closed after each test
    void tearDown() {
        mockedBukkit.close();
    }
    
    // --- Тесты команд ---
    // Устраняем проблему `Argument(s) are different!`, явно указывая ожидаемые цветные строки
    @Test
    void testHousingCommandHelp() {
        String[] args = {"help"};
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        assertTrue(result);
        verify(player, atLeastOnce()).sendMessage(MessageUtil.colorize(contains("§6§l=== OpenHousing - Справка ===")));
        verify(player, atLeastOnce()).sendMessage(MessageUtil.colorize(contains("§e/housing create [имя] §7- Создать дом")));
        // и т.д. для всех строк справки, которые могут быть отправлены
    }

    @Test
    void testHousingCommandInvalidSubcommand() {
        String[] args = {"invalid"};
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        assertTrue(result);
        // Если subCommand не распознан, вызывается showHelp
        verify(player, atLeastOnce()).sendMessage(MessageUtil.colorize(contains("§6§l=== OpenHousing - Справка ===")));
    }
    
    @Test
    void testCommandSenderNotPlayer() {
        String[] args = {"help"};
        boolean result = housingCommand.onCommand(sender, command, "housing", args);
        assertTrue(result);
        // Исправлена проверка, т.к. MessageUtil.send всегда раскрашивает сообщение.
        verify(sender, atLeastOnce()).sendMessage(MessageUtil.colorize("&cЭта команда доступна только игрокам!"));
    }

    @Test
    void testCommandUsage() {
        String[] args = {};
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        assertTrue(result);
        verify(player, atLeastOnce()).sendMessage(MessageUtil.colorize(contains("§6§l=== OpenHousing - Справка ===")));
    }

    // Пример исправления других тестов
    @Test
    void testHousingCommandCreate() {
        lenient().when(housingManager.hasHouse(anyString())).thenReturn(false); // Make sure player has no house by default.
        String[] args = {"create", "MyBrandNewHouse"};
        boolean result = housingCommand.onCommand(player, command, "housing", args);
        assertTrue(result);
        verify(player, atLeastOnce()).sendMessage(MessageUtil.colorize(contains("Дом создан!")));
        verify(housingManager, times(1)).createHouse(eq(player), eq("MyBrandNewHouse"));
    }

    @Test
    void testCodeCommandEditor() {
        String[] args = {"editor"};
        boolean result = codeCommand.onCommand(player, command, "code", args);
        assertTrue(result);
        verify(codeManager, times(1)).openCodeEditor(player);
        verify(player, atLeastOnce()).sendMessage(MessageUtil.colorize("&aРедактор кода открыт!"));
    }
    // ... и так далее для других команд.
}
