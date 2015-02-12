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
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <sys/types.h>
#include <unistd.h>

#include "def.h"
#include "log.h" 
#include "yasul.h"

#ifdef YSL_BUILD_DEBUG
static char *YSL_SU_CANDIDATES[] = {"/bin/bash", NULL};
static char * const YSL_SU_ENV[] = { NULL };
#else
static const char *YSL_SU_CANDIDATES[] = {"/sbin/su",
    "/system/sbin/su",
    "/system/bin/su",
    "/system/xbin/su",
    NULL
};
static char *const YSL_SU_ENV[] = {
            "PATH=/sbin:/vendor/bin:/system/sbin:/system/bin:/system/xbin",
            NULL
};
#endif

static char *ysl_find_suexec();
static int ysl_shell_ack(int ipcin, int ipcout);

// yasul_open_session():
//
ysl_session_t *yasul_open_session(const char *logdir, int flags,
    const char* secontext) {
    ysl_log_printf("+ yasul_open_session(0x%x , <%s>):\n", flags, logdir);

    // lookup su
    char *suexec = ysl_find_suexec();
    if (! suexec) {
        ysl_log_printf("Failed to locate suitable su binary !\n");
        return NULL;
    }
    else
        ysl_log_debugf("Resolved SU binary: %s\n", suexec);

    // prepare IPC sockets
    int svin[2], svout[2], sverr[2];
    if ( (socketpair(PF_LOCAL, SOCK_STREAM, 0, svin))
            || (socketpair(PF_LOCAL, SOCK_STREAM, 0, svout))
            ||  (socketpair(PF_LOCAL, SOCK_STREAM, 0, sverr)) ) {
        ysl_log_errno(errno);
        ysl_log_printf("Failed to setup IPC endpoints !\n");
        return NULL;
    }

    // creates shell process
    pid_t pid = fork();
    if (pid == -1) {
        ysl_log_printf("Failed to create shell process !\n");
        ysl_log_errno(errno);
        return NULL;
    }
    else if (pid == 0) {
        // shell process closes unused descriptors,
        close(svin[0]);
        close(svout[0]);
        close(sverr[0]);
        // duplicates the local socket descriptors to its owns,
        dup2(svin[1], 0);
        dup2(svout[1], 1);
        dup2(sverr[1], 2);
        // and attempts to start requested shell
        char *params[4] = { suexec, NULL, NULL, NULL};
        if (secontext) {
            params[1] = "--context"; 
            params[2] = (char *) secontext;
        }
        execve(suexec, params, YSL_SU_ENV);
        exit(1);
    }

    // parent process, closes unused descriptors
    close(svin[1]);
    close(sverr[1]);
    close(svout[1]);

    // wait for accept/refuse event
    int err;
    if ( (err = ysl_shell_ack(svin[0], svout[0])) ) {
        ysl_log_printf("Failed to confirm shell subprocess PID: %d !\n", pid);
        ysl_log_errno(err);
        goto jmp_on_error;
    }
    else
        ysl_log_debugf("shell subprocess's confirmed session: %d\n", pid);
    
    // creates session
    ysl_session_t *s = ysl_session_create(logdir, pid, flags);
    if (! s) goto jmp_on_error;

    // setut session IPC
    s->ipcin = svin[0];
    s->ipcout = svout[0];
    s->ipcerr = sverr[0];
    // should be ready to start handlers
    pthread_create(s->pthout, 0, ysl_pthout_fn, s);        
    pthread_create(s->ptherr, 0, ysl_ptherr_fn, s); 
    pthread_create(s->kworker, 0, ysl_kworker_fn, s);
    // session's ready
    return s;

jmp_on_error:
    close(svin[0]);
    close(sverr[0]);
    close(svout[0]);
    return NULL;
}

char *ysl_find_suexec() {
    struct stat sustat;
    char* suexec = NULL;
    int i = 0;
    while (YSL_SU_CANDIDATES[i]) {
        if (stat(YSL_SU_CANDIDATES[i], &sustat))
            i++;
        else {
            suexec = (char *) YSL_SU_CANDIDATES[i];
            break;
        }
    }
    return suexec;
}

int ysl_shell_ack(int ipcin, int ipcout) {

    // sends the session confirmation message request
    int err = 0, ackw = 0;
    while ( (! err) 
            &&
            (ackw = send(ipcin, YSL_ACK_CMD, strlen(YSL_ACK_CMD), 
                         MSG_NOSIGNAL | MSG_DONTWAIT))
            != strlen(YSL_ACK_CMD) ) {

        if ( (errno == EAGAIN) || (errno == EWOULDBLOCK) ) {
            ysl_log_debugf("ysl_wait_shell_ack(): waiting I/O ...\n");
            sleep(1); // let's wait 1s
        }
        else {
            err = errno;
            ysl_log_debugf("ysl_wait_shell_ack(): no child process ?\n");
        }
    }
    if (err)
        return err;
    else
        ysl_log_debugf("waiting for shell process ACK ...\n");

    // consume ack to consume it ...
    char ack[strlen(YSL_ACK_TAG)];    
    if ( (read(ipcout, ack, strlen(YSL_ACK_TAG)) == strlen(YSL_ACK_TAG))
            && (strstr(ack, YSL_ACK_TAG)) )
        return 0;
    
    return errno; 
}

