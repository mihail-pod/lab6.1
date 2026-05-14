package tests;

import ast.Node;
import lexer.Lexer;
import org.junit.jupiter.api.Test;
import parser.Parser;
import utils.CompilerException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Тесты парсера проверяют построение AST и ошибки структуры выражения.
public class ParserTest {

    // Проверяет разбор вложенного префиксного выражения.
    @Test
    void parsesNestedExpression() {
        Node node = parse("(*(+A,B),(-C,2))");

        assertEquals("(*(+A,B),(-C,2))", node.print());
    }

    // Проверяет сообщение об ошибке при пропущенной запятой.
    @Test
    void rejectsMissingComma() {
        assertThrows(CompilerException.class, () -> parse("(+A B)"));
    }

    // Вспомогательный метод запускает лексер и парсер вместе.
    private Node parse(String input) {
        return new Parser(new Lexer(input).tokenize()).parse();
    }
}
