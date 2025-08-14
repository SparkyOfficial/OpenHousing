package ru.openhousing.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Утилита для создания ItemStack'ов
 */
public class ItemBuilder {
    
    private final ItemStack itemStack;
    private final ItemMeta meta;
    
    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
        this.meta = itemStack.getItemMeta();
    }
    
    public ItemBuilder(Material material, int amount) {
        this.itemStack = new ItemStack(material, amount);
        this.meta = itemStack.getItemMeta();
    }
    
    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        this.meta = this.itemStack.getItemMeta();
    }
    
    /**
     * Установка имени предмета
     */
    public ItemBuilder name(String name) {
        if (meta != null) {
            meta.setDisplayName(MessageUtil.colorize(name));
        }
        return this;
    }
    
    /**
     * Установка описания предмета
     */
    public ItemBuilder lore(String... lore) {
        return lore(Arrays.asList(lore));
    }
    
    /**
     * Установка описания предмета
     */
    public ItemBuilder lore(List<String> lore) {
        if (meta != null) {
            List<String> colorizedLore = new ArrayList<>();
            for (String line : lore) {
                colorizedLore.add(MessageUtil.colorize(line));
            }
            meta.setLore(colorizedLore);
        }
        return this;
    }
    
    /**
     * Добавление строки к описанию
     */
    public ItemBuilder addLore(String... lines) {
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            
            for (String line : lines) {
                lore.add(MessageUtil.colorize(line));
            }
            
            meta.setLore(lore);
        }
        return this;
    }
    
    /**
     * Установка количества предметов
     */
    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }
    
    /**
     * Добавление чар
     */
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (meta != null) {
            meta.addEnchant(enchantment, level, true);
        }
        return this;
    }
    
    /**
     * Удаление чар
     */
    public ItemBuilder removeEnchant(Enchantment enchantment) {
        if (meta != null) {
            meta.removeEnchant(enchantment);
        }
        return this;
    }
    
    /**
     * Добавление флагов
     */
    public ItemBuilder flags(ItemFlag... flags) {
        if (meta != null) {
            meta.addItemFlags(flags);
        }
        return this;
    }
    
    /**
     * Удаление флагов
     */
    public ItemBuilder removeFlags(ItemFlag... flags) {
        if (meta != null) {
            meta.removeItemFlags(flags);
        }
        return this;
    }
    
    /**
     * Скрытие всех флагов
     */
    public ItemBuilder hideAllFlags() {
        if (meta != null) {
            meta.addItemFlags(ItemFlag.values());
        }
        return this;
    }
    
    /**
     * Установка свечения
     */
    public ItemBuilder glow() {
        return glow(true);
    }
    
    /**
     * Установка свечения
     */
    public ItemBuilder glow(boolean glow) {
        if (glow) {
            enchant(Enchantment.LURE, 1);
            flags(ItemFlag.HIDE_ENCHANTS);
        } else {
            removeEnchant(Enchantment.LURE);
        }
        return this;
    }
    
    /**
     * Установка неразрушимости
     */
    public ItemBuilder unbreakable() {
        return unbreakable(true);
    }
    
    /**
     * Установка неразрушимости
     */
    public ItemBuilder unbreakable(boolean unbreakable) {
        if (meta != null) {
            meta.setUnbreakable(unbreakable);
            if (unbreakable) {
                flags(ItemFlag.HIDE_UNBREAKABLE);
            }
        }
        return this;
    }
    
    /**
     * Установка владельца головы (для PLAYER_HEAD)
     */
    public ItemBuilder skull(String owner) {
        if (meta instanceof SkullMeta) {
            ((SkullMeta) meta).setOwner(owner);
        }
        return this;
    }
    
    /**
     * Установка кастомной модели
     */
    public ItemBuilder customModelData(int data) {
        if (meta != null) {
            meta.setCustomModelData(data);
        }
        return this;
    }
    
    /**
     * Установка головы игрока (только для PLAYER_HEAD)
     */
    public ItemBuilder playerHead(String playerName) {
        if (itemStack.getType() == Material.PLAYER_HEAD) {
            setPlayerHead(itemStack, playerName);
        }
        return this;
    }
    
    /**
     * Создание ItemStack
     */
    public ItemStack build() {
        if (meta != null) {
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }
    
    /**
     * Получение мета данных
     */
    public ItemMeta getMeta() {
        return meta;
    }
    
    /**
     * Клонирование билдера
     */
    public ItemBuilder clone() {
        return new ItemBuilder(build());
    }
    
    // Статические методы для быстрого создания
    
    /**
     * Создание пустого слота
     */
    public static ItemStack createEmptySlot() {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .name(" ")
            .build();
    }
    
    /**
     * Создание разделителя
     */
    public static ItemStack createSeparator() {
        return new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
            .name(" ")
            .build();
    }
    
    /**
     * Создание кнопки "Назад"
     */
    public static ItemStack createBackButton() {
        return new ItemBuilder(Material.ARROW)
            .name("§7Назад")
            .build();
    }
    
    /**
     * Создание кнопки "Закрыть"
     */
    public static ItemStack createCloseButton() {
        return new ItemBuilder(Material.BARRIER)
            .name("§cЗакрыть")
            .build();
    }
    
    /**
     * Создание кнопки "Далее"
     */
    public static ItemStack createNextButton() {
        return new ItemBuilder(Material.ARROW)
            .name("§7Далее")
            .build();
    }
    
    /**
     * Создание кнопки "Предыдущая страница"
     */
    public static ItemStack createPreviousPageButton() {
        return new ItemBuilder(Material.ARROW)
            .name("§7Предыдущая страница")
            .build();
    }
    
    /**
     * Создание кнопки "Следующая страница"
     */
    public static ItemStack createNextPageButton() {
        return new ItemBuilder(Material.ARROW)
            .name("§7Следующая страница")
            .build();
    }
    
    /**
     * Создание информационного предмета
     */
    public static ItemStack createInfo(String title, String... description) {
        return new ItemBuilder(Material.BOOK)
            .name("§e" + title)
            .lore(description)
            .build();
    }
    
    /**
     * Создание предмета успеха
     */
    public static ItemStack createSuccess(String title, String... description) {
        return new ItemBuilder(Material.LIME_DYE)
            .name("§a" + title)
            .lore(description)
            .build();
    }
    
    /**
     * Создание предмета ошибки
     */
    public static ItemStack createError(String title, String... description) {
        return new ItemBuilder(Material.RED_DYE)
            .name("§c" + title)
            .lore(description)
            .build();
    }
    
    /**
     * Создание предмета предупреждения
     */
    public static ItemStack createWarning(String title, String... description) {
        return new ItemBuilder(Material.YELLOW_DYE)
            .name("§e" + title)
            .lore(description)
            .build();
    }
    
    /**
     * Установка головы игрока (статический метод)
     */
    public static void setPlayerHead(ItemStack skull, String playerName) {
        if (skull.getType() != Material.PLAYER_HEAD) {
            return;
        }
        
        try {
            org.bukkit.inventory.meta.SkullMeta skullMeta = (org.bukkit.inventory.meta.SkullMeta) skull.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setOwningPlayer(org.bukkit.Bukkit.getOfflinePlayer(playerName));
                skull.setItemMeta(skullMeta);
            }
        } catch (Exception e) {
            // Если не удалось установить скин, оставляем обычную голову
        }
    }
}
