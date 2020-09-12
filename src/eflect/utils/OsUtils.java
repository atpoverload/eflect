package eflect.utils;

import static jrapl.util.EnergyCheckUtils.SOCKETS;

import com.sun.jna.Library;
import com.sun.jna.Native;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

public final class OsUtils {
  private static int CPUS = Runtime.getRuntime().availableProcessors();
  /** Wrapper for libc methods. This is locked so that users stick to the public API. */
  /** Dummy wrapper for the libc library instance. */
  private static interface libcl extends Library {
    static libcl instance = (libcl) Native.loadLibrary("c", libcl.class);
    int getpid();
  }

  private static AtomicInteger pid = new AtomicInteger(0);

  public static int getProcessId() {
    if (pid.get() != -1) {
      try {
          pid.set(libcl.instance.getpid());
      } catch (UnsatisfiedLinkError e) {
        pid.set(-1);
      }
    }
    return pid.get();
  }
}
