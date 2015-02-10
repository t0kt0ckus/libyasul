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

/**
 * Represents a Shell session.
 * <p>A session is created by
 * {@link org.openmarl.yasul.YslContext#openSession(YslObserver, int) openSession()} and
 * remains valid until:
 * <ul>
 *     <li>the Shell process dies abnormally, or
 *     <li>a client calls {@link #exit(long, boolean) exit()}</li>
 * </ul>
 * At this stage, the session is known as <i>invalidated</i>, and any further API call will produce
 * an {@link org.openmarl.yasul.YslEpipeExcetion YslEpipeExcetion} error.</li>
 * </p>
 *
 * <p>A common session's control flags would be
 * <code>{@link #SF_EOUT} | {@link #SF_EERR} | {@link #SF_VERB}</code>,
 * sometimes clearing <code>{@link #SF_EOUT}</code> and/or <code>{@link #SF_EERR}</code> to
 * temporarily disable logging the shell process output, which could be eventually huge.
 * </p>
 *
 * @see org.openmarl.yasul.YslEpipeExcetion
 */
public class YslSession {

    /** Determines whether to echo the shell process stdout to a log file.
     * This can be useful to temporarily disable logging when a command is expected to produce
     * a huge output.
     * @see YslSession#getStdout()
     */
    public static final int SF_EOUT = 0x01;

    /** Determines whether to echo the shell process stderr to a log file.
     * This can be useful when one don't care about stderr, though she would then
     * miss <i>mksh</i> messages, for eg.
     * @see YslSession#getStderr()
     */
    public static final int SF_EERR = 0x02;

    /** Determines whether to update the last available TTY line after each line red from
     * the shell process stdout. This permits to access from another thread
     * the last output produced by the shell, before the command completed - which can be
     * useful for monitoring purpose.
     * @see YslSession#getLastTty()
     */
    public static final int SF_TAIL = 0x04;

    /** Determines whether to reset the internal TTY line buffer after a command's completed.
     * This can be useful when a session exceptionally produces huge output, and that we want to
     * release memory. When the session does not produce such an output on stdout,
     * performance should be better with this flag kept unset.
     */
    public static final int SF_ZTTY = 0x08;

    /** Determines whether the session's logging should be verbose.
     */
    public static final int SF_VERB = 0x10;


    private boolean mInvalidated; // by shell process broken pipe or normal close()
    private final long mID;
    private final int mPid;
    private String mStdout;
    private String mStderr;

    private Context mAppCtx;
    private YslShell mShell;

    YslSession(Context appCtx, int pid, long id, String stdout, String stderr) {
        mAppCtx = appCtx;
        mPid = pid;
        mID = id;
        mStdout = stdout;
        mStderr = stderr;
        mInvalidated = false;
    }

    /** Access this session's ID.
     *
     * @return The native session's address.
     */
    public long ID() {
        return mID;
    }

    /** Access the Shell process PID.
     *
     * @return The PID.
     */
    public int getPid() {
        return mPid;
    }

    /** Access the Shell process <i>stdout echo file</i>.
     * <p>This is the file where we dump the Shell process <code>stdout</code>,
     * for eg <code>/data/data/com.example.app/files/su_stdout-12345.log</code>,
     * where <code>12345</code> is the Shell process PID.
     * Its content is determined by {@link YslSession#SF_EOUT}.
     * </p>
     *
     * @return An absolute path.
     */
    public String getStdout() {
        return mStdout;
    }

    /** Access the Shell process <i>stderr echo file</i>.
     * <p>This is the file where we dump the Shell process <code>stderr</code>,
     * for eg <code>/data/data/com.example.app/files/su_stderr-12345.log</code>,
     * where <code>12345</code> is the Shell process PID.
     * Its content is determined by {@link YslSession#SF_EERR}.
     * </p>
     *
     * @return An absolute path.
     */
    public String getStderr() {
        return mStderr;
    }

    /** Answers whether this Shell session is ready to accept command strings.
     *
     * @return <code>true</code> when the session is valid.
     *
     * @see org.openmarl.yasul.Libyasul#stat(long)
     */
    public boolean isAvailable() {
        return ((! mInvalidated) && (Libyasul.stat(mID) == 0));
    }

    /** Sets a session control flag.
     *
     * @param flag The flag to set.
     * @param isSet The flag value.
     * @return The new session's flags.
     *
     * @throws YslEpipeExcetion when the session has been invalidated.
     *
     * @see org.openmarl.yasul.Libyasul#cfset(long, int, boolean)
     */
    public int setCtlFlag(int flag, boolean isSet) throws YslEpipeExcetion {
        if (mInvalidated)
            throw new YslEpipeExcetion();
        return Libyasul.cfset(mID, flag, isSet);
    }

    /** Access a session's control flag.
     *
     * @param flag The flag to get.
     *
     * @return <code>true</code> if the flag is set.
     *
     * @throws YslEpipeExcetion when the session has been invalidated.
     *
     * @see org.openmarl.yasul.Libyasul#cfget(long, int)
     */
    public boolean getCtlFlag(int flag) throws YslEpipeExcetion {
        if (mInvalidated)
            throw new YslEpipeExcetion();
        return Libyasul.cfget(mID, flag);
    }

    /** Access the <i>last known TTY line</i> of the Shell process.
     *
     * @return A may be <code>null</code> string.
     *
     * @throws YslEpipeExcetion when the session has been invalidated.
     *
     * @see org.openmarl.yasul.Libyasul#lasttty(long)
     */
    public String getLastTty() throws YslEpipeExcetion {
        if (mInvalidated)
            throw new YslEpipeExcetion();
        return Libyasul.lasttty(mID);
    }

    /** Executes a command string.
     * <p>This call blocks until command completion, and SHOULD NOT occurs on main thread when
     * expected to take some time.
     * <p>Several command strings queued by a single client thread will be executed in order of
     * submission. Several command strings queued by more than one client thread will be
     * atomically executed, but in undetermined order.
     * </p>
     *
     * @param cmdStr A Shell command string.
     *
     * @return The command string execution result.
     *
     * @throws YslEpipeExcetion when the session has been invalidated.
     *
     * @see org.openmarl.yasul.Libyasul#exec(long, String)
     */
    public YslParcel exec(String cmdStr) throws YslEpipeExcetion {
        if (mInvalidated)
            throw new YslEpipeExcetion();
        Log.d(TAG, String.format("%d: #exec(%s)", mPid, cmdStr));

        YslParcel retval = Libyasul.exec(mID, cmdStr);
        if (retval == null)
            invalidateOnEpipe();
        return retval;
    }

    /** Asynchronously executes a command string.
     * <p>Several command strings queued by a single client thread will be executed and signaled
     * in order of submission. Several command strings queued by more than one client thread will be
     * atomically executed and signaled, but in undetermined order.
     * </p>
     *
     * @param client The client to signal once command execution's completed. The client is signaled
     *               on the main/UI thread.
     * @param cmdstr A Shell command string.
     *
     */
    public void exec(YslObserver client, String cmdstr) throws YslEpipeExcetion {
        if (mInvalidated)
            throw new YslEpipeExcetion();
        Log.d(TAG, String.format("%d: #Async_exec(%s)", mPid, cmdstr));
        new YslAsyncExecutor(this, client, cmdstr).execute();
    }

    /** Exits this session.
     * <p>Any further API call will produce an
     * {@link org.openmarl.yasul.YslEpipeExcetion YslEpipeExcetion} error. This call does nothing
     * on a previously invalidated session.
     * </p>
     *
     * @param timeoutMillis The time in millisecond we wait the Shell process to gracefully exit,
     *                      completing any running command string.
     * @param forceKill Determines whether we should explicitly <code>kill</code> the Shell process
     *                  when it's still running after timeout.
     *
     * @see org.openmarl.yasul.Libyasul#exit(long, long, boolean)
     */
    public void exit(long timeoutMillis, boolean forceKill) {
        if (mInvalidated)
            Log.d(TAG, String.format("%d: Skipping double invalidation ...", mPid));
        else {
            Log.i(TAG, String.format("%d: exit(%d sec, forceKill: %b)", mPid, timeoutMillis,
                    forceKill));
            mInvalidated = true;
            Libyasul.exit(mID, timeoutMillis, forceKill);
        }
    }

    /** Provides a user-friendly API to common Shell operations, including creating and removing
     * files and directories, managing permissions, and so on.
     *
     * @return A user-friendly Shell.
     */
    public YslShell getShell() {
        if (mShell == null) {
            mShell = new YslShell(mAppCtx, this);
        }
        return mShell;
    }

    private void invalidateOnEpipe() throws YslEpipeExcetion {
        Log.w(TAG, String.format("%d: invalidate on EPIPE", mPid));
        exit(0L, false);
        throw new YslEpipeExcetion();
    }

    @Override
    public String toString() {
        return String.format("[%s: PID: %d , address: 0x%x, stdout: <%s>, stderr: <%s>]",
                mInvalidated ? "EINVAL" : "valid",
                mPid,
                mID,
                mStdout,
                mStderr);
    }

    private static final String TAG = "YASUL";
}
