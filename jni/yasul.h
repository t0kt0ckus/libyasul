/*
 * yasul: Yet another Android SU library. 
 *
 * t0kt0ckus
 * (C) 2014,2015
 * 
 * License LGPLv2, GPLv3
 * 
 */
#ifndef _YASUL_H
#define _YASUL_H

#include "session.h"

#ifdef __cplusplus
extern "C" {
#endif

ysl_session_t *yasul_open_session(const char *logdir, int flags); 

#ifdef __cplusplus
}
#endif

#endif
