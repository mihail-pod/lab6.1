package utils;

import assembler.AssemblerGenerator;
import ast.Node;
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
import java.util.stream.Collectors;


// этапы компиляции и работа с файлам
public class CompilerService {

    // Читает input.txt из указанной папки компилирует выражение и записывает результаты
    public CompilerResult compileInputFile(Path workingDirectory) {
        try {
            String input = readSource(workingDirectory.resolve("input.txt"));
            return compile(input, workingDirectory, true);
        } catch (IOException ex) {
            String error = "Не удалось прочитать input.txt: " + ex.getMessage();
            try {
                writeOutputs(workingDirectory, "", "", error);
            } catch (IOException writeException) {
                error = error + System.lineSeparator() + "Не удалось записать errors.log: " + writeException.getMessage();
            }
            return new CompilerResult(null, null, Collections.emptyList(), "", error);
        }
    }

    // Читает текстовый файл как UTF-8
    public String readSource(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    // Компилирует строку выражения и при необходимости записывает выходные файлы
    public CompilerResult compile(String input, Path outputDirectory, boolean writeFiles) {
        try {
            Lexer lexer = new Lexer(input);
            Parser parser = new Parser(lexer.tokenize());
            Node originalAst = parser.parse();

            AstOptimizer optimizer = new AstOptimizer();
            Node optimizedAst = optimizer.optimize(originalAst);

            QuadrupleGenerator quadrupleGenerator = new QuadrupleGenerator();
            List<Quadruple> quadruples = quadrupleGenerator.generate(optimizedAst);
            String quadrupleText = quadruples.stream()
                    .map(Quadruple::toString)
                    .collect(Collectors.joining(System.lineSeparator()));

            AssemblerGenerator assemblerGenerator = new AssemblerGenerator();
            String assembler = assemblerGenerator.generate(quadruples);

            if (writeFiles) {
                writeOutputs(outputDirectory, quadrupleText, assembler, "");
            }

            return new CompilerResult(originalAst, optimizedAst, quadruples, assembler, "");
        } catch (RuntimeException | IOException ex) {
            String error = ex.getMessage() == null ? ex.toString() : ex.getMessage();
            if (writeFiles) {
                try {
                    writeOutputs(outputDirectory, "", "", error);
                } catch (IOException ignored) {
                    error = error + System.lineSeparator() + "Не удалось записать выходные файлы ошибок";
                }
            }
            return new CompilerResult(null, null, Collections.emptyList(), "", error);
        }
    }

    // Записывает обязательные файлы
    private void writeOutputs(Path outputDirectory, String quadruples, String assembler, String errors) throws IOException {
        Files.writeString(outputDirectory.resolve("quadruples.txt"), quadruples, StandardCharsets.UTF_8);
        Files.writeString(outputDirectory.resolve("output.asm"), assembler, StandardCharsets.UTF_8);
        Files.writeString(outputDirectory.resolve("errors.log"), errors, StandardCharsets.UTF_8);
    }
}
