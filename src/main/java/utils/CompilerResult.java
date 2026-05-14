package utils;

import ast.Node;
import quadruples.Quadruple;

import java.util.List;

// Результат одного запуска компилятора.
public class CompilerResult {
    private final Node originalAst;
    private final Node optimizedAst;
    private final List<Quadruple> quadruples;
    private final String assembler;
    private final String errors;

    // Собирает все выходные данные компиляции в одном объекте
    public CompilerResult(Node originalAst, Node optimizedAst, List<Quadruple> quadruples, String assembler, String errors) {
        this.originalAst = originalAst;
        this.optimizedAst = optimizedAst;
        this.quadruples = quadruples;
        this.assembler = assembler;
        this.errors = errors;
    }

    public Node getOriginalAst() {
        return originalAst;
    }

    public Node getOptimizedAst() {
        return optimizedAst;
    }

    public List<Quadruple> getQuadruples() {
        return quadruples;
    }

    public String getAssembler() {
        return assembler;
    }

    public String getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return errors != null && !errors.isBlank();
    }
}
