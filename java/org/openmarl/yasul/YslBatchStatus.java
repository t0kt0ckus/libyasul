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

/** Represents a batch execution result.
 *
 * <p>This result is either returned from a synchronous call to
 * {@link YslSession#batch(String[], boolean)}, or signaled upon completion of an
 * asynchronous call to {@link YslSession#batch(YslObserver, String[], boolean)}.
 * </p>
 *
 */
public class YslBatchStatus {

    final int mLastCmd;
    final YslParcel mLastParcel;

    YslBatchStatus() {
        mLastCmd = -1;
        mLastParcel = null;
    }

    YslBatchStatus(int lastCmd, YslParcel lastParcel) {
        this.mLastCmd = lastCmd;
        this.mLastParcel = lastParcel;
    }

    /** Answers the last batch command executed by the shell.
     *
     * @return The index of the last executed command, or a negative value when no command at
     * all was executed.
     */
    public int getLastCmd() {
        return mLastCmd;
    }

    /** Answers the batch exit status.
     *
     * @return The shell result of the last executed command, which may be <code>null</code> if
     * this command produced a <i>broken pipe</i>.
     */
    public YslParcel getLastParcel() {
        return mLastParcel;
    }

    /** Answers whether the batch was successful.
     *
     * @return <code>true</code> when all commands were executed and last exit code is zero.
     */
    public boolean succeeded() {
        return (mLastCmd > 0)
                && (mLastParcel != null)
                && (mLastParcel.mExitCode == 0);
    }

    @Override
    public String toString() {
        return String.format("[last exec: %d %s]", mLastCmd, mLastParcel.toString());
    }
}
