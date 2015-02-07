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
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <unistd.h>

#include "def.h"
#include "log.h"


static ysl_log_t *ysllog;

static void ysl_log_banner();

// ysl_log_init():
//
int ysl_log_init(const char *dirpath, unsigned char debug) {
    if (ysllog)
        return 0;
    ysllog = malloc(sizeof(ysl_log_t));
    if (!ysllog)
        return ENOMEM;
    // debug
    ysllog->debug = debug;
    // path
    ysllog->path = malloc(strlen(YSL_LOGPATH_FMT) + (strlen(dirpath)-2) + 1);
    if (! ysllog->path) {
        ysl_log_dispose();
        return ENOMEM;
    }
    sprintf(ysllog->path, YSL_LOGPATH_FMT, dirpath, getpid());
    // file
    ysllog->file = fopen(ysllog->path, "w");
    if (! ysllog->file) {
        ysl_log_dispose();
        return EACCES;
    }
    // log file header
    ysl_log_timestamp();
    ysl_log_banner();
    return 0;
}

// ysl_log_dispose():
//
void ysl_log_dispose() {
    if (! ysllog)
        return ;

    if (ysllog->file)
        fclose(ysllog->file);

    free(ysllog->path);
    free(ysllog->file);
    free(ysllog);
    ysllog = NULL;
}

// ysl_log_path():
//
const char *ysl_log_path() {
    if (ysllog) 
        return ysllog->path;
    else
        return NULL;
}

// ysl_log_printf():
//
void ysl_log_printf(const char *fmt, ...) {
    if (! ysllog)
        return;
    va_list vargs;
    va_start(vargs, fmt);
    vfprintf(ysllog->file, fmt, vargs);
    va_end(vargs);
    fflush(ysllog->file);
}

// ysl_log_printf2():
//
void ysl_log_printf2(ysl_session_t *s, const char *fmt, ...) {
    if (! ysllog)
        return;
    fprintf(ysllog->file, "[%05d] ", s->pid);
    va_list vargs;
    va_start(vargs, fmt);
    vfprintf(ysllog->file, fmt, vargs);
    va_end(vargs);
    fflush(ysllog->file);
}

// ysl_log_debugf():
//
void ysl_log_debugf(const char *fmt, ...) {
    if (! (ysllog && ysllog->debug) )
        return;
    va_list vargs;
    va_start(vargs, fmt);
    vfprintf(ysllog->file, fmt, vargs);
    va_end(vargs);
    fflush(ysllog->file);
}

// ysl_log_debugf2():
//
void ysl_log_debugf2(ysl_session_t *s, const char *fmt, ...) {
    if (! (ysllog && (s->flags & YSL_SF_VERB)) )
        return;
    fprintf(ysllog->file, "[%05d] ", s->pid);
    va_list vargs;
    va_start(vargs, fmt);
    vfprintf(ysllog->file, fmt, vargs);
    va_end(vargs);
    fflush(ysllog->file);
}

// ysl_log_errno():
//
void ysl_log_errno(int err) {
    if (! ysllog)
        return;
    fprintf(ysllog->file, "POSIX errno(%d): %s\n", err, strerror(err));
}

// ysl_log_errno2():
//
void ysl_log_errno2(ysl_session_t *s, int err) {
    if (! ysllog)
        return;
    fprintf(ysllog->file, "[%05d] ", s->pid);
    fprintf(ysllog->file, "POSIX errno(%d): %s\n", err, strerror(err));
}

// ysl_log_timestamp():
//
void ysl_log_timestamp() {
    if (! ysllog)
        return;
    time_t t0;
    time(&t0);
    fprintf(ysllog->file, "%s", ctime(&t0));
    fflush(ysllog->file);
}

void ysl_log_banner() {
    fprintf(ysllog->file, "%s (%s)\n", YSL_BUILD_LABEL, YSL_BUILD_VERSION);
}
