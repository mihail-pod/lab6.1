package tests;

import ast.Node;
import lexer.Lexer;
import org.junit.jupiter.api.Test;
import parser.Parser;
import quadruples.Quadruple;
import quadruples.QuadrupleGenerator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Тесты генератора четверок проверяют порядок обхода AST и имена временных переменных.
public class QuadrupleGeneratorTest {

    // Проверяет генерацию четверок для выражения с двумя вложенными операциями.
    @Test
    void generatesQuadruplesInPostOrder() {
        Node node = new Parser(new Lexer("(*(+A,B),(-C,2))").tokenize()).parse();
        List<Quadruple> quadruples = new QuadrupleGenerator().generate(node);

        assertEquals("(+, A, B, T1)", quadruples.get(0).toString());
        assertEquals("(-, C, 2, T2)", quadruples.get(1).toString());
        assertEquals("(*, T1, T2, T3)", quadruples.get(2).toString());
    }
}
