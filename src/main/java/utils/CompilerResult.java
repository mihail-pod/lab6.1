package utils;

import ast.Node;
import quadruples.Quadruple;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Результат одного запуска компилятора.
// Объект передает GUI и консоли все данные pipeline: AST, значения переменных,
// итог вычисления, вычисляемые четверки, assembler и сведения об ошибке.
public class CompilerResult {
    private final Node originalAst;
    private final Node optimizedAst;
    private final List<Quadruple> quadruples;
    private final String assembler;
    private final String errors;
    private final Integer errorPosition;
    private final Set<String> variableNames;
    private final Map<String, Integer> variableValues;
    private final Integer evaluationResult;
    private final boolean optimizedToConstant;

    public CompilerResult(Node originalAst, Node optimizedAst, List<Quadruple> quadruples, String assembler, String errors) {
        this(originalAst, optimizedAst, quadruples, assembler, errors, null,
                Collections.emptySet(), Collections.emptyMap(), null, false);
    }

    public CompilerResult(
            Node originalAst,
            Node optimizedAst,
            List<Quadruple> quadruples,
            String assembler,
            String errors,
            Integer errorPosition,
            Set<String> variableNames,
            Map<String, Integer> variableValues,
            Integer evaluationResult,
            boolean optimizedToConstant
    ) {
        this.originalAst = originalAst;
        this.optimizedAst = optimizedAst;
        this.quadruples = quadruples == null ? Collections.emptyList() : List.copyOf(quadruples);
        this.assembler = assembler;
        this.errors = errors;
        this.errorPosition = errorPosition;
        this.variableNames = variableNames == null
                ? Collections.emptySet()
                : Collections.unmodifiableSet(new LinkedHashSet<>(variableNames));
        this.variableValues = variableValues == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(variableValues));
        this.evaluationResult = evaluationResult;
        this.optimizedToConstant = optimizedToConstant;
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

    public Integer getErrorPosition() {
        return errorPosition;
    }

    public Set<String> getVariableNames() {
        return variableNames;
    }

    public Map<String, Integer> getVariableValues() {
        return variableValues;
    }

    public Integer getEvaluationResult() {
        return evaluationResult;
    }

    public boolean isOptimizedToConstant() {
        return optimizedToConstant;
    }

    public boolean hasErrors() {
        return errors != null && !errors.isBlank();
    }
}
