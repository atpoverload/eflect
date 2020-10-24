package eflect.util;

import com.sun.jna.Library;
import com.sun.jna.Native;
import java.util.concurrent.atomic.AtomicLong;

/** Utilities to access the operating system. */
public final class OsUtil {
  // access to getpid() through the JNA
  private static interface libcl extends Library {
    static libcl instance = (libcl) Native.loadLibrary("c", libcl.class);
    int getpid();
  }

  private static AtomicLong pid = new AtomicLong(-1);

  /** Caches and returns the pid if necessary; otherwise return the cached value. */
  public static long getProcessId() {
    if (pid.get() == -1) {
      try {
          pid.set(libcl.instance.getpid());
      } catch (UnsatisfiedLinkError e) {
        pid.set(-1);
      }
    }
    return pid.get();
  }

  /** Resets the pid. */
  public static void setProcessId() {
    pid.set(-1);
    getProcessId();
  }

  /** Sets the pid to an arbitrary process. */
  public static void setProcessId(long id) {
    pid.set(id);
  }

  private OsUtil() {}
}
