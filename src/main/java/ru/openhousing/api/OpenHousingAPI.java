package ru.openhousing.api;

import org.bukkit.entity.Player;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.script.CodeScript;

/**
 * API для разработчиков
 */
public class OpenHousingAPI {
    
    private final OpenHousing plugin;
    
    public OpenHousingAPI(OpenHousing plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Получение скрипта игрока
     */
    public CodeScript getPlayerScript(Player player) {
        return plugin.getCodeManager().getScript(player);
    }
    
    /**
     * Выполнение скрипта игрока
     */
    public void executePlayerScript(Player player) {
        plugin.getCodeManager().executeScript(player);
    }
    
    /**
     * Открытие редактора кода для игрока
     */
    public void openCodeEditor(Player player) {
        plugin.getCodeManager().openCodeEditor(player);
    }
    
    /**
     * Проверка, открыт ли редактор у игрока
     */
    public boolean hasOpenEditor(Player player) {
        return plugin.getCodeManager().hasOpenEditor(player);
    }
}
