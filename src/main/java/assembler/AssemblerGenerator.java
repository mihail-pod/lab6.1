package assembler;

import quadruples.Quadruple;
import utils.CompilerException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Генератор TASM-подобного assembler-кода по таблице четверок
public class AssemblerGenerator {

    // Формирование текста assembler
    public String generate(List<Quadruple> quadruples) {
        StringBuilder code = new StringBuilder();
        Set<String> variables = collectVariables(quadruples);

        code.append("stack1 segment\n");
        code.append("    db 256 dup(?)\n");
        code.append("stack1 ends\n\n");

        code.append("data segment\n");
        for (String variable : variables) {
            code.append("    ").append(variable).append(" dw 0\n");
        }
        code.append("data ends\n\n");

        code.append("code segment\n\n");
        code.append("start:\n");
        code.append("    assume ss:stack1, ds:data, cs:code\n\n");
        code.append("    mov ax, data\n");
        code.append("    mov ds, ax\n\n");

        for (Quadruple quadruple : quadruples) {
            appendOperation(code, quadruple);
        }

        code.append("    mov ah, 4ch\n");
        code.append("    int 21h\n\n");
        code.append("code ends\n");
        code.append("end start\n");

        return code.toString();
    }

    // Собирает все имена объявляемые в сегменте данных
    private Set<String> collectVariables(List<Quadruple> quadruples) {
        Set<String> variables = new LinkedHashSet<>();
        for (Quadruple quadruple : quadruples) {
            addIfMemoryName(variables, quadruple.getArg1());
            addIfMemoryName(variables, quadruple.getArg2());
            addIfMemoryName(variables, quadruple.getResult());
        }
        return variables;
    }

    // Числа не объявляются в data а переменные и T-результаты объявляются.
    private void addIfMemoryName(Set<String> variables, String value) {
        if (!value.matches("\\d+")) {
            variables.add(value);
        }
    }

    // assembler-инструкция для четверки
    private void appendOperation(StringBuilder code, Quadruple q) {
        code.append("    ; ").append(q).append("\n");
        code.append("    mov ax, ").append(q.getArg1()).append("\n");

        switch (q.getOp()) {
            case "+" -> code.append("    add ax, ").append(q.getArg2()).append("\n");
            case "-" -> code.append("    sub ax, ").append(q.getArg2()).append("\n");
            case "*" -> code.append("    mov bx, ").append(q.getArg2()).append("\n")
                    .append("    mul bx\n");
            case "/" -> code.append("    mov dx, 0\n")
                    .append("    mov bx, ").append(q.getArg2()).append("\n")
                    .append("    div bx\n");
            default -> throw new CompilerException("Неизвестная операция assembler-генератора: " + q.getOp());
        }

        code.append("    mov ").append(q.getResult()).append(", ax\n\n");
    }
}
