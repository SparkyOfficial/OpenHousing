package ru.openhousing.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import ru.openhousing.OpenHousing;
import ru.openhousing.housing.House;
import ru.openhousing.utils.ItemBuilder;
import ru.openhousing.utils.MessageUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * GUI для настройки звука в доме
 */
public class SoundSettingsGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final House house;
    private final Inventory inventory;
    
    // Карта звуков для быстрого выбора
    private static final Map<String, Sound> SOUND_MAP = new HashMap<>();
    static {
        SOUND_MAP.put("AMBIENT_CAVE", Sound.AMBIENT_CAVE);
        SOUND_MAP.put("AMBIENT_UNDERWATER_ENTER", Sound.AMBIENT_UNDERWATER_ENTER);
        SOUND_MAP.put("AMBIENT_UNDERWATER_EXIT", Sound.AMBIENT_UNDERWATER_EXIT);
        SOUND_MAP.put("BLOCK_WATER_AMBIENT", Sound.BLOCK_WATER_AMBIENT);
        SOUND_MAP.put("ENTITY_PLAYER_LEVELUP", Sound.ENTITY_PLAYER_LEVELUP);
        SOUND_MAP.put("MUSIC_DISC_13", Sound.MUSIC_DISC_13);
        SOUND_MAP.put("MUSIC_DISC_CAT", Sound.MUSIC_DISC_CAT);
        SOUND_MAP.put("MUSIC_DISC_BLOCKS", Sound.MUSIC_DISC_BLOCKS);
        SOUND_MAP.put("MUSIC_DISC_CHIRP", Sound.MUSIC_DISC_CHIRP);
        SOUND_MAP.put("MUSIC_DISC_FAR", Sound.MUSIC_DISC_FAR);
        SOUND_MAP.put("MUSIC_DISC_MALL", Sound.MUSIC_DISC_MALL);
        SOUND_MAP.put("MUSIC_DISC_MELLOHI", Sound.MUSIC_DISC_MELLOHI);
        SOUND_MAP.put("MUSIC_DISC_STAL", Sound.MUSIC_DISC_STAL);
        SOUND_MAP.put("MUSIC_DISC_STRAD", Sound.MUSIC_DISC_STRAD);
        SOUND_MAP.put("MUSIC_DISC_WARD", Sound.MUSIC_DISC_WARD);
        SOUND_MAP.put("MUSIC_DISC_11", Sound.MUSIC_DISC_11);
        SOUND_MAP.put("MUSIC_DISC_WAIT", Sound.MUSIC_DISC_WAIT);
        SOUND_MAP.put("MUSIC_DISC_PIGSTEP", Sound.MUSIC_DISC_PIGSTEP);
        SOUND_MAP.put("MUSIC_DISC_OTHERSIDE", Sound.MUSIC_DISC_OTHERSIDE);
        SOUND_MAP.put("MUSIC_DISC_5", Sound.MUSIC_DISC_5);
        SOUND_MAP.put("MUSIC_DISC_RELIC", Sound.MUSIC_DISC_RELIC);
    }
    
    public SoundSettingsGUI(OpenHousing plugin, Player player, House house) {
        this.plugin = plugin;
        this.player = player;
        this.house = house;
        this.inventory = Bukkit.createInventory(null, 54, "§6Настройки звука");
        
        setupGUI();
        registerListener();
    }
    
    /**
     * Настройка GUI
     */
    private void setupGUI() {
        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.JUKEBOX)
            .name("§6§lНастройки звука")
            .lore(Arrays.asList(
                "§7Настройте параметры звука",
                "§7в вашем доме",
                "",
                "§eДом: §f" + house.getName()
            ))
            .build());
        
        // Фоновая музыка
        Object musicObj = house.getSetting("background_music");
        boolean music = musicObj instanceof Boolean ? (Boolean) musicObj : false;
        inventory.setItem(19, new ItemBuilder(music ? Material.MUSIC_DISC_13 : Material.BARRIER)
            .name("§eФоновая музыка")
            .lore(Arrays.asList(
                "§7Текущее состояние: " + (music ? "§aВключена" : "§cВыключена"),
                "",
                "§7Если включена, в доме будет",
                "§7играть фоновая музыка",
                "",
                "§eКликните для изменения"
            ))
            .build());
        
        // Выбранная музыка
        Object selectedMusicObj = house.getSetting("selected_music");
        String selectedMusic = selectedMusicObj instanceof String ? (String) selectedMusicObj : "MUSIC_DISC_13";
        inventory.setItem(21, new ItemBuilder(Material.MUSIC_DISC_CAT)
            .name("§eВыбранная музыка")
            .lore(Arrays.asList(
                "§7Текущая музыка: §f" + getMusicDisplayName(selectedMusic),
                "",
                "§7Выберите диск для воспроизведения",
                "",
                "§eКликните для выбора"
            ))
            .build());
        
        // Громкость музыки
        Object volumeObj = house.getSetting("music_volume");
        float volume = volumeObj instanceof Float ? (Float) volumeObj : 0.5f;
        inventory.setItem(23, new ItemBuilder(Material.REDSTONE)
            .name("§eГромкость музыки")
            .lore(Arrays.asList(
                "§7Текущая громкость: §f" + (int)(volume * 100) + "%",
                "",
                "§7Установите громкость музыки",
                "§7(0-100%)",
                "",
                "§eКликните для изменения"
            ))
            .build());
        
        // Звуки окружения
        Object ambientObj = house.getSetting("ambient_sounds");
        boolean ambient = ambientObj instanceof Boolean ? (Boolean) ambientObj : true;
        inventory.setItem(25, new ItemBuilder(ambient ? Material.NOTE_BLOCK : Material.BARRIER)
            .name("§eЗвуки окружения")
            .lore(Arrays.asList(
                "§7Текущее состояние: " + (ambient ? "§aВключены" : "§cВыключены"),
                "",
                "§7Звуки воды, ветра, пещер",
                "§7и другие природные звуки",
                "",
                "§eКликните для изменения"
            ))
            .build());
        
        // Громкость окружения
        Object ambientVolumeObj = house.getSetting("ambient_volume");
        float ambientVolume = ambientVolumeObj instanceof Float ? (Float) ambientVolumeObj : 0.3f;
        inventory.setItem(37, new ItemBuilder(Material.NOTE_BLOCK)
            .name("§eГромкость окружения")
            .lore(Arrays.asList(
                "§7Текущая громкость: §f" + (int)(ambientVolume * 100) + "%",
                "",
                "§7Установите громкость звуков",
                "§7окружения (0-100%)",
                "",
                "§eКликните для изменения"
            ))
            .build());
        
        // Звуки мобов
        Object mobSoundsObj = house.getSetting("mob_sounds");
        boolean mobSounds = mobSoundsObj instanceof Boolean ? (Boolean) mobSoundsObj : true;
        inventory.setItem(39, new ItemBuilder(mobSounds ? Material.COW_SPAWN_EGG : Material.BARRIER)
            .name("§eЗвуки мобов")
            .lore(Arrays.asList(
                "§7Текущее состояние: " + (mobSounds ? "§aВключены" : "§cВыключены"),
                "",
                "§7Звуки животных и мобов",
                "§7в доме",
                "",
                "§eКликните для изменения"
            ))
            .build());
        
        // Кнопки управления
        inventory.setItem(45, new ItemBuilder(Material.LIME_CONCRETE)
            .name("§a§lСохранить")
            .lore(Arrays.asList(
                "§7Сохранить настройки",
                "§7и вернуться к настройкам дома"
            ))
            .build());
        
        inventory.setItem(47, new ItemBuilder(Material.RED_CONCRETE)
            .name("§c§lОтмена")
            .lore(Arrays.asList(
                "§7Отменить изменения",
                "§7и вернуться к настройкам дома"
            ))
            .build());
        
        inventory.setItem(49, new ItemBuilder(Material.BARRIER)
            .name("§4§lСбросить")
            .lore(Arrays.asList(
                "§7Сбросить все настройки",
                "§7к значениям по умолчанию"
            ))
            .build());
    }
    
    /**
     * Получение отображаемого имени музыки
     */
    private String getMusicDisplayName(String musicName) {
        switch (musicName) {
            case "MUSIC_DISC_13": return "13";
            case "MUSIC_DISC_CAT": return "Cat";
            case "MUSIC_DISC_BLOCKS": return "Blocks";
            case "MUSIC_DISC_CHIRP": return "Chirp";
            case "MUSIC_DISC_FAR": return "Far";
            case "MUSIC_DISC_MALL": return "Mall";
            case "MUSIC_DISC_MELLOHI": return "Mellohi";
            case "MUSIC_DISC_STAL": return "Stal";
            case "MUSIC_DISC_STRAD": return "Strad";
            case "MUSIC_DISC_WARD": return "Ward";
            case "MUSIC_DISC_11": return "11";
            case "MUSIC_DISC_WAIT": return "Wait";
            case "MUSIC_DISC_PIGSTEP": return "Pigstep";
            case "MUSIC_DISC_OTHERSIDE": return "Otherside";
            case "MUSIC_DISC_5": return "5";
            case "MUSIC_DISC_RELIC": return "Relic";
            default: return musicName;
        }
    }
    
    /**
     * Регистрация слушателя
     */
    private void registerListener() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Открытие GUI
     */
    public void open() {
        player.openInventory(inventory);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6Настройки звука") || 
            !event.getWhoClicked().equals(player)) {
            return;
        }
        
        event.setCancelled(true);
        
        int slot = event.getRawSlot();
        
        switch (slot) {
            case 19: // Фоновая музыка
                toggleBackgroundMusic();
                break;
                
            case 21: // Выбранная музыка
                openMusicSelector();
                break;
                
            case 23: // Громкость музыки
                openMusicVolumeInput();
                break;
                
            case 25: // Звуки окружения
                toggleAmbientSounds();
                break;
                
            case 37: // Громкость окружения
                openAmbientVolumeInput();
                break;
                
            case 39: // Звуки мобов
                toggleMobSounds();
                break;
                
            case 45: // Сохранить
                saveAndClose();
                break;
                
            case 47: // Отмена
                cancelAndClose();
                break;
                
            case 49: // Сбросить
                resetToDefaults();
                break;
        }
    }
    
    /**
     * Переключение фоновой музыки
     */
    private void toggleBackgroundMusic() {
        Object musicObj = house.getSetting("background_music");
        boolean currentValue = musicObj instanceof Boolean ? (Boolean) musicObj : false;
        boolean newValue = !currentValue;
        house.setSetting("background_music", newValue);
        setupGUI();
        MessageUtil.send(player, "§aФоновая музыка " + (newValue ? "включена" : "выключена"));
    }
    
    /**
     * Открытие селектора музыки
     */
    private void openMusicSelector() {
        Inventory selector = Bukkit.createInventory(null, 54, "§6Выбор музыки");
        
        int slot = 0;
        for (Map.Entry<String, Sound> entry : SOUND_MAP.entrySet()) {
            if (entry.getKey().startsWith("MUSIC_DISC_")) {
                selector.setItem(slot, new ItemBuilder(Material.MUSIC_DISC_13)
                    .name("§e" + getMusicDisplayName(entry.getKey()))
                    .lore(Arrays.asList("§7Кликните для выбора"))
                    .build());
                slot++;
                if (slot >= 45) break; // Ограничиваем количество слотов
            }
        }
        
        // Временный слушатель для селектора
        new MusicSelector(plugin, player, selector, (selectedSlot) -> {
            String[] musicNames = SOUND_MAP.keySet().toArray(new String[0]);
            if (selectedSlot < musicNames.length && musicNames[selectedSlot].startsWith("MUSIC_DISC_")) {
                String selectedMusic = musicNames[selectedSlot];
                house.setSetting("selected_music", selectedMusic);
                setupGUI();
                open();
                MessageUtil.send(player, "§aМузыка выбрана: §f" + getMusicDisplayName(selectedMusic));
            }
        });
    }
    
    /**
     * Открытие ввода громкости музыки
     */
    private void openMusicVolumeInput() {
        MessageUtil.send(player, 
            "§eВведите громкость музыки:",
            "§7(0-100%)"
        );
        
        // Регистрируем временный слушатель чата
        ru.openhousing.listeners.ChatListener.registerTemporaryInput(player, (input) -> {
            try {
                int volume = Integer.parseInt(input.trim());
                if (volume >= 0 && volume <= 100) {
                    float volumeFloat = volume / 100.0f;
                    house.setSetting("music_volume", volumeFloat);
                    setupGUI();
                    open();
                    MessageUtil.send(player, "§aГромкость музыки установлена: §f" + volume + "%");
                } else {
                    MessageUtil.send(player, "§cГромкость должна быть от 0 до 100");
                    open();
                }
            } catch (NumberFormatException e) {
                MessageUtil.send(player, "§cВведите корректное число");
                open();
            }
        });
    }
    
    /**
     * Переключение звуков окружения
     */
    private void toggleAmbientSounds() {
        Object ambientObj = house.getSetting("ambient_sounds");
        boolean currentValue = ambientObj instanceof Boolean ? (Boolean) ambientObj : true;
        boolean newValue = !currentValue;
        house.setSetting("ambient_sounds", newValue);
        setupGUI();
        MessageUtil.send(player, "§aЗвуки окружения " + (newValue ? "включены" : "выключены"));
    }
    
    /**
     * Открытие ввода громкости окружения
     */
    private void openAmbientVolumeInput() {
        MessageUtil.send(player, 
            "§eВведите громкость окружения:",
            "§7(0-100%)"
        );
        
        // Регистрируем временный слушатель чата
        ru.openhousing.listeners.ChatListener.registerTemporaryInput(player, (input) -> {
            try {
                int volume = Integer.parseInt(input.trim());
                if (volume >= 0 && volume <= 100) {
                    float volumeFloat = volume / 100.0f;
                    house.setSetting("ambient_volume", volumeFloat);
                    setupGUI();
                    open();
                    MessageUtil.send(player, "§aГромкость окружения установлена: §f" + volume + "%");
                } else {
                    MessageUtil.send(player, "§cГромкость должна быть от 0 до 100");
                    open();
                }
            } catch (NumberFormatException e) {
                MessageUtil.send(player, "§cВведите корректное число");
                open();
            }
        });
    }
    
    /**
     * Переключение звуков мобов
     */
    private void toggleMobSounds() {
        Object mobSoundsObj = house.getSetting("mob_sounds");
        boolean currentValue = mobSoundsObj instanceof Boolean ? (Boolean) mobSoundsObj : true;
        boolean newValue = !currentValue;
        house.setSetting("mob_sounds", newValue);
        setupGUI();
        MessageUtil.send(player, "§aЗвуки мобов " + (newValue ? "включены" : "выключены"));
    }
    
    /**
     * Сохранение и закрытие
     */
    private void saveAndClose() {
        plugin.getDatabaseManager().saveHouse(house);
        MessageUtil.send(player, "§aНастройки звука сохранены!");
        close();
    }
    
    /**
     * Отмена и закрытие
     */
    private void cancelAndClose() {
        MessageUtil.send(player, "§7Изменения отменены");
        close();
    }
    
    /**
     * Сброс к значениям по умолчанию
     */
    private void resetToDefaults() {
        house.setSetting("background_music", false);
        house.setSetting("selected_music", "MUSIC_DISC_13");
        house.setSetting("music_volume", 0.5f);
        house.setSetting("ambient_sounds", true);
        house.setSetting("ambient_volume", 0.3f);
        house.setSetting("mob_sounds", true);
        
        setupGUI();
        MessageUtil.send(player, "§aНастройки звука сброшены к значениям по умолчанию");
    }
    
    /**
     * Закрытие GUI
     */
    private void close() {
        player.closeInventory();
        InventoryClickEvent.getHandlerList().unregister(this);
        
        // Возвращаемся к настройкам дома
        new HouseSettingsGUI(plugin, player, house).open();
    }
    
    /**
     * Вспомогательный класс для селектора музыки
     */
    private static class MusicSelector implements Listener {
        private final OpenHousing plugin;
        private final Player player;
        private final Inventory inventory;
        private final java.util.function.Consumer<Integer> callback;
        
        public MusicSelector(OpenHousing plugin, Player player, Inventory inventory, java.util.function.Consumer<Integer> callback) {
            this.plugin = plugin;
            this.player = player;
            this.inventory = inventory;
            this.callback = callback;
            
            Bukkit.getPluginManager().registerEvents(this, plugin);
            player.openInventory(inventory);
        }
        
        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!event.getView().getTitle().equals("§6Выбор музыки") || 
                !event.getWhoClicked().equals(player)) {
                return;
            }
            
            event.setCancelled(true);
            
            int slot = event.getRawSlot();
            if (slot < 45 && inventory.getItem(slot) != null) {
                callback.accept(slot);
                player.closeInventory();
                InventoryClickEvent.getHandlerList().unregister(this);
            }
        }
    }
}
