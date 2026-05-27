package assembler;

import quadruples.Quadruple;
import utils.CompilerException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Генерирует учебный TASM-подобный assembler по вычисляемой таблице четверок.
// Значения переменных уже известны, поэтому они записываются в data segment.
public class AssemblerGenerator {

    public String generate(List<Quadruple> quadruples, Map<String, Integer> variableValues) {
        StringBuilder code = new StringBuilder();
        Set<String> variables = collectVariables(quadruples);
        variables.addAll(variableValues.keySet());

        code.append("include io.asm\n\n");
        appendStackSegment(code);
        appendDataSegment(code, variables, variableValues);
        appendCodeStart(code);

        for (Quadruple quadruple : quadruples) {
            appendOperation(code, quadruple);
        }

        appendResultOutput(code, quadruples);
        appendProgramEnd(code);
        return code.toString();
    }

    private void appendStackSegment(StringBuilder code) {
        code.append("stack1 segment stack\n");
        code.append("    db 256 dup(?)\n");
        code.append("stack1 ends\n\n");
    }

    private void appendDataSegment(StringBuilder code, Set<String> variables, Map<String, Integer> variableValues) {
        code.append("data segment\n");
        for (String variable : variables) {
            int value = variableValues.getOrDefault(variable, 0);
            code.append("    ").append(variable).append(" dw ").append(value).append("\n");
        }
        code.append("data ends\n\n");
    }

    private void appendCodeStart(StringBuilder code) {
        code.append("code segment\n\n");
        code.append("start:\n");
        code.append("    assume ss:stack1, ds:data, cs:code\n\n");
        code.append("    mov ax, data\n");
        code.append("    mov ds, ax\n\n");
    }

    // Финальный результат лежит в последней временной переменной.
    // io.asm берет число из AX и печатает его через outint.
    private void appendResultOutput(StringBuilder code, List<Quadruple> quadruples) {
        if (quadruples.isEmpty()) {
            return;
        }

        String finalTemp = quadruples.get(quadruples.size() - 1).getResult();
        code.append("    ; Финальный результат находится в ").append(finalTemp).append("\n");
        code.append("    ; Вывод результата\n");
        code.append("    mov ax, ").append(finalTemp).append("\n");
        code.append("    outint ax\n");
        code.append("    newline\n\n");
    }

    private void appendProgramEnd(StringBuilder code) {
        code.append("    mov ah, 4ch\n");
        code.append("    int 21h\n\n");
        code.append("code ends\n");
        code.append("end start\n");
    }

    // Объявляем только имена памяти: пользовательские переменные и временные Tn.
    private Set<String> collectVariables(List<Quadruple> quadruples) {
        Set<String> variables = new LinkedHashSet<>();
        for (Quadruple quadruple : quadruples) {
            addIfMemoryName(variables, quadruple.getArg1());
            addIfMemoryName(variables, quadruple.getArg2());
            addIfMemoryName(variables, quadruple.getResult());
        }
        return variables;
    }

    private void addIfMemoryName(Set<String> variables, String value) {
        if (!value.matches("-?\\d+")) {
            variables.add(value);
        }
    }

    private void appendOperation(StringBuilder code, Quadruple q) {
        code.append("    ; ").append(q.toClassicString()).append("\n");
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
