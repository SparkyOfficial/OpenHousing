package ru.openhousing.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.script.CodeScript;
import ru.openhousing.utils.MessageUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Команда для работы с визуальным кодингом
 */
public class CodeCommand implements CommandExecutor, TabCompleter {
    
    private final OpenHousing plugin;
    
    public CodeCommand(OpenHousing plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.send(sender, "&cЭта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Проверка разрешений
        if (!player.hasPermission("openhousing.code.editor")) {
            MessageUtil.send(player, "&cУ вас нет разрешения на использование редактора кода!");
            return true;
        }
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "editor":
            case "edit":
                openEditor(player);
                break;
                
            case "execute":
            case "run":
                executeScript(player);
                break;
                
            case "toggle":
                toggleScript(player);
                break;
                
            case "clear":
                clearScript(player);
                break;
                
            case "info":
            case "stats":
                showScriptInfo(player);
                break;
                
            case "debug":
                openDebugger(player);
                break;
                
            case "share":
                shareScript(player, args);
                break;
                
            case "import":
                importScript(player, args);
                break;
                
            case "help":
            default:
                showHelp(player);
                break;
        }
        
        return true;
    }
    
    /**
     * Открытие редактора кода
     */
    private void openEditor(Player player) {
        plugin.getCodeManager().openCodeEditor(player);
        MessageUtil.send(player, "&aРедактор кода открыт!");
    }
    
    /**
     * Выполнение кода
     */
    private void executeScript(Player player) {
        if (!player.hasPermission("openhousing.code.execute")) {
            MessageUtil.send(player, "&cУ вас нет разрешения на выполнение кода!");
            return;
        }
        
        CodeScript script = plugin.getCodeManager().getScript(player);
        if (script == null || script.isEmpty()) {
            MessageUtil.send(player, "&cВаш код пуст! Откройте редактор: &e/code editor");
            return;
        }
        
        if (!script.isEnabled()) {
            MessageUtil.send(player, "&cВаш код отключен! Включите его в настройках.");
            return;
        }
        
        // Проверка на ошибки
        List<String> errors = script.validate();
        if (!errors.isEmpty()) {
            MessageUtil.send(player, "&cВ вашем коде есть ошибки:");
            for (String error : errors) {
                MessageUtil.send(player, "&7- &c" + error);
            }
            return;
        }
        
        MessageUtil.send(player, "&cКоманда устарела! Код теперь выполняется автоматически в режиме &e/play&c.");
        MessageUtil.send(player, "&7Используйте &e/play &7в своем доме для активации кода.");
    }
    
    /**
     * Переключение состояния кода
     */
    private void toggleScript(Player player) {
        CodeScript script = plugin.getCodeManager().getOrCreateScript(player);
        script.setEnabled(!script.isEnabled());
        
        plugin.getCodeManager().saveScript(player, script);
        
        MessageUtil.send(player, script.isEnabled() ? 
            "&aКод включен!" : "&cКод отключен!");
    }
    
    /**
     * Очистка кода
     */
    private void clearScript(Player player) {
        CodeScript script = plugin.getCodeManager().getScript(player);
        if (script == null || script.isEmpty()) {
            MessageUtil.send(player, "&cВаш код уже пуст!");
            return;
        }
        
        script.clear();
        plugin.getCodeManager().saveScript(player, script);
        
        MessageUtil.send(player, "&cКод очищен!");
    }
    
    /**
     * Показ информации о коде
     */
    private void showScriptInfo(Player player) {
        CodeScript script = plugin.getCodeManager().getScript(player);
        if (script == null) {
            MessageUtil.send(player, "&cУ вас нет кода! Создайте его: &e/code editor");
            return;
        }
        
        CodeScript.ScriptStats stats = script.getStats();
        
        MessageUtil.send(player, 
            "&6&l=== Информация о коде ===",
            "&7Статус: " + (script.isEnabled() ? "&aВключен" : "&cОтключен"),
            "&7Всего блоков: &f" + stats.getTotalBlocks(),
            "&7События: &f" + stats.getEventBlocks(),
            "&7Условия: &f" + stats.getConditionBlocks(),
            "&7Действия: &f" + stats.getActionBlocks(),
            "&7Функции: &f" + stats.getFunctionCount(),
            "&7Переменные: &f" + stats.getVariableCount(),
            "&7Последнее изменение: &f" + MessageUtil.formatTime(System.currentTimeMillis() - script.getLastModified()) + " назад",
            ""
        );
        
        if (stats.hasErrors()) {
            MessageUtil.send(player, "&cВ коде есть ошибки! Проверьте в редакторе.");
        } else {
            MessageUtil.send(player, "&aКод готов к выполнению!");
        }
    }
    
    /**
     * Поделиться кодом
     */
    private void shareScript(Player player, String[] args) {
        if (!player.hasPermission("openhousing.code.share")) {
            MessageUtil.send(player, "&cУ вас нет разрешения на публикацию кода!");
            return;
        }
        
        CodeScript script = plugin.getCodeManager().getScript(player);
        if (script == null || script.isEmpty()) {
            MessageUtil.send(player, "&cВаш код пуст!");
            return;
        }
        
        // Здесь можно реализовать систему публикации кода
        // Например, сохранение в базу данных с уникальным кодом
        
        MessageUtil.send(player, "&aФункция поделиться кодом будет добавлена в будущих обновлениях!");
    }
    
    /**
     * Импорт кода
     */
    private void importScript(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtil.send(player, "&cИспользование: &e/code import <код>");
            return;
        }
        
        String importCode = args[1];
        
        // Здесь можно реализовать систему импорта кода
        // Например, загрузка из базы данных по коду
        
        MessageUtil.send(player, "&aФункция импорта кода будет добавлена в будущих обновлениях!");
    }
    
    /**
     * Открытие отладчика кода
     */
    private void openDebugger(Player player) {
        plugin.getCodeManager().openCodeDebugger(player);
        MessageUtil.send(player, "&aОтладчик кода открыт!");
    }
    
    /**
     * Показ справки
     */
    private void showHelp(Player player) {
        MessageUtil.send(player,
            "&6&l=== OpenHousing Code - Справка ===",
            "&e/code editor &7- Открыть редактор кода",
            "&c/code execute &7- УСТАРЕЛО! Используйте /play",
            "&e/code toggle &7- Включить/выключить код",
            "&e/code clear &7- Очистить код",
            "&e/code info &7- Информация о коде",
            "&e/code share &7- Поделиться кодом",
            "&e/code import <код> &7- Импортировать код",
            "&e/code help &7- Показать эту справку",
            ""
        );
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        Player player = (Player) sender;
        
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();
            
            List<String> subCommands = Arrays.asList(
                "editor", "execute", "toggle", "clear", "info", "share", "import", "help"
            );
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    // Проверяем разрешения
                    switch (subCommand) {
                        case "editor":
                            if (player.hasPermission("openhousing.code.editor")) {
                                completions.add(subCommand);
                            }
                            break;
                        case "execute":
                            if (player.hasPermission("openhousing.code.execute")) {
                                completions.add(subCommand);
                            }
                            break;
                        case "share":
                            if (player.hasPermission("openhousing.code.share")) {
                                completions.add(subCommand);
                            }
                            break;
                        default:
                            completions.add(subCommand);
                            break;
                    }
                }
            }
            
            return completions;
        }
        
        return new ArrayList<>();
    }
}
