(module $pidigits.wasm
  (type $t0 (func (param i32) (result i32)))
  (type $t1 (func (param i32 i32 i32) (result i32)))
  (type $t2 (func (param i32 i32)))
  (type $t3 (func))
  (type $t4 (func (param i32)))
  (type $t5 (func (param i32 i32) (result i32)))
  (type $t6 (func (result i32)))
  (type $t7 (func (param i32 i32 i32)))
  (type $t8 (func (param i32 i32 i32 i32) (result i32)))
  (type $t9 (func (param i32 i32 i32 i32 i32) (result i32)))
  (type $t10 (func (param i32 i32 i32 i32)))
  (type $t11 (func (param i32 i32 i32 i32 i32 i32 i32)))
  (type $t12 (func (param i32 i32 i32 i32 i32 i32)))
  (type $t13 (func (param i32 i32 i32 i32 i32)))
  (import "env" "fprintf" (func $fprintf (type $t1)))
  (import "env" "abort" (func $abort (type $t3)))
  (import "env" "assert" (func $assert (type $t4)))
  (import "env" "malloc" (func $malloc (type $t0)))
  (import "env" "realloc" (func $realloc (type $t5)))
  (import "env" "free" (func $free (type $t4)))
  (import "env" "__VERIFIER_nondet_int" (func $__VERIFIER_nondet_int (type $t6)))
  (import "env" "putchar" (func $putchar (type $t0)))
  (import "env" "printf" (func $printf (type $t5)))
  (func $__wasm_call_ctors (type $t3))
  (func $gmp_die (type $t4) (param $p0 i32)
    (local $l1 i32) (local $l2 i32)
    (local.set $l1
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l1))
    (i32.store offset=12
      (local.get $l1)
      (local.get $p0))
    (local.set $l2
      (i32.load
        (i32.const 0)))
    (i32.store
      (local.get $l1)
      (i32.load offset=12
        (local.get $l1)))
    (drop
      (call $fprintf
        (local.get $l2)
        (i32.const 1150)
        (local.get $l1)))
    (call $abort)
    (unreachable))
  (func $gmp_default_alloc (type $t0) (param $p0 i32) (result i32)
    (local $l1 i32) (local $l2 i32)
    (local.set $l1
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l1))
    (i32.store offset=12
      (local.get $l1)
      (local.get $p0))
    (call $assert
      (i32.and
        (i32.gt_u
          (i32.load offset=12
            (local.get $l1))
          (i32.const 0))
        (i32.const 1)))
    (i32.store offset=8
      (local.get $l1)
      (call $malloc
        (i32.load offset=12
          (local.get $l1))))
    (block $B0
      (br_if $B0
        (i32.and
          (i32.ne
            (i32.load offset=8
              (local.get $l1))
            (i32.const 0))
          (i32.const 1)))
      (call $gmp_die
        (i32.const 1099)))
    (local.set $l2
      (i32.load offset=8
        (local.get $l1)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l1)
        (i32.const 16)))
    (return
      (local.get $l2)))
  (func $gmp_default_realloc (type $t1) (param $p0 i32) (param $p1 i32) (param $p2 i32) (result i32)
    (local $l3 i32) (local $l4 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l3))
    (i32.store offset=12
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l3)
      (local.get $p1))
    (i32.store offset=4
      (local.get $l3)
      (local.get $p2))
    (i32.store
      (local.get $l3)
      (call $realloc
        (i32.load offset=12
          (local.get $l3))
        (i32.load offset=4
          (local.get $l3))))
    (block $B0
      (br_if $B0
        (i32.and
          (i32.ne
            (i32.load
              (local.get $l3))
            (i32.const 0))
          (i32.const 1)))
      (call $gmp_die
        (i32.const 1052)))
    (local.set $l4
      (i32.load
        (local.get $l3)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l3)
        (i32.const 16)))
    (return
      (local.get $l4)))
  (func $gmp_default_free (type $t2) (param $p0 i32) (param $p1 i32)
    (local $l2 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l2))
    (i32.store offset=12
      (local.get $l2)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l2)
      (local.get $p1))
    (call $free
      (i32.load offset=12
        (local.get $l2)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l2)
        (i32.const 16)))
    (return))
  (func $gmp_alloc_limbs (type $t0) (param $p0 i32) (result i32)
    (local $l1 i32) (local $l2 i32) (local $l3 i32)
    (local.set $l1
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l1))
    (i32.store offset=12
      (local.get $l1)
      (local.get $p0))
    (local.set $l2
      (i32.load offset=1160
        (i32.const 0)))
    (local.set $l3
      (call_indirect $__indirect_function_table (type $t0)
        (i32.shl
          (i32.load offset=12
            (local.get $l1))
          (i32.const 2))
        (local.get $l2)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l1)
        (i32.const 16)))
    (return
      (local.get $l3)))
  (func $gmp_realloc_limbs (type $t1) (param $p0 i32) (param $p1 i32) (param $p2 i32) (result i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l3))
    (i32.store offset=12
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l3)
      (local.get $p1))
    (i32.store offset=4
      (local.get $l3)
      (local.get $p2))
    (call $assert
      (i32.and
        (i32.gt_s
          (i32.load offset=4
            (local.get $l3))
          (i32.const 0))
        (i32.const 1)))
    (local.set $l4
      (i32.load offset=1164
        (i32.const 0)))
    (local.set $l5
      (call_indirect $__indirect_function_table (type $t1)
        (i32.load offset=12
          (local.get $l3))
        (i32.shl
          (i32.load offset=8
            (local.get $l3))
          (i32.const 2))
        (i32.shl
          (i32.load offset=4
            (local.get $l3))
          (i32.const 2))
        (local.get $l4)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l3)
        (i32.const 16)))
    (return
      (local.get $l5)))
  (func $gmp_free_limbs (type $t2) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l2))
    (i32.store offset=12
      (local.get $l2)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l2)
      (local.get $p1))
    (local.set $l3
      (i32.load offset=1168
        (i32.const 0)))
    (call_indirect $__indirect_function_table (type $t2)
      (i32.load offset=12
        (local.get $l2))
      (i32.shl
        (i32.load offset=8
          (local.get $l2))
        (i32.const 2))
      (local.get $l3))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l2)
        (i32.const 16)))
    (return))
  (func $mpn_copyi (type $t7) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (i32.store offset=12
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l3)
      (local.get $p1))
    (i32.store offset=4
      (local.get $l3)
      (local.get $p2))
    (i32.store
      (local.get $l3)
      (i32.const 0))
    (block $B0
      (loop $L1
        (br_if $B0
          (i32.eqz
            (i32.and
              (i32.lt_s
                (i32.load
                  (local.get $l3))
                (i32.load offset=4
                  (local.get $l3)))
              (i32.const 1))))
        (local.set $l4
          (i32.load
            (i32.add
              (i32.load offset=8
                (local.get $l3))
              (i32.shl
                (i32.load
                  (local.get $l3))
                (i32.const 2)))))
        (i32.store
          (i32.add
            (i32.load offset=12
              (local.get $l3))
            (i32.shl
              (i32.load
                (local.get $l3))
              (i32.const 2)))
          (local.get $l4))
        (i32.store
          (local.get $l3)
          (i32.add
            (i32.load
              (local.get $l3))
            (i32.const 1)))
        (br $L1)))
    (return))
  (func $mpn_copyd (type $t7) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (i32.store offset=12
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l3)
      (local.get $p1))
    (i32.store offset=4
      (local.get $l3)
      (local.get $p2))
    (block $B0
      (loop $L1
        (local.set $l4
          (i32.add
            (i32.load offset=4
              (local.get $l3))
            (i32.const -1)))
        (i32.store offset=4
          (local.get $l3)
          (local.get $l4))
        (br_if $B0
          (i32.eqz
            (i32.and
              (i32.ge_s
                (local.get $l4)
                (i32.const 0))
              (i32.const 1))))
        (local.set $l5
          (i32.load
            (i32.add
              (i32.load offset=8
                (local.get $l3))
              (i32.shl
                (i32.load offset=4
                  (local.get $l3))
                (i32.const 2)))))
        (i32.store
          (i32.add
            (i32.load offset=12
              (local.get $l3))
            (i32.shl
              (i32.load offset=4
                (local.get $l3))
              (i32.const 2)))
          (local.get $l5))
        (br $L1)))
    (return))
  (func $mpn_cmp (type $t1) (param $p0 i32) (param $p1 i32) (param $p2 i32) (result i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (i32.store offset=8
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=4
      (local.get $l3)
      (local.get $p1))
    (i32.store
      (local.get $l3)
      (local.get $p2))
    (block $B0
      (block $B1
        (loop $L2
          (local.set $l4
            (i32.add
              (i32.load
                (local.get $l3))
              (i32.const -1)))
          (i32.store
            (local.get $l3)
            (local.get $l4))
          (br_if $B1
            (i32.eqz
              (i32.and
                (i32.ge_s
                  (local.get $l4)
                  (i32.const 0))
                (i32.const 1))))
          (block $B3
            (br_if $B3
              (i32.eqz
                (i32.and
                  (i32.ne
                    (i32.load
                      (i32.add
                        (i32.load offset=8
                          (local.get $l3))
                        (i32.shl
                          (i32.load
                            (local.get $l3))
                          (i32.const 2))))
                    (i32.load
                      (i32.add
                        (i32.load offset=4
                          (local.get $l3))
                        (i32.shl
                          (i32.load
                            (local.get $l3))
                          (i32.const 2)))))
                  (i32.const 1))))
            (local.set $l5
              (i32.gt_u
                (i32.load
                  (i32.add
                    (i32.load offset=8
                      (local.get $l3))
                    (i32.shl
                      (i32.load
                        (local.get $l3))
                      (i32.const 2))))
                (i32.load
                  (i32.add
                    (i32.load offset=4
                      (local.get $l3))
                    (i32.shl
                      (i32.load
                        (local.get $l3))
                      (i32.const 2))))))
            (i32.store offset=12
              (local.get $l3)
              (select
                (i32.const 1)
                (i32.const -1)
                (i32.and
                  (local.get $l5)
                  (i32.const 1))))
            (br $B0))
          (br $L2)))
      (i32.store offset=12
        (local.get $l3)
        (i32.const 0)))
    (return
      (i32.load offset=12
        (local.get $l3))))
  (func $mpn_cmp4 (type $t8) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (result i32)
    (local $l4 i32) (local $l5 i32) (local $l6 i32)
    (local.set $l4
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 32)))
    (global.set $__stack_pointer
      (local.get $l4))
    (i32.store offset=24
      (local.get $l4)
      (local.get $p0))
    (i32.store offset=20
      (local.get $l4)
      (local.get $p1))
    (i32.store offset=16
      (local.get $l4)
      (local.get $p2))
    (i32.store offset=12
      (local.get $l4)
      (local.get $p3))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.and
              (i32.ne
                (i32.load offset=20
                  (local.get $l4))
                (i32.load offset=12
                  (local.get $l4)))
              (i32.const 1))))
        (local.set $l5
          (i32.lt_s
            (i32.load offset=20
              (local.get $l4))
            (i32.load offset=12
              (local.get $l4))))
        (i32.store offset=28
          (local.get $l4)
          (select
            (i32.const -1)
            (i32.const 1)
            (i32.and
              (local.get $l5)
              (i32.const 1))))
        (br $B0))
      (i32.store offset=28
        (local.get $l4)
        (call $mpn_cmp
          (i32.load offset=24
            (local.get $l4))
          (i32.load offset=16
            (local.get $l4))
          (i32.load offset=20
            (local.get $l4)))))
    (local.set $l6
      (i32.load offset=28
        (local.get $l4)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l4)
        (i32.const 32)))
    (return
      (local.get $l6)))
  (func $mpn_normalized_size (type $t5) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (i32.store offset=12
      (local.get $l2)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l2)
      (local.get $p1))
    (loop $L0
      (local.set $l3
        (i32.gt_s
          (i32.load offset=8
            (local.get $l2))
          (i32.const 0)))
      (local.set $l4
        (i32.const 0))
      (local.set $l5
        (i32.and
          (local.get $l3)
          (i32.const 1)))
      (local.set $l6
        (local.get $l4))
      (block $B1
        (br_if $B1
          (i32.eqz
            (local.get $l5)))
        (local.set $l6
          (i32.eq
            (i32.load
              (i32.add
                (i32.load offset=12
                  (local.get $l2))
                (i32.shl
                  (i32.sub
                    (i32.load offset=8
                      (local.get $l2))
                    (i32.const 1))
                  (i32.const 2))))
            (i32.const 0))))
      (block $B2
        (br_if $B2
          (i32.eqz
            (i32.and
              (local.get $l6)
              (i32.const 1))))
        (i32.store offset=8
          (local.get $l2)
          (i32.add
            (i32.load offset=8
              (local.get $l2))
            (i32.const -1)))
        (br $L0)))
    (return
      (i32.load offset=8
        (local.get $l2))))
  (func $mpn_zero_p (type $t5) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32) (local $l3 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l2))
    (i32.store offset=12
      (local.get $l2)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l2)
      (local.get $p1))
    (local.set $l3
      (i32.and
        (i32.eq
          (call $mpn_normalized_size
            (i32.load offset=12
              (local.get $l2))
            (i32.load offset=8
              (local.get $l2)))
          (i32.const 0))
        (i32.const 1)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l2)
        (i32.const 16)))
    (return
      (local.get $l3)))
  (func $mpn_zero (type $t2) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (i32.store offset=12
      (local.get $l2)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l2)
      (local.get $p1))
    (block $B0
      (loop $L1
        (local.set $l3
          (i32.add
            (i32.load offset=8
              (local.get $l2))
            (i32.const -1)))
        (i32.store offset=8
          (local.get $l2)
          (local.get $l3))
        (br_if $B0
          (i32.eqz
            (i32.and
              (i32.ge_s
                (local.get $l3)
                (i32.const 0))
              (i32.const 1))))
        (i32.store
          (i32.add
            (i32.load offset=12
              (local.get $l2))
            (i32.shl
              (i32.load offset=8
                (local.get $l2))
              (i32.const 2)))
          (i32.const 0))
        (br $L1)))
    (return))
  (func $mpn_add_1 (type $t8) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (result i32)
    (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32)
    (local.set $l4
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 32)))
    (global.set $__stack_pointer
      (local.get $l4))
    (i32.store offset=28
      (local.get $l4)
      (local.get $p0))
    (i32.store offset=24
      (local.get $l4)
      (local.get $p1))
    (i32.store offset=20
      (local.get $l4)
      (local.get $p2))
    (i32.store offset=16
      (local.get $l4)
      (local.get $p3))
    (call $assert
      (i32.and
        (i32.gt_s
          (i32.load offset=20
            (local.get $l4))
          (i32.const 0))
        (i32.const 1)))
    (i32.store offset=12
      (local.get $l4)
      (i32.const 0))
    (loop $L0
      (i32.store offset=8
        (local.get $l4)
        (i32.add
          (i32.load
            (i32.add
              (i32.load offset=24
                (local.get $l4))
              (i32.shl
                (i32.load offset=12
                  (local.get $l4))
                (i32.const 2))))
          (i32.load offset=16
            (local.get $l4))))
      (i32.store offset=16
        (local.get $l4)
        (i32.and
          (i32.lt_u
            (i32.load offset=8
              (local.get $l4))
            (i32.load offset=16
              (local.get $l4)))
          (i32.const 1)))
      (local.set $l5
        (i32.load offset=8
          (local.get $l4)))
      (i32.store
        (i32.add
          (i32.load offset=28
            (local.get $l4))
          (i32.shl
            (i32.load offset=12
              (local.get $l4))
            (i32.const 2)))
        (local.get $l5))
      (local.set $l6
        (i32.add
          (i32.load offset=12
            (local.get $l4))
          (i32.const 1)))
      (i32.store offset=12
        (local.get $l4)
        (local.get $l6))
      (br_if $L0
        (i32.and
          (i32.lt_s
            (local.get $l6)
            (i32.load offset=20
              (local.get $l4)))
          (i32.const 1))))
    (local.set $l7
      (i32.load offset=16
        (local.get $l4)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l4)
        (i32.const 32)))
    (return
      (local.get $l7)))
  (func $mpn_add_n (type $t8) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (result i32)
    (local $l4 i32) (local $l5 i32)
    (local.set $l4
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 48)))
    (i32.store offset=44
      (local.get $l4)
      (local.get $p0))
    (i32.store offset=40
      (local.get $l4)
      (local.get $p1))
    (i32.store offset=36
      (local.get $l4)
      (local.get $p2))
    (i32.store offset=32
      (local.get $l4)
      (local.get $p3))
    (i32.store offset=28
      (local.get $l4)
      (i32.const 0))
    (i32.store offset=24
      (local.get $l4)
      (i32.const 0))
    (block $B0
      (loop $L1
        (br_if $B0
          (i32.eqz
            (i32.and
              (i32.lt_s
                (i32.load offset=28
                  (local.get $l4))
                (i32.load offset=32
                  (local.get $l4)))
              (i32.const 1))))
        (i32.store offset=20
          (local.get $l4)
          (i32.load
            (i32.add
              (i32.load offset=40
                (local.get $l4))
              (i32.shl
                (i32.load offset=28
                  (local.get $l4))
                (i32.const 2)))))
        (i32.store offset=16
          (local.get $l4)
          (i32.load
            (i32.add
              (i32.load offset=36
                (local.get $l4))
              (i32.shl
                (i32.load offset=28
                  (local.get $l4))
                (i32.const 2)))))
        (i32.store offset=12
          (local.get $l4)
          (i32.add
            (i32.load offset=20
              (local.get $l4))
            (i32.load offset=24
              (local.get $l4))))
        (i32.store offset=24
          (local.get $l4)
          (i32.and
            (i32.lt_u
              (i32.load offset=12
                (local.get $l4))
              (i32.load offset=24
                (local.get $l4)))
            (i32.const 1)))
        (i32.store offset=12
          (local.get $l4)
          (i32.add
            (i32.load offset=16
              (local.get $l4))
            (i32.load offset=12
              (local.get $l4))))
        (i32.store offset=24
          (local.get $l4)
          (i32.add
            (i32.and
              (i32.lt_u
                (i32.load offset=12
                  (local.get $l4))
                (i32.load offset=16
                  (local.get $l4)))
              (i32.const 1))
            (i32.load offset=24
              (local.get $l4))))
        (local.set $l5
          (i32.load offset=12
            (local.get $l4)))
        (i32.store
          (i32.add
            (i32.load offset=44
              (local.get $l4))
            (i32.shl
              (i32.load offset=28
                (local.get $l4))
              (i32.const 2)))
          (local.get $l5))
        (i32.store offset=28
          (local.get $l4)
          (i32.add
            (i32.load offset=28
              (local.get $l4))
            (i32.const 1)))
        (br $L1)))
    (return
      (i32.load offset=24
        (local.get $l4))))
  (func $mpn_add (type $t9) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (param $p4 i32) (result i32)
    (local $l5 i32) (local $l6 i32)
    (local.set $l5
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 32)))
    (global.set $__stack_pointer
      (local.get $l5))
    (i32.store offset=28
      (local.get $l5)
      (local.get $p0))
    (i32.store offset=24
      (local.get $l5)
      (local.get $p1))
    (i32.store offset=20
      (local.get $l5)
      (local.get $p2))
    (i32.store offset=16
      (local.get $l5)
      (local.get $p3))
    (i32.store offset=12
      (local.get $l5)
      (local.get $p4))
    (call $assert
      (i32.and
        (i32.ge_s
          (i32.load offset=20
            (local.get $l5))
          (i32.load offset=12
            (local.get $l5)))
        (i32.const 1)))
    (i32.store offset=8
      (local.get $l5)
      (call $mpn_add_n
        (i32.load offset=28
          (local.get $l5))
        (i32.load offset=24
          (local.get $l5))
        (i32.load offset=16
          (local.get $l5))
        (i32.load offset=12
          (local.get $l5))))
    (block $B0
      (br_if $B0
        (i32.eqz
          (i32.and
            (i32.gt_s
              (i32.load offset=20
                (local.get $l5))
              (i32.load offset=12
                (local.get $l5)))
            (i32.const 1))))
      (i32.store offset=8
        (local.get $l5)
        (call $mpn_add_1
          (i32.add
            (i32.load offset=28
              (local.get $l5))
            (i32.shl
              (i32.load offset=12
                (local.get $l5))
              (i32.const 2)))
          (i32.add
            (i32.load offset=24
              (local.get $l5))
            (i32.shl
              (i32.load offset=12
                (local.get $l5))
              (i32.const 2)))
          (i32.sub
            (i32.load offset=20
              (local.get $l5))
            (i32.load offset=12
              (local.get $l5)))
          (i32.load offset=8
            (local.get $l5)))))
    (local.set $l6
      (i32.load offset=8
        (local.get $l5)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l5)
        (i32.const 32)))
    (return
      (local.get $l6)))
  (func $mpn_sub_1 (type $t8) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (result i32)
    (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32)
    (local.set $l4
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 32)))
    (global.set $__stack_pointer
      (local.get $l4))
    (i32.store offset=28
      (local.get $l4)
      (local.get $p0))
    (i32.store offset=24
      (local.get $l4)
      (local.get $p1))
    (i32.store offset=20
      (local.get $l4)
      (local.get $p2))
    (i32.store offset=16
      (local.get $l4)
      (local.get $p3))
    (call $assert
      (i32.and
        (i32.gt_s
          (i32.load offset=20
            (local.get $l4))
          (i32.const 0))
        (i32.const 1)))
    (i32.store offset=12
      (local.get $l4)
      (i32.const 0))
    (loop $L0
      (i32.store offset=8
        (local.get $l4)
        (i32.load
          (i32.add
            (i32.load offset=24
              (local.get $l4))
            (i32.shl
              (i32.load offset=12
                (local.get $l4))
              (i32.const 2)))))
      (i32.store offset=4
        (local.get $l4)
        (i32.and
          (i32.lt_u
            (i32.load offset=8
              (local.get $l4))
            (i32.load offset=16
              (local.get $l4)))
          (i32.const 1)))
      (local.set $l5
        (i32.sub
          (i32.load offset=8
            (local.get $l4))
          (i32.load offset=16
            (local.get $l4))))
      (i32.store
        (i32.add
          (i32.load offset=28
            (local.get $l4))
          (i32.shl
            (i32.load offset=12
              (local.get $l4))
            (i32.const 2)))
        (local.get $l5))
      (i32.store offset=16
        (local.get $l4)
        (i32.load offset=4
          (local.get $l4)))
      (local.set $l6
        (i32.add
          (i32.load offset=12
            (local.get $l4))
          (i32.const 1)))
      (i32.store offset=12
        (local.get $l4)
        (local.get $l6))
      (br_if $L0
        (i32.and
          (i32.lt_s
            (local.get $l6)
            (i32.load offset=20
              (local.get $l4)))
          (i32.const 1))))
    (local.set $l7
      (i32.load offset=16
        (local.get $l4)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l4)
        (i32.const 32)))
    (return
      (local.get $l7)))
  (func $mpn_sub_n (type $t8) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (result i32)
    (local $l4 i32) (local $l5 i32)
    (local.set $l4
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 32)))
    (i32.store offset=28
      (local.get $l4)
      (local.get $p0))
    (i32.store offset=24
      (local.get $l4)
      (local.get $p1))
    (i32.store offset=20
      (local.get $l4)
      (local.get $p2))
    (i32.store offset=16
      (local.get $l4)
      (local.get $p3))
    (i32.store offset=12
      (local.get $l4)
      (i32.const 0))
    (i32.store offset=8
      (local.get $l4)
      (i32.const 0))
    (block $B0
      (loop $L1
        (br_if $B0
          (i32.eqz
            (i32.and
              (i32.lt_s
                (i32.load offset=12
                  (local.get $l4))
                (i32.load offset=16
                  (local.get $l4)))
              (i32.const 1))))
        (i32.store offset=4
          (local.get $l4)
          (i32.load
            (i32.add
              (i32.load offset=24
                (local.get $l4))
              (i32.shl
                (i32.load offset=12
                  (local.get $l4))
                (i32.const 2)))))
        (i32.store
          (local.get $l4)
          (i32.load
            (i32.add
              (i32.load offset=20
                (local.get $l4))
              (i32.shl
                (i32.load offset=12
                  (local.get $l4))
                (i32.const 2)))))
        (i32.store
          (local.get $l4)
          (i32.add
            (i32.load offset=8
              (local.get $l4))
            (i32.load
              (local.get $l4))))
        (i32.store offset=8
          (local.get $l4)
          (i32.and
            (i32.lt_u
              (i32.load
                (local.get $l4))
              (i32.load offset=8
                (local.get $l4)))
            (i32.const 1)))
        (i32.store offset=8
          (local.get $l4)
          (i32.add
            (i32.and
              (i32.lt_u
                (i32.load offset=4
                  (local.get $l4))
                (i32.load
                  (local.get $l4)))
              (i32.const 1))
            (i32.load offset=8
              (local.get $l4))))
        (local.set $l5
          (i32.sub
            (i32.load offset=4
              (local.get $l4))
            (i32.load
              (local.get $l4))))
        (i32.store
          (i32.add
            (i32.load offset=28
              (local.get $l4))
            (i32.shl
              (i32.load offset=12
                (local.get $l4))
              (i32.const 2)))
          (local.get $l5))
        (i32.store offset=12
          (local.get $l4)
          (i32.add
            (i32.load offset=12
              (local.get $l4))
            (i32.const 1)))
        (br $L1)))
    (return
      (i32.load offset=8
        (local.get $l4))))
  (func $mpn_sub (type $t9) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (param $p4 i32) (result i32)
    (local $l5 i32) (local $l6 i32)
    (local.set $l5
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 32)))
    (global.set $__stack_pointer
      (local.get $l5))
    (i32.store offset=28
      (local.get $l5)
      (local.get $p0))
    (i32.store offset=24
      (local.get $l5)
      (local.get $p1))
    (i32.store offset=20
      (local.get $l5)
      (local.get $p2))
    (i32.store offset=16
      (local.get $l5)
      (local.get $p3))
    (i32.store offset=12
      (local.get $l5)
      (local.get $p4))
    (call $assert
      (i32.and
        (i32.ge_s
          (i32.load offset=20
            (local.get $l5))
          (i32.load offset=12
            (local.get $l5)))
        (i32.const 1)))
    (i32.store offset=8
      (local.get $l5)
      (call $mpn_sub_n
        (i32.load offset=28
          (local.get $l5))
        (i32.load offset=24
          (local.get $l5))
        (i32.load offset=16
          (local.get $l5))
        (i32.load offset=12
          (local.get $l5))))
    (block $B0
      (br_if $B0
        (i32.eqz
          (i32.and
            (i32.gt_s
              (i32.load offset=20
                (local.get $l5))
              (i32.load offset=12
                (local.get $l5)))
            (i32.const 1))))
      (i32.store offset=8
        (local.get $l5)
        (call $mpn_sub_1
          (i32.add
            (i32.load offset=28
              (local.get $l5))
            (i32.shl
              (i32.load offset=12
                (local.get $l5))
              (i32.const 2)))
          (i32.add
            (i32.load offset=24
              (local.get $l5))
            (i32.shl
              (i32.load offset=12
                (local.get $l5))
              (i32.const 2)))
          (i32.sub
            (i32.load offset=20
              (local.get $l5))
            (i32.load offset=12
              (local.get $l5)))
          (i32.load offset=8
            (local.get $l5)))))
    (local.set $l6
      (i32.load offset=8
        (local.get $l5)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l5)
        (i32.const 32)))
    (return
      (local.get $l6)))
  (func $mpn_mul_1 (type $t8) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (result i32)
    (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32)
    (local.set $l4
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 80)))
    (global.set $__stack_pointer
      (local.get $l4))
    (i32.store offset=76
      (local.get $l4)
      (local.get $p0))
    (i32.store offset=72
      (local.get $l4)
      (local.get $p1))
    (i32.store offset=68
      (local.get $l4)
      (local.get $p2))
    (i32.store offset=64
      (local.get $l4)
      (local.get $p3))
    (call $assert
      (i32.and
        (i32.ge_s
          (i32.load offset=68
            (local.get $l4))
          (i32.const 1))
        (i32.const 1)))
    (i32.store offset=56
      (local.get $l4)
      (i32.const 0))
    (loop $L0
      (local.set $l5
        (i32.load offset=72
          (local.get $l4)))
      (i32.store offset=72
        (local.get $l4)
        (i32.add
          (local.get $l5)
          (i32.const 4)))
      (i32.store offset=60
        (local.get $l4)
        (i32.load
          (local.get $l5)))
      (i32.store offset=44
        (local.get $l4)
        (i32.const 32))
      (i32.store offset=8
        (local.get $l4)
        (i32.load offset=60
          (local.get $l4)))
      (i32.store offset=4
        (local.get $l4)
        (i32.load offset=64
          (local.get $l4)))
      (call $assert
        (i32.const 1))
      (i32.store offset=24
        (local.get $l4)
        (i32.and
          (i32.load offset=8
            (local.get $l4))
          (i32.const 65535)))
      (i32.store offset=16
        (local.get $l4)
        (i32.shr_u
          (i32.load offset=8
            (local.get $l4))
          (i32.const 16)))
      (i32.store offset=20
        (local.get $l4)
        (i32.and
          (i32.load offset=4
            (local.get $l4))
          (i32.const 65535)))
      (i32.store offset=12
        (local.get $l4)
        (i32.shr_u
          (i32.load offset=4
            (local.get $l4))
          (i32.const 16)))
      (i32.store offset=40
        (local.get $l4)
        (i32.mul
          (i32.load offset=24
            (local.get $l4))
          (i32.load offset=20
            (local.get $l4))))
      (i32.store offset=36
        (local.get $l4)
        (i32.mul
          (i32.load offset=24
            (local.get $l4))
          (i32.load offset=12
            (local.get $l4))))
      (i32.store offset=32
        (local.get $l4)
        (i32.mul
          (i32.load offset=16
            (local.get $l4))
          (i32.load offset=20
            (local.get $l4))))
      (i32.store offset=28
        (local.get $l4)
        (i32.mul
          (i32.load offset=16
            (local.get $l4))
          (i32.load offset=12
            (local.get $l4))))
      (i32.store offset=36
        (local.get $l4)
        (i32.add
          (i32.shr_u
            (i32.load offset=40
              (local.get $l4))
            (i32.const 16))
          (i32.load offset=36
            (local.get $l4))))
      (i32.store offset=36
        (local.get $l4)
        (i32.add
          (i32.load offset=32
            (local.get $l4))
          (i32.load offset=36
            (local.get $l4))))
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.and
              (i32.lt_u
                (i32.load offset=36
                  (local.get $l4))
                (i32.load offset=32
                  (local.get $l4)))
              (i32.const 1))))
        (i32.store offset=28
          (local.get $l4)
          (i32.add
            (i32.load offset=28
              (local.get $l4))
            (i32.const 65536))))
      (i32.store offset=52
        (local.get $l4)
        (i32.add
          (i32.load offset=28
            (local.get $l4))
          (i32.shr_u
            (i32.load offset=36
              (local.get $l4))
            (i32.const 16))))
      (i32.store offset=48
        (local.get $l4)
        (i32.add
          (i32.shl
            (i32.load offset=36
              (local.get $l4))
            (i32.const 16))
          (i32.and
            (i32.load offset=40
              (local.get $l4))
            (i32.const 65535))))
      (i32.store offset=48
        (local.get $l4)
        (i32.add
          (i32.load offset=56
            (local.get $l4))
          (i32.load offset=48
            (local.get $l4))))
      (i32.store offset=56
        (local.get $l4)
        (i32.add
          (i32.and
            (i32.lt_u
              (i32.load offset=48
                (local.get $l4))
              (i32.load offset=56
                (local.get $l4)))
            (i32.const 1))
          (i32.load offset=52
            (local.get $l4))))
      (local.set $l6
        (i32.load offset=48
          (local.get $l4)))
      (local.set $l7
        (i32.load offset=76
          (local.get $l4)))
      (i32.store offset=76
        (local.get $l4)
        (i32.add
          (local.get $l7)
          (i32.const 4)))
      (i32.store
        (local.get $l7)
        (local.get $l6))
      (local.set $l8
        (i32.add
          (i32.load offset=68
            (local.get $l4))
          (i32.const -1)))
      (i32.store offset=68
        (local.get $l4)
        (local.get $l8))
      (br_if $L0
        (local.get $l8)))
    (local.set $l9
      (i32.load offset=56
        (local.get $l4)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l4)
        (i32.const 80)))
    (return
      (local.get $l9)))
  (func $mpn_addmul_1 (type $t8) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (result i32)
    (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32)
    (local.set $l4
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 80)))
    (global.set $__stack_pointer
      (local.get $l4))
    (i32.store offset=76
      (local.get $l4)
      (local.get $p0))
    (i32.store offset=72
      (local.get $l4)
      (local.get $p1))
    (i32.store offset=68
      (local.get $l4)
      (local.get $p2))
    (i32.store offset=64
      (local.get $l4)
      (local.get $p3))
    (call $assert
      (i32.and
        (i32.ge_s
          (i32.load offset=68
            (local.get $l4))
          (i32.const 1))
        (i32.const 1)))
    (i32.store offset=56
      (local.get $l4)
      (i32.const 0))
    (loop $L0
      (local.set $l5
        (i32.load offset=72
          (local.get $l4)))
      (i32.store offset=72
        (local.get $l4)
        (i32.add
          (local.get $l5)
          (i32.const 4)))
      (i32.store offset=60
        (local.get $l4)
        (i32.load
          (local.get $l5)))
      (i32.store offset=40
        (local.get $l4)
        (i32.const 32))
      (i32.store offset=4
        (local.get $l4)
        (i32.load offset=60
          (local.get $l4)))
      (i32.store
        (local.get $l4)
        (i32.load offset=64
          (local.get $l4)))
      (call $assert
        (i32.const 1))
      (i32.store offset=20
        (local.get $l4)
        (i32.and
          (i32.load offset=4
            (local.get $l4))
          (i32.const 65535)))
      (i32.store offset=12
        (local.get $l4)
        (i32.shr_u
          (i32.load offset=4
            (local.get $l4))
          (i32.const 16)))
      (i32.store offset=16
        (local.get $l4)
        (i32.and
          (i32.load
            (local.get $l4))
          (i32.const 65535)))
      (i32.store offset=8
        (local.get $l4)
        (i32.shr_u
          (i32.load
            (local.get $l4))
          (i32.const 16)))
      (i32.store offset=36
        (local.get $l4)
        (i32.mul
          (i32.load offset=20
            (local.get $l4))
          (i32.load offset=16
            (local.get $l4))))
      (i32.store offset=32
        (local.get $l4)
        (i32.mul
          (i32.load offset=20
            (local.get $l4))
          (i32.load offset=8
            (local.get $l4))))
      (i32.store offset=28
        (local.get $l4)
        (i32.mul
          (i32.load offset=12
            (local.get $l4))
          (i32.load offset=16
            (local.get $l4))))
      (i32.store offset=24
        (local.get $l4)
        (i32.mul
          (i32.load offset=12
            (local.get $l4))
          (i32.load offset=8
            (local.get $l4))))
      (i32.store offset=32
        (local.get $l4)
        (i32.add
          (i32.shr_u
            (i32.load offset=36
              (local.get $l4))
            (i32.const 16))
          (i32.load offset=32
            (local.get $l4))))
      (i32.store offset=32
        (local.get $l4)
        (i32.add
          (i32.load offset=28
            (local.get $l4))
          (i32.load offset=32
            (local.get $l4))))
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.and
              (i32.lt_u
                (i32.load offset=32
                  (local.get $l4))
                (i32.load offset=28
                  (local.get $l4)))
              (i32.const 1))))
        (i32.store offset=24
          (local.get $l4)
          (i32.add
            (i32.load offset=24
              (local.get $l4))
            (i32.const 65536))))
      (i32.store offset=52
        (local.get $l4)
        (i32.add
          (i32.load offset=24
            (local.get $l4))
          (i32.shr_u
            (i32.load offset=32
              (local.get $l4))
            (i32.const 16))))
      (i32.store offset=48
        (local.get $l4)
        (i32.add
          (i32.shl
            (i32.load offset=32
              (local.get $l4))
            (i32.const 16))
          (i32.and
            (i32.load offset=36
              (local.get $l4))
            (i32.const 65535))))
      (i32.store offset=48
        (local.get $l4)
        (i32.add
          (i32.load offset=56
            (local.get $l4))
          (i32.load offset=48
            (local.get $l4))))
      (i32.store offset=56
        (local.get $l4)
        (i32.add
          (i32.and
            (i32.lt_u
              (i32.load offset=48
                (local.get $l4))
              (i32.load offset=56
                (local.get $l4)))
            (i32.const 1))
          (i32.load offset=52
            (local.get $l4))))
      (i32.store offset=44
        (local.get $l4)
        (i32.load
          (i32.load offset=76
            (local.get $l4))))
      (i32.store offset=48
        (local.get $l4)
        (i32.add
          (i32.load offset=44
            (local.get $l4))
          (i32.load offset=48
            (local.get $l4))))
      (i32.store offset=56
        (local.get $l4)
        (i32.add
          (i32.and
            (i32.lt_u
              (i32.load offset=48
                (local.get $l4))
              (i32.load offset=44
                (local.get $l4)))
            (i32.const 1))
          (i32.load offset=56
            (local.get $l4))))
      (local.set $l6
        (i32.load offset=48
          (local.get $l4)))
      (local.set $l7
        (i32.load offset=76
          (local.get $l4)))
      (i32.store offset=76
        (local.get $l4)
        (i32.add
          (local.get $l7)
          (i32.const 4)))
      (i32.store
        (local.get $l7)
        (local.get $l6))
      (local.set $l8
        (i32.add
          (i32.load offset=68
            (local.get $l4))
          (i32.const -1)))
      (i32.store offset=68
        (local.get $l4)
        (local.get $l8))
      (br_if $L0
        (local.get $l8)))
    (local.set $l9
      (i32.load offset=56
        (local.get $l4)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l4)
        (i32.const 80)))
    (return
      (local.get $l9)))
  (func $mpn_submul_1 (type $t8) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (result i32)
    (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32)
    (local.set $l4
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 80)))
    (global.set $__stack_pointer
      (local.get $l4))
    (i32.store offset=76
      (local.get $l4)
      (local.get $p0))
    (i32.store offset=72
      (local.get $l4)
      (local.get $p1))
    (i32.store offset=68
      (local.get $l4)
      (local.get $p2))
    (i32.store offset=64
      (local.get $l4)
      (local.get $p3))
    (call $assert
      (i32.and
        (i32.ge_s
          (i32.load offset=68
            (local.get $l4))
          (i32.const 1))
        (i32.const 1)))
    (i32.store offset=56
      (local.get $l4)
      (i32.const 0))
    (loop $L0
      (local.set $l5
        (i32.load offset=72
          (local.get $l4)))
      (i32.store offset=72
        (local.get $l4)
        (i32.add
          (local.get $l5)
          (i32.const 4)))
      (i32.store offset=60
        (local.get $l4)
        (i32.load
          (local.get $l5)))
      (i32.store offset=40
        (local.get $l4)
        (i32.const 32))
      (i32.store offset=4
        (local.get $l4)
        (i32.load offset=60
          (local.get $l4)))
      (i32.store
        (local.get $l4)
        (i32.load offset=64
          (local.get $l4)))
      (call $assert
        (i32.const 1))
      (i32.store offset=20
        (local.get $l4)
        (i32.and
          (i32.load offset=4
            (local.get $l4))
          (i32.const 65535)))
      (i32.store offset=12
        (local.get $l4)
        (i32.shr_u
          (i32.load offset=4
            (local.get $l4))
          (i32.const 16)))
      (i32.store offset=16
        (local.get $l4)
        (i32.and
          (i32.load
            (local.get $l4))
          (i32.const 65535)))
      (i32.store offset=8
        (local.get $l4)
        (i32.shr_u
          (i32.load
            (local.get $l4))
          (i32.const 16)))
      (i32.store offset=36
        (local.get $l4)
        (i32.mul
          (i32.load offset=20
            (local.get $l4))
          (i32.load offset=16
            (local.get $l4))))
      (i32.store offset=32
        (local.get $l4)
        (i32.mul
          (i32.load offset=20
            (local.get $l4))
          (i32.load offset=8
            (local.get $l4))))
      (i32.store offset=28
        (local.get $l4)
        (i32.mul
          (i32.load offset=12
            (local.get $l4))
          (i32.load offset=16
            (local.get $l4))))
      (i32.store offset=24
        (local.get $l4)
        (i32.mul
          (i32.load offset=12
            (local.get $l4))
          (i32.load offset=8
            (local.get $l4))))
      (i32.store offset=32
        (local.get $l4)
        (i32.add
          (i32.shr_u
            (i32.load offset=36
              (local.get $l4))
            (i32.const 16))
          (i32.load offset=32
            (local.get $l4))))
      (i32.store offset=32
        (local.get $l4)
        (i32.add
          (i32.load offset=28
            (local.get $l4))
          (i32.load offset=32
            (local.get $l4))))
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.and
              (i32.lt_u
                (i32.load offset=32
                  (local.get $l4))
                (i32.load offset=28
                  (local.get $l4)))
              (i32.const 1))))
        (i32.store offset=24
          (local.get $l4)
          (i32.add
            (i32.load offset=24
              (local.get $l4))
            (i32.const 65536))))
      (i32.store offset=52
        (local.get $l4)
        (i32.add
          (i32.load offset=24
            (local.get $l4))
          (i32.shr_u
            (i32.load offset=32
              (local.get $l4))
            (i32.const 16))))
      (i32.store offset=48
        (local.get $l4)
        (i32.add
          (i32.shl
            (i32.load offset=32
              (local.get $l4))
            (i32.const 16))
          (i32.and
            (i32.load offset=36
              (local.get $l4))
            (i32.const 65535))))
      (i32.store offset=48
        (local.get $l4)
        (i32.add
          (i32.load offset=56
            (local.get $l4))
          (i32.load offset=48
            (local.get $l4))))
      (i32.store offset=56
        (local.get $l4)
        (i32.add
          (i32.and
            (i32.lt_u
              (i32.load offset=48
                (local.get $l4))
              (i32.load offset=56
                (local.get $l4)))
            (i32.const 1))
          (i32.load offset=52
            (local.get $l4))))
      (i32.store offset=44
        (local.get $l4)
        (i32.load
          (i32.load offset=76
            (local.get $l4))))
      (i32.store offset=48
        (local.get $l4)
        (i32.sub
          (i32.load offset=44
            (local.get $l4))
          (i32.load offset=48
            (local.get $l4))))
      (i32.store offset=56
        (local.get $l4)
        (i32.add
          (i32.and
            (i32.gt_u
              (i32.load offset=48
                (local.get $l4))
              (i32.load offset=44
                (local.get $l4)))
            (i32.const 1))
          (i32.load offset=56
            (local.get $l4))))
      (local.set $l6
        (i32.load offset=48
          (local.get $l4)))
      (local.set $l7
        (i32.load offset=76
          (local.get $l4)))
      (i32.store offset=76
        (local.get $l4)
        (i32.add
          (local.get $l7)
          (i32.const 4)))
      (i32.store
        (local.get $l7)
        (local.get $l6))
      (local.set $l8
        (i32.add
          (i32.load offset=68
            (local.get $l4))
          (i32.const -1)))
      (i32.store offset=68
        (local.get $l4)
        (local.get $l8))
      (br_if $L0
        (local.get $l8)))
    (local.set $l9
      (i32.load offset=56
        (local.get $l4)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l4)
        (i32.const 80)))
    (return
      (local.get $l9)))
  (func $mpn_mul (type $t9) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (param $p4 i32) (result i32)
    (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32) (local $l16 i32) (local $l17 i32)
    (local.set $l5
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 32)))
    (global.set $__stack_pointer
      (local.get $l5))
    (i32.store offset=28
      (local.get $l5)
      (local.get $p0))
    (i32.store offset=24
      (local.get $l5)
      (local.get $p1))
    (i32.store offset=20
      (local.get $l5)
      (local.get $p2))
    (i32.store offset=16
      (local.get $l5)
      (local.get $p3))
    (i32.store offset=12
      (local.get $l5)
      (local.get $p4))
    (call $assert
      (i32.and
        (i32.ge_s
          (i32.load offset=20
            (local.get $l5))
          (i32.load offset=12
            (local.get $l5)))
        (i32.const 1)))
    (call $assert
      (i32.and
        (i32.ge_s
          (i32.load offset=12
            (local.get $l5))
          (i32.const 1))
        (i32.const 1)))
    (local.set $l6
      (i32.gt_u
        (i32.add
          (i32.load offset=28
            (local.get $l5))
          (i32.shl
            (i32.add
              (i32.load offset=20
                (local.get $l5))
              (i32.load offset=12
                (local.get $l5)))
            (i32.const 2)))
        (i32.load offset=24
          (local.get $l5))))
    (local.set $l7
      (i32.const 0))
    (local.set $l8
      (i32.and
        (local.get $l6)
        (i32.const 1)))
    (local.set $l9
      (local.get $l7))
    (block $B0
      (br_if $B0
        (i32.eqz
          (local.get $l8)))
      (local.set $l9
        (i32.gt_u
          (i32.add
            (i32.load offset=24
              (local.get $l5))
            (i32.shl
              (i32.load offset=20
                (local.get $l5))
              (i32.const 2)))
          (i32.load offset=28
            (local.get $l5)))))
    (call $assert
      (i32.and
        (i32.xor
          (local.get $l9)
          (i32.const -1))
        (i32.const 1)))
    (local.set $l10
      (i32.gt_u
        (i32.add
          (i32.load offset=28
            (local.get $l5))
          (i32.shl
            (i32.add
              (i32.load offset=20
                (local.get $l5))
              (i32.load offset=12
                (local.get $l5)))
            (i32.const 2)))
        (i32.load offset=16
          (local.get $l5))))
    (local.set $l11
      (i32.const 0))
    (local.set $l12
      (i32.and
        (local.get $l10)
        (i32.const 1)))
    (local.set $l13
      (local.get $l11))
    (block $B1
      (br_if $B1
        (i32.eqz
          (local.get $l12)))
      (local.set $l13
        (i32.gt_u
          (i32.add
            (i32.load offset=16
              (local.get $l5))
            (i32.shl
              (i32.load offset=12
                (local.get $l5))
              (i32.const 2)))
          (i32.load offset=28
            (local.get $l5)))))
    (call $assert
      (i32.and
        (i32.xor
          (local.get $l13)
          (i32.const -1))
        (i32.const 1)))
    (local.set $l14
      (call $mpn_mul_1
        (i32.load offset=28
          (local.get $l5))
        (i32.load offset=24
          (local.get $l5))
        (i32.load offset=20
          (local.get $l5))
        (i32.load
          (i32.load offset=16
            (local.get $l5)))))
    (i32.store
      (i32.add
        (i32.load offset=28
          (local.get $l5))
        (i32.shl
          (i32.load offset=20
            (local.get $l5))
          (i32.const 2)))
      (local.get $l14))
    (block $B2
      (loop $L3
        (local.set $l15
          (i32.add
            (i32.load offset=12
              (local.get $l5))
            (i32.const -1)))
        (i32.store offset=12
          (local.get $l5)
          (local.get $l15))
        (br_if $B2
          (i32.eqz
            (i32.and
              (i32.ge_s
                (local.get $l15)
                (i32.const 1))
              (i32.const 1))))
        (i32.store offset=28
          (local.get $l5)
          (i32.add
            (i32.load offset=28
              (local.get $l5))
            (i32.const 4)))
        (i32.store offset=16
          (local.get $l5)
          (i32.add
            (i32.load offset=16
              (local.get $l5))
            (i32.const 4)))
        (local.set $l16
          (call $mpn_addmul_1
            (i32.load offset=28
              (local.get $l5))
            (i32.load offset=24
              (local.get $l5))
            (i32.load offset=20
              (local.get $l5))
            (i32.load
              (i32.load offset=16
                (local.get $l5)))))
        (i32.store
          (i32.add
            (i32.load offset=28
              (local.get $l5))
            (i32.shl
              (i32.load offset=20
                (local.get $l5))
              (i32.const 2)))
          (local.get $l16))
        (br $L3)))
    (local.set $l17
      (i32.load
        (i32.add
          (i32.load offset=28
            (local.get $l5))
          (i32.shl
            (i32.load offset=20
              (local.get $l5))
            (i32.const 2)))))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l5)
        (i32.const 32)))
    (return
      (local.get $l17)))
  (func $mpn_mul_n (type $t10) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32)
    (local $l4 i32)
    (local.set $l4
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l4))
    (i32.store offset=12
      (local.get $l4)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l4)
      (local.get $p1))
    (i32.store offset=4
      (local.get $l4)
      (local.get $p2))
    (i32.store
      (local.get $l4)
      (local.get $p3))
    (drop
      (call $mpn_mul
        (i32.load offset=12
          (local.get $l4))
        (i32.load offset=8
          (local.get $l4))
        (i32.load
          (local.get $l4))
        (i32.load offset=4
          (local.get $l4))
        (i32.load
          (local.get $l4))))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l4)
        (i32.const 16)))
    (return))
  (func $mpn_sqr (type $t7) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l3))
    (i32.store offset=12
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l3)
      (local.get $p1))
    (i32.store offset=4
      (local.get $l3)
      (local.get $p2))
    (drop
      (call $mpn_mul
        (i32.load offset=12
          (local.get $l3))
        (i32.load offset=8
          (local.get $l3))
        (i32.load offset=4
          (local.get $l3))
        (i32.load offset=8
          (local.get $l3))
        (i32.load offset=4
          (local.get $l3))))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l3)
        (i32.const 16)))
    (return))
  (func $mpn_lshift (type $t8) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (result i32)
    (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32)
    (local.set $l4
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 32)))
    (global.set $__stack_pointer
      (local.get $l4))
    (i32.store offset=28
      (local.get $l4)
      (local.get $p0))
    (i32.store offset=24
      (local.get $l4)
      (local.get $p1))
    (i32.store offset=20
      (local.get $l4)
      (local.get $p2))
    (i32.store offset=16
      (local.get $l4)
      (local.get $p3))
    (call $assert
      (i32.and
        (i32.ge_s
          (i32.load offset=20
            (local.get $l4))
          (i32.const 1))
        (i32.const 1)))
    (call $assert
      (i32.and
        (i32.ge_u
          (i32.load offset=16
            (local.get $l4))
          (i32.const 1))
        (i32.const 1)))
    (call $assert
      (i32.and
        (i32.lt_u
          (i32.load offset=16
            (local.get $l4))
          (i32.const 32))
        (i32.const 1)))
    (local.set $l5
      (i32.load offset=20
        (local.get $l4)))
    (i32.store offset=24
      (local.get $l4)
      (i32.add
        (i32.load offset=24
          (local.get $l4))
        (i32.shl
          (local.get $l5)
          (i32.const 2))))
    (local.set $l6
      (i32.load offset=20
        (local.get $l4)))
    (i32.store offset=28
      (local.get $l4)
      (i32.add
        (i32.load offset=28
          (local.get $l4))
        (i32.shl
          (local.get $l6)
          (i32.const 2))))
    (local.set $l7
      (i32.load offset=16
        (local.get $l4)))
    (i32.store offset=4
      (local.get $l4)
      (i32.sub
        (i32.const 32)
        (local.get $l7)))
    (local.set $l8
      (i32.add
        (i32.load offset=24
          (local.get $l4))
        (i32.const -4)))
    (i32.store offset=24
      (local.get $l4)
      (local.get $l8))
    (i32.store offset=8
      (local.get $l4)
      (i32.load
        (local.get $l8)))
    (i32.store
      (local.get $l4)
      (i32.shr_u
        (i32.load offset=8
          (local.get $l4))
        (i32.load offset=4
          (local.get $l4))))
    (i32.store offset=12
      (local.get $l4)
      (i32.shl
        (i32.load offset=8
          (local.get $l4))
        (i32.load offset=16
          (local.get $l4))))
    (block $B0
      (loop $L1
        (local.set $l9
          (i32.add
            (i32.load offset=20
              (local.get $l4))
            (i32.const -1)))
        (i32.store offset=20
          (local.get $l4)
          (local.get $l9))
        (br_if $B0
          (i32.eqz
            (local.get $l9)))
        (local.set $l10
          (i32.add
            (i32.load offset=24
              (local.get $l4))
            (i32.const -4)))
        (i32.store offset=24
          (local.get $l4)
          (local.get $l10))
        (i32.store offset=8
          (local.get $l4)
          (i32.load
            (local.get $l10)))
        (local.set $l11
          (i32.or
            (i32.load offset=12
              (local.get $l4))
            (i32.shr_u
              (i32.load offset=8
                (local.get $l4))
              (i32.load offset=4
                (local.get $l4)))))
        (local.set $l12
          (i32.add
            (i32.load offset=28
              (local.get $l4))
            (i32.const -4)))
        (i32.store offset=28
          (local.get $l4)
          (local.get $l12))
        (i32.store
          (local.get $l12)
          (local.get $l11))
        (i32.store offset=12
          (local.get $l4)
          (i32.shl
            (i32.load offset=8
              (local.get $l4))
            (i32.load offset=16
              (local.get $l4))))
        (br $L1)))
    (local.set $l13
      (i32.load offset=12
        (local.get $l4)))
    (local.set $l14
      (i32.add
        (i32.load offset=28
          (local.get $l4))
        (i32.const -4)))
    (i32.store offset=28
      (local.get $l4)
      (local.get $l14))
    (i32.store
      (local.get $l14)
      (local.get $l13))
    (local.set $l15
      (i32.load
        (local.get $l4)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l4)
        (i32.const 32)))
    (return
      (local.get $l15)))
  (func $mpn_rshift (type $t8) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (result i32)
    (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32)
    (local.set $l4
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 32)))
    (global.set $__stack_pointer
      (local.get $l4))
    (i32.store offset=28
      (local.get $l4)
      (local.get $p0))
    (i32.store offset=24
      (local.get $l4)
      (local.get $p1))
    (i32.store offset=20
      (local.get $l4)
      (local.get $p2))
    (i32.store offset=16
      (local.get $l4)
      (local.get $p3))
    (call $assert
      (i32.and
        (i32.ge_s
          (i32.load offset=20
            (local.get $l4))
          (i32.const 1))
        (i32.const 1)))
    (call $assert
      (i32.and
        (i32.ge_u
          (i32.load offset=16
            (local.get $l4))
          (i32.const 1))
        (i32.const 1)))
    (call $assert
      (i32.and
        (i32.lt_u
          (i32.load offset=16
            (local.get $l4))
          (i32.const 32))
        (i32.const 1)))
    (local.set $l5
      (i32.load offset=16
        (local.get $l4)))
    (i32.store offset=4
      (local.get $l4)
      (i32.sub
        (i32.const 32)
        (local.get $l5)))
    (local.set $l6
      (i32.load offset=24
        (local.get $l4)))
    (i32.store offset=24
      (local.get $l4)
      (i32.add
        (local.get $l6)
        (i32.const 4)))
    (i32.store offset=12
      (local.get $l4)
      (i32.load
        (local.get $l6)))
    (i32.store
      (local.get $l4)
      (i32.shl
        (i32.load offset=12
          (local.get $l4))
        (i32.load offset=4
          (local.get $l4))))
    (i32.store offset=8
      (local.get $l4)
      (i32.shr_u
        (i32.load offset=12
          (local.get $l4))
        (i32.load offset=16
          (local.get $l4))))
    (block $B0
      (loop $L1
        (local.set $l7
          (i32.add
            (i32.load offset=20
              (local.get $l4))
            (i32.const -1)))
        (i32.store offset=20
          (local.get $l4)
          (local.get $l7))
        (br_if $B0
          (i32.eqz
            (local.get $l7)))
        (local.set $l8
          (i32.load offset=24
            (local.get $l4)))
        (i32.store offset=24
          (local.get $l4)
          (i32.add
            (local.get $l8)
            (i32.const 4)))
        (i32.store offset=12
          (local.get $l4)
          (i32.load
            (local.get $l8)))
        (local.set $l9
          (i32.or
            (i32.load offset=8
              (local.get $l4))
            (i32.shl
              (i32.load offset=12
                (local.get $l4))
              (i32.load offset=4
                (local.get $l4)))))
        (local.set $l10
          (i32.load offset=28
            (local.get $l4)))
        (i32.store offset=28
          (local.get $l4)
          (i32.add
            (local.get $l10)
            (i32.const 4)))
        (i32.store
          (local.get $l10)
          (local.get $l9))
        (i32.store offset=8
          (local.get $l4)
          (i32.shr_u
            (i32.load offset=12
              (local.get $l4))
            (i32.load offset=16
              (local.get $l4))))
        (br $L1)))
    (local.set $l11
      (i32.load offset=8
        (local.get $l4)))
    (i32.store
      (i32.load offset=28
        (local.get $l4))
      (local.get $l11))
    (local.set $l12
      (i32.load
        (local.get $l4)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l4)
        (i32.const 32)))
    (return
      (local.get $l12)))
  (func $mpn_common_scan (type $t9) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (param $p4 i32) (result i32)
    (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32) (local $l16 i32) (local $l17 i32) (local $l18 i32) (local $l19 i32)
    (local.set $l5
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 48)))
    (global.set $__stack_pointer
      (local.get $l5))
    (i32.store offset=40
      (local.get $l5)
      (local.get $p0))
    (i32.store offset=36
      (local.get $l5)
      (local.get $p1))
    (i32.store offset=32
      (local.get $l5)
      (local.get $p2))
    (i32.store offset=28
      (local.get $l5)
      (local.get $p3))
    (i32.store offset=24
      (local.get $l5)
      (local.get $p4))
    (local.set $l6
      (i32.load offset=24
        (local.get $l5)))
    (local.set $l7
      (i32.const 1))
    (block $B0
      (br_if $B0
        (i32.eqz
          (local.get $l6)))
      (local.set $l7
        (i32.eq
          (i32.load offset=24
            (local.get $l5))
          (i32.const -1))))
    (call $assert
      (i32.and
        (local.get $l7)
        (i32.const 1)))
    (local.set $l8
      (i32.load offset=36
        (local.get $l5)))
    (local.set $l9
      (i32.le_s
        (i32.const 0)
        (local.get $l8)))
    (local.set $l10
      (i32.const 0))
    (local.set $l11
      (i32.and
        (local.get $l9)
        (i32.const 1)))
    (local.set $l12
      (local.get $l10))
    (block $B1
      (br_if $B1
        (i32.eqz
          (local.get $l11)))
      (local.set $l12
        (i32.le_s
          (i32.load offset=36
            (local.get $l5))
          (i32.load offset=28
            (local.get $l5)))))
    (call $assert
      (i32.and
        (local.get $l12)
        (i32.const 1)))
    (block $B2
      (block $B3
        (loop $L4
          (br_if $B3
            (i32.load offset=40
              (local.get $l5)))
          (i32.store offset=36
            (local.get $l5)
            (i32.add
              (i32.load offset=36
                (local.get $l5))
              (i32.const 1)))
          (block $B5
            (br_if $B5
              (i32.eqz
                (i32.and
                  (i32.eq
                    (i32.load offset=36
                      (local.get $l5))
                    (i32.load offset=28
                      (local.get $l5)))
                  (i32.const 1))))
            (block $B6
              (block $B7
                (br_if $B7
                  (i32.load offset=24
                    (local.get $l5)))
                (local.set $l13
                  (i32.const -1))
                (br $B6))
              (local.set $l13
                (i32.shl
                  (i32.load offset=28
                    (local.get $l5))
                  (i32.const 5))))
            (i32.store offset=44
              (local.get $l5)
              (local.get $l13))
            (br $B2))
          (i32.store offset=40
            (local.get $l5)
            (i32.xor
              (i32.load offset=24
                (local.get $l5))
              (i32.load
                (i32.add
                  (i32.load offset=32
                    (local.get $l5))
                  (i32.shl
                    (i32.load offset=36
                      (local.get $l5))
                    (i32.const 2))))))
          (br $L4)))
      (i32.store offset=16
        (local.get $l5)
        (i32.load offset=40
          (local.get $l5)))
      (i32.store offset=12
        (local.get $l5)
        (i32.const 0))
      (local.set $l14
        (i32.load offset=16
          (local.get $l5)))
      (local.set $l15
        (i32.load offset=16
          (local.get $l5)))
      (i32.store offset=8
        (local.get $l5)
        (i32.and
          (local.get $l14)
          (i32.sub
            (i32.const 0)
            (local.get $l15))))
      (i32.store offset=4
        (local.get $l5)
        (i32.const 0))
      (i32.store
        (local.get $l5)
        (i32.const 8))
      (local.set $l16
        (i32.load
          (local.get $l5)))
      (block $B8
        (br_if $B8
          (i32.eqz
            (i32.and
              (i32.gt_u
                (i32.const 32)
                (local.get $l16))
              (i32.const 1))))
        (block $B9
          (loop $L10
            (br_if $B9
              (i32.and
                (i32.load offset=8
                  (local.get $l5))
                (i32.const -16777216)))
            (local.set $l17
              (i32.load
                (local.get $l5)))
            (i32.store offset=8
              (local.get $l5)
              (i32.shl
                (i32.load offset=8
                  (local.get $l5))
                (local.get $l17)))
            (i32.store offset=4
              (local.get $l5)
              (i32.add
                (i32.load offset=4
                  (local.get $l5))
                (i32.const 8)))
            (br $L10))))
      (block $B11
        (loop $L12
          (br_if $B11
            (i32.and
              (i32.load offset=8
                (local.get $l5))
              (i32.const -2147483648)))
          (i32.store offset=8
            (local.get $l5)
            (i32.shl
              (i32.load offset=8
                (local.get $l5))
              (i32.const 1)))
          (i32.store offset=4
            (local.get $l5)
            (i32.add
              (i32.load offset=4
                (local.get $l5))
              (i32.const 1)))
          (br $L12)))
      (i32.store offset=12
        (local.get $l5)
        (i32.load offset=4
          (local.get $l5)))
      (local.set $l18
        (i32.load offset=12
          (local.get $l5)))
      (i32.store offset=20
        (local.get $l5)
        (i32.sub
          (i32.const 31)
          (local.get $l18)))
      (i32.store offset=44
        (local.get $l5)
        (i32.add
          (i32.shl
            (i32.load offset=36
              (local.get $l5))
            (i32.const 5))
          (i32.load offset=20
            (local.get $l5)))))
    (local.set $l19
      (i32.load offset=44
        (local.get $l5)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l5)
        (i32.const 48)))
    (return
      (local.get $l19)))
  (func $mpn_scan1 (type $t5) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l2))
    (i32.store offset=12
      (local.get $l2)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l2)
      (local.get $p1))
    (i32.store offset=4
      (local.get $l2)
      (i32.shr_u
        (i32.load offset=8
          (local.get $l2))
        (i32.const 5)))
    (local.set $l3
      (i32.load
        (i32.add
          (i32.load offset=12
            (local.get $l2))
          (i32.shl
            (i32.load offset=4
              (local.get $l2))
            (i32.const 2)))))
    (local.set $l4
      (i32.and
        (i32.load offset=8
          (local.get $l2))
        (i32.const 31)))
    (local.set $l5
      (call $mpn_common_scan
        (i32.and
          (local.get $l3)
          (i32.shl
            (i32.const -1)
            (local.get $l4)))
        (i32.load offset=4
          (local.get $l2))
        (i32.load offset=12
          (local.get $l2))
        (i32.load offset=4
          (local.get $l2))
        (i32.const 0)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l2)
        (i32.const 16)))
    (return
      (local.get $l5)))
  (func $mpn_scan0 (type $t5) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l2))
    (i32.store offset=12
      (local.get $l2)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l2)
      (local.get $p1))
    (i32.store offset=4
      (local.get $l2)
      (i32.shr_u
        (i32.load offset=8
          (local.get $l2))
        (i32.const 5)))
    (local.set $l3
      (i32.xor
        (i32.load
          (i32.add
            (i32.load offset=12
              (local.get $l2))
            (i32.shl
              (i32.load offset=4
                (local.get $l2))
              (i32.const 2))))
        (i32.const -1)))
    (local.set $l4
      (i32.and
        (i32.load offset=8
          (local.get $l2))
        (i32.const 31)))
    (local.set $l5
      (call $mpn_common_scan
        (i32.and
          (local.get $l3)
          (i32.shl
            (i32.const -1)
            (local.get $l4)))
        (i32.load offset=4
          (local.get $l2))
        (i32.load offset=12
          (local.get $l2))
        (i32.load offset=4
          (local.get $l2))
        (i32.const -1)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l2)
        (i32.const 16)))
    (return
      (local.get $l5)))
  (func $mpn_com (type $t7) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (i32.store offset=12
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l3)
      (local.get $p1))
    (i32.store offset=4
      (local.get $l3)
      (local.get $p2))
    (block $B0
      (loop $L1
        (local.set $l4
          (i32.add
            (i32.load offset=4
              (local.get $l3))
            (i32.const -1)))
        (i32.store offset=4
          (local.get $l3)
          (local.get $l4))
        (br_if $B0
          (i32.eqz
            (i32.and
              (i32.ge_s
                (local.get $l4)
                (i32.const 0))
              (i32.const 1))))
        (local.set $l5
          (i32.load offset=8
            (local.get $l3)))
        (i32.store offset=8
          (local.get $l3)
          (i32.add
            (local.get $l5)
            (i32.const 4)))
        (local.set $l6
          (i32.xor
            (i32.load
              (local.get $l5))
            (i32.const -1)))
        (local.set $l7
          (i32.load offset=12
            (local.get $l3)))
        (i32.store offset=12
          (local.get $l3)
          (i32.add
            (local.get $l7)
            (i32.const 4)))
        (i32.store
          (local.get $l7)
          (local.get $l6))
        (br $L1)))
    (return))
  (func $mpn_neg (type $t1) (param $p0 i32) (param $p1 i32) (param $p2 i32) (result i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l3))
    (i32.store offset=8
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=4
      (local.get $l3)
      (local.get $p1))
    (i32.store
      (local.get $l3)
      (local.get $p2))
    (block $B0
      (block $B1
        (loop $L2
          (br_if $B1
            (i32.load
              (i32.load offset=4
                (local.get $l3))))
          (i32.store
            (i32.load offset=8
              (local.get $l3))
            (i32.const 0))
          (local.set $l4
            (i32.add
              (i32.load
                (local.get $l3))
              (i32.const -1)))
          (i32.store
            (local.get $l3)
            (local.get $l4))
          (block $B3
            (br_if $B3
              (local.get $l4))
            (i32.store offset=12
              (local.get $l3)
              (i32.const 0))
            (br $B0))
          (i32.store offset=4
            (local.get $l3)
            (i32.add
              (i32.load offset=4
                (local.get $l3))
              (i32.const 4)))
          (i32.store offset=8
            (local.get $l3)
            (i32.add
              (i32.load offset=8
                (local.get $l3))
              (i32.const 4)))
          (br $L2)))
      (local.set $l5
        (i32.load
          (i32.load offset=4
            (local.get $l3))))
      (local.set $l6
        (i32.sub
          (i32.const 0)
          (local.get $l5)))
      (i32.store
        (i32.load offset=8
          (local.get $l3))
        (local.get $l6))
      (local.set $l7
        (i32.add
          (i32.load offset=8
            (local.get $l3))
          (i32.const 4)))
      (i32.store offset=8
        (local.get $l3)
        (local.get $l7))
      (local.set $l8
        (i32.add
          (i32.load offset=4
            (local.get $l3))
          (i32.const 4)))
      (i32.store offset=4
        (local.get $l3)
        (local.get $l8))
      (local.set $l9
        (i32.add
          (i32.load
            (local.get $l3))
          (i32.const -1)))
      (i32.store
        (local.get $l3)
        (local.get $l9))
      (call $mpn_com
        (local.get $l7)
        (local.get $l8)
        (local.get $l9))
      (i32.store offset=12
        (local.get $l3)
        (i32.const 1)))
    (local.set $l10
      (i32.load offset=12
        (local.get $l3)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l3)
        (i32.const 16)))
    (return
      (local.get $l10)))
  (func $mpn_invert_3by2 (type $t5) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 96)))
    (global.set $__stack_pointer
      (local.get $l2))
    (i32.store offset=92
      (local.get $l2)
      (local.get $p0))
    (i32.store offset=88
      (local.get $l2)
      (local.get $p1))
    (call $assert
      (i32.const 1))
    (i32.store offset=68
      (local.get $l2)
      (i32.and
        (i32.load offset=92
          (local.get $l2))
        (i32.const 65535)))
    (i32.store offset=64
      (local.get $l2)
      (i32.shr_u
        (i32.load offset=92
          (local.get $l2))
        (i32.const 16)))
    (i32.store offset=60
      (local.get $l2)
      (i32.div_u
        (i32.xor
          (i32.load offset=92
            (local.get $l2))
          (i32.const -1))
        (i32.load offset=64
          (local.get $l2))))
    (i32.store offset=84
      (local.get $l2)
      (i32.or
        (i32.shl
          (i32.sub
            (i32.xor
              (i32.load offset=92
                (local.get $l2))
              (i32.const -1))
            (i32.mul
              (i32.load offset=60
                (local.get $l2))
              (i32.load offset=64
                (local.get $l2))))
          (i32.const 16))
        (i32.const 65535)))
    (i32.store offset=76
      (local.get $l2)
      (i32.mul
        (i32.load offset=60
          (local.get $l2))
        (i32.load offset=68
          (local.get $l2))))
    (block $B0
      (br_if $B0
        (i32.eqz
          (i32.and
            (i32.lt_u
              (i32.load offset=84
                (local.get $l2))
              (i32.load offset=76
                (local.get $l2)))
            (i32.const 1))))
      (i32.store offset=60
        (local.get $l2)
        (i32.add
          (i32.load offset=60
            (local.get $l2))
          (i32.const -1)))
      (i32.store offset=84
        (local.get $l2)
        (i32.add
          (i32.load offset=92
            (local.get $l2))
          (i32.load offset=84
            (local.get $l2))))
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.and
              (i32.ge_u
                (i32.load offset=84
                  (local.get $l2))
                (i32.load offset=92
                  (local.get $l2)))
              (i32.const 1))))
        (block $B2
          (br_if $B2
            (i32.eqz
              (i32.and
                (i32.lt_u
                  (i32.load offset=84
                    (local.get $l2))
                  (i32.load offset=76
                    (local.get $l2)))
                (i32.const 1))))
          (i32.store offset=60
            (local.get $l2)
            (i32.add
              (i32.load offset=60
                (local.get $l2))
              (i32.const -1)))
          (i32.store offset=84
            (local.get $l2)
            (i32.add
              (i32.load offset=92
                (local.get $l2))
              (i32.load offset=84
                (local.get $l2)))))))
    (local.set $l3
      (i32.load offset=76
        (local.get $l2)))
    (i32.store offset=84
      (local.get $l2)
      (i32.sub
        (i32.load offset=84
          (local.get $l2))
        (local.get $l3)))
    (i32.store offset=76
      (local.get $l2)
      (i32.add
        (i32.mul
          (i32.shr_u
            (i32.load offset=84
              (local.get $l2))
            (i32.const 16))
          (i32.load offset=60
            (local.get $l2)))
        (i32.load offset=84
          (local.get $l2))))
    (i32.store offset=72
      (local.get $l2)
      (i32.add
        (i32.shr_u
          (i32.load offset=76
            (local.get $l2))
          (i32.const 16))
        (i32.const 1)))
    (i32.store offset=84
      (local.get $l2)
      (i32.sub
        (i32.add
          (i32.shl
            (i32.load offset=84
              (local.get $l2))
            (i32.const 16))
          (i32.const 65535))
        (i32.mul
          (i32.load offset=72
            (local.get $l2))
          (i32.load offset=92
            (local.get $l2)))))
    (block $B3
      (br_if $B3
        (i32.eqz
          (i32.and
            (i32.ge_u
              (i32.load offset=84
                (local.get $l2))
              (i32.and
                (i32.shl
                  (i32.load offset=76
                    (local.get $l2))
                  (i32.const 16))
                (i32.const -1)))
            (i32.const 1))))
      (i32.store offset=72
        (local.get $l2)
        (i32.add
          (i32.load offset=72
            (local.get $l2))
          (i32.const -1)))
      (i32.store offset=84
        (local.get $l2)
        (i32.add
          (i32.load offset=92
            (local.get $l2))
          (i32.load offset=84
            (local.get $l2)))))
    (i32.store offset=80
      (local.get $l2)
      (i32.add
        (i32.shl
          (i32.load offset=60
            (local.get $l2))
          (i32.const 16))
        (i32.load offset=72
          (local.get $l2))))
    (block $B4
      (br_if $B4
        (i32.eqz
          (i32.and
            (i32.ge_u
              (i32.load offset=84
                (local.get $l2))
              (i32.load offset=92
                (local.get $l2)))
            (i32.const 1))))
      (i32.store offset=80
        (local.get $l2)
        (i32.add
          (i32.load offset=80
            (local.get $l2))
          (i32.const 1)))
      (local.set $l4
        (i32.load offset=92
          (local.get $l2)))
      (i32.store offset=84
        (local.get $l2)
        (i32.sub
          (i32.load offset=84
            (local.get $l2))
          (local.get $l4))))
    (block $B5
      (br_if $B5
        (i32.eqz
          (i32.and
            (i32.gt_u
              (i32.load offset=88
                (local.get $l2))
              (i32.const 0))
            (i32.const 1))))
      (i32.store offset=84
        (local.get $l2)
        (i32.xor
          (i32.load offset=84
            (local.get $l2))
          (i32.const -1)))
      (i32.store offset=84
        (local.get $l2)
        (i32.add
          (i32.load offset=88
            (local.get $l2))
          (i32.load offset=84
            (local.get $l2))))
      (block $B6
        (br_if $B6
          (i32.eqz
            (i32.and
              (i32.lt_u
                (i32.load offset=84
                  (local.get $l2))
                (i32.load offset=88
                  (local.get $l2)))
              (i32.const 1))))
        (i32.store offset=80
          (local.get $l2)
          (i32.add
            (i32.load offset=80
              (local.get $l2))
            (i32.const -1)))
        (block $B7
          (br_if $B7
            (i32.eqz
              (i32.and
                (i32.ge_u
                  (i32.load offset=84
                    (local.get $l2))
                  (i32.load offset=92
                    (local.get $l2)))
                (i32.const 1))))
          (i32.store offset=80
            (local.get $l2)
            (i32.add
              (i32.load offset=80
                (local.get $l2))
              (i32.const -1)))
          (local.set $l5
            (i32.load offset=92
              (local.get $l2)))
          (i32.store offset=84
            (local.get $l2)
            (i32.sub
              (i32.load offset=84
                (local.get $l2))
              (local.get $l5))))
        (local.set $l6
          (i32.load offset=92
            (local.get $l2)))
        (i32.store offset=84
          (local.get $l2)
          (i32.sub
            (i32.load offset=84
              (local.get $l2))
            (local.get $l6))))
      (i32.store offset=48
        (local.get $l2)
        (i32.const 32))
      (i32.store offset=12
        (local.get $l2)
        (i32.load offset=88
          (local.get $l2)))
      (i32.store offset=8
        (local.get $l2)
        (i32.load offset=80
          (local.get $l2)))
      (call $assert
        (i32.const 1))
      (i32.store offset=28
        (local.get $l2)
        (i32.and
          (i32.load offset=12
            (local.get $l2))
          (i32.const 65535)))
      (i32.store offset=20
        (local.get $l2)
        (i32.shr_u
          (i32.load offset=12
            (local.get $l2))
          (i32.const 16)))
      (i32.store offset=24
        (local.get $l2)
        (i32.and
          (i32.load offset=8
            (local.get $l2))
          (i32.const 65535)))
      (i32.store offset=16
        (local.get $l2)
        (i32.shr_u
          (i32.load offset=8
            (local.get $l2))
          (i32.const 16)))
      (i32.store offset=44
        (local.get $l2)
        (i32.mul
          (i32.load offset=28
            (local.get $l2))
          (i32.load offset=24
            (local.get $l2))))
      (i32.store offset=40
        (local.get $l2)
        (i32.mul
          (i32.load offset=28
            (local.get $l2))
          (i32.load offset=16
            (local.get $l2))))
      (i32.store offset=36
        (local.get $l2)
        (i32.mul
          (i32.load offset=20
            (local.get $l2))
          (i32.load offset=24
            (local.get $l2))))
      (i32.store offset=32
        (local.get $l2)
        (i32.mul
          (i32.load offset=20
            (local.get $l2))
          (i32.load offset=16
            (local.get $l2))))
      (i32.store offset=40
        (local.get $l2)
        (i32.add
          (i32.shr_u
            (i32.load offset=44
              (local.get $l2))
            (i32.const 16))
          (i32.load offset=40
            (local.get $l2))))
      (i32.store offset=40
        (local.get $l2)
        (i32.add
          (i32.load offset=36
            (local.get $l2))
          (i32.load offset=40
            (local.get $l2))))
      (block $B8
        (br_if $B8
          (i32.eqz
            (i32.and
              (i32.lt_u
                (i32.load offset=40
                  (local.get $l2))
                (i32.load offset=36
                  (local.get $l2)))
              (i32.const 1))))
        (i32.store offset=32
          (local.get $l2)
          (i32.add
            (i32.load offset=32
              (local.get $l2))
            (i32.const 65536))))
      (i32.store offset=56
        (local.get $l2)
        (i32.add
          (i32.load offset=32
            (local.get $l2))
          (i32.shr_u
            (i32.load offset=40
              (local.get $l2))
            (i32.const 16))))
      (i32.store offset=52
        (local.get $l2)
        (i32.add
          (i32.shl
            (i32.load offset=40
              (local.get $l2))
            (i32.const 16))
          (i32.and
            (i32.load offset=44
              (local.get $l2))
            (i32.const 65535))))
      (i32.store offset=84
        (local.get $l2)
        (i32.add
          (i32.load offset=56
            (local.get $l2))
          (i32.load offset=84
            (local.get $l2))))
      (block $B9
        (br_if $B9
          (i32.eqz
            (i32.and
              (i32.lt_u
                (i32.load offset=84
                  (local.get $l2))
                (i32.load offset=56
                  (local.get $l2)))
              (i32.const 1))))
        (i32.store offset=80
          (local.get $l2)
          (i32.add
            (i32.load offset=80
              (local.get $l2))
            (i32.const -1)))
        (local.set $l7
          (i32.or
            (i32.and
              (i32.gt_u
                (i32.load offset=84
                  (local.get $l2))
                (i32.load offset=92
                  (local.get $l2)))
              (i32.const 1))
            (i32.and
              (i32.and
                (i32.eq
                  (i32.load offset=84
                    (local.get $l2))
                  (i32.load offset=92
                    (local.get $l2)))
                (i32.const 1))
              (i32.and
                (i32.gt_u
                  (i32.load offset=52
                    (local.get $l2))
                  (i32.load offset=88
                    (local.get $l2)))
                (i32.const 1)))))
        (i32.store offset=80
          (local.get $l2)
          (i32.sub
            (i32.load offset=80
              (local.get $l2))
            (local.get $l7)))))
    (local.set $l8
      (i32.load offset=80
        (local.get $l2)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l2)
        (i32.const 96)))
    (return
      (local.get $l8)))
  (func $mpn_div_qr_1_invert (type $t2) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 32)))
    (global.set $__stack_pointer
      (local.get $l2))
    (i32.store offset=28
      (local.get $l2)
      (local.get $p0))
    (i32.store offset=24
      (local.get $l2)
      (local.get $p1))
    (call $assert
      (i32.and
        (i32.gt_u
          (i32.load offset=24
            (local.get $l2))
          (i32.const 0))
        (i32.const 1)))
    (i32.store offset=16
      (local.get $l2)
      (i32.load offset=24
        (local.get $l2)))
    (i32.store offset=12
      (local.get $l2)
      (i32.const 0))
    (i32.store offset=8
      (local.get $l2)
      (i32.const 8))
    (local.set $l3
      (i32.load offset=8
        (local.get $l2)))
    (block $B0
      (br_if $B0
        (i32.eqz
          (i32.and
            (i32.gt_u
              (i32.const 32)
              (local.get $l3))
            (i32.const 1))))
      (block $B1
        (loop $L2
          (br_if $B1
            (i32.and
              (i32.load offset=16
                (local.get $l2))
              (i32.const -16777216)))
          (local.set $l4
            (i32.load offset=8
              (local.get $l2)))
          (i32.store offset=16
            (local.get $l2)
            (i32.shl
              (i32.load offset=16
                (local.get $l2))
              (local.get $l4)))
          (i32.store offset=12
            (local.get $l2)
            (i32.add
              (i32.load offset=12
                (local.get $l2))
              (i32.const 8)))
          (br $L2))))
    (block $B3
      (loop $L4
        (br_if $B3
          (i32.and
            (i32.load offset=16
              (local.get $l2))
            (i32.const -2147483648)))
        (i32.store offset=16
          (local.get $l2)
          (i32.shl
            (i32.load offset=16
              (local.get $l2))
            (i32.const 1)))
        (i32.store offset=12
          (local.get $l2)
          (i32.add
            (i32.load offset=12
              (local.get $l2))
            (i32.const 1)))
        (br $L4)))
    (i32.store offset=20
      (local.get $l2)
      (i32.load offset=12
        (local.get $l2)))
    (local.set $l5
      (i32.load offset=20
        (local.get $l2)))
    (i32.store
      (i32.load offset=28
        (local.get $l2))
      (local.get $l5))
    (local.set $l6
      (i32.shl
        (i32.load offset=24
          (local.get $l2))
        (i32.load offset=20
          (local.get $l2))))
    (i32.store offset=4
      (i32.load offset=28
        (local.get $l2))
      (local.get $l6))
    (local.set $l7
      (call $mpn_invert_3by2
        (i32.load offset=4
          (i32.load offset=28
            (local.get $l2)))
        (i32.const 0)))
    (i32.store offset=12
      (i32.load offset=28
        (local.get $l2))
      (local.get $l7))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l2)
        (i32.const 32)))
    (return))
  (func $mpn_div_qr_2_invert (type $t7) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 32)))
    (global.set $__stack_pointer
      (local.get $l3))
    (i32.store offset=28
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=24
      (local.get $l3)
      (local.get $p1))
    (i32.store offset=20
      (local.get $l3)
      (local.get $p2))
    (call $assert
      (i32.and
        (i32.gt_u
          (i32.load offset=24
            (local.get $l3))
          (i32.const 0))
        (i32.const 1)))
    (i32.store offset=12
      (local.get $l3)
      (i32.load offset=24
        (local.get $l3)))
    (i32.store offset=8
      (local.get $l3)
      (i32.const 0))
    (i32.store offset=4
      (local.get $l3)
      (i32.const 8))
    (local.set $l4
      (i32.load offset=4
        (local.get $l3)))
    (block $B0
      (br_if $B0
        (i32.eqz
          (i32.and
            (i32.gt_u
              (i32.const 32)
              (local.get $l4))
            (i32.const 1))))
      (block $B1
        (loop $L2
          (br_if $B1
            (i32.and
              (i32.load offset=12
                (local.get $l3))
              (i32.const -16777216)))
          (local.set $l5
            (i32.load offset=4
              (local.get $l3)))
          (i32.store offset=12
            (local.get $l3)
            (i32.shl
              (i32.load offset=12
                (local.get $l3))
              (local.get $l5)))
          (i32.store offset=8
            (local.get $l3)
            (i32.add
              (i32.load offset=8
                (local.get $l3))
              (i32.const 8)))
          (br $L2))))
    (block $B3
      (loop $L4
        (br_if $B3
          (i32.and
            (i32.load offset=12
              (local.get $l3))
            (i32.const -2147483648)))
        (i32.store offset=12
          (local.get $l3)
          (i32.shl
            (i32.load offset=12
              (local.get $l3))
            (i32.const 1)))
        (i32.store offset=8
          (local.get $l3)
          (i32.add
            (i32.load offset=8
              (local.get $l3))
            (i32.const 1)))
        (br $L4)))
    (i32.store offset=16
      (local.get $l3)
      (i32.load offset=8
        (local.get $l3)))
    (local.set $l6
      (i32.load offset=16
        (local.get $l3)))
    (i32.store
      (i32.load offset=28
        (local.get $l3))
      (local.get $l6))
    (block $B5
      (br_if $B5
        (i32.eqz
          (i32.and
            (i32.gt_u
              (i32.load offset=16
                (local.get $l3))
              (i32.const 0))
            (i32.const 1))))
      (local.set $l7
        (i32.shl
          (i32.load offset=24
            (local.get $l3))
          (i32.load offset=16
            (local.get $l3))))
      (local.set $l8
        (i32.load offset=20
          (local.get $l3)))
      (local.set $l9
        (i32.load offset=16
          (local.get $l3)))
      (i32.store offset=24
        (local.get $l3)
        (i32.or
          (local.get $l7)
          (i32.shr_u
            (local.get $l8)
            (i32.sub
              (i32.const 32)
              (local.get $l9)))))
      (local.set $l10
        (i32.load offset=16
          (local.get $l3)))
      (i32.store offset=20
        (local.get $l3)
        (i32.shl
          (i32.load offset=20
            (local.get $l3))
          (local.get $l10))))
    (local.set $l11
      (i32.load offset=24
        (local.get $l3)))
    (i32.store offset=4
      (i32.load offset=28
        (local.get $l3))
      (local.get $l11))
    (local.set $l12
      (i32.load offset=20
        (local.get $l3)))
    (i32.store offset=8
      (i32.load offset=28
        (local.get $l3))
      (local.get $l12))
    (local.set $l13
      (call $mpn_invert_3by2
        (i32.load offset=24
          (local.get $l3))
        (i32.load offset=20
          (local.get $l3))))
    (i32.store offset=12
      (i32.load offset=28
        (local.get $l3))
      (local.get $l13))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l3)
        (i32.const 32)))
    (return))
  (func $mpn_div_qr_invert (type $t7) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 48)))
    (global.set $__stack_pointer
      (local.get $l3))
    (i32.store offset=44
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=40
      (local.get $l3)
      (local.get $p1))
    (i32.store offset=36
      (local.get $l3)
      (local.get $p2))
    (call $assert
      (i32.and
        (i32.gt_s
          (i32.load offset=36
            (local.get $l3))
          (i32.const 0))
        (i32.const 1)))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.and
              (i32.eq
                (i32.load offset=36
                  (local.get $l3))
                (i32.const 1))
              (i32.const 1))))
        (call $mpn_div_qr_1_invert
          (i32.load offset=44
            (local.get $l3))
          (i32.load
            (i32.load offset=40
              (local.get $l3))))
        (br $B0))
      (block $B2
        (block $B3
          (br_if $B3
            (i32.eqz
              (i32.and
                (i32.eq
                  (i32.load offset=36
                    (local.get $l3))
                  (i32.const 2))
                (i32.const 1))))
          (call $mpn_div_qr_2_invert
            (i32.load offset=44
              (local.get $l3))
            (i32.load offset=4
              (i32.load offset=40
                (local.get $l3)))
            (i32.load
              (i32.load offset=40
                (local.get $l3))))
          (br $B2))
        (i32.store offset=28
          (local.get $l3)
          (i32.load
            (i32.add
              (i32.load offset=40
                (local.get $l3))
              (i32.shl
                (i32.sub
                  (i32.load offset=36
                    (local.get $l3))
                  (i32.const 1))
                (i32.const 2)))))
        (i32.store offset=24
          (local.get $l3)
          (i32.load
            (i32.add
              (i32.load offset=40
                (local.get $l3))
              (i32.shl
                (i32.sub
                  (i32.load offset=36
                    (local.get $l3))
                  (i32.const 2))
                (i32.const 2)))))
        (call $assert
          (i32.and
            (i32.gt_u
              (i32.load offset=28
                (local.get $l3))
              (i32.const 0))
            (i32.const 1)))
        (i32.store offset=20
          (local.get $l3)
          (i32.load offset=28
            (local.get $l3)))
        (i32.store offset=16
          (local.get $l3)
          (i32.const 0))
        (i32.store offset=12
          (local.get $l3)
          (i32.const 8))
        (local.set $l4
          (i32.load offset=12
            (local.get $l3)))
        (block $B4
          (br_if $B4
            (i32.eqz
              (i32.and
                (i32.gt_u
                  (i32.const 32)
                  (local.get $l4))
                (i32.const 1))))
          (block $B5
            (loop $L6
              (br_if $B5
                (i32.and
                  (i32.load offset=20
                    (local.get $l3))
                  (i32.const -16777216)))
              (local.set $l5
                (i32.load offset=12
                  (local.get $l3)))
              (i32.store offset=20
                (local.get $l3)
                (i32.shl
                  (i32.load offset=20
                    (local.get $l3))
                  (local.get $l5)))
              (i32.store offset=16
                (local.get $l3)
                (i32.add
                  (i32.load offset=16
                    (local.get $l3))
                  (i32.const 8)))
              (br $L6))))
        (block $B7
          (loop $L8
            (br_if $B7
              (i32.and
                (i32.load offset=20
                  (local.get $l3))
                (i32.const -2147483648)))
            (i32.store offset=20
              (local.get $l3)
              (i32.shl
                (i32.load offset=20
                  (local.get $l3))
                (i32.const 1)))
            (i32.store offset=16
              (local.get $l3)
              (i32.add
                (i32.load offset=16
                  (local.get $l3))
                (i32.const 1)))
            (br $L8)))
        (i32.store offset=32
          (local.get $l3)
          (i32.load offset=16
            (local.get $l3)))
        (local.set $l6
          (i32.load offset=32
            (local.get $l3)))
        (i32.store
          (i32.load offset=44
            (local.get $l3))
          (local.get $l6))
        (block $B9
          (br_if $B9
            (i32.eqz
              (i32.and
                (i32.gt_u
                  (i32.load offset=32
                    (local.get $l3))
                  (i32.const 0))
                (i32.const 1))))
          (local.set $l7
            (i32.shl
              (i32.load offset=28
                (local.get $l3))
              (i32.load offset=32
                (local.get $l3))))
          (local.set $l8
            (i32.load offset=24
              (local.get $l3)))
          (local.set $l9
            (i32.load offset=32
              (local.get $l3)))
          (i32.store offset=28
            (local.get $l3)
            (i32.or
              (local.get $l7)
              (i32.shr_u
                (local.get $l8)
                (i32.sub
                  (i32.const 32)
                  (local.get $l9)))))
          (local.set $l10
            (i32.shl
              (i32.load offset=24
                (local.get $l3))
              (i32.load offset=32
                (local.get $l3))))
          (local.set $l11
            (i32.load
              (i32.add
                (i32.load offset=40
                  (local.get $l3))
                (i32.shl
                  (i32.sub
                    (i32.load offset=36
                      (local.get $l3))
                    (i32.const 3))
                  (i32.const 2)))))
          (local.set $l12
            (i32.load offset=32
              (local.get $l3)))
          (i32.store offset=24
            (local.get $l3)
            (i32.or
              (local.get $l10)
              (i32.shr_u
                (local.get $l11)
                (i32.sub
                  (i32.const 32)
                  (local.get $l12))))))
        (local.set $l13
          (i32.load offset=28
            (local.get $l3)))
        (i32.store offset=4
          (i32.load offset=44
            (local.get $l3))
          (local.get $l13))
        (local.set $l14
          (i32.load offset=24
            (local.get $l3)))
        (i32.store offset=8
          (i32.load offset=44
            (local.get $l3))
          (local.get $l14))
        (local.set $l15
          (call $mpn_invert_3by2
            (i32.load offset=28
              (local.get $l3))
            (i32.load offset=24
              (local.get $l3))))
        (i32.store offset=12
          (i32.load offset=44
            (local.get $l3))
          (local.get $l15))))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l3)
        (i32.const 48)))
    (return))
  (func $mpn_div_qr_1_preinv (type $t8) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (result i32)
    (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32)
    (local.set $l4
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 112)))
    (global.set $__stack_pointer
      (local.get $l4))
    (i32.store offset=108
      (local.get $l4)
      (local.get $p0))
    (i32.store offset=104
      (local.get $l4)
      (local.get $p1))
    (i32.store offset=100
      (local.get $l4)
      (local.get $p2))
    (i32.store offset=96
      (local.get $l4)
      (local.get $p3))
    (i32.store offset=80
      (local.get $l4)
      (i32.const 0))
    (i32.store offset=76
      (local.get $l4)
      (i32.const 0))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.and
              (i32.gt_u
                (i32.load
                  (i32.load offset=96
                    (local.get $l4)))
                (i32.const 0))
              (i32.const 1))))
        (i32.store offset=80
          (local.get $l4)
          (i32.load offset=108
            (local.get $l4)))
        (block $B2
          (br_if $B2
            (i32.and
              (i32.ne
                (i32.load offset=80
                  (local.get $l4))
                (i32.const 0))
              (i32.const 1)))
          (i32.store offset=76
            (local.get $l4)
            (i32.load offset=100
              (local.get $l4)))
          (i32.store offset=80
            (local.get $l4)
            (call $gmp_alloc_limbs
              (i32.load offset=76
                (local.get $l4)))))
        (i32.store offset=84
          (local.get $l4)
          (call $mpn_lshift
            (i32.load offset=80
              (local.get $l4))
            (i32.load offset=104
              (local.get $l4))
            (i32.load offset=100
              (local.get $l4))
            (i32.load
              (i32.load offset=96
                (local.get $l4)))))
        (i32.store offset=104
          (local.get $l4)
          (i32.load offset=80
            (local.get $l4)))
        (br $B0))
      (i32.store offset=84
        (local.get $l4)
        (i32.const 0)))
    (i32.store offset=92
      (local.get $l4)
      (i32.load offset=4
        (i32.load offset=96
          (local.get $l4))))
    (i32.store offset=88
      (local.get $l4)
      (i32.load offset=12
        (i32.load offset=96
          (local.get $l4))))
    (block $B3
      (loop $L4
        (local.set $l5
          (i32.add
            (i32.load offset=100
              (local.get $l4))
            (i32.const -1)))
        (i32.store offset=100
          (local.get $l4)
          (local.get $l5))
        (br_if $B3
          (i32.eqz
            (i32.and
              (i32.ge_s
                (local.get $l5)
                (i32.const 0))
              (i32.const 1))))
        (i32.store offset=52
          (local.get $l4)
          (i32.const 32))
        (i32.store offset=16
          (local.get $l4)
          (i32.load offset=84
            (local.get $l4)))
        (i32.store offset=12
          (local.get $l4)
          (i32.load offset=88
            (local.get $l4)))
        (call $assert
          (i32.const 1))
        (i32.store offset=32
          (local.get $l4)
          (i32.and
            (i32.load offset=16
              (local.get $l4))
            (i32.const 65535)))
        (i32.store offset=24
          (local.get $l4)
          (i32.shr_u
            (i32.load offset=16
              (local.get $l4))
            (i32.const 16)))
        (i32.store offset=28
          (local.get $l4)
          (i32.and
            (i32.load offset=12
              (local.get $l4))
            (i32.const 65535)))
        (i32.store offset=20
          (local.get $l4)
          (i32.shr_u
            (i32.load offset=12
              (local.get $l4))
            (i32.const 16)))
        (i32.store offset=48
          (local.get $l4)
          (i32.mul
            (i32.load offset=32
              (local.get $l4))
            (i32.load offset=28
              (local.get $l4))))
        (i32.store offset=44
          (local.get $l4)
          (i32.mul
            (i32.load offset=32
              (local.get $l4))
            (i32.load offset=20
              (local.get $l4))))
        (i32.store offset=40
          (local.get $l4)
          (i32.mul
            (i32.load offset=24
              (local.get $l4))
            (i32.load offset=28
              (local.get $l4))))
        (i32.store offset=36
          (local.get $l4)
          (i32.mul
            (i32.load offset=24
              (local.get $l4))
            (i32.load offset=20
              (local.get $l4))))
        (i32.store offset=44
          (local.get $l4)
          (i32.add
            (i32.shr_u
              (i32.load offset=48
                (local.get $l4))
              (i32.const 16))
            (i32.load offset=44
              (local.get $l4))))
        (i32.store offset=44
          (local.get $l4)
          (i32.add
            (i32.load offset=40
              (local.get $l4))
            (i32.load offset=44
              (local.get $l4))))
        (block $B5
          (br_if $B5
            (i32.eqz
              (i32.and
                (i32.lt_u
                  (i32.load offset=44
                    (local.get $l4))
                  (i32.load offset=40
                    (local.get $l4)))
                (i32.const 1))))
          (i32.store offset=36
            (local.get $l4)
            (i32.add
              (i32.load offset=36
                (local.get $l4))
              (i32.const 65536))))
        (i32.store offset=68
          (local.get $l4)
          (i32.add
            (i32.load offset=36
              (local.get $l4))
            (i32.shr_u
              (i32.load offset=44
                (local.get $l4))
              (i32.const 16))))
        (i32.store offset=64
          (local.get $l4)
          (i32.add
            (i32.shl
              (i32.load offset=44
                (local.get $l4))
              (i32.const 16))
            (i32.and
              (i32.load offset=48
                (local.get $l4))
              (i32.const 65535))))
        (i32.store offset=8
          (local.get $l4)
          (i32.add
            (i32.load offset=64
              (local.get $l4))
            (i32.load
              (i32.add
                (i32.load offset=104
                  (local.get $l4))
                (i32.shl
                  (i32.load offset=100
                    (local.get $l4))
                  (i32.const 2))))))
        (i32.store offset=68
          (local.get $l4)
          (i32.add
            (i32.add
              (i32.load offset=68
                (local.get $l4))
              (i32.add
                (i32.load offset=84
                  (local.get $l4))
                (i32.const 1)))
            (i32.and
              (i32.lt_u
                (i32.load offset=8
                  (local.get $l4))
                (i32.load offset=64
                  (local.get $l4)))
              (i32.const 1))))
        (i32.store offset=64
          (local.get $l4)
          (i32.load offset=8
            (local.get $l4)))
        (i32.store offset=60
          (local.get $l4)
          (i32.sub
            (i32.load
              (i32.add
                (i32.load offset=104
                  (local.get $l4))
                (i32.shl
                  (i32.load offset=100
                    (local.get $l4))
                  (i32.const 2))))
            (i32.mul
              (i32.load offset=68
                (local.get $l4))
              (i32.load offset=92
                (local.get $l4)))))
        (local.set $l6
          (i32.and
            (i32.gt_u
              (i32.load offset=60
                (local.get $l4))
              (i32.load offset=64
                (local.get $l4)))
            (i32.const 1)))
        (i32.store offset=56
          (local.get $l4)
          (i32.sub
            (i32.const 0)
            (local.get $l6)))
        (i32.store offset=68
          (local.get $l4)
          (i32.add
            (i32.load offset=56
              (local.get $l4))
            (i32.load offset=68
              (local.get $l4))))
        (i32.store offset=60
          (local.get $l4)
          (i32.add
            (i32.and
              (i32.load offset=56
                (local.get $l4))
              (i32.load offset=92
                (local.get $l4)))
            (i32.load offset=60
              (local.get $l4))))
        (block $B6
          (br_if $B6
            (i32.eqz
              (i32.and
                (i32.ge_u
                  (i32.load offset=60
                    (local.get $l4))
                  (i32.load offset=92
                    (local.get $l4)))
                (i32.const 1))))
          (local.set $l7
            (i32.load offset=92
              (local.get $l4)))
          (i32.store offset=60
            (local.get $l4)
            (i32.sub
              (i32.load offset=60
                (local.get $l4))
              (local.get $l7)))
          (i32.store offset=68
            (local.get $l4)
            (i32.add
              (i32.load offset=68
                (local.get $l4))
              (i32.const 1))))
        (i32.store offset=84
          (local.get $l4)
          (i32.load offset=60
            (local.get $l4)))
        (i32.store offset=72
          (local.get $l4)
          (i32.load offset=68
            (local.get $l4)))
        (block $B7
          (br_if $B7
            (i32.eqz
              (i32.and
                (i32.ne
                  (i32.load offset=108
                    (local.get $l4))
                  (i32.const 0))
                (i32.const 1))))
          (local.set $l8
            (i32.load offset=72
              (local.get $l4)))
          (i32.store
            (i32.add
              (i32.load offset=108
                (local.get $l4))
              (i32.shl
                (i32.load offset=100
                  (local.get $l4))
                (i32.const 2)))
            (local.get $l8)))
        (br $L4)))
    (block $B8
      (br_if $B8
        (i32.eqz
          (i32.load offset=76
            (local.get $l4))))
      (call $gmp_free_limbs
        (i32.load offset=80
          (local.get $l4))
        (i32.load offset=76
          (local.get $l4))))
    (local.set $l9
      (i32.shr_u
        (i32.load offset=84
          (local.get $l4))
        (i32.load
          (i32.load offset=96
            (local.get $l4)))))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l4)
        (i32.const 112)))
    (return
      (local.get $l9)))
  (func $mpn_div_qr_2_preinv (type $t10) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32)
    (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32) (local $l16 i32)
    (local.set $l4
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 176)))
    (global.set $__stack_pointer
      (local.get $l4))
    (i32.store offset=172
      (local.get $l4)
      (local.get $p0))
    (i32.store offset=168
      (local.get $l4)
      (local.get $p1))
    (i32.store offset=164
      (local.get $l4)
      (local.get $p2))
    (i32.store offset=160
      (local.get $l4)
      (local.get $p3))
    (call $assert
      (i32.and
        (i32.ge_s
          (i32.load offset=164
            (local.get $l4))
          (i32.const 2))
        (i32.const 1)))
    (i32.store offset=156
      (local.get $l4)
      (i32.load
        (i32.load offset=160
          (local.get $l4))))
    (i32.store offset=148
      (local.get $l4)
      (i32.load offset=4
        (i32.load offset=160
          (local.get $l4))))
    (i32.store offset=144
      (local.get $l4)
      (i32.load offset=8
        (i32.load offset=160
          (local.get $l4))))
    (i32.store offset=140
      (local.get $l4)
      (i32.load offset=12
        (i32.load offset=160
          (local.get $l4))))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.and
              (i32.gt_u
                (i32.load offset=156
                  (local.get $l4))
                (i32.const 0))
              (i32.const 1))))
        (i32.store offset=136
          (local.get $l4)
          (call $mpn_lshift
            (i32.load offset=168
              (local.get $l4))
            (i32.load offset=168
              (local.get $l4))
            (i32.load offset=164
              (local.get $l4))
            (i32.load offset=156
              (local.get $l4))))
        (br $B0))
      (i32.store offset=136
        (local.get $l4)
        (i32.const 0)))
    (i32.store offset=132
      (local.get $l4)
      (i32.load
        (i32.add
          (i32.load offset=168
            (local.get $l4))
          (i32.shl
            (i32.sub
              (i32.load offset=164
                (local.get $l4))
              (i32.const 1))
            (i32.const 2)))))
    (i32.store offset=152
      (local.get $l4)
      (i32.sub
        (i32.load offset=164
          (local.get $l4))
        (i32.const 2)))
    (loop $L2
      (i32.store offset=128
        (local.get $l4)
        (i32.load
          (i32.add
            (i32.load offset=168
              (local.get $l4))
            (i32.shl
              (i32.load offset=152
                (local.get $l4))
              (i32.const 2)))))
      (i32.store offset=104
        (local.get $l4)
        (i32.const 32))
      (i32.store offset=68
        (local.get $l4)
        (i32.load offset=136
          (local.get $l4)))
      (i32.store offset=64
        (local.get $l4)
        (i32.load offset=140
          (local.get $l4)))
      (call $assert
        (i32.const 1))
      (i32.store offset=84
        (local.get $l4)
        (i32.and
          (i32.load offset=68
            (local.get $l4))
          (i32.const 65535)))
      (i32.store offset=76
        (local.get $l4)
        (i32.shr_u
          (i32.load offset=68
            (local.get $l4))
          (i32.const 16)))
      (i32.store offset=80
        (local.get $l4)
        (i32.and
          (i32.load offset=64
            (local.get $l4))
          (i32.const 65535)))
      (i32.store offset=72
        (local.get $l4)
        (i32.shr_u
          (i32.load offset=64
            (local.get $l4))
          (i32.const 16)))
      (i32.store offset=100
        (local.get $l4)
        (i32.mul
          (i32.load offset=84
            (local.get $l4))
          (i32.load offset=80
            (local.get $l4))))
      (i32.store offset=96
        (local.get $l4)
        (i32.mul
          (i32.load offset=84
            (local.get $l4))
          (i32.load offset=72
            (local.get $l4))))
      (i32.store offset=92
        (local.get $l4)
        (i32.mul
          (i32.load offset=76
            (local.get $l4))
          (i32.load offset=80
            (local.get $l4))))
      (i32.store offset=88
        (local.get $l4)
        (i32.mul
          (i32.load offset=76
            (local.get $l4))
          (i32.load offset=72
            (local.get $l4))))
      (i32.store offset=96
        (local.get $l4)
        (i32.add
          (i32.shr_u
            (i32.load offset=100
              (local.get $l4))
            (i32.const 16))
          (i32.load offset=96
            (local.get $l4))))
      (i32.store offset=96
        (local.get $l4)
        (i32.add
          (i32.load offset=92
            (local.get $l4))
          (i32.load offset=96
            (local.get $l4))))
      (block $B3
        (br_if $B3
          (i32.eqz
            (i32.and
              (i32.lt_u
                (i32.load offset=96
                  (local.get $l4))
                (i32.load offset=92
                  (local.get $l4)))
              (i32.const 1))))
        (i32.store offset=88
          (local.get $l4)
          (i32.add
            (i32.load offset=88
              (local.get $l4))
            (i32.const 65536))))
      (i32.store offset=124
        (local.get $l4)
        (i32.add
          (i32.load offset=88
            (local.get $l4))
          (i32.shr_u
            (i32.load offset=96
              (local.get $l4))
            (i32.const 16))))
      (i32.store offset=120
        (local.get $l4)
        (i32.add
          (i32.shl
            (i32.load offset=96
              (local.get $l4))
            (i32.const 16))
          (i32.and
            (i32.load offset=100
              (local.get $l4))
            (i32.const 65535))))
      (i32.store offset=60
        (local.get $l4)
        (i32.add
          (i32.load offset=120
            (local.get $l4))
          (i32.load offset=132
            (local.get $l4))))
      (i32.store offset=124
        (local.get $l4)
        (i32.add
          (i32.add
            (i32.load offset=124
              (local.get $l4))
            (i32.load offset=136
              (local.get $l4)))
          (i32.and
            (i32.lt_u
              (i32.load offset=60
                (local.get $l4))
              (i32.load offset=120
                (local.get $l4)))
            (i32.const 1))))
      (i32.store offset=120
        (local.get $l4)
        (i32.load offset=60
          (local.get $l4)))
      (i32.store offset=136
        (local.get $l4)
        (i32.sub
          (i32.load offset=132
            (local.get $l4))
          (i32.mul
            (i32.load offset=148
              (local.get $l4))
            (i32.load offset=124
              (local.get $l4)))))
      (i32.store offset=56
        (local.get $l4)
        (i32.sub
          (i32.load offset=128
            (local.get $l4))
          (i32.load offset=144
            (local.get $l4))))
      (i32.store offset=136
        (local.get $l4)
        (i32.sub
          (i32.sub
            (i32.load offset=136
              (local.get $l4))
            (i32.load offset=148
              (local.get $l4)))
          (i32.and
            (i32.lt_u
              (i32.load offset=128
                (local.get $l4))
              (i32.load offset=144
                (local.get $l4)))
            (i32.const 1))))
      (i32.store offset=132
        (local.get $l4)
        (i32.load offset=56
          (local.get $l4)))
      (i32.store offset=52
        (local.get $l4)
        (i32.const 32))
      (i32.store offset=16
        (local.get $l4)
        (i32.load offset=144
          (local.get $l4)))
      (i32.store offset=12
        (local.get $l4)
        (i32.load offset=124
          (local.get $l4)))
      (call $assert
        (i32.const 1))
      (i32.store offset=32
        (local.get $l4)
        (i32.and
          (i32.load offset=16
            (local.get $l4))
          (i32.const 65535)))
      (i32.store offset=24
        (local.get $l4)
        (i32.shr_u
          (i32.load offset=16
            (local.get $l4))
          (i32.const 16)))
      (i32.store offset=28
        (local.get $l4)
        (i32.and
          (i32.load offset=12
            (local.get $l4))
          (i32.const 65535)))
      (i32.store offset=20
        (local.get $l4)
        (i32.shr_u
          (i32.load offset=12
            (local.get $l4))
          (i32.const 16)))
      (i32.store offset=48
        (local.get $l4)
        (i32.mul
          (i32.load offset=32
            (local.get $l4))
          (i32.load offset=28
            (local.get $l4))))
      (i32.store offset=44
        (local.get $l4)
        (i32.mul
          (i32.load offset=32
            (local.get $l4))
          (i32.load offset=20
            (local.get $l4))))
      (i32.store offset=40
        (local.get $l4)
        (i32.mul
          (i32.load offset=24
            (local.get $l4))
          (i32.load offset=28
            (local.get $l4))))
      (i32.store offset=36
        (local.get $l4)
        (i32.mul
          (i32.load offset=24
            (local.get $l4))
          (i32.load offset=20
            (local.get $l4))))
      (i32.store offset=44
        (local.get $l4)
        (i32.add
          (i32.shr_u
            (i32.load offset=48
              (local.get $l4))
            (i32.const 16))
          (i32.load offset=44
            (local.get $l4))))
      (i32.store offset=44
        (local.get $l4)
        (i32.add
          (i32.load offset=40
            (local.get $l4))
          (i32.load offset=44
            (local.get $l4))))
      (block $B4
        (br_if $B4
          (i32.eqz
            (i32.and
              (i32.lt_u
                (i32.load offset=44
                  (local.get $l4))
                (i32.load offset=40
                  (local.get $l4)))
              (i32.const 1))))
        (i32.store offset=36
          (local.get $l4)
          (i32.add
            (i32.load offset=36
              (local.get $l4))
            (i32.const 65536))))
      (i32.store offset=116
        (local.get $l4)
        (i32.add
          (i32.load offset=36
            (local.get $l4))
          (i32.shr_u
            (i32.load offset=44
              (local.get $l4))
            (i32.const 16))))
      (i32.store offset=112
        (local.get $l4)
        (i32.add
          (i32.shl
            (i32.load offset=44
              (local.get $l4))
            (i32.const 16))
          (i32.and
            (i32.load offset=48
              (local.get $l4))
            (i32.const 65535))))
      (i32.store offset=8
        (local.get $l4)
        (i32.sub
          (i32.load offset=132
            (local.get $l4))
          (i32.load offset=112
            (local.get $l4))))
      (i32.store offset=136
        (local.get $l4)
        (i32.sub
          (i32.sub
            (i32.load offset=136
              (local.get $l4))
            (i32.load offset=116
              (local.get $l4)))
          (i32.and
            (i32.lt_u
              (i32.load offset=132
                (local.get $l4))
              (i32.load offset=112
                (local.get $l4)))
            (i32.const 1))))
      (i32.store offset=132
        (local.get $l4)
        (i32.load offset=8
          (local.get $l4)))
      (i32.store offset=124
        (local.get $l4)
        (i32.add
          (i32.load offset=124
            (local.get $l4))
          (i32.const 1)))
      (local.set $l5
        (i32.and
          (i32.ge_u
            (i32.load offset=136
              (local.get $l4))
            (i32.load offset=120
              (local.get $l4)))
          (i32.const 1)))
      (i32.store offset=108
        (local.get $l4)
        (i32.sub
          (i32.const 0)
          (local.get $l5)))
      (i32.store offset=124
        (local.get $l4)
        (i32.add
          (i32.load offset=108
            (local.get $l4))
          (i32.load offset=124
            (local.get $l4))))
      (i32.store offset=4
        (local.get $l4)
        (i32.add
          (i32.load offset=132
            (local.get $l4))
          (i32.and
            (i32.load offset=108
              (local.get $l4))
            (i32.load offset=144
              (local.get $l4)))))
      (i32.store offset=136
        (local.get $l4)
        (i32.add
          (i32.add
            (i32.load offset=136
              (local.get $l4))
            (i32.and
              (i32.load offset=108
                (local.get $l4))
              (i32.load offset=148
                (local.get $l4))))
          (i32.and
            (i32.lt_u
              (i32.load offset=4
                (local.get $l4))
              (i32.load offset=132
                (local.get $l4)))
            (i32.const 1))))
      (i32.store offset=132
        (local.get $l4)
        (i32.load offset=4
          (local.get $l4)))
      (block $B5
        (br_if $B5
          (i32.eqz
            (i32.and
              (i32.ge_u
                (i32.load offset=136
                  (local.get $l4))
                (i32.load offset=148
                  (local.get $l4)))
              (i32.const 1))))
        (block $B6
          (block $B7
            (br_if $B7
              (i32.and
                (i32.gt_u
                  (i32.load offset=136
                    (local.get $l4))
                  (i32.load offset=148
                    (local.get $l4)))
                (i32.const 1)))
            (br_if $B6
              (i32.eqz
                (i32.and
                  (i32.ge_u
                    (i32.load offset=132
                      (local.get $l4))
                    (i32.load offset=144
                      (local.get $l4)))
                  (i32.const 1)))))
          (i32.store offset=124
            (local.get $l4)
            (i32.add
              (i32.load offset=124
                (local.get $l4))
              (i32.const 1)))
          (i32.store
            (local.get $l4)
            (i32.sub
              (i32.load offset=132
                (local.get $l4))
              (i32.load offset=144
                (local.get $l4))))
          (i32.store offset=136
            (local.get $l4)
            (i32.sub
              (i32.sub
                (i32.load offset=136
                  (local.get $l4))
                (i32.load offset=148
                  (local.get $l4)))
              (i32.and
                (i32.lt_u
                  (i32.load offset=132
                    (local.get $l4))
                  (i32.load offset=144
                    (local.get $l4)))
                (i32.const 1))))
          (i32.store offset=132
            (local.get $l4)
            (i32.load
              (local.get $l4)))))
      (block $B8
        (br_if $B8
          (i32.eqz
            (i32.and
              (i32.ne
                (i32.load offset=172
                  (local.get $l4))
                (i32.const 0))
              (i32.const 1))))
        (local.set $l6
          (i32.load offset=124
            (local.get $l4)))
        (i32.store
          (i32.add
            (i32.load offset=172
              (local.get $l4))
            (i32.shl
              (i32.load offset=152
                (local.get $l4))
              (i32.const 2)))
          (local.get $l6)))
      (local.set $l7
        (i32.add
          (i32.load offset=152
            (local.get $l4))
          (i32.const -1)))
      (i32.store offset=152
        (local.get $l4)
        (local.get $l7))
      (br_if $L2
        (i32.and
          (i32.ge_s
            (local.get $l7)
            (i32.const 0))
          (i32.const 1))))
    (block $B9
      (br_if $B9
        (i32.eqz
          (i32.and
            (i32.gt_u
              (i32.load offset=156
                (local.get $l4))
              (i32.const 0))
            (i32.const 1))))
      (local.set $l8
        (i32.load offset=132
          (local.get $l4)))
      (local.set $l9
        (i32.load offset=156
          (local.get $l4)))
      (local.set $l10
        (i32.sub
          (i32.const 32)
          (local.get $l9)))
      (call $assert
        (i32.and
          (i32.eq
            (i32.and
              (local.get $l8)
              (i32.shr_u
                (i32.const -1)
                (local.get $l10)))
            (i32.const 0))
          (i32.const 1)))
      (local.set $l11
        (i32.shr_u
          (i32.load offset=132
            (local.get $l4))
          (i32.load offset=156
            (local.get $l4))))
      (local.set $l12
        (i32.load offset=136
          (local.get $l4)))
      (local.set $l13
        (i32.load offset=156
          (local.get $l4)))
      (i32.store offset=132
        (local.get $l4)
        (i32.or
          (local.get $l11)
          (i32.shl
            (local.get $l12)
            (i32.sub
              (i32.const 32)
              (local.get $l13)))))
      (local.set $l14
        (i32.load offset=156
          (local.get $l4)))
      (i32.store offset=136
        (local.get $l4)
        (i32.shr_u
          (i32.load offset=136
            (local.get $l4))
          (local.get $l14))))
    (local.set $l15
      (i32.load offset=136
        (local.get $l4)))
    (i32.store offset=4
      (i32.load offset=168
        (local.get $l4))
      (local.get $l15))
    (local.set $l16
      (i32.load offset=132
        (local.get $l4)))
    (i32.store
      (i32.load offset=168
        (local.get $l4))
      (local.get $l16))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l4)
        (i32.const 176)))
    (return))
  (func $mpn_div_qr_pi1 (type $t11) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (param $p4 i32) (param $p5 i32) (param $p6 i32)
    (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32)
    (local.set $l7
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 192)))
    (global.set $__stack_pointer
      (local.get $l7))
    (i32.store offset=188
      (local.get $l7)
      (local.get $p0))
    (i32.store offset=184
      (local.get $l7)
      (local.get $p1))
    (i32.store offset=180
      (local.get $l7)
      (local.get $p2))
    (i32.store offset=176
      (local.get $l7)
      (local.get $p3))
    (i32.store offset=172
      (local.get $l7)
      (local.get $p4))
    (i32.store offset=168
      (local.get $l7)
      (local.get $p5))
    (i32.store offset=164
      (local.get $l7)
      (local.get $p6))
    (call $assert
      (i32.and
        (i32.gt_s
          (i32.load offset=168
            (local.get $l7))
          (i32.const 2))
        (i32.const 1)))
    (call $assert
      (i32.and
        (i32.ge_s
          (i32.load offset=180
            (local.get $l7))
          (i32.load offset=168
            (local.get $l7)))
        (i32.const 1)))
    (i32.store offset=156
      (local.get $l7)
      (i32.load
        (i32.add
          (i32.load offset=172
            (local.get $l7))
          (i32.shl
            (i32.sub
              (i32.load offset=168
                (local.get $l7))
              (i32.const 1))
            (i32.const 2)))))
    (i32.store offset=152
      (local.get $l7)
      (i32.load
        (i32.add
          (i32.load offset=172
            (local.get $l7))
          (i32.shl
            (i32.sub
              (i32.load offset=168
                (local.get $l7))
              (i32.const 2))
            (i32.const 2)))))
    (call $assert
      (i32.and
        (i32.ne
          (i32.and
            (i32.load offset=156
              (local.get $l7))
            (i32.const -2147483648))
          (i32.const 0))
        (i32.const 1)))
    (i32.store offset=160
      (local.get $l7)
      (i32.sub
        (i32.load offset=180
          (local.get $l7))
        (i32.load offset=168
          (local.get $l7))))
    (loop $L0
      (i32.store offset=136
        (local.get $l7)
        (i32.load
          (i32.add
            (i32.load offset=184
              (local.get $l7))
            (i32.shl
              (i32.add
                (i32.sub
                  (i32.load offset=168
                    (local.get $l7))
                  (i32.const 1))
                (i32.load offset=160
                  (local.get $l7)))
              (i32.const 2)))))
      (block $B1
        (block $B2
          (br_if $B2
            (i32.eqz
              (i32.and
                (i32.eq
                  (i32.load offset=176
                    (local.get $l7))
                  (i32.load offset=156
                    (local.get $l7)))
                (i32.const 1))))
          (br_if $B2
            (i32.eqz
              (i32.and
                (i32.eq
                  (i32.load offset=136
                    (local.get $l7))
                  (i32.load offset=152
                    (local.get $l7)))
                (i32.const 1))))
          (i32.store offset=140
            (local.get $l7)
            (i32.const -1))
          (drop
            (call $mpn_submul_1
              (i32.add
                (i32.load offset=184
                  (local.get $l7))
                (i32.shl
                  (i32.load offset=160
                    (local.get $l7))
                  (i32.const 2)))
              (i32.load offset=172
                (local.get $l7))
              (i32.load offset=168
                (local.get $l7))
              (i32.load offset=140
                (local.get $l7))))
          (i32.store offset=176
            (local.get $l7)
            (i32.load
              (i32.add
                (i32.load offset=184
                  (local.get $l7))
                (i32.shl
                  (i32.add
                    (i32.sub
                      (i32.load offset=168
                        (local.get $l7))
                      (i32.const 1))
                    (i32.load offset=160
                      (local.get $l7)))
                  (i32.const 2)))))
          (br $B1))
        (i32.store offset=116
          (local.get $l7)
          (i32.const 32))
        (i32.store offset=80
          (local.get $l7)
          (i32.load offset=176
            (local.get $l7)))
        (i32.store offset=76
          (local.get $l7)
          (i32.load offset=164
            (local.get $l7)))
        (call $assert
          (i32.const 1))
        (i32.store offset=96
          (local.get $l7)
          (i32.and
            (i32.load offset=80
              (local.get $l7))
            (i32.const 65535)))
        (i32.store offset=88
          (local.get $l7)
          (i32.shr_u
            (i32.load offset=80
              (local.get $l7))
            (i32.const 16)))
        (i32.store offset=92
          (local.get $l7)
          (i32.and
            (i32.load offset=76
              (local.get $l7))
            (i32.const 65535)))
        (i32.store offset=84
          (local.get $l7)
          (i32.shr_u
            (i32.load offset=76
              (local.get $l7))
            (i32.const 16)))
        (i32.store offset=112
          (local.get $l7)
          (i32.mul
            (i32.load offset=96
              (local.get $l7))
            (i32.load offset=92
              (local.get $l7))))
        (i32.store offset=108
          (local.get $l7)
          (i32.mul
            (i32.load offset=96
              (local.get $l7))
            (i32.load offset=84
              (local.get $l7))))
        (i32.store offset=104
          (local.get $l7)
          (i32.mul
            (i32.load offset=88
              (local.get $l7))
            (i32.load offset=92
              (local.get $l7))))
        (i32.store offset=100
          (local.get $l7)
          (i32.mul
            (i32.load offset=88
              (local.get $l7))
            (i32.load offset=84
              (local.get $l7))))
        (i32.store offset=108
          (local.get $l7)
          (i32.add
            (i32.shr_u
              (i32.load offset=112
                (local.get $l7))
              (i32.const 16))
            (i32.load offset=108
              (local.get $l7))))
        (i32.store offset=108
          (local.get $l7)
          (i32.add
            (i32.load offset=104
              (local.get $l7))
            (i32.load offset=108
              (local.get $l7))))
        (block $B3
          (br_if $B3
            (i32.eqz
              (i32.and
                (i32.lt_u
                  (i32.load offset=108
                    (local.get $l7))
                  (i32.load offset=104
                    (local.get $l7)))
                (i32.const 1))))
          (i32.store offset=100
            (local.get $l7)
            (i32.add
              (i32.load offset=100
                (local.get $l7))
              (i32.const 65536))))
        (i32.store offset=140
          (local.get $l7)
          (i32.add
            (i32.load offset=100
              (local.get $l7))
            (i32.shr_u
              (i32.load offset=108
                (local.get $l7))
              (i32.const 16))))
        (i32.store offset=132
          (local.get $l7)
          (i32.add
            (i32.shl
              (i32.load offset=108
                (local.get $l7))
              (i32.const 16))
            (i32.and
              (i32.load offset=112
                (local.get $l7))
              (i32.const 65535))))
        (i32.store offset=72
          (local.get $l7)
          (i32.add
            (i32.load offset=132
              (local.get $l7))
            (i32.load offset=136
              (local.get $l7))))
        (i32.store offset=140
          (local.get $l7)
          (i32.add
            (i32.add
              (i32.load offset=140
                (local.get $l7))
              (i32.load offset=176
                (local.get $l7)))
            (i32.and
              (i32.lt_u
                (i32.load offset=72
                  (local.get $l7))
                (i32.load offset=132
                  (local.get $l7)))
              (i32.const 1))))
        (i32.store offset=132
          (local.get $l7)
          (i32.load offset=72
            (local.get $l7)))
        (i32.store offset=176
          (local.get $l7)
          (i32.sub
            (i32.load offset=136
              (local.get $l7))
            (i32.mul
              (i32.load offset=156
                (local.get $l7))
              (i32.load offset=140
                (local.get $l7)))))
        (i32.store offset=68
          (local.get $l7)
          (i32.sub
            (i32.load
              (i32.add
                (i32.load offset=184
                  (local.get $l7))
                (i32.shl
                  (i32.add
                    (i32.sub
                      (i32.load offset=168
                        (local.get $l7))
                      (i32.const 2))
                    (i32.load offset=160
                      (local.get $l7)))
                  (i32.const 2))))
            (i32.load offset=152
              (local.get $l7))))
        (i32.store offset=176
          (local.get $l7)
          (i32.sub
            (i32.sub
              (i32.load offset=176
                (local.get $l7))
              (i32.load offset=156
                (local.get $l7)))
            (i32.and
              (i32.lt_u
                (i32.load
                  (i32.add
                    (i32.load offset=184
                      (local.get $l7))
                    (i32.shl
                      (i32.add
                        (i32.sub
                          (i32.load offset=168
                            (local.get $l7))
                          (i32.const 2))
                        (i32.load offset=160
                          (local.get $l7)))
                      (i32.const 2))))
                (i32.load offset=152
                  (local.get $l7)))
              (i32.const 1))))
        (i32.store offset=136
          (local.get $l7)
          (i32.load offset=68
            (local.get $l7)))
        (i32.store offset=64
          (local.get $l7)
          (i32.const 32))
        (i32.store offset=28
          (local.get $l7)
          (i32.load offset=152
            (local.get $l7)))
        (i32.store offset=24
          (local.get $l7)
          (i32.load offset=140
            (local.get $l7)))
        (call $assert
          (i32.const 1))
        (i32.store offset=44
          (local.get $l7)
          (i32.and
            (i32.load offset=28
              (local.get $l7))
            (i32.const 65535)))
        (i32.store offset=36
          (local.get $l7)
          (i32.shr_u
            (i32.load offset=28
              (local.get $l7))
            (i32.const 16)))
        (i32.store offset=40
          (local.get $l7)
          (i32.and
            (i32.load offset=24
              (local.get $l7))
            (i32.const 65535)))
        (i32.store offset=32
          (local.get $l7)
          (i32.shr_u
            (i32.load offset=24
              (local.get $l7))
            (i32.const 16)))
        (i32.store offset=60
          (local.get $l7)
          (i32.mul
            (i32.load offset=44
              (local.get $l7))
            (i32.load offset=40
              (local.get $l7))))
        (i32.store offset=56
          (local.get $l7)
          (i32.mul
            (i32.load offset=44
              (local.get $l7))
            (i32.load offset=32
              (local.get $l7))))
        (i32.store offset=52
          (local.get $l7)
          (i32.mul
            (i32.load offset=36
              (local.get $l7))
            (i32.load offset=40
              (local.get $l7))))
        (i32.store offset=48
          (local.get $l7)
          (i32.mul
            (i32.load offset=36
              (local.get $l7))
            (i32.load offset=32
              (local.get $l7))))
        (i32.store offset=56
          (local.get $l7)
          (i32.add
            (i32.shr_u
              (i32.load offset=60
                (local.get $l7))
              (i32.const 16))
            (i32.load offset=56
              (local.get $l7))))
        (i32.store offset=56
          (local.get $l7)
          (i32.add
            (i32.load offset=52
              (local.get $l7))
            (i32.load offset=56
              (local.get $l7))))
        (block $B4
          (br_if $B4
            (i32.eqz
              (i32.and
                (i32.lt_u
                  (i32.load offset=56
                    (local.get $l7))
                  (i32.load offset=52
                    (local.get $l7)))
                (i32.const 1))))
          (i32.store offset=48
            (local.get $l7)
            (i32.add
              (i32.load offset=48
                (local.get $l7))
              (i32.const 65536))))
        (i32.store offset=128
          (local.get $l7)
          (i32.add
            (i32.load offset=48
              (local.get $l7))
            (i32.shr_u
              (i32.load offset=56
                (local.get $l7))
              (i32.const 16))))
        (i32.store offset=124
          (local.get $l7)
          (i32.add
            (i32.shl
              (i32.load offset=56
                (local.get $l7))
              (i32.const 16))
            (i32.and
              (i32.load offset=60
                (local.get $l7))
              (i32.const 65535))))
        (i32.store offset=20
          (local.get $l7)
          (i32.sub
            (i32.load offset=136
              (local.get $l7))
            (i32.load offset=124
              (local.get $l7))))
        (i32.store offset=176
          (local.get $l7)
          (i32.sub
            (i32.sub
              (i32.load offset=176
                (local.get $l7))
              (i32.load offset=128
                (local.get $l7)))
            (i32.and
              (i32.lt_u
                (i32.load offset=136
                  (local.get $l7))
                (i32.load offset=124
                  (local.get $l7)))
              (i32.const 1))))
        (i32.store offset=136
          (local.get $l7)
          (i32.load offset=20
            (local.get $l7)))
        (i32.store offset=140
          (local.get $l7)
          (i32.add
            (i32.load offset=140
              (local.get $l7))
            (i32.const 1)))
        (local.set $l8
          (i32.and
            (i32.ge_u
              (i32.load offset=176
                (local.get $l7))
              (i32.load offset=132
                (local.get $l7)))
            (i32.const 1)))
        (i32.store offset=120
          (local.get $l7)
          (i32.sub
            (i32.const 0)
            (local.get $l8)))
        (i32.store offset=140
          (local.get $l7)
          (i32.add
            (i32.load offset=120
              (local.get $l7))
            (i32.load offset=140
              (local.get $l7))))
        (i32.store offset=16
          (local.get $l7)
          (i32.add
            (i32.load offset=136
              (local.get $l7))
            (i32.and
              (i32.load offset=120
                (local.get $l7))
              (i32.load offset=152
                (local.get $l7)))))
        (i32.store offset=176
          (local.get $l7)
          (i32.add
            (i32.add
              (i32.load offset=176
                (local.get $l7))
              (i32.and
                (i32.load offset=120
                  (local.get $l7))
                (i32.load offset=156
                  (local.get $l7))))
            (i32.and
              (i32.lt_u
                (i32.load offset=16
                  (local.get $l7))
                (i32.load offset=136
                  (local.get $l7)))
              (i32.const 1))))
        (i32.store offset=136
          (local.get $l7)
          (i32.load offset=16
            (local.get $l7)))
        (block $B5
          (br_if $B5
            (i32.eqz
              (i32.and
                (i32.ge_u
                  (i32.load offset=176
                    (local.get $l7))
                  (i32.load offset=156
                    (local.get $l7)))
                (i32.const 1))))
          (block $B6
            (block $B7
              (br_if $B7
                (i32.and
                  (i32.gt_u
                    (i32.load offset=176
                      (local.get $l7))
                    (i32.load offset=156
                      (local.get $l7)))
                  (i32.const 1)))
              (br_if $B6
                (i32.eqz
                  (i32.and
                    (i32.ge_u
                      (i32.load offset=136
                        (local.get $l7))
                      (i32.load offset=152
                        (local.get $l7)))
                    (i32.const 1)))))
            (i32.store offset=140
              (local.get $l7)
              (i32.add
                (i32.load offset=140
                  (local.get $l7))
                (i32.const 1)))
            (i32.store offset=12
              (local.get $l7)
              (i32.sub
                (i32.load offset=136
                  (local.get $l7))
                (i32.load offset=152
                  (local.get $l7))))
            (i32.store offset=176
              (local.get $l7)
              (i32.sub
                (i32.sub
                  (i32.load offset=176
                    (local.get $l7))
                  (i32.load offset=156
                    (local.get $l7)))
                (i32.and
                  (i32.lt_u
                    (i32.load offset=136
                      (local.get $l7))
                    (i32.load offset=152
                      (local.get $l7)))
                  (i32.const 1))))
            (i32.store offset=136
              (local.get $l7)
              (i32.load offset=12
                (local.get $l7)))))
        (i32.store offset=148
          (local.get $l7)
          (call $mpn_submul_1
            (i32.add
              (i32.load offset=184
                (local.get $l7))
              (i32.shl
                (i32.load offset=160
                  (local.get $l7))
                (i32.const 2)))
            (i32.load offset=172
              (local.get $l7))
            (i32.sub
              (i32.load offset=168
                (local.get $l7))
              (i32.const 2))
            (i32.load offset=140
              (local.get $l7))))
        (i32.store offset=144
          (local.get $l7)
          (i32.and
            (i32.lt_u
              (i32.load offset=136
                (local.get $l7))
              (i32.load offset=148
                (local.get $l7)))
            (i32.const 1)))
        (i32.store offset=136
          (local.get $l7)
          (i32.sub
            (i32.load offset=136
              (local.get $l7))
            (i32.load offset=148
              (local.get $l7))))
        (i32.store offset=148
          (local.get $l7)
          (i32.and
            (i32.lt_u
              (i32.load offset=176
                (local.get $l7))
              (i32.load offset=144
                (local.get $l7)))
            (i32.const 1)))
        (i32.store offset=176
          (local.get $l7)
          (i32.sub
            (i32.load offset=176
              (local.get $l7))
            (i32.load offset=144
              (local.get $l7))))
        (local.set $l9
          (i32.load offset=136
            (local.get $l7)))
        (i32.store
          (i32.add
            (i32.load offset=184
              (local.get $l7))
            (i32.shl
              (i32.add
                (i32.sub
                  (i32.load offset=168
                    (local.get $l7))
                  (i32.const 2))
                (i32.load offset=160
                  (local.get $l7)))
              (i32.const 2)))
          (local.get $l9))
        (block $B8
          (br_if $B8
            (i32.eqz
              (i32.load offset=148
                (local.get $l7))))
          (i32.store offset=176
            (local.get $l7)
            (i32.add
              (i32.add
                (i32.load offset=156
                  (local.get $l7))
                (call $mpn_add_n
                  (i32.add
                    (i32.load offset=184
                      (local.get $l7))
                    (i32.shl
                      (i32.load offset=160
                        (local.get $l7))
                      (i32.const 2)))
                  (i32.add
                    (i32.load offset=184
                      (local.get $l7))
                    (i32.shl
                      (i32.load offset=160
                        (local.get $l7))
                      (i32.const 2)))
                  (i32.load offset=172
                    (local.get $l7))
                  (i32.sub
                    (i32.load offset=168
                      (local.get $l7))
                    (i32.const 1))))
              (i32.load offset=176
                (local.get $l7))))
          (i32.store offset=140
            (local.get $l7)
            (i32.add
              (i32.load offset=140
                (local.get $l7))
              (i32.const -1)))))
      (block $B9
        (br_if $B9
          (i32.eqz
            (i32.and
              (i32.ne
                (i32.load offset=188
                  (local.get $l7))
                (i32.const 0))
              (i32.const 1))))
        (local.set $l10
          (i32.load offset=140
            (local.get $l7)))
        (i32.store
          (i32.add
            (i32.load offset=188
              (local.get $l7))
            (i32.shl
              (i32.load offset=160
                (local.get $l7))
              (i32.const 2)))
          (local.get $l10)))
      (local.set $l11
        (i32.add
          (i32.load offset=160
            (local.get $l7))
          (i32.const -1)))
      (i32.store offset=160
        (local.get $l7)
        (local.get $l11))
      (br_if $L0
        (i32.and
          (i32.ge_s
            (local.get $l11)
            (i32.const 0))
          (i32.const 1))))
    (local.set $l12
      (i32.load offset=176
        (local.get $l7)))
    (i32.store
      (i32.add
        (i32.load offset=184
          (local.get $l7))
        (i32.shl
          (i32.sub
            (i32.load offset=168
              (local.get $l7))
            (i32.const 1))
          (i32.const 2)))
      (local.get $l12))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l7)
        (i32.const 192)))
    (return))
  (func $mpn_div_qr_preinv (type $t12) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (param $p4 i32) (param $p5 i32)
    (local $l6 i32) (local $l7 i32)
    (local.set $l6
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 48)))
    (global.set $__stack_pointer
      (local.get $l6))
    (i32.store offset=44
      (local.get $l6)
      (local.get $p0))
    (i32.store offset=40
      (local.get $l6)
      (local.get $p1))
    (i32.store offset=36
      (local.get $l6)
      (local.get $p2))
    (i32.store offset=32
      (local.get $l6)
      (local.get $p3))
    (i32.store offset=28
      (local.get $l6)
      (local.get $p4))
    (i32.store offset=24
      (local.get $l6)
      (local.get $p5))
    (call $assert
      (i32.and
        (i32.gt_s
          (i32.load offset=28
            (local.get $l6))
          (i32.const 0))
        (i32.const 1)))
    (call $assert
      (i32.and
        (i32.ge_s
          (i32.load offset=36
            (local.get $l6))
          (i32.load offset=28
            (local.get $l6)))
        (i32.const 1)))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.and
              (i32.eq
                (i32.load offset=28
                  (local.get $l6))
                (i32.const 1))
              (i32.const 1))))
        (local.set $l7
          (call $mpn_div_qr_1_preinv
            (i32.load offset=44
              (local.get $l6))
            (i32.load offset=40
              (local.get $l6))
            (i32.load offset=36
              (local.get $l6))
            (i32.load offset=24
              (local.get $l6))))
        (i32.store
          (i32.load offset=40
            (local.get $l6))
          (local.get $l7))
        (br $B0))
      (block $B2
        (block $B3
          (br_if $B3
            (i32.eqz
              (i32.and
                (i32.eq
                  (i32.load offset=28
                    (local.get $l6))
                  (i32.const 2))
                (i32.const 1))))
          (call $mpn_div_qr_2_preinv
            (i32.load offset=44
              (local.get $l6))
            (i32.load offset=40
              (local.get $l6))
            (i32.load offset=36
              (local.get $l6))
            (i32.load offset=24
              (local.get $l6)))
          (br $B2))
        (call $assert
          (i32.and
            (i32.eq
              (i32.load offset=4
                (i32.load offset=24
                  (local.get $l6)))
              (i32.load
                (i32.add
                  (i32.load offset=32
                    (local.get $l6))
                  (i32.shl
                    (i32.sub
                      (i32.load offset=28
                        (local.get $l6))
                      (i32.const 1))
                    (i32.const 2)))))
            (i32.const 1)))
        (call $assert
          (i32.and
            (i32.eq
              (i32.load offset=8
                (i32.load offset=24
                  (local.get $l6)))
              (i32.load
                (i32.add
                  (i32.load offset=32
                    (local.get $l6))
                  (i32.shl
                    (i32.sub
                      (i32.load offset=28
                        (local.get $l6))
                      (i32.const 2))
                    (i32.const 2)))))
            (i32.const 1)))
        (call $assert
          (i32.and
            (i32.ne
              (i32.and
                (i32.load offset=4
                  (i32.load offset=24
                    (local.get $l6)))
                (i32.const -2147483648))
              (i32.const 0))
            (i32.const 1)))
        (i32.store offset=16
          (local.get $l6)
          (i32.load
            (i32.load offset=24
              (local.get $l6))))
        (block $B4
          (block $B5
            (br_if $B5
              (i32.eqz
                (i32.and
                  (i32.gt_u
                    (i32.load offset=16
                      (local.get $l6))
                    (i32.const 0))
                  (i32.const 1))))
            (i32.store offset=20
              (local.get $l6)
              (call $mpn_lshift
                (i32.load offset=40
                  (local.get $l6))
                (i32.load offset=40
                  (local.get $l6))
                (i32.load offset=36
                  (local.get $l6))
                (i32.load offset=16
                  (local.get $l6))))
            (br $B4))
          (i32.store offset=20
            (local.get $l6)
            (i32.const 0)))
        (call $mpn_div_qr_pi1
          (i32.load offset=44
            (local.get $l6))
          (i32.load offset=40
            (local.get $l6))
          (i32.load offset=36
            (local.get $l6))
          (i32.load offset=20
            (local.get $l6))
          (i32.load offset=32
            (local.get $l6))
          (i32.load offset=28
            (local.get $l6))
          (i32.load offset=12
            (i32.load offset=24
              (local.get $l6))))
        (block $B6
          (br_if $B6
            (i32.eqz
              (i32.and
                (i32.gt_u
                  (i32.load offset=16
                    (local.get $l6))
                  (i32.const 0))
                (i32.const 1))))
          (i32.store offset=12
            (local.get $l6)
            (call $mpn_rshift
              (i32.load offset=40
                (local.get $l6))
              (i32.load offset=40
                (local.get $l6))
              (i32.load offset=28
                (local.get $l6))
              (i32.load offset=16
                (local.get $l6))))
          (call $assert
            (i32.and
              (i32.eq
                (i32.load offset=12
                  (local.get $l6))
                (i32.const 0))
              (i32.const 1))))))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l6)
        (i32.const 48)))
    (return))
  (func $mpn_div_qr (type $t13) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (param $p4 i32)
    (local $l5 i32) (local $l6 i32) (local $l7 i32)
    (local.set $l5
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 48)))
    (global.set $__stack_pointer
      (local.get $l5))
    (i32.store offset=44
      (local.get $l5)
      (local.get $p0))
    (i32.store offset=40
      (local.get $l5)
      (local.get $p1))
    (i32.store offset=36
      (local.get $l5)
      (local.get $p2))
    (i32.store offset=32
      (local.get $l5)
      (local.get $p3))
    (i32.store offset=28
      (local.get $l5)
      (local.get $p4))
    (i32.store offset=8
      (local.get $l5)
      (i32.const 0))
    (call $assert
      (i32.and
        (i32.gt_s
          (i32.load offset=28
            (local.get $l5))
          (i32.const 0))
        (i32.const 1)))
    (call $assert
      (i32.and
        (i32.ge_s
          (i32.load offset=36
            (local.get $l5))
          (i32.load offset=28
            (local.get $l5)))
        (i32.const 1)))
    (local.set $l6
      (i32.load offset=32
        (local.get $l5)))
    (local.set $l7
      (i32.load offset=28
        (local.get $l5)))
    (call $mpn_div_qr_invert
      (i32.add
        (local.get $l5)
        (i32.const 12))
      (local.get $l6)
      (local.get $l7))
    (block $B0
      (br_if $B0
        (i32.eqz
          (i32.and
            (i32.gt_s
              (i32.load offset=28
                (local.get $l5))
              (i32.const 2))
            (i32.const 1))))
      (br_if $B0
        (i32.eqz
          (i32.and
            (i32.gt_u
              (i32.load offset=12
                (local.get $l5))
              (i32.const 0))
            (i32.const 1))))
      (i32.store offset=8
        (local.get $l5)
        (call $gmp_alloc_limbs
          (i32.load offset=28
            (local.get $l5))))
      (i32.store offset=4
        (local.get $l5)
        (call $mpn_lshift
          (i32.load offset=8
            (local.get $l5))
          (i32.load offset=32
            (local.get $l5))
          (i32.load offset=28
            (local.get $l5))
          (i32.load offset=12
            (local.get $l5))))
      (call $assert
        (i32.and
          (i32.eq
            (i32.load offset=4
              (local.get $l5))
            (i32.const 0))
          (i32.const 1)))
      (i32.store offset=32
        (local.get $l5)
        (i32.load offset=8
          (local.get $l5))))
    (call $mpn_div_qr_preinv
      (i32.load offset=44
        (local.get $l5))
      (i32.load offset=40
        (local.get $l5))
      (i32.load offset=36
        (local.get $l5))
      (i32.load offset=32
        (local.get $l5))
      (i32.load offset=28
        (local.get $l5))
      (i32.add
        (local.get $l5)
        (i32.const 12)))
    (block $B1
      (br_if $B1
        (i32.eqz
          (i32.and
            (i32.ne
              (i32.load offset=8
                (local.get $l5))
              (i32.const 0))
            (i32.const 1))))
      (call $gmp_free_limbs
        (i32.load offset=8
          (local.get $l5))
        (i32.load offset=28
          (local.get $l5))))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l5)
        (i32.const 48)))
    (return))
  (func $mpz_init (type $t4) (param $p0 i32)
    (local $l1 i32)
    (local.set $l1
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (i32.store offset=12
      (local.get $l1)
      (local.get $p0))
    (i32.store
      (i32.load offset=12
        (local.get $l1))
      (i32.const 0))
    (i32.store offset=4
      (i32.load offset=12
        (local.get $l1))
      (i32.const 0))
    (i32.store offset=8
      (i32.load offset=12
        (local.get $l1))
      (i32.const 1156))
    (return))
  (func $mpz_init2 (type $t2) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l2))
    (i32.store offset=12
      (local.get $l2)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l2)
      (local.get $p1))
    (local.set $l3
      (i32.and
        (i32.ne
          (i32.load offset=8
            (local.get $l2))
          (i32.const 0))
        (i32.const 1)))
    (i32.store offset=8
      (local.get $l2)
      (i32.sub
        (i32.load offset=8
          (local.get $l2))
        (local.get $l3)))
    (i32.store offset=4
      (local.get $l2)
      (i32.add
        (i32.shr_u
          (i32.load offset=8
            (local.get $l2))
          (i32.const 5))
        (i32.const 1)))
    (local.set $l4
      (i32.load offset=4
        (local.get $l2)))
    (i32.store
      (i32.load offset=12
        (local.get $l2))
      (local.get $l4))
    (i32.store offset=4
      (i32.load offset=12
        (local.get $l2))
      (i32.const 0))
    (local.set $l5
      (call $gmp_alloc_limbs
        (i32.load offset=4
          (local.get $l2))))
    (i32.store offset=8
      (i32.load offset=12
        (local.get $l2))
      (local.get $l5))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l2)
        (i32.const 16)))
    (return))
  (func $mpz_clear (type $t4) (param $p0 i32)
    (local $l1 i32)
    (local.set $l1
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l1))
    (i32.store offset=12
      (local.get $l1)
      (local.get $p0))
    (block $B0
      (br_if $B0
        (i32.eqz
          (i32.load
            (i32.load offset=12
              (local.get $l1)))))
      (call $gmp_free_limbs
        (i32.load offset=8
          (i32.load offset=12
            (local.get $l1)))
        (i32.load
          (i32.load offset=12
            (local.get $l1)))))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l1)
        (i32.const 16)))
    (return))
  (func $mpz_realloc (type $t5) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l2))
    (i32.store offset=12
      (local.get $l2)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l2)
      (local.get $p1))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.and
              (i32.gt_s
                (i32.load offset=8
                  (local.get $l2))
                (i32.const 1))
              (i32.const 1))))
        (local.set $l3
          (i32.load offset=8
            (local.get $l2)))
        (br $B0))
      (local.set $l3
        (i32.const 1)))
    (i32.store offset=8
      (local.get $l2)
      (local.get $l3))
    (block $B2
      (block $B3
        (br_if $B3
          (i32.eqz
            (i32.load
              (i32.load offset=12
                (local.get $l2)))))
        (local.set $l4
          (call $gmp_realloc_limbs
            (i32.load offset=8
              (i32.load offset=12
                (local.get $l2)))
            (i32.load
              (i32.load offset=12
                (local.get $l2)))
            (i32.load offset=8
              (local.get $l2))))
        (i32.store offset=8
          (i32.load offset=12
            (local.get $l2))
          (local.get $l4))
        (br $B2))
      (local.set $l5
        (call $gmp_alloc_limbs
          (i32.load offset=8
            (local.get $l2))))
      (i32.store offset=8
        (i32.load offset=12
          (local.get $l2))
        (local.get $l5)))
    (local.set $l6
      (i32.load offset=8
        (local.get $l2)))
    (i32.store
      (i32.load offset=12
        (local.get $l2))
      (local.get $l6))
    (block $B4
      (block $B5
        (br_if $B5
          (i32.eqz
            (i32.and
              (i32.ge_s
                (i32.load offset=4
                  (i32.load offset=12
                    (local.get $l2)))
                (i32.const 0))
              (i32.const 1))))
        (local.set $l7
          (i32.load offset=4
            (i32.load offset=12
              (local.get $l2))))
        (br $B4))
      (local.set $l8
        (i32.load offset=4
          (i32.load offset=12
            (local.get $l2))))
      (local.set $l7
        (i32.sub
          (i32.const 0)
          (local.get $l8))))
    (block $B6
      (br_if $B6
        (i32.eqz
          (i32.and
            (i32.gt_s
              (local.get $l7)
              (i32.load offset=8
                (local.get $l2)))
            (i32.const 1))))
      (i32.store offset=4
        (i32.load offset=12
          (local.get $l2))
        (i32.const 0)))
    (local.set $l9
      (i32.load offset=8
        (i32.load offset=12
          (local.get $l2))))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l2)
        (i32.const 16)))
    (return
      (local.get $l9)))
  (func $mpz_set_si (type $t2) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l2))
    (i32.store offset=12
      (local.get $l2)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l2)
      (local.get $p1))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.and
              (i32.ge_s
                (i32.load offset=8
                  (local.get $l2))
                (i32.const 0))
              (i32.const 1))))
        (call $mpz_set_ui
          (i32.load offset=12
            (local.get $l2))
          (i32.load offset=8
            (local.get $l2)))
        (br $B0))
      (i32.store offset=4
        (i32.load offset=12
          (local.get $l2))
        (i32.const -1))
      (local.set $l3
        (i32.sub
          (i32.add
            (i32.load offset=8
              (local.get $l2))
            (i32.const 1))
          (i32.const 1)))
      (local.set $l4
        (i32.sub
          (i32.const 0)
          (local.get $l3)))
      (local.set $l5
        (i32.load
          (i32.load offset=12
            (local.get $l2))))
      (block $B2
        (block $B3
          (br_if $B3
            (i32.eqz
              (i32.and
                (i32.gt_s
                  (i32.const 1)
                  (local.get $l5))
                (i32.const 1))))
          (local.set $l6
            (call $mpz_realloc
              (i32.load offset=12
                (local.get $l2))
              (i32.const 1)))
          (br $B2))
        (local.set $l6
          (i32.load offset=8
            (i32.load offset=12
              (local.get $l2)))))
      (i32.store
        (local.get $l6)
        (local.get $l4)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l2)
        (i32.const 16)))
    (return))
  (func $mpz_set_ui (type $t2) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l2))
    (i32.store offset=12
      (local.get $l2)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l2)
      (local.get $p1))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.and
              (i32.gt_u
                (i32.load offset=8
                  (local.get $l2))
                (i32.const 0))
              (i32.const 1))))
        (i32.store offset=4
          (i32.load offset=12
            (local.get $l2))
          (i32.const 1))
        (local.set $l3
          (i32.load offset=8
            (local.get $l2)))
        (local.set $l4
          (i32.load
            (i32.load offset=12
              (local.get $l2))))
        (block $B2
          (block $B3
            (br_if $B3
              (i32.eqz
                (i32.and
                  (i32.gt_s
                    (i32.const 1)
                    (local.get $l4))
                  (i32.const 1))))
            (local.set $l5
              (call $mpz_realloc
                (i32.load offset=12
                  (local.get $l2))
                (i32.const 1)))
            (br $B2))
          (local.set $l5
            (i32.load offset=8
              (i32.load offset=12
                (local.get $l2)))))
        (i32.store
          (local.get $l5)
          (local.get $l3))
        (br $B0))
      (i32.store offset=4
        (i32.load offset=12
          (local.get $l2))
        (i32.const 0)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l2)
        (i32.const 16)))
    (return))
  (func $mpz_set (type $t2) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l2))
    (i32.store offset=12
      (local.get $l2)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l2)
      (local.get $p1))
    (block $B0
      (br_if $B0
        (i32.eqz
          (i32.and
            (i32.ne
              (i32.load offset=12
                (local.get $l2))
              (i32.load offset=8
                (local.get $l2)))
            (i32.const 1))))
      (block $B1
        (block $B2
          (br_if $B2
            (i32.eqz
              (i32.and
                (i32.ge_s
                  (i32.load offset=4
                    (i32.load offset=8
                      (local.get $l2)))
                  (i32.const 0))
                (i32.const 1))))
          (local.set $l3
            (i32.load offset=4
              (i32.load offset=8
                (local.get $l2))))
          (br $B1))
        (local.set $l4
          (i32.load offset=4
            (i32.load offset=8
              (local.get $l2))))
        (local.set $l3
          (i32.sub
            (i32.const 0)
            (local.get $l4))))
      (i32.store offset=4
        (local.get $l2)
        (local.get $l3))
      (block $B3
        (block $B4
          (br_if $B4
            (i32.eqz
              (i32.and
                (i32.gt_s
                  (i32.load offset=4
                    (local.get $l2))
                  (i32.load
                    (i32.load offset=12
                      (local.get $l2))))
                (i32.const 1))))
          (local.set $l5
            (call $mpz_realloc
              (i32.load offset=12
                (local.get $l2))
              (i32.load offset=4
                (local.get $l2))))
          (br $B3))
        (local.set $l5
          (i32.load offset=8
            (i32.load offset=12
              (local.get $l2)))))
      (i32.store
        (local.get $l2)
        (local.get $l5))
      (call $mpn_copyi
        (i32.load
          (local.get $l2))
        (i32.load offset=8
          (i32.load offset=8
            (local.get $l2)))
        (i32.load offset=4
          (local.get $l2)))
      (local.set $l6
        (i32.load offset=4
          (i32.load offset=8
            (local.get $l2))))
      (i32.store offset=4
        (i32.load offset=12
          (local.get $l2))
        (local.get $l6)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l2)
        (i32.const 16)))
    (return))
  (func $mpz_init_set_ui (type $t2) (param $p0 i32) (param $p1 i32)
    (local $l2 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l2))
    (i32.store offset=12
      (local.get $l2)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l2)
      (local.get $p1))
    (call $mpz_init
      (i32.load offset=12
        (local.get $l2)))
    (call $mpz_set_ui
      (i32.load offset=12
        (local.get $l2))
      (i32.load offset=8
        (local.get $l2)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l2)
        (i32.const 16)))
    (return))
  (func $mpz_init_set (type $t2) (param $p0 i32) (param $p1 i32)
    (local $l2 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l2))
    (i32.store offset=12
      (local.get $l2)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l2)
      (local.get $p1))
    (call $mpz_init
      (i32.load offset=12
        (local.get $l2)))
    (call $mpz_set
      (i32.load offset=12
        (local.get $l2))
      (i32.load offset=8
        (local.get $l2)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l2)
        (i32.const 16)))
    (return))
  (func $mpz_get_ui (type $t0) (param $p0 i32) (result i32)
    (local $l1 i32) (local $l2 i32)
    (local.set $l1
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (i32.store offset=12
      (local.get $l1)
      (local.get $p0))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.load offset=4
            (i32.load offset=12
              (local.get $l1))))
        (local.set $l2
          (i32.const 0))
        (br $B0))
      (local.set $l2
        (i32.load
          (i32.load offset=8
            (i32.load offset=12
              (local.get $l1))))))
    (return
      (local.get $l2)))
  (func $mpz_abs (type $t2) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l2))
    (i32.store offset=12
      (local.get $l2)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l2)
      (local.get $p1))
    (call $mpz_set
      (i32.load offset=12
        (local.get $l2))
      (i32.load offset=8
        (local.get $l2)))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.and
              (i32.ge_s
                (i32.load offset=4
                  (i32.load offset=12
                    (local.get $l2)))
                (i32.const 0))
              (i32.const 1))))
        (local.set $l3
          (i32.load offset=4
            (i32.load offset=12
              (local.get $l2))))
        (br $B0))
      (local.set $l4
        (i32.load offset=4
          (i32.load offset=12
            (local.get $l2))))
      (local.set $l3
        (i32.sub
          (i32.const 0)
          (local.get $l4))))
    (local.set $l5
      (local.get $l3))
    (i32.store offset=4
      (i32.load offset=12
        (local.get $l2))
      (local.get $l5))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l2)
        (i32.const 16)))
    (return))
  (func $mpz_neg (type $t2) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l2))
    (i32.store offset=12
      (local.get $l2)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l2)
      (local.get $p1))
    (call $mpz_set
      (i32.load offset=12
        (local.get $l2))
      (i32.load offset=8
        (local.get $l2)))
    (local.set $l3
      (i32.load offset=4
        (i32.load offset=12
          (local.get $l2))))
    (local.set $l4
      (i32.sub
        (i32.const 0)
        (local.get $l3)))
    (i32.store offset=4
      (i32.load offset=12
        (local.get $l2))
      (local.get $l4))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l2)
        (i32.const 16)))
    (return))
  (func $mpz_swap (type $t2) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 32)))
    (i32.store offset=28
      (local.get $l2)
      (local.get $p0))
    (i32.store offset=24
      (local.get $l2)
      (local.get $p1))
    (i32.store offset=20
      (local.get $l2)
      (i32.load
        (i32.load offset=28
          (local.get $l2))))
    (local.set $l3
      (i32.load
        (i32.load offset=24
          (local.get $l2))))
    (i32.store
      (i32.load offset=28
        (local.get $l2))
      (local.get $l3))
    (local.set $l4
      (i32.load offset=20
        (local.get $l2)))
    (i32.store
      (i32.load offset=24
        (local.get $l2))
      (local.get $l4))
    (i32.store offset=16
      (local.get $l2)
      (i32.load offset=8
        (i32.load offset=28
          (local.get $l2))))
    (local.set $l5
      (i32.load offset=8
        (i32.load offset=24
          (local.get $l2))))
    (i32.store offset=8
      (i32.load offset=28
        (local.get $l2))
      (local.get $l5))
    (local.set $l6
      (i32.load offset=16
        (local.get $l2)))
    (i32.store offset=8
      (i32.load offset=24
        (local.get $l2))
      (local.get $l6))
    (i32.store offset=12
      (local.get $l2)
      (i32.load offset=4
        (i32.load offset=28
          (local.get $l2))))
    (local.set $l7
      (i32.load offset=4
        (i32.load offset=24
          (local.get $l2))))
    (i32.store offset=4
      (i32.load offset=28
        (local.get $l2))
      (local.get $l7))
    (local.set $l8
      (i32.load offset=12
        (local.get $l2)))
    (i32.store offset=4
      (i32.load offset=24
        (local.get $l2))
      (local.get $l8))
    (return))
  (func $mpz_add_ui (type $t7) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 32)))
    (global.set $__stack_pointer
      (local.get $l3))
    (i32.store offset=28
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=24
      (local.get $l3)
      (local.get $p1))
    (i32.store offset=20
      (local.get $l3)
      (local.get $p2))
    (call $mpz_init_set_ui
      (i32.add
        (local.get $l3)
        (i32.const 8))
      (i32.load offset=20
        (local.get $l3)))
    (call $mpz_add
      (i32.load offset=28
        (local.get $l3))
      (i32.load offset=24
        (local.get $l3))
      (i32.add
        (local.get $l3)
        (i32.const 8)))
    (call $mpz_clear
      (i32.add
        (local.get $l3)
        (i32.const 8)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l3)
        (i32.const 32)))
    (return))
  (func $mpz_add (type $t7) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l3))
    (i32.store offset=12
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l3)
      (local.get $p1))
    (i32.store offset=4
      (local.get $l3)
      (local.get $p2))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.and
              (i32.ge_s
                (i32.xor
                  (i32.load offset=4
                    (i32.load offset=8
                      (local.get $l3)))
                  (i32.load offset=4
                    (i32.load offset=4
                      (local.get $l3))))
                (i32.const 0))
              (i32.const 1))))
        (i32.store
          (local.get $l3)
          (call $mpz_abs_add
            (i32.load offset=12
              (local.get $l3))
            (i32.load offset=8
              (local.get $l3))
            (i32.load offset=4
              (local.get $l3))))
        (br $B0))
      (i32.store
        (local.get $l3)
        (call $mpz_abs_sub
          (i32.load offset=12
            (local.get $l3))
          (i32.load offset=8
            (local.get $l3))
          (i32.load offset=4
            (local.get $l3)))))
    (block $B2
      (block $B3
        (br_if $B3
          (i32.eqz
            (i32.and
              (i32.ge_s
                (i32.load offset=4
                  (i32.load offset=8
                    (local.get $l3)))
                (i32.const 0))
              (i32.const 1))))
        (local.set $l4
          (i32.load
            (local.get $l3)))
        (br $B2))
      (local.set $l5
        (i32.load
          (local.get $l3)))
      (local.set $l4
        (i32.sub
          (i32.const 0)
          (local.get $l5))))
    (local.set $l6
      (local.get $l4))
    (i32.store offset=4
      (i32.load offset=12
        (local.get $l3))
      (local.get $l6))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l3)
        (i32.const 16)))
    (return))
  (func $mpz_abs_add (type $t1) (param $p0 i32) (param $p1 i32) (param $p2 i32) (result i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 48)))
    (global.set $__stack_pointer
      (local.get $l3))
    (i32.store offset=44
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=40
      (local.get $l3)
      (local.get $p1))
    (i32.store offset=36
      (local.get $l3)
      (local.get $p2))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.and
              (i32.ge_s
                (i32.load offset=4
                  (i32.load offset=40
                    (local.get $l3)))
                (i32.const 0))
              (i32.const 1))))
        (local.set $l4
          (i32.load offset=4
            (i32.load offset=40
              (local.get $l3))))
        (br $B0))
      (local.set $l5
        (i32.load offset=4
          (i32.load offset=40
            (local.get $l3))))
      (local.set $l4
        (i32.sub
          (i32.const 0)
          (local.get $l5))))
    (i32.store offset=32
      (local.get $l3)
      (local.get $l4))
    (block $B2
      (block $B3
        (br_if $B3
          (i32.eqz
            (i32.and
              (i32.ge_s
                (i32.load offset=4
                  (i32.load offset=36
                    (local.get $l3)))
                (i32.const 0))
              (i32.const 1))))
        (local.set $l6
          (i32.load offset=4
            (i32.load offset=36
              (local.get $l3))))
        (br $B2))
      (local.set $l7
        (i32.load offset=4
          (i32.load offset=36
            (local.get $l3))))
      (local.set $l6
        (i32.sub
          (i32.const 0)
          (local.get $l7))))
    (i32.store offset=28
      (local.get $l3)
      (local.get $l6))
    (block $B4
      (br_if $B4
        (i32.eqz
          (i32.and
            (i32.lt_s
              (i32.load offset=32
                (local.get $l3))
              (i32.load offset=28
                (local.get $l3)))
            (i32.const 1))))
      (i32.store offset=16
        (local.get $l3)
        (i32.load offset=40
          (local.get $l3)))
      (i32.store offset=40
        (local.get $l3)
        (i32.load offset=36
          (local.get $l3)))
      (i32.store offset=36
        (local.get $l3)
        (i32.load offset=16
          (local.get $l3)))
      (i32.store offset=12
        (local.get $l3)
        (i32.load offset=32
          (local.get $l3)))
      (i32.store offset=32
        (local.get $l3)
        (i32.load offset=28
          (local.get $l3)))
      (i32.store offset=28
        (local.get $l3)
        (i32.load offset=12
          (local.get $l3))))
    (block $B5
      (block $B6
        (br_if $B6
          (i32.eqz
            (i32.and
              (i32.gt_s
                (i32.add
                  (i32.load offset=32
                    (local.get $l3))
                  (i32.const 1))
                (i32.load
                  (i32.load offset=44
                    (local.get $l3))))
              (i32.const 1))))
        (local.set $l8
          (call $mpz_realloc
            (i32.load offset=44
              (local.get $l3))
            (i32.add
              (i32.load offset=32
                (local.get $l3))
              (i32.const 1))))
        (br $B5))
      (local.set $l8
        (i32.load offset=8
          (i32.load offset=44
            (local.get $l3)))))
    (i32.store offset=24
      (local.get $l3)
      (local.get $l8))
    (i32.store offset=20
      (local.get $l3)
      (call $mpn_add
        (i32.load offset=24
          (local.get $l3))
        (i32.load offset=8
          (i32.load offset=40
            (local.get $l3)))
        (i32.load offset=32
          (local.get $l3))
        (i32.load offset=8
          (i32.load offset=36
            (local.get $l3)))
        (i32.load offset=28
          (local.get $l3))))
    (local.set $l9
      (i32.load offset=20
        (local.get $l3)))
    (i32.store
      (i32.add
        (i32.load offset=24
          (local.get $l3))
        (i32.shl
          (i32.load offset=32
            (local.get $l3))
          (i32.const 2)))
      (local.get $l9))
    (local.set $l10
      (i32.add
        (i32.load offset=32
          (local.get $l3))
        (i32.load offset=20
          (local.get $l3))))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l3)
        (i32.const 48)))
    (return
      (local.get $l10)))
  (func $mpz_abs_sub (type $t1) (param $p0 i32) (param $p1 i32) (param $p2 i32) (result i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 48)))
    (global.set $__stack_pointer
      (local.get $l3))
    (i32.store offset=40
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=36
      (local.get $l3)
      (local.get $p1))
    (i32.store offset=32
      (local.get $l3)
      (local.get $p2))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.and
              (i32.ge_s
                (i32.load offset=4
                  (i32.load offset=36
                    (local.get $l3)))
                (i32.const 0))
              (i32.const 1))))
        (local.set $l4
          (i32.load offset=4
            (i32.load offset=36
              (local.get $l3))))
        (br $B0))
      (local.set $l5
        (i32.load offset=4
          (i32.load offset=36
            (local.get $l3))))
      (local.set $l4
        (i32.sub
          (i32.const 0)
          (local.get $l5))))
    (i32.store offset=28
      (local.get $l3)
      (local.get $l4))
    (block $B2
      (block $B3
        (br_if $B3
          (i32.eqz
            (i32.and
              (i32.ge_s
                (i32.load offset=4
                  (i32.load offset=32
                    (local.get $l3)))
                (i32.const 0))
              (i32.const 1))))
        (local.set $l6
          (i32.load offset=4
            (i32.load offset=32
              (local.get $l3))))
        (br $B2))
      (local.set $l7
        (i32.load offset=4
          (i32.load offset=32
            (local.get $l3))))
      (local.set $l6
        (i32.sub
          (i32.const 0)
          (local.get $l7))))
    (i32.store offset=24
      (local.get $l3)
      (local.get $l6))
    (i32.store offset=20
      (local.get $l3)
      (call $mpn_cmp4
        (i32.load offset=8
          (i32.load offset=36
            (local.get $l3)))
        (i32.load offset=28
          (local.get $l3))
        (i32.load offset=8
          (i32.load offset=32
            (local.get $l3)))
        (i32.load offset=24
          (local.get $l3))))
    (block $B4
      (block $B5
        (br_if $B5
          (i32.eqz
            (i32.and
              (i32.gt_s
                (i32.load offset=20
                  (local.get $l3))
                (i32.const 0))
              (i32.const 1))))
        (block $B6
          (block $B7
            (br_if $B7
              (i32.eqz
                (i32.and
                  (i32.gt_s
                    (i32.load offset=28
                      (local.get $l3))
                    (i32.load
                      (i32.load offset=40
                        (local.get $l3))))
                  (i32.const 1))))
            (local.set $l8
              (call $mpz_realloc
                (i32.load offset=40
                  (local.get $l3))
                (i32.load offset=28
                  (local.get $l3))))
            (br $B6))
          (local.set $l8
            (i32.load offset=8
              (i32.load offset=40
                (local.get $l3)))))
        (i32.store offset=16
          (local.get $l3)
          (local.get $l8))
        (i32.store offset=12
          (local.get $l3)
          (call $mpn_sub
            (i32.load offset=16
              (local.get $l3))
            (i32.load offset=8
              (i32.load offset=36
                (local.get $l3)))
            (i32.load offset=28
              (local.get $l3))
            (i32.load offset=8
              (i32.load offset=32
                (local.get $l3)))
            (i32.load offset=24
              (local.get $l3))))
        (call $assert
          (i32.and
            (i32.eq
              (i32.load offset=12
                (local.get $l3))
              (i32.const 0))
            (i32.const 1)))
        (i32.store offset=44
          (local.get $l3)
          (call $mpn_normalized_size
            (i32.load offset=16
              (local.get $l3))
            (i32.load offset=28
              (local.get $l3))))
        (br $B4))
      (block $B8
        (br_if $B8
          (i32.eqz
            (i32.and
              (i32.lt_s
                (i32.load offset=20
                  (local.get $l3))
                (i32.const 0))
              (i32.const 1))))
        (block $B9
          (block $B10
            (br_if $B10
              (i32.eqz
                (i32.and
                  (i32.gt_s
                    (i32.load offset=24
                      (local.get $l3))
                    (i32.load
                      (i32.load offset=40
                        (local.get $l3))))
                  (i32.const 1))))
            (local.set $l9
              (call $mpz_realloc
                (i32.load offset=40
                  (local.get $l3))
                (i32.load offset=24
                  (local.get $l3))))
            (br $B9))
          (local.set $l9
            (i32.load offset=8
              (i32.load offset=40
                (local.get $l3)))))
        (i32.store offset=16
          (local.get $l3)
          (local.get $l9))
        (i32.store offset=8
          (local.get $l3)
          (call $mpn_sub
            (i32.load offset=16
              (local.get $l3))
            (i32.load offset=8
              (i32.load offset=32
                (local.get $l3)))
            (i32.load offset=24
              (local.get $l3))
            (i32.load offset=8
              (i32.load offset=36
                (local.get $l3)))
            (i32.load offset=28
              (local.get $l3))))
        (call $assert
          (i32.and
            (i32.eq
              (i32.load offset=8
                (local.get $l3))
              (i32.const 0))
            (i32.const 1)))
        (local.set $l10
          (call $mpn_normalized_size
            (i32.load offset=16
              (local.get $l3))
            (i32.load offset=24
              (local.get $l3))))
        (i32.store offset=44
          (local.get $l3)
          (i32.sub
            (i32.const 0)
            (local.get $l10)))
        (br $B4))
      (i32.store offset=44
        (local.get $l3)
        (i32.const 0)))
    (local.set $l11
      (i32.load offset=44
        (local.get $l3)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l3)
        (i32.const 48)))
    (return
      (local.get $l11)))
  (func $mpz_sub_ui (type $t7) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l3))
    (i32.store offset=12
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l3)
      (local.get $p1))
    (i32.store offset=4
      (local.get $l3)
      (local.get $p2))
    (call $mpz_ui_sub
      (i32.load offset=12
        (local.get $l3))
      (i32.load offset=4
        (local.get $l3))
      (i32.load offset=8
        (local.get $l3)))
    (call $mpz_neg
      (i32.load offset=12
        (local.get $l3))
      (i32.load offset=12
        (local.get $l3)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l3)
        (i32.const 16)))
    (return))
  (func $mpz_ui_sub (type $t7) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l3))
    (i32.store offset=12
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l3)
      (local.get $p1))
    (i32.store offset=4
      (local.get $l3)
      (local.get $p2))
    (call $mpz_neg
      (i32.load offset=12
        (local.get $l3))
      (i32.load offset=4
        (local.get $l3)))
    (call $mpz_add_ui
      (i32.load offset=12
        (local.get $l3))
      (i32.load offset=12
        (local.get $l3))
      (i32.load offset=8
        (local.get $l3)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l3)
        (i32.const 16)))
    (return))
  (func $mpz_sub (type $t7) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l3))
    (i32.store offset=12
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l3)
      (local.get $p1))
    (i32.store offset=4
      (local.get $l3)
      (local.get $p2))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.and
              (i32.ge_s
                (i32.xor
                  (i32.load offset=4
                    (i32.load offset=8
                      (local.get $l3)))
                  (i32.load offset=4
                    (i32.load offset=4
                      (local.get $l3))))
                (i32.const 0))
              (i32.const 1))))
        (i32.store
          (local.get $l3)
          (call $mpz_abs_sub
            (i32.load offset=12
              (local.get $l3))
            (i32.load offset=8
              (local.get $l3))
            (i32.load offset=4
              (local.get $l3))))
        (br $B0))
      (i32.store
        (local.get $l3)
        (call $mpz_abs_add
          (i32.load offset=12
            (local.get $l3))
          (i32.load offset=8
            (local.get $l3))
          (i32.load offset=4
            (local.get $l3)))))
    (block $B2
      (block $B3
        (br_if $B3
          (i32.eqz
            (i32.and
              (i32.ge_s
                (i32.load offset=4
                  (i32.load offset=8
                    (local.get $l3)))
                (i32.const 0))
              (i32.const 1))))
        (local.set $l4
          (i32.load
            (local.get $l3)))
        (br $B2))
      (local.set $l5
        (i32.load
          (local.get $l3)))
      (local.set $l4
        (i32.sub
          (i32.const 0)
          (local.get $l5))))
    (local.set $l6
      (local.get $l4))
    (i32.store offset=4
      (i32.load offset=12
        (local.get $l3))
      (local.get $l6))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l3)
        (i32.const 16)))
    (return))
  (func $mpz_mul_ui (type $t7) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 32)))
    (global.set $__stack_pointer
      (local.get $l3))
    (i32.store offset=28
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=24
      (local.get $l3)
      (local.get $p1))
    (i32.store offset=20
      (local.get $l3)
      (local.get $p2))
    (call $mpz_init_set_ui
      (i32.add
        (local.get $l3)
        (i32.const 8))
      (i32.load offset=20
        (local.get $l3)))
    (call $mpz_mul
      (i32.load offset=28
        (local.get $l3))
      (i32.load offset=24
        (local.get $l3))
      (i32.add
        (local.get $l3)
        (i32.const 8)))
    (call $mpz_clear
      (i32.add
        (local.get $l3)
        (i32.const 8)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l3)
        (i32.const 32)))
    (return))
  (func $mpz_mul (type $t7) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 48)))
    (global.set $__stack_pointer
      (local.get $l3))
    (i32.store offset=44
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=40
      (local.get $l3)
      (local.get $p1))
    (i32.store offset=36
      (local.get $l3)
      (local.get $p2))
    (i32.store offset=28
      (local.get $l3)
      (i32.load offset=4
        (i32.load offset=40
          (local.get $l3))))
    (i32.store offset=24
      (local.get $l3)
      (i32.load offset=4
        (i32.load offset=36
          (local.get $l3))))
    (block $B0
      (block $B1
        (block $B2
          (br_if $B2
            (i32.eqz
              (i32.load offset=28
                (local.get $l3))))
          (br_if $B1
            (i32.load offset=24
              (local.get $l3))))
        (i32.store offset=4
          (i32.load offset=44
            (local.get $l3))
          (i32.const 0))
        (br $B0))
      (i32.store offset=32
        (local.get $l3)
        (i32.and
          (i32.lt_s
            (i32.xor
              (i32.load offset=28
                (local.get $l3))
              (i32.load offset=24
                (local.get $l3)))
            (i32.const 0))
          (i32.const 1)))
      (block $B3
        (block $B4
          (br_if $B4
            (i32.eqz
              (i32.and
                (i32.ge_s
                  (i32.load offset=28
                    (local.get $l3))
                  (i32.const 0))
                (i32.const 1))))
          (local.set $l4
            (i32.load offset=28
              (local.get $l3)))
          (br $B3))
        (local.set $l5
          (i32.load offset=28
            (local.get $l3)))
        (local.set $l4
          (i32.sub
            (i32.const 0)
            (local.get $l5))))
      (i32.store offset=28
        (local.get $l3)
        (local.get $l4))
      (block $B5
        (block $B6
          (br_if $B6
            (i32.eqz
              (i32.and
                (i32.ge_s
                  (i32.load offset=24
                    (local.get $l3))
                  (i32.const 0))
                (i32.const 1))))
          (local.set $l6
            (i32.load offset=24
              (local.get $l3)))
          (br $B5))
        (local.set $l7
          (i32.load offset=24
            (local.get $l3)))
        (local.set $l6
          (i32.sub
            (i32.const 0)
            (local.get $l7))))
      (i32.store offset=24
        (local.get $l3)
        (local.get $l6))
      (call $mpz_init2
        (i32.add
          (local.get $l3)
          (i32.const 8))
        (i32.shl
          (i32.add
            (i32.load offset=28
              (local.get $l3))
            (i32.load offset=24
              (local.get $l3)))
          (i32.const 5)))
      (i32.store offset=4
        (local.get $l3)
        (i32.load offset=16
          (local.get $l3)))
      (block $B7
        (block $B8
          (br_if $B8
            (i32.eqz
              (i32.and
                (i32.ge_s
                  (i32.load offset=28
                    (local.get $l3))
                  (i32.load offset=24
                    (local.get $l3)))
                (i32.const 1))))
          (drop
            (call $mpn_mul
              (i32.load offset=4
                (local.get $l3))
              (i32.load offset=8
                (i32.load offset=40
                  (local.get $l3)))
              (i32.load offset=28
                (local.get $l3))
              (i32.load offset=8
                (i32.load offset=36
                  (local.get $l3)))
              (i32.load offset=24
                (local.get $l3))))
          (br $B7))
        (drop
          (call $mpn_mul
            (i32.load offset=4
              (local.get $l3))
            (i32.load offset=8
              (i32.load offset=36
                (local.get $l3)))
            (i32.load offset=24
              (local.get $l3))
            (i32.load offset=8
              (i32.load offset=40
                (local.get $l3)))
            (i32.load offset=28
              (local.get $l3)))))
      (i32.store offset=20
        (local.get $l3)
        (i32.add
          (i32.load offset=28
            (local.get $l3))
          (i32.load offset=24
            (local.get $l3))))
      (local.set $l8
        (i32.and
          (i32.eq
            (i32.load
              (i32.add
                (i32.load offset=4
                  (local.get $l3))
                (i32.shl
                  (i32.sub
                    (i32.load offset=20
                      (local.get $l3))
                    (i32.const 1))
                  (i32.const 2))))
            (i32.const 0))
          (i32.const 1)))
      (i32.store offset=20
        (local.get $l3)
        (i32.sub
          (i32.load offset=20
            (local.get $l3))
          (local.get $l8)))
      (block $B9
        (block $B10
          (br_if $B10
            (i32.eqz
              (i32.load offset=32
                (local.get $l3))))
          (local.set $l9
            (i32.load offset=20
              (local.get $l3)))
          (local.set $l10
            (i32.sub
              (i32.const 0)
              (local.get $l9)))
          (br $B9))
        (local.set $l10
          (i32.load offset=20
            (local.get $l3))))
      (i32.store offset=12
        (local.get $l3)
        (local.get $l10))
      (call $mpz_swap
        (i32.load offset=44
          (local.get $l3))
        (i32.add
          (local.get $l3)
          (i32.const 8)))
      (call $mpz_clear
        (i32.add
          (local.get $l3)
          (i32.const 8))))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l3)
        (i32.const 48)))
    (return))
  (func $mpz_addmul_ui (type $t7) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 32)))
    (global.set $__stack_pointer
      (local.get $l3))
    (i32.store offset=28
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=24
      (local.get $l3)
      (local.get $p1))
    (i32.store offset=20
      (local.get $l3)
      (local.get $p2))
    (call $mpz_init_set_ui
      (i32.add
        (local.get $l3)
        (i32.const 8))
      (i32.load offset=20
        (local.get $l3)))
    (call $mpz_mul
      (i32.add
        (local.get $l3)
        (i32.const 8))
      (i32.load offset=24
        (local.get $l3))
      (i32.add
        (local.get $l3)
        (i32.const 8)))
    (call $mpz_add
      (i32.load offset=28
        (local.get $l3))
      (i32.load offset=28
        (local.get $l3))
      (i32.add
        (local.get $l3)
        (i32.const 8)))
    (call $mpz_clear
      (i32.add
        (local.get $l3)
        (i32.const 8)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l3)
        (i32.const 32)))
    (return))
  (func $mpz_submul_ui (type $t7) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 32)))
    (global.set $__stack_pointer
      (local.get $l3))
    (i32.store offset=28
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=24
      (local.get $l3)
      (local.get $p1))
    (i32.store offset=20
      (local.get $l3)
      (local.get $p2))
    (call $mpz_init_set_ui
      (i32.add
        (local.get $l3)
        (i32.const 8))
      (i32.load offset=20
        (local.get $l3)))
    (call $mpz_mul
      (i32.add
        (local.get $l3)
        (i32.const 8))
      (i32.load offset=24
        (local.get $l3))
      (i32.add
        (local.get $l3)
        (i32.const 8)))
    (call $mpz_sub
      (i32.load offset=28
        (local.get $l3))
      (i32.load offset=28
        (local.get $l3))
      (i32.add
        (local.get $l3)
        (i32.const 8)))
    (call $mpz_clear
      (i32.add
        (local.get $l3)
        (i32.const 8)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l3)
        (i32.const 32)))
    (return))
  (func $mpz_addmul (type $t7) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 32)))
    (global.set $__stack_pointer
      (local.get $l3))
    (i32.store offset=28
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=24
      (local.get $l3)
      (local.get $p1))
    (i32.store offset=20
      (local.get $l3)
      (local.get $p2))
    (call $mpz_init
      (i32.add
        (local.get $l3)
        (i32.const 8)))
    (call $mpz_mul
      (i32.add
        (local.get $l3)
        (i32.const 8))
      (i32.load offset=24
        (local.get $l3))
      (i32.load offset=20
        (local.get $l3)))
    (call $mpz_add
      (i32.load offset=28
        (local.get $l3))
      (i32.load offset=28
        (local.get $l3))
      (i32.add
        (local.get $l3)
        (i32.const 8)))
    (call $mpz_clear
      (i32.add
        (local.get $l3)
        (i32.const 8)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l3)
        (i32.const 32)))
    (return))
  (func $mpz_submul (type $t7) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 32)))
    (global.set $__stack_pointer
      (local.get $l3))
    (i32.store offset=28
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=24
      (local.get $l3)
      (local.get $p1))
    (i32.store offset=20
      (local.get $l3)
      (local.get $p2))
    (call $mpz_init
      (i32.add
        (local.get $l3)
        (i32.const 8)))
    (call $mpz_mul
      (i32.add
        (local.get $l3)
        (i32.const 8))
      (i32.load offset=24
        (local.get $l3))
      (i32.load offset=20
        (local.get $l3)))
    (call $mpz_sub
      (i32.load offset=28
        (local.get $l3))
      (i32.load offset=28
        (local.get $l3))
      (i32.add
        (local.get $l3)
        (i32.const 8)))
    (call $mpz_clear
      (i32.add
        (local.get $l3)
        (i32.const 8)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l3)
        (i32.const 32)))
    (return))
  (func $mpz_div_qr (type $t9) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (param $p4 i32) (result i32)
    (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32)
    (local.set $l5
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 96)))
    (global.set $__stack_pointer
      (local.get $l5))
    (i32.store offset=88
      (local.get $l5)
      (local.get $p0))
    (i32.store offset=84
      (local.get $l5)
      (local.get $p1))
    (i32.store offset=80
      (local.get $l5)
      (local.get $p2))
    (i32.store offset=76
      (local.get $l5)
      (local.get $p3))
    (i32.store offset=72
      (local.get $l5)
      (local.get $p4))
    (i32.store offset=68
      (local.get $l5)
      (i32.load offset=4
        (i32.load offset=80
          (local.get $l5))))
    (i32.store offset=64
      (local.get $l5)
      (i32.load offset=4
        (i32.load offset=76
          (local.get $l5))))
    (block $B0
      (br_if $B0
        (i32.load offset=64
          (local.get $l5)))
      (call $gmp_die
        (i32.const 1024)))
    (block $B1
      (block $B2
        (br_if $B2
          (i32.load offset=68
            (local.get $l5)))
        (block $B3
          (br_if $B3
            (i32.eqz
              (i32.and
                (i32.ne
                  (i32.load offset=88
                    (local.get $l5))
                  (i32.const 0))
                (i32.const 1))))
          (i32.store offset=4
            (i32.load offset=88
              (local.get $l5))
            (i32.const 0)))
        (block $B4
          (br_if $B4
            (i32.eqz
              (i32.and
                (i32.ne
                  (i32.load offset=84
                    (local.get $l5))
                  (i32.const 0))
                (i32.const 1))))
          (i32.store offset=4
            (i32.load offset=84
              (local.get $l5))
            (i32.const 0)))
        (i32.store offset=92
          (local.get $l5)
          (i32.const 0))
        (br $B1))
      (block $B5
        (block $B6
          (br_if $B6
            (i32.eqz
              (i32.and
                (i32.ge_s
                  (i32.load offset=68
                    (local.get $l5))
                  (i32.const 0))
                (i32.const 1))))
          (local.set $l6
            (i32.load offset=68
              (local.get $l5)))
          (br $B5))
        (local.set $l7
          (i32.load offset=68
            (local.get $l5)))
        (local.set $l6
          (i32.sub
            (i32.const 0)
            (local.get $l7))))
      (i32.store offset=60
        (local.get $l5)
        (local.get $l6))
      (block $B7
        (block $B8
          (br_if $B8
            (i32.eqz
              (i32.and
                (i32.ge_s
                  (i32.load offset=64
                    (local.get $l5))
                  (i32.const 0))
                (i32.const 1))))
          (local.set $l8
            (i32.load offset=64
              (local.get $l5)))
          (br $B7))
        (local.set $l9
          (i32.load offset=64
            (local.get $l5)))
        (local.set $l8
          (i32.sub
            (i32.const 0)
            (local.get $l9))))
      (i32.store offset=56
        (local.get $l5)
        (local.get $l8))
      (i32.store offset=52
        (local.get $l5)
        (i32.xor
          (i32.load offset=64
            (local.get $l5))
          (i32.load offset=68
            (local.get $l5))))
      (block $B9
        (br_if $B9
          (i32.eqz
            (i32.and
              (i32.lt_s
                (i32.load offset=60
                  (local.get $l5))
                (i32.load offset=56
                  (local.get $l5)))
              (i32.const 1))))
        (block $B10
          (block $B11
            (br_if $B11
              (i32.eqz
                (i32.and
                  (i32.eq
                    (i32.load offset=72
                      (local.get $l5))
                    (i32.const 1))
                  (i32.const 1))))
            (br_if $B11
              (i32.eqz
                (i32.and
                  (i32.ge_s
                    (i32.load offset=52
                      (local.get $l5))
                    (i32.const 0))
                  (i32.const 1))))
            (block $B12
              (br_if $B12
                (i32.eqz
                  (i32.and
                    (i32.ne
                      (i32.load offset=84
                        (local.get $l5))
                      (i32.const 0))
                    (i32.const 1))))
              (call $mpz_sub
                (i32.load offset=84
                  (local.get $l5))
                (i32.load offset=80
                  (local.get $l5))
                (i32.load offset=76
                  (local.get $l5))))
            (block $B13
              (br_if $B13
                (i32.eqz
                  (i32.and
                    (i32.ne
                      (i32.load offset=88
                        (local.get $l5))
                      (i32.const 0))
                    (i32.const 1))))
              (call $mpz_set_ui
                (i32.load offset=88
                  (local.get $l5))
                (i32.const 1)))
            (br $B10))
          (block $B14
            (block $B15
              (br_if $B15
                (i32.load offset=72
                  (local.get $l5)))
              (br_if $B15
                (i32.eqz
                  (i32.and
                    (i32.lt_s
                      (i32.load offset=52
                        (local.get $l5))
                      (i32.const 0))
                    (i32.const 1))))
              (block $B16
                (br_if $B16
                  (i32.eqz
                    (i32.and
                      (i32.ne
                        (i32.load offset=84
                          (local.get $l5))
                        (i32.const 0))
                      (i32.const 1))))
                (call $mpz_add
                  (i32.load offset=84
                    (local.get $l5))
                  (i32.load offset=80
                    (local.get $l5))
                  (i32.load offset=76
                    (local.get $l5))))
              (block $B17
                (br_if $B17
                  (i32.eqz
                    (i32.and
                      (i32.ne
                        (i32.load offset=88
                          (local.get $l5))
                        (i32.const 0))
                      (i32.const 1))))
                (call $mpz_set_si
                  (i32.load offset=88
                    (local.get $l5))
                  (i32.const -1)))
              (br $B14))
            (block $B18
              (br_if $B18
                (i32.eqz
                  (i32.and
                    (i32.ne
                      (i32.load offset=84
                        (local.get $l5))
                      (i32.const 0))
                    (i32.const 1))))
              (call $mpz_set
                (i32.load offset=84
                  (local.get $l5))
                (i32.load offset=80
                  (local.get $l5))))
            (block $B19
              (br_if $B19
                (i32.eqz
                  (i32.and
                    (i32.ne
                      (i32.load offset=88
                        (local.get $l5))
                      (i32.const 0))
                    (i32.const 1))))
              (i32.store offset=4
                (i32.load offset=88
                  (local.get $l5))
                (i32.const 0)))))
        (i32.store offset=92
          (local.get $l5)
          (i32.const 1))
        (br $B1))
      (call $mpz_init_set
        (i32.add
          (local.get $l5)
          (i32.const 12))
        (i32.load offset=80
          (local.get $l5)))
      (i32.store offset=48
        (local.get $l5)
        (i32.load offset=20
          (local.get $l5)))
      (i32.store offset=40
        (local.get $l5)
        (i32.add
          (i32.sub
            (i32.load offset=60
              (local.get $l5))
            (i32.load offset=56
              (local.get $l5)))
          (i32.const 1)))
      (block $B20
        (block $B21
          (br_if $B21
            (i32.eqz
              (i32.and
                (i32.ne
                  (i32.load offset=88
                    (local.get $l5))
                  (i32.const 0))
                (i32.const 1))))
          (call $mpz_init2
            (i32.add
              (local.get $l5)
              (i32.const 24))
            (i32.shl
              (i32.load offset=40
                (local.get $l5))
              (i32.const 5)))
          (i32.store offset=44
            (local.get $l5)
            (i32.load offset=32
              (local.get $l5)))
          (br $B20))
        (i32.store offset=44
          (local.get $l5)
          (i32.const 0)))
      (call $mpn_div_qr
        (i32.load offset=44
          (local.get $l5))
        (i32.load offset=48
          (local.get $l5))
        (i32.load offset=60
          (local.get $l5))
        (i32.load offset=8
          (i32.load offset=76
            (local.get $l5)))
        (i32.load offset=56
          (local.get $l5)))
      (block $B22
        (br_if $B22
          (i32.eqz
            (i32.and
              (i32.ne
                (i32.load offset=44
                  (local.get $l5))
                (i32.const 0))
              (i32.const 1))))
        (local.set $l10
          (i32.and
            (i32.eq
              (i32.load
                (i32.add
                  (i32.load offset=44
                    (local.get $l5))
                  (i32.shl
                    (i32.sub
                      (i32.load offset=40
                        (local.get $l5))
                      (i32.const 1))
                    (i32.const 2))))
              (i32.const 0))
            (i32.const 1)))
        (i32.store offset=40
          (local.get $l5)
          (i32.sub
            (i32.load offset=40
              (local.get $l5))
            (local.get $l10)))
        (block $B23
          (block $B24
            (br_if $B24
              (i32.eqz
                (i32.and
                  (i32.lt_s
                    (i32.load offset=52
                      (local.get $l5))
                    (i32.const 0))
                  (i32.const 1))))
            (local.set $l11
              (i32.load offset=40
                (local.get $l5)))
            (local.set $l12
              (i32.sub
                (i32.const 0)
                (local.get $l11)))
            (br $B23))
          (local.set $l12
            (i32.load offset=40
              (local.get $l5))))
        (i32.store offset=28
          (local.get $l5)
          (local.get $l12)))
      (i32.store offset=36
        (local.get $l5)
        (call $mpn_normalized_size
          (i32.load offset=48
            (local.get $l5))
          (i32.load offset=56
            (local.get $l5))))
      (block $B25
        (block $B26
          (br_if $B26
            (i32.eqz
              (i32.and
                (i32.lt_s
                  (i32.load offset=68
                    (local.get $l5))
                  (i32.const 0))
                (i32.const 1))))
          (local.set $l13
            (i32.load offset=36
              (local.get $l5)))
          (local.set $l14
            (i32.sub
              (i32.const 0)
              (local.get $l13)))
          (br $B25))
        (local.set $l14
          (i32.load offset=36
            (local.get $l5))))
      (i32.store offset=16
        (local.get $l5)
        (local.get $l14))
      (block $B27
        (block $B28
          (br_if $B28
            (i32.load offset=72
              (local.get $l5)))
          (br_if $B28
            (i32.eqz
              (i32.and
                (i32.lt_s
                  (i32.load offset=52
                    (local.get $l5))
                  (i32.const 0))
                (i32.const 1))))
          (br_if $B28
            (i32.eqz
              (i32.load offset=36
                (local.get $l5))))
          (block $B29
            (br_if $B29
              (i32.eqz
                (i32.and
                  (i32.ne
                    (i32.load offset=88
                      (local.get $l5))
                    (i32.const 0))
                  (i32.const 1))))
            (call $mpz_sub_ui
              (i32.add
                (local.get $l5)
                (i32.const 24))
              (i32.add
                (local.get $l5)
                (i32.const 24))
              (i32.const 1)))
          (block $B30
            (br_if $B30
              (i32.eqz
                (i32.and
                  (i32.ne
                    (i32.load offset=84
                      (local.get $l5))
                    (i32.const 0))
                  (i32.const 1))))
            (call $mpz_add
              (i32.add
                (local.get $l5)
                (i32.const 12))
              (i32.add
                (local.get $l5)
                (i32.const 12))
              (i32.load offset=76
                (local.get $l5))))
          (br $B27))
        (block $B31
          (br_if $B31
            (i32.eqz
              (i32.and
                (i32.eq
                  (i32.load offset=72
                    (local.get $l5))
                  (i32.const 1))
                (i32.const 1))))
          (br_if $B31
            (i32.eqz
              (i32.and
                (i32.ge_s
                  (i32.load offset=52
                    (local.get $l5))
                  (i32.const 0))
                (i32.const 1))))
          (br_if $B31
            (i32.eqz
              (i32.load offset=36
                (local.get $l5))))
          (block $B32
            (br_if $B32
              (i32.eqz
                (i32.and
                  (i32.ne
                    (i32.load offset=88
                      (local.get $l5))
                    (i32.const 0))
                  (i32.const 1))))
            (call $mpz_add_ui
              (i32.add
                (local.get $l5)
                (i32.const 24))
              (i32.add
                (local.get $l5)
                (i32.const 24))
              (i32.const 1)))
          (block $B33
            (br_if $B33
              (i32.eqz
                (i32.and
                  (i32.ne
                    (i32.load offset=84
                      (local.get $l5))
                    (i32.const 0))
                  (i32.const 1))))
            (call $mpz_sub
              (i32.add
                (local.get $l5)
                (i32.const 12))
              (i32.add
                (local.get $l5)
                (i32.const 12))
              (i32.load offset=76
                (local.get $l5))))))
      (block $B34
        (br_if $B34
          (i32.eqz
            (i32.and
              (i32.ne
                (i32.load offset=88
                  (local.get $l5))
                (i32.const 0))
              (i32.const 1))))
        (call $mpz_swap
          (i32.add
            (local.get $l5)
            (i32.const 24))
          (i32.load offset=88
            (local.get $l5)))
        (call $mpz_clear
          (i32.add
            (local.get $l5)
            (i32.const 24))))
      (block $B35
        (br_if $B35
          (i32.eqz
            (i32.and
              (i32.ne
                (i32.load offset=84
                  (local.get $l5))
                (i32.const 0))
              (i32.const 1))))
        (call $mpz_swap
          (i32.add
            (local.get $l5)
            (i32.const 12))
          (i32.load offset=84
            (local.get $l5))))
      (call $mpz_clear
        (i32.add
          (local.get $l5)
          (i32.const 12)))
      (i32.store offset=92
        (local.get $l5)
        (i32.and
          (i32.ne
            (i32.load offset=36
              (local.get $l5))
            (i32.const 0))
          (i32.const 1))))
    (local.set $l15
      (i32.load offset=92
        (local.get $l5)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l5)
        (i32.const 96)))
    (return
      (local.get $l15)))
  (func $mpz_tdiv_q (type $t7) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l3))
    (i32.store offset=12
      (local.get $l3)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l3)
      (local.get $p1))
    (i32.store offset=4
      (local.get $l3)
      (local.get $p2))
    (local.set $l4
      (i32.load offset=12
        (local.get $l3)))
    (local.set $l5
      (i32.load offset=8
        (local.get $l3)))
    (local.set $l6
      (i32.load offset=4
        (local.get $l3)))
    (drop
      (call $mpz_div_qr
        (local.get $l4)
        (i32.const 0)
        (local.get $l5)
        (local.get $l6)
        (i32.const 2)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l3)
        (i32.const 16)))
    (return))
  (func $mpz_cmp (type $t5) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 32)))
    (global.set $__stack_pointer
      (local.get $l2))
    (i32.store offset=24
      (local.get $l2)
      (local.get $p0))
    (i32.store offset=20
      (local.get $l2)
      (local.get $p1))
    (i32.store offset=16
      (local.get $l2)
      (i32.load offset=4
        (i32.load offset=24
          (local.get $l2))))
    (i32.store offset=12
      (local.get $l2)
      (i32.load offset=4
        (i32.load offset=20
          (local.get $l2))))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.and
              (i32.ne
                (i32.load offset=16
                  (local.get $l2))
                (i32.load offset=12
                  (local.get $l2)))
              (i32.const 1))))
        (local.set $l3
          (i32.lt_s
            (i32.load offset=16
              (local.get $l2))
            (i32.load offset=12
              (local.get $l2))))
        (i32.store offset=28
          (local.get $l2)
          (select
            (i32.const -1)
            (i32.const 1)
            (i32.and
              (local.get $l3)
              (i32.const 1))))
        (br $B0))
      (block $B2
        (br_if $B2
          (i32.eqz
            (i32.and
              (i32.ge_s
                (i32.load offset=16
                  (local.get $l2))
                (i32.const 0))
              (i32.const 1))))
        (i32.store offset=28
          (local.get $l2)
          (call $mpn_cmp
            (i32.load offset=8
              (i32.load offset=24
                (local.get $l2)))
            (i32.load offset=8
              (i32.load offset=20
                (local.get $l2)))
            (i32.load offset=16
              (local.get $l2))))
        (br $B0))
      (local.set $l4
        (i32.load offset=8
          (i32.load offset=20
            (local.get $l2))))
      (local.set $l5
        (i32.load offset=8
          (i32.load offset=24
            (local.get $l2))))
      (local.set $l6
        (i32.load offset=16
          (local.get $l2)))
      (i32.store offset=28
        (local.get $l2)
        (call $mpn_cmp
          (local.get $l4)
          (local.get $l5)
          (i32.sub
            (i32.const 0)
            (local.get $l6)))))
    (local.set $l7
      (i32.load offset=28
        (local.get $l2)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l2)
        (i32.const 32)))
    (return
      (local.get $l7)))
  (func $extract_digit (type $t0) (param $p0 i32) (result i32)
    (local $l1 i32) (local $l2 i32) (local $l3 i32)
    (local.set $l1
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l1))
    (i32.store offset=12
      (local.get $l1)
      (local.get $p0))
    (local.set $l2
      (i32.load offset=12
        (local.get $l1)))
    (call $mpz_mul_ui
      (i32.const 1172)
      (i32.const 1184)
      (local.get $l2))
    (call $mpz_add
      (i32.const 1196)
      (i32.const 1172)
      (i32.const 1208))
    (call $mpz_tdiv_q
      (i32.const 1172)
      (i32.const 1196)
      (i32.const 1220))
    (local.set $l3
      (call $mpz_get_ui
        (i32.const 1172)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l1)
        (i32.const 16)))
    (return
      (local.get $l3)))
  (func $eliminate_digit (type $t4) (param $p0 i32)
    (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32)
    (local.set $l1
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l1))
    (i32.store offset=12
      (local.get $l1)
      (local.get $p0))
    (local.set $l2
      (i32.load offset=12
        (local.get $l1)))
    (call $mpz_submul_ui
      (i32.const 1208)
      (i32.const 1220)
      (local.get $l2))
    (local.set $l3
      (i32.const 1208))
    (call $mpz_mul_ui
      (local.get $l3)
      (local.get $l3)
      (i32.const 10))
    (local.set $l4
      (i32.const 1184))
    (call $mpz_mul_ui
      (local.get $l4)
      (local.get $l4)
      (i32.const 10))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l1)
        (i32.const 16)))
    (return))
  (func $next_term (type $t4) (param $p0 i32)
    (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32)
    (local.set $l1
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l1))
    (i32.store offset=12
      (local.get $l1)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l1)
      (i32.add
        (i32.shl
          (i32.load offset=12
            (local.get $l1))
          (i32.const 1))
        (i32.const 1)))
    (call $mpz_addmul_ui
      (i32.const 1208)
      (i32.const 1184)
      (i32.const 2))
    (local.set $l2
      (i32.load offset=8
        (local.get $l1)))
    (local.set $l3
      (i32.const 1208))
    (call $mpz_mul_ui
      (local.get $l3)
      (local.get $l3)
      (local.get $l2))
    (local.set $l4
      (i32.load offset=8
        (local.get $l1)))
    (local.set $l5
      (i32.const 1220))
    (call $mpz_mul_ui
      (local.get $l5)
      (local.get $l5)
      (local.get $l4))
    (local.set $l6
      (i32.load offset=12
        (local.get $l1)))
    (local.set $l7
      (i32.const 1184))
    (call $mpz_mul_ui
      (local.get $l7)
      (local.get $l7)
      (local.get $l6))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l1)
        (i32.const 16)))
    (return))
  (func $_start (type $t6) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32)
    (local.set $l0
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 32)))
    (global.set $__stack_pointer
      (local.get $l0))
    (i32.store offset=16
      (local.get $l0)
      (call $__VERIFIER_nondet_int))
    (call $mpz_init
      (i32.const 1172))
    (call $mpz_init
      (i32.const 1196))
    (call $mpz_init_set_ui
      (i32.const 1208)
      (i32.const 0))
    (call $mpz_init_set_ui
      (i32.const 1220)
      (i32.const 1))
    (call $mpz_init_set_ui
      (i32.const 1184)
      (i32.const 1))
    (i32.store offset=24
      (local.get $l0)
      (i32.const 0))
    (i32.store offset=20
      (local.get $l0)
      (i32.const 0))
    (block $B0
      (loop $L1
        (br_if $B0
          (i32.eqz
            (i32.and
              (i32.lt_u
                (i32.load offset=20
                  (local.get $l0))
                (i32.load offset=16
                  (local.get $l0)))
              (i32.const 1))))
        (local.set $l1
          (i32.add
            (i32.load offset=24
              (local.get $l0))
            (i32.const 1)))
        (i32.store offset=24
          (local.get $l0)
          (local.get $l1))
        (call $next_term
          (local.get $l1))
        (block $B2
          (br_if $B2
            (i32.eqz
              (i32.and
                (i32.gt_s
                  (call $mpz_cmp
                    (i32.const 1184)
                    (i32.const 1208))
                  (i32.const 0))
                (i32.const 1))))
          (br $L1))
        (i32.store offset=28
          (local.get $l0)
          (call $extract_digit
            (i32.const 3)))
        (block $B3
          (br_if $B3
            (i32.eqz
              (i32.and
                (i32.ne
                  (i32.load offset=28
                    (local.get $l0))
                  (call $extract_digit
                    (i32.const 4)))
                (i32.const 1))))
          (br $L1))
        (drop
          (call $putchar
            (i32.add
              (i32.load offset=28
                (local.get $l0))
              (i32.const 48))))
        (local.set $l2
          (i32.add
            (i32.load offset=20
              (local.get $l0))
            (i32.const 1)))
        (i32.store offset=20
          (local.get $l0)
          (local.get $l2))
        (block $B4
          (br_if $B4
            (i32.rem_u
              (local.get $l2)
              (i32.const 10)))
          (i32.store
            (local.get $l0)
            (i32.load offset=20
              (local.get $l0)))
          (drop
            (call $printf
              (i32.const 1144)
              (local.get $l0))))
        (call $eliminate_digit
          (i32.load offset=28
            (local.get $l0)))
        (br $L1)))
    (local.set $l3
      (i32.const 0))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l0)
        (i32.const 32)))
    (return
      (local.get $l3)))
  (table $__indirect_function_table 4 4 funcref)
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66768))
  (global $tmp1 i32 (i32.const 1172))
  (global $num i32 (i32.const 1184))
  (global $tmp2 i32 (i32.const 1196))
  (global $acc i32 (i32.const 1208))
  (global $den i32 (i32.const 1220))
  (global $__dso_handle i32 (i32.const 1024))
  (global $__data_end i32 (i32.const 1232))
  (global $__stack_low i32 (i32.const 1232))
  (global $__stack_high i32 (i32.const 66768))
  (global $__global_base i32 (i32.const 1024))
  (global $__heap_base i32 (i32.const 66768))
  (global $__heap_end i32 (i32.const 131072))
  (global $__memory_base i32 (i32.const 0))
  (global $__table_base i32 (i32.const 1))
  (global $__wasm_first_page_end i32 (i32.const 65536))
  (export "memory" (memory $memory))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "gmp_die" (func $gmp_die))
  (export "gmp_default_alloc" (func $gmp_default_alloc))
  (export "gmp_default_realloc" (func $gmp_default_realloc))
  (export "gmp_default_free" (func $gmp_default_free))
  (export "gmp_alloc_limbs" (func $gmp_alloc_limbs))
  (export "__indirect_function_table" (table $__indirect_function_table))
  (export "gmp_realloc_limbs" (func $gmp_realloc_limbs))
  (export "gmp_free_limbs" (func $gmp_free_limbs))
  (export "mpn_copyi" (func $mpn_copyi))
  (export "mpn_copyd" (func $mpn_copyd))
  (export "mpn_cmp" (func $mpn_cmp))
  (export "mpn_cmp4" (func $mpn_cmp4))
  (export "mpn_normalized_size" (func $mpn_normalized_size))
  (export "mpn_zero_p" (func $mpn_zero_p))
  (export "mpn_zero" (func $mpn_zero))
  (export "mpn_add_1" (func $mpn_add_1))
  (export "mpn_add_n" (func $mpn_add_n))
  (export "mpn_add" (func $mpn_add))
  (export "mpn_sub_1" (func $mpn_sub_1))
  (export "mpn_sub_n" (func $mpn_sub_n))
  (export "mpn_sub" (func $mpn_sub))
  (export "mpn_mul_1" (func $mpn_mul_1))
  (export "mpn_addmul_1" (func $mpn_addmul_1))
  (export "mpn_submul_1" (func $mpn_submul_1))
  (export "mpn_mul" (func $mpn_mul))
  (export "mpn_mul_n" (func $mpn_mul_n))
  (export "mpn_sqr" (func $mpn_sqr))
  (export "mpn_lshift" (func $mpn_lshift))
  (export "mpn_rshift" (func $mpn_rshift))
  (export "mpn_common_scan" (func $mpn_common_scan))
  (export "mpn_scan1" (func $mpn_scan1))
  (export "mpn_scan0" (func $mpn_scan0))
  (export "mpn_com" (func $mpn_com))
  (export "mpn_neg" (func $mpn_neg))
  (export "mpn_invert_3by2" (func $mpn_invert_3by2))
  (export "mpn_div_qr_1_invert" (func $mpn_div_qr_1_invert))
  (export "mpn_div_qr_2_invert" (func $mpn_div_qr_2_invert))
  (export "mpn_div_qr_invert" (func $mpn_div_qr_invert))
  (export "mpn_div_qr_1_preinv" (func $mpn_div_qr_1_preinv))
  (export "mpn_div_qr_2_preinv" (func $mpn_div_qr_2_preinv))
  (export "mpn_div_qr_pi1" (func $mpn_div_qr_pi1))
  (export "mpn_div_qr_preinv" (func $mpn_div_qr_preinv))
  (export "mpn_div_qr" (func $mpn_div_qr))
  (export "mpz_init" (func $mpz_init))
  (export "mpz_init2" (func $mpz_init2))
  (export "mpz_clear" (func $mpz_clear))
  (export "mpz_realloc" (func $mpz_realloc))
  (export "mpz_set_si" (func $mpz_set_si))
  (export "mpz_set_ui" (func $mpz_set_ui))
  (export "mpz_set" (func $mpz_set))
  (export "mpz_init_set_ui" (func $mpz_init_set_ui))
  (export "mpz_init_set" (func $mpz_init_set))
  (export "mpz_get_ui" (func $mpz_get_ui))
  (export "mpz_abs" (func $mpz_abs))
  (export "mpz_neg" (func $mpz_neg))
  (export "mpz_swap" (func $mpz_swap))
  (export "mpz_add_ui" (func $mpz_add_ui))
  (export "mpz_add" (func $mpz_add))
  (export "mpz_abs_add" (func $mpz_abs_add))
  (export "mpz_abs_sub" (func $mpz_abs_sub))
  (export "mpz_sub_ui" (func $mpz_sub_ui))
  (export "mpz_ui_sub" (func $mpz_ui_sub))
  (export "mpz_sub" (func $mpz_sub))
  (export "mpz_mul_ui" (func $mpz_mul_ui))
  (export "mpz_mul" (func $mpz_mul))
  (export "mpz_addmul_ui" (func $mpz_addmul_ui))
  (export "mpz_submul_ui" (func $mpz_submul_ui))
  (export "mpz_addmul" (func $mpz_addmul))
  (export "mpz_submul" (func $mpz_submul))
  (export "mpz_div_qr" (func $mpz_div_qr))
  (export "mpz_tdiv_q" (func $mpz_tdiv_q))
  (export "mpz_cmp" (func $mpz_cmp))
  (export "extract_digit" (func $extract_digit))
  (export "tmp1" (global $tmp1))
  (export "num" (global $num))
  (export "tmp2" (global $tmp2))
  (export "acc" (global $acc))
  (export "den" (global $den))
  (export "eliminate_digit" (func $eliminate_digit))
  (export "next_term" (func $next_term))
  (export "_start" (func $_start))
  (export "__dso_handle" (global $__dso_handle))
  (export "__data_end" (global $__data_end))
  (export "__stack_low" (global $__stack_low))
  (export "__stack_high" (global $__stack_high))
  (export "__global_base" (global $__global_base))
  (export "__heap_base" (global $__heap_base))
  (export "__heap_end" (global $__heap_end))
  (export "__memory_base" (global $__memory_base))
  (export "__table_base" (global $__table_base))
  (export "__wasm_first_page_end" (global $__wasm_first_page_end))
  (elem $e0 (i32.const 1) func $gmp_default_alloc $gmp_default_realloc $gmp_default_free)
  (data $.rodata (i32.const 1024) "mpz_div_qr: Divide by zero.\00gmp_default_realloc: Virtual memory exhausted.\00gmp_default_alloc: Virtual memory exhausted.\00\09:%u\0a\00%s\0a\00\00\00\a0\c1\00\00")
  (data $.data (i32.const 1160) "\01\00\00\00\02\00\00\00\03\00\00\00")
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01U\17\00\00\024\00I\13:\0b;\05\02\18\00\00\03\01\01I\13\00\00\04!\00I\137\0b\00\00\05$\00\03\0e>\0b\0b\0b\00\00\06$\00\03\0e\0b\0b>\0b\00\00\07.\01\11\01\12\06@\18\03\0e:\0b;\05'\19?\19\00\00\084\00\03\0eI\13:\0b;\05\02\18\00\00\09\05\00\02\18\03\0e:\0b;\05I\13\00\00\0a&\00I\13\00\00\0b\16\00I\13\03\0e:\0b;\0b\00\00\0c4\00I\13:\0b;\0b\02\18\00\00\0d4\00\03\0eI\13?\19:\0b;\0b\02\18\00\00\0e\13\01\0b\0b:\0b;\0b\00\00\0f\0d\00\03\0eI\13:\0b;\0b8\0b\00\00\10\0f\00I\13\00\00\11\15\01I\13'\19\00\00\12\05\00I\13\00\00\13\0f\00\00\00\14\15\01'\19\00\00\15\04\01I\13\03\0e\0b\0b:\0b;\05\00\00\16(\00\03\0e\1c\0f\00\00\17.\01\11\01\12\06@\18\03\0e:\0b;\05'\19I\13?\19\00\00\184\00\02\18\03\0e:\0b;\05I\13\00\00\19\0b\01U\17\00\00\1a\0b\01\11\01\12\06\00\00\1b.\01\11\01\12\06@\18\03\0e:\0b;\0b'\19I\13?\19\00\00\1c\05\00\02\18\03\0e:\0b;\0bI\13\00\00\1d.\01\11\01\12\06@\18\03\0e:\0b;\0b'\19?\19\00\00\1e4\00\02\18\03\0e:\0b;\0bI\13\00\00\1f.\01\11\01\12\06@\18\03\0e:\0b;\0bI\13?\19\00\00 \13\01\03\0e\0b\0b:\0b;\05\00\00!\0d\00\03\0eI\13:\0b;\058\0b\00\00\00")
  (@custom ".debug_info" "\06)\00\00\04\00\00\00\00\00\04\01P\06\00\00\1d\00\92\05\00\00\00\00\00\00\11\03\00\00\00\00\00\00`\00\00\00\024\00\00\00\01:\02\05\03~\04\00\00\03@\00\00\00\04G\00\00\00\04\00\05\c2\01\00\00\06\01\06\ee\05\00\00\08\07\02\5c\00\00\00\01G\02\05\03K\04\00\00\03@\00\00\00\04G\00\00\00-\00\02v\00\00\00\01T\02\05\03\1c\04\00\00\03@\00\00\00\04G\00\00\00/\00\0728\00\009\00\00\00\04\ed\00\01\9f\b8\00\00\00\01\90\05\08\cb\05\00\00\b9\00\00\00\01\92\05\05\03\84\04\00\00\09\02\91\0c\d2\01\00\00\01\90\05\ef(\00\00\00\0a\be\00\00\00\0b\c9\00\00\007\01\00\00\016\052\04\00\00\07\04\02\de\00\00\00\01\fb\06\05\03\00\04\00\00\03@\00\00\00\04G\00\00\00\1c\00\0c\f7\00\00\00\02<\05\03x\04\00\00\03@\00\00\00\04G\00\00\00\06\00\0d\ad\06\00\00\14\01\00\00\02\0a\05\03\94\04\00\00\0b\1f\01\00\00\1b\01\00\00\01G\03+\01\00\00\04G\00\00\00\01\00\0b6\01\00\00\0e\01\00\00\01E\0e\0c\01=\0f<\05\00\00_\01\00\00\01?\00\0fe\04\00\00_\01\00\00\01A\04\0f\04\05\00\00f\01\00\00\01D\08\00\05\b0\00\00\00\05\04\10\be\00\00\00\0d\92\06\00\00\14\01\00\00\02\0a\05\03\ac\04\00\00\0d~\05\00\00\14\01\00\00\02\0a\05\03\b8\04\00\00\0d\81\02\00\00\14\01\00\00\02\0a\05\03\c4\04\00\00\0d\b9\02\00\00\14\01\00\00\02\0a\05\03\a0\04\00\00\08Z\05\00\00\c1\01\00\00\01_\02\05\03\88\04\00\00\10\c6\01\00\00\11\d1\01\00\00\12\d2\01\00\00\00\13\0b\c9\00\00\000\01\00\00\03\9c\08F\05\00\00\ef\01\00\00\01`\02\05\03\8c\04\00\00\10\f4\01\00\00\11\d1\01\00\00\12\d1\01\00\00\12\d2\01\00\00\12\d2\01\00\00\00\08l\05\00\00\1b\02\00\00\01a\02\05\03\90\04\00\00\10 \02\00\00\14\12\d1\01\00\00\12\d2\01\00\00\00\15L\02\00\00\c7\04\00\00\04\01\ef\06\16'\06\00\00\00\165\06\00\00\01\16B\06\00\00\02\00\05\a7\00\00\00\07\04\0bf\01\00\00\9d\01\00\00\01:\0b\c9\00\00\00!\01\00\00\018\07\05\00\00\00K\00\00\00\04\ed\00\01\9f\ae\04\00\00\018\02\09\02\91\0c.\04\00\00\018\02\88(\00\00\00\17Q\00\00\00p\00\00\00\04\ed\00\01\9f*\05\00\00\01?\02\d1\01\00\00\09\02\91\0c\99\04\00\00\01?\02\d2\01\00\00\18\02\91\08_\02\00\00\01A\02\d1\01\00\00\00\17\c2\00\00\00r\00\00\00\04\ed\00\03\9f\16\05\00\00\01M\02\d1\01\00\00\09\02\91\0c\e4\04\00\00\01M\02\d1\01\00\00\09\02\91\08n\04\00\00\01M\02\d2\01\00\00\09\02\91\04\5c\04\00\00\01M\02\d2\01\00\00\18\02\91\00_\02\00\00\01O\02\d1\01\00\00\00\075\01\00\00<\00\00\00\04\ed\00\02\9f\b6\04\00\00\01Z\02\09\02\91\0c_\02\00\00\01Z\02\d1\01\00\00\09\02\91\08\92\04\00\00\01Z\02\d2\01\00\00\00\17r\01\00\00N\00\00\00\04\ed\00\01\9fp\01\00\00\01h\02S\02\00\00\09\02\91\0c\99\04\00\00\01h\02k(\00\00\00\17\c1\01\00\00z\00\00\00\04\ed\00\03\9f^\01\00\00\01n\02S\02\00\00\09\02\91\0c\e4\04\00\00\01n\02S\02\00\00\09\02\91\08u\04\00\00\01n\02k(\00\00\09\02\91\04\99\04\00\00\01n\02k(\00\00\00\07<\02\00\00V\00\00\00\04\ed\00\02\9fO\01\00\00\01u\02\09\02\91\0c\e4\04\00\00\01u\02S\02\00\00\09\02\91\08\99\04\00\00\01u\02k(\00\00\00\07\93\02\00\00x\00\00\00\04\ed\00\03\9f\9e\03\00\00\01~\02\09\02\91\0c\08\05\00\00\01~\02S\02\00\00\09\02\91\08\86\01\00\00\01~\02\92(\00\00\09\02\91\04\b7\02\00\00\01~\02k(\00\00\18\02\91\00\17\04\00\00\01\80\02k(\00\00\00\07\0c\03\00\00o\00\00\00\04\ed\00\03\9f\da\04\00\00\01\86\02\09\02\91\0c\08\05\00\00\01\86\02S\02\00\00\09\02\91\08\86\01\00\00\01\86\02\92(\00\00\09\02\91\04\b7\02\00\00\01\86\02k(\00\00\00\17}\03\00\00\ba\00\00\00\04\ed\00\03\9f?\02\00\00\01\8d\02_\01\00\00\09\02\91\08S\02\00\00\01\8d\02\92(\00\00\09\02\91\04J\02\00\00\01\8d\02\92(\00\00\09\02\91\00\b7\02\00\00\01\8d\02k(\00\00\00\179\04\00\00\97\00\00\00\04\ed\00\04\9fe\06\00\00\01\98\02_\01\00\00\09\02\91\18S\02\00\00\01\98\02\92(\00\00\09\02\91\14\98\02\00\00\01\98\02k(\00\00\09\02\91\10J\02\00\00\01\98\02\92(\00\00\09\02\91\0c\88\02\00\00\01\98\02k(\00\00\00\17\d1\04\00\00z\00\00\00\04\ed\00\02\9f~\04\00\00\01\a1\02k(\00\00\09\02\91\0c\e2\01\00\00\01\a1\02\92(\00\00\09\02\91\08\b7\02\00\00\01\a1\02k(\00\00\00\17L\05\00\00K\00\00\00\04\ed\00\02\9fV\02\00\00\01\a9\02_\01\00\00\09\02\91\0c\ee\01\00\00\01\a9\02\92(\00\00\09\02\91\08\b7\02\00\00\01\a9\02k(\00\00\00\07\98\05\00\00U\00\00\00\04\ed\00\02\9fa\02\00\00\01\af\02\09\02\91\0c\ee\01\00\00\01\af\02S\02\00\00\09\02\91\08\b7\02\00\00\01\af\02k(\00\00\00\17\ef\05\00\00\ca\00\00\00\04\ed\00\04\9f\f2\06\00\00\01\b6\02\be\00\00\00\09\02\91\1c\ee\01\00\00\01\b6\02S\02\00\00\09\02\91\18S\02\00\00\01\b6\02\92(\00\00\09\02\91\14\b7\02\00\00\01\b6\02k(\00\00\09\02\91\10\ea\05\00\00\01\b6\02\be\00\00\00\18\02\91\0c\17\04\00\00\01\b8\02k(\00\00\19\00\00\00\00\18\02\91\08\d2\01\00\00\01\be\02\be\00\00\00\00\00\17\bb\06\00\00\f7\00\00\00\04\ed\00\04\9f\a5\02\00\00\01\c9\02\be\00\00\00\09\02\91,\ee\01\00\00\01\c9\02S\02\00\00\09\02\91(S\02\00\00\01\c9\02\92(\00\00\09\02\91$J\02\00\00\01\c9\02\92(\00\00\09\02\91 \b7\02\00\00\01\c9\02k(\00\00\18\02\91\1c\17\04\00\00\01\cb\02k(\00\00\18\02\91\18\02\00\00\00\01\cc\02\be\00\00\00\1a\08\07\00\00\92\00\00\00\18\02\91\14\ec\05\00\00\01\d0\02\be\00\00\00\18\02\91\10\ea\05\00\00\01\d0\02\be\00\00\00\18\02\91\0c\d2\01\00\00\01\d0\02\be\00\00\00\00\00\17\b4\07\00\00\cd\00\00\00\04\ed\00\05\9f\fc\04\00\00\01\dc\02\be\00\00\00\09\02\91\1c\ee\01\00\00\01\dc\02S\02\00\00\09\02\91\18S\02\00\00\01\dc\02\92(\00\00\09\02\91\14\98\02\00\00\01\dc\02k(\00\00\09\02\91\10J\02\00\00\01\dc\02\92(\00\00\09\02\91\0c\88\02\00\00\01\dc\02k(\00\00\18\02\91\08\02\00\00\00\01\de\02\be\00\00\00\00\17\83\08\00\00\d4\00\00\00\04\ed\00\04\9f\fc\06\00\00\01\e9\02\be\00\00\00\09\02\91\1c\ee\01\00\00\01\e9\02S\02\00\00\09\02\91\18S\02\00\00\01\e9\02\92(\00\00\09\02\91\14\b7\02\00\00\01\e9\02k(\00\00\09\02\91\10\ea\05\00\00\01\e9\02\be\00\00\00\18\02\91\0c\17\04\00\00\01\eb\02k(\00\00\19\18\00\00\00\18\02\91\08\ec\05\00\00\01\f2\02\be\00\00\00\18\02\91\04\02\00\00\00\01\f4\02\be\00\00\00\00\00\17Y\09\00\00\ed\00\00\00\04\ed\00\04\9f\af\02\00\00\01\fe\02\be\00\00\00\09\02\91\1c\ee\01\00\00\01\fe\02S\02\00\00\09\02\91\18S\02\00\00\01\fe\02\92(\00\00\09\02\91\14J\02\00\00\01\fe\02\92(\00\00\09\02\91\10\b7\02\00\00\01\fe\02k(\00\00\18\02\91\0c\17\04\00\00\01\00\03k(\00\00\18\02\91\08\02\00\00\00\01\01\03\be\00\00\00\1a\a6\09\00\00\88\00\00\00\18\02\91\04\ec\05\00\00\01\05\03\be\00\00\00\18\02\91\00\ea\05\00\00\01\05\03\be\00\00\00\00\00\17H\0a\00\00\cd\00\00\00\04\ed\00\05\9f\b8\05\00\00\01\10\03\be\00\00\00\09\02\91\1c\ee\01\00\00\01\10\03S\02\00\00\09\02\91\18S\02\00\00\01\10\03\92(\00\00\09\02\91\14\98\02\00\00\01\10\03k(\00\00\09\02\91\10J\02\00\00\01\10\03\92(\00\00\09\02\91\0c\88\02\00\00\01\10\03k(\00\00\18\02\91\08\02\00\00\00\01\12\03\be\00\00\00\00\17\17\0b\00\00\e9\01\00\00\04\ed\00\04\9f\e8\06\00\00\01\1d\03\be\00\00\00\09\03\91\cc\00\ee\01\00\00\01\1d\03S\02\00\00\09\03\91\c8\00\e8\01\00\00\01\1d\03\92(\00\00\09\03\91\c4\00\b7\02\00\00\01\1d\03k(\00\00\09\03\91\c0\00\d1\02\00\00\01\1d\03\be\00\00\00\18\02\91<\fc\02\00\00\01\1f\03\be\00\00\00\18\02\918\8e\03\00\00\01\1f\03\be\00\00\00\18\02\914\0d\03\00\00\01\1f\03\be\00\00\00\18\02\910\09\03\00\00\01\1f\03\be\00\00\00\1a\7f\0b\00\00\0c\01\00\00\18\02\91,\13\06\00\00\01'\03_\01\00\00\1a\86\0b\00\00\05\01\00\00\18\02\91(\06\07\00\00\01'\03\be\00\00\00\18\02\91$\9e\06\00\00\01'\03\be\00\00\00\18\02\91 \83\06\00\00\01'\03\be\00\00\00\18\02\91\1cn\06\00\00\01'\03\be\00\00\00\18\02\91\18\fa\02\00\00\01'\03L\02\00\00\18\02\91\14\cf\02\00\00\01'\03L\02\00\00\18\02\91\10\1e\04\00\00\01'\03L\02\00\00\18\02\91\0c\19\04\00\00\01'\03L\02\00\00\18\02\91\08b\00\00\00\01'\03\be\00\00\00\18\02\91\04^\00\00\00\01'\03\be\00\00\00\00\00\00\17\02\0d\00\00\1f\02\00\00\04\ed\00\04\9f\ce\06\00\00\014\03\be\00\00\00\09\03\91\cc\00\ee\01\00\00\014\03S\02\00\00\09\03\91\c8\00\e8\01\00\00\014\03\92(\00\00\09\03\91\c4\00\b7\02\00\00\014\03k(\00\00\09\03\91\c0\00\d1\02\00\00\014\03\be\00\00\00\18\02\91<\fc\02\00\00\016\03\be\00\00\00\18\02\918\8e\03\00\00\016\03\be\00\00\00\18\02\914\0d\03\00\00\016\03\be\00\00\00\18\02\910\09\03\00\00\016\03\be\00\00\00\18\02\91,\02\03\00\00\016\03\be\00\00\00\1aj\0d\00\00\0c\01\00\00\18\02\91(\13\06\00\00\01>\03_\01\00\00\1aq\0d\00\00\05\01\00\00\18\02\91$\06\07\00\00\01>\03\be\00\00\00\18\02\91 \9e\06\00\00\01>\03\be\00\00\00\18\02\91\1c\83\06\00\00\01>\03\be\00\00\00\18\02\91\18n\06\00\00\01>\03\be\00\00\00\18\02\91\14\fa\02\00\00\01>\03L\02\00\00\18\02\91\10\cf\02\00\00\01>\03L\02\00\00\18\02\91\0c\1e\04\00\00\01>\03L\02\00\00\18\02\91\08\19\04\00\00\01>\03L\02\00\00\18\02\91\04b\00\00\00\01>\03\be\00\00\00\18\02\91\00^\00\00\00\01>\03\be\00\00\00\00\00\00\17#\0f\00\00\1f\02\00\00\04\ed\00\04\9f\db\06\00\00\01N\03\be\00\00\00\09\03\91\cc\00\ee\01\00\00\01N\03S\02\00\00\09\03\91\c8\00\e8\01\00\00\01N\03\92(\00\00\09\03\91\c4\00\b7\02\00\00\01N\03k(\00\00\09\03\91\c0\00\d1\02\00\00\01N\03\be\00\00\00\18\02\91<\fc\02\00\00\01P\03\be\00\00\00\18\02\918\8e\03\00\00\01P\03\be\00\00\00\18\02\914\0d\03\00\00\01P\03\be\00\00\00\18\02\910\09\03\00\00\01P\03\be\00\00\00\18\02\91,\02\03\00\00\01P\03\be\00\00\00\1a\8b\0f\00\00\0c\01\00\00\18\02\91(\13\06\00\00\01X\03_\01\00\00\1a\92\0f\00\00\05\01\00\00\18\02\91$\06\07\00\00\01X\03\be\00\00\00\18\02\91 \9e\06\00\00\01X\03\be\00\00\00\18\02\91\1c\83\06\00\00\01X\03\be\00\00\00\18\02\91\18n\06\00\00\01X\03\be\00\00\00\18\02\91\14\fa\02\00\00\01X\03L\02\00\00\18\02\91\10\cf\02\00\00\01X\03L\02\00\00\18\02\91\0c\1e\04\00\00\01X\03L\02\00\00\18\02\91\08\19\04\00\00\01X\03L\02\00\00\18\02\91\04b\00\00\00\01X\03\be\00\00\00\18\02\91\00^\00\00\00\01X\03\be\00\00\00\00\00\00\17D\11\00\00\d0\01\00\00\04\ed\00\05\9f\f2\02\00\00\01h\03\be\00\00\00\09\02\91\1c\ee\01\00\00\01h\03S\02\00\00\09\02\91\18\e8\01\00\00\01h\03\92(\00\00\09\02\91\14m\02\00\00\01h\03k(\00\00\09\02\91\10\e5\01\00\00\01h\03\92(\00\00\09\02\91\0cj\02\00\00\01h\03k(\00\00\00\07\15\13\00\00_\00\00\00\04\ed\00\04\9f\9b\02\00\00\01\81\03\09\02\91\0c\ee\01\00\00\01\81\03S\02\00\00\09\02\91\08S\02\00\00\01\81\03\92(\00\00\09\02\91\04J\02\00\00\01\81\03\92(\00\00\09\02\91\00\b7\02\00\00\01\81\03k(\00\00\00\07u\13\00\00X\00\00\00\04\ed\00\03\9f\a4\01\00\00\01\87\03\09\02\91\0c\ee\01\00\00\01\87\03S\02\00\00\09\02\91\08S\02\00\00\01\87\03\92(\00\00\09\02\91\04\b7\02\00\00\01\87\03k(\00\00\00\17\cf\13\00\00\88\01\00\00\04\ed\00\04\9f\ee\00\00\00\01\8d\03\be\00\00\00\09\02\91\1c\ee\01\00\00\01\8d\03S\02\00\00\09\02\91\18\e8\01\00\00\01\8d\03\92(\00\00\09\02\91\14\b7\02\00\00\01\8d\03k(\00\00\09\02\91\10\b4\00\00\00\01\8d\03L\02\00\00\18\02\91\0c\df\05\00\00\01\8f\03\be\00\00\00\18\02\91\08\d6\05\00\00\01\8f\03\be\00\00\00\18\02\91\04z\05\00\00\01\90\03L\02\00\00\18\02\91\00\91\03\00\00\01\91\03\be\00\00\00\00\17Y\15\00\00L\01\00\00\04\ed\00\04\9f\e3\00\00\00\01\ab\03\be\00\00\00\09\02\91\1c\ee\01\00\00\01\ab\03S\02\00\00\09\02\91\18\e8\01\00\00\01\ab\03\92(\00\00\09\02\91\14\b7\02\00\00\01\ab\03k(\00\00\09\02\91\10\b4\00\00\00\01\ab\03L\02\00\00\18\02\91\0c\df\05\00\00\01\ad\03\be\00\00\00\18\02\91\08\d6\05\00\00\01\ad\03\be\00\00\00\18\02\91\04z\05\00\00\01\ae\03L\02\00\00\18\02\91\00\91\03\00\00\01\af\03\be\00\00\00\00\17\a7\16\00\00\0c\02\00\00\04\ed\00\05\9f\8b\02\00\00\01\c6\03^\02\00\00\09\02\91(\e4\05\00\00\01\c6\03\be\00\00\00\09\02\91$\17\04\00\00\01\c6\03k(\00\00\09\02\91 \e8\01\00\00\01\c6\03\92(\00\00\09\02\91\1cm\02\00\00\01\c6\03k(\00\00\09\02\91\18\05\00\00\00\01\c7\03\be\00\00\00\18\02\91\14\b4\00\00\00\01\c9\03L\02\00\00\1a\b9\17\00\00\d0\00\00\00\18\02\91\10\08\00\00\00\01\d5\03\be\00\00\00\18\02\91\0c\82\05\00\00\01\d5\03L\02\00\00\1a\ca\17\00\00\ae\00\00\00\18\02\91\08\10\00\00\00\01\d5\03\be\00\00\00\18\02\91\04\8a\05\00\00\01\d5\03L\02\00\00\18\02\91\00\02\06\00\00\01\d5\03_\01\00\00\00\00\00\17\b4\18\00\00~\00\00\00\04\ed\00\02\9f\b2\06\00\00\01\da\03^\02\00\00\09\02\91\0c\a0\01\00\00\01\da\03\92(\00\00\09\02\91\08\df\00\00\00\01\da\03^\02\00\00\18\02\91\04\17\04\00\00\01\dc\03k(\00\00\00\174\19\00\00\81\00\00\00\04\ed\00\02\9f\19\07\00\00\01\e4\03^\02\00\00\09\02\91\0c\a0\01\00\00\01\e4\03\92(\00\00\09\02\91\08\df\00\00\00\01\e4\03^\02\00\00\18\02\91\04\17\04\00\00\01\e6\03k(\00\00\00\07\b6\19\00\00|\00\00\00\04\ed\00\03\9f\c7\02\00\00\01\ee\03\09\02\91\0c\ee\01\00\00\01\ee\03S\02\00\00\09\02\91\08\e8\01\00\00\01\ee\03\92(\00\00\09\02\91\04\b7\02\00\00\01\ee\03k(\00\00\00\174\1a\00\00\fc\00\00\00\04\ed\00\03\9fH\04\00\00\01\f5\03\be\00\00\00\09\02\91\08\ee\01\00\00\01\f5\03S\02\00\00\09\02\91\04\e8\01\00\00\01\f5\03\92(\00\00\09\02\91\00\b7\02\00\00\01\f5\03k(\00\00\00\172\1b\00\00\04\04\00\00\04\ed\00\02\9fs\06\00\00\01\0b\04\be\00\00\00\09\03\91\dc\00\a3\06\00\00\01\0b\04\be\00\00\00\09\03\91\d8\00\0b\07\00\00\01\0b\04\be\00\00\00\18\03\91\d4\00\d2\01\00\00\01\0d\04\be\00\00\00\18\03\91\d0\00\cd\02\00\00\01\0d\04\be\00\00\00\1aW\1b\00\00\b7\01\00\00\18\03\91\cc\00_\02\00\00\01\10\04\be\00\00\00\18\03\91\c8\00\06\03\00\00\01\10\04\be\00\00\00\18\03\91\c4\00\fc\02\00\00\01\11\04L\02\00\00\18\03\91\c0\00 \04\00\00\01\11\04L\02\00\00\18\02\91<(\04\00\00\01\11\04L\02\00\00\00\1a\1f\1d\00\00\fe\01\00\00\18\02\918$\04\00\00\01^\04\be\00\00\00\18\02\914\ff\02\00\00\01^\04\be\00\00\00\1a\a6\1d\00\00\0c\01\00\00\18\02\910\13\06\00\00\01k\04_\01\00\00\1a\ad\1d\00\00\05\01\00\00\18\02\91,\06\07\00\00\01k\04\be\00\00\00\18\02\91(\9e\06\00\00\01k\04\be\00\00\00\18\02\91$\83\06\00\00\01k\04\be\00\00\00\18\02\91 n\06\00\00\01k\04\be\00\00\00\18\02\91\1c\fa\02\00\00\01k\04L\02\00\00\18\02\91\18\cf\02\00\00\01k\04L\02\00\00\18\02\91\14\1e\04\00\00\01k\04L\02\00\00\18\02\91\10\19\04\00\00\01k\04L\02\00\00\18\02\91\0cb\00\00\00\01k\04\be\00\00\00\18\02\91\08^\00\00\00\01k\04\be\00\00\00\00\00\00\00\078\1f\00\00#\01\00\00\04\ed\00\02\9f\8c\00\00\00\01\82\04\09\02\91\1cZ\00\00\00\01\82\04\a2(\00\00\09\02\91\18\08\05\00\00\01\82\04\be\00\00\00\18\02\91\14\f3\00\00\00\01\84\04L\02\00\00\1am\1f\00\00\9d\00\00\00\18\02\91\10\10\00\00\00\01\87\04\be\00\00\00\18\02\91\0c\8a\05\00\00\01\87\04L\02\00\00\18\02\91\08\02\06\00\00\01\87\04_\01\00\00\00\00\07] \00\00\85\01\00\00\04\ed\00\03\9fx\00\00\00\01\8e\04\09\02\91\1cZ\00\00\00\01\8e\04\a2(\00\00\09\02\91\18\cb\06\00\00\01\8f\04\be\00\00\00\09\02\91\14#\07\00\00\01\8f\04\be\00\00\00\18\02\91\10\f3\00\00\00\01\91\04L\02\00\00\1a\99 \00\00\9d\00\00\00\18\02\91\0c\10\00\00\00\01\94\04\be\00\00\00\18\02\91\08\8a\05\00\00\01\94\04L\02\00\00\18\02\91\04\02\06\00\00\01\94\04_\01\00\00\00\00\07\e4!\00\00H\02\00\00\04\ed\00\03\9ff\00\00\00\01\a1\04\09\02\91,Z\00\00\00\01\a1\04\a2(\00\00\09\02\91(G\02\00\00\01\a2\04\92(\00\00\09\02\91$\85\02\00\00\01\a2\04k(\00\00\1ax\22\00\00\a5\01\00\00\18\02\91 \f3\00\00\00\01\ac\04L\02\00\00\18\02\91\1c\cb\06\00\00\01\ad\04\be\00\00\00\18\02\91\18#\07\00\00\01\ad\04\be\00\00\00\1a\bb\22\00\00\9d\00\00\00\18\02\91\14\10\00\00\00\01\b2\04\be\00\00\00\18\02\91\10\8a\05\00\00\01\b2\04L\02\00\00\18\02\91\0c\02\06\00\00\01\b2\04_\01\00\00\00\00\00\17.$\00\00d\03\00\00\04\ed\00\04\9fE\00\00\00\01\c2\04\be\00\00\00\09\03\91\ec\00\f1\01\00\00\01\c2\04S\02\00\00\09\03\91\e8\00\f4\01\00\00\01\c2\04\92(\00\00\09\03\91\e4\00y\02\00\00\01\c2\04k(\00\00\09\03\91\e0\00Z\00\00\00\01\c3\04\e5(\00\00\18\03\91\dc\00\08\05\00\00\01\c5\04\be\00\00\00\18\03\91\d8\00\16\04\00\00\01\c5\04\be\00\00\00\18\03\91\d4\00\d2\01\00\00\01\c6\04\be\00\00\00\18\03\91\d0\00\eb\01\00\00\01\c7\04S\02\00\00\18\03\91\cc\00p\02\00\00\01\c8\04k(\00\00\1a)%\00\00)\02\00\00\18\03\91\c8\00\e0\01\00\00\01\dd\04\be\00\00\00\1a)%\00\00\ff\01\00\00\18\03\91\c4\00'\04\00\00\01\df\04\be\00\00\00\18\03\91\c0\00\05\03\00\00\01\df\04\be\00\00\00\18\02\91<\d1\01\00\00\01\df\04\be\00\00\00\18\02\918\98\03\00\00\01\df\04\be\00\00\00\1a)%\00\00\0c\01\00\00\18\02\914\13\06\00\00\01\df\04_\01\00\00\1a0%\00\00\05\01\00\00\18\02\910\06\07\00\00\01\df\04\be\00\00\00\18\02\91,\9e\06\00\00\01\df\04\be\00\00\00\18\02\91(\83\06\00\00\01\df\04\be\00\00\00\18\02\91$n\06\00\00\01\df\04\be\00\00\00\18\02\91 \fa\02\00\00\01\df\04L\02\00\00\18\02\91\1c\cf\02\00\00\01\df\04L\02\00\00\18\02\91\18\1e\04\00\00\01\df\04L\02\00\00\18\02\91\14\19\04\00\00\01\df\04L\02\00\00\18\02\91\10b\00\00\00\01\df\04\be\00\00\00\18\02\91\0c^\00\00\00\01\df\04\be\00\00\00\00\00\1a5&\00\00H\00\00\00\18\02\91\08\18\00\00\00\01\df\04\be\00\00\00\00\00\00\00\07\94'\00\00\13\06\00\00\04\ed\00\04\9f1\00\00\00\01\ea\04\09\03\91\ac\01\f1\01\00\00\01\ea\04S\02\00\00\09\03\91\a8\01\f4\01\00\00\01\ea\04S\02\00\00\09\03\91\a4\01y\02\00\00\01\ea\04k(\00\00\09\03\91\a0\01Z\00\00\00\01\eb\04\e5(\00\00\18\03\91\9c\01\f3\00\00\00\01\ed\04L\02\00\00\18\03\91\98\01\17\04\00\00\01\ee\04k(\00\00\18\03\91\94\01\cb\06\00\00\01\ef\04\be\00\00\00\18\03\91\90\01#\07\00\00\01\ef\04\be\00\00\00\18\03\91\8c\01\16\04\00\00\01\ef\04\be\00\00\00\18\03\91\88\01\aa\06\00\00\01\ef\04\be\00\00\00\18\03\91\84\01\12\07\00\00\01\ef\04\be\00\00\00\190\00\00\00\18\03\91\80\01 \07\00\00\01\01\05\be\00\00\00\18\03\91\fc\00\e0\01\00\00\01\01\05\be\00\00\00\1a\a2(\00\00\ff\03\00\00\18\03\91\f8\00\15\07\00\00\01\03\05\be\00\00\00\18\03\91\f4\00\a6\06\00\00\01\03\05\be\00\00\00\18\03\91\f0\00\0e\07\00\00\01\03\05\be\00\00\00\18\03\91\ec\00\98\03\00\00\01\03\05\be\00\00\00\1a\a2(\00\00\0e\01\00\00\18\03\91\e8\00\13\06\00\00\01\03\05_\01\00\00\1a\a9(\00\00\07\01\00\00\18\03\91\e4\00\06\07\00\00\01\03\05\be\00\00\00\18\03\91\e0\00\9e\06\00\00\01\03\05\be\00\00\00\18\03\91\dc\00\83\06\00\00\01\03\05\be\00\00\00\18\03\91\d8\00n\06\00\00\01\03\05\be\00\00\00\18\03\91\d4\00\fa\02\00\00\01\03\05L\02\00\00\18\03\91\d0\00\cf\02\00\00\01\03\05L\02\00\00\18\03\91\cc\00\1e\04\00\00\01\03\05L\02\00\00\18\03\91\c8\00\19\04\00\00\01\03\05L\02\00\00\18\03\91\c4\00b\00\00\00\01\03\05\be\00\00\00\18\03\91\c0\00^\00\00\00\01\03\05\be\00\00\00\00\00\1a\b0)\00\00;\00\00\00\18\02\91<\18\00\00\00\01\03\05\be\00\00\00\00\1a\04*\00\00A\00\00\00\18\02\918\18\00\00\00\01\03\05\be\00\00\00\00\1aE*\00\00\0d\01\00\00\18\02\914\13\06\00\00\01\03\05_\01\00\00\1aL*\00\00\06\01\00\00\18\02\910\06\07\00\00\01\03\05\be\00\00\00\18\02\91,\9e\06\00\00\01\03\05\be\00\00\00\18\02\91(\83\06\00\00\01\03\05\be\00\00\00\18\02\91$n\06\00\00\01\03\05\be\00\00\00\18\02\91 \fa\02\00\00\01\03\05L\02\00\00\18\02\91\1c\cf\02\00\00\01\03\05L\02\00\00\18\02\91\18\1e\04\00\00\01\03\05L\02\00\00\18\02\91\14\19\04\00\00\01\03\05L\02\00\00\18\02\91\10b\00\00\00\01\03\05\be\00\00\00\18\02\91\0c^\00\00\00\01\03\05\be\00\00\00\00\00\1aR+\00\00>\00\00\00\18\02\91\08\18\00\00\00\01\03\05\be\00\00\00\00\1a\c8+\00\00L\00\00\00\18\02\91\04\18\00\00\00\01\03\05\be\00\00\00\00\1a`,\00\00A\00\00\00\18\02\91\00\18\00\00\00\01\03\05\be\00\00\00\00\00\00\00\07\a9-\00\00L\07\00\00\04\ed\00\07\9f\bc\06\00\00\01\16\05\09\03\91\bc\01\f1\01\00\00\01\16\05S\02\00\00\09\03\91\b8\01\f4\01\00\00\01\17\05S\02\00\00\09\03\91\b4\01y\02\00\00\01\17\05k(\00\00\09\03\91\b0\01\b9\06\00\00\01\17\05\be\00\00\00\09\03\91\ac\01G\02\00\00\01\18\05\92(\00\00\09\03\91\a8\01\85\02\00\00\01\18\05k(\00\00\09\03\91\a4\01Y\00\00\00\01\19\05\be\00\00\00\18\03\91\a0\01\17\04\00\00\01\1b\05k(\00\00\18\03\91\9c\01\cb\06\00\00\01\1d\05\be\00\00\00\18\03\91\98\01#\07\00\00\01\1d\05\be\00\00\00\18\03\91\94\01\02\00\00\00\01\1e\05\be\00\00\00\18\03\91\90\01\9a\06\00\00\01\1e\05\be\00\00\00\18\03\91\8c\01\e0\01\00\00\01\1f\05\be\00\00\00\19H\00\00\00\18\03\91\88\01 \07\00\00\011\05\be\00\00\00\1a*/\00\00?\04\00\00\18\03\91\84\01\15\07\00\00\01;\05\be\00\00\00\18\03\91\80\01\a6\06\00\00\01;\05\be\00\00\00\18\03\91\fc\00\0e\07\00\00\01;\05\be\00\00\00\18\03\91\f8\00\98\03\00\00\01;\05\be\00\00\00\1a*/\00\00\10\01\00\00\18\03\91\f4\00\13\06\00\00\01;\05_\01\00\00\1a1/\00\00\09\01\00\00\18\03\91\f0\00\06\07\00\00\01;\05\be\00\00\00\18\03\91\ec\00\9e\06\00\00\01;\05\be\00\00\00\18\03\91\e8\00\83\06\00\00\01;\05\be\00\00\00\18\03\91\e4\00n\06\00\00\01;\05\be\00\00\00\18\03\91\e0\00\fa\02\00\00\01;\05L\02\00\00\18\03\91\dc\00\cf\02\00\00\01;\05L\02\00\00\18\03\91\d8\00\1e\04\00\00\01;\05L\02\00\00\18\03\91\d4\00\19\04\00\00\01;\05L\02\00\00\18\03\91\d0\00b\00\00\00\01;\05\be\00\00\00\18\03\91\cc\00^\00\00\00\01;\05\be\00\00\00\00\00\1a:0\00\00@\00\00\00\18\03\91\c8\00\18\00\00\00\01;\05\be\00\00\00\00\1a\940\00\00o\00\00\00\18\03\91\c4\00\18\00\00\00\01;\05\be\00\00\00\00\1a\031\00\00\0f\01\00\00\18\03\91\c0\00\13\06\00\00\01;\05_\01\00\00\1a\0a1\00\00\08\01\00\00\18\02\91<\06\07\00\00\01;\05\be\00\00\00\18\02\918\9e\06\00\00\01;\05\be\00\00\00\18\02\914\83\06\00\00\01;\05\be\00\00\00\18\02\910n\06\00\00\01;\05\be\00\00\00\18\02\91,\fa\02\00\00\01;\05L\02\00\00\18\02\91(\cf\02\00\00\01;\05L\02\00\00\18\02\91$\1e\04\00\00\01;\05L\02\00\00\18\02\91 \19\04\00\00\01;\05L\02\00\00\18\02\91\1cb\00\00\00\01;\05\be\00\00\00\18\02\91\18^\00\00\00\01;\05\be\00\00\00\00\00\1a\122\00\00?\00\00\00\18\02\91\14\18\00\00\00\01;\05\be\00\00\00\00\1a\8e2\00\00L\00\00\00\18\02\91\10\18\00\00\00\01;\05\be\00\00\00\00\1a(3\00\00A\00\00\00\18\02\91\0c\18\00\00\00\01;\05\be\00\00\00\00\00\00\00\07\f74\00\00\fb\01\00\00\04\ed\00\06\9f\1f\00\00\00\01U\05\09\02\91,\f1\01\00\00\01U\05S\02\00\00\09\02\91(\f4\01\00\00\01U\05S\02\00\00\09\02\91$y\02\00\00\01U\05k(\00\00\09\02\91 G\02\00\00\01V\05\92(\00\00\09\02\91\1c\85\02\00\00\01V\05k(\00\00\09\02\91\18Z\00\00\00\01W\05\e5(\00\00\1a\c65\00\00\1c\01\00\00\18\02\91\14+\04\00\00\01b\05\be\00\00\00\18\02\91\10\f3\00\00\00\01c\05L\02\00\00\1a\b26\00\000\00\00\00\18\02\91\0c\00\00\00\00\01r\05\be\00\00\00\00\00\00\07\f46\00\00=\01\00\00\04\ed\00\05\9f\b7\01\00\00\01w\05\09\02\91,\f1\01\00\00\01w\05S\02\00\00\09\02\91(\f4\01\00\00\01w\05S\02\00\00\09\02\91$y\02\00\00\01w\05k(\00\00\09\02\91 G\02\00\00\01w\05\92(\00\00\09\02\91\1c\85\02\00\00\01w\05k(\00\00\18\02\91\0cZ\00\00\00\01y\05\a7(\00\00\18\02\91\08\eb\01\00\00\01z\05S\02\00\00\1a\a47\00\000\00\00\00\18\02\91\04\00\00\00\00\01\83\05\be\00\00\00\00\00\07m8\00\00\8d\00\00\00\04\ed\00\02\9f\88\06\00\00\01\9c\05\09\02\91\0c\d2\01\00\00\01\9c\05\ef(\00\00\09\02\91\08A\01\00\00\01\9c\05^\02\00\00\18\02\91\04s\02\00\00\01\9e\05k(\00\00\00\07\fb8\00\00N\00\00\00\04\ed\00\01\9f\c7\01\00\00\01\a9\05\09\02\91\0c\d2\01\00\00\01\a9\05\ef(\00\00\00\17K9\00\00\16\01\00\00\04\ed\00\02\9f\0a\05\00\00\01\b0\05S\02\00\00\09\02\91\0c\d2\01\00\00\01\b0\05\ef(\00\00\09\02\91\08\99\04\00\00\01\b0\05k(\00\00\00\07c:\00\00\b2\00\00\00\04\ed\00\02\9f\0b\04\00\00\01\c6\05\09\02\91\0c\d2\01\00\00\01\c6\05\ef(\00\00\09\02\91\08\1a\00\00\00\01\c6\05v(\00\00\00\07\17;\00\00\9f\00\00\00\04\ed\00\02\9f\a8\03\00\00\01\d8\05\09\02\91\0c\d2\01\00\00\01\d8\05\ef(\00\00\09\02\91\08\1a\00\00\00\01\d8\05\c9\00\00\00\00\07\b8;\00\00\eb\00\00\00\04\ed\00\02\9f\f9\00\00\00\01\ed\05\09\02\91\0c\d2\01\00\00\01\ed\05\ef(\00\00\09\02\91\08\1a\00\00\00\01\ed\05\f4(\00\00\1a\ef;\00\00\a6\00\00\00\18\02\91\04\b7\02\00\00\01\f2\05k(\00\00\18\02\91\00\ee\01\00\00\01\f3\05S\02\00\00\00\00\07\a4<\00\00L\00\00\00\04\ed\00\02\9f\b3\03\00\00\01\fe\05\09\02\91\0c\d2\01\00\00\01\fe\05\ef(\00\00\09\02\91\08\1a\00\00\00\01\fe\05\c9\00\00\00\00\07\f1<\00\00L\00\00\00\04\ed\00\02\9f\01\01\00\00\01\05\06\09\02\91\0c\d2\01\00\00\01\05\06\ef(\00\00\09\02\91\08\1a\00\00\00\01\05\06\f4(\00\00\00\17>=\00\00<\00\00\00\04\ed\00\01\9f\c3\03\00\00\01\0c\06\c9\00\00\00\09\02\91\0cd\00\00\00\01\0c\06\f4(\00\00\00\07|=\00\00\83\00\00\00\04\ed\00\02\9f\80\01\00\00\01\1d\06\09\02\91\0c\d2\01\00\00\01\1d\06\ef(\00\00\09\02\91\08d\00\00\00\01\1d\06\f4(\00\00\00\07\00>\00\00\5c\00\00\00\04\ed\00\02\9f@\04\00\00\01$\06\09\02\91\0c\d2\01\00\00\01$\06\ef(\00\00\09\02\91\08d\00\00\00\01$\06\f4(\00\00\00\07^>\00\00\b4\00\00\00\04\ed\00\02\9fM\02\00\00\01+\06\09\02\91\1cd\00\00\00\01+\06\ef(\00\00\09\02\91\18`\00\00\00\01+\06\ef(\00\00\1az>\00\002\00\00\00\18\02\91\14\f7\01\00\00\01-\06k(\00\00\00\1a\ac>\00\002\00\00\00\18\02\91\10$\02\00\00\01.\06S\02\00\00\00\1a\de>\00\002\00\00\00\18\02\91\0c\f7\01\00\00\01.\06k(\00\00\00\00\07\13?\00\00h\00\00\00\04\ed\00\03\9f\f5\03\00\00\016\06\09\02\91\1c\d2\01\00\00\016\06\ef(\00\00\09\02\91\18\ec\05\00\00\016\06\f4(\00\00\09\02\91\14\ea\05\00\00\016\06\c9\00\00\00\18\02\91\08\e9\05\00\00\018\06\14\01\00\00\00\07}?\00\00\ca\00\00\00\04\ed\00\03\9f\e8\04\00\00\01|\06\09\02\91\0c\d2\01\00\00\01|\06\ef(\00\00\09\02\91\08\ec\05\00\00\01|\06\f4(\00\00\09\02\91\04\ea\05\00\00\01|\06\f4(\00\00\18\02\91\00s\02\00\00\01~\06k(\00\00\00\07\13D\00\00]\00\00\00\04\ed\00\03\9f\00\04\00\00\01?\06\09\02\91\0c\d2\01\00\00\01?\06\ef(\00\00\09\02\91\08\ec\05\00\00\01?\06\f4(\00\00\09\02\91\04\ea\05\00\00\01?\06\c9\00\00\00\00\07qD\00\00]\00\00\00\04\ed\00\03\9f\c0\05\00\00\01F\06\09\02\91\0c\d2\01\00\00\01F\06\ef(\00\00\09\02\91\08\ec\05\00\00\01F\06\c9\00\00\00\09\02\91\04\ea\05\00\00\01F\06\f4(\00\00\00\17I@\00\00\96\01\00\00\04\ed\00\03\9f\f0\04\00\00\01M\06k(\00\00\09\02\91,\d2\01\00\00\01M\06\ef(\00\00\09\02\91(\ec\05\00\00\01M\06\f4(\00\00\09\02\91$\ea\05\00\00\01M\06\f4(\00\00\18\02\91 \98\02\00\00\01O\06k(\00\00\18\02\91\1c\88\02\00\00\01P\06k(\00\00\18\02\91\18\ee\01\00\00\01Q\06S\02\00\00\18\02\91\14\02\00\00\00\01R\06\be\00\00\00\1a\fd@\00\00\1e\00\00\00\18\02\91\10\0d\02\00\00\01V\06\fe(\00\00\00\1a\1bA\00\00\1e\00\00\00\18\02\91\0c\f7\01\00\00\01W\06k(\00\00\00\00\17\e1A\00\001\02\00\00\04\ed\00\03\9f\ac\05\00\00\01c\06k(\00\00\09\02\91(\d2\01\00\00\01c\06\ef(\00\00\09\02\91$\ec\05\00\00\01c\06\f4(\00\00\09\02\91 \ea\05\00\00\01c\06\f4(\00\00\18\02\91\1c\98\02\00\00\01e\06k(\00\00\18\02\91\18\88\02\00\00\01f\06k(\00\00\18\02\91\14C\02\00\00\01g\06_\01\00\00\18\02\91\10\ee\01\00\00\01h\06S\02\00\00\1a\f8B\00\00;\00\00\00\18\02\91\0c\00\00\00\00\01n\06\be\00\00\00\00\1a\9aC\00\00;\00\00\00\18\02\91\08\00\00\00\00\01t\06\be\00\00\00\00\00\07\d0D\00\00\ca\00\00\00\04\ed\00\03\9f\a4\05\00\00\01\89\06\09\02\91\0c\d2\01\00\00\01\89\06\ef(\00\00\09\02\91\08\ec\05\00\00\01\89\06\f4(\00\00\09\02\91\04\ea\05\00\00\01\89\06\f4(\00\00\18\02\91\00s\02\00\00\01\8b\06k(\00\00\00\07\9bE\00\00h\00\00\00\04\ed\00\03\9f\ea\03\00\00\01\96\06\09\02\91\1c\d2\01\00\00\01\96\06\ef(\00\00\09\02\91\18d\00\00\00\01\96\06\f4(\00\00\09\02\91\14`\00\00\00\01\96\06\c9\00\00\00\18\02\91\08\1c\00\00\00\01\98\06\14\01\00\00\00\07\05F\00\00\f8\01\00\00\04\ed\00\03\9f\ea\02\00\00\01\a0\06\09\02\91,\d2\01\00\00\01\a0\06\ef(\00\00\09\02\91(d\00\00\00\01\a0\06\f4(\00\00\09\02\91$`\00\00\00\01\a0\06\f4(\00\00\18\02\91 |\02\00\00\01\a2\06_\01\00\00\18\02\91\1cm\02\00\00\01\a3\06k(\00\00\18\02\91\18j\02\00\00\01\a3\06k(\00\00\18\02\91\14s\02\00\00\01\a3\06k(\00\00\18\02\91\08?\01\00\00\01\a4\06\14\01\00\00\18\02\91\04\eb\01\00\00\01\a5\06S\02\00\00\00\07\feG\00\00}\00\00\00\04\ed\00\03\9f\ce\03\00\00\01\c6\06\09\02\91\1c\d2\01\00\00\01\c6\06\ef(\00\00\09\02\91\18d\00\00\00\01\c6\06\f4(\00\00\09\02\91\14`\00\00\00\01\c6\06\c9\00\00\00\18\02\91\08?\01\00\00\01\c8\06\14\01\00\00\00\07|H\00\00}\00\00\00\04\ed\00\03\9f\dc\03\00\00\01\d0\06\09\02\91\1c\d2\01\00\00\01\d0\06\ef(\00\00\09\02\91\18d\00\00\00\01\d0\06\f4(\00\00\09\02\91\14`\00\00\00\01\d0\06\c9\00\00\00\18\02\91\08?\01\00\00\01\d2\06\14\01\00\00\00\07\faH\00\00x\00\00\00\04\ed\00\03\9f\d4\02\00\00\01\da\06\09\02\91\1c\d2\01\00\00\01\da\06\ef(\00\00\09\02\91\18d\00\00\00\01\da\06\f4(\00\00\09\02\91\14`\00\00\00\01\da\06\f4(\00\00\18\02\91\08?\01\00\00\01\dc\06\14\01\00\00\00\07sI\00\00x\00\00\00\04\ed\00\03\9f\df\02\00\00\01\e4\06\09\02\91\1c\d2\01\00\00\01\e4\06\ef(\00\00\09\02\91\18d\00\00\00\01\e4\06\f4(\00\00\09\02\91\14`\00\00\00\01\e4\06\f4(\00\00\18\02\91\08?\01\00\00\01\e6\06\14\01\00\00\00\17\edI\00\00\e5\04\00\00\04\ed\00\05\9f\ac\01\00\00\01\f3\06_\01\00\00\09\03\91\d8\00\e0\01\00\00\01\f3\06\ef(\00\00\09\03\91\d4\00\d2\01\00\00\01\f3\06\ef(\00\00\09\03\91\d0\00\b7\02\00\00\01\f4\06\f4(\00\00\09\03\91\cc\00\08\05\00\00\01\f4\06\f4(\00\00\09\03\91\c8\00\d5\04\00\00\01\f4\06,\02\00\00\18\03\91\c4\00I\01\00\00\01\f6\06k(\00\00\18\03\91\c0\00L\01\00\00\01\f6\06k(\00\00\18\02\91<y\02\00\00\01\f6\06k(\00\00\18\02\918\85\02\00\00\01\f6\06k(\00\00\18\02\914F\01\00\00\01\f6\06k(\00\00\1a8L\00\00\82\02\00\00\18\02\910\f4\01\00\00\01)\07S\02\00\00\18\02\91,\f1\01\00\00\01)\07S\02\00\00\18\02\91(v\02\00\00\01*\07k(\00\00\18\02\91$s\02\00\00\01*\07k(\00\00\18\02\91\18\d4\01\00\00\01+\07\14\01\00\00\18\02\91\0c\a1\01\00\00\01+\07\14\01\00\00\00\00\07\d3N\00\00^\00\00\00\04\ed\00\03\9f\d7\01\00\00\01c\07\09\02\91\0c\e0\01\00\00\01c\07\ef(\00\00\09\02\91\08\b7\02\00\00\01c\07\f4(\00\00\09\02\91\04\08\05\00\00\01c\07\f4(\00\00\00\173O\00\00\eb\00\00\00\04\ed\00\02\9f7\02\00\00\01i\07_\01\00\00\09\02\91\18\ec\05\00\00\01i\07\f4(\00\00\09\02\91\14\ea\05\00\00\01i\07\f4(\00\00\18\02\91\10V\04\00\00\01k\07k(\00\00\18\02\91\0cP\04\00\00\01l\07k(\00\00\00\1b P\00\00\85\00\00\00\04\ed\00\01\9f\c1\00\00\00\02\0d}(\00\00\1c\02\91\0c#\04\00\00\02\0d}(\00\00\00\1d\a6P\00\00m\00\00\00\04\ed\00\01\9f\cf\00\00\00\02\16\1c\02\91\0c\08\05\00\00\02\16}(\00\00\00\1d\15Q\00\00\9f\00\00\00\04\ed\00\01\9f\bd\02\00\00\02\1c\1c\02\91\0c\9c\03\00\00\02\1c}(\00\00\1e\02\91\08\97\06\00\00\02\1d}(\00\00\00\1f\b6Q\00\00J\01\00\00\04\ed\00\00\9f\a0\00\00\00\02%_\01\00\00\1e\02\91\1c\08\05\00\00\02&}(\00\00\1e\02\91\18\9c\03\00\00\02&}(\00\00\1e\02\91\14\17\04\00\00\02&}(\00\00\1e\02\91\10\b7\02\00\00\02'_\01\00\00\00\0bv(\00\00-\01\00\00\017\05;\04\00\00\05\04\0bL\02\00\00\08\04\00\00\02\0b\10\8d(\00\00\0a@\00\00\00\0b\9d(\00\00\93\01\00\00\01;\10\b9\00\00\00\10\a7(\00\00 \9e\04\00\00\10\01w\04!\f3\00\00\00L\02\00\00\01z\04\00!\cb\06\00\00\be\00\00\00\01|\04\04!#\07\00\00\be\00\00\00\01|\04\08!\16\04\00\00\be\00\00\00\01~\04\0c\00\10\ea(\00\00\0a\a7(\00\00\10+\01\00\00\10\f9(\00\00\0a+\01\00\00\0b\f4(\00\00\88\01\00\00\01J\00")
  (@custom ".debug_ranges" "9\06\00\00\84\06\00\00\9d\06\00\00\a3\06\00\00\00\00\00\00\00\00\00\00\cd\08\00\00\22\09\00\00;\09\00\00A\09\00\00\00\00\00\00\00\00\00\00\87(\00\00\d0,\00\00\e9,\00\00\ef,\00\00\00\00\00\00\00\00\00\00\84.\00\00\a84\00\00\c14\00\00\c74\00\00\00\00\00\00\00\00\00\00\05\00\00\00P\00\00\00Q\00\00\00\c1\00\00\00\c2\00\00\004\01\00\005\01\00\00q\01\00\00r\01\00\00\c0\01\00\00\c1\01\00\00;\02\00\00<\02\00\00\92\02\00\00\93\02\00\00\0b\03\00\00\0c\03\00\00{\03\00\00}\03\00\007\04\00\009\04\00\00\d0\04\00\00\d1\04\00\00K\05\00\00L\05\00\00\97\05\00\00\98\05\00\00\ed\05\00\00\ef\05\00\00\b9\06\00\00\bb\06\00\00\b2\07\00\00\b4\07\00\00\81\08\00\00\83\08\00\00W\09\00\00Y\09\00\00F\0a\00\00H\0a\00\00\15\0b\00\00\17\0b\00\00\00\0d\00\00\02\0d\00\00!\0f\00\00#\0f\00\00B\11\00\00D\11\00\00\14\13\00\00\15\13\00\00t\13\00\00u\13\00\00\cd\13\00\00\cf\13\00\00W\15\00\00Y\15\00\00\a5\16\00\00\a7\16\00\00\b3\18\00\00\b4\18\00\002\19\00\004\19\00\00\b5\19\00\00\b6\19\00\002\1a\00\004\1a\00\000\1b\00\002\1b\00\006\1f\00\008\1f\00\00[ \00\00] \00\00\e2!\00\00\e4!\00\00,$\00\00.$\00\00\92'\00\00\94'\00\00\a7-\00\00\a9-\00\00\f54\00\00\f74\00\00\f26\00\00\f46\00\0018\00\0028\00\00k8\00\00m8\00\00\fa8\00\00\fb8\00\00I9\00\00K9\00\00a:\00\00c:\00\00\15;\00\00\17;\00\00\b6;\00\00\b8;\00\00\a3<\00\00\a4<\00\00\f0<\00\00\f1<\00\00==\00\00>=\00\00z=\00\00|=\00\00\ff=\00\00\00>\00\00\5c>\00\00^>\00\00\12?\00\00\13?\00\00{?\00\00}?\00\00G@\00\00\13D\00\00pD\00\00qD\00\00\ceD\00\00I@\00\00\dfA\00\00\e1A\00\00\12D\00\00\d0D\00\00\9aE\00\00\9bE\00\00\03F\00\00\05F\00\00\fdG\00\00\feG\00\00{H\00\00|H\00\00\f9H\00\00\faH\00\00rI\00\00sI\00\00\ebI\00\00\edI\00\00\d2N\00\00\d3N\00\001O\00\003O\00\00\1eP\00\00 P\00\00\a5P\00\00\a6P\00\00\13Q\00\00\15Q\00\00\b4Q\00\00\b6Q\00\00\00S\00\00\00\00\00\00\00\00\00\00")
  (@custom ".debug_str" "__cy\00ux\00__ctz_x\00__clz_x\00__x\00vv\00mpn_div_qr_preinv\00mpn_div_qr_2_preinv\00mpn_div_qr_1_preinv\00dinv\00__v\00__u\00mpn_div_qr_invert\00mpn_div_qr_2_invert\00mpn_div_qr_1_invert\00_start\00unsigned int\00cnt\00mpz_init\00extract_digit\00eliminate_digit\00bit\00mpn_rshift\00mpn_lshift\00mpz_set\00mpz_init_set\00__mpz_struct\00mpz_t\00mp_bitcnt_t\00mp_size_t\00mp_limb_t\00bits\00qs\00ns\00ds\00gmp_free_limbs\00gmp_realloc_limbs\00gmp_alloc_limbs\00mpz_abs\00mpz_srcptr\00mp_srcptr\00mp_ptr\00mpn_sqr\00mpz_div_qr\00mpn_div_qr\00char\00mpz_clear\00_r\00tq\00mpz_tdiv_q\00xp\00vp\00up\00tp\00rp\00qp\00np\00__mp_size_t_swap__tmp\00__mpz_srcptr_swap__tmp\00__mp_ptr_swap__tmp\00mpz_cmp\00mpn_cmp\00dp\00bp\00mpz_swap\00mpn_zero_p\00mpn_zero\00vn\00un\00tn\00rn\00qn\00nn\00sign\00den\00dn\00bn\00mpn_common_scan\00mpn_mul_n\00mpn_add_n\00mpn_sub_n\00num\00next_term\00mpn_com\00__vl\00mpz_addmul\00mpz_submul\00mpz_mul\00mpn_mul\00__ul\00tl\00rl\00_ql\00lpl\00hpl\00/home/sve70582/Documents/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/o0_debug_export_all\00cl\00retval\00_mask\00mpn_copyi\00mpz_set_ui\00mpz_init_set_ui\00mpz_get_ui\00mpz_addmul_ui\00mpz_submul_ui\00mpz_mul_ui\00mpz_add_ui\00mpz_sub_ui\00mpz_set_si\00di\00__vh\00__uh\00nth\00_qh\00nh\00msg\00unsigned long\00mpz_neg\00mpn_neg\00bsize\00asize\00new_size\00_mp_size\00unused_old_size\00mpn_normalized_size\00unused_size\00gmp_div_inverse\00gmp_die\00gmp_default_free\00mpz_div_round_mode\00mpn_copyd\00old\00mpz_add\00mpz_abs_add\00mpn_add\00_mp_d\00mpz_realloc\00gmp_default_realloc\00gmp_default_alloc\00_mp_alloc\00gmp_reallocate_func\00gmp_allocate_func\00gmp_free_func\00tnc\00acc\00__ctz_c\00__clz_c\00../src/pidigits.c\00mpz_sub\00mpz_abs_sub\00mpn_sub\00mpz_ui_sub\00dummy_limb\00low_limb\00high_limb\00bb\00a\00__ARRAY_SIZE_TYPE__\00LOCAL_SHIFT_BITS\00LOCAL_GMP_LIMB_BITS\00GMP_DIV_FLOOR\00GMP_DIV_CEIL\00GMP_DIV_TRUNC\00clang version 21.1.7\00mpn_cmp4\00__x3\00mpn_invert_3by2\00__x2\00mpz_init2\00tmp2\00k2\00cy1\00__x1\00u1\00_t1\00r1\00tmp1\00mpn_scan1\00mpn_div_qr_pi1\00d1\00mpn_addmul_1\00mpn_submul_1\00mpn_mul_1\00mpn_add_1\00mpn_sub_1\00__x0\00u0\00_t0\00r0\00_q0\00mpn_scan0\00d0\00")
  (@custom ".debug_line" "\1c&\00\00\04\00P\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01../src\00../src/../..\00\00mini-gmp.h\00\01\00\00pidigits.c\00\01\00\00stdlib.h\00\02\00\00\00\00\05\02\05\00\00\00\03\b8\04\01\05\0c\0a\08\bb\05\1c\06\ac\05\03t\06\08=\02\08\00\01\01\00\05\02Q\00\00\00\03\bf\04\01\05\0b\0a\08\bd\05\10\06X\05\03f\05\0f\06h\05\07\06t\05\05f\05\08\06=\05\07\06\9e\05\05\06Y\06\03\b9{\ba\05\0a\06\03\c9\04 \05\03\06t\02\0f\00\01\01\00\05\02\c2\00\00\00\03\cd\04\01\05\10\0a\02+\15\05\15\06t\05\07X\05\05f\05\08\06>\05\07\06\9e\05\05\06Y\06\03\ac{\ba\05\0a\06\03\d6\04 \05\03\06t\02\0f\00\01\01\00\05\025\01\00\00\03\da\04\01\05\09\0a\02$\13\05\03\06X\05\01\06g\02\0d\00\01\01\00\05\02r\01\00\00\03\e8\04\01\05\13\0a\08\bb\05\03\06\02\22\12\02\0f\00\01\01\00\05\02\c1\01\00\00\03\ee\04\01\05\0b\0a\02+\13\05\10\06X\05\03f\05\13\06g\05\03\06\02/\12\02\0f\00\01\01\00\05\02<\02\00\00\03\f5\04\01\05\03\0a\02$\13\05\01\02%\13\02\0d\00\01\01\00\05\02\93\02\00\00\03\fe\04\01\05\0a\0a\02#\14\05\0f\06t\05\13\90\05\11X\05\03 \05\0c\06g\05\0e\06X\05\0cX\05\05\90\05\07X\05\05X\05\0aJ\05\17\06W\05\03\06\c8.\05\01\060\02\02\00\01\01\00\05\02\0c\03\00\00\03\86\05\01\05\0a\0a\02#\13\05\0e\06\08J\05\03X\05\0c\06g\05\0e\06X\05\0cX\05\05\90\05\07X\05\05X\05\0aJ\05\03\06W\05\01L\02\02\00\01\01\00\05\02}\03\00\00\03\8d\05\01\05\0a\0a\02#\13\05\0e\06\08f\05\03X\05\0b\06h\05\0e\06t\05\0bX\05\14t\05\17X\05\14X\05\11t\05\09\06u\05\0c\06X\05\09X\05\11t\05\14X\05\11X\05\0ft\05\09<\05\02\ba\03\eezX\05\03\06\03\8f\05 O\06\03\eczt\05\01\06\03\95\05 \02\07\00\01\01\00\05\029\04\00\00\03\98\05\01\05\07\0a\022\13\05\0d\06\90\05\0aX\05\0c\06u\05\11\06X\05\0fX\05\0c<\05\05\ba\03\e5zX\05\15\06\03\9d\05 \05\19\06t\05\1dX\05\0cX\05\05f\03\e3z<\05\01\06\03\9e\05 \02\16\00\01\01\00\05\02\d1\04\00\00\03\a1\05\01\05\0a\0a\08\ad\05\0c\06t\05\10X\05\13\08X\05\16X\05\17X\05\13<\05\1bt\03\ddzX\05\03\03\a3\05X\05\05\06g\05\03\c7\06\03\ddz.\03\a3\05 \05\0a\06\22\05\03\06X\02\02\00\01\01\00\05\02L\05\00\00\03\a9\05\01\05\1f\0a\02$\13\05#\06X\05\0aX\05&f\05\03\82\02\0f\00\01\01\00\05\02\98\05\00\00\03\af\05\01\05\0a\0a\08\ad\05\0e\06\08J\05\03X\05\05\06g\05\08\06X\05\05X\05\0bJ\05\03\06W\05\01L\02\02\00\01\01\00\05\02\ef\05\00\00\03\b6\05\01\05\0b\0a\022\15\05\0d\06X\05\03f\05\05\06g\05\15w\05\18\06\90\05\15X\05\1dt\05\1bX\05\11 \05\0c\06>\05\10\06t\05\0eX\05\09J\05\0f\06=\05\07\06t\05\0aX\05\07X\05\0dJ\05\0a\06Z\05\10\06\08\12\05\0et\05\05\06\1f\05\0ai\05\03\06t\02\0f\00\01\01\00\05\02\bb\06\00\00\03\c9\05\01\05\0a\0a\02*\16\05\12\06t\05\17t\05\1b\90\05\19X\05\03 \05\0b\06i\05\0e\06t\05\0bX\05\09t\05\16<\05\19t\05\16X\05\14t\05\0b\06=\05\0f\06t\05\0dX\05\09 \05\0d\06=\05\11\06t\05\0fX\05\0aJ\05\0c\06=\05\09\06t\05\0e\06\91\05\12\06t\05\10X\05\0aJ\05\0f\06\91\05\07\06t\05\0aX\05\07X\05\0dJ\05\1f\06\03xX\05\03\06\c8.\05\0a\06\03\0a.\05\03\06X\02\02\00\01\01\00\05\02\b4\07\00\00\03\dc\05\01\05\0b\0a\029\15\05\11\06X\05\0eX\05\03J\05\13\06h\05\17\06t\05\1bX\05\1fX\05\08X\05\06f\05\07\06=\05\0c\06t\05\0aX\05\15\06u\05\1a\06t\05\18X\05\1eJ\05#X\05!X\05'J\05,X\05*X\050 \05\0aX\05\08f\03\9cz<\05\0a\06\03\e5\05 \05\03\06t\02\0f\00\01\01\00\05\02\83\08\00\00\03\e9\05\01\05\0b\0a\022\15\05\0d\06X\05\03f\05\05\06h\05\15w\05\18\06\90\05\15X\05\11t\05\16\06>\05\1a\06t\05\18X\05\11J\05\0f\06=\05\13\06X\05\11X\05\07<\05\0aX\05\07X\05\0dJ\05\0b\06Y\05\09\06t\05\0a\06>\05\10\06\08\12\05\0et\05\05\06\1f\05\0ai\05\03\06t\02\0f\00\01\01\00\05\02Y\09\00\00\03\fe\05\01\05\0a\0a\02*\16\05\12\06t\05\17t\05\1b\90\05\19X\05\03 \05\0b\06i\05\0e\06t\05\0bX\05\09t\05\16<\05\19t\05\16X\05\14t\05\0c\06=\05\09\06t\05\0d\06\91\05\11\06t\05\0fX\05\0aJ\05\0e\06=\05\12\06t\05\10X\05\0aJ\05\0f\06\91\05\13\06X\05\11X\05\07<\05\0aX\05\07X\05\0dJ\05\1f\06\03yX\05\03\06\c8.\05\0a\06\03\09.\05\03\06X\02\02\00\01\01\00\05\02H\0a\00\00\03\90\06\01\05\0b\0a\029\15\05\11\06X\05\0eX\05\03J\05\13\06h\05\17\06t\05\1bX\05\1fX\05\08X\05\06f\05\07\06=\05\0c\06t\05\0aX\05\15\06u\05\1a\06t\05\18X\05\1eJ\05#X\05!X\05'J\05,X\05*X\050 \05\0aX\05\08f\03\e8y<\05\0a\06\03\99\06 \05\03\06t\02\0f\00\01\01\00\05\02\17\0b\00\00\03\9d\06\01\05\0b\0a\023\15\05\0d\06X\05\03f\05\06\06h\05\0fw\05\0c\06\08.\05\0at\05\07\06=\06t\02\b7\01\12\03\d9y\02\22\01\03\a7\06 \05\0e\06\02+\14\05\0b\06t\05\0d\06\91\05\13\06t\05\11X\05\19J\05\17X\05\0a \05\0f\06>\05\0a\06t\05\0d\08\12\05\0a\06v\05\05\ff\05\0a[\05\03\06t\02\10\00\01\01\00\05\02\02\0d\00\00\03\b4\06\01\05\0b\0a\023\15\05\0d\06X\05\03f\05\06\06h\05\0fw\05\0c\06\08.\05\0at\05\07\06=\06t\02\b7\01\12\03\c2y\02\22\01\03\be\06 \05\0e\06\02+\14\05\0b\06t\05\0d\06\91\05\13\06t\05\11X\05\19J\05\17X\05\0a \05\0d\06>\05\0c\06t\05\0a<\05\0d\06=\05\12\06t\05\10X\05\0b \05\0d\06=\05\13\06t\05\11X\05\0aJ\05\0f\06\91\05\0a\06t\05\0d\08\12\05\0a\06v\05\05\ff\05\0a[\05\03\06t\02\10\00\01\01\00\05\02#\0f\00\00\03\ce\06\01\05\0b\0a\023\15\05\0d\06X\05\03f\05\06\06h\05\0fw\05\0c\06\08.\05\0at\05\07\06=\06t\02\b7\01\12\03\a8y\02\22\01\03\d8\06 \05\0e\06\02+\14\05\0b\06t\05\0d\06\91\05\13\06t\05\11X\05\19J\05\17X\05\0a \05\0d\06>\05\0c\06t\05\0a<\05\0d\06=\05\12\06t\05\10X\05\0b \05\0d\06=\05\13\06t\05\11X\05\0aJ\05\0f\06\91\05\0a\06t\05\0d\08\12\05\0a\06v\05\05\ff\05\0a[\05\03\06t\02\10\00\01\01\00\05\02D\11\00\00\03\e8\06\01\05\0b\0a\029\13\05\11\06X\05\0eX\05\03J\05\0b\06g\05\0e\06X\05\03f\05\0c\06g\06\03\94y\02H\01\05\0b\03\ec\06<\05\03f\05\0c\06g\06\03\93y\02H\01\05\0b\03\ed\06<\05\03f\05\17\06l\05\1b\06X\05\1fX\05#X\05\0c\82\05\03\82\05\06X\05\03X\05\0aJ\06]\05\0f\06\08J\05\03X\05\0a\06h\05\13\06\c8\05\1e\06\c9\05\22\06X\05&X\05*X\05\10\82\05\07\82\05\0aX\05\07X\05\0eJ\05\03\06U\05\0aO\05\0d\06X\05\0aX\05\03\90\02\0f\00\01\01\00\05\02\15\13\00\00\03\81\07\01\05\0c\0a\022\13\05\10\06X\05\14X\05\17X\05\1bX\05\03X\05\01\06u\02\0d\00\01\01\00\05\02u\13\00\00\03\87\07\01\05\0c\0a\02+\13\05\10\06X\05\14X\05\17X\05\1bX\05\03X\05\01\06u\02\0d\00\01\01\00\05\02\cf\13\00\00\03\8d\07\01\05\0b\0a\022\17\05\0d\06X\05\03f\05\0b\06g\05\0f\06X\05\03f\05\0b\06g\05\0f\06X\05\03f\05\09\06h\05\06\06t\05\09\06\f3\05\06\06t\05\19\06\f4\05\17\06t\05\07t\05\0f\06=\05\0e\06\08\12\05\0ct\06=\05\18\06t\05\15X\05\0a \05\10\06=\05\1c\06t\05\19X\05\0d \05\0a\06>\05\03\06\08J\05\13\06Z\05\12\06\08\12\05\10t\05\0f\06=\05\1c\06X\05(X\05%X\05\19 \05\08<\05\0d\08\12\05\14\06u\05 \06t\05\1dX\05\11 \05\03\068\05\0bP\05\04\06t\05\09\08\12\05\0a\06v\05\03\06t\02\0f\00\01\01\00\05\02Y\15\00\00\03\ab\07\01\05\0b\0a\022\17\05\0d\06X\05\03f\05\0b\06g\05\0f\06X\05\03f\05\0b\06g\05\0f\06X\05\03f\05\19\06h\05\17\06t\05\07t\05\12\06=\05\0f\06\08\12\05\0dt\06=\05\1a\06t\05\17X\05\0a \05\0e\06=\05\1b\06t\05\18X\05\0c \05\0a\06>\05\03\06\08J\05\16\06Z\05\13\06\08\12\05\11t\05\0f\06=\05\1b\06X\05(X\05%X\05\18 \05\0a<\05\0d\08\12\05\12\06u\05\1f\06t\05\1cX\05\10 \05\03\068\05\09P\05\04\06t\05\07X\05\0a\06Z\05\03\06t\02\0f\00\01\01\00\05\02\a7\16\00\00\03\c7\07\01\05\0b\0a\029\15\05\13\06t\05\16\ac\05\19X\03\b5xX\05\13\03\cb\07<\05\03<\05\10\06g\05\0d\06t\05\12t\05\15\08X\05\1aX\05\17X\03\b4x<\05\12\03\cc\07<\05\03<\05\0a\06h\05\03\06\ac\05\08\060\05\0b\c9\05\10\06t\05\0dX\05\0a\06u\06\03\aex\08\12\05'\03\d2\07 \05*X\03\aexX\05\0a\03\d2\07 \05\02J\03\aexX\05\0e\06\03\d3\07 \05\13\06t\05\16X\05\13X\05\11t\05\0c \05\03\067Q\06\08\12\02)\12\06\08<\06\e4.\08<\e4\03\abx.\06\03\d5\07 \06\f2.\08\ac.\9e\05\18\06\08\13\05\1a\06t\05,<\05*X\05\03 \03\aax<\05\01\06\03\d7\07 \02\16\00\01\01\00\05\02\b4\18\00\00\03\da\07\01\05\07\0a\02$\14\05\0b\06t\05\05<\05\1c\06>\05 \06X\05\1cX\057\90\05;X\053X\05#t\05\06\06!\05\09\06X\05\0eX\05\0a\06W\05\03\06\9e\02\0f\00\01\01\00\05\024\19\00\00\03\e4\07\01\05\07\0a\02$\14\05\0b\06t\05\05<\05\1c\06>\05 \06X\05\1cX\05\1bt\057X\05;X\053X\05#t\05\06\06!\05\09\06X\05\0eX\05\0a\06W\05\03\06\9e\02\0f\00\01\01\00\05\02\b6\19\00\00\03\ee\07\01\05\0a\0a\02#\13\05\0e\06\08J\05\03X\05\12\06g\05\0f\06\08\12\05\0dX\05\08X\05\0b\08\12\05\03\06s\05\01L\02\02\00\01\01\00\05\024\1a\00\00\03\f5\07\01\05\0b\0a\02+\13\05\0a\06\ac\05\03<\05\08\060\05\0b\06X\05\0c\06Y\05\0b\06\08\12\05\02\06g\06\03\85x\90\05\07\06\03\fc\07 \05\0d\06\c8\05\03\06\c3\05\0cQ\05\0b\06X\05\09X\05\04t\05\07X\05\0c\06Y\05\12\06\08\12\05\18\08\12\05\03\08\12\06\bb\06\03\80xt\05\01\06\03\81\08 \02\16\00\01\01\00\05\022\1b\00\00\03\8b\08\01\05\05\0a\02%\19\05\0a\85\05\0d\06t\05\08X\05\0a\06=\05\0d\06t\05\08<\05\0b\06A\05\0e\06t\05 <\05\1eX\05\08 \05\0c\06\03\11<\05\0b\06t\05\1d<\05\22X\05 X\05\0f \05& \05><\05\07X\05\15\06>\05\1a\06t\05\18X\05\07 \05\09\06>\05\0d\06t\05\0bX\05\04\06v\05\07\c9\05\04\06t\05\06\06\91\05\0b\06t\05\08X\06u\05\0c\06t\05\0aX\06v\05\0d\c9\05\0a\06t\03\c7w\90\06\03\bc\08<\05\07\06t\05\0a\06\03\09\c8\05\0c\06t\05&<\05$X\05+ \05)X\05\07 \05\0b\06?\05\0d\06t\05%<\05\08<\05\0a\06?\05\0c\06t\05$<\057X\05<X\05:X\055 \05\07 \05\09\06>\05\1f\06t\05!X\05\1c<\05\0b<\05\04\06v\05\07\c9\05\04\06t\03\b0w\90\05\16\06\03\d2\08 \05\19\06t\053<\051X\05\07 \05\09\06=\05\0e\06t\05\0bX\05\03\06v\05\07\c9\05\04\06t\03\aaw\c8\05\07\06\03\dc\08 \05\0a\06t\05\0c\06\93\05\0b\06t\05\09<\05\0c\06=\05\09\06t\05\0b\06\91\05\0f\06t\05\0dX\05\05\06v\05\08\c9\05\0d\06t\05\0aX\05\09\06v\05\0d\c9\05\0a\06t\03\99w\c8\05\09\06\03\e9\08 \05\06\06t\03\97w\c8\05\07\06\03\eb\08 \06t\02\b7\01\12\03\95w\02\22\01\03\eb\08 \05\0c\06\02+\13\05\09\06t\05\0b\06\91\05\0f\06t\05\0dX\05\05\06v\05\0b\c9\05\0f\06X\05\0dX\05\17J\05\1cX\05\19X\05#J\05(X\05&X\05 J\05\13 \05\06<\03\90w\c8\05\0a\06\03\f4\08.\05\03\06t\02\10\00\01\01\00\05\028\1f\00\00\03\82\09\01\05\0b\0a\02$\15\05\0d\06X\05\03f\06g\06\08t\06\08<\06\e4.\08<\e4\03\f9v.\06\03\87\09 \06\f2.\08\ac.\05\10\06\9f\05\03\06t\05\0eX\05\0d\06Y\05\12\06X\05\0fX\05\03<\05\0bX\05\0d\06Y\05\03\06\08 \05\0bX\05\01\06Y\02\0d\00\01\01\00\05\02] \00\00\03\8f\09\01\05\0b\0a\02+\15\05\0e\06X\05\03f\06g\06\08t\06\08<\06\e4.\08<\e4\03\ecv.\06\03\94\09 \06\f2.\08\ac.\05\10\06\9f\05\03\06t\05\0eX\05\07\06Y\05\0d\06t\06\92\05\13\06X\05\10X\05\1d<\054t\052t\05 \ac\05\1a \05\0a \05\0e\06=\05\0a\06t\03\e7v\c8\05\0d\06\03\9b\09 \05\03\06t\05\0bX\05\0d\06Y\05\03\06t\05\0bX\05\1e\06Y\05\22\06X\05\0dX\05\03\82\05\0bX\05\01\06Y\02\0d\00\01\01\00\05\02\e4!\00\00\03\a2\09\01\05\0b\0a\02+\13\05\0e\06X\05\03f\05\07\06h\05\0a\06\90\05\1a\06\91\05\1f\06X\05\05\82\03\d9v\82\05\0c\06\03\a8\09 \05\0f\06\90\05\1a\06\91\05\1f\06X\05&\82\05\05\82\03\d7v\82\05\0c\06\03\af\09 \05\0f\06t\05\11X\05\0c<\05\0at\05\0c\06=\05\0f\06t\05\11X\05\0c<\05\0at\05\0f\06=\05\12\06X\05\07f\06g\06\08t\06\08<\06\e4.\08<\e4\03\cev.\06\03\b2\09 \06\f2.\08\ac.\05\14\06\9f\05\07\06t\05\12X\05\0b\06Y\05\11\06t\05\0a\06\92\05\10\06X\05\0dX\05\1a<\051t\05/t\05\1d\ac\05\17 \05\07 \05\0a\06=\05\10\06X\05\0dX\05\1a<\05\1dX\05\1fX\05\1a<\057\90\055t\05#\ac\05\17 \05\07 \03\c9v<\05\11\06\03\b9\09 \05\07\06t\05\0fX\05\11\06Y\05\07\06t\05\0fX\05\22\06Y\05&\06X\05\11X\05\07\82\05\0fX\03\c5vX\05\01\06\03\bd\09.\02\0d\00\01\01\00\05\02.$\00\00\03\c3\09\01\05\0a\0a\023\15\05\0du\05\07v\05\0c\06\90\05\12<\05\0c\06\93\05\0a\06t\05\0c\06=\05\0b\06\9e\05\0a\06Z\05\08\06t\05\1b\06=\05\0a\06t\05\08f\03\afv<\05\17\06\03\d3\09 \05\1b\06t\05\1fX\05#X\05(X\05\0b<\05\09f\05\0c\06=\05\0a\06t\05\05\06=\06\03\abv.\05\07\06\03\d7\09 \06\03\a9vt\06\03\d9\09 \05\0c\06t\05\05<\05\08\06=\05\0d\06t\05\06<\05\0a\06=\05\0f\06\08J\05\03X\05\07\06j\06t\02\b7\01\12\03\a1v\02\22\01\03\df\09 \06\02+\12\06\02H\12\02b\12\08.\03\a1v\08\f2\03\df\09 \05\0b\06\08=\f3\05\02\06t\05\05X\05\02X\05\09J\03\9fvX\05\03\06\03\db\09 \05\07R\05\15\9f\05\19\06X\05\05X\03\9cvf\05\0a\06\03\e6\09 \05\0f\06X\05\14X\05\0c<\05\03<\02\10\00\01\01\00\05\02\94'\00\00\03\eb\09\01\05\0b\0a\027\17\05\0e\06f\05\03f\05\0b\06g\05\10\06\82\05\09<\05\08\06K\05\0d\06\82\05\06<\05\08\06K\05\0d\06\82\05\06<\05\08\06K\05\0d\06\82\05\06<\05\07\06L\05\0d\06\9e\05\16\06\91\05\1a\06\82\05\1ef\05\22f\05\0af\05\08f\05\05J\03\88v.\05\08\06\03\fa\09 \06\03\86v\82\06\03\fc\09 \05\0b\06\82\05\0ef\05\08<\05\06t\05\07\06L\05\0a\06\82\05\05<\05\0c\06N\05\0f\06\9e\05\0cf\05\0at\05\07\06K\06t\02\b9\01\12\03\fdu\02\22\01\03\83\0a \06\02+\12\06\02;\12\08\82\06\02A\12\06t\02\b8\01\12\03\fdu\02\22\01\03\83\0a \06\02+\12\06\02>\12\028\12\02L\12\08J\03\fdu\02)\01\03\83\0a \c8\03\fdu\02A\01\05\0b\06\03\85\0a.\05\0a\08\13\05\02\06t\05\05f\05\02f\05\08J\03\fauX\05\0a\06\03\88\0a \05\0e\06\08.\05\05\06W\05\07i\05\0d\06\82\05\10\06\92\057\06\82\055\82\05#t\05\13t\05@ \05\07f\05\0d\06g\05\13\06f\05\10f\05\1d<\054\82\052\82\05 \ac\05\1a \05\0a \05\0e\06K\05\0a\06\82\03\f2u\e4\05\0b\06\03\91\0a \05\03\06\82\05\09f\05\0b\06Y\05\03\06\82\05\09f\05\01\06Y\02\0e\00\01\01\00\05\02\a9-\00\00\03\99\0a\01\05\0b\0a\02O\19\05\0e\06f\05\03f\05\0b\06g\05\11\06f\05\0ef\05\03J\05\08\06h\05\0b\06\82\05\0ef\05\08<\05\06t\05\08\06K\05\0b\06\82\05\0ef\05\08<\05\06t\05\0c\06L\05\0f\06f\05#t\05\03f\05\07\06m\05\0c\06\82\05\0af\05\05 \05\16\06M\05\19\06\9e\05\1bf\05\1e<\05\1df\05\16 \05\11t\05\0b\06L\05\11\06\9e\05\0ef\05\14 \05\17f\05\1df\05\1af\05\14 \05\06\06h\05\12\83\05\15\06f\05\14f\05\18J\05\1cf\05 f\05\04f\05\09\06u\05\0c\06\82\05\0ef\05\11<\05\10f\05\09 \05\07t\05\02\06K\06\03\c8u.\05\04\06\03\bb\0a \06t\02\b9\01\12\03\c5u\02\22\01\03\bb\0a \06\02-\12\06\02@\12\08\90\06\02o\12\06t\02\b9\01\12\03\c5u\02\22\01\03\bb\0a \06\02,\12\06\02?\12\02=\12\02L\12\08J\03\c5u\02)\01\03\bb\0a \e4\03\c5u\02A\01\05\17\06\03\bd\0a.\05\1c\06\82\05\1af\05\1fJ\05#f\05%f\05)<\05\09f\05\07f\05\0a\06L\05\0f\06\82\05\0df\05\08J\05\09\06K\05\0e\06\82\05\0cf\05\07 \05\09\06K\05\0e\06\82\05\0cf\05\07J\05\09\06K\05\0e\06\82\05\0cf\05\07 \05\11\06K\05\04\06\82\05\07f\05\09f\05\0c<\05\0bf\05\04 \05\0fJ\05\08\06Z\05\0b\06\82\05\0e\06>\05\1e\06\82\05#f\05!f\05&J\05+f\05)f\05.J\052f\055f\05\13<\05\11f\05\0b \05\09\06\ad\06\03\b8u\e4\05\0b\06\03\cc\0a.\05\0a\08\13\05\02\06\82\05\05f\05\02f\05\08J\03\b3uX\05\0a\06\03\cf\0a \05\0e\06\08.\05\05\06W\05\10i\05\03\06\82\05\06f\05\09f\05\03<\05\0eJ\05\01\06Y\02\0e\00\01\01\00\05\02\f74\00\00\03\d7\0a\01\05\0b\0a\02@\13\05\0e\06X\05\03f\05\0b\06g\05\11\06X\05\0eX\05\03J\05\07\06h\05\0a\06\90\05\22\06\91\05&\06X\05*X\05.X\05\0dX\05\05\82\05\0bX\05\05X\03\a3u.\05\0c\06\03\de\0a \05\0f\06\90\05\1a\06\91\05\1e\06X\05\22X\05&X\05\05X\03\a1u\82\05\0f\06\03\e5\0a \05\14\06X\05\1a<\05\1dX\05\1fX\05\1a<\05\17t\05\07J\05\0f\06g\05\14\06X\05\1a<\05\1dX\05\1fX\05\1a<\05\17t\05\07J\05\10\06g\05\15\06X\05\18<\05,t\05\07f\05\0f\06h\05\14\06t\05\0d<\05\0b\06=\05\11\06\90\05\13\06\91\05\17\06t\05\1bX\05\1fX\05\07X\05\05f\05\02<\03\95u.\05\05\06\03\ed\0a \06\03\93ut\05\17\06\03\ef\0a \05\1b\06X\05\1fX\05#X\05'X\05+X\05/X\054X\05\07<\05\0b\06h\05\11\06t\05\02\06\91\06\03\8eu\020\01\05\01\06\03\f4\0a<\02\0d\00\01\01\00\05\02\f46\00\00\03\f7\0a\01\05\0a\0a\029\14\05\0bv\05\0e\06X\05\03f\05\0b\06g\05\11\06X\05\0eX\05\03J\05\1c\06h\05 \06t\05\03t\05\07\06\e5\05\0a\06t\05\0e<\05\15f\05\1bX\05\0e<\05\1d\06h\05\0c\06t\05\0af\05\07\06=\05\0c\020\13\05\0a\06t\03\fct<\05\16\06\03\86\0b \05\1a\06X\05\1eX\05\22X\05&X\05\03X\05\07\06\ad\05\15\f3\05\19\06X\05\05X\03\f8tf\05\01\06\03\89\0b \02\0d\00\01\01\00\05\0228\00\00\03\90\0b\01\05\03\0a\08M\05\10\06X\05\03\06Y\05\0f\06X\05\03\06Y\05\0c\06X\05\01\06\91\02\02\00\01\01\00\05\02m8\00\00\03\9c\0b\01\05\0c\0a\02$\15\05\11\06X\05\08\82\05\0c\06\c9\05\11\06t\05\0a<\05\06<\05\12\06>\05\03\06t\05\10X\05\03\06Y\05\0f\06X\05\1f\06Y\05\0e\06X\05\03\82\05\0cX\05\01\06Y\02\0d\00\01\01\00\05\02\fb8\00\00\03\a9\0b\01\05\07\0a\08\bb\05\0a\06t\05\07<\05\15\06=\05\18\06X\05\1f<\05\22X\05\05<\03\d4tf\05\01\06\03\ad\0b \02\0d\00\01\01\00\05\02K9\00\00\03\b0\0b\01\05\0a\0a\02$\13\06\03\cet\08\9e\03\b2\0b \03\cetJ\03\b2\0b \05\08J\05\07\06>\05\0a\06\90\05\07<\05#\06=\05&\06X\05-<\050X\05;<\05\10X\05\05\82\05\0eX\05\05X\03\cbt.\05!\06\03\b7\0b \05\10\06X\05\05\82\05\0eX\03\c9tX\05\12\06\03\b8\0b \05\03\06t\05\10X\05\07\06Z\06\03\c6t\08\f2\03\ba\0b \03\c6t\08\12\03\ba\0b \05\1fJ\05\1dX\05\05\06u\05\11\06X\03\c5tX\05\0a\06\03\bd\0b \05\0d\06X\05\03X\02\0f\00\01\01\00\05\02c:\00\00\03\c6\0b\01\05\07\0a\02$\13\05\09\06\90\05\11\06\91\05\14\06X\05\05X\03\b7t\82\05\07\06\03\d2\0b \05\13\06X\05\1f\06Y\05\07\06\08<\03\adt\02*\01\03\d3\0b \03\adt\9e\03\d3\0b \05\1d.\03\adtX\05\01\06\03\d5\0b \02\0d\00\01\01\00\05\02\17;\00\00\03\d8\0b\01\05\07\0a\02$\13\05\09\06\90\05\07\06\92\05\13\06X\05\1f\06Y\05\07\06t\03\a3t\02*\01\03\dd\0b \03\a3t\9e\03\dd\0b \05\1d.\05\05\06\03\0aX\06\03\99t.\06\03\e9\0b \05\11\06X\03\97tX\05\01\06\03\ea\0b \02\0d\00\01\01\00\05\02\b8;\00\00\03\ed\0b\01\05\07\0a\02$\14\05\0c\06t\05\09X\05\0b\06y\06\03\8bt\08\f2\03\f5\0b \03\8bt\08\12\03\f5\0b \05\09J\05\0c\06=\06\03\8at\02,\01\03\f6\0b \03\8at\9e\03\f6\0b \05\0aJ\05\12\06>\05\16\06X\05\19X\05 <\05\07X\05\15\06g\05\18\06X\05\07X\05\13X\03\87tX\05\01\06\03\fb\0b \02\0d\00\01\01\00\05\02\a4<\00\00\03\fe\0b\01\05\0d\0a\02$\13\05\03\06X\05\0f\06g\05\12\06X\05\03X\05\01\06g\02\0d\00\01\01\00\05\02\f1<\00\00\03\85\0c\01\05\0d\0a\02$\13\05\03\06X\05\0c\06g\05\0f\06X\05\03X\05\01\06g\02\0d\00\01\01\00\05\02>=\00\00\03\8c\0c\01\05\0a\0a\03\0c\08J\05\0d\06\90\05\0a<\03\e7s\82\05!\03\99\0c \05$X\05!<\03\e7sX\05\0a\03\99\0c \05\03.\02\02\00\01\01\00\05\02|=\00\00\03\9d\0c\01\05\0c\0a\02$\13\05\0f\06X\05\03X\05\11\06g\06\03\e0s\08\f2\03\a0\0c \03\e0s\08\12\03\a0\0c \05\03J\05\0fX\05\01\06Y\02\0d\00\01\01\00\05\02\00>\00\00\03\a4\0c\01\05\0c\0a\02$\13\05\0f\06X\05\03X\05\12\06g\05\15\06X\05\11X\05\03t\05\0fX\05\01\06Y\02\0d\00\01\01\00\05\02^>\00\00\03\ab\0c\01\05\03\0a\08\ad\022\13\022\12\05\01\022\13\02\02\00\01\01\00\05\02\13?\00\00\03\b6\0c\01\05\14\0a\02+\14\05\18\06X\05\03X\05\0c\06g\05\0f\06X\05\12X\05\03X\05\0e\06g\05\03\06X\05\01\06g\02\0d\00\01\01\00\05\02}?\00\00\03\fc\0c\01\05\09\0a\02+\15\05\0c\06\90\05\17<\05\1aX\05\15<\05$ \05\17\06\91\05\1a\06t\05\1dX\05\0aX\05\08f\05\05<\03\ffr.\05\17\06\03\83\0d \05\1a\06t\05\1dX\05\0aX\05\08f\03\fdr<\05\11\06\03\85\0d \05\14\06\90\05\1d<\05\11<\05$f\05\11t\03\fbr.\05+\03\85\0d \05)t\03\fbrt\05\11\03\85\0d \05\03J\05\0fX\05\01\06Y\02\0d\00\01\01\00\05\02\13D\00\00\03\bf\0c\01\05\0f\0a\02+\13\05\12\06X\05\15X\05\03X\05\0c\06g\05\0f\06X\05\03X\05\01\06g\02\0d\00\01\01\00\05\02qD\00\00\03\c6\0c\01\05\0c\0a\02+\13\05\0f\06X\05\03X\05\0f\06g\05\12\06X\05\15X\05\03X\05\01\06g\02\0d\00\01\01\00\05\02I@\00\00\03\cd\0c\01\05\12\0a\02+\13\06\03\b1s\08\f2\03\cf\0c \03\b1s\08\12\03\cf\0c \05\0dJ\05\12\06=\06\03\b0s\08\f2\03\d0\0c \03\b0s\08\12\03\d0\0c \05\0dJ\05\07\06@\05\0c\06t\05\0aX\05\07\06v\08\c9\06\03\a9s\08\c8\05\08\06\03\da\0c \06\03\a6s\022\01\03\da\0c \03\a6s\9e\03\da\0c \05\06J\05\11\06=\05\15\06t\05\18X\05\1f<\05#X\05&X\05-<\05\08X\05\06f\05\0c\06>\05\03\06t\05\06X\05\03X\05\0aJ\06Z\05\0f\06X\05\0dX\05\03<\02\0f\00\01\01\00\05\02\e1A\00\00\03\e3\0c\01\05\12\0a\02+\13\06\03\9bs\08\f2\03\e5\0c \03\9bs\08\12\03\e5\0c \05\0dJ\05\12\06=\06\03\9as\08\f2\03\e6\0c \03\9as\08\12\03\e6\0c \05\0dJ\05\13\06@\05\16\06t\05\1d<\05!X\05$X\05+<\05\09X\05\07f\06=\05\0b\06\90\05\0c\06\92\06\03\93s\02,\01\03\ed\0c \03\93s\9e\03\ed\0c \05\0aJ\05\07\06=\05#\02;\13\05'\06t\05\0eX\05\07f\03\91sX\05\0c\06\03\f1\0c \05\10\06t\05\0c\06\92\06\03\8ds\02,\01\03\f3\0c \03\8ds\9e\03\f3\0c \05\0aJ\05\07\06=\05$\02;\13\05(\06X\05\0fX\05\0e\82\05\07t\03\8bsX\05\05\06\03\f8\0c \06\03\88st\05\01\06\03\f9\0c \02\16\00\01\01\00\05\02\d0D\00\00\03\89\0d\01\05\09\0a\02+\15\05\0c\06\90\05\17<\05\1aX\05\15<\05$ \05\17\06\91\05\1a\06t\05\1dX\05\0aX\05\08f\05\05<\03\f2r.\05\17\06\03\90\0d \05\1a\06t\05\1dX\05\0aX\05\08f\03\f0r<\05\11\06\03\92\0d \05\14\06\90\05\1d<\05\11<\05$f\05\11t\03\eer.\05+\03\92\0d \05)t\03\eert\05\11\03\92\0d \05\03J\05\0fX\05\01\06Y\02\0d\00\01\01\00\05\02\9bE\00\00\03\96\0d\01\05\14\0a\02+\14\05\18\06X\05\03X\05\0c\06g\05\0f\06X\05\12X\05\03X\05\0e\06g\05\03\06X\06g\02\0d\00\01\01\00\05\02\05F\00\00\03\a0\0d\01\05\08\0a\02+\18\05\0b\06t\05\06<\05\08\06=\05\0b\06t\05\06<\05\07\06>\05\0f\06\ac\05\12<\05\0fX\03\d6r.\05\07\06\03\ac\0d \05\13\06X\05\07\06Y\06\03\d3r.\05\0b\06\03\b0\0d \05\10\06t\05\0eX\05\14 \05\08f\06>\06\03\cer\08\9e\03\b2\0d \03\cer\d6\03\b2\0d \05\06J\05\08\06=\06\03\cdr\08\9e\03\b3\0d \03\cdr\d6\03\b3\0d \05\06J\05\0e\06>\05\12\06X\05\17X\05\15X\05\1b \05\03<\05\0b\06h\05\06\06t\05\07\06=\05\0d\06\90\05\0aX\05\0e\06u\05\12\06X\05\15X\05\1c<\05 X\05#X\05*<\05\05X\03\c7r\90\05\0e\06\03\bb\0d \05\12\06X\05\15X\05\1c<\05 X\05#X\05*<\05\05X\03\c5rt\05\08\06\03\bd\0d \05\0d\06t\05\0bX\05\06 \05\09\06=\05\0c\06X\05\0eX\05\09<\05\12t\05\06\82\05\11\06\ca\05\1a\06\ba\05\18t\05\11t\03\c0r.\05\1f\03\c0\0d \03\c0rt\05\11\03\c0\0d \05\0fJ\05\0d\06=\05\10\06X\05\03X\05\0e\06g\05\03\06X\03\berf\05\01\06\03\c3\0d \02\0d\00\01\01\00\05\02\feG\00\00\03\c6\0d\01\05\14\0a\02+\14\05\17\06X\05\03X\05\0c\06g\05\0f\06X\05\12X\05\03X\05\0c\06g\05\0f\06X\05\12X\05\03X\05\0e\06g\05\03\06X\05\01\06g\02\0d\00\01\01\00\05\02|H\00\00\03\d0\0d\01\05\14\0a\02+\14\05\17\06X\05\03X\05\0c\06g\05\0f\06X\05\12X\05\03X\05\0c\06g\05\0f\06X\05\12X\05\03X\05\0e\06g\05\03\06X\05\01\06g\02\0d\00\01\01\00\05\02\faH\00\00\03\da\0d\01\05\0d\0a\02+\14\05\03\06X\05\0c\06g\05\0f\06X\05\12X\05\03X\05\0c\06g\05\0f\06X\05\12X\05\03X\05\0e\06g\05\03\06X\05\01\06g\02\0d\00\01\01\00\05\02sI\00\00\03\e4\0d\01\05\0d\0a\02+\14\05\03\06X\05\0c\06g\05\0f\06X\05\12X\05\03X\05\0c\06g\05\0f\06X\05\12X\05\03X\05\0e\06g\05\03\06X\05\01\06g\02\0d\00\01\01\00\05\02\edI\00\00\03\f4\0d\01\05\08\0a\02:\14\05\0b\06t\05\06<\05\08\06=\05\0b\06t\05\06<\05\07\06>\05\0a\06t\05\05\06/\06\03\85r\ba\05\07\06\03\fd\0d \05\0a\06\90\05\0b\060\05\02\f3\05\0e\06X\03\80rX\05\0b\06\03\81\0e \05\02\f3\05\0e\06X\03\feqX\05\07\06\03\83\0e \06\03\fdq\90\05\08\06\03\86\0e \06\03\faq\08\9e\03\86\0e \03\faq\d6\03\86\0e \05\06J\05\08\06=\06\03\f9q\08\9e\03\87\0e \03\f9q\d6\03\87\0e \05\06J\05\08\06>\05\0d\06t\05\0bX\05\06 \05\07\06>\05\0c\06t\05\0aX\05\0b\06v\05\10\06\90\05 <\05#f\05&X\05 <\05\08\06i\05\0f\f3\05\12\06X\05\15X\05\06X\03\efqf\05\08\06\03\92\0e \05\12\f3\05\06\06X\03\edq\82\05\02\06\03\94\0e \06\03\ecq.\05\10\06\03\95\0e \05&\06\90\05).\05,X\05&<\05\08\06i\05\0f\f3\05\12\06X\05\15X\05\06X\03\e7qf\05\08\06\03\9a\0e \05\12\f3\05\06\06X\03\e5q\82\05\02\06\03\9c\0e \06\03\e4q.\05\08\06\03\a0\0e \05\0f\f3\05\12\06X\05\06X\03\dfqf\05\08\06\03\a2\0e \05\06\f3\05\12\06X\03\ddqX\05\07\06\03\a5\0e<\06\03\dbq\90\05\15\06\03\ad\0e \05\19\06X\05\07X\05\10\06g\05\0a\06t\05\0c\06>\05\11\06t\05\0fX\05\14 \05\0a<\05\0b\06>\05\0f\08\22\05\13\06X\05\16X\05\04<\05\0d\06g\05\07\06t\05\02\06=\06\03\caq.\05\05\06\03\b8\0e \06\03\c8qt\05\13\06\03\ba\0e \05\17\06X\05\1bX\05\1fX\05\22X\05)<\05\07X\05\0b\06h\f4\05\0e\06X\05\10X\05\0b<\05\14t\05\07\82\05\13\06\ca\05\16\06\90\05\13<\05\1df\05\1ct\05\13t\03\c0q.\05\22\03\c0\0e \03\c0qt\05\13\03\c0\0e \05\11J\03\c0q<\05!\06\03\c2\0e \05%\06t\05\0cX\05\0af\05\16\06=\05\19\06\90\05\16<\05!f\05\1ft\05\16t\03\bdq.\05&\03\c3\0e \03\bdqt\05\16\03\c3\0e \05\14J\05\0b\06>\05!\06\90\05$.\05'X\05+<\05.f\05+X\05\08\06>\05\12\f3\05\16\06X\05\06X\03\b8q\82\05\08\06\03\c9\0e \05\0f\f3\05\13\06X\05\17X\05\06X\03\b6qf\05\02\06\03\cb\0e \06\03\b5q.\05\10\06\03\cc\0e \05\15\06t\05%<\05(f\05+X\050<\053f\050X\05\08\06>\05\12\f3\05\16\06X\05\06X\03\b1q\82\05\08\06\03\d0\0e \05\0f\f3\05\13\06X\05\17X\05\06X\03\afqf\05\0b\06\03\d4\0e<\05\0e\f4\05\12\06X\05\04X\05\0f\06g\05\04\06X\03\a9qf\05\0b\06\03\d9\0e \05\0c\f3\05\10\06X\05\02X\03\a6qf\05\12\06\03\dc\0e \05\07\06X\05\0e\06h\05\11\06t\05\07f\03\a2q<\05\01\06\03\e0\0e \02\17\00\01\01\00\05\02\d3N\00\00\03\e3\0e\01\05\0f\0a\02+\13\05\18\06t\05\1bt\05\03t\05\01\06\08\13\02\0d\00\01\01\00\05\023O\00\00\03\e9\0e\01\05\15\0a\02$\13\05\18\06t\05\0d<\05\15\06=\05\18\06t\05\0d<\05\07\06>\05\10\06\90\05\0dX\06u\05\15\06X\05\13X\05\0c<\05\05\ba\03\91qX\05\0c\06\03\f0\0e \05\12\06t\05\15\06\91\05\18\06t\05\1f<\05\22X\05)<\05\0cX\05\05f\03\8fqX\05\15\06\03\f3\0e \05\18\06X\05\1fX\05\22X\05*X\05)t\05\0c\ac\05\05f\03\8dq<\05\01\06\03\f4\0e \02\16\00\01\01\04\02\00\05\02 P\00\00\03\0c\01\05\1a\0a\08\bc\05\04\06t\06\08=\08u\05\0b\08v\05\04\06\d6\02\0f\00\01\01\04\02\00\05\02\a6P\00\00\03\15\01\05\1c\0a\08\bb\05\04\06t\06\08=\08=\05\01\08=\02\0d\00\01\01\04\02\00\05\02\15Q\00\00\03\1b\01\05\0c\0a\08\bb\05\0e\06t\05\13<\05\07<\05\04\06>\05\19\08=\05\04\06t\05\19\06\08=\05\04\06t\05\19\06\08=\05\04\06t\05\01\06\08=\02\0d\00\01\01\04\02\00\05\02\b6Q\00\00\03$\01\05\0c\0a\08Z\05\08\06\82\05\04\06?\bb\bc\d7\d7\05\0f\d8\05\0b\06t\05\14\06t\05\18\06\90\05\16X\05\04 \05\11\06g\05\07\06\08\12\05\0b\06\83\05\1d\06\08<\05\0a\06\91\06\03L.\05\0b\06\036 \05\09\06\9e\05\0b\06=\05\10\06t\05\0d\82\05\0a\06u\06\03H.\05\15\06\03: \05\13\06X\05\07<\05\0b\06u\05\0f\06\08\12\05\14t\05\1c\06/\05\0a\06t\03D\08 \05\17\06\03= \05\07\06X\05\04\06\03tf\06.\06\03\0f.\02\13\00\01\01")
  (@custom "name" "\00\0e\0dpidigits.wasm\01\b4\08U\00\07fprintf\01\05abort\02\06assert\03\06malloc\04\07realloc\05\04free\06\15__VERIFIER_nondet_int\07\07putchar\08\06printf\09\11__wasm_call_ctors\0a\07gmp_die\0b\11gmp_default_alloc\0c\13gmp_default_realloc\0d\10gmp_default_free\0e\0fgmp_alloc_limbs\0f\11gmp_realloc_limbs\10\0egmp_free_limbs\11\09mpn_copyi\12\09mpn_copyd\13\07mpn_cmp\14\08mpn_cmp4\15\13mpn_normalized_size\16\0ampn_zero_p\17\08mpn_zero\18\09mpn_add_1\19\09mpn_add_n\1a\07mpn_add\1b\09mpn_sub_1\1c\09mpn_sub_n\1d\07mpn_sub\1e\09mpn_mul_1\1f\0cmpn_addmul_1 \0cmpn_submul_1!\07mpn_mul\22\09mpn_mul_n#\07mpn_sqr$\0ampn_lshift%\0ampn_rshift&\0fmpn_common_scan'\09mpn_scan1(\09mpn_scan0)\07mpn_com*\07mpn_neg+\0fmpn_invert_3by2,\13mpn_div_qr_1_invert-\13mpn_div_qr_2_invert.\11mpn_div_qr_invert/\13mpn_div_qr_1_preinv0\13mpn_div_qr_2_preinv1\0empn_div_qr_pi12\11mpn_div_qr_preinv3\0ampn_div_qr4\08mpz_init5\09mpz_init26\09mpz_clear7\0bmpz_realloc8\0ampz_set_si9\0ampz_set_ui:\07mpz_set;\0fmpz_init_set_ui<\0cmpz_init_set=\0ampz_get_ui>\07mpz_abs?\07mpz_neg@\08mpz_swapA\0ampz_add_uiB\07mpz_addC\0bmpz_abs_addD\0bmpz_abs_subE\0ampz_sub_uiF\0ampz_ui_subG\07mpz_subH\0ampz_mul_uiI\07mpz_mulJ\0dmpz_addmul_uiK\0dmpz_submul_uiL\0ampz_addmulM\0ampz_submulN\0ampz_div_qrO\0ampz_tdiv_qP\07mpz_cmpQ\0dextract_digitR\0feliminate_digitS\09next_termT\06_start\07\12\01\00\0f__stack_pointer\09\11\02\00\07.rodata\01\05.data")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
