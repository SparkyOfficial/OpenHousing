package ru.openhousing.coding.blocks.conditions;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionContext;
import ru.openhousing.coding.blocks.CodeBlock.ExecutionResult;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Блок условия проверки наличия предметов у игрока
 * Проверяет наличие, количество, качество и другие параметры предметов
 */
public class IfPlayerHasItemConditionBlock extends CodeBlock {

    public IfPlayerHasItemConditionBlock() {
        super(BlockType.IF_PLAYER_HAS_ITEM);
        initializeDefaultParameters();
    }

    private void initializeDefaultParameters() {
        setParameter("enabled", true);
        setParameter("checkMainHand", false);
        setParameter("checkOffHand", false);
        setParameter("checkInventory", true);
        setParameter("checkArmor", false);
        setParameter("checkEnderChest", false);
        
        // Параметры предмета
        setParameter("itemType", "STONE");
        setParameter("itemAmount", "1");
        setParameter("checkAmount", true);
        setParameter("amountOperator", ">=");
        setParameter("checkDurability", false);
        setParameter("durability", "0");
        setParameter("durabilityOperator", ">=");
        setParameter("checkEnchantments", false);
        setParameter("requiredEnchantment", "SHARPNESS");
        setParameter("enchantmentLevel", "1");
        setParameter("checkLore", false);
        setParameter("loreContains", "");
        setParameter("checkName", false);
        setParameter("nameContains", "");
        setParameter("checkCustomModelData", false);
        setParameter("customModelData", "0");
        
        // Логические операторы
        setParameter("useAndOperator", true);
        setParameter("useOrOperator", false);
        setParameter("invertCondition", false);
        
        // Дополнительные проверки
        setParameter("checkSlot", false);
        setParameter("slotNumber", "0");
        setParameter("checkHotbar", false);
        setParameter("checkSpecificSlots", false);
        setParameter("specificSlots", "0,1,2,3,4,5,6,7,8");
        setParameter("checkEmptySlots", false);
        setParameter("minEmptySlots", "0");
        setParameter("maxEmptySlots", "36");
        
        // Результат
        setParameter("setVariable", false);
        setParameter("variableName", "hasItem");
        setParameter("setVariableAmount", false);
        setParameter("amountVariableName", "itemAmount");
        setParameter("setVariableSlot", false);
        setParameter("slotVariableName", "itemSlot");
        
        // Действия при успехе
        setParameter("sendMessageOnSuccess", false);
        setParameter("successMessage", "У вас есть нужный предмет!");
        setParameter("playSoundOnSuccess", false);
        setParameter("successSound", "ENTITY_PLAYER_LEVELUP");
        setParameter("showTitleOnSuccess", false);
        setParameter("successTitle", "Предмет найден");
        setParameter("successSubtitle", "Условие выполнено");
        
        // Действия при неудаче
        setParameter("sendMessageOnFailure", false);
        setParameter("failureMessage", "У вас нет нужного предмета!");
        setParameter("playSoundOnFailure", false);
        setParameter("failureSound", "ENTITY_VILLAGER_NO");
        setParameter("showTitleOnFailure", false);
        setParameter("failureTitle", "Предмет не найден");
        setParameter("failureSubtitle", "Условие не выполнено");
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден");
        }

        try {
            // Получаем параметры
            boolean enabled = (Boolean) getParameter("enabled");
            if (!enabled) {
                return ExecutionResult.success();
            }

            boolean result = checkItemConditions(player, context);
            
            // Инвертируем результат если нужно
            if ((Boolean) getParameter("invertCondition")) {
                result = !result;
            }

            // Выполняем действия в зависимости от результата
            if (result) {
                executeSuccessActions(player, context);
            } else {
                executeFailureActions(player, context);
            }

            // Устанавливаем переменные
            if ((Boolean) getParameter("setVariable")) {
                String varName = (String) getParameter("variableName");
                context.setVariable(varName, result);
            }

            if ((Boolean) getParameter("setVariableAmount") && result) {
                String varName = (String) getParameter("amountVariableName");
                int amount = getItemAmount(player, context);
                context.setVariable(varName, amount);
            }

            if ((Boolean) getParameter("setVariableSlot") && result) {
                String varName = (String) getParameter("slotVariableName");
                int slot = findItemSlot(player, context);
                context.setVariable(varName, slot);
            }

            return result ? ExecutionResult.success() : ExecutionResult.error("Предмет не найден");
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения IfPlayerHasItemConditionBlock: " + e.getMessage());
        }
    }

    private boolean checkItemConditions(Player player, ExecutionContext context) {
        String itemType = (String) getParameter("itemType");
        Material targetMaterial = Material.valueOf(itemType);
        
        boolean checkMainHand = (Boolean) getParameter("checkMainHand");
        boolean checkOffHand = (Boolean) getParameter("checkOffHand");
        boolean checkInventory = (Boolean) getParameter("checkInventory");
        boolean checkArmor = (Boolean) getParameter("checkArmor");
        boolean checkEnderChest = (Boolean) getParameter("checkEnderChest");
        
        boolean useAndOperator = (Boolean) getParameter("useAndOperator");
        boolean useOrOperator = (Boolean) getParameter("useOrOperator");
        
        boolean mainHandResult = false;
        boolean offHandResult = false;
        boolean inventoryResult = false;
        boolean armorResult = false;
        boolean enderChestResult = false;
        
        // Проверяем главную руку
        if (checkMainHand) {
            ItemStack mainHandItem = player.getInventory().getItemInMainHand();
            mainHandResult = checkItemStack(mainHandItem, targetMaterial, context);
        }
        
        // Проверяем вторую руку
        if (checkOffHand) {
            ItemStack offHandItem = player.getInventory().getItemInOffHand();
            offHandResult = checkItemStack(offHandItem, targetMaterial, context);
        }
        
        // Проверяем инвентарь
        if (checkInventory) {
            inventoryResult = checkInventoryForItem(player, targetMaterial, context);
        }
        
        // Проверяем броню
        if (checkArmor) {
            armorResult = checkArmorForItem(player, targetMaterial, context);
        }
        
        // Проверяем эндер сундук
        if (checkEnderChest) {
            enderChestResult = checkEnderChestForItem(player, targetMaterial, context);
        }
        
        // Объединяем результаты
        if (useAndOperator) {
            return (checkMainHand ? mainHandResult : true) &&
                   (checkOffHand ? offHandResult : true) &&
                   (checkInventory ? inventoryResult : true) &&
                   (checkArmor ? armorResult : true) &&
                   (checkEnderChest ? enderChestResult : true);
        } else if (useOrOperator) {
            return (checkMainHand && mainHandResult) ||
                   (checkOffHand && offHandResult) ||
                   (checkInventory && inventoryResult) ||
                   (checkArmor && armorResult) ||
                   (checkEnderChest && enderChestResult);
        } else {
            // По умолчанию проверяем только инвентарь
            return inventoryResult;
        }
    }

    private boolean checkItemStack(ItemStack item, Material targetMaterial, ExecutionContext context) {
        if (item == null || item.getType() != targetMaterial) {
            return false;
        }
        
        // Проверяем количество
        if ((Boolean) getParameter("checkAmount")) {
            int requiredAmount = Integer.parseInt((String) getParameter("itemAmount"));
            String operator = (String) getParameter("amountOperator");
            int actualAmount = item.getAmount();
            
            switch (operator) {
                case "==":
                    if (actualAmount != requiredAmount) return false;
                    break;
                case "!=":
                    if (actualAmount == requiredAmount) return false;
                    break;
                case ">":
                    if (actualAmount <= requiredAmount) return false;
                    break;
                case ">=":
                    if (actualAmount < requiredAmount) return false;
                    break;
                case "<":
                    if (actualAmount >= requiredAmount) return false;
                    break;
                case "<=":
                    if (actualAmount > requiredAmount) return false;
                    break;
            }
        }
        
        // Проверяем прочность
        if ((Boolean) getParameter("checkDurability")) {
            int requiredDurability = Integer.parseInt((String) getParameter("durability"));
            String operator = (String) getParameter("durabilityOperator");
            int actualDurability = item.getDurability();
            
            switch (operator) {
                case "==":
                    if (actualDurability != requiredDurability) return false;
                    break;
                case "!=":
                    if (actualDurability == requiredDurability) return false;
                    break;
                case ">":
                    if (actualDurability <= requiredDurability) return false;
                    break;
                case ">=":
                    if (actualDurability < requiredDurability) return false;
                    break;
                case "<":
                    if (actualDurability >= requiredDurability) return false;
                    break;
                case "<=":
                    if (actualDurability > requiredDurability) return false;
                    break;
            }
        }
        
        // Проверяем зачарования
        if ((Boolean) getParameter("checkEnchantments")) {
            String requiredEnchantment = (String) getParameter("requiredEnchantment");
            int requiredLevel = Integer.parseInt((String) getParameter("enchantmentLevel"));
            
            if (!item.containsEnchantment(org.bukkit.enchantments.Enchantment.getByName(requiredEnchantment))) {
                return false;
            }
            
            if (item.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.getByName(requiredEnchantment)) < requiredLevel) {
                return false;
            }
        }
        
        // Проверяем лор
        if ((Boolean) getParameter("checkLore")) {
            String loreContains = (String) getParameter("loreContains");
            if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
                List<String> lore = item.getItemMeta().getLore();
                boolean found = false;
                for (String line : lore) {
                    if (line.contains(loreContains)) {
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
            } else {
                return false;
            }
        }
        
        // Проверяем имя
        if ((Boolean) getParameter("checkName")) {
            String nameContains = (String) getParameter("nameContains");
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                String displayName = item.getItemMeta().getDisplayName();
                if (!displayName.contains(nameContains)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        
        // Проверяем CustomModelData
        if ((Boolean) getParameter("checkCustomModelData")) {
            int requiredModelData = Integer.parseInt((String) getParameter("customModelData"));
            if (item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {
                int modelData = item.getItemMeta().getCustomModelData();
                if (modelData != requiredModelData) {
                    return false;
                }
            } else {
                return false;
            }
        }
        
        return true;
    }

    private boolean checkInventoryForItem(Player player, Material targetMaterial, ExecutionContext context) {
        ItemStack[] contents = player.getInventory().getContents();
        
        // Проверяем конкретный слот
        if ((Boolean) getParameter("checkSlot")) {
            int slot = Integer.parseInt((String) getParameter("slotNumber"));
            if (slot >= 0 && slot < contents.length) {
                return checkItemStack(contents[slot], targetMaterial, context);
            }
            return false;
        }
        
        // Проверяем хотбар
        if ((Boolean) getParameter("checkHotbar")) {
            for (int i = 0; i < 9; i++) {
                if (checkItemStack(contents[i], targetMaterial, context)) {
                    return true;
                }
            }
            return false;
        }
        
        // Проверяем конкретные слоты
        if ((Boolean) getParameter("checkSpecificSlots")) {
            String slotsStr = (String) getParameter("specificSlots");
            String[] slots = slotsStr.split(",");
            for (String slotStr : slots) {
                try {
                    int slot = Integer.parseInt(slotStr.trim());
                    if (slot >= 0 && slot < contents.length) {
                        if (checkItemStack(contents[slot], targetMaterial, context)) {
                            return true;
                        }
                    }
                } catch (NumberFormatException e) {
                    // Игнорируем некорректные номера слотов
                }
            }
            return false;
        }
        
        // Проверяем весь инвентарь
        for (ItemStack item : contents) {
            if (checkItemStack(item, targetMaterial, context)) {
                return true;
            }
        }
        
        return false;
    }

    private boolean checkArmorForItem(Player player, Material targetMaterial, ExecutionContext context) {
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        for (ItemStack item : armorContents) {
            if (checkItemStack(item, targetMaterial, context)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkEnderChestForItem(Player player, Material targetMaterial, ExecutionContext context) {
        ItemStack[] enderContents = player.getEnderChest().getContents();
        for (ItemStack item : enderContents) {
            if (checkItemStack(item, targetMaterial, context)) {
                return true;
            }
        }
        return false;
    }

    private int getItemAmount(Player player, ExecutionContext context) {
        String itemType = (String) getParameter("itemType");
        Material targetMaterial = Material.valueOf(itemType);
        int totalAmount = 0;
        
        // Подсчитываем количество в инвентаре
        ItemStack[] contents = player.getInventory().getContents();
        for (ItemStack item : contents) {
            if (item != null && item.getType() == targetMaterial) {
                totalAmount += item.getAmount();
            }
        }
        
        return totalAmount;
    }

    private int findItemSlot(Player player, ExecutionContext context) {
        String itemType = (String) getParameter("itemType");
        Material targetMaterial = Material.valueOf(itemType);
        
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null && contents[i].getType() == targetMaterial) {
                return i;
            }
        }
        
        return -1; // Предмет не найден
    }

    private void executeSuccessActions(Player player, ExecutionContext context) {
        // Отправляем сообщение об успехе
        if ((Boolean) getParameter("sendMessageOnSuccess")) {
            String message = (String) getParameter("successMessage");
            message = replacePlaceholders(message, player, context);
            player.sendMessage(message);
        }
        
        // Воспроизводим звук успеха
        if ((Boolean) getParameter("playSoundOnSuccess")) {
            try {
                String soundType = (String) getParameter("successSound");
                org.bukkit.Sound sound = org.bukkit.Sound.valueOf(soundType);
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (Exception e) {
                // Игнорируем ошибки звука
            }
        }
        
        // Показываем заголовок успеха
        if ((Boolean) getParameter("showTitleOnSuccess")) {
            String title = (String) getParameter("successTitle");
            String subtitle = (String) getParameter("successSubtitle");
            title = replacePlaceholders(title, player, context);
            subtitle = replacePlaceholders(subtitle, player, context);
            player.sendTitle(title, subtitle, 10, 20, 10);
        }
    }

    private void executeFailureActions(Player player, ExecutionContext context) {
        // Отправляем сообщение о неудаче
        if ((Boolean) getParameter("sendMessageOnFailure")) {
            String message = (String) getParameter("failureMessage");
            message = replacePlaceholders(message, player, context);
            player.sendMessage(message);
        }
        
        // Воспроизводим звук неудачи
        if ((Boolean) getParameter("playSoundOnFailure")) {
            try {
                String soundType = (String) getParameter("failureSound");
                org.bukkit.Sound sound = org.bukkit.Sound.valueOf(soundType);
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (Exception e) {
                // Игнорируем ошибки звука
            }
        }
        
        // Показываем заголовок неудачи
        if ((Boolean) getParameter("showTitleOnFailure")) {
            String title = (String) getParameter("failureTitle");
            String subtitle = (String) getParameter("failureSubtitle");
            title = replacePlaceholders(title, player, context);
            subtitle = replacePlaceholders(subtitle, player, context);
            player.sendTitle(title, subtitle, 10, 20, 10);
        }
    }

    private String replacePlaceholders(String text, Player player, ExecutionContext context) {
        if (text == null) return "";
        
        return text
            .replace("{player}", player.getName())
            .replace("{uuid}", player.getUniqueId().toString())
            .replace("{item}", (String) getParameter("itemType"))
            .replace("{amount}", (String) getParameter("itemAmount"))
            .replace("{world}", player.getWorld().getName())
            .replace("{x}", String.valueOf(player.getLocation().getBlockX()))
            .replace("{y}", String.valueOf(player.getLocation().getBlockY()))
            .replace("{z}", String.valueOf(player.getLocation().getBlockZ()));
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("enabled", getParameter("enabled"));
        params.put("checkMainHand", getParameter("checkMainHand"));
        params.put("checkOffHand", getParameter("checkOffHand"));
        params.put("checkInventory", getParameter("checkInventory"));
        params.put("checkArmor", getParameter("checkArmor"));
        params.put("checkEnderChest", getParameter("checkEnderChest"));
        params.put("itemType", getParameter("itemType"));
        params.put("itemAmount", getParameter("itemAmount"));
        params.put("checkAmount", getParameter("checkAmount"));
        params.put("amountOperator", getParameter("amountOperator"));
        params.put("checkDurability", getParameter("checkDurability"));
        params.put("durability", getParameter("durability"));
        params.put("durabilityOperator", getParameter("durabilityOperator"));
        params.put("checkEnchantments", getParameter("checkEnchantments"));
        params.put("requiredEnchantment", getParameter("requiredEnchantment"));
        params.put("enchantmentLevel", getParameter("enchantmentLevel"));
        params.put("checkLore", getParameter("checkLore"));
        params.put("loreContains", getParameter("loreContains"));
        params.put("checkName", getParameter("checkName"));
        params.put("nameContains", getParameter("nameContains"));
        params.put("checkCustomModelData", getParameter("checkCustomModelData"));
        params.put("customModelData", getParameter("customModelData"));
        params.put("useAndOperator", getParameter("useAndOperator"));
        params.put("useOrOperator", getParameter("useOrOperator"));
        params.put("invertCondition", getParameter("invertCondition"));
        params.put("checkSlot", getParameter("checkSlot"));
        params.put("slotNumber", getParameter("slotNumber"));
        params.put("checkHotbar", getParameter("checkHotbar"));
        params.put("checkSpecificSlots", getParameter("checkSpecificSlots"));
        params.put("specificSlots", getParameter("specificSlots"));
        params.put("checkEmptySlots", getParameter("checkEmptySlots"));
        params.put("minEmptySlots", getParameter("minEmptySlots"));
        params.put("maxEmptySlots", getParameter("maxEmptySlots"));
        params.put("setVariable", getParameter("setVariable"));
        params.put("variableName", getParameter("variableName"));
        params.put("setVariableAmount", getParameter("setVariableAmount"));
        params.put("amountVariableName", getParameter("amountVariableName"));
        params.put("setVariableSlot", getParameter("setVariableSlot"));
        params.put("slotVariableName", getParameter("slotVariableName"));
        params.put("sendMessageOnSuccess", getParameter("sendMessageOnSuccess"));
        params.put("successMessage", getParameter("successMessage"));
        params.put("playSoundOnSuccess", getParameter("playSoundOnSuccess"));
        params.put("successSound", getParameter("successSound"));
        params.put("showTitleOnSuccess", getParameter("showTitleOnSuccess"));
        params.put("successTitle", getParameter("successTitle"));
        params.put("successSubtitle", getParameter("successSubtitle"));
        params.put("sendMessageOnFailure", getParameter("sendMessageOnFailure"));
        params.put("failureMessage", getParameter("failureMessage"));
        params.put("playSoundOnFailure", getParameter("playSoundOnFailure"));
        params.put("failureSound", getParameter("failureSound"));
        params.put("showTitleOnFailure", getParameter("showTitleOnFailure"));
        params.put("failureTitle", getParameter("failureTitle"));
        params.put("failureSubtitle", getParameter("failureSubtitle"));
        return params;
    }

    @Override
    public boolean validate() {
        return true; // Базовая валидация
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("Блок условия проверки предметов");
        description.add("Проверяет наличие предметов у игрока:");
        description.add("- Тип и количество предметов");
        description.add("- Прочность и зачарования");
        description.add("- Лор и имена предметов");
        description.add("- Конкретные слоты инвентаря");
        description.add("- Главная/вторая рука");
        description.add("- Броня и эндер сундук");
        return description;
    }
}
