package ru.openhousing.commands;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.openhousing.OpenHousing;
import ru.openhousing.housing.House;
import ru.openhousing.housing.HouseMode;
import ru.openhousing.utils.MessageUtil;

/**
 * Команды для переключения режимов дома
 */
public class HouseModeCommand implements CommandExecutor {
    
    private final OpenHousing plugin;
    
    public HouseModeCommand(OpenHousing plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.send(sender, "&cЭта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        House house = plugin.getHousingManager().getHouseAt(player.getLocation());
        
        if (house == null) {
            MessageUtil.send(player, "&cВы не находитесь в доме!");
            return true;
        }
        
        // Проверяем права
        if (!house.getOwnerId().equals(player.getUniqueId()) && 
            !player.hasPermission("openhousing.admin.mode")) {
            MessageUtil.send(player, "&cТолько владелец дома может менять режим!");
            return true;
        }
        
        String commandName = label.toLowerCase();
        
        switch (commandName) {
            case "play":
                setPlayMode(player, house);
                break;
            case "build":
                setBuildMode(player, house);
                break;
        }
        
        return true;
    }
    
    /**
     * Установка режима игры
     */
    private void setPlayMode(Player player, House house) {
        house.setMode(HouseMode.PLAY);
        plugin.getDatabaseManager().saveHouse(house);
        
        // КРИТИЧЕСКОЕ ИСПРАВЛЕНИЕ: Регистрируем код игрока в EventManager
        if (plugin.getCodeManager() != null) {
            ru.openhousing.coding.script.CodeScript script = plugin.getCodeManager().getPlayerScript(player.getUniqueId());
            if (script != null && !script.isEmpty()) {
                plugin.getCodeManager().getEventManager().registerPlayerScript(player, script);
                plugin.getLogger().info("[PLAY MODE] Зарегистрирован код игрока " + player.getName() + " с " + script.getBlockCount() + " блоками");
            } else {
                plugin.getLogger().info("[PLAY MODE] У игрока " + player.getName() + " нет кода для регистрации");
            }
        }
        
        // Устанавливаем игровой режим для всех игроков в доме
        for (Player housePlayer : house.getPlayersInside()) {
            if (housePlayer.getGameMode() != GameMode.SPECTATOR) {
                housePlayer.setGameMode(GameMode.ADVENTURE);
            }
        }
        
        MessageUtil.send(player, "&aРежим игры активирован!");
        MessageUtil.send(player, "&7Код теперь работает, игровой режим: Приключение");
        
        // Уведомляем других игроков в доме
        for (Player housePlayer : house.getPlayersInside()) {
            if (!housePlayer.equals(player)) {
                MessageUtil.send(housePlayer, "&eРежим дома изменен на: &aИгра");
                MessageUtil.send(housePlayer, "&7Код теперь активен!");
            }
        }
    }
    
    /**
     * Установка режима строительства
     */
    private void setBuildMode(Player player, House house) {
        house.setMode(HouseMode.BUILD);
        plugin.getDatabaseManager().saveHouse(house);
        
        // Устанавливаем игровой режим
        for (Player housePlayer : house.getPlayersInside()) {
            if (housePlayer.equals(player)) {
                // Владелец получает креатив
                if (housePlayer.getGameMode() != GameMode.SPECTATOR) {
                    housePlayer.setGameMode(GameMode.CREATIVE);
                }
            } else {
                // Остальные получают приключение
                if (housePlayer.getGameMode() != GameMode.SPECTATOR) {
                    housePlayer.setGameMode(GameMode.ADVENTURE);
                }
            }
        }
        
        MessageUtil.send(player, "&6Режим строительства активирован!");
        MessageUtil.send(player, "&7Код отключен, игровой режим: Креатив");
        
        // Уведомляем других игроков в доме
        for (Player housePlayer : house.getPlayersInside()) {
            if (!housePlayer.equals(player)) {
                MessageUtil.send(housePlayer, "&eРежим дома изменен на: &6Строительство");
                MessageUtil.send(housePlayer, "&7Код отключен!");
            }
        }
    }
}
