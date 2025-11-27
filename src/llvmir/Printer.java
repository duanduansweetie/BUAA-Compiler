package llvmir;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import llvmir.value.structure.Module; // 添加这一行

public class Printer {
    public static BufferedWriter writer;
    private static Module module;

    public Printer(Module module) {
        this.module =module;
    }



    public static void initBuffer(String outputFile) throws IOException {
        writer = new BufferedWriter(new FileWriter(outputFile));
    }

    public static void closeBuffer() throws IOException {
        if (writer != null) writer.close();
    }

    public static void showIr() throws IOException {
        writer.write(module.toString());
    }
}
