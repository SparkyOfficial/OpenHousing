package ru.openhousing.coding.blocks.functions;

import ru.openhousing.coding.blocks.BlockType;
import ru.openhousing.coding.blocks.CodeBlock;

import java.util.Arrays;
import java.util.List;

/**
 * Блок вызова функции
 */
public class CallFunctionBlock extends CodeBlock {
    
    public CallFunctionBlock() {
        super(BlockType.CALL_FUNCTION);
        setParameter("functionName", "myFunction");
        setParameter("arguments", ""); // Аргументы через запятую
    }
    
    @Override
    public ExecutionResult execute(ExecutionContext context) {
        String functionName = (String) getParameter(ru.openhousing.coding.constants.BlockParams.FUNCTION_NAME);
        String argumentsStr = (String) getParameter(ru.openhousing.coding.constants.BlockParams.ARGUMENTS);
        
        if (functionName == null || functionName.isEmpty()) {
            return ExecutionResult.error("Не указано имя функции");
        }
        
        // Получаем функцию
        CodeBlock functionBlock = context.getFunction(functionName);
        if (functionBlock == null) {
            return ExecutionResult.error("Функция не найдена: " + functionName);
        }
        
        if (!(functionBlock instanceof FunctionBlock)) {
            return ExecutionResult.error("Неверный тип функции: " + functionName);
        }
        
        FunctionBlock function = (FunctionBlock) functionBlock;
        
        // Парсим аргументы
        Object[] args = parseArguments(argumentsStr, context);
        
        // Проверяем количество аргументов
        int expectedParams = function.getParameterCount();
        if (args.length != expectedParams) {
            return ExecutionResult.error("Неверное количество аргументов для функции " + functionName + 
                                       ". Ожидается: " + expectedParams + ", получено: " + args.length);
        }
        
        // Вызываем функцию
        try {
            // Устанавливаем аргументы в контекст
            String[] paramNames = function.getFunctionParameters();
            for (int i = 0; i < Math.min(paramNames.length, args.length); i++) {
                context.setVariable(paramNames[i], args[i]);
            }
            
            return function.execute(context);
        } catch (Exception e) {
            return ExecutionResult.error("Ошибка выполнения функции " + functionName + ": " + e.getMessage());
        }
    }
    
    /**
     * Парсинг аргументов из строки
     */
    private Object[] parseArguments(String argumentsStr, ExecutionContext context) {
        if (argumentsStr == null || argumentsStr.trim().isEmpty()) {
            return new Object[0];
        }
        
        String[] argStrings = argumentsStr.split(",");
        Object[] args = new Object[argStrings.length];
        
        for (int i = 0; i < argStrings.length; i++) {
            String arg = argStrings[i].trim();
            args[i] = parseArgument(arg, context);
        }
        
        return args;
    }
    
    /**
     * Парсинг одного аргумента
     */
    private Object parseArgument(String arg, ExecutionContext context) {
        if (arg.isEmpty()) {
            return "";
        }
        
        // Проверяем, является ли аргумент переменной
        if (arg.startsWith("{") && arg.endsWith("}")) {
            String varName = arg.substring(1, arg.length() - 1);
            Object value = context.getVariable(varName);
            return value != null ? value : "";
        }
        
        // Проверяем, является ли аргумент строкой в кавычках
        if (arg.startsWith("\"") && arg.endsWith("\"")) {
            return arg.substring(1, arg.length() - 1);
        }
        
        // Пытаемся парсить как число
        try {
            if (arg.contains(".")) {
                return Double.parseDouble(arg);
            } else {
                return Integer.parseInt(arg);
            }
        } catch (NumberFormatException e) {
            // Игнорируем ошибку
        }
        
        // Пытаемся парсить как булево значение
        if (arg.equalsIgnoreCase("true")) {
            return true;
        } else if (arg.equalsIgnoreCase("false")) {
            return false;
        }
        
        // Возвращаем как строку
        return arg;
    }
    
    @Override
    public boolean validate() {
        String functionName = (String) getParameter(ru.openhousing.coding.constants.BlockParams.FUNCTION_NAME);
        return functionName != null && !functionName.trim().isEmpty();
    }
    
    @Override
    public List<String> getDescription() {
        String functionName = (String) getParameter(ru.openhousing.coding.constants.BlockParams.FUNCTION_NAME);
        String arguments = (String) getParameter(ru.openhousing.coding.constants.BlockParams.ARGUMENTS);
        
        return Arrays.asList(
            "§6Вызвать функцию",
            "§7Функция: §f" + (functionName != null ? functionName : "Не указана"),
            "§7Аргументы: §f" + (arguments != null && !arguments.isEmpty() ? arguments : "Нет"),
            "",
            "§8Вызывает указанную функцию с переданными аргументами"
        );
    }
}
