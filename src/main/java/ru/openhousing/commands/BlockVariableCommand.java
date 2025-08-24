package ru.openhousing.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.BlockVariableManager;
import ru.openhousing.coding.blocks.BlockVariable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Команда для управления переменными блоков
 */
public class BlockVariableCommand implements CommandExecutor, TabCompleter {

    private final OpenHousing plugin;
    private final BlockVariableManager variableManager;

    public BlockVariableCommand(OpenHousing plugin, BlockVariableManager variableManager) {
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
                    player.sendMessage("§cИспользование: /blockvar give <имя_переменной>");
                    return true;
                }
                variableManager.giveVariableToPlayer(player, args[1]);
                break;
            case "list":
                variableManager.showAvailableVariables(player);
                break;
            case "create":
                if (args.length < 4) {
                    player.sendMessage("§cИспользование: /blockvar create <имя> <описание> <тип> [значение]");
                    return true;
                }
                createVariable(player, args);
                break;
            case "remove":
                if (args.length < 2) {
                    player.sendMessage("§cИспользование: /blockvar remove <имя_переменной>");
                    return true;
                }
                removeVariable(player, args[1]);
                break;
            case "info":
                if (args.length < 2) {
                    player.sendMessage("§cИспользование: /blockvar info <имя_переменной>");
                    return true;
                }
                showVariableInfo(player, args[1]);
                break;
            case "help":
                showHelp(player);
                break;
            default:
                player.sendMessage("§cНеизвестная подкоманда. Используйте /blockvar help");
                break;
        }

        return true;
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
            return value; // Возвращаем как есть, если не удалось распарсить
        }
    }

    private void removeVariable(Player player, String name) {
        BlockVariable variable = variableManager.getGlobalVariable(name);
        if (variable != null) {
            variableManager.removeVariable(name);
            player.sendMessage("§a✓ Переменная '" + name + "' удалена!");
        } else {
            player.sendMessage("§c✗ Переменная '" + name + "' не найдена");
        }
    }

    private void showVariableInfo(Player player, String name) {
        BlockVariable variable = variableManager.getGlobalVariable(name);
        if (variable != null) {
            player.sendMessage("§6=== Информация о переменной ===");
            player.sendMessage("§eИмя: " + variable.getName());
            player.sendMessage("§eТип: " + variable.getType().getDisplayName());
            player.sendMessage("§eОписание: " + variable.getDescription());
            player.sendMessage("§eТекущее значение: " + formatVariableValue(variable));
            player.sendMessage("§eПример: " + variable.getType().getExample());
        } else {
            player.sendMessage("§c✗ Переменная '" + name + "' не найдена");
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
        player.sendMessage("§6=== Помощь по команде BlockVariable ===");
        player.sendMessage("§e/blockvar give <имя> - Получить переменную");
        player.sendMessage("§e/blockvar list - Список доступных переменных");
        player.sendMessage("§e/blockvar create <имя> <описание> <тип> [значение] - Создать переменную");
        player.sendMessage("§e/blockvar remove <имя> - Удалить переменную");
        player.sendMessage("§e/blockvar info <имя> - Информация о переменной");
        player.sendMessage("§e/blockvar help - Показать эту справку");
        player.sendMessage("");
        player.sendMessage("§7Примеры:");
        player.sendMessage("§7• /blockvar give antiSpam");
        player.sendMessage("§7• /blockvar create cooldown \"Задержка между командами\" INTEGER 1000");
        player.sendMessage("§7• /blockvar create commands \"Список команд\" LIST help,list,me");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("give", "list", "create", "remove", "info", "help"));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if ("give".equals(subCommand) || "remove".equals(subCommand) || "info".equals(subCommand)) {
                // Получаем список доступных переменных
                // TODO: Реализовать получение списка переменных из менеджера
                completions.addAll(Arrays.asList("antiSpam", "cooldown", "commands", "logging"));
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
