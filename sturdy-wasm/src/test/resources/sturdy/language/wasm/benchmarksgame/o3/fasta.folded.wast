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
    (local.set $l2
      (call $strlen
        (local.get $p0)))
    (block $B0
      (br_if $B0
        (i32.lt_s
          (local.get $p1)
          (i32.const 1)))
      (local.set $l3
        (i32.const 0))
      (loop $L1
        (drop
          (call $putchar
            (i32.load8_s
              (i32.add
                (local.get $p0)
                (i32.rem_s
                  (local.get $l3)
                  (local.get $l2))))))
        (block $B2
          (br_if $B2
            (i32.ne
              (i32.add
                (i32.mul
                  (i32.div_u
                    (local.get $l3)
                    (i32.const 60))
                  (i32.const 60))
                (i32.const 59))
              (local.get $l3)))
          (drop
            (call $putchar
              (i32.const 10))))
        (br_if $L1
          (i32.ne
            (local.get $p1)
            (local.tee $l3
              (i32.add
                (local.get $l3)
                (i32.const 1))))))
      (br_if $B0
        (i32.eqz
          (i32.rem_u
            (local.get $p1)
            (i32.const 60))))
      (drop
        (call $putchar
          (i32.const 10)))))
  (func $random_fasta (type $t3) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 f64)
    (block $B0
      (br_if $B0
        (i32.lt_s
          (local.get $p2)
          (i32.const 1)))
      (block $B1
        (block $B2
          (br_if $B2
            (i32.gt_s
              (local.tee $l3
                (call $strlen
                  (local.get $p0)))
              (i32.const 1)))
          (local.set $l3
            (i32.const 59))
          (local.set $l4
            (local.get $p2))
          (local.set $l5
            (i32.const 0))
          (loop $L3
            (i32.store offset=1572
              (i32.const 0)
              (i32.rem_u
                (i32.add
                  (i32.mul
                    (i32.load offset=1572
                      (i32.const 0))
                    (i32.const 3877))
                  (i32.const 29573))
                (i32.const 139968)))
            (drop
              (call $putchar
                (i32.load8_s
                  (local.get $p0))))
            (block $B4
              (br_if $B4
                (i32.add
                  (local.get $l3)
                  (i32.mul
                    (i32.div_u
                      (local.get $l5)
                      (i32.const 60))
                    (i32.const 60))))
              (drop
                (call $putchar
                  (i32.const 10))))
            (local.set $l3
              (i32.add
                (local.get $l3)
                (i32.const -1)))
            (local.set $l5
              (i32.add
                (local.get $l5)
                (i32.const 1)))
            (br_if $L3
              (local.tee $l4
                (i32.add
                  (local.get $l4)
                  (i32.const -1))))
            (br $B1)))
        (local.set $l4
          (i32.add
            (local.get $l3)
            (i32.const -1)))
        (local.set $l6
          (i32.const 0))
        (loop $L5
          (i32.store offset=1572
            (i32.const 0)
            (local.tee $l3
              (i32.rem_u
                (i32.add
                  (i32.mul
                    (i32.load offset=1572
                      (i32.const 0))
                    (i32.const 3877))
                  (i32.const 29573))
                (i32.const 139968))))
          (local.set $l7
            (f64.promote_f32
              (f32.div
                (f32.convert_i32_u
                  (local.get $l3))
                (f32.const 0x1.116p+17 (;=139968;)))))
          (local.set $l3
            (local.get $p1))
          (local.set $l5
            (i32.const 0))
          (block $B6
            (loop $L7
              (br_if $B6
                (f64.lt
                  (local.tee $l7
                    (f64.sub
                      (local.get $l7)
                      (f64.load
                        (local.get $l3))))
                  (f64.const 0x0p+0 (;=0;))))
              (local.set $l3
                (i32.add
                  (local.get $l3)
                  (i32.const 8)))
              (br_if $L7
                (i32.ne
                  (local.get $l4)
                  (local.tee $l5
                    (i32.add
                      (local.get $l5)
                      (i32.const 1))))))
            (local.set $l5
              (local.get $l4)))
          (drop
            (call $putchar
              (i32.load8_s
                (i32.add
                  (local.get $p0)
                  (local.get $l5)))))
          (block $B8
            (br_if $B8
              (i32.ne
                (i32.rem_u
                  (local.get $l6)
                  (i32.const 60))
                (i32.const 59)))
            (drop
              (call $putchar
                (i32.const 10))))
          (br_if $L5
            (i32.ne
              (local.tee $l6
                (i32.add
                  (local.get $l6)
                  (i32.const 1)))
              (local.get $p2)))))
      (br_if $B0
        (i32.eqz
          (i32.rem_u
            (local.get $p2)
            (i32.const 60))))
      (drop
        (call $putchar
          (i32.const 10)))))
  (func $_start (type $t1) (result i32)
    (local $l0 i32)
    (local.set $l0
      (call $__VERIFIER_nondet_int))
    (drop
      (call $puts
        (i32.const 1214)))
    (call $repeat_fasta
      (i32.const 1282)
      (i32.shl
        (local.get $l0)
        (i32.const 1)))
    (drop
      (call $puts
        (i32.const 1241)))
    (call $random_fasta
      (i32.const 1266)
      (i32.const 1024)
      (i32.mul
        (local.get $l0)
        (i32.const 3)))
    (drop
      (call $puts
        (i32.const 1184)))
    (call $random_fasta
      (i32.const 1236)
      (i32.const 1152)
      (i32.mul
        (local.get $l0)
        (i32.const 5)))
    (i32.const 0))
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 67120))
  (export "memory" (memory $memory))
  (export "_start" (func $_start))
  (data $.rodata (i32.const 1024) "H\e1z\14\aeG\d1?\b8\1e\85\ebQ\b8\be?\b8\1e\85\ebQ\b8\be?H\e1z\14\aeG\d1?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?{\14\aeG\e1z\94?\00\00\00\00\00\00\00\00[\eb\ba \9dc\d3?\1bV\cd=\aeW\c9?\bf\c2\b6\ea:I\c9?8\08\03K\eeK\d3?>THREE Homo sapiens frequency\00>ONE Homo sapiens alu\00acgt\00>TWO IUB ambiguity codes\00acgtBDHKMNRSVWY\00GGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAA\00")
  (data $.data (i32.const 1572) "*\00\00\00")
  (@custom "name" "\00\0b\0afasta.wasm\01S\07\00\06strlen\01\07putchar\02\15__VERIFIER_nondet_int\03\04puts\04\0crepeat_fasta\05\0crandom_fasta\06\06_start\07\12\01\00\0f__stack_pointer\09\11\02\00\07.rodata\01\05.data")
  (@custom "producers" "\01\0cprocessed-by\01\05clang\0621.1.7")
  (@custom "target_features" "\08+\0bbulk-memory+\0fbulk-memory-opt+\16call-indirect-overlong+\0amultivalue+\0fmutable-globals+\13nontrapping-fptoint+\0freference-types+\08sign-ext"))
