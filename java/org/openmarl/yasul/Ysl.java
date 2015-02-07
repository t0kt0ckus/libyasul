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

/** Defines a Yasul session's available control flags.
 * <p>Flags are to be bitwise or-ed. They can be
 * {@link org.openmarl.yasul.YslSession#setCtlFlag(int, boolean) set} between each command
 * {@link org.openmarl.yasul.YslSession#exec(String) execution}, and
 * {@link org.openmarl.yasul.YslSession#getCtlFlag(int) red} at any time.
 * </p>
 * <p>A common setting would be <code>{@link #SF_EOUT} | {@link #SF_EERR} | {@link #SF_VERB}</code>,
 * sometimes clearing <code>{@link #SF_EOUT}</code> and/or <code>{@link #SF_EERR}</code> to
 * temporarily disable logging the shell process output, which could be eventually huge.
 * </p>
 */
public interface Ysl {

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

}
