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
import org.openmarl.yasul.YslParcel;
import org.openmarl.yasul.YslSession;
import org.openmarl.yasul.YslShell;

import java.io.File;

public class YasulCoreUT extends AbstractYasulUT {

    private static final String SAFE_PLACE = "ut-core";
    private static final String SOME_FILE = "somefile";
    private static final String UNDEF_COMMAND = "thisisnotathing";

    public YasulCoreUT(YslContext ctx) {
        super(ctx, SAFE_PLACE, YslSession.SF_DEFAULTS, null);
    }

    @Override
    public void ut(YslSession session) throws YslTestException, YslEpipeException {
        YslShell shell = session.getShell();
        shell.rm("*"); // we are at safe place, re-init before test

        // starting by a stupid undef command
        YslParcel result = session.exec(UNDEF_COMMAND);
        if (result.exitCode() == 0)
            throw new YslTestException("UNDEF_COMMAND should have an error code !");
        else
            Log.d(TAG, "UNDEF_COMMAND: " + result.toString());

        File outefile = new File(session.getStdout());
        File errefile = new File(session.getStderr());
        if ( (! outefile.exists()) || (! errefile.exists()) )
            throw new YslTestException("Session stdout/stderr missing !");

        boolean SF_EOUT = session.getCtlFlag(YslSession.SF_EOUT);
        boolean SF_EERR = session.getCtlFlag(YslSession.SF_EERR);
        boolean SF_TAIL = session.getCtlFlag(YslSession.SF_TAIL);
        boolean SF_VERB = session.getCtlFlag(YslSession.SF_VERB);
        boolean SF_ZTTY = session.getCtlFlag(YslSession.SF_ZTTY);

        if (! (SF_EOUT && SF_EERR && SF_VERB && (!SF_TAIL) && (!SF_ZTTY)) )
            throw new YslTestException("Ctl flags should be defaults !");
        else
            Log.d(TAG, "Ctl flags set to defaults");

        long sz0 = outefile.length();
        shell.id();
        long sz1 = outefile.length();
        if ((sz1 - sz0) > 0)
            Log.d(TAG, String.format("id() appends %d bytes to stdout", (sz1-sz0)));
        else
            throw new YslTestException("Echo to stdout may be broken !");

        Log.d(TAG, "Disabling echo to stdout");
        session.setCtlFlag(YslSession.SF_EOUT, false);
        sz0 = outefile.length();
        shell.id();
        sz1 = outefile.length();
        if ((sz1 - sz0) > 0)
            throw new YslTestException("This command should not be echoed to stdout !");

        session.setCtlFlag(YslSession.SF_EOUT, true);
        sz0 = outefile.length();
        shell.id();
        sz1 = outefile.length();
        if (session.getCtlFlag(YslSession.SF_EOUT) && ((sz1 - sz0) > 0) )
            Log.d(TAG, "Re-enabled echo to stdout");
        else
            throw new YslTestException("Failed to restore echo to stdout !");

        String pwd = shell.pwd();
        if ( (pwd.length() < 1) || (! pwd.equals(session.getLastTty())) )
            throw new YslTestException("Failed to test getLastTty");

        shell.touch(SOME_FILE, 0666);
        if (! pwd.equals(session.getLastTty()))
            throw new YslTestException("touch() should not have reset getLastTty !");

        Log.d(TAG, "Test Enable SF_ZTTY ...");
        session.setCtlFlag(YslSession.SF_ZTTY, true);
        shell.touch(SOME_FILE, 0666);
        if (pwd.equals(session.getLastTty()))
            throw new YslTestException("touch() should have reset getLastTty !");

        Log.d(TAG, "Test Disable SF_ZTTY ...");
        pwd = shell.pwd();
        session.setCtlFlag(YslSession.SF_ZTTY, false);
        shell.touch(SOME_FILE, 0666);
        if (! pwd.equals(session.getLastTty()))
            throw new YslTestException("touch() should not have reset getLastTty !");

        if (session.isAvailable()
                && shell.kill(session.getPid(), YslShell.SIG_KILL)
                && session.isAvailable())
            throw new YslTestException("kill() should have invalidated the session !");
    }

    private static final String TAG = "YASUL_UT_CORE";
}
