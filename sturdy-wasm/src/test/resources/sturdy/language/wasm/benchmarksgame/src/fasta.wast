(module
  (type $t0 (func (param i32) (result i32)))
  (type $t1 (func (result i32)))
  (type $t2 (func))
  (type $t3 (func (param f32) (result f32)))
  (type $t4 (func (param i32 i32)))
  (type $t5 (func (param i32 i32 i32)))
  (import "env" "strlen" (func $env.strlen (type $t0)))
  (import "env" "putchar" (func $env.putchar (type $t0)))
  (import "env" "__VERIFIER_nondet_int" (func $env.__VERIFIER_nondet_int (type $t1)))
  (import "env" "puts" (func $env.puts (type $t0)))
  (func $__wasm_call_ctors (type $t2))
  (func $fasta_rand (type $t3) (param $p0 f32) (result f32)
    (local $l1 i32)
    i32.const 1572
    i32.const 1572
    i32.load
    i32.const 3877
    i32.mul
    i32.const 29573
    i32.add
    i32.const 139968
    i32.rem_u
    local.tee $l1
    i32.store
    local.get $l1
    f32.convert_i32_u
    local.get $p0
    f32.mul
    f32.const 0x1.116p+17 (;=139968;)
    f32.div)
  (func $repeat_fasta (type $t4) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32)
    local.get $p0
    call $env.strlen
    local.set $l3
    block $B0
      local.get $p1
      i32.const 0
      i32.le_s
      br_if $B0
      loop $L1
        local.get $p0
        local.get $l2
        local.get $l3
        i32.rem_s
        i32.add
        i32.load8_s
        call $env.putchar
        drop
        local.get $l2
        local.get $l2
        i32.const 60
        i32.div_u
        i32.const 60
        i32.mul
        i32.const 59
        i32.add
        i32.eq
        if $I2
          i32.const 10
          call $env.putchar
          drop
        end
        local.get $p1
        local.get $l2
        i32.const 1
        i32.add
        local.tee $l2
        i32.ne
        br_if $L1
      end
      local.get $p1
      i32.const 60
      i32.rem_u
      i32.eqz
      br_if $B0
      i32.const 10
      call $env.putchar
      drop
    end)
  (func $random_fasta (type $t5) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 f64)
    block $B0
      local.get $p2
      i32.const 0
      i32.le_s
      br_if $B0
      block $B1
        local.get $p0
        call $env.strlen
        local.tee $l5
        i32.const 1
        i32.le_s
        if $I2
          i32.const 59
          local.set $l3
          local.get $p2
          local.set $p1
          loop $L3
            i32.const 1572
            i32.const 1572
            i32.load
            i32.const 3877
            i32.mul
            i32.const 29573
            i32.add
            i32.const 139968
            i32.rem_u
            i32.store
            local.get $p0
            i32.load8_s
            call $env.putchar
            drop
            local.get $l3
            local.get $l4
            i32.const 60
            i32.div_u
            i32.const 60
            i32.mul
            i32.add
            i32.eqz
            if $I4
              i32.const 10
              call $env.putchar
              drop
            end
            local.get $l3
            i32.const 1
            i32.sub
            local.set $l3
            local.get $l4
            i32.const 1
            i32.add
            local.set $l4
            local.get $p1
            i32.const 1
            i32.sub
            local.tee $p1
            br_if $L3
          end
          br $B1
        end
        local.get $l5
        i32.const 1
        i32.sub
        local.set $l5
        loop $L5
          i32.const 1572
          i32.const 1572
          i32.load
          i32.const 3877
          i32.mul
          i32.const 29573
          i32.add
          i32.const 139968
          i32.rem_u
          local.tee $l3
          i32.store
          local.get $l3
          f32.convert_i32_u
          f32.const 0x1.116p+17 (;=139968;)
          f32.div
          f64.promote_f32
          local.set $l7
          local.get $p1
          local.set $l3
          i32.const 0
          local.set $l4
          block $B6
            loop $L7
              local.get $l7
              local.get $l3
              f64.load
              f64.sub
              local.tee $l7
              f64.const 0x0p+0 (;=0;)
              f64.lt
              br_if $B6
              local.get $l3
              i32.const 8
              i32.add
              local.set $l3
              local.get $l5
              local.get $l4
              i32.const 1
              i32.add
              local.tee $l4
              i32.ne
              br_if $L7
            end
            local.get $l5
            local.set $l4
          end
          local.get $p0
          local.get $l4
          i32.add
          i32.load8_s
          call $env.putchar
          drop
          local.get $l6
          i32.const 60
          i32.rem_u
          i32.const 59
          i32.eq
          if $I8
            i32.const 10
            call $env.putchar
            drop
          end
          local.get $l6
          i32.const 1
          i32.add
          local.tee $l6
          local.get $p2
          i32.ne
          br_if $L5
        end
      end
      local.get $p2
      i32.const 60
      i32.rem_u
      i32.eqz
      br_if $B0
      i32.const 10
      call $env.putchar
      drop
    end)
  (func $_start (type $t1) (result i32)
    (local $l0 i32)
    call $env.__VERIFIER_nondet_int
    local.set $l0
    i32.const 1214
    call $env.puts
    drop
    i32.const 1282
    local.get $l0
    i32.const 1
    i32.shl
    call $repeat_fasta
    i32.const 1241
    call $env.puts
    drop
    i32.const 1266
    i32.const 1024
    local.get $l0
    i32.const 3
    i32.mul
    call $random_fasta
    i32.const 1184
    call $env.puts
    drop
    i32.const 1236
    i32.const 1152
    local.get $l0
    i32.const 5
    i32.mul
    call $random_fasta
    i32.const 0)
  (memory $memory 2)
  (global $__dso_handle i32 (i32.const 1024))
  (global $__data_end i32 (i32.const 1576))
  (global $__stack_low i32 (i32.const 1584))
  (global $__stack_high i32 (i32.const 67120))
  (global $__global_base i32 (i32.const 1024))
  (global $__heap_base i32 (i32.const 67120))
  (global $__heap_end i32 (i32.const 131072))
  (global $__memory_base i32 (i32.const 0))
  (global $__table_base i32 (i32.const 1))
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
  (data $d0 (i32.const 1024) "H\e1z\14\aeG\d1?\b8\1e\85\ebQ\b8\be?\b8\1e\85\ebQ\b8\be?H\e1z\14\aeG\d1?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?\00\00\00\00\00\00\00\00[\eb\ba \9dc\d3?\1bV\cd=\aeW\c9?\bf\c2\b6\ea:I\c9?8\08\03K\eeK\d3?>THREE Homo sapiens frequency\00>ONE Homo sapiens alu\00acgt\00>TWO IUB ambiguity codes\00acgtBDHKMNRSVWY\00GGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAA")
  (data $d1 (i32.const 1572) "*")
  (@custom ".debug_loc" "\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\00\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\03\00\11\00\9f\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\03\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\02\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\03\00\11\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\05\9f\01\00\00\00\01\00\00\00\03\00\11\00\9f\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\06\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\03\9f\01\00\00\00\01\00\00\00\04\00\ed\00\03\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\06\00\9e\04\00\00\80?\01\00\00\00\01\00\00\00\06\00\9e\04\00\00\80?\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\07\9f\01\00\00\00\01\00\00\00\04\00\ed\02\00\9f\01\00\00\00\01\00\00\00\04\00\ed\00\07\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\03\00\11\00\9f\01\00\00\00\01\00\00\00\03\00\11\00\9f\01\00\00\00\01\00\00\00\04\00\ed\02\01\9f\01\00\00\00\01\00\00\00\04\00\ed\00\05\9f\00\00\00\00\00\00\00\00\ff\ff\ff\ff\00\00\00\00\01\00\00\00\01\00\00\00\04\00\ed\00\00\9f\00\00\00\00\00\00\00\00")
  (@custom ".debug_abbrev" "\01\11\01%\0e\13\05\03\0e\10\17\1b\0e\11\01U\17\00\00\024\00I\13:\0b;\0b\00\00\03\01\01I\13\00\00\04!\00I\137\0b\00\00\05$\00\03\0e>\0b\0b\0b\00\00\06$\00\03\0e\0b\0b>\0b\00\00\074\00\03\0eI\13:\0b;\0b\02\18\00\00\08\16\00I\13\03\0e:\0b;\0b\00\00\094\00I\13:\0b;\0b\02\18\00\00\0a!\00I\137\05\00\00\0b4\00\03\0eI\13:\0b;\0b\00\00\0c\0f\00I\13\00\00\0d&\00I\13\00\00\0e.\01\11\01\12\06@\18\97B\191\13\00\00\0f\05\00\02\171\13\00\00\10.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0b'\19?\19\00\00\11\05\00\02\18\03\0e:\0b;\0bI\13\00\00\124\00\02\17\03\0e:\0b;\0bI\13\00\00\13\89\82\01\001\13\11\01\00\00\14.\01\03\0e:\0b;\0b'\19I\13<\19?\19\00\00\15\05\00I\13\00\00\16.\01\03\0e:\0b;\0b'\19I\13?\19 \0b\00\00\17\05\00\03\0e:\0b;\0bI\13\00\00\18\0b\01U\17\00\00\19\1d\011\13U\17X\0bY\0bW\0b\00\00\1a.\01\11\01\12\06@\18\97B\19\03\0e:\0b;\0bI\13?\19\00\00\00")
  (@custom ".debug_info" "c\03\00\00\04\00\00\00\00\00\04\015\01\00\00\1d\00\f6\00\00\00\00\00\00\00\97\00\00\00\00\00\00\008\00\00\00\02-\00\00\00\01b\039\00\00\00\04@\00\00\00\17\00\05J\00\00\00\06\01\06!\01\00\00\08\07\02N\00\00\00\01e\039\00\00\00\04@\00\00\00\1a\00\02a\00\00\00\01h\039\00\00\00\04@\00\00\00\1f\00\07\92\00\00\00~\00\00\00\01\14\05\03$\06\00\00\08\89\00\00\002\00\00\00\02@\08\94\00\00\000\00\00\00\02\17\05\1d\00\00\00\07\04\09\a8\00\00\00\01\1b\05\03\02\05\00\00\039\00\00\00\0a@\00\00\00 \01\00\0b\12\00\00\00\c0\00\00\00\01\1a\0c\c5\00\00\00\0d9\00\00\00\09\d7\00\00\00\01#\05\03\f2\04\00\00\039\00\00\00\04@\00\00\00\10\00\0b\fe\00\00\00\c0\00\00\00\01#\07a\00\00\00\ff\00\00\00\01$\05\03\00\04\00\00\03\0b\01\00\00\04@\00\00\00\0f\00\0d\10\01\00\00\05\80\00\00\00\04\08\09$\01\00\00\015\05\03\d4\04\00\00\039\00\00\00\04@\00\00\00\05\00\0b;\00\00\00\c0\00\00\00\015\07S\00\00\00L\01\00\00\016\05\03\80\04\00\00\03\0b\01\00\00\04@\00\00\00\04\00\0e\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9f!\02\00\00\0f\00\00\00\00-\02\00\00\00\10\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9f\07\01\00\00\01A\11\04\ed\00\00\9fO\00\00\00\01A\c0\00\00\00\11\04\ed\00\01\9fl\00\00\00\01B\5c\03\00\00\12\1e\00\00\00p\00\00\00\01D\1a\02\00\00\12W\00\00\00j\00\00\00\01C\5c\03\00\00\13\f1\01\00\00\00\00\00\00\13\09\02\00\00\00\00\00\00\13\09\02\00\00\00\00\00\00\13\09\02\00\00\00\00\00\00\00\14g\00\00\00\02\db\02\02\00\00\15\c0\00\00\00\00\05r\00\00\00\07\04\14G\00\00\00\02\ee\1a\02\00\00\15\1a\02\00\00\00\05&\00\00\00\05\04\16\87\00\00\00\01\159\02\00\00\01\17\0c\00\00\00\01\159\02\00\00\00\05*\00\00\00\04\04\10\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9f\14\01\00\00\01M\11\04\ed\00\00\9f\02\01\00\00\01M\c0\00\00\00\11\04\ed\00\01\9f\00\00\00\00\01Na\03\00\00\11\04\ed\00\02\9fl\00\00\00\01O\5c\03\00\00\12u\00\00\00p\00\00\00\01Q\1a\02\00\00\12\c9\00\00\00j\00\00\00\01P\5c\03\00\00\12m\01\00\00n\00\00\00\01Q\1a\02\00\00\18\00\00\00\00\123\01\00\00\10\00\00\00\01S\10\01\00\00\19!\02\00\00\18\00\00\00\01S\10\0f\03\01\00\00-\02\00\00\00\00\13\f1\01\00\00\00\00\00\00\13\09\02\00\00\00\00\00\00\13\09\02\00\00\00\00\00\00\13\09\02\00\00\00\00\00\00\13\09\02\00\00\00\00\00\00\13\09\02\00\00\00\00\00\00\00\1a\00\00\00\00\00\00\00\00\07\ed\03\00\00\00\00\9f\16\00\00\00\01_\1a\02\00\00\12\b3\01\00\00l\00\00\00\01`\1a\02\00\00\13w\01\00\00\00\00\00\00\13@\02\00\00\00\00\00\00\13@\02\00\00\00\00\00\00\00\0d\1a\02\00\00\0c\0b\01\00\00\00")
  (@custom ".debug_ranges" "\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\01\00\00\00\00\00\00\00\00\00\00\00")
  (@custom ".debug_str" "probability\00max\00v\00alu\00_start\00unsigned int\00float\00__uint32_t\00homosapiens\00putchar\00seq\00homosapiens_p\00iub_p\00strlen\00j\00i\00unsigned long\00double\00fasta_rand\00seed\00/home/sven/sturdy.scala/sturdy-wasm/src/test/resources/sturdy/language/wasm/benchmarksgame/src\00fasta.c\00iub\00symb\00repeat_fasta\00random_fasta\00__ARRAY_SIZE_TYPE__\00clang version 19.1.7\00")
  (@custom ".debug_line" "9\00\00\00\04\003\00\00\00\01\01\01\fb\0e\0d\00\01\01\01\01\00\00\00\01\00\00\01./../..\00\00fasta.c\00\00\00\00stdlib.h\00\01\00\00\00")
  (@custom "producers" "\02\08language\01\03C11\00\0cprocessed-by\01\05clang\0619.1.7")
  (@custom "target_features" "\04+\0fmutable-globals+\08sign-ext+\0freference-types+\0amultivalue"))
