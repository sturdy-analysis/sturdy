(module $spectral-norm.wasm
  (type $t0 (func (param i32 i32) (result i32)))
  (type $t1 (func (result i32)))
  (import "env" "printf" (func $printf (type $t0)))
  (func $_start (type $t1) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 f64) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 f64) (local $l12 f64) (local $l13 f64)
    (global.set $__stack_pointer
      (local.tee $l0
        (i32.sub
          (global.get $__stack_pointer)
          (i32.const 2416))))
    (local.set $l1
      (i32.const 0))
    (loop $L0
      (i64.store
        (local.tee $l2
          (i32.add
            (i32.add
              (local.get $l0)
              (i32.const 816))
            (local.get $l1)))
        (i64.const 4607182418800017408))
      (i64.store
        (i32.add
          (local.get $l2)
          (i32.const 32))
        (i64.const 4607182418800017408))
      (i64.store
        (i32.add
          (local.get $l2)
          (i32.const 24))
        (i64.const 4607182418800017408))
      (i64.store
        (i32.add
          (local.get $l2)
          (i32.const 16))
        (i64.const 4607182418800017408))
      (i64.store
        (i32.add
          (local.get $l2)
          (i32.const 8))
        (i64.const 4607182418800017408))
      (br_if $L0
        (i32.ne
          (local.tee $l1
            (i32.add
              (local.get $l1)
              (i32.const 40)))
          (i32.const 800))))
    (local.set $l3
      (i32.const 0))
    (loop $L1
      (local.set $l4
        (i32.const 0))
      (loop $L2
        (local.set $l5
          (i32.add
            (local.get $l4)
            (i32.const 1)))
        (local.set $l2
          (i32.const 0))
        (local.set $l6
          (f64.const 0x0p+0 (;=0;)))
        (local.set $l1
          (i32.add
            (local.get $l0)
            (i32.const 816)))
        (loop $L3
          (local.set $l6
            (f64.add
              (f64.mul
                (f64.div
                  (f64.const 0x1p+0 (;=1;))
                  (f64.convert_i32_s
                    (i32.add
                      (i32.shr_u
                        (i32.mul
                          (i32.add
                            (local.tee $l7
                              (i32.add
                                (local.get $l4)
                                (local.get $l2)))
                            (i32.const 1))
                          (local.get $l7))
                        (i32.const 1))
                      (local.get $l5))))
                (f64.load
                  (local.get $l1)))
              (local.get $l6)))
          (local.set $l1
            (i32.add
              (local.get $l1)
              (i32.const 8)))
          (br_if $L3
            (i32.ne
              (local.tee $l2
                (i32.add
                  (local.get $l2)
                  (i32.const 1)))
              (i32.const 100))))
        (f64.store
          (i32.add
            (i32.add
              (local.get $l0)
              (i32.const 1616))
            (i32.shl
              (local.get $l4)
              (i32.const 3)))
          (local.get $l6))
        (local.set $l4
          (local.get $l5))
        (br_if $L2
          (i32.ne
            (local.get $l5)
            (i32.const 100))))
      (local.set $l5
        (i32.const 2))
      (local.set $l8
        (i32.const 0))
      (local.set $l9
        (i32.const 0))
      (loop $L4
        (local.set $l2
          (i32.const 1))
        (local.set $l10
          (i32.add
            (local.get $l9)
            (i32.const 1)))
        (local.set $l6
          (f64.const 0x0p+0 (;=0;)))
        (local.set $l1
          (local.get $l8))
        (local.set $l7
          (local.get $l5))
        (local.set $l4
          (i32.const 0))
        (loop $L5
          (local.set $l6
            (f64.add
              (f64.mul
                (f64.div
                  (f64.const 0x1p+0 (;=1;))
                  (f64.convert_i32_s
                    (i32.add
                      (local.get $l2)
                      (i32.shr_u
                        (local.get $l1)
                        (i32.const 1)))))
                (f64.load
                  (i32.add
                    (i32.add
                      (local.get $l0)
                      (i32.const 1616))
                    (local.get $l4))))
              (local.get $l6)))
          (local.set $l1
            (i32.add
              (local.get $l1)
              (local.get $l7)))
          (local.set $l7
            (i32.add
              (local.get $l7)
              (i32.const 2)))
          (local.set $l2
            (i32.add
              (local.get $l2)
              (i32.const 1)))
          (br_if $L5
            (i32.ne
              (local.tee $l4
                (i32.add
                  (local.get $l4)
                  (i32.const 8)))
              (i32.const 800))))
        (f64.store
          (i32.add
            (i32.add
              (local.get $l0)
              (i32.const 16))
            (i32.shl
              (local.get $l9)
              (i32.const 3)))
          (local.get $l6))
        (local.set $l8
          (i32.add
            (local.get $l8)
            (local.get $l5)))
        (local.set $l5
          (i32.add
            (local.get $l5)
            (i32.const 2)))
        (local.set $l9
          (local.get $l10))
        (br_if $L4
          (i32.ne
            (local.get $l10)
            (i32.const 100))))
      (local.set $l4
        (i32.const 0))
      (loop $L6
        (local.set $l5
          (i32.add
            (local.get $l4)
            (i32.const 1)))
        (local.set $l6
          (f64.const 0x0p+0 (;=0;)))
        (local.set $l2
          (i32.add
            (local.get $l0)
            (i32.const 16)))
        (local.set $l1
          (i32.const 0))
        (loop $L7
          (local.set $l6
            (f64.add
              (f64.mul
                (f64.div
                  (f64.const 0x1p+0 (;=1;))
                  (f64.convert_i32_s
                    (i32.add
                      (i32.shr_u
                        (i32.mul
                          (i32.add
                            (local.tee $l7
                              (i32.add
                                (local.get $l4)
                                (local.get $l1)))
                            (i32.const 1))
                          (local.get $l7))
                        (i32.const 1))
                      (local.get $l5))))
                (f64.load
                  (local.get $l2)))
              (local.get $l6)))
          (local.set $l2
            (i32.add
              (local.get $l2)
              (i32.const 8)))
          (br_if $L7
            (i32.ne
              (local.tee $l1
                (i32.add
                  (local.get $l1)
                  (i32.const 1)))
              (i32.const 100))))
        (f64.store
          (i32.add
            (i32.add
              (local.get $l0)
              (i32.const 1616))
            (i32.shl
              (local.get $l4)
              (i32.const 3)))
          (local.get $l6))
        (local.set $l4
          (local.get $l5))
        (br_if $L6
          (i32.ne
            (local.get $l5)
            (i32.const 100))))
      (local.set $l5
        (i32.const 2))
      (local.set $l8
        (i32.const 0))
      (local.set $l9
        (i32.const 0))
      (loop $L8
        (local.set $l2
          (i32.const 1))
        (local.set $l10
          (i32.add
            (local.get $l9)
            (i32.const 1)))
        (local.set $l6
          (f64.const 0x0p+0 (;=0;)))
        (local.set $l1
          (local.get $l8))
        (local.set $l7
          (local.get $l5))
        (local.set $l4
          (i32.const 0))
        (loop $L9
          (local.set $l6
            (f64.add
              (f64.mul
                (f64.div
                  (f64.const 0x1p+0 (;=1;))
                  (f64.convert_i32_s
                    (i32.add
                      (local.get $l2)
                      (i32.shr_u
                        (local.get $l1)
                        (i32.const 1)))))
                (f64.load
                  (i32.add
                    (i32.add
                      (local.get $l0)
                      (i32.const 1616))
                    (local.get $l4))))
              (local.get $l6)))
          (local.set $l1
            (i32.add
              (local.get $l1)
              (local.get $l7)))
          (local.set $l7
            (i32.add
              (local.get $l7)
              (i32.const 2)))
          (local.set $l2
            (i32.add
              (local.get $l2)
              (i32.const 1)))
          (br_if $L9
            (i32.ne
              (local.tee $l4
                (i32.add
                  (local.get $l4)
                  (i32.const 8)))
              (i32.const 800))))
        (f64.store
          (i32.add
            (i32.add
              (local.get $l0)
              (i32.const 816))
            (i32.shl
              (local.get $l9)
              (i32.const 3)))
          (local.get $l6))
        (local.set $l8
          (i32.add
            (local.get $l8)
            (local.get $l5)))
        (local.set $l5
          (i32.add
            (local.get $l5)
            (i32.const 2)))
        (local.set $l9
          (local.get $l10))
        (br_if $L8
          (i32.ne
            (local.get $l10)
            (i32.const 100))))
      (br_if $L1
        (i32.ne
          (local.tee $l3
            (i32.add
              (local.get $l3)
              (i32.const 1)))
          (i32.const 10))))
    (local.set $l11
      (f64.const 0x0p+0 (;=0;)))
    (local.set $l2
      (i32.const 0))
    (local.set $l12
      (f64.const 0x0p+0 (;=0;)))
    (loop $L10
      (local.set $l11
        (f64.add
          (f64.mul
            (local.tee $l6
              (f64.load
                (i32.add
                  (local.tee $l1
                    (i32.add
                      (i32.add
                        (local.get $l0)
                        (i32.const 16))
                      (local.get $l2)))
                  (i32.const 8))))
            (local.get $l6))
          (f64.add
            (f64.mul
              (local.tee $l13
                (f64.load
                  (local.get $l1)))
              (local.get $l13))
            (local.get $l11))))
      (local.set $l12
        (f64.add
          (f64.mul
            (f64.load
              (i32.add
                (local.tee $l1
                  (i32.add
                    (i32.add
                      (local.get $l0)
                      (i32.const 816))
                    (local.get $l2)))
                (i32.const 8)))
            (local.get $l6))
          (f64.add
            (f64.mul
              (f64.load
                (local.get $l1))
              (local.get $l13))
            (local.get $l12))))
      (br_if $L10
        (i32.ne
          (local.tee $l2
            (i32.add
              (local.get $l2)
              (i32.const 16)))
          (i32.const 800))))
    (f64.store
      (local.get $l0)
      (f64.sqrt
        (f64.div
          (local.get $l12)
          (local.get $l11))))
    (drop
      (call $printf
        (i32.const 1024)
        (local.get $l0)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l0)
        (i32.const 2416)))
    (i32.const 0))
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66576))
  (export "memory" (memory $memory))
  (export "_start" (func $_start))
  (data $.rodata (i32.const 1024) "%0.9f\0a\00")
  (@custom "name" "\00\13\12spectral-norm.wasm\01\11\02\00\06printf\01\06_start\07\12\01\00\0f__stack_pointer\09\0a\01\00\07.rodata")
  (@custom "producers" "\01\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
