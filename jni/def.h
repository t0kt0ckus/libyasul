/*
 * yasul: Yet another Android SU library. 
 *
 * t0kt0ckus
 * (C) 2014,2015
 * 
 * License LGPLv2, GPLv3
 * 
 */
#ifndef _YASUL_DEF_H
#define _YASUL_DEF_H

/* Yasul version information
 */
#define YSL_BUILD_VERSION "0.1"
#define YSL_BUILD_LABEL "Yasul, Yet another Android SU library"

/* Yasul sessions control flags (all may impact performances)
 */

// Echo out: echo shell process stdout to a log file
#define YSL_SF_EOUT (0x01)

// Echo err: echo shell process stderr to a log file
#define YSL_SF_EERR (0x02)

// Tail: To enable multi-threading and command execution monitoring, maintains
// a copy of the last shell process stdout line always ("last tty") available .
#define YSL_SF_TAIL (0x04)

// Zero TTY before executing next command.
// When this flag is set, a command that does not produce any output
// will update last TTY to an empty value.
// When this flag is not set, a command that does not produce any output
// does not update the last TTY available value.
#define YSL_SF_ZTTY (0x08)

// Verbose: logs a session's commands and exit codes, ...
#define YSL_SF_VERB (0x10)


/* Log file.
 */
#define YSL_LOGPATH_FMT "%s/yasul-%05d.log"

/* Echo files
 */
#define YSL_EOUT_FMT "%s/su_stdout-%05d.log"
#define YSL_EERR_FMT "%s/su_stderr-%05d.log"

/* Buffer management.  
 */

// initial buffer capacity
#define YSL_BUF_INISZ (64)
 
// number of bytes that a buffer capacity should have over its requested length.
#define YSL_BUF_WINDOW (128)

// max buffer capacity
#define YSL_BUF_MAXSZ (1024)


/* Exit code request command and parsing
 */ 
#define YSL_ACK_CMD "echo \"<YSL_ACK>\"\n"
#define YSL_ACK_TAG "<YSL_ACK>\n"
#define YSL_LEC_CMD "echo \"<YSL_LEC>\"$?\n"
#define YSL_LEC_TAG "<YSL_LEC>"
#define YSL_LEC_SSCAN "<YSL_LEC>%d"

/* Session termination command
 */
#define YSL_EXIT_CMD "exit\n"

/* JNI bridge
 */
#define YSL_J_YslPort "org/openmarl/yasul/YslPort"
#define YSL_J_YslPort_init_s "(IJLjava/lang/String;Ljava/lang/String;)V"
#define YSL_J_YslParcel "org/openmarl/yasul/YslParcel"
#define YSL_J_YslParcel_init_s "(ILjava/lang/String;)V"

#ifdef __cplusplus
extern "C" {
#endif

/* Thread functions
 */
void *ysl_pthout_fn(void *); 
void *ysl_ptherr_fn(void *);
void *ysl_kworker_fn(void *);
 
#ifdef __cplusplus
}
#endif

#endif
