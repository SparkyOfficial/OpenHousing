package ru.openhousing.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.OpenHousing;
import ru.openhousing.utils.ItemBuilder;
import ru.openhousing.utils.MessageUtil;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * GUI для выбора предметов
 */
public class ItemSelectorGUI implements Listener {
    
    private final OpenHousing plugin;
    private final Player player;
    private final Consumer<Material> onItemSelect;
    private final Consumer<Void> onCancel;
    private final Inventory inventory;
    private final List<Material> materials;
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 45;
    
    // Категории предметов
    private static final Material[][] CATEGORIES = {
        // Строительные блоки
        {Material.STONE, Material.DIRT, Material.GRASS_BLOCK, Material.SAND, Material.GRAVEL, Material.OAK_LOG, Material.OAK_PLANKS, Material.BRICKS, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE},
        // Руда и минералы
        {Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.LAPIS_ORE, Material.REDSTONE_ORE, Material.NETHER_QUARTZ_ORE, Material.ANCIENT_DEBRIS},
        // Инструменты
        {Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
         Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE,
         Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL},
        // Оружие и броня
        {Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
         Material.LEATHER_HELMET, Material.IRON_HELMET, Material.GOLDEN_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET,
         Material.LEATHER_CHESTPLATE, Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE},
        // Еда
        {Material.APPLE, Material.BREAD, Material.COOKED_BEEF, Material.COOKED_CHICKEN, Material.COOKED_PORKCHOP, Material.COOKED_MUTTON, Material.COOKED_RABBIT,
         Material.BAKED_POTATO, Material.CARROT, Material.POTATO, Material.BEETROOT, Material.MELON_SLICE, Material.SWEET_BERRIES, Material.GLOW_BERRIES},
        // Специальные предметы
        {Material.TORCH, Material.LANTERN, Material.CAMPFIRE, Material.FURNACE, Material.CRAFTING_TABLE, Material.ANVIL, Material.ENCHANTING_TABLE,
         Material.BOOKSHELF, Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST, Material.SHULKER_BOX, Material.BARREL}
    };
    
    private static final String[] CATEGORY_NAMES = {
        "Строительные блоки", "Руда и минералы", "Инструменты", "Оружие и броня", "Еда", "Специальные предметы"
    };
    
    public ItemSelectorGUI(OpenHousing plugin, Player player, Consumer<Material> onItemSelect) {
        this(plugin, player, onItemSelect, null);
    }
    
    public ItemSelectorGUI(OpenHousing plugin, Player player, Consumer<Material> onItemSelect, Consumer<Void> onCancel) {
        this.plugin = plugin;
        this.player = player;
        this.onItemSelect = onItemSelect;
        this.onCancel = onCancel;
        this.materials = Arrays.asList(Material.values());
        this.inventory = Bukkit.createInventory(null, 54, "§6Выбор предмета");
        
        setupGUI();
        registerListener();
    }
    
    /**
     * Настройка GUI
     */
    private void setupGUI() {
        // Заголовок
        inventory.setItem(4, new ItemBuilder(Material.CHEST)
            .name("§6§lВыбор предмета")
            .lore(Arrays.asList(
                "§7Выберите предмет из списка",
                "§7или используйте поиск",
                "",
                "§eВсего предметов: §f" + materials.size()
            ))
            .build());
        
        // Категории предметов
        for (int i = 0; i < Math.min(CATEGORIES.length, 9); i++) {
            Material[] category = CATEGORIES[i];
            if (category.length > 0) {
                inventory.setItem(9 + i, new ItemBuilder(category[0])
                    .name("§e" + CATEGORY_NAMES[i])
                    .lore(Arrays.asList(
                        "§7Кликните для просмотра",
                        "§7предметов этой категории",
                        "",
                        "§7Предметов: §f" + category.length
                    ))
                    .build());
            }
        }
        
        // Отображение предметов (если не выбрана категория)
        if (currentPage == 0) {
            // Показываем популярные предметы
            Material[] popularItems = {
                Material.DIAMOND, Material.IRON_INGOT, Material.GOLD_INGOT, Material.EMERALD,
                Material.COAL, Material.REDSTONE, Material.LAPIS_LAZULI, Material.QUARTZ,
                Material.STONE, Material.DIRT, Material.WOOD, Material.PLANKS,
                Material.APPLE, Material.BREAD, Material.COOKED_BEEF, Material.TORCH
            };
            
            for (int i = 0; i < Math.min(popularItems.length, 27); i++) {
                inventory.setItem(18 + i, new ItemBuilder(popularItems[i])
                    .name("§e" + getItemDisplayName(popularItems[i]))
                    .lore(Arrays.asList("§7Кликните для выбора"))
                    .build());
            }
        } else {
            // Показываем предметы выбранной категории
            int categoryIndex = currentPage - 1;
            if (categoryIndex < CATEGORIES.length) {
                Material[] category = CATEGORIES[categoryIndex];
                for (int i = 0; i < Math.min(category.length, 27); i++) {
                    inventory.setItem(18 + i, new ItemBuilder(category[i])
                        .name("§e" + getItemDisplayName(category[i]))
                        .lore(Arrays.asList("§7Кликните для выбора"))
                        .build());
                }
            }
        }
        
        // Кнопки управления
        inventory.setItem(45, new ItemBuilder(Material.ARROW)
            .name("§eНазад")
            .lore(Arrays.asList("§7Вернуться к категориям"))
            .build());
        
        inventory.setItem(47, new ItemBuilder(Material.RED_CONCRETE)
            .name("§c§lОтмена")
            .lore(Arrays.asList("§7Отменить выбор"))
            .build());
        
        // Поиск предмета
        inventory.setItem(49, new ItemBuilder(Material.COMPASS)
            .name("§eПоиск предмета")
            .lore(Arrays.asList(
                "§7Найти предмет по имени",
                "§7(введите в чат)"
            ))
            .build());
        
        // Случайный предмет
        inventory.setItem(51, new ItemBuilder(Material.DICE)
            .name("§eСлучайный предмет")
            .lore(Arrays.asList("§7Выбрать случайный предмет"))
            .build());
    }
    
    /**
     * Получение отображаемого имени предмета
     */
    private String getItemDisplayName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }
        
        return result.toString().trim();
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
        if (!event.getView().getTitle().equals("§6Выбор предмета") || 
            !event.getWhoClicked().equals(player)) {
            return;
        }
        
        event.setCancelled(true);
        
        int slot = event.getRawSlot();
        
        // Выбор категории
        if (slot >= 9 && slot < 18) {
            int categoryIndex = slot - 9;
            if (categoryIndex < CATEGORIES.length) {
                currentPage = categoryIndex + 1;
                setupGUI();
            }
            return;
        }
        
        // Выбор предмета
        if (slot >= 18 && slot < 45) {
            int itemIndex = slot - 18;
            
            if (currentPage == 0) {
                // Главная страница - популярные предметы
                Material[] popularItems = {
                    Material.DIAMOND, Material.IRON_INGOT, Material.GOLD_INGOT, Material.EMERALD,
                    Material.COAL, Material.REDSTONE, Material.LAPIS_LAZULI, Material.QUARTZ,
                    Material.STONE, Material.DIRT, Material.WOOD, Material.PLANKS,
                    Material.APPLE, Material.BREAD, Material.COOKED_BEEF, Material.TORCH
                };
                
                if (itemIndex < popularItems.length) {
                    onItemSelect.accept(popularItems[itemIndex]);
                    close();
                    return;
                }
            } else {
                // Страница категории
                int categoryIndex = currentPage - 1;
                if (categoryIndex < CATEGORIES.length) {
                    Material[] category = CATEGORIES[categoryIndex];
                    if (itemIndex < category.length) {
                        onItemSelect.accept(category[itemIndex]);
                        close();
                        return;
                    }
                }
            }
        }
        
        // Кнопки управления
        switch (slot) {
            case 45: // Назад
                if (currentPage > 0) {
                    currentPage = 0;
                    setupGUI();
                }
                break;
                
            case 47: // Отмена
                if (onCancel != null) {
                    onCancel.accept(null);
                }
                close();
                break;
                
            case 49: // Поиск предмета
                openItemSearch();
                break;
                
            case 51: // Случайный предмет
                selectRandomItem();
                break;
        }
    }
    
    /**
     * Открытие поиска предмета
     */
    private void openItemSearch() {
        MessageUtil.send(player, 
            "§eВведите название предмета:",
            "§7(или часть названия)"
        );
        
        // Регистрируем временный слушатель чата
        ru.openhousing.listeners.ChatListener.registerTemporaryInput(player, (input) -> {
            String searchQuery = input.trim().toLowerCase();
            
            if (searchQuery.isEmpty()) {
                MessageUtil.send(player, "§cПоисковый запрос не может быть пустым");
                open();
                return;
            }
            
            // Ищем предметы по названию
            List<Material> foundItems = materials.stream()
                .filter(material -> getItemDisplayName(material).toLowerCase().contains(searchQuery))
                .toList();
            
            if (foundItems.isEmpty()) {
                MessageUtil.send(player, "§cПредметы с названием '" + searchQuery + "' не найдены");
                open();
            } else if (foundItems.size() == 1) {
                // Если найден только один предмет, выбираем его
                onItemSelect.accept(foundItems.get(0));
                close();
            } else {
                // Если найдено несколько предметов, показываем их
                MessageUtil.send(player, "§aНайдено предметов: §f" + foundItems.size());
                for (int i = 0; i < Math.min(foundItems.size(), 10); i++) {
                    Material item = foundItems.get(i);
                    MessageUtil.send(player, "§e• " + getItemDisplayName(item));
                }
                if (foundItems.size() > 10) {
                    MessageUtil.send(player, "§7... и еще " + (foundItems.size() - 10) + " предметов");
                }
                MessageUtil.send(player, "§7Уточните поиск или выберите из списка выше");
                open();
            }
        });
    }
    
    /**
     * Выбор случайного предмета
     */
    private void selectRandomItem() {
        int randomIndex = (int) (Math.random() * materials.size());
        Material randomItem = materials.get(randomIndex);
        onItemSelect.accept(randomItem);
        close();
    }
    
    /**
     * Закрытие GUI
     */
    private void close() {
        player.closeInventory();
        InventoryClickEvent.getHandlerList().unregister(this);
    }
}
