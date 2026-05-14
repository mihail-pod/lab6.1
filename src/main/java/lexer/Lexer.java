package lexer;

import java.util.ArrayList;
import java.util.List;

import lexer.Token.TokenType;
import utils.CompilerException;


// последовательно просматривает строку и превращает символы в токены
public class Lexer {
    private final String input;
    private int position;

    // Принимает исходное арифметическое выражение
    public Lexer(String input) {
        this.input = input == null ? "" : input;
    }

    // полный лексический анализ и возвращает список токенов
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (position < input.length()) {
            char current = input.charAt(position);

            // Пробелы, табуляции и переводы строк не влияют на грамматику
            if (Character.isWhitespace(current)) {
                position++;
                continue;
            }

            // Числа могут состоять из нескольких цифр
            if (Character.isDigit(current)) {
                tokens.add(readNumber());
                continue;
            }

            // Переменная в задании состоит ровно из одного символа-буквы
            if (Character.isLetter(current)) {
                tokens.add(new Token(TokenType.VARIABLE, String.valueOf(current), position));
                position++;
                continue;
            }

            // Односимвольные служебные знаки сразу превращаются в токены
            switch (current) {
                case '+' -> tokens.add(single(TokenType.PLUS, current));
                case '-' -> tokens.add(single(TokenType.MINUS, current));
                case '*' -> tokens.add(single(TokenType.MUL, current));
                case '/' -> tokens.add(single(TokenType.DIV, current));
                case '(' -> tokens.add(single(TokenType.LPAREN, current));
                case ')' -> tokens.add(single(TokenType.RPAREN, current));
                case ',' -> tokens.add(single(TokenType.COMMA, current));
                default -> throw new CompilerException("Неизвестный символ '" + current + "'", position);
            }
        }

        tokens.add(new Token(TokenType.EOF, "", position));
        return tokens;
    }

    // Считывает целое число до первого нецифрового символа.
    private Token readNumber() {
        int start = position;
        while (position < input.length() && Character.isDigit(input.charAt(position))) {
            position++;
        }
        return new Token(TokenType.NUMBER, input.substring(start, position), start);
    }

    // Создает токен для односимвольного знака и сдвигает позицию лексера.
    private Token single(TokenType type, char value) {
        Token token = new Token(type, String.valueOf(value), position);
        position++;
        return token;
    }
}
