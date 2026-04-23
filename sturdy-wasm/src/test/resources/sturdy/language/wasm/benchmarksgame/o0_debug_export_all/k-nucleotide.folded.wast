(module $k-nucleotide.wasm
  (type $t0 (func (param i32 i32) (result i32)))
  (type $t1 (func (param i32)))
  (type $t2 (func (param i32) (result i32)))
  (type $t3 (func (param i32 i32 i32 i32)))
  (type $t4 (func (param i32 i32 i32) (result i32)))
  (type $t5 (func))
  (type $t6 (func (result i32)))
  (type $t7 (func (param i32 i64) (result i32)))
  (type $t8 (func (param i32 i64 i32) (result i32)))
  (type $t9 (func (param i32 i32)))
  (import "env" "calloc" (func $calloc (type $t0)))
  (import "env" "free" (func $free (type $t1)))
  (import "env" "malloc" (func $malloc (type $t2)))
  (import "env" "realloc" (func $realloc (type $t0)))
  (import "env" "qsort" (func $qsort (type $t3)))
  (import "env" "strlen" (func $strlen (type $t2)))
  (import "env" "fgets" (func $fgets (type $t4)))
  (import "env" "memcmp" (func $memcmp (type $t4)))
  (func $__wasm_call_ctors (type $t5))
  (func $kh_init_oligonucleotide (type $t6) (result i32)
    (return
      (call $calloc
        (i32.const 1)
        (i32.const 28))))
  (func $kh_destroy_oligonucleotide (type $t1) (param $p0 i32)
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
          (i32.and
            (i32.ne
              (i32.load offset=12
                (local.get $l1))
              (i32.const 0))
            (i32.const 1))))
      (call $free
        (i32.load offset=20
          (i32.load offset=12
            (local.get $l1))))
      (call $free
        (i32.load offset=16
          (i32.load offset=12
            (local.get $l1))))
      (call $free
        (i32.load offset=24
          (i32.load offset=12
            (local.get $l1))))
      (call $free
        (i32.load offset=12
          (local.get $l1))))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l1)
        (i32.const 16)))
    (return))
  (func $kh_clear_oligonucleotide (type $t1) (param $p0 i32)
    (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32)
    (local.set $l1
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (i32.store offset=12
      (local.get $l1)
      (local.get $p0))
    (block $B0
      (br_if $B0
        (i32.eqz
          (i32.and
            (i32.ne
              (i32.load offset=12
                (local.get $l1))
              (i32.const 0))
            (i32.const 1))))
      (br_if $B0
        (i32.eqz
          (i32.and
            (i32.ne
              (i32.load offset=16
                (i32.load offset=12
                  (local.get $l1)))
              (i32.const 0))
            (i32.const 1))))
      (local.set $l2
        (i32.load offset=16
          (i32.load offset=12
            (local.get $l1))))
      (block $B1
        (block $B2
          (br_if $B2
            (i32.eqz
              (i32.and
                (i32.lt_u
                  (i32.load
                    (i32.load offset=12
                      (local.get $l1)))
                  (i32.const 16))
                (i32.const 1))))
          (local.set $l3
            (i32.const 1))
          (br $B1))
        (local.set $l3
          (i32.shr_u
            (i32.load
              (i32.load offset=12
                (local.get $l1)))
            (i32.const 4))))
      (local.set $l4
        (i32.shl
          (local.get $l3)
          (i32.const 2)))
      (local.set $l5
        (i32.const 170))
      (block $B3
        (br_if $B3
          (i32.eqz
            (local.get $l4)))
        (memory.fill
          (local.get $l2)
          (local.get $l5)
          (local.get $l4)))
      (i32.store offset=8
        (i32.load offset=12
          (local.get $l1))
        (i32.const 0))
      (i32.store offset=4
        (i32.load offset=12
          (local.get $l1))
        (i32.const 0)))
    (return))
  (func $kh_get_oligonucleotide (type $t7) (param $p0 i32) (param $p1 i64) (result i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 48)))
    (i32.store offset=40
      (local.get $l2)
      (local.get $p0))
    (i64.store offset=32
      (local.get $l2)
      (local.get $p1))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.load
              (i32.load offset=40
                (local.get $l2)))))
        (i32.store offset=12
          (local.get $l2)
          (i32.const 0))
        (i32.store offset=16
          (local.get $l2)
          (i32.sub
            (i32.load
              (i32.load offset=40
                (local.get $l2)))
            (i32.const 1)))
        (i32.store offset=28
          (local.get $l2)
          (i32.wrap_i64
            (i64.xor
              (i64.load offset=32
                (local.get $l2))
              (i64.shr_u
                (i64.load offset=32
                  (local.get $l2))
                (i64.const 7)))))
        (i32.store offset=24
          (local.get $l2)
          (i32.and
            (i32.load offset=28
              (local.get $l2))
            (i32.load offset=16
              (local.get $l2))))
        (i32.store offset=20
          (local.get $l2)
          (i32.load offset=24
            (local.get $l2)))
        (loop $L2
          (local.set $l3
            (i32.and
              (i32.shr_u
                (i32.load
                  (i32.add
                    (i32.load offset=16
                      (i32.load offset=40
                        (local.get $l2)))
                    (i32.shl
                      (i32.shr_u
                        (i32.load offset=24
                          (local.get $l2))
                        (i32.const 4))
                      (i32.const 2))))
                (i32.shl
                  (i32.and
                    (i32.load offset=24
                      (local.get $l2))
                    (i32.const 15))
                  (i32.const 1)))
              (i32.const 2)))
          (local.set $l4
            (i32.const 0))
          (block $B3
            (br_if $B3
              (local.get $l3))
            (local.set $l5
              (i32.and
                (i32.shr_u
                  (i32.load
                    (i32.add
                      (i32.load offset=16
                        (i32.load offset=40
                          (local.get $l2)))
                      (i32.shl
                        (i32.shr_u
                          (i32.load offset=24
                            (local.get $l2))
                          (i32.const 4))
                        (i32.const 2))))
                  (i32.shl
                    (i32.and
                      (i32.load offset=24
                        (local.get $l2))
                      (i32.const 15))
                    (i32.const 1)))
                (i32.const 1)))
            (local.set $l6
              (i32.const 1))
            (block $B4
              (br_if $B4
                (local.get $l5))
              (local.set $l6
                (i32.xor
                  (i64.eq
                    (i64.load
                      (i32.add
                        (i32.load offset=20
                          (i32.load offset=40
                            (local.get $l2)))
                        (i32.shl
                          (i32.load offset=24
                            (local.get $l2))
                          (i32.const 3))))
                    (i64.load offset=32
                      (local.get $l2)))
                  (i32.const -1))))
            (local.set $l4
              (local.get $l6)))
          (block $B5
            (br_if $B5
              (i32.eqz
                (i32.and
                  (local.get $l4)
                  (i32.const 1))))
            (local.set $l7
              (i32.load offset=24
                (local.get $l2)))
            (local.set $l8
              (i32.add
                (i32.load offset=12
                  (local.get $l2))
                (i32.const 1)))
            (i32.store offset=12
              (local.get $l2)
              (local.get $l8))
            (i32.store offset=24
              (local.get $l2)
              (i32.and
                (i32.add
                  (local.get $l7)
                  (local.get $l8))
                (i32.load offset=16
                  (local.get $l2))))
            (block $B6
              (br_if $B6
                (i32.eqz
                  (i32.and
                    (i32.eq
                      (i32.load offset=24
                        (local.get $l2))
                      (i32.load offset=20
                        (local.get $l2)))
                    (i32.const 1))))
              (i32.store offset=44
                (local.get $l2)
                (i32.load
                  (i32.load offset=40
                    (local.get $l2))))
              (br $B0))
            (br $L2)))
        (block $B7
          (block $B8
            (br_if $B8
              (i32.eqz
                (i32.and
                  (i32.shr_u
                    (i32.load
                      (i32.add
                        (i32.load offset=16
                          (i32.load offset=40
                            (local.get $l2)))
                        (i32.shl
                          (i32.shr_u
                            (i32.load offset=24
                              (local.get $l2))
                            (i32.const 4))
                          (i32.const 2))))
                    (i32.shl
                      (i32.and
                        (i32.load offset=24
                          (local.get $l2))
                        (i32.const 15))
                      (i32.const 1)))
                  (i32.const 3))))
            (local.set $l9
              (i32.load
                (i32.load offset=40
                  (local.get $l2))))
            (br $B7))
          (local.set $l9
            (i32.load offset=24
              (local.get $l2))))
        (i32.store offset=44
          (local.get $l2)
          (local.get $l9))
        (br $B0))
      (i32.store offset=44
        (local.get $l2)
        (i32.const 0)))
    (return
      (i32.load offset=44
        (local.get $l2))))
  (func $kh_resize_oligonucleotide (type $t0) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32) (local $l16 i32) (local $l17 i32) (local $l18 i64) (local $l19 i32) (local $l20 i32) (local $l21 i32) (local $l22 i32) (local $l23 i64) (local $l24 i32) (local $l25 i32) (local $l26 i32) (local $l27 i32) (local $l28 i32) (local $l29 i32) (local $l30 i32) (local $l31 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 80)))
    (global.set $__stack_pointer
      (local.get $l2))
    (i32.store offset=72
      (local.get $l2)
      (local.get $p0))
    (i32.store offset=68
      (local.get $l2)
      (local.get $p1))
    (i32.store offset=64
      (local.get $l2)
      (i32.const 0))
    (i32.store offset=60
      (local.get $l2)
      (i32.const 1))
    (i32.store offset=68
      (local.get $l2)
      (i32.add
        (i32.load offset=68
          (local.get $l2))
        (i32.const -1)))
    (i32.store offset=68
      (local.get $l2)
      (i32.or
        (i32.shr_u
          (i32.load offset=68
            (local.get $l2))
          (i32.const 1))
        (i32.load offset=68
          (local.get $l2))))
    (i32.store offset=68
      (local.get $l2)
      (i32.or
        (i32.shr_u
          (i32.load offset=68
            (local.get $l2))
          (i32.const 2))
        (i32.load offset=68
          (local.get $l2))))
    (i32.store offset=68
      (local.get $l2)
      (i32.or
        (i32.shr_u
          (i32.load offset=68
            (local.get $l2))
          (i32.const 4))
        (i32.load offset=68
          (local.get $l2))))
    (i32.store offset=68
      (local.get $l2)
      (i32.or
        (i32.shr_u
          (i32.load offset=68
            (local.get $l2))
          (i32.const 8))
        (i32.load offset=68
          (local.get $l2))))
    (i32.store offset=68
      (local.get $l2)
      (i32.or
        (i32.shr_u
          (i32.load offset=68
            (local.get $l2))
          (i32.const 16))
        (i32.load offset=68
          (local.get $l2))))
    (i32.store offset=68
      (local.get $l2)
      (i32.add
        (i32.load offset=68
          (local.get $l2))
        (i32.const 1)))
    (block $B0
      (br_if $B0
        (i32.eqz
          (i32.and
            (i32.lt_u
              (i32.load offset=68
                (local.get $l2))
              (i32.const 4))
            (i32.const 1))))
      (i32.store offset=68
        (local.get $l2)
        (i32.const 4)))
    (block $B1
      (block $B2
        (block $B3
          (br_if $B3
            (i32.eqz
              (i32.and
                (i32.ge_u
                  (i32.load offset=4
                    (i32.load offset=72
                      (local.get $l2)))
                  (i32.trunc_sat_f64_u
                    (f64.add
                      (f64.mul
                        (f64.convert_i32_u
                          (i32.load offset=68
                            (local.get $l2)))
                        (f64.const 0x1.8a3d70a3d70a4p-1 (;=0.77;)))
                      (f64.const 0x1p-1 (;=0.5;)))))
                (i32.const 1))))
          (i32.store offset=60
            (local.get $l2)
            (i32.const 0))
          (br $B2))
        (block $B4
          (block $B5
            (br_if $B5
              (i32.eqz
                (i32.and
                  (i32.lt_u
                    (i32.load offset=68
                      (local.get $l2))
                    (i32.const 16))
                  (i32.const 1))))
            (local.set $l3
              (i32.const 1))
            (br $B4))
          (local.set $l3
            (i32.shr_u
              (i32.load offset=68
                (local.get $l2))
              (i32.const 4))))
        (i32.store offset=64
          (local.get $l2)
          (call $malloc
            (i32.shl
              (local.get $l3)
              (i32.const 2))))
        (block $B6
          (br_if $B6
            (i32.and
              (i32.ne
                (i32.load offset=64
                  (local.get $l2))
                (i32.const 0))
              (i32.const 1)))
          (i32.store offset=76
            (local.get $l2)
            (i32.const -1))
          (br $B1))
        (local.set $l4
          (i32.load offset=64
            (local.get $l2)))
        (block $B7
          (block $B8
            (br_if $B8
              (i32.eqz
                (i32.and
                  (i32.lt_u
                    (i32.load offset=68
                      (local.get $l2))
                    (i32.const 16))
                  (i32.const 1))))
            (local.set $l5
              (i32.const 1))
            (br $B7))
          (local.set $l5
            (i32.shr_u
              (i32.load offset=68
                (local.get $l2))
              (i32.const 4))))
        (local.set $l6
          (i32.shl
            (local.get $l5)
            (i32.const 2)))
        (local.set $l7
          (i32.const 170))
        (block $B9
          (br_if $B9
            (i32.eqz
              (local.get $l6)))
          (memory.fill
            (local.get $l4)
            (local.get $l7)
            (local.get $l6)))
        (block $B10
          (br_if $B10
            (i32.eqz
              (i32.and
                (i32.lt_u
                  (i32.load
                    (i32.load offset=72
                      (local.get $l2)))
                  (i32.load offset=68
                    (local.get $l2)))
                (i32.const 1))))
          (i32.store offset=56
            (local.get $l2)
            (call $realloc
              (i32.load offset=20
                (i32.load offset=72
                  (local.get $l2)))
              (i32.shl
                (i32.load offset=68
                  (local.get $l2))
                (i32.const 3))))
          (block $B11
            (br_if $B11
              (i32.and
                (i32.ne
                  (i32.load offset=56
                    (local.get $l2))
                  (i32.const 0))
                (i32.const 1)))
            (call $free
              (i32.load offset=64
                (local.get $l2)))
            (i32.store offset=76
              (local.get $l2)
              (i32.const -1))
            (br $B1))
          (local.set $l8
            (i32.load offset=56
              (local.get $l2)))
          (i32.store offset=20
            (i32.load offset=72
              (local.get $l2))
            (local.get $l8))
          (i32.store offset=52
            (local.get $l2)
            (call $realloc
              (i32.load offset=24
                (i32.load offset=72
                  (local.get $l2)))
              (i32.shl
                (i32.load offset=68
                  (local.get $l2))
                (i32.const 2))))
          (block $B12
            (br_if $B12
              (i32.and
                (i32.ne
                  (i32.load offset=52
                    (local.get $l2))
                  (i32.const 0))
                (i32.const 1)))
            (call $free
              (i32.load offset=64
                (local.get $l2)))
            (i32.store offset=76
              (local.get $l2)
              (i32.const -1))
            (br $B1))
          (local.set $l9
            (i32.load offset=52
              (local.get $l2)))
          (i32.store offset=24
            (i32.load offset=72
              (local.get $l2))
            (local.get $l9))))
      (block $B13
        (br_if $B13
          (i32.eqz
            (i32.load offset=60
              (local.get $l2))))
        (i32.store offset=60
          (local.get $l2)
          (i32.const 0))
        (block $B14
          (loop $L15
            (br_if $B14
              (i32.eqz
                (i32.and
                  (i32.ne
                    (i32.load offset=60
                      (local.get $l2))
                    (i32.load
                      (i32.load offset=72
                        (local.get $l2))))
                  (i32.const 1))))
            (block $B16
              (br_if $B16
                (i32.and
                  (i32.shr_u
                    (i32.load
                      (i32.add
                        (i32.load offset=16
                          (i32.load offset=72
                            (local.get $l2)))
                        (i32.shl
                          (i32.shr_u
                            (i32.load offset=60
                              (local.get $l2))
                            (i32.const 4))
                          (i32.const 2))))
                    (i32.shl
                      (i32.and
                        (i32.load offset=60
                          (local.get $l2))
                        (i32.const 15))
                      (i32.const 1)))
                  (i32.const 3)))
              (i64.store offset=40
                (local.get $l2)
                (i64.load
                  (i32.add
                    (i32.load offset=20
                      (i32.load offset=72
                        (local.get $l2)))
                    (i32.shl
                      (i32.load offset=60
                        (local.get $l2))
                      (i32.const 3)))))
              (i32.store offset=32
                (local.get $l2)
                (i32.sub
                  (i32.load offset=68
                    (local.get $l2))
                  (i32.const 1)))
              (i32.store offset=36
                (local.get $l2)
                (i32.load
                  (i32.add
                    (i32.load offset=24
                      (i32.load offset=72
                        (local.get $l2)))
                    (i32.shl
                      (i32.load offset=60
                        (local.get $l2))
                      (i32.const 2)))))
              (local.set $l10
                (i32.shl
                  (i32.and
                    (i32.load offset=60
                      (local.get $l2))
                    (i32.const 15))
                  (i32.const 1)))
              (local.set $l11
                (i32.shl
                  (i32.const 1)
                  (local.get $l10)))
              (local.set $l12
                (i32.add
                  (i32.load offset=16
                    (i32.load offset=72
                      (local.get $l2)))
                  (i32.shl
                    (i32.shr_u
                      (i32.load offset=60
                        (local.get $l2))
                      (i32.const 4))
                    (i32.const 2))))
              (i32.store
                (local.get $l12)
                (i32.or
                  (local.get $l11)
                  (i32.load
                    (local.get $l12))))
              (loop $L17
                (i32.store offset=20
                  (local.get $l2)
                  (i32.const 0))
                (i32.store offset=28
                  (local.get $l2)
                  (i32.wrap_i64
                    (i64.xor
                      (i64.load offset=40
                        (local.get $l2))
                      (i64.shr_u
                        (i64.load offset=40
                          (local.get $l2))
                        (i64.const 7)))))
                (i32.store offset=24
                  (local.get $l2)
                  (i32.and
                    (i32.load offset=28
                      (local.get $l2))
                    (i32.load offset=32
                      (local.get $l2))))
                (block $B18
                  (loop $L19
                    (br_if $B18
                      (i32.eqz
                        (i32.and
                          (i32.xor
                            (i32.ne
                              (i32.and
                                (i32.shr_u
                                  (i32.load
                                    (i32.add
                                      (i32.load offset=64
                                        (local.get $l2))
                                      (i32.shl
                                        (i32.shr_u
                                          (i32.load offset=24
                                            (local.get $l2))
                                          (i32.const 4))
                                        (i32.const 2))))
                                  (i32.shl
                                    (i32.and
                                      (i32.load offset=24
                                        (local.get $l2))
                                      (i32.const 15))
                                    (i32.const 1)))
                                (i32.const 2))
                              (i32.const 0))
                            (i32.const -1))
                          (i32.const 1))))
                    (local.set $l13
                      (i32.load offset=24
                        (local.get $l2)))
                    (local.set $l14
                      (i32.add
                        (i32.load offset=20
                          (local.get $l2))
                        (i32.const 1)))
                    (i32.store offset=20
                      (local.get $l2)
                      (local.get $l14))
                    (i32.store offset=24
                      (local.get $l2)
                      (i32.and
                        (i32.add
                          (local.get $l13)
                          (local.get $l14))
                        (i32.load offset=32
                          (local.get $l2))))
                    (br $L19)))
                (local.set $l15
                  (i32.shl
                    (i32.and
                      (i32.load offset=24
                        (local.get $l2))
                      (i32.const 15))
                    (i32.const 1)))
                (local.set $l16
                  (i32.xor
                    (i32.shl
                      (i32.const 2)
                      (local.get $l15))
                    (i32.const -1)))
                (local.set $l17
                  (i32.add
                    (i32.load offset=64
                      (local.get $l2))
                    (i32.shl
                      (i32.shr_u
                        (i32.load offset=24
                          (local.get $l2))
                        (i32.const 4))
                      (i32.const 2))))
                (i32.store
                  (local.get $l17)
                  (i32.and
                    (local.get $l16)
                    (i32.load
                      (local.get $l17))))
                (block $B20
                  (block $B21
                    (block $B22
                      (br_if $B22
                        (i32.eqz
                          (i32.and
                            (i32.lt_u
                              (i32.load offset=24
                                (local.get $l2))
                              (i32.load
                                (i32.load offset=72
                                  (local.get $l2))))
                            (i32.const 1))))
                      (br_if $B22
                        (i32.and
                          (i32.shr_u
                            (i32.load
                              (i32.add
                                (i32.load offset=16
                                  (i32.load offset=72
                                    (local.get $l2)))
                                (i32.shl
                                  (i32.shr_u
                                    (i32.load offset=24
                                      (local.get $l2))
                                    (i32.const 4))
                                  (i32.const 2))))
                            (i32.shl
                              (i32.and
                                (i32.load offset=24
                                  (local.get $l2))
                                (i32.const 15))
                              (i32.const 1)))
                          (i32.const 3)))
                      (i64.store offset=8
                        (local.get $l2)
                        (i64.load
                          (i32.add
                            (i32.load offset=20
                              (i32.load offset=72
                                (local.get $l2)))
                            (i32.shl
                              (i32.load offset=24
                                (local.get $l2))
                              (i32.const 3)))))
                      (local.set $l18
                        (i64.load offset=40
                          (local.get $l2)))
                      (i64.store
                        (i32.add
                          (i32.load offset=20
                            (i32.load offset=72
                              (local.get $l2)))
                          (i32.shl
                            (i32.load offset=24
                              (local.get $l2))
                            (i32.const 3)))
                        (local.get $l18))
                      (i64.store offset=40
                        (local.get $l2)
                        (i64.load offset=8
                          (local.get $l2)))
                      (i32.store offset=4
                        (local.get $l2)
                        (i32.load
                          (i32.add
                            (i32.load offset=24
                              (i32.load offset=72
                                (local.get $l2)))
                            (i32.shl
                              (i32.load offset=24
                                (local.get $l2))
                              (i32.const 2)))))
                      (local.set $l19
                        (i32.load offset=36
                          (local.get $l2)))
                      (i32.store
                        (i32.add
                          (i32.load offset=24
                            (i32.load offset=72
                              (local.get $l2)))
                          (i32.shl
                            (i32.load offset=24
                              (local.get $l2))
                            (i32.const 2)))
                        (local.get $l19))
                      (i32.store offset=36
                        (local.get $l2)
                        (i32.load offset=4
                          (local.get $l2)))
                      (local.set $l20
                        (i32.shl
                          (i32.and
                            (i32.load offset=24
                              (local.get $l2))
                            (i32.const 15))
                          (i32.const 1)))
                      (local.set $l21
                        (i32.shl
                          (i32.const 1)
                          (local.get $l20)))
                      (local.set $l22
                        (i32.add
                          (i32.load offset=16
                            (i32.load offset=72
                              (local.get $l2)))
                          (i32.shl
                            (i32.shr_u
                              (i32.load offset=24
                                (local.get $l2))
                              (i32.const 4))
                            (i32.const 2))))
                      (i32.store
                        (local.get $l22)
                        (i32.or
                          (local.get $l21)
                          (i32.load
                            (local.get $l22))))
                      (br $B21))
                    (local.set $l23
                      (i64.load offset=40
                        (local.get $l2)))
                    (i64.store
                      (i32.add
                        (i32.load offset=20
                          (i32.load offset=72
                            (local.get $l2)))
                        (i32.shl
                          (i32.load offset=24
                            (local.get $l2))
                          (i32.const 3)))
                      (local.get $l23))
                    (local.set $l24
                      (i32.load offset=36
                        (local.get $l2)))
                    (i32.store
                      (i32.add
                        (i32.load offset=24
                          (i32.load offset=72
                            (local.get $l2)))
                        (i32.shl
                          (i32.load offset=24
                            (local.get $l2))
                          (i32.const 2)))
                      (local.get $l24))
                    (br $B20))
                  (br $L17))))
            (i32.store offset=60
              (local.get $l2)
              (i32.add
                (i32.load offset=60
                  (local.get $l2))
                (i32.const 1)))
            (br $L15)))
        (block $B23
          (br_if $B23
            (i32.eqz
              (i32.and
                (i32.gt_u
                  (i32.load
                    (i32.load offset=72
                      (local.get $l2)))
                  (i32.load offset=68
                    (local.get $l2)))
                (i32.const 1))))
          (local.set $l25
            (call $realloc
              (i32.load offset=20
                (i32.load offset=72
                  (local.get $l2)))
              (i32.shl
                (i32.load offset=68
                  (local.get $l2))
                (i32.const 3))))
          (i32.store offset=20
            (i32.load offset=72
              (local.get $l2))
            (local.get $l25))
          (local.set $l26
            (call $realloc
              (i32.load offset=24
                (i32.load offset=72
                  (local.get $l2)))
              (i32.shl
                (i32.load offset=68
                  (local.get $l2))
                (i32.const 2))))
          (i32.store offset=24
            (i32.load offset=72
              (local.get $l2))
            (local.get $l26)))
        (call $free
          (i32.load offset=16
            (i32.load offset=72
              (local.get $l2))))
        (local.set $l27
          (i32.load offset=64
            (local.get $l2)))
        (i32.store offset=16
          (i32.load offset=72
            (local.get $l2))
          (local.get $l27))
        (local.set $l28
          (i32.load offset=68
            (local.get $l2)))
        (i32.store
          (i32.load offset=72
            (local.get $l2))
          (local.get $l28))
        (local.set $l29
          (i32.load offset=72
            (local.get $l2)))
        (i32.store offset=8
          (local.get $l29)
          (i32.load offset=4
            (local.get $l29)))
        (local.set $l30
          (i32.trunc_sat_f64_u
            (f64.add
              (f64.mul
                (f64.convert_i32_u
                  (i32.load
                    (i32.load offset=72
                      (local.get $l2))))
                (f64.const 0x1.8a3d70a3d70a4p-1 (;=0.77;)))
              (f64.const 0x1p-1 (;=0.5;)))))
        (i32.store offset=12
          (i32.load offset=72
            (local.get $l2))
          (local.get $l30)))
      (i32.store offset=76
        (local.get $l2)
        (i32.const 0)))
    (local.set $l31
      (i32.load offset=76
        (local.get $l2)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l2)
        (i32.const 80)))
    (return
      (local.get $l31)))
  (func $kh_put_oligonucleotide (type $t8) (param $p0 i32) (param $p1 i64) (param $p2 i32) (result i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i64) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32) (local $l16 i32) (local $l17 i64) (local $l18 i32) (local $l19 i32) (local $l20 i32) (local $l21 i32) (local $l22 i32)
    (local.set $l3
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 48)))
    (global.set $__stack_pointer
      (local.get $l3))
    (i32.store offset=40
      (local.get $l3)
      (local.get $p0))
    (i64.store offset=32
      (local.get $l3)
      (local.get $p1))
    (i32.store offset=28
      (local.get $l3)
      (local.get $p2))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.and
              (i32.ge_u
                (i32.load offset=8
                  (i32.load offset=40
                    (local.get $l3)))
                (i32.load offset=12
                  (i32.load offset=40
                    (local.get $l3))))
              (i32.const 1))))
        (block $B2
          (block $B3
            (br_if $B3
              (i32.eqz
                (i32.and
                  (i32.gt_u
                    (i32.load
                      (i32.load offset=40
                        (local.get $l3)))
                    (i32.shl
                      (i32.load offset=4
                        (i32.load offset=40
                          (local.get $l3)))
                      (i32.const 1)))
                  (i32.const 1))))
            (block $B4
              (br_if $B4
                (i32.eqz
                  (i32.and
                    (i32.lt_s
                      (call $kh_resize_oligonucleotide
                        (i32.load offset=40
                          (local.get $l3))
                        (i32.sub
                          (i32.load
                            (i32.load offset=40
                              (local.get $l3)))
                          (i32.const 1)))
                      (i32.const 0))
                    (i32.const 1))))
              (i32.store
                (i32.load offset=28
                  (local.get $l3))
                (i32.const -1))
              (i32.store offset=44
                (local.get $l3)
                (i32.load
                  (i32.load offset=40
                    (local.get $l3))))
              (br $B0))
            (br $B2))
          (block $B5
            (br_if $B5
              (i32.eqz
                (i32.and
                  (i32.lt_s
                    (call $kh_resize_oligonucleotide
                      (i32.load offset=40
                        (local.get $l3))
                      (i32.add
                        (i32.load
                          (i32.load offset=40
                            (local.get $l3)))
                        (i32.const 1)))
                    (i32.const 0))
                  (i32.const 1))))
            (i32.store
              (i32.load offset=28
                (local.get $l3))
              (i32.const -1))
            (i32.store offset=44
              (local.get $l3)
              (i32.load
                (i32.load offset=40
                  (local.get $l3))))
            (br $B0))))
      (i32.store offset=4
        (local.get $l3)
        (i32.sub
          (i32.load
            (i32.load offset=40
              (local.get $l3)))
          (i32.const 1)))
      (i32.store
        (local.get $l3)
        (i32.const 0))
      (local.set $l4
        (i32.load
          (i32.load offset=40
            (local.get $l3))))
      (i32.store offset=12
        (local.get $l3)
        (local.get $l4))
      (i32.store offset=24
        (local.get $l3)
        (local.get $l4))
      (i32.store offset=20
        (local.get $l3)
        (i32.wrap_i64
          (i64.xor
            (i64.load offset=32
              (local.get $l3))
            (i64.shr_u
              (i64.load offset=32
                (local.get $l3))
              (i64.const 7)))))
      (i32.store offset=16
        (local.get $l3)
        (i32.and
          (i32.load offset=20
            (local.get $l3))
          (i32.load offset=4
            (local.get $l3))))
      (block $B6
        (block $B7
          (br_if $B7
            (i32.eqz
              (i32.and
                (i32.shr_u
                  (i32.load
                    (i32.add
                      (i32.load offset=16
                        (i32.load offset=40
                          (local.get $l3)))
                      (i32.shl
                        (i32.shr_u
                          (i32.load offset=16
                            (local.get $l3))
                          (i32.const 4))
                        (i32.const 2))))
                  (i32.shl
                    (i32.and
                      (i32.load offset=16
                        (local.get $l3))
                      (i32.const 15))
                    (i32.const 1)))
                (i32.const 2))))
          (i32.store offset=24
            (local.get $l3)
            (i32.load offset=16
              (local.get $l3)))
          (br $B6))
        (i32.store offset=8
          (local.get $l3)
          (i32.load offset=16
            (local.get $l3)))
        (loop $L8
          (local.set $l5
            (i32.and
              (i32.shr_u
                (i32.load
                  (i32.add
                    (i32.load offset=16
                      (i32.load offset=40
                        (local.get $l3)))
                    (i32.shl
                      (i32.shr_u
                        (i32.load offset=16
                          (local.get $l3))
                        (i32.const 4))
                      (i32.const 2))))
                (i32.shl
                  (i32.and
                    (i32.load offset=16
                      (local.get $l3))
                    (i32.const 15))
                  (i32.const 1)))
              (i32.const 2)))
          (local.set $l6
            (i32.const 0))
          (block $B9
            (br_if $B9
              (local.get $l5))
            (local.set $l7
              (i32.and
                (i32.shr_u
                  (i32.load
                    (i32.add
                      (i32.load offset=16
                        (i32.load offset=40
                          (local.get $l3)))
                      (i32.shl
                        (i32.shr_u
                          (i32.load offset=16
                            (local.get $l3))
                          (i32.const 4))
                        (i32.const 2))))
                  (i32.shl
                    (i32.and
                      (i32.load offset=16
                        (local.get $l3))
                      (i32.const 15))
                    (i32.const 1)))
                (i32.const 1)))
            (local.set $l8
              (i32.const 1))
            (block $B10
              (br_if $B10
                (local.get $l7))
              (local.set $l8
                (i32.xor
                  (i64.eq
                    (i64.load
                      (i32.add
                        (i32.load offset=20
                          (i32.load offset=40
                            (local.get $l3)))
                        (i32.shl
                          (i32.load offset=16
                            (local.get $l3))
                          (i32.const 3))))
                    (i64.load offset=32
                      (local.get $l3)))
                  (i32.const -1))))
            (local.set $l6
              (local.get $l8)))
          (block $B11
            (br_if $B11
              (i32.eqz
                (i32.and
                  (local.get $l6)
                  (i32.const 1))))
            (block $B12
              (br_if $B12
                (i32.eqz
                  (i32.and
                    (i32.shr_u
                      (i32.load
                        (i32.add
                          (i32.load offset=16
                            (i32.load offset=40
                              (local.get $l3)))
                          (i32.shl
                            (i32.shr_u
                              (i32.load offset=16
                                (local.get $l3))
                              (i32.const 4))
                            (i32.const 2))))
                      (i32.shl
                        (i32.and
                          (i32.load offset=16
                            (local.get $l3))
                          (i32.const 15))
                        (i32.const 1)))
                    (i32.const 1))))
              (i32.store offset=12
                (local.get $l3)
                (i32.load offset=16
                  (local.get $l3))))
            (local.set $l9
              (i32.load offset=16
                (local.get $l3)))
            (local.set $l10
              (i32.add
                (i32.load
                  (local.get $l3))
                (i32.const 1)))
            (i32.store
              (local.get $l3)
              (local.get $l10))
            (i32.store offset=16
              (local.get $l3)
              (i32.and
                (i32.add
                  (local.get $l9)
                  (local.get $l10))
                (i32.load offset=4
                  (local.get $l3))))
            (block $B13
              (br_if $B13
                (i32.eqz
                  (i32.and
                    (i32.eq
                      (i32.load offset=16
                        (local.get $l3))
                      (i32.load offset=8
                        (local.get $l3)))
                    (i32.const 1))))
              (i32.store offset=24
                (local.get $l3)
                (i32.load offset=12
                  (local.get $l3)))
              (br $B11))
            (br $L8)))
        (block $B14
          (br_if $B14
            (i32.eqz
              (i32.and
                (i32.eq
                  (i32.load offset=24
                    (local.get $l3))
                  (i32.load
                    (i32.load offset=40
                      (local.get $l3))))
                (i32.const 1))))
          (block $B15
            (block $B16
              (br_if $B16
                (i32.eqz
                  (i32.and
                    (i32.shr_u
                      (i32.load
                        (i32.add
                          (i32.load offset=16
                            (i32.load offset=40
                              (local.get $l3)))
                          (i32.shl
                            (i32.shr_u
                              (i32.load offset=16
                                (local.get $l3))
                              (i32.const 4))
                            (i32.const 2))))
                      (i32.shl
                        (i32.and
                          (i32.load offset=16
                            (local.get $l3))
                          (i32.const 15))
                        (i32.const 1)))
                    (i32.const 2))))
              (br_if $B16
                (i32.eqz
                  (i32.and
                    (i32.ne
                      (i32.load offset=12
                        (local.get $l3))
                      (i32.load
                        (i32.load offset=40
                          (local.get $l3))))
                    (i32.const 1))))
              (i32.store offset=24
                (local.get $l3)
                (i32.load offset=12
                  (local.get $l3)))
              (br $B15))
            (i32.store offset=24
              (local.get $l3)
              (i32.load offset=16
                (local.get $l3))))))
      (block $B17
        (block $B18
          (br_if $B18
            (i32.eqz
              (i32.and
                (i32.shr_u
                  (i32.load
                    (i32.add
                      (i32.load offset=16
                        (i32.load offset=40
                          (local.get $l3)))
                      (i32.shl
                        (i32.shr_u
                          (i32.load offset=24
                            (local.get $l3))
                          (i32.const 4))
                        (i32.const 2))))
                  (i32.shl
                    (i32.and
                      (i32.load offset=24
                        (local.get $l3))
                      (i32.const 15))
                    (i32.const 1)))
                (i32.const 2))))
          (local.set $l11
            (i64.load offset=32
              (local.get $l3)))
          (i64.store
            (i32.add
              (i32.load offset=20
                (i32.load offset=40
                  (local.get $l3)))
              (i32.shl
                (i32.load offset=24
                  (local.get $l3))
                (i32.const 3)))
            (local.get $l11))
          (local.set $l12
            (i32.shl
              (i32.and
                (i32.load offset=24
                  (local.get $l3))
                (i32.const 15))
              (i32.const 1)))
          (local.set $l13
            (i32.xor
              (i32.shl
                (i32.const 3)
                (local.get $l12))
              (i32.const -1)))
          (local.set $l14
            (i32.add
              (i32.load offset=16
                (i32.load offset=40
                  (local.get $l3)))
              (i32.shl
                (i32.shr_u
                  (i32.load offset=24
                    (local.get $l3))
                  (i32.const 4))
                (i32.const 2))))
          (i32.store
            (local.get $l14)
            (i32.and
              (local.get $l13)
              (i32.load
                (local.get $l14))))
          (local.set $l15
            (i32.load offset=40
              (local.get $l3)))
          (i32.store offset=4
            (local.get $l15)
            (i32.add
              (i32.load offset=4
                (local.get $l15))
              (i32.const 1)))
          (local.set $l16
            (i32.load offset=40
              (local.get $l3)))
          (i32.store offset=8
            (local.get $l16)
            (i32.add
              (i32.load offset=8
                (local.get $l16))
              (i32.const 1)))
          (i32.store
            (i32.load offset=28
              (local.get $l3))
            (i32.const 1))
          (br $B17))
        (block $B19
          (block $B20
            (br_if $B20
              (i32.eqz
                (i32.and
                  (i32.shr_u
                    (i32.load
                      (i32.add
                        (i32.load offset=16
                          (i32.load offset=40
                            (local.get $l3)))
                        (i32.shl
                          (i32.shr_u
                            (i32.load offset=24
                              (local.get $l3))
                            (i32.const 4))
                          (i32.const 2))))
                    (i32.shl
                      (i32.and
                        (i32.load offset=24
                          (local.get $l3))
                        (i32.const 15))
                      (i32.const 1)))
                  (i32.const 1))))
            (local.set $l17
              (i64.load offset=32
                (local.get $l3)))
            (i64.store
              (i32.add
                (i32.load offset=20
                  (i32.load offset=40
                    (local.get $l3)))
                (i32.shl
                  (i32.load offset=24
                    (local.get $l3))
                  (i32.const 3)))
              (local.get $l17))
            (local.set $l18
              (i32.shl
                (i32.and
                  (i32.load offset=24
                    (local.get $l3))
                  (i32.const 15))
                (i32.const 1)))
            (local.set $l19
              (i32.xor
                (i32.shl
                  (i32.const 3)
                  (local.get $l18))
                (i32.const -1)))
            (local.set $l20
              (i32.add
                (i32.load offset=16
                  (i32.load offset=40
                    (local.get $l3)))
                (i32.shl
                  (i32.shr_u
                    (i32.load offset=24
                      (local.get $l3))
                    (i32.const 4))
                  (i32.const 2))))
            (i32.store
              (local.get $l20)
              (i32.and
                (local.get $l19)
                (i32.load
                  (local.get $l20))))
            (local.set $l21
              (i32.load offset=40
                (local.get $l3)))
            (i32.store offset=4
              (local.get $l21)
              (i32.add
                (i32.load offset=4
                  (local.get $l21))
                (i32.const 1)))
            (i32.store
              (i32.load offset=28
                (local.get $l3))
              (i32.const 2))
            (br $B19))
          (i32.store
            (i32.load offset=28
              (local.get $l3))
            (i32.const 0))))
      (i32.store offset=44
        (local.get $l3)
        (i32.load offset=24
          (local.get $l3))))
    (local.set $l22
      (i32.load offset=44
        (local.get $l3)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l3)
        (i32.const 48)))
    (return
      (local.get $l22)))
  (func $kh_del_oligonucleotide (type $t9) (param $p0 i32) (param $p1 i32)
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
    (block $B0
      (br_if $B0
        (i32.eqz
          (i32.and
            (i32.ne
              (i32.load offset=8
                (local.get $l2))
              (i32.load
                (i32.load offset=12
                  (local.get $l2))))
            (i32.const 1))))
      (br_if $B0
        (i32.and
          (i32.shr_u
            (i32.load
              (i32.add
                (i32.load offset=16
                  (i32.load offset=12
                    (local.get $l2)))
                (i32.shl
                  (i32.shr_u
                    (i32.load offset=8
                      (local.get $l2))
                    (i32.const 4))
                  (i32.const 2))))
            (i32.shl
              (i32.and
                (i32.load offset=8
                  (local.get $l2))
                (i32.const 15))
              (i32.const 1)))
          (i32.const 3)))
      (local.set $l3
        (i32.shl
          (i32.and
            (i32.load offset=8
              (local.get $l2))
            (i32.const 15))
          (i32.const 1)))
      (local.set $l4
        (i32.shl
          (i32.const 1)
          (local.get $l3)))
      (local.set $l5
        (i32.add
          (i32.load offset=16
            (i32.load offset=12
              (local.get $l2)))
          (i32.shl
            (i32.shr_u
              (i32.load offset=8
                (local.get $l2))
              (i32.const 4))
            (i32.const 2))))
      (i32.store
        (local.get $l5)
        (i32.or
          (local.get $l4)
          (i32.load
            (local.get $l5))))
      (local.set $l6
        (i32.load offset=12
          (local.get $l2)))
      (i32.store offset=4
        (local.get $l6)
        (i32.add
          (i32.load offset=4
            (local.get $l6))
          (i32.const -1))))
    (return))
  (func $element_Compare (type $t0) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32) (local $l3 i32)
    (local.set $l2
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (i32.store offset=8
      (local.get $l2)
      (local.get $p0))
    (i32.store offset=4
      (local.get $l2)
      (local.get $p1))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.eqz
            (i32.and
              (i32.lt_u
                (i32.load offset=8
                  (i32.load offset=8
                    (local.get $l2)))
                (i32.load offset=8
                  (i32.load offset=4
                    (local.get $l2))))
              (i32.const 1))))
        (i32.store offset=12
          (local.get $l2)
          (i32.const 1))
        (br $B0))
      (block $B2
        (br_if $B2
          (i32.eqz
            (i32.and
              (i32.gt_u
                (i32.load offset=8
                  (i32.load offset=8
                    (local.get $l2)))
                (i32.load offset=8
                  (i32.load offset=4
                    (local.get $l2))))
              (i32.const 1))))
        (i32.store offset=12
          (local.get $l2)
          (i32.const -1))
        (br $B0))
      (local.set $l3
        (i64.gt_u
          (i64.load
            (i32.load offset=8
              (local.get $l2)))
          (i64.load
            (i32.load offset=4
              (local.get $l2)))))
      (i32.store offset=12
        (local.get $l2)
        (select
          (i32.const 1)
          (i32.const -1)
          (i32.and
            (local.get $l3)
            (i32.const 1)))))
    (return
      (i32.load offset=12
        (local.get $l2))))
  (func $generate_Frequencies_For_Desired_Length_Oligonucleotides (type $t3) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32)
    (local $l4 i32) (local $l5 i32) (local $l6 i64) (local $l7 i64) (local $l8 i32) (local $l9 i32) (local $l10 i64) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32) (local $l16 i32) (local $l17 i32) (local $l18 i32) (local $l19 i32) (local $l20 i32) (local $l21 i32) (local $l22 i32) (local $l23 i32)
    (local.set $l4
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 128)))
    (local.set $l5
      (local.get $l4))
    (global.set $__stack_pointer
      (local.get $l4))
    (i32.store offset=124
      (local.get $l5)
      (local.get $p0))
    (i32.store offset=120
      (local.get $l5)
      (local.get $p1))
    (i32.store offset=116
      (local.get $l5)
      (local.get $p2))
    (i32.store offset=112
      (local.get $l5)
      (local.get $p3))
    (i32.store offset=108
      (local.get $l5)
      (call $kh_init_oligonucleotide))
    (i64.store offset=96
      (local.get $l5)
      (i64.const 0))
    (local.set $l6
      (i64.extend_i32_u
        (i32.shl
          (i32.load offset=116
            (local.get $l5))
          (i32.const 1))))
    (i64.store offset=88
      (local.get $l5)
      (i64.sub
        (i64.shl
          (i64.const 1)
          (local.get $l6))
        (i64.const 1)))
    (i32.store offset=84
      (local.get $l5)
      (i32.const 0))
    (block $B0
      (loop $L1
        (br_if $B0
          (i32.eqz
            (i32.and
              (i32.lt_s
                (i32.load offset=84
                  (local.get $l5))
                (i32.sub
                  (i32.load offset=116
                    (local.get $l5))
                  (i32.const 1)))
              (i32.const 1))))
        (local.set $l7
          (i64.and
            (i64.shl
              (i64.load offset=96
                (local.get $l5))
              (i64.const 2))
            (i64.load offset=88
              (local.get $l5))))
        (local.set $l8
          (i32.load8_u
            (i32.add
              (i32.load offset=124
                (local.get $l5))
              (i32.load offset=84
                (local.get $l5)))))
        (local.set $l9
          (i32.const 24))
        (i64.store offset=96
          (local.get $l5)
          (i64.or
            (local.get $l7)
            (i64.extend_i32_s
              (i32.shr_s
                (i32.shl
                  (local.get $l8)
                  (local.get $l9))
                (local.get $l9)))))
        (i32.store offset=84
          (local.get $l5)
          (i32.add
            (i32.load offset=84
              (local.get $l5))
            (i32.const 1)))
        (br $L1)))
    (i32.store offset=80
      (local.get $l5)
      (i32.sub
        (i32.load offset=116
          (local.get $l5))
        (i32.const 1)))
    (block $B2
      (loop $L3
        (br_if $B2
          (i32.eqz
            (i32.and
              (i32.lt_s
                (i32.load offset=80
                  (local.get $l5))
                (i32.load offset=120
                  (local.get $l5)))
              (i32.const 1))))
        (local.set $l10
          (i64.and
            (i64.shl
              (i64.load offset=96
                (local.get $l5))
              (i64.const 2))
            (i64.load offset=88
              (local.get $l5))))
        (local.set $l11
          (i32.load8_u
            (i32.add
              (i32.load offset=124
                (local.get $l5))
              (i32.load offset=80
                (local.get $l5)))))
        (local.set $l12
          (i32.const 24))
        (i64.store offset=96
          (local.get $l5)
          (i64.or
            (local.get $l10)
            (i64.extend_i32_s
              (i32.shr_s
                (i32.shl
                  (local.get $l11)
                  (local.get $l12))
                (local.get $l12)))))
        (i32.store offset=72
          (local.get $l5)
          (call $kh_put_oligonucleotide
            (i32.load offset=108
              (local.get $l5))
            (i64.load offset=96
              (local.get $l5))
            (i32.add
              (local.get $l5)
              (i32.const 76))))
        (block $B4
          (block $B5
            (br_if $B5
              (i32.eqz
                (i32.load offset=76
                  (local.get $l5))))
            (i32.store
              (i32.add
                (i32.load offset=24
                  (i32.load offset=108
                    (local.get $l5)))
                (i32.shl
                  (i32.load offset=72
                    (local.get $l5))
                  (i32.const 2)))
              (i32.const 1))
            (br $B4))
          (local.set $l13
            (i32.add
              (i32.load offset=24
                (i32.load offset=108
                  (local.get $l5)))
              (i32.shl
                (i32.load offset=72
                  (local.get $l5))
                (i32.const 2))))
          (i32.store
            (local.get $l13)
            (i32.add
              (i32.load
                (local.get $l13))
              (i32.const 1))))
        (i32.store offset=80
          (local.get $l5)
          (i32.add
            (i32.load offset=80
              (local.get $l5))
            (i32.const 1)))
        (br $L3)))
    (i32.store offset=68
      (local.get $l5)
      (i32.load offset=4
        (i32.load offset=108
          (local.get $l5))))
    (i32.store offset=64
      (local.get $l5)
      (i32.const 0))
    (i32.store offset=60
      (local.get $l5)
      (call $malloc
        (i32.shl
          (i32.load offset=68
            (local.get $l5))
          (i32.const 4))))
    (i32.store offset=52
      (local.get $l5)
      (i32.const 0))
    (block $B6
      (loop $L7
        (br_if $B6
          (i32.eqz
            (i32.and
              (i32.ne
                (i32.load offset=52
                  (local.get $l5))
                (i32.load
                  (i32.load offset=108
                    (local.get $l5))))
              (i32.const 1))))
        (block $B8
          (block $B9
            (br_if $B9
              (i32.eqz
                (i32.and
                  (i32.shr_u
                    (i32.load
                      (i32.add
                        (i32.load offset=16
                          (i32.load offset=108
                            (local.get $l5)))
                        (i32.shl
                          (i32.shr_u
                            (i32.load offset=52
                              (local.get $l5))
                            (i32.const 4))
                          (i32.const 2))))
                    (i32.shl
                      (i32.and
                        (i32.load offset=52
                          (local.get $l5))
                        (i32.const 15))
                      (i32.const 1)))
                  (i32.const 3))))
            (br $B8))
          (i64.store offset=96
            (local.get $l5)
            (i64.load
              (i32.add
                (i32.load offset=20
                  (i32.load offset=108
                    (local.get $l5)))
                (i32.shl
                  (i32.load offset=52
                    (local.get $l5))
                  (i32.const 3)))))
          (i32.store offset=56
            (local.get $l5)
            (i32.load
              (i32.add
                (i32.load offset=24
                  (i32.load offset=108
                    (local.get $l5)))
                (i32.shl
                  (i32.load offset=52
                    (local.get $l5))
                  (i32.const 2)))))
          (local.set $l14
            (i32.load offset=60
              (local.get $l5)))
          (local.set $l15
            (i32.load offset=64
              (local.get $l5)))
          (i32.store offset=64
            (local.get $l5)
            (i32.add
              (local.get $l15)
              (i32.const 1)))
          (local.set $l16
            (i32.add
              (local.get $l14)
              (i32.shl
                (local.get $l15)
                (i32.const 4))))
          (i64.store offset=32
            (local.get $l5)
            (i64.load offset=96
              (local.get $l5)))
          (i32.store offset=40
            (local.get $l5)
            (i32.load offset=56
              (local.get $l5)))
          (i32.store
            (i32.add
              (i32.add
                (local.get $l5)
                (i32.const 32))
              (i32.const 12))
            (i32.const 0))
          (i64.store
            (local.get $l16)
            (i64.load offset=32
              (local.get $l5)))
          (local.set $l17
            (i32.const 8))
          (i64.store
            (i32.add
              (local.get $l16)
              (local.get $l17))
            (i64.load
              (i32.add
                (local.get $l17)
                (i32.add
                  (local.get $l5)
                  (i32.const 32))))))
        (i32.store offset=52
          (local.get $l5)
          (i32.add
            (i32.load offset=52
              (local.get $l5))
            (i32.const 1)))
        (br $L7)))
    (call $kh_destroy_oligonucleotide
      (i32.load offset=108
        (local.get $l5)))
    (call $qsort
      (i32.load offset=60
        (local.get $l5))
      (i32.load offset=68
        (local.get $l5))
      (i32.const 16)
      (i32.const 1))
    (i32.store offset=28
      (local.get $l5)
      (i32.const 0))
    (i32.store offset=24
      (local.get $l5)
      (i32.const 0))
    (block $B10
      (loop $L11
        (br_if $B10
          (i32.eqz
            (i32.and
              (i32.lt_s
                (i32.load offset=24
                  (local.get $l5))
                (i32.load offset=68
                  (local.get $l5)))
              (i32.const 1))))
        (local.set $l18
          (i32.load offset=116
            (local.get $l5)))
        (local.set $l19
          (i32.add
            (local.get $l18)
            (i32.const 1)))
        (i32.store offset=20
          (local.get $l5)
          (local.get $l4))
        (local.set $l20
          (i32.and
            (i32.add
              (local.get $l18)
              (i32.const 16))
            (i32.const -16)))
        (local.set $l21
          (i32.sub
            (local.get $l4)
            (local.get $l20)))
        (local.set $l4
          (local.get $l21))
        (global.set $__stack_pointer
          (local.get $l4))
        (i32.store offset=16
          (local.get $l5)
          (local.get $l19))
        (i32.store offset=12
          (local.get $l5)
          (i32.sub
            (i32.load offset=116
              (local.get $l5))
            (i32.const 1)))
        (block $B12
          (loop $L13
            (br_if $B12
              (i32.eqz
                (i32.and
                  (i32.gt_s
                    (i32.load offset=12
                      (local.get $l5))
                    (i32.const -1))
                  (i32.const 1))))
            (local.set $l22
              (i32.load8_u offset=1048
                (i32.wrap_i64
                  (i64.and
                    (i64.load
                      (i32.add
                        (i32.load offset=60
                          (local.get $l5))
                        (i32.shl
                          (i32.load offset=24
                            (local.get $l5))
                          (i32.const 4))))
                    (i64.const 3)))))
            (i32.store8
              (i32.add
                (local.get $l21)
                (i32.load offset=12
                  (local.get $l5)))
              (local.get $l22))
            (local.set $l23
              (i32.add
                (i32.load offset=60
                  (local.get $l5))
                (i32.shl
                  (i32.load offset=24
                    (local.get $l5))
                  (i32.const 4))))
            (i64.store
              (local.get $l23)
              (i64.shr_u
                (i64.load
                  (local.get $l23))
                (i64.const 2)))
            (i32.store offset=12
              (local.get $l5)
              (i32.add
                (i32.load offset=12
                  (local.get $l5))
                (i32.const -1)))
            (br $L13)))
        (i32.store8
          (i32.add
            (local.get $l21)
            (i32.load offset=116
              (local.get $l5)))
          (i32.const 0))
        (local.set $l4
          (i32.load offset=20
            (local.get $l5)))
        (i32.store offset=24
          (local.get $l5)
          (i32.add
            (i32.load offset=24
              (local.get $l5))
            (i32.const 1)))
        (br $L11)))
    (call $free
      (i32.load offset=60
        (local.get $l5)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l5)
        (i32.const 128)))
    (return))
  (func $generate_Count_For_Oligonucleotide (type $t3) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32)
    (local $l4 i32) (local $l5 i64) (local $l6 i64) (local $l7 i32) (local $l8 i32) (local $l9 i64) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i64) (local $l14 i32) (local $l15 i32) (local $l16 i32) (local $l17 i32) (local $l18 i32)
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
    (i32.store offset=60
      (local.get $l4)
      (call $strlen
        (i32.load offset=68
          (local.get $l4))))
    (i32.store offset=56
      (local.get $l4)
      (call $kh_init_oligonucleotide))
    (i64.store offset=48
      (local.get $l4)
      (i64.const 0))
    (local.set $l5
      (i64.extend_i32_u
        (i32.shl
          (i32.load offset=60
            (local.get $l4))
          (i32.const 1))))
    (i64.store offset=40
      (local.get $l4)
      (i64.sub
        (i64.shl
          (i64.const 1)
          (local.get $l5))
        (i64.const 1)))
    (i32.store offset=36
      (local.get $l4)
      (i32.const 0))
    (block $B0
      (loop $L1
        (br_if $B0
          (i32.eqz
            (i32.and
              (i32.lt_s
                (i32.load offset=36
                  (local.get $l4))
                (i32.sub
                  (i32.load offset=60
                    (local.get $l4))
                  (i32.const 1)))
              (i32.const 1))))
        (local.set $l6
          (i64.and
            (i64.shl
              (i64.load offset=48
                (local.get $l4))
              (i64.const 2))
            (i64.load offset=40
              (local.get $l4))))
        (local.set $l7
          (i32.load8_u
            (i32.add
              (i32.load offset=76
                (local.get $l4))
              (i32.load offset=36
                (local.get $l4)))))
        (local.set $l8
          (i32.const 24))
        (i64.store offset=48
          (local.get $l4)
          (i64.or
            (local.get $l6)
            (i64.extend_i32_s
              (i32.shr_s
                (i32.shl
                  (local.get $l7)
                  (local.get $l8))
                (local.get $l8)))))
        (i32.store offset=36
          (local.get $l4)
          (i32.add
            (i32.load offset=36
              (local.get $l4))
            (i32.const 1)))
        (br $L1)))
    (i32.store offset=32
      (local.get $l4)
      (i32.sub
        (i32.load offset=60
          (local.get $l4))
        (i32.const 1)))
    (block $B2
      (loop $L3
        (br_if $B2
          (i32.eqz
            (i32.and
              (i32.lt_s
                (i32.load offset=32
                  (local.get $l4))
                (i32.load offset=72
                  (local.get $l4)))
              (i32.const 1))))
        (local.set $l9
          (i64.and
            (i64.shl
              (i64.load offset=48
                (local.get $l4))
              (i64.const 2))
            (i64.load offset=40
              (local.get $l4))))
        (local.set $l10
          (i32.load8_u
            (i32.add
              (i32.load offset=76
                (local.get $l4))
              (i32.load offset=32
                (local.get $l4)))))
        (local.set $l11
          (i32.const 24))
        (i64.store offset=48
          (local.get $l4)
          (i64.or
            (local.get $l9)
            (i64.extend_i32_s
              (i32.shr_s
                (i32.shl
                  (local.get $l10)
                  (local.get $l11))
                (local.get $l11)))))
        (i32.store offset=24
          (local.get $l4)
          (call $kh_put_oligonucleotide
            (i32.load offset=56
              (local.get $l4))
            (i64.load offset=48
              (local.get $l4))
            (i32.add
              (local.get $l4)
              (i32.const 28))))
        (block $B4
          (block $B5
            (br_if $B5
              (i32.eqz
                (i32.load offset=28
                  (local.get $l4))))
            (i32.store
              (i32.add
                (i32.load offset=24
                  (i32.load offset=56
                    (local.get $l4)))
                (i32.shl
                  (i32.load offset=24
                    (local.get $l4))
                  (i32.const 2)))
              (i32.const 1))
            (br $B4))
          (local.set $l12
            (i32.add
              (i32.load offset=24
                (i32.load offset=56
                  (local.get $l4)))
              (i32.shl
                (i32.load offset=24
                  (local.get $l4))
                (i32.const 2))))
          (i32.store
            (local.get $l12)
            (i32.add
              (i32.load
                (local.get $l12))
              (i32.const 1))))
        (i32.store offset=32
          (local.get $l4)
          (i32.add
            (i32.load offset=32
              (local.get $l4))
            (i32.const 1)))
        (br $L3)))
    (i64.store offset=48
      (local.get $l4)
      (i64.const 0))
    (i32.store offset=20
      (local.get $l4)
      (i32.const 0))
    (block $B6
      (loop $L7
        (br_if $B6
          (i32.eqz
            (i32.and
              (i32.lt_s
                (i32.load offset=20
                  (local.get $l4))
                (i32.load offset=60
                  (local.get $l4)))
              (i32.const 1))))
        (local.set $l13
          (i64.shl
            (i64.load offset=48
              (local.get $l4))
            (i64.const 2)))
        (local.set $l14
          (i32.load8_u
            (i32.add
              (i32.load offset=68
                (local.get $l4))
              (i32.load offset=20
                (local.get $l4)))))
        (local.set $l15
          (i32.const 24))
        (local.set $l16
          (i32.load8_u offset=1084
            (i32.and
              (i32.shr_s
                (i32.shl
                  (local.get $l14)
                  (local.get $l15))
                (local.get $l15))
              (i32.const 7))))
        (local.set $l17
          (i32.const 24))
        (i64.store offset=48
          (local.get $l4)
          (i64.or
            (local.get $l13)
            (i64.extend_i32_s
              (i32.shr_s
                (i32.shl
                  (local.get $l16)
                  (local.get $l17))
                (local.get $l17)))))
        (i32.store offset=20
          (local.get $l4)
          (i32.add
            (i32.load offset=20
              (local.get $l4))
            (i32.const 1)))
        (br $L7)))
    (i32.store offset=16
      (local.get $l4)
      (call $kh_get_oligonucleotide
        (i32.load offset=56
          (local.get $l4))
        (i64.load offset=48
          (local.get $l4))))
    (block $B8
      (block $B9
        (br_if $B9
          (i32.eqz
            (i32.and
              (i32.eq
                (i32.load offset=16
                  (local.get $l4))
                (i32.load
                  (i32.load offset=56
                    (local.get $l4))))
              (i32.const 1))))
        (local.set $l18
          (i32.const 0))
        (br $B8))
      (local.set $l18
        (i32.load
          (i32.add
            (i32.load offset=24
              (i32.load offset=56
                (local.get $l4)))
            (i32.shl
              (i32.load offset=16
                (local.get $l4))
              (i32.const 2))))))
    (i64.store offset=8
      (local.get $l4)
      (i64.extend_i32_u
        (local.get $l18)))
    (call $kh_destroy_oligonucleotide
      (i32.load offset=56
        (local.get $l4)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l4)
        (i32.const 80)))
    (return))
  (func $_start (type $t6) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32) (local $l16 i32) (local $l17 i32) (local $l18 i32) (local $l19 i32) (local $l20 i32) (local $l21 i32) (local $l22 i32) (local $l23 i32) (local $l24 i32) (local $l25 i32) (local $l26 i32) (local $l27 i32) (local $l28 i32) (local $l29 i32) (local $l30 i32) (local $l31 i32) (local $l32 i32) (local $l33 i32) (local $l34 i32) (local $l35 i32) (local $l36 i32) (local $l37 i32) (local $l38 i32) (local $l39 i32) (local $l40 i32) (local $l41 i32) (local $l42 i32) (local $l43 i32) (local $l44 i32) (local $l45 i32) (local $l46 i32) (local $l47 i32) (local $l48 i32)
    (local.set $l0
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 32784)))
    (global.set $__stack_pointer
      (local.get $l0))
    (loop $L0
      (local.set $l1
        (i32.add
          (local.get $l0)
          (i32.const 28688)))
      (local.set $l2
        (i32.load
          (i32.const 0)))
      (local.set $l3
        (i32.ne
          (call $fgets
            (local.get $l1)
            (i32.const 4096)
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
        (local.set $l7
          (i32.add
            (local.get $l0)
            (i32.const 28688)))
        (local.set $l6
          (i32.ne
            (call $memcmp
              (i32.const 1072)
              (local.get $l7)
              (i32.const 6))
            (i32.const 0))))
      (block $B2
        (br_if $B2
          (i32.eqz
            (i32.and
              (local.get $l6)
              (i32.const 1))))
        (br $L0)))
    (i32.store offset=28684
      (local.get $l0)
      (i32.const 1048576))
    (i32.store offset=28680
      (local.get $l0)
      (i32.const 0))
    (i32.store offset=28676
      (local.get $l0)
      (call $malloc
        (i32.load offset=28684
          (local.get $l0))))
    (loop $L3
      (local.set $l8
        (i32.add
          (local.get $l0)
          (i32.const 28688)))
      (local.set $l9
        (i32.load
          (i32.const 0)))
      (local.set $l10
        (i32.ne
          (call $fgets
            (local.get $l8)
            (i32.const 4096)
            (local.get $l9))
          (i32.const 0)))
      (local.set $l11
        (i32.const 0))
      (local.set $l12
        (i32.and
          (local.get $l10)
          (i32.const 1)))
      (local.set $l13
        (local.get $l11))
      (block $B4
        (br_if $B4
          (i32.eqz
            (local.get $l12)))
        (local.set $l14
          (i32.load8_u offset=28688
            (local.get $l0)))
        (local.set $l15
          (i32.const 24))
        (local.set $l13
          (i32.ne
            (i32.shr_s
              (i32.shl
                (local.get $l14)
                (local.get $l15))
              (local.get $l15))
            (i32.const 62))))
      (block $B5
        (br_if $B5
          (i32.eqz
            (i32.and
              (local.get $l13)
              (i32.const 1))))
        (i32.store offset=28672
          (local.get $l0)
          (i32.const 0))
        (block $B6
          (loop $L7
            (local.set $l16
              (i32.load8_u
                (i32.add
                  (i32.load offset=28672
                    (local.get $l0))
                  (i32.add
                    (local.get $l0)
                    (i32.const 28688)))))
            (local.set $l17
              (i32.const 24))
            (br_if $B6
              (i32.eqz
                (i32.shr_s
                  (i32.shl
                    (local.get $l16)
                    (local.get $l17))
                  (local.get $l17))))
            (local.set $l18
              (i32.load8_u
                (i32.add
                  (i32.load offset=28672
                    (local.get $l0))
                  (i32.add
                    (local.get $l0)
                    (i32.const 28688)))))
            (local.set $l19
              (i32.const 24))
            (block $B8
              (br_if $B8
                (i32.eqz
                  (i32.and
                    (i32.ne
                      (i32.shr_s
                        (i32.shl
                          (local.get $l18)
                          (local.get $l19))
                        (local.get $l19))
                      (i32.const 10))
                    (i32.const 1))))
              (local.set $l20
                (i32.load8_u
                  (i32.add
                    (i32.load offset=28672
                      (local.get $l0))
                    (i32.add
                      (local.get $l0)
                      (i32.const 28688)))))
              (local.set $l21
                (i32.const 24))
              (local.set $l22
                (i32.load8_u offset=1084
                  (i32.and
                    (i32.shr_s
                      (i32.shl
                        (local.get $l20)
                        (local.get $l21))
                      (local.get $l21))
                    (i32.const 7))))
              (local.set $l23
                (i32.load offset=28676
                  (local.get $l0)))
              (local.set $l24
                (i32.load offset=28680
                  (local.get $l0)))
              (i32.store offset=28680
                (local.get $l0)
                (i32.add
                  (local.get $l24)
                  (i32.const 1)))
              (i32.store8
                (i32.add
                  (local.get $l23)
                  (local.get $l24))
                (local.get $l22)))
            (i32.store offset=28672
              (local.get $l0)
              (i32.add
                (i32.load offset=28672
                  (local.get $l0))
                (i32.const 1)))
            (br $L7)))
        (block $B9
          (br_if $B9
            (i32.eqz
              (i32.and
                (i32.lt_u
                  (i32.sub
                    (i32.load offset=28684
                      (local.get $l0))
                    (i32.load offset=28680
                      (local.get $l0)))
                  (i32.const 4096))
                (i32.const 1))))
          (local.set $l25
            (i32.load offset=28676
              (local.get $l0)))
          (local.set $l26
            (i32.shl
              (i32.load offset=28684
                (local.get $l0))
              (i32.const 1)))
          (i32.store offset=28684
            (local.get $l0)
            (local.get $l26))
          (i32.store offset=28676
            (local.get $l0)
            (call $realloc
              (local.get $l25)
              (local.get $l26))))
        (br $L3)))
    (i32.store offset=28676
      (local.get $l0)
      (call $realloc
        (i32.load offset=28676
          (local.get $l0))
        (i32.load offset=28680
          (local.get $l0))))
    (local.set $l27
      (i32.load offset=28676
        (local.get $l0)))
    (local.set $l28
      (i32.load offset=28680
        (local.get $l0)))
    (local.set $l29
      (i32.add
        (local.get $l0)
        (i32.const 24576)))
    (call $generate_Count_For_Oligonucleotide
      (local.get $l27)
      (local.get $l28)
      (i32.const 1053)
      (local.get $l29))
    (local.set $l30
      (i32.load offset=28676
        (local.get $l0)))
    (local.set $l31
      (i32.load offset=28680
        (local.get $l0)))
    (local.set $l32
      (i32.add
        (local.get $l0)
        (i32.const 20480)))
    (call $generate_Count_For_Oligonucleotide
      (local.get $l30)
      (local.get $l31)
      (i32.const 1031)
      (local.get $l32))
    (local.set $l33
      (i32.load offset=28676
        (local.get $l0)))
    (local.set $l34
      (i32.load offset=28680
        (local.get $l0)))
    (local.set $l35
      (i32.add
        (local.get $l0)
        (i32.const 16384)))
    (call $generate_Count_For_Oligonucleotide
      (local.get $l33)
      (local.get $l34)
      (i32.const 1024)
      (local.get $l35))
    (local.set $l36
      (i32.load offset=28676
        (local.get $l0)))
    (local.set $l37
      (i32.load offset=28680
        (local.get $l0)))
    (local.set $l38
      (i32.add
        (local.get $l0)
        (i32.const 12288)))
    (call $generate_Count_For_Oligonucleotide
      (local.get $l36)
      (local.get $l37)
      (i32.const 1079)
      (local.get $l38))
    (local.set $l39
      (i32.load offset=28676
        (local.get $l0)))
    (local.set $l40
      (i32.load offset=28680
        (local.get $l0)))
    (local.set $l41
      (i32.add
        (local.get $l0)
        (i32.const 8192)))
    (call $generate_Count_For_Oligonucleotide
      (local.get $l39)
      (local.get $l40)
      (i32.const 1044)
      (local.get $l41))
    (local.set $l42
      (i32.load offset=28676
        (local.get $l0)))
    (local.set $l43
      (i32.load offset=28680
        (local.get $l0)))
    (local.set $l44
      (i32.add
        (local.get $l0)
        (i32.const 4096)))
    (call $generate_Frequencies_For_Desired_Length_Oligonucleotides
      (local.get $l42)
      (local.get $l43)
      (i32.const 2)
      (local.get $l44))
    (local.set $l45
      (i32.load offset=28676
        (local.get $l0)))
    (local.set $l46
      (i32.load offset=28680
        (local.get $l0)))
    (local.set $l47
      (local.get $l0))
    (call $generate_Frequencies_For_Desired_Length_Oligonucleotides
      (local.get $l45)
      (local.get $l46)
      (i32.const 1)
      (local.get $l47))
    (call $free
      (i32.load offset=28676
        (local.get $l0)))
    (local.set $l48
      (i32.const 0))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l0)
        (i32.const 32784)))
    (return
      (local.get $l48)))
  (table $__indirect_function_table 2 2 funcref)
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66640))
  (global $__dso_handle i32 (i32.const 1024))
  (global $__data_end i32 (i32.const 1093))
  (global $__stack_low i32 (i32.const 1104))
  (global $__stack_high i32 (i32.const 66640))
  (global $__global_base i32 (i32.const 1024))
  (global $__heap_base i32 (i32.const 66640))
  (global $__heap_end i32 (i32.const 131072))
  (global $__memory_base i32 (i32.const 0))
  (global $__table_base i32 (i32.const 1))
  (global $__wasm_first_page_end i32 (i32.const 65536))
  (export "memory" (memory $memory))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "kh_init_oligonucleotide" (func $kh_init_oligonucleotide))
  (export "kh_destroy_oligonucleotide" (func $kh_destroy_oligonucleotide))
  (export "kh_clear_oligonucleotide" (func $kh_clear_oligonucleotide))
  (export "kh_get_oligonucleotide" (func $kh_get_oligonucleotide))
  (export "kh_resize_oligonucleotide" (func $kh_resize_oligonucleotide))
  (export "kh_put_oligonucleotide" (func $kh_put_oligonucleotide))
  (export "kh_del_oligonucleotide" (func $kh_del_oligonucleotide))
  (export "element_Compare" (func $element_Compare))
  (export "generate_Frequencies_For_Desired_Length_Oligonucleotides" (func $generate_Frequencies_For_Desired_Length_Oligonucleotides))
  (export "generate_Count_For_Oligonucleotide" (func $generate_Count_For_Oligonucleotide))
  (export "_start" (func $_start))
  (export "__indirect_function_table" (table $__indirect_function_table))
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
  (elem $e0 (i32.const 1) func $element_Compare)
  (data $.rodata (i32.const 1024) "GGTATT\00GGTATTTTAATT\00GGT\00ACGT\00GGTATTTTAATTTATAGT\00>THREE\00GGTA\00 \00 \01\03  \02\00")
  (@custom ".debug_loc" "\ff\ff\ff\ff\bb\0e\00\004\03\00\00\c0\03\00\00\03\00\ed\00\15\00\00\00\00\00\00\00\00")
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01U\17\00\00\024\00\03\0eI\13:\0b;\0b\1c\0f\00\00\03&\00I\13\00\00\04$\00\03\0e>\0b\0b\0b\00\00\054\00I\13:\0b;\0b\02\18\00\00\06\01\01I\13\00\00\07!\00I\137\0b\00\00\08$\00\03\0e\0b\0b>\0b\00\00\09\0f\00I\13\00\00\0a\16\00I\13\03\0e:\0b;\0b\00\00\0b\13\01\03\0e\0b\0b:\0b;\0b\00\00\0c\0d\00\03\0eI\13:\0b;\0b8\0b\00\00\0d\0f\00\00\00\0e\15\01I\13'\19\00\00\0f\05\00I\13\00\00\10&\00\00\00\11.\00\11\01\12\06@\18\03\0e:\0b;\0b'\19I\13?\19\00\00\12.\01\11\01\12\06@\18\03\0e:\0b;\0b'\19?\19\00\00\13\05\00\02\18\03\0e:\0b;\0bI\13\00\00\14.\01\11\01\12\06@\18\03\0e:\0b;\0b'\19I\13?\19\00\00\15\0b\01\11\01\12\06\00\00\164\00\02\18\03\0e:\0b;\0bI\13\00\00\17\0b\01U\17\00\00\184\00\02\18\03\0eI\134\19\00\00\194\00\02\17\03\0e:\0b;\0bI\13\00\00\1a.\01\11\01\12\06@\18\03\0e:\0b;\0bI\13?\19\00\00\1b\13\01\0b\0b:\0b;\0b\00\00\1c!\00I\137\13\00\00\1d!\00I\137\05\00\00\00")
  (@custom ".debug_info" "\02\09\00\00\04\00\00\00\00\00\04\01\07\04\00\00\1d\00\cd\03\00\00\00\00\00\00\b0\01\00\00\00\00\00\00\18\00\00\00\02\f7\03\00\00:\00\00\00\01\be\a4\e1\f5\d1\f0\fa\a8\f4?\03?\00\00\00\04\b4\02\00\00\04\08\05S\00\00\00\02s\05\03\18\04\00\00\06_\00\00\00\07f\00\00\00\05\00\04\92\01\00\00\06\01\08\e3\03\00\00\08\07\05z\00\00\00\02\a9\05\03<\04\00\00\06_\00\00\00\07f\00\00\00\09\00\05\93\00\00\00\02\b8\05\030\04\00\00\06_\00\00\00\07f\00\00\00\07\00\05\ac\00\00\00\02\d8\05\03\1d\04\00\00\06_\00\00\00\07f\00\00\00\13\00\05\c5\00\00\00\02\db\05\03\07\04\00\00\06_\00\00\00\07f\00\00\00\0d\00\05\93\00\00\00\02\de\05\03\00\04\00\00\05S\00\00\00\02\e1\05\037\04\00\00\05\f8\00\00\00\02\e4\05\03\14\04\00\00\06_\00\00\00\07f\00\00\00\04\00\09\09\01\00\00\0a\14\01\00\00\ac\00\00\00\02\14\0bh\01\00\00\1c\02\14\0c\ee\00\00\00q\01\00\00\02\14\00\0c\80\02\00\00q\01\00\00\02\14\04\0c\c2\03\00\00q\01\00\00\02\14\08\0c\a3\03\00\00q\01\00\00\02\14\0c\0c\05\01\00\00\8e\01\00\00\02\14\10\0c\e5\00\00\00\93\01\00\00\02\14\14\0c\fc\00\00\00\b5\01\00\00\02\14\18\00\0a|\01\00\00\86\00\00\00\01\a0\0a\87\01\00\00\d7\00\00\00\01\85\04F\00\00\00\07\04\09|\01\00\00\09\98\01\00\00\0a\a3\01\00\00\c3\00\00\00\03A\0a\ae\01\00\00\c1\00\00\00\03\1d\04m\02\00\00\07\08\09\ba\01\00\00\0a\c5\01\00\00\ce\00\00\00\03@\0a\87\01\00\00\cc\00\00\00\03\17\0d\09\d6\01\00\00\0e\e6\01\00\00\0f\ed\01\00\00\0f\ed\01\00\00\00\04O\00\00\00\05\04\09\f2\01\00\00\10\11\05\00\00\00\0d\00\00\00\07\ed\03\00\00\00\00\9f\07\03\00\00\02\14\04\01\00\00\12\13\00\00\00p\00\00\00\04\ed\00\01\9f\d5\02\00\00\02\14\13\02\91\0ck\02\00\00\02\14\04\01\00\00\00\12\85\00\00\00\9e\00\00\00\04\ed\00\01\9f6\03\00\00\02\14\13\02\91\0ck\02\00\00\02\14\04\01\00\00\00\14%\01\00\00\af\01\00\00\04\ed\00\02\9f\1f\03\00\00\02\14q\01\00\00\13\02\91(k\02\00\00\02\14'\08\00\00\13\02\91 \18\00\00\00\02\14\98\01\00\00\15P\01\00\00t\01\00\00\16\02\91\1c8\02\00\00\02\14q\01\00\00\16\02\91\18>\02\00\00\02\14q\01\00\00\16\02\91\144\00\00\00\02\14q\01\00\00\16\02\91\105\02\00\00\02\14q\01\00\00\16\02\91\0c\9b\01\00\00\02\14q\01\00\00\00\00\14\d6\02\00\00\16\06\00\00\04\ed\00\02\9ff\03\00\00\02\14\e6\01\00\00\13\03\91\c8\00k\02\00\00\02\14\04\01\00\00\13\03\91\c4\00\ea\00\00\00\02\14q\01\00\00\16\03\91\c0\00\01\01\00\00\02\14\8e\01\00\00\16\02\91<:\02\00\00\02\14q\01\00\00\15\88\04\00\00\a0\00\00\00\16\02\918\e1\00\00\00\02\14\93\01\00\00\15\d8\04\00\00P\00\00\00\16\02\914\f8\00\00\00\02\14\b5\01\00\00\00\00\15}\05\00\00v\02\00\00\16\02\91(\18\00\00\00\02\14\98\01\00\00\16\02\91$-\02\00\00\02\14\ba\01\00\00\16\02\91 1\02\00\00\02\14q\01\00\00\17\00\00\00\00\16\02\91\1c8\02\00\00\02\14q\01\00\00\16\02\91\18>\02\00\00\02\14q\01\00\00\16\02\91\14\9b\01\00\00\02\14q\01\00\00\15\f8\06\00\00@\00\00\00\16\02\91\08\97\01\00\00\02\14\98\01\00\00\00\158\07\00\00@\00\00\00\16\02\91\04\97\01\00\00\02\14\ba\01\00\00\00\00\00\00\14\ee\08\00\00\91\04\00\00\04\ed\00\03\9f\f0\02\00\00\02\14q\01\00\00\13\02\91(k\02\00\00\02\14\04\01\00\00\13\02\91 \18\00\00\00\02\14\98\01\00\00\13\02\91\1cv\00\00\00\02\141\08\00\00\16\02\91\18+\00\00\00\02\14q\01\00\00\15\d5\09\00\00\1c\02\00\00\16\02\91\148\02\00\00\02\14q\01\00\00\16\02\91\10>\02\00\00\02\14q\01\00\00\16\02\91\0c\9f\02\00\00\02\14q\01\00\00\16\02\91\084\00\00\00\02\14q\01\00\00\16\02\91\045\02\00\00\02\14q\01\00\00\16\02\91\00\9b\01\00\00\02\14q\01\00\00\00\00\12\81\0d\00\00\a8\00\00\00\04\ed\00\02\9fO\03\00\00\02\14\13\02\91\0ck\02\00\00\02\14\04\01\00\00\13\02\91\08+\00\00\00\02\14q\01\00\00\00\14+\0e\00\00\8e\00\00\00\04\ed\00\02\9f\a4\02\00\00\02/\e6\01\00\00\13\02\91\08i\00\00\00\02/6\08\00\00\13\02\91\04[\00\00\00\0206\08\00\00\00\12\bb\0e\00\00\ea\03\00\00\04\ed\00\05\9f/\01\00\00\02>\13\03\91\fc\00\c6\02\00\00\02?m\08\00\00\13\03\91\f8\00@\02\00\00\02?|\08\00\00\13\03\91\f4\00\0b\01\00\00\02@|\08\00\00\13\03\91\f0\00-\00\00\00\02@\97\08\00\00\16\03\91\ec\00\bb\02\00\00\02B\04\01\00\00\16\03\91\e0\00\18\00\00\00\02D\98\01\00\00\16\03\91\d8\005\02\00\00\02E\a1\08\00\00\16\03\91\c4\00\85\02\00\00\02a\81\08\00\00\16\03\91\c0\00>\02\00\00\02a\81\08\00\00\16\02\91<\1c\00\00\00\02b\b6\08\00\00\16\02\918\99\02\00\00\02c\ba\01\00\00\15$\0f\00\00e\00\00\00\16\03\91\d4\00>\02\00\00\02I\81\08\00\00\00\15\89\0f\00\00\c9\00\00\00\16\03\91\d0\00>\02\00\00\02O\81\08\00\00\15\ab\0f\00\00\95\00\00\00\16\03\91\cc\00\af\03\00\00\02T\e6\01\00\00\16\03\91\c8\008\02\00\00\02U\a6\08\00\00\00\00\15y\10\00\00\f7\00\00\00\16\02\914<\02\00\00\02dq\01\00\00\00\15\93\11\00\00\f9\00\00\00\16\02\91\1c\a0\01\00\00\02n\81\08\00\00\16\02\91\18>\02\00\00\02n\81\08\00\00\15\b6\11\00\00\be\00\00\00\18\02\91\10\1c\04\00\00\87\01\00\00\19\00\00\00\00p\03\00\00\02q\bb\08\00\00\15\ef\11\00\00x\00\00\00\16\02\91\0c:\02\00\00\02r\81\08\00\00\00\00\00\00\12\a7\12\00\00\8d\02\00\00\04\ed\00\04\9f\80\03\00\00\02\85\13\03\91\cc\00\c6\02\00\00\02\86m\08\00\00\13\03\91\c8\00@\02\00\00\02\86|\08\00\00\13\03\91\c4\00p\03\00\00\02\87m\08\00\00\13\03\91\c0\00-\00\00\00\02\87\97\08\00\00\16\02\91<V\02\00\00\02\88|\08\00\00\16\02\918\bb\02\00\00\02\8a\ca\08\00\00\16\02\910\18\00\00\00\02\8c\98\01\00\00\16\02\91(5\02\00\00\02\8d\a1\08\00\00\16\02\91\108\02\00\00\02\ac\ab\08\00\00\16\02\91\08@\00\00\00\02\ad\cf\08\00\00\15 \13\00\00e\00\00\00\16\02\91$>\02\00\00\02\91\81\08\00\00\00\15\85\13\00\00\c8\00\00\00\16\02\91 >\02\00\00\02\96\81\08\00\00\15\a7\13\00\00\94\00\00\00\16\02\91\1c\af\03\00\00\02\9a\e6\01\00\00\16\02\91\188\02\00\00\02\9b\a6\08\00\00\00\00\15T\14\00\00t\00\00\00\16\02\91\14>\02\00\00\02\a8\81\08\00\00\00\00\1a6\15\00\00\8d\03\00\00\04\ed\00\00\9f9\00\00\00\02\b4\e6\01\00\00\16\04\91\90\e0\01}\01\00\00\02\b5\e5\08\00\00\16\04\91\8c\e0\01\00\00\00\00\02\bd\81\08\00\00\16\04\91\88\e0\01@\02\00\00\02\be\81\08\00\00\16\04\91\84\e0\01\c6\02\00\00\02\bf\9c\08\00\00\16\02\91\00\84\01\00\00\02\d1\f2\08\00\00\15E\16\00\00\c5\00\00\00\16\04\91\80\e0\01>\02\00\00\02\c3\81\08\00\00\00\00\09,\08\00\00\03\09\01\00\00\09\e6\01\00\00\03;\08\00\00\09@\08\00\00\03E\08\00\00\0aP\08\00\00S\00\00\00\02\1d\1b\10\02\1a\0c\18\00\00\00\98\01\00\00\02\1b\00\0c\99\02\00\00\ba\01\00\00\02\1c\08\00\03r\08\00\00\09w\08\00\00\03_\00\00\00\03\81\08\00\00\0a\8c\08\00\00\a0\00\00\00\02\18\0a\e6\01\00\00\8e\00\00\00\03p\03\9c\08\00\00\09_\00\00\00\03\98\01\00\00\03\ab\08\00\00\0aq\01\00\00\97\00\00\00\01\a1\09E\08\00\00\06_\00\00\00\1cf\00\00\00]\06\00\00\00\03\04\01\00\00\0a\da\08\00\00|\00\00\00\03x\0a\ae\01\00\00z\00\00\00\036\06_\00\00\00\1df\00\00\00\00\10\00\06_\00\00\00\07f\00\00\00\07\1df\00\00\00\00\10\00\00")
  (@custom ".debug_ranges" "\f3\05\00\00\ee\07\00\00\f2\07\00\00\f3\07\00\00\00\00\00\00\00\00\00\00\05\00\00\00\12\00\00\00\13\00\00\00\83\00\00\00\85\00\00\00#\01\00\00%\01\00\00\d4\02\00\00\d6\02\00\00\ec\08\00\00\ee\08\00\00\7f\0d\00\00\81\0d\00\00)\0e\00\00+\0e\00\00\b9\0e\00\00\bb\0e\00\00\a5\12\00\00\a7\12\00\004\15\00\006\15\00\00\c3\18\00\00\00\00\00\00\00\00\00\00")
  (@custom ".debug_str" "polynucleotide_Capacity\00key\00elements_Array\00x\00output\00last\00_start\00count\00unsigned int\00element\00right_Element\00left_Element\00ret\00__uintmax_t\00khint_t\00intptr_t\00khiter_t\00intnative_t\00kh_oligonucleotide_t\00__uint64_t\00__uint32_t\00khint32_t\00new_keys\00new_n_buckets\00new_vals\00new_flags\00desired_Length_For_Oligonucleotides\00generate_Frequencies_For_Desired_Length_Oligonucleotides\00kh_oligonucleotide_s\00buffer\00output_Buffer\00char\00tmp\00step\00output_Position\00/home/sve70582/Documents/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/o0_debug_export_all\00val\00new_mask\00j\00__i\00polynucleotide_Length\00oligonucleotide_Length\00unsigned long long\00size\00elements_Array_Size\00value\00site\00element_Compare\00double\00hash_Table\00polynucleotide\00kh_destroy_oligonucleotide\00kh_put_oligonucleotide\00kh_init_oligonucleotide\00kh_get_oligonucleotide\00kh_clear_oligonucleotide\00kh_del_oligonucleotide\00kh_resize_oligonucleotide\00generate_Count_For_Oligonucleotide\00upper_bound\00element_Was_Unused\00n_occupied\00../src/k-nucleotide.c\00__ARRAY_SIZE_TYPE__\00__ac_HASH_UPPER\00clang version 21.1.7\00__vla_expr0\00")
  (@custom ".debug_line" "L\07\00\00\04\00Q\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01../src\00../src/../..\00\00khash.h\00\01\00\00k-nucleotide.c\00\01\00\00stdlib.h\00\02\00\00\00\04\02\05\01\0a\00\05\02\06\00\00\00\03\13\01\02\0c\00\01\01\04\02\00\05\02\13\00\00\00\03\13\01\05\01\0a\08\ba\06\f2\03l\025\01\03\14 \02\0d\00\01\01\04\02\00\05\02\85\00\00\00\03\13\01\05\01\0a\08J\06\08\f2\03l\02%\01\03\14 \03l\c8\03\14 \03l\08\ac\03\14 \03l\08<\03\14 \02\02\00\01\01\04\02\00\05\02%\01\00\00\03\13\01\05\01\0a\08\ac\06\e4\03l\02\ca\01\01\06\03\14\9e\06f\02(\12\03l\02\22\01\03\14 \03l.\03\14 \03l\02:\01\03\14 \03lt\03\14 \03l\90\03\14 \03lt\03\14 \02\07\00\01\01\04\02\00\05\02\d6\02\00\00\03\13\01\05\01\0a\02-\12\06\d6\02y\12\03l\08f\03\14 \03l\02:\01\03\14 \03l\08t\03\14 \03l\9e\03\14 \f2\03l\08t\03\14 \03l\08\d6\03\14 \03l\9e\03\14 \03l\08\ac\03\14 \08X\08\9e\e4\03l\08<\03\14 \08\12\08\9e\e4\03l\08<\03\14 \03l\08\12\06\03\14.\06\9et\08 f\02*\12\02&\12\08\82\027\12\02\c3\01\12\02B\12\02@\12\02@\12\03l\029\01\03\14 \08\ba\08\ba\03l.\03\14 \03l.\03\14 \03l \06\03\14 \06\e4.\08X\02\22\12\03l\02\22\01\03\14 \03l\02l\01\03\14 \03lt\03\14 \02\17\00\01\01\04\02\00\05\02\ee\08\00\00\03\13\01\05\01\0a\023\12\06\08\9e\08\c8\08\f2\03l\08\82\03\14 \03l.\03\14 \08\f2\03l\08\82\06\03\14<\06\02S\12\03l\029\01\03\14 \03l\02\8f\01\01\06\03\14\9e\06f\03l\025\01\03\14 \02(\12\08.\03l\ba\03\14 \03l.\03\14  \08X\03l\02M\01\03\14 \03l\9e\06\03\14<\06\02-\12\03l\02\8b\01\01\03\14 \02-\12\03l\02w\01\03\14 \03l\9e\06\03\14.\06\03l\9e\03\14 \02\16\00\01\01\04\02\00\05\02\81\0d\00\00\03\13\01\05\01\0a\08\ac\06\02>\12\03l\02K\01\03\14 \02\02\00\01\01\04\02\00\05\02+\0e\00\00\03/\01\05\05\0a\08\af\05\13\06\90\05\1b<\05*X\05\19<\051t\03M\90\05\05\06\034 \05\13\06t\05\1b<\05*X\05\19<\051t\03L\90\05\09\06\038 \05\17\06X\05\1d<\05,X\05\1b<\05\09<\05\02\ba\03H<\05\01\06\039 \02\07\00\01\01\04\02\00\05\02\bb\0e\00\00\03?\01\05(\0a\02?\14\05\1d\06\82\05\0b\06>\05&u\05%\06X\05\22<\05J\9e\05\11<\05\12\06@\05\17\06t\05\19\90\05<X\05\18<\05\02 \05\08\06g\05\0b\06X\05\11<\05\0fX\05\19<\05(X\05\19X\05\17\08f\05\06 \05A\06;\05\02\06\c8.\05\14\064\057\06t\05\12<\05\06\06=\05\08\06\90\05\07X\05\02\06\1f\05\08i\05\0b\06X\05\11<\05\0fX\05\19<\05(X\05\19X\05\17\08f\05\06 \05\14\06?\05\12\06\08t\05\06\06A\05\04\bb\05\1b\06\08\12\05\04X\03\a5\7f.\06\03\dd\00 \05\1b\06\08.\03\a3\7f\c8\05 \06\03\d0\00 \05\02\c7\06.\05\22\06\03\12.\05\0e\06\9e\057<\05\22\06u\055\06t\05\1b<\05\0cf\05\02\06>\06t\08 f\03\9c\7f\02/\01\03\e4\00 \03\9c\7f\02\96\01\01\03\e4\00 \e4\061\05\08\af\05\18\06X\05\02X\05\12\06\da\05%\06t\05*t\05,\90\05+X\05\02 \05\18\06i\05;\06\ac\05\03<\05\15\06\02+\13\058\06t\05\13<\05<<\05=\90\05\03<\05\17\06g\05\14\06\08\c8\05\04t\05\16 \05\04\06Y\05\13\06X\05\04X\05\19f\05C\06\c6\05\03\06\c8.\05\13\062\05\03\06t\057 \05\02\06_\05B\03qt\05\02\06\c8.\05\07\06\03\11.\05\02\06X\05\01\06g\02\0e\00\01\01\04\02\00\05\02\a7\12\00\00\03\86\01\01\052\0a\02?\13\05+\06t\05\14f\05.\06>\05#\06\82\05\0b\06>\05&u\05%\06X\05\22<\05=\9e\05\11<\05\12\06@\05\17\06t\05\19\90\05/X\05\18<\05\02 \05\08\06g\05\0b\06X\05\11<\05\0fX\05\19<\05(X\05\19X\05\17\08f\05\06 \054\06;\05\02\06\c8.\05\14\063\05*\06t\05\12<\05.<\050\90\05/X\05\02 \05\08\06h\05\0b\06X\05\11<\05\0fX\05\19<\05(X\05\19X\05\17\08f\05\06 \05\14\06?\05\12\06\08f\05\06\06A\05\04\bb\05\1b\06\08\12\05\04X\03\df~.\06\03\a3\01 \05\1b\06\08.\03\dd~\c8\05H\06\03\96\01 \05\02\06\c8.\05\05\06\03\11.\05\12u\05\17\06t\05\19\90\05\18X\05\02 \05\08\06g\05\0b\06X\05\12X\05\10\029\12\05\06 \052\06;\05\02\06\c8.\05\0d\062\05\0b\06\08 \05\12\06=\05\15\06\90\05\13\82\05\12 \03\d3~\ba\05.\03\ad\01 \03\d3~\08X\05\12\03\ad\01 \05\0cX\05\02\06?\05\01\ad\02\0e\00\01\01\04\02\00\05\026\15\00\00\03\b3\01\01\05\0e\0a\08x\05&\06\ac\05\08\ac\05-\08 \05A\08X\050\90\05-\f2\03\c8~X\05\02\03\b8\01X\03\c8~\82\03\b8\01 \05\0e\06%\bb\05\1f\91\05\18\06\90\05\09f\05\0e\06[\05&\06\ac\05\08\ac\05-\08 \050\08X\059\08J\03\be~X\05\02\03\c2\01X\05\13\06g\05\1f\06\90\05\18\ac\05\03\08\82\05\0e\06=\05\07\06t\05\10\08\9e\05\08\06\92\05\05\02,\11\05)\06\90\05\05\08J\05\07\06Y\06\03\ba~X\05*\06\03\c3\01 \05\03\06\08\12.\05\06\065\05\1e\06\90\05\1dt\053 \05\1b\06\9f\05B\06\90\05\13\08J\05\12\ba\03\b5~X\05\02\06\03\c2\01 \06\03\be~.\03\c2\01 \05\19\06\03\0d \05)\06\90\05\11t\05\10f\05&\06`\05\07\91\054\06\90\05\03\06\8f\05&\08#\05\07\91\05.\06\90\05\03\06\8f\05&\08#\05\07\91\05(\06\90\05\03\06\8f\05&\08#\05\07\91\05&\06\90\05\03\06\8f\05&\08#\05\07\91\05%\06\90\05\03\06\8f\05<\08$\05\07\91\05!\06\90\05\03\06\81\05<\d9\05\07\91\05!\06\90\05\03\06I\05\07\dd\05\02\06t\06h\02\15\00\01\01")
  (@custom "name" "\00\12\11k-nucleotide.wasm\01\fa\02\14\00\06calloc\01\04free\02\06malloc\03\07realloc\04\05qsort\05\06strlen\06\05fgets\07\06memcmp\08\11__wasm_call_ctors\09\17kh_init_oligonucleotide\0a\1akh_destroy_oligonucleotide\0b\18kh_clear_oligonucleotide\0c\16kh_get_oligonucleotide\0d\19kh_resize_oligonucleotide\0e\16kh_put_oligonucleotide\0f\16kh_del_oligonucleotide\10\0felement_Compare\118generate_Frequencies_For_Desired_Length_Oligonucleotides\12\22generate_Count_For_Oligonucleotide\13\06_start\07\12\01\00\0f__stack_pointer\09\0a\01\00\07.rodata")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
