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

public class YasulFilesUT extends AbstractYasulUT {

    private static final String SAFE_PLACE = "ut-files";

    private static final String FOO_DIR = "FOO";
    private static final String BAR_DIR = "BAR";
    private static final String FOO_FILE = "foo";
    private static final String BAR_FILE = "bar";
    private static final String ASSET_NAME = "ysl_utping";
    private static final int ASSET_SIZE = 44;
    private static final int ASSET_MODE = 0755;
    private static final String TEST_LTTY = "YSL_UT_ACK";

    public YasulFilesUT(YslContext ctx) {
        super(ctx, SAFE_PLACE, YslSession.SF_DEFAULTS, null);
    }

    @Override
    public void ut(YslSession session) throws YslTestException, YslEpipeException {
        YslShell shell = session.getShell();
        shell.rm("*"); // we are at safe place, re-init before test

        final String assetDestDir = String.format("%s/%s", YSL_UT_BASE_DIR, mSafeplace);
        final String assetPath = String.format("%s/%s", assetDestDir, ASSET_NAME);

        // creates FOO_DIR
        if (! (shell.mkdir(FOO_DIR, 0666)
                && shell.stat(FOO_DIR)
                && (shell.stat_a(FOO_DIR) == 0666) ))
            throw new YslTestException("FOO_DIR should exist with access rights 0666 !");

        // FOO_DIR -> BAR_DIR
        if (! (shell.mv(FOO_DIR, BAR_DIR)
                && shell.stat(BAR_DIR)
                && (! shell.stat(FOO_DIR)) ))
            throw new YslTestException("FOO_DIR should have moved to BAR_DIR !");

        // creates BAR_DIR/FOO_FILE
        String path = BAR_DIR + "/" + FOO_FILE;
        if (! (shell.touch(path, 0666) && shell.stat(path)))
            throw new YslTestException("BAR_DIR/FOO_FILE should exist !");

        // cpd BAR_DIR to FOO_DIR
        if (! (shell.cpd(BAR_DIR, FOO_DIR, 0600)
                && shell.stat(FOO_DIR)
                && shell.stat(FOO_DIR+"/"+FOO_FILE)
                && (shell.stat_a(FOO_DIR) == 0600) ))
            throw new YslTestException("cpd() failed somewhere !");

        // rmd FOO_DIR
        if (! (shell.rmd(FOO_DIR) && (! shell.stat(FOO_DIR)) ))
            throw new YslTestException("FOO_DIR should no more exist !");

        // cp BAR_DIR/FOO_FILE to FOO_FILE
        if (! (shell.cp(path, BAR_FILE, 0666)
                && shell.stat(BAR_FILE)
                && (shell.stat_a(BAR_FILE) == 0666) ))
            throw new YslTestException("cp() failed somewhere !");

        // rm BAR_DIR/FOO_FILE
        if (! (shell.rm(path) && (! shell.stat(path)) ))
            throw new YslTestException("BAR_DIR/FOO_FILE should not exist !");

        // rmd BAR_DIR
        if (! (shell.rmd(BAR_DIR) && (! shell.stat(BAR_DIR)) ))
            throw new YslTestException("BAR_DIR should not exist !");

        // rm FOO_FILE
        if (! (shell.rm(FOO_FILE) && (! shell.stat(FOO_FILE)) ))
            throw new YslTestException("FOO_FILE should no more exist !");

        // cpa
        if (! (shell.cpa(ASSET_NAME, assetPath, ASSET_MODE)
                && shell.stat(assetPath)
                && (shell.stat_a(assetPath) == ASSET_MODE)
                && (shell.stat_s(assetPath) == ASSET_SIZE) ))
            throw new YslTestException("cpa() failed somewhere: " + assetPath);

        // test env
        String envPath = shell.envAddPath(assetDestDir,false);
        if (! ((envPath != null) && envPath.contains(assetDestDir)) )
            throw new YslTestException("envAddpath() failed: " + envPath);
        else
            Log.d(TAG, "PATH: " + envPath);

        YslParcel result = session.exec(ASSET_NAME);
        if (! ( (result.exitCode() == 0) && TEST_LTTY.equals(result.lastTty())) )
            throw new YslTestException("ASSET should be executable and within PATH: "
                + result.toString());

    }

    private static final String TAG = "YASULT_UT_FILES";
}
