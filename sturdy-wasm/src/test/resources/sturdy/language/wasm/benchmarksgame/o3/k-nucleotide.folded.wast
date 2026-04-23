(module $k-nucleotide.wasm
  (type $t0 (func (param i32 i32) (result i32)))
  (type $t1 (func (param i32)))
  (type $t2 (func (param i32) (result i32)))
  (type $t3 (func (param i32 i32 i32 i32)))
  (type $t4 (func (param i32 i32 i32) (result i32)))
  (type $t5 (func (param i32 i64 i32) (result i32)))
  (type $t6 (func (result i32)))
  (import "env" "calloc" (func $calloc (type $t0)))
  (import "env" "free" (func $free (type $t1)))
  (import "env" "malloc" (func $malloc (type $t2)))
  (import "env" "realloc" (func $realloc (type $t0)))
  (import "env" "qsort" (func $qsort (type $t3)))
  (import "env" "strlen" (func $strlen (type $t2)))
  (import "env" "fgets" (func $fgets (type $t4)))
  (import "env" "memcmp" (func $memcmp (type $t4)))
  (func $kh_resize_oligonucleotide (type $t0) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i64) (local $l15 i32) (local $l16 i32) (local $l17 i32) (local $l18 i64)
    (block $B0
      (block $B1
        (br_if $B1
          (i32.ge_u
            (i32.load offset=4
              (local.get $p0))
            (local.tee $l3
              (i32.trunc_sat_f64_u
                (f64.add
                  (f64.mul
                    (f64.convert_i32_u
                      (local.tee $l2
                        (select
                          (local.tee $p1
                            (i32.add
                              (i32.or
                                (i32.shr_u
                                  (local.tee $p1
                                    (i32.or
                                      (i32.shr_u
                                        (local.tee $p1
                                          (i32.or
                                            (i32.shr_u
                                              (local.tee $p1
                                                (i32.or
                                                  (i32.shr_u
                                                    (local.tee $p1
                                                      (i32.or
                                                        (i32.shr_u
                                                          (local.tee $p1
                                                            (i32.add
                                                              (local.get $p1)
                                                              (i32.const -1)))
                                                          (i32.const 1))
                                                        (local.get $p1)))
                                                    (i32.const 2))
                                                  (local.get $p1)))
                                              (i32.const 4))
                                            (local.get $p1)))
                                        (i32.const 8))
                                      (local.get $p1)))
                                  (i32.const 16))
                                (local.get $p1))
                              (i32.const 1)))
                          (i32.const 4)
                          (i32.gt_u
                            (local.get $p1)
                            (i32.const 4)))))
                    (f64.const 0x1.8a3d70a3d70a4p-1 (;=0.77;)))
                  (f64.const 0x1p-1 (;=0.5;)))))))
        (block $B2
          (br_if $B2
            (local.tee $l4
              (call $malloc
                (local.tee $p1
                  (select
                    (i32.const 4)
                    (i32.and
                      (i32.shr_u
                        (local.get $l2)
                        (i32.const 2))
                      (i32.const 1073741820))
                    (i32.lt_u
                      (local.get $p1)
                      (i32.const 16)))))))
          (return
            (i32.const -1)))
        (block $B3
          (br_if $B3
            (i32.eqz
              (local.get $p1)))
          (memory.fill
            (local.get $l4)
            (i32.const 170)
            (local.get $p1)))
        (block $B4
          (block $B5
            (br_if $B5
              (i32.ge_u
                (local.tee $p1
                  (i32.load
                    (local.get $p0)))
                (local.get $l2)))
            (br_if $B0
              (i32.eqz
                (local.tee $p1
                  (call $realloc
                    (i32.load offset=20
                      (local.get $p0))
                    (i32.shl
                      (local.get $l2)
                      (i32.const 3))))))
            (i32.store offset=20
              (local.get $p0)
              (local.get $p1))
            (br_if $B0
              (i32.eqz
                (local.tee $p1
                  (call $realloc
                    (i32.load offset=24
                      (local.get $p0))
                    (i32.shl
                      (local.get $l2)
                      (i32.const 2))))))
            (i32.store offset=24
              (local.get $p0)
              (local.get $p1))
            (br_if $B4
              (i32.eqz
                (local.tee $p1
                  (i32.load
                    (local.get $p0))))))
          (local.set $l5
            (i32.add
              (local.get $l2)
              (i32.const -1)))
          (local.set $l6
            (i32.load offset=16
              (local.get $p0)))
          (local.set $l7
            (i32.const 0))
          (loop $L6
            (block $B7
              (br_if $B7
                (i32.and
                  (i32.shr_u
                    (local.tee $l9
                      (i32.load
                        (local.tee $l8
                          (i32.add
                            (local.get $l6)
                            (i32.and
                              (i32.shr_u
                                (local.get $l7)
                                (i32.const 2))
                              (i32.const 1073741820))))))
                    (local.tee $l10
                      (i32.shl
                        (local.get $l7)
                        (i32.const 1))))
                  (i32.const 3)))
              (local.set $l12
                (i32.load
                  (i32.add
                    (local.tee $l11
                      (i32.load offset=24
                        (local.get $p0)))
                    (i32.shl
                      (local.get $l7)
                      (i32.const 2)))))
              (i32.store
                (local.get $l8)
                (i32.or
                  (local.get $l9)
                  (i32.shl
                    (i32.const 1)
                    (i32.and
                      (local.get $l10)
                      (i32.const 30)))))
              (local.set $l14
                (i64.load
                  (i32.add
                    (local.tee $l13
                      (i32.load offset=20
                        (local.get $p0)))
                    (i32.shl
                      (local.get $l7)
                      (i32.const 3)))))
              (loop $L8
                (block $B9
                  (block $B10
                    (br_if $B10
                      (i32.eqz
                        (i32.and
                          (local.tee $l9
                            (i32.shl
                              (i32.const 2)
                              (local.tee $l8
                                (i32.shl
                                  (local.tee $p1
                                    (i32.and
                                      (local.get $l5)
                                      (i32.wrap_i64
                                        (i64.xor
                                          (i64.shr_u
                                            (local.get $l14)
                                            (i64.const 7))
                                          (local.get $l14)))))
                                  (i32.const 1)))))
                          (local.tee $l16
                            (i32.load
                              (local.tee $l10
                                (i32.add
                                  (local.get $l4)
                                  (i32.shl
                                    (local.tee $l15
                                      (i32.shr_u
                                        (local.get $p1)
                                        (i32.const 4)))
                                    (i32.const 2)))))))))
                    (local.set $l8
                      (i32.and
                        (local.get $l8)
                        (i32.const 30)))
                    (br $B9))
                  (local.set $l8
                    (i32.const 1))
                  (loop $L11
                    (local.set $p1
                      (i32.add
                        (local.get $p1)
                        (local.get $l8)))
                    (local.set $l8
                      (i32.add
                        (local.get $l8)
                        (i32.const 1)))
                    (br_if $L11
                      (i32.eqz
                        (i32.and
                          (local.tee $l9
                            (i32.shl
                              (i32.const 2)
                              (local.tee $l17
                                (i32.shl
                                  (local.tee $p1
                                    (i32.and
                                      (local.get $p1)
                                      (local.get $l5)))
                                  (i32.const 1)))))
                          (local.tee $l16
                            (i32.load
                              (local.tee $l10
                                (i32.add
                                  (local.get $l4)
                                  (i32.shl
                                    (local.tee $l15
                                      (i32.shr_u
                                        (local.get $p1)
                                        (i32.const 4)))
                                    (i32.const 2))))))))))
                  (local.set $l8
                    (i32.and
                      (local.get $l17)
                      (i32.const 30))))
                (i32.store
                  (local.get $l10)
                  (i32.and
                    (local.get $l16)
                    (i32.xor
                      (local.get $l9)
                      (i32.const -1))))
                (block $B12
                  (block $B13
                    (br_if $B13
                      (i32.ge_u
                        (local.get $p1)
                        (i32.load
                          (local.get $p0))))
                    (br_if $B12
                      (i32.eqz
                        (i32.and
                          (i32.shr_u
                            (i32.load
                              (local.tee $l9
                                (i32.add
                                  (local.get $l6)
                                  (i32.shl
                                    (local.get $l15)
                                    (i32.const 2)))))
                            (local.get $l8))
                          (i32.const 3)))))
                  (i32.store
                    (i32.add
                      (local.get $l11)
                      (i32.shl
                        (local.get $p1)
                        (i32.const 2)))
                    (local.get $l12))
                  (i64.store
                    (i32.add
                      (local.get $l13)
                      (i32.shl
                        (local.get $p1)
                        (i32.const 3)))
                    (local.get $l14))
                  (local.set $p1
                    (i32.load
                      (local.get $p0)))
                  (br $B7))
                (local.set $l16
                  (i32.load
                    (local.tee $l10
                      (i32.add
                        (local.get $l11)
                        (i32.shl
                          (local.get $p1)
                          (i32.const 2))))))
                (i32.store
                  (local.get $l10)
                  (local.get $l12))
                (local.set $l18
                  (i64.load
                    (local.tee $p1
                      (i32.add
                        (local.get $l13)
                        (i32.shl
                          (local.get $p1)
                          (i32.const 3))))))
                (i64.store
                  (local.get $p1)
                  (local.get $l14))
                (i32.store
                  (local.get $l9)
                  (i32.or
                    (i32.load
                      (local.get $l9))
                    (i32.shl
                      (i32.const 1)
                      (local.get $l8))))
                (local.set $l14
                  (local.get $l18))
                (local.set $l12
                  (local.get $l16))
                (br $L8)))
            (br_if $L6
              (i32.ne
                (local.tee $l7
                  (i32.add
                    (local.get $l7)
                    (i32.const 1)))
                (local.get $p1))))
          (br_if $B4
            (i32.le_u
              (local.get $p1)
              (local.get $l2)))
          (i32.store offset=20
            (local.get $p0)
            (call $realloc
              (i32.load offset=20
                (local.get $p0))
              (i32.shl
                (local.get $l2)
                (i32.const 3))))
          (i32.store offset=24
            (local.get $p0)
            (call $realloc
              (i32.load offset=24
                (local.get $p0))
              (i32.shl
                (local.get $l2)
                (i32.const 2)))))
        (call $free
          (i32.load offset=16
            (local.get $p0)))
        (i32.store
          (local.get $p0)
          (local.get $l2))
        (i32.store offset=16
          (local.get $p0)
          (local.get $l4))
        (i32.store offset=12
          (local.get $p0)
          (local.get $l3))
        (i32.store offset=8
          (local.get $p0)
          (i32.load offset=4
            (local.get $p0))))
      (return
        (i32.const 0)))
    (call $free
      (local.get $l4))
    (i32.const -1))
  (func $kh_put_oligonucleotide (type $t5) (param $p0 i32) (param $p1 i64) (param $p2 i32) (result i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32)
    (block $B0
      (br_if $B0
        (i32.lt_u
          (i32.load offset=8
            (local.get $p0))
          (i32.load offset=12
            (local.get $p0))))
      (block $B1
        (br_if $B1
          (i32.le_u
            (local.tee $l3
              (i32.load
                (local.get $p0)))
            (i32.shl
              (i32.load offset=4
                (local.get $p0))
              (i32.const 1))))
        (br_if $B0
          (i32.gt_s
            (call $kh_resize_oligonucleotide
              (local.get $p0)
              (i32.add
                (local.get $l3)
                (i32.const -1)))
            (i32.const -1)))
        (i32.store
          (local.get $p2)
          (i32.const -1))
        (return
          (i32.load
            (local.get $p0))))
      (br_if $B0
        (i32.gt_s
          (call $kh_resize_oligonucleotide
            (local.get $p0)
            (i32.add
              (local.get $l3)
              (i32.const 1)))
          (i32.const -1)))
      (i32.store
        (local.get $p2)
        (i32.const -1))
      (return
        (i32.load
          (local.get $p0))))
    (block $B2
      (block $B3
        (br_if $B3
          (i32.eqz
            (i32.and
              (i32.shr_u
                (i32.load
                  (i32.add
                    (local.tee $l4
                      (i32.load offset=16
                        (local.get $p0)))
                    (i32.and
                      (i32.shr_u
                        (local.tee $l7
                          (i32.and
                            (local.tee $l6
                              (i32.add
                                (local.tee $l5
                                  (i32.load
                                    (local.get $p0)))
                                (i32.const -1)))
                            (i32.wrap_i64
                              (i64.xor
                                (i64.shr_u
                                  (local.get $p1)
                                  (i64.const 7))
                                (local.get $p1)))))
                        (i32.const 2))
                      (i32.const 1073741820))))
                (i32.shl
                  (local.get $l7)
                  (i32.const 1)))
              (i32.const 2))))
        (local.set $l8
          (local.get $l7))
        (br $B2))
      (local.set $l9
        (i32.const 1))
      (local.set $l3
        (local.get $l7))
      (local.set $l8
        (local.get $l5))
      (block $B4
        (block $B5
          (loop $L6
            (local.set $l11
              (i32.and
                (local.tee $l10
                  (i32.shl
                    (local.get $l3)
                    (i32.const 1)))
                (i32.const 30)))
            (br_if $B5
              (i32.and
                (local.tee $l10
                  (i32.shr_u
                    (local.tee $l12
                      (i32.load
                        (i32.add
                          (local.get $l4)
                          (i32.and
                            (i32.shr_u
                              (local.get $l3)
                              (i32.const 2))
                            (i32.const 1073741820)))))
                    (local.get $l10)))
                (i32.const 2)))
            (block $B7
              (br_if $B7
                (i32.and
                  (local.get $l10)
                  (i32.const 1)))
              (br_if $B5
                (i64.eq
                  (i64.load
                    (i32.add
                      (i32.load offset=20
                        (local.get $p0))
                      (i32.shl
                        (local.get $l3)
                        (i32.const 3))))
                  (local.get $p1))))
            (local.set $l8
              (select
                (local.get $l3)
                (local.get $l8)
                (i32.and
                  (i32.shr_u
                    (local.get $l12)
                    (local.get $l11))
                  (i32.const 1))))
            (local.set $l3
              (i32.add
                (local.get $l3)
                (local.get $l9)))
            (local.set $l9
              (i32.add
                (local.get $l9)
                (i32.const 1)))
            (br_if $L6
              (i32.ne
                (local.tee $l3
                  (i32.and
                    (local.get $l3)
                    (local.get $l6)))
                (local.get $l7))))
          (local.set $l10
            (i32.const 1))
          (local.set $l9
            (local.get $l5))
          (br_if $B4
            (i32.eq
              (local.get $l8)
              (local.get $l5)))
          (br $B2))
        (local.set $l10
          (i32.eqz
            (i32.and
              (i32.shr_u
                (local.get $l12)
                (local.get $l11))
              (i32.const 2))))
        (local.set $l9
          (local.get $l8))
        (local.set $l7
          (local.get $l3)))
      (local.set $l8
        (select
          (local.get $l7)
          (select
            (local.get $l7)
            (local.get $l9)
            (i32.eq
              (local.get $l9)
              (local.get $l5)))
          (local.get $l10))))
    (local.set $l9
      (i32.and
        (local.tee $l3
          (i32.shl
            (local.get $l8)
            (i32.const 1)))
        (i32.const 30)))
    (block $B8
      (br_if $B8
        (i32.eqz
          (i32.and
            (local.tee $l3
              (i32.shr_u
                (local.tee $l11
                  (i32.load
                    (local.tee $l10
                      (i32.add
                        (local.get $l4)
                        (i32.and
                          (i32.shr_u
                            (local.get $l8)
                            (i32.const 2))
                          (i32.const 1073741820))))))
                (local.get $l3)))
            (i32.const 2))))
      (i32.store
        (local.get $l10)
        (i32.and
          (local.get $l11)
          (i32.xor
            (i32.shl
              (i32.const 3)
              (local.get $l9))
            (i32.const -1))))
      (i32.store offset=8
        (local.get $p0)
        (i32.add
          (i32.load offset=8
            (local.get $p0))
          (i32.const 1)))
      (i32.store offset=4
        (local.get $p0)
        (i32.add
          (i32.load offset=4
            (local.get $p0))
          (i32.const 1)))
      (i64.store
        (i32.add
          (i32.load offset=20
            (local.get $p0))
          (i32.shl
            (local.get $l8)
            (i32.const 3)))
        (local.get $p1))
      (i32.store
        (local.get $p2)
        (i32.const 1))
      (return
        (local.get $l8)))
    (block $B9
      (br_if $B9
        (i32.eqz
          (i32.and
            (local.get $l3)
            (i32.const 1))))
      (i32.store
        (local.get $l10)
        (i32.and
          (local.get $l11)
          (i32.xor
            (i32.shl
              (i32.const 3)
              (local.get $l9))
            (i32.const -1))))
      (i32.store offset=4
        (local.get $p0)
        (i32.add
          (i32.load offset=4
            (local.get $p0))
          (i32.const 1)))
      (i64.store
        (i32.add
          (i32.load offset=20
            (local.get $p0))
          (i32.shl
            (local.get $l8)
            (i32.const 3)))
        (local.get $p1))
      (i32.store
        (local.get $p2)
        (i32.const 2))
      (return
        (local.get $l8)))
    (i32.store
      (local.get $p2)
      (i32.const 0))
    (local.get $l8))
  (func $element_Compare (type $t0) (param $p0 i32) (param $p1 i32) (result i32)
    (local $l2 i32) (local $l3 i32)
    (block $B0
      (br_if $B0
        (i32.ge_u
          (local.tee $l2
            (i32.load offset=8
              (local.get $p0)))
          (local.tee $l3
            (i32.load offset=8
              (local.get $p1)))))
      (return
        (i32.const 1)))
    (block $B1
      (br_if $B1
        (i32.le_u
          (local.get $l2)
          (local.get $l3)))
      (return
        (i32.const -1)))
    (select
      (i32.const 1)
      (i32.const -1)
      (i64.gt_u
        (i64.load
          (local.get $p0))
        (i64.load
          (local.get $p1)))))
  (func $generate_Frequencies_For_Desired_Length_Oligonucleotides (type $t3) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32)
    (local $l4 i32) (local $l5 i32) (local $l6 i64) (local $l7 i64) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 i32) (local $l14 i32) (local $l15 i32) (local $l16 i32) (local $l17 i32)
    (global.set $__stack_pointer
      (local.tee $l4
        (i32.sub
          (global.get $__stack_pointer)
          (i32.const 16))))
    (local.set $l5
      (i32.add
        (local.get $p2)
        (i32.const -1)))
    (local.set $l6
      (i64.xor
        (i64.shl
          (i64.const -1)
          (i64.extend_i32_u
            (i32.shl
              (local.get $p2)
              (i32.const 1))))
        (i64.const -1)))
    (local.set $l7
      (i64.const 0))
    (local.set $l8
      (call $calloc
        (i32.const 1)
        (i32.const 28)))
    (block $B0
      (br_if $B0
        (i32.lt_s
          (local.get $p2)
          (i32.const 2)))
      (local.set $l9
        (i32.and
          (local.get $l5)
          (i32.const 3)))
      (block $B1
        (block $B2
          (br_if $B2
            (i32.ge_u
              (i32.add
                (local.get $p2)
                (i32.const -2))
              (i32.const 3)))
          (local.set $l10
            (i32.const 0))
          (local.set $l7
            (i64.const 0))
          (br $B1))
        (local.set $l11
          (i32.and
            (local.get $l5)
            (i32.const -4)))
        (local.set $l10
          (i32.const 0))
        (local.set $l7
          (i64.const 0))
        (loop $L3
          (local.set $l7
            (i64.or
              (i64.and
                (i64.shl
                  (i64.or
                    (i64.and
                      (i64.shl
                        (i64.or
                          (i64.and
                            (i64.shl
                              (i64.or
                                (i64.and
                                  (i64.shl
                                    (local.get $l7)
                                    (i64.const 2))
                                  (local.get $l6))
                                (i64.load8_s
                                  (local.tee $l12
                                    (i32.add
                                      (local.get $p0)
                                      (local.get $l10)))))
                              (i64.const 2))
                            (local.get $l6))
                          (i64.load8_s
                            (i32.add
                              (local.get $l12)
                              (i32.const 1))))
                        (i64.const 2))
                      (local.get $l6))
                    (i64.load8_s
                      (i32.add
                        (local.get $l12)
                        (i32.const 2))))
                  (i64.const 2))
                (local.get $l6))
              (i64.load8_s
                (i32.add
                  (local.get $l12)
                  (i32.const 3)))))
          (br_if $L3
            (i32.ne
              (local.get $l11)
              (local.tee $l10
                (i32.add
                  (local.get $l10)
                  (i32.const 4)))))))
      (br_if $B0
        (i32.eqz
          (local.get $l9)))
      (local.set $l12
        (i32.add
          (local.get $p0)
          (local.get $l10)))
      (loop $L4
        (local.set $l7
          (i64.or
            (i64.and
              (i64.shl
                (local.get $l7)
                (i64.const 2))
              (local.get $l6))
            (i64.load8_s
              (local.get $l12))))
        (local.set $l12
          (i32.add
            (local.get $l12)
            (i32.const 1)))
        (br_if $L4
          (local.tee $l9
            (i32.add
              (local.get $l9)
              (i32.const -1))))))
    (block $B5
      (block $B6
        (br_if $B6
          (i32.le_s
            (local.get $p2)
            (local.get $p1)))
        (local.set $p1
          (i32.const 0))
        (local.set $l11
          (call $malloc
            (i32.const 0)))
        (local.set $l13
          (i32.const 0))
        (local.set $l14
          (i32.load offset=16
            (local.get $l8)))
        (br $B5))
      (local.set $l12
        (i32.add
          (local.get $p0)
          (local.get $l5)))
      (local.set $p0
        (i32.add
          (i32.sub
            (local.get $p1)
            (local.get $p2))
          (i32.const 1)))
      (loop $L7
        (local.set $l10
          (call $kh_put_oligonucleotide
            (local.get $l8)
            (local.tee $l7
              (i64.or
                (i64.and
                  (i64.shl
                    (local.get $l7)
                    (i64.const 2))
                  (local.get $l6))
                (i64.load8_s
                  (local.get $l12))))
            (i32.add
              (local.get $l4)
              (i32.const 12))))
        (local.set $l10
          (i32.add
            (local.tee $l13
              (i32.load offset=24
                (local.get $l8)))
            (i32.shl
              (local.get $l10)
              (i32.const 2))))
        (local.set $l9
          (i32.const 1))
        (block $B8
          (br_if $B8
            (i32.load offset=12
              (local.get $l4)))
          (local.set $l9
            (i32.add
              (i32.load
                (local.get $l10))
              (i32.const 1))))
        (i32.store
          (local.get $l10)
          (local.get $l9))
        (local.set $l12
          (i32.add
            (local.get $l12)
            (i32.const 1)))
        (br_if $L7
          (local.tee $p0
            (i32.add
              (local.get $p0)
              (i32.const -1)))))
      (local.set $l15
        (i32.load
          (local.get $l8)))
      (local.set $l11
        (call $malloc
          (i32.shl
            (local.tee $p1
              (i32.load offset=4
                (local.get $l8)))
            (i32.const 4))))
      (block $B9
        (br_if $B9
          (local.get $l15))
        (local.set $l14
          (i32.load offset=16
            (local.get $l8)))
        (br $B5))
      (local.set $l14
        (i32.load offset=16
          (local.get $l8)))
      (local.set $l12
        (i32.const 0))
      (local.set $l9
        (i32.const 0))
      (local.set $p0
        (local.get $l13))
      (local.set $l16
        (i32.const 0))
      (local.set $l10
        (i32.const 0))
      (loop $L10
        (block $B11
          (br_if $B11
            (i32.and
              (i32.shr_u
                (i32.load
                  (i32.add
                    (local.get $l14)
                    (i32.and
                      (i32.shr_u
                        (local.get $l10)
                        (i32.const 2))
                      (i32.const 1073741820))))
                (i32.and
                  (local.get $l12)
                  (i32.const 30)))
              (i32.const 3)))
          (i32.store offset=8
            (local.tee $l17
              (i32.add
                (local.get $l11)
                (i32.shl
                  (local.get $l16)
                  (i32.const 4))))
            (i32.load
              (local.get $p0)))
          (local.set $l7
            (i64.load
              (i32.add
                (i32.load offset=20
                  (local.get $l8))
                (local.get $l9))))
          (i32.store offset=12
            (local.get $l17)
            (i32.const 0))
          (i64.store
            (local.get $l17)
            (local.get $l7))
          (local.set $l16
            (i32.add
              (local.get $l16)
              (i32.const 1))))
        (local.set $l12
          (i32.add
            (local.get $l12)
            (i32.const 2)))
        (local.set $l9
          (i32.add
            (local.get $l9)
            (i32.const 8)))
        (local.set $p0
          (i32.add
            (local.get $p0)
            (i32.const 4)))
        (br_if $L10
          (i32.ne
            (local.get $l15)
            (local.tee $l10
              (i32.add
                (local.get $l10)
                (i32.const 1)))))))
    (call $free
      (i32.load offset=20
        (local.get $l8)))
    (call $free
      (local.get $l14))
    (call $free
      (local.get $l13))
    (call $free
      (local.get $l8))
    (call $qsort
      (local.get $l11)
      (local.get $p1)
      (i32.const 16)
      (i32.const 1))
    (block $B12
      (br_if $B12
        (i32.lt_s
          (local.get $p1)
          (i32.const 1)))
      (br_if $B12
        (i32.lt_s
          (local.get $p2)
          (i32.const 1)))
      (local.set $p0
        (i32.and
          (local.get $p2)
          (i32.const 7)))
      (local.set $l10
        (i32.const 0))
      (local.set $l13
        (i32.lt_u
          (local.get $p2)
          (i32.const 8)))
      (loop $L13
        (local.set $l7
          (i64.load
            (local.tee $l9
              (i32.add
                (local.get $l11)
                (i32.shl
                  (local.get $l10)
                  (i32.const 4))))))
        (block $B14
          (block $B15
            (br_if $B15
              (local.get $p0))
            (local.set $l12
              (local.get $l5))
            (br $B14))
          (local.set $l8
            (local.get $p0))
          (local.set $l12
            (local.get $l5))
          (loop $L16
            (local.set $l12
              (i32.add
                (local.get $l12)
                (i32.const -1)))
            (local.set $l7
              (i64.shr_u
                (local.get $l7)
                (i64.const 2)))
            (br_if $L16
              (local.tee $l8
                (i32.add
                  (local.get $l8)
                  (i32.const -1))))))
        (block $B17
          (br_if $B17
            (local.get $l13))
          (local.set $l12
            (i32.add
              (local.get $l12)
              (i32.const 8)))
          (loop $L18
            (local.set $l7
              (i64.shr_u
                (local.get $l7)
                (i64.const 16)))
            (br_if $L18
              (i32.gt_s
                (local.tee $l12
                  (i32.add
                    (local.get $l12)
                    (i32.const -8)))
                (i32.const 7)))))
        (i64.store
          (local.get $l9)
          (local.get $l7))
        (br_if $L13
          (i32.ne
            (local.tee $l10
              (i32.add
                (local.get $l10)
                (i32.const 1)))
            (local.get $p1)))))
    (call $free
      (local.get $l11))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l4)
        (i32.const 16))))
  (func $generate_Count_For_Oligonucleotide (type $t3) (param $p0 i32) (param $p1 i32) (param $p2 i32) (param $p3 i32)
    (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i64) (local $l8 i64) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32)
    (global.set $__stack_pointer
      (local.tee $l4
        (i32.sub
          (global.get $__stack_pointer)
          (i32.const 16))))
    (local.set $l6
      (i32.add
        (local.tee $l5
          (call $strlen
            (local.get $p2)))
        (i32.const -1)))
    (local.set $l7
      (i64.xor
        (i64.shl
          (i64.const -1)
          (i64.extend_i32_u
            (i32.shl
              (local.get $l5)
              (i32.const 1))))
        (i64.const -1)))
    (local.set $l8
      (i64.const 0))
    (local.set $l9
      (call $calloc
        (i32.const 1)
        (i32.const 28)))
    (block $B0
      (br_if $B0
        (i32.lt_s
          (local.get $l5)
          (i32.const 2)))
      (local.set $l10
        (i32.and
          (local.get $l6)
          (i32.const 3)))
      (block $B1
        (block $B2
          (br_if $B2
            (i32.ge_u
              (i32.add
                (local.get $l5)
                (i32.const -2))
              (i32.const 3)))
          (local.set $l11
            (i32.const 0))
          (local.set $l8
            (i64.const 0))
          (br $B1))
        (local.set $l12
          (i32.and
            (local.get $l6)
            (i32.const -4)))
        (local.set $l11
          (i32.const 0))
        (local.set $l8
          (i64.const 0))
        (loop $L3
          (local.set $l8
            (i64.or
              (i64.and
                (i64.shl
                  (i64.or
                    (i64.and
                      (i64.shl
                        (i64.or
                          (i64.and
                            (i64.shl
                              (i64.or
                                (i64.and
                                  (i64.shl
                                    (local.get $l8)
                                    (i64.const 2))
                                  (local.get $l7))
                                (i64.load8_s
                                  (local.tee $p2
                                    (i32.add
                                      (local.get $p0)
                                      (local.get $l11)))))
                              (i64.const 2))
                            (local.get $l7))
                          (i64.load8_s
                            (i32.add
                              (local.get $p2)
                              (i32.const 1))))
                        (i64.const 2))
                      (local.get $l7))
                    (i64.load8_s
                      (i32.add
                        (local.get $p2)
                        (i32.const 2))))
                  (i64.const 2))
                (local.get $l7))
              (i64.load8_s
                (i32.add
                  (local.get $p2)
                  (i32.const 3)))))
          (br_if $L3
            (i32.ne
              (local.get $l12)
              (local.tee $l11
                (i32.add
                  (local.get $l11)
                  (i32.const 4)))))))
      (br_if $B0
        (i32.eqz
          (local.get $l10)))
      (local.set $p2
        (i32.add
          (local.get $p0)
          (local.get $l11)))
      (loop $L4
        (local.set $l8
          (i64.or
            (i64.and
              (i64.shl
                (local.get $l8)
                (i64.const 2))
              (local.get $l7))
            (i64.load8_s
              (local.get $p2))))
        (local.set $p2
          (i32.add
            (local.get $p2)
            (i32.const 1)))
        (br_if $L4
          (local.tee $l10
            (i32.add
              (local.get $l10)
              (i32.const -1))))))
    (local.set $l12
      (i32.const 0))
    (block $B5
      (br_if $B5
        (i32.gt_s
          (local.get $l5)
          (local.get $p1)))
      (local.set $p2
        (i32.add
          (local.get $p0)
          (local.get $l6)))
      (local.set $p0
        (i32.add
          (i32.sub
            (local.get $p1)
            (local.get $l5))
          (i32.const 1)))
      (loop $L6
        (local.set $l11
          (call $kh_put_oligonucleotide
            (local.get $l9)
            (local.tee $l8
              (i64.or
                (i64.and
                  (i64.shl
                    (local.get $l8)
                    (i64.const 2))
                  (local.get $l7))
                (i64.load8_s
                  (local.get $p2))))
            (i32.add
              (local.get $l4)
              (i32.const 12))))
        (local.set $l11
          (i32.add
            (local.tee $l12
              (i32.load offset=24
                (local.get $l9)))
            (i32.shl
              (local.get $l11)
              (i32.const 2))))
        (local.set $l10
          (i32.const 1))
        (block $B7
          (br_if $B7
            (i32.load offset=12
              (local.get $l4)))
          (local.set $l10
            (i32.add
              (i32.load
                (local.get $l11))
              (i32.const 1))))
        (i32.store
          (local.get $l11)
          (local.get $l10))
        (local.set $p2
          (i32.add
            (local.get $p2)
            (i32.const 1)))
        (br_if $L6
          (local.tee $p0
            (i32.add
              (local.get $p0)
              (i32.const -1))))))
    (call $free
      (i32.load offset=20
        (local.get $l9)))
    (call $free
      (i32.load offset=16
        (local.get $l9)))
    (call $free
      (local.get $l12))
    (call $free
      (local.get $l9))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l4)
        (i32.const 16))))
  (func $_start (type $t6) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32)
    (global.set $__stack_pointer
      (local.tee $l0
        (i32.sub
          (global.get $__stack_pointer)
          (i32.const 4096))))
    (block $B0
      (loop $L1
        (br_if $B0
          (i32.eqz
            (call $fgets
              (local.get $l0)
              (i32.const 4096)
              (i32.load
                (i32.const 0)))))
        (br_if $L1
          (call $memcmp
            (i32.const 1076)
            (local.get $l0)
            (i32.const 6)))))
    (local.set $l1
      (call $malloc
        (i32.const 1048576)))
    (block $B2
      (block $B3
        (br_if $B3
          (call $fgets
            (local.get $l0)
            (i32.const 4096)
            (i32.load
              (i32.const 0))))
        (local.set $l2
          (i32.const 0))
        (br $B2))
      (block $B4
        (br_if $B4
          (i32.ne
            (i32.and
              (local.tee $l3
                (i32.load8_u
                  (local.get $l0)))
              (i32.const 255))
            (i32.const 62)))
        (local.set $l2
          (i32.const 0))
        (br $B2))
      (local.set $l4
        (i32.or
          (local.get $l0)
          (i32.const 1)))
      (local.set $l5
        (i32.const 1048576))
      (local.set $l2
        (i32.const 0))
      (loop $L5
        (local.set $l6
          (local.get $l4))
        (loop $L6
          (block $B7
            (block $B8
              (block $B9
                (br_table $B9 $B8 $B8 $B8 $B8 $B8 $B8 $B8 $B8 $B8 $B7 $B8
                  (i32.and
                    (local.get $l3)
                    (i32.const 255))))
              (block $B10
                (br_if $B10
                  (i32.ge_u
                    (i32.sub
                      (local.get $l5)
                      (local.get $l2))
                    (i32.const 4096)))
                (local.set $l1
                  (call $realloc
                    (local.get $l1)
                    (local.tee $l5
                      (i32.shl
                        (local.get $l5)
                        (i32.const 1))))))
              (br_if $B2
                (i32.eqz
                  (call $fgets
                    (local.get $l0)
                    (i32.const 4096)
                    (i32.load
                      (i32.const 0)))))
              (br_if $L5
                (i32.ne
                  (i32.and
                    (local.tee $l3
                      (i32.load8_u
                        (local.get $l0)))
                    (i32.const 255))
                  (i32.const 62)))
              (br $B2))
            (i32.store8
              (i32.add
                (local.get $l1)
                (local.get $l2))
              (i32.load8_u
                (i32.add
                  (i32.and
                    (local.get $l3)
                    (i32.const 7))
                  (i32.const 1024))))
            (local.set $l2
              (i32.add
                (local.get $l2)
                (i32.const 1))))
          (local.set $l3
            (i32.load8_u
              (local.get $l6)))
          (local.set $l6
            (i32.add
              (local.get $l6)
              (i32.const 1)))
          (br $L6))))
    (call $generate_Count_For_Oligonucleotide
      (local.tee $l6
        (call $realloc
          (local.get $l1)
          (local.get $l2)))
      (local.get $l2)
      (i32.const 1057)
      (local.get $l6))
    (call $generate_Count_For_Oligonucleotide
      (local.get $l6)
      (local.get $l2)
      (i32.const 1040)
      (local.get $l6))
    (call $generate_Count_For_Oligonucleotide
      (local.get $l6)
      (local.get $l2)
      (i32.const 1033)
      (local.get $l6))
    (call $generate_Count_For_Oligonucleotide
      (local.get $l6)
      (local.get $l2)
      (i32.const 1083)
      (local.get $l6))
    (call $generate_Count_For_Oligonucleotide
      (local.get $l6)
      (local.get $l2)
      (i32.const 1053)
      (local.get $l6))
    (call $generate_Frequencies_For_Desired_Length_Oligonucleotides
      (local.get $l6)
      (local.get $l2)
      (i32.const 2)
      (local.get $l6))
    (call $generate_Frequencies_For_Desired_Length_Oligonucleotides
      (local.get $l6)
      (local.get $l2)
      (i32.const 1)
      (local.get $l6))
    (call $free
      (local.get $l6))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l0)
        (i32.const 4096)))
    (i32.const 0))
  (table $T0 2 2 funcref)
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66624))
  (export "memory" (memory $memory))
  (export "_start" (func $_start))
  (elem $e0 (i32.const 1) func $element_Compare)
  (data $.rodata (i32.const 1024) " \00 \01\03  \02\00GGTATT\00GGTATTTTAATT\00GGT\00GGTATTTTAATTTATAGT\00>THREE\00GGTA\00")
  (@custom "name" "\00\12\11k-nucleotide.wasm\01\e8\01\0e\00\06calloc\01\04free\02\06malloc\03\07realloc\04\05qsort\05\06strlen\06\05fgets\07\06memcmp\08\19kh_resize_oligonucleotide\09\16kh_put_oligonucleotide\0a\0felement_Compare\0b8generate_Frequencies_For_Desired_Length_Oligonucleotides\0c\22generate_Count_For_Oligonucleotide\0d\06_start\07\12\01\00\0f__stack_pointer\09\0a\01\00\07.rodata")
  (@custom "producers" "\01\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
