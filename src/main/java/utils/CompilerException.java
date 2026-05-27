package utils;

// Общее исключение компилятора.
// Если ошибка связана с конкретным символом входной строки, хранится позиция.
public class CompilerException extends RuntimeException {
    private final int position;

    public CompilerException(String message) {
        super(message);
        this.position = -1;
    }

    public CompilerException(String message, int position) {
        super(message + " в позиции " + position);
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
