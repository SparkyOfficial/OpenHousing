package ru.openhousing.coding.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.variables.DynamicVariable;
import ru.openhousing.coding.variables.VariableType;
import ru.openhousing.utils.ItemBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Обработчик взаимодействий с переменными
 * Shift + ПКМ - сохранить переменную (добавляет СОХРАНЕНО)
 */
public class VariableInteractionListener implements Listener {
    
    private final OpenHousing plugin;
    
    public VariableInteractionListener(OpenHousing plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (!isVariableItem(item)) return;
        
        // Проверяем Shift + ПКМ
        if (event.getAction().name().contains("RIGHT_CLICK") && player.isSneaking()) {
            event.setCancelled(true);
            
            VariableType variableType = getVariableTypeFromItem(item);
            if (variableType == VariableType.DYNAMIC) {
                saveVariable(player, item);
            } else if (variableType == VariableType.LOCATION) {
                recordLocation(player, item);
            } else {
                player.sendMessage("§cЭтот тип переменной нельзя сохранить таким способом!");
            }
        }
        
        // ПКМ по пустоте для местоположения
        else if (event.getAction().name().contains("RIGHT_CLICK_AIR") && !player.isSneaking()) {
            VariableType variableType = getVariableTypeFromItem(item);
            if (variableType == VariableType.LOCATION) {
                event.setCancelled(true);
                recordPlayerLocation(player, item);
            }
        }
        
        // Shift + ЛКМ для телепорта к местоположению
        else if (event.getAction().name().contains("LEFT_CLICK") && player.isSneaking()) {
            VariableType variableType = getVariableTypeFromItem(item);
            if (variableType == VariableType.LOCATION) {
                event.setCancelled(true);
                teleportToLocation(player, item);
            }
        }
    }
    
    /**
     * Проверка, является ли предмет переменной
     */
    private boolean isVariableItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        ItemMeta meta = item.getItemMeta();
        return meta.hasCustomModelData() && meta.getCustomModelData() >= 1000;
    }
    
    /**
     * Получение типа переменной из предмета
     */
    private VariableType getVariableTypeFromItem(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) {
            return null;
        }
        
        int modelData = item.getItemMeta().getCustomModelData();
        int typeIndex = modelData - 1000;
        
        VariableType[] types = VariableType.values();
        if (typeIndex >= 0 && typeIndex < types.length) {
            return types[typeIndex];
        }
        
        return null;
    }
    
    /**
     * Сохранение динамической переменной (Shift + ПКМ)
     */
    private void saveVariable(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        String currentName = meta.getDisplayName();
        
        // Проверяем, уже ли сохранена
        if (currentName.contains("СОХРАНЕНО")) {
            player.sendMessage("§cПеременная уже сохранена!");
            return;
        }
        
        // Добавляем статус СОХРАНЕНО
        String newName = currentName + " §8СОХРАНЕНО";
        meta.setDisplayName(newName);
        
        // Обновляем описание
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add("");
        lore.add("§8✓ Переменная сохранена навсегда");
        lore.add("§8✓ Будет доступна после перезагрузки");
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        
        // Сохраняем в системе
        String variableName = extractVariableName(currentName);
        DynamicVariable dynamicVar = DynamicVariable.fromPlaceholder("%" + variableName + "%");
        dynamicVar.makeSaved();
        
        player.sendMessage("§aПеременная сохранена навсегда!");
        player.sendMessage("§7Теперь она будет доступна после перезагрузки сервера");
        
        // Эффект сохранения
        player.getWorld().spawnParticle(
            org.bukkit.Particle.HAPPY_VILLAGER, 
            player.getLocation().add(0, 1, 0), 
            10, 0.5, 0.5, 0.5, 0
        );
    }
    
    /**
     * Запись местоположения игрока (ПКМ по пустоте)
     */
    private void recordPlayerLocation(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        
        // Записываем координаты
        String locationString = String.format("%.1f, %.1f, %.1f (%s)", 
            player.getLocation().getX(),
            player.getLocation().getY(), 
            player.getLocation().getZ(),
            player.getWorld().getName()
        );
        
        // Обновляем название и описание
        String baseName = extractVariableName(meta.getDisplayName());
        meta.setDisplayName("§bМестоположение: " + baseName);
        
        List<String> lore = Arrays.asList(
            "§7Записанные координаты:",
            "§fX: " + String.format("%.1f", player.getLocation().getX()),
            "§fY: " + String.format("%.1f", player.getLocation().getY()),
            "§fZ: " + String.format("%.1f", player.getLocation().getZ()),
            "§fМир: " + player.getWorld().getName(),
            "",
            "§7Shift+ЛКМ - телепортироваться",
            "§7Shift+ПКМ - записать новые координаты"
        );
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        
        player.sendMessage("§aКоординаты записаны: §f" + locationString);
        
        // Эффект записи
        player.getWorld().spawnParticle(
            org.bukkit.Particle.ENCHANT,
            player.getLocation(),
            20, 1, 1, 1, 0
        );
    }
    
    /**
     * Запись координат блока (ПКМ по блоку)
     */
    private void recordLocation(Player player, ItemStack item) {
        // Аналогично recordPlayerLocation, но для блока
        recordPlayerLocation(player, item);
    }
    
    /**
     * Телепортация к записанному местоположению (Shift + ЛКМ)
     */
    private void teleportToLocation(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        
        if (lore == null || lore.size() < 5) {
            player.sendMessage("§cВ этой переменной нет записанных координат!");
            return;
        }
        
        try {
            // Извлекаем координаты из lore
            String xLine = lore.get(1).replace("§fX: ", "");
            String yLine = lore.get(2).replace("§fY: ", "");
            String zLine = lore.get(3).replace("§fZ: ", "");
            String worldLine = lore.get(4).replace("§fМир: ", "");
            
            double x = Double.parseDouble(xLine);
            double y = Double.parseDouble(yLine);
            double z = Double.parseDouble(zLine);
            
            org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldLine);
            if (world == null) {
                player.sendMessage("§cМир '" + worldLine + "' не найден!");
                return;
            }
            
            org.bukkit.Location location = new org.bukkit.Location(world, x, y, z);
            player.teleport(location);
            
            player.sendMessage("§aТелепортация выполнена!");
            player.sendMessage("§7Координаты: §f" + x + ", " + y + ", " + z);
            
            // Эффект телепортации
            player.getWorld().spawnParticle(
                org.bukkit.Particle.PORTAL,
                player.getLocation(),
                50, 1, 1, 1, 0
            );
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка при телепортации: " + e.getMessage());
        }
    }
    
    /**
     * Извлечение имени переменной из отображаемого имени
     */
    private String extractVariableName(String displayName) {
        if (displayName == null) return "variable";
        
        // Убираем цветовые коды и статусы
        String clean = displayName.replaceAll("§[0-9a-fk-or]", "");
        clean = clean.replace("СОХРАНЕНО", "").trim();
        
        if (clean.isEmpty()) return "variable";
        return clean;
    }
}
