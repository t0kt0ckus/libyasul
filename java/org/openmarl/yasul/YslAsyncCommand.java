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

import android.os.AsyncTask;


/** Implements a simple asynchronous command string execution.
 * <p>As this implementation relies upon and Android <code>AsyncTask</code>,
 * it "should ideally be used for short operations (a few seconds at the most)",
 * and client is signaled on the application main/UI thread.
 * </p>
 */
public class YslAsyncCommand extends AsyncTask<Void,Void,YslParcel> {

    private final YslSession mYslSession;
    private final YslObserver mClient;
    private final String mCmdstr;

    /** Creates a new asynchronous command.
     *
     * @param yslSession The session to execute the command with.
     * @param client The client to signal once command execution's completed.
     * @param cmdstr The command string to execute.
     */
    public YslAsyncCommand(YslSession yslSession, YslObserver client, String cmdstr) {
        mYslSession = yslSession;
        mClient = client;
        mCmdstr = cmdstr;
    }

    @Override
    protected YslParcel doInBackground(Void... params) {
        YslParcel parcel = null;
        try {
            parcel = mYslSession.exec(mCmdstr);
        }
        catch (YslEpipeException yslEpipeExcetion) {
            // this EPIPE will be reported when signaling client with a null Shell result
        }
        return parcel;
    }

    @Override
    protected void onPostExecute(YslParcel yslParcel) {
        super.onPostExecute(yslParcel);
        if (mClient != null)
            mClient.onAsyncCommandEvent(yslParcel);
    }

    private static final String TAG = "YASUL";
}
