package ru.openhousing.commands;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.script.CodeLine;
import ru.openhousing.coding.script.CodeScript;
import ru.openhousing.housing.House;
import ru.openhousing.housing.HouseMode;
import ru.openhousing.utils.MessageUtil;

import java.util.List;

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
        boolean debugMode = plugin.getConfigManager().getMainConfig().getBoolean("general.debug", false);
        
        if (debugMode) plugin.getLogger().info("[DEBUG] Setting PLAY mode for house: " + house.getId() + ", player: " + player.getName());
        
        house.setMode(HouseMode.PLAY);
        plugin.getDatabaseManager().saveHouse(house);
        
        // КРИТИЧЕСКОЕ ИСПРАВЛЕНИЕ: Регистрируем код игрока в EventManager
        if (plugin.getCodeManager() != null) {
            if (debugMode) plugin.getLogger().info("[DEBUG] Getting script for player: " + player.getName());
            CodeScript script = plugin.getCodeManager().getScript(player);
            
            if (script != null && !script.isEmpty()) {
                if (debugMode) plugin.getLogger().info("[DEBUG] Registering script with EventManager for player: " + player.getName());
                plugin.getCodeManager().getEventManager().registerPlayer(player, script);
                
                // НАЧАЛО ИЗМЕНЕНИЙ: Выполняем стартовый блок GAME_START
                executeGameStartBlock(player, script);
                
                plugin.getLogger().info("[PLAY MODE] Зарегистрирован код игрока " + player.getName() + " с " + script.getBlockCount() + " блоками");
            } else {
                if (debugMode) plugin.getLogger().info("[DEBUG] No script found or script is empty for player: " + player.getName());
                plugin.getLogger().info("[PLAY MODE] У игрока " + player.getName() + " нет кода для регистрации");
            }
        } else {
            plugin.getLogger().warning("[WARNING] CodeManager is null in setPlayMode!");
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
     * Выполняет стартовый блок GAME_START из скрипта игрока
     */
    private void executeGameStartBlock(Player player, CodeScript script) {
        // Ищем блок GAME_START в скрипте
        List<CodeBlock> allBlocks = script.getAllBlocks();
        CodeBlock gameStartBlock = null;
        
        for (CodeBlock block : allBlocks) {
            if (block.getType() == BlockType.GAME_START) {
                gameStartBlock = block;
                break;
            }
        }
        
        // Если блок найден, выполняем его
        if (gameStartBlock != null) {
            try {
                // Создаем контекст выполнения
                CodeBlock.ExecutionContext context = new CodeBlock.ExecutionContext(player);
                
                // Передаем глобальные переменные в контекст
                context.getVariables().putAll(script.getGlobalVariables());
                
                // Выполняем блок
                MessageUtil.send(player, "&aЗапускаем ваш код...");
                CodeBlock.ExecutionResult result = gameStartBlock.execute(context);
                
                // Сохраняем измененные переменные обратно в скрипт
                script.getGlobalVariables().putAll(context.getVariables());
                
                if (result.isSuccess()) {
                    plugin.getLogger().info("[GAME START] Код игрока " + player.getName() + " успешно выполнен");
                } else {
                    plugin.getLogger().warning("[GAME START] Ошибка выполнения кода игрока " + player.getName() + ": " + result.getMessage());
                    MessageUtil.send(player, "&cОшибка выполнения кода: " + result.getMessage());
                }
            } catch (Exception e) {
                plugin.getLogger().severe("[GAME START] КРИТИЧЕСКАЯ ОШИБКА при выполнении кода игрока " + player.getName() + ": " + e.getMessage());
                e.printStackTrace();
                MessageUtil.send(player, "&cКритическая ошибка выполнения кода! Проверьте логи сервера.");
            }
        } else {
            plugin.getLogger().info("[GAME START] У игрока " + player.getName() + " не найден блок GAME_START");
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