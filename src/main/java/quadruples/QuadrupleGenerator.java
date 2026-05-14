package quadruples;

import ast.BinaryOperationNode;
import ast.Node;
import ast.ValueNode;
import utils.CompilerException;

import java.util.ArrayList;
import java.util.List;

// Генератор таблицы четверок
// Он обходит AST после оптимизации и создает временные переменные T
public class QuadrupleGenerator {
    private final List<Quadruple> quadruples = new ArrayList<>();
    private int tempCounter;

    // Запускает генерацию и возвращает список четверок
    public List<Quadruple> generate(Node root) {
        quadruples.clear();
        tempCounter = 0;
        generateForNode(root);
        return new ArrayList<>(quadruples);
    }

    // Рекурсивно генерирует код для узла и возвращает имя результата этого узла
    public String generateForNode(Node node) {
        if (node instanceof ValueNode value) {
            return value.getValue();
        }
        if (node instanceof BinaryOperationNode operation) {
            String left = generateForNode(operation.getLeft());
            String right = generateForNode(operation.getRight());
            String result = newTemp();
            quadruples.add(new Quadruple(operation.getOperator(), left, right, result));
            return result;
        }
        throw new CompilerException("Неизвестный тип AST-узла");
    }

    // Генерирует новую временную переменную для результата операции
    private String newTemp() {
        tempCounter++;
        return "T" + tempCounter;
    }
}
