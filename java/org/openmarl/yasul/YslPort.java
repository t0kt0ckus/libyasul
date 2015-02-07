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

public class YslPort implements Serializable {

    public final int pid;
    public final long ID;
    public final String stdout;
    public final String stderr;

    public YslPort(int pid, long ID, String stdout, String stderr) {
        this.pid = pid;
        this.ID = ID;
        this.stdout = stdout;
        this.stderr = stderr;
    }
}
