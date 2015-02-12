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
public class YslAsyncBatch extends AsyncTask<Void,Void,YslBatchStatus> {

    private final YslSession mYslSession;
    private final YslObserver mClient;
    private final String[] mBatchCommands;
    private final boolean mStopOnError;

    /** Creates a new asynchronous batch.
     *
     * @param yslSession The session to execute the command with.
     * @param client The client to signal once command execution's completed.
     * @param commandStrings The batch command strings.
     * @param stopOnError Whether to stop batch on error.
     */
    public YslAsyncBatch(YslSession yslSession, YslObserver client, String[] commandStrings,
                         boolean stopOnError) {
        mYslSession = yslSession;
        mClient = client;
        mBatchCommands = commandStrings;
        mStopOnError = stopOnError;
    }

    @Override
    protected YslBatchStatus doInBackground(Void... params) {
        YslBatchStatus batchStatus;
        try {
            batchStatus = mYslSession.batch(mBatchCommands, mStopOnError);
        }
        catch (YslEpipeException yslEpipeExcetion) {
            // batch() only throws EPIPE when session was already invalidated
            batchStatus = new YslBatchStatus(); // -1 , null
        }
        return batchStatus;
    }

    @Override
    protected void onPostExecute(YslBatchStatus yslBatchSatus) {
        super.onPostExecute(yslBatchSatus);
        if (mClient != null)
            mClient.onAsyncBatchEvent(yslBatchSatus);
    }

    private static final String TAG = "YASUL";
}
