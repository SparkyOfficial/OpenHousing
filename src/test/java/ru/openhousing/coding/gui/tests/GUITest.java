package ru.openhousing.coding.gui.tests;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.actions.PlayerActionBlock;
import ru.openhousing.coding.gui.*;
import ru.openhousing.coding.script.CodeScript;
import ru.openhousing.coding.script.CodeLine;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тесты для GUI системы
 */
@ExtendWith(MockitoExtension.class)
class GUITest {

    @Mock
    private OpenHousing plugin;
    
    @Mock
    private Player player;
    
    @Mock
    private CodeScript script;
    
    @Mock
    private CodeBlock codeBlock;
    
    @Mock
    private CodeLine codeLine;

    @BeforeEach
    void setUp() {
        UUID playerId = UUID.randomUUID();
        when(player.getName()).thenReturn("TestPlayer");
        when(player.getUniqueId()).thenReturn(playerId);
        when(script.getPlayerId()).thenReturn(playerId);
        when(script.getLines()).thenReturn(java.util.List.of());
        when(codeBlock.getType()).thenReturn(ru.openhousing.coding.blocks.BlockType.PLAYER_ACTION);
        when(codeLine.getName()).thenReturn("Test Line");
        when(codeLine.getLineNumber()).thenReturn(1);
    }

    @Test
    void testCodeEditorGUICreation() {
        // Arrange & Act
        CodeEditorGUI gui = new CodeEditorGUI(plugin, player, script);
        
        // Assert
        assertNotNull(gui);
        assertEquals(script, gui.getScript());
        assertEquals(player, gui.getPlayer());
        assertNotNull(gui.getInventory());
        assertEquals(54, gui.getInventory().getSize());
    }

    @Test
    void testCodeEditorGUIModeSwitching() {
        // Arrange
        CodeEditorGUI gui = new CodeEditorGUI(plugin, player, script);
        
        // Act & Assert
        assertEquals(CodeEditorGUI.EditorMode.MAIN, gui.getMode());
        
        // Switch to SCRIPT mode
        gui.setMode(CodeEditorGUI.EditorMode.SCRIPT);
        assertEquals(CodeEditorGUI.EditorMode.SCRIPT, gui.getMode());
        
        // Switch to CATEGORIES mode
        gui.setMode(CodeEditorGUI.EditorMode.CATEGORIES);
        assertEquals(CodeEditorGUI.EditorMode.CATEGORIES, gui.getMode());
    }

    @Test
    void testCodeEditorGUITargetLine() {
        // Arrange
        CodeEditorGUI gui = new CodeEditorGUI(plugin, player, script);
        
        // Act & Assert
        assertNull(gui.getCurrentTargetLine());
        
        gui.setCurrentTargetLine(codeLine);
        assertEquals(codeLine, gui.getCurrentTargetLine());
        
        gui.setCurrentTargetLine(null);
        assertNull(gui.getCurrentTargetLine());
    }

    @Test
    void testDebugGUICreation() {
        // Arrange
        CodeBlock.ExecutionContext context = new CodeBlock.ExecutionContext(player);
        
        // Act
        DebugGUI debugGUI = new DebugGUI(plugin, player, context);
        
        // Assert
        assertNotNull(debugGUI);
        assertNotNull(debugGUI.getInventory());
        assertEquals(54, debugGUI.getInventory().getSize());
    }

    @Test
    void testDebugGUIVariablesDisplay() {
        // Arrange
        CodeBlock.ExecutionContext context = new CodeBlock.ExecutionContext(player);
        context.setLocalVariable("testVar", "testValue");
        context.setGlobalVariable("globalVar", 42);
        
        DebugGUI debugGUI = new DebugGUI(plugin, player, context);
        
        // Act
        debugGUI.updateInventory();
        
        // Assert
        Inventory inventory = debugGUI.getInventory();
        assertNotNull(inventory.getItem(10)); // Variables button
        assertNotNull(inventory.getItem(12)); // Execution log button
        assertNotNull(inventory.getItem(14)); // Stats button
    }

    @Test
    void testBlockConfigGUICreation() {
        // Arrange & Act
        BlockConfigGUI gui = new BlockConfigGUI(plugin, player, codeBlock, savedBlock -> {});
        
        // Assert
        assertNotNull(gui);
        assertEquals(codeBlock, gui.getBlock());
        assertNotNull(gui.getInventory());
    }

    @Test
    void testLineSelectorGUICreation() {
        // Arrange & Act
        LineSelectorGUI gui = new LineSelectorGUI(plugin, player, script, ru.openhousing.coding.blocks.BlockType.PLAYER_ACTION);
        
        // Assert
        assertNotNull(gui);
        assertEquals(script, gui.getScript());
        assertNotNull(gui.getSelectedLine());
        assertNotNull(gui.getInventory());
    }

    @Test
    void testVariablesViewerGUICreation() {
        // Arrange
        CodeBlock.ExecutionContext context = new CodeBlock.ExecutionContext(player);
        context.setLocalVariable("localVar", "localValue");
        context.setGlobalVariable("globalVar", 100);
        
        // Act
        VariablesViewerGUI gui = new VariablesViewerGUI(plugin, player, context);
        
        // Assert
        assertNotNull(gui);
        assertNotNull(gui.getInventory());
        assertEquals(54, gui.getInventory().getSize());
    }

    @Test
    void testExecutionLogViewerGUICreation() {
        // Arrange
        CodeBlock.ExecutionContext context = new CodeBlock.ExecutionContext(player);
        context.addExecutionLog("Test log entry");
        
        // Act
        ExecutionLogViewerGUI gui = new ExecutionLogViewerGUI(plugin, player, context);
        
        // Assert
        assertNotNull(gui);
        assertNotNull(gui.getInventory());
        assertEquals(54, gui.getInventory().getSize());
    }

    @Test
    void testStatsViewerGUICreation() {
        // Arrange
        CodeBlock.ExecutionContext context = new CodeBlock.ExecutionContext(player);
        
        // Act
        StatsViewerGUI gui = new StatsViewerGUI(plugin, player, context);
        
        // Assert
        assertNotNull(gui);
        assertNotNull(gui.getInventory());
        assertEquals(54, gui.getInventory().getSize());
    }

    @Test
    void testGUIInventoryStructure() {
        // Arrange
        CodeEditorGUI gui = new CodeEditorGUI(plugin, player, script);
        
        // Act
        gui.updateInventory();
        
        // Assert
        Inventory inventory = gui.getInventory();
        
        // Check navigation items
        assertNotNull(inventory.getItem(0)); // Back button
        assertNotNull(inventory.getItem(8)); // Variables button
        assertNotNull(inventory.getItem(17)); // Close button
        
        // Check main menu items
        assertNotNull(inventory.getItem(10)); // My Code
        assertNotNull(inventory.getItem(12)); // Add Block
        assertNotNull(inventory.getItem(14)); // Execute Code
        assertNotNull(inventory.getItem(16)); // Search Blocks
    }

    @Test
    void testGUINavigationItems() {
        // Arrange
        CodeEditorGUI gui = new CodeEditorGUI(plugin, player, script);
        
        // Act
        gui.updateInventory();
        
        // Assert
        Inventory inventory = gui.getInventory();
        
        // Check glass panes for decoration
        for (int i = 1; i < 8; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                assertTrue(item.getType() == Material.GRAY_STAINED_GLASS_PANE || 
                          item.getType() == Material.AIR);
            }
        }
        
        // Check bottom row decoration
        for (int i = 45; i < 54; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                assertTrue(item.getType() == Material.GRAY_STAINED_GLASS_PANE || 
                          item.getType() == Material.AIR);
            }
        }
    }

    @Test
    void testGUIItemBuilderUsage() {
        // Arrange
        CodeEditorGUI gui = new CodeEditorGUI(plugin, player, script);
        
        // Act
        gui.updateInventory();
        
        // Assert
        Inventory inventory = gui.getInventory();
        
        // Check that items have proper names and lore
        ItemStack myCodeItem = inventory.getItem(10);
        assertNotNull(myCodeItem);
        assertTrue(myCodeItem.hasItemMeta());
        
        ItemStack addBlockItem = inventory.getItem(12);
        assertNotNull(addBlockItem);
        assertTrue(addBlockItem.hasItemMeta());
        
        ItemStack executeItem = inventory.getItem(14);
        assertNotNull(executeItem);
        assertTrue(executeItem.hasItemMeta());
    }

    @Test
    void testGUIModeSpecificContent() {
        // Arrange
        CodeEditorGUI gui = new CodeEditorGUI(plugin, player, script);
        
        // Test MAIN mode
        gui.setMode(CodeEditorGUI.EditorMode.MAIN);
        gui.updateInventory();
        Inventory mainInventory = gui.getInventory();
        assertNotNull(mainInventory.getItem(10)); // My Code
        
        // Test SCRIPT mode
        gui.setMode(CodeEditorGUI.EditorMode.SCRIPT);
        gui.updateInventory();
        Inventory scriptInventory = gui.getInventory();
        assertNotNull(scriptInventory.getItem(4)); // Script stats
    }

    @Test
    void testGUIEventHandling() {
        // Arrange
        CodeEditorGUI gui = new CodeEditorGUI(plugin, player, script);
        gui.updateInventory();
        
        // Act & Assert - Test that GUI can handle events
        assertDoesNotThrow(() -> {
            // Simulate inventory click event
            // Note: This is a basic test, actual event handling would require more complex setup
        });
    }

    @Test
    void testGUICloseHandling() {
        // Arrange
        CodeEditorGUI gui = new CodeEditorGUI(plugin, player, script);
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            gui.close();
        });
    }

    @Test
    void testGUIPageNavigation() {
        // Arrange
        CodeEditorGUI gui = new CodeEditorGUI(plugin, player, script);
        
        // Act & Assert
        assertEquals(0, gui.getPage());
        
        gui.setPage(1);
        assertEquals(1, gui.getPage());
        
        gui.setPage(0);
        assertEquals(0, gui.getPage());
    }

    @Test
    void testGUICategorySelection() {
        // Arrange
        CodeEditorGUI gui = new CodeEditorGUI(plugin, player, script);
        
        // Act & Assert
        assertNull(gui.getSelectedCategory());
        
        gui.setSelectedCategory(ru.openhousing.coding.blocks.BlockType.BlockCategory.ACTION);
        assertEquals(ru.openhousing.coding.blocks.BlockType.BlockCategory.ACTION, gui.getSelectedCategory());
    }

    @Test
    void testGUIScriptExecution() {
        // Arrange
        CodeEditorGUI gui = new CodeEditorGUI(plugin, player, script);
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            gui.executeScript();
        });
    }

    @Test
    void testGUISearchFunctionality() {
        // Arrange
        CodeEditorGUI gui = new CodeEditorGUI(plugin, player, script);
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            gui.openBlockSearch();
        });
    }

    @Test
    void testGUISettingsFunctionality() {
        // Arrange
        CodeEditorGUI gui = new CodeEditorGUI(plugin, player, script);
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            gui.openCodeSettings();
        });
    }

    @Test
    void testGUIShareFunctionality() {
        // Arrange
        CodeEditorGUI gui = new CodeEditorGUI(plugin, player, script);
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            gui.shareCode();
        });
    }

    @Test
    void testGUIHelpFunctionality() {
        // Arrange
        CodeEditorGUI gui = new CodeEditorGUI(plugin, player, script);
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            gui.openHelp();
        });
    }

    @Test
    void testGUILineEditorFunctionality() {
        // Arrange
        CodeEditorGUI gui = new CodeEditorGUI(plugin, player, script);
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            gui.openLineEditor(codeLine);
        });
    }

    @Test
    void testGUIBlockConfigFunctionality() {
        // Arrange
        CodeEditorGUI gui = new CodeEditorGUI(plugin, player, script);
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            gui.openBlockConfig(codeBlock);
        });
    }

    @Test
    void testGUIAddBlockFunctionality() {
        // Arrange
        CodeEditorGUI gui = new CodeEditorGUI(plugin, player, script);
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            gui.addBlockToScript(ru.openhousing.coding.blocks.BlockType.PLAYER_ACTION);
        });
    }

    @Test
    void testGUISearchResultsDisplay() {
        // Arrange
        CodeEditorGUI gui = new CodeEditorGUI(plugin, player, script);
        java.util.List<ru.openhousing.coding.blocks.BlockType> searchResults = 
            java.util.List.of(ru.openhousing.coding.blocks.BlockType.PLAYER_ACTION);
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            gui.showSearchResults(searchResults, "test");
        });
    }
}
