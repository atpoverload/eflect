package eflect.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/** Utility to write csv data. */
// TODO(timur): the entire data exchange will eventually have to change, so this will eventually go
// away
public final class WriterUtil {
  public static void writeCsv(String directory, String fileName, String header, Iterable<?> data) {
    try (PrintWriter writer = new PrintWriter(new FileWriter(new File(directory, fileName)))) {
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
