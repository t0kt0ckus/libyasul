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

/** Defines a minimal Shell interface that wraps most common commands within a simple API.
 * <p>All commands are executed in the context of a parent <code>root</code> Shell session,
 * and are implemented as blocking calls.
 * </p>
 *
 * <p>Such a shell is available through {@link YslSession#getShell()}, and include calls to:
 * <ul>
 *     <li>Navigate the filesystem: {@link #pwd()}, {@link #cd(String) cd()}
 *     </li>
 *     <li>Access files and directories properties: {@link #stat(String) stat()},
 *     {@link #stat_s(String) stat_s()}, {@link #stat_a(String) stat_a()},
 *     {@link #stat_g(String) stat_g()}, {@link #stat_G(String) stat_G()},
 *     {@link #stat_u(String) stat_u()}, {@link #stat_U(String) stat_U()}
 *     </li>
 *     <li>Manage files and directories permissions: {@link #chmod(String, int) chmod()}
 *     {@link #chown(String, String) chown()}, {@link #chgrp(String, int) chgrp()}
 *     </li>
 *     <li>Create and remove files and directories: {@link #touch(String, int) touch()},
 *     {@link #mkdir(String, int) mkdir()}, {@link #cp(String, String, int) cp()},
 *     {@link #cpd(String, String, int) cpd()}, {@link #rm(String) rm()},
 *     {@link #rmd(String) rmd()}, {@link #mv(String, String) mv()}
 *     </li>
 *     <li>Export assets: {@link #cpa(String, String, int) cpa()}
 *     </li>
 *     <li>Signal processes: {@link #kill(int, int) kill()}, {@link #pkill(String, int) pkill()}
 *     </li>
 *     <li>Manage environment variables: {@link #getenv(String) getenv()},
 *     {@link #setenv(String, String, boolean) setenv()}
 *     </li>
 *     <li>Access user and system information: {@link #id()}, {@link #uname()}, {@link #uname_m()},
 *     {@link #uname_r()}
 *     </li>
 *     <li>Manage SE Linux context: {@link #getenforce()}, {@link #setenforce(boolean) setenforce()},
 *     {@link #sel_current()}
 *     </li>
 * </ul>
 * </p>
 *
 */
public class YslShell {

    private final Context mAppCtx;
    private final YslSession mYslSession;

    private int mSelCurrentStackSize;

    YslShell(Context appCtx, YslSession session) {
        mAppCtx = appCtx;
        mYslSession = session;
        mSelCurrentStackSize = 0;
    }

    /** POSIX <code>SIGHUP</code> signal (<code>1</code>).
     */
    public static final int SIG_HUP = 1;

    /** POSIX <code>SIGINT</code> signal (<code>2</code>).
     */
    public static final int SIG_INT = 2;

    /** POSIX <code>SIGQUIT</code> signal (<code>3</code>).
     */
    public static final int SIG_QUIT = 3;

    /** POSIX <code>SIGKILL</code> signal (<code>9</code>).
     */
    public static final int SIG_KILL = 9;

    /** POSIX <code>SIGUSR1</code> signal (<code>10</code>).
     */
    public static final int SIG_USR1 = 10;

    /** POSIX <code>SIGUSR2</code> signal (<code>12</code>).
     */
    public static final int SIG_USR2 = 12;

    /** POSIX <code>SIGALARM</code> signal (<code>14</code>).
     */
    public static final int SIG_ALARM = 14;

    /** POSIX <code>SIGTERM</code> signal (<code>15</code>).
     */
    public static final int SIG_TERM = 15;

    /** POSIX <code>SIGCONT</code> signal (<code>18</code>).
     */
    public static final int SIG_CONT = 18;

    /** POSIX <code>SIGSTOP</code> signal (<code>19</code>).
     */
    public static final int SIG_STOP = 19;

    /** Do no set a created file or directory's access rights.
     */
    public static final int DEFAULT_ACCESS_RIGHTS = 0x00;


    /** Access to the parent <code>root</code> Shell session.
     *
     * @return A Yasul session.
     */
    public YslSession getSession() {
        return mYslSession;
    }

    /** Access current working directory.
     * <p>Maps to: <code>pwd</code>.
     * </p>
     *
     * @return The current working directory.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public String pwd() throws YslEpipeException {
        YslParcel parcel = mYslSession.exec("pwd");
        return parcel.mLastTty;
    }

    /** Changes current working directory.
     * <p>Maps to: <code>cd</code>.
     * </p>
     *
     * @param path A directory path.
     *
     * @return <code>false</code> when failed to changed current working directory.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public boolean cd(String path) throws YslEpipeException {
        YslParcel parcel = mYslSession.exec(String.format("cd %s", path));
        return (parcel.mExitCode == 0);
    }

    /** Test for a file or directory existence.
     * <p>Maps to: <code>stat -c %n</code>.
     * </p>
     *
      * @param path A file or directory path.
     *
     * @return <code>false</code> when the file or directory does not exist.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public boolean stat(String path) throws YslEpipeException {
        YslParcel parcel = mYslSession.exec(String.format( "stat -c %%n %s", path));
        return (parcel.mExitCode == 0);
    }

    /** Access a file or directory's owner.
     * <p>Maps to: <code>stat -c %U</code>.
     * </p>
     *
     * @param path A file or directory path.
     *
     * @return The owner name, or <code>null</code> when the file or directory does not exist.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public String stat_U(String path) throws YslEpipeException {
        YslParcel parcel = mYslSession.exec(String.format( "stat -c %%U %s", path));
        if (parcel.mExitCode == 0)
            return parcel.mLastTty;
        else
            return null;
    }

    /** Access a file or directory's owner UID.
     * <p>Maps to: <code>stat -c %u</code>.
     * </p>
     *
     * @param path A file or directory path.
     *
     * @return The owner UID, or a negative value when the file or directory does not exist.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public int stat_u(String path) throws YslEpipeException {
        YslParcel parcel = mYslSession.exec(String.format( "stat -c %%u %s", path));
        int uid = -1;
        try {
            uid = Integer.valueOf(parcel.mLastTty);
        }
        catch (NumberFormatException e) {
            Log.w(TAG, e.toString());
        }
        return uid;
    }

    /** Access a file or directory's group.
     * <p>Maps to: <code>stat -c %G</code>.
     * </p>
     *
     * @param path A file or directory path.
     *
     * @return The group name, or <code>null</code> when the file or directory does not exist.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public String stat_G(String path) throws YslEpipeException {
        YslParcel parcel = mYslSession.exec(String.format( "stat -c %%G %s", path));
        if (parcel.mExitCode == 0)
            return parcel.mLastTty;
        else
            return null;
    }

    /** Access a file or directory's group ID.
     * <p>Maps to: <code>stat -c %g</code>.
     * </p>
     *
     * @param path A file or directory path.
     *
     * @return The GID, or a negative value when the file or directory does not exist.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public int stat_g(String path) throws YslEpipeException {
        YslParcel parcel = mYslSession.exec(String.format( "stat -c %%g %s", path));
        int gid = -1;
        try {
            gid = Integer.valueOf(parcel.mLastTty);
        }
        catch (NumberFormatException e) {
            Log.w(TAG, e.toString());
        }
        return gid;
    }

    /** Access a file's size in bytes.
     * <p>Maps to: <code>stat -c %s</code>.
     * </p>
     *
     * @param path A file or directory path.
     *
     * @return The file size in bytes, or a negative value when the file does not exist.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public long stat_s(String path) throws YslEpipeException {
        YslParcel parcel = mYslSession.exec(String.format( "stat -c %%s %s", path));
        int sz = -1;
        try {
            sz = Integer.valueOf(parcel.mLastTty);
        }
        catch (NumberFormatException e) {
            Log.w(TAG, e.toString());
        }
        return sz;
    }

    /** Access a file or directory's access rights.
     * <p>Maps to: <code>stat -c %a</code>.
     * </p>
     *
     * @param path A file or directory path.
     *
     * @return The access rights, or a negative value when the file or directory does not exist.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public int stat_a(String path) throws YslEpipeException {
        YslParcel parcel = mYslSession.exec(String.format( "stat -c %%a %s", path));
        int mode = -1;
        try {
            mode = Integer.parseInt(parcel.mLastTty, 8);
        }
        catch (NumberFormatException e) {
            Log.w(TAG, e.toString());
        }
        return mode;
    }

    /** Changes a file or directory's owner.
     * <p>Maps to: <code>chown</code>.
     * </p>
     *
     * @param path A file or directory path.
     * @param user A user name or UID.
     *
     * @return <code>false</code> when failed to set file or directory owner.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public boolean chown(String path, String user) throws YslEpipeException {
        int uid = -1;
        try {
            uid = Integer.parseInt(user);
        }
        catch (NumberFormatException e) {
            Log.w(TAG, e.toString());
        }
        String cmdstr = (uid > 0) ? String.format("chown %d %s", uid, path)
                : String.format( "chown %s %s", user, path);
        YslParcel parcel = mYslSession.exec(cmdstr);
        return (parcel.mExitCode == 0);
    }

    /** Changes a file or directory's group.
     * <p>Maps to: <code>chgrp</code>.
     * </p>
     *
     * @param path A file or directory path.
     * @param group A group name or GID.
     *
     * @return <code>false</code> when failed to set file or directory group.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public boolean chgrp(String path, int group) throws YslEpipeException {
        YslParcel parcel = mYslSession.exec(String.format("chgrp %d %s", group, path));
        return (parcel.mExitCode == 0);
    }

    /** Changes a file or directory's access rights.
     * <p>Maps to: <code>chmod</code>.
     * </p>
     *
     * @param path A file or directory path.
     * @param mode Access rights, typically in octal form, for eg. <code>0666</code>.
     *
     * @return <code>false</code> when failed to set file or directory access rights.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public boolean chmod(String path, int mode) throws YslEpipeException {
        YslParcel parcel = mYslSession.exec(String.format( "chmod %#04o %s", mode, path));
        return (parcel.mExitCode == 0);
    }

    /** Creates a file, or updates a file or directory's <i>last modified time</i> if it already
     * exists.
     * <p>Maps to: <code>touch</code> then <code>chmod</code>.
     * </p>
     *
     * @param path A file or directory path.
     * @param mode Access rights of created file or existing file or directory,
     *             typically in octal form, for eg. <code>0666</code>.
     *
     * @return <code>false</code> when failed to create the file, or to set its access rights.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public boolean touch(String path, int mode) throws YslEpipeException {
        YslParcel parcel = mYslSession.exec(String.format("touch %s", path));
        if (parcel.mExitCode == 0)
            return chmod(path, mode);
        else
            return false;
    }

    /** Creates a directory, including its parents if needed.
     * <p>Maps to: <code>mkdir -p</code> then <code>chmod</code>.
     * </p>
     *
     * @param path A directory path.
     * @param mode Access rights, typically in octal form, for eg. <code>0666</code>.
     *
     * @return <code>false</code> when failed to create the directory or one of its parent,
     * or to set its access rights.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public boolean mkdir(String path, int mode) throws YslEpipeException {
        YslParcel parcel = mYslSession.exec(String.format( "mkdir -p %s", path));
        if (parcel.mExitCode == 0)
            return chmod(path, mode);
        else
            return false;
    }

    /** Copies a file.
     * <p>Maps to: <code>cp</code> then <code>chmod</code>.
     * </p>
     *
     * @param srcPath The source path.
     * @param destPath The destination path.
     * @param mode Access rights of destination file, typically in octal form, for eg. <code>0666</code>.
     *
     * @return <code>false</code> when failed to copy the file or to set its access rights.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public boolean cp(String srcPath, String destPath, int mode) throws YslEpipeException {
        YslParcel parcel = mYslSession.exec(String.format( "cp %s %s", srcPath, destPath));
        if (parcel.mExitCode == 0)
            return chmod(destPath, mode);
        else
            return false;
    }

    /** Copies a directory.
     * <p>Maps to: <code>cp -R</code> then <code>chmod</code>.
     * </p>
     *
     * @param srcPath The source path.
     * @param destPath The destination path.
     * @param mode Access rights of destination directory, typically in octal form,
     *             for eg. <code>0666</code>.
     *
     * @return <code>false</code> when failed to copy the directory or to set its access rights.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public boolean cpd(String srcPath, String destPath, int mode) throws YslEpipeException {
        YslParcel parcel = mYslSession.exec(String.format( "cp -R %s %s", srcPath, destPath));
        if (parcel.mExitCode == 0)
            return chmod(destPath, mode);
        else
            return false;
    }

    /** Removes a file.
     * <p>Maps to: <code>rm -f</code>.
     * </p>
     *
     * @param path The file path.
     *
     * @return <code>false</code> when failed to remove file.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public boolean rm(String path) throws YslEpipeException {
        YslParcel parcel = mYslSession.exec(String.format("rm -f %s", path));
        return (parcel.mExitCode == 0);
    }

    /** Removes a directory.
     * <p>Maps to: <code>rm -R -f</code>.
     * </p>
     *
     * @param path The directory path.
     *
     * @return <code>false</code> when failed to remove directory.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public boolean rmd(String path) throws YslEpipeException {
        YslParcel parcel = mYslSession.exec(String.format( "rm -R -f %s", path));
        return (parcel.mExitCode == 0);
    }

    /** Moves a file or directory.
     * <p>Maps to: <code>mv</code>.
     * </p>
     *
     * @param srcPath The source path.
     * @param destPath The destination path.
     *
     * @return <code>false</code> when failed to move file or directory.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public boolean mv(String srcPath, String destPath) throws YslEpipeException {
        YslParcel parcel = mYslSession.exec(String.format("mv %s %s", srcPath, destPath));
        return (parcel.mExitCode == 0);
    }

    /** Exports an Android application asset to device filesystem.
     * <p>A file <code>asset.xxx</code> in the application <code>res/raw</code> directory,
     * defines an asset with name <code>asset</code>.
     * </p>
     *
     * @param asset The asset name.
     * @param destPath The destination path.
     * @param mode The destination file access rights.
     *
     * @return <code>false</code> when failed to export asset or to set its access rights.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public boolean cpa(String asset, String destPath, int mode)
            throws YslEpipeException {
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
                if (chmod(destPath, mode))
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

    /** Access a Shell environment variable.
     *
     * @param name The variable name.
     *
     * @return The variable value, or <code>null</code> if the variable is undefined.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public String getenv(String name) throws YslEpipeException {
        YslParcel parcel = mYslSession.exec(String.format( "echo \"$%s\"", name));
        return ( (parcel.mLastTty != null) && (parcel.mLastTty.length()>0) ) ? parcel.mLastTty : null;
    }

    /** Sets a Shell environment variable.
     *
     * @param name The variable name.
     * @param value The variable value (it will be escaped as <code>\"value\"</code>/
     * @param export Determines whether the session should export the new variable value to Shell
     *               sub-processes.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public void setenv(String name, String value, boolean export)
            throws YslEpipeException {
        YslParcel parcel = mYslSession.exec(String.format( "%s %s=\"%s\"",
                export ? "export" : "",
                name,
                value));
    }

    /** Adds a directory to the session's environment <code>PATH</code> variable.
     * <p>The Shell should then resolve ant executable file within this directory as new <i>command</i>.
     * </p>
     *
     * @param dirpath An directory's absolute path.
     * @param export Determines whether the session should export the new variable value to Shell
     *               sub-processes.
     *
     * @return The new <code>PATH</code> value.
     */
    public String envAddPath(String dirpath, boolean export) throws YslEpipeException {
        final String oldPath =getenv(ENVV_PATH);
        final String envPath = ((oldPath == null) || (oldPath.length() == 0)) ? dirpath
                : String.format("%s:%s", oldPath, dirpath);
        final String cmdstr = String.format("%s %s=%s", export ? "export" : "",
                ENVV_PATH,
                envPath);

        mYslSession.exec(cmdstr);
        return getenv(ENVV_PATH);
    }
    private static final String ENVV_PATH="PATH";


    /** Adds a directory to the session's environment <code>LD_LIBRARY_PATH</code> variable.
     * <p>The Shell should then resolve any <code>.so</code> file within this directory as a
     * loadable <i>shared library</i>.
     * </p>
     *
     * @param dirpath An directory's absolute path.
     * @param export Determines whether the session should export the new variable value to Shell
     *               sub-processes.
     *
     * @return The new <code>LD_LIBRARY_PATH</code> value.
     */
    public String envAddLdPath(String dirpath, boolean export) throws YslEpipeException {
        final String oldPath =getenv(ENVV_LDPATH);
        final String envLdPath = ((oldPath == null) || (oldPath.length() == 0)) ? dirpath
                : String.format("%s:%s", oldPath, dirpath);
        final String cmdstr = String.format("%s %s=%s", export ? "export" : "",
                ENVV_LDPATH,
                envLdPath);

        mYslSession.exec(cmdstr);
        return getenv(ENVV_LDPATH);
    }
    private static final String ENVV_LDPATH="LD_LIBRARY_PATH";

    /** Signals a process by PID.
     * <p>Maps to: <code>kill -signum</code>.
     * </p>
     *
     * @param pid The destination process PID.
     * @param signum The signal to send. See <code>YslShell.SIG_XXXX</code> constants for some
     *               of defined signals.
     *
     * @return <code>false</code> when failed to signal the process.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public boolean kill(int pid, int signum) throws YslEpipeException {
        YslParcel parcel = mYslSession.exec(String.format("kill -%d %s", signum, pid));
        return (parcel.mExitCode == 0);
    }

    /** Signals a process by name.
     * <p>Maps to: <code>pkill -signum</code>.
     * </p>
     *
     * @param procname The destination process name REGEXP.
     * @param signum The signal to send. See <code>YslShell.SIG_XXXX</code> constants for some
     *               of defined signals.
     *
     * @return <code>false</code> when failed to signal the process.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public boolean pkill(String procname, int signum) throws YslEpipeException {
        YslParcel parcel = mYslSession.exec(String.format("pkill -%d %s", signum, procname));
        return (parcel.mExitCode == 0);
    }

    /** Answers user, groups and context information.
     * <p>Maps to: <code>id</code>.
     * </p>
     *
     * @return The <code>id</code> string,
     * for eg. <code>uid=0(root) gid=0(root) context=u:r:init:s0</code>, or <code>null</code> when
     * not available.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public String id() throws YslEpipeException {
        YslParcel parcel = mYslSession.exec("id");
        if (parcel.mExitCode == 0)
            return parcel.mLastTty;
        else
            return null;
    }

    /** Answers system information.
     * <p>Maps to: <code>uname -a</code>.
     * </p>
     *
     * @return The full system information, for eg.
     * <code>
     * Linux localhost 3.4.42-gb89c9dd #1 SMP PREEMPT Fri Jun 20 04:30:42 CDT 2014 armv7l GNU/Linux
     * </code>, or <code>null</code> when not available.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public String uname() throws YslEpipeException {
        YslParcel parcel = mYslSession.exec("uname -a");
        if (parcel.mExitCode == 0)
            return parcel.mLastTty;
        else
            return null;
    }

    /** Answers machine hardware information.
     * <p>Maps to: <code>uname -m</code>.
     * </p>
     *
     * @return The hardware information, for eg. <code>armv7l</code>,
     * or <code>null</code> when not available.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public String uname_m() throws YslEpipeException {
        YslParcel parcel = mYslSession.exec("uname -m");
        if (parcel.mExitCode == 0)
            return parcel.mLastTty;
        else
            return null;
    }

    /** Answers OS release information.
     * <p>Maps to: <code>uname -r</code>.
     * </p>
     *
     * @return The release information, for eg. <code>3.4.42-gb89c9dd3.4.42-gb89c9dd</code>,
     * or <code>null</code> when not available.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public String uname_r() throws YslEpipeException {
        YslParcel parcel = mYslSession.exec("uname -r");
        if (parcel.mExitCode == 0)
            return parcel.mLastTty;
        else
            return null;
    }

    /** Answers whether SE Linux is in <i>Enforcing</i> or <i>Permissive</i> mode on this system.
     * <p>Maps to: <code>echo `cat /sys/fs/selinux/enforce`</code>.
     * </p>
     *
     * @return <code>true</code> when <code>/sys/fs/selinux/enforce</code> evaluates to <code>1</code>,
     * which means the system is in <i>Enforcing</i> mode, <code>false</code> when in
     * <i>Permissive</i> mode or <code>/sys/fs/selinux/enforce</code> is not available.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public boolean getenforce() throws YslEpipeException {
        YslParcel parcel = mYslSession.exec("echo `cat /sys/fs/selinux/enforce`");
        if (parcel.mExitCode == 0) {
            return (parcel.mLastTty.charAt(0) == '1');
        }
        return false;
    }

    /** Sets whether SE Linux should be in <i>Enforcing</i> or <i>Permissive</i> mode on this system.
     * <p>Maps to: <code>setenforce</code>.
     * </p>
     *
     * @param enforce <code>true</code> means <i>Enforcing</i>, <code>false</code> means
     *                <i>Permissive</i>.
     *
     * @return Whether SE Linux is now in <i>Enforcing</i> mode on this system.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public boolean setenforce(boolean enforce) throws YslEpipeException {
        YslParcel parcel = mYslSession.exec(String.format("setenforce %d", enforce ? 1 : 0));
        return getenforce();
    }

    /** Answers the current SE Linux context.
     * <p>Maps to: <code>echo `cat /proc/self/attr/current`</code>.
     * </p>
     *
     * @return The current SE Linux context, or <code>null</code> when
     * <code>/proc/self/attr/current</code> is not available.
     *
     * @throws YslEpipeException when the session has been invalidated.
     */
    public String sel_current() throws YslEpipeException {
        YslParcel parcel = mYslSession.exec("echo `cat /proc/self/attr/current`");
        if (parcel.mExitCode == 0)
            return parcel.mLastTty;
        else
            return null;
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
