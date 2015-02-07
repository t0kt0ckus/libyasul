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

public class YslEpipeExcetion extends Exception {

    public YslEpipeExcetion() {
        super();
    }

    public YslEpipeExcetion(String detailMessage) {
        super(detailMessage);
    }

    public YslEpipeExcetion(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public YslEpipeExcetion(Throwable throwable) {
        super(throwable);
    }
}
