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

import org.openmarl.yasul.YslBatchStatus;
import org.openmarl.yasul.YslEpipeException;
import org.openmarl.yasul.YslParcel;
import org.openmarl.yasul.YslContext;
import org.openmarl.yasul.YslObserver;
import org.openmarl.yasul.YslSession;
import org.openmarl.yasul.YslShell;

public abstract class AbstractYasulUT implements YslObserver {

    static final String YSL_UT_BASE_DIR = "/data/local/tmp/yasul";

    protected final YslContext mYslContext;
    protected final String mSafeplace;
    protected final int mFlags;
    protected final String mSelCtx;

    public AbstractYasulUT(YslContext ctx, String safedir, int flags, String secontext) {
        mYslContext = ctx;
        mSafeplace = safedir;
        mFlags =flags;
        mSelCtx = secontext;
    }

    public void start() {
        mYslContext.openSession(this, mFlags, mSelCtx);
    }

    public abstract void ut(YslSession session) throws YslTestException, YslEpipeException;

    @Override
    public void onSessionFactoryEvent(YslSession session) {
        if (session != null) {
            try {
                Log.i(TAG, String.format("%s: Starting test ...", getClass().getSimpleName()));
                cdToSafePlace(session.getShell());

                long t0 = System.currentTimeMillis();
                ut(session);
                long t1 = System.currentTimeMillis();

                Log.i(TAG, String.format("%s: Test succeeded (%d ms)", getClass().getSimpleName(),
                        (t1 - t0)));
            } catch (YslTestException e) {
                Log.e(TAG, String.format("%s: %s", getClass().getSimpleName(), e.toString()));
                Log.e(TAG, String.format("%s: Test failed !", getClass().getSimpleName()));
            } catch (YslEpipeException e) {
                Log.e(TAG, String.format("%s: Test failed (EPIPE) !", getClass().getSimpleName()));
            }
            session.exit(1L, false);
        }
        else
            Log.e(TAG, "Failed to open session !");
    }

    private void cdToSafePlace(YslShell shell) throws YslTestException, YslEpipeException {
        shell.mkdir(YSL_UT_BASE_DIR, 0777);
        String safedir = (mSafeplace != null) ? String.format("%s/%s", YSL_UT_BASE_DIR, mSafeplace)
                : YSL_UT_BASE_DIR;

        if (shell.mkdir(safedir, 0777)
            && shell.cd(safedir)
            && safedir.equals(shell.pwd()) )
            Log.d(TAG, String.format("%s: at safe place <%s>", getClass().getSimpleName(),
                safedir));
        else
            throw new YslTestException("Failed to init safe place: " + safedir);
    }

    @Override
    public void onAsyncCommandEvent(YslParcel shellResponse) {
    }
    @Override
    public void onAsyncBatchEvent(YslBatchStatus batchStatus) {
    }

    private static final String TAG = "YASUL_TEST";
}
