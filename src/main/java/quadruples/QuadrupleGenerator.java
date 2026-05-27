package quadruples;

import ast.BinaryOperationNode;
import ast.Node;
import ast.ValueNode;
import utils.CompilerException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Генерирует вычисляемую таблицу четверок.
// Рекурсивный обход идет снизу вверх: сначала создаются четверки вложенных операций,
// затем их временные результаты T1/T2/T3 используются в родительских операциях.
public class QuadrupleGenerator {
    private final List<Quadruple> quadruples = new ArrayList<>();
    private final Map<String, Integer> temporaryValues = new HashMap<>();
    private Map<String, Integer> variableValues = Map.of();
    private int tempCounter;

    // Запускает генерацию для оптимизированного AST и введенных пользователем значений.
    public List<Quadruple> generate(Node root, Map<String, Integer> variableValues) {
        quadruples.clear();
        temporaryValues.clear();
        tempCounter = 0;
        this.variableValues = variableValues == null ? Map.of() : variableValues;

        generateForNode(root);
        return new ArrayList<>(quadruples);
    }

    // Возвращает имя операнда, которое будет записано в четверку.
    // Для листа это число или переменная, для операции это новая временная переменная Tn.
    private String generateForNode(Node node) {
        if (node instanceof ValueNode value) {
            return value.getValue();
        }

        if (node instanceof BinaryOperationNode operation) {
            String left = generateForNode(operation.getLeft());
            String right = generateForNode(operation.getRight());
            String resultName = newTemp();

            int evaluatedLeft = resolveValue(left);
            int evaluatedRight = resolveValue(right);
            int evaluatedResult = calculate(operation.getOperator(), evaluatedLeft, evaluatedRight);

            temporaryValues.put(resultName, evaluatedResult);
            quadruples.add(new Quadruple(
                    operation.getOperator(),
                    left,
                    right,
                    resultName,
                    evaluatedLeft,
                    evaluatedRight,
                    evaluatedResult
            ));

            return resultName;
        }

        throw new CompilerException("Неизвестный тип AST-узла");
    }

    // Подставляет значение операнда: числовую константу, переменную A/B/C
    // или ранее вычисленную временную переменную Tn.
    private int resolveValue(String operand) {
        if (operand.matches("-?\\d+")) {
            return Integer.parseInt(operand);
        }
        if (temporaryValues.containsKey(operand)) {
            return temporaryValues.get(operand);
        }
        if (variableValues.containsKey(operand)) {
            return variableValues.get(operand);
        }
        throw new CompilerException("Не найдено значение операнда " + operand);
    }

    private int calculate(String operator, int left, int right) {
        return switch (operator) {
            case "+" -> left + right;
            case "-" -> left - right;
            case "*" -> left * right;
            case "/" -> {
                if (right == 0) {
                    throw new CompilerException("Деление на 0 во время вычисления четверки");
                }
                yield left / right;
            }
            default -> throw new CompilerException("Неизвестная операция четверки: " + operator);
        };
    }

    private String newTemp() {
        tempCounter++;
        return "T" + tempCounter;
    }
}
