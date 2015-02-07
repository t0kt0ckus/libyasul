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
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <unistd.h>

#include "def.h"
#include "log.h"
#include "session.h"


void *ysl_kworker_fn(void *arg) {
    ysl_session_t *s = (ysl_session_t *) arg;
    ysl_log_debugf2(s, "ysl_kworker_fn():\n");

    void *buf;
    pthread_join(*s->pthout, &buf);
    pthread_join(*s->ptherr, &buf);

    // close all I/Os
    close(s->ipcin);
    close(s->ipcout);
    close(s->ipcerr);

    s->einval = 1;

    ysl_log_debugf2(s, "ysl_kworker_fn(): session invalidated.\n");
    pthread_exit(NULL);
} 

