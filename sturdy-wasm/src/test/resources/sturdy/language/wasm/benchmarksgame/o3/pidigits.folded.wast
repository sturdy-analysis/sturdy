(module $pidigits.wasm
  (type $t0 (func (param i32 i32 i32) (result i32)))
  (type $t1 (func))
  (type $t2 (func (param i32)))
  (type $t3 (func (param i32) (result i32)))
  (type $t4 (func (param i32 i32) (result i32)))
  (type $t5 (func (result i32)))
  (type $t6 (func (param i32 i32 i32 i32 i32) (result i32)))
  (type $t7 (func (param i32 i32)))
  (type $t8 (func (param i32 i32 i32)))
  (type $t9 (func (param i32 i32 i32 i32) (result i32)))
  (type $t10 (func (param i32 i32 i32 i32)))
  (type $t11 (func (param i32 i32 i32 i32 i32 i32 i32)))
  (type $t12 (func (param i32 i32 i32 i32 i32 i32)))
  (type $t13 (func (param i32 i32 i32 i32 i32)))
  (import "env" "fprintf" (func $fprintf (type $t0)))
  (import "env" "abort" (func $abort (type $t1)))
  (import "env" "assert" (func $assert (type $t2)))
  (import "env" "malloc" (func $malloc (type $t3)))
  (import "env" "realloc" (func $realloc (type $t4)))
  (import "env" "free" (func $free (type $t2)))
  (import "env" "__VERIFIER_nondet_int" (func $__VERIFIER_nondet_int (type $t5)))
  (import "env" "putchar" (func $putchar (type $t3)))
  (import "env" "printf" (func $printf (type $t4)))
  (func $gmp_die (type $t2) (param $p0 i32)
    (local $l1 i32)
    (global.set $__stack_pointer
      (local.tee $l1
        (i32.sub
          (global.get $__stack_pointer)
          (i32.const 16))))
    (i32.store
      (local.get $l1)
      (local.get $p0))
    (drop
      (call $fprintf
        (i32.load
          (i32.const 0))
        (i32.const 1150)
        (local.get $l1)))
    (call $abort)
    (unreachable))
  (func $mpn_mul (type $t6) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (param $p4 i32) (result i32)
    (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32) (local $l16 i32) (local $l17 i32)
    (call $assert
      (i32.ge_s
        (local.get $p2)
        (local.get $p4)))
    (local.set $l5
      (i32.const 0))
    (call $assert
      (i32.gt_s
        (local.get $p4)
        (i32.const 0)))
    (call $assert
      (i32.or
        (i32.le_u
          (local.tee $l9
            (i32.add
              (local.tee $l7
                (i32.add
                  (local.get $p0)
                  (local.tee $l6
                    (i32.shl
                      (local.get $p2)
                      (i32.const 2)))))
              (local.tee $l8
                (i32.shl
                  (local.get $p4)
                  (i32.const 2)))))
          (local.get $p1))
        (i32.le_u
          (i32.add
            (local.get $p1)
            (local.get $l6))
          (local.get $p0))))
    (call $assert
      (i32.or
        (i32.le_u
          (local.get $l9)
          (local.get $p3))
        (i32.le_u
          (i32.add
            (local.get $p3)
            (local.get $l8))
          (local.get $p0))))
    (local.set $l6
      (i32.load
        (local.get $p3)))
    (call $assert
      (local.tee $l10
        (i32.gt_s
          (local.get $p2)
          (i32.const 0))))
    (local.set $l11
      (i32.shr_u
        (local.get $l6)
        (i32.const 16)))
    (local.set $l12
      (i32.and
        (local.get $l6)
        (i32.const 65535)))
    (local.set $l13
      (local.get $p2))
    (local.set $l6
      (local.get $p1))
    (local.set $l8
      (local.get $p0))
    (loop $L0
      (local.set $l9
        (i32.load
          (local.get $l6)))
      (call $assert
        (i32.const 1))
      (i32.store
        (local.get $l8)
        (local.tee $l14
          (i32.add
            (i32.add
              (i32.and
                (local.tee $l15
                  (i32.mul
                    (local.tee $l14
                      (i32.and
                        (local.get $l9)
                        (i32.const 65535)))
                    (local.get $l12)))
                (i32.const 65535))
              (local.get $l5))
            (i32.shl
              (local.tee $l9
                (i32.add
                  (i32.add
                    (local.tee $l17
                      (i32.mul
                        (local.tee $l16
                          (i32.shr_u
                            (local.get $l9)
                            (i32.const 16)))
                        (local.get $l12)))
                    (i32.mul
                      (local.get $l14)
                      (local.get $l11)))
                  (i32.shr_u
                    (local.get $l15)
                    (i32.const 16))))
              (i32.const 16)))))
      (local.set $l5
        (i32.add
          (i32.add
            (select
              (i32.add
                (local.tee $l15
                  (i32.mul
                    (local.get $l16)
                    (local.get $l11)))
                (i32.const 65536))
              (local.get $l15)
              (i32.lt_u
                (local.get $l9)
                (local.get $l17)))
            (i32.shr_u
              (local.get $l9)
              (i32.const 16)))
          (i32.lt_u
            (local.get $l14)
            (local.get $l5))))
      (local.set $l8
        (i32.add
          (local.get $l8)
          (i32.const 4)))
      (local.set $l6
        (i32.add
          (local.get $l6)
          (i32.const 4)))
      (br_if $L0
        (local.tee $l13
          (i32.add
            (local.get $l13)
            (i32.const -1)))))
    (i32.store
      (local.get $l7)
      (local.get $l5))
    (block $B1
      (br_if $B1
        (i32.lt_s
          (local.get $p4)
          (i32.const 2)))
      (loop $L2
        (local.set $l6
          (i32.load offset=4
            (local.get $p3)))
        (call $assert
          (local.get $l10))
        (local.set $p3
          (i32.add
            (local.get $p3)
            (i32.const 4)))
        (local.set $l7
          (i32.add
            (local.get $p0)
            (i32.const 4)))
        (local.set $l11
          (i32.shr_u
            (local.get $l6)
            (i32.const 16)))
        (local.set $l12
          (i32.and
            (local.get $l6)
            (i32.const 65535)))
        (local.set $l6
          (i32.const 0))
        (local.set $l5
          (i32.const 0))
        (local.set $l13
          (local.get $p2))
        (loop $L3
          (local.set $l8
            (i32.load
              (i32.add
                (local.get $p1)
                (local.get $l6))))
          (call $assert
            (i32.const 1))
          (i32.store
            (local.tee $l14
              (i32.add
                (i32.add
                  (local.get $p0)
                  (local.get $l6))
                (i32.const 4)))
            (local.tee $l14
              (i32.add
                (local.tee $l9
                  (i32.add
                    (i32.add
                      (i32.and
                        (local.tee $l15
                          (i32.mul
                            (local.tee $l9
                              (i32.and
                                (local.get $l8)
                                (i32.const 65535)))
                            (local.get $l12)))
                        (i32.const 65535))
                      (local.get $l5))
                    (i32.shl
                      (local.tee $l8
                        (i32.add
                          (i32.add
                            (local.tee $l17
                              (i32.mul
                                (local.tee $l16
                                  (i32.shr_u
                                    (local.get $l8)
                                    (i32.const 16)))
                                (local.get $l12)))
                            (i32.mul
                              (local.get $l9)
                              (local.get $l11)))
                          (i32.shr_u
                            (local.get $l15)
                            (i32.const 16))))
                      (i32.const 16))))
                (i32.load
                  (local.get $l14)))))
          (local.set $l5
            (i32.add
              (i32.add
                (i32.add
                  (select
                    (i32.add
                      (local.tee $l15
                        (i32.mul
                          (local.get $l16)
                          (local.get $l11)))
                      (i32.const 65536))
                    (local.get $l15)
                    (i32.lt_u
                      (local.get $l8)
                      (local.get $l17)))
                  (i32.shr_u
                    (local.get $l8)
                    (i32.const 16)))
                (i32.lt_u
                  (local.get $l9)
                  (local.get $l5)))
              (i32.lt_u
                (local.get $l14)
                (local.get $l9))))
          (local.set $l6
            (i32.add
              (local.get $l6)
              (i32.const 4)))
          (br_if $L3
            (local.tee $l13
              (i32.add
                (local.get $l13)
                (i32.const -1)))))
        (i32.store
          (i32.add
            (local.get $l7)
            (i32.shl
              (local.get $p2)
              (i32.const 2)))
          (local.get $l5))
        (local.set $l6
          (i32.gt_s
            (local.get $p4)
            (i32.const 2)))
        (local.set $p4
          (i32.add
            (local.get $p4)
            (i32.const -1)))
        (local.set $p0
          (local.get $l7))
        (br_if $L2
          (local.get $l6))))
    (local.get $l5))
  (func $mpn_invert_3by2 (type $t4) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32)
    (call $assert
      (i32.const 1))
    (block $B0
      (br_if $B0
        (i32.ge_u
          (local.tee $l3
            (i32.or
              (i32.shl
                (i32.sub
                  (local.tee $l2
                    (i32.xor
                      (local.get $p0)
                      (i32.const -1)))
                  (i32.mul
                    (local.tee $l2
                      (i32.div_u
                        (local.get $l2)
                        (local.tee $l3
                          (i32.shr_u
                            (local.get $p0)
                            (i32.const 16)))))
                    (local.get $l3)))
                (i32.const 16))
              (i32.const 65535)))
          (local.tee $l4
            (i32.mul
              (local.get $l2)
              (i32.and
                (local.get $p0)
                (i32.const 65535))))))
      (local.set $l5
        (i32.add
          (local.get $l2)
          (i32.const -1)))
      (block $B1
        (br_if $B1
          (i32.ge_u
            (local.tee $l3
              (i32.add
                (local.get $l3)
                (local.get $p0)))
            (local.get $p0)))
        (local.set $l2
          (local.get $l5))
        (br $B0))
      (block $B2
        (br_if $B2
          (i32.lt_u
            (local.get $l3)
            (local.get $l4)))
        (local.set $l2
          (local.get $l5))
        (br $B0))
      (local.set $l3
        (i32.add
          (local.get $l3)
          (local.get $p0)))
      (local.set $l2
        (i32.add
          (local.get $l2)
          (i32.const -2))))
    (local.set $l2
      (i32.add
        (i32.add
          (i32.add
            (local.tee $l5
              (i32.shr_u
                (local.tee $l4
                  (i32.add
                    (i32.mul
                      (i32.shr_u
                        (local.tee $l3
                          (i32.sub
                            (local.get $l3)
                            (local.get $l4)))
                        (i32.const 16))
                      (local.get $l2))
                    (local.get $l3)))
                (i32.const 16)))
            (local.tee $l4
              (i32.lt_u
                (local.tee $l3
                  (i32.add
                    (i32.add
                      (i32.mul
                        (local.get $p0)
                        (i32.xor
                          (local.get $l5)
                          (i32.const -1)))
                      (i32.shl
                        (local.get $l3)
                        (i32.const 16)))
                    (i32.const 65535)))
                (i32.shl
                  (local.get $l4)
                  (i32.const 16)))))
          (i32.shl
            (local.get $l2)
            (i32.const 16)))
        (local.tee $l4
          (i32.ge_u
            (local.tee $l3
              (i32.add
                (select
                  (i32.const 0)
                  (local.get $p0)
                  (local.get $l4))
                (local.get $l3)))
            (local.get $p0)))))
    (block $B3
      (br_if $B3
        (i32.eqz
          (local.get $p1)))
      (block $B4
        (br_if $B4
          (i32.ge_u
            (local.tee $l3
              (i32.add
                (i32.add
                  (local.get $p1)
                  (i32.xor
                    (local.get $l3)
                    (i32.const -1)))
                (select
                  (local.get $p0)
                  (i32.const 0)
                  (local.get $l4))))
            (local.get $p1)))
        (local.set $l2
          (i32.add
            (select
              (i32.const -1)
              (i32.const -2)
              (local.tee $l4
                (i32.lt_u
                  (local.get $l3)
                  (local.get $p0))))
            (local.get $l2)))
        (local.set $l3
          (i32.sub
            (local.get $l3)
            (i32.add
              (local.get $p0)
              (select
                (i32.const 0)
                (local.get $p0)
                (local.get $l4))))))
      (call $assert
        (i32.const 1))
      (br_if $B3
        (i32.ge_u
          (local.tee $l3
            (i32.add
              (local.tee $l5
                (i32.add
                  (select
                    (i32.add
                      (local.tee $l6
                        (i32.mul
                          (local.tee $l4
                            (i32.shr_u
                              (local.get $l2)
                              (i32.const 16)))
                          (local.tee $l5
                            (i32.shr_u
                              (local.get $p1)
                              (i32.const 16)))))
                      (i32.const 65536))
                    (local.get $l6)
                    (i32.lt_u
                      (local.tee $l4
                        (i32.add
                          (i32.add
                            (local.tee $l5
                              (i32.mul
                                (local.tee $l7
                                  (i32.and
                                    (local.get $l2)
                                    (i32.const 65535)))
                                (local.get $l5)))
                            (i32.mul
                              (local.get $l4)
                              (local.tee $l8
                                (i32.and
                                  (local.get $p1)
                                  (i32.const 65535)))))
                          (i32.shr_u
                            (local.tee $l7
                              (i32.mul
                                (local.get $l7)
                                (local.get $l8)))
                            (i32.const 16))))
                      (local.get $l5)))
                  (i32.shr_u
                    (local.get $l4)
                    (i32.const 16))))
              (local.get $l3)))
          (local.get $l5)))
      (local.set $l2
        (i32.add
          (i32.xor
            (i32.or
              (i32.gt_u
                (local.get $l3)
                (local.get $p0))
              (i32.and
                (i32.gt_u
                  (i32.or
                    (i32.shl
                      (local.get $l4)
                      (i32.const 16))
                    (i32.and
                      (local.get $l7)
                      (i32.const 65535)))
                  (local.get $p1))
                (i32.eq
                  (local.get $l3)
                  (local.get $p0))))
            (i32.const -1))
          (local.get $l2))))
    (local.get $l2))
  (func $mpn_div_qr_1_invert (type $t7) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32)
    (call $assert
      (i32.ne
        (local.get $p1)
        (i32.const 0)))
    (local.set $l2
      (i32.const 0))
    (local.set $l3
      (local.get $p1))
    (block $B0
      (br_if $B0
        (i32.gt_u
          (local.get $p1)
          (i32.const 16777215)))
      (local.set $l2
        (i32.const 0))
      (local.set $l4
        (local.get $p1))
      (loop $L1
        (local.set $l2
          (i32.add
            (local.get $l2)
            (i32.const 8)))
        (local.set $l5
          (i32.lt_u
            (local.get $l4)
            (i32.const 65536)))
        (local.set $l4
          (local.tee $l3
            (i32.shl
              (local.get $l4)
              (i32.const 8))))
        (br_if $L1
          (local.get $l5))))
    (block $B2
      (br_if $B2
        (i32.lt_s
          (local.get $l3)
          (i32.const 0)))
      (loop $L3
        (local.set $l2
          (i32.add
            (local.get $l2)
            (i32.const 1)))
        (br_if $L3
          (i32.gt_s
            (local.tee $l3
              (i32.shl
                (local.get $l3)
                (i32.const 1)))
            (i32.const -1)))))
    (i32.store
      (local.get $p0)
      (local.get $l2))
    (i32.store offset=4
      (local.get $p0)
      (local.tee $l2
        (i32.shl
          (local.get $p1)
          (local.get $l2))))
    (call $assert
      (i32.const 1))
    (block $B4
      (br_if $B4
        (i32.ge_u
          (local.tee $l4
            (i32.or
              (i32.shl
                (i32.sub
                  (local.tee $l3
                    (i32.xor
                      (local.get $l2)
                      (i32.const -1)))
                  (i32.mul
                    (local.tee $l3
                      (i32.div_u
                        (local.get $l3)
                        (local.tee $l4
                          (i32.shr_u
                            (local.get $l2)
                            (i32.const 16)))))
                    (local.get $l4)))
                (i32.const 16))
              (i32.const 65535)))
          (local.tee $l5
            (i32.mul
              (local.get $l3)
              (i32.and
                (local.get $l2)
                (i32.const 65535))))))
      (local.set $p1
        (i32.add
          (local.get $l3)
          (i32.const -1)))
      (block $B5
        (br_if $B5
          (i32.ge_u
            (local.tee $l4
              (i32.add
                (local.get $l4)
                (local.get $l2)))
            (local.get $l2)))
        (local.set $l3
          (local.get $p1))
        (br $B4))
      (block $B6
        (br_if $B6
          (i32.lt_u
            (local.get $l4)
            (local.get $l5)))
        (local.set $l3
          (local.get $p1))
        (br $B4))
      (local.set $l4
        (i32.add
          (local.get $l4)
          (local.get $l2)))
      (local.set $l3
        (i32.add
          (local.get $l3)
          (i32.const -2))))
    (i32.store offset=12
      (local.get $p0)
      (i32.add
        (i32.add
          (i32.or
            (local.tee $p1
              (i32.shr_u
                (local.tee $l5
                  (i32.add
                    (i32.mul
                      (i32.shr_u
                        (local.tee $l4
                          (i32.sub
                            (local.get $l4)
                            (local.get $l5)))
                        (i32.const 16))
                      (local.get $l3))
                    (local.get $l4)))
                (i32.const 16)))
            (i32.shl
              (local.get $l3)
              (i32.const 16)))
          (local.tee $l4
            (i32.lt_u
              (local.tee $l3
                (i32.add
                  (i32.add
                    (i32.mul
                      (local.get $l2)
                      (i32.xor
                        (local.get $p1)
                        (i32.const -1)))
                    (i32.shl
                      (local.get $l4)
                      (i32.const 16)))
                  (i32.const 65535)))
              (i32.shl
                (local.get $l5)
                (i32.const 16)))))
        (i32.ge_u
          (i32.add
            (select
              (i32.const 0)
              (local.get $l2)
              (local.get $l4))
            (local.get $l3))
          (local.get $l2)))))
  (func $mpn_div_qr_invert (type $t8) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32)
    (local.set $l3
      (i32.const 0))
    (call $assert
      (i32.gt_s
        (local.get $p2)
        (i32.const 0)))
    (block $B0
      (block $B1
        (block $B2
          (br_table $B2 $B1 $B0
            (i32.add
              (local.get $p2)
              (i32.const -1))))
        (call $mpn_div_qr_1_invert
          (local.get $p0)
          (i32.load
            (local.get $p1)))
        (return))
      (local.set $l4
        (i32.load
          (local.get $p1)))
      (call $assert
        (i32.ne
          (local.tee $l5
            (i32.load offset=4
              (local.get $p1)))
          (i32.const 0)))
      (local.set $p2
        (local.get $l5))
      (block $B3
        (br_if $B3
          (i32.gt_u
            (local.get $l5)
            (i32.const 16777215)))
        (local.set $l3
          (i32.const 0))
        (local.set $p1
          (local.get $l5))
        (loop $L4
          (local.set $l3
            (i32.add
              (local.get $l3)
              (i32.const 8)))
          (local.set $l6
            (i32.lt_u
              (local.get $p1)
              (i32.const 65536)))
          (local.set $p1
            (local.tee $p2
              (i32.shl
                (local.get $p1)
                (i32.const 8))))
          (br_if $L4
            (local.get $l6))))
      (block $B5
        (br_if $B5
          (i32.lt_s
            (local.get $p2)
            (i32.const 0)))
        (loop $L6
          (local.set $l3
            (i32.add
              (local.get $l3)
              (i32.const 1)))
          (br_if $L6
            (i32.gt_s
              (local.tee $p2
                (i32.shl
                  (local.get $p2)
                  (i32.const 1)))
              (i32.const -1)))))
      (i32.store
        (local.get $p0)
        (local.get $l3))
      (i32.store offset=8
        (local.get $p0)
        (local.tee $p2
          (i32.shl
            (local.get $l4)
            (local.get $l3))))
      (i32.store offset=4
        (local.get $p0)
        (local.tee $l3
          (i32.or
            (i32.shl
              (local.get $l5)
              (local.get $l3))
            (i32.shr_u
              (i32.shr_u
                (local.get $l4)
                (i32.const 1))
              (i32.xor
                (local.get $l3)
                (i32.const -1))))))
      (i32.store offset=12
        (local.get $p0)
        (call $mpn_invert_3by2
          (local.get $l3)
          (local.get $p2)))
      (return))
    (local.set $l4
      (i32.load
        (i32.add
          (local.tee $l7
            (i32.add
              (local.get $p1)
              (i32.shl
                (local.get $p2)
                (i32.const 2))))
          (i32.const -8))))
    (call $assert
      (i32.ne
        (local.tee $l5
          (i32.load
            (i32.add
              (local.get $l7)
              (i32.const -4))))
        (i32.const 0)))
    (local.set $p2
      (local.get $l5))
    (local.set $l3
      (i32.const 0))
    (block $B7
      (br_if $B7
        (i32.gt_u
          (local.get $l5)
          (i32.const 16777215)))
      (local.set $l3
        (i32.const 0))
      (local.set $p1
        (local.get $l5))
      (loop $L8
        (local.set $l3
          (i32.add
            (local.get $l3)
            (i32.const 8)))
        (local.set $l6
          (i32.lt_u
            (local.get $p1)
            (i32.const 65536)))
        (local.set $p1
          (local.tee $p2
            (i32.shl
              (local.get $p1)
              (i32.const 8))))
        (br_if $L8
          (local.get $l6))))
    (block $B9
      (br_if $B9
        (i32.lt_s
          (local.get $p2)
          (i32.const 0)))
      (loop $L10
        (local.set $l3
          (i32.add
            (local.get $l3)
            (i32.const 1)))
        (br_if $L10
          (i32.gt_s
            (local.tee $p2
              (i32.shl
                (local.get $p2)
                (i32.const 1)))
            (i32.const -1)))))
    (i32.store
      (local.get $p0)
      (local.get $l3))
    (block $B11
      (block $B12
        (br_if $B12
          (local.get $l3))
        (local.set $p2
          (local.get $l4))
        (br $B11))
      (local.set $p2
        (i32.or
          (i32.shr_u
            (i32.load
              (i32.add
                (local.get $l7)
                (i32.const -12)))
            (i32.sub
              (i32.const 32)
              (local.get $l3)))
          (i32.shl
            (local.get $l4)
            (local.get $l3)))))
    (i32.store offset=8
      (local.get $p0)
      (local.get $p2))
    (i32.store offset=4
      (local.get $p0)
      (local.tee $l3
        (i32.or
          (i32.shl
            (local.get $l5)
            (local.get $l3))
          (i32.shr_u
            (i32.shr_u
              (local.get $l4)
              (i32.const 1))
            (i32.xor
              (local.get $l3)
              (i32.const -1))))))
    (i32.store offset=12
      (local.get $p0)
      (call $mpn_invert_3by2
        (local.get $l3)
        (local.get $p2))))
  (func $mpn_div_qr_1_preinv (type $t9) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (result i32)
    (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32)
    (block $B0
      (block $B1
        (block $B2
          (br_if $B2
            (local.tee $l4
              (i32.load
                (local.get $p3))))
          (local.set $l5
            (i32.const 0))
          (local.set $l6
            (i32.const 1))
          (local.set $l7
            (i32.const 0))
          (br $B1))
        (local.set $l6
          (i32.const 1))
        (local.set $l5
          (local.get $p0))
        (block $B3
          (br_if $B3
            (local.get $p0))
          (call $assert
            (i32.ne
              (local.tee $l7
                (i32.shl
                  (local.get $p2)
                  (i32.const 2)))
              (i32.const 0)))
          (br_if $B0
            (i32.eqz
              (local.tee $l5
                (call $malloc
                  (local.get $l7)))))
          (local.set $l6
            (i32.eqz
              (local.get $p2)))
          (local.set $l4
            (i32.load
              (local.get $p3))))
        (call $assert
          (i32.gt_s
            (local.get $p2)
            (i32.const 0)))
        (call $assert
          (i32.ne
            (local.get $l4)
            (i32.const 0)))
        (call $assert
          (i32.lt_u
            (local.get $l4)
            (i32.const 32)))
        (local.set $l8
          (i32.sub
            (i32.const 32)
            (local.get $l4)))
        (local.set $l7
          (i32.add
            (local.get $l5)
            (local.tee $l9
              (i32.shl
                (local.get $p2)
                (i32.const 2)))))
        (local.set $l12
          (i32.shl
            (local.tee $l11
              (i32.load
                (local.tee $l9
                  (i32.add
                    (local.tee $l10
                      (i32.add
                        (local.get $p1)
                        (local.get $l9)))
                    (i32.const -4)))))
            (local.get $l4)))
        (block $B4
          (br_if $B4
            (i32.eqz
              (local.tee $p1
                (i32.add
                  (local.get $p2)
                  (i32.const -1)))))
          (block $B5
            (br_if $B5
              (i32.eqz
                (i32.and
                  (local.get $p1)
                  (i32.const 1))))
            (i32.store
              (local.tee $l7
                (i32.add
                  (local.get $l7)
                  (i32.const -4)))
              (i32.or
                (i32.shr_u
                  (local.tee $l10
                    (i32.load
                      (local.tee $l9
                        (i32.add
                          (local.get $l10)
                          (i32.const -8)))))
                  (local.get $l8))
                (local.get $l12)))
            (local.set $p1
              (i32.add
                (local.get $p2)
                (i32.const -2)))
            (local.set $l12
              (i32.shl
                (local.get $l10)
                (local.get $l4))))
          (br_if $B4
            (i32.eq
              (local.get $p2)
              (i32.const 2)))
          (local.set $l9
            (i32.add
              (local.get $l9)
              (i32.const -8)))
          (local.set $l7
            (i32.add
              (local.get $l7)
              (i32.const -8)))
          (loop $L6
            (i32.store
              (i32.add
                (local.get $l7)
                (i32.const 4))
              (i32.or
                (i32.shr_u
                  (local.tee $l10
                    (i32.load
                      (i32.add
                        (local.get $l9)
                        (i32.const 4))))
                  (local.get $l8))
                (local.get $l12)))
            (i32.store
              (local.get $l7)
              (i32.or
                (i32.shr_u
                  (local.tee $l12
                    (i32.load
                      (local.get $l9)))
                  (local.get $l8))
                (i32.shl
                  (local.get $l10)
                  (local.get $l4))))
            (local.set $l9
              (i32.add
                (local.get $l9)
                (i32.const -8)))
            (local.set $l7
              (i32.add
                (local.get $l7)
                (i32.const -8)))
            (local.set $l12
              (i32.shl
                (local.get $l12)
                (local.get $l4)))
            (br_if $L6
              (local.tee $p1
                (i32.add
                  (local.get $p1)
                  (i32.const -2)))))
          (local.set $l7
            (i32.add
              (local.get $l7)
              (i32.const 8))))
        (i32.store
          (i32.add
            (local.get $l7)
            (i32.const -4))
          (local.get $l12))
        (local.set $l7
          (i32.shr_u
            (local.get $l11)
            (local.get $l8)))
        (local.set $p1
          (local.get $l5)))
      (block $B7
        (br_if $B7
          (i32.lt_s
            (local.get $p2)
            (i32.const 1)))
        (local.set $l9
          (i32.load offset=4
            (local.get $p3)))
        (local.set $l4
          (i32.shr_u
            (local.tee $l12
              (i32.load offset=12
                (local.get $p3)))
            (i32.const 16)))
        (local.set $l12
          (i32.and
            (local.get $l12)
            (i32.const 65535)))
        (block $B8
          (br_if $B8
            (i32.eqz
              (local.get $p0)))
          (local.set $l8
            (i32.add
              (local.get $p1)
              (local.tee $l10
                (i32.add
                  (i32.shl
                    (local.get $p2)
                    (i32.const 2))
                  (i32.const -4)))))
          (local.set $p1
            (i32.add
              (local.get $p0)
              (local.get $l10)))
          (loop $L9
            (call $assert
              (i32.const 1))
            (i32.store
              (local.get $p1)
              (i32.add
                (i32.sub
                  (local.tee $p0
                    (i32.add
                      (i32.add
                        (i32.add
                          (i32.add
                            (local.get $l7)
                            (i32.shr_u
                              (local.tee $l10
                                (i32.add
                                  (i32.add
                                    (local.tee $l11
                                      (i32.mul
                                        (local.tee $p0
                                          (i32.shr_u
                                            (local.get $l7)
                                            (i32.const 16)))
                                        (local.get $l12)))
                                    (i32.mul
                                      (local.tee $l10
                                        (i32.and
                                          (local.get $l7)
                                          (i32.const 65535)))
                                      (local.get $l4)))
                                  (i32.shr_u
                                    (local.tee $l13
                                      (i32.mul
                                        (local.get $l10)
                                        (local.get $l12)))
                                    (i32.const 16))))
                              (i32.const 16)))
                          (select
                            (i32.add
                              (local.tee $l7
                                (i32.mul
                                  (local.get $p0)
                                  (local.get $l4)))
                              (i32.const 65536))
                            (local.get $l7)
                            (i32.lt_u
                              (local.get $l10)
                              (local.get $l11))))
                        (i32.lt_u
                          (local.tee $l10
                            (i32.add
                              (local.tee $l7
                                (i32.load
                                  (local.get $l8)))
                              (i32.or
                                (i32.shl
                                  (local.get $l10)
                                  (i32.const 16))
                                (i32.and
                                  (local.get $l13)
                                  (i32.const 65535)))))
                          (local.get $l7)))
                      (i32.const 1)))
                  (local.tee $l10
                    (i32.gt_u
                      (local.tee $l7
                        (i32.sub
                          (local.get $l7)
                          (i32.mul
                            (local.get $p0)
                            (local.get $l9))))
                      (local.get $l10))))
                (local.tee $l10
                  (i32.ge_u
                    (local.tee $l7
                      (i32.add
                        (select
                          (local.get $l9)
                          (i32.const 0)
                          (local.get $l10))
                        (local.get $l7)))
                    (local.get $l9)))))
            (local.set $l7
              (i32.sub
                (local.get $l7)
                (select
                  (local.get $l9)
                  (i32.const 0)
                  (local.get $l10))))
            (local.set $l8
              (i32.add
                (local.get $l8)
                (i32.const -4)))
            (local.set $p1
              (i32.add
                (local.get $p1)
                (i32.const -4)))
            (br_if $L9
              (local.tee $p2
                (i32.add
                  (local.get $p2)
                  (i32.const -1))))
            (br $B7)))
        (local.set $l8
          (i32.add
            (i32.add
              (i32.shl
                (local.get $p2)
                (i32.const 2))
              (local.get $p1))
            (i32.const -4)))
        (loop $L10
          (call $assert
            (i32.const 1))
          (local.set $l7
            (i32.sub
              (local.tee $l7
                (i32.add
                  (select
                    (local.get $l9)
                    (i32.const 0)
                    (i32.gt_u
                      (local.tee $p1
                        (i32.sub
                          (local.tee $p1
                            (i32.load
                              (local.get $l8)))
                          (i32.mul
                            (i32.add
                              (i32.add
                                (i32.add
                                  (i32.add
                                    (local.get $l7)
                                    (i32.shr_u
                                      (local.tee $l10
                                        (i32.add
                                          (i32.add
                                            (local.tee $l11
                                              (i32.mul
                                                (local.tee $p0
                                                  (i32.shr_u
                                                    (local.get $l7)
                                                    (i32.const 16)))
                                                (local.get $l12)))
                                            (i32.mul
                                              (local.tee $l10
                                                (i32.and
                                                  (local.get $l7)
                                                  (i32.const 65535)))
                                              (local.get $l4)))
                                          (i32.shr_u
                                            (local.tee $l13
                                              (i32.mul
                                                (local.get $l10)
                                                (local.get $l12)))
                                            (i32.const 16))))
                                      (i32.const 16)))
                                  (select
                                    (i32.add
                                      (local.tee $l7
                                        (i32.mul
                                          (local.get $p0)
                                          (local.get $l4)))
                                      (i32.const 65536))
                                    (local.get $l7)
                                    (i32.lt_u
                                      (local.get $l10)
                                      (local.get $l11))))
                                (i32.lt_u
                                  (local.tee $l7
                                    (i32.add
                                      (local.get $p1)
                                      (i32.or
                                        (i32.shl
                                          (local.get $l10)
                                          (i32.const 16))
                                        (i32.and
                                          (local.get $l13)
                                          (i32.const 65535)))))
                                  (local.get $p1)))
                              (i32.const 1))
                            (local.get $l9))))
                      (local.get $l7)))
                  (local.get $p1)))
              (select
                (i32.const 0)
                (local.get $l9)
                (i32.lt_u
                  (local.get $l7)
                  (local.get $l9)))))
          (local.set $l8
            (i32.add
              (local.get $l8)
              (i32.const -4)))
          (br_if $L10
            (local.tee $p2
              (i32.add
                (local.get $p2)
                (i32.const -1))))))
      (block $B11
        (br_if $B11
          (local.get $l6))
        (call $free
          (local.get $l5)))
      (return
        (i32.shr_u
          (local.get $l7)
          (i32.load
            (local.get $p3)))))
    (call $gmp_die
      (i32.const 1099))
    (unreachable))
  (func $mpn_div_qr_2_preinv (type $t10) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32)
    (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32) (local $l16 i32) (local $l17 i32)
    (call $assert
      (i32.gt_s
        (local.get $p2)
        (i32.const 1)))
    (local.set $l4
      (i32.load offset=12
        (local.get $p3)))
    (local.set $l5
      (i32.load offset=8
        (local.get $p3)))
    (local.set $l6
      (i32.load offset=4
        (local.get $p3)))
    (local.set $l7
      (i32.const 0))
    (block $B0
      (br_if $B0
        (i32.eqz
          (local.tee $l8
            (i32.load
              (local.get $p3)))))
      (call $assert
        (i32.gt_s
          (local.get $p2)
          (i32.const 0)))
      (call $assert
        (i32.const 1))
      (call $assert
        (i32.lt_u
          (local.get $l8)
          (i32.const 32)))
      (local.set $l9
        (i32.sub
          (i32.const 32)
          (local.get $l8)))
      (local.set $l12
        (i32.shl
          (local.tee $l11
            (i32.load
              (local.tee $l10
                (i32.add
                  (local.tee $l7
                    (i32.add
                      (local.get $p1)
                      (i32.shl
                        (local.get $p2)
                        (i32.const 2))))
                  (i32.const -4)))))
          (local.get $l8)))
      (block $B1
        (block $B2
          (br_if $B2
            (local.tee $l13
              (i32.add
                (local.get $p2)
                (i32.const -1))))
          (local.set $p3
            (local.get $l7))
          (br $B1))
        (block $B3
          (block $B4
            (br_if $B4
              (i32.and
                (local.get $l13)
                (i32.const 1)))
            (br $B3))
          (i32.store
            (local.tee $p3
              (i32.add
                (local.get $l7)
                (i32.const -4)))
            (i32.or
              (i32.shr_u
                (local.tee $l7
                  (i32.load
                    (local.tee $l10
                      (i32.add
                        (local.get $l7)
                        (i32.const -8)))))
                (local.get $l9))
              (local.get $l12)))
          (local.set $l13
            (i32.add
              (local.get $p2)
              (i32.const -2)))
          (local.set $l12
            (i32.shl
              (local.get $l7)
              (local.get $l8)))
          (local.set $l7
            (local.get $p3)))
        (br_if $B1
          (i32.eq
            (local.get $p2)
            (i32.const 2)))
        (local.set $p3
          (i32.add
            (local.get $l10)
            (i32.const -8)))
        (local.set $l7
          (i32.add
            (local.get $l7)
            (i32.const -8)))
        (loop $L5
          (i32.store
            (i32.add
              (local.get $l7)
              (i32.const 4))
            (i32.or
              (i32.shr_u
                (local.tee $l10
                  (i32.load
                    (i32.add
                      (local.get $p3)
                      (i32.const 4))))
                (local.get $l9))
              (local.get $l12)))
          (i32.store
            (local.get $l7)
            (i32.or
              (i32.shr_u
                (local.tee $l12
                  (i32.load
                    (local.get $p3)))
                (local.get $l9))
              (i32.shl
                (local.get $l10)
                (local.get $l8))))
          (local.set $p3
            (i32.add
              (local.get $p3)
              (i32.const -8)))
          (local.set $l7
            (i32.add
              (local.get $l7)
              (i32.const -8)))
          (local.set $l12
            (i32.shl
              (local.get $l12)
              (local.get $l8)))
          (br_if $L5
            (local.tee $l13
              (i32.add
                (local.get $l13)
                (i32.const -2)))))
        (local.set $p3
          (i32.add
            (local.get $l7)
            (i32.const 8))))
      (i32.store
        (i32.add
          (local.get $p3)
          (i32.const -4))
        (local.get $l12))
      (local.set $l7
        (i32.shr_u
          (local.get $l11)
          (local.get $l9))))
    (local.set $l10
      (i32.add
        (local.get $p2)
        (i32.const -1)))
    (local.set $l11
      (i32.shr_u
        (local.get $l5)
        (i32.const 16)))
    (local.set $l14
      (i32.and
        (local.get $l5)
        (i32.const 65535)))
    (local.set $l15
      (i32.shr_u
        (local.get $l4)
        (i32.const 16)))
    (local.set $l4
      (i32.and
        (local.get $l4)
        (i32.const 65535)))
    (local.set $p3
      (i32.add
        (local.tee $l12
          (i32.shl
            (local.get $p2)
            (i32.const 2)))
        (i32.const -8)))
    (local.set $l12
      (i32.load
        (i32.add
          (i32.add
            (local.get $p1)
            (local.get $l12))
          (i32.const -4))))
    (loop $L6
      (local.set $l9
        (i32.load
          (i32.add
            (local.get $p1)
            (local.get $p3))))
      (call $assert
        (i32.const 1))
      (call $assert
        (i32.const 1))
      (local.set $l13
        (i32.add
          (i32.sub
            (local.tee $l7
              (i32.add
                (i32.add
                  (i32.add
                    (i32.shr_u
                      (local.tee $l13
                        (i32.add
                          (i32.add
                            (local.tee $l16
                              (i32.mul
                                (local.tee $p2
                                  (i32.shr_u
                                    (local.get $l7)
                                    (i32.const 16)))
                                (local.get $l4)))
                            (i32.mul
                              (local.tee $l13
                                (i32.and
                                  (local.get $l7)
                                  (i32.const 65535)))
                              (local.get $l15)))
                          (i32.shr_u
                            (local.tee $l17
                              (i32.mul
                                (local.get $l13)
                                (local.get $l4)))
                            (i32.const 16))))
                      (i32.const 16))
                    (local.get $l7))
                  (select
                    (i32.add
                      (local.tee $l7
                        (i32.mul
                          (local.get $p2)
                          (local.get $l15)))
                      (i32.const 65536))
                    (local.get $l7)
                    (i32.lt_u
                      (local.get $l13)
                      (local.get $l16))))
                (i32.lt_u
                  (local.tee $l13
                    (i32.add
                      (local.tee $l7
                        (i32.or
                          (i32.shl
                            (local.get $l13)
                            (i32.const 16))
                          (i32.and
                            (local.get $l17)
                            (i32.const 65535))))
                      (local.get $l12)))
                  (local.get $l7))))
            (local.tee $l7
              (i32.ge_u
                (local.tee $p2
                  (i32.sub
                    (i32.sub
                      (i32.add
                        (i32.sub
                          (local.get $l12)
                          (i32.lt_u
                            (local.get $l9)
                            (local.get $l5)))
                        (i32.mul
                          (local.get $l6)
                          (i32.xor
                            (local.get $l7)
                            (i32.const -1))))
                      (i32.add
                        (i32.shr_u
                          (local.tee $l7
                            (i32.add
                              (i32.add
                                (local.tee $p2
                                  (i32.mul
                                    (local.tee $l12
                                      (i32.and
                                        (local.get $l7)
                                        (i32.const 65535)))
                                    (local.get $l11)))
                                (i32.mul
                                  (local.tee $l16
                                    (i32.shr_u
                                      (local.get $l7)
                                      (i32.const 16)))
                                  (local.get $l14)))
                              (i32.shr_u
                                (local.tee $l12
                                  (i32.mul
                                    (local.get $l12)
                                    (local.get $l14)))
                                (i32.const 16))))
                          (i32.const 16))
                        (select
                          (i32.add
                            (local.tee $l16
                              (i32.mul
                                (local.get $l16)
                                (local.get $l11)))
                            (i32.const 65536))
                          (local.get $l16)
                          (i32.lt_u
                            (local.get $l7)
                            (local.get $p2)))))
                    (i32.lt_u
                      (local.tee $l9
                        (i32.sub
                          (local.get $l9)
                          (local.get $l5)))
                      (local.tee $l12
                        (i32.or
                          (i32.shl
                            (local.get $l7)
                            (i32.const 16))
                          (i32.and
                            (local.get $l12)
                            (i32.const 65535)))))))
                (local.get $l13))))
          (i32.const 1)))
      (block $B7
        (block $B8
          (br_if $B8
            (i32.ge_u
              (local.tee $l7
                (i32.add
                  (i32.add
                    (select
                      (local.get $l6)
                      (i32.const 0)
                      (local.get $l7))
                    (local.get $p2))
                  (i32.lt_u
                    (local.tee $l9
                      (i32.add
                        (local.tee $l7
                          (select
                            (local.get $l5)
                            (i32.const 0)
                            (local.get $l7)))
                        (i32.sub
                          (local.get $l9)
                          (local.get $l12))))
                    (local.get $l7))))
              (local.get $l6)))
          (local.set $l12
            (local.get $l9))
          (br $B7))
        (block $B9
          (br_if $B9
            (i32.gt_u
              (local.get $l7)
              (local.get $l6)))
          (br_if $B9
            (i32.ge_u
              (local.get $l9)
              (local.get $l5)))
          (local.set $l12
            (local.get $l9))
          (br $B7))
        (local.set $l12
          (i32.sub
            (local.get $l9)
            (local.get $l5)))
        (local.set $l13
          (i32.add
            (local.get $l13)
            (i32.const 1)))
        (local.set $l7
          (i32.add
            (i32.sub
              (select
                (i32.const -1)
                (i32.const 0)
                (i32.lt_u
                  (local.get $l9)
                  (local.get $l5)))
              (local.get $l6))
            (local.get $l7))))
      (block $B10
        (br_if $B10
          (i32.eqz
            (local.get $p0)))
        (i32.store
          (i32.add
            (local.get $p0)
            (local.get $p3))
          (local.get $l13)))
      (local.set $p3
        (i32.add
          (local.get $p3)
          (i32.const -4)))
      (br_if $L6
        (i32.gt_s
          (local.tee $l10
            (i32.add
              (local.get $l10)
              (i32.const -1)))
          (i32.const 0))))
    (local.set $l5
      (local.get $l7))
    (block $B11
      (br_if $B11
        (i32.eqz
          (local.get $l8)))
      (call $assert
        (i32.eqz
          (i32.shl
            (local.get $l12)
            (i32.sub
              (i32.const 32)
              (local.get $l8)))))
      (local.set $l5
        (i32.shr_u
          (local.get $l7)
          (local.get $l8))))
    (i32.store offset=4
      (local.get $p1)
      (local.get $l5))
    (i32.store
      (local.get $p1)
      (i32.or
        (i32.shl
          (i32.shl
            (local.get $l7)
            (i32.const 1))
          (i32.xor
            (local.get $l8)
            (i32.const -1)))
        (i32.shr_u
          (local.get $l12)
          (local.get $l8)))))
  (func $mpn_div_qr_pi1 (type $t11) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (param $p4 i32) (param $p5 i32) (param $p6 i32)
    (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32) (local $l16 i32) (local $l17 i32) (local $l18 i32) (local $l19 i32) (local $l20 i32) (local $l21 i32) (local $l22 i32) (local $l23 i32) (local $l24 i32) (local $l25 i32) (local $l26 i32) (local $l27 i32) (local $l28 i32) (local $l29 i32) (local $l30 i32) (local $l31 i32) (local $l32 i32) (local $l33 i32) (local $l34 i32) (local $l35 i32) (local $l36 i32)
    (call $assert
      (local.tee $l7
        (i32.gt_s
          (local.get $p5)
          (i32.const 2))))
    (call $assert
      (i32.ge_s
        (local.get $p2)
        (local.get $p5)))
    (local.set $l10
      (i32.load
        (i32.add
          (local.get $p4)
          (local.tee $l9
            (i32.shl
              (local.tee $l8
                (i32.add
                  (local.get $p5)
                  (i32.const -2)))
              (i32.const 2))))))
    (call $assert
      (i32.shr_u
        (local.tee $l13
          (i32.load
            (i32.add
              (local.get $p4)
              (local.tee $l12
                (i32.shl
                  (local.tee $l11
                    (i32.add
                      (local.get $p5)
                      (i32.const -1)))
                  (i32.const 2))))))
        (i32.const 31)))
    (local.set $l14
      (i32.add
        (local.get $p1)
        (i32.sub
          (i32.shl
            (local.get $p2)
            (i32.const 2))
          (i32.shl
            (local.get $p5)
            (i32.const 2)))))
    (local.set $l15
      (i32.gt_s
        (local.get $p5)
        (i32.const 0)))
    (local.set $l16
      (i32.shr_u
        (local.get $p6)
        (i32.const 16)))
    (local.set $l17
      (i32.and
        (local.get $p6)
        (i32.const 65535)))
    (local.set $l18
      (i32.sub
        (local.get $p2)
        (local.get $p5)))
    (local.set $l19
      (i32.and
        (local.get $l11)
        (i32.const 1)))
    (local.set $l20
      (i32.sub
        (i32.const 0)
        (i32.and
          (local.get $l11)
          (i32.const -2))))
    (local.set $l21
      (i32.add
        (local.get $p1)
        (local.get $l9)))
    (local.set $l22
      (i32.add
        (local.get $p1)
        (local.get $l12)))
    (local.set $l23
      (i32.shr_u
        (local.get $l10)
        (i32.const 16)))
    (local.set $l24
      (i32.and
        (local.get $l10)
        (i32.const 65535)))
    (loop $L0
      (local.set $p2
        (i32.load
          (local.tee $l26
            (i32.add
              (local.get $l22)
              (local.tee $l25
                (i32.shl
                  (local.get $l18)
                  (i32.const 2)))))))
      (block $B1
        (block $B2
          (br_if $B2
            (i32.ne
              (local.get $p3)
              (local.get $l13)))
          (br_if $B2
            (i32.ne
              (local.get $p2)
              (local.get $l10)))
          (call $assert
            (local.get $l15))
          (local.set $p2
            (i32.const 0))
          (local.set $l11
            (i32.const 0))
          (local.set $l27
            (local.get $p5))
          (loop $L3
            (local.set $p6
              (i32.load
                (i32.add
                  (local.get $p4)
                  (local.get $p2))))
            (call $assert
              (i32.const 1))
            (i32.store
              (local.tee $l9
                (i32.add
                  (local.get $l14)
                  (local.get $p2)))
              (i32.sub
                (local.tee $l28
                  (i32.load
                    (local.get $l9)))
                (local.tee $l12
                  (i32.add
                    (i32.add
                      (i32.and
                        (local.tee $l9
                          (i32.mul
                            (i32.and
                              (local.get $p6)
                              (i32.const 65535))
                            (i32.const 65535)))
                        (i32.const 65535))
                      (local.get $l11))
                    (i32.shl
                      (local.tee $l9
                        (i32.add
                          (i32.add
                            (local.tee $p6
                              (i32.mul
                                (i32.shr_u
                                  (local.get $p6)
                                  (i32.const 16))
                                (i32.const 65535)))
                            (local.get $l9))
                          (i32.shr_u
                            (local.get $l9)
                            (i32.const 16))))
                      (i32.const 16))))))
            (local.set $l11
              (i32.add
                (i32.add
                  (i32.add
                    (select
                      (i32.add
                        (local.get $p6)
                        (i32.const 65536))
                      (local.get $p6)
                      (i32.lt_u
                        (local.get $l9)
                        (local.get $p6)))
                    (i32.shr_u
                      (local.get $l9)
                      (i32.const 16)))
                  (i32.lt_u
                    (local.get $l12)
                    (local.get $l11)))
                (i32.lt_u
                  (local.get $l28)
                  (local.get $l12))))
            (local.set $p2
              (i32.add
                (local.get $p2)
                (i32.const 4)))
            (br_if $L3
              (local.tee $l27
                (i32.add
                  (local.get $l27)
                  (i32.const -1)))))
          (local.set $p3
            (i32.load
              (local.get $l26)))
          (local.set $l29
            (i32.const -1))
          (br $B1))
        (call $assert
          (i32.const 1))
        (local.set $l11
          (i32.load
            (local.tee $l30
              (i32.add
                (local.get $l21)
                (local.get $l25)))))
        (call $assert
          (i32.const 1))
        (local.set $l29
          (i32.add
            (i32.sub
              (local.tee $p6
                (i32.add
                  (i32.add
                    (i32.add
                      (i32.shr_u
                        (local.tee $p6
                          (i32.add
                            (i32.add
                              (local.tee $l12
                                (i32.mul
                                  (local.tee $l9
                                    (i32.shr_u
                                      (local.get $p3)
                                      (i32.const 16)))
                                  (local.get $l17)))
                              (i32.mul
                                (local.tee $p6
                                  (i32.and
                                    (local.get $p3)
                                    (i32.const 65535)))
                                (local.get $l16)))
                            (i32.shr_u
                              (local.tee $l27
                                (i32.mul
                                  (local.get $p6)
                                  (local.get $l17)))
                              (i32.const 16))))
                        (i32.const 16))
                      (local.get $p3))
                    (select
                      (i32.add
                        (local.tee $l9
                          (i32.mul
                            (local.get $l9)
                            (local.get $l16)))
                        (i32.const 65536))
                      (local.get $l9)
                      (i32.lt_u
                        (local.get $p6)
                        (local.get $l12))))
                  (i32.lt_u
                    (local.tee $l9
                      (i32.add
                        (local.tee $p6
                          (i32.or
                            (i32.shl
                              (local.get $p6)
                              (i32.const 16))
                            (i32.and
                              (local.get $l27)
                              (i32.const 65535))))
                        (local.get $p2)))
                    (local.get $p6))))
              (local.tee $p2
                (i32.ge_u
                  (local.tee $l12
                    (i32.sub
                      (i32.sub
                        (i32.sub
                          (i32.add
                            (i32.mul
                              (local.get $l13)
                              (i32.xor
                                (local.get $p6)
                                (i32.const -1)))
                            (local.get $p2))
                          (i32.add
                            (i32.shr_u
                              (local.tee $p2
                                (i32.add
                                  (i32.add
                                    (local.tee $l12
                                      (i32.mul
                                        (local.tee $p2
                                          (i32.and
                                            (local.get $p6)
                                            (i32.const 65535)))
                                        (local.get $l23)))
                                    (i32.mul
                                      (local.tee $p6
                                        (i32.shr_u
                                          (local.get $p6)
                                          (i32.const 16)))
                                      (local.get $l24)))
                                  (i32.shr_u
                                    (local.tee $l27
                                      (i32.mul
                                        (local.get $p2)
                                        (local.get $l24)))
                                    (i32.const 16))))
                              (i32.const 16))
                            (select
                              (i32.add
                                (local.tee $p6
                                  (i32.mul
                                    (local.get $p6)
                                    (local.get $l23)))
                                (i32.const 65536))
                              (local.get $p6)
                              (i32.lt_u
                                (local.get $p2)
                                (local.get $l12)))))
                        (i32.lt_u
                          (local.get $l11)
                          (local.get $l10)))
                      (i32.lt_u
                        (local.tee $p6
                          (i32.sub
                            (local.get $l11)
                            (local.get $l10)))
                        (local.tee $l11
                          (i32.or
                            (i32.shl
                              (local.get $p2)
                              (i32.const 16))
                            (i32.and
                              (local.get $l27)
                              (i32.const 65535)))))))
                  (local.get $l9))))
            (i32.const 1)))
        (block $B4
          (block $B5
            (br_if $B5
              (i32.ge_u
                (local.tee $l31
                  (i32.add
                    (i32.add
                      (select
                        (local.get $l13)
                        (i32.const 0)
                        (local.get $p2))
                      (local.get $l12))
                    (i32.lt_u
                      (local.tee $p2
                        (i32.add
                          (local.tee $l9
                            (select
                              (local.get $l10)
                              (i32.const 0)
                              (local.get $p2)))
                          (i32.sub
                            (local.get $p6)
                            (local.get $l11))))
                      (local.get $l9))))
                (local.get $l13)))
            (local.set $l32
              (local.get $p2))
            (br $B4))
          (block $B6
            (br_if $B6
              (i32.gt_u
                (local.get $l31)
                (local.get $l13)))
            (br_if $B6
              (i32.ge_u
                (local.get $p2)
                (local.get $l10)))
            (local.set $l32
              (local.get $p2))
            (br $B4))
          (local.set $l32
            (i32.sub
              (local.get $p2)
              (local.get $l10)))
          (local.set $l29
            (i32.add
              (local.get $l29)
              (i32.const 1)))
          (local.set $l31
            (i32.add
              (i32.sub
                (select
                  (i32.const -1)
                  (i32.const 0)
                  (i32.lt_u
                    (local.get $p2)
                    (local.get $l10)))
                (local.get $l13))
              (local.get $l31))))
        (call $assert
          (local.get $l7))
        (local.set $l27
          (i32.shr_u
            (local.get $l29)
            (i32.const 16)))
        (local.set $l28
          (i32.and
            (local.get $l29)
            (i32.const 65535)))
        (local.set $p6
          (i32.const 0))
        (local.set $p3
          (local.get $l8))
        (local.set $l11
          (local.get $p4))
        (local.set $p2
          (local.tee $l33
            (i32.add
              (local.get $p1)
              (local.get $l25))))
        (loop $L7
          (local.set $l9
            (i32.load
              (local.get $l11)))
          (call $assert
            (i32.const 1))
          (i32.store
            (local.get $p2)
            (i32.sub
              (local.tee $l26
                (i32.load
                  (local.get $p2)))
              (local.tee $l12
                (i32.add
                  (i32.add
                    (i32.and
                      (local.tee $l34
                        (i32.mul
                          (local.tee $l12
                            (i32.and
                              (local.get $l9)
                              (i32.const 65535)))
                          (local.get $l28)))
                      (i32.const 65535))
                    (local.get $p6))
                  (i32.shl
                    (local.tee $l9
                      (i32.add
                        (i32.add
                          (local.tee $l36
                            (i32.mul
                              (local.tee $l35
                                (i32.shr_u
                                  (local.get $l9)
                                  (i32.const 16)))
                              (local.get $l28)))
                          (i32.mul
                            (local.get $l12)
                            (local.get $l27)))
                        (i32.shr_u
                          (local.get $l34)
                          (i32.const 16))))
                    (i32.const 16))))))
          (local.set $p6
            (i32.add
              (i32.add
                (i32.add
                  (select
                    (i32.add
                      (local.tee $l34
                        (i32.mul
                          (local.get $l35)
                          (local.get $l27)))
                      (i32.const 65536))
                    (local.get $l34)
                    (i32.lt_u
                      (local.get $l9)
                      (local.get $l36)))
                  (i32.shr_u
                    (local.get $l9)
                    (i32.const 16)))
                (i32.lt_u
                  (local.get $l12)
                  (local.get $p6)))
              (i32.lt_u
                (local.get $l26)
                (local.get $l12))))
          (local.set $p2
            (i32.add
              (local.get $p2)
              (i32.const 4)))
          (local.set $l11
            (i32.add
              (local.get $l11)
              (i32.const 4)))
          (br_if $L7
            (local.tee $p3
              (i32.add
                (local.get $p3)
                (i32.const -1)))))
        (i32.store
          (local.get $l30)
          (i32.sub
            (local.get $l32)
            (local.get $p6)))
        (local.set $p3
          (i32.sub
            (local.get $l31)
            (local.tee $p2
              (i32.lt_u
                (local.get $l32)
                (local.get $p6)))))
        (br_if $B1
          (i32.ge_u
            (local.get $l31)
            (local.get $p2)))
        (local.set $l11
          (i32.const 0))
        (block $B8
          (br_if $B8
            (i32.lt_s
              (local.get $p5)
              (i32.const 2)))
          (block $B9
            (block $B10
              (br_if $B10
                (local.get $l8))
              (local.set $p2
                (i32.const 0))
              (local.set $l11
                (i32.const 0))
              (br $B9))
            (local.set $l9
              (i32.const 0))
            (local.set $p2
              (local.get $l14))
            (local.set $p6
              (local.get $p4))
            (local.set $l11
              (i32.const 0))
            (loop $L11
              (i32.store
                (local.get $p2)
                (local.tee $l27
                  (i32.add
                    (local.tee $l11
                      (i32.add
                        (local.tee $l12
                          (i32.load
                            (local.get $p2)))
                        (local.get $l11)))
                    (i32.load
                      (local.get $p6)))))
              (i32.store
                (local.tee $l28
                  (i32.add
                    (local.get $p2)
                    (i32.const 4)))
                (local.tee $l12
                  (i32.add
                    (local.tee $l11
                      (i32.add
                        (local.tee $l28
                          (i32.load
                            (local.get $l28)))
                        (i32.add
                          (i32.lt_u
                            (local.get $l27)
                            (local.get $l11))
                          (i32.lt_u
                            (local.get $l11)
                            (local.get $l12)))))
                    (i32.load
                      (i32.add
                        (local.get $p6)
                        (i32.const 4))))))
              (local.set $l11
                (i32.add
                  (i32.lt_u
                    (local.get $l12)
                    (local.get $l11))
                  (i32.lt_u
                    (local.get $l11)
                    (local.get $l28))))
              (local.set $p2
                (i32.add
                  (local.get $p2)
                  (i32.const 8)))
              (local.set $p6
                (i32.add
                  (local.get $p6)
                  (i32.const 8)))
              (br_if $L11
                (i32.ne
                  (local.get $l20)
                  (local.tee $l9
                    (i32.add
                      (local.get $l9)
                      (i32.const -2))))))
            (local.set $p2
              (i32.sub
                (i32.const 0)
                (local.get $l9))))
          (br_if $B8
            (i32.eqz
              (local.get $l19)))
          (i32.store
            (local.tee $p2
              (i32.add
                (local.get $l33)
                (local.tee $p6
                  (i32.shl
                    (local.get $p2)
                    (i32.const 2)))))
            (local.tee $p6
              (i32.add
                (local.tee $p2
                  (i32.add
                    (local.tee $l9
                      (i32.load
                        (local.get $p2)))
                    (local.get $l11)))
                (i32.load
                  (i32.add
                    (local.get $p4)
                    (local.get $p6))))))
          (local.set $l11
            (i32.add
              (i32.lt_u
                (local.get $p6)
                (local.get $p2))
              (i32.lt_u
                (local.get $p2)
                (local.get $l9)))))
        (local.set $l29
          (i32.add
            (local.get $l29)
            (i32.const -1)))
        (local.set $p3
          (i32.add
            (i32.add
              (local.get $p3)
              (local.get $l13))
            (local.get $l11))))
      (block $B12
        (br_if $B12
          (i32.eqz
            (local.get $p0)))
        (i32.store
          (i32.add
            (local.get $p0)
            (local.get $l25))
          (local.get $l29)))
      (local.set $l14
        (i32.add
          (local.get $l14)
          (i32.const -4)))
      (local.set $p2
        (i32.gt_s
          (local.get $l18)
          (i32.const 0)))
      (local.set $l18
        (i32.add
          (local.get $l18)
          (i32.const -1)))
      (br_if $L0
        (local.get $p2)))
    (i32.store
      (local.get $l22)
      (local.get $p3)))
  (func $mpn_div_qr_preinv (type $t12) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (param $p4 i32) (param $p5 i32)
    (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32)
    (call $assert
      (local.tee $l6
        (i32.gt_s
          (local.get $p4)
          (i32.const 0))))
    (call $assert
      (i32.ge_s
        (local.get $p2)
        (local.get $p4)))
    (block $B0
      (block $B1
        (block $B2
          (br_table $B2 $B1 $B0
            (i32.add
              (local.get $p4)
              (i32.const -1))))
        (i32.store
          (local.get $p1)
          (call $mpn_div_qr_1_preinv
            (local.get $p0)
            (local.get $p1)
            (local.get $p2)
            (local.get $p5)))
        (return))
      (call $mpn_div_qr_2_preinv
        (local.get $p0)
        (local.get $p1)
        (local.get $p2)
        (local.get $p5))
      (return))
    (call $assert
      (i32.eq
        (i32.load offset=4
          (local.get $p5))
        (i32.load
          (i32.add
            (local.tee $l7
              (i32.add
                (local.get $p3)
                (i32.shl
                  (local.get $p4)
                  (i32.const 2))))
            (i32.const -4)))))
    (call $assert
      (i32.eq
        (i32.load offset=8
          (local.get $p5))
        (i32.load
          (i32.add
            (local.get $l7)
            (i32.const -8)))))
    (call $assert
      (i32.shr_u
        (i32.load offset=4
          (local.get $p5))
        (i32.const 31)))
    (block $B3
      (br_if $B3
        (i32.eqz
          (local.tee $l7
            (i32.load
              (local.get $p5)))))
      (call $assert
        (i32.gt_s
          (local.get $p2)
          (i32.const 0)))
      (call $assert
        (i32.const 1))
      (call $assert
        (local.tee $l8
          (i32.lt_u
            (local.get $l7)
            (i32.const 32))))
      (local.set $l9
        (i32.sub
          (i32.const 32)
          (local.get $l7)))
      (local.set $l13
        (i32.shl
          (local.tee $l12
            (i32.load
              (local.tee $l11
                (i32.add
                  (local.tee $l10
                    (i32.add
                      (local.get $p1)
                      (i32.shl
                        (local.get $p2)
                        (i32.const 2))))
                  (i32.const -4)))))
          (local.get $l7)))
      (block $B4
        (block $B5
          (br_if $B5
            (local.tee $l14
              (i32.add
                (local.get $p2)
                (i32.const -1))))
          (local.set $l15
            (local.get $l10))
          (br $B4))
        (block $B6
          (block $B7
            (br_if $B7
              (i32.and
                (local.get $l14)
                (i32.const 1)))
            (br $B6))
          (i32.store
            (local.tee $l15
              (i32.add
                (local.get $l10)
                (i32.const -4)))
            (i32.or
              (i32.shr_u
                (local.tee $l10
                  (i32.load
                    (local.tee $l11
                      (i32.add
                        (local.get $l10)
                        (i32.const -8)))))
                (local.get $l9))
              (local.get $l13)))
          (local.set $l14
            (i32.add
              (local.get $p2)
              (i32.const -2)))
          (local.set $l13
            (i32.shl
              (local.get $l10)
              (local.get $l7)))
          (local.set $l10
            (local.get $l15)))
        (br_if $B4
          (i32.eq
            (local.get $p2)
            (i32.const 2)))
        (local.set $l15
          (i32.add
            (local.get $l11)
            (i32.const -8)))
        (local.set $l10
          (i32.add
            (local.get $l10)
            (i32.const -8)))
        (loop $L8
          (i32.store
            (i32.add
              (local.get $l10)
              (i32.const 4))
            (i32.or
              (i32.shr_u
                (local.tee $l11
                  (i32.load
                    (i32.add
                      (local.get $l15)
                      (i32.const 4))))
                (local.get $l9))
              (local.get $l13)))
          (i32.store
            (local.get $l10)
            (i32.or
              (i32.shr_u
                (local.tee $l13
                  (i32.load
                    (local.get $l15)))
                (local.get $l9))
              (i32.shl
                (local.get $l11)
                (local.get $l7))))
          (local.set $l15
            (i32.add
              (local.get $l15)
              (i32.const -8)))
          (local.set $l10
            (i32.add
              (local.get $l10)
              (i32.const -8)))
          (local.set $l13
            (i32.shl
              (local.get $l13)
              (local.get $l7)))
          (br_if $L8
            (local.tee $l14
              (i32.add
                (local.get $l14)
                (i32.const -2)))))
        (local.set $l15
          (i32.add
            (local.get $l10)
            (i32.const 8))))
      (i32.store
        (i32.add
          (local.get $l15)
          (i32.const -4))
        (local.get $l13))
      (call $mpn_div_qr_pi1
        (local.get $p0)
        (local.get $p1)
        (local.get $p2)
        (i32.shr_u
          (local.get $l12)
          (local.get $l9))
        (local.get $p3)
        (local.get $p4)
        (i32.load offset=12
          (local.get $p5)))
      (call $assert
        (local.get $l6))
      (call $assert
        (i32.const 1))
      (call $assert
        (local.get $l8))
      (local.set $l14
        (i32.add
          (local.get $p4)
          (i32.const -2)))
      (local.set $l10
        (i32.shr_u
          (local.tee $l11
            (i32.load
              (local.get $p1)))
          (local.get $l7)))
      (block $B9
        (br_if $B9
          (i32.eqz
            (local.tee $p2
              (i32.and
                (local.tee $l15
                  (i32.add
                    (local.get $p4)
                    (i32.const -1)))
                (i32.const 3)))))
        (local.set $p4
          (local.get $p1))
        (loop $L10
          (i32.store
            (local.get $p4)
            (i32.add
              (i32.shl
                (local.tee $l13
                  (i32.load
                    (local.tee $p1
                      (i32.add
                        (local.get $p4)
                        (i32.const 4)))))
                (local.get $l9))
              (local.get $l10)))
          (local.set $l15
            (i32.add
              (local.get $l15)
              (i32.const -1)))
          (local.set $l10
            (i32.shr_u
              (local.get $l13)
              (local.get $l7)))
          (local.set $p4
            (local.get $p1))
          (br_if $L10
            (local.tee $p2
              (i32.add
                (local.get $p2)
                (i32.const -1))))))
      (block $B11
        (br_if $B11
          (i32.lt_u
            (local.get $l14)
            (i32.const 3)))
        (loop $L12
          (i32.store
            (local.get $p1)
            (i32.add
              (i32.shl
                (local.tee $p2
                  (i32.load
                    (local.tee $p4
                      (i32.add
                        (local.get $p1)
                        (i32.const 4)))))
                (local.get $l9))
              (local.get $l10)))
          (i32.store
            (local.tee $l10
              (i32.add
                (local.get $p1)
                (i32.const 8)))
            (i32.or
              (i32.shl
                (local.tee $l14
                  (i32.load
                    (local.tee $l13
                      (i32.add
                        (local.get $p1)
                        (i32.const 12)))))
                (local.get $l9))
              (i32.shr_u
                (local.tee $l10
                  (i32.load
                    (local.get $l10)))
                (local.get $l7))))
          (i32.store
            (local.get $p4)
            (i32.or
              (i32.shl
                (local.get $l10)
                (local.get $l9))
              (i32.shr_u
                (local.get $p2)
                (local.get $l7))))
          (i32.store
            (local.get $l13)
            (i32.or
              (i32.shl
                (local.tee $l10
                  (i32.load
                    (local.tee $p1
                      (i32.add
                        (local.get $p1)
                        (i32.const 16)))))
                (local.get $l9))
              (i32.shr_u
                (local.get $l14)
                (local.get $l7))))
          (local.set $l10
            (i32.shr_u
              (local.get $l10)
              (local.get $l7)))
          (br_if $L12
            (local.tee $l15
              (i32.add
                (local.get $l15)
                (i32.const -4))))))
      (i32.store
        (local.get $p1)
        (local.get $l10))
      (call $assert
        (i32.eqz
          (i32.shl
            (local.get $l11)
            (local.get $l9))))
      (return))
    (call $mpn_div_qr_pi1
      (local.get $p0)
      (local.get $p1)
      (local.get $p2)
      (i32.const 0)
      (local.get $p3)
      (local.get $p4)
      (i32.load offset=12
        (local.get $p5))))
  (func $mpn_div_qr (type $t13) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (param $p4 i32)
    (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32)
    (global.set $__stack_pointer
      (local.tee $l5
        (i32.sub
          (global.get $__stack_pointer)
          (i32.const 16))))
    (call $assert
      (local.tee $l6
        (i32.gt_s
          (local.get $p4)
          (i32.const 0))))
    (call $assert
      (i32.ge_s
        (local.get $p2)
        (local.get $p4)))
    (call $mpn_div_qr_invert
      (local.get $l5)
      (local.get $p3)
      (local.get $p4))
    (block $B0
      (block $B1
        (block $B2
          (block $B3
            (br_if $B3
              (i32.lt_s
                (local.get $p4)
                (i32.const 3)))
            (br_if $B2
              (local.tee $l7
                (i32.load
                  (local.get $l5)))))
          (call $mpn_div_qr_preinv
            (local.get $p0)
            (local.get $p1)
            (local.get $p2)
            (local.get $p3)
            (local.get $p4)
            (local.get $l5))
          (br $B1))
        (call $assert
          (i32.ne
            (local.tee $l8
              (i32.shl
                (local.get $p4)
                (i32.const 2)))
            (i32.const 0)))
        (br_if $B0
          (i32.eqz
            (local.tee $l9
              (call $malloc
                (local.get $l8)))))
        (call $assert
          (local.get $l6))
        (call $assert
          (i32.const 1))
        (call $assert
          (i32.lt_u
            (local.get $l7)
            (i32.const 32)))
        (local.set $l10
          (i32.sub
            (i32.const 32)
            (local.get $l7)))
        (local.set $l11
          (i32.add
            (local.get $l9)
            (local.get $l8)))
        (local.set $l6
          (i32.shl
            (local.tee $l12
              (i32.load
                (local.tee $p3
                  (i32.add
                    (local.tee $l8
                      (i32.add
                        (local.get $p3)
                        (local.get $l8)))
                    (i32.const -4)))))
            (local.get $l7)))
        (block $B4
          (br_if $B4
            (i32.eqz
              (i32.and
                (local.tee $l13
                  (i32.add
                    (local.get $p4)
                    (i32.const -1)))
                (i32.const 1))))
          (i32.store
            (local.tee $l11
              (i32.add
                (local.get $l11)
                (i32.const -4)))
            (i32.or
              (i32.shr_u
                (local.tee $l8
                  (i32.load
                    (local.tee $p3
                      (i32.add
                        (local.get $l8)
                        (i32.const -8)))))
                (local.get $l10))
              (local.get $l6)))
          (local.set $l13
            (i32.add
              (local.get $p4)
              (i32.const -2)))
          (local.set $l6
            (i32.shl
              (local.get $l8)
              (local.get $l7))))
        (local.set $l8
          (i32.add
            (local.get $p3)
            (i32.const -8)))
        (local.set $p3
          (i32.add
            (local.get $l11)
            (i32.const -8)))
        (loop $L5
          (i32.store
            (i32.add
              (local.get $p3)
              (i32.const 4))
            (i32.or
              (i32.shr_u
                (local.tee $l11
                  (i32.load
                    (i32.add
                      (local.get $l8)
                      (i32.const 4))))
                (local.get $l10))
              (local.get $l6)))
          (i32.store
            (local.get $p3)
            (i32.or
              (i32.shr_u
                (local.tee $l6
                  (i32.load
                    (local.get $l8)))
                (local.get $l10))
              (i32.shl
                (local.get $l11)
                (local.get $l7))))
          (local.set $l8
            (i32.add
              (local.get $l8)
              (i32.const -8)))
          (local.set $p3
            (i32.add
              (local.get $p3)
              (i32.const -8)))
          (local.set $l6
            (i32.shl
              (local.get $l6)
              (local.get $l7)))
          (br_if $L5
            (local.tee $l13
              (i32.add
                (local.get $l13)
                (i32.const -2)))))
        (i32.store
          (i32.add
            (local.get $p3)
            (i32.const 4))
          (local.get $l6))
        (call $assert
          (i32.eqz
            (i32.shr_u
              (local.get $l12)
              (local.get $l10))))
        (call $mpn_div_qr_preinv
          (local.get $p0)
          (local.get $p1)
          (local.get $p2)
          (local.get $l9)
          (local.get $p4)
          (local.get $l5))
        (call $free
          (local.get $l9)))
      (global.set $__stack_pointer
        (i32.add
          (local.get $l5)
          (i32.const 16)))
      (return))
    (call $gmp_die
      (i32.const 1099))
    (unreachable))
  (func $mpz_realloc (type $t4) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32) (local $l3 i32)
    (local.set $p1
      (select
        (local.get $p1)
        (i32.const 1)
        (i32.gt_s
          (local.get $p1)
          (i32.const 1))))
    (block $B0
      (block $B1
        (block $B2
          (br_if $B2
            (i32.eqz
              (i32.load
                (local.get $p0))))
          (local.set $l2
            (i32.load offset=8
              (local.get $p0)))
          (call $assert
            (i32.const 1))
          (br_if $B1
            (local.tee $l2
              (call $realloc
                (local.get $l2)
                (i32.shl
                  (local.get $p1)
                  (i32.const 2)))))
          (call $gmp_die
            (i32.const 1052))
          (unreachable))
        (call $assert
          (i32.ne
            (local.tee $l2
              (i32.shl
                (local.get $p1)
                (i32.const 2)))
            (i32.const 0)))
        (br_if $B0
          (i32.eqz
            (local.tee $l2
              (call $malloc
                (local.get $l2))))))
      (i32.store offset=8
        (local.get $p0)
        (local.get $l2))
      (i32.store
        (local.get $p0)
        (local.get $p1))
      (block $B3
        (br_if $B3
          (i32.le_u
            (i32.sub
              (i32.xor
                (local.tee $l3
                  (i32.load offset=4
                    (local.get $p0)))
                (local.tee $l3
                  (i32.shr_s
                    (local.get $l3)
                    (i32.const 31))))
              (local.get $l3))
            (local.get $p1)))
        (i32.store offset=4
          (local.get $p0)
          (i32.const 0)))
      (return
        (local.get $l2)))
    (call $gmp_die
      (i32.const 1099))
    (unreachable))
  (func $mpz_add_ui (type $t8) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32)
    (global.set $__stack_pointer
      (local.tee $l3
        (i32.sub
          (global.get $__stack_pointer)
          (i32.const 16))))
    (local.set $l4
      (i32.const 1156))
    (i32.store offset=12
      (local.get $l3)
      (i32.const 1156))
    (i32.store offset=4
      (local.get $l3)
      (i32.const 0))
    (block $B0
      (block $B1
        (block $B2
          (br_if $B2
            (i32.eqz
              (local.get $p2)))
          (i32.store offset=8
            (local.get $l3)
            (i32.const 1))
          (call $assert
            (i32.const 1))
          (br_if $B0
            (i32.eqz
              (local.tee $l4
                (call $malloc
                  (i32.const 4)))))
          (i32.store
            (local.get $l4)
            (local.get $p2))
          (i32.store offset=4
            (local.get $l3)
            (i32.const 1))
          (i32.store offset=12
            (local.get $l3)
            (local.get $l4))
          (br $B1))
        (i32.store offset=8
          (local.get $l3)
          (i32.const 0)))
      (block $B3
        (block $B4
          (br_if $B4
            (i32.lt_s
              (i32.load offset=4
                (local.get $p1))
              (i32.const 0)))
          (local.set $l5
            (call $mpz_abs_add
              (local.get $p0)
              (local.get $p1)
              (i32.add
                (local.get $l3)
                (i32.const 4))))
          (br $B3))
        (local.set $l5
          (call $mpz_abs_sub
            (local.get $p0)
            (local.get $p1)
            (i32.add
              (local.get $l3)
              (i32.const 4)))))
      (i32.store offset=4
        (local.get $p0)
        (select
          (i32.sub
            (i32.const 0)
            (local.get $l5))
          (local.get $l5)
          (i32.lt_s
            (i32.load offset=4
              (local.get $p1))
            (i32.const 0))))
      (block $B5
        (br_if $B5
          (i32.eqz
            (local.get $p2)))
        (call $free
          (local.get $l4)))
      (global.set $__stack_pointer
        (i32.add
          (local.get $l3)
          (i32.const 16)))
      (return))
    (call $gmp_die
      (i32.const 1099))
    (unreachable))
  (func $mpz_abs_add (type $t0) (param $p0 i32) (param $p1 i32) (param $p2 i32) (result i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32)
    (block $B0
      (block $B1
        (br_if $B1
          (i32.lt_u
            (local.tee $l3
              (i32.sub
                (i32.xor
                  (local.tee $l3
                    (i32.load offset=4
                      (local.get $p1)))
                  (local.tee $l3
                    (i32.shr_s
                      (local.get $l3)
                      (i32.const 31))))
                (local.get $l3)))
            (local.tee $l5
              (i32.sub
                (i32.xor
                  (local.tee $l4
                    (i32.load offset=4
                      (local.get $p2)))
                  (local.tee $l4
                    (i32.shr_s
                      (local.get $l4)
                      (i32.const 31))))
                (local.get $l4)))))
        (local.set $l6
          (local.get $l5))
        (local.set $l5
          (local.get $l3))
        (local.set $l3
          (local.get $p2))
        (local.set $p2
          (local.get $p1))
        (br $B0))
      (local.set $l6
        (local.get $l3))
      (local.set $l3
        (local.get $p1)))
    (block $B2
      (block $B3
        (br_if $B3
          (i32.lt_s
            (local.get $l5)
            (i32.load
              (local.get $p0))))
        (local.set $l7
          (call $mpz_realloc
            (local.get $p0)
            (i32.add
              (local.get $l5)
              (i32.const 1))))
        (br $B2))
      (local.set $l7
        (i32.load offset=8
          (local.get $p0))))
    (local.set $l8
      (i32.load offset=8
        (local.get $l3)))
    (local.set $l9
      (i32.load offset=8
        (local.get $p2)))
    (call $assert
      (i32.ge_u
        (local.get $l5)
        (local.get $l6)))
    (block $B4
      (block $B5
        (br_if $B5
          (local.get $l6))
        (local.set $l3
          (i32.const 0))
        (br $B4))
      (local.set $l10
        (i32.and
          (local.get $l6)
          (i32.const 1)))
      (block $B6
        (block $B7
          (br_if $B7
            (i32.ne
              (local.get $l6)
              (i32.const 1)))
          (local.set $l4
            (i32.const 0))
          (local.set $l3
            (i32.const 0))
          (br $B6))
        (local.set $l11
          (i32.and
            (local.get $l6)
            (i32.const 2147483646)))
        (local.set $l4
          (i32.const 0))
        (local.set $p2
          (local.get $l7))
        (local.set $p1
          (local.get $l8))
        (local.set $p0
          (local.get $l9))
        (local.set $l3
          (i32.const 0))
        (loop $L8
          (i32.store
            (local.get $p2)
            (local.tee $l13
              (i32.add
                (local.tee $l3
                  (i32.add
                    (local.tee $l12
                      (i32.load
                        (local.get $p0)))
                    (local.get $l3)))
                (i32.load
                  (local.get $p1)))))
          (i32.store
            (i32.add
              (local.get $p2)
              (i32.const 4))
            (local.tee $l12
              (i32.add
                (local.tee $l3
                  (i32.add
                    (local.tee $l14
                      (i32.load
                        (i32.add
                          (local.get $p0)
                          (i32.const 4))))
                    (i32.add
                      (i32.lt_u
                        (local.get $l13)
                        (local.get $l3))
                      (i32.lt_u
                        (local.get $l3)
                        (local.get $l12)))))
                (i32.load
                  (i32.add
                    (local.get $p1)
                    (i32.const 4))))))
          (local.set $l3
            (i32.add
              (i32.lt_u
                (local.get $l12)
                (local.get $l3))
              (i32.lt_u
                (local.get $l3)
                (local.get $l14))))
          (local.set $p2
            (i32.add
              (local.get $p2)
              (i32.const 8)))
          (local.set $p1
            (i32.add
              (local.get $p1)
              (i32.const 8)))
          (local.set $p0
            (i32.add
              (local.get $p0)
              (i32.const 8)))
          (br_if $L8
            (i32.ne
              (local.get $l11)
              (local.tee $l4
                (i32.add
                  (local.get $l4)
                  (i32.const 2)))))))
      (br_if $B4
        (i32.eqz
          (local.get $l10)))
      (i32.store
        (i32.add
          (local.get $l7)
          (local.tee $p2
            (i32.shl
              (local.get $l4)
              (i32.const 2))))
        (local.tee $p2
          (i32.add
            (local.tee $p1
              (i32.add
                (local.tee $p0
                  (i32.load
                    (i32.add
                      (local.get $l9)
                      (local.get $p2))))
                (local.get $l3)))
            (i32.load
              (i32.add
                (local.get $l8)
                (local.get $p2))))))
      (local.set $l3
        (i32.add
          (i32.lt_u
            (local.get $p2)
            (local.get $p1))
          (i32.lt_u
            (local.get $p1)
            (local.get $p0)))))
    (block $B9
      (br_if $B9
        (i32.le_u
          (local.get $l5)
          (local.get $l6)))
      (call $assert
        (i32.const 1))
      (local.set $l14
        (i32.and
          (local.tee $p1
            (i32.sub
              (local.get $l5)
              (local.get $l6)))
          (i32.const 1)))
      (local.set $l11
        (i32.add
          (local.get $l9)
          (local.tee $p2
            (i32.shl
              (local.get $l6)
              (i32.const 2)))))
      (local.set $l9
        (i32.add
          (local.get $l7)
          (local.get $p2)))
      (block $B10
        (block $B11
          (br_if $B11
            (i32.ne
              (local.get $l5)
              (i32.add
                (local.get $l6)
                (i32.const 1))))
          (local.set $p0
            (i32.const 0))
          (br $B10))
        (local.set $l13
          (i32.and
            (local.get $p1)
            (i32.const 2147483646)))
        (local.set $p0
          (i32.const 0))
        (local.set $p2
          (local.get $l9))
        (local.set $p1
          (local.get $l11))
        (loop $L12
          (i32.store
            (local.get $p2)
            (local.tee $l3
              (i32.add
                (local.tee $l4
                  (i32.load
                    (local.get $p1)))
                (local.get $l3))))
          (i32.store
            (i32.add
              (local.get $p2)
              (i32.const 4))
            (local.tee $l3
              (i32.add
                (local.tee $l12
                  (i32.load
                    (i32.add
                      (local.get $p1)
                      (i32.const 4))))
                (i32.lt_u
                  (local.get $l3)
                  (local.get $l4)))))
          (local.set $l3
            (i32.lt_u
              (local.get $l3)
              (local.get $l12)))
          (local.set $p2
            (i32.add
              (local.get $p2)
              (i32.const 8)))
          (local.set $p1
            (i32.add
              (local.get $p1)
              (i32.const 8)))
          (br_if $L12
            (i32.ne
              (local.get $l13)
              (local.tee $p0
                (i32.add
                  (local.get $p0)
                  (i32.const 2)))))))
      (br_if $B9
        (i32.eqz
          (local.get $l14)))
      (i32.store
        (i32.add
          (local.get $l9)
          (local.tee $p2
            (i32.shl
              (local.get $p0)
              (i32.const 2))))
        (local.tee $p1
          (i32.add
            (local.tee $p2
              (i32.load
                (i32.add
                  (local.get $l11)
                  (local.get $p2))))
            (local.get $l3))))
      (local.set $l3
        (i32.lt_u
          (local.get $p1)
          (local.get $p2))))
    (i32.store
      (i32.add
        (local.get $l7)
        (i32.shl
          (local.get $l5)
          (i32.const 2)))
      (local.get $l3))
    (i32.add
      (local.get $l3)
      (local.get $l5)))
  (func $mpz_abs_sub (type $t0) (param $p0 i32) (param $p1 i32) (param $p2 i32) (result i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32) (local $l16 i32)
    (local.set $l3
      (i32.load offset=8
        (local.get $p2)))
    (local.set $l4
      (i32.load offset=8
        (local.get $p1)))
    (block $B0
      (block $B1
        (block $B2
          (block $B3
            (br_if $B3
              (i32.ne
                (local.tee $l7
                  (i32.sub
                    (i32.xor
                      (local.tee $l5
                        (i32.load offset=4
                          (local.get $p1)))
                      (local.tee $l6
                        (i32.shr_s
                          (local.get $l5)
                          (i32.const 31))))
                    (local.get $l6)))
                (local.tee $l9
                  (i32.sub
                    (i32.xor
                      (local.tee $l8
                        (i32.load offset=4
                          (local.get $p2)))
                      (local.tee $l6
                        (i32.shr_s
                          (local.get $l8)
                          (i32.const 31))))
                    (local.get $l6)))))
            (local.set $l10
              (i32.add
                (local.get $l7)
                (i32.const 1)))
            (local.set $l6
              (i32.add
                (local.get $l4)
                (local.tee $l11
                  (i32.add
                    (i32.shl
                      (local.get $l7)
                      (i32.const 2))
                    (i32.const -4)))))
            (local.set $l11
              (i32.add
                (local.get $l3)
                (local.get $l11)))
            (loop $L4
              (block $B5
                (br_if $B5
                  (i32.ge_s
                    (local.tee $l10
                      (i32.add
                        (local.get $l10)
                        (i32.const -1)))
                    (i32.const 1)))
                (return
                  (i32.const 0)))
              (local.set $l12
                (i32.load
                  (local.get $l11)))
              (local.set $l13
                (i32.load
                  (local.get $l6)))
              (local.set $l6
                (i32.add
                  (local.get $l6)
                  (i32.const -4)))
              (local.set $l11
                (i32.add
                  (local.get $l11)
                  (i32.const -4)))
              (br_if $L4
                (i32.eq
                  (local.get $l13)
                  (local.get $l12))))
            (br_if $B2
              (i32.le_u
                (local.get $l13)
                (local.get $l12)))
            (br $B1))
          (br_if $B1
            (i32.ge_u
              (local.get $l7)
              (local.get $l9))))
        (block $B6
          (block $B7
            (br_if $B7
              (i32.le_s
                (local.get $l9)
                (i32.load
                  (local.get $p0))))
            (local.set $l14
              (call $mpz_realloc
                (local.get $p0)
                (local.get $l9)))
            (local.set $l4
              (i32.load offset=8
                (local.get $p1)))
            (local.set $l3
              (i32.load offset=8
                (local.get $p2)))
            (br $B6))
          (local.set $l14
            (i32.load offset=8
              (local.get $p0))))
        (call $assert
          (i32.ge_u
            (local.get $l9)
            (local.get $l7)))
        (block $B8
          (block $B9
            (br_if $B9
              (local.get $l5))
            (local.set $l6
              (i32.const 0))
            (br $B8))
          (local.set $l5
            (i32.and
              (local.get $l7)
              (i32.const 1)))
          (block $B10
            (block $B11
              (br_if $B11
                (i32.ne
                  (local.get $l7)
                  (i32.const 1)))
              (local.set $l13
                (i32.const 0))
              (local.set $l6
                (i32.const 0))
              (br $B10))
            (local.set $l15
              (i32.and
                (local.get $l7)
                (i32.const 2147483646)))
            (local.set $l13
              (i32.const 0))
            (local.set $l11
              (local.get $l14))
            (local.set $l10
              (local.get $l4))
            (local.set $l12
              (local.get $l3))
            (local.set $l6
              (i32.const 0))
            (loop $L12
              (i32.store
                (local.get $l11)
                (i32.sub
                  (local.tee $p2
                    (i32.load
                      (local.get $l12)))
                  (local.tee $l6
                    (i32.add
                      (local.tee $p1
                        (i32.load
                          (local.get $l10)))
                      (local.get $l6)))))
              (i32.store
                (i32.add
                  (local.get $l11)
                  (i32.const 4))
                (i32.sub
                  (local.tee $p0
                    (i32.load
                      (i32.add
                        (local.get $l12)
                        (i32.const 4))))
                  (local.tee $l6
                    (i32.add
                      (local.tee $l16
                        (i32.load
                          (i32.add
                            (local.get $l10)
                            (i32.const 4))))
                      (i32.add
                        (i32.lt_u
                          (local.get $l6)
                          (local.get $p1))
                        (i32.lt_u
                          (local.get $p2)
                          (local.get $l6)))))))
              (local.set $l6
                (i32.add
                  (i32.lt_u
                    (local.get $l6)
                    (local.get $l16))
                  (i32.lt_u
                    (local.get $p0)
                    (local.get $l6))))
              (local.set $l11
                (i32.add
                  (local.get $l11)
                  (i32.const 8)))
              (local.set $l10
                (i32.add
                  (local.get $l10)
                  (i32.const 8)))
              (local.set $l12
                (i32.add
                  (local.get $l12)
                  (i32.const 8)))
              (br_if $L12
                (i32.ne
                  (local.get $l15)
                  (local.tee $l13
                    (i32.add
                      (local.get $l13)
                      (i32.const 2)))))))
          (br_if $B8
            (i32.eqz
              (local.get $l5)))
          (i32.store
            (i32.add
              (local.get $l14)
              (local.tee $l11
                (i32.shl
                  (local.get $l13)
                  (i32.const 2))))
            (i32.sub
              (local.tee $l10
                (i32.load
                  (i32.add
                    (local.get $l3)
                    (local.get $l11))))
              (local.tee $l6
                (i32.add
                  (local.tee $l11
                    (i32.load
                      (i32.add
                        (local.get $l4)
                        (local.get $l11))))
                  (local.get $l6)))))
          (local.set $l6
            (i32.add
              (i32.lt_u
                (local.get $l6)
                (local.get $l11))
              (i32.lt_u
                (local.get $l10)
                (local.get $l6)))))
        (block $B13
          (br_if $B13
            (i32.le_u
              (local.get $l9)
              (local.get $l7)))
          (call $assert
            (i32.const 1))
          (local.set $p1
            (i32.and
              (local.tee $l10
                (i32.sub
                  (local.get $l9)
                  (local.get $l7)))
              (i32.const 1)))
          (local.set $p0
            (i32.add
              (local.get $l3)
              (local.tee $l11
                (i32.shl
                  (local.get $l7)
                  (i32.const 2)))))
          (local.set $l16
            (i32.add
              (local.get $l14)
              (local.get $l11)))
          (block $B14
            (block $B15
              (br_if $B15
                (i32.ne
                  (local.get $l9)
                  (i32.add
                    (local.get $l7)
                    (i32.const 1))))
              (local.set $l10
                (i32.const 0))
              (br $B14))
            (local.set $p2
              (i32.and
                (local.get $l10)
                (i32.const 2147483646)))
            (local.set $l10
              (i32.const 0))
            (local.set $l11
              (local.get $l16))
            (local.set $l7
              (local.get $p0))
            (loop $L16
              (i32.store
                (local.get $l11)
                (i32.sub
                  (local.tee $l12
                    (i32.load
                      (local.get $l7)))
                  (local.get $l6)))
              (i32.store
                (i32.add
                  (local.get $l11)
                  (i32.const 4))
                (i32.sub
                  (local.tee $l13
                    (i32.load
                      (i32.add
                        (local.get $l7)
                        (i32.const 4))))
                  (local.tee $l6
                    (i32.lt_u
                      (local.get $l12)
                      (local.get $l6)))))
              (local.set $l6
                (i32.lt_u
                  (local.get $l13)
                  (local.get $l6)))
              (local.set $l11
                (i32.add
                  (local.get $l11)
                  (i32.const 8)))
              (local.set $l7
                (i32.add
                  (local.get $l7)
                  (i32.const 8)))
              (br_if $L16
                (i32.ne
                  (local.get $p2)
                  (local.tee $l10
                    (i32.add
                      (local.get $l10)
                      (i32.const 2)))))))
          (br_if $B13
            (i32.eqz
              (local.get $p1)))
          (i32.store
            (i32.add
              (local.get $l16)
              (local.tee $l11
                (i32.shl
                  (local.get $l10)
                  (i32.const 2))))
            (i32.sub
              (local.tee $l11
                (i32.load
                  (i32.add
                    (local.get $p0)
                    (local.get $l11))))
              (local.get $l6)))
          (local.set $l6
            (i32.lt_u
              (local.get $l11)
              (local.get $l6))))
        (call $assert
          (i32.eqz
            (local.get $l6)))
        (block $B17
          (br_if $B17
            (i32.eqz
              (local.get $l8)))
          (local.set $l6
            (i32.add
              (i32.add
                (i32.shl
                  (local.get $l9)
                  (i32.const 2))
                (local.get $l14))
              (i32.const -4)))
          (loop $L18
            (br_if $B17
              (i32.load
                (local.get $l6)))
            (local.set $l6
              (i32.add
                (local.get $l6)
                (i32.const -4)))
            (local.set $l11
              (i32.gt_s
                (local.get $l9)
                (i32.const 1)))
            (local.set $l9
              (i32.add
                (local.get $l9)
                (i32.const -1)))
            (br_if $L18
              (local.get $l11)))
          (local.set $l9
            (i32.const 0)))
        (local.set $l7
          (i32.sub
            (i32.const 0)
            (local.get $l9)))
        (br $B0))
      (block $B19
        (block $B20
          (br_if $B20
            (i32.le_s
              (local.get $l7)
              (i32.load
                (local.get $p0))))
          (local.set $l14
            (call $mpz_realloc
              (local.get $p0)
              (local.get $l7)))
          (local.set $l3
            (i32.load offset=8
              (local.get $p2)))
          (local.set $l4
            (i32.load offset=8
              (local.get $p1)))
          (br $B19))
        (local.set $l14
          (i32.load offset=8
            (local.get $p0))))
      (call $assert
        (i32.ge_u
          (local.get $l7)
          (local.get $l9)))
      (block $B21
        (block $B22
          (br_if $B22
            (local.get $l8))
          (local.set $l6
            (i32.const 0))
          (br $B21))
        (local.set $l8
          (i32.and
            (local.get $l9)
            (i32.const 1)))
        (block $B23
          (block $B24
            (br_if $B24
              (i32.ne
                (local.get $l9)
                (i32.const 1)))
            (local.set $l13
              (i32.const 0))
            (local.set $l6
              (i32.const 0))
            (br $B23))
          (local.set $l15
            (i32.and
              (local.get $l9)
              (i32.const 2147483646)))
          (local.set $l13
            (i32.const 0))
          (local.set $l11
            (local.get $l14))
          (local.set $l10
            (local.get $l3))
          (local.set $l12
            (local.get $l4))
          (local.set $l6
            (i32.const 0))
          (loop $L25
            (i32.store
              (local.get $l11)
              (i32.sub
                (local.tee $p2
                  (i32.load
                    (local.get $l12)))
                (local.tee $l6
                  (i32.add
                    (local.tee $p1
                      (i32.load
                        (local.get $l10)))
                    (local.get $l6)))))
            (i32.store
              (i32.add
                (local.get $l11)
                (i32.const 4))
              (i32.sub
                (local.tee $p0
                  (i32.load
                    (i32.add
                      (local.get $l12)
                      (i32.const 4))))
                (local.tee $l6
                  (i32.add
                    (local.tee $l16
                      (i32.load
                        (i32.add
                          (local.get $l10)
                          (i32.const 4))))
                    (i32.add
                      (i32.lt_u
                        (local.get $l6)
                        (local.get $p1))
                      (i32.lt_u
                        (local.get $p2)
                        (local.get $l6)))))))
            (local.set $l6
              (i32.add
                (i32.lt_u
                  (local.get $l6)
                  (local.get $l16))
                (i32.lt_u
                  (local.get $p0)
                  (local.get $l6))))
            (local.set $l11
              (i32.add
                (local.get $l11)
                (i32.const 8)))
            (local.set $l10
              (i32.add
                (local.get $l10)
                (i32.const 8)))
            (local.set $l12
              (i32.add
                (local.get $l12)
                (i32.const 8)))
            (br_if $L25
              (i32.ne
                (local.get $l15)
                (local.tee $l13
                  (i32.add
                    (local.get $l13)
                    (i32.const 2)))))))
        (br_if $B21
          (i32.eqz
            (local.get $l8)))
        (i32.store
          (i32.add
            (local.get $l14)
            (local.tee $l11
              (i32.shl
                (local.get $l13)
                (i32.const 2))))
          (i32.sub
            (local.tee $l10
              (i32.load
                (i32.add
                  (local.get $l4)
                  (local.get $l11))))
            (local.tee $l6
              (i32.add
                (local.tee $l11
                  (i32.load
                    (i32.add
                      (local.get $l3)
                      (local.get $l11))))
                (local.get $l6)))))
        (local.set $l6
          (i32.add
            (i32.lt_u
              (local.get $l6)
              (local.get $l11))
            (i32.lt_u
              (local.get $l10)
              (local.get $l6)))))
      (block $B26
        (br_if $B26
          (i32.le_u
            (local.get $l7)
            (local.get $l9)))
        (call $assert
          (i32.const 1))
        (local.set $p1
          (i32.and
            (local.tee $l10
              (i32.sub
                (local.get $l7)
                (local.get $l9)))
            (i32.const 1)))
        (local.set $p0
          (i32.add
            (local.get $l4)
            (local.tee $l11
              (i32.shl
                (local.get $l9)
                (i32.const 2)))))
        (local.set $l16
          (i32.add
            (local.get $l14)
            (local.get $l11)))
        (block $B27
          (block $B28
            (br_if $B28
              (i32.ne
                (local.get $l7)
                (i32.add
                  (local.get $l9)
                  (i32.const 1))))
            (local.set $l10
              (i32.const 0))
            (br $B27))
          (local.set $p2
            (i32.and
              (local.get $l10)
              (i32.const 2147483646)))
          (local.set $l10
            (i32.const 0))
          (local.set $l11
            (local.get $l16))
          (local.set $l9
            (local.get $p0))
          (loop $L29
            (i32.store
              (local.get $l11)
              (i32.sub
                (local.tee $l12
                  (i32.load
                    (local.get $l9)))
                (local.get $l6)))
            (i32.store
              (i32.add
                (local.get $l11)
                (i32.const 4))
              (i32.sub
                (local.tee $l13
                  (i32.load
                    (i32.add
                      (local.get $l9)
                      (i32.const 4))))
                (local.tee $l6
                  (i32.lt_u
                    (local.get $l12)
                    (local.get $l6)))))
            (local.set $l6
              (i32.lt_u
                (local.get $l13)
                (local.get $l6)))
            (local.set $l11
              (i32.add
                (local.get $l11)
                (i32.const 8)))
            (local.set $l9
              (i32.add
                (local.get $l9)
                (i32.const 8)))
            (br_if $L29
              (i32.ne
                (local.get $p2)
                (local.tee $l10
                  (i32.add
                    (local.get $l10)
                    (i32.const 2)))))))
        (br_if $B26
          (i32.eqz
            (local.get $p1)))
        (i32.store
          (i32.add
            (local.get $l16)
            (local.tee $l11
              (i32.shl
                (local.get $l10)
                (i32.const 2))))
          (i32.sub
            (local.tee $l11
              (i32.load
                (i32.add
                  (local.get $p0)
                  (local.get $l11))))
            (local.get $l6)))
        (local.set $l6
          (i32.lt_u
            (local.get $l11)
            (local.get $l6))))
      (call $assert
        (i32.eqz
          (local.get $l6)))
      (br_if $B0
        (i32.eqz
          (local.get $l5)))
      (local.set $l6
        (i32.add
          (i32.add
            (i32.shl
              (local.get $l7)
              (i32.const 2))
            (local.get $l14))
          (i32.const -4)))
      (loop $L30
        (br_if $B0
          (i32.load
            (local.get $l6)))
        (local.set $l6
          (i32.add
            (local.get $l6)
            (i32.const -4)))
        (local.set $l11
          (i32.gt_s
            (local.get $l7)
            (i32.const 1)))
        (local.set $l7
          (i32.add
            (local.get $l7)
            (i32.const -1)))
        (br_if $L30
          (local.get $l11)))
      (return
        (i32.const 0)))
    (local.get $l7))
  (func $mpz_mul (type $t8) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32)
    (block $B0
      (block $B1
        (br_if $B1
          (i32.eqz
            (local.tee $l3
              (i32.load offset=4
                (local.get $p1)))))
        (br_if $B0
          (local.tee $l4
            (i32.load offset=4
              (local.get $p2)))))
      (i32.store offset=4
        (local.get $p0)
        (i32.const 0))
      (return))
    (call $assert
      (i32.const 1))
    (block $B2
      (br_if $B2
        (i32.eqz
          (local.tee $l5
            (call $malloc
              (i32.shl
                (local.tee $l9
                  (i32.add
                    (i32.shr_u
                      (i32.sub
                        (local.tee $l5
                          (i32.shl
                            (local.tee $l8
                              (i32.add
                                (local.tee $l6
                                  (i32.sub
                                    (i32.xor
                                      (local.get $l4)
                                      (local.tee $l5
                                        (i32.shr_s
                                          (local.get $l4)
                                          (i32.const 31))))
                                    (local.get $l5)))
                                (local.tee $l7
                                  (i32.sub
                                    (i32.xor
                                      (local.get $l3)
                                      (local.tee $l5
                                        (i32.shr_s
                                          (local.get $l3)
                                          (i32.const 31))))
                                    (local.get $l5)))))
                            (i32.const 5)))
                        (i32.ne
                          (local.get $l5)
                          (i32.const 0)))
                      (i32.const 5))
                    (i32.const 1)))
                (i32.const 2))))))
      (local.set $l3
        (i32.xor
          (local.get $l4)
          (local.get $l3)))
      (block $B3
        (block $B4
          (br_if $B4
            (i32.lt_u
              (local.get $l7)
              (local.get $l6)))
          (drop
            (call $mpn_mul
              (local.get $l5)
              (i32.load offset=8
                (local.get $p1))
              (local.get $l7)
              (i32.load offset=8
                (local.get $p2))
              (local.get $l6)))
          (br $B3))
        (drop
          (call $mpn_mul
            (local.get $l5)
            (i32.load offset=8
              (local.get $p2))
            (local.get $l6)
            (i32.load offset=8
              (local.get $p1))
            (local.get $l7))))
      (local.set $p1
        (i32.load
          (local.get $p0)))
      (i32.store
        (local.get $p0)
        (local.get $l9))
      (local.set $l4
        (i32.load offset=8
          (local.get $p0)))
      (i32.store offset=8
        (local.get $p0)
        (local.get $l5))
      (i32.store offset=4
        (local.get $p0)
        (select
          (i32.sub
            (i32.const 0)
            (local.tee $p2
              (i32.sub
                (local.get $l8)
                (i32.eqz
                  (i32.load
                    (i32.add
                      (i32.add
                        (local.get $l5)
                        (i32.shl
                          (local.get $l8)
                          (i32.const 2)))
                      (i32.const -4)))))))
          (local.get $p2)
          (i32.lt_s
            (local.get $l3)
            (i32.const 0))))
      (block $B5
        (br_if $B5
          (i32.eqz
            (local.get $p1)))
        (call $free
          (local.get $l4)))
      (return))
    (call $gmp_die
      (i32.const 1099))
    (unreachable))
  (func $mpz_addmul_ui (type $t8) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32)
    (global.set $__stack_pointer
      (local.tee $l3
        (i32.sub
          (global.get $__stack_pointer)
          (i32.const 16))))
    (i32.store offset=12
      (local.get $l3)
      (i32.const 1156))
    (i32.store offset=4
      (local.get $l3)
      (i32.const 0))
    (block $B0
      (block $B1
        (block $B2
          (br_if $B2
            (i32.eqz
              (local.get $p2)))
          (i32.store offset=8
            (local.get $l3)
            (i32.const 1))
          (call $assert
            (i32.const 1))
          (br_if $B0
            (i32.eqz
              (local.tee $l4
                (call $malloc
                  (i32.const 4)))))
          (i32.store
            (local.get $l4)
            (local.get $p2))
          (i32.store offset=4
            (local.get $l3)
            (i32.const 1))
          (i32.store offset=12
            (local.get $l3)
            (local.get $l4))
          (br $B1))
        (i32.store offset=8
          (local.get $l3)
          (i32.const 0)))
      (call $mpz_mul
        (i32.add
          (local.get $l3)
          (i32.const 4))
        (local.get $p1)
        (i32.add
          (local.get $l3)
          (i32.const 4)))
      (block $B3
        (block $B4
          (br_if $B4
            (i32.lt_s
              (i32.xor
                (i32.load offset=8
                  (local.get $l3))
                (i32.load offset=4
                  (local.get $p0)))
              (i32.const 0)))
          (local.set $p2
            (call $mpz_abs_add
              (local.get $p0)
              (local.get $p0)
              (i32.add
                (local.get $l3)
                (i32.const 4))))
          (br $B3))
        (local.set $p2
          (call $mpz_abs_sub
            (local.get $p0)
            (local.get $p0)
            (i32.add
              (local.get $l3)
              (i32.const 4)))))
      (i32.store offset=4
        (local.get $p0)
        (select
          (i32.sub
            (i32.const 0)
            (local.get $p2))
          (local.get $p2)
          (i32.lt_s
            (i32.load offset=4
              (local.get $p0))
            (i32.const 0))))
      (block $B5
        (br_if $B5
          (i32.eqz
            (i32.load offset=4
              (local.get $l3))))
        (call $free
          (i32.load offset=12
            (local.get $l3))))
      (global.set $__stack_pointer
        (i32.add
          (local.get $l3)
          (i32.const 16)))
      (return))
    (call $gmp_die
      (i32.const 1099))
    (unreachable))
  (func $mpz_submul_ui (type $t8) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32)
    (global.set $__stack_pointer
      (local.tee $l3
        (i32.sub
          (global.get $__stack_pointer)
          (i32.const 16))))
    (i32.store offset=12
      (local.get $l3)
      (i32.const 1156))
    (i32.store offset=4
      (local.get $l3)
      (i32.const 0))
    (block $B0
      (block $B1
        (block $B2
          (br_if $B2
            (i32.eqz
              (local.get $p2)))
          (i32.store offset=8
            (local.get $l3)
            (i32.const 1))
          (call $assert
            (i32.const 1))
          (br_if $B0
            (i32.eqz
              (local.tee $l4
                (call $malloc
                  (i32.const 4)))))
          (i32.store
            (local.get $l4)
            (local.get $p2))
          (i32.store offset=4
            (local.get $l3)
            (i32.const 1))
          (i32.store offset=12
            (local.get $l3)
            (local.get $l4))
          (br $B1))
        (i32.store offset=8
          (local.get $l3)
          (i32.const 0)))
      (call $mpz_mul
        (i32.add
          (local.get $l3)
          (i32.const 4))
        (local.get $p1)
        (i32.add
          (local.get $l3)
          (i32.const 4)))
      (block $B3
        (block $B4
          (br_if $B4
            (i32.lt_s
              (i32.xor
                (i32.load offset=8
                  (local.get $l3))
                (i32.load offset=4
                  (local.get $p0)))
              (i32.const 0)))
          (local.set $p2
            (call $mpz_abs_sub
              (local.get $p0)
              (local.get $p0)
              (i32.add
                (local.get $l3)
                (i32.const 4))))
          (br $B3))
        (local.set $p2
          (call $mpz_abs_add
            (local.get $p0)
            (local.get $p0)
            (i32.add
              (local.get $l3)
              (i32.const 4)))))
      (i32.store offset=4
        (local.get $p0)
        (select
          (i32.sub
            (i32.const 0)
            (local.get $p2))
          (local.get $p2)
          (i32.lt_s
            (i32.load offset=4
              (local.get $p0))
            (i32.const 0))))
      (block $B5
        (br_if $B5
          (i32.eqz
            (i32.load offset=4
              (local.get $l3))))
        (call $free
          (i32.load offset=12
            (local.get $l3))))
      (global.set $__stack_pointer
        (i32.add
          (local.get $l3)
          (i32.const 16)))
      (return))
    (call $gmp_die
      (i32.const 1099))
    (unreachable))
  (func $mpz_div_qr (type $t6) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32) (param $p4 i32) (result i32)
    (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32) (local $l16 i32)
    (global.set $__stack_pointer
      (local.tee $l5
        (i32.sub
          (global.get $__stack_pointer)
          (i32.const 32))))
    (block $B0
      (block $B1
        (block $B2
          (br_if $B2
            (i32.eqz
              (local.tee $l6
                (i32.load offset=4
                  (local.get $p3)))))
          (block $B3
            (block $B4
              (br_if $B4
                (local.tee $l7
                  (i32.load offset=4
                    (local.get $p2))))
              (block $B5
                (br_if $B5
                  (i32.eqz
                    (local.get $p0)))
                (i32.store offset=4
                  (local.get $p0)
                  (i32.const 0)))
              (local.set $l6
                (i32.const 0))
              (br_if $B3
                (i32.eqz
                  (local.get $p1)))
              (i32.store offset=4
                (local.get $p1)
                (i32.const 0))
              (br $B3))
            (local.set $l8
              (i32.xor
                (local.get $l6)
                (local.get $l7)))
            (block $B6
              (br_if $B6
                (i32.ge_u
                  (local.tee $l10
                    (i32.sub
                      (i32.xor
                        (local.get $l7)
                        (local.tee $l9
                          (i32.shr_s
                            (local.get $l7)
                            (i32.const 31))))
                      (local.get $l9)))
                  (local.tee $l11
                    (i32.sub
                      (i32.xor
                        (local.get $l6)
                        (local.tee $l9
                          (i32.shr_s
                            (local.get $l6)
                            (i32.const 31))))
                      (local.get $l9)))))
              (block $B7
                (br_if $B7
                  (i32.ne
                    (local.get $p4)
                    (i32.const 1)))
                (br_if $B7
                  (i32.lt_s
                    (local.get $l8)
                    (i32.const 0)))
                (block $B8
                  (br_if $B8
                    (i32.eqz
                      (local.get $p1)))
                  (i32.store offset=4
                    (local.get $p1)
                    (select
                      (i32.sub
                        (i32.const 0)
                        (local.tee $l6
                          (call $mpz_abs_sub
                            (local.get $p1)
                            (local.get $p2)
                            (local.get $p3))))
                      (local.get $l6)
                      (i32.lt_s
                        (i32.load offset=4
                          (local.get $p2))
                        (i32.const 0)))))
                (block $B9
                  (br_if $B9
                    (local.get $p0))
                  (local.set $l6
                    (i32.const 1))
                  (br $B3))
                (i32.store offset=4
                  (local.get $p0)
                  (i32.const 1))
                (block $B10
                  (block $B11
                    (br_if $B11
                      (i32.gt_s
                        (i32.load
                          (local.get $p0))
                        (i32.const 0)))
                    (local.set $p2
                      (call $mpz_realloc
                        (local.get $p0)
                        (i32.const 1)))
                    (br $B10))
                  (local.set $p2
                    (i32.load offset=8
                      (local.get $p0))))
                (local.set $l6
                  (i32.const 1))
                (i32.store
                  (local.get $p2)
                  (i32.const 1))
                (br $B3))
              (block $B12
                (br_if $B12
                  (local.get $p4))
                (br_if $B12
                  (i32.gt_s
                    (local.get $l8)
                    (i32.const -1)))
                (block $B13
                  (br_if $B13
                    (i32.eqz
                      (local.get $p1)))
                  (i32.store offset=4
                    (local.get $p1)
                    (select
                      (i32.sub
                        (i32.const 0)
                        (local.tee $l6
                          (call $mpz_abs_sub
                            (local.get $p1)
                            (local.get $p2)
                            (local.get $p3))))
                      (local.get $l6)
                      (i32.lt_s
                        (i32.load offset=4
                          (local.get $p2))
                        (i32.const 0)))))
                (block $B14
                  (br_if $B14
                    (local.get $p0))
                  (local.set $l6
                    (i32.const 1))
                  (br $B3))
                (i32.store offset=4
                  (local.get $p0)
                  (i32.const -1))
                (block $B15
                  (block $B16
                    (br_if $B16
                      (i32.gt_s
                        (i32.load
                          (local.get $p0))
                        (i32.const 0)))
                    (local.set $p2
                      (call $mpz_realloc
                        (local.get $p0)
                        (i32.const 1)))
                    (br $B15))
                  (local.set $p2
                    (i32.load offset=8
                      (local.get $p0))))
                (local.set $l6
                  (i32.const 1))
                (i32.store
                  (local.get $p2)
                  (i32.const 1))
                (br $B3))
              (block $B17
                (br_if $B17
                  (i32.eqz
                    (local.get $p1)))
                (br_if $B17
                  (i32.eq
                    (local.get $p1)
                    (local.get $p2)))
                (block $B18
                  (block $B19
                    (br_if $B19
                      (i32.le_s
                        (local.get $l10)
                        (i32.load
                          (local.get $p1))))
                    (local.set $l12
                      (call $mpz_realloc
                        (local.get $p1)
                        (local.get $l10)))
                    (br $B18))
                  (local.set $l12
                    (i32.load offset=8
                      (local.get $p1))))
                (local.set $l13
                  (i32.and
                    (local.get $l10)
                    (i32.const 3)))
                (local.set $l14
                  (i32.load offset=8
                    (local.get $p2)))
                (local.set $l15
                  (i32.const 0))
                (block $B20
                  (br_if $B20
                    (i32.lt_u
                      (local.get $l10)
                      (i32.const 4)))
                  (local.set $l16
                    (i32.and
                      (local.get $l10)
                      (i32.const 2147483644)))
                  (local.set $l6
                    (i32.const 0))
                  (local.set $l15
                    (i32.const 0))
                  (loop $L21
                    (i32.store
                      (local.tee $l9
                        (i32.add
                          (local.get $l12)
                          (local.get $l6)))
                      (i32.load
                        (local.tee $l11
                          (i32.add
                            (local.get $l14)
                            (local.get $l6)))))
                    (i32.store
                      (i32.add
                        (local.get $l9)
                        (i32.const 4))
                      (i32.load
                        (i32.add
                          (local.get $l11)
                          (i32.const 4))))
                    (i32.store
                      (i32.add
                        (local.get $l9)
                        (i32.const 8))
                      (i32.load
                        (i32.add
                          (local.get $l11)
                          (i32.const 8))))
                    (i32.store
                      (i32.add
                        (local.get $l9)
                        (i32.const 12))
                      (i32.load
                        (i32.add
                          (local.get $l11)
                          (i32.const 12))))
                    (local.set $l6
                      (i32.add
                        (local.get $l6)
                        (i32.const 16)))
                    (br_if $L21
                      (i32.ne
                        (local.get $l16)
                        (local.tee $l15
                          (i32.add
                            (local.get $l15)
                            (i32.const 4)))))))
                (block $B22
                  (br_if $B22
                    (i32.eqz
                      (local.get $l13)))
                  (local.set $l6
                    (i32.add
                      (local.get $l14)
                      (local.tee $l9
                        (i32.shl
                          (local.get $l15)
                          (i32.const 2)))))
                  (local.set $l9
                    (i32.add
                      (local.get $l12)
                      (local.get $l9)))
                  (loop $L23
                    (i32.store
                      (local.get $l9)
                      (i32.load
                        (local.get $l6)))
                    (local.set $l6
                      (i32.add
                        (local.get $l6)
                        (i32.const 4)))
                    (local.set $l9
                      (i32.add
                        (local.get $l9)
                        (i32.const 4)))
                    (br_if $L23
                      (local.tee $l13
                        (i32.add
                          (local.get $l13)
                          (i32.const -1))))))
                (i32.store offset=4
                  (local.get $p1)
                  (i32.load offset=4
                    (local.get $p2))))
              (local.set $l6
                (i32.const 1))
              (br_if $B3
                (i32.eqz
                  (local.get $p0)))
              (i32.store offset=4
                (local.get $p0)
                (i32.const 0))
              (br $B3))
            (local.set $l15
              (i32.const 0))
            (i32.store offset=12
              (local.get $l5)
              (i32.const 0))
            (call $assert
              (i32.ne
                (local.tee $l6
                  (i32.shl
                    (local.get $l10)
                    (i32.const 2)))
                (i32.const 0)))
            (br_if $B1
              (i32.eqz
                (local.tee $l12
                  (call $malloc
                    (local.get $l6)))))
            (i32.store offset=16
              (local.get $l5)
              (local.get $l12))
            (i32.store offset=8
              (local.get $l5)
              (local.get $l10))
            (local.set $l13
              (i32.and
                (local.get $l10)
                (i32.const 3)))
            (local.set $l14
              (i32.load offset=8
                (local.get $p2)))
            (block $B24
              (br_if $B24
                (i32.lt_u
                  (local.get $l10)
                  (i32.const 4)))
              (local.set $l16
                (i32.and
                  (local.get $l10)
                  (i32.const 2147483644)))
              (local.set $l6
                (i32.const 0))
              (local.set $l15
                (i32.const 0))
              (loop $L25
                (i32.store
                  (local.tee $p2
                    (i32.add
                      (local.get $l12)
                      (local.get $l6)))
                  (i32.load
                    (local.tee $l9
                      (i32.add
                        (local.get $l14)
                        (local.get $l6)))))
                (i32.store
                  (i32.add
                    (local.get $p2)
                    (i32.const 4))
                  (i32.load
                    (i32.add
                      (local.get $l9)
                      (i32.const 4))))
                (i32.store
                  (i32.add
                    (local.get $p2)
                    (i32.const 8))
                  (i32.load
                    (i32.add
                      (local.get $l9)
                      (i32.const 8))))
                (i32.store
                  (i32.add
                    (local.get $p2)
                    (i32.const 12))
                  (i32.load
                    (i32.add
                      (local.get $l9)
                      (i32.const 12))))
                (local.set $l6
                  (i32.add
                    (local.get $l6)
                    (i32.const 16)))
                (br_if $L25
                  (i32.ne
                    (local.get $l16)
                    (local.tee $l15
                      (i32.add
                        (local.get $l15)
                        (i32.const 4)))))))
            (block $B26
              (br_if $B26
                (i32.eqz
                  (local.get $l13)))
              (local.set $l6
                (i32.add
                  (local.get $l14)
                  (local.tee $p2
                    (i32.shl
                      (local.get $l15)
                      (i32.const 2)))))
              (local.set $p2
                (i32.add
                  (local.get $l12)
                  (local.get $p2)))
              (loop $L27
                (i32.store
                  (local.get $p2)
                  (i32.load
                    (local.get $l6)))
                (local.set $l6
                  (i32.add
                    (local.get $l6)
                    (i32.const 4)))
                (local.set $p2
                  (i32.add
                    (local.get $p2)
                    (i32.const 4)))
                (br_if $L27
                  (local.tee $l13
                    (i32.add
                      (local.get $l13)
                      (i32.const -1))))))
            (block $B28
              (block $B29
                (br_if $B29
                  (local.get $p0))
                (call $mpn_div_qr
                  (i32.const 0)
                  (local.get $l12)
                  (local.get $l10)
                  (i32.load offset=8
                    (local.get $p3))
                  (local.get $l11))
                (br $B28))
              (i32.store offset=20
                (local.get $l5)
                (local.tee $l6
                  (i32.add
                    (i32.shr_u
                      (i32.sub
                        (local.tee $l6
                          (i32.shl
                            (local.tee $l9
                              (i32.add
                                (local.tee $p2
                                  (i32.sub
                                    (local.get $l10)
                                    (local.get $l11)))
                                (i32.const 1)))
                            (i32.const 5)))
                        (i32.ne
                          (local.get $l6)
                          (i32.const 0)))
                      (i32.const 5))
                    (i32.const 1))))
              (call $assert
                (i32.const 1))
              (br_if $B0
                (i32.eqz
                  (local.tee $l6
                    (call $malloc
                      (i32.shl
                        (local.get $l6)
                        (i32.const 2))))))
              (i32.store offset=28
                (local.get $l5)
                (local.get $l6))
              (call $mpn_div_qr
                (local.get $l6)
                (local.get $l12)
                (local.get $l10)
                (i32.load offset=8
                  (local.get $p3))
                (local.get $l11))
              (i32.store offset=24
                (local.get $l5)
                (local.tee $l9
                  (select
                    (i32.sub
                      (i32.const 0)
                      (local.tee $l6
                        (i32.sub
                          (local.get $l9)
                          (i32.eqz
                            (i32.load
                              (i32.add
                                (local.get $l6)
                                (i32.shl
                                  (local.get $p2)
                                  (i32.const 2))))))))
                    (local.get $l6)
                    (i32.lt_s
                      (local.get $l8)
                      (i32.const 0))))))
            (local.set $l6
              (i32.add
                (i32.add
                  (i32.shl
                    (local.get $l11)
                    (i32.const 2))
                  (local.get $l12))
                (i32.const -4)))
            (block $B30
              (loop $L31
                (br_if $B30
                  (i32.load
                    (local.get $l6)))
                (local.set $l6
                  (i32.add
                    (local.get $l6)
                    (i32.const -4)))
                (local.set $p2
                  (i32.gt_s
                    (local.get $l11)
                    (i32.const 1)))
                (local.set $l11
                  (i32.add
                    (local.get $l11)
                    (i32.const -1)))
                (br_if $L31
                  (local.get $p2)))
              (local.set $l11
                (i32.const 0)))
            (i32.store offset=12
              (local.get $l5)
              (local.tee $l6
                (select
                  (i32.sub
                    (i32.const 0)
                    (local.get $l11))
                  (local.get $l11)
                  (i32.lt_s
                    (local.get $l7)
                    (i32.const 0)))))
            (block $B32
              (block $B33
                (br_if $B33
                  (local.get $p4))
                (br_if $B33
                  (i32.gt_s
                    (local.get $l8)
                    (i32.const -1)))
                (br_if $B33
                  (i32.eqz
                    (local.get $l11)))
                (block $B34
                  (br_if $B34
                    (i32.eqz
                      (local.get $p0)))
                  (i32.store offset=24
                    (local.get $l5)
                    (i32.sub
                      (i32.const 0)
                      (local.get $l9)))
                  (call $mpz_add_ui
                    (i32.add
                      (local.get $l5)
                      (i32.const 20))
                    (i32.add
                      (local.get $l5)
                      (i32.const 20))
                    (i32.const 1))
                  (i32.store offset=24
                    (local.get $l5)
                    (i32.sub
                      (i32.const 0)
                      (i32.load offset=24
                        (local.get $l5)))))
                (br_if $B32
                  (i32.eqz
                    (local.get $p1)))
                (block $B35
                  (block $B36
                    (br_if $B36
                      (i32.lt_s
                        (i32.xor
                          (i32.load offset=4
                            (local.get $p3))
                          (local.get $l6))
                        (i32.const 0)))
                    (local.set $l6
                      (call $mpz_abs_add
                        (i32.add
                          (local.get $l5)
                          (i32.const 8))
                        (i32.add
                          (local.get $l5)
                          (i32.const 8))
                        (local.get $p3)))
                    (br $B35))
                  (local.set $l6
                    (call $mpz_abs_sub
                      (i32.add
                        (local.get $l5)
                        (i32.const 8))
                      (i32.add
                        (local.get $l5)
                        (i32.const 8))
                      (local.get $p3))))
                (local.set $l6
                  (select
                    (i32.sub
                      (i32.const 0)
                      (local.get $l6))
                    (local.get $l6)
                    (i32.lt_s
                      (i32.load offset=12
                        (local.get $l5))
                      (i32.const 0))))
                (br $B32))
              (br_if $B32
                (i32.ne
                  (local.get $p4)
                  (i32.const 1)))
              (br_if $B32
                (i32.lt_s
                  (local.get $l8)
                  (i32.const 0)))
              (br_if $B32
                (i32.eqz
                  (local.get $l11)))
              (block $B37
                (br_if $B37
                  (i32.eqz
                    (local.get $p0)))
                (call $mpz_add_ui
                  (i32.add
                    (local.get $l5)
                    (i32.const 20))
                  (i32.add
                    (local.get $l5)
                    (i32.const 20))
                  (i32.const 1)))
              (br_if $B32
                (i32.eqz
                  (local.get $p1)))
              (block $B38
                (block $B39
                  (br_if $B39
                    (i32.lt_s
                      (i32.xor
                        (i32.load offset=4
                          (local.get $p3))
                        (local.get $l6))
                      (i32.const 0)))
                  (local.set $l6
                    (call $mpz_abs_sub
                      (i32.add
                        (local.get $l5)
                        (i32.const 8))
                      (i32.add
                        (local.get $l5)
                        (i32.const 8))
                      (local.get $p3)))
                  (br $B38))
                (local.set $l6
                  (call $mpz_abs_add
                    (i32.add
                      (local.get $l5)
                      (i32.const 8))
                    (i32.add
                      (local.get $l5)
                      (i32.const 8))
                    (local.get $p3))))
              (local.set $l6
                (select
                  (i32.sub
                    (i32.const 0)
                    (local.get $l6))
                  (local.get $l6)
                  (i32.lt_s
                    (i32.load offset=12
                      (local.get $l5))
                    (i32.const 0)))))
            (block $B40
              (br_if $B40
                (i32.eqz
                  (local.get $p0)))
              (local.set $p2
                (i32.load
                  (local.get $p0)))
              (i32.store
                (local.get $p0)
                (i32.load offset=20
                  (local.get $l5)))
              (local.set $l9
                (i32.load offset=8
                  (local.get $p0)))
              (i64.store offset=4 align=4
                (local.get $p0)
                (i64.load offset=24 align=4
                  (local.get $l5)))
              (br_if $B40
                (i32.eqz
                  (local.get $p2)))
              (call $free
                (local.get $l9)))
            (local.set $p2
              (i32.load offset=8
                (local.get $l5)))
            (block $B41
              (block $B42
                (br_if $B42
                  (local.get $p1))
                (local.set $l6
                  (local.get $p2))
                (br $B41))
              (i32.store offset=4
                (local.get $p1)
                (local.get $l6))
              (local.set $l6
                (i32.load
                  (local.get $p1)))
              (i32.store
                (local.get $p1)
                (local.get $p2))
              (local.set $p2
                (i32.load offset=8
                  (local.get $p1)))
              (i32.store offset=8
                (local.get $p1)
                (i32.load offset=16
                  (local.get $l5)))
              (i32.store offset=16
                (local.get $l5)
                (local.get $p2)))
            (block $B43
              (br_if $B43
                (i32.eqz
                  (local.get $l6)))
              (call $free
                (i32.load offset=16
                  (local.get $l5))))
            (local.set $l6
              (i32.ne
                (local.get $l11)
                (i32.const 0))))
          (global.set $__stack_pointer
            (i32.add
              (local.get $l5)
              (i32.const 32)))
          (return
            (local.get $l6)))
        (call $gmp_die
          (i32.const 1024))
        (unreachable))
      (call $gmp_die
        (i32.const 1099))
      (unreachable))
    (call $gmp_die
      (i32.const 1099))
    (unreachable))
  (func $extract_digit (type $t3) (param $p0 i32) (result i32)
    (local $l1 i32) (local $l2 i32)
    (global.set $__stack_pointer
      (local.tee $l1
        (i32.sub
          (global.get $__stack_pointer)
          (i32.const 16))))
    (i32.store offset=12
      (local.get $l1)
      (i32.const 1156))
    (i32.store offset=4
      (local.get $l1)
      (i32.const 0))
    (block $B0
      (block $B1
        (block $B2
          (br_if $B2
            (i32.eqz
              (local.get $p0)))
          (i32.store offset=8
            (local.get $l1)
            (i32.const 1))
          (call $assert
            (i32.const 1))
          (br_if $B1
            (local.tee $l2
              (call $malloc
                (i32.const 4))))
          (call $gmp_die
            (i32.const 1099))
          (unreachable))
        (i32.store offset=8
          (local.get $l1)
          (i32.const 0))
        (call $mpz_mul
          (i32.const 1160)
          (i32.const 1172)
          (i32.add
            (local.get $l1)
            (i32.const 4)))
        (br $B0))
      (i32.store
        (local.get $l2)
        (local.get $p0))
      (i32.store offset=4
        (local.get $l1)
        (i32.const 1))
      (i32.store offset=12
        (local.get $l1)
        (local.get $l2))
      (call $mpz_mul
        (i32.const 1160)
        (i32.const 1172)
        (i32.add
          (local.get $l1)
          (i32.const 4)))
      (call $free
        (local.get $l2)))
    (block $B3
      (block $B4
        (br_if $B4
          (i32.lt_s
            (i32.xor
              (i32.load offset=1200
                (i32.const 0))
              (i32.load offset=1164
                (i32.const 0)))
            (i32.const 0)))
        (local.set $p0
          (call $mpz_abs_add
            (i32.const 1184)
            (i32.const 1160)
            (i32.const 1196)))
        (br $B3))
      (local.set $p0
        (call $mpz_abs_sub
          (i32.const 1184)
          (i32.const 1160)
          (i32.const 1196))))
    (local.set $l2
      (i32.const 0))
    (i32.store offset=1188
      (i32.const 0)
      (select
        (i32.sub
          (i32.const 0)
          (local.get $p0))
        (local.get $p0)
        (i32.lt_s
          (i32.load offset=1164
            (i32.const 0))
          (i32.const 0))))
    (drop
      (call $mpz_div_qr
        (i32.const 1160)
        (i32.const 0)
        (i32.const 1184)
        (i32.const 1208)
        (i32.const 2)))
    (block $B5
      (br_if $B5
        (i32.eqz
          (i32.load offset=1164
            (i32.const 0))))
      (local.set $l2
        (i32.load
          (i32.load offset=1168
            (i32.const 0)))))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l1)
        (i32.const 16)))
    (local.get $l2))
  (func $eliminate_digit (type $t2) (param $p0 i32)
    (local $l1 i32)
    (global.set $__stack_pointer
      (local.tee $l1
        (i32.sub
          (global.get $__stack_pointer)
          (i32.const 16))))
    (call $mpz_submul_ui
      (i32.const 1196)
      (i32.const 1208)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l1)
      (i32.const 1))
    (call $assert
      (i32.const 1))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.eqz
            (local.tee $p0
              (call $malloc
                (i32.const 4)))))
        (i32.store
          (local.get $p0)
          (i32.const 10))
        (i32.store offset=4
          (local.get $l1)
          (i32.const 1))
        (i32.store offset=12
          (local.get $l1)
          (local.get $p0))
        (call $mpz_mul
          (i32.const 1196)
          (i32.const 1196)
          (i32.add
            (local.get $l1)
            (i32.const 4)))
        (call $free
          (local.get $p0))
        (i32.store offset=8
          (local.get $l1)
          (i32.const 1))
        (call $assert
          (i32.const 1))
        (br_if $B0
          (i32.eqz
            (local.tee $p0
              (call $malloc
                (i32.const 4)))))
        (i32.store
          (local.get $p0)
          (i32.const 10))
        (i32.store offset=4
          (local.get $l1)
          (i32.const 1))
        (i32.store offset=12
          (local.get $l1)
          (local.get $p0))
        (call $mpz_mul
          (i32.const 1172)
          (i32.const 1172)
          (i32.add
            (local.get $l1)
            (i32.const 4)))
        (call $free
          (local.get $p0))
        (global.set $__stack_pointer
          (i32.add
            (local.get $l1)
            (i32.const 16)))
        (return))
      (call $gmp_die
        (i32.const 1099))
      (unreachable))
    (call $gmp_die
      (i32.const 1099))
    (unreachable))
  (func $next_term (type $t2) (param $p0 i32)
    (local $l1 i32) (local $l2 i32) (local $l3 i32)
    (global.set $__stack_pointer
      (local.tee $l1
        (i32.sub
          (global.get $__stack_pointer)
          (i32.const 16))))
    (call $mpz_addmul_ui
      (i32.const 1196)
      (i32.const 1172)
      (i32.const 2))
    (i32.store offset=8
      (local.get $l1)
      (i32.const 1))
    (call $assert
      (i32.const 1))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.eqz
            (local.tee $l2
              (call $malloc
                (i32.const 4)))))
        (i32.store
          (local.get $l2)
          (local.tee $l3
            (i32.or
              (i32.shl
                (local.get $p0)
                (i32.const 1))
              (i32.const 1))))
        (i32.store offset=4
          (local.get $l1)
          (i32.const 1))
        (i32.store offset=12
          (local.get $l1)
          (local.get $l2))
        (call $mpz_mul
          (i32.const 1196)
          (i32.const 1196)
          (i32.add
            (local.get $l1)
            (i32.const 4)))
        (call $free
          (local.get $l2))
        (i32.store offset=8
          (local.get $l1)
          (i32.const 1))
        (call $assert
          (i32.const 1))
        (br_if $B0
          (i32.eqz
            (local.tee $l2
              (call $malloc
                (i32.const 4)))))
        (i32.store
          (local.get $l2)
          (local.get $l3))
        (i32.store offset=4
          (local.get $l1)
          (i32.const 1))
        (i32.store offset=12
          (local.get $l1)
          (local.get $l2))
        (call $mpz_mul
          (i32.const 1208)
          (i32.const 1208)
          (i32.add
            (local.get $l1)
            (i32.const 4)))
        (call $free
          (local.get $l2))
        (i32.store offset=12
          (local.get $l1)
          (i32.const 1156))
        (i32.store offset=4
          (local.get $l1)
          (i32.const 0))
        (block $B2
          (block $B3
            (block $B4
              (br_if $B4
                (i32.eqz
                  (local.get $p0)))
              (i32.store offset=8
                (local.get $l1)
                (i32.const 1))
              (call $assert
                (i32.const 1))
              (br_if $B3
                (local.tee $l2
                  (call $malloc
                    (i32.const 4))))
              (call $gmp_die
                (i32.const 1099))
              (unreachable))
            (i32.store offset=8
              (local.get $l1)
              (i32.const 0))
            (call $mpz_mul
              (i32.const 1172)
              (i32.const 1172)
              (i32.add
                (local.get $l1)
                (i32.const 4)))
            (br $B2))
          (i32.store
            (local.get $l2)
            (local.get $p0))
          (i32.store offset=4
            (local.get $l1)
            (i32.const 1))
          (i32.store offset=12
            (local.get $l1)
            (local.get $l2))
          (call $mpz_mul
            (i32.const 1172)
            (i32.const 1172)
            (i32.add
              (local.get $l1)
              (i32.const 4)))
          (call $free
            (local.get $l2)))
        (global.set $__stack_pointer
          (i32.add
            (local.get $l1)
            (i32.const 16)))
        (return))
      (call $gmp_die
        (i32.const 1099))
      (unreachable))
    (call $gmp_die
      (i32.const 1099))
    (unreachable))
  (func $_start (type $t5) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32)
    (global.set $__stack_pointer
      (local.tee $l0
        (i32.sub
          (global.get $__stack_pointer)
          (i32.const 16))))
    (local.set $l1
      (call $__VERIFIER_nondet_int))
    (i32.store offset=1168
      (i32.const 0)
      (i32.const 1156))
    (i64.store offset=1160 align=4
      (i32.const 0)
      (i64.const 0))
    (i32.store offset=1192
      (i32.const 0)
      (i32.const 1156))
    (i64.store offset=1184 align=4
      (i32.const 0)
      (i64.const 0))
    (i32.store offset=1204
      (i32.const 0)
      (i32.const 1156))
    (i64.store offset=1196 align=4
      (i32.const 0)
      (i64.const 0))
    (i32.store offset=1216
      (i32.const 0)
      (i32.const 1156))
    (i64.store offset=1208 align=4
      (i32.const 0)
      (i64.const 4294967296))
    (local.set $l2
      (call $mpz_realloc
        (i32.const 1208)
        (i32.const 1)))
    (i32.store offset=1180
      (i32.const 0)
      (i32.const 1156))
    (i32.store
      (local.get $l2)
      (i32.const 1))
    (i64.store offset=1172 align=4
      (i32.const 0)
      (i64.const 4294967296))
    (i32.store
      (call $mpz_realloc
        (i32.const 1172)
        (i32.const 1))
      (i32.const 1))
    (block $B0
      (br_if $B0
        (i32.eqz
          (local.get $l1)))
      (local.set $l3
        (i32.const 0))
      (local.set $l4
        (i32.const 0))
      (loop $L1
        (call $next_term
          (local.tee $l3
            (i32.add
              (local.get $l3)
              (i32.const 1))))
        (block $B2
          (block $B3
            (br_if $B3
              (i32.eq
                (local.tee $l2
                  (i32.load offset=1176
                    (i32.const 0)))
                (local.tee $l5
                  (i32.load offset=1200
                    (i32.const 0)))))
            (br_if $L1
              (i32.ge_s
                (local.get $l2)
                (local.get $l5)))
            (br $B2))
          (block $B4
            (br_if $B4
              (i32.lt_s
                (local.get $l2)
                (i32.const 0)))
            (local.set $l6
              (i32.add
                (local.get $l2)
                (i32.const 1)))
            (local.set $l2
              (i32.add
                (i32.add
                  (i32.load offset=1180
                    (i32.const 0))
                  (local.tee $l5
                    (i32.shl
                      (local.get $l2)
                      (i32.const 2))))
                (i32.const -4)))
            (local.set $l5
              (i32.add
                (i32.add
                  (i32.load offset=1204
                    (i32.const 0))
                  (local.get $l5))
                (i32.const -4)))
            (loop $L5
              (br_if $B2
                (i32.lt_s
                  (local.tee $l6
                    (i32.add
                      (local.get $l6)
                      (i32.const -1)))
                  (i32.const 1)))
              (local.set $l7
                (i32.load
                  (local.get $l5)))
              (local.set $l8
                (i32.load
                  (local.get $l2)))
              (local.set $l2
                (i32.add
                  (local.get $l2)
                  (i32.const -4)))
              (local.set $l5
                (i32.add
                  (local.get $l5)
                  (i32.const -4)))
              (br_if $L5
                (i32.eq
                  (local.get $l8)
                  (local.get $l7))))
            (br_if $L1
              (i32.gt_u
                (local.get $l8)
                (local.get $l7)))
            (br $B2))
          (local.set $l6
            (i32.sub
              (i32.const 1)
              (local.get $l2)))
          (local.set $l2
            (i32.add
              (i32.sub
                (i32.load offset=1204
                  (i32.const 0))
                (local.tee $l5
                  (i32.shl
                    (local.get $l2)
                    (i32.const 2))))
              (i32.const -4)))
          (local.set $l5
            (i32.add
              (i32.sub
                (i32.load offset=1180
                  (i32.const 0))
                (local.get $l5))
              (i32.const -4)))
          (loop $L6
            (br_if $B2
              (i32.lt_s
                (local.tee $l6
                  (i32.add
                    (local.get $l6)
                    (i32.const -1)))
                (i32.const 1)))
            (local.set $l7
              (i32.load
                (local.get $l5)))
            (local.set $l8
              (i32.load
                (local.get $l2)))
            (local.set $l2
              (i32.add
                (local.get $l2)
                (i32.const -4)))
            (local.set $l5
              (i32.add
                (local.get $l5)
                (i32.const -4)))
            (br_if $L6
              (i32.eq
                (local.get $l8)
                (local.get $l7))))
          (br_if $L1
            (i32.gt_u
              (local.get $l8)
              (local.get $l7))))
        (br_if $L1
          (i32.ne
            (local.tee $l2
              (call $extract_digit
                (i32.const 3)))
            (call $extract_digit
              (i32.const 4))))
        (drop
          (call $putchar
            (i32.add
              (local.get $l2)
              (i32.const 48))))
        (block $B7
          (br_if $B7
            (i32.rem_u
              (local.tee $l4
                (i32.add
                  (local.get $l4)
                  (i32.const 1)))
              (i32.const 10)))
          (i32.store
            (local.get $l0)
            (local.get $l4))
          (drop
            (call $printf
              (i32.const 1144)
              (local.get $l0))))
        (call $eliminate_digit
          (local.get $l2))
        (br_if $L1
          (i32.ne
            (local.get $l4)
            (local.get $l1)))))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l0)
        (i32.const 16)))
    (i32.const 0))
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66768))
  (export "memory" (memory $memory))
  (export "_start" (func $_start))
  (data $.rodata (i32.const 1024) "mpz_div_qr: Divide by zero.\00gmp_default_realloc: Virtual memory exhausted.\00gmp_default_alloc: Virtual memory exhausted.\00\09:%u\0a\00%s\0a\00\00\00\a0\c1\00\00")
  (@custom "name" "\00\0e\0dpidigits.wasm\01\95\03\1f\00\07fprintf\01\05abort\02\06assert\03\06malloc\04\07realloc\05\04free\06\15__VERIFIER_nondet_int\07\07putchar\08\06printf\09\07gmp_die\0a\07mpn_mul\0b\0fmpn_invert_3by2\0c\13mpn_div_qr_1_invert\0d\11mpn_div_qr_invert\0e\13mpn_div_qr_1_preinv\0f\13mpn_div_qr_2_preinv\10\0empn_div_qr_pi1\11\11mpn_div_qr_preinv\12\0ampn_div_qr\13\0bmpz_realloc\14\0ampz_add_ui\15\0bmpz_abs_add\16\0bmpz_abs_sub\17\07mpz_mul\18\0dmpz_addmul_ui\19\0dmpz_submul_ui\1a\0ampz_div_qr\1b\0dextract_digit\1c\0feliminate_digit\1d\09next_term\1e\06_start\07\12\01\00\0f__stack_pointer\09\0a\01\00\07.rodata")
  (@custom "producers" "\01\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
