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
 * Represents a privileged shell session.
 * <p>The session's behavior is specified through control flags that configure its
 * logging level, whether the shell <code>STDOUT</code> and <code>STDERR</code> streams
 * should be echoed to specific log files, and how and when the <i>last known TTY</i>
 * should be updated. They are to be bitwise or-ed, and {@link #SF_DEFAULTS} provides
 * a reasonable starting value.
 * </p>
 *
 * <p>While valid, a session permits to execute commands and batch, synchronously and asynchronously.
 * </p>
 * <p>When a <i>broken pipe</i> occurs (the shell process died abnormally), or after a solicited
 * {@link #exit(long, boolean) exit()}, the session is marked <i>invalidated</i>, and any attempt
 * to execute a command or batch, or to access control flags, will produce an
 * {@link org.openmarl.yasul.YslEpipeException} error.
 * </p>
 * <p>The session API also provides a <i>user friendly</i> shell that wraps
 * most common shell commands into more trivial calls.
 * </p>
 *
 */
public class YslSession {

    /** Determines whether to echo the shell <code>STDOUT</code> to a log file.
     * This can be useful to temporarily disable logging when a command is expected to produce
     * a huge output.
     *
     * @see #getStdout()
     */
    public static final int SF_EOUT = 0x01;

    /** Determines whether to echo the shell <code>STDERR</code> to a log file.
     *
     * @see #getStderr()
     */
    public static final int SF_EERR = 0x02;

    /** Determines whether to update the <i>last known TTY line</i> after each line red from
     * the shell <code>STDOUT</code>. This permits access from another thread to
     * the last output produced by the shell, before the command execution completes - which can be
     * useful for monitoring purpose.
     * @see YslSession#getLastTty()
     */
    public static final int SF_TAIL = 0x04;

    /** Determines whether a command that does not produce any output should preserve or clear
     * the <i>last known TTY line</i>.
     * <p>When this flag is set, the internal buffer is reset after each command execution,
     * which may help when a session exceptionally produces huge output, and that we want to
     * release memory. Otherwise, keeping this flag unset should increase performance.
     */
    public static final int SF_ZTTY = 0x08;

    /** Determines whether the session's logging should be verbose.
     */
    public static final int SF_VERB = 0x10;

    /** A common session's control flags bitmask.
     * <p>Its value is <code>{@link #SF_EOUT} | {@link #SF_EERR} | {@link #SF_VERB}</code>, where
     * one would sometimes clear <code>{@link #SF_EOUT}</code> and/or <code>{@link #SF_EERR}</code>
     * to temporarily disable logging the shell process output.
     * </p>
     */
    public static final int SF_DEFAULTS = SF_EOUT | SF_EERR | SF_VERB;

    private boolean mInvalidated;
    private final long mID;
    private final int mPid;
    private final String mStdout;
    private final String mStderr;

    private final Context mAppCtx;
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

    /** Access the shell process PID.
     *
     * @return The PID.
     */
    public int getPid() {
        return mPid;
    }

    /** Access the shell process <i>stdout echo file</i>.
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

    /** Access the shell process <i>stderr echo file</i>.
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

    /** Answers whether this session is ready to accept command strings.
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
     * @throws YslEpipeException when the session has been invalidated.
     *
     * @see org.openmarl.yasul.Libyasul#cfset(long, int, boolean)
     */
    public int setCtlFlag(int flag, boolean isSet) throws YslEpipeException {
        if (mInvalidated)
            throw new YslEpipeException();
        return Libyasul.cfset(mID, flag, isSet);
    }

    /** Access a session's control flag.
     *
     * @param flag The flag to get.
     *
     * @return <code>true</code> if the flag is set.
     *
     * @throws YslEpipeException when the session has been invalidated.
     *
     * @see org.openmarl.yasul.Libyasul#cfget(long, int)
     */
    public boolean getCtlFlag(int flag) throws YslEpipeException {
        if (mInvalidated)
            throw new YslEpipeException();
        return Libyasul.cfget(mID, flag);
    }

    /** Access the <i>last known TTY line</i> produced by the shell process to its
     * <code>STDOUT</code>.
     * <p>Actual value depends upon {@link #SF_TAIL} and {@link #SF_ZTTY} control flags.
     * </p>
     *
     * @return A TTY line, or <code>null</code>.
     *
     * @throws YslEpipeException when the session has been invalidated.
     *
     * @see org.openmarl.yasul.Libyasul#lasttty(long)
     */
    public String getLastTty() throws YslEpipeException {
        if (mInvalidated)
            throw new YslEpipeException();
        return Libyasul.lasttty(mID);
    }

    /** Executes a command string.
     * <p>This call blocks until command completion, and should not occur on main/UI thread when
     * expected to take some time.
     * <p>Several command strings queued by a single client thread will be executed in order of
     * submission. Several command strings queued by more than one client thread will be
     * executed atomically, but in undetermined order.
     * </p>
     *
     * @param cmdStr A command string.
     *
     * @return The command string execution result red from the shell.
     *
     * @throws YslEpipeException when the session has been invalidated.
     *
     * @see org.openmarl.yasul.Libyasul#exec(long, String)
     */
    public YslParcel exec(String cmdStr) throws YslEpipeException {
        if (mInvalidated)
            throw new YslEpipeException();
        Log.d(TAG, String.format("%d: #exec(%s)", mPid, cmdStr));

        YslParcel retval = Libyasul.exec(mID, cmdStr);
        if (retval == null)
            invalidateOnEpipe();
        return retval;
    }

    /** Asynchronously executes a command string.
     *
     * <p>The client is signaled through an
     * {@link YslObserver#onAsyncCommandEvent(YslParcel) onAsyncCommandEvent()} event.
     * </p>
     *
     * <p>Several command strings queued by a single client thread will be executed and signaled
     * in order of submission. Several command strings queued by more than one client thread will be
     * executed and signaled atomically, but in undetermined order.
     * </p>
     *
     * @param client The client to signal once command execution's completed.
     * @param cmdstr A command string.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public void exec(YslObserver client, String cmdstr) throws YslEpipeException {
        if (mInvalidated)
            throw new YslEpipeException();
        Log.d(TAG, String.format("%d: #Async_exec(%s)", mPid, cmdstr));
        new YslAsyncCommand(this, client, cmdstr).execute();
    }

    /** Executes a batch.
     *
     * @param batchCommands An ordered list of command strings.
     * @param stopOnError Determines whether to abort the batch when an error occurs, that is
     *                    when a command exit code is not <code>0</code>.
     *
     * @return The batch status.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public YslBatchStatus batch(String[] batchCommands, boolean stopOnError) throws YslEpipeException {
        if (mInvalidated)
            throw new YslEpipeException();

        YslParcel lastParcel = null;
        int lastCmd = -1;

        for (String cmdstr : batchCommands) {
            try {
                lastParcel = exec(cmdstr);
            }
            catch (YslEpipeException e) {
                lastParcel = null;
            }
            lastCmd++;
            if ( (lastParcel == null)
                    || (stopOnError && (lastParcel.mExitCode != 0)) )
                break;
        }

        return new YslBatchStatus(lastCmd, lastParcel);
    }

    /** Asynchronously executes a batch of command strings.
     * <p>The client is signaled through an
     * {@link YslObserver#onAsyncBatchEvent(YslBatchStatus) onAsyncBatchEvent()} event.
     * </p>
     *
     * @param client The client to signal.
     * @param batchCommands An ordered list of command strings.
     * @param stopOnError Determines whether to abort the batch when an error occurs, that is
     *                    when a command exit code is not <code>0</code>.
     */
    public void batch(YslObserver client, String[] batchCommands, boolean stopOnError) {
        new YslAsyncBatch(this, client, batchCommands, stopOnError);
    }

    /** Exits this session, releasing all associated resources.
     * <p>Any further API call will produce an
     * {@link YslEpipeException YslEpipeExcetion} error. This call does nothing
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

    /** Provides a <i>user friendly</i> API to common tasks.
     *
     * @return A bunch of convenient wrappers.
     */
    public YslShell getShell() {
        if (mShell == null) {
            mShell = new YslShell(mAppCtx, this);
        }
        return mShell;
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

    void dumpCtlFalgs() {
        try {
            Log.d(TAG, String.format("SF_EOUT: [%c]", getCtlFlag(SF_EOUT) ? 'X' : '.'));
            Log.d(TAG, String.format("SF_EERR: [%c]", getCtlFlag(SF_EERR) ? 'X' : '.'));
            Log.d(TAG, String.format("SF_TAIL: [%c]", getCtlFlag(SF_TAIL) ? 'X' : '.'));
            Log.d(TAG, String.format("SF_VERB: [%c]", getCtlFlag(SF_VERB) ? 'X' : '.'));
            Log.d(TAG, String.format("SF_ZTTY: [%c]", getCtlFlag(SF_ZTTY) ? 'X' : '.'));
        }
        catch(YslEpipeException e) {
            Log.e(TAG, e.toString()); // silent catch
        }
    }

    private void invalidateOnEpipe() throws YslEpipeException {
        Log.w(TAG, String.format("%d: invalidate on EPIPE", mPid));
        exit(0L, false);
        throw new YslEpipeException();
    }

    private static final String TAG = "YASUL";
}
