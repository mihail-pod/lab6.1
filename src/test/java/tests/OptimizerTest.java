package tests;

import ast.Node;
import lexer.Lexer;
import optimizer.AstOptimizer;
import org.junit.jupiter.api.Test;
import parser.Parser;
import utils.CompilerException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Тесты оптимизатора проверяют простые алгебраические преобразования.
public class OptimizerTest {

    // Проверяет правило X + 0 -> X.
    @Test
    void removesAdditionWithZero() {
        Node optimized = optimize("(+A,0)");

        assertEquals("A", optimized.print());
    }

    // Проверяет правило X * 0 -> 0.
    @Test
    void replacesMultiplicationByZero() {
        Node optimized = optimize("(*(+A,B),0)");

        assertEquals("0", optimized.print());
    }

    // Проверяет статическое обнаружение деления на ноль.
    @Test
    void detectsDivisionByZero() {
        assertThrows(CompilerException.class, () -> optimize("(/A,0)"));
    }

    // Вспомогательный метод строит AST и запускает оптимизатор.
    private Node optimize(String input) {
        Node node = new Parser(new Lexer(input).tokenize()).parse();
        return new AstOptimizer().optimize(node);
    }
}
