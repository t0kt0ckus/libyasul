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

import android.content.Context;
import android.util.Log;

/** Initializes the Yasul shared library, and acts as a shell sessions factory.
 *
 */
public class YslContext {


    private static YslContext _singleton = null;

    /** Access to the Ysl context singleton.
     * <p>If needed, the context initializes the shared library:
     * this initialization may occur on main thread.
     * </p>
     *
     * @param appCtx A valid Android application context.
     * @param shouldDebugYsl When set, enable additional logging information at context level.
     *
     * @return An initialized context, or <code>null</code> if any error occurred.
     *
     * @see org.openmarl.yasul.Libyasul#bootstrap(String, boolean)
     */
    public static YslContext getInstance(Context appCtx, boolean shouldDebugYsl) {
        if (_singleton == null) {
            try {
                _singleton = new YslContext(appCtx, shouldDebugYsl);
            }
            catch (BootstrapError e) {
                _singleton = null;
            }
        }
        return _singleton;
    }

    /** Access to the Ysl context singleton.
     *
     * @return An initialized context, or <code>null</code> when the shared library has not been
     * initialized.
     */
    public static YslContext getInstance() {
        return _singleton;
    }

    private Context mAppCtx;

    private YslContext(Context appContext, boolean shouldDebugYsl) throws BootstrapError {
        mAppCtx = appContext;
        String baseDir = appContext.getApplicationContext().getFilesDir().getAbsolutePath();
        Log.i(TAG,
                String.format("Bootstraping Ysl context in %s mode with base directory %s",
                        shouldDebugYsl ? "debug" : "quiet",
                        baseDir));

        if (Libyasul.bootstrap(baseDir, shouldDebugYsl) != 0) {
            Log.e(TAG,
                    "Failed to bootstrap, see Logcat and/or yasul-<pid>.log for possible cause !");
            throw new BootstrapError();
        }
    }

    /** Access the Yasul library version.
     *
     * @return The version number.
     */
    public String getVersion() {
        return Libyasul.getversion();
    }

    /** Access the Yasul library log file.
     *
     * @return The absolute log file path, for eg.
     * <code>/data/data/com.example.app/files/yasul-12345.log</code>, where
     * <code>/data/data/com.example.app</code> is the private filesystem of the
     * Android application that loaded the library, and <code>12345</code> its PID.
     */
    public String getLogpath() {
        return Libyasul.getlog();
    }

    /** Initiates the creation of a new shell session.
     *
     * <p>This may take more or less time, and involve user interactions and/or a timeout.
     * This implementation is synchronous, and should not be used on main/UI thread.
     * </p>
     *
     * @param ctlFlags The session's initial control flags. The available flags are defined as
     *                 <code>YslSession.SF_XXXX</code>.
     * @param secontext An SE Linux context name, or <code>null</code> when no context switch is
     *                  specified. This feature is only compatible with
     *                  <a href="http://su.chainfire.eu/#selinux-contexts-switching-how">SuperSU versions 1.90 and up</a>.
     *
     * @return The created session, or <code>null</code> on error.
     */
    public YslSession openSession(int ctlFlags, String secontext) {
        YslPort port = Libyasul.open(ctlFlags, secontext);
        if (port != null) {
            YslSession session = new YslSession(mAppCtx, port.pid, port.ID, port.stdout,
                    port.stderr);
            Log.i(TAG,
                    String.format("Shell session: %s", session.toString()));
            return session;
        }
        Log.e(TAG,
                "Failed to create session, see Logcat and/or yasul-<pid>.log for possible cause !");
        return null;
    }

    /** Asynchronously initiates the creation of a new shell session.
     *
     * <p>This may take more or less time, and involve user interactions and/or a timeout.
     * Thus, this {@link YslAsyncSessionFactory implementation} is asynchronous and session's
     * initialization occurs on a background thread. The client will be signaled on the main/UI
     * thread once the setup result is available.
     * </p>
     *
     * @param client The client to signal.
     * @param ctlFlags The session's initial control flags. The available flags are defined as
     *                 <code>YslSession.SF_XXXX</code>.
     * @param secontext An SE Linux context name, or <code>null</code> when no context switch is
     *                  specified. This feature is only compatible with
     *                  <a href="http://su.chainfire.eu/#selinux-contexts-switching-how">SuperSU versions 1.90 and up</a>.
     */
    public void openSessionAsync(YslObserver client, int ctlFlags, String secontext) {
        new YslAsyncSessionFactory(mAppCtx, client, ctlFlags, secontext).execute();
    }

    private class BootstrapError extends Exception {
    }

    private static final String TAG = "YASUL";
}
