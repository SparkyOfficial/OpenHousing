package ru.openhousing.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.CodeBlockFactory;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.events.PlayerCommandEventBlock;
import ru.openhousing.coding.blocks.events.PlayerTeleportEventBlock;
import ru.openhousing.coding.blocks.conditions.IfPlayerOnlineConditionBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Команда для настройки специализированных блоков
 */
public class BlockConfigCommand implements CommandExecutor, TabCompleter {

    private final OpenHousing plugin;

    public BlockConfigCommand(OpenHousing plugin) {
        this.plugin = plugin;
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
            case "gui":
                if (args.length < 2) {
                    player.sendMessage("§cИспользование: /blockconfig gui <тип_блока>");
                    return true;
                }
                openBlockGUI(player, args[1]);
                break;
            case "settings":
                if (args.length < 2) {
                    player.sendMessage("§cИспользование: /blockconfig settings <тип_блока>");
                    return true;
                }
                showBlockSettings(player, args[1]);
                break;
            case "list":
                showAvailableBlocks(player);
                break;
            case "help":
                showHelp(player);
                break;
            default:
                player.sendMessage("§cНеизвестная подкоманда. Используйте /blockconfig help");
                break;
        }

        return true;
    }

    private void openBlockGUI(Player player, String blockTypeStr) {
        try {
            BlockType blockType = BlockType.valueOf(blockTypeStr.toUpperCase());
            CodeBlock block = CodeBlockFactory.createBlock(blockType);

            if (block instanceof PlayerCommandEventBlock) {
                ((PlayerCommandEventBlock) block).openConfigurationGUI(player);
            } else if (block instanceof PlayerTeleportEventBlock) {
                ((PlayerTeleportEventBlock) block).openConfigurationGUI(player);
            } else if (block instanceof IfPlayerOnlineConditionBlock) {
                ((IfPlayerOnlineConditionBlock) block).openConfigurationGUI(player);
            } else {
                player.sendMessage("§cЭтот блок не поддерживает расширенную настройку через GUI");
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cНеизвестный тип блока: " + blockTypeStr);
        } catch (Exception e) {
            player.sendMessage("§cОшибка при открытии GUI: " + e.getMessage());
        }
    }

    private void showBlockSettings(Player player, String blockTypeStr) {
        try {
            BlockType blockType = BlockType.valueOf(blockTypeStr.toUpperCase());
            CodeBlock block = CodeBlockFactory.createBlock(blockType);

            if (block instanceof PlayerCommandEventBlock) {
                ((PlayerCommandEventBlock) block).showSettings(player);
            } else if (block instanceof PlayerTeleportEventBlock) {
                ((PlayerTeleportEventBlock) block).showSettings(player);
            } else if (block instanceof IfPlayerOnlineConditionBlock) {
                ((IfPlayerOnlineConditionBlock) block).showSettings(player);
            } else {
                player.sendMessage("§cЭтот блок не поддерживает расширенную настройку");
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cНеизвестный тип блока: " + blockTypeStr);
        } catch (Exception e) {
            player.sendMessage("§cОшибка при показе настроек: " + e.getMessage());
        }
    }

    private void showAvailableBlocks(Player player) {
        player.sendMessage("§6=== Доступные блоки для настройки ===");
        player.sendMessage("§eСобытия:");
        player.sendMessage("§7• PLAYER_COMMAND - Настройка команд игроков");
        player.sendMessage("§7• PLAYER_TELEPORT - Настройка телепортации");
        player.sendMessage("§eУсловия:");
        player.sendMessage("§7• IF_PLAYER_ONLINE - Проверка онлайн статуса");
        player.sendMessage("");
        player.sendMessage("§7Используйте: /blockconfig gui <тип_блока>");
        player.sendMessage("§7Или: /blockconfig settings <тип_блока>");
    }

    private void showHelp(Player player) {
        player.sendMessage("§6=== Помощь по команде BlockConfig ===");
        player.sendMessage("§e/blockconfig gui <тип_блока> - Открыть GUI настройки");
        player.sendMessage("§e/blockconfig settings <тип_блока> - Показать текущие настройки");
        player.sendMessage("§e/blockconfig list - Список доступных блоков");
        player.sendMessage("§e/blockconfig help - Показать эту справку");
        player.sendMessage("");
        player.sendMessage("§7Примеры:");
        player.sendMessage("§7• /blockconfig gui PLAYER_COMMAND");
        player.sendMessage("§7• /blockconfig settings PLAYER_TELEPORT");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("gui", "settings", "list", "help"));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if ("gui".equals(subCommand) || "settings".equals(subCommand)) {
                completions.addAll(Arrays.asList(
                    "PLAYER_COMMAND",
                    "PLAYER_TELEPORT", 
                    "IF_PLAYER_ONLINE"
                ));
            }
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
