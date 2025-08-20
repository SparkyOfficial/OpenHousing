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
        try {
            // Используем AnvilGUI библиотеку
            new net.wesjd.anvilgui.AnvilGUI.Builder()
                .onComplete((player, text) -> {
                    if (onComplete != null) {
                        onComplete.accept(text);
                    }
                    return net.wesjd.anvilgui.AnvilGUI.Response.close();
                })
                .onClose(player -> {
                    // Игрок закрыл GUI без ввода
                })
                .text(prompt.length() > 30 ? "Введите значение" : prompt)
                .itemLeft(new org.bukkit.inventory.ItemStack(org.bukkit.Material.PAPER))
                .title("§6Ввод значения")
                .plugin(plugin)
                .open(player);
        } catch (Exception e) {
            // Fallback к чат-вводу если AnvilGUI недоступен
            player.sendMessage("§e" + prompt);
            player.sendMessage("§7Введите значение в чат:");
            
            // Регистрируем временный слушатель чата
            ru.openhousing.listeners.ChatListener.registerTemporaryInput(player, onComplete);
        }
    }
}
