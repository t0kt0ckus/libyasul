/*
 * yasul: Yet another Android SU library. 
 *
 * t0kt0ckus
 * (C) 2014,2015
 * 
 * License LGPLv2, GPLv3
 * 
 */
#include <pthread.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "def.h"
#include "log.h"
#include "buf.h"

// ysl_buf_create():
//
ysl_buf_t *ysl_buf_create() {
    ysl_buf_t *b = malloc(sizeof(ysl_buf_t));
    if (! b)
        pthread_exit(NULL);
    b->data = malloc(YSL_BUF_INISZ);
    if (! b->data)
        pthread_exit(NULL);

    b->size = YSL_BUF_INISZ;
    b->len = 0;
    return b;
}

// ysl_buf_create():
//
void ysl_buf_enforce(ysl_buf_t *b, int inc) {
    if (b->size < (b->len + inc)) {
        int nsz = b->len + inc + YSL_BUF_WINDOW;
        b->data = realloc(b->data, nsz);
        if (! b->data)
            pthread_exit(0);
        b->size = nsz;
        ysl_log_debugf("increased buffer size to %d bytes\n", b->size);
    }
}

// ysl_buf_add():
//
void ysl_buf_add(ysl_buf_t *b, char *src, int n) {
    ysl_buf_enforce(b, n);
    memcpy(b->data + b->len, src, n);
    b->len += n;
}

// ysl_buf_addbyte():
//
void ysl_buf_addbyte(ysl_buf_t *b, char z) {
    ysl_buf_enforce(b, 1);
    memcpy(b->data + b->len, &z, 1);
    ++b->len;
}

// ysl_buf_addstr():
//
void ysl_buf_addstr(ysl_buf_t *b, char *str) {
    ysl_buf_enforce(b, strlen(str) + 1);
    strcpy(b->data + b->len, str);
    b->len += strlen(str) + 1;
}

char *ysl_buf_strstr(ysl_buf_t *b, const char *str) {
    if (b->len == 0)
        return NULL;
    else
        return strstr(b->data, str);
}

// ysl_buf_reset():
//
void ysl_buf_reset(ysl_buf_t *b) {
    b->len = 0;
    /*
    if (b->size > 0)
        *(b->data) = 0x00;
        */
}

// ysl_buf_delete():
//
void ysl_buf_delete(ysl_buf_t *b) {
    if (! b)
        return;
    free(b->data);
    free(b);
}
