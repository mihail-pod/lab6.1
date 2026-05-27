package evaluator;

import ast.BinaryOperationNode;
import ast.Node;
import ast.ValueNode;
import utils.CompilerException;

import java.util.Map;

// Вычисляет значение AST напрямую, без генерации промежуточного кода.
public class ExpressionEvaluator {

    // Рекурсивно обходит дерево: лист дает число, операция вычисляет оба поддерева.
    public int evaluate(Node node, Map<String, Integer> variables) {
        if (node instanceof ValueNode value) {
            return evaluateValue(value, variables);
        }

        if (node instanceof BinaryOperationNode operation) {
            int left = evaluate(operation.getLeft(), variables);
            int right = evaluate(operation.getRight(), variables);
            return calculate(operation.getOperator(), left, right);
        }

        throw new CompilerException("Неизвестный тип AST-узла");
    }

    private int evaluateValue(ValueNode value, Map<String, Integer> variables) {
        if (value.isNumber()) {
            return value.getNumber();
        }

        Integer variableValue = variables.get(value.getValue());
        if (variableValue == null) {
            throw new CompilerException("Не задано значение переменной " + value.getValue());
        }
        return variableValue;
    }

    private int calculate(String operator, int left, int right) {
        return switch (operator) {
            case "+" -> left + right;
            case "-" -> left - right;
            case "*" -> left * right;
            case "/" -> {
                if (right == 0) {
                    throw new CompilerException("Деление на 0 во время вычисления выражения");
                }
                yield left / right;
            }
            default -> throw new CompilerException("Неизвестная операция: " + operator);
        };
    }
}
