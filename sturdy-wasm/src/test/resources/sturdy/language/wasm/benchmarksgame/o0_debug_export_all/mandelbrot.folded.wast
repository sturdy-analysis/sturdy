(module $mandelbrot.wasm
  (type $t0 (func (param i32) (result i32)))
  (type $t1 (func (param i32 i32 i32) (result i32)))
  (type $t2 (func (param i32 i32 i32 i32) (result i32)))
  (type $t3 (func (param i32)))
  (type $t4 (func))
  (type $t5 (func (result i32)))
  (import "env" "malloc" (func $malloc (type $t0)))
  (import "env" "fprintf" (func $fprintf (type $t1)))
  (import "env" "fwrite" (func $fwrite (type $t2)))
  (import "env" "free" (func $free (type $t3)))
  (func $__wasm_call_ctors (type $t4))
  (func $_start (type $t5) (result i32)
    (local $l0 i32) (local $l1 f64) (local $l2 i32) (local $l3 f64) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 f64) (local $l8 i32) (local $l9 f64) (local $l10 i32) (local $l11 i32) (local $l12 i32) (local $l13 f64) (local $l14 f64) (local $l15 f64) (local $l16 i32) (local $l17 f64) (local $l18 i32) (local $l19 f64) (local $l20 f64) (local $l21 f64) (local $l22 i32) (local $l23 f64) (local $l24 f64) (local $l25 i32) (local $l26 i32) (local $l27 i32) (local $l28 i32) (local $l29 i32) (local $l30 i64) (local $l31 i32) (local $l32 i32) (local $l33 i32)
    (local.set $l0
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 8432)))
    (global.set $__stack_pointer
      (local.get $l0))
    (i32.store offset=8428
      (local.get $l0)
      (i32.const 512))
    (i32.store offset=8424
      (local.get $l0)
      (call $malloc
        (i32.const 32768)))
    (i32.store offset=8420
      (local.get $l0)
      (local.get $l0))
    (i32.store offset=8416
      (local.get $l0)
      (i32.const 0))
    (block $B0
      (loop $L1
        (br_if $B0
          (i32.eqz
            (i32.and
              (i32.lt_s
                (i32.load offset=8416
                  (local.get $l0))
                (i32.const 512))
              (i32.const 1))))
        (local.set $l1
          (f64.add
            (f64.mul
              (f64.convert_i32_s
                (i32.load offset=8416
                  (local.get $l0)))
              (f64.const 0x1p-8 (;=0.00390625;)))
            (f64.const -0x1.8p+0 (;=-1.5;))))
        (local.set $l2
          (i32.load offset=8416
            (local.get $l0)))
        (f64.store
          (i32.add
            (i32.add
              (local.get $l0)
              (i32.const 4112))
            (i32.shl
              (local.get $l2)
              (i32.const 3)))
          (local.get $l1))
        (local.set $l3
          (f64.add
            (f64.mul
              (f64.convert_i32_s
                (i32.load offset=8416
                  (local.get $l0)))
              (f64.const 0x1p-8 (;=0.00390625;)))
            (f64.const -0x1p+0 (;=-1;))))
        (local.set $l4
          (i32.load offset=8416
            (local.get $l0)))
        (f64.store
          (i32.add
            (i32.add
              (local.get $l0)
              (i32.const 16))
            (i32.shl
              (local.get $l4)
              (i32.const 3)))
          (local.get $l3))
        (i32.store offset=8416
          (local.get $l0)
          (i32.add
            (i32.load offset=8416
              (local.get $l0))
            (i32.const 1)))
        (br $L1)))
    (i32.store offset=8412
      (local.get $l0)
      (i32.const 0))
    (block $B2
      (loop $L3
        (br_if $B2
          (i32.eqz
            (i32.and
              (i32.lt_s
                (i32.load offset=8412
                  (local.get $l0))
                (i32.const 512))
              (i32.const 1))))
        (local.set $l5
          (i32.load offset=8412
            (local.get $l0)))
        (f64.store offset=8400
          (local.get $l0)
          (f64.load
            (i32.add
              (i32.add
                (local.get $l0)
                (i32.const 16))
              (i32.shl
                (local.get $l5)
                (i32.const 3)))))
        (i32.store offset=8396
          (local.get $l0)
          (i32.const 0))
        (block $B4
          (loop $L5
            (br_if $B4
              (i32.eqz
                (i32.and
                  (i32.lt_s
                    (i32.load offset=8396
                      (local.get $l0))
                    (i32.const 512))
                  (i32.const 1))))
            (i32.store offset=8252
              (local.get $l0)
              (i32.const 0))
            (block $B6
              (loop $L7
                (br_if $B6
                  (i32.eqz
                    (i32.and
                      (i32.lt_s
                        (i32.load offset=8252
                          (local.get $l0))
                        (i32.const 8))
                      (i32.const 1))))
                (local.set $l6
                  (i32.add
                    (i32.load offset=8396
                      (local.get $l0))
                    (i32.load offset=8252
                      (local.get $l0))))
                (local.set $l7
                  (f64.load
                    (i32.add
                      (i32.add
                        (local.get $l0)
                        (i32.const 4112))
                      (i32.shl
                        (local.get $l6)
                        (i32.const 3)))))
                (local.set $l8
                  (i32.load offset=8252
                    (local.get $l0)))
                (f64.store
                  (i32.add
                    (i32.add
                      (local.get $l0)
                      (i32.const 8320))
                    (i32.shl
                      (local.get $l8)
                      (i32.const 3)))
                  (local.get $l7))
                (local.set $l9
                  (f64.load offset=8400
                    (local.get $l0)))
                (local.set $l10
                  (i32.load offset=8252
                    (local.get $l0)))
                (f64.store
                  (i32.add
                    (i32.add
                      (local.get $l0)
                      (i32.const 8256))
                    (i32.shl
                      (local.get $l10)
                      (i32.const 3)))
                  (local.get $l9))
                (i32.store offset=8252
                  (local.get $l0)
                  (i32.add
                    (i32.load offset=8252
                      (local.get $l0))
                    (i32.const 1)))
                (br $L7)))
            (i32.store8 offset=8251
              (local.get $l0)
              (i32.const 255))
            (i32.store offset=8244
              (local.get $l0)
              (i32.const 5))
            (loop $L8
              (i32.store8 offset=8243
                (local.get $l0)
                (i32.const 128))
              (i32.store offset=8236
                (local.get $l0)
                (i32.const 0))
              (block $B9
                (loop $L10
                  (br_if $B9
                    (i32.eqz
                      (i32.and
                        (i32.lt_s
                          (i32.load offset=8236
                            (local.get $l0))
                          (i32.const 8))
                        (i32.const 1))))
                  (local.set $l11
                    (i32.load offset=8236
                      (local.get $l0)))
                  (f64.store offset=8224
                    (local.get $l0)
                    (f64.load
                      (i32.add
                        (i32.add
                          (local.get $l0)
                          (i32.const 8320))
                        (i32.shl
                          (local.get $l11)
                          (i32.const 3)))))
                  (local.set $l12
                    (i32.load offset=8236
                      (local.get $l0)))
                  (f64.store offset=8216
                    (local.get $l0)
                    (f64.load
                      (i32.add
                        (i32.add
                          (local.get $l0)
                          (i32.const 8256))
                        (i32.shl
                          (local.get $l12)
                          (i32.const 3)))))
                  (local.set $l13
                    (f64.load offset=8224
                      (local.get $l0)))
                  (local.set $l14
                    (f64.load offset=8224
                      (local.get $l0)))
                  (local.set $l15
                    (f64.add
                      (f64.neg
                        (f64.mul
                          (f64.load offset=8216
                            (local.get $l0))
                          (f64.load offset=8216
                            (local.get $l0))))
                      (f64.mul
                        (local.get $l13)
                        (local.get $l14))))
                  (local.set $l16
                    (i32.add
                      (i32.load offset=8396
                        (local.get $l0))
                      (i32.load offset=8236
                        (local.get $l0))))
                  (local.set $l17
                    (f64.add
                      (local.get $l15)
                      (f64.load
                        (i32.add
                          (i32.add
                            (local.get $l0)
                            (i32.const 4112))
                          (i32.shl
                            (local.get $l16)
                            (i32.const 3))))))
                  (local.set $l18
                    (i32.load offset=8236
                      (local.get $l0)))
                  (f64.store
                    (i32.add
                      (i32.add
                        (local.get $l0)
                        (i32.const 8320))
                      (i32.shl
                        (local.get $l18)
                        (i32.const 3)))
                    (local.get $l17))
                  (local.set $l19
                    (f64.mul
                      (f64.load offset=8224
                        (local.get $l0))
                      (f64.const 0x1p+1 (;=2;))))
                  (local.set $l20
                    (f64.load offset=8216
                      (local.get $l0)))
                  (local.set $l21
                    (f64.add
                      (f64.load offset=8400
                        (local.get $l0))
                      (f64.mul
                        (local.get $l19)
                        (local.get $l20))))
                  (local.set $l22
                    (i32.load offset=8236
                      (local.get $l0)))
                  (f64.store
                    (i32.add
                      (i32.add
                        (local.get $l0)
                        (i32.const 8256))
                      (i32.shl
                        (local.get $l22)
                        (i32.const 3)))
                    (local.get $l21))
                  (local.set $l23
                    (f64.load offset=8224
                      (local.get $l0)))
                  (local.set $l24
                    (f64.load offset=8224
                      (local.get $l0)))
                  (block $B11
                    (br_if $B11
                      (i32.eqz
                        (i32.and
                          (f64.gt
                            (f64.add
                              (f64.mul
                                (f64.load offset=8216
                                  (local.get $l0))
                                (f64.load offset=8216
                                  (local.get $l0)))
                              (f64.mul
                                (local.get $l23)
                                (local.get $l24)))
                            (f64.const 0x1p+2 (;=4;)))
                          (i32.const 1))))
                    (i32.store8 offset=8251
                      (local.get $l0)
                      (i32.and
                        (i32.xor
                          (i32.and
                            (i32.load8_u offset=8243
                              (local.get $l0))
                            (i32.const 255))
                          (i32.const -1))
                        (i32.and
                          (i32.load8_u offset=8251
                            (local.get $l0))
                          (i32.const 255)))))
                  (i32.store8 offset=8243
                    (local.get $l0)
                    (i32.shr_s
                      (i32.and
                        (i32.load8_u offset=8243
                          (local.get $l0))
                        (i32.const 255))
                      (i32.const 1)))
                  (i32.store offset=8236
                    (local.get $l0)
                    (i32.add
                      (i32.load offset=8236
                        (local.get $l0))
                      (i32.const 1)))
                  (br $L10)))
              (local.set $l25
                (i32.and
                  (i32.load8_u offset=8251
                    (local.get $l0))
                  (i32.const 255)))
              (local.set $l26
                (i32.const 0))
              (block $B12
                (br_if $B12
                  (i32.eqz
                    (local.get $l25)))
                (local.set $l27
                  (i32.add
                    (i32.load offset=8244
                      (local.get $l0))
                    (i32.const -1)))
                (i32.store offset=8244
                  (local.get $l0)
                  (local.get $l27))
                (local.set $l26
                  (i32.ne
                    (local.get $l27)
                    (i32.const 0))))
              (br_if $L8
                (i32.and
                  (local.get $l26)
                  (i32.const 1))))
            (local.set $l28
              (i32.load8_u offset=8251
                (local.get $l0)))
            (i32.store8
              (i32.add
                (i32.load offset=8424
                  (local.get $l0))
                (i32.add
                  (i32.div_s
                    (i32.shl
                      (i32.load offset=8412
                        (local.get $l0))
                      (i32.const 9))
                    (i32.const 8))
                  (i32.div_s
                    (i32.load offset=8396
                      (local.get $l0))
                    (i32.const 8))))
              (local.get $l28))
            (i32.store offset=8396
              (local.get $l0)
              (i32.add
                (i32.load offset=8396
                  (local.get $l0))
                (i32.const 8)))
            (br $L5)))
        (i32.store offset=8412
          (local.get $l0)
          (i32.add
            (i32.load offset=8412
              (local.get $l0))
            (i32.const 1)))
        (br $L3)))
    (local.set $l29
      (i32.load
        (i32.const 0)))
    (local.set $l30
      (i64.const 512))
    (i64.store offset=8
      (local.get $l0)
      (local.get $l30))
    (i64.store
      (local.get $l0)
      (local.get $l30))
    (drop
      (call $fprintf
        (local.get $l29)
        (i32.const 1024)
        (local.get $l0)))
    (local.set $l31
      (i32.load offset=8424
        (local.get $l0)))
    (local.set $l32
      (i32.load
        (i32.const 0)))
    (drop
      (call $fwrite
        (local.get $l31)
        (i32.const 32768)
        (i32.const 1)
        (local.get $l32)))
    (call $free
      (i32.load offset=8424
        (local.get $l0)))
    (local.set $l0
      (i32.load offset=8420
        (local.get $l0)))
    (local.set $l33
      (i32.const 0))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l0)
        (i32.const 8432)))
    (return
      (local.get $l33)))
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66576))
  (global $__dso_handle i32 (i32.const 1024))
  (global $__data_end i32 (i32.const 1036))
  (global $__stack_low i32 (i32.const 1040))
  (global $__stack_high i32 (i32.const 66576))
  (global $__global_base i32 (i32.const 1024))
  (global $__heap_base i32 (i32.const 66576))
  (global $__heap_end i32 (i32.const 131072))
  (global $__memory_base i32 (i32.const 0))
  (global $__table_base i32 (i32.const 1))
  (global $__wasm_first_page_end i32 (i32.const 65536))
  (export "memory" (memory $memory))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
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
  (data $.rodata (i32.const 1024) "P4\0a%jd %jd\0a\00")
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01\12\06\00\00\024\00I\13:\0b;\0b\02\18\00\00\03\01\01I\13\00\00\04!\00I\137\0b\00\00\05$\00\03\0e>\0b\0b\0b\00\00\06$\00\03\0e\0b\0b>\0b\00\00\07\16\00I\13\03\0e:\0b;\0b\00\00\08.\01\11\01\12\06@\18\03\0e:\0b;\0bI\13?\19\00\00\094\00\02\18\03\0e:\0b;\0bI\13\00\00\0a\0b\01\11\01\12\06\00\00\0b\0b\01U\17\00\00\0c&\00I\13\00\00\0d\0f\00I\13\00\00\0e!\00I\137\05\00\00\00")
  (@custom ".debug_info" "Q\02\00\00\04\00\00\00\00\00\04\01\9c\01\00\00\1d\00t\01\00\00\00\00\00\00\a3\00\00\00\06\00\00\00m\04\00\00\023\00\00\00\01R\05\03\00\04\00\00\03?\00\00\00\04F\00\00\00\0c\00\05|\00\00\00\06\01\06\88\01\00\00\08\07\07X\00\00\00'\00\00\00\02w\07c\00\00\00%\00\00\00\025\05c\01\00\00\05\08\08\06\00\00\00m\04\00\00\04\ed\00\00\9f\03\00\00\00\01\12\e6\01\00\00\09\04\91\ec\c1\00\0e\00\00\00\01\14\ed\01\00\00\09\04\91\e8\c1\00O\00\00\00\01\1c\08\02\00\00\09\03\91\90 \8f\00\00\00\01!/\02\00\00\09\02\91\10D\01\00\00\01!/\02\00\00\0ac\00\00\00\9a\00\00\00\09\04\91\e0\c1\00\00\00\00\00\01#\f2\01\00\00\00\0a\fd\00\00\00\fa\02\00\00\09\04\91\dc\c1\00\01\00\00\00\01)\f2\01\00\00\0a\19\01\00\00\cb\02\00\00\09\04\91\d0\c1\00N\01\00\00\01*C\02\00\00\0a5\01\00\00\af\02\00\00\09\04\91\cc\c1\00k\00\00\00\01+\f2\01\00\00\0aQ\01\00\00\80\02\00\00\09\04\91\80\c1\00\81\00\00\00\010H\02\00\00\09\04\91\c0\c0\006\01\00\00\010H\02\00\00\09\04\91\bb\c0\00V\00\00\00\017\12\02\00\00\09\04\91\b4\c0\00\99\00\00\00\019\f2\01\00\00\0aQ\01\00\00\8a\00\00\00\09\04\91\bc\c0\00c\00\00\00\011\f2\01\00\00\00\0b\00\00\00\00\09\04\91\b3\c0\00 \01\00\00\01;\12\02\00\00\0a\f7\01\00\00v\01\00\00\09\04\91\ac\c0\00c\00\00\00\01<\f2\01\00\00\0a\12\02\00\00H\01\00\00\09\04\91\a0\c0\00\97\00\00\00\01=C\02\00\00\09\04\91\98\c0\00a\01\00\00\01>C\02\00\00\00\00\00\00\00\00\00\00\05\0a\00\00\00\05\04\0c\f2\01\00\00\07\fd\01\00\009\00\00\00\01\10\07\e6\01\00\000\00\00\00\02p\0c\0d\02\00\00\0d\12\02\00\00\07\1d\02\00\00G\00\00\00\02>\07(\02\00\00E\00\00\00\02\13\05s\00\00\00\08\01\03<\02\00\00\0eF\00\00\00\00\02\00\05m\01\00\00\04\08\0c<\02\00\00\03<\02\00\00\04F\00\00\00\08\00\00")
  (@custom ".debug_ranges" "\e6\01\00\00g\03\00\00\9b\03\00\00\a1\03\00\00\00\00\00\00\00\00\00\00")
  (@custom ".debug_str" "xy\00_start\00int\00image_Width_And_Height\00__intmax_t\00intptr_t\00intnative_t\00__uint8_t\00pixels\00eight_Pixels\00x_Minor\00x_Major\00unsigned char\00pixel_Group_r\00initial_r\00iteration\00/home/sve70582/Documents/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/o0_debug_export_all\00current_Pixel_Bitmask\00pixel_Group_i\00initial_i\00prefetched_Initial_i\00long long\00double\00../src/mandelbrot.c\00__ARRAY_SIZE_TYPE__\00clang version 21.1.7\00")
  (@custom ".debug_line" ")\02\00\00\04\00D\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01../src\00../src/../..\00\00mandelbrot.c\00\01\00\00stdlib.h\00\02\00\00\00\00\05\02\06\00\00\00\03\11\01\05\16\0a\02<\14\05\1b\98\05\14\06\ba\05\04\06O\05\14\84\05\1a\06\82\05\1c\9e\05\04J\050\06g\053\06\f2\05\11\c8\05\07\82\05\14\ba\050\06Y\053\06\f2\05\11\c8\05\07\82\05\14\ac\057\06V\05\04\06\e4.\05\14\064\05\19\06\82\05\1a\9e\05\04J\053\06g\05)\06\82\05\14\f2\05\17\06K\05\22\06\82\05)\9e\05\07J\05\1a\06l\05%\06\82\05,\9e\05\0a<\05.\06g\056\06f\055f\05$<\05\1b\08\12\05\0d\82\05#\c8\05$\06Y\05\1b\06\82\05\0d\82\05#\c8\057\06V\05\0a\06\e4.\05\12\064\05\16\92\05\15\84\05\1d\ad\05(\06\82\05/\9e\05\0d<\05-\06g\05\1f\06\82\05\1d\08 \05-\06K\05\1f\06\82\05\1d\08 \05'\06L\05)\06\82\05-\82\05/f\05.f\05+ \05\1c\06\91\05$\06f\05#f\05\12<\051\06\ff\05\1e\06<\05\10\82\05&\c8\05+\06Z\05*\06f\05-\ba\051\82\05/f\05\1e\82\05\10\82\05&\c8\05\13\06\5c\05\15\06\82\05\19\82\05\1b\82\05\1af\05\17 \05\1cf\05\22\06\f3\05!\06\ba\05\1f<\03\b9\7f\e4\05%\06\03\c9\00 \05:\03s\08.\05\0d\06\e4.\05\11\06\03\0f.\05\1e\06\ba\05!\ac\05\1e\08.\03\b5\7ft\05\0a\03\cb\00<\059\06h\05\0a\06\82\05\11f\05\12f\05)<\05.<\055f\05,<\05\0a \058 \05I\06\03^X\05\07\06\e4.\054\06,\05\04\06\e4.\05\0c\06\03).\05\04\06\f2\05\0b\06\08\d7\05G\06\82\05\04\ac\05\09\06\08\14\05\04\06f\05\01\06i\02\1d\00\01\01")
  (@custom "name" "\00\10\0fmandelbrot.wasm\01;\06\00\06malloc\01\07fprintf\02\06fwrite\03\04free\04\11__wasm_call_ctors\05\06_start\07\12\01\00\0f__stack_pointer\09\0a\01\00\07.rodata")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
