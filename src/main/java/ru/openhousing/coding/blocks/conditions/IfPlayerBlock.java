package ru.openhousing.coding.blocks.conditions;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок условия "Если игрок"
 */
public class IfPlayerBlock extends CodeBlock {
    
    public enum PlayerConditionType {
        HAS_PERMISSION("Имеет разрешение", "Проверяет наличие разрешения"),
        IN_GAMEMODE("В режиме игры", "Проверяет режим игры"),
        HAS_ITEM("Имеет предмет", "Проверяет наличие предмета"),
        HEALTH_ABOVE("Здоровье больше", "Проверяет уровень здоровья"),
        HEALTH_BELOW("Здоровье меньше", "Проверяет уровень здоровья"),
        LEVEL_ABOVE("Уровень больше", "Проверяет уровень опыта"),
        LEVEL_BELOW("Уровень меньше", "Проверяет уровень опыта"),
        IS_SNEAKING("Приседает", "Проверяет, приседает ли игрок"),
        IS_SPRINTING("Бежит", "Проверяет, бежит ли игрок"),
        IS_FLYING("Летает", "Проверяет, летает ли игрок"),
        IS_ONLINE("В сети", "Проверяет, онлайн ли игрок"),
        IS_OP("Оператор", "Проверяет, является ли игрок оператором"),
        IN_WORLD("В мире", "Проверяет нахождение в определенном мире"),
        NEAR_LOCATION("Рядом с локацией", "Проверяет расстояние до локации"),
        HAS_MONEY("Имеет деньги", "Проверяет количество денег"),
        NAME_EQUALS("Имя равно", "Проверяет имя игрока"),
        NAME_CONTAINS("Имя содержит", "Проверяет часть имени игрока");
        
        private final String displayName;
        private final String description;
        
        PlayerConditionType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public IfPlayerBlock() {
        super(BlockType.IF_PLAYER);
        setParameter("conditionType", PlayerConditionType.HAS_PERMISSION);
        setParameter("value", "");
        setParameter("compareValue", "");
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден");
        }
        
        boolean conditionMet = checkCondition(player, context);
        
        if (conditionMet) {
            return executeChildren(context);
        }
        
        return ExecutionResult.success();
    }
    
    private boolean checkCondition(Player player, ExecutionContext context) {
        PlayerConditionType conditionType = (PlayerConditionType) getParameter("conditionType");
        String value = (String) getParameter("value");
        String compareValue = (String) getParameter("compareValue");
        
        if (conditionType == null || value == null) {
            return false;
        }
        
        try {
            switch (conditionType) {
                case HAS_PERMISSION:
                    return player.hasPermission(value);
                    
                case IN_GAMEMODE:
                    GameMode gameMode = GameMode.valueOf(value.toUpperCase());
                    return player.getGameMode() == gameMode;
                    
                case HAS_ITEM:
                    return player.getInventory().contains(org.bukkit.Material.valueOf(value.toUpperCase()));
                    
                case HEALTH_ABOVE:
                    double healthAbove = Double.parseDouble(value);
                    return player.getHealth() > healthAbove;
                    
                case HEALTH_BELOW:
                    double healthBelow = Double.parseDouble(value);
                    return player.getHealth() < healthBelow;
                    
                case LEVEL_ABOVE:
                    int levelAbove = Integer.parseInt(value);
                    return player.getLevel() > levelAbove;
                    
                case LEVEL_BELOW:
                    int levelBelow = Integer.parseInt(value);
                    return player.getLevel() < levelBelow;
                    
                case IS_SNEAKING:
                    return player.isSneaking();
                    
                case IS_SPRINTING:
                    return player.isSprinting();
                    
                case IS_FLYING:
                    return player.isFlying();
                    
                case IS_ONLINE:
                    return player.isOnline();
                    
                case IS_OP:
                    return player.isOp();
                    
                case IN_WORLD:
                    return player.getWorld().getName().equals(value);
                    
                case NEAR_LOCATION:
                    // Формат: world,x,y,z,distance
                    String[] locationParts = value.split(",");
                    if (locationParts.length == 5) {
                        String worldName = locationParts[0];
                        double x = Double.parseDouble(locationParts[1]);
                        double y = Double.parseDouble(locationParts[2]);
                        double z = Double.parseDouble(locationParts[3]);
                        double maxDistance = Double.parseDouble(locationParts[4]);
                        
                        if (player.getWorld().getName().equals(worldName)) {
                            double distance = player.getLocation().distance(new org.bukkit.Location(
                                player.getWorld(), x, y, z
                            ));
                            return distance <= maxDistance;
                        }
                    }
                    return false;
                    
                case HAS_MONEY:
                    // Требует Vault
                    try {
                        ru.openhousing.OpenHousing plugin = ru.openhousing.OpenHousing.getInstance();
                        if (plugin.getEconomy() != null) {
                            double requiredMoney = Double.parseDouble(value);
                            return plugin.getEconomy().getBalance(player) >= requiredMoney;
                        }
                    } catch (Exception e) {
                        return false;
                    }
                    return false;
                    
                case NAME_EQUALS:
                    return player.getName().equals(value);
                    
                case NAME_CONTAINS:
                    return player.getName().toLowerCase().contains(value.toLowerCase());
                    
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean validate() {
        return getParameter("conditionType") != null && getParameter("value") != null;
    }
    
    @Override
    public List<String> getDescription() {
        PlayerConditionType conditionType = (PlayerConditionType) getParameter("conditionType");
        String value = (String) getParameter("value");
        
        return Arrays.asList(
            "§6Если игрок",
            "§7Условие: §f" + (conditionType != null ? conditionType.getDisplayName() : "Не выбрано"),
            "§7Значение: §f" + (value != null ? value : "Не указано"),
            "",
            "§8Дочерних блоков: " + childBlocks.size()
        );
    }
}
