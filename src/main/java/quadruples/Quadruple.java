package quadruples;

// Четверка хранит исходную операцию и ее вычисленную форму.
// Левая часть строки показывает IR: (+, A, B, T1).
// Правая часть показывает подстановку значений: (+, 5, 3, 8).
public class Quadruple {
    private final String op;
    private final String arg1;
    private final String arg2;
    private final String result;
    private final Integer evaluatedArg1;
    private final Integer evaluatedArg2;
    private final Integer evaluatedResult;

    public Quadruple(
            String op,
            String arg1,
            String arg2,
            String result,
            Integer evaluatedArg1,
            Integer evaluatedArg2,
            Integer evaluatedResult
    ) {
        this.op = op;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.result = result;
        this.evaluatedArg1 = evaluatedArg1;
        this.evaluatedArg2 = evaluatedArg2;
        this.evaluatedResult = evaluatedResult;
    }

    public String getOp() {
        return op;
    }

    public String getArg1() {
        return arg1;
    }

    public String getArg2() {
        return arg2;
    }

    public String getResult() {
        return result;
    }

    public Integer getEvaluatedArg1() {
        return evaluatedArg1;
    }

    public Integer getEvaluatedArg2() {
        return evaluatedArg2;
    }

    public Integer getEvaluatedResult() {
        return evaluatedResult;
    }

    public String toClassicString() {
        return "(" + op + ", " + arg1 + ", " + arg2 + ", " + result + ")";
    }

    public String toEvaluatedString() {
        return toClassicString() + " -> ("
                + op + ", "
                + evaluatedArg1 + ", "
                + evaluatedArg2 + ", "
                + evaluatedResult + ")";
    }

    @Override
    public String toString() {
        return toEvaluatedString();
    }
}
