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

/** Represents a handle to a native session.
 *
 */
public class YslPort implements Serializable {

    /** Shell process PID.
     *
     */
    public final int pid;
    /** Native session's address.
     *
     */
    public final long ID;
    /** <code>stdout</code> echo file.
     *
     */
    public final String stdout;
    /** <code>stderr</code> echo file.
     *
     */
    public final String stderr;

    public YslPort(int pid, long ID, String stdout, String stderr) {
        this.pid = pid;
        this.ID = ID;
        this.stdout = stdout;
        this.stderr = stderr;
    }
}
