/*
 * yasul: Yet another Android SU library. 
 *
 * t0kt0ckus
 * (C) 2014,2015
 * 
 * License LGPLv2, GPLv3
 * 
 */
package org.openmarl.yasul.test;

public class YslTestException extends Exception {

    YslTestException() {
        super();
    }

    YslTestException(String detailMessage) {
        super(detailMessage);
    }

    YslTestException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    YslTestException(Throwable throwable) {
        super(throwable);
    }
}
