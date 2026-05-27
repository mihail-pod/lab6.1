package lexer;

import lexer.Token.TokenType;
import utils.CompilerException;

import java.util.ArrayList;
import java.util.List;

// Лексер превращает входную строку в последовательность токенов.
public class Lexer {
    private final String input;
    private int position;

    public Lexer(String input) {
        this.input = input == null ? "" : input;
    }

    // Последовательно читает символы и создает токены для parser.
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (position < input.length()) {
            char current = input.charAt(position);

            if (Character.isWhitespace(current)) {
                position++;
                continue;
            }

            if (Character.isDigit(current)) {
                tokens.add(readNumber());
                continue;
            }

            if (Character.isLetter(current)) {
                tokens.add(new Token(TokenType.VARIABLE, String.valueOf(current), position));
                position++;
                continue;
            }

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

    private Token readNumber() {
        int start = position;
        while (position < input.length() && Character.isDigit(input.charAt(position))) {
            position++;
        }
        return new Token(TokenType.NUMBER, input.substring(start, position), start);
    }

    private Token single(TokenType type, char value) {
        Token token = new Token(type, String.valueOf(value), position);
        position++;
        return token;
    }
}
