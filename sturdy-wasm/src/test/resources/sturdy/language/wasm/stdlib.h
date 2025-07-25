/**
 * Standard library stubs to replace libc's functions.
 */


#define NULL 0
typedef typeof(sizeof(0)) size_t;
extern void* malloc(size_t size);
extern void free(void* ptr);
extern int ext_printf(const char* str, ...);
extern int __VERIFIER_nondet_int();
extern void assert(int condition);
extern double ext_pow(double, double);