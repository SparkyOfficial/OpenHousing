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
        super(BlockType.IF_PLAYER_ONLINE); // Используем любое условие игрока как базовое
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
        PlayerConditionType conditionType = (PlayerConditionType) getParameter(ru.openhousing.coding.constants.BlockParams.CONDITION_TYPE);
        String value = (String) getParameter(ru.openhousing.coding.constants.BlockParams.VALUE);
        String compareValue = (String) getParameter(ru.openhousing.coding.constants.BlockParams.COMPARE_VALUE);
        
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
                    try {
                        double healthAbove = Double.parseDouble(value);
                        if (healthAbove < 0 || healthAbove > 20) {
                            if (player != null) {
                                player.sendMessage("§c[OpenHousing] Здоровье должно быть от 0 до 20. Используется значение по умолчанию: 10");
                            }
                            healthAbove = Math.min(Math.max(healthAbove, 0), 20);
                        }
                        return player.getHealth() > healthAbove;
                    } catch (NumberFormatException e) {
                        if (player != null) {
                            player.sendMessage("§c[OpenHousing] Неверное значение здоровья: '" + value + "'. Ожидается число от 0 до 20");
                        }
                        return false;
                    }
                    
                case HEALTH_BELOW:
                    try {
                        double healthBelow = Double.parseDouble(value);
                        if (healthBelow < 0 || healthBelow > 20) {
                            if (player != null) {
                                player.sendMessage("§c[OpenHousing] Здоровье должно быть от 0 до 20. Используется значение по умолчанию: 10");
                            }
                            healthBelow = Math.min(Math.max(healthBelow, 0), 20);
                        }
                        return player.getHealth() < healthBelow;
                    } catch (NumberFormatException e) {
                        if (player != null) {
                            player.sendMessage("§c[OpenHousing] Неверное значение здоровья: '" + value + "'. Ожидается число от 0 до 20");
                        }
                        return false;
                    }
                    
                case LEVEL_ABOVE:
                    try {
                        int levelAbove = Integer.parseInt(value);
                        if (levelAbove < 0 || levelAbove > 1000) {
                            if (player != null) {
                                player.sendMessage("§c[OpenHousing] Уровень должен быть от 0 до 1000. Используется значение по умолчанию: 50");
                            }
                            levelAbove = Math.min(Math.max(levelAbove, 0), 1000);
                        }
                        return player.getLevel() > levelAbove;
                    } catch (NumberFormatException e) {
                        if (player != null) {
                            player.sendMessage("§c[OpenHousing] Неверный уровень: '" + value + "'. Ожидается число от 0 до 1000");
                        }
                        return false;
                    }
                    
                case LEVEL_BELOW:
                    try {
                        int levelBelow = Integer.parseInt(value);
                        if (levelBelow < 0 || levelBelow > 1000) {
                            if (player != null) {
                                player.sendMessage("§c[OpenHousing] Уровень должен быть от 0 до 1000. Используется значение по умолчанию: 50");
                            }
                            levelBelow = Math.min(Math.max(levelBelow, 0), 1000);
                        }
                        return player.getLevel() < levelBelow;
                    } catch (NumberFormatException e) {
                        if (player != null) {
                            player.sendMessage("§c[OpenHousing] Неверный уровень: '" + value + "'. Ожидается число от 0 до 1000");
                        }
                        return false;
                    }
                    
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
                        try {
                            String worldName = locationParts[0];
                            double x = Double.parseDouble(locationParts[1]);
                            double y = Double.parseDouble(locationParts[2]);
                            double z = Double.parseDouble(locationParts[3]);
                            double maxDistance = Double.parseDouble(locationParts[4]);
                            
                            // Проверяем разумность расстояния
                            if (maxDistance < 0 || maxDistance > 1000) {
                                if (player != null) {
                                    player.sendMessage("§c[OpenHousing] Расстояние должно быть от 0 до 1000. Используется значение по умолчанию: 10");
                                }
                                maxDistance = Math.min(Math.max(maxDistance, 0), 1000);
                            }
                            
                            if (player.getWorld().getName().equals(worldName)) {
                                double distance = player.getLocation().distance(new org.bukkit.Location(
                                    player.getWorld(), x, y, z
                                ));
                                return distance <= maxDistance;
                            } else {
                                if (player != null) {
                                    player.sendMessage("§c[OpenHousing] Игрок находится в мире '" + player.getWorld().getName() + "', а не в '" + worldName + "'");
                                }
                            }
                        } catch (NumberFormatException e) {
                            if (player != null) {
                                player.sendMessage("§c[OpenHousing] Неверный формат локации: '" + value + "'. Ожидается: мир,x,y,z,расстояние");
                            }
                        }
                    } else {
                        if (player != null) {
                            player.sendMessage("§c[OpenHousing] Неверный формат локации: '" + value + "'. Ожидается 5 значений через запятую: мир,x,y,z,расстояние");
                        }
                    }
                    return false;
                    
                case HAS_MONEY:
                    // Требует Vault
                    try {
                        ru.openhousing.OpenHousing plugin = ru.openhousing.OpenHousing.getInstance();
                        if (plugin.getEconomy() != null) {
                            double requiredMoney = Double.parseDouble(value);
                            if (requiredMoney < 0) {
                                if (player != null) {
                                    player.sendMessage("§c[OpenHousing] Сумма денег не может быть отрицательной.");
                                }
                                return false;
                            } else if (requiredMoney > 1000000) {
                                if (player != null) {
                                    player.sendMessage("§c[OpenHousing] Слишком большая сумма: " + requiredMoney + ". Максимум: 1,000,000");
                                }
                                return false;
                            }
                            return plugin.getEconomy().getBalance(player) >= requiredMoney;
                        } else {
                            if (player != null) {
                                player.sendMessage("§c[OpenHousing] Экономика недоступна на этом сервере");
                            }
                        }
                    } catch (NumberFormatException e) {
                        if (player != null) {
                            player.sendMessage("§c[OpenHousing] Неверная сумма денег: '" + value + "'. Ожидается число");
                        }
                    } catch (Exception e) {
                        if (player != null) {
                            player.sendMessage("§c[OpenHousing] Ошибка при проверке баланса: " + e.getMessage());
                        }
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
        return getParameter(ru.openhousing.coding.constants.BlockParams.CONDITION_TYPE) != null && getParameter(ru.openhousing.coding.constants.BlockParams.VALUE) != null;
    }
    
    @Override
    public List<String> getDescription() {
        PlayerConditionType conditionType = (PlayerConditionType) getParameter(ru.openhousing.coding.constants.BlockParams.CONDITION_TYPE);
        String value = (String) getParameter(ru.openhousing.coding.constants.BlockParams.VALUE);
        
        return Arrays.asList(
            "§6Если игрок",
            "§7Условие: §f" + (conditionType != null ? conditionType.getDisplayName() : "Не выбрано"),
            "§7Значение: §f" + (value != null ? value : "Не указано"),
            "",
            "§8Дочерних блоков: " + childBlocks.size()
        );
    }
}
