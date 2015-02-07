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

import android.content.Context;
import android.util.Log;

public class YslSession {

    private boolean mInvalidated; // by shell process broken pipe or normal close()
    private final long mID;
    private final int mPid;
    private String mStdout;
    private String mStderr;

    private Context mAppCtx;
    private YslShell mShell;

    public YslSession(Context appCtx, int pid, long id, String stdout, String stderr) {
        mAppCtx = appCtx;
        mPid = pid;
        mID = id;
        mStdout = stdout;
        mStderr = stderr;
        mInvalidated = false;
    }

    public long ID() {
        return mID;
    }

    public int getPid() {
        return mPid;
    }

    public String getStdout() {
        return mStdout;
    }

    public String getStderr() {
        return mStderr;
    }

    public boolean isAvailable() {
        return ((! mInvalidated) && (Libyasul.stat(mID) == 0));
    }

    public int setCtlFlag(int flag, boolean isSet) throws YslEpipeExcetion {
        if (mInvalidated)
            throw new YslEpipeExcetion();
        return Libyasul.cfset(mID, flag, isSet);
    }

    public boolean getCtlFlag(int flag) throws YslEpipeExcetion {
        if (mInvalidated)
            throw new YslEpipeExcetion();
        return Libyasul.cfget(mID, flag);
    }

    public String getLastTty() throws YslEpipeExcetion {
        if (mInvalidated)
            throw new YslEpipeExcetion();
        return Libyasul.lasttty(mID);
    }

    public YslParcel exec(String cmdStr) throws YslEpipeExcetion {
        YslParcel retval = Libyasul.exec(mID, cmdStr);
        if (retval == null)
            invalidateOnEpipe();
        return retval;
    }

    public void exit(long timeoutMillis, boolean forceKill) {
        if (mInvalidated) {

        }
        else {
            mInvalidated = true;
            Libyasul.exit(mID, timeoutMillis, forceKill);
        }
    }

    public YslShell getShell() {
        if (mShell == null) {
            mShell = new YslShell(mAppCtx, this);
        }
        return mShell;
    }

    private void invalidateOnEpipe() throws YslEpipeExcetion {
        Log.w(TAG, String.format("EPIPE: %s", this.toString()));
        exit(0L, false);
        throw new YslEpipeExcetion();
    }

    @Override
    public String toString() {
        return String.format("[%s: PID: %d , address: 0x%x]",
                mInvalidated ? "EINVAL" : "valid",
                mPid, mID);
    }

    private static final String TAG = "YASUL";
}
