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

/** Represents a client that should be signaled of sessions initialization status, and
 * asynchronous commands execution results.
 *
 */
public interface YslObserver {

    /** Signals that a session's native initialization has terminated.
     *
     * @param session An initialized Shell session, or <code>null</code> if any error occurred.
     */
    public void onSessionFactoryEvent(YslSession session);

    /** Signals that a command string execution has terminated. This occurs on application
     * main/UI thread.
     *
     * @param shellResponse The Shell response, or <code>null</code> if the session has been
     *                      invalidated.
     */
    public void onAsynCommandEvent(YslParcel shellResponse);
}
