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

import java.io.Serializable;

/** Represents a command string execution result.
 *
 */
public class YslParcel implements Serializable {

    final int mExitCode;
    final String mLastTty;

    YslParcel(int exitCode, String lastTty) {
        mExitCode = exitCode;
        mLastTty = lastTty;
    }

    /** Answers the command exit code.
     *
     * @return An integer value, usually <code>0</code> indicates success.
     */
    public int exitCode() {
        return mExitCode;
    }

    /** Answers the last line produced by the shell to its <code>STDOUT</code> as a consequence
     * of the command execution.
     * <p>This is the <i>literal</i> result of the command string, that may be given some
     * semantic: for eg. <code>stat -c %s ysl_utping</code> will answer <code>42</code>,
     * which is interpreted as the file size in bytes.
     * </p>
     * <p>One should not confuse with the static
     * {@link YslSession#getLastTty() YslSession.getLastTty()} API call that answers the
     * <i>last known TTY line</i>.
     * </p>
     * <p>Actual content also depends upon the {@link YslSession#SF_ZTTY} and
     * {@link YslSession#SF_TAIL} session's control flags.
     * </p>
     *
     * @return The last line produced by the shell.
     */
    public String lastTty() {
        return mLastTty;
    }

    @Override
    public String toString() {
        return String.format("[exit code: %d , LTTY: %s]", mExitCode, mLastTty);
    }
}
