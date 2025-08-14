package ru.openhousing.coding.blocks.control;

import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок повторения (цикла)
 */
public class RepeatBlock extends CodeBlock {
    
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
    
    public RepeatBlock() {
        super(BlockType.REPEAT);
        setParameter("repeatType", RepeatType.TIMES);
        setParameter("value", "5"); // Количество повторений или условие
        setParameter("maxIterations", "1000"); // Максимальное количество итераций для безопасности
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        RepeatType repeatType = (RepeatType) getParameter(ru.openhousing.coding.constants.BlockParams.REPEAT_TYPE);
        String value = replaceVariables((String) getParameter(ru.openhousing.coding.constants.BlockParams.VALUE), context);
        int maxIterations = getMaxIterations();
        
        if (repeatType == null) {
            return ExecutionResult.error("Не указан тип повторения");
        }
        
        try {
            return executeRepeat(context, repeatType, value, maxIterations);
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения цикла: " + e.getMessage());
        }
    }
    
    private ExecutionResult executeRepeat(ExecutionContext context, RepeatType repeatType, 
                                        String value, int maxIterations) {
        
        int iterations = 0;
        
        switch (repeatType) {
            case TIMES:
                return executeTimesRepeat(context, value, maxIterations);
                
            case WHILE:
                return executeWhileRepeat(context, value, maxIterations);
                
            case FOR_EACH:
                return executeForEachRepeat(context, value, maxIterations);
                
            case FOREVER:
                return executeForeverRepeat(context, maxIterations);
                
            case UNTIL:
                return executeUntilRepeat(context, value, maxIterations);
                
            default:
                return ExecutionResult.error("Неизвестный тип повторения");
        }
    }
    
    /**
     * Повторение указанное количество раз
     */
    private ExecutionResult executeTimesRepeat(ExecutionContext context, String value, int maxIterations) {
        try {
            int times = Integer.parseInt(value);
            times = Math.min(times, maxIterations); // Ограничиваем количество итераций
            
            for (int i = 0; i < times; i++) {
                // Устанавливаем переменную счетчика
                context.setVariable("_loop_index", i);
                context.setVariable("_loop_count", i + 1);
                
                ExecutionResult result = executeChildren(context);
                
                // Обработка управляющих команд
                switch (result.getType()) {
                    case BREAK:
                        return ExecutionResult.success();
                    case CONTINUE:
                        continue;
                    case RETURN:
                    case ERROR:
                        return result;
                }
            }
            
            return ExecutionResult.success();
            
        } catch (NumberFormatException e) {
            return ExecutionResult.error("Неверное число повторений: " + value);
        }
    }
    
    /**
     * Повторение пока условие истинно
     */
    private ExecutionResult executeWhileRepeat(ExecutionContext context, String value, int maxIterations) {
        int iterations = 0;
        
        while (iterations < maxIterations) {
            // Проверяем условие
            if (!evaluateCondition(value, context)) {
                break;
            }
            
            context.setVariable("_loop_index", iterations);
            context.setVariable("_loop_count", iterations + 1);
            
            ExecutionResult result = executeChildren(context);
            
            switch (result.getType()) {
                case BREAK:
                    return ExecutionResult.success();
                case CONTINUE:
                    iterations++;
                    continue;
                case RETURN:
                case ERROR:
                    return result;
            }
            
            iterations++;
        }
        
        if (iterations >= maxIterations) {
            return ExecutionResult.error("Превышено максимальное количество итераций: " + maxIterations);
        }
        
        return ExecutionResult.success();
    }
    
    /**
     * Повторение для каждого элемента
     */
    private ExecutionResult executeForEachRepeat(ExecutionContext context, String value, int maxIterations) {
        // Получаем список элементов (из переменной или строки через запятую)
        Object listObj = context.getVariable(value);
        String[] items;
        
        if (listObj != null) {
            if (listObj instanceof String[]) {
                items = (String[]) listObj;
            } else {
                // Преобразуем в строку и разделяем по запятым
                items = listObj.toString().split(",");
            }
        } else {
            // Если переменная не найдена, пытаемся разделить value по запятым
            items = value.split(",");
        }
        
        int iterations = Math.min(items.length, maxIterations);
        
        for (int i = 0; i < iterations; i++) {
            context.setVariable("_loop_item", items[i].trim());
            context.setVariable("_loop_index", i);
            context.setVariable("_loop_count", i + 1);
            
            ExecutionResult result = executeChildren(context);
            
            switch (result.getType()) {
                case BREAK:
                    return ExecutionResult.success();
                case CONTINUE:
                    continue;
                case RETURN:
                case ERROR:
                    return result;
            }
        }
        
        return ExecutionResult.success();
    }
    
    /**
     * Бесконечное повторение (с ограничением)
     */
    private ExecutionResult executeForeverRepeat(ExecutionContext context, int maxIterations) {
        for (int i = 0; i < maxIterations; i++) {
            context.setVariable("_loop_index", i);
            context.setVariable("_loop_count", i + 1);
            
            ExecutionResult result = executeChildren(context);
            
            switch (result.getType()) {
                case BREAK:
                    return ExecutionResult.success();
                case CONTINUE:
                    continue;
                case RETURN:
                case ERROR:
                    return result;
            }
        }
        
        return ExecutionResult.error("Превышено максимальное количество итераций для бесконечного цикла: " + maxIterations);
    }
    
    /**
     * Повторение до выполнения условия
     */
    private ExecutionResult executeUntilRepeat(ExecutionContext context, String value, int maxIterations) {
        int iterations = 0;
        
        while (iterations < maxIterations) {
            context.setVariable("_loop_index", iterations);
            context.setVariable("_loop_count", iterations + 1);
            
            ExecutionResult result = executeChildren(context);
            
            switch (result.getType()) {
                case BREAK:
                    return ExecutionResult.success();
                case CONTINUE:
                    // Проверяем условие после continue
                    if (evaluateCondition(value, context)) {
                        return ExecutionResult.success();
                    }
                    iterations++;
                    continue;
                case RETURN:
                case ERROR:
                    return result;
            }
            
            // Проверяем условие после выполнения тела цикла
            if (evaluateCondition(value, context)) {
                return ExecutionResult.success();
            }
            
            iterations++;
        }
        
        return ExecutionResult.error("Превышено максимальное количество итераций: " + maxIterations);
    }
    
    /**
     * Простая оценка условия
     */
    private boolean evaluateCondition(String condition, ExecutionContext context) {
        if (condition == null || condition.isEmpty()) {
            return false;
        }
        
        // Замена переменных
        String evaluatedCondition = replaceVariables(condition, context);
        
        // Простые условия
        if (evaluatedCondition.equalsIgnoreCase("true")) {
            return true;
        } else if (evaluatedCondition.equalsIgnoreCase("false")) {
            return false;
        }
        
        // Попытка оценить как число (не равно 0)
        try {
            double value = Double.parseDouble(evaluatedCondition);
            return value != 0;
        } catch (NumberFormatException e) {
            // Игнорируем ошибку
        }
        
        // Если строка не пустая, считаем условие истинным
        return !evaluatedCondition.trim().isEmpty();
    }
    
    /**
     * Получение максимального количества итераций
     */
    private int getMaxIterations() {
        String maxIterStr = (String) getParameter(ru.openhousing.coding.constants.BlockParams.MAX_ITERATIONS);
        try {
            return Integer.parseInt(maxIterStr);
        } catch (NumberFormatException e) {
            return 1000; // По умолчанию
        }
    }
    
    // Используем метод из базового класса CodeBlock
    
    @Override
    public boolean validate() {
        String value = (String) getParameter(ru.openhousing.coding.constants.BlockParams.VALUE);
        RepeatType repeatType = (RepeatType) getParameter(ru.openhousing.coding.constants.BlockParams.REPEAT_TYPE);
        
        if (repeatType == null) {
            return false;
        }
        
        if (repeatType == RepeatType.TIMES && value != null) {
            try {
                int times = Integer.parseInt(value);
                return times > 0 && times <= 10000; // Разумные ограничения
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public List<String> getDescription() {
        RepeatType repeatType = (RepeatType) getParameter(ru.openhousing.coding.constants.BlockParams.REPEAT_TYPE);
        String value = (String) getParameter(ru.openhousing.coding.constants.BlockParams.VALUE);
        
        return Arrays.asList(
            "§6Повторение",
            "§7Тип: §f" + (repeatType != null ? repeatType.getDisplayName() : "Не выбран"),
            "§7Значение: §f" + (value != null && !value.isEmpty() ? value : "Не указано"),
            "",
            "§8Дочерних блоков: " + childBlocks.size(),
            "§8" + (repeatType != null ? repeatType.getDescription() : "")
        );
    }
}
