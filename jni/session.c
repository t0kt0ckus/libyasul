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
#include <pthread.h>
#include <signal.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

#include "def.h"
#include "log.h"
#include "session.h"

// ysl_session_create():
//
ysl_session_t *ysl_session_create(const char *logdir, int pid, int flags) {
    ysl_session_t *s = malloc(sizeof(ysl_session_t));
    if (! s)
        return NULL;

    s->einval = 0;
    s->pid = pid;
    s->flags = flags;
    s->ipcin = s->ipcout = s->ipcerr = -1;
    s->ltty = NULL;

    s->outepath = malloc(strlen(YSL_EOUT_FMT) + (strlen(logdir)-2) + 1 +1);
    s->errepath = malloc(strlen(YSL_EERR_FMT) + (strlen(logdir)-2) + 1 +1);
    if ( (! s->outepath) || (! s->errepath) )
        goto jmp_on_error;
    sprintf(s->outepath, YSL_EOUT_FMT, logdir, pid);
    s->outefile = NULL;
    sprintf(s->errepath, YSL_EERR_FMT, logdir, pid);
    s->errefile = NULL;

    s->ism = malloc(sizeof(pthread_mutex_t));
    s->lecc = malloc(sizeof(pthread_cond_t));
    if ( (! s->ism) || (! s->lecc) )
        goto jmp_on_error;
    pthread_mutex_init(s->ism, NULL); // default attrs
    pthread_cond_init(s->lecc, NULL); // default attrs (Linux ignores this)

    s->pthout = malloc(sizeof(pthread_t));
    s->ptherr = malloc(sizeof(pthread_t));
    s->kworker = malloc(sizeof(pthread_t));
    if ( (! s->pthout) || (! s->ptherr) || (! s->kworker))
        goto jmp_on_error; 

    ysl_log_debugf("created new session [%d] at address: 0x%x\n",
            s->pid, s);
    return s;

jmp_on_error:
    ysl_log_printf2(s, "Failed to allocate session !\n");
    ysl_log_printf2(s, "errno: %d, %s\n", errno, strerror(errno));
    ysl_session_destroy(s);
    return NULL;
}

// ysl_session_destroy():
//
void ysl_session_destroy(ysl_session_t *s) {
    if (s) {
        ysl_log_debugf("delete session [%d] at address: 0x%x\n", s->pid, s);

        if (s->lecc) {
            pthread_cond_destroy(s->lecc);
            free(s->lecc);
        }
        if (s->ism) {
            pthread_mutex_destroy(s->ism);
            free(s->ism);
        }
        
        if (s->ipcin > -1)
            close(s->ipcin);
        if (s->ipcout > -1)
            close(s->ipcout);
        if (s->ipcerr > -1)
            close(s->ipcerr);

        free(s->outepath);
        free(s->errepath);
        if (s->outefile)
            fclose(s->outefile);
        if (s->errefile)
            fclose(s->errefile);

        free(s->ltty);

        free(s->pthout);
        free(s->ptherr);
        free(s->kworker);

        free(s);
    }
}

// ysl_session_cfset():
//
int ysl_session_cfset(ysl_session_t *s, int flag, unsigned char isfset) {
    ysl_log_debugf2(s, "ysl_session_cfset(Ox%x , %d)\n", flag, isfset);

    // clear flag bit
    s->flags = s->flags & ~flag;
    // set flag bit
    if (isfset)
        s->flags |= flag;

    ysl_log_debugf2(s, "new flags: Ox%x\n", s->flags);
    return s->flags;
}

// ysl_session_cfget():
//
unsigned char ysl_session_cfget(ysl_session_t *s, int flag) {
    return s->flags & flag;
}

// ysl_session_stat():
//
int ysl_session_stat(ysl_session_t *session) {
    if ( (session->einval)
            || (send(session->ipcin, NULL, 0, MSG_NOSIGNAL) != 0) )
        return EPIPE;
    else    
        return 0;
}

// ysl_session_exec():
//
int ysl_session_exec(ysl_session_t *s, const char *cmdstr, int *ecode,
        char** ltty) {
    if (s->einval)
        return EPIPE;
    ysl_log_debugf2(s, "ysl_session_exec(\"%s\"):\n", cmdstr);
    
    int err = 0;

    // acquires lock
    pthread_mutex_lock(s->ism);
   
    // send() cmd string
    int cmdlen = strlen(cmdstr);
    int eoclen = strlen(YSL_LEC_CMD);
    if ( (send(s->ipcin, cmdstr, cmdlen, MSG_NOSIGNAL) == cmdlen)
            && (send(s->ipcin, "\n", 1, MSG_NOSIGNAL) == 1)
            && (send(s->ipcin,
                    YSL_LEC_CMD,
                    eoclen,
                    MSG_NOSIGNAL) == eoclen) ) {
        // waits for completion
        pthread_cond_wait(s->lecc, s->ism); // releases lock, then relock
                                            // when signaled
        // sets exit code
        (*ecode) = s->lec;
        // sets last TTY line
        (*ltty) = malloc(strlen(s->ltty)+1);
        if (*ltty)
            strcpy(*ltty, s->ltty);
        else
            err = ENOMEM;
    }
    else 
        err = EPIPE;

    if (err) {
        ysl_log_printf2(s, "Failed to process command !\n");
        ysl_log_errno2(s, errno);
    }
    else
        ysl_log_printf2(s, "exit code: %d , LTTY: \"%s\"\n", (*ecode),
                (*ltty));

    // unlocks before exit
    pthread_mutex_unlock(s->ism);
    return err;
}

// ysl_session_exit():
//
void ysl_session_exit(ysl_session_t *s, int maxwait_sec, 
        unsigned char forcek) {
    ysl_log_debugf2(s, "ysl_session_exit(%d sec , kill: %d):\n",
                maxwait_sec, forcek);

    if (! s->einval) {
        ysl_log_debugf2(s, "session is valid, invalidating ...\n");
        int err = 0;

        // try to properly exit shell process within timeout
        int sz = strlen(YSL_EXIT_CMD);
        if (send(s->ipcin, YSL_EXIT_CMD, sz, MSG_NOSIGNAL) == sz)
            ysl_log_debugf2(s, "#exit\n");
        else {
            ysl_log_printf2(s, "Failed to send() exit command !\n");
            err = EPIPE;
        }        
        if (! err) {
            // wait for shell exit
            int sec = 0;
            int status;
            while ( (sec < maxwait_sec)
                    &&
                    ((err = waitpid(s->pid, &status, WNOHANG)) != s->pid) ){
                    ysl_log_debugf2(s, "waiting for shell death ...\n");

                sleep(1);
                sec++;
            }
            err = (err == s->pid) ? 0 : err;
        }

        if (err) {
            // shell process's still alive
            if (forcek) {
                ysl_log_debugf2(s, "forcek set, SIGKILL ...\n"); 
                kill(s->pid, SIGKILL);
            }
            else {
                ysl_log_debugf2(s, "forcek not set, stop IPC I/O ...\n");
                close(s->ipcout);
                s->ipcout = -1;
                close(s->ipcerr);
                s->ipcerr = -1;
            }
        }

        // at this stage, we should be able to wait for the worker
        void *retval;
        pthread_join(*s->kworker, &retval);
    }

    // at this stage session should be invalidated
    if (s->einval)
        ysl_session_destroy(s);
    else
        ysl_log_printf2(s, "Failed to properly release this session !\n");
}

