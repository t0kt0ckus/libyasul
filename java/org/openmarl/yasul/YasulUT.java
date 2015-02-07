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

import android.util.Log;

public class YasulUT {

    private final YslSession mYslSession;

    public YasulUT(YslSession yslSession) {
        mYslSession = yslSession;
    }

    public boolean start() throws YslEpipeExcetion {
        YslShell shell = mYslSession.getShell();
        final String SAFE_PLACE = "/data/data/org.openmarl.yasultest/files/TEST";
        final String TEST_PATH_1 = "test_1";
        final String TEST_PATH_2 = "test_2";
        final String TEST_USER = "shell";
        final int TEST_UID = 2000;
        final int TEST_GID = 2000;
        final String TEST_ENVAR = "YSL_TEST_VAR";
        final String TEST_ENVAL = "Ysl";
        final String TEST_ASSET_NAME = "ysl_test_asset";
        final String TEST_ASSET_DEST = "/data/local/tmp/ysl_test_asset.txt";
        final String TEST_CMD_LINE = "com.android.phone";
        final String TEST_UNDEF_CMD = "this_cmd_should_not_exist";

        YslParcel parcel;
        boolean bResult;
        String txtResult;
        int nRetval;

        // output logpath
        String yslLogpath = YslContext.getInstance().getLogpath();
        if (yslLogpath == null) {
            Log.e(TAG, "SHOULD NOT be null !");
            return false;
        }
        Log.d(TAG , String.format("Libyasul log path: %s", yslLogpath));

        // check stupid command
        //
        parcel = mYslSession.exec(TEST_UNDEF_CMD);
        Log.d(TAG, String.format("#%s: %d", TEST_UNDEF_CMD, parcel.exitCode));
        if (parcel.exitCode == 0) {
            Log.e(TAG, "SHOULD NOT be 0 !");
            return false;
        }

        txtResult = shell.id();
        Log.d(TAG, String.format("#id(): %s", txtResult));
        txtResult = shell.pwd();
        Log.d(TAG, String.format("#pwd(): %s", txtResult));

        // we assume this works ;-)
        shell.mkdir(SAFE_PLACE, 0666);

        // test cd():
        //
        bResult = shell.cd(SAFE_PLACE);
        Log.d(TAG, String.format("#cd(%s): %b", SAFE_PLACE, bResult));
        txtResult = shell.pwd();
        if (! txtResult.equals(SAFE_PLACE)) {
            Log.e(TAG, "cd() failed !");
            return false;
        }
        else
            Log.d(TAG, String.format("we should be in a sandbox now: %s", txtResult));

        // test stat():
        //
        bResult = shell.stat(TEST_PATH_1);
        Log.d(TAG, String.format("#stat(%s): %b", TEST_PATH_1, bResult));
        if (bResult) {
            Log.d(TAG, "stat(): file should not exist !");
            return false;
        }

        // test touch():
        //
        bResult = shell.touch(TEST_PATH_1, 0666);
        Log.d(TAG, String.format("#touch(%s): %b", TEST_PATH_1, bResult));
        bResult = shell.stat(TEST_PATH_1);
        if (! bResult) {
            Log.d(TAG, "touch(): file should exist !");
            return false;
        }

        // test stat_a():
        //
        nRetval = shell.stat_a(TEST_PATH_1);
        Log.d(TAG, String.format("#stat_a(%s): %#04o", TEST_PATH_1, nRetval));
        if (nRetval != 0666) {
            Log.d(TAG, "stat_a(): SHOULD BE 0666");
            return false;
        }

        // test stat_u():
        //
        nRetval = shell.stat_u(TEST_PATH_1);
        Log.d(TAG, String.format("#stat_u(%s): %d", TEST_PATH_1, nRetval));
        if (nRetval != 0) {
            Log.d(TAG, "stat_u(): SHOULD BE root(0)");
            return false;
        }

        // test stat_g():
        //
        nRetval = shell.stat_g(TEST_PATH_1);
        Log.d(TAG, String.format("#stat_g(%s): %d", TEST_PATH_1, nRetval));
        if (nRetval != 0) {
            Log.d(TAG, "stat_g(): SHOULD BE root(0)");
            return false;
        }

        // test chown():
        //
        bResult = shell.chown(TEST_USER, TEST_PATH_1);
        Log.d(TAG, String.format("#chown(%s , %s): %b", TEST_USER, TEST_PATH_1, bResult));
        nRetval = shell.stat_u(TEST_PATH_1);
        if (nRetval != TEST_UID) {
            Log.d(TAG, String.format("chown(): SHOULD BE %s(%d)", TEST_USER, TEST_UID));
            return false;
        }

        // test chgrp():
        //
        bResult = shell.chgrp(TEST_GID, TEST_PATH_1);
        Log.d(TAG, String.format("#chgrp(%d , %s): %b", TEST_GID, TEST_PATH_1, bResult));
        nRetval = shell.stat_g(TEST_PATH_1);
        if (nRetval != TEST_GID) {
            Log.d(TAG, String.format("chgrp(): SHOULD BE %d", TEST_GID));
            return false;
        }

        // test cp():
        //
        bResult = shell.cp(TEST_PATH_1, TEST_PATH_2);
        Log.d(TAG, String.format("#cp(%s , %s): %b", TEST_PATH_1, TEST_PATH_2, bResult));
        bResult = shell.stat(TEST_PATH_2);
        if (! bResult) {
            Log.d(TAG, "stat(): file should exist !");
            return false;
        }

        // test rm():
        //
        bResult = shell.rm(TEST_PATH_1, false);
        Log.d(TAG, String.format("#rm(%s): %b", TEST_PATH_1, bResult));
        bResult = shell.stat(TEST_PATH_1);
        if (bResult) {
            Log.d(TAG, "rm(): file should not exist !");
            return false;
        }
        bResult = shell.rm(TEST_PATH_2, false);
        Log.d(TAG, String.format("#rm(%s): %b", TEST_PATH_2, bResult));
        bResult = shell.stat(TEST_PATH_2);
        if (bResult) {
            Log.d(TAG, "rm(): file should not exist !");
            return false;
        }

        // test mkdir():
        //
        String testDir = String.format("%s/%s", TEST_PATH_1, TEST_PATH_2);
        bResult = shell.mkdir(testDir, 0600);
        Log.d(TAG, String.format("#mkdir(%s): %b", testDir, bResult));
        bResult = shell.stat(testDir);
        if (! bResult) {
            Log.d(TAG, "mkdir(): file should exist !");
            return false;
        }
        // test another value ;-)
        nRetval = shell.stat_a(testDir);
        if (nRetval != 0600) {
            Log.d(TAG, "mkdir(): SHOULD BE 0600");
            return false;
        }

        // test cpd():
        //
        bResult = shell.cpd(testDir, TEST_PATH_2);
        Log.d(TAG, String.format("#cpd(%s , %s): %b", testDir, TEST_PATH_2, bResult));
        bResult = shell.stat(TEST_PATH_2);
        if (! bResult) {
            Log.d(TAG, "cpd(): file should exist !");
            return false;
        }

        // test rmd():
        //
        bResult = shell.rmd(testDir, false);
        Log.d(TAG, String.format("#rmd(%s): %b", testDir, bResult));
        bResult = shell.stat(testDir);
        if (bResult) {
            Log.d(TAG, "#rmd(): file should not exist !");
            return false;
        }
        bResult = shell.rmd(TEST_PATH_2, false);
        Log.d(TAG, String.format("#rmd(%s): %b", TEST_PATH_2, bResult));
        bResult = shell.stat(TEST_PATH_2);
        if (bResult) {
            Log.d(TAG, "#rmd(): file should not exist !");
            return false;
        }

        // test set/getenv():
        //
        txtResult = shell.getenv(TEST_ENVAR);
        Log.d(TAG, String.format("#getenv(%s): <%s>", TEST_ENVAR,
                txtResult != null ? txtResult : "NULL"));
        //
        shell.setenv(TEST_ENVAR, TEST_ENVAL, true);
        txtResult = shell.getenv(TEST_ENVAR);
        if ((txtResult == null) || (! txtResult.equals(TEST_ENVAL))) {
            Log.e(TAG, String.format("set/getenv(): should now be %s, is <%s>", TEST_ENVAL,
                    txtResult));
            return false;
        }

        // test cpa():
        //
        bResult = shell.cpa(TEST_ASSET_NAME, TEST_ASSET_DEST, 0660);
        Log.d(TAG, String.format("#cpa(%s , %s): %b", TEST_ASSET_NAME, TEST_ASSET_DEST, bResult));
        bResult = shell.stat(TEST_ASSET_DEST);
        if (! bResult) {
            Log.d(TAG, "cpa(): file should exist !");
            return false;
        }
        //
        nRetval = shell.stat_a(TEST_ASSET_DEST);
        if (nRetval == 0660)
            Log.d(TAG, String.format("exported asset with propert mode: %#04o", nRetval));
        else {
            Log.d(TAG, "cpa(): SHOULD BE 0660");
            return false;
        }

        // test findPidByCmdline():
        //
        /*
        nRetval = YslContext.getInstance().findPidByCmdline(TEST_CMD_LINE);
        Log.d(TAG, String.format("findPidByCmdline(%s): %d", TEST_CMD_LINE, nRetval));
        if (nRetval < 0) {
            Log.e(TAG, "SHOULD have find this process");
            return false;
        }
        */

        // test cfset/get():
        //
        bResult = mYslSession.getCtlFlag(Ysl.SF_EOUT);
        Log.d(TAG, String.format("cfget(%x): %b", Ysl.SF_EOUT, bResult));
        if (! bResult) {
            Log.e(TAG, "This flag SHOULD be set");
            return false;
        }
        //
        nRetval = mYslSession.setCtlFlag(Ysl.SF_EOUT, false);
        Log.d(TAG, String.format("cfset(SF_EOUT, false): %b", Ysl.SF_EOUT, bResult));
        bResult = mYslSession.getCtlFlag(Ysl.SF_EOUT);
        if (bResult) {
            Log.e(TAG, "This flag SHOULD NOT be set");
            return false;
        }
        else {
            shell.id();
            Log.d(TAG, "check that this id() is missing from stdout log");
            // reset flag now
            nRetval = mYslSession.setCtlFlag(Ysl.SF_EOUT, true);
        }

        // test lastty():
        //
        shell.pwd();
        txtResult = mYslSession.getLastTty();
        if (! SAFE_PLACE.equals(txtResult)) {
            Log.e(TAG, String.format("lasttty(): SHOULD be %s, is <%s> !",
                    SAFE_PLACE, txtResult));
        }

        Log.d(TAG, "======= UT successful =======");
        return true;
    }

    private static final String TAG = "YASUL_TEST";
}
