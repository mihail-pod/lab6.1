package lexer;

// Один токен входного выражения
// Хранит тип, исходный текст и позицию для понятных сообщений об ошибках
public class Token {
    // Типы токенов
    public enum TokenType {
        PLUS,
        MINUS,
        MUL,
        DIV,
        LPAREN,
        RPAREN,
        COMMA,
        NUMBER,
        VARIABLE,
        EOF
    }

    private final TokenType type;
    private final String text;
    private final int position;

    // Создает токен после распознавания фрагмента входной строки
    public Token(TokenType type, String text, int position) {
        this.type = type;
        this.text = text;
        this.position = position;
    }

    public TokenType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return type + "('" + text + "') at " + position;
    }
}
