package eflect.utils;

import com.sun.jna.Library;
import com.sun.jna.Native;
import java.util.concurrent.atomic.AtomicInteger;

/** Utilities to access the operating system. */
public final class OsUtils {
  private static interface libcl extends Library {
    static libcl instance = (libcl) Native.loadLibrary("c", libcl.class);
    int getpid();
  }

  private static AtomicInteger pid = new AtomicInteger(-1);

  public static int getProcessId() {
    if (pid.get() == -1) {
      try {
          pid.set(libcl.instance.getpid());
      } catch (UnsatisfiedLinkError e) {
        pid.set(-1);
      }
    }
    return pid.get();
  }

  public static void setProcessId() {
    pid.set(-1);
    getProcessId();
  }

  public static void setProcessId(int id) {
    pid.set(id);
  }
}
