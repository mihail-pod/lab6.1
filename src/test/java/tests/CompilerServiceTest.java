package tests;

import org.junit.jupiter.api.Test;
import utils.CompilerResult;
import utils.CompilerService;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompilerServiceTest {

    @Test
    void skipsIrAndAssemblerWhenOptimizedToConstant() {
        CompilerService service = new CompilerService();
        CompilerResult prepared = service.prepare("(/(*(-A,0),(*C,0)),(+C,0))");

        CompilerResult result = service.compilePrepared(prepared, Path.of("."), false, Map.of());

        assertTrue(result.isOptimizedToConstant());
        assertEquals(0, result.getEvaluationResult());
        assertTrue(result.getQuadruples().isEmpty());
        assertEquals("", result.getAssembler());
    }
}
