package evaluator;

import ast.BinaryOperationNode;
import ast.Node;
import ast.ValueNode;
import utils.CompilerException;

import java.util.LinkedHashSet;
import java.util.Set;

// Собирает уникальные переменные из AST после оптимизации.
public class VariableCollector {

    public Set<String> collect(Node node) {
        Set<String> variables = new LinkedHashSet<>();
        collect(node, variables);
        return variables;
    }

    // Обход идет слева направо, поэтому порядок запроса значений совпадает с выражением.
    private void collect(Node node, Set<String> variables) {
        if (node instanceof ValueNode value) {
            if (!value.isNumber()) {
                variables.add(value.getValue());
            }
            return;
        }

        if (node instanceof BinaryOperationNode operation) {
            collect(operation.getLeft(), variables);
            collect(operation.getRight(), variables);
            return;
        }

        throw new CompilerException("Неизвестный тип AST-узла");
    }
}
