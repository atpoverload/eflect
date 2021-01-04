package eflect.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import one.profiler.AsyncProfiler;
import one.profiler.Events;

/** Wrapper around the async-profiler that safely sets it up from an internal jar. */
public final class AsyncProfilerUtil {
  private static final int DEFAULT_RATE_MS = 1;
  // TODO(timurbey): this should probably come from a module
  private static final Duration asyncRate =
      Duration.ofMillis(
          Long.parseLong(
              System.getProperty("chappie.rate.async", Integer.toString(DEFAULT_RATE_MS))));
  private static final boolean noAsync = !setupAsync();

  // adapted from https://github.com/adamheinrich/native-utils
  private static File createLibraryFileFromJar(String library) throws IOException {
    File temp = File.createTempFile(library, null);
    temp.deleteOnExit();

    if (!temp.exists()) {
      throw new FileNotFoundException("Could not create a temporary file.");
    }

    // Prepare buffer for data copying
    byte[] buffer = new byte[1024];
    int readBytes;

    try (InputStream is = AsyncProfilerUtil.class.getResourceAsStream(library)) {
      if (is == null) {
        throw new FileNotFoundException("Could not find library " + library + " in jar.");
      }
      // Open output stream and copy data between source file in JAR and the temporary file
      try (FileOutputStream os = new FileOutputStream(temp)) {
        try {
          while ((readBytes = is.read(buffer)) != -1) {
            os.write(buffer, 0, readBytes);
          }
        } finally {
          return temp;
        }
      }
    }
  }

  /** Set up and start the async-profiler. */
  private static boolean setupAsync() {
    try {
      // only supporting sub-second for the moment
      long rate = asyncRate.getNano();
      AsyncProfiler.getInstance(
              createLibraryFileFromJar("/external/async_profiler/libasyncProfiler.so").getPath())
          .start(Events.CPU, asyncRate.getNano());
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /** Returns un-split string of async-profiler records while safely pausing the profiler. */
  public static String readAsyncProfiler() {
    AsyncProfiler.getInstance().stop();
    String traces = AsyncProfiler.getInstance().dumpRecords();
    AsyncProfiler.getInstance().resume(Events.CPU, asyncRate.getNano());
    return traces;
  }
}
