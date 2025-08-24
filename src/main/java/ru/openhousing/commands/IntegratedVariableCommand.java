package ru.openhousing.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.BlockVariableManager;
import ru.openhousing.coding.blocks.BlockVariable;
import ru.openhousing.coding.blocks.BlockVariableAdapter;
import ru.openhousing.coding.variables.VariableType;
import ru.openhousing.coding.variables.DynamicVariable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Интегрированная команда для работы с переменными
 * Объединяет существующую систему переменных с новой системой "переменных в руке"
 */
public class IntegratedVariableCommand implements CommandExecutor, TabCompleter {

    private final OpenHousing plugin;
    private final BlockVariableManager variableManager;

    public IntegratedVariableCommand(OpenHousing plugin, BlockVariableManager variableManager) {
        this.plugin = plugin;
        this.variableManager = variableManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "give":
                if (args.length < 2) {
                    player.sendMessage("§cИспользование: /var give <имя_переменной>");
                    return true;
                }
                giveVariable(player, args[1]);
                break;
            case "list":
                showAllVariables(player);
                break;
            case "create":
                if (args.length < 4) {
                    player.sendMessage("§cИспользование: /var create <имя> <описание> <тип> [значение]");
                    return true;
                }
                createVariable(player, args);
                break;
            case "existing":
                showExistingVariables(player);
                break;
            case "convert":
                if (args.length < 2) {
                    player.sendMessage("§cИспользование: /var convert <имя_существующей_переменной>");
                    return true;
                }
                convertExistingVariable(player, args[1]);
                break;
            case "sync":
                syncAllVariables(player);
                break;
            case "help":
                showHelp(player);
                break;
            default:
                player.sendMessage("§cНеизвестная подкоманда. Используйте /var help");
                break;
        }

        return true;
    }

    private void giveVariable(Player player, String variableName) {
        // Сначала проверяем новую систему
        BlockVariable variable = variableManager.getGlobalVariable(variableName);
        if (variable != null) {
            variableManager.giveVariableToPlayer(player, variableName);
            return;
        }

        // Затем проверяем существующую систему через адаптер
        BlockVariable existingVar = BlockVariableAdapter.getVariableByName(variableName);
        if (existingVar != null) {
            player.getInventory().addItem(BlockVariableAdapter.createEnhancedItemStack(existingVar));
            player.sendMessage("§a✓ Вы получили переменную '" + variableName + "' из существующей системы!");
            player.sendMessage("§7Эта переменная интегрирована с VAR_SET, VAR_ADD и другими блоками");
            return;
        }

        player.sendMessage("§c✗ Переменная '" + variableName + "' не найдена ни в одной системе");
        player.sendMessage("§7Используйте /var list для просмотра доступных переменных");
    }

    private void showAllVariables(Player player) {
        player.sendMessage("§6=== Все доступные переменные ===");
        
        // Новые переменные
        player.sendMessage("§eНовые переменные (drag-n-drop):");
        List<BlockVariable> newVars = BlockVariableAdapter.getAllAvailableVariables();
        if (newVars.isEmpty()) {
            player.sendMessage("§7• Нет новых переменных");
        } else {
            for (BlockVariable var : newVars) {
                player.sendMessage(String.format("§7• %s §7(%s): %s", 
                    var.getName(), 
                    var.getType().getDisplayName(),
                    formatVariableValue(var)));
            }
        }
        
        player.sendMessage("");
        player.sendMessage("§eСуществующие переменные:");
        player.sendMessage("§7• VAR_SET, VAR_ADD, VAR_MULTIPLY и др.");
        player.sendMessage("§7• Динамические переменные с плейсхолдерами");
        player.sendMessage("§7• Системные переменные (player_name, world_name)");
        
        player.sendMessage("");
        player.sendMessage("§7Используйте: /var give <имя> для получения переменной");
        player.sendMessage("§7Или: /var convert <имя> для конвертации существующей");
    }

    private void showExistingVariables(Player player) {
        player.sendMessage("§6=== Существующая система переменных ===");
        player.sendMessage("§eДействия с переменными:");
        player.sendMessage("§7• VAR_SET - Установить значение");
        player.sendMessage("§7• VAR_ADD - Прибавить к переменной");
        player.sendMessage("§7• VAR_MULTIPLY - Умножить переменную");
        player.sendMessage("§7• VAR_APPEND_TEXT - Добавить текст");
        player.sendMessage("§7• VAR_SAVE - Сохранить навсегда");
        
        player.sendMessage("");
        player.sendMessage("§eТипы переменных:");
        for (VariableType type : VariableType.values()) {
            player.sendMessage(String.format("§7• %s - %s", 
                type.name(), type.getDisplayName()));
        }
        
        player.sendMessage("");
        player.sendMessage("§7Эти переменные работают в блоках кода");
        player.sendMessage("§7Используйте /var convert для создания drag-n-drop версии");
    }

    private void createVariable(Player player, String[] args) {
        String name = args[1];
        String description = args[2];
        String typeStr = args[3].toUpperCase();
        String defaultValue = args.length > 4 ? args[4] : null;

        try {
            BlockVariable.VariableType type = BlockVariable.VariableType.valueOf(typeStr);
            Object value = parseDefaultValue(type, defaultValue);
            
            BlockVariable variable = variableManager.createBlockVariable(name, description, type, value);
            player.sendMessage("§a✓ Переменная '" + name + "' создана!");
            player.sendMessage("§7Тип: " + type.getDisplayName());
            player.sendMessage("§7Описание: " + description);
            player.sendMessage("§7Интеграция: Автоматически доступна в VAR_SET блоках");
            
            // Автоматически выдаем переменную игроку
            variableManager.giveVariableToPlayer(player, name);
            
        } catch (IllegalArgumentException e) {
            player.sendMessage("§c✗ Неверный тип переменной: " + typeStr);
            player.sendMessage("§7Доступные типы:");
            for (BlockVariable.VariableType type : BlockVariable.VariableType.values()) {
                player.sendMessage("§7• " + type.name() + " - " + type.getDisplayName());
            }
        }
    }

    private void convertExistingVariable(Player player, String variableName) {
        // Создаем переменную по умолчанию на основе имени
        BlockVariable.VariableType type = BlockVariable.VariableType.STRING;
        String description = "Конвертированная переменная";
        
        if (variableName.contains("count") || variableName.contains("number")) {
            type = BlockVariable.VariableType.INTEGER;
            description = "Счетчик";
        } else if (variableName.contains("location") || variableName.contains("pos")) {
            type = BlockVariable.VariableType.LOCATION;
            description = "Координаты";
        } else if (variableName.contains("enabled") || variableName.contains("active")) {
            type = BlockVariable.VariableType.BOOLEAN;
            description = "Флаг состояния";
        }
        
        BlockVariable variable = BlockVariableAdapter.createDefaultVariable(variableName, description, type);
        
        player.getInventory().addItem(BlockVariableAdapter.createEnhancedItemStack(variable));
        player.sendMessage("§a✓ Переменная '" + variableName + "' конвертирована!");
        player.sendMessage("§7Теперь вы можете использовать её как drag-n-drop переменную");
        player.sendMessage("§7И она автоматически синхронизируется с VAR_SET блоками");
    }

    private void syncAllVariables(Player player) {
        int count = BlockVariableAdapter.getCachedVariableCount();
        BlockVariableAdapter.clearCache();
        
        player.sendMessage("§a✓ Кэш переменных очищен!");
        player.sendMessage("§7Переменные будут пересозданы при следующем использовании");
        player.sendMessage("§7Синхронизация с существующей системой активна");
    }

    private Object parseDefaultValue(BlockVariable.VariableType type, String value) {
        if (value == null) {
            switch (type) {
                case BOOLEAN: return false;
                case INTEGER: return 0;
                case DOUBLE: return 0.0;
                case STRING: return "";
                case LIST: return new ArrayList<>();
                case LOCATION: return "0,0,0,world";
                case PLAYER: return "";
                case WORLD: return "world";
                case PERMISSION: return "";
                case GROUP: return "";
                default: return null;
            }
        }

        try {
            switch (type) {
                case BOOLEAN:
                    return Boolean.parseBoolean(value);
                case INTEGER:
                    return Integer.parseInt(value);
                case DOUBLE:
                    return Double.parseDouble(value);
                case STRING:
                case PLAYER:
                case WORLD:
                case PERMISSION:
                case GROUP:
                    return value;
                case LIST:
                    return Arrays.asList(value.split(","));
                case LOCATION:
                    return value;
                default:
                    return value;
            }
        } catch (Exception e) {
            return value;
        }
    }

    private String formatVariableValue(BlockVariable variable) {
        Object value = variable.getValue();
        if (value == null) return "§cНе задано";
        
        switch (variable.getType()) {
            case BOOLEAN:
                return (Boolean) value ? "§aВключено" : "§cВыключено";
            case INTEGER:
            case DOUBLE:
                return "§e" + value.toString();
            case STRING:
                return "§f" + value.toString();
            case LIST:
                if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    return "§f" + String.join(", ", list.stream().map(Object::toString).toArray(String[]::new));
                }
                return "§f" + value.toString();
            default:
                return "§f" + value.toString();
        }
    }

    private void showHelp(Player player) {
        player.sendMessage("§6=== Помощь по команде IntegratedVariable ===");
        player.sendMessage("§e/var give <имя> - Получить переменную (из любой системы)");
        player.sendMessage("§e/var list - Список всех доступных переменных");
        player.sendMessage("§e/var create <имя> <описание> <тип> [значение] - Создать новую переменную");
        player.sendMessage("§e/var existing - Показать существующую систему переменных");
        player.sendMessage("§e/var convert <имя> - Конвертировать существующую переменную");
        player.sendMessage("§e/var sync - Синхронизировать все переменные");
        player.sendMessage("§e/var help - Показать эту справку");
        player.sendMessage("");
        player.sendMessage("§7Интеграция:");
        player.sendMessage("§7• Новые переменные автоматически доступны в VAR_SET блоках");
        player.sendMessage("§7• Существующие переменные можно конвертировать в drag-n-drop");
        player.sendMessage("§7• Полная совместимость с обеими системами");
        player.sendMessage("");
        player.sendMessage("§7Примеры:");
        player.sendMessage("§7• /var give player_name");
        player.sendMessage("§7• /var create cooldown \"Задержка\" INTEGER 1000");
        player.sendMessage("§7• /var convert kill_count");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("give", "list", "create", "existing", "convert", "sync", "help"));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if ("give".equals(subCommand) || "convert".equals(subCommand)) {
                // Получаем список всех доступных переменных
                List<BlockVariable> allVars = BlockVariableAdapter.getAllAvailableVariables();
                for (BlockVariable var : allVars) {
                    completions.add(var.getName());
                }
                // Добавляем системные переменные
                completions.addAll(Arrays.asList("player_name", "world_name", "online_players", "server_tps"));
            } else if ("create".equals(subCommand)) {
                completions.addAll(Arrays.asList("имя_переменной"));
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            if ("create".equals(subCommand)) {
                completions.addAll(Arrays.asList("описание_переменной"));
            }
        } else if (args.length == 4) {
            String subCommand = args[0].toLowerCase();
            if ("create".equals(subCommand)) {
                // Доступные типы переменных
                for (BlockVariable.VariableType type : BlockVariable.VariableType.values()) {
                    completions.add(type.name());
                }
            }
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
