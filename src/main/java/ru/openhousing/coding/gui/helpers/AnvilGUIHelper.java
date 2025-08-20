package ru.openhousing.coding.gui.helpers;

import org.bukkit.entity.Player;
import ru.openhousing.OpenHousing;

import java.util.function.Consumer;

/**
 * Помощник для создания AnvilGUI для ввода текста
 */
public class AnvilGUIHelper {
    
    private final OpenHousing plugin;
    private final Player player;
    private final String prompt;
    private final Consumer<String> onComplete;
    
    public AnvilGUIHelper(OpenHousing plugin, Player player, String prompt, Consumer<String> onComplete) {
        this.plugin = plugin;
        this.player = player;
        this.prompt = prompt;
        this.onComplete = onComplete;
    }
    
    public void open() {
        // Простая реализация без AnvilGUI для избежания зависимостей
        player.sendMessage("§e" + prompt);
        player.sendMessage("§7Введите значение в чат:");
        
        // TODO: Реализовать через chat listener или использовать другой подход
        if (onComplete != null) {
            onComplete.accept(""); // Временная заглушка
        }
    }
}
