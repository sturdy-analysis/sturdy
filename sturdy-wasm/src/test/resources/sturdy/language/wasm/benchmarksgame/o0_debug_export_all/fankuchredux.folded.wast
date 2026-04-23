(module $fankuchredux.wasm
  (type $t0 (func (result i32)))
  (type $t1 (func (param i32 i32 i32) (result i32)))
  (type $t2 (func (param i32)))
  (type $t3 (func (param i32 i32) (result i32)))
  (type $t4 (func))
  (import "env" "__VERIFIER_nondet_int" (func $__VERIFIER_nondet_int (type $t0)))
  (import "env" "fprintf" (func $fprintf (type $t1)))
  (import "env" "exit" (func $exit (type $t2)))
  (import "env" "printf" (func $printf (type $t3)))
  (func $__wasm_call_ctors (type $t4))
  (func $flip (type $t0) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32)
    (local.set $l0
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (i32.store offset=8
      (local.get $l0)
      (i32.const 1168))
    (i32.store offset=4
      (local.get $l0)
      (i32.const 1232))
    (local.set $l1
      (i32.load offset=8
        (local.get $l0)))
    (local.set $l2
      (i32.load offset=4
        (local.get $l0)))
    (local.set $l3
      (i32.load offset=1296
        (i32.const 0)))
    (block $B0
      (br_if $B0
        (i32.eqz
          (local.get $l3)))
      (memory.copy
        (local.get $l1)
        (local.get $l2)
        (local.get $l3)))
    (i32.store offset=12
      (local.get $l0)
      (i32.const 1))
    (loop $L1
      (i32.store offset=8
        (local.get $l0)
        (i32.const 1168))
      (local.set $l4
        (i32.load offset=1168
          (i32.const 0)))
      (i32.store offset=4
        (local.get $l0)
        (i32.add
          (i32.const 1168)
          (i32.shl
            (local.get $l4)
            (i32.const 2))))
      (block $B2
        (loop $L3
          (br_if $B2
            (i32.eqz
              (i32.and
                (i32.lt_u
                  (i32.load offset=8
                    (local.get $l0))
                  (i32.load offset=4
                    (local.get $l0)))
                (i32.const 1))))
          (i32.store
            (local.get $l0)
            (i32.load
              (i32.load offset=8
                (local.get $l0))))
          (local.set $l5
            (i32.load
              (i32.load offset=4
                (local.get $l0))))
          (local.set $l6
            (i32.load offset=8
              (local.get $l0)))
          (i32.store offset=8
            (local.get $l0)
            (i32.add
              (local.get $l6)
              (i32.const 4)))
          (i32.store
            (local.get $l6)
            (local.get $l5))
          (local.set $l7
            (i32.load
              (local.get $l0)))
          (local.set $l8
            (i32.load offset=4
              (local.get $l0)))
          (i32.store offset=4
            (local.get $l0)
            (i32.add
              (local.get $l8)
              (i32.const -4)))
          (i32.store
            (local.get $l8)
            (local.get $l7))
          (br $L3)))
      (i32.store offset=12
        (local.get $l0)
        (i32.add
          (i32.load offset=12
            (local.get $l0))
          (i32.const 1)))
      (local.set $l9
        (i32.load offset=1168
          (i32.const 0)))
      (br_if $L1
        (i32.load
          (i32.add
            (i32.const 1168)
            (i32.shl
              (local.get $l9)
              (i32.const 2))))))
    (return
      (i32.load offset=12
        (local.get $l0))))
  (func $rotate (type $t2) (param $p0 i32)
    (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32)
    (local.set $l1
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (i32.store offset=12
      (local.get $l1)
      (local.get $p0))
    (i32.store offset=8
      (local.get $l1)
      (i32.load offset=1232
        (i32.const 0)))
    (i32.store offset=4
      (local.get $l1)
      (i32.const 1))
    (block $B0
      (loop $L1
        (br_if $B0
          (i32.eqz
            (i32.and
              (i32.le_s
                (i32.load offset=4
                  (local.get $l1))
                (i32.load offset=12
                  (local.get $l1)))
              (i32.const 1))))
        (local.set $l2
          (i32.load offset=4
            (local.get $l1)))
        (local.set $l3
          (i32.load
            (i32.add
              (i32.const 1232)
              (i32.shl
                (local.get $l2)
                (i32.const 2)))))
        (local.set $l4
          (i32.sub
            (i32.load offset=4
              (local.get $l1))
            (i32.const 1)))
        (i32.store
          (i32.add
            (i32.const 1232)
            (i32.shl
              (local.get $l4)
              (i32.const 2)))
          (local.get $l3))
        (i32.store offset=4
          (local.get $l1)
          (i32.add
            (i32.load offset=4
              (local.get $l1))
            (i32.const 1)))
        (br $L1)))
    (local.set $l5
      (i32.load offset=8
        (local.get $l1)))
    (local.set $l6
      (i32.load offset=12
        (local.get $l1)))
    (i32.store
      (i32.add
        (i32.const 1232)
        (i32.shl
          (local.get $l6)
          (i32.const 2)))
      (local.get $l5))
    (return))
  (func $tk (type $t2) (param $p0 i32)
    (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32)
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
      (i32.const 0))
    (block $B0
      (loop $L1
        (br_if $B0
          (i32.eqz
            (i32.and
              (i32.lt_s
                (i32.load offset=8
                  (local.get $l1))
                (i32.load offset=12
                  (local.get $l1)))
              (i32.const 1))))
        (call $rotate
          (i32.load offset=8
            (local.get $l1)))
        (local.set $l2
          (i32.load offset=8
            (local.get $l1)))
        (block $B2
          (br_if $B2
            (i32.eqz
              (i32.and
                (i32.ge_s
                  (i32.load
                    (i32.add
                      (i32.const 1088)
                      (i32.shl
                        (local.get $l2)
                        (i32.const 2))))
                  (i32.load offset=8
                    (local.get $l1)))
                (i32.const 1))))
          (local.set $l3
            (i32.load offset=8
              (local.get $l1)))
          (i32.store offset=8
            (local.get $l1)
            (i32.add
              (local.get $l3)
              (i32.const 1)))
          (i32.store
            (i32.add
              (i32.const 1088)
              (i32.shl
                (local.get $l3)
                (i32.const 2)))
            (i32.const 0))
          (br $L1))
        (local.set $l4
          (i32.load offset=8
            (local.get $l1)))
        (local.set $l5
          (i32.add
            (i32.const 1088)
            (i32.shl
              (local.get $l4)
              (i32.const 2))))
        (i32.store
          (local.get $l5)
          (i32.add
            (i32.load
              (local.get $l5))
            (i32.const 1)))
        (i32.store offset=8
          (local.get $l1)
          (i32.const 1))
        (local.set $l6
          (i32.xor
            (i32.load offset=1156
              (i32.const 0))
            (i32.const -1)))
        (i32.store offset=1156
          (i32.const 0)
          (local.get $l6))
        (block $B3
          (br_if $B3
            (i32.eqz
              (i32.load offset=1232
                (i32.const 0))))
          (local.set $l7
            (i32.load offset=1232
              (i32.const 0)))
          (block $B4
            (block $B5
              (br_if $B5
                (i32.eqz
                  (i32.load
                    (i32.add
                      (i32.const 1232)
                      (i32.shl
                        (local.get $l7)
                        (i32.const 2))))))
              (local.set $l8
                (call $flip))
              (br $B4))
            (local.set $l8
              (i32.const 1)))
          (i32.store offset=4
            (local.get $l1)
            (local.get $l8))
          (block $B6
            (br_if $B6
              (i32.eqz
                (i32.and
                  (i32.gt_s
                    (i32.load offset=4
                      (local.get $l1))
                    (i32.load offset=1152
                      (i32.const 0)))
                  (i32.const 1))))
            (local.set $l9
              (i32.load offset=4
                (local.get $l1)))
            (i32.store offset=1152
              (i32.const 0)
              (local.get $l9)))
          (block $B7
            (block $B8
              (br_if $B8
                (i32.eqz
                  (i32.load offset=1156
                    (i32.const 0))))
              (local.set $l10
                (i32.load offset=4
                  (local.get $l1)))
              (local.set $l11
                (i32.sub
                  (i32.const 0)
                  (local.get $l10)))
              (br $B7))
            (local.set $l11
              (i32.load offset=4
                (local.get $l1))))
          (local.set $l12
            (i32.add
              (local.get $l11)
              (i32.load offset=1160
                (i32.const 0))))
          (i32.store offset=1160
            (i32.const 0)
            (local.get $l12)))
        (br $L1)))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l1)
        (i32.const 16)))
    (return))
  (func $_start (type $t0) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32)
    (local.set $l0
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l0))
    (local.set $l1
      (call $__VERIFIER_nondet_int))
    (i32.store offset=1296
      (i32.const 0)
      (local.get $l1))
    (block $B0
      (block $B1
        (br_if $B1
          (i32.and
            (i32.lt_s
              (i32.load offset=1296
                (i32.const 0))
              (i32.const 3))
            (i32.const 1)))
        (br_if $B0
          (i32.eqz
            (i32.and
              (i32.gt_s
                (i32.load offset=1296
                  (i32.const 0))
                (i32.const 15))
              (i32.const 1)))))
      (drop
        (call $fprintf
          (i32.load
            (i32.const 0))
          (i32.const 1049)
          (i32.const 0)))
      (call $exit
        (i32.const 1))
      (unreachable))
    (i32.store offset=12
      (local.get $l0)
      (i32.const 0))
    (block $B2
      (loop $L3
        (br_if $B2
          (i32.eqz
            (i32.and
              (i32.lt_s
                (i32.load offset=12
                  (local.get $l0))
                (i32.load offset=1296
                  (i32.const 0)))
              (i32.const 1))))
        (local.set $l2
          (i32.load offset=12
            (local.get $l0)))
        (local.set $l3
          (i32.load offset=12
            (local.get $l0)))
        (i32.store
          (i32.add
            (i32.const 1232)
            (i32.shl
              (local.get $l3)
              (i32.const 2)))
          (local.get $l2))
        (i32.store offset=12
          (local.get $l0)
          (i32.add
            (i32.load offset=12
              (local.get $l0))
            (i32.const 1)))
        (br $L3)))
    (call $tk
      (i32.load offset=1296
        (i32.const 0)))
    (local.set $l4
      (i32.load offset=1160
        (i32.const 0)))
    (local.set $l5
      (i32.load offset=1296
        (i32.const 0)))
    (i32.store offset=8
      (local.get $l0)
      (i32.load offset=1152
        (i32.const 0)))
    (i32.store offset=4
      (local.get $l0)
      (local.get $l5))
    (i32.store
      (local.get $l0)
      (local.get $l4))
    (drop
      (call $printf
        (i32.const 1024)
        (local.get $l0)))
    (local.set $l6
      (i32.const 0))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l0)
        (i32.const 16)))
    (return
      (local.get $l6)))
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 66848))
  (global $t i32 (i32.const 1168))
  (global $s i32 (i32.const 1232))
  (global $max_n i32 (i32.const 1296))
  (global $c i32 (i32.const 1088))
  (global $odd i32 (i32.const 1156))
  (global $maxflips i32 (i32.const 1152))
  (global $checksum i32 (i32.const 1160))
  (global $__dso_handle i32 (i32.const 1024))
  (global $__data_end i32 (i32.const 1300))
  (global $__stack_low i32 (i32.const 1312))
  (global $__stack_high i32 (i32.const 66848))
  (global $__global_base i32 (i32.const 1024))
  (global $__heap_base i32 (i32.const 66848))
  (global $__heap_end i32 (i32.const 131072))
  (global $__memory_base i32 (i32.const 0))
  (global $__table_base i32 (i32.const 1))
  (global $__wasm_first_page_end i32 (i32.const 65536))
  (export "memory" (memory $memory))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "flip" (func $flip))
  (export "t" (global $t))
  (export "s" (global $s))
  (export "max_n" (global $max_n))
  (export "rotate" (func $rotate))
  (export "tk" (func $tk))
  (export "c" (global $c))
  (export "odd" (global $odd))
  (export "maxflips" (global $maxflips))
  (export "checksum" (global $checksum))
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
  (data $.rodata (i32.const 1024) "%d\0aPfannkuchen(%d) = %d\0a\00range: must be 3 <= n <= 12\0a\00")
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01U\17\00\00\024\00\03\0eI\13?\19:\0b;\0b\02\18\00\00\03\01\01I\13\00\00\04!\00I\137\0b\00\00\05\16\00I\13\03\0e:\0b;\0b\00\00\06$\00\03\0e>\0b\0b\0b\00\00\07$\00\03\0e\0b\0b>\0b\00\00\084\00I\13:\0b;\0b\02\18\00\00\09.\01\11\01\12\06@\18\03\0e:\0b;\0bI\13?\19\00\00\0a4\00\02\18\03\0e:\0b;\0bI\13\00\00\0b.\01\11\01\12\06@\18\03\0e:\0b;\0b'\19?\19\00\00\0c\05\00\02\18\03\0e:\0b;\0bI\13\00\00\0d\0f\00I\13\00\00\00")
  (@custom ".debug_info" "\f3\01\00\00\04\00\00\00\00\00\04\01\ef\00\00\00\1d\00\c5\00\00\00\00\00\00\006\00\00\00\00\00\00\00\00\00\00\00\02\d9\00\00\007\00\00\00\01\10\05\03@\04\00\00\03C\00\00\00\04U\00\00\00\10\00\05N\00\00\001\00\00\00\01\0d\06\0b\00\00\00\05\04\07\db\00\00\00\08\07\02\0f\00\00\00N\00\00\00\01\12\05\03\80\04\00\00\02\c1\00\00\00N\00\00\00\01\14\05\03\84\04\00\00\02(\00\00\00N\00\00\00\01\15\05\03\88\04\00\00\08\9c\00\00\00\01P\05\03\19\04\00\00\03\a8\00\00\00\04U\00\00\00\1d\00\06\18\00\00\00\06\01\08\bc\00\00\00\01W\05\03\00\04\00\00\03\a8\00\00\00\04U\00\00\00\19\00\02\16\00\00\007\00\00\00\01\0f\05\03\d0\04\00\00\02\0d\00\00\007\00\00\00\01\0f\05\03\90\04\00\00\02\22\00\00\00N\00\00\00\01\13\05\03\10\05\00\00\09\06\00\00\00\17\01\00\00\04\ed\00\00\9f\1d\00\00\00\01\17N\00\00\00\0a\02\91\0c\b6\00\00\00\01\19N\00\00\00\0a\02\91\08\02\00\00\00\01\1a\f1\01\00\00\0a\02\91\04\00\00\00\00\01\1a\f1\01\00\00\0a\02\91\00\d9\00\00\00\01\1aC\00\00\00\00\0b\1f\01\00\00\a4\00\00\00\04\ed\00\01\9f\ba\00\00\00\01*\0c\02\91\0c&\00\00\00\01*N\00\00\00\0a\02\91\08\d9\00\00\00\01,C\00\00\00\0a\02\91\04\b6\00\00\00\01-N\00\00\00\00\0b\c5\01\00\00\93\01\00\00\04\ed\00\01\9f\b3\00\00\00\014\0c\02\91\0c&\00\00\00\014N\00\00\00\0a\02\91\08\b6\00\00\00\016N\00\00\00\0a\02\91\04\b8\00\00\00\016N\00\00\00\00\09Z\03\00\00&\01\00\00\04\ed\00\00\9f\04\00\00\00\01JN\00\00\00\0a\02\91\0c\b6\00\00\00\01LN\00\00\00\00\0dC\00\00\00\00")
  (@custom ".debug_ranges" "\06\00\00\00\1d\01\00\00\1f\01\00\00\c3\01\00\00\c5\01\00\00X\03\00\00Z\03\00\00\80\04\00\00\00\00\00\00\00\00\00\00")
  (@custom ".debug_str" "y\00x\00_start\00int\00maxflips\00char\00flip\00max_n\00checksum\00elem\00/home/sve70582/Documents/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/o0_debug_export_all\00tk\00i\00f\00rotate\00odd\00../src/fankuchredux.c\00__ARRAY_SIZE_TYPE__\00clang version 21.1.7\00")
  (@custom ".debug_line" "d\02\00\00\04\00-\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01../src\00\00fankuchredux.c\00\01\00\00\00\00\05\02\06\00\00\00\03\17\01\05\06\0a\dc\ad\05\0b\ad\05\0e\06t\05\11t\05\04\ac\03`\08\12\05\06\06\03! \05\0ev\05\1b\06\c8\05\19\ac\05\15\d6\05!<\05%\90\05#X\05\07 \05\0f\06g\05\0e\06t\05\0c<\05\1a<\05\19X\05\14X\05\17\08\12\05$t\05\1ft\05\22\08\12\05\07\06s\06.\05\08\060\05\0f\c9\05\0d\06\ac\05\04\e4\05\0b\06=\05\04\06X\02\02\00\01\01\00\05\02\1f\01\00\00\03*\01\05\08\0a\08M\05\06\06\ac\05\0b\06=\05\10\06t\05\15\90\05\12X\05\04 \05(f\05&t\05\1f\08\12\05 X\05\1dX\05$\ba\05\19X\05\04\c8.\05\0b\06/\05\06\06t\05\04t\05\09\ba\05\01\06Y\02\02\00\01\01\00\05\02\c5\01\00\00\034\01\05\08\0a\08\bb\05\0bv\05\0f\06\90\05\0dX\05\04 \05\0e\06g\05\07\06X\05\0d\06g\05\0b\06t\05\13\08\12\05\10X\05\0d\06u\05\0a\06\08\12\05\11\ba\05\0a\06Y\06\03D.\05\09\06\03? \05\07\06t\05\0b\d6\05\09\06\c9\05\0eu\05\0d\06\90\05\0bX\06\ad\05\10\d7\05\0e\06\ac\05\18\08X\05\0e\82\03\bd\7f.\03\c3\00 \03\bd\7fJ\03\c3\00 \05\0cJ\05\0e\06=\05\12\06t\05\10\90\05't\05%t\03\bc\7f\ac\05\16\06\03\c5\00 \05\1d\06\f2\05\1ct\05\16t\03\bb\7f.\05!\03\c5\00 \03\bb\7ft\05\16\03\c5\00 \05\13.\03\bb\7f\08f\05\04\06\038 \05\01\03\10J\02\0d\00\01\01\00\05\02Z\03\00\00\03\ca\00\01\05\0c\0a\08[\05\0a\06\82\05\08\06\ad\05\0e\06\c8\05\12<\05\15X\05\1b\90\05\12<\03\b1\7ff\05\0f\06\03\d0\00 \05\07\06\90\06\e5\06\03\af\7f\90\05\0b\06\03\d4\00 \05\10\06t\05\14\90\05\12\90\05\04 \05'f\05\22t\05 t\05%\ba\05\1cX\05\04\c8.\05\07\06/\05\04\06\90\05)\06h\053\06\ac\05:\ac\05\04\ac\06\08\e6\02\13\00\01\01")
  (@custom "name" "\00\12\11fankuchredux.wasm\01\5c\09\00\15__VERIFIER_nondet_int\01\07fprintf\02\04exit\03\06printf\04\11__wasm_call_ctors\05\04flip\06\06rotate\07\02tk\08\06_start\07\12\01\00\0f__stack_pointer\09\0a\01\00\07.rodata")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
