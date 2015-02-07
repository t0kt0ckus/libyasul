/*
 * yasul: Yet another Android SU library. 
 *
 * t0kt0ckus
 * (C) 2014,2015
 * 
 * License LGPLv2, GPLv3
 * 
 */
#ifndef _YASUL_BUF_H
#define _YASUL_BUF_H

#ifdef __cplusplus
extern "C" {
#endif

typedef struct ysl_buf {
    int size;
    int len;
    char *data;

} ysl_buf_t;

// Allocates a new buffer.
// Exits current thread if an error occurs.
//
// Returns: the buffer, never returns NULL.
//
ysl_buf_t *ysl_buf_create();

// Enforces that the buffer will accept the requested additional bytes. 
// Exits current thread if an error occurs.
//
// inc: number of additional elements to expect
//
void ysl_buf_enforce(ysl_buf_t *b, int inc);

// Adds n bytes from src to the buffer, adapting capacity. 
// Exits current thread if an error occurs.
//
void ysl_buf_add(ysl_buf_t *b, char *src, int n);

// Adds one byte to the buffer.
//
void ysl_buf_addbyte(ysl_buf_t *b, char z);

// Adds a NULL terminated (including) string to the buffer.
// Exits current thread if an error occurs.
//
void ysl_buf_addstr(ysl_buf_t *b, char *str);

char *ysl_buf_strstr(ysl_buf_t *b, const char *str);

// Resets this buffer size to zero.
// Does not affect capacity.
//
void ysl_buf_reset(ysl_buf_t *b);

// Free buffer memory.
//
void ysl_buf_delete(ysl_buf_t *b);

#ifdef __cplusplus
}
#endif

#endif

