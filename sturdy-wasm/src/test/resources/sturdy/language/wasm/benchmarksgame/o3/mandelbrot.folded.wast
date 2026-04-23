(module $mandelbrot.wasm
  (type $t0 (func (param i32) (result i32)))
  (type $t1 (func (param i32 i32 i32) (result i32)))
  (type $t2 (func (param i32 i32 i32 i32) (result i32)))
  (type $t3 (func (param i32)))
  (type $t4 (func (result i32)))
  (import "env" "malloc" (func $malloc (type $t0)))
  (import "env" "fprintf" (func $fprintf (type $t1)))
  (import "env" "fwrite" (func $fwrite (type $t2)))
  (import "env" "free" (func $free (type $t3)))
  (func $_start (type $t4) (result i32)
    (local $l0 i32) (local $l1 f64) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 f64) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 f64) (local $l12 f64) (local $l13 f64) (local $l14 f64) (local $l15 f64) (local $l16 f64) (local $l17 f64) (local $l18 f64) (local $l19 f64) (local $l20 f64) (local $l21 f64) (local $l22 f64) (local $l23 f64) (local $l24 f64) (local $l25 f64) (local $l26 f64) (local $l27 f64) (local $l28 f64) (local $l29 f64) (local $l30 f64) (local $l31 f64) (local $l32 f64) (local $l33 f64) (local $l34 f64) (local $l35 f64) (local $l36 f64) (local $l37 f64) (local $l38 f64) (local $l39 f64) (local $l40 f64) (local $l41 f64) (local $l42 f64) (local $l43 f64) (local $l44 f64) (local $l45 f64) (local $l46 f64) (local $l47 f64) (local $l48 f64) (local $l49 f64)
    (global.set $__stack_pointer
      (local.tee $l0
        (i32.sub
          (global.get $__stack_pointer)
          (i32.const 8208))))
    (local.set $l1
      (f64.const 0x0p+0 (;=0;)))
    (local.set $l2
      (i32.const 1))
    (local.set $l3
      (i32.add
        (local.get $l0)
        (i32.const 4112)))
    (local.set $l4
      (i32.add
        (local.get $l0)
        (i32.const 16)))
    (local.set $l5
      (call $malloc
        (i32.const 32768)))
    (loop $L0
      (f64.store
        (local.get $l4)
        (f64.add
          (local.tee $l6
            (f64.mul
              (local.get $l1)
              (f64.const 0x1p-8 (;=0.00390625;))))
          (f64.const -0x1p+0 (;=-1;))))
      (f64.store
        (local.get $l3)
        (f64.add
          (local.get $l6)
          (f64.const -0x1.8p+0 (;=-1.5;))))
      (f64.store
        (i32.add
          (local.get $l3)
          (i32.const 8))
        (f64.add
          (local.tee $l6
            (f64.mul
              (f64.convert_i32_u
                (local.get $l2))
              (f64.const 0x1p-8 (;=0.00390625;))))
          (f64.const -0x1.8p+0 (;=-1.5;))))
      (f64.store
        (i32.add
          (local.get $l4)
          (i32.const 8))
        (f64.add
          (local.get $l6)
          (f64.const -0x1p+0 (;=-1;))))
      (local.set $l4
        (i32.add
          (local.get $l4)
          (i32.const 16)))
      (local.set $l3
        (i32.add
          (local.get $l3)
          (i32.const 16)))
      (local.set $l1
        (f64.add
          (local.get $l1)
          (f64.const 0x1p+1 (;=2;))))
      (br_if $L0
        (i32.ne
          (local.tee $l2
            (i32.add
              (local.get $l2)
              (i32.const 2)))
          (i32.const 513))))
    (local.set $l7
      (i32.const 0))
    (loop $L1
      (local.set $l8
        (i32.add
          (local.get $l5)
          (i32.shl
            (local.get $l7)
            (i32.const 6))))
      (local.set $l1
        (f64.load
          (i32.add
            (i32.add
              (local.get $l0)
              (i32.const 16))
            (i32.shl
              (local.get $l7)
              (i32.const 3)))))
      (local.set $l9
        (i32.const 0))
      (local.set $l10
        (i32.const 0))
      (loop $L2
        (local.set $l11
          (f64.load offset=56
            (local.tee $l3
              (i32.add
                (i32.add
                  (local.get $l0)
                  (i32.const 4112))
                (i32.shl
                  (local.get $l10)
                  (i32.const 3))))))
        (local.set $l12
          (f64.load offset=48
            (local.get $l3)))
        (local.set $l13
          (f64.load offset=40
            (local.get $l3)))
        (local.set $l14
          (f64.load offset=32
            (local.get $l3)))
        (local.set $l15
          (f64.load offset=24
            (local.get $l3)))
        (local.set $l16
          (f64.load offset=16
            (local.get $l3)))
        (local.set $l17
          (f64.load offset=8
            (local.get $l3)))
        (local.set $l18
          (f64.load
            (local.get $l3)))
        (local.set $l6
          (f64.load offset=56
            (local.tee $l3
              (i32.add
                (i32.add
                  (local.get $l0)
                  (i32.const 4112))
                (i32.shl
                  (local.get $l9)
                  (i32.const 6))))))
        (local.set $l19
          (f64.load offset=48
            (local.get $l3)))
        (local.set $l20
          (f64.load offset=40
            (local.get $l3)))
        (local.set $l21
          (f64.load offset=32
            (local.get $l3)))
        (local.set $l22
          (f64.load offset=24
            (local.get $l3)))
        (local.set $l23
          (f64.load offset=16
            (local.get $l3)))
        (local.set $l24
          (f64.load offset=8
            (local.get $l3)))
        (local.set $l25
          (f64.load
            (local.get $l3)))
        (local.set $l4
          (i32.const 255))
        (local.set $l2
          (i32.const -4))
        (local.set $l26
          (local.get $l1))
        (local.set $l27
          (local.get $l1))
        (local.set $l28
          (local.get $l1))
        (local.set $l29
          (local.get $l1))
        (local.set $l30
          (local.get $l1))
        (local.set $l31
          (local.get $l1))
        (local.set $l32
          (local.get $l1))
        (local.set $l33
          (local.get $l1))
        (block $B3
          (loop $L4
            (local.set $l3
              (local.get $l2))
            (br_if $B3
              (i32.eqz
                (i32.and
                  (local.tee $l4
                    (i32.and
                      (select
                        (i32.const -2)
                        (i32.const -1)
                        (f64.gt
                          (f64.add
                            (local.tee $l34
                              (f64.mul
                                (local.get $l6)
                                (local.get $l6)))
                            (local.tee $l35
                              (f64.mul
                                (local.get $l26)
                                (local.get $l26))))
                          (f64.const 0x1p+2 (;=4;))))
                      (i32.and
                        (select
                          (i32.const -3)
                          (i32.const -1)
                          (f64.gt
                            (f64.add
                              (local.tee $l36
                                (f64.mul
                                  (local.get $l19)
                                  (local.get $l19)))
                              (local.tee $l37
                                (f64.mul
                                  (local.get $l27)
                                  (local.get $l27))))
                            (f64.const 0x1p+2 (;=4;))))
                        (i32.and
                          (select
                            (i32.const -5)
                            (i32.const -1)
                            (f64.gt
                              (f64.add
                                (local.tee $l38
                                  (f64.mul
                                    (local.get $l20)
                                    (local.get $l20)))
                                (local.tee $l39
                                  (f64.mul
                                    (local.get $l28)
                                    (local.get $l28))))
                              (f64.const 0x1p+2 (;=4;))))
                          (i32.and
                            (select
                              (i32.const -9)
                              (i32.const -1)
                              (f64.gt
                                (f64.add
                                  (local.tee $l40
                                    (f64.mul
                                      (local.get $l21)
                                      (local.get $l21)))
                                  (local.tee $l41
                                    (f64.mul
                                      (local.get $l29)
                                      (local.get $l29))))
                                (f64.const 0x1p+2 (;=4;))))
                            (i32.and
                              (select
                                (i32.const -17)
                                (i32.const -1)
                                (f64.gt
                                  (f64.add
                                    (local.tee $l42
                                      (f64.mul
                                        (local.get $l22)
                                        (local.get $l22)))
                                    (local.tee $l43
                                      (f64.mul
                                        (local.get $l30)
                                        (local.get $l30))))
                                  (f64.const 0x1p+2 (;=4;))))
                              (i32.and
                                (select
                                  (i32.const -33)
                                  (i32.const -1)
                                  (f64.gt
                                    (f64.add
                                      (local.tee $l44
                                        (f64.mul
                                          (local.get $l23)
                                          (local.get $l23)))
                                      (local.tee $l45
                                        (f64.mul
                                          (local.get $l31)
                                          (local.get $l31))))
                                    (f64.const 0x1p+2 (;=4;))))
                                (i32.and
                                  (select
                                    (i32.const -65)
                                    (i32.const -1)
                                    (f64.gt
                                      (f64.add
                                        (local.tee $l46
                                          (f64.mul
                                            (local.get $l24)
                                            (local.get $l24)))
                                        (local.tee $l47
                                          (f64.mul
                                            (local.get $l32)
                                            (local.get $l32))))
                                      (f64.const 0x1p+2 (;=4;))))
                                  (i32.and
                                    (select
                                      (i32.const 127)
                                      (i32.const -1)
                                      (f64.gt
                                        (f64.add
                                          (local.tee $l48
                                            (f64.mul
                                              (local.get $l25)
                                              (local.get $l25)))
                                          (local.tee $l49
                                            (f64.mul
                                              (local.get $l33)
                                              (local.get $l33))))
                                        (f64.const 0x1p+2 (;=4;))))
                                    (local.get $l4))))))))))
                  (i32.const 255))))
            (local.set $l2
              (i32.add
                (local.get $l3)
                (i32.const 1)))
            (local.set $l26
              (f64.add
                (f64.mul
                  (f64.add
                    (local.get $l6)
                    (local.get $l6))
                  (local.get $l26))
                (local.get $l1)))
            (local.set $l6
              (f64.add
                (local.get $l11)
                (f64.sub
                  (local.get $l34)
                  (local.get $l35))))
            (local.set $l27
              (f64.add
                (f64.mul
                  (f64.add
                    (local.get $l19)
                    (local.get $l19))
                  (local.get $l27))
                (local.get $l1)))
            (local.set $l19
              (f64.add
                (local.get $l12)
                (f64.sub
                  (local.get $l36)
                  (local.get $l37))))
            (local.set $l28
              (f64.add
                (f64.mul
                  (f64.add
                    (local.get $l20)
                    (local.get $l20))
                  (local.get $l28))
                (local.get $l1)))
            (local.set $l20
              (f64.add
                (local.get $l13)
                (f64.sub
                  (local.get $l38)
                  (local.get $l39))))
            (local.set $l29
              (f64.add
                (f64.mul
                  (f64.add
                    (local.get $l21)
                    (local.get $l21))
                  (local.get $l29))
                (local.get $l1)))
            (local.set $l21
              (f64.add
                (local.get $l14)
                (f64.sub
                  (local.get $l40)
                  (local.get $l41))))
            (local.set $l30
              (f64.add
                (f64.mul
                  (f64.add
                    (local.get $l22)
                    (local.get $l22))
                  (local.get $l30))
                (local.get $l1)))
            (local.set $l22
              (f64.add
                (local.get $l15)
                (f64.sub
                  (local.get $l42)
                  (local.get $l43))))
            (local.set $l31
              (f64.add
                (f64.mul
                  (f64.add
                    (local.get $l23)
                    (local.get $l23))
                  (local.get $l31))
                (local.get $l1)))
            (local.set $l23
              (f64.add
                (local.get $l16)
                (f64.sub
                  (local.get $l44)
                  (local.get $l45))))
            (local.set $l32
              (f64.add
                (f64.mul
                  (f64.add
                    (local.get $l24)
                    (local.get $l24))
                  (local.get $l32))
                (local.get $l1)))
            (local.set $l24
              (f64.add
                (local.get $l17)
                (f64.sub
                  (local.get $l46)
                  (local.get $l47))))
            (local.set $l33
              (f64.add
                (f64.mul
                  (f64.add
                    (local.get $l25)
                    (local.get $l25))
                  (local.get $l33))
                (local.get $l1)))
            (local.set $l25
              (f64.add
                (local.get $l18)
                (f64.sub
                  (local.get $l48)
                  (local.get $l49))))
            (br_if $L4
              (local.get $l3))))
        (i32.store8
          (i32.add
            (local.get $l8)
            (i32.shr_u
              (local.get $l10)
              (i32.const 3)))
          (local.get $l4))
        (local.set $l10
          (i32.add
            (local.get $l10)
            (i32.const 8)))
        (br_if $L2
          (i32.ne
            (local.tee $l9
              (i32.add
                (local.get $l9)
                (i32.const 1)))
            (i32.const 64))))
      (br_if $L1
        (i32.ne
          (local.tee $l7
            (i32.add
              (local.get $l7)
              (i32.const 1)))
          (i32.const 512))))
    (i64.store
      (local.get $l0)
      (i64.const 512))
    (i64.store offset=8
      (local.get $l0)
      (i64.const 512))
    (drop
      (call $fprintf
        (i32.load
          (i32.const 0))
        (i32.const 1024)
        (local.get $l0)))
    (drop
      (call $fwrite
        (local.get $l5)
        (i32.const 32768)
        (i32.const 1)
        (i32.load
          (i32.const 0))))
    (call $free
      (local.get $l5))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l0)
        (i32.const 8208)))
    (i32.const 0))
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66576))
  (export "memory" (memory $memory))
  (export "_start" (func $_start))
  (data $.rodata (i32.const 1024) "P4\0a%jd %jd\0a\00")
  (@custom "name" "\00\10\0fmandelbrot.wasm\01(\05\00\06malloc\01\07fprintf\02\06fwrite\03\04free\04\06_start\07\12\01\00\0f__stack_pointer\09\0a\01\00\07.rodata")
  (@custom "producers" "\01\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
