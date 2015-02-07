/*
 * yasul: Yet another Android SU library. 
 *
 * t0kt0ckus
 * (C) 2014,2015
 * 
 * License LGPLv2, GPLv3
 * 
 */
#ifndef _YASUL_SESSION_H
#define _YASUL_SESSION_H

#include <stdio.h>
#include <pthread.h>
#include <sys/types.h>


#ifdef __cplusplus
extern "C" {
#endif

// Defines a SU shell session.
//
typedef struct ysl_session {
    
    pid_t pid;  // shell PID    
    int flags;  // session control flags bit mask (see def.h)

    // invalidation flag: an invalidated session is kept in memory till exit()
    unsigned char einval; 

    // IPC sockets
    int ipcin;   // fd bound to shell process stdin
    int ipcout;  // fd bound to shell process stdout
    int ipcerr;  // fd bound to shell process stderr

    // echo files
    char *outepath;
    FILE *outefile;
    char *errepath;
    FILE *errefile;

    // interactive shell mutext
    pthread_mutex_t *ism;

    // last exit code and associated condition
    pthread_cond_t *lecc;
    int lec;
    
    // last shell process TTY output
    char *ltty;

    // threads
    pthread_t *pthout;
    pthread_t *ptherr;
    pthread_t *kworker;

} ysl_session_t;

// Initializes a new session in memory.
//
// pid: pid of associated shell process.
// logdir: base directory for session's login.
// flags: initial bit mask of session's flags.
//
// Returns: The created sessio, or NULL on any error.
//
ysl_session_t *ysl_session_create(const char *logdir, int pid, int flags);

// Release a session's memory resources.
//
void ysl_session_destroy(ysl_session_t *session);

// Set a session's control flag.
//
// flag: one in YSL_SF_XXXX.
// isfset: 0 or 1.
// 
// Returns: the new session's flags bit mask.
//
int ysl_session_cfset(ysl_session_t *session, int flag,
    unsigned char isfset);

// Determines whether a particular control flag is set.
//
unsigned char ysl_session_cfget(ysl_session_t *session, int flag);

// Determines wheter a session is ready to process command strings.
//
// Returns: 0 on success, EPIPE on error.
//
int ysl_session_stat(ysl_session_t *session);

// Executes a command string.
//
// ecode: to store the command string exit code.
// ltty: to store last line red on shell stdout that relates to this command
// string execution (the memory is allocated here and should be freed by
// client code).
//
// Returns: 0 on success.
//
int ysl_session_exec(ysl_session_t *session, const char *cmdstr, int *ecode,
    char** ltty);

// Terminates a session.
//
// maxwait_sec:
// forcek:
//
void ysl_session_exit(ysl_session_t *session, int maxwait_sec, 
        unsigned char forcek);

#ifdef __cplusplus
}
#endif

#endif
