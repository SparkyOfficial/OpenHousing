package ru.openhousing.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.script.CodeScript;

/**
 * Листенер для обработки событий кода
 */
public class CodeListener implements Listener {
    
    private final OpenHousing plugin;
    
    public CodeListener(OpenHousing plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Асинхронно загружаем код игрока
        plugin.getDatabaseManager().loadCodeScriptAsync(player.getUniqueId(), script -> {
            if (script != null) {
                // Регистрируем обработчики событий в основном потоке
                plugin.getCodeManager().registerScript(player, script);
            }
        });
        
        // Обрабатываем событие в коде
        plugin.getCodeManager().handleEvent(event, player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Обрабатываем событие в коде
        plugin.getCodeManager().handleEvent(event, player);
        
        // Очищаем данные игрока
        plugin.getCodeManager().onPlayerQuit(player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        plugin.getCodeManager().handleEvent(event, player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Проверяем, действительно ли игрок переместился
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
            event.getFrom().getBlockY() != event.getTo().getBlockY() ||
            event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            
            plugin.getCodeManager().handleEvent(event, player);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        plugin.getCodeManager().handleEvent(event, player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        plugin.getCodeManager().handleEvent(event, player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            plugin.getCodeManager().handleEvent(event, player);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        plugin.getCodeManager().handleEvent(event, player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        plugin.getCodeManager().handleEvent(event, player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        plugin.getCodeManager().handleEvent(event, player);
    }
    
    // Удален InventoryClickEvent из CodeListener для избежания конфликтов
    // Все GUI события обрабатываются в InventoryListener
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        plugin.getCodeManager().handleEvent(event, player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        plugin.getCodeManager().handleEvent(event, player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        plugin.getCodeManager().handleEvent(event, player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        plugin.getCodeManager().handleEvent(event, player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        plugin.getCodeManager().handleEvent(event, player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        plugin.getCodeManager().handleEvent(event, player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        plugin.getCodeManager().handleEvent(event, player);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        plugin.getCodeManager().handleEvent(event, player);
    }
}
