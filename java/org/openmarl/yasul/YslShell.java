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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

public class YslShell {

    private final Context mAppCtx;
    private final YslSession mYslSession;

    YslShell(Context appCtx, YslSession session) {
        mAppCtx = appCtx;
        mYslSession = session;
    }

    public String pwd() throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec("pwd");
        return parcel.lastTty;
    }

    public boolean cd(String path) throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec(String.format("cd %s", path));
        return (parcel.exitCode == 0);
    }

    public boolean mkdir(String path, int mode) throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec(String.format( "mkdir -p %s", path));
        if (parcel.exitCode == 0)
            return chmod(mode, path);
        else
            return false;
    }

    public boolean cp(String srcPath, String destPath) throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec(String.format( "cp %s %s", srcPath, destPath));
        return (parcel.exitCode == 0);
    }

    public boolean cpd(String srcPath, String destPath) throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec(String.format( "cp -R %s %s", srcPath, destPath));
        return (parcel.exitCode == 0);
    }

    public boolean cpa(String asset, String destPath, int mode)
            throws YslEpipeExcetion {
        int assetId = getRawAssetId(asset);
        if (assetId < 0)
            return false; // unknown asset

        boolean isExported = false;
        byte buf[] = new byte[256];
        int iBytes = -1;

        // initializing file with mode such as we can write
        if (touch(destPath, 0666)) {
            try {
                InputStream is = mAppCtx.getResources().openRawResource(assetId);
                OutputStream os = new FileOutputStream(new File(destPath));
                int red;
                // copying stream
                while((red = is.read(buf)) > 0) {
                    os.write(buf, 0, red);
                    iBytes += red;
                }
                is.close();
                os.close();
                // set requested mode
                if (chmod(mode, destPath))
                    isExported = true;

                Log.d(TAG,
                        String.format("exported %d bytes of asset <%s> to: %s",
                                iBytes, asset, destPath));
            }
            catch (IOException e) {
                Log.e(TAG, String.format("I/O error while copying asset: %s", e.toString()));
            }
        }
        return isExported;
    }

    public boolean rm(String path, boolean force) throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec(String.format( "rm %s %s", force ? "-f" : "", path));
        return (parcel.exitCode == 0);
    }

    public boolean rmd(String path, boolean force) throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec(String.format( "rm -R %s %s", force ? "-f" : "", path));
        return (parcel.exitCode == 0);
    }

    public boolean mv(String srcPath, String destPath) throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec(String.format( "mv %s %s", srcPath, destPath));
        return (parcel.exitCode == 0);
    }

    public String getenv(String variableName) throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec(String.format( "echo \"$%s\"", variableName));
        return ( (parcel.lastTty != null) && (parcel.lastTty.length()>0) ) ? parcel.lastTty : null;
    }

    public void setenv(String variableName, String variableValue, boolean exportSubproc)
            throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec(String.format( "%s %s=\"%s\"",
                exportSubproc ? "export" : "",
                variableName,
                variableValue));
    }

    public boolean chown(String user, String path) throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec(String.format( "chown %s %s", user, path));
        return (parcel.exitCode == 0);
    }

    public boolean chgrp(int group, String path) throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec(String.format( "chgrp %d %s", group, path));
        return (parcel.exitCode == 0);
    }

    public boolean chmod(int mode, String path) throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec(String.format( "chmod %#04o %s", mode, path));
        return (parcel.exitCode == 0);
    }

    public boolean stat(String path) throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec(String.format( "stat -c %%n %s", path));
        return (parcel.exitCode == 0);
    }

    public int stat_u(String path) throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec(String.format( "stat -c %%u %s", path));
        int uid = -1;
        try {
            uid = Integer.valueOf(parcel.lastTty);
        }
        catch (NumberFormatException e) {
            Log.w(TAG, e.toString());
        }
        return uid;
    }

    public long stat_s(String path) throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec(String.format( "stat -c %%s %s", path));
        int sz = -1;
        try {
            sz = Integer.valueOf(parcel.lastTty);
        }
        catch (NumberFormatException e) {
            Log.w(TAG, e.toString());
        }
        return sz;
    }

    public int stat_g(String path) throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec(String.format( "stat -c %%g %s", path));
        int gid = -1;
        try {
            gid = Integer.valueOf(parcel.lastTty);
        }
        catch (NumberFormatException e) {
            Log.w(TAG, e.toString());
        }
        return gid;
    }

    public int stat_a(String path) throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec(String.format( "stat -c %%a %s", path));
        int mode = -1;
        try {
            mode = Integer.parseInt(parcel.lastTty, 8);
        }
        catch (NumberFormatException e) {
            Log.w(TAG, e.toString());
        }
        return mode;
    }

    public boolean touch(String path, int mode) throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec(String.format("touch %s", path));
        if (parcel.exitCode == 0)
            return chmod(mode, path);
        else
            return false;
    }

    public String id() throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec("id");
        return parcel.lastTty;
    }

    public String uname() throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec("uname -a");
        return parcel.lastTty;
    }

    public String uname_m() throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec("uname -m");
        return parcel.lastTty;
    }

    public String uname_r() throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec("uname -r");
        return parcel.lastTty;
    }

    public String hostname() throws YslEpipeExcetion {
        YslParcel parcel = mYslSession.exec("uname -n");
        return parcel.lastTty;
    }

    private int getRawAssetId(String rawAsset) {
        int assetId = -1;

        try {
            Class R_class = Class.forName( String.format("%s.R",
                    mAppCtx.getApplicationContext().getPackageName()));
            Class R_raw_class = null;
            for (Class clazz : R_class.getDeclaredClasses()) {
                if ("raw".equals(clazz.getSimpleName()))
                    R_raw_class = clazz;
            }
            if (R_raw_class != null) {
                Field R_raw_asset = R_raw_class.getField(rawAsset);
                assetId = R_raw_asset.getInt(null);
            }
        }
        catch (ClassNotFoundException e) {
            Log.e(TAG,
                    String.format("Not sure to run an Android application: %s",
                            e.toString())); // should not happen
        }
        catch (IllegalAccessException e) {
            Log.e(TAG, e.toString()); // should never happen
        }
        catch (NoSuchFieldException e) {
            Log.e(TAG, String.format("Unknown asset: %s", rawAsset));
        }

        return assetId;
    }

    private static final String TAG = "YASUL";
}
