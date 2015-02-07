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

void *ysl_ptherr_fn(void *arg) {
    ysl_session_t *s = (ysl_session_t *) arg;

    s->errefile = fopen(s->errepath, "w");
    if (! s->errefile) {
        ysl_log_printf2(s, "ysl_ptherr_fn(): Failed to open <%s> !\n",
                s->errepath);
        pthread_exit(NULL);
    }
    int fderr = fileno(s->errefile);
    
    ysl_log_debugf2(s, "ysl_ptherr_fn(): %s\n", s->errepath);
    
    char z;
    while (read(s->ipcerr, &z, 1) == 1) {
        // echo byte if requested
        if (s->flags & YSL_SF_EERR)
            write(fderr, &z, 1);
    } // end read()

    if (s->flags & YSL_SF_VERB)
        ysl_log_debugf2(s, "ysl_ptherr_fn(): exiting ...\n");
    
    pthread_exit(NULL);
} 

