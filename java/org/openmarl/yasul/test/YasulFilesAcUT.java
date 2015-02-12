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
import org.openmarl.yasul.YslEpipeException;
import org.openmarl.yasul.YslSession;
import org.openmarl.yasul.YslShell;

public class YasulFilesAcUT extends AbstractYasulUT {

    private static final String SAFE_PLACE = "ut-filesac";

    private static final String OTHER_NAME = "shell";
    private static final int OTHER_UID = 2000;
    private static final int OTHER_GID = 2000;
    private static final String FOO_FILE = "foo";

    public YasulFilesAcUT(YslContext ctx) {
        super(ctx, SAFE_PLACE, YslSession.SF_DEFAULTS, null);
    }

    @Override
    public void ut(YslSession session) throws YslTestException, YslEpipeException {
        YslShell shell = session.getShell();
        shell.rm("*"); // we are at safe place, re-init before test

        if (shell.stat(FOO_FILE))
            throw new YslTestException("FOO_FILE should not exist !");
        if (! (shell.touch(FOO_FILE, 0666) && shell.stat(FOO_FILE)) )
            throw new YslTestException("FOO_FILE should now exist !");
        if (shell.stat_a(FOO_FILE) != 0666)
            throw new YslTestException("FOO_FILE access rights should be 0666 !");

        ;
        if (! (shell.chmod(FOO_FILE, 0660) && (shell.stat_a(FOO_FILE) == 0660)) )
            throw new YslTestException("FOO_FILE access rights should be 0660 !");

        if (shell.stat_u(FOO_FILE) != 0)
            throw new YslTestException("FOO_FILE should be uid 0 !");
        if (shell.stat_g(FOO_FILE) != 0)
            throw new YslTestException("FOO_FILE should be gid 0 !");

        if (! (shell.chown(FOO_FILE, OTHER_NAME)
                && (shell.stat_u(FOO_FILE) == OTHER_UID)
                /*&& OTHER_NAME.equals(shell.stat_U(FOO_FILE))*/ ))
            throw new YslTestException("FOO_FILE should now be uid shell !");

        if (! (shell.chgrp(FOO_FILE, OTHER_GID)
                && (shell.stat_g(FOO_FILE) == OTHER_GID)
                /* && OTHER_NAME.equals(shell.stat_G(FOO_FILE)) */ ))
            throw new YslTestException("FOO_FILE should now be gid shell !");

    }

    private static final String TAG = "YASUL_UT_FSAC";
}
