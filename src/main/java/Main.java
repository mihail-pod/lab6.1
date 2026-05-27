import quadruples.Quadruple;
import utils.CompilerResult;
import utils.CompilerService;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

// Консольный режим повторяет основной pipeline GUI.
public class Main {

    public static void main(String[] args) {
        CompilerService compiler = new CompilerService();
        Path workingDirectory = Path.of(".");

        try {
            String input = compiler.readSource(workingDirectory.resolve("input.txt"));
            CompilerResult prepared = compiler.prepare(input);

            if (prepared.hasErrors()) {
                prepared = compiler.compile(input, workingDirectory, true, Map.of());
                printErrors(prepared);
                return;
            }

            Map<String, Integer> variableValues = prepared.isOptimizedToConstant()
                    ? Map.of()
                    : readVariableValues(prepared);

            CompilerResult result = compiler.compilePrepared(prepared, workingDirectory, true, variableValues);

            if (result.hasErrors()) {
                printErrors(result);
                return;
            }

            System.out.println(formatResult(result));
            System.out.println("Созданы файлы: quadruples.txt, output.asm, errors.log");
        } catch (Exception ex) {
            System.out.println("Компиляция завершена с ошибками.");
            System.out.println(ex.getMessage());
        }
    }

    private static Map<String, Integer> readVariableValues(CompilerResult prepared) {
        Map<String, Integer> values = new LinkedHashMap<>();
        Scanner scanner = new Scanner(System.in);

        for (String variable : prepared.getVariableNames()) {
            while (true) {
                System.out.print("Введите значение " + variable + ": ");
                String input = scanner.nextLine();
                try {
                    values.put(variable, Integer.parseInt(input.trim()));
                    break;
                } catch (NumberFormatException ex) {
                    System.out.println("Введите целое число.");
                }
            }
        }

        return values;
    }

    private static String formatResult(CompilerResult result) {
        if (result.isOptimizedToConstant()) {
            return "AST исходный:\n" + result.getOriginalAst().print()
                    + "\n\nAST после оптимизации:\n" + result.getOptimizedAst().print()
                    + "\n\nВыражение полностью оптимизировано до константы."
                    + "\n\nРезультат выражения: " + result.getEvaluationResult()
                    + "\n\nКлассическая таблица четверок:\nне требуется"
                    + "\n\nВычисляемая таблица четверок:\nне требуется"
                    + "\n\nAssembler:\nне требуется";
        }

        String evaluatedQuadruples = result.getQuadruples().stream()
                .map(Quadruple::toEvaluatedString)
                .collect(Collectors.joining(System.lineSeparator()));

        return "AST исходный:\n" + result.getOriginalAst().print()
                + "\n\nAST после оптимизации:\n" + result.getOptimizedAst().print()
                + "\n\nЗначения переменных:\n" + formatVariables(result)
                + "\n\nРезультат выражения: " + result.getEvaluationResult()
                + "\n\nКлассическая таблица четверок:\nсм. левую часть вычисляемой таблицы"
                + "\n\nВычисляемая таблица четверок:\n" + evaluatedQuadruples;
    }

    private static String formatVariables(CompilerResult result) {
        return result.getVariableValues().entrySet().stream()
                .map(entry -> entry.getKey() + " = " + entry.getValue())
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private static void printErrors(CompilerResult result) {
        System.out.println("Компиляция завершена с ошибками. Подробности записаны в errors.log");
        if (result.getErrorPosition() != null) {
            System.out.println("Позиция ошибки: " + result.getErrorPosition());
        }
        System.out.println(result.getErrors());
    }
}
