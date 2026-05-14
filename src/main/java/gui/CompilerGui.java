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
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.io.File;
import java.nio.file.Path;
import java.util.stream.Collectors;


// десктоп
public class CompilerGui extends JFrame {
    private final JTextArea inputArea = new JTextArea();
    private final JTextArea outputArea = new JTextArea();
    private final CompilerService compilerService = new CompilerService();
    private Path currentDirectory = Path.of(".");

    // Создание окна
    public CompilerGui() {
        super("Компилятор префиксных арифметических выражений");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        inputArea.setLineWrap(true);
        outputArea.setEditable(false);

        add(createToolbar(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
    }

    // Панель с кнопками загрузки файла и запуска компиляции
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

    // окно области ввода и результатов
    private JSplitPane createMainPanel() {
        JPanel left = new JPanel(new BorderLayout());
        left.add(new JLabel("Входное выражение"), BorderLayout.NORTH);
        left.add(new JScrollPane(inputArea), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout());
        right.add(new JLabel("Результаты компиляции"), BorderLayout.NORTH);
        right.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        splitPane.setResizeWeight(0.35);
        return splitPane;
    }

    // загружает выбранный текстовый файл
    private void chooseFile() {
        JFileChooser chooser = new JFileChooser(currentDirectory.toFile());
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        try {
            currentDirectory = file.toPath().getParent();
            inputArea.setText(compilerService.readSource(file.toPath()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Не удалось прочитать файл: " + ex.getMessage());
        }
    }

    // Запускает компиляцию выражения из поля ввода и записывает выходные файлы
    private void compile() {
        CompilerResult result = compilerService.compile(inputArea.getText(), currentDirectory, true);
        outputArea.setText(formatResult(result));
    }

    // текст вывода
    private String formatResult(CompilerResult result) {
        if (result.hasErrors()) {
            return "ОШИБКИ\n" + result.getErrors() + "\n\nФайл errors.log создан или обновлен.";
        }

        String quadruples = result.getQuadruples().stream()
                .map(Quadruple::toString)
                .collect(Collectors.joining(System.lineSeparator()));

        return "AST исходный:\n" + result.getOriginalAst().print()
                + "\n\nAST после оптимизации:\n" + result.getOptimizedAst().print()
                + "\n\nЧетверки:\n" + quadruples
                + "\n\nAssembler:\n" + result.getAssembler()
                + "\nФайлы quadruples.txt, output.asm и errors.log созданы или обновлены.";
    }

    // запуск
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CompilerGui().setVisible(true));
    }
}
