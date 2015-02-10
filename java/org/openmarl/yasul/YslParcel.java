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

    /** The command execution exit code.
     */
    public final int exitCode;

    /** The last <i>TTY line</i> produced by the Shell process to its <code>stdout</code>,
     * during a command execution.
     * Actually content depends upon the {@link YslSession#SF_ZTTY} control flag.
     */
    public final String lastTty;

    YslParcel(int exitCode, String lastTty) {
        this.exitCode = exitCode;
        this.lastTty = lastTty;
    }

    @Override
    public String toString() {
        return String.format("[exit code: %d , LTTY: %s]", exitCode, lastTty);
    }
}
