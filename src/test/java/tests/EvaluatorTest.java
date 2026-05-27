package tests;

import ast.Node;
import evaluator.ExpressionEvaluator;
import evaluator.VariableCollector;
import lexer.Lexer;
import optimizer.AstOptimizer;
import org.junit.jupiter.api.Test;
import parser.Parser;
import utils.CompilerException;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EvaluatorTest {

    @Test
    void collectsUniqueVariablesFromAst() {
        Node node = parseAndOptimize("(*(+A,B),(-A,C))");

        assertEquals(Set.of("A", "B", "C"), new VariableCollector().collect(node));
    }

    @Test
    void evaluatesExpressionWithVariableValues() {
        Node node = parseAndOptimize("(*(+A,B),C)");

        int result = new ExpressionEvaluator().evaluate(node, Map.of("A", 5, "B", 3, "C", 2));

        assertEquals(16, result);
    }

    @Test
    void rejectsRuntimeDivisionByZero() {
        Node node = parseAndOptimize("(/A,B)");

        assertThrows(CompilerException.class, () ->
                new ExpressionEvaluator().evaluate(node, Map.of("A", 5, "B", 0))
        );
    }

    private Node parseAndOptimize(String input) {
        Node node = new Parser(new Lexer(input).tokenize()).parse();
        return new AstOptimizer().optimize(node);
    }
}
