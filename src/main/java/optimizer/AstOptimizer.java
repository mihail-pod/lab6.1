package optimizer;

import ast.BinaryOperationNode;
import ast.Node;
import ast.ValueNode;
import utils.CompilerException;

// Оптимизатор AST возвращает новое дерево или существующие
public class AstOptimizer {

    // Рекурсивно оптимизирует дерево снизу вверх
    public Node optimize(Node node) {
        if (!(node instanceof BinaryOperationNode operation)) {
            return node;
        }

        Node left = optimize(operation.getLeft());
        Node right = optimize(operation.getRight());
        String op = operation.getOperator();

        // Деление на ноль
        if ("/".equals(op) && isNumber(right, 0)) {
            throw new CompilerException("Деление на 0 обнаружено во время оптимизации");
        }

        if ("+".equals(op) && isNumber(right, 0)) {
            return left;
        }
        if ("+".equals(op) && isNumber(left, 0)) {
            return right;
        }
        if ("-".equals(op) && isNumber(right, 0)) {
            return left;
        }
        if ("*".equals(op) && (isNumber(left, 0) || isNumber(right, 0))) {
            return ValueNode.number(0);
        }
        if ("*".equals(op) && isNumber(right, 1)) {
            return left;
        }
        if ("*".equals(op) && isNumber(left, 1)) {
            return right;
        }
        if ("/".equals(op) && isNumber(right, 1)) {
            return left;
        }

        return new BinaryOperationNode(op, left, right);
    }

    // Проверяет, является ли узел числом с конкретным значением
    private boolean isNumber(Node node, int value) {
        return node instanceof ValueNode valueNode && valueNode.isNumber() && valueNode.getNumber() == value;
    }
}
