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
#include <unistd.h>

#include "def.h"
#include "log.h"
#include "buf.h"
#include "session.h"

void *ysl_pthout_fn(void *arg) {
    ysl_session_t *s = (ysl_session_t *) arg;

    s->outefile = fopen(s->outepath, "w");
    if (! s->outefile) {
        ysl_log_printf2(s, "ysl_pthout_fn(): Failed to open <%s> !\n",
                s->outepath);
        pthread_exit(NULL);
    }

    int fdout = fileno(s->outefile);
    ysl_log_debugf2(s, "ysl_pthout_fn(): %s\n", s->outepath);
    
    // human-readable last tty
    int hrsz = 0;
    char *hrtty = NULL;
    // parser
    ysl_buf_t *b = ysl_buf_create();
    char z;

    // starts to parse shell process STDOUT as commands output
    while (recv(s->ipcout, &z, 1, MSG_NOSIGNAL) == 1) {
        // echo byte if requested
        if (s->flags & YSL_SF_EOUT)
            write(fdout, &z, 1);

        if (z != '\n')
            ysl_buf_add(b, &z, 1);        
        else {
            // EOL
            if (ysl_buf_strstr(b, YSL_LEC_TAG)) {
                // End of command

                // update session's ltty if needed
                if (! (s->flags & YSL_SF_TAIL)) {
                    free(s->ltty);
                    if (hrsz > 0) {
                        s->ltty = malloc(hrsz);
                        if (s->ltty) 
                            memcpy(s->ltty, hrtty, hrsz);
                    }
                    else {
                        // we assume ltty should then be empty
                        if (s->flags & YSL_SF_ZTTY) {
                            s->ltty = malloc(1);
                            *(s->ltty) = 0x00;
                        }
                    } 
                }

                // update LEC
                sscanf(b->data, YSL_LEC_SSCAN, &s->lec);

                // wakeup main thread waiting for exit code
                pthread_cond_signal(s->lecc);

                // reset hltty/hsz
                hrsz = 0;
            }
            else {
                // "normal" shell stdout line

                // updates humain readable ltty if needed                    
                ysl_buf_addbyte(b, 0x00); // terminates C string
                free(hrtty);                    
                hrtty = b->data;
                hrsz = b->len;
                // FIXME: try with buffer reset
                b = ysl_buf_create();

                // when tail flag set, copy to session ltty
                if (s->flags & YSL_SF_TAIL) {
                    free(s->ltty);
                    if (hrsz > 0) {
                        s->ltty = malloc(hrsz);
                        if (s->ltty) 
                            memcpy(s->ltty, hrtty, hrsz);
                    }
                    else {
                        // we assume ltty should then be empty
                        if (s->flags & YSL_SF_ZTTY) {
                            s->ltty = malloc(1);
                            *(s->ltty) = 0x00;
                        }
                    }
                } 
            }
            
            ysl_buf_reset(b);
        } // end EOL

    } // end read()
    ysl_buf_delete(b);

    // in case of broken pipe, resume waiting client thread
    if (pthread_mutex_trylock(s->ism) == 0) {
        pthread_cond_signal(s->lecc);
        pthread_mutex_unlock(s->ism);
    }

    ysl_log_printf2(s, "ysl_pthout_fn(): exit()\n");
    pthread_exit(NULL);
} 

