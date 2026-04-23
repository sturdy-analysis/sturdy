(module $fasta.wasm
  (type $t0 (func (param i32) (result i32)))
  (type $t1 (func (result i32)))
  (type $t2 (func (param i32 i32) (result i32)))
  (type $t3 (func))
  (type $t4 (func (param f32) (result f32)))
  (type $t5 (func (param i32 i32)))
  (type $t6 (func (param i32 i32 i32)))
  (import "env" "strlen" (func $strlen (type $t0)))
  (import "env" "putchar" (func $putchar (type $t0)))
  (import "env" "__VERIFIER_nondet_int" (func $__VERIFIER_nondet_int (type $t1)))
  (import "env" "printf" (func $printf (type $t2)))
  (func $__wasm_call_ctors (type $t3))
  (func $fasta_rand (type $t4) (param $p0 f32) (result f32)
    (local $l1 i32) (local $l2 i32)
    (local.set $l1
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (f32.store offset=12
      (local.get $l1)
      (local.get $p0))
    (local.set $l2
      (i32.const 0))
    (i32.store offset=1584
      (local.get $l2)
      (i32.rem_u
        (i32.add
          (i32.mul
            (i32.load offset=1584
              (local.get $l2))
            (i32.const 3877))
          (i32.const 29573))
        (i32.const 139968)))
    (return
      (f32.div
        (f32.mul
          (f32.load offset=12
            (local.get $l1))
          (f32.convert_i32_u
            (i32.load offset=1584
              (local.get $l2))))
        (f32.const 0x1.116p+17 (;=139968;)))))
  (func $repeat_fasta (type $t5) (param $p0 i32) (param $p1 i32)
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
    (i32.store offset=4
      (local.get $l2)
      (call $strlen
        (i32.load offset=12
          (local.get $l2))))
    (i32.store
      (local.get $l2)
      (i32.const 0))
    (block $B0
      (loop $L1
        (br_if $B0
          (i32.eqz
            (i32.and
              (i32.lt_s
                (i32.load
                  (local.get $l2))
                (i32.load offset=8
                  (local.get $l2)))
              (i32.const 1))))
        (local.set $l3
          (i32.load8_u
            (i32.add
              (i32.load offset=12
                (local.get $l2))
              (i32.rem_s
                (i32.load
                  (local.get $l2))
                (i32.load offset=4
                  (local.get $l2))))))
        (local.set $l4
          (i32.const 24))
        (drop
          (call $putchar
            (i32.shr_s
              (i32.shl
                (local.get $l3)
                (local.get $l4))
              (local.get $l4))))
        (block $B2
          (br_if $B2
            (i32.eqz
              (i32.and
                (i32.eq
                  (i32.rem_s
                    (i32.load
                      (local.get $l2))
                    (i32.const 60))
                  (i32.const 59))
                (i32.const 1))))
          (drop
            (call $putchar
              (i32.const 10))))
        (i32.store
          (local.get $l2)
          (i32.add
            (i32.load
              (local.get $l2))
            (i32.const 1)))
        (br $L1)))
    (block $B3
      (br_if $B3
        (i32.eqz
          (i32.rem_s
            (i32.load
              (local.get $l2))
            (i32.const 60))))
      (drop
        (call $putchar
          (i32.const 10))))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l2)
        (i32.const 16)))
    (return))
  (func $random_fasta (type $t6) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 f64) (local $l5 i32) (local $l6 i32)
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
    (i32.store offset=16
      (local.get $l3)
      (call $strlen
        (i32.load offset=28
          (local.get $l3))))
    (i32.store offset=12
      (local.get $l3)
      (i32.const 0))
    (block $B0
      (loop $L1
        (br_if $B0
          (i32.eqz
            (i32.and
              (i32.lt_s
                (i32.load offset=12
                  (local.get $l3))
                (i32.load offset=20
                  (local.get $l3)))
              (i32.const 1))))
        (f64.store
          (local.get $l3)
          (f64.promote_f32
            (call $fasta_rand
              (f32.const 0x1p+0 (;=1;)))))
        (i32.store offset=8
          (local.get $l3)
          (i32.const 0))
        (block $B2
          (loop $L3
            (br_if $B2
              (i32.eqz
                (i32.and
                  (i32.lt_s
                    (i32.load offset=8
                      (local.get $l3))
                    (i32.sub
                      (i32.load offset=16
                        (local.get $l3))
                      (i32.const 1)))
                  (i32.const 1))))
            (local.set $l4
              (f64.load
                (i32.add
                  (i32.load offset=24
                    (local.get $l3))
                  (i32.shl
                    (i32.load offset=8
                      (local.get $l3))
                    (i32.const 3)))))
            (f64.store
              (local.get $l3)
              (f64.sub
                (f64.load
                  (local.get $l3))
                (local.get $l4)))
            (block $B4
              (br_if $B4
                (i32.eqz
                  (i32.and
                    (f64.lt
                      (f64.load
                        (local.get $l3))
                      (f64.convert_i32_s
                        (i32.const 0)))
                    (i32.const 1))))
              (br $B2))
            (i32.store offset=8
              (local.get $l3)
              (i32.add
                (i32.load offset=8
                  (local.get $l3))
                (i32.const 1)))
            (br $L3)))
        (local.set $l5
          (i32.load8_u
            (i32.add
              (i32.load offset=28
                (local.get $l3))
              (i32.load offset=8
                (local.get $l3)))))
        (local.set $l6
          (i32.const 24))
        (drop
          (call $putchar
            (i32.shr_s
              (i32.shl
                (local.get $l5)
                (local.get $l6))
              (local.get $l6))))
        (block $B5
          (br_if $B5
            (i32.eqz
              (i32.and
                (i32.eq
                  (i32.rem_s
                    (i32.load offset=12
                      (local.get $l3))
                    (i32.const 60))
                  (i32.const 59))
                (i32.const 1))))
          (drop
            (call $putchar
              (i32.const 10))))
        (i32.store offset=12
          (local.get $l3)
          (i32.add
            (i32.load offset=12
              (local.get $l3))
            (i32.const 1)))
        (br $L1)))
    (block $B6
      (br_if $B6
        (i32.eqz
          (i32.rem_s
            (i32.load offset=12
              (local.get $l3))
            (i32.const 60))))
      (drop
        (call $putchar
          (i32.const 10))))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l3)
        (i32.const 32)))
    (return))
  (func $_start (type $t1) (result i32)
    (local $l0 i32) (local $l1 i32) (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32)
    (local.set $l0
      (i32.sub
        (global.get $__stack_pointer)
        (i32.const 16)))
    (global.set $__stack_pointer
      (local.get $l0))
    (i32.store offset=12
      (local.get $l0)
      (call $__VERIFIER_nondet_int))
    (drop
      (call $printf
        (i32.const 1364)
        (i32.const 0)))
    (call $repeat_fasta
      (i32.load offset=1588
        (i32.const 0))
      (i32.shl
        (i32.load offset=12
          (local.get $l0))
        (i32.const 1)))
    (drop
      (call $printf
        (i32.const 1387)
        (i32.const 0)))
    (local.set $l1
      (i32.load offset=1592
        (i32.const 0)))
    (local.set $l2
      (i32.mul
        (i32.load offset=12
          (local.get $l0))
        (i32.const 3)))
    (call $random_fasta
      (local.get $l1)
      (i32.const 1424)
      (local.get $l2))
    (drop
      (call $printf
        (i32.const 1333)
        (i32.const 0)))
    (local.set $l3
      (i32.load offset=1596
        (i32.const 0)))
    (local.set $l4
      (i32.mul
        (i32.load offset=12
          (local.get $l0))
        (i32.const 5)))
    (call $random_fasta
      (local.get $l3)
      (i32.const 1552)
      (local.get $l4))
    (local.set $l5
      (i32.const 0))
    (global.set $__stack_pointer
      (i32.add
        (local.get $l0)
        (i32.const 16)))
    (return
      (local.get $l5)))
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 67136))
  (global $__dso_handle i32 (i32.const 1024))
  (global $__data_end i32 (i32.const 1600))
  (global $__stack_low i32 (i32.const 1600))
  (global $__stack_high i32 (i32.const 67136))
  (global $__global_base i32 (i32.const 1024))
  (global $__heap_base i32 (i32.const 67136))
  (global $__heap_end i32 (i32.const 131072))
  (global $__memory_base i32 (i32.const 0))
  (global $__table_base i32 (i32.const 1))
  (global $__wasm_first_page_end i32 (i32.const 65536))
  (export "memory" (memory $memory))
  (export "__wasm_call_ctors" (func $__wasm_call_ctors))
  (export "fasta_rand" (func $fasta_rand))
  (export "repeat_fasta" (func $repeat_fasta))
  (export "random_fasta" (func $random_fasta))
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
  (data $.rodata (i32.const 1024) "acgt\00acgtBDHKMNRSVWY\00GGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAA\00>THREE Homo sapiens frequency\0a\00>ONE Homo sapiens alu\0a\00>TWO IUB ambiguity codes\0a\00\00\00\00\00\00\00\00\00\00\00\00H\e1z\14\aeG\d1?\b8\1e\85\ebQ\b8\be?\b8\1e\85\ebQ\b8\be?H\e1z\14\aeG\d1?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?\00\00\00\00\00\00\00\00[\eb\ba \9dc\d3?\1bV\cd=\aeW\c9?\bf\c2\b6\ea:I\c9?8\08\03K\eeK\d3?")
  (data $.data (i32.const 1584) "*\00\00\00\15\04\00\00\05\04\00\00\00\04\00\00")
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01U\17\00\00\024\00I\13:\0b;\0b\02\18\00\00\03\01\01I\13\00\00\04!\00I\137\0b\00\00\05$\00\03\0e>\0b\0b\0b\00\00\06$\00\03\0e\0b\0b>\0b\00\00\074\00\03\0eI\13:\0b;\0b\02\18\00\00\08\16\00I\13\03\0e:\0b;\0b\00\00\09!\00I\137\05\00\00\0a\0f\00I\13\00\00\0b&\00I\13\00\00\0c.\01\11\01\12\06@\18\03\0e:\0b;\0b'\19I\13?\19\00\00\0d\05\00\02\18\03\0e:\0b;\0bI\13\00\00\0e.\01\11\01\12\06@\18\03\0e:\0b;\0b'\19?\19\00\00\0f4\00\02\18\03\0e:\0b;\0bI\13\00\00\10\0b\01\11\01\12\06\00\00\11.\01\11\01\12\06@\18\03\0e:\0b;\0bI\13?\19\00\00\00")
  (@custom ".debug_info" "\ad\02\00\00\04\00\00\00\00\00\04\01F\01\00\00\1d\00\00\01\00\00\00\00\00\00h\00\00\00\00\00\00\00\00\00\00\00\023\00\00\00\01b\05\03T\05\00\00\03?\00\00\00\04F\00\00\00\17\00\05G\00\00\00\06\01\062\01\00\00\08\07\02Z\00\00\00\01e\05\03k\05\00\00\03?\00\00\00\04F\00\00\00\1a\00\02s\00\00\00\01h\05\035\05\00\00\03?\00\00\00\04F\00\00\00\1f\00\07\fb\00\00\00\90\00\00\00\01\14\05\030\06\00\00\08\9b\00\00\002\00\00\00\02@\08\a6\00\00\000\00\00\00\02\17\05\1d\00\00\00\07\04\02\ba\00\00\00\01\1b\05\03\15\04\00\00\03?\00\00\00\09F\00\00\00 \01\00\07\12\00\00\00\d8\00\00\00\01\1a\05\034\06\00\00\0a\dd\00\00\00\0b?\00\00\00\02\ef\00\00\00\01#\05\03\05\04\00\00\03?\00\00\00\04F\00\00\00\10\00\07\0f\01\00\00\d8\00\00\00\01#\05\038\06\00\00\07^\00\00\00\1d\01\00\00\01$\05\03\90\05\00\00\03)\01\00\00\04F\00\00\00\0f\00\0b.\01\00\00\05\e9\00\00\00\04\08\02B\01\00\00\015\05\03\00\04\00\00\03?\00\00\00\04F\00\00\00\05\00\07;\00\00\00\d8\00\00\00\015\05\03<\06\00\00\07P\00\00\00p\01\00\00\016\05\03\10\06\00\00\03)\01\00\00\04F\00\00\00\04\00\0c\05\00\00\00Q\00\00\00\04\ed\00\01\9f\f0\00\00\00\01\15\98\02\00\00\0d\02\91\0c\0c\00\00\00\01\15\98\02\00\00\00\0eX\00\00\00\cb\00\00\00\04\ed\00\02\9f\18\01\00\00\01A\0d\02\91\0cL\00\00\00\01A\d8\00\00\00\0d\02\91\08f\00\00\00\01B\a6\02\00\00\0f\02\91\04d\00\00\00\01C\a6\02\00\00\0f\02\91\00\e7\00\00\00\01D\9f\02\00\00\00\0e%\01\00\00E\01\00\00\04\ed\00\03\9f%\01\00\00\01M\0d\02\91\1c\13\01\00\00\01M\d8\00\00\00\0d\02\91\18\00\00\00\00\01N\ab\02\00\00\0d\02\91\14f\00\00\00\01O\a6\02\00\00\0f\02\91\10d\00\00\00\01P\a6\02\00\00\0f\02\91\0c\e7\00\00\00\01Q\9f\02\00\00\0f\02\91\08\e5\00\00\00\01Q\9f\02\00\00\10\87\01\00\00\ad\00\00\00\0f\02\91\00\10\00\00\00\01S.\01\00\00\00\00\11l\02\00\00\c2\00\00\00\04\ed\00\00\9f\16\00\00\00\01_\9f\02\00\00\0f\02\91\0cf\00\00\00\01`\9f\02\00\00\00\05*\00\00\00\04\04\05&\00\00\00\05\04\0b\9f\02\00\00\0a)\01\00\00\00")
  (@custom ".debug_ranges" "\05\00\00\00V\00\00\00X\00\00\00#\01\00\00%\01\00\00j\02\00\00l\02\00\00.\03\00\00\00\00\00\00\00\00\00\00")
  (@custom ".debug_str" "probability\00max\00v\00alu\00_start\00unsigned int\00float\00__uint32_t\00homosapiens\00char\00seq\00homosapiens_p\00iub_p\00len\00/home/sve70582/Documents/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/o0_debug_export_all\00j\00i\00double\00fasta_rand\00seed\00../src/fasta.c\00iub\00symb\00repeat_fasta\00random_fasta\00__ARRAY_SIZE_TYPE__\00clang version 21.1.7\00")
  (@custom ".debug_line" "\00\02\00\00\04\00?\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01../src\00../src/../..\00\00fasta.c\00\01\00\00stdlib.h\00\02\00\00\00\00\05\02\05\00\00\00\03\14\01\05\0b\0a\08K\05\10\06\08 \05\15X\05\1cX\05\08 \05\0a\06u\05\10\06X\05\0e\9e\05\15 \05\03f\02\02\00\01\01\00\05\02X\00\00\00\03\c1\00\01\05\1a\0a\02$\13\05\13\06t\05\0df\05\09\06>\05\0d\06t\05\0f\90\05\0eX\05\03 \05\0d\06g\05\11\06X\05\15X\05\13X\05\0d \05\05\08 \05\09\06u\05\0b\06t\05\15<\05%\90\03\b9\7f\90\05\13\06\03\c5\00 \05\03\06\c8.\05\07\062\05\09\06t\05\13<\05\19<\03\b7\7f\90\05\01\06\03\ca\00 \02\0d\00\01\01\00\05\02%\01\00\00\03\ce\00\01\05\1a\0a\02/\13\05\13\06t\05\0df\05\09\06>\05\0d\06t\05\0f\90\05\0eX\05\03 \03\ae\7ff\05\10\06\03\d3\00t\05\0c\06t\05\0b\06>\05\0f\06t\05\11\90\05\14X\05\10<\05\05 \05\0c\06g\05\18\06X\05\0cX\05\09\90\05\0b\06\c9\05\0c\06t\05\10\9e\03\a9\7f.\05\19\06\03\d5\00 \05\05\06\c8\05\10\060\05\0d0\05\12\06X\05\0dX\05\05\08 \05\09\06u\05\0b\06t\05\15<\05%\90\03\a6\7f\90\05\13\06\03\d2\00 \05\03\06\c8.\05\07\06\03\0a.\05\09\06t\05\13<\05\19<\03\a4\7f\90\05\01\06\03\dd\00 \02\0d\00\01\01\00\05\02l\02\00\00\03\de\00\01\05\0b\0a\08Y\05\07\06\82\05\03\06>\05\10\e5\05\15\06\90\05\16X\05\03<\06h\05\10\e5\05\1c\06\ac\05\1dX\05\03X\06\f4\05\10\e5\05,\06\ac\05-X\05\03X\06\f4\02\13\00\01\01")
  (@custom "name" "\00\0b\0afasta.wasm\01t\09\00\06strlen\01\07putchar\02\15__VERIFIER_nondet_int\03\06printf\04\11__wasm_call_ctors\05\0afasta_rand\06\0crepeat_fasta\07\0crandom_fasta\08\06_start\07\12\01\00\0f__stack_pointer\09\11\02\00\07.rodata\01\05.data")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
