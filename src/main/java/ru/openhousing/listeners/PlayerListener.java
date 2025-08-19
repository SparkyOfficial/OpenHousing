package ru.openhousing.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.openhousing.OpenHousing;

/**
 * Основной листенер для игроков
 */
public class PlayerListener implements Listener {
    
    private final OpenHousing plugin;
    
    public PlayerListener(OpenHousing plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Загружаем данные игрока
        plugin.getCodeManager().loadScript(player);
        
        // Приветственное сообщение (если первый заход)
        if (!player.hasPlayedBefore()) {
            player.sendMessage("§6Добро пожаловать в OpenHousing!");
            player.sendMessage("§7Используйте §e/code editor §7для создания кода!");
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Сохраняем данные игрока
        plugin.getCodeManager().onPlayerQuit(player);
    }
}
