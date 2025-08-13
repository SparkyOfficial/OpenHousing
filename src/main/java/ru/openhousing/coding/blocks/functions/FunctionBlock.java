package ru.openhousing.coding.blocks.functions;

import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок создания функции
 */
public class FunctionBlock extends CodeBlock {
    
    public FunctionBlock() {
        super(BlockType.FUNCTION);
        setParameter("name", "myFunction");
        setParameter("description", "");
        setParameter("parameters", ""); // Параметры функции через запятую
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        // Функции не выполняются напрямую, они регистрируются при загрузке скрипта
        String functionName = (String) getParameter("name");
        if (functionName != null && !functionName.isEmpty()) {
            context.setFunction(functionName, this);
        }
        
        return ExecutionResult.success();
    }
    
    /**
     * Выполнение функции с параметрами
     */
    public ExecutionResult executeFunction(ExecutionContext context, Object... args) {
        // Создаем дочерний контекст для функции
        ExecutionContext functionContext = context.createChildContext();
        
        // Устанавливаем параметры функции
        String parametersStr = (String) getParameter("parameters");
        if (parametersStr != null && !parametersStr.isEmpty()) {
            String[] paramNames = parametersStr.split(",");
            for (int i = 0; i < paramNames.length && i < args.length; i++) {
                String paramName = paramNames[i].trim();
                if (!paramName.isEmpty()) {
                    functionContext.setVariable(paramName, args[i]);
                }
            }
        }
        
        // Выполняем дочерние блоки функции
        ExecutionResult result = executeChildren(functionContext);
        
        // Копируем изменения переменных обратно в основной контекст
        // (только глобальные переменные, локальные остаются в функции)
        for (String varName : functionContext.getVariables().keySet()) {
            if (context.getVariables().containsKey(varName)) {
                context.setVariable(varName, functionContext.getVariable(varName));
            }
        }
        
        return result;
    }
    
    /**
     * Получение количества параметров функции
     */
    public int getParameterCount() {
        String parametersStr = (String) getParameter("parameters");
        if (parametersStr == null || parametersStr.trim().isEmpty()) {
            return 0;
        }
        
        String[] paramNames = parametersStr.split(",");
        int count = 0;
        for (String param : paramNames) {
            if (!param.trim().isEmpty()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Получение имен параметров функции
     */
    public String[] getParameterNames() {
        String parametersStr = (String) getParameter("parameters");
        if (parametersStr == null || parametersStr.trim().isEmpty()) {
            return new String[0];
        }
        
        String[] paramNames = parametersStr.split(",");
        for (int i = 0; i < paramNames.length; i++) {
            paramNames[i] = paramNames[i].trim();
        }
        return paramNames;
    }
    
    @Override
    public boolean validate() {
        String functionName = (String) getParameter("name");
        return functionName != null && !functionName.trim().isEmpty();
    }
    
    @Override
    public List<String> getDescription() {
        String functionName = (String) getParameter("name");
        String description = (String) getParameter("description");
        String parameters = (String) getParameter("parameters");
        
        List<String> desc = Arrays.asList(
            "§6Функция",
            "§7Имя: §f" + (functionName != null ? functionName : "Не указано"),
            "§7Описание: §f" + (description != null && !description.isEmpty() ? description : "Нет"),
            "§7Параметры: §f" + (parameters != null && !parameters.isEmpty() ? parameters : "Нет"),
            "",
            "§8Дочерних блоков: " + childBlocks.size()
        );
        
        return desc;
    }
}
