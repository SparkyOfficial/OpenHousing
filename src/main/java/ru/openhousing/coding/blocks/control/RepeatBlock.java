package ru.openhousing.coding.blocks.control;

import org.bukkit.entity.Player;
import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок цикла REPEAT для повторения кода
 */
public class RepeatBlock extends CodeBlock {
    
    public enum RepeatType {
        TIMES("Количество раз", "times"),
        WHILE("Пока условие", "while"),
        UNTIL("До условия", "until"),
        FOREVER("Бесконечно", "forever"),
        FOR_EACH("Для каждого", "foreach");
        
        private final String displayName;
        private final String symbol;
        
        RepeatType(String displayName, String symbol) {
            this.displayName = displayName;
            this.symbol = symbol;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getSymbol() {
            return symbol;
        }
    }
    
    public RepeatBlock() {
        super(BlockType.REPEAT);
        setParameter("repeatType", RepeatType.TIMES);
        setParameter("repeatCount", 10);
        setParameter("condition", "");
        setParameter("delayBetween", 0);
        setParameter("maxIterations", 1000);
        setParameter("breakOnError", true);
        setParameter("continueOnError", false);
        setParameter("logIterations", false);
        setParameter("showProgress", false);
        setParameter("progressMessage", "Повторение %current% из %total%");
        setParameter("saveIterationCount", false);
        setParameter("iterationVariable", "current_iteration");
        setParameter("totalIterationsVariable", "total_iterations");
        setParameter("remainingIterationsVariable", "remaining_iterations");
        setParameter("executionTimeVariable", "execution_time");
        setParameter("addEffects", false);
        setParameter("effects", "SPEED:30:1");
        setParameter("spawnParticles", false);
        setParameter("particleType", "FIREWORK");
        setParameter("particleCount", 5);
        setParameter("playSound", false);
        setParameter("soundType", "ENTITY_EXPERIENCE_ORB_PICKUP");
        setParameter("broadcastProgress", false);
        setParameter("broadcastMessage", "§e%player_name% выполняет цикл: %current%/%total%");
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Игрок не найден");
        }
        
        try {
            RepeatType repeatType = (RepeatType) getParameter("repeatType");
            Integer repeatCount = (Integer) getParameter("repeatCount");
            String condition = (String) getParameter("condition");
            Integer delayBetween = (Integer) getParameter("delayBetween");
            Integer maxIterations = (Integer) getParameter("maxIterations");
            boolean breakOnError = (Boolean) getParameter("breakOnError");
            boolean continueOnError = (Boolean) getParameter("continueOnError");
            boolean logIterations = (Boolean) getParameter("logIterations");
            boolean showProgress = (Boolean) getParameter("showProgress");
            String progressMessage = (String) getParameter("progressMessage");
            boolean saveIterationCount = (Boolean) getParameter("saveIterationCount");
            String iterationVariable = (String) getParameter("iterationVariable");
            String totalIterationsVariable = (String) getParameter("totalIterationsVariable");
            String remainingIterationsVariable = (String) getParameter("remainingIterationsVariable");
            String executionTimeVariable = (String) getParameter("executionTimeVariable");
            boolean addEffects = (Boolean) getParameter("addEffects");
            String effects = (String) getParameter("effects");
            boolean spawnParticles = (Boolean) getParameter("spawnParticles");
            String particleType = (String) getParameter("particleType");
            Integer particleCount = (Integer) getParameter("particleCount");
            boolean playSound = (Boolean) getParameter("playSound");
            String soundType = (String) getParameter("soundType");
            boolean broadcastProgress = (Boolean) getParameter("broadcastProgress");
            String broadcastMessage = (String) getParameter("broadcastMessage");
            
            long startTime = System.currentTimeMillis();
            int currentIteration = 0;
            int totalIterations = 0;
            
            // Определяем общее количество итераций
            switch (repeatType) {
                case TIMES:
                    totalIterations = repeatCount != null ? repeatCount : 1;
                    break;
                case WHILE:
                case UNTIL:
                case FOREVER:
                    totalIterations = maxIterations != null ? maxIterations : 1000;
                    break;
                case FOR_EACH:
                    totalIterations = repeatCount != null ? repeatCount : 1;
                    break;
            }
            
            // Сохраняем общее количество итераций
            if (saveIterationCount && totalIterationsVariable != null) {
                context.setVariable(totalIterationsVariable, totalIterations);
            }
            
            // Основной цикл
            while (shouldContinue(repeatType, currentIteration, repeatCount, condition, context)) {
                currentIteration++;
                
                // Проверяем максимальное количество итераций
                if (maxIterations != null && currentIteration > maxIterations) {
                    if (logIterations) {
                        System.out.println("[REPEAT] Достигнуто максимальное количество итераций: " + maxIterations);
                    }
                    break;
                }
                
                // Сохраняем текущую итерацию
                if (saveIterationCount && iterationVariable != null) {
                    context.setVariable(iterationVariable, currentIteration);
                }
                
                if (saveIterationCount && remainingIterationsVariable != null) {
                    context.setVariable(remainingIterationsVariable, totalIterations - currentIteration);
                }
                
                // Показываем прогресс
                if (showProgress && progressMessage != null) {
                    String message = replaceVariables(progressMessage, context);
                    message = message.replace("%current%", String.valueOf(currentIteration));
                    message = message.replace("%total%", String.valueOf(totalIterations));
                    message = message.replace("%remaining%", String.valueOf(totalIterations - currentIteration));
                    player.sendMessage(message);
                }
                
                // Трансляция прогресса
                if (broadcastProgress && broadcastMessage != null) {
                    String message = replaceVariables(broadcastMessage, context);
                    message = message.replace("%current%", String.valueOf(currentIteration));
                    message = message.replace("%total%", String.valueOf(totalIterations));
                    player.getServer().broadcastMessage(message);
                }
                
                // Логирование итераций
                if (logIterations) {
                    System.out.println("[REPEAT] Итерация " + currentIteration + " из " + totalIterations);
                }
                
                // Выполняем дочерние блоки
                ExecutionResult childResult = executeChildren(context);
                
                // Обрабатываем результат выполнения
                switch (childResult.getType()) {
                    case BREAK:
                        if (logIterations) {
                            System.out.println("[REPEAT] Цикл прерван командой break");
                        }
                        return ExecutionResult.success();
                        
                    case RETURN:
                        if (logIterations) {
                            System.out.println("[REPEAT] Цикл прерван командой return");
                        }
                        return childResult;
                        
                    case ERROR:
                        if (breakOnError) {
                            if (logIterations) {
                                System.out.println("[REPEAT] Цикл прерван из-за ошибки: " + childResult.getMessage());
                            }
                            return childResult;
                        } else if (!continueOnError) {
                            if (logIterations) {
                                System.out.println("[REPEAT] Пропускаем итерацию из-за ошибки: " + childResult.getMessage());
                            }
                            continue;
                        }
                        break;
                        
                    case SUCCESS:
                    default:
                        break;
                }
                
                // Добавляем эффекты
                if (addEffects && effects != null) {
                    addRepeatEffects(player, effects);
                }
                
                // Создаем частицы
                if (spawnParticles && particleType != null && particleCount != null) {
                    spawnRepeatParticles(player, particleType, particleCount);
                }
                
                // Воспроизводим звук
                if (playSound && soundType != null) {
                    playRepeatSound(player, soundType);
                }
                
                // Задержка между итерациями
                if (delayBetween != null && delayBetween > 0) {
                    try {
                        Thread.sleep(delayBetween);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return ExecutionResult.error("Цикл прерван");
                    }
                }
            }
            
            // Сохраняем время выполнения
            if (saveIterationCount && executionTimeVariable != null) {
                long executionTime = System.currentTimeMillis() - startTime;
                context.setVariable(executionTimeVariable, executionTime);
            }
            
            if (logIterations) {
                System.out.println("[REPEAT] Цикл завершен. Выполнено итераций: " + currentIteration);
            }
            
            return ExecutionResult.success();
            
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения цикла: " + e.getMessage());
        }
    }
    
    /**
     * Определяет, нужно ли продолжать цикл
     */
    private boolean shouldContinue(RepeatType repeatType, int currentIteration, Integer repeatCount, String condition, ExecutionContext context) {
        switch (repeatType) {
            case TIMES:
                return repeatCount != null && currentIteration < repeatCount;
                
            case WHILE:
                if (condition != null && !condition.trim().isEmpty()) {
                    return evaluateCondition(condition, context);
                }
                return true;
                
            case UNTIL:
                if (condition != null && !condition.trim().isEmpty()) {
                    return !evaluateCondition(condition, context);
                }
                return true;
                
            case FOREVER:
                return true;
                
            case FOR_EACH:
                return repeatCount != null && currentIteration < repeatCount;
                
            default:
                return false;
        }
    }
    
    /**
     * Вычисляет условие
     */
    private boolean evaluateCondition(String condition, ExecutionContext context) {
        try {
            // Простая проверка на равенство переменной
            if (condition.contains("==")) {
                String[] parts = condition.split("==");
                if (parts.length == 2) {
                    String varName = parts[0].trim();
                    String expectedValue = parts[1].trim();
                    Object actualValue = context.getVariable(varName);
                    return String.valueOf(actualValue).equals(expectedValue);
                }
            }
            
            // Проверка на неравенство
            if (condition.contains("!=")) {
                String[] parts = condition.split("!=");
                if (parts.length == 2) {
                    String varName = parts[0].trim();
                    String expectedValue = parts[1].trim();
                    Object actualValue = context.getVariable(varName);
                    return !String.valueOf(actualValue).equals(expectedValue);
                }
            }
            
            // Проверка на больше/меньше
            if (condition.contains(">") || condition.contains("<")) {
                String[] parts = condition.split("[><]");
                if (parts.length == 2) {
                    String varName = parts[0].trim();
                    String expectedValue = parts[1].trim();
                    Object actualValue = context.getVariable(varName);
                    
                    try {
                        double actual = Double.parseDouble(String.valueOf(actualValue));
                        double expected = Double.parseDouble(expectedValue);
                        
                        if (condition.contains(">")) {
                            return actual > expected;
                        } else {
                            return actual < expected;
                        }
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            }
            
            // Проверка на true/false
            if ("true".equalsIgnoreCase(condition.trim())) {
                return true;
            }
            if ("false".equalsIgnoreCase(condition.trim())) {
                return false;
            }
            
            // Проверка существования переменной
            Object value = context.getVariable(condition.trim());
            return value != null && !String.valueOf(value).isEmpty();
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Добавить эффекты цикла
     */
    private void addRepeatEffects(Player player, String effectsStr) {
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
     * Создать частицы цикла
     */
    private void spawnRepeatParticles(Player player, String particleType, Integer particleCount) {
        try {
            org.bukkit.Particle particle = org.bukkit.Particle.valueOf(particleType);
            player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1, 0), particleCount);
        } catch (IllegalArgumentException e) {
            // Тип частиц не найден, игнорируем
        }
    }
    
    /**
     * Воспроизвести звук цикла
     */
    private void playRepeatSound(Player player, String soundType) {
        try {
            org.bukkit.Sound sound = org.bukkit.Sound.valueOf(soundType);
            player.playSound(player.getLocation(), sound, 0.5f, 1.0f);
        } catch (IllegalArgumentException e) {
            // Звук не найден, игнорируем
        }
    }
    
    @Override
    public boolean validate() {
        return getParameter("repeatType") != null;
    }
    
    @Override
    public List<String> getDescription() {
        return Arrays.asList(
            "§7Повторяет выполнение кода",
            "",
            "§eТипы циклов:",
            "§7• Количество раз - фиксированное число",
            "§7• Пока условие - пока условие истинно",
            "§7• До условия - до выполнения условия",
            "§7• Бесконечно - бесконечный цикл",
            "§7• Для каждого - для каждого элемента",
            "",
            "§eПараметры:",
            "§7• Количество повторений",
            "§7• Условие цикла",
            "§7• Задержка между итерациями",
            "§7• Максимальное количество итераций",
            "§7• Обработка ошибок",
            "§7• Логирование",
            "§7• Показ прогресса",
            "§7• Сохранение переменных",
            "§7• Эффекты и частицы",
            "§7• Звуки",
            "§7• Трансляция прогресса"
        );
    }
    
    @Override
    public boolean matchesEvent(Object event) {
        return false; // Циклы не привязаны к событиям
    }
    
    @Override
    public ExecutionContext createContextFromEvent(Object event) {
        return null; // Циклы не создают контекст из событий
    }
}
