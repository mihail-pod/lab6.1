package tests;

import lexer.Lexer;
import lexer.Token;
import lexer.Token.TokenType;
import org.junit.jupiter.api.Test;
import utils.CompilerException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Тесты лексера проверяют разбиение строки на токены и обработку неизвестных символов.
public class LexerTest {

    // Проверяет корректное выражение с операцией, переменными и числом.
    @Test
    void tokenizesValidExpression() {
        List<Token> tokens = new Lexer("(*(+A,B),2)").tokenize();

        assertEquals(TokenType.LPAREN, tokens.get(0).getType());
        assertEquals(TokenType.MUL, tokens.get(1).getType());
        assertEquals(TokenType.PLUS, tokens.get(3).getType());
        assertEquals(TokenType.NUMBER, tokens.get(9).getType());
        assertEquals(TokenType.EOF, tokens.get(tokens.size() - 1).getType());
    }

    // Проверяет, что неизвестный символ не пропускается молча.
    @Test
    void rejectsUnknownCharacter() {
        assertThrows(CompilerException.class, () -> new Lexer("(+A,@)").tokenize());
    }
}
