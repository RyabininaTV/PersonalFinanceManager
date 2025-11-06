import java.io.*;
import java.nio.charset.StandardCharsets;

public class TestEncoding {
    public static void main(String[] args) throws Exception {
        String russianText = "Тоша";
        System.out.println("Текст: " + russianText);
        System.out.println("Длина: " + russianText.length());
        System.out.println("Байты в UTF-8: " + java.util.Arrays.toString(russianText.getBytes(StandardCharsets.UTF_8)));

        // Тест записи и чтения
        try (Writer w = new OutputStreamWriter(new FileOutputStream("test.txt"), StandardCharsets.UTF_8)) {
            w.write(russianText);
        }

        try (Reader r = new InputStreamReader(new FileInputStream("test.txt"), StandardCharsets.UTF_8)) {
            char[] buffer = new char[10];
            int read = r.read(buffer);
            String readText = new String(buffer, 0, read);
            System.out.println("Прочитано: " + readText);
            System.out.println("Совпадает: " + russianText.equals(readText));
        }
    }
}