package tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class OutputTools {

    public static void write(String output, String filePath) {
        try (FileWriter writer = new FileWriter(new File(filePath))) {
            writer.write(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
