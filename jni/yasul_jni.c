/*
 * yasul: Yet another Android SU library. 
 *
 * t0kt0ckus
 * (C) 2014,2015
 * 
 * License LGPLv2, GPLv3
 * 
 */
#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "def.h"
#include "log.h"
#include "ostools.h"
#include "yasul.h"
#include "yasul_jni.h"

static char *yjni_basedir;

static jclass yjni_YslPort;
static jmethodID yjni_YslPort_init;
static jclass yjni_YslParcel;
static jmethodID yjni_YslParcel_init;

static int yjni_init(JNIEnv *env);
static int yjni_fatal;

JNIEXPORT int JNICALL
    Java_org_openmarl_yasul_Libyasul_bootstrap(JNIEnv *env, 
            jobject jInstance,
            jstring jLogdir,
            jboolean jDebug) {

    unsigned char ysldbg = jDebug;

    const char *src = (*env)->GetStringUTFChars(env, jLogdir, 0);
    if (src && (yjni_basedir = malloc(strlen(src) + 1)))
        strcpy(yjni_basedir, src);
    else
        yjni_fatal = ENOMEM;
    (*env)->ReleaseStringUTFChars(env, jLogdir, src);

    yjni_fatal |= ysl_log_init(yjni_basedir, ysldbg);
    yjni_fatal |= yjni_init(env); 
    return yjni_fatal;
}
JNIEXPORT jobject JNICALL
    Java_org_openmarl_yasul_Libyasul_getversion(JNIEnv *env, 
            jobject jInstance) {
    jstring jVersion = (*env)->NewStringUTF(env, YSL_BUILD_VERSION);    
    return jVersion;    
}

JNIEXPORT jobject JNICALL
    Java_org_openmarl_yasul_Libyasul_getlog(JNIEnv *env, 
            jobject jInstance) {
  
    jstring jLogpath = NULL;
    const char *logpath = ysl_log_path();
    if (logpath)
        jLogpath = (*env)->NewStringUTF(env, logpath);
    
    return jLogpath;
}

JNIEXPORT jobject JNICALL
    Java_org_openmarl_yasul_Libyasul_open(JNIEnv *env, 
            jobject jInstance,
            jint jFlags) {
    if (yjni_fatal)
        return NULL;

    jobject jYslport = NULL;
    ysl_session_t *s = yasul_open_session(yjni_basedir, jFlags);

    if (s) {
        jint jPid = s->pid;
        jlong jID = (jlong) (long) s;
        jstring jStdout = (*env)->NewStringUTF(env, s->outepath);
        jstring jStderr = (*env)->NewStringUTF(env, s->errepath);
        jYslport = (*env)->NewObject(env, yjni_YslPort, yjni_YslPort_init,
                jPid, jID, jStdout, jStderr);

        if ((jYslport == NULL) || (*env)->ExceptionOccurred(env)) {
            (*env)->ExceptionDescribe(env);
            (*env)->ExceptionClear(NULL);
        }
    }
    return jYslport;
}

JNIEXPORT jint JNICALL
    Java_org_openmarl_yasul_Libyasul_stat(JNIEnv *jEnv, 
            jobject jInstance,
            jlong jSessionId) {
    long addr = jSessionId;
    ysl_session_t *s = (ysl_session_t *) addr;
    int err = ysl_session_stat(s);
    ysl_log_debugf("JNI: stat(0x%x): %d\n", addr, err);
    return err;
}

JNIEXPORT jint JNICALL
    Java_org_openmarl_yasul_Libyasul_cfset(JNIEnv *env, 
            jobject jInstance,
            jlong jSessionId,
            jint jFlag,
            jboolean jIsSet) {
    long addr = jSessionId;
    ysl_session_t *s = (ysl_session_t *) addr;
    
    int flag = jFlag;
    unsigned char isset = jIsSet;
    int newflags = ysl_session_cfset(s, flag, isset);
    return newflags;
}

JNIEXPORT jboolean JNICALL
    Java_org_openmarl_yasul_Libyasul_cfget(JNIEnv *env, 
            jobject jInstance,
            jlong jSessionId,
            jint jFlag) {
    long addr = jSessionId;
    ysl_session_t *s = (ysl_session_t *) addr;
    
    int flag = jFlag;
    unsigned char isset = ysl_session_cfget(s, flag);
    return isset;
}

JNIEXPORT jobject JNICALL
    Java_org_openmarl_yasul_Libyasul_lasttty(JNIEnv *env, 
            jobject jInstance,
            jlong jSessionId) {
            
    long addr = jSessionId;
    ysl_session_t *s = (ysl_session_t *) addr;
    
    jstring jLasttty = (*env)->NewStringUTF(env, s->ltty);
    return jLasttty;    
}
            
JNIEXPORT jobject JNICALL
    Java_org_openmarl_yasul_Libyasul_exec(JNIEnv *env, 
            jobject jInstance,
            jlong jSessionId,
            jstring jCmdstr) {
    // native parms
    long addr = jSessionId;
    const char *cmdstr = (*env)->GetStringUTFChars(env, jCmdstr, NULL);
    ysl_log_debugf("JNI: exec(0x%x , %s)\n", addr, cmdstr);

    // resolve session
    ysl_session_t *s = (ysl_session_t *) addr;

    // exec command string
    char*  ltty;
    int ecode;
    int err = ysl_session_exec(s, cmdstr, &ecode, &ltty);
    (*env)->ReleaseStringUTFChars(env, jCmdstr, cmdstr);
    if (err)
        return NULL;

    // build response
    jint jEcode = ecode;
    jstring jLtty = (*env)->NewStringUTF(env, ltty);
    jobject jParcel = (*env)->NewObject(env, yjni_YslParcel,
            yjni_YslParcel_init, jEcode, jLtty);
    if ((jParcel == NULL) || (*env)->ExceptionOccurred(env)) {
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(NULL);
    }

    return jParcel;
}
   
JNIEXPORT void JNICALL
    Java_org_openmarl_yasul_Libyasul_exit(JNIEnv *jEnv, 
            jobject jInstance,
            jlong jSessionId,
            jlong jTimeout,
            jboolean jForceKill) {
    // native parms
    long addr = jSessionId;
    ysl_log_debugf("JNI: exit(0x%x , %d, %d)\n",
     addr, jTimeout, jForceKill);

    // resolve session (last time it will not crash !)
    ysl_session_t *s = (ysl_session_t *) addr;
    ysl_session_exit(s, 3, 0);
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {
}


int yjni_init(JNIEnv *env) {
    ysl_log_debugf("initilizing JNI requirements ...\n");

    // YslPort class
    jclass jLocal = (*env)->FindClass(env, YSL_J_YslPort);
    if ((jLocal == NULL) || (*env)->ExceptionOccurred(env)) {
        ysl_log_printf("Failed to resolve JNI symbol: %s !\n",
                YSL_J_YslPort);
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
        return -1;
    }
    else {
        yjni_YslPort = (*env)->NewGlobalRef(env, jLocal);
        (*env)->DeleteLocalRef(env, jLocal);
    }

    // YslPort constructor
    yjni_YslPort_init = (*env)->GetMethodID(env, yjni_YslPort, "<init>",
            YSL_J_YslPort_init_s);
    if ((yjni_YslPort_init == NULL) || (*env)->ExceptionOccurred(env)) {
        ysl_log_printf("Failed to resolve JNI symbol: %s#%s(%s) !\n",
                YSL_J_YslPort,
                "<init>",
                YSL_J_YslPort_init_s);
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
        return -1;
    }

    // YslParcel class
    jLocal = (*env)->FindClass(env, YSL_J_YslParcel);
    if ((jLocal == NULL) || (*env)->ExceptionOccurred(env)) {
        ysl_log_printf("Failed to resolve JNI symbol: %s !\n",
                YSL_J_YslParcel);
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
        return -1;
    }
    else {
        yjni_YslParcel = (*env)->NewGlobalRef(env, jLocal);
        (*env)->DeleteLocalRef(env, jLocal);
    }

    // YslParcel constructor
    yjni_YslParcel_init = (*env)->GetMethodID(env, yjni_YslParcel, "<init>",
            YSL_J_YslParcel_init_s);
    if ((yjni_YslParcel_init == NULL) || (*env)->ExceptionOccurred(env)) {
        ysl_log_printf("Failed to resolve JNI symbol: %s#%s(%s) !\n",
                YSL_J_YslParcel,
                "<init>",
                YSL_J_YslParcel_init_s);
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
        return -1;
    }

    ysl_log_debugf("looks good regarding JNI requirements.\n");
    return 0;
}

/*
JNIEXPORT jint JNICALL
    Java_org_openmarl_yasul_Libyasul_findPidByCmdline(JNIEnv *env, 
            jobject jInstance,
            jstring jCmdline) {
    int pid = -1;
    const char *cmdline = (*env)->GetStringUTFChars(env, jCmdline, 0);
    if (cmdline) {
        pid = ysl_os_find_pid(cmdline);
        (*env)->ReleaseStringUTFChars(env, jCmdline, cmdline);
    }
    jint jPid = pid;
    return jPid;
}
*/

