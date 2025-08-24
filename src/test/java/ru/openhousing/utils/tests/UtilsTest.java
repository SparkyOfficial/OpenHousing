package ru.openhousing.utils.tests;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.openhousing.utils.ItemBuilder;
import ru.openhousing.utils.MessageUtil;
import ru.openhousing.utils.AnvilGUIHelper;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для утилит плагина
 */
@ExtendWith(MockitoExtension.class)
class UtilsTest {

    @Test
    void testItemBuilderCreation() {
        // Arrange & Act
        ItemBuilder builder = new ItemBuilder(Material.DIAMOND);
        
        // Assert
        assertNotNull(builder);
    }

    @Test
    void testItemBuilderName() {
        // Arrange
        ItemBuilder builder = new ItemBuilder(Material.DIAMOND);
        String name = "Test Item";
        
        // Act
        ItemBuilder result = builder.name(name);
        ItemStack item = result.build();
        
        // Assert
        assertNotNull(item);
        assertTrue(item.hasItemMeta());
        ItemMeta meta = item.getItemMeta();
        assertNotNull(meta);
        assertEquals(name, meta.getDisplayName());
    }

    @Test
    void testItemBuilderLore() {
        // Arrange
        ItemBuilder builder = new ItemBuilder(Material.DIAMOND);
        List<String> lore = Arrays.asList("Line 1", "Line 2", "Line 3");
        
        // Act
        ItemBuilder result = builder.lore(lore);
        ItemStack item = result.build();
        
        // Assert
        assertNotNull(item);
        assertTrue(item.hasItemMeta());
        ItemMeta meta = item.getItemMeta();
        assertNotNull(meta);
        assertNotNull(meta.getLore());
        assertEquals(lore, meta.getLore());
    }

    @Test
    void testItemBuilderLoreVarargs() {
        // Arrange
        ItemBuilder builder = new ItemBuilder(Material.DIAMOND);
        
        // Act
        ItemBuilder result = builder.lore("Line 1", "Line 2", "Line 3");
        ItemStack item = result.build();
        
        // Assert
        assertNotNull(item);
        assertTrue(item.hasItemMeta());
        ItemMeta meta = item.getItemMeta();
        assertNotNull(meta);
        assertNotNull(meta.getLore());
        assertEquals(3, meta.getLore().size());
        assertEquals("Line 1", meta.getLore().get(0));
        assertEquals("Line 2", meta.getLore().get(1));
        assertEquals("Line 3", meta.getLore().get(2));
    }

    @Test
    void testItemBuilderAmount() {
        // Arrange
        ItemBuilder builder = new ItemBuilder(Material.DIAMOND);
        int amount = 64;
        
        // Act
        ItemBuilder result = builder.amount(amount);
        ItemStack item = result.build();
        
        // Assert
        assertNotNull(item);
        assertEquals(amount, item.getAmount());
    }

    @Test
    void testItemBuilderDurability() {
        // Arrange
        ItemBuilder builder = new ItemBuilder(Material.DIAMOND_PICKAXE);
        short durability = 100;
        
        // Act
        ItemBuilder result = builder.durability(durability);
        ItemStack item = result.build();
        
        // Assert
        assertNotNull(item);
        assertEquals(durability, item.getDurability());
    }

    @Test
    void testItemBuilderEnchantment() {
        // Arrange
        ItemBuilder builder = new ItemBuilder(Material.DIAMOND_SWORD);
        
        // Act
        ItemBuilder result = builder.enchant(org.bukkit.enchantments.Enchantment.DAMAGE_ALL, 1);
        ItemStack item = result.build();
        
        // Assert
        assertNotNull(item);
        assertTrue(item.containsEnchantment(org.bukkit.enchantments.Enchantment.DAMAGE_ALL));
        assertEquals(1, item.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.DAMAGE_ALL));
    }

    @Test
    void testItemBuilderGlow() {
        // Arrange
        ItemBuilder builder = new ItemBuilder(Material.DIAMOND);
        
        // Act
        ItemBuilder result = builder.glow();
        ItemStack item = result.build();
        
        // Assert
        assertNotNull(item);
        assertTrue(item.containsEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY));
    }

    @Test
    void testItemBuilderUnbreakable() {
        // Arrange
        ItemBuilder builder = new ItemBuilder(Material.DIAMOND);
        
        // Act
        ItemBuilder result = builder.unbreakable();
        ItemStack item = result.build();
        
        // Assert
        assertNotNull(item);
        assertTrue(item.hasItemMeta());
        ItemMeta meta = item.getItemMeta();
        assertNotNull(meta);
        assertTrue(meta.isUnbreakable());
    }

    @Test
    void testItemBuilderCustomModelData() {
        // Arrange
        ItemBuilder builder = new ItemBuilder(Material.DIAMOND);
        int customModelData = 12345;
        
        // Act
        ItemBuilder result = builder.customModelData(customModelData);
        ItemStack item = result.build();
        
        // Assert
        assertNotNull(item);
        assertTrue(item.hasItemMeta());
        ItemMeta meta = item.getItemMeta();
        assertNotNull(meta);
        assertEquals(customModelData, meta.getCustomModelData());
    }

    @Test
    void testItemBuilderChaining() {
        // Arrange
        ItemBuilder builder = new ItemBuilder(Material.DIAMOND);
        
        // Act
        ItemBuilder result = builder
            .name("Test Item")
            .lore("Line 1", "Line 2")
            .amount(5)
            .glow();
        ItemStack item = result.build();
        
        // Assert
        assertNotNull(item);
        assertEquals(Material.DIAMOND, item.getType());
        assertEquals(5, item.getAmount());
        assertTrue(item.hasItemMeta());
        ItemMeta meta = item.getItemMeta();
        assertNotNull(meta);
        assertEquals("Test Item", meta.getDisplayName());
        assertNotNull(meta.getLore());
        assertEquals(2, meta.getLore().size());
        assertTrue(item.containsEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY));
    }

    @Test
    void testItemBuilderEmptyLore() {
        // Arrange
        ItemBuilder builder = new ItemBuilder(Material.DIAMOND);
        
        // Act
        ItemBuilder result = builder.lore();
        ItemStack item = result.build();
        
        // Assert
        assertNotNull(item);
        assertTrue(item.hasItemMeta());
        ItemMeta meta = item.getItemMeta();
        assertNotNull(meta);
        assertNull(meta.getLore());
    }

    @Test
    void testItemBuilderNullLore() {
        // Arrange
        ItemBuilder builder = new ItemBuilder(Material.DIAMOND);
        
        // Act
        ItemBuilder result = builder.lore((List<String>) null);
        ItemStack item = result.build();
        
        // Assert
        assertNotNull(item);
        assertTrue(item.hasItemMeta());
        ItemMeta meta = item.getItemMeta();
        assertNotNull(meta);
        assertNull(meta.getLore());
    }

    @Test
    void testItemBuilderEmptyName() {
        // Arrange
        ItemBuilder builder = new ItemBuilder(Material.DIAMOND);
        
        // Act
        ItemBuilder result = builder.name("");
        ItemStack item = result.build();
        
        // Assert
        assertNotNull(item);
        assertTrue(item.hasItemMeta());
        ItemMeta meta = item.getItemMeta();
        assertNotNull(meta);
        assertEquals("", meta.getDisplayName());
    }

    @Test
    void testItemBuilderNullName() {
        // Arrange
        ItemBuilder builder = new ItemBuilder(Material.DIAMOND);
        
        // Act
        ItemBuilder result = builder.name(null);
        ItemStack item = result.build();
        
        // Assert
        assertNotNull(item);
        assertTrue(item.hasItemMeta());
        ItemMeta meta = item.getItemMeta();
        assertNotNull(meta);
        assertNull(meta.getDisplayName());
    }

    @Test
    void testItemBuilderInvalidAmount() {
        // Arrange
        ItemBuilder builder = new ItemBuilder(Material.DIAMOND);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            builder.amount(0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            builder.amount(-1);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            builder.amount(65);
        });
    }

    @Test
    void testItemBuilderInvalidDurability() {
        // Arrange
        ItemBuilder builder = new ItemBuilder(Material.DIAMOND_PICKAXE);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            builder.durability(-1);
        });
    }

    @Test
    void testMessageUtilColorize() {
        // Arrange
        String message = "&aHello &bWorld &cTest";
        
        // Act
        String result = MessageUtil.colorize(message);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("§a"));
        assertTrue(result.contains("§b"));
        assertTrue(result.contains("§c"));
    }

    @Test
    void testMessageUtilColorizeNull() {
        // Arrange
        String message = null;
        
        // Act
        String result = MessageUtil.colorize(message);
        
        // Assert
        assertNull(result);
    }

    @Test
    void testMessageUtilColorizeEmpty() {
        // Arrange
        String message = "";
        
        // Act
        String result = MessageUtil.colorize(message);
        
        // Assert
        assertEquals("", result);
    }

    @Test
    void testMessageUtilColorizeNoColors() {
        // Arrange
        String message = "Hello World Test";
        
        // Act
        String result = MessageUtil.colorize(message);
        
        // Assert
        assertEquals(message, result);
    }

    @Test
    void testMessageUtilColorizeSpecialCharacters() {
        // Arrange
        String message = "&aHello &bWorld &cTest &d&e&f";
        
        // Act
        String result = MessageUtil.colorize(message);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("§a"));
        assertTrue(result.contains("§b"));
        assertTrue(result.contains("§c"));
        assertTrue(result.contains("§d"));
        assertTrue(result.contains("§e"));
        assertTrue(result.contains("§f"));
    }

    @Test
    void testMessageUtilStripColors() {
        // Arrange
        String message = "§aHello §bWorld §cTest";
        
        // Act
        String result = MessageUtil.stripColors(message);
        
        // Assert
        assertEquals("Hello World Test", result);
    }

    @Test
    void testMessageUtilStripColorsNull() {
        // Arrange
        String message = null;
        
        // Act
        String result = MessageUtil.stripColors(message);
        
        // Assert
        assertNull(result);
    }

    @Test
    void testMessageUtilStripColorsEmpty() {
        // Arrange
        String message = "";
        
        // Act
        String result = MessageUtil.stripColors(message);
        
        // Assert
        assertEquals("", result);
    }

    @Test
    void testMessageUtilStripColorsNoColors() {
        // Arrange
        String message = "Hello World Test";
        
        // Act
        String result = MessageUtil.stripColors(message);
        
        // Assert
        assertEquals(message, result);
    }

    @Test
    void testMessageUtilFormat() {
        // Arrange
        String template = "Hello {name}, you have {count} items";
        String name = "Player";
        int count = 5;
        
        // Act
        String result = MessageUtil.format(template, "name", name, "count", count);
        
        // Assert
        assertEquals("Hello Player, you have 5 items", result);
    }

    @Test
    void testMessageUtilFormatNullTemplate() {
        // Arrange
        String template = null;
        
        // Act
        String result = MessageUtil.format(template, "name", "Player");
        
        // Assert
        assertNull(result);
    }

    @Test
    void testMessageUtilFormatEmptyTemplate() {
        // Arrange
        String template = "";
        
        // Act
        String result = MessageUtil.format(template, "name", "Player");
        
        // Assert
        assertEquals("", result);
    }

    @Test
    void testMessageUtilFormatNoPlaceholders() {
        // Arrange
        String template = "Hello World";
        
        // Act
        String result = MessageUtil.format(template, "name", "Player");
        
        // Assert
        assertEquals(template, result);
    }

    @Test
    void testMessageUtilFormatOddArguments() {
        // Arrange
        String template = "Hello {name}";
        
        // Act
        String result = MessageUtil.format(template, "name", "Player", "extra");
        
        // Assert
        assertEquals("Hello Player", result);
    }

    @Test
    void testAnvilGUIHelperAvailability() {
        // Arrange & Act
        boolean available = AnvilGUIHelper.isAnvilGUIAvailable();
        
        // Assert
        // In test environment, AnvilGUI is not available
        assertFalse(available);
    }

    @Test
    void testAnvilGUIHelperCheckAvailability() {
        // Arrange & Act
        boolean available = AnvilGUIHelper.checkAnvilGUIAvailability();
        
        // Assert
        // In test environment, AnvilGUI is not available
        assertFalse(available);
    }

    @Test
    void testAnvilGUIHelperOpenTextInput() {
        // Arrange
        String title = "Enter text";
        String defaultText = "default";
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            AnvilGUIHelper.openTextInput(null, title, defaultText, text -> {});
        });
    }

    @Test
    void testAnvilGUIHelperOpenNumberInput() {
        // Arrange
        String title = "Enter number";
        String defaultText = "0";
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            AnvilGUIHelper.openNumberInput(null, title, defaultText, number -> {});
        });
    }

    @Test
    void testAnvilGUIHelperFallbackToChatInput() {
        // Arrange
        String message = "Please enter text:";
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            AnvilGUIHelper.fallbackToChatInput(null, message, text -> {});
        });
    }

    @Test
    void testItemBuilderClone() {
        // Arrange
        ItemBuilder original = new ItemBuilder(Material.DIAMOND)
            .name("Original")
            .lore("Original lore");
        
        // Act
        ItemBuilder clone = original.clone();
        ItemStack originalItem = original.build();
        ItemStack cloneItem = clone.build();
        
        // Assert
        assertNotNull(clone);
        assertNotSame(original, clone);
        assertEquals(originalItem.getType(), cloneItem.getType());
        assertEquals(originalItem.getItemMeta().getDisplayName(), cloneItem.getItemMeta().getDisplayName());
        assertEquals(originalItem.getItemMeta().getLore(), cloneItem.getItemMeta().getLore());
    }

    @Test
    void testItemBuilderModifyClone() {
        // Arrange
        ItemBuilder original = new ItemBuilder(Material.DIAMOND)
            .name("Original")
            .lore("Original lore");
        
        // Act
        ItemBuilder clone = original.clone();
        clone.name("Modified").lore("Modified lore");
        
        ItemStack originalItem = original.build();
        ItemStack cloneItem = clone.build();
        
        // Assert
        assertEquals("Original", originalItem.getItemMeta().getDisplayName());
        assertEquals("Modified", cloneItem.getItemMeta().getDisplayName());
        assertEquals("Original lore", originalItem.getItemMeta().getLore().get(0));
        assertEquals("Modified lore", cloneItem.getItemMeta().getLore().get(0));
    }

    @Test
    void testMessageUtilColorizeList() {
        // Arrange
        List<String> messages = Arrays.asList("&aHello", "&bWorld", "&cTest");
        
        // Act
        List<String> result = MessageUtil.colorizeList(messages);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.get(0).contains("§a"));
        assertTrue(result.get(1).contains("§b"));
        assertTrue(result.get(2).contains("§c"));
    }

    @Test
    void testMessageUtilColorizeListNull() {
        // Arrange
        List<String> messages = null;
        
        // Act
        List<String> result = MessageUtil.colorizeList(messages);
        
        // Assert
        assertNull(result);
    }

    @Test
    void testMessageUtilColorizeListEmpty() {
        // Arrange
        List<String> messages = Arrays.asList();
        
        // Act
        List<String> result = MessageUtil.colorizeList(messages);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testMessageUtilStripColorsList() {
        // Arrange
        List<String> messages = Arrays.asList("§aHello", "§bWorld", "§cTest");
        
        // Act
        List<String> result = MessageUtil.stripColorsList(messages);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Hello", result.get(0));
        assertEquals("World", result.get(1));
        assertEquals("Test", result.get(2));
    }

    @Test
    void testMessageUtilStripColorsListNull() {
        // Arrange
        List<String> messages = null;
        
        // Act
        List<String> result = MessageUtil.stripColorsList(messages);
        
        // Assert
        assertNull(result);
    }

    @Test
    void testMessageUtilStripColorsListEmpty() {
        // Arrange
        List<String> messages = Arrays.asList();
        
        // Act
        List<String> result = MessageUtil.stripColorsList(messages);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
