package com.termux.terminal;

/**
 * JNI bindings for libtermux.so.
 * Handles true POSIX pseudo-terminal (PTY) allocation and subprocess spawning on Android.
 */
public final class JNI {
    static {
        System.loadLibrary("termux");
    }

    public static native int createSubprocess(
        String cmd,
        String cwd,
        String[] args,
        String[] envVars,
        int[] processIdArray,
        int rows,
        int columns
    );

    public static native void setPtyWindowSize(int fd, int rows, int cols);
    public static native void setPtyUTF8Mode(int fd);
    public static native int waitFor(int pid);
    public static native void close(int fd);
}
