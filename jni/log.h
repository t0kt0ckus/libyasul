/*
 * yasul: Yet another Android SU library. 
 *
 * t0kt0ckus
 * (C) 2014,2015
 * 
 * License LGPLv2, GPLv3
 * 
 */
#ifndef _YASUL_LOG_H
#define _YASUL_LOG_H

#include <stdio.h>
#include <stdarg.h>

#include "session.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef struct ysl_log {
    unsigned char debug;
    char *path;
    FILE *file;

} ysl_log_t;

// Initializes logging feature. Should be called before API bellow,
// though it's safe not to do so ;-)
//
// Returns: 0 on succes.
//
int ysl_log_init(const char *dirpath, unsigned char debug);

// Releases logging resources.
//
void ysl_log_dispose();

// Answers absolute path to yasul log file.
//
const char *ysl_log_path();

void ysl_log_printf(const char *fmt, ...);
void ysl_log_printf2(ysl_session_t *s, const char *fmt, ...);

void ysl_log_debugf(const char *fmt, ...);
void ysl_log_debugf2(ysl_session_t *s, const char *fmt, ...);

void ysl_log_errno(int err);
void ysl_log_errno2(ysl_session_t *s, int err);

void ysl_log_timestamp();

#ifdef __cplusplus
}
#endif

#endif
