package ru.openhousing.integrations;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.openhousing.OpenHousing;
import ru.openhousing.housing.House;

import java.util.UUID;

/**
 * Интеграция с WorldGuard для защиты домов
 */
public class WorldGuardIntegration {
    
    private final OpenHousing plugin;
    private boolean enabled = false;
    
    public WorldGuardIntegration(OpenHousing plugin) {
        this.plugin = plugin;
        initializeIntegration();
    }
    
    /**
     * Инициализация интеграции
     */
    private void initializeIntegration() {
        try {
            if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
                enabled = true;
                plugin.getLogger().info("WorldGuard integration enabled!");
            } else {
                plugin.getLogger().info("WorldGuard not found, region protection disabled");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize WorldGuard integration: " + e.getMessage());
            enabled = false;
        }
    }
    
    /**
     * Проверка активности интеграции
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Создание региона для дома
     */
    public boolean createHouseRegion(House house) {
        if (!enabled) return false;
        
        try {
            World world = house.getWorld();
            if (world == null) return false;
            
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
            
            if (regionManager == null) return false;
            
            // Получаем размеры дома
            House.HouseSize size = house.getSize();
            int sizeValue = size.getSize();
            
            // Создаем регион вокруг спавна дома
            Location spawnLocation = house.getSpawnLocation();
            if (spawnLocation == null) {
                spawnLocation = new Location(world, 0, 64, 0);
            }
            
            int halfSize = sizeValue / 2;
            BlockVector3 min = BlockVector3.at(
                spawnLocation.getBlockX() - halfSize,
                0, // От нижнего предела мира
                spawnLocation.getBlockZ() - halfSize
            );
            BlockVector3 max = BlockVector3.at(
                spawnLocation.getBlockX() + halfSize,
                world.getMaxHeight(), // До верхнего предела мира
                spawnLocation.getBlockZ() + halfSize
            );
            
            String regionId = "house_" + house.getId();
            ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionId, min, max);
            
            // Настройка флагов региона
            setupRegionFlags(region, house);
            
            // Добавление региона
            regionManager.addRegion(region);
            
            plugin.getLogger().info("Created WorldGuard region for house " + house.getId() + 
                " (" + house.getName() + ") with size " + sizeValue);
            
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create WorldGuard region for house " + 
                house.getId() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Настройка флагов региона
     */
    private void setupRegionFlags(ProtectedRegion region, House house) {
        try {
            // Владелец дома
            region.getOwners().addPlayer(house.getOwnerId());
            
            // Разрешенные игроки
            for (UUID allowedPlayer : house.getAllowedPlayers()) {
                region.getMembers().addPlayer(allowedPlayer);
            }
            
            // Настройки безопасности из дома
            Object pvpObj = house.getSetting("pvp_allowed");
            boolean pvpAllowed = pvpObj instanceof Boolean ? (Boolean) pvpObj : false;
            region.setFlag(Flags.PVP, pvpAllowed ? com.sk89q.worldguard.protection.flags.StateFlag.State.ALLOW : 
                com.sk89q.worldguard.protection.flags.StateFlag.State.DENY);
            
            Object explosionsObj = house.getSetting("explosions_allowed");
            boolean explosionsAllowed = explosionsObj instanceof Boolean ? (Boolean) explosionsObj : false;
            region.setFlag(Flags.TNT, explosionsAllowed ? com.sk89q.worldguard.protection.flags.StateFlag.State.ALLOW : 
                com.sk89q.worldguard.protection.flags.StateFlag.State.DENY);
            region.setFlag(Flags.CREEPER_EXPLOSION, explosionsAllowed ? com.sk89q.worldguard.protection.flags.StateFlag.State.ALLOW : 
                com.sk89q.worldguard.protection.flags.StateFlag.State.DENY);
            
            // Запрет на вход для заблокированных игроков
            if (!house.getBannedPlayers().isEmpty()) {
                region.setFlag(Flags.ENTRY, com.sk89q.worldguard.protection.flags.StateFlag.State.DENY);
                // Разрешаем вход только владельцу и участникам
                region.setFlag(Flags.ENTRY.getRegionGroupFlag(), com.sk89q.worldguard.protection.flags.RegionGroup.MEMBERS);
            }
            
            // Общие флаги для защиты дома
            region.setFlag(Flags.BUILD, com.sk89q.worldguard.protection.flags.StateFlag.State.DENY);
            region.setFlag(Flags.INTERACT, com.sk89q.worldguard.protection.flags.StateFlag.State.DENY);
            region.setFlag(Flags.USE, com.sk89q.worldguard.protection.flags.StateFlag.State.DENY);
            
            // Разрешаем владельцу и участникам
            region.setFlag(Flags.BUILD.getRegionGroupFlag(), com.sk89q.worldguard.protection.flags.RegionGroup.MEMBERS);
            region.setFlag(Flags.INTERACT.getRegionGroupFlag(), com.sk89q.worldguard.protection.flags.RegionGroup.MEMBERS);
            region.setFlag(Flags.USE.getRegionGroupFlag(), com.sk89q.worldguard.protection.flags.RegionGroup.MEMBERS);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to setup region flags: " + e.getMessage());
        }
    }
    
    /**
     * Обновление региона дома
     */
    public boolean updateHouseRegion(House house) {
        if (!enabled) return false;
        
        try {
            // Удаляем старый регион
            deleteHouseRegion(house);
            
            // Создаем новый
            return createHouseRegion(house);
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to update WorldGuard region for house " + 
                house.getId() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Удаление региона дома
     */
    public boolean deleteHouseRegion(House house) {
        if (!enabled) return false;
        
        try {
            World world = house.getWorld();
            if (world == null) return false;
            
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
            
            if (regionManager == null) return false;
            
            String regionId = "house_" + house.getId();
            ProtectedRegion region = regionManager.removeRegion(regionId);
            
            if (region != null) {
                plugin.getLogger().info("Removed WorldGuard region for house " + house.getId());
                return true;
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to remove WorldGuard region for house " + 
                house.getId() + ": " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Проверка разрешений в регионе
     */
    public boolean canPlayerBuild(Player player, Location location) {
        if (!enabled) return true; // Если WorldGuard отключен, разрешаем
        
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = container.get(BukkitAdapter.adapt(location.getWorld()));
            
            if (regionManager == null) return true;
            
            ApplicableRegionSet regions = regionManager.getApplicableRegions(
                BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ())
            );
            
            return regions.testState(WorldGuard.getInstance().getPlatform().getSessionManager()
                .get(BukkitAdapter.adapt(player)), Flags.BUILD);
                
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check build permission: " + e.getMessage());
            return true; // В случае ошибки разрешаем
        }
    }
    
    /**
     * Проверка входа в регион
     */
    public boolean canPlayerEnter(Player player, Location location) {
        if (!enabled) return true;
        
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = container.get(BukkitAdapter.adapt(location.getWorld()));
            
            if (regionManager == null) return true;
            
            ApplicableRegionSet regions = regionManager.getApplicableRegions(
                BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ())
            );
            
            return regions.testState(WorldGuard.getInstance().getPlatform().getSessionManager()
                .get(BukkitAdapter.adapt(player)), Flags.ENTRY);
                
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check entry permission: " + e.getMessage());
            return true;
        }
    }
    
    /**
     * Получение дома по региону
     */
    public House getHouseByRegion(String regionId) {
        if (!regionId.startsWith("house_")) return null;
        
        try {
            int houseId = Integer.parseInt(regionId.substring(6));
            return plugin.getHousingManager().getHouseById(houseId);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}