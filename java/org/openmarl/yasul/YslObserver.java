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

/** Represents a client to which signal sessions initialization status, and
 * asynchronous command and batch execution results.
 *
 */
public interface YslObserver {

    /** Signals that a session's initialization status is available.
     * <p>The implementing client is signaled on the main/UI thread.
     * </p>
     *
     * @param session An initialized shell session, or <code>null</code> if any error occurred.
     */
    public void onSessionFactoryEvent(YslSession session);

    /** Signals that a command string execution result is available.
     * <p>The implementing client is signaled on the main/UI thread.
     * </p>
     *
     * @param shellResponse The shell provided result, or <code>null</code> if the session has been
     *                      invalidated.
     */
    public void onAsyncCommandEvent(YslParcel shellResponse);

    /** Signals that a batch execution result is available.
     * <p>The implementing client is signaled on the main/UI thread.
     * </p>
     *
     * @param batchStatus The batch status, describing the execution result.
     */
    public void onAsyncBatchEvent(YslBatchStatus batchStatus);
}
