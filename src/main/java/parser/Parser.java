package parser;

import ast.BinaryOperationNode;
import ast.Node;
import ast.ValueNode;
import lexer.Token;
import lexer.Token.TokenType;
import utils.CompilerException;

import java.util.List;

// Рекурсивный parser
public class Parser {
    private final List<Token> tokens;
    private int position;

    // список токенов от лексера
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // разбор входа и проверка лишних токенов.
    public Node parse() {
        Node root = parseExpression();
        expect(TokenType.EOF, "После выражения найдены лишние символы");
        return root;
    }

    // Разбирает одно выражение и рекурсивно строит AST-дерево
    private Node parseExpression() {
        Token current = current();

        // Число и переменная являются простыми листьями дерева
        if (match(TokenType.NUMBER)) {
            return ValueNode.number(Integer.parseInt(current.getText()));
        }
        if (match(TokenType.VARIABLE)) {
            return ValueNode.variable(current.getText());
        }

        // Составное выражение всегда начинается со скобки
        if (match(TokenType.LPAREN)) {
            Token operator = current();
            if (!isOperator(operator.getType())) {
                throw error("Ожидалась операция +, -, * или /");
            }
            position++;

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

    // Проверяет является ли токен одной из разрешенных операций
    private boolean isOperator(TokenType type) {
        return type == TokenType.PLUS || type == TokenType.MINUS || type == TokenType.MUL || type == TokenType.DIV;
    }

    // Если текущий токен нужного типа потребляет его и возвращает true
    private boolean match(TokenType type) {
        if (current().getType() == type) {
            position++;
            return true;
        }
        return false;
    }

    // Требует конкретный токен и формирует понятную ошибку если его нет.
    private void expect(TokenType type, String message) {
        if (!match(type)) {
            throw error(message);
        }
    }

    // Возвращает текущий токен без сдвига позиции
    private Token current() {
        return tokens.get(position);
    }

    // Добавляет к сообщению позицию токена, на котором парсер остановился
    private CompilerException error(String message) {
        return new CompilerException(message, current().getPosition());
    }
}
