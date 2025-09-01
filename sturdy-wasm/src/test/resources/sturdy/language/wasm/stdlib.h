/**
 * Standard library stubs to replace libc's functions.
 */


#define NULL 0

// types.h

#define	__S16_TYPE		short int
#define __U16_TYPE		unsigned short int
#define	__S32_TYPE		int
#define __U32_TYPE		unsigned int
#define __SLONGWORD_TYPE	long int
#define __ULONGWORD_TYPE	unsigned long int
#define __SQUAD_TYPE		long int
#define __UQUAD_TYPE		unsigned long int
#define __SWORD_TYPE		long int
#define __UWORD_TYPE		unsigned long int
#define __SLONG32_TYPE		int
#define __ULONG32_TYPE		unsigned int
#define __S64_TYPE		long int
#define __U64_TYPE		unsigned long int
#define __STD_TYPE		typedef

typedef typeof(sizeof(0)) size_t;
typedef signed char __int8_t;
typedef unsigned char __uint8_t;
typedef signed short int __int16_t;
typedef unsigned short int __uint16_t;
typedef signed int __int32_t;
typedef unsigned int __uint32_t;
typedef signed long int __int64_t;
typedef unsigned long int __uint64_t;
typedef __uint8_t uint8_t;
typedef __uint16_t uint16_t;
typedef __uint32_t uint32_t;
typedef __uint64_t uint64_t;
typedef __int8_t __int_least8_t;
typedef __uint8_t __uint_least8_t;
typedef __int16_t __int_least16_t;
typedef __uint16_t __uint_least16_t;
typedef __int32_t __int_least32_t;
typedef __uint32_t __uint_least32_t;
typedef __int64_t __int_least64_t;
typedef __uint64_t __uint_least64_t;
typedef long int __intmax_t;
typedef unsigned long int __uintmax_t;
typedef __intmax_t		intmax_t;
typedef __uintmax_t		uintmax_t;

extern int toupper(int chr);
extern int tolower(int chr);

extern void* malloc(size_t size);
extern void* realloc(void*, unsigned long);
extern void free(void* ptr);
extern void* memcpy(void*, const void*, unsigned long);
extern void* memmove(void*, const void*, unsigned long);
extern unsigned long strlen(const char*);

extern int ext_printf(const char* str, ...);
extern int __VERIFIER_nondet_int();
extern void assert(int condition);

extern double ext_pow(double, double);
extern double sqrt(double);

extern void exit (int status);

typedef __SWORD_TYPE ssize_t;
struct _IO_FILE;
typedef struct _IO_FILE FILE;
extern ssize_t write (int fd, const void *buf, size_t nbytes);
extern ssize_t read (int fd, void *buf, size_t nbytes);
extern size_t fwrite(const void *data, size_t size, size_t count, FILE *stream);
extern int fputs(const char* str, FILE* fp);
extern char* fgets (char *buf, int n, FILE* fp);
extern int fileno(FILE* fp);
extern FILE* stdin;
extern FILE* stdout;
extern FILE* stderr;


typedef signed long intptr_t;