import utils.CompilerResult;
import utils.CompilerService;

import java.nio.file.Path;

// Консольная точка входа
// Вся логика чтения input.txt и записи выходных файлов находится в CompilerService
public class Main {

    // Основной метод консольного режима
    public static void main(String[] args) {
        CompilerService compiler = new CompilerService();
        CompilerResult result = compiler.compileInputFile(Path.of("."));

        if (result.hasErrors()) {
            System.out.println("Компиляция завершена с ошибками. Подробности записаны в errors.log");
            System.out.println(result.getErrors());
        } else {
            System.out.println("Компиляция успешно завершена.");
            System.out.println("Созданы файлы: quadruples.txt, output.asm, errors.log");
        }
    }
}
