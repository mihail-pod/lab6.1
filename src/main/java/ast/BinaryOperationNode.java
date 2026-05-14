package ast;

// Узел бинарной операции
public class BinaryOperationNode extends Node {
    private final String operator;
    private final Node left;
    private final Node right;

    // Создает узел операции и связывает его с левым и правым поддеревом.
    public BinaryOperationNode(String operator, Node left, Node right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    // Возвращает символ операции для оптимизатора и генераторов.
    public String getOperator() {
        return operator;
    }

    // Возвращает левый операнд операции.
    public Node getLeft() {
        return left;
    }

    // Возвращает правый операнд операции.
    public Node getRight() {
        return right;
    }

    // Печатает дерево в компактной префиксной форме.
    @Override
    public String print() {
        return "(" + operator + left.print() + "," + right.print() + ")";
    }
}
