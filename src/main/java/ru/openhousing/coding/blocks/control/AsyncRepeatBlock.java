package ru.openhousing.coding.blocks.control;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.openhousing.OpenHousing;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;
import ru.openhousing.coding.constants.BlockParams;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Асинхронный блок повторения (цикла) - предотвращает server lag
 */
public class AsyncRepeatBlock extends CodeBlock {
    
    private static final ConcurrentHashMap<UUID, AsyncLoopTask> activeLoops = new ConcurrentHashMap<>();
    
    public enum RepeatType {
        TIMES("Количество раз", "Повторяет указанное количество раз"),
        WHILE("Пока условие истинно", "Повторяет пока условие выполняется"),
        FOR_EACH("Для каждого", "Повторяет для каждого элемента в списке"),
        FOREVER("Бесконечно", "Повторяет бесконечно (опасно!)"),
        UNTIL("До тех пор пока", "Повторяет до выполнения условия");
        
        private final String displayName;
        private final String description;
        
        RepeatType(String displayName, String description) {
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
    
    public AsyncRepeatBlock() {
        super(BlockType.REPEAT);
        setParameter(BlockParams.REPEAT_TYPE, RepeatType.TIMES);
        setParameter(BlockParams.VALUE, "5");
        setParameter(BlockParams.MAX_ITERATIONS, "1000");
        setParameter("delay_ticks", "1"); // Задержка между итерациями в тиках
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден");
        }
        
        // Проверяем, не выполняется ли уже цикл для этого игрока
        if (activeLoops.containsKey(player.getUniqueId())) {
            return ExecutionResult.error("Цикл уже выполняется для этого игрока");
        }
        
        RepeatType repeatType = (RepeatType) getParameter(BlockParams.REPEAT_TYPE);
        String value = replaceVariables((String) getParameter(BlockParams.VALUE), context);
        int maxIterations = getMaxIterations();
        int delayTicks = getDelayTicks();
        
        if (repeatType == null) {
            return ExecutionResult.error("Не указан тип повторения");
        }
        
        try {
            // Запускаем асинхронный цикл
            AsyncLoopTask loopTask = new AsyncLoopTask(
                this, context, repeatType, value, maxIterations, delayTicks
            );
            
            activeLoops.put(player.getUniqueId(), loopTask);
            loopTask.start();
            
            return ExecutionResult.success("Цикл запущен асинхронно");
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка запуска цикла: " + e.getMessage());
        }
    }
    
    @Override
    public String getDescription() {
        return "Асинхронный блок повторения - предотвращает server lag";
    }
    
    @Override
    public boolean validate() {
        return getParameter(BlockParams.REPEAT_TYPE) != null && 
               getParameter(BlockParams.VALUE) != null;
    }
    
    /**
     * Остановка цикла для игрока
     */
    public static void stopLoop(Player player) {
        AsyncLoopTask task = activeLoops.remove(player.getUniqueId());
        if (task != null) {
            task.stop();
        }
    }
    
    /**
     * Проверка, выполняется ли цикл для игрока
     */
    public static boolean isLoopActive(Player player) {
        return activeLoops.containsKey(player.getUniqueId());
    }
    
    /**
     * Получение количества активных циклов
     */
    public static int getActiveLoopsCount() {
        return activeLoops.size();
    }
    
    /**
     * Очистка всех циклов (при выключении плагина)
     */
    public static void stopAllLoops() {
        activeLoops.values().forEach(AsyncLoopTask::stop);
        activeLoops.clear();
    }
    
    private int getMaxIterations() {
        try {
            return Integer.parseInt((String) getParameter(BlockParams.MAX_ITERATIONS));
        } catch (Exception e) {
            return 1000;
        }
    }
    
    private int getDelayTicks() {
        try {
            return Integer.parseInt((String) getParameter("delay_ticks"));
        } catch (Exception e) {
            return 1;
        }
    }
    
    /**
     * Асинхронная задача выполнения цикла
     */
    private static class AsyncLoopTask {
        private final AsyncRepeatBlock block;
        private final ExecutionContext context;
        private final RepeatType repeatType;
        private final String value;
        private final int maxIterations;
        private final int delayTicks;
        
        private final Player player;
        private final UUID playerId;
        private boolean running = false;
        private int currentIteration = 0;
        
        public AsyncLoopTask(AsyncRepeatBlock block, ExecutionContext context, 
                           RepeatType repeatType, String value, int maxIterations, int delayTicks) {
            this.block = block;
            this.context = context;
            this.repeatType = repeatType;
            this.value = value;
            this.maxIterations = maxIterations;
            this.delayTicks = delayTicks;
            this.player = context.getPlayer();
            this.playerId = player.getUniqueId();
        }
        
        public void start() {
            if (running) return;
            running = true;
            
            // Запускаем первую итерацию
            Bukkit.getScheduler().runTask(OpenHousing.getInstance(), this::executeNextIteration);
        }
        
        public void stop() {
            running = false;
            activeLoops.remove(playerId);
        }
        
        private void executeNextIteration() {
            if (!running || !player.isOnline()) {
                stop();
                return;
            }
            
            try {
                // Выполняем текущую итерацию
                ExecutionResult result = executeCurrentIteration();
                
                // Обрабатываем результат
                if (result.getType() == CodeBlock.ExecutionResult.Type.BREAK) {
                    // Выход из цикла
                    stop();
                    return;
                } else if (result.getType() == CodeBlock.ExecutionResult.Type.ERROR) {
                    // Ошибка - останавливаем цикл
                    player.sendMessage("§cОшибка в цикле: " + result.getMessage());
                    stop();
                    return;
                }
                
                // Проверяем, нужно ли продолжать
                if (shouldContinue()) {
                    // Планируем следующую итерацию
                    Bukkit.getScheduler().runTaskLater(
                        OpenHousing.getInstance(), 
                        this::executeNextIteration, 
                        delayTicks
                    );
                } else {
                    // Цикл завершен
                    stop();
                }
                
            } catch (Exception e) {
                player.sendMessage("§cОшибка выполнения цикла: " + e.getMessage());
                stop();
            }
        }
        
        private ExecutionResult executeCurrentIteration() {
            // Устанавливаем переменные цикла
            context.setVariable("_loop_index", currentIteration);
            context.setVariable("_loop_count", currentIteration + 1);
            
            // Выполняем дочерние блоки
            return block.executeChildren(context);
        }
        
        private boolean shouldContinue() {
            currentIteration++;
            
            if (currentIteration >= maxIterations) {
                player.sendMessage("§eЦикл остановлен: достигнут лимит итераций (" + maxIterations + ")");
                return false;
            }
            
            switch (repeatType) {
                case TIMES:
                    try {
                        int times = Integer.parseInt(value);
                        return currentIteration < times;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    
                case WHILE:
                    return evaluateCondition(value, context);
                    
                case FOR_EACH:
                    // Получаем список элементов
                    Object listObj = context.getVariable(value);
                    if (listObj instanceof String[]) {
                        String[] items = (String[]) listObj;
                        return currentIteration < items.length;
                    } else if (listObj instanceof String) {
                        String[] items = listObj.toString().split(",");
                        return currentIteration < items.length;
                    }
                    return false;
                    
                case FOREVER:
                    return true; // Бесконечный цикл
                    
                case UNTIL:
                    return !evaluateCondition(value, context);
                    
                default:
                    return false;
            }
        }
        
        private boolean evaluateCondition(String condition, ExecutionContext context) {
            // Простая проверка условия (можно расширить)
            try {
                // Если это число, проверяем больше 0
                double numValue = Double.parseDouble(condition);
                return numValue > 0;
            } catch (NumberFormatException e) {
                // Если это строка, проверяем не пустая
                return condition != null && !condition.trim().isEmpty();
            }
        }
    }
}
