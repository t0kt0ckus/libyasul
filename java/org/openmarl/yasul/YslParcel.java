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

public class YslParcel implements Serializable {

    public final int exitCode;
    public final String lastTty;

    public YslParcel(int exitCode, String lastTty) {
        this.exitCode = exitCode;
        this.lastTty = lastTty;
    }

    @Override
    public String toString() {
        return String.format("[exit code: %d , LTTY: %s]", exitCode, lastTty);
    }
}
