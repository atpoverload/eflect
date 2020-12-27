package eflect.experiments;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public final class WriterUtils {
  public static void writeCsv(String fileName, String header, Iterable<?> data) {
    try (PrintWriter writer = new PrintWriter(new FileWriter(new File(fileName)))) {
      writer.println(header);
      for (Object d : data) {
        writer.println(d.toString());
      }
    } catch (IOException e) {
      System.out.println("couldn't write eflect log");
      e.printStackTrace();
    }
  }
}
