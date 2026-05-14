package ast;

// Единый узел значения для числа или переменной
public class ValueNode extends Node {
    private final String value;
    private final boolean number;

    // Создает узел значения, Флаг числа - number
    private ValueNode(String value, boolean number) {
        this.value = value;
        this.number = number;
    }

    // узел числовой константы
    public static ValueNode number(int value) {
        return new ValueNode(Integer.toString(value), true);
    }

    // узел переменной из одного символа
    public static ValueNode variable(String name) {
        return new ValueNode(name, false);
    }

    // Возвращает текст значения для четверок и assembler-кода
    public String getValue() {
        return value;
    }

    // Позволяет оптимизатору отличать число от переменной.
    public boolean isNumber() {
        return number;
    }

    // Возвращает числовое значение
    public int getNumber() {
        return Integer.parseInt(value);
    }

    // Возвращает символ
    @Override
    public String print() {
        return value;
    }
}
