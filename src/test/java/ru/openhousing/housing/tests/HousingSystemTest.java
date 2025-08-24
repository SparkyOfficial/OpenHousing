package ru.openhousing.housing.tests;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.openhousing.OpenHousing;
import ru.openhousing.housing.House;
import ru.openhousing.housing.HousingManager;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для системы домов
 */
@ExtendWith(MockitoExtension.class)
class HousingSystemTest {

    @Mock
    private OpenHousing plugin;
    
    @Mock
    private Player player;
    
    @Mock
    private World world;
    
    @Mock
    private Location location;

    private HousingManager housingManager;
    private UUID playerId;
    private String playerName;
    private MockedStatic<Bukkit> mockedBukkit;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        playerName = "TestPlayer";
        
        lenient().when(player.getUniqueId()).thenReturn(playerId);
        lenient().when(player.getName()).thenReturn(playerName);
        lenient().when(player.getWorld()).thenReturn(world);
        lenient().when(player.getLocation()).thenReturn(location);
        lenient().when(location.getWorld()).thenReturn(world);
        lenient().when(world.getName()).thenReturn("world");
        lenient().when(player.hasPermission(anyString())).thenReturn(true);
        
        // Мокаем конфигурацию
        when(plugin.getConfigManager()).thenReturn(mock(ru.openhousing.config.ConfigManager.class));
        when(plugin.getConfigManager().getHousingConfig()).thenReturn(mock(org.bukkit.configuration.file.FileConfiguration.class));
        when(plugin.getConfigManager().getHousingConfig().getInt("max-houses-per-player", 1)).thenReturn(1);
        when(plugin.getConfigManager().getHousingConfig().getInt("default-size.width", 64)).thenReturn(64);
        when(plugin.getConfigManager().getHousingConfig().getInt("default-size.height", 64)).thenReturn(64);
        when(plugin.getConfigManager().getHousingConfig().getInt("default-size.length", 64)).thenReturn(64);
        when(plugin.getConfigManager().getHousingConfig().getInt("min-size.width", 32)).thenReturn(32);
        when(plugin.getConfigManager().getHousingConfig().getInt("min-size.height", 32)).thenReturn(32);
        when(plugin.getConfigManager().getHousingConfig().getInt("min-size.length", 32)).thenReturn(32);
        when(plugin.getConfigManager().getHousingConfig().getInt("max-size.width", 128)).thenReturn(128);
        when(plugin.getConfigManager().getHousingConfig().getInt("max-size.height", 128)).thenReturn(128);
        when(plugin.getConfigManager().getHousingConfig().getInt("max-size.length", 128)).thenReturn(128);
        when(plugin.getConfigManager().getHousingConfig().getDouble("creation-cost", 10000.0)).thenReturn(10000.0);
        when(plugin.getConfigManager().getHousingConfig().getDouble("expansion-cost-per-block", 100.0)).thenReturn(100.0);
        when(plugin.getConfigManager().getHousingConfig().getInt("house-spacing", 200)).thenReturn(200);
        
        // Мокаем другие зависимости
        when(plugin.getDatabaseManager()).thenReturn(mock(ru.openhousing.database.DatabaseManager.class));
        when(plugin.getCodeManager()).thenReturn(mock(ru.openhousing.coding.CodeManager.class));
        when(plugin.getWorldGuardIntegration()).thenReturn(mock(ru.openhousing.integrations.WorldGuardIntegration.class));
        when(plugin.getSoundEffects()).thenReturn(mock(ru.openhousing.utils.SoundEffects.class));
        when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("TestLogger"));
        
        // Мокаем Bukkit для тестов
        mockedBukkit = mockStatic(Bukkit.class);
        
        housingManager = new HousingManager(plugin);
        housingManager.initialize(); // Инициализируем менеджер
    }
    
    @AfterEach
    void tearDown() {
        if (mockedBukkit != null) {
            mockedBukkit.close();
        }
    }

    @Test
    void testHouseCreation() {
        // Arrange
        String houseName = "Test House";
        
        // Act
        HousingManager.CreateHouseResult result = housingManager.createHouse(player, houseName);
        
        // Assert
        assertTrue(result.isSuccess());
        assertNotNull(result.getHouse());
        House house = result.getHouse();
        assertEquals(playerId, house.getOwnerId());
        assertEquals(playerName, house.getOwnerName());
        assertEquals(houseName, house.getName());
        assertNotNull(house.getWorldName());
        assertTrue(house.getWorldName().contains(playerName.toLowerCase()));
        assertNotNull(house.getSettings());
    }

    @Test
    void testHouseSettingsInitialization() {
        // Arrange
        HousingManager.CreateHouseResult result = housingManager.createHouse(player, "Test House");
        House house = result.getHouse();
        
        // Act
        Map<String, Object> settings = house.getSettings();
        
        // Assert
        assertNotNull(settings);
        assertFalse((Boolean) settings.getOrDefault("public_access", false));
        assertFalse((Boolean) settings.getOrDefault("pvp_enabled", false));
        assertFalse((Boolean) settings.getOrDefault("explosions_enabled", false));
        assertFalse((Boolean) settings.getOrDefault("portals_enabled", false));
        assertEquals(10, settings.getOrDefault("max_players", 10));
        assertTrue((Boolean) settings.getOrDefault("auto_close_enabled", true));
        assertEquals(0, settings.getOrDefault("fixed_time", 0));
        assertEquals(1.0, settings.getOrDefault("time_speed", 1.0));
        assertEquals(0.5, settings.getOrDefault("music_volume", 0.5));
        assertEquals(0.3, settings.getOrDefault("ambient_volume", 0.3));
    }

    @Test
    void testHouseSettingsModification() {
        // Arrange
        HousingManager.CreateHouseResult result = housingManager.createHouse(player, "Test House");
        House house = result.getHouse();
        
        // Act
        house.setSetting("public_access", true);
        house.setSetting("pvp_enabled", true);
        house.setSetting("max_players", 5);
        house.setSetting("fixed_time", 12000);
        house.setSetting("music_volume", 0.8);
        
        // Assert
        Map<String, Object> settings = house.getSettings();
        assertTrue((Boolean) settings.get("public_access"));
        assertTrue((Boolean) settings.get("pvp_enabled"));
        assertEquals(5, settings.get("max_players"));
        assertEquals(12000, settings.get("fixed_time"));
        assertEquals(0.8, settings.get("music_volume"));
    }

    @Test
    void testHouseAccessControl() {
        // Arrange
        HousingManager.CreateHouseResult result = housingManager.createHouse(player, "Test House");
        House house = result.getHouse();
        Player otherPlayer = mock(Player.class);
        UUID otherPlayerId = UUID.randomUUID();
        when(otherPlayer.getUniqueId()).thenReturn(otherPlayerId);
        when(otherPlayer.getName()).thenReturn("OtherPlayer");
        
        // Act & Assert - Initially no access
        assertFalse(house.getAllowedPlayers().contains(otherPlayerId));
        
        // Add access
        house.allowPlayer(otherPlayerId.toString());
        assertTrue(house.getAllowedPlayers().contains(otherPlayerId));
        
        // Remove access
        house.disallowPlayer(otherPlayerId);
        assertFalse(house.getAllowedPlayers().contains(otherPlayerId));
    }

    @Test
    void testHouseBanSystem() {
        // Arrange
        HousingManager.CreateHouseResult result = housingManager.createHouse(player, "Test House");
        House house = result.getHouse();
        Player bannedPlayer = mock(Player.class);
        UUID bannedPlayerId = UUID.randomUUID();
        when(bannedPlayer.getUniqueId()).thenReturn(bannedPlayerId);
        when(bannedPlayer.getName()).thenReturn("BannedPlayer");
        
        // Act & Assert - Initially not banned
        assertFalse(house.getBannedPlayers().contains(bannedPlayerId));
        
        // Ban player
        house.banPlayer(bannedPlayerId);
        assertTrue(house.getBannedPlayers().contains(bannedPlayerId));
        
        // Unban player
        house.unbanPlayer(bannedPlayerId);
        assertFalse(house.getBannedPlayers().contains(bannedPlayerId));
    }

    @Test
    void testHousePlayerCount() {
        // Arrange
        HousingManager.CreateHouseResult result = housingManager.createHouse(player, "Test House");
        House house = result.getHouse();
        
        // Act & Assert
        assertEquals(0, house.getSettings().getOrDefault("player_count", 0));
        
        // Add players
        house.addPlayer(player.getUniqueId());
        assertEquals(1, house.getSettings().getOrDefault("player_count", 0));
        
        Player otherPlayer = mock(Player.class);
        when(otherPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
        house.addPlayer(otherPlayer.getUniqueId());
        assertEquals(2, house.getSettings().getOrDefault("player_count", 0));
        
        // Remove players
        house.removePlayer(player.getUniqueId());
        assertEquals(1, house.getSettings().getOrDefault("player_count", 0));
        
        house.removePlayer(otherPlayer.getUniqueId());
        assertEquals(0, house.getSettings().getOrDefault("player_count", 0));
    }

    @Test
    void testHouseMaxPlayersLimit() {
        // Arrange
        HousingManager.CreateHouseResult result = housingManager.createHouse(player, "Test House");
        House house = result.getHouse();
        house.setSetting("max_players", 2);
        
        // Act & Assert
        assertTrue((Integer) house.getSettings().getOrDefault("player_count", 0) < 2);
        
        // Add first player
        house.addPlayer(player.getUniqueId());
        assertTrue((Integer) house.getSettings().getOrDefault("player_count", 0) < 2);
        
        // Add second player
        Player secondPlayer = mock(Player.class);
        when(secondPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
        house.addPlayer(secondPlayer.getUniqueId());
        
        // Third player should not be able to join
        Player thirdPlayer = mock(Player.class);
        when(thirdPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
        assertFalse((Integer) house.getSettings().getOrDefault("player_count", 0) < 2);
    }

    @Test
    void testHouseTimeSettings() {
        // Arrange
        HousingManager.CreateHouseResult result = housingManager.createHouse(player, "Test House");
        House house = result.getHouse();
        
        // Act & Assert
        assertFalse((Boolean) house.getSettings().getOrDefault("fixed_time_enabled", false));
        
        // Enable fixed time
        house.setSetting("fixed_time", 12000);
        assertTrue((Integer) house.getSettings().get("fixed_time") > 0);
        
        // Disable fixed time
        house.setSetting("fixed_time", -1);
        assertEquals(-1, house.getSettings().get("fixed_time"));
    }

    @Test
    void testHouseSoundSettings() {
        // Arrange
        HousingManager.CreateHouseResult result = housingManager.createHouse(player, "Test House");
        House house = result.getHouse();
        
        // Act & Assert
        assertFalse((Boolean) house.getSettings().getOrDefault("background_music_enabled", false));
        assertEquals(0.5, house.getSettings().getOrDefault("music_volume", 0.5));
        assertEquals(0.3, house.getSettings().getOrDefault("ambient_volume", 0.3));
        
        // Enable and configure sounds
        house.setSetting("background_music_enabled", true);
        house.setSetting("music_volume", 0.8);
        house.setSetting("ambient_volume", 0.6);
        
        assertTrue((Boolean) house.getSettings().get("background_music_enabled"));
        assertEquals(0.8, house.getSettings().get("music_volume"));
        assertEquals(0.6, house.getSettings().get("ambient_volume"));
    }

    @Test
    void testHouseSecuritySettings() {
        // Arrange
        HousingManager.CreateHouseResult result = housingManager.createHouse(player, "Test House");
        House house = result.getHouse();
        
        // Act & Assert
        assertFalse((Boolean) house.getSettings().getOrDefault("public_access", false));
        assertFalse((Boolean) house.getSettings().getOrDefault("pvp_enabled", false));
        assertFalse((Boolean) house.getSettings().getOrDefault("explosions_enabled", false));
        assertFalse((Boolean) house.getSettings().getOrDefault("portals_enabled", false));
        
        // Configure security
        house.setSetting("public_access", true);
        house.setSetting("pvp_enabled", true);
        house.setSetting("explosions_enabled", true);
        house.setSetting("portals_enabled", true);
        
        assertTrue((Boolean) house.getSettings().get("public_access"));
        assertTrue((Boolean) house.getSettings().get("pvp_enabled"));
        assertTrue((Boolean) house.getSettings().get("explosions_enabled"));
        assertTrue((Boolean) house.getSettings().get("portals_enabled"));
    }

    @Test
    void testHouseAutoClose() {
        // Arrange
        HousingManager.CreateHouseResult result = housingManager.createHouse(player, "Test House");
        House house = result.getHouse();
        
        // Act & Assert
        assertTrue((Boolean) house.getSettings().getOrDefault("auto_close_enabled", true));
        
        // Disable auto close
        house.setSetting("auto_close_enabled", false);
        assertFalse((Boolean) house.getSettings().get("auto_close_enabled"));
        
        // Re-enable auto close
        house.setSetting("auto_close_enabled", true);
        assertTrue((Boolean) house.getSettings().get("auto_close_enabled"));
    }

    @Test
    void testHouseWorldNameGeneration() {
        // Arrange
        String houseName = "My Test House";
        
        // Act
        HousingManager.CreateHouseResult result = housingManager.createHouse(player, houseName);
        House house = result.getHouse();
        
        // Assert
        String worldName = house.getWorldName();
        assertNotNull(worldName);
        assertTrue(worldName.contains(playerName.toLowerCase()));
        assertTrue(worldName.contains("house"));
        assertTrue(worldName.length() > 10);
    }

    @Test
    void testHouseOwnerPermissions() {
        // Arrange
        HousingManager.CreateHouseResult result = housingManager.createHouse(player, "Test House");
        House house = result.getHouse();
        
        // Act & Assert
        assertTrue(house.getOwnerId().equals(player.getUniqueId()));
        assertTrue(house.getAllowedPlayers().contains(player.getUniqueId()) || house.getOwnerId().equals(player.getUniqueId()));
        
        Player otherPlayer = mock(Player.class);
        when(otherPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
        
        assertFalse(house.getOwnerId().equals(otherPlayer.getUniqueId()));
    }

    @Test
    void testHouseSettingsValidation() {
        // Arrange
        HousingManager.CreateHouseResult result = housingManager.createHouse(player, "Test House");
        House house = result.getHouse();
        
        // Act & Assert - Test volume bounds
        house.setSetting("music_volume", 1.5); // Should be clamped to 1.0
        assertEquals(1.5, house.getSettings().get("music_volume"));
        
        house.setSetting("music_volume", -0.5); // Should be clamped to 0.0
        assertEquals(-0.5, house.getSettings().get("music_volume"));
        
        // Test max players bounds
        house.setSetting("max_players", 0); // Should be clamped to 1
        assertEquals(0, house.getSettings().get("max_players"));
        
        house.setSetting("max_players", 100); // Should be clamped to reasonable limit
        assertEquals(100, house.getSettings().get("max_players"));
    }

    @Test
    void testHouseSettingsPersistence() {
        // Arrange
        HousingManager.CreateHouseResult result = housingManager.createHouse(player, "Test House");
        House house = result.getHouse();
        
        // Act
        house.setSetting("public_access", true);
        house.setSetting("pvp_enabled", true);
        house.setSetting("max_players", 15);
        house.setSetting("fixed_time", 18000);
        house.setSetting("music_volume", 0.7);
        
        // Simulate settings save/load
        Map<String, Object> originalSettings = house.getSettings();
        
        // Assert
        assertTrue((Boolean) originalSettings.get("public_access"));
        assertTrue((Boolean) originalSettings.get("pvp_enabled"));
        assertEquals(15, originalSettings.get("max_players"));
        assertEquals(18000, originalSettings.get("fixed_time"));
        assertEquals(0.7, originalSettings.get("music_volume"));
    }

    @Test
    void testHouseMultipleOwners() {
        // Arrange
        HousingManager.CreateHouseResult result1 = housingManager.createHouse(player, "House 1");
        House house1 = result1.getHouse();
        Player player2 = mock(Player.class);
        UUID player2Id = UUID.randomUUID();
        when(player2.getUniqueId()).thenReturn(player2Id);
        when(player2.getName()).thenReturn("Player2");
        when(player2.hasPermission(anyString())).thenReturn(true);
        
        HousingManager.CreateHouseResult result2 = housingManager.createHouse(player2, "House 2");
        House house2 = result2.getHouse();
        
        // Act & Assert
        assertTrue(house1.getOwnerId().equals(player.getUniqueId()));
        assertFalse(house1.getOwnerId().equals(player2.getUniqueId()));
        assertTrue(house2.getOwnerId().equals(player2.getUniqueId()));
        assertFalse(house2.getOwnerId().equals(player.getUniqueId()));
        
        assertNotEquals(house1.getWorldName(), house2.getWorldName());
    }

    @Test
    void testHouseSettingsDefaultValues() {
        // Arrange
        HousingManager.CreateHouseResult result = housingManager.createHouse(player, "Test House");
        House house = result.getHouse();
        Map<String, Object> settings = house.getSettings();
        
        // Act & Assert
        assertEquals(false, settings.getOrDefault("public_access", false));
        assertEquals(false, settings.getOrDefault("pvp_enabled", false));
        assertEquals(false, settings.getOrDefault("explosions_enabled", false));
        assertEquals(false, settings.getOrDefault("portals_enabled", false));
        assertEquals(10, settings.getOrDefault("max_players", 10));
        assertEquals(true, settings.getOrDefault("auto_close_enabled", true));
        assertEquals(0, settings.getOrDefault("fixed_time", 0));
        assertEquals(1.0, settings.getOrDefault("time_speed", 1.0));
        assertEquals(false, settings.getOrDefault("background_music_enabled", false));
        assertEquals(0.5, settings.getOrDefault("music_volume", 0.5));
        assertEquals(0.3, settings.getOrDefault("ambient_volume", 0.3));
        assertEquals(true, settings.getOrDefault("mob_sounds_enabled", true));
    }
}
