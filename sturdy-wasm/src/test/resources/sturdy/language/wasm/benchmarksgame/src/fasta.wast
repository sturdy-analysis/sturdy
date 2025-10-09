(module $fasta.wasm
  (type $t0 (func (param i32 i32 i32) (result i32)))
  (type $t1 (func))
  (type $t2 (func (param i32 i32) (result i32)))
  (type $t3 (func (param i32) (result i32)))
  (type $t4 (func (param i32 i32 i32 i32) (result i32)))
  (type $t5 (func (param i32)))
  (type $t6 (func (result i32)))
  (type $t7 (func (param i32 i32)))
  (type $t8 (func (param i32 i32 i32)))
  (import "env" "fputs" (func $fputs (type $t2)))
  (import "env" "memcpy" (func $memcpy (type $t0)))
  (import "env" "strlen" (func $strlen (type $t3)))
  (import "env" "fwrite" (func $fwrite (type $t4)))
  (import "env" "exit" (func $exit (type $t5)))
  (func $__wasm_call_ctors (type $t1)
    nop)
  (func $_start (type $t6) (result i32)
    (local $l0 i32) (local $l1 i32)
    global.get $__stack_pointer
    i32.const 464
    i32.sub
    local.tee $l0
    global.set $__stack_pointer
    local.get $l0
    i32.const 2500
    i32.store offset=460
    i32.const 1055
    i32.const 0
    i32.load
    call $fputs
    drop
    local.get $l0
    i32.const 160
    i32.add
    i32.const 1104
    i32.const 288
    call $memcpy
    drop
    local.get $l0
    i32.const 160
    i32.add
    i32.const 5000
    call $repeat_And_Wrap_String
    call $rng_init
    call $out_init
    i32.const 1078
    i32.const 0
    i32.load
    call $fputs
    drop
    local.get $l0
    i32.const 32
    i32.add
    i32.const 1392
    i32.const 120
    call $memcpy
    drop
    local.get $l0
    i32.const 32
    i32.add
    i32.const 15
    i32.const 7500
    call $generate_And_Wrap_Pseudorandom_DNA_Sequence
    i32.const 1024
    i32.const 0
    i32.load
    call $fputs
    drop
    local.get $l0
    i32.const 24
    i32.add
    i32.const 0
    local.tee $l1
    i64.load offset=1544
    i64.store
    local.get $l0
    i32.const 16
    i32.add
    local.get $l1
    i64.load offset=1536
    i64.store
    local.get $l0
    local.get $l1
    i64.load offset=1528
    i64.store offset=8
    local.get $l0
    local.get $l1
    i64.load offset=1520
    i64.store
    local.get $l0
    i32.const 4
    i32.const 12500
    call $generate_And_Wrap_Pseudorandom_DNA_Sequence
    local.get $l0
    i32.const 464
    i32.add
    global.set $__stack_pointer
    i32.const 0)
  (func $repeat_And_Wrap_String (type $t7) (param $p0 i32) (param $p1 i32)
    (local $l2 i32) (local $l3 i32) (local $l4 i32) (local $l5 i32)
    global.get $__stack_pointer
    i32.const 112
    i32.sub
    local.tee $l3
    local.set $l2
    local.get $l3
    global.set $__stack_pointer
    local.get $l2
    local.get $p0
    i32.store offset=108
    local.get $l2
    local.get $p1
    i32.store offset=104
    local.get $l2
    local.get $l2
    i32.load offset=108
    call $strlen
    i32.store offset=100
    local.get $l2
    i32.load offset=100
    local.set $l4
    local.get $l2
    local.get $l3
    i32.store offset=96
    local.get $l3
    local.get $l4
    i32.const 75
    i32.add
    i32.const -16
    i32.and
    i32.sub
    local.tee $l5
    local.tee $l3
    global.set $__stack_pointer
    local.get $l2
    local.get $l4
    i32.const 60
    i32.add
    i32.store offset=92
    local.get $l2
    i32.const 0
    i32.store offset=88
    loop $L0
      local.get $l2
      i32.load offset=88
      local.get $l2
      i32.load offset=100
      i32.const 60
      i32.add
      i32.ge_s
      i32.eqz
      if $I1
        local.get $l2
        i32.load offset=88
        local.get $l5
        i32.add
        local.get $l2
        i32.load offset=108
        local.get $l2
        i32.load offset=88
        local.get $l2
        i32.load offset=100
        i32.rem_s
        i32.add
        i32.load8_u
        i32.store8
        local.get $l2
        local.get $l2
        i32.load offset=88
        i32.const 1
        i32.add
        i32.store offset=88
        br $L0
      end
    end
    local.get $l2
    i32.const 0
    i32.store offset=84
    local.get $l2
    i32.const 10
    i32.store8 offset=76
    local.get $l2
    local.get $l2
    i32.load offset=104
    i32.store offset=12
    loop $L2
      local.get $l2
      i32.load offset=12
      i32.const 0
      i32.gt_s
      if $I3
        local.get $l2
        i32.const 60
        i32.store offset=8
        local.get $l2
        i32.load offset=12
        i32.const 60
        i32.lt_s
        if $I4
          local.get $l2
          local.get $l2
          i32.load offset=12
          i32.store offset=8
          local.get $l2
          i32.load offset=8
          local.get $l2
          i32.const 16
          i32.add
          i32.add
          i32.const 10
          i32.store8
        end
        local.get $l2
        i32.const 16
        i32.add
        local.get $l2
        i32.load offset=84
        local.get $l5
        i32.add
        local.get $l2
        i32.load offset=8
        call $memcpy
        drop
        local.get $l2
        local.get $l2
        i32.load offset=8
        local.get $l2
        i32.load offset=84
        i32.add
        i32.store offset=84
        local.get $l2
        i32.load offset=84
        local.get $l2
        i32.load offset=100
        i32.gt_s
        if $I5
          local.get $l2
          local.get $l2
          i32.load offset=84
          local.get $l2
          i32.load offset=100
          i32.sub
          i32.store offset=84
        end
        local.get $l2
        i32.const 16
        i32.add
        local.get $l2
        i32.load offset=8
        i32.const 1
        i32.add
        i32.const 1
        i32.const 0
        i32.load
        call $fwrite
        drop
        local.get $l2
        local.get $l2
        i32.load offset=12
        local.get $l2
        i32.load offset=8
        i32.sub
        i32.store offset=12
        br $L2
      end
    end
    local.get $l2
    i32.load offset=96
    local.set $l3
    local.get $l2
    i32.const 112
    i32.add
    global.set $__stack_pointer)
  (func $rng_init (type $t1)
    i32.const 1568
    i32.const 0
    i32.store)
  (func $out_init (type $t1)
    i32.const 1568
    i32.const 0
    i32.store)
  (func $generate_And_Wrap_Pseudorandom_DNA_Sequence (type $t8) (param $p0 i32) (param $p1 i32) (param $p2 i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 f32) (local $l8 i32) (local $l9 i32) (local $l10 i32) (local $l11 i32) (local $l12 i32)
    global.get $__stack_pointer
    i32.const 308304
    i32.sub
    local.tee $l4
    local.set $l3
    local.get $l4
    global.set $__stack_pointer
    local.get $l3
    local.get $p0
    i32.store offset=308300
    local.get $l3
    local.get $p1
    i32.store offset=308296
    local.get $l3
    local.get $p2
    i32.store offset=308292
    local.get $l3
    i32.load offset=308296
    local.set $l5
    local.get $l3
    local.get $l4
    i32.store offset=308288
    local.get $l4
    local.get $l5
    i32.const 2
    i32.shl
    i32.const 15
    i32.add
    i32.const -16
    i32.and
    i32.sub
    local.tee $l6
    local.tee $l4
    global.set $__stack_pointer
    local.get $l3
    local.get $l5
    i32.store offset=308284
    local.get $l3
    f32.const 0x0p+0 (;=0;)
    f32.store offset=308280
    local.get $l3
    i32.const 0
    i32.store offset=308276
    loop $L0
      local.get $l3
      i32.load offset=308276
      local.get $l3
      i32.load offset=308296
      i32.ge_s
      i32.eqz
      if $I1
        local.get $l3
        local.get $l3
        f32.load offset=308280
        local.get $l3
        i32.load offset=308300
        local.get $l3
        i32.load offset=308276
        i32.const 3
        i32.shl
        i32.add
        f32.load offset=4
        f32.add
        f32.store offset=308280
        local.get $l3
        i32.load offset=308276
        i32.const 2
        i32.shl
        local.get $l6
        i32.add
        block $B2 (result i32)
          local.get $l3
          f32.load offset=308280
          f32.const 0x1.116p+17 (;=139968;)
          f32.mul
          local.tee $l7
          f32.const 0x1p+32 (;=4.29497e+09;)
          f32.lt
          local.get $l7
          f32.const 0x0p+0 (;=0;)
          f32.ge
          i32.and
          if $I3
            local.get $l7
            i32.trunc_f32_u
            br $B2
          end
          i32.const 0
        end
        i32.const 1
        i32.add
        i32.store
        local.get $l3
        local.get $l3
        i32.load offset=308276
        i32.const 1
        i32.add
        i32.store offset=308276
        br $L0
      end
    end
    i32.const 1568
    i32.const 0
    i32.store
    i32.const 1572
    i32.const 0
    i32.store
    i32.const 1564
    local.get $l3
    i32.load offset=308292
    i32.store
    local.get $l3
    i32.const 0
    i32.store offset=4
    loop $L4
      loop $L5
        local.get $l3
        local.get $l3
        i32.const 32
        i32.add
        i32.const 61440
        local.get $l3
        i32.load offset=4
        call $rng_gen_blk
        i32.store offset=24
        local.get $l3
        i32.load offset=24
        i32.const -1
        i32.eq
        br_if $L5
      end
      local.get $l3
      i32.load offset=24
      if $I6
        local.get $l3
        local.get $l3
        i32.const 245808
        i32.add
        i32.store offset=245804
        local.get $l3
        i32.const 0
        i32.store offset=20
        local.get $l3
        i32.const 0
        i32.store offset=16
        loop $L7
          local.get $l3
          i32.load offset=16
          local.get $l3
          i32.load offset=24
          i32.lt_s
          if $I8
            local.get $l3
            local.get $l3
            i32.const 32
            i32.add
            local.get $l3
            i32.load offset=16
            i32.const 2
            i32.shl
            i32.add
            i32.load
            i32.store offset=28
            local.get $l3
            i32.const 0
            i32.store offset=8
            local.get $l3
            i32.const 0
            i32.store offset=12
            loop $L9
              local.get $l3
              i32.load offset=12
              local.get $l3
              i32.load offset=308296
              i32.lt_s
              if $I10
                local.get $l3
                i32.load offset=12
                i32.const 2
                i32.shl
                local.get $l6
                i32.add
                i32.load
                local.get $l3
                i32.load offset=28
                i32.le_u
                if $I11
                  local.get $l3
                  local.get $l3
                  i32.load offset=8
                  i32.const 1
                  i32.add
                  i32.store offset=8
                end
                local.get $l3
                local.get $l3
                i32.load offset=12
                i32.const 1
                i32.add
                i32.store offset=12
                br $L9
              end
            end
            local.get $l3
            i32.load offset=308300
            local.get $l3
            i32.load offset=8
            i32.const 3
            i32.shl
            i32.add
            i32.load8_u
            local.set $l8
            local.get $l3
            local.get $l3
            i32.load offset=245804
            local.tee $l9
            i32.const 1
            i32.add
            i32.store offset=245804
            local.get $l9
            local.get $l8
            i32.store8
            local.get $l3
            local.get $l3
            i32.load offset=20
            i32.const 1
            i32.add
            local.tee $l10
            i32.store offset=20
            local.get $l10
            i32.const 60
            i32.ge_s
            if $I12
              local.get $l3
              i32.const 0
              i32.store offset=20
              local.get $l3
              local.get $l3
              i32.load offset=245804
              local.tee $l11
              i32.const 1
              i32.add
              i32.store offset=245804
              local.get $l11
              i32.const 10
              i32.store8
            end
            local.get $l3
            local.get $l3
            i32.load offset=16
            i32.const 1
            i32.add
            i32.store offset=16
            br $L7
          end
        end
        local.get $l3
        i32.load offset=20
        if $I13
          local.get $l3
          local.get $l3
          i32.load offset=245804
          local.tee $l12
          i32.const 1
          i32.add
          i32.store offset=245804
          local.get $l12
          i32.const 10
          i32.store8
        end
        loop $L14
          local.get $l3
          local.get $l3
          i32.const 245808
          i32.add
          local.get $l3
          i32.load offset=245804
          local.get $l3
          i32.const 245808
          i32.add
          i32.sub
          local.get $l3
          i32.load offset=4
          call $out_write
          i32.store offset=24
          local.get $l3
          i32.load offset=24
          i32.const -1
          i32.eq
          br_if $L14
        end
        local.get $l3
        i32.load offset=24
        i32.eqz
        if $I15
          i32.const 1
          call $exit
          unreachable
        end
        br $L4
      end
    end
    local.get $l3
    i32.load offset=308288
    local.set $l4
    local.get $l3
    i32.const 308304
    i32.add
    global.set $__stack_pointer)
  (func $rng_gen_blk (type $t0) (param $p0 i32) (param $p1 i32) (param $p2 i32) (result i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32) (local $l6 i32) (local $l7 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l3
    local.get $p0
    i32.store offset=12
    local.get $l3
    local.get $p1
    i32.store offset=8
    local.get $l3
    local.get $p2
    i32.store offset=4
    local.get $l3
    i32.const -1
    i32.store
    i32.const 1568
    i32.load
    local.get $l3
    i32.load offset=4
    i32.eq
    if $I0
      i32.const 1568
      i32.const 1568
      i32.load
      i32.const 1
      i32.add
      local.tee $l4
      i32.store
      local.get $l4
      i32.const 1556
      i32.load
      i32.ge_s
      if $I1
        i32.const 1568
        i32.const 0
        i32.store
      end
      local.get $l3
      block $B2 (result i32)
        local.get $l3
        i32.load offset=8
        i32.const 1564
        i32.load
        i32.lt_s
        if $I3
          local.get $l3
          i32.load offset=8
          br $B2
        end
        i32.const 1564
        i32.load
      end
      i32.store
      i32.const 1564
      i32.const 1564
      i32.load
      local.get $l3
      i32.load
      i32.sub
      i32.store
      local.get $l3
      local.get $l3
      i32.load
      i32.store offset=8
      loop $L4
        local.get $l3
        local.get $l3
        i32.load offset=8
        local.tee $l5
        i32.const 1
        i32.sub
        i32.store offset=8
        local.get $l5
        if $I5
          i32.const 1552
          i32.const 1552
          i32.load
          i32.const 3877
          i32.mul
          i32.const 29573
          i32.add
          i32.const 139968
          i32.rem_u
          i32.store
          i32.const 1552
          i32.load
          local.set $l6
          local.get $l3
          local.get $l3
          i32.load offset=12
          local.tee $l7
          i32.const 4
          i32.add
          i32.store offset=12
          local.get $l7
          local.get $l6
          i32.store
          br $L4
        end
      end
    end
    local.get $l3
    i32.load)
  (func $out_write (type $t0) (param $p0 i32) (param $p1 i32) (param $p2 i32) (result i32)
    (local $l3 i32) (local $l4 i32) (local $l5 i32)
    global.get $__stack_pointer
    i32.const 16
    i32.sub
    local.tee $l3
    global.set $__stack_pointer
    local.get $l3
    local.get $p0
    i32.store offset=12
    local.get $l3
    local.get $p1
    i32.store offset=8
    local.get $l3
    local.get $p2
    i32.store offset=4
    local.get $l3
    i32.const -1
    i32.store
    i32.const 1572
    i32.load
    local.get $l3
    i32.load offset=4
    i32.eq
    if $I0
      i32.const 1572
      i32.const 1572
      i32.load
      i32.const 1
      i32.add
      local.tee $l4
      i32.store
      local.get $l4
      i32.const 1560
      i32.load
      i32.ge_s
      if $I1
        i32.const 1572
        i32.const 0
        i32.store
      end
      local.get $l3
      local.get $l3
      i32.load offset=12
      local.get $l3
      i32.load offset=8
      i32.const 1
      i32.const 0
      i32.load
      call $fwrite
      i32.store
    end
    local.get $l3
    i32.load
    local.set $l5
    local.get $l3
    i32.const 16
    i32.add
    global.set $__stack_pointer
    local.get $l5)
  (memory $memory 2)
  (global $__stack_pointer (mut i32) (i32.const 67120))
  (global $rng_tid i32 (i32.const 1568))
  (global $out_tid i32 (i32.const 1572))
  (global $rng_cnt i32 (i32.const 1564))
  (global $rng_tnum i32 (i32.const 1556))
  (global $seed i32 (i32.const 1552))
  (global $out_tnum i32 (i32.const 1560))
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
  (export "_start" (func $_start))
  (export "rng_tid" (global $rng_tid))
  (export "out_tid" (global $out_tid))
  (export "rng_cnt" (global $rng_cnt))
  (export "rng_tnum" (global $rng_tnum))
  (export "seed" (global $seed))
  (export "out_tnum" (global $out_tnum))
  (export "__dso_handle" (global $__dso_handle))
  (export "__data_end" (global $__data_end))
  (export "__stack_low" (global $__stack_low))
  (export "__stack_high" (global $__stack_high))
  (export "__global_base" (global $__global_base))
  (export "__heap_base" (global $__heap_base))
  (export "__heap_end" (global $__heap_end))
  (export "__memory_base" (global $__memory_base))
  (export "__table_base" (global $__table_base))
  (data $.rodata (i32.const 1024) ">THREE Homo sapiens frequency\0a\00>ONE Homo sapiens alu\0a\00>TWO IUB ambiguity codes\0a\00GGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGGGAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGACCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAATACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCAGCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGGAGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCCAGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAA\00a\00\00\00q=\8a>c\00\00\00\8f\c2\f5=g\00\00\00\8f\c2\f5=t\00\00\00q=\8a>B\00\00\00\0a\d7\a3<D\00\00\00\0a\d7\a3<H\00\00\00\0a\d7\a3<K\00\00\00\0a\d7\a3<M\00\00\00\0a\d7\a3<N\00\00\00\0a\d7\a3<R\00\00\00\0a\d7\a3<S\00\00\00\0a\d7\a3<V\00\00\00\0a\d7\a3<W\00\00\00\0a\d7\a3<Y\00\00\00\0a\d7\a3<\00\00\00\00\00\00\00\00a\00\00\00\e9\1c\9b>c\00\00\00r\bdJ>g\00\00\00\d7IJ>t\00\00\00r_\9a>")
  (data $.data (i32.const 1552) "*\00\00\00\01\00\00\00\01"))
