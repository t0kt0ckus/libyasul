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

public interface YslObserver {

    /**
     * When context null, failure.
     *
     * @param session
     */
    public void onSessionFactoryEvent(YslSession session);
}
