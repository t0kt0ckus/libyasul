/*
 * yasul: Yet another Android SU library. 
 *
 * t0kt0ckus
 * (C) 2014,2015
 *
 * License LGPLv2, GPLv3
 *
 */
package org.openmarl.yasul;

/**
 * JNI bridge to the native Yasul library (<code>libyasul.so</code>).
 * <p>The corresponding C API is specified in <code>yasul_jni.h</code>.
 * </p>
 *
 */
public class Libyasul {

    /** Bootstraps the Yasul library.
     * SHOULD be called once when initializing an application.
     *
     * @param logDir The base directory for logging.
     * @param debug When set, enables additional log content.
     */
    public static native int bootstrap(String logDir, boolean debug);

    /** Access to the Yasul library log file.
     *
     * @return The absolute log file path, for eg.
     * <code>/data/data/com.example.app/files/yasul-12345.log</code>, where
     * <code>/data/data/com.example.app</code> is the private filesystem of the
     * Android application that loaded the library, and <code>12345</code> its PID.
     */
    public static native String getlog();

    /** Access to the Yasul library version number.
     *
     * @return The version number.
     */
    public static native String getversion();

    /** Opens a Yasul session.
     * This call blocks until accept/refuse/timeout, and SHOULD NOT occurs on main thread.
     * <p>A session SHOULD be released through {@link #exit(long, long, boolean) exit()} when
     * no more needed, or as soon as a <i>broken pipe</i> is detected (the Shell process's died
     * abnormally).
     * </p>
     *
     * @param flags The session's initial control flags.
     *
     * @return A port to an initialized Shell session on success, <code>null</code> on failure.
     */
    public static native YslPort open(int flags);

    /** Answers whether a Shell session is accepting command strings.
     *
     * @param sessionId A session ID (the session SHOULD NOT have been previously closed with
     *                  {@link #exit(long, long, boolean) exit()}).
     *
     * @return <code>0</code> when this session accepts command strings, or
     * <code>EPIPE</code> when it's been invalidated due to a <i>broken pipe</i> since last
     * successful <code>exec()</code>.
     */
    public static native int stat(long sessionId);

    /** Sets a session's control flag.
     * <p>Refer to <code>YslSession.SF_XXXX</code> for a description of available flags.
     * </p>
     *
     * @param sessionId A session ID (the session SHOULD NOT have been previously closed with
     *                  {@link #exit(long, long, boolean) exit()}).
     * @param flag The flag to set.
     * @param isSet The flag value.
     *
     * @return The new session's flags bitmask.
     */
    public static native int cfset(long sessionId, int flag, boolean isSet);

    /** Access a session's control flag.
     * <p>Refer to <code>YslSession.SF_XXXX</code> for a description of available flags.
     * </p>
     *
     * @param sessionId A session ID (the session SHOULD NOT have been previously closed with
     *                  {@link #exit(long, long, boolean) exit()}).
     * @param flag The flag to get.
     *
     * @return <code>true</code> when the flag is set.
     */
    public static native boolean cfget(long sessionId, int flag);

    /** Executes a command string.
     * This call blocks until command completion, and SHOULD NOT occurs on main thread when
     * expected to take some time.
     * <p>Several command strings queued by a single client thread will be executed in order of
     * submission. Several command strings queued by more than one client thread will be
     * atomically executed, but in an undetermined order
     * (as <code>pthtread_unlock()</code> call will wake-up any of the waiting threads).
     * </p>
     *
     * @param sessionId A session ID (the session SHOULD NOT have been previously closed with
     *                  {@link #exit(long, long, boolean) exit()}).
     * @param cmdStr The command string to execute, which may be a <i>Simple Command</i>,
     *               a <i>Pipeline</i>, a <i>List</i> or a <i>Compound Command</i>,
     *               as specified by the relevant <code>man</code> page.
     *
     * @return A parcel containing the Shell <i>response</i>, or <code>null</code> when the session
     * has been invalidated due to a <i>broken pipe</i> since last successful <code>exec()</code>.
     *
     */
    public static native YslParcel exec(long sessionId, String cmdStr);

    /** Answers the last <i>TTY line</i> known from the Shell process.
     * <p> This is the last output the shell process produced to <code>stdout</code>,
     * and that's been made available to any client thread for reading before command completion.
     *  Its actual content is configured by the {@link YslSession#SF_TAIL SF_TAIL} and
     * {@link YslSession#SF_ZTTY SF_ZTTY} session's control flags.
     * </p>
     *
     * @param sessionId A session ID (the session SHOULD NOT have been previously closed with
     *                  {@link #exit(long, long, boolean) exit()}).
     *
     * @return The last available <i>TTY line</i>.
     */
    public static native String lasttty(long sessionId);

    /** Terminates a Shell session.
     * <p>A terminated Shell session SHOULD NOT be further referenced by its ID,
     * as it's natively removed.</p>
     *
     * @param sessionId A session ID (the session SHOULD NOT have been previously closed with
     *                  {@link #exit(long, long, boolean) exit()}).
     * @param timeoutMillisec The maximum time to wait for Shell process termination.
     * @param forceKill When set, if the Shell process's still running after timeout,
     *                  we'll try to <code>kill -SIGKILL</code> it. When unset, the Shell process
     *                  would possibly later become <i>zombie</i> (parent died), as there's a
     *                  probability the Android application exits first.
     */
    public static native void exit(long sessionId, long timeoutMillisec, boolean forceKill);

    /** Answers the PID of process with a given <code>cmdline</code>.
     * This is done through parsing the Linux <code>/proc</code> filesystem.
     *
     * @param cmdline The cmdline as it appears in <code>proc/PID/cmdline</code>.
     *
     * @return The process PID on success, a negative value on failure.
     */
    public static native int pid_cmdline(String cmdline);


    static final String LIBRARY = "yasul";
    static {
        System.loadLibrary(LIBRARY);
    }

    private Libyasul() {}
}
