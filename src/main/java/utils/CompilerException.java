package utils;

// Общая ошибка компилятора для всех этапов лексера парсера оптимизатора и генераторов
public class CompilerException extends RuntimeException {

    // Используется, когда позиция ошибки не нужна или неизвестна
    public CompilerException(String message) {
        super(message);
    }

    // Используется лексером и парсером, чтобы показать место ошибки во входной строке
    public CompilerException(String message, int position) {
        super(message + " в позиции " + position);
    }
}
