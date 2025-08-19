package ru.openhousing.coding.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.CodeManager;

/**
 * Слушатель событий для системы визуального кодинга
 * Перехватывает события Bukkit и передает их в EventManager через CodeManager
 */
public class CodeListener implements Listener {
    
    private final OpenHousing plugin;
    private final CodeManager codeManager;
    
    public CodeListener(OpenHousing plugin, CodeManager codeManager) {
        this.plugin = plugin;
        this.codeManager = codeManager;
    }
    
    // ========== СОБЫТИЯ ИГРОКА ==========
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        codeManager.handleEvent(event, player);
        
        // Загружаем скрипт игрока при подключении
        codeManager.loadScript(player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        codeManager.handleEvent(event, player);
        
        // Очищаем данные игрока при отключении
        codeManager.onPlayerQuit(player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        codeManager.handleEvent(event, event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Проверяем, действительно ли игрок переместился (не только повернул голову)
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
            event.getFrom().getBlockY() != event.getTo().getBlockY() ||
            event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            codeManager.handleEvent(event, event.getPlayer());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        codeManager.handleEvent(event, event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        codeManager.handleEvent(event, event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        codeManager.handleEvent(event, event.getEntity());
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        codeManager.handleEvent(event, event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        codeManager.handleEvent(event, event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        codeManager.handleEvent(event, event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        codeManager.handleEvent(event, event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        codeManager.handleEvent(event, event.getPlayer());
    }
    
    // ========== СОБЫТИЯ БЛОКОВ ==========
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        codeManager.handleEvent(event, event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        codeManager.handleEvent(event, event.getPlayer());
    }
    
    // ========== СОБЫТИЯ ИНВЕНТАРЯ ==========
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            codeManager.handleEvent(event, (Player) event.getWhoClicked());
        }
    }
    
    // ========== СОБЫТИЯ СУЩНОСТЕЙ ==========
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        // Для событий сущностей передаем всем онлайн игрокам
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            codeManager.handleEvent(event, player);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        // Для событий сущностей передаем всем онлайн игрокам
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            codeManager.handleEvent(event, player);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        // Для событий сущностей передаем всем онлайн игрокам
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            codeManager.handleEvent(event, player);
        }
    }
}
