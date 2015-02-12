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

/** Indicates that a Shell session has been <i>invalidated</i>.
 * This may result from a <i>broken pipe</i> or a call to
 * {@link org.openmarl.yasul.YslSession#exit(long, boolean) exit()}.
 */
public class YslEpipeException extends Exception {

    YslEpipeException() {
        super();
    }

    YslEpipeException(String detailMessage) {
        super(detailMessage);
    }

    YslEpipeException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    YslEpipeException(Throwable throwable) {
        super(throwable);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
