/* mini-gmp, a minimalistic implementation of a GNU GMP subset.

Copyright 2011-2015, 2017, 2019-2021 Free Software Foundation, Inc.

This file is part of the GNU MP Library.

The GNU MP Library is free software; you can redistribute it and/or modify
it under the terms of either:

  * the GNU Lesser General Public License as published by the Free
    Software Foundation; either version 3 of the License, or (at your
    option) any later version.

or

  * the GNU General Public License as published by the Free Software
    Foundation; either version 2 of the License, or (at your option) any
    later version.

or both in parallel, as here.

The GNU MP Library is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
for more details.

You should have received copies of the GNU General Public License and the
GNU Lesser General Public License along with the GNU MP Library.  If not,
see https://www.gnu.org/licenses/.  */

/* About mini-gmp: This is a minimal implementation of a subset of the
   GMP interface. It is intended for inclusion into applications which
   have modest bignums needs, as a fallback when the real GMP library
   is not installed.

   This file defines the public interface. */

#ifndef __MINI_GMP_H__
#define __MINI_GMP_H__


void mp_set_memory_functions (void *(*) (size_t),
			      void *(*) (void *, size_t, size_t),
			      void (*) (void *, size_t));

void mp_get_memory_functions (void *(**) (size_t),
			      void *(**) (void *, size_t, size_t),
			      void (**) (void *, size_t));

#ifndef MINI_GMP_LIMB_TYPE
#define MINI_GMP_LIMB_TYPE long
#endif

typedef unsigned MINI_GMP_LIMB_TYPE mp_limb_t;
typedef long mp_size_t;
typedef unsigned long mp_bitcnt_t;

typedef mp_limb_t *mp_ptr;
typedef const mp_limb_t *mp_srcptr;

typedef struct
{
  int _mp_alloc;		/* Number of *limbs* allocated and pointed
				   to by the _mp_d field.  */
  int _mp_size;			/* abs(_mp_size) is the number of limbs the
				   last field points to.  If _mp_size is
				   negative this is a negative number.  */
  mp_limb_t *_mp_d;		/* Pointer to the limbs.  */
} __mpz_struct;

typedef __mpz_struct mpz_t[1];

typedef __mpz_struct *mpz_ptr;
typedef const __mpz_struct *mpz_srcptr;

void mpn_copyi (mp_ptr, mp_srcptr, mp_size_t);
void mpn_copyd (mp_ptr, mp_srcptr, mp_size_t);
void mpn_zero (mp_ptr, mp_size_t);

int mpn_cmp (mp_srcptr, mp_srcptr, mp_size_t);
int mpn_zero_p (mp_srcptr, mp_size_t);

mp_limb_t mpn_add_1 (mp_ptr, mp_srcptr, mp_size_t, mp_limb_t);
mp_limb_t mpn_add_n (mp_ptr, mp_srcptr, mp_srcptr, mp_size_t);
mp_limb_t mpn_add (mp_ptr, mp_srcptr, mp_size_t, mp_srcptr, mp_size_t);

mp_limb_t mpn_sub_1 (mp_ptr, mp_srcptr, mp_size_t, mp_limb_t);
mp_limb_t mpn_sub_n (mp_ptr, mp_srcptr, mp_srcptr, mp_size_t);
mp_limb_t mpn_sub (mp_ptr, mp_srcptr, mp_size_t, mp_srcptr, mp_size_t);

mp_limb_t mpn_mul_1 (mp_ptr, mp_srcptr, mp_size_t, mp_limb_t);
mp_limb_t mpn_addmul_1 (mp_ptr, mp_srcptr, mp_size_t, mp_limb_t);
mp_limb_t mpn_submul_1 (mp_ptr, mp_srcptr, mp_size_t, mp_limb_t);

mp_limb_t mpn_mul (mp_ptr, mp_srcptr, mp_size_t, mp_srcptr, mp_size_t);
void mpn_mul_n (mp_ptr, mp_srcptr, mp_srcptr, mp_size_t);
void mpn_sqr (mp_ptr, mp_srcptr, mp_size_t);
int mpn_perfect_square_p (mp_srcptr, mp_size_t);
mp_size_t mpn_sqrtrem (mp_ptr, mp_ptr, mp_srcptr, mp_size_t);
mp_size_t mpn_gcd (mp_ptr, mp_ptr, mp_size_t, mp_ptr, mp_size_t);

mp_limb_t mpn_lshift (mp_ptr, mp_srcptr, mp_size_t, unsigned int);
mp_limb_t mpn_rshift (mp_ptr, mp_srcptr, mp_size_t, unsigned int);

mp_bitcnt_t mpn_scan0 (mp_srcptr, mp_bitcnt_t);
mp_bitcnt_t mpn_scan1 (mp_srcptr, mp_bitcnt_t);

void mpn_com (mp_ptr, mp_srcptr, mp_size_t);
mp_limb_t mpn_neg (mp_ptr, mp_srcptr, mp_size_t);

mp_bitcnt_t mpn_popcount (mp_srcptr, mp_size_t);

mp_limb_t mpn_invert_3by2 (mp_limb_t, mp_limb_t);
#define mpn_invert_limb(x) mpn_invert_3by2 ((x), 0)

size_t mpn_get_str (unsigned char *, int, mp_ptr, mp_size_t);
mp_size_t mpn_set_str (mp_ptr, const unsigned char *, size_t, int);

void mpz_init (mpz_t);
void mpz_init2 (mpz_t, mp_bitcnt_t);
void mpz_clear (mpz_t);

#define mpz_odd_p(z)   (((z)->_mp_size != 0) & (int) (z)->_mp_d[0])
#define mpz_even_p(z)  (! mpz_odd_p (z))

int mpz_sgn (const mpz_t);
int mpz_cmp_si (const mpz_t, long);
int mpz_cmp_ui (const mpz_t, unsigned long);
int mpz_cmp (const mpz_t, const mpz_t);
int mpz_cmpabs_ui (const mpz_t, unsigned long);
int mpz_cmpabs (const mpz_t, const mpz_t);
int mpz_cmp_d (const mpz_t, double);
int mpz_cmpabs_d (const mpz_t, double);

void mpz_abs (mpz_t, const mpz_t);
void mpz_neg (mpz_t, const mpz_t);
void mpz_swap (mpz_t, mpz_t);

void mpz_add_ui (mpz_t, const mpz_t, unsigned long);
void mpz_add (mpz_t, const mpz_t, const mpz_t);
void mpz_sub_ui (mpz_t, const mpz_t, unsigned long);
void mpz_ui_sub (mpz_t, unsigned long, const mpz_t);
void mpz_sub (mpz_t, const mpz_t, const mpz_t);

void mpz_mul_si (mpz_t, const mpz_t, long int);
void mpz_mul_ui (mpz_t, const mpz_t, unsigned long int);
void mpz_mul (mpz_t, const mpz_t, const mpz_t);
void mpz_mul_2exp (mpz_t, const mpz_t, mp_bitcnt_t);
void mpz_addmul_ui (mpz_t, const mpz_t, unsigned long int);
void mpz_addmul (mpz_t, const mpz_t, const mpz_t);
void mpz_submul_ui (mpz_t, const mpz_t, unsigned long int);
void mpz_submul (mpz_t, const mpz_t, const mpz_t);

void mpz_cdiv_qr (mpz_t, mpz_t, const mpz_t, const mpz_t);
void mpz_fdiv_qr (mpz_t, mpz_t, const mpz_t, const mpz_t);
void mpz_tdiv_qr (mpz_t, mpz_t, const mpz_t, const mpz_t);
void mpz_cdiv_q (mpz_t, const mpz_t, const mpz_t);
void mpz_fdiv_q (mpz_t, const mpz_t, const mpz_t);
void mpz_tdiv_q (mpz_t, const mpz_t, const mpz_t);
void mpz_cdiv_r (mpz_t, const mpz_t, const mpz_t);
void mpz_fdiv_r (mpz_t, const mpz_t, const mpz_t);
void mpz_tdiv_r (mpz_t, const mpz_t, const mpz_t);

void mpz_cdiv_q_2exp (mpz_t, const mpz_t, mp_bitcnt_t);
void mpz_fdiv_q_2exp (mpz_t, const mpz_t, mp_bitcnt_t);
void mpz_tdiv_q_2exp (mpz_t, const mpz_t, mp_bitcnt_t);
void mpz_cdiv_r_2exp (mpz_t, const mpz_t, mp_bitcnt_t);
void mpz_fdiv_r_2exp (mpz_t, const mpz_t, mp_bitcnt_t);
void mpz_tdiv_r_2exp (mpz_t, const mpz_t, mp_bitcnt_t);

void mpz_mod (mpz_t, const mpz_t, const mpz_t);

void mpz_divexact (mpz_t, const mpz_t, const mpz_t);

int mpz_divisible_p (const mpz_t, const mpz_t);
int mpz_congruent_p (const mpz_t, const mpz_t, const mpz_t);

unsigned long mpz_cdiv_qr_ui (mpz_t, mpz_t, const mpz_t, unsigned long);
unsigned long mpz_fdiv_qr_ui (mpz_t, mpz_t, const mpz_t, unsigned long);
unsigned long mpz_tdiv_qr_ui (mpz_t, mpz_t, const mpz_t, unsigned long);
unsigned long mpz_cdiv_q_ui (mpz_t, const mpz_t, unsigned long);
unsigned long mpz_fdiv_q_ui (mpz_t, const mpz_t, unsigned long);
unsigned long mpz_tdiv_q_ui (mpz_t, const mpz_t, unsigned long);
unsigned long mpz_cdiv_r_ui (mpz_t, const mpz_t, unsigned long);
unsigned long mpz_fdiv_r_ui (mpz_t, const mpz_t, unsigned long);
unsigned long mpz_tdiv_r_ui (mpz_t, const mpz_t, unsigned long);
unsigned long mpz_cdiv_ui (const mpz_t, unsigned long);
unsigned long mpz_fdiv_ui (const mpz_t, unsigned long);
unsigned long mpz_tdiv_ui (const mpz_t, unsigned long);

unsigned long mpz_mod_ui (mpz_t, const mpz_t, unsigned long);

void mpz_divexact_ui (mpz_t, const mpz_t, unsigned long);

int mpz_divisible_ui_p (const mpz_t, unsigned long);

unsigned long mpz_gcd_ui (mpz_t, const mpz_t, unsigned long);
void mpz_gcd (mpz_t, const mpz_t, const mpz_t);
void mpz_gcdext (mpz_t, mpz_t, mpz_t, const mpz_t, const mpz_t);
void mpz_lcm_ui (mpz_t, const mpz_t, unsigned long);
void mpz_lcm (mpz_t, const mpz_t, const mpz_t);
int mpz_invert (mpz_t, const mpz_t, const mpz_t);

void mpz_sqrtrem (mpz_t, mpz_t, const mpz_t);
void mpz_sqrt (mpz_t, const mpz_t);
int mpz_perfect_square_p (const mpz_t);

void mpz_pow_ui (mpz_t, const mpz_t, unsigned long);
void mpz_ui_pow_ui (mpz_t, unsigned long, unsigned long);
void mpz_powm (mpz_t, const mpz_t, const mpz_t, const mpz_t);
void mpz_powm_ui (mpz_t, const mpz_t, unsigned long, const mpz_t);

void mpz_rootrem (mpz_t, mpz_t, const mpz_t, unsigned long);
int mpz_root (mpz_t, const mpz_t, unsigned long);

void mpz_fac_ui (mpz_t, unsigned long);
void mpz_2fac_ui (mpz_t, unsigned long);
void mpz_mfac_uiui (mpz_t, unsigned long, unsigned long);
void mpz_bin_uiui (mpz_t, unsigned long, unsigned long);

int mpz_probab_prime_p (const mpz_t, int);

int mpz_tstbit (const mpz_t, mp_bitcnt_t);
void mpz_setbit (mpz_t, mp_bitcnt_t);
void mpz_clrbit (mpz_t, mp_bitcnt_t);
void mpz_combit (mpz_t, mp_bitcnt_t);

void mpz_com (mpz_t, const mpz_t);
void mpz_and (mpz_t, const mpz_t, const mpz_t);
void mpz_ior (mpz_t, const mpz_t, const mpz_t);
void mpz_xor (mpz_t, const mpz_t, const mpz_t);

mp_bitcnt_t mpz_popcount (const mpz_t);
mp_bitcnt_t mpz_hamdist (const mpz_t, const mpz_t);
mp_bitcnt_t mpz_scan0 (const mpz_t, mp_bitcnt_t);
mp_bitcnt_t mpz_scan1 (const mpz_t, mp_bitcnt_t);

int mpz_fits_slong_p (const mpz_t);
int mpz_fits_ulong_p (const mpz_t);
int mpz_fits_sint_p (const mpz_t);
int mpz_fits_uint_p (const mpz_t);
int mpz_fits_sshort_p (const mpz_t);
int mpz_fits_ushort_p (const mpz_t);
long int mpz_get_si (const mpz_t);
unsigned long int mpz_get_ui (const mpz_t);
double mpz_get_d (const mpz_t);
size_t mpz_size (const mpz_t);
mp_limb_t mpz_getlimbn (const mpz_t, mp_size_t);

void mpz_realloc2 (mpz_t, mp_bitcnt_t);
mp_srcptr mpz_limbs_read (mpz_srcptr);
mp_ptr mpz_limbs_modify (mpz_t, mp_size_t);
mp_ptr mpz_limbs_write (mpz_t, mp_size_t);
void mpz_limbs_finish (mpz_t, mp_size_t);
mpz_srcptr mpz_roinit_n (mpz_t, mp_srcptr, mp_size_t);

#define MPZ_ROINIT_N(xp, xs) {{0, (xs),(xp) }}

void mpz_set_si (mpz_t, signed long int);
void mpz_set_ui (mpz_t, unsigned long int);
void mpz_set (mpz_t, const mpz_t);
void mpz_set_d (mpz_t, double);

void mpz_init_set_si (mpz_t, signed long int);
void mpz_init_set_ui (mpz_t, unsigned long int);
void mpz_init_set (mpz_t, const mpz_t);
void mpz_init_set_d (mpz_t, double);

/* This long list taken from gmp.h. */
/* For reference, "defined(EOF)" cannot be used here.  In g++ 2.95.4,
   <iostream> defines EOF but not FILE.  */
#if defined (FILE)                                              \
  || defined (H_STDIO)                                          \
  || defined (_H_STDIO)               /* AIX */                 \
  || defined (_STDIO_H)               /* glibc, Sun, SCO */     \
  || defined (_STDIO_H_)              /* BSD, OSF */            \
  || defined (__STDIO_H)              /* Borland */             \
  || defined (__STDIO_H__)            /* IRIX */                \
  || defined (_STDIO_INCLUDED)        /* HPUX */                \
  || defined (__dj_include_stdio_h_)  /* DJGPP */               \
  || defined (_FILE_DEFINED)          /* Microsoft */           \
  || defined (__STDIO__)              /* Apple MPW MrC */       \
  || defined (_MSL_STDIO_H)           /* Metrowerks */          \
  || defined (_STDIO_H_INCLUDED)      /* QNX4 */		\
  || defined (_ISO_STDIO_ISO_H)       /* Sun C++ */		\
  || defined (__STDIO_LOADED)         /* VMS */			\
  || defined (_STDIO)                 /* HPE NonStop */         \
  || defined (__DEFINED_FILE)         /* musl */
size_t mpz_out_str (FILE *, int, const mpz_t);
#endif

void mpz_import (mpz_t, size_t, int, size_t, int, size_t, const void *);
void *mpz_export (void *, size_t *, int, size_t, int, size_t, const mpz_t);

/* mini-gmp, a minimalistic implementation of a GNU GMP subset.

   Contributed to the GNU project by Niels Möller
   Additional functionalities and improvements by Marco Bodrato.

Copyright 1991-1997, 1999-2022 Free Software Foundation, Inc.

This file is part of the GNU MP Library.

The GNU MP Library is free software; you can redistribute it and/or modify
it under the terms of either:

  * the GNU Lesser General Public License as published by the Free
    Software Foundation; either version 3 of the License, or (at your
    option) any later version.

or

  * the GNU General Public License as published by the Free Software
    Foundation; either version 2 of the License, or (at your option) any
    later version.

or both in parallel, as here.

The GNU MP Library is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
for more details.

You should have received copies of the GNU General Public License and the
GNU Lesser General Public License along with the GNU MP Library.  If not,
see https://www.gnu.org/licenses/.  */

/* NOTE: All functions in this file which are not declared in
   mini-gmp.h are internal, and are not intended to be compatible
   with GMP or with future versions of mini-gmp. */

/* Much of the material copied from GMP files, including: gmp-impl.h,
   longlong.h, mpn/generic/add_n.c, mpn/generic/addmul_1.c,
   mpn/generic/lshift.c, mpn/generic/mul_1.c,
   mpn/generic/mul_basecase.c, mpn/generic/rshift.c,
   mpn/generic/sbpi1_div_qr.c, mpn/generic/sub_n.c,
   mpn/generic/submul_1.c. */


/* Macros */
#define GMP_LIMB_BITS (sizeof(mp_limb_t) * CHAR_BIT)

#define GMP_LIMB_MAX ((mp_limb_t) ~ (mp_limb_t) 0)
#define GMP_LIMB_HIGHBIT ((mp_limb_t) 1 << (GMP_LIMB_BITS - 1))

#define GMP_HLIMB_BIT ((mp_limb_t) 1 << (GMP_LIMB_BITS / 2))
#define GMP_LLIMB_MASK (GMP_HLIMB_BIT - 1)

#define GMP_ULONG_BITS (sizeof(unsigned long) * CHAR_BIT)
#define GMP_ULONG_HIGHBIT ((unsigned long) 1 << (GMP_ULONG_BITS - 1))

#define GMP_ABS(x) ((x) >= 0 ? (x) : -(x))
#define GMP_NEG_CAST(T,x) (-((T)((x) + 1) - 1))

#define GMP_MIN(a, b) ((a) < (b) ? (a) : (b))
#define GMP_MAX(a, b) ((a) > (b) ? (a) : (b))

#define GMP_CMP(a,b) (((a) > (b)) - ((a) < (b)))

#if defined(DBL_MANT_DIG) && FLT_RADIX == 2
#define GMP_DBL_MANT_BITS DBL_MANT_DIG
#else
#define GMP_DBL_MANT_BITS (53)
#endif

/* Return non-zero if xp,xsize and yp,ysize overlap.
   If xp+xsize<=yp there's no overlap, or if yp+ysize<=xp there's no
   overlap.  If both these are false, there's an overlap. */
#define GMP_MPN_OVERLAP_P(xp, xsize, yp, ysize)				\
  ((xp) + (xsize) > (yp) && (yp) + (ysize) > (xp))

#define gmp_assert_nocarry(x) do { \
    mp_limb_t __cy = (x);	   \
    assert (__cy == 0);		   \
    (void) (__cy);		   \
  } while (0)

#define gmp_clz(count, x) do {						\
    mp_limb_t __clz_x = (x);						\
    unsigned __clz_c = 0;						\
    int LOCAL_SHIFT_BITS = 8;						\
    if (GMP_LIMB_BITS > LOCAL_SHIFT_BITS)				\
      for (;								\
	   (__clz_x & ((mp_limb_t) 0xff << (GMP_LIMB_BITS - 8))) == 0;	\
	   __clz_c += 8)						\
	{ __clz_x <<= LOCAL_SHIFT_BITS;	}				\
    for (; (__clz_x & GMP_LIMB_HIGHBIT) == 0; __clz_c++)		\
      __clz_x <<= 1;							\
    (count) = __clz_c;							\
  } while (0)

#define gmp_ctz(count, x) do {						\
    mp_limb_t __ctz_x = (x);						\
    unsigned __ctz_c = 0;						\
    gmp_clz (__ctz_c, __ctz_x & - __ctz_x);				\
    (count) = GMP_LIMB_BITS - 1 - __ctz_c;				\
  } while (0)

#define gmp_add_ssaaaa(sh, sl, ah, al, bh, bl) \
  do {									\
    mp_limb_t __x;							\
    __x = (al) + (bl);							\
    (sh) = (ah) + (bh) + (__x < (al));					\
    (sl) = __x;								\
  } while (0)

#define gmp_sub_ddmmss(sh, sl, ah, al, bh, bl) \
  do {									\
    mp_limb_t __x;							\
    __x = (al) - (bl);							\
    (sh) = (ah) - (bh) - ((al) < (bl));					\
    (sl) = __x;								\
  } while (0)

#define gmp_umul_ppmm(w1, w0, u, v)					\
  do {									\
    int LOCAL_GMP_LIMB_BITS = GMP_LIMB_BITS;				\
    if (sizeof(unsigned int) * CHAR_BIT >= 2 * GMP_LIMB_BITS)		\
      {									\
	unsigned int __ww = (unsigned int) (u) * (v);			\
	w0 = (mp_limb_t) __ww;						\
	w1 = (mp_limb_t) (__ww >> LOCAL_GMP_LIMB_BITS);			\
      }									\
    else if (GMP_ULONG_BITS >= 2 * GMP_LIMB_BITS)			\
      {									\
	unsigned long int __ww = (unsigned long int) (u) * (v);		\
	w0 = (mp_limb_t) __ww;						\
	w1 = (mp_limb_t) (__ww >> LOCAL_GMP_LIMB_BITS);			\
      }									\
    else {								\
      mp_limb_t __x0, __x1, __x2, __x3;					\
      unsigned __ul, __vl, __uh, __vh;					\
      mp_limb_t __u = (u), __v = (v);					\
      assert (sizeof (unsigned) * 2 >= sizeof (mp_limb_t));		\
									\
      __ul = __u & GMP_LLIMB_MASK;					\
      __uh = __u >> (GMP_LIMB_BITS / 2);				\
      __vl = __v & GMP_LLIMB_MASK;					\
      __vh = __v >> (GMP_LIMB_BITS / 2);				\
									\
      __x0 = (mp_limb_t) __ul * __vl;					\
      __x1 = (mp_limb_t) __ul * __vh;					\
      __x2 = (mp_limb_t) __uh * __vl;					\
      __x3 = (mp_limb_t) __uh * __vh;					\
									\
      __x1 += __x0 >> (GMP_LIMB_BITS / 2);/* this can't give carry */	\
      __x1 += __x2;		/* but this indeed can */		\
      if (__x1 < __x2)		/* did we get it? */			\
	__x3 += GMP_HLIMB_BIT;	/* yes, add it in the proper pos. */	\
									\
      (w1) = __x3 + (__x1 >> (GMP_LIMB_BITS / 2));			\
      (w0) = (__x1 << (GMP_LIMB_BITS / 2)) + (__x0 & GMP_LLIMB_MASK);	\
    }									\
  } while (0)

/* If mp_limb_t is of size smaller than int, plain u*v implies
   automatic promotion to *signed* int, and then multiply may overflow
   and cause undefined behavior. Explicitly cast to unsigned int for
   that case. */
#define gmp_umullo_limb(u, v) \
  ((sizeof(mp_limb_t) >= sizeof(int)) ? (u)*(v) : (unsigned int)(u) * (v))

#define gmp_udiv_qrnnd_preinv(q, r, nh, nl, d, di)			\
  do {									\
    mp_limb_t _qh, _ql, _r, _mask;					\
    gmp_umul_ppmm (_qh, _ql, (nh), (di));				\
    gmp_add_ssaaaa (_qh, _ql, _qh, _ql, (nh) + 1, (nl));		\
    _r = (nl) - gmp_umullo_limb (_qh, (d));				\
    _mask = -(mp_limb_t) (_r > _ql); /* both > and >= are OK */		\
    _qh += _mask;							\
    _r += _mask & (d);							\
    if (_r >= (d))							\
      {									\
	_r -= (d);							\
	_qh++;								\
      }									\
									\
    (r) = _r;								\
    (q) = _qh;								\
  } while (0)

#define gmp_udiv_qr_3by2(q, r1, r0, n2, n1, n0, d1, d0, dinv)		\
  do {									\
    mp_limb_t _q0, _t1, _t0, _mask;					\
    gmp_umul_ppmm ((q), _q0, (n2), (dinv));				\
    gmp_add_ssaaaa ((q), _q0, (q), _q0, (n2), (n1));			\
									\
    /* Compute the two most significant limbs of n - q'd */		\
    (r1) = (n1) - gmp_umullo_limb ((d1), (q));				\
    gmp_sub_ddmmss ((r1), (r0), (r1), (n0), (d1), (d0));		\
    gmp_umul_ppmm (_t1, _t0, (d0), (q));				\
    gmp_sub_ddmmss ((r1), (r0), (r1), (r0), _t1, _t0);			\
    (q)++;								\
									\
    /* Conditionally adjust q and the remainders */			\
    _mask = - (mp_limb_t) ((r1) >= _q0);				\
    (q) += _mask;							\
    gmp_add_ssaaaa ((r1), (r0), (r1), (r0), _mask & (d1), _mask & (d0)); \
    if ((r1) >= (d1))							\
      {									\
	if ((r1) > (d1) || (r0) >= (d0))				\
	  {								\
	    (q)++;							\
	    gmp_sub_ddmmss ((r1), (r0), (r1), (r0), (d1), (d0));	\
	  }								\
      }									\
  } while (0)

/* Swap macros. */
#define MP_LIMB_T_SWAP(x, y)						\
  do {									\
    mp_limb_t __mp_limb_t_swap__tmp = (x);				\
    (x) = (y);								\
    (y) = __mp_limb_t_swap__tmp;					\
  } while (0)
#define MP_SIZE_T_SWAP(x, y)						\
  do {									\
    mp_size_t __mp_size_t_swap__tmp = (x);				\
    (x) = (y);								\
    (y) = __mp_size_t_swap__tmp;					\
  } while (0)
#define MP_BITCNT_T_SWAP(x,y)			\
  do {						\
    mp_bitcnt_t __mp_bitcnt_t_swap__tmp = (x);	\
    (x) = (y);					\
    (y) = __mp_bitcnt_t_swap__tmp;		\
  } while (0)
#define MP_PTR_SWAP(x, y)						\
  do {									\
    mp_ptr __mp_ptr_swap__tmp = (x);					\
    (x) = (y);								\
    (y) = __mp_ptr_swap__tmp;						\
  } while (0)
#define MP_SRCPTR_SWAP(x, y)						\
  do {									\
    mp_srcptr __mp_srcptr_swap__tmp = (x);				\
    (x) = (y);								\
    (y) = __mp_srcptr_swap__tmp;					\
  } while (0)

#define MPN_PTR_SWAP(xp,xs, yp,ys)					\
  do {									\
    MP_PTR_SWAP (xp, yp);						\
    MP_SIZE_T_SWAP (xs, ys);						\
  } while(0)
#define MPN_SRCPTR_SWAP(xp,xs, yp,ys)					\
  do {									\
    MP_SRCPTR_SWAP (xp, yp);						\
    MP_SIZE_T_SWAP (xs, ys);						\
  } while(0)

#define MPZ_PTR_SWAP(x, y)						\
  do {									\
    mpz_ptr __mpz_ptr_swap__tmp = (x);					\
    (x) = (y);								\
    (y) = __mpz_ptr_swap__tmp;						\
  } while (0)
#define MPZ_SRCPTR_SWAP(x, y)						\
  do {									\
    mpz_srcptr __mpz_srcptr_swap__tmp = (x);				\
    (x) = (y);								\
    (y) = __mpz_srcptr_swap__tmp;					\
  } while (0)


/* Memory allocation and other helper functions. */
void
gmp_die (const char *msg)
{
  fprintf (stderr, "%s\n", msg);
  abort();
}

void *
gmp_default_alloc (size_t size)
{
  void *p;

  assert (size > 0);

  p = malloc (size);
  if (!p)
    gmp_die("gmp_default_alloc: Virtual memory exhausted.");

  return p;
}

void *
gmp_default_realloc (void *old, size_t unused_old_size, size_t new_size)
{
  void * p;

  p = realloc (old, new_size);

  if (!p)
    gmp_die("gmp_default_realloc: Virtual memory exhausted.");

  return p;
}

void
gmp_default_free (void *p, size_t unused_size)
{
  free (p);
}

static void * (*gmp_allocate_func) (size_t) = gmp_default_alloc;
static void * (*gmp_reallocate_func) (void *, size_t, size_t) = gmp_default_realloc;
static void (*gmp_free_func) (void *, size_t) = gmp_default_free;

#define gmp_alloc(size) ((*gmp_allocate_func)((size)))
#define gmp_free(p, size) ((*gmp_free_func) ((p), (size)))
#define gmp_realloc(ptr, old_size, size) ((*gmp_reallocate_func)(ptr, old_size, size))

mp_ptr
gmp_alloc_limbs (mp_size_t size)
{
  return (mp_ptr) gmp_alloc (size * sizeof (mp_limb_t));
}

mp_ptr
gmp_realloc_limbs (mp_ptr old, mp_size_t old_size, mp_size_t size)
{
  assert (size > 0);
  return (mp_ptr) gmp_realloc (old, old_size * sizeof (mp_limb_t), size * sizeof (mp_limb_t));
}

void
gmp_free_limbs (mp_ptr old, mp_size_t size)
{
  gmp_free (old, size * sizeof (mp_limb_t));
}


/* MPN interface */

void
mpn_copyi (mp_ptr d, mp_srcptr s, mp_size_t n)
{
  mp_size_t i;
  for (i = 0; i < n; i++)
    d[i] = s[i];
}

void
mpn_copyd (mp_ptr d, mp_srcptr s, mp_size_t n)
{
  while (--n >= 0)
    d[n] = s[n];
}

int
mpn_cmp (mp_srcptr ap, mp_srcptr bp, mp_size_t n)
{
  while (--n >= 0)
    {
      if (ap[n] != bp[n])
	return ap[n] > bp[n] ? 1 : -1;
    }
  return 0;
}

int
mpn_cmp4 (mp_srcptr ap, mp_size_t an, mp_srcptr bp, mp_size_t bn)
{
  if (an != bn)
    return an < bn ? -1 : 1;
  else
    return mpn_cmp (ap, bp, an);
}

mp_size_t
mpn_normalized_size (mp_srcptr xp, mp_size_t n)
{
  while (n > 0 && xp[n-1] == 0)
    --n;
  return n;
}

int
mpn_zero_p(mp_srcptr rp, mp_size_t n)
{
  return mpn_normalized_size (rp, n) == 0;
}

void
mpn_zero (mp_ptr rp, mp_size_t n)
{
  while (--n >= 0)
    rp[n] = 0;
}

mp_limb_t
mpn_add_1 (mp_ptr rp, mp_srcptr ap, mp_size_t n, mp_limb_t b)
{
  mp_size_t i;

  assert (n > 0);
  i = 0;
  do
    {
      mp_limb_t r = ap[i] + b;
      /* Carry out */
      b = (r < b);
      rp[i] = r;
    }
  while (++i < n);

  return b;
}

mp_limb_t
mpn_add_n (mp_ptr rp, mp_srcptr ap, mp_srcptr bp, mp_size_t n)
{
  mp_size_t i;
  mp_limb_t cy;

  for (i = 0, cy = 0; i < n; i++)
    {
      mp_limb_t a, b, r;
      a = ap[i]; b = bp[i];
      r = a + cy;
      cy = (r < cy);
      r += b;
      cy += (r < b);
      rp[i] = r;
    }
  return cy;
}

mp_limb_t
mpn_add (mp_ptr rp, mp_srcptr ap, mp_size_t an, mp_srcptr bp, mp_size_t bn)
{
  mp_limb_t cy;

  assert (an >= bn);

  cy = mpn_add_n (rp, ap, bp, bn);
  if (an > bn)
    cy = mpn_add_1 (rp + bn, ap + bn, an - bn, cy);
  return cy;
}

mp_limb_t
mpn_sub_1 (mp_ptr rp, mp_srcptr ap, mp_size_t n, mp_limb_t b)
{
  mp_size_t i;

  assert (n > 0);

  i = 0;
  do
    {
      mp_limb_t a = ap[i];
      /* Carry out */
      mp_limb_t cy = a < b;
      rp[i] = a - b;
      b = cy;
    }
  while (++i < n);

  return b;
}

mp_limb_t
mpn_sub_n (mp_ptr rp, mp_srcptr ap, mp_srcptr bp, mp_size_t n)
{
  mp_size_t i;
  mp_limb_t cy;

  for (i = 0, cy = 0; i < n; i++)
    {
      mp_limb_t a, b;
      a = ap[i]; b = bp[i];
      b += cy;
      cy = (b < cy);
      cy += (a < b);
      rp[i] = a - b;
    }
  return cy;
}

mp_limb_t
mpn_sub (mp_ptr rp, mp_srcptr ap, mp_size_t an, mp_srcptr bp, mp_size_t bn)
{
  mp_limb_t cy;

  assert (an >= bn);

  cy = mpn_sub_n (rp, ap, bp, bn);
  if (an > bn)
    cy = mpn_sub_1 (rp + bn, ap + bn, an - bn, cy);
  return cy;
}

mp_limb_t
mpn_mul_1 (mp_ptr rp, mp_srcptr up, mp_size_t n, mp_limb_t vl)
{
  mp_limb_t ul, cl, hpl, lpl;

  assert (n >= 1);

  cl = 0;
  do
    {
      ul = *up++;
      gmp_umul_ppmm (hpl, lpl, ul, vl);

      lpl += cl;
      cl = (lpl < cl) + hpl;

      *rp++ = lpl;
    }
  while (--n != 0);

  return cl;
}

mp_limb_t
mpn_addmul_1 (mp_ptr rp, mp_srcptr up, mp_size_t n, mp_limb_t vl)
{
  mp_limb_t ul, cl, hpl, lpl, rl;

  assert (n >= 1);

  cl = 0;
  do
    {
      ul = *up++;
      gmp_umul_ppmm (hpl, lpl, ul, vl);

      lpl += cl;
      cl = (lpl < cl) + hpl;

      rl = *rp;
      lpl = rl + lpl;
      cl += lpl < rl;
      *rp++ = lpl;
    }
  while (--n != 0);

  return cl;
}

mp_limb_t
mpn_submul_1 (mp_ptr rp, mp_srcptr up, mp_size_t n, mp_limb_t vl)
{
  mp_limb_t ul, cl, hpl, lpl, rl;

  assert (n >= 1);

  cl = 0;
  do
    {
      ul = *up++;
      gmp_umul_ppmm (hpl, lpl, ul, vl);

      lpl += cl;
      cl = (lpl < cl) + hpl;

      rl = *rp;
      lpl = rl - lpl;
      cl += lpl > rl;
      *rp++ = lpl;
    }
  while (--n != 0);

  return cl;
}

mp_limb_t
mpn_mul (mp_ptr rp, mp_srcptr up, mp_size_t un, mp_srcptr vp, mp_size_t vn)
{
  assert (un >= vn);
  assert (vn >= 1);
  assert (!GMP_MPN_OVERLAP_P(rp, un + vn, up, un));
  assert (!GMP_MPN_OVERLAP_P(rp, un + vn, vp, vn));

  /* We first multiply by the low order limb. This result can be
     stored, not added, to rp. We also avoid a loop for zeroing this
     way. */

  rp[un] = mpn_mul_1 (rp, up, un, vp[0]);

  /* Now accumulate the product of up[] and the next higher limb from
     vp[]. */

  while (--vn >= 1)
    {
      rp += 1, vp += 1;
      rp[un] = mpn_addmul_1 (rp, up, un, vp[0]);
    }
  return rp[un];
}

void
mpn_mul_n (mp_ptr rp, mp_srcptr ap, mp_srcptr bp, mp_size_t n)
{
  mpn_mul (rp, ap, n, bp, n);
}

void
mpn_sqr (mp_ptr rp, mp_srcptr ap, mp_size_t n)
{
  mpn_mul (rp, ap, n, ap, n);
}

mp_limb_t
mpn_lshift (mp_ptr rp, mp_srcptr up, mp_size_t n, unsigned int cnt)
{
  mp_limb_t high_limb, low_limb;
  unsigned int tnc;
  mp_limb_t retval;

  assert (n >= 1);
  assert (cnt >= 1);
  assert (cnt < GMP_LIMB_BITS);

  up += n;
  rp += n;

  tnc = GMP_LIMB_BITS - cnt;
  low_limb = *--up;
  retval = low_limb >> tnc;
  high_limb = (low_limb << cnt);

  while (--n != 0)
    {
      low_limb = *--up;
      *--rp = high_limb | (low_limb >> tnc);
      high_limb = (low_limb << cnt);
    }
  *--rp = high_limb;

  return retval;
}

mp_limb_t
mpn_rshift (mp_ptr rp, mp_srcptr up, mp_size_t n, unsigned int cnt)
{
  mp_limb_t high_limb, low_limb;
  unsigned int tnc;
  mp_limb_t retval;

  assert (n >= 1);
  assert (cnt >= 1);
  assert (cnt < GMP_LIMB_BITS);

  tnc = GMP_LIMB_BITS - cnt;
  high_limb = *up++;
  retval = (high_limb << tnc);
  low_limb = high_limb >> cnt;

  while (--n != 0)
    {
      high_limb = *up++;
      *rp++ = low_limb | (high_limb << tnc);
      low_limb = high_limb >> cnt;
    }
  *rp = low_limb;

  return retval;
}

mp_bitcnt_t
mpn_common_scan (mp_limb_t limb, mp_size_t i, mp_srcptr up, mp_size_t un,
		 mp_limb_t ux)
{
  unsigned cnt;

  assert (ux == 0 || ux == GMP_LIMB_MAX);
  assert (0 <= i && i <= un );

  while (limb == 0)
    {
      i++;
      if (i == un)
	return (ux == 0 ? ~(mp_bitcnt_t) 0 : un * GMP_LIMB_BITS);
      limb = ux ^ up[i];
    }
  gmp_ctz (cnt, limb);
  return (mp_bitcnt_t) i * GMP_LIMB_BITS + cnt;
}

mp_bitcnt_t
mpn_scan1 (mp_srcptr ptr, mp_bitcnt_t bit)
{
  mp_size_t i;
  i = bit / GMP_LIMB_BITS;

  return mpn_common_scan ( ptr[i] & (GMP_LIMB_MAX << (bit % GMP_LIMB_BITS)),
			  i, ptr, i, 0);
}

mp_bitcnt_t
mpn_scan0 (mp_srcptr ptr, mp_bitcnt_t bit)
{
  mp_size_t i;
  i = bit / GMP_LIMB_BITS;

  return mpn_common_scan (~ptr[i] & (GMP_LIMB_MAX << (bit % GMP_LIMB_BITS)),
			  i, ptr, i, GMP_LIMB_MAX);
}

void
mpn_com (mp_ptr rp, mp_srcptr up, mp_size_t n)
{
  while (--n >= 0)
    *rp++ = ~ *up++;
}

mp_limb_t
mpn_neg (mp_ptr rp, mp_srcptr up, mp_size_t n)
{
  while (*up == 0)
    {
      *rp = 0;
      if (!--n)
	return 0;
      ++up; ++rp;
    }
  *rp = - *up;
  mpn_com (++rp, ++up, --n);
  return 1;
}


/* MPN division interface. */

/* The 3/2 inverse is defined as

     m = floor( (B^3-1) / (B u1 + u0)) - B
*/
mp_limb_t
mpn_invert_3by2 (mp_limb_t u1, mp_limb_t u0)
{
  mp_limb_t r, m;

  {
    mp_limb_t p, ql;
    unsigned ul, uh, qh;

    assert (sizeof (unsigned) * 2 >= sizeof (mp_limb_t));
    /* For notation, let b denote the half-limb base, so that B = b^2.
       Split u1 = b uh + ul. */
    ul = u1 & GMP_LLIMB_MASK;
    uh = u1 >> (GMP_LIMB_BITS / 2);

    /* Approximation of the high half of quotient. Differs from the 2/1
       inverse of the half limb uh, since we have already subtracted
       u0. */
    qh = (u1 ^ GMP_LIMB_MAX) / uh;

    /* Adjust to get a half-limb 3/2 inverse, i.e., we want

       qh' = floor( (b^3 - 1) / u) - b = floor ((b^3 - b u - 1) / u
	   = floor( (b (~u) + b-1) / u),

       and the remainder

       r = b (~u) + b-1 - qh (b uh + ul)
       = b (~u - qh uh) + b-1 - qh ul

       Subtraction of qh ul may underflow, which implies adjustments.
       But by normalization, 2 u >= B > qh ul, so we need to adjust by
       at most 2.
    */

    r = ((~u1 - (mp_limb_t) qh * uh) << (GMP_LIMB_BITS / 2)) | GMP_LLIMB_MASK;

    p = (mp_limb_t) qh * ul;
    /* Adjustment steps taken from udiv_qrnnd_c */
    if (r < p)
      {
	qh--;
	r += u1;
	if (r >= u1) /* i.e. we didn't get carry when adding to r */
	  if (r < p)
	    {
	      qh--;
	      r += u1;
	    }
      }
    r -= p;

    /* Low half of the quotient is

       ql = floor ( (b r + b-1) / u1).

       This is a 3/2 division (on half-limbs), for which qh is a
       suitable inverse. */

    p = (r >> (GMP_LIMB_BITS / 2)) * qh + r;
    /* Unlike full-limb 3/2, we can add 1 without overflow. For this to
       work, it is essential that ql is a full mp_limb_t. */
    ql = (p >> (GMP_LIMB_BITS / 2)) + 1;

    /* By the 3/2 trick, we don't need the high half limb. */
    r = (r << (GMP_LIMB_BITS / 2)) + GMP_LLIMB_MASK - ql * u1;

    if (r >= (GMP_LIMB_MAX & (p << (GMP_LIMB_BITS / 2))))
      {
	ql--;
	r += u1;
      }
    m = ((mp_limb_t) qh << (GMP_LIMB_BITS / 2)) + ql;
    if (r >= u1)
      {
	m++;
	r -= u1;
      }
  }

  /* Now m is the 2/1 inverse of u1. If u0 > 0, adjust it to become a
     3/2 inverse. */
  if (u0 > 0)
    {
      mp_limb_t th, tl;
      r = ~r;
      r += u0;
      if (r < u0)
	{
	  m--;
	  if (r >= u1)
	    {
	      m--;
	      r -= u1;
	    }
	  r -= u1;
	}
      gmp_umul_ppmm (th, tl, u0, m);
      r += th;
      if (r < th)
	{
	  m--;
	  m -= ((r > u1) | ((r == u1) & (tl > u0)));
	}
    }

  return m;
}

struct gmp_div_inverse
{
  /* Normalization shift count. */
  unsigned shift;
  /* Normalized divisor (d0 unused for mpn_div_qr_1) */
  mp_limb_t d1, d0;
  /* Inverse, for 2/1 or 3/2. */
  mp_limb_t di;
};

void
mpn_div_qr_1_invert (struct gmp_div_inverse *inv, mp_limb_t d)
{
  unsigned shift;

  assert (d > 0);
  gmp_clz (shift, d);
  inv->shift = shift;
  inv->d1 = d << shift;
  inv->di = mpn_invert_limb (inv->d1);
}

void
mpn_div_qr_2_invert (struct gmp_div_inverse *inv,
		     mp_limb_t d1, mp_limb_t d0)
{
  unsigned shift;

  assert (d1 > 0);
  gmp_clz (shift, d1);
  inv->shift = shift;
  if (shift > 0)
    {
      d1 = (d1 << shift) | (d0 >> (GMP_LIMB_BITS - shift));
      d0 <<= shift;
    }
  inv->d1 = d1;
  inv->d0 = d0;
  inv->di = mpn_invert_3by2 (d1, d0);
}

void
mpn_div_qr_invert (struct gmp_div_inverse *inv,
		   mp_srcptr dp, mp_size_t dn)
{
  assert (dn > 0);

  if (dn == 1)
    mpn_div_qr_1_invert (inv, dp[0]);
  else if (dn == 2)
    mpn_div_qr_2_invert (inv, dp[1], dp[0]);
  else
    {
      unsigned shift;
      mp_limb_t d1, d0;

      d1 = dp[dn-1];
      d0 = dp[dn-2];
      assert (d1 > 0);
      gmp_clz (shift, d1);
      inv->shift = shift;
      if (shift > 0)
	{
	  d1 = (d1 << shift) | (d0 >> (GMP_LIMB_BITS - shift));
	  d0 = (d0 << shift) | (dp[dn-3] >> (GMP_LIMB_BITS - shift));
	}
      inv->d1 = d1;
      inv->d0 = d0;
      inv->di = mpn_invert_3by2 (d1, d0);
    }
}

/* Not matching current public gmp interface, rather corresponding to
   the sbpi1_div_* functions. */
mp_limb_t
mpn_div_qr_1_preinv (mp_ptr qp, mp_srcptr np, mp_size_t nn,
		     const struct gmp_div_inverse *inv)
{
  mp_limb_t d, di;
  mp_limb_t r;
  mp_ptr tp = NULL;
  mp_size_t tn = 0;

  if (inv->shift > 0)
    {
      /* Shift, reusing qp area if possible. In-place shift if qp == np. */
      tp = qp;
      if (!tp)
        {
	   tn = nn;
	   tp = gmp_alloc_limbs (tn);
        }
      r = mpn_lshift (tp, np, nn, inv->shift);
      np = tp;
    }
  else
    r = 0;

  d = inv->d1;
  di = inv->di;
  while (--nn >= 0)
    {
      mp_limb_t q;

      gmp_udiv_qrnnd_preinv (q, r, r, np[nn], d, di);
      if (qp)
	qp[nn] = q;
    }
  if (tn)
    gmp_free_limbs (tp, tn);

  return r >> inv->shift;
}

void
mpn_div_qr_2_preinv (mp_ptr qp, mp_ptr np, mp_size_t nn,
		     const struct gmp_div_inverse *inv)
{
  unsigned shift;
  mp_size_t i;
  mp_limb_t d1, d0, di, r1, r0;

  assert (nn >= 2);
  shift = inv->shift;
  d1 = inv->d1;
  d0 = inv->d0;
  di = inv->di;

  if (shift > 0)
    r1 = mpn_lshift (np, np, nn, shift);
  else
    r1 = 0;

  r0 = np[nn - 1];

  i = nn - 2;
  do
    {
      mp_limb_t n0, q;
      n0 = np[i];
      gmp_udiv_qr_3by2 (q, r1, r0, r1, r0, n0, d1, d0, di);

      if (qp)
	qp[i] = q;
    }
  while (--i >= 0);

  if (shift > 0)
    {
      assert ((r0 & (GMP_LIMB_MAX >> (GMP_LIMB_BITS - shift))) == 0);
      r0 = (r0 >> shift) | (r1 << (GMP_LIMB_BITS - shift));
      r1 >>= shift;
    }

  np[1] = r1;
  np[0] = r0;
}

void
mpn_div_qr_pi1 (mp_ptr qp,
		mp_ptr np, mp_size_t nn, mp_limb_t n1,
		mp_srcptr dp, mp_size_t dn,
		mp_limb_t dinv)
{
  mp_size_t i;

  mp_limb_t d1, d0;
  mp_limb_t cy, cy1;
  mp_limb_t q;

  assert (dn > 2);
  assert (nn >= dn);

  d1 = dp[dn - 1];
  d0 = dp[dn - 2];

  assert ((d1 & GMP_LIMB_HIGHBIT) != 0);
  /* Iteration variable is the index of the q limb.
   *
   * We divide <n1, np[dn-1+i], np[dn-2+i], np[dn-3+i],..., np[i]>
   * by            <d1,          d0,        dp[dn-3],  ..., dp[0] >
   */

  i = nn - dn;
  do
    {
      mp_limb_t n0 = np[dn-1+i];

      if (n1 == d1 && n0 == d0)
	{
	  q = GMP_LIMB_MAX;
	  mpn_submul_1 (np+i, dp, dn, q);
	  n1 = np[dn-1+i];	/* update n1, last loop's value will now be invalid */
	}
      else
	{
	  gmp_udiv_qr_3by2 (q, n1, n0, n1, n0, np[dn-2+i], d1, d0, dinv);

	  cy = mpn_submul_1 (np + i, dp, dn-2, q);

	  cy1 = n0 < cy;
	  n0 = n0 - cy;
	  cy = n1 < cy1;
	  n1 = n1 - cy1;
	  np[dn-2+i] = n0;

	  if (cy != 0)
	    {
	      n1 += d1 + mpn_add_n (np + i, np + i, dp, dn - 1);
	      q--;
	    }
	}

      if (qp)
	qp[i] = q;
    }
  while (--i >= 0);

  np[dn - 1] = n1;
}

void
mpn_div_qr_preinv (mp_ptr qp, mp_ptr np, mp_size_t nn,
		   mp_srcptr dp, mp_size_t dn,
		   const struct gmp_div_inverse *inv)
{
  assert (dn > 0);
  assert (nn >= dn);

  if (dn == 1)
    np[0] = mpn_div_qr_1_preinv (qp, np, nn, inv);
  else if (dn == 2)
    mpn_div_qr_2_preinv (qp, np, nn, inv);
  else
    {
      mp_limb_t nh;
      unsigned shift;

      assert (inv->d1 == dp[dn-1]);
      assert (inv->d0 == dp[dn-2]);
      assert ((inv->d1 & GMP_LIMB_HIGHBIT) != 0);

      shift = inv->shift;
      if (shift > 0)
	nh = mpn_lshift (np, np, nn, shift);
      else
	nh = 0;

      mpn_div_qr_pi1 (qp, np, nn, nh, dp, dn, inv->di);

      if (shift > 0)
	gmp_assert_nocarry (mpn_rshift (np, np, dn, shift));
    }
}

void
mpn_div_qr (mp_ptr qp, mp_ptr np, mp_size_t nn, mp_srcptr dp, mp_size_t dn)
{
  struct gmp_div_inverse inv;
  mp_ptr tp = NULL;

  assert (dn > 0);
  assert (nn >= dn);

  mpn_div_qr_invert (&inv, dp, dn);
  if (dn > 2 && inv.shift > 0)
    {
      tp = gmp_alloc_limbs (dn);
      gmp_assert_nocarry (mpn_lshift (tp, dp, dn, inv.shift));
      dp = tp;
    }
  mpn_div_qr_preinv (qp, np, nn, dp, dn, &inv);
  if (tp)
    gmp_free_limbs (tp, dn);
}




/* MPZ interface */
void
mpz_init (mpz_t r)
{
  static const mp_limb_t dummy_limb = GMP_LIMB_MAX & 0xc1a0;

  r->_mp_alloc = 0;
  r->_mp_size = 0;
  r->_mp_d = (mp_ptr) &dummy_limb;
}

/* The utility of this function is a bit limited, since many functions
   assigns the result variable using mpz_swap. */
void
mpz_init2 (mpz_t r, mp_bitcnt_t bits)
{
  mp_size_t rn;

  bits -= (bits != 0);		/* Round down, except if 0 */
  rn = 1 + bits / GMP_LIMB_BITS;

  r->_mp_alloc = rn;
  r->_mp_size = 0;
  r->_mp_d = gmp_alloc_limbs (rn);
}

void
mpz_clear (mpz_t r)
{
  if (r->_mp_alloc)
    gmp_free_limbs (r->_mp_d, r->_mp_alloc);
}

mp_ptr
mpz_realloc (mpz_t r, mp_size_t size)
{
  size = GMP_MAX (size, 1);

  if (r->_mp_alloc)
    r->_mp_d = gmp_realloc_limbs (r->_mp_d, r->_mp_alloc, size);
  else
    r->_mp_d = gmp_alloc_limbs (size);
  r->_mp_alloc = size;

  if (GMP_ABS (r->_mp_size) > size)
    r->_mp_size = 0;

  return r->_mp_d;
}

/* Realloc for an mpz_t WHAT if it has less than NEEDED limbs.  */
#define MPZ_REALLOC(z,n) ((n) > (z)->_mp_alloc			\
			  ? mpz_realloc(z,n)			\
			  : (z)->_mp_d)

void
mpz_set_si (mpz_t r, signed long int x)
{
  if (x >= 0)
    mpz_set_ui (r, x);
  else /* (x < 0) */
    if (GMP_LIMB_BITS < GMP_ULONG_BITS)
      {
	mpz_set_ui (r, GMP_NEG_CAST (unsigned long int, x));
	mpz_neg (r, r);
      }
  else
    {
      r->_mp_size = -1;
      MPZ_REALLOC (r, 1)[0] = GMP_NEG_CAST (unsigned long int, x);
    }
}

void
mpz_set_ui (mpz_t r, unsigned long int x)
{
  if (x > 0)
    {
      r->_mp_size = 1;
      MPZ_REALLOC (r, 1)[0] = x;
      if (GMP_LIMB_BITS < GMP_ULONG_BITS)
	{
	  int LOCAL_GMP_LIMB_BITS = GMP_LIMB_BITS;
	  while (x >>= LOCAL_GMP_LIMB_BITS)
	    {
	      ++ r->_mp_size;
	      MPZ_REALLOC (r, r->_mp_size)[r->_mp_size - 1] = x;
	    }
	}
    }
  else
    r->_mp_size = 0;
}

void
mpz_set (mpz_t r, const mpz_t x)
{
  /* Allow the NOP r == x */
  if (r != x)
    {
      mp_size_t n;
      mp_ptr rp;

      n = GMP_ABS (x->_mp_size);
      rp = MPZ_REALLOC (r, n);

      mpn_copyi (rp, x->_mp_d, n);
      r->_mp_size = x->_mp_size;
    }
}

void
mpz_init_set_ui (mpz_t r, unsigned long int x)
{
  mpz_init (r);
  mpz_set_ui (r, x);
}

void
mpz_init_set (mpz_t r, const mpz_t x)
{
  mpz_init (r);
  mpz_set (r, x);
}

unsigned long int
mpz_get_ui (const mpz_t u)
{
  if (GMP_LIMB_BITS < GMP_ULONG_BITS)
    {
      int LOCAL_GMP_LIMB_BITS = GMP_LIMB_BITS;
      unsigned long r = 0;
      mp_size_t n = GMP_ABS (u->_mp_size);
      n = GMP_MIN (n, 1 + (mp_size_t) (GMP_ULONG_BITS - 1) / GMP_LIMB_BITS);
      while (--n >= 0)
	r = (r << LOCAL_GMP_LIMB_BITS) + u->_mp_d[n];
      return r;
    }

  return u->_mp_size == 0 ? 0 : u->_mp_d[0];
}

void
mpz_abs (mpz_t r, const mpz_t u)
{
  mpz_set (r, u);
  r->_mp_size = GMP_ABS (r->_mp_size);
}

void
mpz_neg (mpz_t r, const mpz_t u)
{
  mpz_set (r, u);
  r->_mp_size = -r->_mp_size;
}

void
mpz_swap (mpz_t u, mpz_t v)
{
  MP_SIZE_T_SWAP (u->_mp_alloc, v->_mp_alloc);
  MPN_PTR_SWAP (u->_mp_d, u->_mp_size, v->_mp_d, v->_mp_size);
}


/* MPZ addition and subtraction */


void
mpz_add_ui (mpz_t r, const mpz_t a, unsigned long b)
{
  mpz_t bb;
  mpz_init_set_ui (bb, b);
  mpz_add (r, a, bb);
  mpz_clear (bb);
}

void
mpz_sub_ui (mpz_t r, const mpz_t a, unsigned long b)
{
  mpz_ui_sub (r, b, a);
  mpz_neg (r, r);
}

void
mpz_ui_sub (mpz_t r, unsigned long a, const mpz_t b)
{
  mpz_neg (r, b);
  mpz_add_ui (r, r, a);
}

mp_size_t
mpz_abs_add (mpz_t r, const mpz_t a, const mpz_t b)
{
  mp_size_t an = GMP_ABS (a->_mp_size);
  mp_size_t bn = GMP_ABS (b->_mp_size);
  mp_ptr rp;
  mp_limb_t cy;

  if (an < bn)
    {
      MPZ_SRCPTR_SWAP (a, b);
      MP_SIZE_T_SWAP (an, bn);
    }

  rp = MPZ_REALLOC (r, an + 1);
  cy = mpn_add (rp, a->_mp_d, an, b->_mp_d, bn);

  rp[an] = cy;

  return an + cy;
}

mp_size_t
mpz_abs_sub (mpz_t r, const mpz_t a, const mpz_t b)
{
  mp_size_t an = GMP_ABS (a->_mp_size);
  mp_size_t bn = GMP_ABS (b->_mp_size);
  int cmp;
  mp_ptr rp;

  cmp = mpn_cmp4 (a->_mp_d, an, b->_mp_d, bn);
  if (cmp > 0)
    {
      rp = MPZ_REALLOC (r, an);
      gmp_assert_nocarry (mpn_sub (rp, a->_mp_d, an, b->_mp_d, bn));
      return mpn_normalized_size (rp, an);
    }
  else if (cmp < 0)
    {
      rp = MPZ_REALLOC (r, bn);
      gmp_assert_nocarry (mpn_sub (rp, b->_mp_d, bn, a->_mp_d, an));
      return -mpn_normalized_size (rp, bn);
    }
  else
    return 0;
}

void
mpz_add (mpz_t r, const mpz_t a, const mpz_t b)
{
  mp_size_t rn;

  if ( (a->_mp_size ^ b->_mp_size) >= 0)
    rn = mpz_abs_add (r, a, b);
  else
    rn = mpz_abs_sub (r, a, b);

  r->_mp_size = a->_mp_size >= 0 ? rn : - rn;
}

void
mpz_sub (mpz_t r, const mpz_t a, const mpz_t b)
{
  mp_size_t rn;

  if ( (a->_mp_size ^ b->_mp_size) >= 0)
    rn = mpz_abs_sub (r, a, b);
  else
    rn = mpz_abs_add (r, a, b);

  r->_mp_size = a->_mp_size >= 0 ? rn : - rn;
}

void
mpz_mul_ui (mpz_t r, const mpz_t u, unsigned long int v)
{
  mpz_t vv;
  mpz_init_set_ui (vv, v);
  mpz_mul (r, u, vv);
  mpz_clear (vv);
  return;
}

void
mpz_mul (mpz_t r, const mpz_t u, const mpz_t v)
{
  int sign;
  mp_size_t un, vn, rn;
  mpz_t t;
  mp_ptr tp;

  un = u->_mp_size;
  vn = v->_mp_size;

  if (un == 0 || vn == 0)
    {
      r->_mp_size = 0;
      return;
    }

  sign = (un ^ vn) < 0;

  un = GMP_ABS (un);
  vn = GMP_ABS (vn);

  mpz_init2 (t, (un + vn) * GMP_LIMB_BITS);

  tp = t->_mp_d;
  if (un >= vn)
    mpn_mul (tp, u->_mp_d, un, v->_mp_d, vn);
  else
    mpn_mul (tp, v->_mp_d, vn, u->_mp_d, un);

  rn = un + vn;
  rn -= tp[rn-1] == 0;

  t->_mp_size = sign ? - rn : rn;
  mpz_swap (r, t);
  mpz_clear (t);
}

void
mpz_addmul_ui (mpz_t r, const mpz_t u, unsigned long int v)
{
  mpz_t t;
  mpz_init_set_ui (t, v);
  mpz_mul (t, u, t);
  mpz_add (r, r, t);
  mpz_clear (t);
}

void
mpz_submul_ui (mpz_t r, const mpz_t u, unsigned long int v)
{
  mpz_t t;
  mpz_init_set_ui (t, v);
  mpz_mul (t, u, t);
  mpz_sub (r, r, t);
  mpz_clear (t);
}

void
mpz_addmul (mpz_t r, const mpz_t u, const mpz_t v)
{
  mpz_t t;
  mpz_init (t);
  mpz_mul (t, u, v);
  mpz_add (r, r, t);
  mpz_clear (t);
}

void
mpz_submul (mpz_t r, const mpz_t u, const mpz_t v)
{
  mpz_t t;
  mpz_init (t);
  mpz_mul (t, u, v);
  mpz_sub (r, r, t);
  mpz_clear (t);
}


/* MPZ division */
enum mpz_div_round_mode { GMP_DIV_FLOOR, GMP_DIV_CEIL, GMP_DIV_TRUNC };

/* Allows q or r to be zero. Returns 1 iff remainder is non-zero. */
int
mpz_div_qr (mpz_t q, mpz_t r,
	    const mpz_t n, const mpz_t d, enum mpz_div_round_mode mode)
{
  mp_size_t ns, ds, nn, dn, qs;
  ns = n->_mp_size;
  ds = d->_mp_size;

  if (ds == 0)
    gmp_die("mpz_div_qr: Divide by zero.");

  if (ns == 0)
    {
      if (q)
	q->_mp_size = 0;
      if (r)
	r->_mp_size = 0;
      return 0;
    }

  nn = GMP_ABS (ns);
  dn = GMP_ABS (ds);

  qs = ds ^ ns;

  if (nn < dn)
    {
      if (mode == GMP_DIV_CEIL && qs >= 0)
	{
	  /* q = 1, r = n - d */
	  if (r)
	    mpz_sub (r, n, d);
	  if (q)
	    mpz_set_ui (q, 1);
	}
      else if (mode == GMP_DIV_FLOOR && qs < 0)
	{
	  /* q = -1, r = n + d */
	  if (r)
	    mpz_add (r, n, d);
	  if (q)
	    mpz_set_si (q, -1);
	}
      else
	{
	  /* q = 0, r = d */
	  if (r)
	    mpz_set (r, n);
	  if (q)
	    q->_mp_size = 0;
	}
      return 1;
    }
  else
    {
      mp_ptr np, qp;
      mp_size_t qn, rn;
      mpz_t tq, tr;

      mpz_init_set (tr, n);
      np = tr->_mp_d;

      qn = nn - dn + 1;

      if (q)
	{
	  mpz_init2 (tq, qn * GMP_LIMB_BITS);
	  qp = tq->_mp_d;
	}
      else
	qp = NULL;

      mpn_div_qr (qp, np, nn, d->_mp_d, dn);

      if (qp)
	{
	  qn -= (qp[qn-1] == 0);

	  tq->_mp_size = qs < 0 ? -qn : qn;
	}
      rn = mpn_normalized_size (np, dn);
      tr->_mp_size = ns < 0 ? - rn : rn;

      if (mode == GMP_DIV_FLOOR && qs < 0 && rn != 0)
	{
	  if (q)
	    mpz_sub_ui (tq, tq, 1);
	  if (r)
	    mpz_add (tr, tr, d);
	}
      else if (mode == GMP_DIV_CEIL && qs >= 0 && rn != 0)
	{
	  if (q)
	    mpz_add_ui (tq, tq, 1);
	  if (r)
	    mpz_sub (tr, tr, d);
	}

      if (q)
	{
	  mpz_swap (tq, q);
	  mpz_clear (tq);
	}
      if (r)
	mpz_swap (tr, r);

      mpz_clear (tr);

      return rn != 0;
    }
}

void
mpz_tdiv_q (mpz_t q, const mpz_t n, const mpz_t d)
{
  mpz_div_qr (q, NULL, n, d, GMP_DIV_TRUNC);
}

int
mpz_cmp (const mpz_t a, const mpz_t b)
{
  mp_size_t asize = a->_mp_size;
  mp_size_t bsize = b->_mp_size;

  if (asize != bsize)
    return (asize < bsize) ? -1 : 1;
  else if (asize >= 0)
    return mpn_cmp (a->_mp_d, b->_mp_d, asize);
  else
    return mpn_cmp (b->_mp_d, a->_mp_d, -asize);
}

#endif /* __MINI_GMP_H__ */

