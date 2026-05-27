package tests;

import ast.Node;
import lexer.Lexer;
import org.junit.jupiter.api.Test;
import parser.Parser;
import quadruples.Quadruple;
import quadruples.QuadrupleGenerator;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Тесты генератора четверок проверяют порядок обхода AST и имена временных переменных.
public class QuadrupleGeneratorTest {

    // Проверяет генерацию четверок для выражения с двумя вложенными операциями.
    @Test
    void generatesQuadruplesInPostOrder() {
        Node node = new Parser(new Lexer("(*(+A,B),(-C,2))").tokenize()).parse();
        List<Quadruple> quadruples = new QuadrupleGenerator().generate(node, Map.of("A", 1, "B", 2, "C", 5));

        assertEquals("(+, A, B, T1)", quadruples.get(0).toClassicString());
        assertEquals("(-, C, 2, T2)", quadruples.get(1).toClassicString());
        assertEquals("(*, T1, T2, T3)", quadruples.get(2).toClassicString());
    }

    @Test
    void generatesEvaluatedQuadruples() {
        Node node = new Parser(new Lexer("(/(*(-A,B),(-C,2)),(+C,D))").tokenize()).parse();
        List<Quadruple> quadruples = new QuadrupleGenerator().generate(
                node,
                Map.of("A", 3, "B", 4, "C", 5, "D", 7)
        );

        assertEquals("(-, A, B, T1) -> (-, 3, 4, -1)", quadruples.get(0).toEvaluatedString());
        assertEquals("(-, C, 2, T2) -> (-, 5, 2, 3)", quadruples.get(1).toEvaluatedString());
        assertEquals("(*, T1, T2, T3) -> (*, -1, 3, -3)", quadruples.get(2).toEvaluatedString());
        assertEquals("(+, C, D, T4) -> (+, 5, 7, 12)", quadruples.get(3).toEvaluatedString());
        assertEquals("(/, T3, T4, T5) -> (/, -3, 12, 0)", quadruples.get(4).toEvaluatedString());
    }
}
