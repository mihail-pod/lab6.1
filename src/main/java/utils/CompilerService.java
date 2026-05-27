package utils;

import assembler.AssemblerGenerator;
import ast.Node;
import ast.ValueNode;
import evaluator.ExpressionEvaluator;
import evaluator.VariableCollector;
import lexer.Lexer;
import optimizer.AstOptimizer;
import parser.Parser;
import quadruples.Quadruple;
import quadruples.QuadrupleGenerator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// Центральный сервис компилятора.
// GUI и консоль вызывают его вместо прямой работы с lexer, parser, optimizer и генераторами.
public class CompilerService {

    public String readSource(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    // Первый этап: строим исходный AST, оптимизированный AST и список переменных.
    // Если оптимизация полностью свернула выражение в число, переменные больше не нужны.
    public CompilerResult prepare(String input) {
        try {
            Lexer lexer = new Lexer(input);
            Parser parser = new Parser(lexer.tokenize());
            Node originalAst = parser.parse();

            AstOptimizer optimizer = new AstOptimizer();
            Node optimizedAst = optimizer.optimize(originalAst);
            boolean optimizedToConstant = isNumberNode(optimizedAst);

            Set<String> variableNames = optimizedToConstant
                    ? Collections.emptySet()
                    : new VariableCollector().collect(optimizedAst);

            return new CompilerResult(
                    originalAst,
                    optimizedAst,
                    Collections.emptyList(),
                    "",
                    "",
                    null,
                    variableNames,
                    Collections.emptyMap(),
                    optimizedToConstant ? ((ValueNode) optimizedAst).getNumber() : null,
                    optimizedToConstant
            );
        } catch (RuntimeException ex) {
            return errorResult(ex);
        }
    }

    public CompilerResult compile(String input, Path outputDirectory, boolean writeFiles) {
        return compile(input, outputDirectory, writeFiles, Collections.emptyMap());
    }

    public CompilerResult compile(String input, Path outputDirectory, boolean writeFiles, Map<String, Integer> variableValues) {
        CompilerResult prepared = prepare(input);
        if (prepared.hasErrors()) {
            if (writeFiles) {
                writeError(outputDirectory, prepared.getErrors());
            }
            return prepared;
        }
        return compilePrepared(prepared, outputDirectory, writeFiles, variableValues);
    }

    // Второй этап: пользовательские значения уже известны.
    // Для константного AST pipeline завершается без IR и assembler.
    public CompilerResult compilePrepared(
            CompilerResult prepared,
            Path outputDirectory,
            boolean writeFiles,
            Map<String, Integer> variableValues
    ) {
        try {
            if (prepared.isOptimizedToConstant()) {
                return finishConstantResult(prepared, outputDirectory, writeFiles);
            }

            requireAllVariables(prepared.getVariableNames(), variableValues);

            int evaluationResult = new ExpressionEvaluator().evaluate(prepared.getOptimizedAst(), variableValues);
            List<Quadruple> quadruples = new QuadrupleGenerator().generate(prepared.getOptimizedAst(), variableValues);
            String assembler = new AssemblerGenerator().generate(quadruples, variableValues);

            if (writeFiles) {
                writeOutputs(outputDirectory, formatQuadruples(quadruples), assembler, "");
            }

            return new CompilerResult(
                    prepared.getOriginalAst(),
                    prepared.getOptimizedAst(),
                    quadruples,
                    assembler,
                    "",
                    null,
                    prepared.getVariableNames(),
                    variableValues,
                    evaluationResult,
                    false
            );
        } catch (RuntimeException | IOException ex) {
            if (writeFiles) {
                writeError(outputDirectory, messageOf(ex));
            }
            return new CompilerResult(
                    prepared.getOriginalAst(),
                    prepared.getOptimizedAst(),
                    Collections.emptyList(),
                    "",
                    messageOf(ex),
                    positionOf(ex),
                    prepared.getVariableNames(),
                    variableValues,
                    null,
                    prepared.isOptimizedToConstant()
            );
        }
    }

    // Константный результат не требует четверок и assembler:
    // оптимизатор уже доказал, что все выражение равно одному числу.
    private CompilerResult finishConstantResult(CompilerResult prepared, Path outputDirectory, boolean writeFiles) throws IOException {
        int value = ((ValueNode) prepared.getOptimizedAst()).getNumber();
        if (writeFiles) {
            writeOutputs(outputDirectory, "не требуется", "не требуется", "");
        }
        return new CompilerResult(
                prepared.getOriginalAst(),
                prepared.getOptimizedAst(),
                Collections.emptyList(),
                "",
                "",
                null,
                Collections.emptySet(),
                Collections.emptyMap(),
                value,
                true
        );
    }

    private void requireAllVariables(Set<String> variableNames, Map<String, Integer> variableValues) {
        for (String variableName : variableNames) {
            if (!variableValues.containsKey(variableName)) {
                throw new CompilerException("Не задано значение переменной " + variableName);
            }
        }
    }

    // Формат файла quadruples.txt. Отдельная классическая таблица больше не создается:
    // вычисляемая строка уже содержит исходную четверку слева от стрелки.
    private String formatQuadruples(List<Quadruple> quadruples) {
        return quadruples.stream()
                .map(Quadruple::toEvaluatedString)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private boolean isNumberNode(Node node) {
        return node instanceof ValueNode valueNode && valueNode.isNumber();
    }

    private CompilerResult errorResult(RuntimeException ex) {
        return new CompilerResult(
                null,
                null,
                Collections.emptyList(),
                "",
                messageOf(ex),
                positionOf(ex),
                Collections.emptySet(),
                Collections.emptyMap(),
                null,
                false
        );
    }

    private String messageOf(Throwable ex) {
        return ex.getMessage() == null ? ex.toString() : ex.getMessage();
    }

    private Integer positionOf(Throwable ex) {
        if (ex instanceof CompilerException compilerException && compilerException.getPosition() >= 0) {
            return compilerException.getPosition();
        }
        return null;
    }

    private void writeError(Path outputDirectory, String error) {
        try {
            writeOutputs(outputDirectory, "", "", error);
        } catch (IOException ignored) {
            // Ошибка записи не должна скрывать исходную ошибку компиляции.
        }
    }

    private void writeOutputs(Path outputDirectory, String quadruples, String assembler, String errors) throws IOException {
        Files.writeString(outputDirectory.resolve("quadruples.txt"), quadruples, StandardCharsets.UTF_8);
        Files.writeString(outputDirectory.resolve("output.asm"), assembler, StandardCharsets.UTF_8);
        Files.writeString(outputDirectory.resolve("errors.log"), errors, StandardCharsets.UTF_8);
    }
}
