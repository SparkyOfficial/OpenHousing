package ru.openhousing.coding.blocks.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок события ломания блоков игроком
 */
public class PlayerBlockBreakEventBlock extends CodeBlock {
    
    public PlayerBlockBreakEventBlock() {
        super(BlockType.PLAYER_BLOCK_BREAK);
        setParameter("blockType", "ANY");
        setParameter("toolType", "ANY");
        setParameter("breakMessage", "§aВы сломали %block_type%!");
        setParameter("showTitle", false);
        setParameter("titleText", "§aБлок сломан!");
        setParameter("subtitleText", "§7%block_type%");
        setParameter("playSound", true);
        setParameter("soundType", "BLOCK_STONE_BREAK");
        setParameter("dropItems", true);
        setParameter("customDrops", false);
        setParameter("dropMultiplier", 1.0);
        setParameter("giveExperience", true);
        setParameter("experienceAmount", 0);
        setParameter("experienceMultiplier", 1.0);
        setParameter("cancelBreak", false);
        setParameter("replaceBlock", false);
        setParameter("replacementBlock", "AIR");
        setParameter("replacementDelay", 0);
        setParameter("spawnParticles", true);
        setParameter("particleType", "BLOCK_CRACK");
        setParameter("particleCount", 10);
        setParameter("addEffects", false);
        setParameter("effects", "HASTE:30:1");
        setParameter("trackStatistics", true);
        setParameter("statVariable", "blocks_broken");
        setParameter("blockTypeVariable", "last_broken_block");
        setParameter("locationVariable", "last_break_location");
        setParameter("toolVariable", "last_break_tool");
        setParameter("breakTimeVariable", "last_break_time");
        setParameter("broadcastBreak", false);
        setParameter("logBreak", false);
        setParameter("checkPermissions", false);
        setParameter("permissionNode", "openhousing.break");
        setParameter("denyMessage", "§cУ вас нет разрешения ломать этот блок!");
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден");
        }
        
        try {
            // Получаем информацию о блоке из контекста
            Block block = (Block) context.getVariable("block");
            if (block == null) {
                return ExecutionResult.error("Блок не найден в контексте");
            }
            
            Material blockType = block.getType();
            ItemStack tool = player.getInventory().getItemInMainHand();
            Material toolType = tool.getType();
            
            // Проверка разрешений
            if ((Boolean) getParameter("checkPermissions")) {
                String permissionNode = (String) getParameter("permissionNode");
                if (permissionNode != null && !player.hasPermission(permissionNode)) {
                    String denyMessage = replaceVariables((String) getParameter("denyMessage"), context);
                    player.sendMessage(denyMessage);
                    return ExecutionResult.error("Недостаточно прав");
                }
            }
            
            // Проверка типа блока
            String requiredBlockType = (String) getParameter("blockType");
            if (!"ANY".equals(requiredBlockType) && !blockType.name().equals(requiredBlockType)) {
                return ExecutionResult.success(); // Не подходящий блок, пропускаем
            }
            
            // Проверка типа инструмента
            String requiredToolType = (String) getParameter("toolType");
            if (!"ANY".equals(requiredToolType) && !toolType.name().equals(requiredToolType)) {
                return ExecutionResult.success(); // Не подходящий инструмент, пропускаем
            }
            
            // Сообщение о ломании
            String breakMessage = replaceVariables((String) getParameter("breakMessage"), context);
            if (breakMessage != null && !breakMessage.trim().isEmpty()) {
                player.sendMessage(breakMessage);
            }
            
            // Показать заголовок
            if ((Boolean) getParameter("showTitle")) {
                String titleText = replaceVariables((String) getParameter("titleText"), context);
                String subtitleText = replaceVariables((String) getParameter("subtitleText"), context);
                player.sendTitle(titleText, subtitleText, 10, 70, 20);
            }
            
            // Воспроизвести звук
            if ((Boolean) getParameter("playSound")) {
                String soundType = (String) getParameter("soundType");
                if (soundType != null) {
                    try {
                        org.bukkit.Sound sound = org.bukkit.Sound.valueOf(soundType);
                        player.playSound(block.getLocation(), sound, 1.0f, 1.0f);
                    } catch (IllegalArgumentException e) {
                        // Звук не найден, игнорируем
                    }
                }
            }
            
            // Настройки дропа предметов
            boolean dropItems = (Boolean) getParameter("dropItems");
            boolean customDrops = (Boolean) getParameter("customDrops");
            Double dropMultiplier = (Double) getParameter("dropMultiplier");
            
            if (dropItems && customDrops && dropMultiplier != null && dropMultiplier > 0) {
                // Кастомные дропы
                ItemStack dropItem = new ItemStack(blockType);
                dropItem.setAmount((int) Math.max(1, dropItem.getAmount() * dropMultiplier));
                block.getWorld().dropItemNaturally(block.getLocation(), dropItem);
            }
            
            // Дать опыт
            if ((Boolean) getParameter("giveExperience")) {
                Integer expAmount = (Integer) getParameter("experienceAmount");
                Double expMultiplier = (Double) getParameter("experienceMultiplier");
                
                if (expAmount != null && expMultiplier != null) {
                    int finalExp = (int) (expAmount * expMultiplier);
                    if (finalExp > 0) {
                        player.giveExp(finalExp);
                    }
                }
            }
            
            // Заменить блок
            if ((Boolean) getParameter("replaceBlock")) {
                String replacementBlockType = (String) getParameter("replacementBlock");
                Integer replacementDelay = (Integer) getParameter("replacementDelay");
                
                if (replacementBlockType != null) {
                    try {
                        Material replacementMaterial = Material.valueOf(replacementBlockType);
                        
                        if (replacementDelay != null && replacementDelay > 0) {
                            // Задержка замены
                            player.getServer().getScheduler().runTaskLater(
                                org.bukkit.Bukkit.getPluginManager().getPlugin("OpenHousing"),
                                () -> block.setType(replacementMaterial),
                                replacementDelay * 20L
                            );
                        } else {
                            // Мгновенная замена
                            block.setType(replacementMaterial);
                        }
                    } catch (IllegalArgumentException e) {
                        // Материал не найден, игнорируем
                    }
                }
            }
            
            // Создать частицы
            if ((Boolean) getParameter("spawnParticles")) {
                String particleType = (String) getParameter("particleType");
                Integer particleCount = (Integer) getParameter("particleCount");
                
                if (particleType != null && particleCount != null) {
                    try {
                        org.bukkit.Particle particle = org.bukkit.Particle.valueOf(particleType);
                        block.getWorld().spawnParticle(particle, block.getLocation().add(0.5, 0.5, 0.5), particleCount);
                    } catch (IllegalArgumentException e) {
                        // Тип частиц не найден, игнорируем
                    }
                }
            }
            
            // Добавить эффекты
            if ((Boolean) getParameter("addEffects")) {
                String effects = (String) getParameter("effects");
                if (effects != null) {
                    addBreakEffects(player, effects);
                }
            }
            
            // Отслеживание статистики
            if ((Boolean) getParameter("trackStatistics")) {
                trackBreakStatistics(player, context, blockType, toolType, block);
            }
            
            // Трансляция ломания
            if ((Boolean) getParameter("broadcastBreak")) {
                String broadcastMessage = replaceVariables("§e%player_name% сломал %block_type%!", context);
                player.getServer().broadcastMessage(broadcastMessage);
            }
            
            // Логирование ломания
            if ((Boolean) getParameter("logBreak")) {
                logBlockBreak(player, context, blockType, toolType, block);
            }
            
            // Добавляем информацию о событии в контекст
            context.setVariable("break_time", System.currentTimeMillis());
            context.setVariable("break_location", block.getLocation());
            context.setVariable("break_block_type", blockType.name());
            context.setVariable("break_tool_type", toolType.name());
            context.setVariable("break_tool_durability", tool.getDurability());
            context.setVariable("break_tool_enchantments", tool.getEnchantments());
            
            return ExecutionResult.success();
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения события ломания блока: " + e.getMessage());
        }
    }
    
    /**
     * Добавить эффекты ломания
     */
    private void addBreakEffects(Player player, String effectsStr) {
        String[] effects = effectsStr.split(",");
        for (String effect : effects) {
            String[] parts = effect.trim().split(":");
            if (parts.length >= 2) {
                try {
                    org.bukkit.potion.PotionEffectType type = org.bukkit.potion.PotionEffectType.getByName(parts[0].toUpperCase());
                    int duration = Integer.parseInt(parts[1]) * 20; // Конвертируем в тики
                    int amplifier = parts.length > 2 ? Integer.parseInt(parts[2]) - 1 : 0;
                    
                    if (type != null) {
                        player.addPotionEffect(new org.bukkit.potion.PotionEffect(type, duration, amplifier));
                    }
                } catch (NumberFormatException e) {
                    // Игнорируем некорректные значения
                }
            }
        }
    }
    
    /**
     * Отслеживание статистики ломания
     */
    private void trackBreakStatistics(Player player, ExecutionContext context, Material blockType, Material toolType, Block block) {
        // Общий счетчик сломанных блоков
        String statVariable = (String) getParameter("statVariable");
        if (statVariable != null) {
            Object currentBlocks = context.getVariable(statVariable);
            int blocks = currentBlocks instanceof Number ? ((Number) currentBlocks).intValue() : 0;
            context.setVariable(statVariable, blocks + 1);
        }
        
        // Счетчик по типам блоков
        String blockTypeVar = "blocks_broken_" + blockType.name().toLowerCase();
        Object currentBlockType = context.getVariable(blockTypeVar);
        int blockTypeCount = currentBlockType instanceof Number ? ((Number) currentBlockType).intValue() : 0;
        context.setVariable(blockTypeVar, blockTypeCount + 1);
        
        // Последний сломанный блок
        String blockTypeVariable = (String) getParameter("blockTypeVariable");
        if (blockTypeVariable != null) {
            context.setVariable(blockTypeVariable, blockType.name());
        }
        
        // Последнее место ломания
        String locationVariable = (String) getParameter("locationVariable");
        if (locationVariable != null) {
            context.setVariable(locationVariable, block.getLocation());
        }
        
        // Последний инструмент
        String toolVariable = (String) getParameter("toolVariable");
        if (toolVariable != null) {
            context.setVariable(toolVariable, toolType.name());
        }
        
        // Время последнего ломания
        String breakTimeVariable = (String) getParameter("breakTimeVariable");
        if (breakTimeVariable != null) {
            context.setVariable(breakTimeVariable, System.currentTimeMillis());
        }
    }
    
    /**
     * Логировать ломание блока
     */
    private void logBlockBreak(Player player, ExecutionContext context, Material blockType, Material toolType, Block block) {
        String logMessage = String.format(
            "[BLOCK_BREAK] Player %s broke %s at %s in world %s using %s",
            player.getName(),
            blockType.name(),
            block.getLocation().toString(),
            block.getWorld().getName(),
            toolType.name()
        );
        
        // Здесь можно добавить логирование в файл или базу данных
        System.out.println(logMessage);
    }
    
    @Override
    public boolean validate() {
        return getParameter("blockType") != null;
    }
    
    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "§7Срабатывает при ломании блоков игроком",
            "",
            "§eПараметры:",
            "§7• Тип блока (ANY для всех)",
            "§7• Тип инструмента (ANY для всех)",
            "§7• Сообщение о ломании",
            "§7• Показать заголовок",
            "§7• Воспроизвести звук",
            "§7• Настройки дропа предметов",
            "§7• Множитель дропа",
            "§7• Дать опыт",
            "§7• Множитель опыта",
            "§7• Отменить ломание",
            "§7• Заменить блок",
            "§7• Задержка замены",
            "§7• Создать частицы",
            "§7• Добавить эффекты",
            "§7• Отслеживание статистики",
            "§7• Переменные статистики",
            "§7• Трансляция ломания",
            "§7• Логирование",
            "§7• Проверка разрешений"
        );
    }
    
    @Override
    public boolean matchesEvent(Object event) {
        return event instanceof BlockBreakEvent;
    }
    
    @Override
    public ExecutionContext createContextFromEvent(Object event) {
        if (event instanceof BlockBreakEvent) {
            BlockBreakEvent breakEvent = (BlockBreakEvent) event;
            ExecutionContext context = new ExecutionContext(breakEvent.getPlayer());
            context.setVariable("block", breakEvent.getBlock());
            context.setVariable("block_type", breakEvent.getBlock().getType().name());
            context.setVariable("tool_type", breakEvent.getPlayer().getInventory().getItemInMainHand().getType().name());
            return context;
        }
        return null;
    }
}
