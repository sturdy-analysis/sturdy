(module $fasta.wasm
  (type $t0 (func (param i32) (result i32)))
  (type $t1 (func (result i32)))
  (type $t2 (func (param i32 i32)))
  (type $t3 (func (param i32 i32 i32)))
  (import "env" "strlen" (func $strlen (type $t0)))
  (import "env" "putchar" (func $putchar (type $t0)))
  (import "env" "__VERIFIER_nondet_int" (func $__VERIFIER_nondet_int (type $t1)))
  (import "env" "puts" (func $puts (type $t0)))
  (func $repeat_fasta (type $t2) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32)
    local.get $p0
    call $strlen
    local.set $l2
    block $B0
      local.get $p1
      i32.const 1
      i32.lt_s
      br_if $B0
      i32.const 0
      local.set $l3
      loop $L1
        local.get $p0
        local.get $l3
        local.get $l2
        i32.rem_s
        i32.add
        i32.load8_s
        call $putchar
        drop
        block $B2
          local.get $l3
          i32.const 60
          i32.div_u
          i32.const 60
          i32.mul
          i32.const 59
          i32.add
          local.get $l3
          i32.ne
          br_if $B2
          i32.const 10
          call $putchar
          drop
        end
        local.get $p1
        local.get $l3
        i32.const 1
        i32.add
        local.tee $l3
        i32.ne
        br_if $L1
      end
      local.get $p1
      i32.const 60
      i32.rem_u
      i32.eqz
      br_if $B0
      i32.const 10
      call $putchar
      drop
    end)
  (func $random_fasta (type $t3) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 f64)
    block $B0
      local.get $p2
      i32.const 1
      i32.lt_s
      br_if $B0
      block $B1
        block $B2
          local.get $p0
          call $strlen
          local.tee $l3
          i32.const 1
          i32.gt_s
          br_if $B2
          i32.const 59
          local.set $l3
          local.get $p2
          local.set $l4
          i32.const 0
          local.set $l5
          loop $L3
            i32.const 0
            i32.const 0
            i32.load offset=1572
            i32.const 3877
            i32.mul
            i32.const 29573
            i32.add
            i32.const 139968
            i32.rem_u
            i32.store offset=1572
            local.get $p0
            i32.load8_s
            call $putchar
            drop
            block $B4
              local.get $l3
              local.get $l5
              i32.const 60
              i32.div_u
              i32.const 60
              i32.mul
              i32.add
              br_if $B4
              i32.const 10
              call $putchar
              drop
            end
            local.get $l3
            i32.const -1
            i32.add
            local.set $l3
            local.get $l5
            i32.const 1
            i32.add
            local.set $l5
            local.get $l4
            i32.const -1
            i32.add
            local.tee $l4
            br_if $L3
            br $B1
          end
        end
        local.get $l3
        i32.const -1
        i32.add
        local.set $l4
        i32.const 0
        local.set $l6
        loop $L5
          i32.const 0
          i32.const 0
          i32.load offset=1572
          i32.const 3877
          i32.mul
          i32.const 29573
          i32.add
          i32.const 139968
          i32.rem_u
          local.tee $l3
          i32.store offset=1572
          local.get $l3
          f32.convert_i32_u
          f32.const 0x1.116p+17 (;=139968;)
          f32.div
          f64.promote_f32
          local.set $l7
          local.get $p1
          local.set $l3
          i32.const 0
          local.set $l5
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
              local.get $l4
              local.get $l5
              i32.const 1
              i32.add
              local.tee $l5
              i32.ne
              br_if $L7
            end
            local.get $l4
            local.set $l5
          end
          local.get $p0
          local.get $l5
          i32.add
          i32.load8_s
          call $putchar
          drop
          block $B8
            local.get $l6
            i32.const 60
            i32.rem_u
            i32.const 59
            i32.ne
            br_if $B8
            i32.const 10
            call $putchar
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
      call $putchar
      drop
    end)
  (func $_start (type $t1) (result i32)
    (local $l0 i32)
    call $__VERIFIER_nondet_int
    local.set $l0
    i32.const 1214
    call $puts
    drop
    i32.const 1282
    local.get $l0
    i32.const 1
    i32.shl
    call $repeat_fasta
    i32.const 1241
    call $puts
    drop
    i32.const 1266
    i32.const 1024
    local.get $l0
    i32.const 3
    i32.mul
    call $random_fasta
    i32.const 1184
    call $puts
    drop
    i32.const 1236
    i32.const 1152
    local.get $l0
    i32.const 5
    i32.mul
    call $random_fasta
    i32.const 0)
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 67120))
  (export "memory" (memory $memory))
  (export "_start" (func $_start))
  (data $.rodata (i32.const 1024) "H\e1z\14\aeG\d1?\b8\1e\85\ebQ\b8\be?\b8\1e\85\ebQ\b8\be?H\e1z\14\aeG\d1?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?\00\00\00\00\00\00\00\00[\eb\ba \9dc\d3?\1bV\cd=\aeW\c9?\bf\c2\b6\ea:I\c9?8\08\03K\eeK\d3?>THREE Homo sapiens frequency\00>ONE Homo sapiens alu\00acgt\00>TWO IUB ambiguity codes\00acgtBDHKMNRSVWY\00GGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAA\00")
  (data $.data (i32.const 1572) "*\00\00\00")
  (@custom "name" "\00\0b\0afasta.wasm\01S\07\00\06strlen\01\07putchar\02\15__VERIFIER_nondet_int\03\04puts\04\0crepeat_fasta\05\0crandom_fasta\06\06_start\07\12\01\00\0f__stack_pointer\09\11\02\00\07.rodata\01\05.data")
  (@custom "producers" "\01\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
