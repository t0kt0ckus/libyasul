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

import android.util.Log;

import org.openmarl.yasul.YslContext;
import org.openmarl.yasul.YslEpipeException;
import org.openmarl.yasul.YslSession;
import org.openmarl.yasul.YslShell;

public class YasulSelUT extends AbstractYasulUT {

    static final String SE_SWITCH_CONTEXT = "u:r:untrusted_app:s0";

    public YasulSelUT(YslContext ctx) {
        super(ctx, null, YslSession.SF_DEFAULTS, SE_SWITCH_CONTEXT);
    }

    @Override
    public void ut(YslSession session) throws YslTestException, YslEpipeException {
        YslShell shell = session.getShell();

        // get current mode
        Log.d(TAG,
                String.format("SEL system mode: %s",
                        shell.getenforce() ? "Enforcing, good." : "Permissive, oops"));

        // try to get permissive
        if (shell.setenforce(false))
            throw new YslTestException("SEL mode should now be Permissive !");

        Log.d(TAG, "SEL mode is now Permissive, restoring ...");
        if (! shell.setenforce(true))
            throw new YslTestException("SEL mode should now be Enforcing !");

    }

    private static final String TAG = "YASUL_UT_SEL";
}
