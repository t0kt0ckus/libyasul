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

/** Initializes the Yasul shared library, and acts as a Shell sessions factory.
 * <p>This is the recommended entry point for any client Android application.
 * </p>
 *
 */
public class YslContext {


    private static YslContext _singleton = null;

    /** Access to the Ysl context singleton.
     * <p>If needed, the context is initialized: this initialization may occur on main thread.</p>
     *
     * @param appCtx A valid Android application context.
     * @param shouldDebugYsl When set, enabled additional logging information.
     *
     * @return An initialized context, or <code>null</code> when failed to initialize.
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
     * @return An initialized context, or <code>null</code> when no such initialization occurred.
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

    /** Access the Yasul context log file.
     *
     * @return An absolute path.
     *
     * @see Libyasul#getlog()
     */
    public String getLogpath() {
        return Libyasul.getlog();
    }

    /** Opens a new Shell session.
     * <p>This may take more or less time, and involve user interactions and/or timeout.
     * Thus, this implementation is asynchronous
     * ({@link org.openmarl.yasul.YslAsyncFactory YslAsyncFactory}, and calling thread will be
     * signaled once the native IPC setup status is consistent.
     * </p>
     * <p>This native IPC setup is implemented by
     * <code>ysl_session_t *yasul_open_session(const char *logdir, int flags)</code>:
     * <ol>
     *     <li>If no suitable <code>su</code> binary is found, abort.</li>
     *     <li>If fails to initialize IPC sockets, abort.</li>
     *     <li>If fails to <code>fork()</code>, abort.</li>
     *     <li>Sends a preamble command string to child process, and wait ...</li>
     *     <ul>
     *         <li>a) the user refuses SU permission, or some confirmation dialog
     *         times out: abort.</li>
     *         <li>b) the child process died for any other reason: abort.</li>
     *         <li>c) the user accepts SU permission, or no interaction was involved: continue ...
     *         </li>
     *     </ul>
     *     <li>Shell session is confirmed:</li>
     *     <ul>
     *         <li>initializes native session peer</li>
     *         <li><code>dup2()</code> appropriate socket descriptors</li>
     *         <li>create native session's threads (3)</li>
     *     </ul>
     *     <li>The native session is ready to tunnel command strings to Shell child process.</li>
     * </ol>
     * </p>
     *
     * @param client The client to signal once native IPC setup has completed.
     * @param ctlFlags The session's initial control flags. The available flags are defined as
     *                 <code>YslSession.SF_XXXX</code>.
     *
     */
    public void openSession(YslObserver client, int ctlFlags) {
        new YslAsyncFactory(mAppCtx, client, ctlFlags).execute();
    }

    private class BootstrapError extends Exception {
    }

    private static final String TAG = "YASUL";
}
