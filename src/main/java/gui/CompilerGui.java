package gui;

import quadruples.Quadruple;
import utils.CompilerResult;
import utils.CompilerService;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

// GUI отвечает за ввод данных и отображение результата.
// Вся компиляция остается в CompilerService, поэтому окно не дублирует бизнес-логику.
public class CompilerGui extends JFrame {
    private final JTextPane inputPane = new JTextPane();
    private final JTextArea outputArea = new JTextArea();
    private final JPanel variablePanel = new JPanel(new GridLayout(0, 2, 6, 6));
    private final Map<String, JTextField> variableFields = new LinkedHashMap<>();
    private final CompilerService compilerService = new CompilerService();
    private Path currentDirectory = Path.of(".");
    private CompilerResult preparedResult;

    public CompilerGui() {
        super("Компилятор префиксных арифметических выражений");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 760);
        setLocationRelativeTo(null);

        outputArea.setEditable(false);

        add(createToolbar(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
    }

    private JPanel createToolbar() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel buttons = new JPanel();

        JButton openButton = new JButton("Выбрать файл");
        openButton.addActionListener(event -> chooseFile());

        JButton compileButton = new JButton("Компилировать");
        compileButton.addActionListener(event -> compile());

        buttons.add(openButton);
        buttons.add(compileButton);
        panel.add(new JLabel(" Введите выражение или загрузите input.txt"), BorderLayout.WEST);
        panel.add(buttons, BorderLayout.EAST);
        return panel;
    }

    private JSplitPane createMainPanel() {
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        JPanel left = new JPanel(new BorderLayout());
        left.add(new JLabel("Входное выражение"), BorderLayout.NORTH);
        left.add(new JScrollPane(inputPane), BorderLayout.CENTER);
        left.add(createVariableInputPanel(), BorderLayout.SOUTH);

        JPanel right = new JPanel(new BorderLayout());
        right.add(new JLabel("Результаты компиляции"), BorderLayout.NORTH);
        right.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        splitPane.setResizeWeight(0.38);
        return splitPane;
    }

    private JPanel createVariableInputPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(new JLabel("Значения переменных"), BorderLayout.NORTH);
        wrapper.add(variablePanel, BorderLayout.CENTER);
        return wrapper;
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser(currentDirectory.toFile());
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        try {
            currentDirectory = file.toPath().getParent();
            inputPane.setText(compilerService.readSource(file.toPath()));
            clearErrorHighlight();
            preparedResult = null;
            rebuildVariablePanel(new CompilerResult(null, null, null, "", ""));
            outputArea.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Не удалось прочитать файл: " + ex.getMessage());
        }
    }

    // Сначала строим AST и список переменных. Если синтаксис неверный,
    // подсвечиваем позицию ошибки прямо во входном выражении.
    private void compile() {
        clearErrorHighlight();
        CompilerResult prepared = compilerService.prepare(inputPane.getText());
        if (prepared.hasErrors()) {
            prepared = compilerService.compile(inputPane.getText(), currentDirectory, true, Map.of());
            showError(prepared);
            preparedResult = prepared;
            rebuildVariablePanel(prepared);
            return;
        }

        if (prepared.isOptimizedToConstant()) {
            CompilerResult result = compilerService.compilePrepared(prepared, currentDirectory, true, Map.of());
            preparedResult = prepared;
            rebuildVariablePanel(result);
            outputArea.setText(formatResult(result));
            return;
        }

        if (preparedResult == null || !preparedResult.getVariableNames().equals(prepared.getVariableNames())) {
            preparedResult = prepared;
            rebuildVariablePanel(prepared);
        }

        Map<String, Integer> variableValues = readVariableValues();
        if (variableValues == null) {
            outputArea.setText("Введите значения всех переменных и нажмите \"Компилировать\" еще раз.");
            return;
        }

        CompilerResult result = compilerService.compilePrepared(prepared, currentDirectory, true, variableValues);
        if (result.hasErrors()) {
            showError(result);
            return;
        }

        preparedResult = prepared;
        outputArea.setText(formatResult(result));
    }

    private void rebuildVariablePanel(CompilerResult prepared) {
        variablePanel.removeAll();
        variableFields.clear();

        for (String variable : prepared.getVariableNames()) {
            JTextField field = new JTextField(8);
            variableFields.put(variable, field);
            variablePanel.add(new JLabel("Введите значение " + variable + ":"));
            variablePanel.add(field);
        }

        if (prepared.getVariableNames().isEmpty()) {
            variablePanel.add(new JLabel("Переменные не требуются"));
        }

        variablePanel.revalidate();
        variablePanel.repaint();
    }

    private Map<String, Integer> readVariableValues() {
        Map<String, Integer> values = new LinkedHashMap<>();

        for (Map.Entry<String, JTextField> entry : variableFields.entrySet()) {
            String text = entry.getValue().getText().trim();
            if (text.isEmpty()) {
                return null;
            }
            try {
                values.put(entry.getKey(), Integer.parseInt(text));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Значение " + entry.getKey() + " должно быть целым числом.");
                return null;
            }
        }

        return values;
    }

    private void showError(CompilerResult result) {
        highlightError(result.getErrorPosition());
        outputArea.setText(formatError(result));
    }

    private void clearErrorHighlight() {
        inputPane.getHighlighter().removeAllHighlights();
    }

    private void highlightError(Integer position) {
        if (position == null) {
            return;
        }

        int length = inputPane.getDocument().getLength();
        if (length == 0) {
            return;
        }
        int start = Math.max(0, Math.min(position, length - 1));
        int end = Math.min(start + 1, length);

        try {
            Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 170, 170));
            inputPane.getHighlighter().addHighlight(start, end, painter);
            inputPane.setCaretPosition(start);
        } catch (BadLocationException ignored) {
            // Если позиция вышла за границы документа, текст ошибки все равно будет показан справа.
        }
    }

    private String formatError(CompilerResult result) {
        StringBuilder builder = new StringBuilder();
        builder.append("ОШИБКА\n");
        if (result.getErrorPosition() != null) {
            builder.append("Позиция ошибки: ").append(result.getErrorPosition()).append("\n\n");
            builder.append(inputPane.getText()).append("\n");
            builder.append(" ".repeat(Math.max(0, result.getErrorPosition())-1)).append("^\n\n");
        }
        builder.append("Ошибка: ").append(result.getErrors());
        builder.append("\n\nФайл errors.log создан или обновлен.");
        return builder.toString();
    }

    private String formatResult(CompilerResult result) {
        if (result.isOptimizedToConstant()) {
            return "AST исходный:\n" + result.getOriginalAst().print()
                    + "\n\nAST после оптимизации:\n" + result.getOptimizedAst().print()
                    + "\n\nВыражение полностью оптимизировано до константы."
                    + "\n\nРезультат выражения: " + result.getEvaluationResult()
                    + "\n\nКлассическая таблица четверок:\nне требуется"
                    + "\n\nВычисляемая таблица четверок:\nне требуется"
                    + "\n\nAssembler:\nне требуется";
        }

        String evaluatedQuadruples = result.getQuadruples().stream()
                .map(Quadruple::toEvaluatedString)
                .collect(Collectors.joining(System.lineSeparator()));

        return "AST исходный:\n" + result.getOriginalAst().print()
                + "\n\nAST после оптимизации:\n" + result.getOptimizedAst().print()
                + "\n\nЗначения переменных:\n" + formatVariables(result)
                + "\n\nРезультат выражения: " + result.getEvaluationResult()
                + "\n\nКлассическая таблица четверок:\nсм. левую часть вычисляемой таблицы"
                + "\n\nВычисляемая таблица четверок:\n" + evaluatedQuadruples
                + "\n\nAssembler:\n" + result.getAssembler()
                + "\nФайлы quadruples.txt, output.asm и errors.log созданы или обновлены.";
    }

    private String formatVariables(CompilerResult result) {
        if (result.getVariableValues().isEmpty()) {
            return "нет";
        }
        return result.getVariableValues().entrySet().stream()
                .map(entry -> entry.getKey() + " = " + entry.getValue())
                .collect(Collectors.joining(System.lineSeparator()));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CompilerGui().setVisible(true));
    }
}
