package parser;

import ast.BinaryOperationNode;
import ast.Node;
import ast.ValueNode;
import lexer.Token;
import lexer.Token.TokenType;
import utils.CompilerException;

import java.util.List;

// Parser строит AST по префиксной записи вида (*(+A,B),C)
// В проекте используется рекурсивный descent parser: каждый вызов parseExpression()
// разбирает ровно одно выражение и возвращает узел дерева
public class Parser {
    private final List<Token> tokens;
    private int position;

    // Parser получает уже готовый список токенов от Lexer
    // position указывает на текущий токен, который еще не был разобран
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // Точка входа синтаксического анализа
    // После разбора одного выражения обязательно проверяем EOF, чтобы отловить лишний текст
    public Node parse() {
        Node root = parseExpression();
        expect(TokenType.EOF, "После выражения найдены лишние символы");
        return root;
    }

    // Разбирает одно выражение и рекурсивно строит AST
    // Возможны два типа выражений
    // 1. лист: число или переменная
    // 2. бинарная операция в скобках: (operator left,right)
    private Node parseExpression() {
        Token current = current();

        // Число и переменная становятся листовыми узлами AST
        // У них нет дочерних элементов, поэтому рекурсия здесь заканчивается
        if (match(TokenType.NUMBER)) {
            return ValueNode.number(Integer.parseInt(current.getText()));
        }
        if (match(TokenType.VARIABLE)) {
            return ValueNode.variable(current.getText());
        }

        // Составное выражение начинается с открывающей скобки
        // После нее обязательно должна идти операция
        if (match(TokenType.LPAREN)) {
            Token operator = current();
            if (!isOperator(operator.getType())) {
                throw error("Ожидалась операция +, -, * или /");
            }
            position++;

            // Левый и правый операнды сами являются выражениями
            // Поэтому parser поддерживает вложенные конструкции
            Node left = parseExpression();
            expect(TokenType.COMMA, "Пропущена запятая между операндами");
            Node right = parseExpression();
            expect(TokenType.RPAREN, "Пропущена закрывающая скобка");

            return new BinaryOperationNode(operator.getText(), left, right);
        }

        if (current.getType() == TokenType.EOF) {
            throw error("Недостаточно операндов или выражение пустое");
        }

        throw error("Неправильная структура выражения");
    }

    // Проверяет, является ли токен допустимой арифметической операцией
    private boolean isOperator(TokenType type) {
        return type == TokenType.PLUS || type == TokenType.MINUS || type == TokenType.MUL || type == TokenType.DIV;
    }

    // Если текущий токен имеет нужный тип, он считается разобранным, а position сдвигается на следующий токен
    private boolean match(TokenType type) {
        if (current().getType() == type) {
            position++;
            return true;
        }
        return false;
    }

    // Требует конкретный токен. Если токена нет, ошибка содержит позицию во входной строке
    private void expect(TokenType type, String message) {
        if (!match(type)) {
            throw error(message);
        }
    }

    // Возвращает текущий токен без сдвига position
    private Token current() {
        return tokens.get(position);
    }

    // Создает CompilerException с позицией токена, на котором parser остановился
    private CompilerException error(String message) {
        return new CompilerException(message, current().getPosition());
    }
}
