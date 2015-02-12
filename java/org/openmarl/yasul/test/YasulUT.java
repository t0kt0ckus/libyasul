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

import org.openmarl.yasul.YslContext;

/** Basic testing.
 *
 */
public class YasulUT {

    public static void start(YslContext yslCtx) {
        new YasulSelUT(yslCtx).start();
        new YasulCoreUT(yslCtx).start();
        new YasulFilesAcUT(yslCtx).start();
        new YasulFilesUT(yslCtx).start();
    }

}
